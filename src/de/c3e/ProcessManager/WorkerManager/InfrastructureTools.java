package de.c3e.ProcessManager.WorkerManager;

import de.c3e.ProcessManager.Utils.AbstractImageObject;
import de.c3e.ProcessManager.Utils.AbstractTableObject;

/**
 * Helper class
 * todo: get rid of it, this is just on more thing to maintain.
 */
public class InfrastructureTools
{
    public static boolean IsPartialType(Object value)
    {
        return value instanceof AbstractImageObject  || value instanceof AbstractTableObject;
    }


}
