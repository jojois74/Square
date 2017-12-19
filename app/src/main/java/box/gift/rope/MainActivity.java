package box.gift.rope;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import box.shoe.gameutils.L;
import box.shoe.gameutils.AbstractGameEngine;
import box.shoe.gameutils.AbstractGameSurfaceView;


public class MainActivity extends Activity //TODO: destructive callbacks can do work before calling super, do not unpause when game unpauses, look at lunar landing ex
{
    private Context appContext;
    private AbstractGameEngine gameEngine;
    private AbstractGameSurfaceView gameScreen;

    private Runnable surfaceChangedListener;

    private ViewGroup gameFrame;
    private View pauseMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        print("CREATE");
        super.onCreate(savedInstanceState);

        appContext = getApplicationContext();

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
        pauseGameIfPlaying();
        super.onPause();
        print("DONE PAUSE");
    }

    /**
     * If playing the game, pause it.
     * If game is paused, resume it.
     * Otherwise, default back button action.
     */
    @Override
    public void onBackPressed()
    {
        if (pauseGameIfPlaying())
        {
            // On a game pause, since we have not left the activity, show the pause menu.
            showPauseMenu();
        }
        else if (gameEngine != null && gameEngine.isActive() && !gameEngine.isPlaying())
        {
            // If the game is paused, return to the game, as if the resume game button was pressed.
            resumeGame(null);
        }
        else
        {
            // If the game was not active, simply let the OS do whatever it wants.
            super.onBackPressed();
        }
    }

    // Returns whether or not the game was running (and thus was paused)
    private boolean pauseGameIfPlaying()
    {
        if (gameScreen != null)
        {
            gameScreen.unregisterSurfaceChangedListener(); //TODO: this should be done as soon as the game starts, not waiting until resume??? when is this called anyway, when screen is flipped? BAD! (maybe bad)
            if (gameEngine.isPlaying())
            {
                gameEngine.pauseGame();
                return true;
            }
        }
        return false;
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

    /**
     * Be a good citizen and stop the game for good on destroy.
     * Free memory, kill threads. This is not necessary, because the system will do any reclaim
     * necessary. Even so, we can dismantle our threads properly without much effort.
     * But if onDestroy is never called (as is possible), there will not be any problems.
     */
    @Override
    protected void onDestroy()
    {
        if (gameEngine != null)
        {
            gameScreen.unregisterSurfaceChangedListener();
            if (gameEngine.isActive())
            {
                gameEngine.stopGame(); //TODO: run cleanup on gameEngine if we decide that stopGame doesn't do so automatically.
            }
        }
        gameEngine = null;
        //TODO; deal will gameScreen

        print("DESTROY");
        super.onDestroy();
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
            // Show the pause menu
            showPauseMenu();

            // Tell the game to paint a single frame so that we can see the game.
            gameEngine.paintOneFrame(); //TODO: timing issues cause frame jumps and visual jumps! low priority fix
        }
    }

    public void resumeGame(View view)
    {
        hidePauseMenu();
        gameEngine.resumeGame();
    }

    // Should work even if pause menu is already showing.
    private void showPauseMenu()
    {
        pauseMenu = findViewById(R.id.pauseMenu);
        pauseMenu.setVisibility(View.VISIBLE);
    }

    private void hidePauseMenu()
    {
        pauseMenu.setVisibility(View.GONE);
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

    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        print("RESTORE INSTANCE STATE");
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }
    public static void print(String msg)
    {
        Log.d("SQUARE", msg);
    }
}
