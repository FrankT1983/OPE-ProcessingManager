package de.c3e.ProcessManager.WorkerManager;



import de.c3e.ProcessManager.DataTypes.GraphBlock;

import java.util.List;

/**
 * Interface for abstracting the creation of IWorker objects.
 */
public interface IWorkerManager
{
    /**
     * Request a free worker from teh worker manager
     * @return A free worker. Null if none is free.
     */
    IWorker getFreeWorker();

    /**
     * Return a list of all free workers.
     * @return A list of all the free workers.
     */
    List<IWorker> getFreeWorkers();

    /**
     * Return the communication infrastructure this worker uses.
     * @return The communication infrastructure.
     */
    ICommunicationInfrastructure getInfrastructure();

    /**
     * Tell the work manager, that the work is done and he close his workers.
     */
    void Finalize();

    /**
     * Check if a error in one of the workers occurred.
     * @return True, if a error was detected.
     */
    boolean HadError();

    void SendEvictionNotice(GraphBlock b);
}
