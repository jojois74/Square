package box.shoe.gameutils;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Created by Joseph on 12/31/2017.
 */

public class GameState
{
    // The time at which the update which generated this GameState occurred.
    private volatile long timeStamp;

    // All data necessary for painting of this GameState.
    private Map<String, Object> data;

    // Storage of all Entities along with their provided Interpolatables.
    // Used to interpolate values for the Entities at this GameState.
    /*pack*/ IdentityHashMap<Entity, InterpolatablesCarrier> interps;

    /*pack*/ GameState()
    {
        data = new HashMap<>();
        interps = new IdentityHashMap<>();
    }

    /*pack*/ void setTimeStamp(long timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    /*pack*/ long getTimeStamp()
    {
        return timeStamp;
    }

    public void put(String key, Object value)
    {
        data.put(key, value);
    }

    public <T> T get(String key)
    {
        return (T) data.get(key);
    }
}
