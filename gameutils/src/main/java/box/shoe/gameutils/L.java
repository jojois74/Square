package box.shoe.gameutils;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Joseph on 12/11/2017.
 */

public class L
{
    public static boolean LOG = false;
    private static HashMap<String, Boolean> logChannels = new HashMap<>();

    public static void d(Object msg, String channel)
    {
        if (!logChannels.containsKey(channel))
        {
            logChannels.put(channel, true);
        }
        if (LOG && logChannels.get(channel))
        {
            Log.d(channel, msg.toString());
        }
    }

    public static void w(Object msg, String channel)
    {
        if (!logChannels.containsKey(channel))
        {
            logChannels.put(channel, true);
        }
        if (LOG && logChannels.get(channel))
        {
            Log.w(channel, msg.toString());
        }
    }

    public static void i(Object msg, String channel)
    {
        if (!logChannels.containsKey(channel))
        {
            logChannels.put(channel, true);
        }
        if (LOG && logChannels.get(channel))
        {
            Log.i(channel, msg.toString());
        }
    }

    public static void disableChannel(String channel)
    {
        logChannels.put(channel, false);
    }
}
