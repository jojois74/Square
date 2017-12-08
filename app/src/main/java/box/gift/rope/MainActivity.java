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

import com.rits.cloning.Cloner;

import box.shoe.gameutils.AbstractGameEngine;
import box.shoe.gameutils.AbstractGameSurfaceView;
import box.shoe.gameutils.GameEventConstants;


public class MainActivity extends Activity //TODO: destructive callbacks can do work before calling super, do not unpause when game unpauses, look at lunar landing ex
{//TODO: when back button is pressed it should safely pause like normal
    public static Context appContext;
    private AbstractGameEngine game;
    private AbstractGameSurfaceView gameScreen;
    private ViewGroup gameFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.appContext = getApplicationContext();
        print("CREATE");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        setupMenu(-1, pref.getInt("best", 0));
    }

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
                    gameScreen = new RopeSurfaceView(appContext);
                    game = new Rope(appContext, gameScreen);
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
                /*
                if (game != null)
                    if (game.isRunning())
                        return false;
                        */
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        print("START");
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        print("RESTORE INSTANCE STATE");
    }

    @Override
    protected void onResume() {
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
    protected void onPause() {
        print("PAUSE");
        if (game != null && game.isPlaying())
        {
            game.pauseGame();
            (findViewById(R.id.pauseMenu)).setVisibility(View.VISIBLE);
        }
        super.onPause();
    }

    public void continueGame(View view)
    {
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

    static void print(String msg)
    {
        Log.d(appContext.getResources().getString(R.string.app_name), msg);
    }
    static void print(int msg)
    {
        Log.d(appContext.getResources().getString(R.string.app_name), msg + "");
    }
    static void print(double msg)
    {
        Log.d(appContext.getResources().getString(R.string.app_name), msg + "");
    }
    static void print(Object msg)
    {
        Log.d(appContext.getResources().getString(R.string.app_name), msg.toString());
    }
}
