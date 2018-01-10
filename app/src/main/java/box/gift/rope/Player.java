package box.gift.rope;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import box.shoe.gameutils.Entity;
import box.shoe.gameutils.Paintable;
import box.shoe.gameutils.Vector;

/**
 * Created by Joseph on 11/29/2017.
 */

public class Player extends Entity implements Paintable
{
    private Paint paint;
    /*package*/ static final Vector jumpVelocity = new Vector(0, -30);

    public Player(double initialX, double initialY)
    {
        super(initialX, initialY, 60, 60, Vector.ZERO, new Vector(0, 2));
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
    }

    @Override
    public void paint(Canvas canvas)
    {
        canvas.drawRect((float) _position.getX(), (float) _position.getY(), (float) (_position.getX() + _width), (float) (_position.getY() + _height), paint);
    }

    @Override
    public void cleanup()
    {
        super.cleanup();
        paint = null;
    }
}
