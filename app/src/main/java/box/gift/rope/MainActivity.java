package box.gift.rope;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import box.shoe.gameutils.L;
import box.shoe.gameutils.AbstractGameEngine;
import box.shoe.gameutils.AbstractGameSurfaceView;
import box.shoe.gameutils.GameEventConstants;


public class MainActivity extends Activity //TODO: destructive callbacks can do work before calling super, do not unpause when game unpauses, look at lunar landing ex
{//TODO: when back button is pressed it should safely pause like normal
    public static Context appContext;
    private AbstractGameEngine gameEngine;
    private AbstractGameSurfaceView gameScreen;

    private Runnable surfaceChangedListener;

    private ViewGroup gameFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        print("CREATE");
        super.onCreate(savedInstanceState);

        MainActivity.appContext = getApplicationContext();

        //L.disableChannel("trace");
        L.disableChannel("thread");
        L.disableChannel("new");
        L.disableChannel("rewrite");
        L.disableChannel("rewrite2");

        surfaceChangedListener = new Runnable()
        {
            @Override
            public void run()
            {
                if (!gameEngine.isActive()) //TODO: implies that a stopped game can be run again?
                {
                    print("Received surfaceChanged callback.");
                    gameEngine.startGame();
                    gameScreen.unregisterSurfaceChangedListener();
                }
            }
        };

        showMainMenuLayout();
/*
        L.LOG = true;
        L.disableChannel("thread");
        L.disableChannel("stop");
        L.disableChannel("new");
*/
        //SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
       // setupMenu(-1, pref.getInt("best", 0));
    }

    private void showMainMenuLayout()
    {
        setContentView(R.layout.menu_layout);
    }

    public void mainMenuClicked(View view)
    {
        print("CLICK");

        showGameLayout();
        createGame();
        showGame();
    }

    private void showGameLayout()
    {
        setContentView(R.layout.game_layout);
        gameFrame = findViewById(R.id.gameFrame);
    }

    private void createGame()
    {
        gameScreen = new RopeScreen(appContext, surfaceChangedListener);
        gameEngine = new RopeGame(appContext, gameScreen);
    }

    private void showGame()
    {
        gameFrame.removeAllViews();
        gameFrame.addView(gameScreen);
        print("Game added to layout.");
    }

    @Override
    protected void onPause()
    {
        print("PAUSE");
        if (gameScreen != null)
        {
            gameScreen.unregisterSurfaceChangedListener();
            if (gameEngine.isPlaying())
            {
                gameEngine.pauseGame();
            }
        }
        super.onPause();
        print("DONE PAUSE");
    }

    @Override
    protected void onStop()
    {
        print("STOP");
        /*
        if (gameScreen != null)
        {
            gameScreen.unregisterSurfaceChangedListener();
        }
        if (gameEngine != null && gameEngine.isActive())
        {
            gameEngine.stopGame();
        }
        gameEngine = null;
        gameScreen = null;
        */
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        print("DESTROY");
    }

    @Override
    protected void onResume()
    {
        print("RESUME");
        super.onResume();

        // Determine if the game is stopped or just paused.
        if (gameEngine == null || !gameEngine.isActive())
        {
            showMainMenuLayout();
        }
        else
        {
            gameEngine.unpauseGame();
        }
    }

/*
    private void setupMenu(int s, int b)
    {
        setContentView(R.layout.menu_layout);
        View scoreWrapView = findViewById(R.id.scoreWrap);
        TextView scoreView = (TextView) findViewById(R.id.score);
        if (s > -1)
        {
            scoreView.setText(String.valueOf(s));
            scoreWrapView.setVisibility(View.VISIBLE);
        }
        else
        {
            scoreWrapView.setVisibility(View.INVISIBLE);
        }

        TextView bestView = (TextView) findViewById(R.id.best);
        bestView.setText(String.valueOf(b));

        (findViewById(R.id.menu_root)).setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                int action = event.getActionMasked();
                if (action == MotionEvent.ACTION_DOWN)
                {
                    setContentView(R.layout.game_layout);
                    gameFrame = ((ViewGroup) findViewById(R.id.gameFrame));
                    gameFrame.removeAllViews();
                    gameScreen = new RopeScreen(appContext);
                    L.d("Creating new rope game", "trace");
                    game = new RopeGame(appContext, gameScreen);
                    game.addEventListener(GameEventConstants.GAME_OVER, new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            int score = game.score;
                            SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
                            int currentBest = pref.getInt("best", 0);
                            if (score > currentBest)
                            {
                                SharedPreferences.Editor edit = pref.edit();
                                edit.putInt("best", score);
                                edit.commit();
                            }
                            game = null;
                            setupMenu(score, pref.getInt("best", 0));
                        }
                    });
                    gameFrame.addView(gameScreen);
                    game.startGame();
                }
                return true;
            }
        });
    }
    */
/*
    @Override
    protected void onStart()
    {
        super.onStart();
        print("START");
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        print("RESTORE INSTANCE STATE");
    }

    @Override
    protected void onResume()
    {
        print("resume");
        super.onResume();
        print("RESUME");
        if (game != null && game.isActive() && !game.isPlaying())
        {
            // Don't unpause the game just yet, but we would like to paint one frame behind the pause menu_layout
            //game.unpauseGame();

            print("Painting a single frame.");
            game.paintOneFrame();
        }
    }

    @Override
    protected void onPause()
    {
        print("PAUSE");
        if (game != null && game.isPlaying())
        {
            game.pauseGame();
            print("after game pause");
            //(findViewById(R.id.pauseMenu)).setVisibility(View.VISIBLE);
        }
        super.onPause();
        print("after super pause");
    }

    public void continueGame(View view)
    {
        print("CONTINUE");
        game.unpauseGame();
        (findViewById(R.id.pauseMenu)).setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStop()
    {
        print("STOP: " + Thread.currentThread().getName());
        super.onStop();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        print("RESTART");
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }
*/
    public static void print(String msg)
    {
        Log.d("SQUARE", msg);
    }
}
