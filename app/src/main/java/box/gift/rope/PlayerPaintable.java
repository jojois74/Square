package box.gift.rope;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;

import box.shoe.gameutils.AbstractPaintable;

import static android.R.attr.x;
import static android.R.attr.y;


/**
 * Created by Joseph on 11/29/2017.
 */

public class PlayerPaintable extends AbstractPaintable
{
    private int degrees = 45;

    public PlayerPaintable(int width, int height)
    {
        super(width, height);
        setRegistrationPoint(width, height / 2);
    }

    @Override
    protected void blueprintPaint(int width, int height, Canvas canvas)
    {
        paint.setColor(Color.BLACK);
        //canvas.rotate(degrees, width / 2, height / 2);
        int x = 0;
        int y = 0;
        int w = width;
        int h = height;
        canvas.drawRect(x, y, x + w, y + h, paint);
        degrees = (degrees + 1) % 360;
    }
}
