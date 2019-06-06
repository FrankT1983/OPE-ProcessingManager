package de.c3e.ProcessManager.WorkerManager;

import de.c3e.ProcessingManager.Interfaces.IWorkBlock;
import de.c3e.ProcessingManager.Types.SplitType;

import java.util.Map;

/**
 * Interface for a worker.
 * A worker is a class that will execute a given piece of work on given .
 */
public interface IWorker
{
    /**
     * Is this worker currently busy running some work.
     * @return
     */
    boolean isBusy();

    /**
     * Check if this worker finished his work.
     * @return true if finished. False otherwise.
     */
    boolean isFinished();

    /**
     * The id of the worker, in case identification is needed.
     * @return
     */
    int getId();

    boolean isRemoteWorker();

    /**
     * Set what type of work block should be performed.
     * This should be used, if the processing manager was used to determine the workblock.
     * @param worker
     */
    void SetWork( IWorkBlock worker);

    /**
     * Set the type of work block that should be performd by this worker.
     * This should be used, if the worker should generate the work block by him self.
     * @param workType
     */
    void SetWorkType(String workType);
    String GetWorkType();

    /**
     * Schedule the worker to run type of work using the given parameters.
     */
    boolean ScheduleBlock(Map<String, Object> inputs, long scheduleId , String blockId);

    /**
     * Schedule the worker to run type of work using the given parameters.
     * This function also tells the worker, that he has some constrains on how to do its work
     * => example: how long he should try work.
     * @return true if successful
     */
    boolean ScheduleBlock(Map<String, Object> inputs, long scheduleId , String blockId, PartialWorkRequest howToSplit);

    /**
     * Determine what kind of image data splitting this worker supports.
     * @return The supported split type
     */
    SplitType getSplitType();

    /**
     * Get the results from this worker.
     * This should also make the worker register those results inside his com infrastructure.
     * @return The map with teh named results.
     */
    Map<String, Object> GetAndRegisterResults();

    void Finalize();
}
