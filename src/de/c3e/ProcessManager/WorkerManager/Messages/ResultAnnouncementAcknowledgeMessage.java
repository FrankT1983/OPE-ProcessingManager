package de.c3e.ProcessManager.WorkerManager.Messages;

/**
 * Created by Frank on 27.07.2016.
 */
public class ResultAnnouncementAcknowledgeMessage extends MessageBase
{
    private final int announcementId;

    public ResultAnnouncementAcknowledgeMessage(int announcementId)
    {
        this.announcementId = announcementId;
    }

    public int getAnnouncementId()
    {
        return announcementId;
    }
}
