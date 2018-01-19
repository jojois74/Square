package box.shoe.gameutils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by Joseph on 10/23/2017.
 */
//fixme: buffering just for screenshot is taking a lot more thread cpu time!
    //TODO: when game resumes, should not jump so much from previous frame!
public abstract class AbstractGameSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Screen
{
    private static final boolean DEBUG_SHOW_BOUNDING_BOXES = false; //TODO: allow to be set from public function
    private SurfaceHolder holder;
    private volatile boolean surfaceReady = false;
    private boolean preparedToVisualize = false;

    // Canvas returned from lockCanvas. Must be passed back to unlockCanvasAndPost.
    private Canvas surfaceCanvas;

    // We do not draw onto the surfaceCanvas directly.
    // We first draw to a buffer, which is great because now we can get a screenshot
    // whenever we want (from the bufferBitmap).
    private Canvas bufferCanvas;
    private Bitmap bufferBitmap;

    private boolean hasDimensions = false;
    private Runnable readyForPaintingListener;

    public AbstractGameSurfaceView(Context context, Runnable readyForPaintingListener)
    {
        super(context);
        setWillNotDraw(true);
        this.readyForPaintingListener = readyForPaintingListener;
        holder = getHolder();
        holder.addCallback(this);
    }
/*
    public void giveDataReference(AbstractGameEngine abstractData)
    {
        this.abstractData = abstractData;
    }
*/

    @Override
    public void preparePaint()
    {
        if (!surfaceReady)
        {
            throw new IllegalStateException("Surface is not ready to paint. Please call canVisualize() to check.");
        }
        surfaceCanvas = holder.lockCanvas();

        // Set coordinate origin to (0, 0) and make +x = right, +y = up.
        /*surfaceCanvas.translate(0, bufferCanvas.getHeight());
        surfaceCanvas.scale(1, -1);*/

        preparedToVisualize = true;
    }

    private void checkState()
    {
        if (!hasPreparedPaint())
        {
            throw new IllegalStateException("Not prepared to paintFrame. Please call preparePaint() before calling paintFrame each time.");
        }
        if (!surfaceReady)
        {
            throw new IllegalStateException("Surface is not ready to paint. Please call canVisualize() to check.");
        }
    }

    private void postCanvas()
    {
        surfaceCanvas.drawBitmap(bufferBitmap, 0, 0, null);
        preparedToVisualize = false;
        holder.unlockCanvasAndPost(surfaceCanvas);
    }

    @Override
    public void paintFrame(@NonNull GameState gameState)
    {
        checkState();

        // Clear the canvas
        bufferCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        paint(bufferCanvas, gameState);
        // Create debug artifacts, which follow the actual in-game positions, and box each Entity.
        if (AbstractGameSurfaceView.DEBUG_SHOW_BOUNDING_BOXES)
        {
            // We will draw all Entities, not just Paintables.
            for (Entity entity : gameState.interps.keySet())
            {
                bufferCanvas.drawRect((float) entity.position.getX(), (float) entity.position.getY(), (float) (entity.position.getX() + entity.width), (float) (entity.position.getY() + entity.height), new Paint(Paint.ANTI_ALIAS_FLAG));
            }
        }

        postCanvas();
    }

    @Override
    public void paintStatic(@NonNull Bitmap bitmap)
    {
        checkState();

        // Clear the canvas
        bufferCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // Draw the bitmap
        bufferCanvas.drawBitmap(bitmap, 0, 0,null);

        postCanvas();
    }

    @Override
    public void paintStatic(int color)
    {
        checkState();

        // Draw the color
        bufferCanvas.drawColor(color);

        postCanvas();
    }

    @Override
    public void unpreparePaint()
    {
        checkState();
        bufferCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        postCanvas();
    }

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
            if (bufferBitmap != null)
            {
                bufferBitmap.recycle();
            }
            bufferBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            bufferCanvas = new Canvas(bufferBitmap);

            hasDimensions = true;
            if (readyForPaintingListener != null)
            {
                readyForPaintingListener.run();
            }
        }
    }

    @Override
    public View asView()
    {
        return this;
    }

    public void unregisterReadyForPaintingListener() //Irreversable
    {
        readyForPaintingListener = null;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        surfaceReady = false;
    }

    @Override
    public boolean hasInitialized()
    {
        return surfaceReady && hasDimensions;
    }

    @Override
    public boolean hasPreparedPaint()
    {
        return preparedToVisualize;
    }

    public Bitmap getScreenshot()
    {
        return Bitmap.createBitmap(bufferBitmap);
    }

    @Override
    public void finalize()
    {
        bufferBitmap.recycle();
    }
}
