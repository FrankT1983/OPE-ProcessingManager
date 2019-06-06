package de.c3e.Tests;

import de.c3e.ProcessManager.DataTypes.BlockGraph;
import de.c3e.ProcessManager.DataTypes.GraphReader;
import de.c3e.ProcessManager.DataTypes.ScriptInputParameters;

import de.c3e.ProcessManager.LoadBalancing.AverageExecutionTimeBalancer;
import de.c3e.ProcessManager.LoadBalancing.IBalancer;
import de.c3e.ProcessManager.Main;
import de.c3e.ProcessManager.ProcessingManager;
import de.c3e.ProcessManager.Utils.AbstractImageObject;
import de.c3e.ProcessManager.Utils.ParameterReader;
import de.c3e.ProcessManager.Utils.SerializeDeserializeHelper;
import de.c3e.ProcessManager.TransportLayer.LocalTransportLayer;
import de.c3e.ProcessManager.WorkerManager.RemoteWorker;
import de.c3e.ProcessManager.WorkerManager.RemoteWorkerManager;
import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Class to test out interaction between multiple process managers
 * => master slave mode
 */
public class MultipleInstancesTest
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
    }

    @Test()
    public void SimpleSendReceiveInfrastructureTest_OneMaster_OneWorker()
    {
        int numberClients = 2;      // start with one master and one worker
        RunTestForNumberOfClients(numberClients, false);
    }

    @Test()
    public void SimpleSendReceiveInfrastructureTest_OneMaster_OneWorker_WithDeserialization()
    {
        int numberClients = 2;      // start with one master and one worker
        RunTestForNumberOfClients(numberClients, true);
    }

    @Test()
    public void SimpleSendReceiveInfrastructureTest_OneMaster_MoreWorkerThanWork_WithDeserialization()
    {
        int numberClients = 10;      // start with one master and one worker
        RunTestForNumberOfClients(numberClients, true);
    }

    @Test()
    public void SimpleSendReceiveInfrastructureTest_OneMaster_ThreeWorker()
    {
    int numberClients = 4;      // start with one master and one worker
        RunTestForNumberOfClients(numberClients, true);
    }

    private void RunTestForNumberOfClients(int numberClients, boolean deserialize )
    {
        final LocalTransportLayer transportLayer = new LocalTransportLayer(numberClients, deserialize);

        int sizeX = 5;
        int sizeY = 5;
        int X = 1;
        int Y = 2;

        Sequence image = new Sequence();
        IcyBufferedImage icy_img = new IcyBufferedImage(sizeX, sizeY, 1, DataType.BYTE);
        byte[] inputData = new byte[sizeX * sizeY];
        for (byte i = 0; i < inputData.length; i++)
        {
            inputData[i] = i;
        }
        Array1DUtil.byteArrayToSafeArray(inputData, icy_img.getDataXY(0), icy_img.isSignedDataType(), icy_img.isSignedDataType());
        image.setImage(0, 0, icy_img);

        File tempFile = null;
        try
        {
            tempFile = File.createTempFile("UnitTestResult", "foo");
            tempFile.deleteOnExit();
        }catch (Exception e)
        {
            assertTrue(false);
            e.printStackTrace();
        }

        // run test
        final BlockGraph graph = BlockTest.ConstructDoualAddXWithResultsListGraph(X, Y, AbstractImageObject.fromSequence(image), "DelayedAddXYBlock",tempFile.getAbsolutePath());
        RunGraphOnClients(numberClients, transportLayer, graph);

        assertNotNull(graph);

        assertTrue(tempFile.exists());
        Object result = SerializeDeserializeHelper.ObjectFromFile(tempFile.getAbsolutePath());
        assertNotNull(result);
        assertTrue(result instanceof AbstractImageObject);

        AbstractImageObject resultImg = (AbstractImageObject)result;

        double[] resultData = resultImg.getDataXYAsDoubleArray(0,0,0);
        assertEquals(inputData.length, resultData.length);
        for (int i =0;i< inputData.length; i++)
        {
            assertEquals(inputData[i] + 2*X +2*Y, resultData[i] , 0.0001);
        }
    }

    static void RunGraphOnClients(int numberClients, final BlockGraph graph)
    {
        final LocalTransportLayer transportLayer = new LocalTransportLayer(numberClients, true);

        RunGraphOnClients(numberClients,transportLayer, graph);
    }

    public static void RunFromFiles(int numberClients,final String graphFile, final String parameterFile)
    {
        final LocalTransportLayer transportLayer = new LocalTransportLayer(numberClients, true);
        List<Thread> workThreads = new ArrayList<>();
        for (int i= 0; i< numberClients ; i++)
        {
            if (i == 0)
            {
                final int rank = i;
                Thread masterThread =
                        new Thread()
                        {
                            public void run()
                            {
                                try
                                {
                                    // master node
                                    RemoteWorkerManager workerManager = new RemoteWorkerManager(transportLayer.getAccessPoint(rank), new AverageExecutionTimeBalancer());
                                    ProcessingManager manager = new ProcessingManager(workerManager);
                                    List<ScriptInputParameters> params = ParameterReader.ParametersFromParameterString(parameterFile);
                                    manager.InitGraphFromString(graphFile, params);
                                    manager.SetTimeout(300000);
                                    boolean result = manager.StartProcessingLoop();
                                    manager.Finalize();
                                    assertTrue("Should run successful", result);

                                } catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        };

                workThreads.add(masterThread);
            }
            else
            {
                final int rank = i;
                Thread slaveThread =
                        new Thread()
                        {
                            public void run()
                            {
                                try
                                {
                                    RemoteWorker workerNode = new RemoteWorker(transportLayer.getAccessPoint(rank),new AverageExecutionTimeBalancer());
                                    workerNode.RunMainLoop();
                                } catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        };
                workThreads.add(slaveThread);
            }
        }

        for (int i= 0; i< numberClients ; i++)
        {
            workThreads.get(i).start();
        }


        boolean someoneAlive = true;
        while (someoneAlive)
        {
            someoneAlive = false;
            for (int i = 0; i < numberClients; i++)
            {
                someoneAlive |= workThreads.get(i).isAlive();
            }
        }

    }

    public static void RunFromFilesJson(int numberClients,final String jsonString, final IBalancer balancer)
    {
        RunFromFilesJsonDumpResults(numberClients, jsonString, false, null, balancer);
    }

    public static void RunFromFilesJson(int numberClients,final String jsonString)
    {
        RunFromFilesJsonDumpResults(numberClients,jsonString, false, null);
    }

    public static void RunFromFilesJsonDumpResults(int numberClients, final String jsonString, final boolean dumpIntermediates, final String dumpFolder)
    {
        RunFromFilesJsonDumpResults(numberClients, jsonString,dumpIntermediates, dumpFolder, new AverageExecutionTimeBalancer());
    }

    private static void RunGraphOnClients(int numberClients, final LocalTransportLayer transportLayer, final BlockGraph graph)
    {
        List<Thread> workThreads = new ArrayList<>();
        for (int i= 0; i< numberClients ; i++)
        {
            if (i == 0)
            {
                final int rank = i;
                Thread masterThread =
                        new Thread()
                        {
                            public void run()
                            {
                                try
                                {
                                    // master node
                                    RemoteWorkerManager workerManager = new RemoteWorkerManager(transportLayer.getAccessPoint(rank), new AverageExecutionTimeBalancer());
                                    ProcessingManager manager = new ProcessingManager(workerManager);
                                    manager.InitManager(graph);
                                    manager.SetTimeout(-1);
                                    boolean success = manager.StartProcessingLoop();
                                    manager.Finalize();
                                    assertTrue("Should run successful", success);
                                } catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        };

                workThreads.add(masterThread);
            }
            else
            {
                final int rank = i;
                Thread slaveThread =
                        new Thread()
                        {
                            public void run()
                            {
                                try
                                {
                                    RemoteWorker workerNode = new RemoteWorker(transportLayer.getAccessPoint(rank),new AverageExecutionTimeBalancer());
                                    workerNode.RunMainLoop();
                                } catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        };
                workThreads.add(slaveThread);
            }
        }

        for (int i= 0; i< numberClients ; i++)
        {
            workThreads.get(i).start();
        }


        boolean someoneAlive = true;
        while (someoneAlive)
        {
            someoneAlive = false;
            for (int i = 0; i < numberClients; i++)
            {
                someoneAlive |= workThreads.get(i).isAlive();
            }
        }
    }

    public static void RunFromFilesJsonDumpResults(int numberClients, final String jsonString, final boolean dumpIntermediates, final String dumpFolder, final IBalancer balancer)
    {
        final LocalTransportLayer transportLayer = new LocalTransportLayer(numberClients, true);
        final List<Thread> workThreads = new ArrayList<>();
        for (int i= 0; i< numberClients ; i++)
        {
            if (i == 0)
            {
                final int rank = i;
                Thread masterThread =
                        new Thread()
                        {
                            public void run()
                            {
                                try
                                {
                                    // master node
                                    RemoteWorkerManager workerManager = new RemoteWorkerManager(transportLayer.getAccessPoint(rank), balancer);
                                    ProcessingManager manager = new ProcessingManager(workerManager);
                                    List<ScriptInputParameters> params = ParameterReader.ParametersFromJsonString(jsonString);
                                    BlockGraph graph = GraphReader.GraphFromJson(jsonString);

                                    manager.InitManager(params, graph);
                                    manager.SetTimeout(30000);
                                    manager.LoadBalancerStatistics();
                                    boolean result = manager.StartProcessingLoop();

                                    if (dumpFolder != null)
                                    {
                                        manager.CollectExecutionStatistics(dumpFolder);
                                    }

                                    if (dumpIntermediates)
                                    {
                                        manager.DumpIntermediatesToFolder(dumpFolder);
                                    }

                                    manager.Finalize();
                                    manager.SaveBalancerStatistics();
                                    assertTrue("Should run successful", result);

                                } catch (Exception e)
                                {
                                    e.printStackTrace();
                                    for(Thread t : workThreads)
                                    {
                                        t.interrupt();
                                    }
                                }
                            }
                        };

                workThreads.add(masterThread);
            }
            else
            {
                final int rank = i;
                Thread slaveThread =
                        new Thread()
                        {
                            RemoteWorker workerNode = null;
                            public void run()
                            {
                                try
                                {
                                    workerNode = new RemoteWorker(transportLayer.getAccessPoint(rank),new AverageExecutionTimeBalancer());
                                    workerNode.RunMainLoop();
                                } catch (Exception e)
                                {
                                    e.printStackTrace();
                                    assertTrue("Should run successful, had exception: " +e,false);

                                }
                            }

                            @Override
                            public void interrupt() {
                                super.interrupt();
                                if (workerNode == null) return;

                                workerNode.Finalize();
                            }
                        };
                workThreads.add(slaveThread);
            }
        }

        for (int i= 0; i< numberClients ; i++)
        {
            workThreads.get(i).start();
        }


        boolean someoneAlive = true;
        while (someoneAlive)
        {
            someoneAlive = false;
            for (int i = 0; i < numberClients; i++)
            {
                someoneAlive |= workThreads.get(i).isAlive();
            }

            try
            {
                Thread.sleep(100);
            } catch (InterruptedException e){e.printStackTrace();}
        }

    }
}
