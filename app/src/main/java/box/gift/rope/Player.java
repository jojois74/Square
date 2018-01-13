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
    private int w = 4;
    /*package*/ static final Vector jumpVelocity = new Vector(0, -30);

    public Player(double initialX, double initialY)
    {
        super(initialX, initialY, 78, 78, Vector.ZERO, new Vector(0, 2));
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    public void paint(Canvas canvas)
    {
        paint.setColor(Color.WHITE);
        canvas.drawRect((float) _position.getX(), (float) _position.getY(), (float) (_position.getX() + _width), (float) (_position.getY() + _height), paint);
        paint.setColor(Color.parseColor("#bbbbff"));
        canvas.drawRect((float) (_position.getX() + w), (float) (_position.getY() + w), (float) (_position.getX() + _width - w), (float) (_position.getY() + _height - w), paint);
    }

    @Override
    public void cleanup()
    {
        super.cleanup();
        paint = null;
    }
}
