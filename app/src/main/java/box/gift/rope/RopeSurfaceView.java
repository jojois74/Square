package box.gift.rope;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import box.shoe.gameutils.AbstractGameSurfaceView;
import box.shoe.gameutils.Entity;
import box.shoe.gameutils.InterpolatableEntity;
import box.shoe.gameutils.GameState;
import box.shoe.gameutils.VisualizableEntity;

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
        Set<InterpolatableEntity> interps = interpolatedState.getInterpolatedEntities();
        //MainActivity.print(interps.toString());

        // Background
        canvas.drawColor(Color.WHITE);

        // Player
        for (InterpolatableEntity interp : interps)
        {
            if (interp instanceof VisualizableEntity)
            {
                ((VisualizableEntity) interp).paint(canvas);
            }
        }

        /*if (player != null)
            player.paint(canvas);*/

/*
        // Particle
        if (data.effect != null)
            data.effect.paint(canvas, interpolationRatio);
*/
        // Walls
        /*for (InterpolatableEntity wall : walls)
        {
            wall.paint(canvas);
        }*/
/*
        // Coins
        LinkedList<InterpolatableEntity> coins = data.coins;
        for (InterpolatableEntity coin : coins)
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
