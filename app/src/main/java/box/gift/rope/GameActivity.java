package box.gift.rope;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import box.shoe.gameutils.GameEvents;
import box.shoe.gameutils.L;
import box.shoe.gameutils.AbstractGameEngine;
import box.shoe.gameutils.AbstractGameSurfaceView;
//TODO: there should be an interface updateable for entities, taskschedulers. particleeffetc etccc...
//TODO: place in gameutils module, so that it can be simply extended in the app module. (similarly, the layout files, somehow make customizable still)
public class GameActivity extends Activity //TODO: destructive callbacks can do work before calling super, do not unpause when game unpauses, look at lunar landing ex
{ //TODO: theme up splash screen
    private Context appContext;
    private AbstractGameEngine gameEngine;
    private AbstractGameSurfaceView gameScreen; //TODO: change var type to Screen once interface is robust enough
    private Bitmap screenshot;

    private Runnable readyForPaintingListener;
    private Runnable gameOverHandler;

    private ViewGroup gameFrame;
    private View pauseMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        L.d("CREATE", "lifecycle");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        super.onCreate(savedInstanceState);

        appContext = getApplicationContext();

        L.disableChannel("trace");
        L.disableChannel("thread");
        L.disableChannel("new");
        L.disableChannel("rewrite");
        L.disableChannel("rewrite2");
        L.disableChannel("memory");
        L.disableChannel("gameOver");

        readyForPaintingListener = new Runnable()
        {
            @Override
            public void run()
            {
                if (gameScreen != null && gameEngine != null)
                {
                    if (!gameEngine.isActive()) //TODO: implies that a stopped game can be run again?
                    {
                        L.d("Received surfaceChanged callback.", "lifecycle");
                        gameEngine.startGame();
                    }
                    else if (!gameEngine.isPlaying())
                    {
                        L.d("use screenshot?", "screenshot");
                        if (screenshot != null && !screenshot.isRecycled())
                        {
                            L.d("using screenshot", "screenshot");
                            gameScreen.preparePaint();
                            gameScreen.paintBitmap(screenshot);
                        }
                    }
                }
            }
        };

        gameOverHandler = new Runnable()
        {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        L.d("stop on ui thread", "gameOver");
                        stopGame(null);
                        L.d("after on ui thread", "gameOver");
                    }
                });
            }
        };

        showMainMenuLayout(0, 0);//TODO:fix scores
/*
        L.LOG = true;
        L.disableChannel("thread");
        L.disableChannel("stop");
        L.disableChannel("new");
*/
        //SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
       // setupMenu(-1, pref.getInt("best", 0));
    }

    private void showMainMenuLayout(int score, int best) //TODO: score and best are a certain type of game, this should be an override in app mod, or even better, provided as a type of activity option
    {
        L.d("showMM: before set content view", "gameOver");
        setContentView(R.layout.menu_layout);

        TextView scoreView = findViewById(R.id.score);
        TextView bestView = findViewById(R.id.best);

        scoreView.setText(String.valueOf(score));
        bestView.setText(String.valueOf(best));

        L.d("showMM: after set content view", "gameOver");
    }

    private void showGameLayout()
    {
        setContentView(R.layout.game_layout);
        gameFrame = findViewById(R.id.gameFrame);
    }

    private void createGame()
    {
        gameScreen = new RopeScreen(appContext, readyForPaintingListener);
        gameEngine = new RopeGame(appContext, gameScreen);
        gameEngine.addEventListener(GameEvents.GAME_OVER, gameOverHandler);
    }

    private void showGame()
    {
        gameFrame.removeAllViews();
        gameFrame.addView(gameScreen);
        L.d("Game added to layout.", "lifecycle");
    }

    @Override
    protected void onPause()
    {
        L.d("PAUSE", "lifecycle");
        pauseGameIfPlaying();
        super.onPause();
        L.d("end of onPause callback", "gameOver");
    }

    /**
     * If playing the game, pause it.
     * If game is paused, resume it.
     * Otherwise, default back button action.
     */
    @Override
    public void onBackPressed()
    {
        L.d("back pressed : beginning", "gameOver");
        if (pauseGameIfPlaying())
        {
            L.d("back pressed : choice 1", "gameOver");
            // On a game pause, since we have not left the activity, show the pause menu.
            showPauseMenu();
        }
        else if (gameEngine != null && gameEngine.isActive() && !gameEngine.isPlaying())
        {
            L.d("back pressed : choice 2", "gameOver");
            // If the game is paused, return to the game, as if the resume game button was pressed.
            resumeGame(null);
        }
        else
        {
            L.d("back pressed : choice 3", "gameOver");
            // If the game was not active, simply let the OS do whatever it wants.
            super.onBackPressed();
        }
        L.d("back pressed : end", "gameOver");
    }

    // Returns whether or not the game was running (and thus was paused)
    private boolean pauseGameIfPlaying()
    {
        L.d("pause game if playing called", "gameOver");
        if (gameScreen != null)
        {
            if (gameEngine.isPlaying())
            {
                L.d("pause game if playing has determined that the game needs to be paused.", "gameOver");
                gameEngine.pauseGame();
                return true;
            }
        }
        L.d("pause game if playing end", "gameOver");
        return false;
    }

    @Override
    protected void onStop()
    {
        L.d("STOP", "lifecycle");

        // save screenshot of the screen to paint when we resume.
        if (gameScreen != null && gameEngine != null && gameEngine.isActive())
        {
            L.d("Saving screenshot", "screenshot");
            if (screenshot != null && !screenshot.isRecycled())
            {
                screenshot.recycle();
            }
            screenshot = gameScreen.getScreenshot();
        }
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
        L.d("DESTROY", "lifecycle");
        if (screenshot != null)
        {
            screenshot.recycle();
            screenshot = null;
        }
        if (gameEngine != null)
        {
            gameScreen.unregisterReadyForPaintingListener();
            if (gameEngine.isActive())
            {
                gameEngine.stopGame(); //TODO: run cleanup on gameEngine if we decide that stopGame doesn't do so automatically.
            }
        }
        gameEngine = null;
        readyForPaintingListener = null;
        gameFrame = null;
        pauseMenu = null;
        //TODO; deal will gameScreen

        appContext = null;
        super.onDestroy();
    }

    @Override
    protected void onResume()
    {
        L.d("RESUME", "lifecycle");
        super.onResume();

        // Determine if the game is stopped or just paused.
        if (gameEngine == null || !gameEngine.isActive())
        {
            L.d("onResume, decided to show main menu because gameEngine isn't active, (or doesn't exist).", "gameOver");
            showMainMenuLayout(0, 0);//TODO:fix scores
        }
        else
        {
            // Show the pause menu
            showPauseMenu();
        }
    }

    public void startGame(View view)
    {
        L.d("start game", "gameOver");
        showGameLayout();
        createGame();
        showGame();
    }

    public void resumeGame(View view)
    {
        L.d("resume game", "gameOver");
        hidePauseMenu();
        gameEngine.resumeGame();
    }

    public void stopGame(View view)
    {
        L.d("begin stop game, view=" + view, "gameOver");
        if (gameEngine.isActive())
        {
            gameEngine.stopGame();
        }
        else
        {
            throw new IllegalStateException("Cannot stop an inactive game!");
        }

        int score = gameEngine.getResult();

        gameEngine = null;
        gameScreen = null;
        //TODO; cleanup gamescreen/gameengine
        // If we came from the pause menu, hide it.
        if (view != null)
        {
            L.d("will hide pause menu", "gameOver");
            hidePauseMenu();
        }
        showMainMenuLayout(score, 0); //TODO:fix scores

        L.d("end of stop game with view=" + view, "gameOver");
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
                    game.addEventListener(GameEvents.GAME_OVER, new Runnable()
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

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState)
    {//TODO: save last score
        L.d("SAVE INSTANCE STATE", "lifecycle");
        super.onSaveInstanceState(savedInstanceState);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {//TODO: restore last score
        L.d("RESTORE INSTANCE STATE", "lifecycle");
        super.onRestoreInstanceState(savedInstanceState);
    }
}