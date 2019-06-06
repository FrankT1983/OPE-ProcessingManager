package de.c3e.ProcessManager.Utils;

import de.c3e.ProcessManager.DataTypes.TimeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Helper class for generating a short version of the run statistics, so I don't have to analyse each statistic file
 * => do that maybe later.
 */
public class StatisticsOverviewFactory
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public static void DumpToCommandLine(List<ExecutionTimeMeasurements> statistics)
    {
        TimeData overallTime = new TimeData(0, TimeUnit.MILLISECONDS);
        TimeData overallInitTime = new TimeData(0, TimeUnit.MILLISECONDS);
        TimeData overallInputCollection = new TimeData(0, TimeUnit.MILLISECONDS);
        TimeData overallCalculationTime = new TimeData(0, TimeUnit.MILLISECONDS);

        List<String> track = Arrays.asList("loadImage","de.c3e.ProcessManager.BlockRepository.Benchmarking.LinearDelayBlock","output" ,"DeconvolutionTool","CellProfilerTool", "de.c3e.BlockTemplates.Examples.AddXBlock", "TimePointSelection" , "HistogramLinearization");
        HashMap<String,TimeData>  timeByOp = new HashMap<>();
        HashMap<String,Integer>  timesRun = new HashMap<>();
        HashMap<String,HashMap<String,Long>>  pixelsProcessed = new HashMap<>();
        HashMap<String,List<Integer>>  timesRunOn = new HashMap<>();
        for (String t : track)
        {
            timeByOp.put(t, new TimeData(0, TimeUnit.MILLISECONDS));
            timesRun.put(t, 0);
            timesRunOn.put(t, new ArrayList<Integer>());
            pixelsProcessed.put(t, new HashMap<String, Long>());
            for (int i=0; i<10;i++)
            {
                pixelsProcessed.get(t).put(String.valueOf(i), (long)0);     // preserve a few for better table
            }
        }


        String outtext;
        String outtextTransport = "###";

        for (ExecutionTimeMeasurements measurement :
                statistics)
        {
            if (measurement.getWorkerId() == 0)
            {
                List<ExecutionTimeMeasurementEntry> res = measurement.EntriesOfMeasurementTyp(ExecutionTimeMeasurementEntry.MeasuredType.Running);
                if (res.size() == 1)
                {
                    // master node
                    overallTime = res.get(0).getTimeNeeded();
                }
                else
                {
                    DebugHelper.BreakIntoDebug();
                }
            }
            else
            {
                int workerId = measurement.getWorkerId();

                AddTime(overallInitTime       , measurement,ExecutionTimeMeasurementEntry.MeasuredType.Initialization );
                AddTime(overallInputCollection, measurement,ExecutionTimeMeasurementEntry.MeasuredType.InputCollection );
                AddTime(overallCalculationTime, measurement,ExecutionTimeMeasurementEntry.MeasuredType.Calculation );

                List<ExecutionTimeMeasurementEntry> calculation = measurement.EntriesOfMeasurementTyp(ExecutionTimeMeasurementEntry.MeasuredType.Calculation );
                for (ExecutionTimeMeasurementEntry e : calculation)
                {
                    String key = e.getBlockExecuted();
                    if (timeByOp.containsKey( key))
                    {
                        timeByOp.get(key).addTime(e.getTimeNeeded());
                        timesRun.put(key,timesRun.get(key) + 1 );
                        timesRunOn.get(key).add(workerId);

                        String keyString = String.valueOf(workerId);
                        HashMap<String,Long> foo  = pixelsProcessed.get(key);

                        if (!foo.containsKey(keyString))
                        {   foo.put(keyString,(long)0); }
                        long pixelCount =  e. getInputPixelCount();
                        if (pixelCount == 0) { pixelCount = 1;}
                        foo.put(keyString,foo.get(keyString) + pixelCount);
                    }
                }
            }

            List<ExecutionTimeMeasurementEntry> sendStatistics = measurement.EntriesOfMeasurementTyp(ExecutionTimeMeasurementEntry.MeasuredType.TransportLayerStatistic );
            for(ExecutionTimeMeasurementEntry send : sendStatistics)
            {
                outtextTransport += "Node "+ measurement.getWorkerId() +"\t" + send.getBlockExecuted() + "\t";
            }
        }

        outtext  = "++Statistics:  took \t" + overallTime.toUnit(TimeUnit.MILLISECONDS) + "\t Milliseconds \t";
        outtext += "init time \t" + overallInitTime.toUnit(TimeUnit.MILLISECONDS) + "\t Milliseconds \t";
        outtext += "input prep \t" + overallInputCollection.toUnit(TimeUnit.MILLISECONDS) + "\t Milliseconds \t";

        for (String key : track)
        {
            outtext += "calculation \t  " + key + "\t";
            outtext += "took \t" +  timeByOp.get(key).toUnit(TimeUnit.MILLISECONDS) + " \t Milliseconds \t";
            outtext += "was run \t" +  timesRun.get(key).toString() + "\tx times \t";
        }

        logger.warn(outtext);
        logger.warn(outtextTransport);

        for ( String key:  timeByOp.keySet())
        {
            StringBuilder builder = new StringBuilder();
            HashMap<String,Long> s = pixelsProcessed.get(key);

            List<String> keys = new ArrayList<>(s.keySet());
            Collections.sort(keys);


            for (String k :keys)
            {
                builder.append("\tNode\t");
                builder.append(k);
                builder.append("\tprocessed\t");
                builder.append(s.get(k).toString());
            }

            logger.warn("||| Processing of \t" + key + "\t" + builder.toString());
        }

        for ( String key:  timeByOp.keySet())
        {
            StringBuilder builder = new StringBuilder();
            for(Integer s : timesRunOn.get(key)) {
                builder.append(s + " ");
            }

            logger.warn("*** Calculation \t" + key + "\t was run \t" + timesRun.get(key) + "\t times for a total of \t" + timeByOp.get(key) + "\t on nodes\t [" + builder.toString().trim() + "]");
        }

    }

    private static void AddTime(TimeData overallCalculationTime, ExecutionTimeMeasurements measurement, ExecutionTimeMeasurementEntry.MeasuredType measurementTyp)
    {
        List<ExecutionTimeMeasurementEntry> res2 = measurement.EntriesOfMeasurementTyp(measurementTyp);
        for (ExecutionTimeMeasurementEntry e : res2)
        {
            overallCalculationTime.addTime(e.getTimeNeeded());
        }
    }
}


