package box.gift.rope;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

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
    private Paint paint;

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
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    public void initialize() {}

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

        // Background
        canvas.drawColor(Color.parseColor("#aaaaff"));

        // Player
        player.paint(canvas);

        // Walls
        LinkedList<Paintable> walls = gameState.get("walls");
        for (Paintable wall : walls)
        {
            wall.paint(canvas);
        }

        // Top and Bottom
        Paintable topBar = gameState.get("top");
        Paintable botBar = gameState.get("bot");
        topBar.paint(canvas);
        botBar.paint(canvas);

        // Coins
        LinkedList<Paintable> coins = gameState.get("coins");
        for (Paintable coin : coins)
        {
            coin.paint(canvas);
        }

        // Score
        paint.setTextSize(50);
        paint.setColor(Color.WHITE);
        canvas.drawText(String.valueOf(gameState.get("score")), 60, 96, paint);
    }

    @Override
    public void cleanup()
    {
        super.cleanup();
        paint = null;
    }
}
