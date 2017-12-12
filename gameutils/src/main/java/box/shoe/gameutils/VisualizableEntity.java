package box.shoe.gameutils;

import android.graphics.Canvas;
import android.graphics.Point;
import android.support.annotation.CallSuper;

/**
 * Created by Joseph on 12/9/2017.
 */

public class VisualizableEntity extends InterpolatableEntity implements Cleanable
{
    private Paintable paintable;

    public VisualizableEntity(double initialX, double initialY, Paintable paintable)
    {
        super(initialX, initialY);
        this.paintable = paintable;
    }

    public VisualizableEntity(double initialX, double initialY, double initialWidth, double initialHeight, Paintable paintable) //TODO: Perhaps pass in actual visual object not class type? ponder implications
    {
        super(initialX, initialY, initialWidth, initialHeight);
        this.paintable = paintable;
    }

    public VisualizableEntity(double initialX, double initialY, double initialWidth, double initialHeight, Vector initialVelocity, Paintable paintable)
    {
        super(initialX, initialY, initialWidth, initialHeight, initialVelocity);
        this.paintable = paintable;
    }
    public VisualizableEntity(double initialX, double initialY, double initialWidth, double initialHeight, Vector initialVelocity, Vector initialAcceleration, Paintable paintable)
    {
        super(initialX, initialY, initialWidth, initialHeight, initialVelocity, initialAcceleration);
        this.paintable = paintable;
    }

    @CallSuper
    public void paint(Canvas canvas)
    {
        // Display this Entity by painting the paintable at current interpolated x, y coordinates, offset by the registration point.
        L.d(paintable.getClass().getName(), "stop");
        paintable.paint(toCoord(interpolatedX - registrationPoint.x), toCoord(interpolatedY - registrationPoint.y), canvas);

        // And then (or before) can paint any other thing relevant to this entity in subclass (call super at some point!)
    }

    private int toCoord(double value)
    {
        return (int) Math.round(value); //TODO: slow? also, is round needed? is floor better? does it matter?
    }

    public int getDisplayWidth()
    {
        return paintable.getWidth();
    }

    public int getDisplayHeight()
    {
        return paintable.getHeight();
    }

    public VisualizableEntity setDisplayWidth(int newWidth)
    {
        paintable.setWidth(newWidth);
        return this;
    }

    public VisualizableEntity setDisplayHeight(int newHeight)
    {
        paintable.setHeight(newHeight);
        return this;
    }

    @Override
    public void cleanup()
    {
        super.cleanup();
        paintable.cleanup();
        paintable = null;
    }
}
