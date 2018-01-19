package box.gift.rope;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

import box.shoe.gameutils.AbstractGameActivity;
import box.shoe.gameutils.AbstractGameEngine;
import box.shoe.gameutils.GameEvents;
import box.shoe.gameutils.Screen;
import box.shoe.gameutils.Weaver;

/**
 * Created by Joseph on 1/18/2018.
 */

public class RopeActivity extends AbstractGameActivity
{
    public static int foregroundColor;
    public static int backgroundColor;
    public static int accentColor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        foregroundColor = getResources().getColor(R.color.foreground);
        backgroundColor = getResources().getColor(R.color.background);
        accentColor = getResources().getColor(R.color.accent);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Screen provideNewScreen(Context context, Runnable readyForPaintingListener)
    {
        return new RopeScreen(context, readyForPaintingListener);
    }

    @Override
    protected AbstractGameEngine provideNewAbstractGameEngine(Context context, Screen screen)
    {
        return new RopeGame(context, screen);
    }

    @Override
    protected int provideMainMenuLayoutResId()
    {
        return R.layout.main_menu_layout;
    }

    @Override
    protected int providePauseMenuLayoutResId()
    {
        return R.layout.pause_menu_layout;
    }

    @Override
    protected int provideScoreTextViewIdResId()
    {
        return R.id.score;
    }

    @Override
    protected int provideBestTextViewIdResId()
    {
        return R.id.best;
    }

    @Override
    protected int provideGameSplashColorId()
    {
        return R.color.background;
    }

    public void start(View v)
    {
        Weaver.tug(GameEvents.GAME_START);
    }

    public void resume(View v)
    {
        Weaver.tug(GameEvents.GAME_RESUME);
    }

    public void quit(View v)
    {
        Weaver.tug(GameEvents.GAME_QUIT);
    }
}