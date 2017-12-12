package box.gift.rope;

import android.graphics.Canvas;
import android.graphics.Color;

import box.shoe.gameutils.AbstractPaintable;
import box.shoe.gameutils.Paintable;

/**
 * Created by Joseph on 11/29/2017.
 */

public class WallPaintable extends AbstractPaintable implements Paintable
{
    public WallPaintable(int width, int height)
    {
        super(width, height); //TODO: should be variable, based on screen width/height
    }

    @Override
    protected void blueprintPaint(int width, int height, Canvas canvas)
    {
        paint.setColor(Color.RED);
        canvas.drawRect(0, 0, width, height, paint);
    }
}
