package box.shoe.gameutils;

import android.os.Parcel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Joseph on 12/31/2017.
 */

public class Interpolatables
{
    private LinkedList<Object> interps;

    public Interpolatables()
    {
        interps = new LinkedList<>();
    }

    public void provide(Object obj)
    {
        if (obj.getClass().equals(double.class) || obj.getClass().equals(Double.class)) //TODO: do I need to check primitive? isnt it autoboxed? rearange in order of likelihood.
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

    /*pack*/ Interpolatables interpolateTo(Interpolatables other, double interpolationRatio)
    {
        // First, check size compatibility
        if (size() != other.size())
        {
            throw new IllegalStateException("Interpolatables not compatible.");
        }

        Interpolatables toReturn = new Interpolatables();

        Iterator<Object> iterator = interps.iterator();
        Iterator<Object> otherIterator = other.interps.iterator();
        while (iterator.hasNext())
        {
            Object obj = iterator.next();
            Object otherObj = otherIterator.next();

            if (!obj.getClass().equals(otherObj.getClass()))
            {
                throw new IllegalStateException("Interpolatables not compatible.");
            }
            else
            {
                if (obj.getClass().equals(double.class) || obj.getClass().equals(Double.class)) //TODO: do I need to check primitive? isnt it autoboxed? rearange in order of likelihood.
                {
                    double iObj = (double) obj;
                    double iOtherObj = (double) otherObj;
                    double i = iOtherObj * (interpolationRatio + 1) - (interpolationRatio * iObj);
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
}
