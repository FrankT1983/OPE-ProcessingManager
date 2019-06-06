package de.c3e.ProcessManager.WorkerManager;

/**
 * A part of a result.
 * All parts are identified by a shared id.
 */
public interface IPartialResult
{
    /**
     * Add a part to a given object;
     * @param value The part to add
     */
    void AddPart(Object value);

    /**
     * Get all local parts of this object to be send to another object of the same type for merge.
     * @param myRank The rank of the worker this happens on. In case the IPartialResult does not now anything
     *               of the transport infrastructure.
     * @return The object containing all merged data.
     */
    Object GetLocalParts(int myRank);

    /***
     * Todo: get rid of this ... this is only practical for non size changing image projections
     * @return
     */
    boolean isComplete();

    void PullAllRemoteParts();
}

