package box.gift.rope;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
    private SharedPreferences sharedPreferences;
    private AbstractGameEngine gameEngine;
    private AbstractGameSurfaceView gameScreen; //TODO: change var type to Screen once interface is robust enough
    private Bitmap screenshot;

    private Runnable readyForPaintingListener;
    private Runnable gameOverHandler;

    private ViewGroup gameFrame;
    private View pauseMenu;

    private boolean wasJustCreated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        L.d("CREATE", "lifecycle");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        super.onCreate(savedInstanceState);

        appContext = getApplicationContext();

        L.disableChannel("lifecycle");

        readyForPaintingListener = new Runnable()
        {
            @Override
            public void run()
            {
                if (gameScreen != null && gameEngine != null)
                {
                    if (!gameEngine.isActive()) //TODO: only start if hasn't been started (i.e. do not start if the same engine has simply been stopped).
                    {
                        gameEngine.startGame();
                    }
                    else if (!gameEngine.isPlaying())
                    {
                        if (screenshot != null && !screenshot.isRecycled())
                        {
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
                        stopGame(null);
                    }
                });
            }
        };

        sharedPreferences = getPreferences(Context.MODE_PRIVATE);

        showMainMenuLayout(0, sharedPreferences.getInt(getString(R.string.pref_best), 0));
        wasJustCreated = true;
    }

    private void showMainMenuLayout(int score, int best) //TODO: score and best are a certain type of game, this should be an override in app mod, or even better, provided as a type of activity option
    {
        setContentView(R.layout.menu_layout);

        TextView scoreView = findViewById(R.id.score);
        TextView bestView = findViewById(R.id.best);

        scoreView.setText(String.valueOf(score));
        bestView.setText(String.valueOf(best));
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
    }

    @Override
    protected void onStart()
    {
        L.d("START", "lifecycle");
        super.onStart();

        // Determine if the game is stopped or just paused.
        if (gameEngine == null || !gameEngine.isActive())
        {
            if (!wasJustCreated)
            {
                showMainMenuLayout(sharedPreferences.getInt(getString(R.string.pref_last_score), 0), sharedPreferences.getInt(getString(R.string.pref_best), 0));
            }
        }
        else
        {
            // Show the pause menu
            showPauseMenu();
        }

        wasJustCreated = false;
    }

    @Override
    protected void onPause()
    {
        L.d("PAUSE", "lifecycle");
        pauseGameIfPlaying();
        super.onPause();
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
        L.d("STOP", "lifecycle");

        // save screenshot of the screen to paint when we resume.
        if (gameScreen != null && gameEngine != null && gameEngine.isActive())
        {
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

    public void startGame(View view)
    {
        showGameLayout();
        createGame();
        showGame();
    }

    public void resumeGame(View view)
    {
        hidePauseMenu();
        gameEngine.resumeGame();
    }

    public void stopGame(View view)
    {
        if (gameEngine.isActive())
        {
            gameEngine.stopGame();
        }
        else
        {
            throw new IllegalStateException("Cannot stop an inactive game!");
        }

        int score = gameEngine.getResult();
        int best = sharedPreferences.getInt(getString(R.string.pref_best), 0);

        gameEngine = null;
        gameScreen = null;
        //TODO; cleanup gamescreen/gameengine
        // If we came from the pause menu, hide it.
        if (view != null)
        {
            hidePauseMenu();
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Save last score.
        editor.putInt(getString(R.string.pref_last_score), score);
        // If new high score, update best.
        if (score > best)
        {
            editor.putInt(getString(R.string.pref_best), score);
        }
        editor.apply();

        showMainMenuLayout(score, sharedPreferences.getInt(getString(R.string.pref_best), 0));
    }

    // Should work even if pause menu is already showing.
    private void showPauseMenu() //TODO: why is pause menu hiding the game beneath it?!
    {
        pauseMenu = findViewById(R.id.pauseMenu); //TODO: only run this once.
        pauseMenu.setVisibility(View.VISIBLE);
    }

    private void hidePauseMenu()
    {
        pauseMenu.setVisibility(View.GONE);
    }
}
