package box.shoe.gameutils;

import android.annotation.SuppressLint;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by Joseph on 12/31/2017.
 */

public class InterpolatablesCarrier
{
    private LinkedList<Object> interps;

    public InterpolatablesCarrier()
    {
        interps = new LinkedList<>();
    }

    public void provide(Object obj)
    {
        if (obj.getClass().equals(Double.class))
        {
            interps.addLast(obj);
        }
        else if (obj instanceof Interpolatable)
        {
            interps.addLast(((Interpolatable) obj).copy());
        }
        else
        {
            throw new IllegalArgumentException("Provided object must be a double or of type Interpolatable!");
        }
    }

    public <T> T recall()
    {
        return (T) interps.remove(0);
    }

    private int size()
    {
        return interps.size();
    }

    /*pack*/ InterpolatablesCarrier interpolateTo(InterpolatablesCarrier other, double interpolationRatio)
    {
        // First, check size compatibility
        if (size() != other.size())
        {
            throw new IllegalStateException("InterpolatablesCarrier not compatible.");
        }

        InterpolatablesCarrier toReturn = new InterpolatablesCarrier();

        Iterator<Object> iterator = interps.iterator();
        Iterator<Object> otherIterator = other.interps.iterator();
        while (iterator.hasNext())
        {
            Object obj = iterator.next();
            Object otherObj = otherIterator.next();

            if (!obj.getClass().equals(otherObj.getClass()))
            {
                throw new IllegalStateException("InterpolatablesCarrier not compatible.");
            }
            else
            {
                if (obj.getClass().equals(Double.class))
                {
                    double iObj = (double) obj;
                    double iOtherObj = (double) otherObj;
                    double i = iObj * (1 - interpolationRatio) + iOtherObj * interpolationRatio;
                    toReturn.provide(i);
                }
                else if (obj instanceof Interpolatable)
                {
                    Interpolatable iObj = (Interpolatable) obj;
                    Interpolatable iOtherObj = (Interpolatable) otherObj;
                    toReturn.provide(iObj.interpolateTo(iOtherObj, interpolationRatio));
                }
            }
        }

        return toReturn;
    }

    /*pack*/ boolean isEmpty()
    {
        // For some reason this checks for size() == 0 (according to docs).
        // That is O(n), while isEmpty should be an O(1) check.
        // Remedy: scream at top of lungs!
        return interps.isEmpty();
    }
}
