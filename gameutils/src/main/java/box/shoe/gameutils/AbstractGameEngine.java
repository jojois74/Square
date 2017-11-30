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
    private volatile boolean updateThreadNoMoreWork = false;
    private volatile boolean frameThreadNoMoreWork = false;

    // Threads
    private Thread gameUpdateThread;
    private Thread viewPaintThread;

    private volatile boolean wantToLaunch = false;
    private volatile boolean viewHasDimension = false;

    // Monitors
    private Integer updateAndFrameAndInputMonitor = new Integer(1);
    private Integer endGameMonitor = new Integer(2);

    // Objs
    private Choreographer vsync;
    protected Rand random;
    protected Vibrator rumble;

    // Etc
    public volatile int score;

    public AbstractGameEngine(Context appContext, AbstractGameSurfaceView screen, int targetUPS) //target ups should divide evenly into 1000000000, updates are accurately caleld to within about 10ms
    {
        this.appContext = appContext;

        Display display = ((WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        this.refreshRate = display.getRefreshRate();

        this.targetUPS = targetUPS;
        this.expectedUpdateTimeNS = 1000000000 / this.targetUPS;
        this.totalUpdateTimeNS = expectedUpdateTimeNS;

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
        gameScreen.setDimensionListener(new AbstractGameSurfaceView.DimensionListener()
        {
            @Override
            public void onDimensionGiven()
            {
                viewHasDimension = true;
                if (wantToLaunch)
                {
                    start();
                }
            }
        });
        gameScreen.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (started && !stopped)
                {
                    synchronized (updateAndFrameAndInputMonitor)
                    {
                        onTouchEvent(event);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void launch()
    {
        wantToLaunch = true;
        if (viewHasDimension)
        {
            start();
        }
    }

    private void start()
    {
        if (started)
        {
            throw new IllegalStateException("Game already started!");
        }
        else
        {
            started = true;
            initialize();
            //We will launch two threads.
            //1) Do game logic
            //2) Update surface view
            //The surface will paint more often than the game will update, so it will use interpolation
            gameUpdateThread.start();
            viewPaintThread.start();
        }
    }

    protected abstract void initialize(); //Always called once before update() calls first begin

    private void runGame()
    {
        while (!stopped)
        {
            long startTime = System.nanoTime();
            synchronized (updateAndFrameAndInputMonitor)
            {
                //Log.d("Gonna update", "now, set lastupdatetime to: " + lastUpdateTimeNS);
                update(); //Run frame code
                long currentTimeNS = System.nanoTime();
                actualUpdateTimeNS = currentTimeNS - lastUpdateTimeNS;
                lastUpdateTimeNS = currentTimeNS;
                //Log.d("UPDATE",actualUpdateTimeNS+"");
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
        synchronized (endGameMonitor)
        {
            updateThreadNoMoreWork = true;
            if (frameThreadNoMoreWork)
            {
                Handler handler = new Handler(appContext.getMainLooper());
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        cleanup();
                    }
                });
            }
        }
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

                    if (gameScreen.canVisualize())
                    {
                        gameScreen.prepareVisualize();
                        double ratio = ((double) (System.nanoTime() - lastUpdateTimeNS)) / expectedUpdateTimeNS; //TODO: perhaps each entity should calculate ration based on when it is drawn? This may improve interpolation for a scene with many objects.
                        //Log.d("RATIO", ratio+"");
                        //Log.d("Gonna frame", "now, read lastupdatetime as: " + lastUpdateTimeNS);
                        if (ratio < 0) ratio = 0;
                        if (ratio > 1) ratio = 1;
                        gameScreen.visualize(ratio);
                    }

                    vsync.postFrameCallback(this); //Must ask for new callback each frame!
                }
            }
        };
        vsync.postFrameCallback(callback);
        Looper.loop();
        synchronized (endGameMonitor)
        {
            frameThreadNoMoreWork = true;
            if (updateThreadNoMoreWork)
            {
                Handler handler = new Handler(appContext.getMainLooper());
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        cleanup();
                    }
                });
            }
        }
    }

    public boolean isRunning()
    {
        return started && !stopped;
    }

    protected abstract void update();

    /**
     * Tell threads to stop
     */
    public void endGame()
    {
        stopped = true;
    }

    /**
     * Now that the threads have stopped, clean up
     */
    protected void cleanup()
    {
        Log.d("h", "CLEANUP");
        vsync = null;
        random = null;
        rumble = null;
        gameUpdateThread = null;
        viewPaintThread = null;

        //Cleanup gameScreen
    }

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
