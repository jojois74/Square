package box.shoe.gameutils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.annotation.MainThread;
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

public abstract class AbstractPaintable
{
    private int width;
    private int height;

    private Point registrationPoint;

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
        this.width = width;
        this.height = height;
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paintableBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        paintableCanvas = new Canvas(paintableBitmap);
    }
    protected AbstractPaintable() //If using this method, call to setDimensions must be done before attempting to paint
    {//TODO disalow this constructor if not coming from an imagepaintable or subclass
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public final void paint(double x, double y, Canvas canvas) //Don't override this
    {
        int intX = (int) Math.round(x);
        int intY = (int) Math.round(y);
        if (registrationPoint == null)
        {
            throw new IllegalStateException("Registration point has not been set yet");
        }
        else
        {
            //Draw the paintable on a new canvas, then copy it over to the original canvas
            paintableCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //Clear the temporary canvas
            paintableCanvas.save();
            blueprintPaint(width, height, paintableCanvas); //Paint at (0, 0)
            canvas.drawBitmap(paintableBitmap, new Rect(0, 0, width, height), new Rect(intX - registrationPoint.x, intY - registrationPoint.y, intX - registrationPoint.x + width, intY - registrationPoint.y + height), paint); //Place the item i the appropriate spot on the canvas
            paintableCanvas.restore();
        }
    }

    protected abstract void blueprintPaint(int width, int height, Canvas canvas); //Override thise

    public void setWidth(int newWidth)
    {
        width = newWidth;
        paintableBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        paintableCanvas = new Canvas(paintableBitmap);
        recalculateRegistrationPoint(width, height);
    }

    public void setHeight(int newHeight)
    {
        height = newHeight;
        paintableBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        paintableCanvas = new Canvas(paintableBitmap);
        recalculateRegistrationPoint(width, height);
    }

    public void setDimensions(int newWidth, int newHeight)
    {
        width = newWidth;
        height = newHeight;
        paintableBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        paintableCanvas = new Canvas(paintableBitmap);
        recalculateRegistrationPoint(width, height);
    }

    protected void recalculateRegistrationPoint(int width, int height)
    {
        throw new UnsupportedOperationException("Subclass must override recalculate registration point to set registration point based on new width and height");
    }

    protected final void setRegistrationPoint(int x, int y)
    {
        registrationPoint = new Point(x, y);
    }

    protected final void setRegistrationPoint(Point point)
    {
        registrationPoint = point;
    }

    protected Point getRegistrationPoint()
    {
        return registrationPoint;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }
}
