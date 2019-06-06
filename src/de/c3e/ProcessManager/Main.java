package de.c3e.ProcessManager;

import de.c3e.ProcessManager.CommandLineStuff.CommandLineParameters;
import de.c3e.ProcessManager.DataTypes.BlockGraph;
import de.c3e.ProcessManager.DataTypes.GraphReader;
import de.c3e.ProcessManager.DataTypes.ScriptInputParameters;
import de.c3e.ProcessManager.LoadBalancing.AverageWithTransferTimeBalancer;
import de.c3e.ProcessManager.LoadBalancing.ConstantSizeBalancer;
import de.c3e.ProcessManager.LoadBalancing.AverageExecutionTimeBalancer;
import de.c3e.ProcessManager.LoadBalancing.IBalancer;
import de.c3e.ProcessManager.TransportLayer.ITransportLayer;
import de.c3e.ProcessManager.TransportLayer.OpenMpiTransportLayer;
import de.c3e.ProcessManager.Utils.*;

import de.c3e.ProcessManager.WorkerManager.IWorkerManager;
import de.c3e.ProcessManager.WorkerManager.MainThreadWorkManager;
import de.c3e.ProcessManager.WorkerManager.RemoteWorker;
import de.c3e.ProcessManager.WorkerManager.RemoteWorkerManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import loci.common.DebugTools;
import org.slf4j.Logger;


import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Main
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());
    public static void main(String[] args)
    {
        DebugTools.enableIJLogging(false);
        DebugTools.enableLogging("INFO");

        InitIcy();
        CommandLineParameters parameters = new CommandLineParameters(args);

        if (parameters.RunInSingleNodeMode)
        {
            logger.info("Setup Single Node Mode");
            singleModeMain(parameters);
            return;
        }

        ITransportLayer transportLayer;
        try
        {
            //transportLayer = new IntelMpiTransportLayer(args);
            transportLayer = new OpenMpiTransportLayer(args);
            transportLayer.Initialize();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("Could not init openMPI " + e.toString());
            return;
        }

        // parameters with effect on all nodes
        IBalancer balancer;
        balancer = SelectBalancer(parameters, transportLayer.getName());

        CleanupTempFolder(transportLayer.getName());

        if (transportLayer.getMyRank() == 0)
        {
            RemoteWorkerManager workerManager = new RemoteWorkerManager(transportLayer,balancer);
            RunMasterNode(parameters, transportLayer.getName(), workerManager);
        }
        else
        {
            // worker node
            int rank=transportLayer.getMyRank();
            logger.info(transportLayer.getMyRank()+ ": " +"Start Execution : WorkerNode " + rank );

            RemoteWorker workerNode = new RemoteWorker(transportLayer,balancer);
            workerNode.RunMainLoop();
        }
        transportLayer.Finalize();
    }

    private static void RunMasterNode(CommandLineParameters parameters, String nodeName, IWorkerManager workerManager)
    {
        boolean dumpIntermediates = false;
        logger.info("Start Execution : Master Node");
        List<ScriptInputParameters> inputs = new ArrayList<>();
        if (StringUtils.isNotBlank(parameters.ParameterFile))
        {
            inputs = ParameterReader.ParametersFromParameterFile(parameters.ParameterFile);
            logger.info( nodeName + ": " + "Use Parameters from file " + parameters.ParameterFile);
        }
        else if (StringUtils.isNotBlank(parameters.JsonFile))
        {
            logger.info(nodeName + ": " +"Use Parameters from json file " + parameters.JsonFile);
            inputs = ParameterReader.ParametersFromParameterJsonFile(parameters.JsonFile);
        }

        if (StringUtils.isNotBlank(parameters.JsonFile))
        {
            dumpIntermediates = ParameterReader.DumpIntermediatesParameterFromJsonFile(parameters.JsonFile);
            if (dumpIntermediates)
            {
                logger.info(nodeName + ": " +"Dump Intermediates");
            }
        }

        String graphFile = null;
        if (StringUtils.isNotBlank(parameters.ProtocolFile))
        {
            graphFile = GlobalSettings.WorkFolder + parameters.ProtocolFile;
            logger.info(nodeName + ": " +"Graph file: " + graphFile);
        }


        ProcessingManager manager = new ProcessingManager(workerManager);
        if (workerManager instanceof MainThreadWorkManager)
        {
            manager.SetTimeout(0); // todo: encapsule in manager
        }

        boolean init = false;

        if (graphFile != null)
        {
            init = manager.InitGraphFromFile(graphFile, inputs);
        }
        else if (StringUtils.isNotBlank(parameters.JsonFile))
        {
            try
            {
                BlockGraph graph = GraphReader.GraphFromJson(TextFileUtil.FileToString(parameters.JsonFile));
                init = manager.InitManager(inputs, graph);
            }
            catch (Exception e)
            {
                logger.error(nodeName + ": " +"Load from json failed : " + e.toString());
            }
        }

        boolean successful = false;
        if (init)
        {
            try
            {
                manager.LoadBalancerStatistics();
                successful = manager.StartProcessingLoop();
                logger.info(nodeName + ": " +"Finished execution. Success: " + successful);
            }
            catch (Exception e)
            {
                logger.error(nodeName+ ": " +"Error on main node : \n" + e.toString() + "\n" + org.apache.commons.lang.exception.ExceptionUtils.getFullStackTrace(e));
                e.printStackTrace();
            }
        }
        else
        {
            try
            {
                // todo: fix this hack
                // wait for other workers to finish startup
                Thread.sleep(10000);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            logger.error(nodeName+ ": " +"Initialization failed");
        }

        if (successful)
        {
            manager.CollectExecutionStatistics("/statistics/");

            try
            {
                if (dumpIntermediates)
                {
                    // don't add WorkFolder => this is done by the blocks them selves
                    String intermediatesPath = "/intermediates/";
                    logger.info(nodeName + ": " + "Dump Intermediates to " + intermediatesPath);
                    manager.DumpIntermediatesToFolder(intermediatesPath);
                }
            } catch (Exception e)
            {
                logger.error(nodeName + ": " + "Exception while getting intermediates: ", e);
            }
        }

        manager.Finalize();
        manager.SaveBalancerStatistics();

       //DumpRunningThreads(rank);
    }

    private static void CleanupTempFolder(String nodeName)
    {
        logger.info("Purge old tmp folders.");
        int pre = CountTmpFiles("Ope");
        CleanupTmpFolder("Ope" ,nodeName);    // actually this stuff should be deleted by the plugin
        int post= CountTmpFiles("Ope");
        logger.info( nodeName + " : Cleanup: " + pre + " -> " + post );
    }

    private static IBalancer SelectBalancer(CommandLineParameters parameters, String nodeName)
    {
        IBalancer balancer;
        double delay = 0.0;
        try
        {
            delay = Double.parseDouble(parameters.TransferDelay);
        }catch (Exception e){ logger.error( nodeName + " : Error parsing string " + parameters.TransferDelay ); }
        BenchmarkHelper.SetDelayFactor(delay);

        if (StringUtils.isNotBlank(parameters.WorkingFolder))
        {
            logger.info(nodeName + ": Set Working folder  " + parameters.WorkingFolder);
            GlobalSettings.WorkFolder = parameters.WorkingFolder;
            GlobalSettings.IcyFolder = parameters.WorkingFolder;
        }

        balancer = SelectBalancer(parameters);
        logger.info(nodeName + ": Use balancer : "  + balancer.getClass().getName());
        return balancer;
    }

    private static void singleModeMain(CommandLineParameters parameters)
    {
        String nodeName = "SingleNodeMode";

        CleanupTempFolder(nodeName);

        if (StringUtils.isNotBlank(parameters.WorkingFolder))
        {
            logger.info( "Set working folder  " + parameters.WorkingFolder);
            GlobalSettings.WorkFolder = parameters.WorkingFolder;
            GlobalSettings.IcyFolder = parameters.WorkingFolder;
        }

        MainThreadWorkManager mainThreadWorker = new MainThreadWorkManager();
        RunMasterNode(parameters, nodeName, mainThreadWorker);
    }

    private static IBalancer SelectBalancer(CommandLineParameters parameters)
    {
        if (StringUtils.isNotBlank(parameters.Balancer))
        {
            switch (parameters.Balancer.toLowerCase())
            {
                case "consttime" :
                    long size = 1000;
                    try
                    {
                        size = Long.parseLong(parameters.BalancerSize);
                    }catch (Exception e) { DebugHelper.PrintException(e,logger); }

                    return new ConstantSizeBalancer(size);

                case "average_time" :
                    return new AverageExecutionTimeBalancer();

                case "average_time_data_transfer" :
                    return new AverageWithTransferTimeBalancer();


                case "default" :
                default: return new AverageExecutionTimeBalancer();
            }
        }
        else
        {
            return new AverageExecutionTimeBalancer();
        }
    }

    @SuppressWarnings("unused")
    private static void DumpRunningThreads(int rank)
    {
        try
        {
            Thread.sleep(10000 + 1000 * rank);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
        for (Thread t :
                threadArray)
        {
            if (Thread.currentThread() == t)
                continue;

            System.out.println(rank + " : Running " + t + " alive: " + t.isAlive());
            for (StackTraceElement el : t.getStackTrace())
            {
                System.out.println(rank + " :     " + el.toString());
            }
        }
    }

    private static int CountTmpFiles(String folderPrefix)
    {
        String folder = "/tmp/";
        File tmpFolder = new File(folder);
        String[] list = tmpFolder.list();
        if (null == list )  {return -1;}
        int i =0;
        for (String f:list)
        {
            if (f.startsWith(folderPrefix))
            {
               i++;
            }
        }
        return i;
    }

    private static void CleanupTmpFolder(String folderPrefix, String name)
    {
        String folder = "/tmp/";
        File tmpFolder = new File(folder);
        File[] list = tmpFolder.listFiles();
        if (null == list )  {return;}
        for (File f:list)
        {
            if (f.getName().startsWith(folderPrefix))
            {
                if (!f.isDirectory())
                {
                    logger.error(f.getAbsolutePath() + " Is no directory");
                }

                try
                {

                    FileUtils.deleteDirectory(f);
                    if (f.exists())
                    {
                        logger.error(f.getAbsolutePath() + " : Still there" + f);
                    }
                }
                catch (Exception ex)
                {
                    //logger.error(name + " : Could not delete " + f);
                    DebugHelper.PrintException(ex,logger);
                }
            }
        }
    }

    public static void InitIcy()
    {
        icy.preferences.ApplicationPreferences.load();
        icy.preferences.GeneralPreferences.load();
        icy.preferences.PluginPreferences.load();
        icy.preferences.PluginsPreferences.load();
    }
}
