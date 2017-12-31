package box.gift.rope;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import box.shoe.gameutils.AbstractGameSurfaceView;
import box.shoe.gameutils.Entity;
import box.shoe.gameutils.InterpolatableEntity;
import box.shoe.gameutils.GameState;
import box.shoe.gameutils.L;
import box.shoe.gameutils.Paintable;
import box.shoe.gameutils.ParticleEffect;

/**
 * Created by Joseph on 10/23/2017.
 */

public class RopeScreen extends AbstractGameSurfaceView
{
    public RopeScreen(Context context, Runnable surfaceChangedListener)
    {
        super(context, surfaceChangedListener);
    }

    @Override
    protected void paint(Canvas canvas, GameState gameState)
    {
        //List<InterpolatableEntity> interps = interpolatedState.getInterpolatedEntities();
        //MainActivity.print(interps.toString());

        // Background
        canvas.drawColor(Color.WHITE);

        // Player
        Player player = gameState.get("player");
        player.paint(canvas);

        /*
        // Effect
        LinkedList<ParticleEffect> effects = interpolatedState.getData("effects");
        for (ParticleEffect effect : effects)
        {
            effect.paint(0, 0, canvas);
        }*/

        // Walls
        LinkedList<Paintable> walls = gameState.get("walls");
        for (Paintable wall : walls)
        {
            wall.paint(canvas);
        }

        // Coins
        /*LinkedList<Paintable> coins = interpolatedState.getData("coins");
        for (Paintable coin : coins)
        {
            //if (coin.interpolatedThisFrame)
            {
                coin.paint(canvas);
            }
        }*/

        // Top and Bottom
        paint.setColor(Color.RED);
        int thickness = 14;
        canvas.drawRect(0, 0, getWidth(), thickness, paint);
        canvas.drawRect(0, getHeight() - thickness, getWidth(), getHeight(), paint);

        // Score
        /*paint.setTextSize(50);
        paint.setColor(Color.BLUE);
        canvas.drawText("Score: " + String.valueOf(interpolatedState.getData("score")), 40, 90, paint);*/
    }
}
