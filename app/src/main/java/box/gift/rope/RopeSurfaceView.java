package box.gift.rope;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;

import java.math.BigDecimal;
import java.util.LinkedList;

import box.shoe.gameutils.AbstractGameEngine;
import box.shoe.gameutils.AbstractGameSurfaceView;
import box.shoe.gameutils.Entity;

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
    protected void paint(Canvas canvas, AbstractGameEngine abstractData, double interpolationRatio)
    {
        Rope data = (Rope) abstractData;

        // Background
        canvas.drawColor(Color.WHITE);

        // Player
        data.player.paint(canvas, interpolationRatio);

        // Particle
        data.effect.paint(canvas, interpolationRatio);

        // Walls
        LinkedList<Entity> walls = data.walls;
        for (Entity wall : walls)
        {
            wall.paint(canvas, interpolationRatio);
        }

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
        canvas.drawText("Score: " + String.valueOf(data.score), 40, 90, paint);
    }
}
