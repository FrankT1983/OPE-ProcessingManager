package de.c3e.ProcessManager.WorkerManager.Messages;

import java.io.Serializable;

/**
 * Created by Frank on 26.07.2016.
 */
public abstract class MessageBase implements Serializable
{
    private int messageOriginId;

    public int getMessageOriginId()
    {
        return messageOriginId;
    }

    public void setOriginId(int originId)
    {
        this.messageOriginId = originId;
    }
}
