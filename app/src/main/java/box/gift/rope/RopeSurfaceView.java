package box.gift.rope;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import box.shoe.gameutils.AbstractGameEngine;
import box.shoe.gameutils.AbstractGameSurfaceView;
import box.shoe.gameutils.Entity;
import box.shoe.gameutils.GameState;

import static android.R.attr.width;

/**
 * Created by Joseph on 10/23/2017.
 */

public class RopeSurfaceView extends AbstractGameSurfaceView
{
    public RopeSurfaceView(Context context)
    {
        super(context);
    }

    @Override
    protected void paint(Canvas canvas, GameState interpolatedState)
    {
        Player player = interpolatedState.getData("player"); //TODO: replace strings with constants
        LinkedList<Entity> walls = interpolatedState.getData("walls");

        //TODO: paint from interpolated states, change entities to be nice (already sorta done).

        // Background
        canvas.drawColor(Color.WHITE);

        // Player
        if (player != null)
            player.paint(canvas);

/*
        // Particle
        if (data.effect != null)
            data.effect.paint(canvas, interpolationRatio);
*/
        // Walls
        for (Entity wall : walls)
        {
            wall.paint(canvas);
        }
/*
        // Coins
        LinkedList<Entity> coins = data.coins;
        for (Entity coin : coins)
        {
            coin.paint(canvas, interpolationRatio);
        }

        // Top and Bottom
        paint.setColor(Color.RED);
        int thickness = 14;
        canvas.drawRect(0, 0, getWidth(), thickness, paint);
        canvas.drawRect(0, getHeight() - thickness, getWidth(), getHeight(), paint);

        // Score
        paint.setTextSize(50);
        paint.setColor(Color.BLUE);
        canvas.drawText("Score: " + String.valueOf(data.score), 40, 90, paint);*/
    }
}
