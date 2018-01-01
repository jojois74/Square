package box.shoe.gameutils;

/**
 * Created by Joseph on 1/1/2018.
 */

public interface Screen
{
    // Called before drawing begins, once the game, and thus the screen, has dimensions.
    void initialize();

    boolean isVisible(Paintable paintable);
    boolean isInbounds(Entity entity);
}
