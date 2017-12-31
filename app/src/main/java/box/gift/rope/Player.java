package box.gift.rope;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import box.shoe.gameutils.Entity;
import box.shoe.gameutils.InterpolateSource;
import box.shoe.gameutils.InterpolateTarget;
import box.shoe.gameutils.Paintable;
import box.shoe.gameutils.Vector;

/**
 * Created by Joseph on 11/29/2017.
 */

public class Player extends Entity implements Paintable
{
    private final Paint paint;

    @InterpolateSource(id = "opacity")
    public double opacity = 0;

    @InterpolateTarget(id = "opacity")
    public double _opacity = 0;
    /*package*/ static final Vector jumpVelocity = new Vector(0, -30);

    public Player(double initialX, double initialY)
    {
        super(initialX, initialY, 60, 60, Vector.ZERO, new Vector(0, 2));
        registration = new Vector(width / 4, 0);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
    }

    @Override
    public void paint(Canvas canvas)
    {
        //System.out.println(_position.getY());
        canvas.drawRect((int) Math.round(_position.getX()), (int) Math.round(_position.getY()), (int) Math.round((_position.getX() + _width)), (int) Math.round((_position.getY() + _height)), paint);
    }
}
