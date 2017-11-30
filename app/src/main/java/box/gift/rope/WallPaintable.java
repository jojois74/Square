package box.gift.rope;

import android.graphics.Canvas;
import android.graphics.Color;

import box.shoe.gameutils.AbstractPaintable;

/**
 * Created by Joseph on 11/29/2017.
 */

public class WallPaintable extends AbstractPaintable
{
    public WallPaintable(int width, int height)
    {
        super(width, height);
        setRegistrationPoint(0, 0);
    }

    @Override
    protected void blueprintPaint(int width, int height, Canvas canvas)
    {
        paint.setColor(Color.RED);
        canvas.drawRect(0, 0, width, height, paint);
    }
}
