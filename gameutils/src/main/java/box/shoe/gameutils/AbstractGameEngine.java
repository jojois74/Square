package box.shoe.gameutils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.annotation.IntDef;
import android.util.Log;
import android.util.Pair;
import android.view.Choreographer;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import box.gift.gameutils.R;

/**
 * Created by Joseph on 10/23/2017.
 *
 * The Game Engine uses two threads. One for game updates, and one to paint frames.
 * Game updates are run at a frequency determined by the supplied UPS given in the constructor.
 * Frames are painted at each new VSYNC (Screen refresh), supplied by internal Choreographer.
 * Because these are not aligned, the engine interpolates between two game states for frame painting based on time,
 * and gives the interpolated game state to the AbstractGameSurfaceView supplied to the constructor.
 */
public abstract class AbstractGameEngine extends AbstractEventDispatcher implements Cleanable
{
    // Define the possible UPS options, which are factors of 1000 (so we get an even number of MS per update).
    // This is not a hard requirement, and the annotation may be suppressed,
    // at the risk of possible jittery frame display.
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({1, 2, 4, 5, 8, 10, 20, 25, 40, 50, 100, 125, 200, 250, 500, 1000})
    public @interface UPS_Options {} //Bug - '@interface' should be syntax highlighted annotation color (yellow), not keyword color (blue). ( Android Studio :D )

    // Number of Updates Per Second that we would like to receive.
    // There are timing accuracy limitations,
    // and it is possible for the updates to take too long
    // for this to be possible (lag), hence 'target.'
    private final int targetUPS;

    // Based on the targetUPS we can define how long we expect each update to take.
    private final long expectedUpdateTimeMS;
    private final long expectedUpdateTimeNS;

    // The screen which will display a representation of the state of the game.
    private final AbstractGameSurfaceView gameScreen;

    private Context appContext;

    // Control - try to mitigate use of volatile variables when possible.
    // (issue is not the number of volatile variables, rather how often they
    // are read causing memory to be flushed by the thread that set it).
    private volatile boolean started = false;
    private volatile boolean stopped = false;
    private volatile boolean stopThreads = false;
    private volatile boolean paused = false;
    private volatile boolean pauseThreads = false;
    private volatile boolean oneFrameThenPause = false;
    private volatile int numberOfThreadsThatNeedToPause = 0;

    // Threads
    private Thread updateThread;
    private Thread frameThread;

    // Concurrent - for simplicity, define as few as possible.
    private final Object updateAndFrameAndInputMonitor = new Object();
    private CountDownLatch pauseLatch; // Makes sure all necessary threads pause before returning pauseGame.

    // Objs - remember to cleanup those that can be!
    private Choreographer vsync;
    protected Rand random;
    protected Vibrator rumble;
    private List<GameState> gameStates; // We want to use it like a queue, but we need to access the first two elements, so it cannot be one.
    private GameState lastVisualizedGameState;

    // Etc
    public volatile int score;

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

    public AbstractGameEngine(Context appContext, @UPS_Options int targetUPS, AbstractGameSurfaceView screen) //target ups should divide evenly into 1000000000, updates are accurately caleld to within about 10ms
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
        random = new Rand();
        rumble = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);

        //Setup the 'Updates' thread.
        updateThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                numberOfThreadsThatNeedToPause++;
                runUpdates();
            }
        }, appContext.getString(R.string.update_thread_name));

        //Setup the 'Frames' thread.
        frameThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                numberOfThreadsThatNeedToPause++;
                runFrames();
            }
        }, appContext.getString(R.string.frame_thread_name));

        gameScreen = screen;
        gameScreen.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                v.performClick();
                // Only use touch event if not paused TODO: maybe change this behavior? the idea is that pause menu handled by ui thread, bad idea?
                if (isPlaying())
                {
                    synchronized (updateAndFrameAndInputMonitor)
                    {
                        onTouchEvent(event);
                        return true;
                    }
                } //TODO: join() this thread in stop, and for pause make this part of countdown latch, and wait this thread to make sure that it is done before pause happens? likely not necessary... simply have the child not do stuff with inputs after it ends the game, and the occasional input after a puase makes no diff
                // TODO: and its not even a thread, usually just hijacks off main...
                // TODO: so make sure its only called from main thread? possibly needed?
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
    protected abstract void initialize(); //TODO: if at all possible, find a way to make abstract methods only callable by subclass.... ('protected' gives package access, why....)

    private void runUpdates()
    {
        // Make sure that we are on updateThread.
        if (!Thread.currentThread().getName().equals(appContext.getString(R.string.update_thread_name)))
        {
            throw new IllegalThreadStateException("Can only be called from updateThread!");
        }

        Looper.prepare();
        final Handler updateHandler = new Handler();

        Runnable updateCallback = new Runnable()
        {
            private boolean updateThreadPaused;

            @Override
            public void run()
            {

                L.d("runUpdatesCallback", "rewrite");
                // Keep track of what time it is now. Goes first to get most accurate timing.
                long startTime = System.nanoTime();

                // Schedule next update. Goes second to get as accurate as possible updates.
                // We do it at the start to make sure we are waiting a precise amount of time
                // (as precise as we can get with postDelayed). This means we manually remove
                // the callback if the game stops.
                //updateHandler.removeCallbacksAndMessages(null);
                updateHandler.postDelayed(this, expectedUpdateTimeMS); //TODO: inject stutter updates to test various drawing schemes

                // Acquire the monitor lock, because we cannot update the game at the same time we are trying to draw it.
                synchronized (updateAndFrameAndInputMonitor)
                {
                    L.d("Update V", "thread");

                    // Do a game update.
                    update();

                    // Save game state for painting.
                    GameState gameState = new GameState();
                    saveGameState(gameState);
                    gameState.setTimeStamp(startTime);
                    gameStates.add(gameState);

                    // Execute interpolation service for all Entities.
                    saveInterpolationFields();

                    // Pause game (postDelayed runnable should not run while this thread is waiting, so no issues there)
                    while (pauseThreads)
                    {
                        try
                        {
                            if (!updateThreadPaused)
                            {
                                L.d("pausedUpdates", "pause");
                                pauseLatch.countDown();
                            }
                            updateThreadPaused = true;
                            updateAndFrameAndInputMonitor.wait();
                            L.d("WOKEN_U", "WOKEN");
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
                        return;
                    }

                    L.d("Update ^", "thread");
                    //TODO: if updates are consistently taking too long (or even too short!) we can switch visualization modes.
                }
            }
        };

        updateHandler.post(updateCallback);
        Looper.loop();
    }

    private void saveInterpolationFields()
    {
        LinkedList<Entity> entities = Entity.ENTITIES;
        for (Entity entity : entities)
        {
            // Generate new Interpolatables.
            Interpolatables newInterpolatables = new Interpolatables();
            entity.provideInterpolatables(newInterpolatables);

            // Shift and save.
            entity.oldInterpolatables = entity.newInterpolatables;
            entity.newInterpolatables = newInterpolatables;
        }
    }

    private void runFrames()
    {
        // Make sure that we are on frameThread.
        if (!Thread.currentThread().getName().equals(appContext.getString(R.string.frame_thread_name)))
        {
            throw new IllegalThreadStateException("Can only be called from frameThread!");
        }

        Looper.prepare();
        vsync = Choreographer.getInstance();

        Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback()
        {
            private boolean frameThreadPaused;

            @Override
            public void doFrame(long frameTimeNanos)
            {
                // Must ask for new callback each frame!
                // We ask at the start because the Choreographer automatically
                // skips frames for us if we don't draw fast enough,
                // and it will make a Log.i to let us know that it skipped frames (so we know)
                // If we move it to the end we essentially manually skip frames,
                // but we won't know that an issue occurred.
                vsync.postFrameCallback(this);

                // Correct for minor difference in vsync time.
                // This is probably totally unnecessary. (And will only change frameTimeNanos in a sufficiently high API anyway)
                frameTimeNanos -= vsyncOffsetNanos;
                synchronized (updateAndFrameAndInputMonitor)
                {
                    L.d("Frame V", "thread");

                    // Pause game
                    // Spin lock when we want to pause
                    while (pauseThreads && !oneFrameThenPause)
                    {
                        try
                        {
                            // Do not count down the latch off spurious wakeup!
                            if (!frameThreadPaused)
                            {
                                pauseLatch.countDown();
                                L.d("pausedFrames", "pause");
                            }
                            frameThreadPaused = true;
                            updateAndFrameAndInputMonitor.wait();
                            L.d("WOKEN_F", "WOKEN");
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    frameThreadPaused = false;
                    L.d("between pause and stop", "rewrite2");
                    // Stop game if prompted
                    if (stopThreads)
                    {
                        // Since we asked for the callback up above,
                        // we should remove it if we plan on quiting
                        // so we do not do an extra callback.
                        L.d("about to remove callback", "rewrite2");
                        vsync.removeFrameCallback(this);
                        L.d("removed callback", "rewrite2");
                        if (gameScreen.preparedToVisualize())
                        {
                            if (lastVisualizedGameState != null)
                            {
                                gameScreen.visualize(lastVisualizedGameState);
                            }
                            else
                            {
                                // Unlock the canvas without posting anything.
                                gameScreen.unlockCanvasAndClear();
                            }
                        }
                        return;
                    }

                    // Acquire the canvas lock now to save time later (this can be an expensive operation!) //TODO: this saves no time unless does async, or while waiting for synchronize lock. That may be a bad idea since 1) we may give up our synchronize cahnce and 2) we need to release the canvas prepare lock on stop or pause
                    if (gameScreen.canVisualize() && !gameScreen.preparedToVisualize())
                    {
                        gameScreen.prepareVisualize();
                    }

                    boolean paintedFrame = false;

                    // Paint frame
                    if (gameScreen.preparedToVisualize())
                    {
                        GameState oldState;
                        GameState newState;

                        while (true)
                        {
                            L.d("GameStates: " + gameStates.size(), "new");
                            if (gameStates.size() >= 2) // We need two states to draw (saveInterpolationFields between them)
                            {
                                // Get the first two saved states
                                oldState = gameStates.get(0);
                                newState = gameStates.get(1);

                                // Interpolate based on time that has past since the second active game state
                                // as a fraction of the time between the two active states.
                                //double interpolationRatio = (frameTimeNanos - newState.timeStamp) / ((double) timeBetween);
                                double interpolationRatio;

                                if (displayMode == DIS_MODE_FIX_UPDATE_DISPLAY_DURATION)
                                {
                                    interpolationRatio = (frameTimeNanos - newState.getTimeStamp()) / ((double) expectedUpdateTimeNS);
                                }
                                else if (displayMode == DIS_MODE_VAR_UPDATE_DISPLAY_DURATION)
                                {
                                    // Time that passed between the game states in question.
                                    long timeBetween = newState.getTimeStamp() - oldState.getTimeStamp();
                                    interpolationRatio = (frameTimeNanos - newState.getTimeStamp()) / ((double) timeBetween);
                                }
                                else
                                {
                                    throw new IllegalStateException("Engine is in an invalid displayMode.");
                                }

                                // If we are up to the new update, remove the old one as it is not needed.
                                if (interpolationRatio >= 1)
                                {
                                    L.d("Removing a state we have used up", "new");
                                    // Remove the old update.
                                    if (gameStates.size() >= 1)
                                    {
                                        //gameStates.get(0).cleanup(); //TODO: find way to clean up without causing error wen lastVisualizedGameState is used
                                        gameStates.remove(0);
                                    }
                                    continue;
                                }
                                else
                                {
                                    List<Entity> entities = Entity.ENTITIES;
                                    for (Entity entity : entities)
                                    {
                                        Interpolatables oldInterpolatables = entity.oldInterpolatables;
                                        Interpolatables newInterpolatables = entity.newInterpolatables;

                                        try
                                        {
                                            Interpolatables interp = oldInterpolatables.interpolateTo(newInterpolatables, interpolationRatio);
                                            entity.recallInterpolatables(interp);
                                        }
                                        catch (IllegalStateException e)
                                        {
                                            System.out.println("Noncompatible interpolations.");
                                        }
                                    }

                                    gameScreen.visualize(newState);
                                    paintedFrame = true;
                                    lastVisualizedGameState = newState;
                                    break;
                                }
                            }
                            else
                            {
                                Log.w("T", "We want to draw but there aren't enough new updates!");
                                if (lastVisualizedGameState != null)
                                {
                                    gameScreen.visualize(lastVisualizedGameState);
                                    paintedFrame = true;
                                    Log.w("T", " Using previous valid state.");
                                }
                                else
                                {
                                    gameScreen.unlockCanvasAndClear();
                                    paintedFrame = true; // This shoulc count for the purpose of paintOneFrame because there is never going to be another valid GameState anyway if there isn't already
                                }
                                break;
                            }
                        }
                    }

                    if (paintedFrame)
                    {
                        oneFrameThenPause = false;
                    }
                    L.d("Frame ^", "thread");
                    L.d("end of sync block", "rewrite2");
                }
                L.d("after sync block", "rewrite2");
            }
        };
        vsync.postFrameCallback(frameCallback);
        Looper.loop();
    }


    /**
     * Stop update and frame threads.
     * After the threads finish their current loop execution,
     * The game will then call the subclasses cleanup.
     */
    public void stopGame()
    {
        if (!Thread.currentThread().getName().equals("main"))
        {
            //Log.w("T", "Was not called from the main thread, instead from: " + Thread.currentThread().getName() + ". Will be run from the Main thread instead.");
            throw new IllegalThreadStateException("Must be called from main thread");
        }

        stopThreads = true; //TODO: synchronize this?

        // Check if we are paused.
        if (!isPlaying())
        {
            L.d("unpause in stop", "rewrite2");
            // We will need to unpause the game if we are currently paused in order to stop it.
            resumeGame();
        }

        try
        {
            updateThread.join();
            frameThread.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        stopped = true;

        /*
        Handler mainHandler = new Handler(appContext.getMainLooper());
        mainHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    updateThread.join();
                    frameThread.join();
                    L.d("Two threads stopped", "stop");
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    L.d("Finally running", "stop");
                    cleanup();
                    dispatchEvent(GameEventConstants.GAME_OVER);
                }
            }
        });*/
    }

    /**
     * Cleanup and garbage collect here, which will run before the superclass cleans.
     */
    @SuppressLint("MissingSuperCall") //Because this is the top level implementor
    public void cleanup()
    {
        Log.d("h", "CLEANUP");
        vsync = null;
        random = null;
        rumble = null;
        updateThread = null;
        frameThread = null;

        //Cleanup gameScreen TODO: more
        gameScreen.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                v.performClick();
                return false;
            }
        });
    }

    /**
     * Pause the threads
     */
    public void pauseGame() //TODO: should only be called from main thread?
    {
        /*if (Thread.currentThread().equals(updateThread) || Thread.currentThread().equals(frameThread))
        {
            pauseThreads = true;
            return;
        }*/
        if (!isActive())
        {
            Log.w("F", "No need to pause game that isn't running.");
            return;
        }

        L.d("pausing in abstract game engine", "pause");

        // 1 frame thread + 1 update thread = 2
        pauseLatch = new CountDownLatch(numberOfThreadsThatNeedToPause);

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

        paused = true;
        L.d("after pausing in abstract game engine", "pause");
    }

    public void resumeGame()
    {
        if (!isActive())
        {
            Log.w("F", "Cannot unpause game that isn't active.");
            return;
        }
        Log.d("G", "Unpausing game");

        pauseThreads = false;
        paused = false;
        synchronized (updateAndFrameAndInputMonitor)
        {
            updateAndFrameAndInputMonitor.notifyAll();
        }
    }

    /**
     * When the app resumes and the game is paused, the surface view will have cleared.
     * Use this function to paint a single new frame onto the view if you are not unpausing right away.
     */
    public void paintOneFrame()
    {
        L.d("paintOneFrame", "p");
        synchronized (updateAndFrameAndInputMonitor)
        {
            if (isPlaying())
            {
                throw new IllegalStateException("Game must be paused first!");
            }

            // Set the flag which allows the frame thread to escape from pause for one frame
            oneFrameThenPause = true;

            // Wakeup the frame thread.
            // We use notify all to make sure the frame thread gets woken.
            // The update thread will immediately wait() because the game is still paused.
            updateAndFrameAndInputMonitor.notifyAll();
        }
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

    public abstract void onTouchEvent(MotionEvent event);
}