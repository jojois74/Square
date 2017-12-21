package box.gift.rope;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import box.shoe.gameutils.Entity;
import box.shoe.gameutils.Paintable;
import box.shoe.gameutils.Vector;
import box.shoe.gameutils.VisualizableEntity;

/**
 * Created by Joseph on 11/29/2017.
 */

public class Player extends Entity implements Paintable
{
    private final Paint paint;
    /*package*/ final Vector actionVelocity = new Vector(0, -30);

    public Player(double initialX, double initialY)
    {
        super(initialX, initialY, 60, 60, Vector.ZERO, new Vector(0, 2));
        registration = new Vector(width / 4, 0);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
    }

    @Override
    public void paint(int x, int y, Canvas canvas)
    {
        canvas.drawRect(x, y, (float) (x + width), (float) (y + height), paint);
    }
}
