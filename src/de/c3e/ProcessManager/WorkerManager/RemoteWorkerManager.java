package de.c3e.ProcessManager.WorkerManager;

import de.c3e.ProcessManager.DataTypes.GraphBlock;
import de.c3e.ProcessManager.LoadBalancing.IBalancer;
import de.c3e.ProcessManager.TransportLayer.ITransportLayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by Frank on 18.07.2016.
 */
public class RemoteWorkerManager implements IWorkerManager
{
    final private ICommunicationInfrastructure comInfrastructure;
    final private List<? extends IWorker> registerdWorkers;

    public RemoteWorkerManager(ITransportLayer accessPoint, IBalancer balancer)
    {
        this.comInfrastructure = new SendReceiveInfrastructure(accessPoint, balancer);
        this.registerdWorkers = this.comInfrastructure.GetRegisterWorkers();
    }

    @Override
    public IWorker getFreeWorker()
    {
        List<IWorker> registerdCopy = new ArrayList<>(this.registerdWorkers);
        for (IWorker w : registerdCopy)
        {
            if (!w.isBusy())
            {
                return w;
            }
        }

        return null;
    }

    @Override
    public List<IWorker> getFreeWorkers()
    {
        List<IWorker> registerdCopy = new ArrayList<>(this.registerdWorkers);
        List<IWorker> freeWorkers = new ArrayList<>();


        for (IWorker w : registerdCopy)
        {
            if (w== null)
            {   continue;   }
            if (!w.isBusy())
            {
                freeWorkers.add(w);
            }
        }

        long seed = System.nanoTime();
        Collections.shuffle(freeWorkers, new Random(seed));

        return freeWorkers;
    }

    @Override
    public ICommunicationInfrastructure getInfrastructure()
    {
        return this.comInfrastructure;
    }

    @Override
    public void Finalize()
    {
        this.comInfrastructure.Finalize();
    }

    @Override
    public boolean HadError()
    {
        return  this.comInfrastructure.getErrors().size() > 0;
    }

    @Override
    public void SendEvictionNotice(GraphBlock b)
    {
        this.comInfrastructure.BroadcastBlockEvictionNotice(b.Id);

    }
}
