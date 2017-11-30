package box.shoe.gameutils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;

/**
 * Created by Joseph on 10/23/2017.
 */

public class ImagePaintable extends AbstractPaintable
{
    private Point setAsRegPoint = null;
    private Bitmap sprite;

    /**
     * Convenient one-bitmap-only-paintable (it is useful to use a paintable for this application rather than simply drawing the bitmap directly if you ant to set a registration point other than the top left)
     */
    public ImagePaintable(final int imgId, final Resources res, final Point registrationPoint)
    {
        super();
        double density = res.getDisplayMetrics().density;
        sprite = BitmapFactory.decodeResource(res, imgId);
        int width = (int) (sprite.getWidth() / density);
        int height = (int) (sprite.getHeight() / density);
        setAsRegPoint = registrationPoint;
        setDimensions(width, height);
    }

    public ImagePaintable(final int imgId, final Resources res, final double registrationPointXPercent, final double registrationPointYPercent)
    {
        super();
        double density = res.getDisplayMetrics().density;
        sprite = BitmapFactory.decodeResource(res, imgId);
        int width = (int) (sprite.getWidth() / density);
        int height = (int) (sprite.getHeight() / density);
        setAsRegPoint = new Point((int) (registrationPointXPercent * width), (int) (registrationPointYPercent * height));
        setDimensions(width, height);
    }

    @Override
    protected void blueprintPaint(int width, int height, Canvas canvas)
    {
        canvas.drawBitmap(sprite, null, new Rect(0, 0, width, height), null);
    }

    @Override
    protected void recalculateRegistrationPoint(int width, int height)
    {
        setRegistrationPoint(setAsRegPoint);
    }
}
