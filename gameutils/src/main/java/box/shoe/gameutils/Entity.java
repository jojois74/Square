package box.shoe.gameutils;

import android.annotation.SuppressLint;
import android.support.annotation.CallSuper;

import java.util.LinkedList;

/**
 * Created by Joseph on 12/9/2017.
 * A game object which holds a position and space on the screen and can move around.
 * Technically: a Game object with x and y coordinates, which can be fractional, width and height,
 * which can be fractional (or 0 to indicate no space taken up)
 * and Vector objectsfor velocity and acceleration.
 * Width and height are different from display-width and display-height.
 */
public class Entity //TODO: position should also be vector in constructors?
{ //TODO: extend Weaver?
    /*pack*/ static final LinkedList<Entity> ENTITIES = new LinkedList<>();

    // Width represents how much horizontal space this takes up.
    public double width;
    // Height represents how much vertical space this takes up.
    public double height;

    // Vector which represents where this is on the screen. Positive direction indicates how far right and down this is on the screen.
    // (In other words, displacement from the (top-left) origin in the x (rightward) and y (downward) directions.)
    public Vector position;
    // Vector which represents how many x and y units the position will change by per update.
    public Vector velocity;
    // Vector which represents how many x and y units the velocity will change by per update.
    public Vector acceleration;

    // Relatively from the position, where to find the point of origin from which all positioning of this object is calculated.
    public Vector registration; //TODO: make final somehow

    protected double _width;
    protected double _height;
    protected Vector _position;

    /**
     * Creates an Entity with the specified x and y coordinates,
     * with no width or height, with no velocity or acceleration.
     * @param initialX the starting x coordinate.
     * @param initialY the starting y coordinate.
     */
    public Entity(double initialX, double initialY)
    {
        this(initialX, initialY, 0, 0, Vector.ZERO, Vector.ZERO);
    }

    /**
     * Creates an Entity with the specified x and y coordinates,
     * width and height, with no velocity or acceleration.
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
        width = _width = initialWidth;
        height = _height = initialHeight;
        position = _position = new Vector(initialX, initialY); //TODO: we are setting the visual fields equal to the normal ones for display before the first couple frames (before it has been interpolated). the other option may be better -- do not paint entities until they can be interpolated ?
        velocity = initialVelocity;
        acceleration = initialAcceleration;
        registration = new Vector(0, 0);

        // Save this Entity for interpolation //TODO: figure out how to free entities when removed
        Entity.createEntity(this);
    }

    private static void createEntity(Entity entity)
    {
        ENTITIES.add(entity);
    }

    private static void destroyEntity(Entity entity)
    {
        //TODO: implementation here
        ENTITIES.remove(entity);
    }

    // Lay groundwork for Entity pool
    public Entity fromPool(double initialX, double initialY, double initialWidth, double initialHeight)
    {
        return fromPool(initialX, initialY, initialWidth, initialHeight, Vector.ZERO, Vector.ZERO);
    }
    public Entity fromPool(double initialX, double initialY, double initialWidth, double initialHeight, Vector initialVelocity)
    {
        return fromPool(initialX, initialY, initialWidth, initialHeight, initialVelocity, Vector.ZERO);
    }
    public Entity fromPool(double initialX, double initialY, double initialWidth, double initialHeight, Vector initialVelocity, Vector initialAcceleration)
    {
        return null;
    }

    /**
     * Updates velocity based on current acceleration, and then updates position based on new velocity.
     * We do not need to multiply by dt because every timestep is of equal length.
     */
    @CallSuper
    public void update()
    {
        // We will update velocity based on acceleration first,
        // and update position based on velocity second.
        // This is apparently called Semi-Implicit Euler and is a more accurate form of integration
        // when acceleration is not constant.
        // More importantly though, we do it because we subscribe to the holy faith
        // of Gaffer On Games, whose recommendations are infallible.

        // Update velocity first based on current acceleration.
        velocity = velocity.add(acceleration);

        // Update position based on new velocity.
        position = position.add(velocity);
    }

    /**
     * Convenience accessor function for returning the x position.
     * (Contract: getX() == position.getX())
     * @return the x position.
     */
    public double getX()
    {
        return position.getX();
    }

    /**
     * Convenience accessor function for returning the y position.
     * (Contract: getY() == position.getY())
     * @return the y position.
     */
    public double getY()
    {
        return position.getY();
    }

    /**
     * Convenience accessor function for returning the interpolated x position.
     * (Contract: _getX() == _position.getX())
     * @return the x position.
     */
    public double _getX()
    {
        return _position.getX();
    }

    /**
     * Convenience accessor function for returning the interpolated y position.
     * (Contract: _getY() == _position.getY())
     * @return the y position.
     */
    public double _getY()
    {
        return _position.getY();
    }

    @CallSuper
    protected void provideInterpolatables(InterpolatablesCarrier in)
    {
        in.provide(width);
        in.provide(height);
        in.provide(position);
    }

    @CallSuper
    protected void recallInterpolatables(InterpolatablesCarrier out)
    {
        _width = out.recall();
        _height = out.recall();
        _position = out.recall();
    }
}
