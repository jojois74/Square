package box.shoe.gameutils;

import android.annotation.SuppressLint;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Joseph on 10/26/2017.
 */

public class TaskScheduler implements Cleanable
{
    // Updates per millisecond of the engine that calls tick().
    // Used to calculate approx how many ticks for a given time.
    private final double UPMS;

    // The scheduled tasks.
    private Set<Task> tasks;

    // Temporary buffer of tasks that are to be scheduled.
    // We do not always schedule tasks right away, in case we are currently using the tasks,
    // So we avoid a Concurrent modification Exception by waiting until the appropriate time.
    // Contract: tasks are scheduled before they are due to be fired.
    private Set<Task> tasksBuffer;

    /**
     * Create a TaskScheduler.
     * @param UPS the updates per second o the game engine that will call tick().
     */
    public TaskScheduler(int UPS)
    {
        this.UPMS = UPS / 1000.0;
        tasks = new HashSet<>();
        tasksBuffer = new HashSet<>();
    }

    public void schedule(int ms, int repetitions, Runnable schedulable) //0 for repetitions means eternal, ms accurate to within about 10ms if tick() is called on an AbstractGameEngine update
    {
        // We add new tasks to a buffer, because we won't schedule them for real until
        // we know it is safe to do so (the scheduled tasks are not being accessed some other way).
        // If we scheduled them to the real list now, then, e.g., if the firing and removal of
        // one task scheduled another while the tasks were still ticking, a CoMoEx would be thrown.
        tasksBuffer.add(new Task((int) Math.round(UPMS * ms), repetitions, schedulable));
    }

    public synchronized void cancelAll()
    {
        tasks.clear();
        tasksBuffer.clear();
    }

    public synchronized void tick()
    {
        // Now we can safely schedule the tasks from the buffer.
        tasks.addAll(tasksBuffer);
        tasksBuffer.clear();

        // Iterate over all of the tasks and tock them. Then remove the ones which have
        // exhausted their number of firings.
        Iterator<Task> iterator = tasks.iterator();
        while (iterator.hasNext())
        {
            boolean taskExhausted = iterator.next().tock();
            if (taskExhausted)
            {
                iterator.remove();
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void cleanup()
    {
        for (Task task : tasks)
        {
            task.cleanup();
        }
        tasks.clear();
        tasks = null;
    }

    //TODO: hash code?
    private class Task implements Cleanable
    {
        private int maxFrames;
        private int currentFrame;
        private int maxRepetitions;
        private int currentRepetition;
        private Runnable schedulable;

        private Task(int maxFrames, int repetitions, Runnable schedulable)
        {
            currentFrame = 0;
            this.maxFrames = maxFrames;
            currentRepetition = 0;
            this.maxRepetitions = repetitions;
            if (this.maxFrames == 0)
            {
                throw new IllegalArgumentException("This event is scheduled to occur every" +
                        "0 frames at this UPS. Events must be scheduled for the future.");
            }
            this.schedulable = schedulable;
        }

        private boolean tock() //Returns true if this event should be removed from the set (it should not repeat)
        {
            currentFrame++;
            if (currentFrame >= maxFrames) //If we have exhausted the delay
            {
                currentFrame = 0;
                schedulable.run();

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
            if (otherObject.getClass() != getClass())
                return false;
            Task otherTask = (Task) otherObject;
            if (maxFrames == otherTask.maxFrames && schedulable.equals(otherTask.schedulable))
            {
                return true;
            }
            return false;
        }

        @SuppressLint("MissingSuperCall")
        @Override
        public void cleanup()
        {
            schedulable = null;
        }
    }
}
