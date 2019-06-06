package de.c3e.ProcessManager.WorkerManager;

import de.c3e.ProcessManager.DataTypes.GraphBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Implementation of a worker manager, that used threads to do work.
 */
public class ThreadsWorkerManager implements IWorkerManager
{
    private int maxThreads;

    private final List<ThreadWorker> workerList = new ArrayList<>();

    public ThreadsWorkerManager(int maxWorkers)
    {
        this.maxThreads = maxWorkers;
    }

    @Override
    public List<IWorker> getFreeWorkers()
    {
        List<IWorker> result = new ArrayList<>();
        synchronized (workerList)
        {
            // try finding a empty one
            for (ThreadWorker worker : this.workerList)
            {
                if (!worker.isBusy())
                {
                    result.add(worker);
                }
            }


            // create a new on if possible
            while (workerList.size() < maxThreads)
            {
                ThreadWorker newWorker = new ThreadWorker();
                this.workerList.add(newWorker);
                result.add(newWorker);
            }

        }

        long seed = System.nanoTime();
        Collections.shuffle(result, new Random(seed));
        return result;
    }

    @Override
    public ICommunicationInfrastructure getInfrastructure()
    {
        return null;
    }

    @Override
    public void Finalize()
    {

    }

    @Override
    public boolean HadError()
    {
        return false;
    }

    @Override
    public void SendEvictionNotice(GraphBlock b)
    {
        // todo: implement, when implementing single node run mode
    }

    @Override
    public IWorker getFreeWorker()
    {
        synchronized (workerList)
        {
            // try finding a empty one
            for (ThreadWorker worker : this.workerList)
            {
                if (!worker.isBusy())
                {
                    return worker;
                }
            }

            // create a new on if possible
            if (workerList.size() < maxThreads)
            {
                ThreadWorker newWorker = new ThreadWorker();
                this.workerList.add(newWorker);
                return newWorker;
            }

            return null;
        }
    }
}
