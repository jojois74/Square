package box.shoe.gameutils;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.support.annotation.CallSuper;

/**
 * Created by Joseph on 12/9/2017.
 * A game object which holds a position and space on the screen and can move around.
 * Technically: a Game object with x and y coordinates, which can be fractional, width and height, which can be fractional (or 0 to indicate no space taken up) and Vector objects for velocity and acceleration.
 * Width and height are different from display-width and display-height.
 */

public class Entity implements Cleanable
{
    // X coordinate, representative of how far right this is on the screen.
    public double x;
    // Y coordinate, representative of how far up this is on the screen. //TODO: make this actually true, override canvas????? (or provide higher level abstraction for drawing...)
    public double y;

    // Width represents how much horizontal space this takes up.
    public double width;
    // Height represents how much vertical space this takes up.
    public double height;

    // Vector which represents how many x and y units this will move per update.
    public Vector velocity;
    // Vector which represents how many x and y units the velocity will change by per update.
    public Vector acceleration;

    // Point of origin from which all positioning of this object is calculated.
    public Point registrationPoint;

    /**
     * Creates an Entity with the specified x and y coordinates, with no width or height, with no velocity or acceleration.
     * @param initialX the starting x coordinate.
     * @param initialY the starting y coordinate.
     */
    public Entity(double initialX, double initialY)
    {
        this(initialX, initialY, 0, 0, Vector.ZERO, Vector.ZERO);
    }

    /**
     * Creates an Entity with the specified x and y coordinates, width and height, with no velocity or acceleration.
     * @param initialX the starting x coordinate.
     * @param initialY the starting y coordinate.
     * @param initialWidth the starting width.
     * @param initialHeight the starting height.
     */
    public Entity(double initialX, double initialY, double initialWidth, double initialHeight)
    {
        this(initialX, initialY, initialWidth, initialHeight, Vector.ZERO, Vector.ZERO);
    }

    /**
     * Creates an Entity with the specified x and y coordinates and velocity, with no acceleration.
     * @param initialX the starting x coordinate.
     * @param initialY the starting y coordinate.
     * @param initialWidth the starting width.
     * @param initialHeight the starting height.
     * @param initialVelocity the starting velocity.
     */
    public Entity(double initialX, double initialY, double initialWidth, double initialHeight, Vector initialVelocity)
    {
        this(initialX, initialY, initialWidth, initialHeight, initialVelocity, Vector.ZERO);
    }

    /**
     * Creates an Entity with the specified x and y coordinates, velocity and acceleration.
     * @param initialX the starting x coordinate.
     * @param initialY the starting y coordinate.
     * @param initialWidth the starting width.
     * @param initialHeight the starting height.
     * @param initialVelocity the starting velocity.
     * @param initialAcceleration the starting acceleration.
     */
    public Entity(double initialX, double initialY, double initialWidth, double initialHeight, Vector initialVelocity, Vector initialAcceleration)
    {
        x = initialX;
        y = initialY;
        width = initialWidth;
        height = initialHeight;
        velocity = initialVelocity;
        acceleration = initialAcceleration;
        registrationPoint = new Point(0, 0);
    }

    /**
     * Updates position based on current velocity, and then updates velocity based on current acceleration.
     */
    @CallSuper
    public void update()
    {
        // Update positions first based on current velocity
        x += velocity.getX();
        y += velocity.getY();

        // Update velocity based on current acceleration
        velocity = velocity.add(acceleration);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void cleanup()
    {
        velocity = null;
        acceleration = null;
    }
}
