package box.shoe.gameutils;

/**
 * Created by Joseph on 12/21/2017.
 */

public interface Interpolatable<T>
{
    T copy();
    T interpolateTo(T other, double interpolationRatio);
}
