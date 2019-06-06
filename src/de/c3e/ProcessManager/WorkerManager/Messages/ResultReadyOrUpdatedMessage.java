package de.c3e.ProcessManager.WorkerManager.Messages;

import de.c3e.ProcessManager.WorkerManager.IRemoteObject;

/**
 * Reports that a result is ready.
 */
public class ResultReadyOrUpdatedMessage extends MessageBase
{
    /**
     * The out port that produced this result.
     * Since currently every worker can only work on one block, the block does not need to be specified.
     */
    private final String port;

    /**
     * The actual value of the object.
     * This could be the actual object
     * or an something implementing IRemote object.
     */
    private final Object value ;

    /**
     * Each Result message can carry an announcement id.
     * This can be used by the maser node to acknowledge the message.
     */
    private final int announcementId;

    public ResultReadyOrUpdatedMessage(String outPort, Object resultObject, int announcementId)
    {
        this.port = outPort;
        this.value = resultObject;
        this.announcementId = announcementId;
    }

    public Object getObject()
    {
        return value;
    }

    public int getAnnouncementId()
    {
        return announcementId;
    }

    public String getPort()
    {
        return port;
    }
}
