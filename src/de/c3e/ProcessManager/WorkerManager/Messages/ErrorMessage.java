package de.c3e.ProcessManager.WorkerManager.Messages;

import de.c3e.ProcessManager.WorkerManager.ErrorStatus;

import java.io.Serializable;

public class ErrorMessage  extends MessageBase implements Serializable
{
    private final ErrorStatus error;

    public ErrorMessage(ErrorStatus error)
    {
        this.error = error;
    }

    public ErrorStatus getError()
    {
        return error;
    }
}
