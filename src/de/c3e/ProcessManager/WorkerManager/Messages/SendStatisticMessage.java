package de.c3e.ProcessManager.WorkerManager.Messages;

import de.c3e.ProcessManager.Utils.ExecutionTimeMeasurementEntry;

public class SendStatisticMessage extends MessageBase
{
    private final ExecutionTimeMeasurementEntry statistics;

    public SendStatisticMessage(ExecutionTimeMeasurementEntry statisticEntryToSend)
    {
        this.statistics = statisticEntryToSend;
    }

    public ExecutionTimeMeasurementEntry getStatistics()
    {
        return statistics;
    }
}
