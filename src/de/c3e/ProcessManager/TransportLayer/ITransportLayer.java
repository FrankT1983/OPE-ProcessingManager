package de.c3e.ProcessManager.TransportLayer;

import de.c3e.ProcessManager.Utils.Consumer;
import de.c3e.ProcessManager.Utils.ExecutionTimeMeasurementEntry;

import java.io.Serializable;

/**
 * Abstraction for the actual transport layer
 */
public interface ITransportLayer
{
    /**
     * Send an object to a node with the given rank.
     * @param object The object to send.
     * @param receiver The rank of the destination node.
     */
    boolean SendObjectTo(Serializable object, int receiver);

    /**
     * Try to receive an object through the transport layer.
     * This should not block.
     * @return A object for this node. Null if none is available.
     */
    Serializable ReceiveObject();

    /**
     * Initialize the transport layer.
     */
    void Initialize();

    /***
     * Get the rank in the communication infrastructure.
     * Can be used to identify a node.
     * @return The rank of this node in the transport layer.
     */
    int getMyRank();

    /***
     * A way to get statistic information from the transport layer.
     * @return Not sure yet.
     */
    String getStatistics();

    String getName();

    /**
     * Called on cleanup
     */
    void Finalize();

    void SetReportingFunction(Consumer<ExecutionTimeMeasurementEntry> function);
}
