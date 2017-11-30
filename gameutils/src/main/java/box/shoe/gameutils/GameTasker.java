package box.shoe.gameutils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Joseph on 10/26/2017.
 */

public class GameTasker
{
    private double UPMS; // Updates per millisecond
    private Set<Task> tasks;

    public GameTasker(int UPS)
    {
        this.UPMS = UPS / 1000.0;
        tasks = new HashSet<>();
    }

    public void register(int ms, int repetitions, Fireable schedulable) //0 for repetions means eternal, ms accurate to within about 10ms if tick() is called on an AbstractGameEngine update
    {
        tasks.add(new Task((int) (UPMS * ms), repetitions, schedulable));
    }

    public void cancelAll()
    {
        tasks.clear();
    }

    public void tick()
    {
        Iterator<Task> iterator = tasks.iterator();
        while (iterator.hasNext())
        {
            boolean removeEvent = iterator.next().tock();
            if (removeEvent)
                iterator.remove();
        }
    }

    private class Task
    {
        private int maxFrames;
        private int currentFrame;
        private int maxRepetitions;
        private int currentRepetition;
        private Fireable schedulable;

        private Task(int maxFrames, int repetitions, Fireable schedulable)
        {
            currentFrame = 0;
            this.maxFrames = maxFrames;
            currentRepetition = 0;
            this.maxRepetitions = repetitions;
            if (this.maxFrames == 0)
            {
                throw new IllegalArgumentException("This event is scheduled to occur every 0 frames at this UPS. Events must be scheduled for the future.");
            }
            this.schedulable = schedulable;
        }

        private boolean tock() //Returns true if this event should be removed from the set (it should not repeat)
        {
            currentFrame++;
            if (currentFrame >= maxFrames) //If we have exhausted the delay
            {
                currentFrame = 0;
                schedulable.fire();

                if (maxRepetitions == 0)
                    return false; //Eternally repeat

                currentRepetition++;
                if (currentRepetition >= maxRepetitions)
                    return true; //If we have reached the desired repetitions, remove
            }

            return false;
        }

        @Override
        public boolean equals(Object otherObject)
        {
            if (otherObject == null)
                return false;
            Task otherTask = (Task) otherObject;
            if (otherTask.getClass() != Task.class)
                return false;
            if (maxFrames == otherTask.maxFrames && schedulable == otherTask.schedulable)
            {
                return true;
            }
            return false;
        }
    }
}
