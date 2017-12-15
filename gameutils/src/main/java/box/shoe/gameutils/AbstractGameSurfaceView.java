package box.shoe.gameutils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.math.BigDecimal;

/**
 * Created by Joseph on 10/23/2017.
 */

public abstract class AbstractGameSurfaceView extends SurfaceView implements SurfaceHolder.Callback, SurfaceHolder.Callback2, Cleanable
{
    private SurfaceHolder holder;
    private volatile boolean surfaceReady = false;
    //private AbstractGameEngine abstractData = null;
    private Runnable dimensionListener;
    public Paint paint;
    private boolean preparedToVisualize = false;
    private Canvas canvas;

    public AbstractGameSurfaceView(Context context)
    {
        super(context);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        holder = getHolder();
        holder.addCallback(this);
    }
/*
    public void giveDataReference(AbstractGameEngine abstractData)
    {
        this.abstractData = abstractData;
    }
*/

    public void surfaceRedrawNeeded(SurfaceHolder surfaceHolder)
    {
        Log.w("Redraw needed", "Redraw needed called.");
    }

    public void prepareVisualize()
    {
        if (!surfaceReady)
        {
            throw new IllegalStateException("Surface is not ready to paint. Please call canVisualize() to check.");
        }
        canvas = holder.lockCanvas(null);
        preparedToVisualize = true;
    }

    public void visualize(@NonNull GameState interpolatedState)
    {
        if (!preparedToVisualize())
        {
            throw new IllegalStateException("Not prepared to visualize. Please call prepareVisualize() before calling visualize each time.");
        }
        if (!surfaceReady)
        {
            throw new IllegalStateException("Surface is not ready to paint. Please call canVisualize() to check.");
        }/*
        if (abstractData == null)
        {
            throw new IllegalStateException("Data has not been giving for painting. Please call giveDataReference(AbstractGameEngine) to supply it.");
        }*/
        // Clear the canvas
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        paint(canvas, interpolatedState);
        holder.unlockCanvasAndPost(canvas);
        preparedToVisualize = false;
    }

    public void unlockCanvasAndClear()
    {
        if (preparedToVisualize())
        {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    //protected abstract void paint(Canvas canvas, AbstractGameEngine abstractData, double interpolationRatio);
    protected abstract void paint(Canvas canvas, GameState interpolatedState);

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        surfaceReady = true;
    }

    public void setDimensionListener(Runnable r)
    {
        dimensionListener = r;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        if (width > 0 && dimensionListener != null)
        {
            L.d("Alert the dimension listener", "trace");
            dimensionListener.run();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        surfaceReady = false;
    }

    public boolean canVisualize()
    {
        return surfaceReady;
    }

    public boolean preparedToVisualize()
    {
        return preparedToVisualize;
    }

    @SuppressLint("MissingSuperCall")
    public void cleanup()
    {
        paint = null;
    }
}
