package box.shoe.gameutils;

import android.graphics.Point;
import android.util.Log;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Created by Joseph on 12/7/2017.
 */

public class GameState
{
    private Map<String, Object> data;
    private IdentityHashMap<InterpolatableEntity, InterpolatableState> interpolatableEntitiesMap;
    public long timeStamp;

    public GameState()
    {
        data = new HashMap<>();
        interpolatableEntitiesMap = new IdentityHashMap<>();
    }

    public void saveInterpolatableEntity(InterpolatableEntity interpolatableEntity)
    {
        interpolatableEntitiesMap.put(interpolatableEntity, new InterpolatableState(interpolatableEntity.x, interpolatableEntity.y));
    }

    protected IdentityHashMap getInterpolatableEntitiesMap()
    {
        return interpolatableEntitiesMap;
    }

    public Set<InterpolatableEntity> getInterpolatedEntities()
    {
        return interpolatableEntitiesMap.keySet();
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
    }
/*
    public void saveInterpolatableEntity(Object obj)
    {
        if (!(obj instanceof InterpolatableEntity) || !((InterpolatableEntity) obj).usesInterpolation())
        {
            throw new IllegalArgumentException("Must be interpolatable InterpolatableEntity");
        }
        interpolatableEntities.add((InterpolatableEntity) obj);
    }
*/
    public Map<String, Object> getDataMap()
    {
        return data;
    }

/*
    //TODO: save entire collection and reclaim it later
    //TODO: or find some other way to save entities that are stored among other objects
    public void saveEntityCollection(String key, Collection<InterpolatableEntity> collection)
    {
        for (InterpolatableEntity entity : collection)
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
