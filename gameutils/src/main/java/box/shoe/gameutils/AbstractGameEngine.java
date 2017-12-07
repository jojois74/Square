package box.shoe.gameutils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.Choreographer;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Joseph on 10/23/2017.
 *
 * The game engine runs game update code on a new thread (how often it runs is supplied by the UPS parameter in the constructor)
 * And it runs visual update code on another new thread, painted to the supplied GameSurfaceView
 * It paints every new VSYNC (before a new frame wants to be drawn)
 * Therefore game updates may happen less frequently than the game is painted (and the two are not aligned)
 * So the engine attempts to make interpolations in order to draw.
 * This means that rendering will always be at most one frame behind user input (not a big deal!)
 * This is because we interpolate between the previous update and the latest update
 */

public abstract class AbstractGameEngine extends AbstractEventDispatcher
{
    private double refreshRate; //The times per second that the screen can redraw (hardware specific)
    private int targetUPS;
    private long expectedUpdateTimeNS;
    private long totalUpdateTimeNS;
    private volatile long lastUpdateTimeNS; //In System.nanoTime timebase
    private volatile long actualUpdateTimeNS;

    private final AbstractGameSurfaceView gameScreen;
    private volatile boolean started = false;
    private Context appContext;

    // Control
    private volatile boolean stopped = false;
    private volatile boolean updateThreadStopped = false;
    private volatile boolean frameThreadStopped = false;
    private volatile boolean paused = false;
    private volatile boolean frameThreadPaused = false;
    private volatile boolean updateThreadPaused = false;
    private volatile boolean oneFrameThenPause = false;

    // Threads
    private Thread gameUpdateThread;
    private Thread viewPaintThread;

    private volatile boolean wantToLaunch = false;
    private volatile boolean viewHasDimension = false;

    // Monitors
    private final Integer updateAndFrameAndInputMonitor = new Integer(1);
    private CountDownLatch pauseLatch;

    // Objs
    private Choreographer vsync;
    protected Rand random;
    protected Vibrator rumble;
    private List<Entity> entityList;

    // Etc
    public volatile int score;

    public AbstractGameEngine(Context appContext, int targetUPS, AbstractGameSurfaceView screen) //target ups should divide evenly into 1000000000, updates are accurately caleld to within about 10ms
    {
        this.appContext = appContext;

        Display display = ((WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        this.refreshRate = display.getRefreshRate();
        // TODO: for display fine grain, use getAppVsyncOffsetNanos() https://developer.android.com/reference/android/view/Display.html#getAppVsyncOffsetNanos()

        this.targetUPS = targetUPS;
        this.expectedUpdateTimeNS = 1000000000 / this.targetUPS;
        this.totalUpdateTimeNS = expectedUpdateTimeNS;

        entityList = new ArrayList<>();
        random = new Rand();
        rumble = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);

        gameUpdateThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                runGame();
            }
        }, "Game Update Thread");

        viewPaintThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                runGraphics();
            }
        }, "View Paint Thread");

        gameScreen = screen;
        gameScreen.giveDataReference(this);
        gameScreen.setDimensionListener(new Runnable()
        {
            @Override
            public void run()
            {
                viewHasDimension = true;
                if (wantToLaunch)
                {
                    launch();
                }
            }
        });
        gameScreen.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
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
        wantToLaunch = true;
        if (viewHasDimension)
        {
            launch();
        }
    }

    private void launch()
    {
        wantToLaunch = false;
        if (started)
        {
            // May not start a game that is already started
            throw new IllegalStateException("Game already started!");
        }
        else
        {
            started = true;

            // At this point, the surfaceView (and thus the game) has dimensions, so we can do initialization based on them.
            initialize();

            // We will launch two threads.
            // 1) Do game logic
            // 2) Update surface view

            gameUpdateThread.start();
            viewPaintThread.start();
        }
    }

    /**
     * Called once before updates and frames begin.
     * In this call, we are guaranteed that the surfaceViiew (and the game) has dimensions
     * So do any initialization that involves getGameWidth/getGameHeight.
     */
    protected abstract void initialize(); //Always called once before update() calls first begin

    private void runGame()
    {
        while (!stopped)
        {
            long startTime = System.nanoTime();
            synchronized (updateAndFrameAndInputMonitor)
            {
                //Log.d("Gonna update", "now, set lastupdatetime to: " + lastUpdateTimeNS);
                // Save for interpolation
                for (Entity entity : entityList)
                {
                    entity.saveOldPosition();
                }
                update(); //Run frame code
                long currentTimeNS = System.nanoTime();
                actualUpdateTimeNS = currentTimeNS - lastUpdateTimeNS;
                lastUpdateTimeNS = currentTimeNS;
                //Log.d("UPDATE",actualUpdateTimeNS+"");
                while (paused)
                {
                    try
                    {
                        if (!updateThreadPaused)
                        {
                            pauseLatch.countDown();
                        }
                        updateThreadPaused = true;
                        updateAndFrameAndInputMonitor.wait();
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                updateThreadPaused = false;
            }
            long endTime = System.nanoTime();
            totalUpdateTimeNS = endTime - startTime;

            //Figure out how much to delay based on how much time is left over in this frame (or no delay at all if we went over the time limit)
            int amountToDelayNS = (int) Math.max(expectedUpdateTimeNS - totalUpdateTimeNS, 0);

            /*if (amountToDelayNS+totalUpdateTimeNS != 40000000)
            {
                Log.d("ERROR", "ERROR");
                Log.d("TOTAL", amountToDelayNS + totalUpdateTimeNS + "");
                Log.d("delay", amountToDelayNS + "");
            }*/
            int amountToDelayMS = amountToDelayNS / 1000000;
            amountToDelayNS %= 1000000;
            try {
                Thread.sleep(amountToDelayMS, amountToDelayNS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        updateThreadStopped = true;
    }

    private void runGraphics()
    {
        Looper.prepare();
        vsync = Choreographer.getInstance();
        //final Handler viewPaintThreadHandler = new Handler();
        Choreographer.FrameCallback callback = new Choreographer.FrameCallback()
        {
            @Override
            public void doFrame(long frameTimeNanos)
            {
                synchronized (updateAndFrameAndInputMonitor)
                {
                    if (stopped)
                    {
                        Looper.myLooper().quit();
                        return;
                    }
                    /*if (System.nanoTime() - frameTimeNanos > 2000000)
                    {
                        vsync.postFrameCallback(this);
                        return; //Drop frame
                    }*/

                    while (paused)
                    {
                        try
                        {
                            if (!frameThreadPaused)
                            {
                                pauseLatch.countDown();
                            }
                            frameThreadPaused = true;
                            updateAndFrameAndInputMonitor.wait();
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    frameThreadPaused = false;

                    boolean framePainted = false;
                    if (gameScreen.canVisualize())
                    {
                        gameScreen.prepareVisualize();
                        framePainted = true;
                        double ratio = ((double) (System.nanoTime() - lastUpdateTimeNS)) / expectedUpdateTimeNS; //TODO: perhaps each entity should calculate ration based on when it is drawn? This may improve interpolation for a scene with many objects.
                        //Log.d("RATIO", ratio+"");
                        //Log.d("Gonna frame", "now, read lastupdatetime as: " + lastUpdateTimeNS);
                        if (ratio < 0) ratio = 0;
                        if (ratio > 1) ratio = 1;
                        gameScreen.visualize(ratio);
                    }

                    // We only painted if we could visualize in the first place
                    if (oneFrameThenPause && framePainted)
                    {
                        oneFrameThenPause = false;
                        pauseGame();
                    }

                    vsync.postFrameCallback(this); //Must ask for new callback each frame!
                }
            }
        };
        vsync.postFrameCallback(callback);
        Looper.loop();
        frameThreadStopped = true;
    }


    /**
     * Stop update and frame threads.
     * After the threads finish their current loop execution,
     * The game will then call the subclasses cleanup and then cleanup itself.
     */
    public void stopGame()
    {
        if (!Thread.currentThread().getName().equals("main"))
        {
            Log.w("T", "Was not called from the main thread, instead from: " + Thread.currentThread().getName() + ". Will be run from the Main thread instead.");
        }

        stopped = true; //TODO: synchronize this?

        Handler mainHandler = new Handler(appContext.getMainLooper());
        mainHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    gameUpdateThread.join();
                    viewPaintThread.join();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    cleanup();
                    internalCleanup();
                    dispatchEvent(GameEventConstants.GAME_OVER);
                }
            }
        });
    }

    /**
     * Cleanup and garbage collect here, which will run before the superclass cleans.
     */
    protected abstract void cleanup();

    /**
     * Pause the threads
     */
    public void pauseGame(/*final Runnable callback*/)
    {
        if (Thread.currentThread().equals(gameUpdateThread) || Thread.currentThread().equals(viewPaintThread))
        {
            paused = true;
            return;
        }
        if (!isActive())
        {
            Log.w("F", "No need to pause game that isn't running.");
            return;
        }/*
        Handler handler = new Handler(appContext.getMainLooper());
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                Log.d("M", Thread.currentThread().getName());
                paused = true; //Tell threads to pause
                Log.d("M", "Abstract Game Engine attempt to pause threads.");
                while (!frameThreadPaused && !updateThreadPaused);
                Log.d("M", "Abstract Game Engine sees threads paused.");
            }
        });*/

        // 1 frame thread + 1 update thread = 2
        pauseLatch = new CountDownLatch(2);

        // Tell threads to stop
        paused = true;

        try
        {
            pauseLatch.await();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void unpauseGame()
    {
        if (!isActive())
        {
            Log.w("F", "Cannot unpause game that isn't active.");
            return;
        }
        Log.d("G", "Unpausing game");
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
        oneFrameThenPause = true;
        unpauseGame();
    }


    /**
     * Now that the threads have stopped, clean up
     */
    private void internalCleanup()
    {
        Log.d("h", "CLEANUP");
        vsync = null;
        random = null;
        rumble = null;
        gameUpdateThread = null;
        viewPaintThread = null;
        entityList.clear();
        entityList = null;
        //Cleanup gameScreen
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

    /**
     * Tracks an entity for interpolation purposes and returns it
     */
    protected <T extends Entity> T track(T entityKind)
    {
        entityList.add(entityKind);
        return entityKind;
    }

    /**
     * Black magic or utter genius?
     * Perhaps add overloaded constructor for Entity itself that does not require you to pass Entity.class?
     */
    /*
    protected <T extends Entity> T createEntity(Class<T> type, Object... args)
    {
        T creation = null;

        // Find the desired constructor by analysing the args that were passed
        Constructor[] constructors = type.getDeclaredConstructors();
        Constructor matchedConstructor = null;
        for (Constructor constructor : constructors)
        {
            boolean matchWorks = true;
            Class[] conArgTypes = constructor.getParameterTypes();
            if (args.length != conArgTypes.length)
            {
                matchWorks = false;
                Log.d("A", "Wrong length");
            }
            else
            {
                for (int i = 0; i < args.length; i++)
                {
                    Class argType = args[i].getClass();
                    Class conArgType = conArgTypes[i];

                    if (argType.isPrimitive() && conArgType.isPrimitive()) //This check does not work on wrapper classes. Needs a fix!
                    {
                        Log.d("A", "Both primitive");
                    }
                    else if (!(conArgType.isAssignableFrom(argType)))
                    {
                        matchWorks = false;
                        Log.d("A", "Arg mismatch. constructor wanted: " + conArgType + ". arg supplied: " + argType);
                        break;
                    }
                }
            }
            if (matchWorks)
            {
                matchedConstructor = constructor;
            }
        }
        if (matchedConstructor == null)
        {
            throw new IllegalArgumentException("No constructor found matching the supplied arguments.");
        }

        try
        {
            Log.d("h", "Class exists" + type.getDeclaredConstructors()[0].toString());
            creation = type.cast(matchedConstructor.newInstance(args));
            entityList.add(creation);
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
        return creation;
    }*/

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
