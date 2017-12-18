package box.shoe.gameutils;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Joseph on 12/11/2017.
 * Logs debug output to the console on multiple channels.
 * Each channel can be disabled sperately to prevent output sent on them to be run.
 * In addition, all logging can be disabled.
 */
public class L
{
    // Determines whether or not we will send any output to the console
    public static boolean LOG = true;

    // Saves log channels along with their state of activity (enabled=true or disabled=false)
    private static HashMap<String, Boolean> logChannels = new HashMap<>();

    /**
     * Output a debug message to the console along a given channel.
     * @param msg the message to output. Any object can be given, and its toString method will be called.
     * @param channel the channel to send the output along.
     */
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

    /**
     * Disable a particular output channel so that its messages will not be sent to the console.
     * @param channel the channel to disable.
     */
    public static void disableChannel(String channel)
    {
        logChannels.put(channel, false);
    }
}
