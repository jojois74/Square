package box.shoe.gameutils;

/**
 * Created by Joseph on 12/4/2017.
 *
 * This class will be certainly re-made, or removed entirely, or split, etc. etc. etc. when the module InterpolatableEntity system is remade
 */

public class EntityCollisions //TODO: Needs a rename!!! //TODO: maybe all these variables are inefficient? >>:)))
{
    public static boolean entityEntity(Entity a, Entity b)
    {
        // Entity a
        double minXA = a.position.getX() - a.registration.getX();
        double maxXA = minXA + a.width;
        double minYA = a.position.getY() - a.registration.getY();
        double maxYA = minYA + a.height;

        // Entity b
        double minXB = b.position.getX() - b.registration.getX();
        double maxXB = minXB + b.width;
        double minYB = b.position.getY() - b.registration.getY();
        double maxYB = minYB + b.height;

        return (
                minXA < maxXB &&
                maxXA > minXB &&
                minYA < maxYB &&
                maxYA > minYB
        );
    }

    //TODO: below here is untested. (Probably doesn't work)
    public static boolean pointEntity(double x, double y, Entity a)
    {
        // Entity a
        double minXA = a.position.getX() - a.registration.getX();
        double maxXA = minXA + a.width;
        double minYA = a.position.getY() - a.registration.getY();
        double maxYA = minYA + a.height;

        return (
                minXA < x &&
                maxXA > x &&
                minYA < y &&
                maxYA > y
        );
    }

    public static boolean circleSquare(double x, double y, double r, Entity a)
    {
        // Entity a
        double centerXA = a.position.getX() - a.registration.getX() + a.width / 2;
        double centerYA = a.position.getY() - a.registration.getY() + a.height / 2;
        double sqRadiusA = sqDistance(a.width / 2, a.height / 2, a.width, a.height);

        double sqDist = sqDistance(centerXA, centerYA, x, y);
        if (sqDist > sqRadiusA + r) return false;
        if (sqDist < a.width + r) return true;

        Vector toRadius = new Vector(x - centerXA, y - centerYA).unit().scale(r);

        return pointEntity(toRadius.getX(), toRadius.getY(), a);
    }

    private static double sqDistance(double x1, double y1, double x2, double y2)
    {
        return Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2);
    }
    private static double distance(double x1, double y1, double x2, double y2)
    {
        return Math.sqrt(sqDistance(x1, y1, x2, y2));
    }
}
