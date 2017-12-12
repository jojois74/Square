package box.shoe.gameutils;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.MainThread;
import android.support.annotation.RestrictTo;
import android.util.Log;

import box.gift.gameutils.R;

import static android.R.attr.width;
import static android.R.attr.x;
import static android.R.attr.y;

/**
 * Created by Joseph on 10/21/2017.
 *
 * A AbstractPaintable represents something which can be painted to a canvas.
 * A subclass provides an object which implements the Painter interface which supplies a paint method which draws the item to the canvas at (0, 0)
 * The subclass provides a registration point, which is the point at which the user expects their inputed coordinates to match up with on the item
 * The item is finally drawn with the registration point at the inputed coordinates
 */

public abstract class AbstractPaintable implements Cleanable, Paintable
{
    private int width;
    private int height;
    private boolean dimensionsChangedSinceLastPaint = false;

    //For ease
    protected Paint paint;
    protected int thisPartX;
    protected int thisPartY;
    protected int thisPartWidth;
    protected int thisPartHeight;

    //Do not construct new objects each cycle
    private Bitmap paintableBitmap;
    private Canvas paintableCanvas;

    /**
     * Subclasses should first call constructor, and then set the registration point based on scale (or image size), and set painter
     */
    protected AbstractPaintable(final int width, final int height)
    {
        L.d("Paintable creating " + width, "stop");
        this.width = width;
        this.height = height;
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paintableBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        paintableCanvas = new Canvas(paintableBitmap);
    }

    public final void paint(int x, int y, Canvas canvas) //Don't override this
    {
        if (dimensionsChangedSinceLastPaint)
        {
            // We do not always create a new canvas because most of the time it stays the same and we don't want to create unnecessary objects each frame.
            createLocalCanvas();
        }
        dimensionsChangedSinceLastPaint = false;
        //Draw the paintable on a new canvas, then copy it over to the original canvas
        paintableCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //Clear the temporary canvas
        paintableCanvas.save();
        blueprintPaint(width, height, paintableCanvas); //Paint at (0, 0)
        canvas.drawBitmap(paintableBitmap, new Rect(0, 0, width, height), new Rect(x, y, x + width, y + height), paint);
        paintableCanvas.restore();
    }

    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    protected abstract void blueprintPaint(int width, int height, Canvas canvas); //Override this

    //TODO: dimension changes should invoke new creation of bitmap etc perhaps only once when change is detected and not twice in a row when width and height are changed?
    public void setWidth(int newWidth)
    {
        dimensionsChangedSinceLastPaint = true;
        width = newWidth;
        paintableBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        paintableCanvas = new Canvas(paintableBitmap);
    }

    public void setHeight(int newHeight)
    {
        dimensionsChangedSinceLastPaint = true;
        height = newHeight;
    }

    private void createLocalCanvas()
    {
        if (paintableBitmap == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
        {
            paintableBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            paintableCanvas = new Canvas(paintableBitmap);
        }
        else
        {
            paintableBitmap.setWidth(width);
            paintableBitmap.setHeight(height);
            paintableCanvas.setBitmap(paintableBitmap);
        }
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    @SuppressLint("MissingSuperCall")
    public void cleanup()
    {
        L.d("Paintable cleaning up", "stop");
        paint = null;
        paintableCanvas = null;
        paintableBitmap = null;
    }
}
