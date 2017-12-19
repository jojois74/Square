package box.shoe.gameutils;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.util.ArraySet;
import android.util.Log;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Joseph on 12/7/2017.
 */
//TODO: we do not want to paint entities in their first update unless we have an intelligent place to put them (interpolate based on velocity?)
    //TODO: but we do want to paint entities even if they are removed next update
        //TODO: some of these functions should be renamed
            //TODO: perhaps the GameState system is due for a tiny rewrite in general
                //TODO: GameState should be renamed in any case to something like SaveState (but that isn't good either)
public class GameState implements Cleanable
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
        interpolatableEntitiesMap.put(interpolatableEntity, new InterpolatableState(interpolatableEntity.position));
    }

    public void saveInterpolatableEntity(String key, InterpolatableEntity interpolatableEntity)
    {
        interpolatableEntitiesMap.put(interpolatableEntity, new InterpolatableState(interpolatableEntity.position));
        saveData(key, interpolatableEntity);
    }

    protected IdentityHashMap<InterpolatableEntity, InterpolatableState> getInterpolatableEntitiesMap()
    {
        return interpolatableEntitiesMap;
    }

    public List<InterpolatableEntity> getInterpolatedEntities()
    {
        // Retrieve the keys from the map
        Set<InterpolatableEntity> interpolatableEntities = getInterpolatableEntitiesMap().keySet();

        // Store the elements we want in a List
        ArrayList<InterpolatableEntity> interpolatedEntities = new ArrayList<>(interpolatableEntities.size());

        // Iterate over the keys
        Iterator<InterpolatableEntity> iterator = interpolatableEntities.iterator();
        while (iterator.hasNext())
        {
            InterpolatableEntity interpolatableEntity = iterator.next();
            if (interpolatableEntity.interpolatedThisFrame)
            {
                interpolatedEntities.add(interpolatableEntity);
            }
        }

        // This list will not be added to any more, so save al the memory we can
        interpolatedEntities.trimToSize();
        return interpolatedEntities;
    }

    public <T> T getData(String key)
    {
        return (T) data.get(key);
    }

    public void saveData(String key, Object obj)
    {
        data.put(key, obj);
    }

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

    @SuppressLint("MissingSuperCall")
    @Override
    public void cleanup()
    {
        data.clear();
        data = null;
        //TODO: cleanup all elements? or not, since they are references to entities in use by the engine!

        interpolatableEntitiesMap.clear();
        interpolatableEntitiesMap = null;
        //TODO: cleanup all elements?  or not, since they are references to entities in use by the engine!
    }
}
