package box.shoe.gameutils;

/**
 * Created by Joseph on 12/4/2017.
 *
 * This class will be certainly re-made, or removed entirely, or split, etc. etc. etc. when the module InterpolatableEntity system is remade
 */

public class EntityCollisions //TODO: Needs a rename!!! //TODO: maybe all these variables are inefficient? >>:)))
{
    // TODO: could implement this as collideRectangle if ok with creating a new Entity
    // TODO: should take a Screen, not width and height
    // TODO: so maybe not useful if we are spawning entities offscreen to begin with to move onto the screen.
    public static boolean isOffscreen(Entity a, int screenWidth, int screenHeight)
    {
        // Entity a
        double minXA = a.position.getX() - a.registrationPoint.x;
        double maxXA = minXA + a.width;
        double minYA = a.position.getY() - a.registrationPoint.y;
        double maxYA = minYA + a.height;

        // Screen
        double minXB = 0;
        double maxXB = screenWidth;
        double minYB = 0;
        double maxYB = screenHeight;

        return (
                minXA < maxXB &&
                maxXA > minXB &&
                minYA < maxYB &&
                maxYA > minYB
        );
    }

    public static boolean collideRectangle(Entity a, Entity b)
    {
        // Entity a
        double minXA = a.position.getX() - a.registrationPoint.x;
        double maxXA = minXA + a.width;
        double minYA = a.position.getY() - a.registrationPoint.y;
        double maxYA = minYA + a.height;

        // Entity b
        double minXB = b.position.getX() - b.registrationPoint.x;
        double maxXB = minXB + b.width;
        double minYB = b.position.getY() - b.registrationPoint.y;
        double maxYB = minYB + b.height;

        return (
                minXA < maxXB &&
                maxXA > minXB &&
                minYA < maxYB &&
                maxYA > minYB
        );
    }
}
