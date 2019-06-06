package de.c3e.ProcessManager;

import com.google.common.base.Joiner;
import de.c3e.ProcessManager.BlockRepository.DataToCsvFileBlock;
import de.c3e.ProcessManager.BlockRepository.LoadImageWorkBlock;
import de.c3e.ProcessManager.BlockRepository.OutputWorkBlock;
import de.c3e.ProcessManager.DataTypes.*;
import de.c3e.ProcessManager.Executors.BlockExecutor;
import de.c3e.ProcessManager.LoadBalancing.AverageExecutionTimeBalancer;
import de.c3e.ProcessManager.LoadBalancing.IBalancer;
import de.c3e.ProcessManager.Utils.*;

import de.c3e.ProcessManager.WorkerManager.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Main manager to handle data flow.
 */
public class ProcessingManager
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private static int originIds = 1;

    private static synchronized int GetNextOriginId()
    {
        return originIds ++;
    }

    private BlockGraph graph = null;
    private BlockExecutor executor = new BlockExecutor();
    private final IWorkerManager workerManager;
    private int timeOut = 5000;
    private long StartTime;

    private IBalancer balancer = new AverageExecutionTimeBalancer();

    public ProcessingManager(IWorkerManager workerManager)
    {
        this.workerManager = workerManager;
        if (this.workerManager.getInfrastructure() != null)
        {
            this.balancer = this.workerManager.getInfrastructure().getBalancer();
        }
        this.StartTime = System.currentTimeMillis();
    }

    public void Finalize()
    {
        logger.info("Finalize Processing Manager");
        this.workerManager.Finalize();
    }

    public boolean InitGraphFromString(String graphString, List<ScriptInputParameters> inputs)
    {
        BlockGraph graph;
        if (StringUtils.isNotBlank(graphString))
        {
            graph = GraphReader.ParseFromString(graphString);
        }
        else
        {
            logger.error("no graph string");
            return false;
        }

        if (graph == null)
        {
            logger.error("no graph loaded");
            return false;
        }

        return InitManager(inputs, graph);
    }

    /**
     * Initialize the manger from a given graph, and a inputs for it
     * @param inputs the inputs for the graph.
     * @param graph The graph.
     * @return True, if the initialization was successful.
     */
    public boolean InitManager(List<ScriptInputParameters> inputs, BlockGraph graph)
    {
        GraphTransformations.ModifyGraphForUseInManager(graph);
        BlockGraphAnalyser.CheckGraph(graph);

        if (inputs != null)
        {
            FillGraphWithInputs(graph,inputs);
        }

        InitManager(graph);
        return true;
    }

    public void CollectExecutionStatistics(String dumpFolder)
    {
        ICommunicationInfrastructure comInfrastructure = this.workerManager.getInfrastructure();
        if (comInfrastructure == null)
        {return;}
        List<ExecutionTimeMeasurements> statistics = comInfrastructure.CollectExecutionInformation();

        ExecutionTimeMeasurements mainNode = new ExecutionTimeMeasurements(0);
        mainNode.add(ExecutionTimeMeasurementEntry.RuntimeEntry(this.StartTime, System.currentTimeMillis()));
        mainNode.add(this.workerManager.getInfrastructure().GetTransportStatistics());
        statistics.add(mainNode);

        StatisticsOverviewFactory.DumpToCommandLine(statistics);

        List<Map<String,Object>> serializable = new ArrayList<>();
        for (ExecutionTimeMeasurements s : statistics)
        {
            serializable.add(s.toJsonSerializableForm());
        }

        dumpFolder = FileSystemHelpers.CheckAndCorrectDirectoryPathEnd(dumpFolder);
        FileSystemHelpers.CreateFolder(GlobalSettings.WorkFolder + dumpFolder);
        File statisticsJson = new File(GlobalSettings.WorkFolder + dumpFolder + "statistics.txt");
        Map<String,Object> wrap = new HashMap<>();        wrap.put("Statistics" , serializable);
        JSONObject mapJson = new JSONObject(wrap);
        try(BufferedWriter w = new BufferedWriter(new FileWriter(statisticsJson)))
        {
            mapJson.write(w);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        File statisticsCsv = new File(GlobalSettings.WorkFolder + dumpFolder + "statistics.csv");
        {   // support multi run statistics for csv -> this is for benchmarking results
            int i=1;
            while (statisticsCsv.exists())
            {
                statisticsCsv = new File(GlobalSettings.WorkFolder + dumpFolder + "statistics_" + String.valueOf(i)+ ".csv");
                i++;
            }
        }
        try(BufferedWriter w = new BufferedWriter(new FileWriter(statisticsCsv)))
        {
            for (ExecutionTimeMeasurements s : statistics)
            {
                TextFileUtil.WriteArrayAsCsv(s.toCsvStringArray(),w);
            }
        }
        catch(IOException e)
        {
            logger.error("Exception writing file " + statisticsCsv);
            e.printStackTrace();
        }
    }

    public void DumpIntermediatesToFolder(String dumpFolder)
    {
        dumpFolder = FileSystemHelpers.CheckAndCorrectDirectoryPathEnd(dumpFolder);

        FileSystemHelpers.CreateFolder(GlobalSettings.WorkFolder + dumpFolder);

        List<BlockLink> links = this.graph.Links;
        Set<String> allreadySaved = new HashSet<>();
        Map<String, String> smallObjects = new HashMap<>();
        for (BlockLink link : links)
        {
            // skip blocks that where inserted by the graph transformation
            // in this case the string with the image name to be loaded
            if (link.DestinationBlock.Type.equals(LoadImageWorkBlock.typeName))
            {
                continue;
            }

            BlockIO originPort = link.OriginPort;

            // get date for destination
            Object dataObject = originPort.getValue();
            String originId = GraphTransformations.RemoveOriginalBlockPrefix(link.OriginBlock.Id);


            String uniquePortId = originId + "_" + originPort.Id;

            if (allreadySaved.contains(uniquePortId))
            {
                continue;
            }

            allreadySaved.add(uniquePortId);
            ICommunicationInfrastructure comInfrastructure = this.workerManager.getInfrastructure();
            if (dataObject instanceof INeedsComInfrastructe)
            {
                ((INeedsComInfrastructe) dataObject).setCommunicationInfrastructure(comInfrastructure);
            }

            if (dataObject instanceof RemoteResult)
            {
                RemoteResult remoteResult = (RemoteResult) dataObject;
                Future<Object> result = comInfrastructure.RequestObjectFromWorker(remoteResult.getWorkerId(), remoteResult.getObjectGraphPosition());
                try
                {
                    dataObject = result.get();
                } catch (Exception e)
                {
                    e.printStackTrace();
                    dataObject = null;
                }
            }

            if (dataObject instanceof AbstractImageObject)
            {
                ((AbstractImageObject) dataObject).PullAllRemoteParts();
                OutputWorkBlock block = new OutputWorkBlock();
                Map<String, Object> inputs = new HashMap<>();
                inputs.put(OutputWorkBlock.ImageInput, dataObject);
                inputs.put(OutputWorkBlock.PathInput, dumpFolder + uniquePortId + ".png");
                block.SetInputs(inputs);
                block.RunWork();
            } else if ((dataObject instanceof UnBoxedWorkBook) ||
                    (dataObject instanceof UnBoxedIcyRoi)
                    )
            {
                DataToCsvFileBlock block = new DataToCsvFileBlock();
                Map<String, Object> inputs = new HashMap<>();
                inputs.put(DataToCsvFileBlock.dataName, dataObject);
                inputs.put(DataToCsvFileBlock.fileName, dumpFolder + uniquePortId + ".csv");
                block.SetInputs(inputs);
                block.RunWork();
            } else
            {
                // the rest goes into one map, that will be saved to one single file.
                if (dataObject instanceof double[])
                {
                    smallObjects.put(uniquePortId, Arrays.toString((double[]) dataObject));
                } else
                {
                    if (dataObject != null)
                    {
                        smallObjects.put(uniquePortId, (dataObject).toString());
                    }
                }
            }
        }

        if (!smallObjects.isEmpty())
        {
            File smallObjectFile = new File(GlobalSettings.WorkFolder + dumpFolder + "smallObjects.txt");
            JSONObject mapJson = new JSONObject(smallObjects);
            try (BufferedWriter w = new BufferedWriter(new FileWriter(smallObjectFile)))
            {
                mapJson.write(w);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private final String FilePath = "BalancerData.json";
    public void SaveBalancerStatistics()
    {
        ICommunicationInfrastructure comInv =this.workerManager.getInfrastructure();
        if (comInv == null) {   return; }
        comInv.getBalancer().SaveStatistics(this.FilePath);
    }

    public void LoadBalancerStatistics()
    {
        if (this.workerManager.getInfrastructure()!=null)
        {
            this.workerManager.getInfrastructure().getBalancer().LoadStatistics(this.FilePath);
        }
    }

    boolean InitGraphFromFile(String file, List<ScriptInputParameters> inputs)
    {
        String graphString = "";
        if (file != null)
        {
            File protocol = new File(file);
            if (protocol.exists() || protocol.isFile())
            {
                logger.info("Using graph from " + file );
                graphString = TextFileUtil.FileToString(protocol);
            }
        }

        return this.InitGraphFromString(graphString,inputs);
    }


    /**
     * Init the manager only with a given graph.
     * This graph will not be modified.
     * @param graph The graph to use for initialization.
     */
    public void InitManager(BlockGraph graph)
    {
        this.graph = graph;
    }

    private void FillGraphWithInputs(BlockGraph graph, List<ScriptInputParameters> inputs)
    {
        List<String> notFound = GraphTransformations.FillGraph(graph, inputs);

        for (String nf : notFound)
        {
            logger.error(nf);
        }
    }

    public void SetTimeout(int timeout)
    {
        this.timeOut = timeout;
    }

    public boolean StartProcessingLoop()
    {
        Thread.currentThread().setName("Master Processing Loop");
        if (this.graph == null)
        {   return false; }

        long lastActivity = System.currentTimeMillis();

        try
        {
            do
            {
                // try start something ready
                {
                    List<GraphBlock> readyBlocks = BlockGraphAnalyser.GetWorkReadyBlocks(this.graph);
                    if (readyBlocks.size() > 0)
                    {
                        Queue<IWorker> workers = new LinkedBlockingDeque<>(this.workerManager.getFreeWorkers());

                        List<String> rb = new ArrayList<>();
                        for(GraphBlock b:readyBlocks){rb.add(b.Type);}

                        System.out.println("ReadyBlocks: " + readyBlocks.size() + " " + Joiner.on(" ").join(rb) + " ReadyWorkers " + workers.size()+" running: " +  executor.GetRunningBlocks());

                        if (!workers.isEmpty())
                        {

                            // reschedule blocks, that are not finished, but only if they are no longer running
                            // => in case not all parts could be started
                            this.executor.removeExecutingBlocks(readyBlocks);

                            // trivial case: do everything that can't be split
                            List<GraphBlock> unsplittableBlocks = BlockGraphAnalyser.GetUnSplittableWork(readyBlocks);
                            readyBlocks.removeAll(unsplittableBlocks);

                            MutableBoolean couldSchedule = new MutableBoolean(true);
                            long desiredBlockTime = 10;
                            {
                                for (GraphBlock block : unsplittableBlocks)
                                {
                                    if (block.uniqueScheduleId < 0 )
                                    {
                                        block.uniqueScheduleId = GetNextOriginId();
                                    }



                                    Queue<CalculationAllocation> allocation = this.balancer.ScheduleBlockOnWorkers(block, workers, desiredBlockTime);
                                    if (ScheduleAllocations(allocation,couldSchedule))
                                    {
                                        lastActivity = System.currentTimeMillis();
                                    }

                                    if (!couldSchedule.booleanValue())
                                    {
                                        logger.error("++++++++++ something is wrong, could not schedule ++++++");
                                        return false;
                                    }

                                    if (workers.isEmpty())
                                    {
                                        break;
                                    }
                                }
                            }

                            if (readyBlocks.size() > 0 && !workers.isEmpty())
                            {
                                // try to balance it in a way, that each worker works roughly 10 seconds for each sub block
                                // start all the remaining blocks
                                for (GraphBlock block : readyBlocks)
                                {
                                    if (block.uniqueScheduleId < 0 )
                                    {
                                        block.uniqueScheduleId = GetNextOriginId();
                                    }

                                    Queue<CalculationAllocation> allocation =  this.balancer.ScheduleBlockOnWorkers(block, workers, desiredBlockTime);
                                    if (this.ScheduleAllocations(allocation,couldSchedule))
                                    {
                                        lastActivity = System.currentTimeMillis();
                                    }

                                    if (!couldSchedule.booleanValue())
                                    {
                                        logger.error("++++++++++ something is wrong, could not schedule ++++++");
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }

                // collect results
                {
                    this.executor.CollectResultsFromBlocks();
                    BlockGraphAnalyser.PushResultOverLinks(this.graph);
                }

                // hibernate unneeded blocks
                {
                    List<GraphBlock> toEvict = BlockGraphAnalyser.GetEvictableBlocks(this.graph);
                    this.EvictBlocks(toEvict);
                }

                if (this.executor.IsBusy())
                {
                    lastActivity = System.currentTimeMillis();
                }

                if (this.workerManager.HadError())
                {
                    logger.error("++++++++++ something is wrong, an error in a worker occurred. ++++++");
                    return false;
                }

                // check status of work
                if (timeOut > 0)
                {
                    if (System.currentTimeMillis() - lastActivity > timeOut)
                    {
                        logger.error("++++++++++ something is wrong, no activity since " + (System.currentTimeMillis() - lastActivity) + " ms ++++++");
                        DebugHelper.BreakIntoDebug();
                        return false;
                    }
                }

                try
                {
                    // sleep a bit => wait for the message results ...
                    Thread.sleep(100);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            } while (!BlockGraphAnalyser.AllDone(this.graph));
        }
        catch (Throwable e)
        {
            logger.error("Exception in Main Processing loop ", e);
            return false;
        }
        logger.info("All done.");
        return true;
    }

    private void EvictBlocks(List<GraphBlock> toEvict)
    {
        if (toEvict == null || toEvict.size() == 0)
        {   return; }

        for (GraphBlock b : toEvict)
        {
            this.workerManager.SendEvictionNotice(b);
            if (b.getStatus() != BlockStatus.Finished)  {DebugHelper.BreakIntoDebug();}
            b.setStatus(BlockStatus.FinishedAndEvicted);
        }
    }

    private boolean ScheduleAllocations(Queue<CalculationAllocation> allocation, MutableBoolean wasOK)
    {
        // todo: get rid  of Mutable Bool, make return status type
        boolean didSomeThing  = false;

        boolean everythingWasFine = true;
        for (CalculationAllocation a : allocation)
        {
            if (a.worker == null)
            {
                DebugHelper.BreakIntoDebug();   // this should not happen, it will mean, that an allocation is lost
                continue;
            }

            a.block.workPartsScheduled++;
            PartialWorkRequest howToSplit = new PartialWorkRequest(a.OriginId,a.imageAreas,a.splitType);
            logger.info("Start execution of ready Block " + a.block.Type + " - " + a.block.Id + " (" +  a.block.workPartsScheduled + ") [" + a.block.Itteration + "] " + ((a.imageAreas==null)?"":"("+Joiner.on(" ").join(a.imageAreas)) +")" );

               if (!this.executor.StartExecution(a.block, a.worker, howToSplit))
               {
                   everythingWasFine = false;
               }
            didSomeThing = true;
        }
        wasOK.setValue(everythingWasFine);
        return  didSomeThing;
    }
}
