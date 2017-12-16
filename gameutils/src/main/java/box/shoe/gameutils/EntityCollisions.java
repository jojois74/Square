package box.shoe.gameutils;

/**
 * Created by Joseph on 12/4/2017.
 *
 * This class will be certainly re-made, or removed entirely, or split, etc. etc. etc. when the module InterpolatableEntity system is remade
 */

public class EntityCollisions
{
    public static boolean collideRectangle(Entity a, Entity b)
    {
        // Entity a
        double minXA = a.x - a.registrationPoint.x;
        double maxXA = minXA + a.width;
        double minYA = a.y - a.registrationPoint.y;
        double maxYA = minYA + a.height;

        // InterpolatableEntity b
        double minXB = b.x - b.registrationPoint.x;
        double maxXB = minXB + b.width;
        double minYB = b.y - b.registrationPoint.y;
        double maxYB = minYB + b.height;

        return (
                minXA < maxXB &&
                maxXA > minXB &&
                minYA < maxYB &&
                maxYA > minYB
        );
    }
}
