package box.gift.rope;

import box.shoe.gameutils.AbstractPaintable;
import box.shoe.gameutils.Entity;
import box.shoe.gameutils.Vector;

/**
 * Created by Joseph on 11/29/2017.
 */

public class Player extends Entity
{
    public Vector actionVelocity = new Vector(0, -30);

    public Player(double initialX, double initialY, AbstractPaintable visual)
    {
        super(initialX, initialY, visual);
    }
}
