package de.c3e.ProcessManager.BlockRepository;

import de.c3e.ProcessManager.DataTypes.GraphBlock;
import de.c3e.ProcessManager.WorkerManager.IWorker;
import de.c3e.ProcessManager.WorkerManager.RemoteWorkerMasterSide;
import de.c3e.ProcessingManager.Interfaces.IWorkBlock;
;import java.util.Map;


/**
 * Helper abstract base class to manage all the management data in one place.
 */
public class GraphBlockTrackingDecorator
{
    private GraphBlock blockInGraph;

    private final IWorkBlock containdWorkBlock;

    private final IWorker worker;

    public GraphBlockTrackingDecorator(IWorkBlock blockToRun, GraphBlock graphBlock, IWorker id)
    {
        this.blockInGraph = graphBlock;
        this.containdWorkBlock = blockToRun;
        this.worker = id;
    }

    public Map<String, Object> CollectResults()
    {
        if (this.containdWorkBlock != null)
        {
            return this.containdWorkBlock.GetResults();
        }
        else
        {
            return this.worker.GetAndRegisterResults();
        }
    }


    /**
     * Get the block in the graph, that this work block belongs to.
     * Todo: Move the management of this to te Executor
     * @return The Block in the graph
     */
    public GraphBlock getBlockInGraph()
    {
        return blockInGraph;
    }


    public String getWorkerIdentification()
    {
        if (this.worker == null)
        {return "";}


        if (this.worker instanceof RemoteWorkerMasterSide)
        {
            return String.valueOf(((RemoteWorkerMasterSide) this.worker).getRemoteId());
        }

        return String.valueOf(this.worker.getId());
    }

    public boolean isFinished()
    {
        if (this.worker != null)
        {
            return this.worker.isFinished();
        }
        // fallback for working on main thread
        return true;
    }
}

