package de.c3e.ProcessManager.WorkerManager;

import de.c3e.ProcessManager.DataTypes.ResultGraphPosition;
import de.c3e.ProcessManager.LoadBalancing.IBalancer;
import de.c3e.ProcessManager.TransportLayer.ITransportLayer;
import de.c3e.ProcessManager.Utils.*;
import de.c3e.ProcessManager.WorkerManager.Messages.*;
import org.slf4j.Logger;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.*;

/**
 * Com infrastructure that uses a transport layer to receive and send stuff
 */
class SendReceiveInfrastructure implements ICommunicationInfrastructure
{
    private static final int MasterNodeId = 0;
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private final List<RemoteWorkerMasterSide> registeredRemoteWorkers = Collections.synchronizedList(new ArrayList<RemoteWorkerMasterSide>());
    private final List<IWorker> localWorkers = Collections.synchronizedList(new ArrayList<IWorker>());
    private final ITransportLayer transportLayer;

    private final Map<ResultGraphPosition, Object> results =  new ConcurrentSkipListMap<>();

    private final IntSet openAnnounces = new IntSet();

    private final List<ErrorMessage> errors = new ArrayList<>();

    private final SendReceiveInfrastructure thisInfrastructure = this;
    private ExecutorService executorService = Executors.newFixedThreadPool(2);  // need at least 2: 1 for scheduling / 1 for sending
    private ExecutorService requestExecutorService = Executors.newFixedThreadPool(2);  // work on request threads with separate worker to prevents deadlocks
    private ExecutorService evictionService = Executors.newFixedThreadPool(1);  // 1 thread to evict data from results


    // todo: think about removing this => measurements are send as soon as the are recorded
    private ExecutionTimeMeasurements executionStatistics;      // the statistics of this worker
    final private Map<Integer,ExecutionTimeMeasurements> remoteExecutionStatistics = new HashMap<>();      // the statistics of the other workers

    final private IBalancer balancer;
    private final List<Future> sendMessages = Collections.synchronizedList(new ArrayList<Future>());    // keep the futures, for error checking

    private void ShutdownExecutors()
    {
        this.executorService.shutdown();
        this.requestExecutorService.shutdown();
        this.evictionService.shutdown();

        try
        {
            if (!this.executorService.awaitTermination(5,TimeUnit.SECONDS))
            {
                logger.error("Could not finish remaining tasks in 5 seconds ");
            }
        }
        catch (Exception ex)
        {

        }

        try
        {
            if (!this.requestExecutorService.awaitTermination(5,TimeUnit.SECONDS))
            {
                logger.error("Could not finish remaining tasks 2 in 5 seconds ");
            }
        }
        catch (Exception ex)
        {

        }

        try
        {
            if (!this.evictionService.awaitTermination(5,TimeUnit.SECONDS))
            {
                logger.error("Could not finish remaining tasks 2 in 5 seconds ");
            }
        }
        catch (Exception ex)
        {

        }
    }

    private Thread checkForMessagesThread = new Thread()
    {
        public void run()
        {
            // basic endless thread as shown in http://docs.oracle.com/javase/7/docs/technotes/guides/concurrency/threadPrimitiveDeprecation.html
            Thread thisThread = Thread.currentThread();
            this.setName("Send/Receive loop of Worker " + transportLayer.getMyRank());
            while (checkForMessagesThread == thisThread)
            {
                    CheckAndCleanupMessageLog();

                    Serializable objectReceived = transportLayer.ReceiveObject();
                    if (objectReceived instanceof MessageBase)
                    {
                        MessageBase message = (MessageBase)objectReceived;
                        int originId = message.getMessageOriginId();

                        LogMessageToStatistics(message,false);

                        if (message instanceof WorkerReadyMessage)
                        {
                            if (transportLayer.getMyRank() == MasterNodeId)
                            {
                                synchronized (registeredRemoteWorkers)
                                {
                                    // this part will create a worker directory on the master node
                                    while (registeredRemoteWorkers.size() <= originId)
                                    {
                                        registeredRemoteWorkers.add(null);
                                    }

                                    if (registeredRemoteWorkers.get(originId) == null)
                                    {
                                        registeredRemoteWorkers.set(originId, new RemoteWorkerMasterSide(originId, thisInfrastructure));
                                    }


                                    boolean ready = ((WorkerReadyMessage) message).isReady();
                                    LogMessageAsInfo("Received Ready message from Worker [" + originId + "] ready: " + ready);
                                    registeredRemoteWorkers.get(originId).SetReadyState(ready);
                                    continue;
                                }
                            }
                            else
                            {
                                logger.error("Ready messages should go to the maser node");
                            }
                        }

                        if (message instanceof StartWorkMessage)
                        {
                            final StartWorkMessage m = (StartWorkMessage) message;
                            if (m.getHowToSplit() != null  && m.getHowToSplit().ImageAllocation != null )
                            {

                                LogMessageAsInfo("Received Start work message : " + m.getWorkType() + "[" + m.getSchedulingId() + "]\t" + SubSetFinder.ListToString(m.getHowToSplit().ImageAllocation));
                            }
                            else
                            {
                                LogMessageAsInfo("Received Start work message : " + m.getWorkType() + "[" + m.getSchedulingId() + "]" + "   " + m.toString());
                            }

                            // push this on a different thread, scheduling the block will probably
                            // request data from other workers => needs a running Message Loop
                            executorService.execute(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    IWorker worker =localWorkers.get(0);
                                    worker.SetWorkType(m.getWorkType());
                                    ConnectObjectsToInfrastructure(m.getInputs().values());
                                    worker.ScheduleBlock(m.getInputs(),m.getSchedulingId(), m.getBlockID(), m.getHowToSplit());
                                }
                            });
                            continue;
                        }

                        if (message instanceof RequestObjectMessage)
                        {
                            RequestObjectMessage m = (RequestObjectMessage) message;
                            LogMessageAsInfo("Received object request message : "  + m.getRequestedObjectId() );
                            SendObjectToWorker(m.getMessageOriginId(),m.getRequestedObjectId());
                            continue;
                        }

                        if (message instanceof SendObjectMessage)
                        {
                            SendObjectMessage m = (SendObjectMessage) message;
                            LogMessageAsInfo("Received object : "  + m.getObjectId() + m.getObject() + "  " + m.toString());
                            AddResult(m.getObjectId(),m.getObject());
                            continue;
                        }

                        if (message instanceof ResultReadyOrUpdatedMessage)
                        {
                            ResultReadyOrUpdatedMessage m = (ResultReadyOrUpdatedMessage) message;
                            String remoteID = m.getObject() instanceof IRemoteObject ? ((IRemoteObject) m.getObject()).getObjectGraphPosition() +" ": "";
                            LogMessageAsInfo("Received result notification from Worker [" + m.getMessageOriginId() +"] : " + remoteID + m.getObject() + " :: AnnouncementID =" + m.getAnnouncementId() );
                            RemoteWorkerMasterSide worker = registeredRemoteWorkers.get(m.getMessageOriginId());
                            worker.AddResult(m.getPort(),m.getObject());

                            if (m.getAnnouncementId() > 0)
                            {
                                ResultAnnouncementAcknowledgeMessage reply = new ResultAnnouncementAcknowledgeMessage(m.getAnnouncementId());
                                SendMessage(reply,m.getMessageOriginId());
                            }

                            continue;
                        }

                        if (message instanceof RequestObjectPartMessage)
                        {
                            RequestObjectPartMessage m = (RequestObjectPartMessage) message;
                            RequestImagePartMessage m_img = null;
                            if (m instanceof RequestImagePartMessage)
                            {
                                m_img =(RequestImagePartMessage) m;
                                LogMessageAsInfo("Received request for object part from Worker [" + m.getMessageOriginId() +"] : "
                                                + m_img.getPartIdentification() + " of " + m.getFullObjectId() + " Subset " + m_img.getSubSet());
                            }
                            else
                            {
                                LogMessageAsInfo("Received request for object part from Worker [" + m.getMessageOriginId() +"]" );
                            }

                            Object fullObject = results.get(m.getFullObjectId());

                            // todo: generalise what it means to support having sub blocks
                            if (fullObject instanceof AbstractImageObject)
                            {
                                if (m_img == null)  { DebugHelper.BreakIntoDebug();}

                                AbstractImageObject  fullImage = (AbstractImageObject) fullObject;
                                ImageSubSet requestedSubSet =  m_img.getSubSet();

                                boolean wasEvicted = fullImage.isEvicted();
                                if (wasEvicted)
                                {
                                    fullImage.DeEvict();
                                }

                                List<ImageSubBlock> requested = ImageSubBlockUtils.ConstructSubBlocksFromParts(Collections.singletonList(requestedSubSet),fullImage.getLocalPartsSubBlocks());

                                if (requested != null && requested.size()>0)
                                {
                                    for (ImageSubBlock block : requested)
                                    {
                                        SendObjectPartMessage reply = new SendObjectPartMessage(block, m.getFullObjectId());
                                        SendMessage(reply, m.getMessageOriginId());
                                    }
                                }
                                else
                                {
                                    logger.error("Something went wrong. Could not provide requested sub image");
                                    DebugHelper.BreakIntoDebug();
                                }

                                if (wasEvicted)
                                {
                                    fullImage.Evict();
                                }
                            }else if (fullObject instanceof IPartialResult)
                            {
                                IPartialResult full = (IPartialResult) fullObject;
                                SendObjectPartMessage reply = new SendObjectPartMessage(full.GetLocalParts(transportLayer.getMyRank()), m.getFullObjectId());
                                SendMessage(reply, m.getMessageOriginId());
                            }

                            continue;
                        }

                        if (message instanceof ResultAnnouncementAcknowledgeMessage)
                        {
                            ResultAnnouncementAcknowledgeMessage m = (ResultAnnouncementAcknowledgeMessage) message;
                            LogMessageAsInfo("Received acknowledge of result announcement from [" + m.getMessageOriginId() +"] for AnnouncementID " + m.getAnnouncementId());
                            synchronized (openAnnounces)
                            {
                                openAnnounces.remove(m.getAnnouncementId());
                            }
                            continue;
                        }

                        if (message instanceof SendObjectPartMessage)
                        {
                            SendObjectPartMessage m = (SendObjectPartMessage) message;
                            Object fullObject = results.get(m.getFullObjectId());
                            if (fullObject instanceof IPartialResult)
                            {
                                IPartialResult localCounterpart = ((IPartialResult) fullObject);
                                localCounterpart.AddPart(m.getObjectPart());
                            }

                            if (m.getObjectPart() instanceof  ImageSubBlock)
                            {
                                LogMessageAsInfo("Received object part from [" + m.getMessageOriginId() + "]  of " + m.getFullObjectId() + " " +  ((ImageSubBlock)m.getObjectPart()).dimensions);
                            }
                            else if (fullObject != null)
                            {
                                LogMessageAsInfo("Received object part from [" + m.getMessageOriginId() + "]  of " + m.getFullObjectId());
                            }
                            continue;
                        }

                        if (message instanceof RequestStatisticsMessage)
                        {
                            RequestStatisticsMessage m = (RequestStatisticsMessage) message;
                            LogMessageAsInfo("Request execution statistics ");
                            LogTransportLayerStatistics();
                            SendCompleteStatisticMessage reply = new SendCompleteStatisticMessage(executionStatistics);
                            SendMessage(reply,m.getMessageOriginId());
                            continue;
                        }

                        if (message instanceof SendCompleteStatisticMessage)
                        {
                            SendCompleteStatisticMessage m = (SendCompleteStatisticMessage) message;
                            LogMessageAsInfo("Received complete statistics from " + m.getMessageOriginId());
                            statistics.put(m.getMessageOriginId(),m.getStatistics());
                            continue;
                        }

                        if (message instanceof SendStatisticMessage)
                        {
                            SendStatisticMessage m = (SendStatisticMessage) message;
                            LogMessageAsInfo("Received statistics from " + m.getMessageOriginId());
                            Integer origin = m.getMessageOriginId();
                            if (!remoteExecutionStatistics.containsKey(origin))
                            {
                                remoteExecutionStatistics.put(origin, new ExecutionTimeMeasurements(origin));
                            }

                            remoteExecutionStatistics.get(m.getMessageOriginId()) .add(m.getStatistics());
                            continue;
                        }

                        if (message instanceof ErrorMessage)
                        {
                            ErrorMessage m = (ErrorMessage) message;
                            LogMessageAsInfo("Received error message from [" + m.getMessageOriginId() +"] : " + m.getError());
                            synchronized (errors)
                            {
                                errors.add(m);
                            }
                            continue;
                        }

                        if (message instanceof RequestEvictionMessage)
                        {
                            LogMessageAsInfo("Received eviction message");
                            RequestEvictionMessage m = (RequestEvictionMessage) message;
                            final String id = m.getBlockIdToEvict();
                            requestExecutorService.execute(new Runnable()
                                                                 {
                                                                     @Override
                                                                     public void run()
                                                                     {  EvictDataOfBlock(id);}
                                                                 });
                            continue;
                        }

                        if (message instanceof FinalizationMessage)
                        {
                            LogMessageAsInfo("Received finalization message quitting message loop");
                            for (IWorker w : localWorkers )
                            {
                                w.Finalize();
                            }
                            ShutdownExecutors();
                            LogMessageAsInfo("Send Receive loop finished for " + getMyRank());
                            return;
                        }


                    }

                Wait();
            }
            LogMessageAsInfo("Send Receive loop finished for " + getMyRank());
        }
    };

    private void LogTransportLayerStatistics()
    {
        if (this.executionStatistics == null)
        {   return;}

        this.executionStatistics.add(this.GetTransportStatistics());
    }

    public ExecutionTimeMeasurementEntry GetTransportStatistics()
    {
        return  ExecutionTimeMeasurementEntry.TransportLayerStatistic(this.transportLayer.getStatistics());
    }

    private void LogMessageToStatistics(MessageBase message, boolean send)
    {
        if (message instanceof SendStatisticMessage)
        {return;}

        if (this.executionStatistics == null)
        {   return;}

        if (send)
        {
            this.executionStatistics.add(ExecutionTimeMeasurementEntry.MessageSend(System.currentTimeMillis(), message.getClass().toString()));
        }
        else
        {
            this.executionStatistics.add(ExecutionTimeMeasurementEntry.MessageReceived(System.currentTimeMillis(), message.getClass().toString()));
        }
    }

    // todo: refactor in class
    // this is needed, because i record every send message, to check if something went wrong
    private void CheckAndCleanupMessageLog()
    {
        synchronized (this.sendMessages)
        {
            for (int i= this.sendMessages.size()-1;i>=0; i--)
            {
                Future f = this.sendMessages.get(i);
                if(f.isCancelled())
                {
                    logger.error("Message Send was canceled : ");
                    this.ReportErrorStatus(ErrorStatus.SendError);
                }

                if (f.isDone())
                {
                    try
                    {
                        f.get();
                        this.sendMessages.remove(i);
                    }
                    catch (Throwable e)
                    {
                        logger.error("Message Send threw exception : " ,e);
                        DebugHelper.BreakIntoDebug();
                        this.ReportErrorStatus(ErrorStatus.SendError);
                    }
                }
            }
        }
    }

    private static void Wait()
    {
        try
        {
            Thread.sleep(50);
        } catch (InterruptedException e){e.printStackTrace();}
    }

    /**
     * Construct a new SendReceiveInfrastructure;
     * @param transport The transport layer to be used
     * @param balancer The balancer that should be used.
     */
    SendReceiveInfrastructure(ITransportLayer transport, IBalancer balancer)
    {
        this.transportLayer = transport;
        this.checkForMessagesThread.start();
        this.balancer = balancer;
        if (balancer != null)
        {
            this.balancer.RegisterExecutionStatistics(this.remoteExecutionStatistics);
        }
        this.transportLayer.SetReportingFunction(new Consumer<ExecutionTimeMeasurementEntry>()
        {
            @Override
            public void accept(ExecutionTimeMeasurementEntry parameter)
            {
                AcceptTransportLayerReporting(parameter);
            }
        });
    }

    /**
     * Register a list of time measurements to the Infrastructure.
     * The infrastructure will decide how and when to synchronise this.
     * Or a synchronisation can be requested.
     * @param statistics The list to the statistics
     */
    void RegisterExecutionStatistics( ExecutionTimeMeasurements statistics)
    {
        this.executionStatistics = statistics;
        this.executionStatistics.setStartTime(System.currentTimeMillis());
        this.executionStatistics.setWorkerId(this.getMyRank());
    }

    Object AddRemoteResult(IRemoteObject object)
    {
        if (object instanceof IPartialResult)
        {
            if (this.results.containsKey(object.getObjectGraphPosition()))
            {
                Object image = this.results.get(object.getObjectGraphPosition());
                if (image instanceof IPartialResult)
                {
                    ((IPartialResult)image).AddPart(object);
                }
                return image;
            }
        }

        this.AddResult(object.getObjectGraphPosition(), object);
        return object;
    }

    private void ConnectObjectsToInfrastructure(Collection<Object> values)
    {
        for (Object o : values)
        {
            if (o instanceof INeedsComInfrastructe)
            {
                ((INeedsComInfrastructe) o).setCommunicationInfrastructure(this);
            }
        }
    }

    private void LogMessageAsInfo(String info)
    {
        logger.info( "{ThreadId " + Thread.currentThread().getId() + "}[" + this.transportLayer.getMyRank() + "]" + info);
    }

    private void Connect(Object object)
    {
        if (object instanceof INeedsComInfrastructe)
        {
            ((INeedsComInfrastructe) object).setCommunicationInfrastructure(this);
        }
    }

    synchronized private void AddResult(ResultGraphPosition objectGraphPosition, Object object)
    {
        logger.info(this.getMyRank() + " AddResult " + objectGraphPosition + " " + object.getClass() + " " + object.hashCode() + " " + object.toString() + ((object instanceof AbstractImageObject)? ((AbstractImageObject) object).getFullBounds() : ""));
        if (this.results.containsKey(objectGraphPosition))
        {
            DebugHelper.BreakIntoDebug();   /// replacing stuff should be the exception
        }
        this.results.put(objectGraphPosition, object);
    }

    private void SendObjectToWorker(final int messageOriginId, final ResultGraphPosition requestedObjectId)
    {
        executorService.execute(new Runnable()
        {
            @Override
            public void run()
            {
                Object value = results.get(requestedObjectId);
                SendObjectMessage message = new SendObjectMessage(requestedObjectId, value);
                SendMessage(message, messageOriginId);
            }
        });
    }

    @Override
    public Future<Object> RequestObjectFromWorker(int workerId, final ResultGraphPosition objectPosition)
    {
        final int worker = workerId;

        return requestExecutorService.submit(new Callable<Object>()
        {
            @Override
            public Object call() throws Exception
            {
                RequestObjectMessage request = new RequestObjectMessage(objectPosition);
                SendMessage(request, worker);
                while (!results.containsKey(objectPosition))
                {   Wait();    }

                while (results.get(objectPosition) instanceof RemoteResult)
                {   Wait();    }


                return results.get(objectPosition);
            }
        });
    }

    @Override
    public void RegisterLocalWorker(IWorker newOne)
    {
        this.localWorkers.add(newOne);
    }

    @Override
    public List<? extends IWorker> GetRegisterWorkers()
    {
        return this.registeredRemoteWorkers;
    }

    @Override
    public void RegisterResult(Object value , ResultGraphPosition position)
    {
        this.AddResult(position,value);
    }

    // todo Local and sendreceive share logic for registering results => base class ?
    @Override
    public void RegisterPartialResult(Object value, ResultGraphPosition position, PartialWorkRequest usedSplit, String output)
    {
        if (!InfrastructureTools.IsPartialType(value))
        {
            RegisterResult(value,position);
            return;
        }

        synchronized (results)
        {
            if (!this.results.containsKey(position))
            {
                // create a new part. Since I have full information about the id it will get (originId + port) here, I don't need to do
                // centralised id management (technical originId was generated centrally when scheduling this block)
                RegisterResult(value,position);
            }
            else
            {
                Object alreadyThere = this.results.get(position);
                if (alreadyThere instanceof  IPartialResult)
                {
                    ((IPartialResult) alreadyThere).AddPart(value);
                }
            }
        }
    }


    @Override
    public void ReportReadyState()
    {
        WorkerReadyMessage ready = new WorkerReadyMessage(true);
        this.SendMessage(ready,MasterNodeId);
    }

    @Override
    public void Finalize()
    {
        // stop the tread as shown in  http://docs.oracle.com/javase/7/docs/technotes/guides/concurrency/threadPrimitiveDeprecation.html
        // Thread tmp = this.checkForMessagesThread;
        this.checkForMessagesThread= null;

        FinalizationMessage msg = new FinalizationMessage();
        logger.info("Finalize " + this.registeredRemoteWorkers.size());
        for(int i = 1; i<this.registeredRemoteWorkers.size();i++)
        {
            logger.info("Send finalization for " + i);
            // todo: potential bug, don't use i => use correct rank
            this.SendMessage(msg, i);
        }

        ShutdownExecutors();
        logger.info(this.getMyRank() + " Send Receive finalized");
    }

    public List<ExecutionTimeMeasurements> CollectExecutionInformation()
    {
        List<Future<ExecutionTimeMeasurements>> statistics = new ArrayList<>();

        for(int i = 1; i<this.registeredRemoteWorkers.size();i++)
        {
            logger.info("Send statistics request for " + i);
            RemoteWorkerMasterSide worker = this.registeredRemoteWorkers.get(i);
            statistics.add(this.RequestExecutionStatisticsFromWorker(worker.getRemoteId()));
        }

        try
        {
            List<ExecutionTimeMeasurements> result =AsyncHelpers.FuturesToList(statistics);
            logger.info("Collected statistics.");
            return result;
        }
        catch (Exception e)
        {
            logger.error("Could not get statistics " + e.toString());
            return null;
        }
    }

    @Override
    public void AnnounceResults(Map<String, Object> resultsToAnnounce)
    {
        for (Map.Entry<String,Object> r : resultsToAnnounce.entrySet())
        {
            synchronized (this.openAnnounces)
            {
                int announcementId = this.openAnnounces.getRandomNonContained();

                this.openAnnounces.add(announcementId);

                ResultReadyOrUpdatedMessage m = new ResultReadyOrUpdatedMessage(r.getKey(), r.getValue(), announcementId);
                logger.info("Send Result Announcement [" + this.transportLayer.getMyRank() + "] : " + announcementId);
                this.SendMessage(m, MasterNodeId);
            }
        }

        boolean done = false;
        while(!done)
        {
            synchronized (this.openAnnounces)
            {
                done = this.openAnnounces.size() == 0;
            }

            Wait();
        }
    }

    @Override
    public int getMyRank()
    {
        return this.transportLayer.getMyRank();
    }

    @Override
    public void RequestPartOfPartialObject(RemoteObjectPartRequest remotePart)
    {
        if (remotePart.Block.getWorkerId() == this.getMyRank())
        {
            logger.trace("Request Part from self through sending.");
            DebugHelper.BreakIntoDebug();
        }

        RequestObjectPartMessage m = ObjectRequestMessageFactory.ConstructRequestMessage(remotePart);
        this.SendMessage(m,remotePart.Block.getWorkerId());
    }

    @Override
    public Object RegisterInput(ResultGraphPosition key, Object value)
    {
        if (key == null)
        {
            // this should only happen for locally injected objects
            return value;
        }

        if (!this.results.containsKey(key))
        {
            if (value != null)
            {
                this.Connect(value);
                this.AddResult(key, value);
                return value;
            }
        }
        else
        {
            Object localValue = this.results.get(key);
            if( localValue instanceof IPartialResult)
            {
                ((IPartialResult) localValue).AddPart(value);
            }
            return localValue;
        }

        return this.results.get(key);
    }

    void SendMessage(final MessageBase message,final int destination)
    {
        synchronized (sendMessages)
        {
            try
            {
                sendMessages.add(executorService.submit(new Callable<Object>()
                {
                    @Override
                    public Object call() throws Exception
                    {
                        try
                        {
                            message.setOriginId(transportLayer.getMyRank());
                            boolean res = transportLayer.SendObjectTo(message, destination);
                            LogMessageToStatistics(message, true);

                            if (!res)
                            {
                                logger.error("Error Sending Message");
                                ReportErrorStatus(ErrorStatus.SendError);
                            }
                        } catch (Exception e)
                        {
                            logger.error("Exception in message sending to " + destination + " : " + e.toString() + "\n" + message.toString());
                            DebugHelper.BreakIntoDebug();
                            ReportErrorStatus(ErrorStatus.SendError);
                        }
                        return null;
                    }
                }));
            }
            catch (Exception ex)
            {
                DebugHelper.PrintException(ex,logger);
                DebugHelper.BreakIntoDebug();

                throw ex;   // rethrow
            }
        }
    }


    final private Map<Integer, ExecutionTimeMeasurements> statistics = new HashMap<>();

    @Override
    public Future<ExecutionTimeMeasurements> RequestExecutionStatisticsFromWorker(int workerId)
    {
        final int worker = workerId;
        statistics.remove(workerId);
        RequestStatisticsMessage request = new RequestStatisticsMessage();
        SendMessage(request, worker);

        return requestExecutorService.submit(new Callable<ExecutionTimeMeasurements>()
        {
            @Override
            public ExecutionTimeMeasurements call() throws Exception
            {
                while (!statistics.containsKey(worker))
                {   Wait();    }

                return statistics.get(worker);
            }
        });
    }


    @Override
    public void ReportErrorStatus(ErrorStatus error)
    {
        ErrorMessage err = new ErrorMessage(error);
        this.SendMessage(err,MasterNodeId);
    }

    @Override
    public List<ErrorStatus> getErrors()
    {
        List<ErrorStatus> statuses = new ArrayList<>();
        synchronized (this.errors)
        {
            for (ErrorMessage error : this.errors)
            {
                    statuses.add(error.getError());
            }
        }

        return statuses;
    }

    @Override
    public void ReportTimeMeasurement(ExecutionTimeMeasurementEntry toReport)
    {
        SendStatisticMessage m = new SendStatisticMessage(toReport);
        this.SendMessage(m,MasterNodeId);
    }

    @Override
    public IBalancer getBalancer()
    {
        return this.balancer;
    }

    @Override
    public void BroadcastBlockEvictionNotice(String blockId)
    {
        logger.info("Send eviction notice for \"" + blockId + "\"");
        RequestEvictionMessage  msg = new RequestEvictionMessage (blockId);
        for(int i = 1; i<this.registeredRemoteWorkers.size();i++)
        {

            // todo: potential bug, don't use i => use correct rank
            this.SendMessage(msg, i);
        }
    }

    private void AcceptTransportLayerReporting(ExecutionTimeMeasurementEntry parameter)
    {
        this.ReportTimeMeasurement(parameter);
    }

    private  void EvictDataOfBlock(String blockId)
    {
        if (blockId == null)
        {   return; }

        synchronized (results)
        {
            List<ResultGraphPosition> keysToEvict = new ArrayList<>();
            for(ResultGraphPosition k : results.keySet())
            {
                if (k.BlockId == null)
                {continue;}

                if (k.BlockId.equals(blockId))
                {
                    keysToEvict.add(k);
                }
            }

            for(ResultGraphPosition k : keysToEvict)
            {
                Object value = results.get(k);
                if(value instanceof ISupportsEviction)
                {
                    ((ISupportsEviction) value).Evict();
                }
            }
        }
    }
}
