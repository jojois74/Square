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
    public double interpolatedY;

    // Whether or not this has been interpolated by the Game Engine and therefore should be drawn.
    // It is possible that an InterpolatableEntity exists in a GameState but has not been interpolated (e.g. it did not exist in the old GameState).
    // This means that InterpolatableEntities that exist for exactly one update cycle may
    // never be drawn (this makes sense, because such an object cannot have time to move, so we should not be using interpolation on it anyway).
    public boolean interpolatedThisFrame = false;

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
