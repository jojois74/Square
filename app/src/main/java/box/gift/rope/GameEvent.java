package box.gift.rope;

import java.util.EventObject;

/**
 * Created by Joseph on 11/30/2017.
 */

public class GameEvent extends EventObject
{
    public static final String GAME_OVER = "game over";

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public GameEvent(Object source)
    {
        super(source);
    }
}
