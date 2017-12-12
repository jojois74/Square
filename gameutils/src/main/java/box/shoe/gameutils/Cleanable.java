package box.shoe.gameutils;

import android.support.annotation.CallSuper;

/**
 * Created by Joseph on 12/7/2017.
 * Indicates a class that can be cleaned up.
 */

public interface Cleanable
{
    /**
     * Remove all references recursively.
     * Precondition: the object that will be cleaned up is no longer needed, and will no longer be used.
     * Postcondition: the object that has cleaned itself up will no longer hold any references to objects. (Those objects may still be referenced elsewhere, in which case the garbage collector will not reclaim them).
     */
    @CallSuper
    void cleanup();
}
