package box.gift.rope;

import box.shoe.gameutils.Paintable;
import box.shoe.gameutils.Vector;
import box.shoe.gameutils.VisualizableEntity;

/**
 * Created by Joseph on 11/29/2017.
 */

public class Player extends VisualizableEntity
{
    public Vector actionVelocity = new Vector(0, -30);

    public Player(double initialX, double initialY)
    {
        super(initialX, initialY, 60, 60, Vector.ZERO, new Vector(0, 2), new PlayerPaintable());
    }
}
