package de.c3e.ProcessManager.WorkerManager.Messages;

import de.c3e.ProcessManager.DataTypes.ResultGraphPosition;
import de.c3e.ProcessManager.Utils.ExecutionTimeMeasurementEntry;
import de.c3e.ProcessManager.Utils.ExecutionTimeMeasurements;

import java.util.List;

/**
 * Reply with the requested statistics
 */
public class SendCompleteStatisticMessage extends MessageBase
{
    private final ExecutionTimeMeasurements statistics;

    public SendCompleteStatisticMessage(ExecutionTimeMeasurements statisticsToSend)
    {
        this.statistics = statisticsToSend;
    }

    public ExecutionTimeMeasurements getStatistics()
    {
        return statistics;
    }
}

