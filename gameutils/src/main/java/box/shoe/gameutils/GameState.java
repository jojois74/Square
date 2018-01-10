package box.shoe.gameutils;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Created by Joseph on 12/31/2017.
 */

public class GameState implements Cleanable
{
    private long timeStamp;
    private Map<String, Object> data;
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

    @SuppressLint("MissingSuperCall")
    @Override
    public void cleanup()
    {
        data = null;

        for (InterpolatablesCarrier interpolatablesCarrier : interps.values())
        {
            interpolatablesCarrier.cleanup();
        }
        interps = null;
    }
}
