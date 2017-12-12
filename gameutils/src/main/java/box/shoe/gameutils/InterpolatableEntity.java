package box.shoe.gameutils;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Point;

/**
 * Created by Joseph on 10/24/2017.
 * An Entity which is able (and should be if on) to be painted at a position interpolated from its last two positions.
 * Technically: no distinctions from Entity, yet... TODO:!!! if no good imp details found, perhaps use interface for interpolatable??? (unlikely)
 */
public class InterpolatableEntity extends Entity
{
    public double interpolatedX;
    public double interpolatedY; //TODO: necessary to set interp X and Y to initial values? I don't think so, because they are always set before painting.

    public InterpolatableEntity(double initialX, double initialY)
    {
        super(initialX, initialY);
    }

    public InterpolatableEntity(double initialX, double initialY, double initialWidth, double initialHeight)
    {
        super(initialX, initialY, initialWidth, initialHeight);
    }

    public InterpolatableEntity(double initialX, double initialY, double initialWidth, double initialHeight, Vector initialVelocity)
    {
        super(initialX, initialY, initialWidth, initialHeight, initialVelocity);
    }

    public InterpolatableEntity(double initialX, double initialY, double initialWidth, double initialHeight, Vector initialVelocity, Vector initialAcceleration)
    {
        super(initialX, initialY, initialWidth, initialHeight, initialVelocity, initialAcceleration);
    }
}
