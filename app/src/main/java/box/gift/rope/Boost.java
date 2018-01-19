package box.gift.rope;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import box.shoe.gameutils.Entity;
import box.shoe.gameutils.Paintable;
import box.shoe.gameutils.Vector;

/**
 * Created by Joseph on 1/13/2018.
 */

public class Boost extends Entity implements Paintable
{
    private Paint paint;
    private int borderThickness = 4;

    public Boost(double initialX, double initialY)
    {
        super(initialX, initialY, 76, 76, new Vector(-21, 0));
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    public void paint(Canvas canvas)
    {
        paint.setColor(RopeActivity.foregroundColor);
        canvas.drawCircle((float) (_position.getX() + _width / 2), (float) (_position.getY() + _width / 2), (float) (_width / 2), paint);
        paint.setColor(RopeActivity.backgroundColor);
        canvas.drawCircle((float) (_position.getX() + _width / 2), (float) (_position.getY() + _width / 2), (float) (_width / 2 - borderThickness), paint);

        paint.setColor(RopeActivity.foregroundColor);
        canvas.drawCircle((float) (_position.getX() + _width / 2), (float) (_position.getY() + _width / 2), (float) (_width / 2.6), paint);
        paint.setColor(RopeActivity.backgroundColor);
        canvas.drawCircle((float) (_position.getX() + _width / 2), (float) (_position.getY() + _width / 2), (float) (_width / 2.6 - borderThickness), paint);
    }
}