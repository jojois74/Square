package box.shoe.gameutils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import box.gift.gameutils.R;
import box.shoe.gameutils.Screen;
import box.shoe.gameutils.Weaver;
import box.shoe.gameutils.GameEvents;
import box.shoe.gameutils.L;
import box.shoe.gameutils.AbstractGameEngine;
import box.shoe.gameutils.AbstractGameSurfaceView;
//TODO: there should be an interface updateable to make a thing update for entities, taskschedulers. particleeffetc etccc...
//TODO: place in gameutils module, so that it can be simply extended in the app module. (similarly, the layout files, somehow make customizable still)
//TODO: destructive callbacks can do work before calling super, do not unpause when game unpauses, look at lunar landing ex
public abstract class AbstractGameActivity extends Activity
{
    private Context appContext;
    private SharedPreferences sharedPreferences;
    private AbstractGameEngine gameEngine;
    private Screen gameScreen; //TODO: change var type to Screen once interface is robust enough
    private Bitmap screenshot;

    private Runnable readyForPaintingListener;
    private Runnable gameOverHandler;

    private ViewGroup gameView;
    private ViewGroup mainMenuView;
    private ViewGroup gameContainer;
    private View pauseMenu;

    private int gameSplashColor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        appContext = getApplicationContext();

        gameSplashColor = getResources().getColor(provideGameSplashColorId());

        readyForPaintingListener = new Runnable()
        {
            @Override
            public void run()
            {
                if (gameScreen != null && gameEngine != null)
                {
                    if (!gameEngine.isActive()) //TODO: only start if hasn't been started (i.e. do not start if the same engine has simply been stopped).
                    {
                        gameScreen.preparePaint();
                        gameScreen.paintStatic(gameSplashColor);
                        gameEngine.startGame();
                        gameContainer.setVisibility(View.VISIBLE);
                    }
                    else if (!gameEngine.isPlaying())
                    {
                        if (screenshot != null && !screenshot.isRecycled())
                        {
                            gameScreen.preparePaint();
                            gameScreen.paintStatic(screenshot);
                            gameContainer.setVisibility(View.VISIBLE);
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
                        stopGame();
                    }
                });
            }
        };

        sharedPreferences = getPreferences(Context.MODE_PRIVATE);

        setContentView(R.layout.master_layout);

        // Programmatically get the Main Menu layout and inflate the stub.
        ViewStub stub = findViewById(R.id.mainMenuStub);
        stub.setLayoutResource(provideMainMenuLayoutResId());
        stub.inflate();

        // Programmatically get the Pause Menu layout and inflate the stub.
        stub = findViewById(R.id.pauseMenuStub);
        stub.setLayoutResource(providePauseMenuLayoutResId());
        stub.inflate();

        // Save references to various Views we will need.
        gameView = findViewById(R.id.gameScreen);
        mainMenuView = findViewById(R.id.mainScreen);
        gameContainer = findViewById(R.id.gameContainer);
        pauseMenu = findViewById(R.id.pauseMenu);

        gameContainer.setVisibility(View.INVISIBLE);

        Weaver.hook(GameEvents.GAME_OVER, gameOverHandler);
        Weaver.hook(GameEvents.GAME_QUIT, gameOverHandler);
        Weaver.hook(GameEvents.GAME_START, new Runnable()
        {
            @Override
            public void run()
            {
                startGame();
            }
        });
        Weaver.hook(GameEvents.GAME_RESUME, new Runnable()
        {
            @Override
            public void run()
            {
                resumeGame();
            }
        });

        showMainMenuLayout(0, sharedPreferences.getInt(getString(R.string.pref_best), 0));
    }

    private void showMainMenuLayout(int score, int best) //TODO: score and best are a certain type of game, this should be an override in app mod, or even better, provided as a type of activity option
    {
        gameView.setVisibility(View.GONE);
        mainMenuView.setVisibility(View.VISIBLE);

        TextView scoreView = findViewById(provideScoreTextViewIdResId());
        TextView bestView = findViewById(provideBestTextViewIdResId());

        scoreView.setText(String.valueOf(score));
        bestView.setText(String.valueOf(best));
    }

    private void showGameLayout()
    {
        mainMenuView.setVisibility(View.GONE);
        gameView.setVisibility(View.VISIBLE);
    }

    private void createGame()
    {
        gameScreen = provideNewScreen(appContext, readyForPaintingListener);
        if (!(gameScreen instanceof View))
        {
            throw new IllegalStateException("provideNewScreen() must supply a Screen which is also a View.");
        }
        gameEngine = provideNewAbstractGameEngine(appContext, gameScreen);
    }

    private void showGame()
    {
        gameContainer.removeAllViews();
        gameContainer.addView(gameScreen.asView());
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // Determine if the game is stopped or just paused.
        if (gameEngine != null && gameEngine.isActive())
        {
            // Show the pause menu
            showPauseMenu();
        }
    }

    @Override
    protected void onPause()
    {
        pauseGameIfPlaying();
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        System.out.println(hasFocus);
        if (!hasFocus)
        {
            pauseGameIfPlaying();
        }
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
            resumeGame();
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
        gameContainer = null;
        pauseMenu = null;
        //TODO; deal will gameScreen

        appContext = null;
        super.onDestroy();
    }

    private void startGame()
    {
        showGameLayout();
        createGame();
        showGame();
    }

    private void resumeGame()
    {
        hidePauseMenu();
        gameEngine.resumeGame();
    }

    private void stopGame()
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
        hidePauseMenu();

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
    private void showPauseMenu()
    {
        pauseMenu.setVisibility(View.VISIBLE);
    }

    private void hidePauseMenu()
    {
        pauseMenu.setVisibility(View.GONE);
    }

    protected abstract Screen provideNewScreen(Context context, Runnable readyForPaintingListener);
    protected abstract AbstractGameEngine provideNewAbstractGameEngine(Context context, Screen screen);

    protected abstract int provideMainMenuLayoutResId();
    protected abstract int providePauseMenuLayoutResId();

    protected abstract int provideScoreTextViewIdResId();
    protected abstract int provideBestTextViewIdResId();

    protected abstract int provideGameSplashColorId();
}
