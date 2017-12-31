package box.shoe.gameutils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Joseph on 12/31/2017.
 */

public class GameState
{
    private long timeStamp;
    private Map<String, Object> data;

    /*pack*/ GameState()
    {
        data = new HashMap<>();
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
