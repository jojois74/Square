package box.gift.rope;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;

import java.util.LinkedList;

import box.shoe.gameutils.AbstractGameSurfaceView;
import box.shoe.gameutils.Entity;
import box.shoe.gameutils.FollowCamera;
import box.shoe.gameutils.GameState;
import box.shoe.gameutils.Paintable;
import box.shoe.gameutils.Vector;

/**
 * Created by Joseph on 10/23/2017.
 */

public class RopeScreen extends AbstractGameSurfaceView
{
    FollowCamera camera;

    /* For tool use only (xml layout viewer will construct views to display) */
    private RopeScreen(Context context)
    {
        super(context, null);
        init();
    }

    public RopeScreen(Context context, Runnable surfaceChangedListener)
    {
        super(context, surfaceChangedListener);
        init();
    }

    private void init()
    {
        camera = new FollowCamera(FollowCamera.FOLLOW_X);
    }

    @Override
    public void initialize()
    {
        camera.setOffset(new Vector(getWidth() / 10, 0));
    }

    @Override
    public boolean isVisible(Paintable paintable)
    {
        return false;
    }

    @Override
    public boolean isInbounds(Entity entity)
    {
        return false;
    }

    @Override
    protected void paint(Canvas canvas, GameState gameState)
    {
        Player player = gameState.get("player");
        camera.follow(player);

        // Background
        canvas.drawColor(Color.WHITE);

        camera.view(canvas);

        // Player
        player.paint(canvas);

        // Walls
        LinkedList<Paintable> walls = gameState.get("walls");
        for (Paintable wall : walls)
        {
            wall.paint(canvas);
        }

        // Coins
        LinkedList<Paintable> coins = gameState.get("coins");
        for (Paintable coin : coins)
        {
            coin.paint(canvas);
        }

        camera.unview(canvas);

        // Top and Bottom
        paint.setColor(Color.RED);
        int thickness = 14;
        canvas.drawRect(0, 0, getWidth(), thickness, paint);
        canvas.drawRect(0, getHeight() - thickness, getWidth(), getHeight(), paint);

        // Score - drawing text is a pain. We flipped the canvas to get first quadrant coords,
        // but now we must temporarily reverse so our text isn't flipped!
        canvas.save();
        canvas.scale(1, -1);
        canvas.translate(0, -canvas.getHeight());
        paint.setTextSize(50);
        paint.setColor(Color.BLUE);
        canvas.drawText("Score: " + String.valueOf(gameState.get("score")), 40, 90, paint);
        canvas.restore();
    }
}
