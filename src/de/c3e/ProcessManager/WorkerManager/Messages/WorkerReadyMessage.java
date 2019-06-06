package de.c3e.ProcessManager.WorkerManager.Messages;

import java.io.Serializable;

/**
 * Send by a worker in case his status changes.
 */
public class WorkerReadyMessage  extends MessageBase implements Serializable
{
    private final boolean isReady;

    public WorkerReadyMessage(boolean isReady)
    {
        this.isReady = isReady;
    }

    public boolean isReady()
    {
        return isReady;
    }
}


