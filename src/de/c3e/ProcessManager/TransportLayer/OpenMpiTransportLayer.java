package de.c3e.ProcessManager.TransportLayer;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


import de.c3e.ProcessManager.Utils.*;

import mpi.*;
import org.slf4j.Logger;

/**
 * Implementation of an transport layer, base on open mpi and its java bindings.
 */

public class OpenMpiTransportLayer implements ITransportLayer
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private static final int MessageTag = 1;

    private final int myRank;

    private volatile boolean requestFinalization = false;

    private volatile boolean requestShutdownSend = false;

    private volatile boolean wasFinalized = false;

    private volatile long sendBytes = 0;

    private final Queue<Serializable> receivedQueue =  new ConcurrentLinkedQueue<>();

    private Thread checkForMessagesThread = new Thread()
    {
        public void run()
        {
            while (!requestFinalization)
            {
                CheckForMessages();
            }

            logger.info("Shutdown Send thread");
            requestShutdownSend = true;
            while(sendMessagesThread.isAlive())
            {
                CheckForMessages();
                try
                {
                    Thread.sleep(20);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            logger.info("Send and Receive thread terminated.");
        }
    };

    private Thread sendMessagesThread = new Thread()
    {
        public void run()
        {
            while (!requestShutdownSend)
            {
                SendMessageFromQueue();
            }

            logger.info("Shutting down send ["+ myRank + "] : remaining " + toSendQueue.size());

            // empty queue
            synchronized (toSendQueue)
            {
                while (!toSendQueue.isEmpty())
                {
                    SendMessageFromQueue();
                }
            }

            // finish last sending
            while (true)
            {
                synchronized (toSendQueue)
                {
                    if (!sending)
                    {
                        break;
                    }
                }
                try
                {Thread.sleep(20);}
                catch (Exception e)
                {   e.printStackTrace();    }
            }

            logger.info("Shutting down finished");

        }
    };

    class SendPackage
    {
        byte[] Data;
        int Receiver;
        Serializable ObjectRef;

        public SendPackage(byte[] objectAsByte, int receiver, Serializable object)
        {
            this.Data= objectAsByte;
            this.Receiver = receiver;
            this.ObjectRef = object;
        }
    }


    private final List<String> foo2 = new ArrayList<>();

    public OpenMpiTransportLayer(String[] args) throws MPIException
    {
        //MPI.Init(args);
        MPI.InitThread(args,MPI.THREAD_MULTIPLE);
        this.myRank = MPI.COMM_WORLD.getRank();
        this.checkForMessagesThread.start();
        this.sendMessagesThread.start();
        String name = MPI.getProcessorName();
        logger.info("Start OpenMpi TransportLayer on node :" + name);
    }

    public  String getName()
    {
        try
        {
            return MPI.getProcessorName();
        }catch (Exception ex)
        {
            return "Error getting name " + ex.getMessage();
        }

    }

    private final Queue<SendPackage> toSendQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean sending = false;

    private synchronized void SendMessageFromQueue()
    {
        SendPackage toSend;
        synchronized (toSendQueue)
        {
            if (this.toSendQueue.size() < 1)
            {
                return;
            }


            toSend= this.toSendQueue.poll();
            sending = true;

        }

        BenchmarkHelper.DelayDataTransfer(toSend.ObjectRef , "MPITrans\t"+ this.myRank + "\t->\t" + toSend.Receiver);
        byte[] objectAsByte = toSend .Data;
        long startTime = System.currentTimeMillis();
        if (objectAsByte != null)
        {
            try
            {
                this.sendBytes+= objectAsByte.length;
                MPI.COMM_WORLD.send(objectAsByte, objectAsByte.length, MPI.BYTE, toSend.Receiver, MessageTag);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                DebugHelper.BreakIntoDebug();
                logger.error("Sending failed: " + e.toString());
            }
        }


        long duration = System.currentTimeMillis() - startTime;
        Report(ExecutionTimeMeasurementEntry.DataTransferStatistics(duration,toSend.ObjectRef,objectAsByte.length, toSend.Receiver, this.getMyRank()));

        synchronized (toSendQueue)
        {
            sending = false;
        }
    }

    @Override
    public boolean SendObjectTo(Serializable object, int receiver)
    {
        if (this.requestFinalization || this.wasFinalized)
        {
            logger.error(this.myRank + " : Sending objects after Finalization was requested: " + object.toString());
            DebugHelper.BreakIntoDebug();
            Thread.dumpStack();
            return false;
        }

        byte[] objectAsByte = SerializeDeserializeHelper.ObjectToBytes(object);
        if (objectAsByte == null)
        {
            logger.error("Could not deserialize object " + object + " being send " + this.myRank + " -> " + receiver );

            return false;
        }

        synchronized (foo2)
        {
            foo2.add(object.getClass().getName() + " (" +  objectAsByte.length + ")");
        }

        synchronized(toSendQueue)
        {
            this.toSendQueue.add(new SendPackage(objectAsByte, receiver, object));
        }
        return true;
    }

    @Override
    public synchronized Serializable ReceiveObject()
    {
        synchronized (receivedQueue)
        {
            if (this.receivedQueue.isEmpty())
            {
                return null;
            }

            return this.receivedQueue.poll();
        }
    }

    @Override
    public void Initialize()
    {
    }


    public int getMyRank()
    {
        return this.myRank;
    }

    @Override
    public String getStatistics()
    {
        logger.info(this.myRank + " : " + Arrays.toString(this.foo2.toArray()) );
        return String.valueOf(this.sendBytes) + " Byte";
    }

    /**
     * Safely shut down this transportation layer
     */
    public void Finalize()
    {
        try
        {
            this.requestFinalization = true;
            logger.info(myRank + " Request finalization");
            while(this.checkForMessagesThread.isAlive())
            {
                try
                {
                    Thread.sleep(20);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            logger.info(myRank + " Waiting for Finalization barrier");
            MPI.COMM_WORLD.barrier();
            logger.info(myRank + " passed Finalization barrier");

            MPI.Finalize();
            this.wasFinalized = true;
            logger.info(myRank + " passed mpi finalize");
        }
        catch (Exception e)
        {
            logger.error("Finalisation failed: " + e.toString());
            DebugHelper.BreakIntoDebug();
            e.printStackTrace();
        }
    }

    private void CheckForMessages()
    {
        try
        {
            Status probeStatus = MPI.COMM_WORLD.iProbe(MPI.ANY_SOURCE, MPI.ANY_TAG);
            if (probeStatus != null)
            {
                int bytesReceived = probeStatus.getCount(MPI.BYTE);
                if (bytesReceived > 0)
                {
                    byte[] inComingBuffer = new byte[bytesReceived];
                    MPI.COMM_WORLD.recv(inComingBuffer, bytesReceived, MPI.BYTE, probeStatus.getSource(), probeStatus.getTag());

                    try
                    {
                        Serializable o = SerializeDeserializeHelper.BytesToObject(inComingBuffer);
                        synchronized (receivedQueue)
                        {
                            if (o != null)
                            {
                                receivedQueue.add(o);
                            }
                            else
                            {
                                logger.error("Received " + bytesReceived + " bytes  but deserialization returned null.");
                            }
                        }
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                        logger.error("Could not deserialize bytes to object. " + inComingBuffer.length + " bytes.");
                    }
                }
            }

            try
            {
                Thread.sleep(10);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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


