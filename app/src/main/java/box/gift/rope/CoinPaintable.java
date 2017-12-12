package box.gift.rope;

import android.graphics.Canvas;
import android.graphics.Color;

import box.shoe.gameutils.AbstractPaintable;

/**
 * Created by Joseph on 11/30/2017.
 */

public class CoinPaintable extends AbstractPaintable
{
    public CoinPaintable(int width, int height)
    {
        super(width, height);
    }

    @Override
    protected void blueprintPaint(int width, int height, Canvas canvas)
    {
        paint.setColor(Color.parseColor("#e8c125"));
        canvas.drawCircle(width / 2, height / 2, width / 2, paint);
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(width / 2, height / 2, width / 3, paint);
    }
}
