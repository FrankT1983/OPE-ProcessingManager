package de.c3e.ProcessManager.LoadBalancing;

import de.c3e.ProcessManager.Utils.AbstractImageObject;
import de.c3e.ProcessManager.Utils.ImageSubSet;
import de.c3e.ProcessManager.WorkerManager.IWorker;
import de.c3e.ProcessingManager.Types.SplitType;

import java.util.List;
import java.util.Queue;

public interface IAdvancedBalancer extends IBalancer
{
    /***
     * Estimate a list of Image Subset to be calculated on a given Node executing a given calculation for a given time span.
     * @param workType The type of the calculation to be run.
     * @param estimateForNodeId The node on which the calculation should be executed.
     * @param timeInSeconds The time span the calculation has to fill.
     * @param img The image object -> since it tracks where which part resides.
     * @param splitType How the image can be split into sub parts.
     * @param requesterId A id identifying the calculation in the workflow. Used to track which parts already have been handled.
     * @return The estimated subset.
     */
    List<ImageSubSet> EstimateCalculationSubsetsForTime(String workType, int estimateForNodeId, double timeInSeconds, AbstractImageObject img, SplitType splitType, long requesterId);
}
