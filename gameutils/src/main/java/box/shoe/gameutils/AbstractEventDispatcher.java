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
    private Map<String, List<Fireable>> listeners;

    public AbstractEventDispatcher()
    {
        listeners = new HashMap<>();
    }

    public void addEventListener(String eventType, Fireable fireable)
    {
        if (listeners.containsKey(eventType))
        {
            listeners.get(eventType).add(fireable);
        }
        else
        {
            List<Fireable> fireables = new ArrayList<>(); //Perhaps change to linkedlist if removal is implemented?
            fireables.add(fireable);
            listeners.put(eventType, fireables);
        }
    }

    /*
    public void removeEventListener(String eventType, Class this???); //TODO: not sure
    */

    protected void dispatchEvent(String eventType)
    {
        if (listeners.containsKey(eventType))
        {
            List<Fireable> fireables = listeners.get(eventType);
            for (Fireable fireable : fireables)
            {
                fireable.fire();
            }
        }
    }
}
