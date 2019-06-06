package de.c3e.ProcessManager.WorkerManager;

import de.c3e.ProcessManager.DataTypes.ResultGraphPosition;
import de.c3e.ProcessManager.Utils.ImageSubSet;

/**
 * Created by Frank on 27.07.2016.
 */
public interface IRemoteObjectPart
{
    int getWorkerId();

    ResultGraphPosition getFullObjectId();

    Object getPartIdentification();
}
