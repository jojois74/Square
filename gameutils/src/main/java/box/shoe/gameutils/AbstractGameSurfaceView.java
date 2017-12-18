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

public abstract class AbstractGameSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Cleanable
{
    private SurfaceHolder holder;
    private volatile boolean surfaceReady = false;
    public Paint paint;
    private boolean preparedToVisualize = false;
    private Canvas canvas;

    private boolean hasDimensions = false;
    private Runnable surfaceChangedListener;

    public AbstractGameSurfaceView(Context context, Runnable surfaceChangedListener)
    {
        super(context);
        this.surfaceChangedListener = surfaceChangedListener;
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
        preparedToVisualize = false;
        holder.unlockCanvasAndPost(canvas);
    }

    public void unlockCanvasAndClear()
    {
        L.d("unlock canvas and clear called", "clear");
        if (!preparedToVisualize())
        {
            throw new IllegalStateException("Not prepared to visualize. Please call prepareVisualize() before calling visualize each time.");
        }
        if (!surfaceReady)
        {
            throw new IllegalStateException("Surface is not ready to paint. Please call canVisualize() to check.");
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        preparedToVisualize = false;
        holder.unlockCanvasAndPost(canvas);
    }

    //protected abstract void paint(Canvas canvas, AbstractGameEngine abstractData, double interpolationRatio);
    protected abstract void paint(Canvas canvas, GameState interpolatedState);

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        surfaceReady = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        if (width > 0 && height > 0)
        {
            hasDimensions = true;
            if (surfaceChangedListener != null)
            {
                surfaceChangedListener.run();
            }
        }
    }

    public void unregisterSurfaceChangedListener() //Irreversable
    {
        surfaceChangedListener = null;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        surfaceReady = false;
    }

    public boolean canVisualize()
    {
        return surfaceReady && hasDimensions;
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
