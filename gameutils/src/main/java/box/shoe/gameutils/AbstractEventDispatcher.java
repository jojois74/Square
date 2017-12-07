package box.shoe.gameutils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Joseph on 11/30/2017.
 */

public abstract class AbstractEventDispatcher
{
    private Map<String, List<Runnable>> listeners;

    public AbstractEventDispatcher()
    {
        listeners = new HashMap<>();
    }

    public void addEventListener(String eventType, Runnable runnable)
    {
        if (listeners.containsKey(eventType))
        {
            listeners.get(eventType).add(runnable);
        }
        else
        {
            List<Runnable> runnables = new ArrayList<>(); //Perhaps change to LinkedList if removal is implemented?
            runnables.add(runnable);
            listeners.put(eventType, runnables);
        }
    }

    /*
    public void removeEventListener(String eventType, Class this???); //TODO: not sure
    */

    protected void dispatchEvent(String eventType)
    {
        if (listeners.containsKey(eventType))
        {
            List<Runnable> runnables = listeners.get(eventType);
            for (Runnable runnable : runnables)
            {
                runnable.run();
            }
        }
    }
}
