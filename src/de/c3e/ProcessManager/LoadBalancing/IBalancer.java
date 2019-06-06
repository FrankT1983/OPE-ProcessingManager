package de.c3e.ProcessManager.LoadBalancing;

import de.c3e.ProcessManager.DataTypes.GraphBlock;
import de.c3e.ProcessManager.Utils.AbstractImageObject;
import de.c3e.ProcessManager.Utils.ExecutionTimeMeasurements;
import de.c3e.ProcessManager.Utils.ImageSubSet;
import de.c3e.ProcessManager.WorkerManager.CalculationAllocation;
import de.c3e.ProcessManager.WorkerManager.IWorker;

import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Interface for a load balancer.
 */
public interface IBalancer
{
    void RegisterExecutionStatistics(Map<Integer, ExecutionTimeMeasurements> remoteExecutionStatistics);
    long RestrictDesiredPixelsByImageSize(long desiredPixelCount, long fullSizePixelCount);


    long EstimatePixelCountForTime(String workType, double timeInSeconds);

    /**
     * Persist the statistics of the balancer to file to reuse them in later runs.
     * @param filePath The path to the statistics file to write.
     */
    void SaveStatistics(String filePath);

    /**
     * Load statistics for the balancer from a file.
     * @param filePath The path to the statistics file to load.
     */
    void LoadStatistics(String filePath);

    Queue<CalculationAllocation> ScheduleBlockOnWorkers(GraphBlock block, Queue<IWorker> workers, long desiredBlockTime);
}


