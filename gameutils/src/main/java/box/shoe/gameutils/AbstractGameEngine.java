package box.shoe.gameutils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.IntDef;
import android.util.Log;
import android.view.Choreographer;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import box.gift.gameutils.R;

/**
 * Created by Joseph on 10/23/2017.
 *
 * The Game Engine uses two threads. One for game updates, and one to paint frames.
 * Game updates are run at a frequency determined by the supplied UPS given in the constructor.
 * Frames are painted at each new VSYNC (Screen refresh), supplied by internal Choreographer.
 * Because these are not aligned, the engine interpolates between two game states for frame painting based on time,
 * and gives the interpolated game state to the Screen supplied to the constructor.
 */
public abstract class AbstractGameEngine
{ //TODO: remove isActive()/isPlaying() and replace with a single state variable.
    // Define the possible UPS options, which are factors of 1000 (so we get an even number of MS per update).
    // This is not a hard requirement, and the annotation may be suppressed,
    // at the risk of possible jittery frame display.
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({1, 2, 4, 5, 8, 10, 20, 25, 40, 50, 100, 125, 200, 250, 500, 1000})
    public @interface UPS_Options {}

    // Number of Updates Per Second that we would like to receive.
    // There are timing accuracy limitations,
    // and it is possible for the updates to take too long
    // for this to be possible (lag), hence 'target.'
    private final int targetUPS;

    // Based on the targetUPS we can define how long we expect each update to take.
    private final long expectedUpdateTimeMS;
    private final long expectedUpdateTimeNS;

    // The screen which will display a representation of the state of the game.
    private final Screen gameScreen;

    private Context appContext;

    // Control - try to mitigate use of volatile variables when possible.
    // (issue is not the number of volatile variables, rather how often they
    // are read causing memory to be flushed by the thread that set it).
    private volatile boolean started = false;
    private volatile boolean stopped = false;
    private volatile boolean stopThreads = false;
    private volatile boolean paused = false;
    private volatile boolean pauseThreads = false;
    private volatile int state = AbstractGameEngine.INACTIVE;

    // Threads
    private Thread updateThread;
    private Looper updateThreadLooper;
    private Thread frameThread;
    private Looper frameThreadLooper;
    private final int NUMBER_OF_THREADS = 2;

    // Concurrent - for simplicity, define as few as possible.
    private final Object monitorUpdateFrame = new Object();
    private final Object monitorControl = new Object();
    private CountDownLatch pauseLatch; // Makes sure all necessary threads pause before returning pauseGame.
    private CountDownLatch stopLatch; // Makes sure all necessary threads stop before returning stopGame.

    // Objs - remember to cleanup those that can be!
    private Choreographer vsync;
    private List<GameState> gameStates; // We want to use it like a queue, but we need to access the first two elements, so it cannot be one.
    private GameState lastVisualizedGameState = null;

    // Const
    public static final int INACTIVE = 0;
    public static final int PLAYING = 1;
    public static final int PAUSED = 2;

    // Etc
    protected boolean screenTouched = false;
    private long gamePausedTimeStamp;

    // Fixed display mode - display will attempt to paint
    // pairs of updates for a fixed amount of time (expectedUpdateDelayNS)
    // regardless of the amount of time that passed between
    // the generation of the two updates. When an update
    // happens too quickly or slowly, this will cut short
    // or artificially lengthen the painting of a pair of updates
    // because the next update comes too early or late.
    // Pro: looks better when an occasional update comes too early or too late.
    private static final int DIS_MODE_FIX_UPDATE_DISPLAY_DURATION = 0;

    // Varied display mode - display will lengthen or shorten
    // the amount of time it takes to display a pair of updates.
    // When an update happens too quickly or slowly, this will
    // cause quite a jitter.
    // Pro: When many updates in a row come to early or late, instead
    // of jittering the display will simply speed up or slow down
    // to keep pace with the updates, which looks very nice.
    private static final int DIS_MODE_VAR_UPDATE_DISPLAY_DURATION = 1;

    // Which display mode the engine is currently using.
    private int displayMode = DIS_MODE_FIX_UPDATE_DISPLAY_DURATION;

    // Choreographer tells you a fake timeStamp for beginning of vsync. It really occurs at (frameTimeNanos - vsyncOffsetNanos).
    // This is not a huge deal, but if we can correct for it, why not?
    // Default it to 0 if our API level is not high enough to get the real value.
    private long vsyncOffsetNanos = 0;

    public AbstractGameEngine(Context appContext, @UPS_Options int targetUPS, Screen screen) //target ups should divide evenly into 1000000000, updates are accurately caleld to within about 10ms
    {
        this.appContext = appContext;

        Display display = ((WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        //this.refreshRate = display.getRefreshRate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            vsyncOffsetNanos = display.getAppVsyncOffsetNanos();
        }

        this.targetUPS = targetUPS;
        this.expectedUpdateTimeMS = 1000 / this.targetUPS;
        this.expectedUpdateTimeNS = this.expectedUpdateTimeMS * 1000000;

        gameStates = new LinkedList<>();

        // Setup the 'Updates' thread.
        updateThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
                Looper.prepare();
                updateThreadLooper = Looper.myLooper();
                runUpdates();

            }
        }, appContext.getString(R.string.update_thread_name));

        // Setup the 'Frames' thread.
        frameThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
                Looper.prepare();
                frameThreadLooper = Looper.myLooper();
                runFrames();
            }
        }, appContext.getString(R.string.frame_thread_name));

        gameScreen = screen;
        gameScreen.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                // Humor the Android system.
                v.performClick();
                // Only use touch event if not paused
                if (isPlaying())
                {
                    onTouchEvent(event);
                    return true;
                }
                return false;
            }
        });
    }

    public void startGame()
    {
        if (getGameWidth() == 0 || getGameHeight() == 0)
        {
            throw new IllegalStateException("Cannot start the game before the screen has been given dimensions!");
        }
        launch();
    }

    private void launch()
    {
        if (started)
        {
            // May not start a game that is already started.
            throw new IllegalStateException("Game already started!");
        }
        else
        {
            started = true;

            // At this point, the surfaceView (and thus the game) has dimensions, so we can do initialization based on them.
            initialize();
            gameScreen.initialize();

            // We will launch two threads.
            // 1) Do game logic (game updates)
            // 2) Alert surface view (paint frames)
            updateThread.start();
            frameThread.start();
        }
    }

    /**
     * Called once before updates and frames begin.
     * In this call, we are guaranteed that the surfaceView (and thus the game) has dimensions,
     * so do any initialization that involves getGameWidth/getGameHeight.
     */
    protected abstract void initialize(); //TODO: if at all possible, find a way to make abstract methods only callable by subclass.... ('protected' gives package access, why....) (priority=low)

    private void runUpdates()
    {
        // Make sure that we are on updateThread.
        if (!Thread.currentThread().getName().equals(appContext.getString(R.string.update_thread_name)))
        {
            throw new IllegalThreadStateException("Can only be called from updateThread!");
        }

        final Handler updateHandler = new Handler();

        Runnable updateCallback = new Runnable()
        {
            private boolean updateThreadPaused;
            private long startTime = 0;

            @Override
            public void run()
            {
                // Keep track of what time it is now. Goes first to get most accurate timing.
                startTime = System.nanoTime();

                // Schedule next update. Goes second to get as accurate as possible updates.
                // We do it at the start to make sure we are waiting a precise amount of time
                // (as precise as we can get with postDelayed). This means we manually remove
                // the callback if the game stops.
                //updateHandler.removeCallbacksAndMessages(null);
                updateHandler.postDelayed(this, expectedUpdateTimeMS);

                // Acquire the monitor lock, because we cannot update the game at the same time we are trying to draw it.
                synchronized (monitorUpdateFrame)
                {
                    // Do a game update.
                    update();
                    screenTouched = false;

                    // Make new game state with time stamp of the start of this update round.
                    GameState gameState = new GameState();
                    gameState.setTimeStamp(startTime);
                    gameStates.add(gameState);

                    // Save items to this game state for painting.
                    saveGameState(gameState);

                    // Execute interpolation service for all Entities and bind to this game state.
                    saveInterpolationFields(gameState);

                    // Pause game (postDelayed runnable should not run while this thread is waiting, so no issues there)
                    while (pauseThreads)
                    {
                        try
                        {
                            if (!updateThreadPaused)
                            {
                                pauseLatch.countDown();
                            }
                            updateThreadPaused = true;
                            monitorUpdateFrame.wait();
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    updateThreadPaused = false;

                    // Stop thread.
                    if (stopThreads)
                    {
                        updateHandler.removeCallbacksAndMessages(null);
                        stopLatch.countDown();
                        return;
                    }
                    //TODO: if updates are consistently taking too long (or even too short! [impossible?]) we can switch visualization modes.
                }
            }
        };

        updateHandler.post(updateCallback);
        Looper.loop();
    }

    private void saveInterpolationFields(GameState gameState)
    {
        LinkedList<Entity> entities = Entity.ENTITIES;
        for (Entity entity : entities)
        {
            // Generate new InterpolatablesCarrier.
            InterpolatablesCarrier newInterpolatablesCarrier = new InterpolatablesCarrier();
            entity.provideInterpolatables(newInterpolatablesCarrier);

            // Save to gameState for this Entity.
            gameState.interps.put(entity, newInterpolatablesCarrier);
        }
    }

    private void runFrames()
    {
        // Make sure that we are on frameThread.
        if (!Thread.currentThread().getName().equals(appContext.getString(R.string.frame_thread_name)))
        {
            throw new IllegalThreadStateException("Can only be called from frameThread!");
        }

        vsync = Choreographer.getInstance();

        Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback()
        {
            private boolean frameThreadPaused;

            @Override
            public void doFrame(long frameTimeNanos)
            {
                // Correct for minor difference in vsync time.
                // This is probably totally unnecessary. (And will only change frameTimeNanos in a sufficiently high API anyway)
                frameTimeNanos -= vsyncOffsetNanos;
                synchronized (monitorUpdateFrame)
                {
                    // Pause game.
                    // Spin lock when we want to pause.
                    while (pauseThreads)
                    {
                        try
                        {
                            // Do not count down the latch off spurious wakeup!
                            if (!frameThreadPaused)
                            {
                                if (gameScreen.hasPreparedPaint())
                                {
                                    if (lastVisualizedGameState != null)
                                    {
                                        gameScreen.paintFrame(lastVisualizedGameState);
                                    }
                                    else
                                    {
                                        // Unlock the canvas without posting anything.
                                        gameScreen.unpreparePaint();
                                    }
                                }
                                pauseLatch.countDown();
                            }
                            frameThreadPaused = true;
                            monitorUpdateFrame.wait();
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    frameThreadPaused = false;

                    // Stop game if prompted
                    if (stopThreads)
                    {
                        stopLatch.countDown();
                        return;
                    }

                    // Must ask for new callback each frame!
                    // We ask at the start because the Choreographer automatically
                    // skips frames for us if we don't draw fast enough,
                    // and it will make a Log.i to let us know that it skipped frames (so we know)
                    // If we move it to the end we essentially manually skip frames,
                    // but we won't know that an issue occurred.
                    vsync.postFrameCallback(this);

                    // Paint frame
                    if (gameScreen.hasPreparedPaint())
                    {
                        GameState oldState;
                        GameState newState;

                        while (true)
                        {
                            if (gameStates.size() >= 2) // We need two states to draw (saveInterpolationFields between them)
                            {
                                // Get the first two saved states
                                oldState = gameStates.get(0);
                                newState = gameStates.get(1);

                                // Interpolate based on time that has past since the second active game state
                                // as a fraction of the time between the two active states.
                                double interpolationRatio;

                                // TODO: auto switch paint modes in response to update lag. priority=low
                                if (displayMode == DIS_MODE_FIX_UPDATE_DISPLAY_DURATION)
                                {
                                    interpolationRatio = (frameTimeNanos - newState.getTimeStamp()) / ((double) expectedUpdateTimeNS);
                                    if (interpolationRatio < 0)
                                    {
                                        Log.i("AbstractGameEngine", "interpolation ratio < 0: " + interpolationRatio);
                                    }
                                }
                                else if (displayMode == DIS_MODE_VAR_UPDATE_DISPLAY_DURATION)
                                {
                                    // Time that passed between the game states in question.
                                    long timeBetween = newState.getTimeStamp() - oldState.getTimeStamp();
                                    interpolationRatio = (frameTimeNanos - newState.getTimeStamp()) / ((double) timeBetween);
                                    if (interpolationRatio < 0)
                                    {
                                        Log.i("AbstractGameEngine", "interpolation ratio < 0: " + interpolationRatio);
                                    }
                                }
                                else
                                {
                                    throw new IllegalStateException("Engine is in an invalid displayMode.");
                                }

                                // If we are up to the new update, remove the old one as it is not needed.
                                if (interpolationRatio >= 1)
                                {
                                    // Remove the old update.
                                    if (gameStates.size() >= 1)
                                    {
                                        //gameStates.get(0).cleanup(); //TODO: find way to clean up without causing error when lastVisualizedGameState is used
                                        gameStates.remove(0);
                                    }
                                    continue;
                                }
                                else
                                {
                                    for (Entity entity : Entity.ENTITIES)
                                    {
                                        InterpolatablesCarrier oldInterpolatablesCarrier = oldState.interps.get(entity);
                                        InterpolatablesCarrier newInterpolatablesCarrier = newState.interps.get(entity);

                                        if (oldInterpolatablesCarrier != null && newInterpolatablesCarrier != null)
                                        {
                                            try
                                            {
                                                InterpolatablesCarrier interp = oldInterpolatablesCarrier.interpolateTo(newInterpolatablesCarrier, interpolationRatio);
                                                entity.recallInterpolatables(interp);
                                                if (!interp.isEmpty())
                                                { //TODO: can this error happen? aren't non equal length in and out incompatible error'd?
                                                    throw new IllegalStateException(entity + " not all interpolatables were recalled!");
                                                }
                                            }
                                            catch (IllegalStateException e)
                                            {
                                                throw new IllegalStateException("Noncompatible interpolations.");
                                            }
                                        }
                                    }

                                    gameScreen.paintFrame(newState);
                                    lastVisualizedGameState = newState;
                                    break;
                                }
                            }
                            else
                            {
                                Log.i("Frames", "We want to draw but there aren't enough new updates!");
                                if (lastVisualizedGameState != null)
                                {
                                    gameScreen.paintFrame(lastVisualizedGameState);
                                    Log.i("Frames", " Using previous valid state.");
                                }
                                else
                                {
                                    gameScreen.unpreparePaint();
                                }
                                break;
                            }
                        }
                    }
                    else
                    {
                        Log.i("Frames", "Skipped painting frame because unprepared");
                    }
                }

                // Prepare for next frame now, when we have all the time in the world.
                // TODO: only do this if we actually have extra time, do not miss next frame callback!
                if (gameScreen.hasInitialized() && !gameScreen.hasPreparedPaint())
                {
                    // Only prepare if we think we will have something to paint.
                    // It is ok to prepare if there are not enough game states, because maybe by next
                    // callback there will be. But if there is no fallback in case no state is
                    // available, we should be safe and not prepare.
                    if (gameStates.size() > 1 || lastVisualizedGameState != null)
                    {
                        gameScreen.preparePaint();
                    }
                    else
                    {
                        Log.i("AbstractGameEngine", "Skip preparing because we may not be able to paint next frame.");
                    }
                }
            }
        };
        //Looper.myLooper().setMessageLogging(new LogPrinter(Log.DEBUG, "Looper"));
        vsync.postFrameCallback(frameCallback);
        Looper.loop();
    }


    /**
     * Stop update and frame threads.
     * Stop update and frame threads.
     * After the threads finish their current loop execution,
     * The game will then call the subclasses cleanup.
     */
    public void stopGame()
    {
        synchronized (monitorControl)
        {
            if (Thread.currentThread().getName().equals(appContext.getString(R.string.update_thread_name)))
            {
                throw new IllegalThreadStateException("Cannot be called from "
                        + appContext.getString(R.string.update_thread_name)
                        + " thread, because it cannot stop itself!");
            }
            else if (Thread.currentThread().getName().equals(appContext.getString(R.string.frame_thread_name)))
            {
                throw new IllegalThreadStateException("Cannot be called from "
                        + appContext.getString(R.string.frame_thread_name)
                        + " thread, because it cannot stop itself!");
            }

            if (stopThreads)
            {
                throw new IllegalStateException("Engine already in process of stopping!");
            }

            if (!isActive())
            {
                throw new IllegalStateException("Cannot stop if game is not active!");
            }

            // Check if we are paused.
            if (isPlaying())
            {
                pauseGame();
            }

            stopLatch = new CountDownLatch(NUMBER_OF_THREADS);
            stopThreads = true;

            resumeGame();

            try
            {
                stopLatch.await();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            // Make sure the threads have actually returned from their callbacks
            // before stopping the Loopers. This ensures that the Handlers can gracefully
            // finish processing their messages before we wrench the Loopers from their cold,
            // dead hands. Therefore, wait until we can grab the lock.
            synchronized (monitorUpdateFrame)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                {
                    updateThreadLooper.quitSafely();
                    frameThreadLooper.quitSafely();
                }
                else
                {
                    updateThreadLooper.quit();
                    frameThreadLooper.quit();
                }
            }

            stopped = true;
            stopThreads = false;
        }
    }

    /**
     * Pause the threads
     */
    public void pauseGame()
    {
        synchronized (monitorControl)
        {
            if (Thread.currentThread().getName().equals(appContext.getString(R.string.update_thread_name)))
            {
                throw new IllegalThreadStateException("Cannot be called from "
                        + appContext.getString(R.string.update_thread_name)
                        + " thread, because it cannot stop itself!");
            }
            else if (Thread.currentThread().getName().equals(appContext.getString(R.string.frame_thread_name)))
            {
                throw new IllegalThreadStateException("Cannot be called from "
                        + appContext.getString(R.string.frame_thread_name)
                        + " thread, because it cannot stop itself!");
            }

            if (pauseThreads)
            {
                throw new IllegalStateException("Engine already in process of pausing!");
            }

            if (!isActive())
            {
                throw new IllegalStateException("Cannot pause game that isn't running!");
            }

            pauseLatch = new CountDownLatch(NUMBER_OF_THREADS);

            // Tell threads to pause
            pauseThreads = true;

            try
            {
                pauseLatch.await();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            gamePausedTimeStamp = System.nanoTime();
            paused = true;
        }
    }

    public void resumeGame()
    {
        if (Thread.currentThread().getName().equals(appContext.getString(R.string.update_thread_name)))
        {
            throw new IllegalThreadStateException("Cannot be called from "
                    + appContext.getString(R.string.update_thread_name)
                    + " thread, because it cannot resume itself!");
        }
        else if (Thread.currentThread().getName().equals(appContext.getString(R.string.frame_thread_name)))
        {
            throw new IllegalThreadStateException("Cannot be called from "
                    + appContext.getString(R.string.frame_thread_name)
                    + " thread, because it cannot resume itself!");
        }
        if (!isActive())
        {
            throw new IllegalStateException("Cannot resume game that isn't active.");
        }
        if (isPlaying())
        {
            throw new IllegalStateException("Cannot resume game that isn't paused.");
        }

        // When we resume, time has passed, so push game states ahead
        // because they are not invalid yet. (Visual fix).
        long currentTimeStamp = System.nanoTime();
        long sincePause = currentTimeStamp - gamePausedTimeStamp;
        for (GameState gameState : gameStates)
        {
            gameState.setTimeStamp(gameState.getTimeStamp() + sincePause);// + 1000000);
        }

        pauseThreads = false;
        paused = false;
        synchronized (monitorUpdateFrame)
        {
            monitorUpdateFrame.notifyAll();
        }
    }

    //TODO: set states and then make use of this function
    public boolean isInState(int... states)
    {
        for (int state : states)
        {
            if (this.state == state)
            {
                return true;
            }
        }
        return false;
    }

    public boolean isActive()
    {
        return started && !stopped;
    }

    public boolean isPlaying()
    {
        return isActive() && !paused;
    }

    protected abstract void update();

    protected abstract void saveGameState(GameState gameState);

    public int getGameWidth()
    {
        return gameScreen.getWidth();
    }
    public int getGameHeight()
    {
        return gameScreen.getHeight();
    }

    protected void onTouchEvent(MotionEvent event)
    {
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN)
        {
            screenTouched = true;
        }
        else if (action == MotionEvent.ACTION_CANCEL)
        {
            screenTouched = false;
        }
    }

    public abstract int getResult(); //TODO: remove eventually when we separate game engines from score game engines
}