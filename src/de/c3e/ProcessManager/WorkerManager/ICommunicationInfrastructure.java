package de.c3e.ProcessManager.WorkerManager;

import de.c3e.ProcessManager.DataTypes.ResultGraphPosition;
import de.c3e.ProcessManager.LoadBalancing.IBalancer;
import de.c3e.ProcessManager.Utils.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Interface to abstract the implementation details of the communication between workers
 */
public interface ICommunicationInfrastructure
{
    /**
     * Get the worker to use its communication infrastructure to send the object with a given id to a designated receiver.
     * @return true if the send was started, false otherwise.
     */
    Future<Object> RequestObjectFromWorker(int workerId, ResultGraphPosition objectPosition);

    /**
     * Register a local worker, that can be used for running calculations.
     * @param newOne the worker to be registered
     */
    void RegisterLocalWorker(IWorker newOne);

    List<? extends IWorker> GetRegisterWorkers();

    /***
     * Register a Result into the communication infrastructure
     * @param value The value of the result.
     * @param position The position in the graph where this value belongs.
     */
    void RegisterResult(Object value, ResultGraphPosition position);

    /**
     * Register a partial result inside the communication infrastructure
     * @param value The result.
     * @param usedSplit The parameters that produced this split data
     */
    void RegisterPartialResult(Object value, ResultGraphPosition position, PartialWorkRequest usedSplit, String output);

    void ReportReadyState();

    void Finalize();

    /***
     * Distribute through net
     * todo: merge with register result and register not only an object, but also where in the graph it belongs.
     */
    void AnnounceResults(Map<String, Object> resultsToAnnounce);

    int getMyRank();

    void RequestPartOfPartialObject(RemoteObjectPartRequest neededObjectPart);

    /**
     * Register an input. This has to be done, in case the infrastructure is asked to
     * request missing parts => it must know where to put the parts on their arrival.
     * @param key The graph position of object
     * @param value The input object
     */
    Object RegisterInput(ResultGraphPosition key, Object value);

    Future<ExecutionTimeMeasurements> RequestExecutionStatisticsFromWorker(int workerId);

    /***
     * Collect the execution information and statistics from all workers.
     * @return A List with statistics with each worker
     */
    // todo: think about removing this => measurements are send as soon as the are recorded
    List<ExecutionTimeMeasurements> CollectExecutionInformation();

    ExecutionTimeMeasurementEntry GetTransportStatistics();

    /**
     * Signal the master, that something went wrong with a worker
     * @param error The Error that occurred.
     */
    void ReportErrorStatus(ErrorStatus error);

    List<ErrorStatus> getErrors();

    /**
     * Report a new time measurement to the infrastructure.
     * @param toReport The measurement to be reported.
     */
    void ReportTimeMeasurement(ExecutionTimeMeasurementEntry toReport);

    IBalancer getBalancer();

    void BroadcastBlockEvictionNotice(String blockId);
}


