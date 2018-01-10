package box.shoe.gameutils;

/**
 * Created by Joseph on 12/21/2017.
 * A class is Interpolatable if it is usable by the interpolation service.
 * The interpolation service creates an instance of a class which
 * represents a fractional state in between two other instances.
 */

public interface Interpolatable<T>
{
    /**
     * Return an instance which
     * a) returns true when equals() is called on it.
     * b) will not be modified.
     * This instance need not be a true clone, as long as the implicit
     * parameter is guaranteed to never be modified (e.g. T is an immutable class).
     * @return the instance.
     */
    T copy();

    /**
     * Return an instance which represents a fractional, in between state
     * between the current state and the other instance's state,
     * where the interpolationRatio acts as the arbiter on where to draw
     * the line between the two states.
     * @param other the instance to interpolate to.
     * @param interpolationRatio the ratio at which the returned instance lies,
     *                           between the two states.
     * @return the interpolated instance, which you guarantee to never modify.
     */
    T interpolateTo(T other, double interpolationRatio);
}
