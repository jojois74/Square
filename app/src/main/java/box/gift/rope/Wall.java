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

public class Wall extends Entity implements Paintable
{
    private Paint paint;
    private boolean horiz;

    public Wall(double initialX, double initialY, double initialWidth, double initialHeight, boolean horiz)
    {
        super(initialX, initialY, initialWidth, initialHeight, new Vector(-21, 0));
        this.horiz = horiz;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
    }

    @Override
    public void paint(Canvas canvas)
    {
        paint.setColor(Color.parseColor("#60cde5"));
        if (horiz)
        {
            for (int i = 0; i < Math.round(_height); i++)
            {
                float y = (float) (_getY() + i);
                float h = 1;
                float w = RopeGame.random.intFrom(0, (int) Math.round(_width));
                float x = (float) (_getX() + ((_width - w) / 2));
                canvas.drawRect(x, y, x + w, y + h, paint);
            }
        }
        else
        {
            for (int i = 0; i < Math.round(_width); i++)
            {
                float h = RopeGame.random.intFrom(0, (int) Math.round(_height));
                float w = 1;
                float x = (float) (_getX() + i);
                float y = (float) (_getY() + ((_height - h) / 2));
                canvas.drawRect(x, y, x + w, y + h, paint);
            }
        }
    }

    @Override
    public void cleanup()
    {
        super.cleanup();
        paint = null;
    }
}
