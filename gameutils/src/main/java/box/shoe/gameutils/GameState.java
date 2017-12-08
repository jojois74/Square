package box.shoe.gameutils;

import android.util.Log;

import com.rits.cloning.Cloner;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Created by Joseph on 12/7/2017.
 */

public class GameState
{
    private Map<String, Object> data;
    private LinkedList<Entity> interpolatableEntities;
    public long timeStamp;

    public GameState()
    {
        data = new HashMap<>();
    }

    public <T> T getData(String key)
    {
        Object value = data.get(key);
        if (value == null)
        {
            return null;
        }
        return (T) value.getClass().cast(value);
    }

    public void saveData(String key, Object obj)
    {
        data.put(key, obj);
        /*
        if ((obj instanceof Entity) && ((Entity) obj).usesInterpolation())
        {
            saveInterpolatableEntity(obj);
        }
        */
    }
/*
    public void saveInterpolatableEntity(Object obj)
    {
        if (!(obj instanceof Entity) || !((Entity) obj).usesInterpolation())
        {
            throw new IllegalArgumentException("Must be interpolatable Entity");
        }
        interpolatableEntities.add((Entity) obj);
    }
*/
    public Map<String, Object> getDataMap()
    {
        return data;
    }

    public LinkedList<Entity> getInterpolatableEntities()
    {
        return interpolatableEntities;
    }

/*
    //TODO: save entire collection and reclaim it later
    //TODO: or find some other way to save entities that are stored among other objects
    public void saveEntityCollection(String key, Collection<Entity> collection)
    {
        for (Entity entity : collection)
        {
            saveEntity(key, entity);
        }
    }
    */
    public Set<Map.Entry<String, Object>> dataEntrySet()
    {
        return data.entrySet();
    }
}
