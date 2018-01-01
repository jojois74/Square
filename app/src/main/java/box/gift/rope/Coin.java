package box.gift.rope;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import box.shoe.gameutils.Entity;
import box.shoe.gameutils.Paintable;
import box.shoe.gameutils.Vector;

/**
 * Created by Joseph on 12/21/2017.
 */

public class Coin extends Entity implements Paintable
{
    private Paint paint;

    public Coin(double initialX, double initialY, double initialWidth, double initialHeight)
    {
        super(initialX, initialY, initialWidth, initialHeight, new Vector(-10, 0));
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    public void paint(Canvas canvas)
    {
        paint.setColor(Color.parseColor("#e8c125"));
        canvas.drawCircle((float) (_position.getX() + width / 2), (float) (_position.getY() + width / 2), (float) width / 2, paint);
        paint.setColor(Color.YELLOW);
        canvas.drawCircle((float) (_position.getX() + width / 2), (float) (_position.getY() + width / 2), (float) width / 3, paint);
    }
}
