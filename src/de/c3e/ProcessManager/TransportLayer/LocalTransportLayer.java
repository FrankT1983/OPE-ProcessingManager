package de.c3e.ProcessManager.TransportLayer;

import de.c3e.ProcessManager.Utils.*;
import de.c3e.ProcessManager.WorkerManager.Messages.SendObjectPartMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Transport infrastructure for communicating between different worker. This on uses on local object.
 */
public class LocalTransportLayer
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private final Queue<Serializable>[] inTransitbuffer;
    private final boolean doSerialization;

    public LocalTransportLayer(int maxClients, boolean serializeDeSerialize)
    {
        this.inTransitbuffer = new Queue[maxClients];
        this.doSerialization = serializeDeSerialize;

        for (int i=0;i<maxClients;i++)
        {
            this.inTransitbuffer[i] = new ConcurrentLinkedQueue<>();
        }
    }

    private synchronized void SendObjectTo(Serializable object, int r)
    {
        if (this.doSerialization)
        {
            object = SerializeDeserialize(object);
        }
        this.inTransitbuffer[r].add(object);
    }

    private Serializable SerializeDeserialize(Serializable object)
    {
        byte[] ser = SerializeDeserializeHelper.ObjectToBytes(object);
        return SerializeDeserializeHelper.BytesToObject(ser);
    }

    private synchronized Serializable ReceiveObject(int rank)
    {
        synchronized (this.inTransitbuffer[rank])
        {
            if (this.inTransitbuffer[rank].isEmpty())
            {
                return null;
            }

            Serializable res = this.inTransitbuffer[rank].poll();
            return res;
        }
    }

    public ITransportLayer getAccessPoint(int desiredId)
    {
        AccessPoint a =new AccessPoint(desiredId,this);
        a.Initialize();

        return a;
    }

    private class AccessPoint implements ITransportLayer
    {
        private final LocalTransportLayer localTransport;
        private final int myRank;
        private volatile long sendBytes = 0;
        private volatile List<Integer> foo = new ArrayList<>();
        private volatile List<String> foo2 = new ArrayList<>();
        private volatile List<String> foo3 = new ArrayList<>();

        AccessPoint(int myId, LocalTransportLayer parent)
        {
            this.myRank = myId;
            this.localTransport = parent;
        }

        @Override
        public boolean SendObjectTo(Serializable object, int receiver)
        {
            long startTime = System.currentTimeMillis();
            int objectLengt = 0;
            // todo : remove this later
            synchronized (foo)
            {
                byte[] ser = SerializeDeserializeHelper.ObjectToBytes(object);
                objectLengt =ser.length;
                this.sendBytes += objectLengt;
                foo.add(objectLengt);
                foo2.add(object.getClass().getName() + " (" +  objectLengt + ")");
                if (object instanceof SendObjectPartMessage)
                {
                    Object part = ((SendObjectPartMessage)object).getObjectPart();
                    if (part instanceof ImageSubBlock)
                    {
                        ImageSubBlock block = (ImageSubBlock)part;
                        foo3.add(this.myRank + " ->" + receiver + " pixels " + block.dimensions.getPixelSize() + block.dimensions.toString());
                    }
                    else
                    {
                        foo3.add(this.myRank + " ->" + receiver);
                    }
                }
            }

            BenchmarkHelper.DelayDataTransfer(object , "LocalTrans "+ this.myRank + " -> " + receiver);

            this.localTransport.SendObjectTo(object,receiver);
            long duration = System.currentTimeMillis() - startTime;
            Report(ExecutionTimeMeasurementEntry.DataTransferStatistics(duration,object,objectLengt, receiver, this.getMyRank()));
            return true;
        }

        @Override
        public Serializable ReceiveObject()
        {
            return this.localTransport.ReceiveObject(this.myRank);
        }

        @Override
        public void Initialize()
        {
        }

        @Override
        public int getMyRank()
        {
            return myRank;
        }

        @Override
        public String getStatistics()
        {
            logger.info( this.toString()  + " getgetStatistics()" );
            logger.info(this.myRank + " : " + Arrays.toString(this.foo.toArray()) );
            logger.info(this.myRank + " : " + Arrays.toString(this.foo2.toArray()) );
            logger.info(this.myRank + " : " + Arrays.toString(this.foo3.toArray()) );
            return String.valueOf(this.sendBytes) + " Byte";
        }

        @Override
        public String getName()
        {
            return "LocalTransport AccessPoint " + this.myRank;
        }

        @Override
        public void Finalize()
        {
        }

        private Consumer<ExecutionTimeMeasurementEntry> reporter;

        private void Report(ExecutionTimeMeasurementEntry toReport)
        {
            if (toReport != null && reporter != null)
            {
                this.reporter.accept(toReport);
            }
        }

        @Override
        public void SetReportingFunction(Consumer<ExecutionTimeMeasurementEntry> function)
        {
            this.reporter = function;
        }
    }
}
