package box.shoe.gameutils;

/**
 * Created by Joseph on 12/9/2017.
 */

public class InterpolatableState //TODO: if this class only needs to hold a position, replace with Vector and delete this class
{
    public Vector position;

    public InterpolatableState(Vector savePosition)
    {
        this.position = savePosition;
    }
}
