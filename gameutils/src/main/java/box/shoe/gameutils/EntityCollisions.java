package box.shoe.gameutils;

import box.shoe.gameutils.Entity;

/**
 * Created by Joseph on 12/4/2017.
 *
 * This class will be certainly re-made, or removed entirely, or split, etc. etc. etc. when the module Entity system is remade
 */

public class EntityCollisions
{
    //Should not use visual params, or at least respect reg point.
    //The Entity system is a big mess, redesign ASAP
    public static boolean collideRectangle(Entity a, Entity b)
    {
        // Entity a
        double minXA = a.getX();
        double maxXA = minXA + a.getVisualWidth();
        double minYA = a.getY();
        double maxYA = minYA + a.getVisualHeight();

        // Entity b
        double minXB = b.getX();
        double maxXB = minXB + b.getVisualWidth();
        double minYB = b.getY();
        double maxYB = minYB + b.getVisualHeight();

        return (
                minXA < maxXB &&
                maxXA > minXB &&
                minYA < maxYB &&
                maxYA > minYB
        );
    }
}
