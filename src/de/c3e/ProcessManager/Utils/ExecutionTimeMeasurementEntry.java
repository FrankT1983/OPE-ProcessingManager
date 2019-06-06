package de.c3e.ProcessManager.Utils;

import de.c3e.ProcessManager.DataTypes.TimeData;
import de.c3e.ProcessManager.WorkerManager.Messages.SendObjectPartMessage;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Struct holding time information about the execution of a work block.
 */
public class ExecutionTimeMeasurementEntry implements Serializable
{
    private final long StartTime ;
    private final TimeData TimeNeeded ;
    private final String BlockExecuted ;
    private final MeasuredType Measured;
    private long inputPixelCount ;

    public static ExecutionTimeMeasurementEntry CalculationEntry(long startOfLastCalculation, long durationOfLastCalculation, String currentBlockType, Map<String, Object> usedInputs)
    {
        ExecutionTimeMeasurementEntry result = new ExecutionTimeMeasurementEntry(
                startOfLastCalculation,
                durationOfLastCalculation,
                currentBlockType,
                MeasuredType.Calculation);

        if (usedInputs != null)
        {
            result.inputPixelCount = GetPixelCountFromInputs(usedInputs.values());
        }
        return  result;
    }

    public static ExecutionTimeMeasurementEntry DataCollectionEntry(long start, long end)
    {
        ExecutionTimeMeasurementEntry result = new ExecutionTimeMeasurementEntry(
                start,
                end-start,
                "Unbox or boxing Data",
                MeasuredType.InputCollection);
        return result;
    }

    public static ExecutionTimeMeasurementEntry RuntimeEntry(long start, long end)
    {
        ExecutionTimeMeasurementEntry result = new ExecutionTimeMeasurementEntry(
                start,
                end-start,
                "Running",
                MeasuredType.Running);
        return result;
    }

    public static ExecutionTimeMeasurementEntry MessageReceived(long timePoint, String type)
    {
        ExecutionTimeMeasurementEntry result = new ExecutionTimeMeasurementEntry(
                timePoint,
                0,
                "Message Received:" + type,
                MeasuredType.MessageReceived);
        return result;
    }


    public static ExecutionTimeMeasurementEntry MessageSend(long timePoint, String type)
    {
        ExecutionTimeMeasurementEntry result = new ExecutionTimeMeasurementEntry(
                timePoint,
                0,
                "Message Send:" + type,
                MeasuredType.MessageSend);
        return result;
    }

    public static ExecutionTimeMeasurementEntry TransportLayerStatistic(String statistics)
    {
        ExecutionTimeMeasurementEntry result = new ExecutionTimeMeasurementEntry(
                0,
                0,
                statistics,
                MeasuredType.TransportLayerStatistic);
        return result;
    }

    public static ExecutionTimeMeasurementEntry DataTransferStatistics(long durration, Serializable object, int length, int receiver, int sender)
    {
        if (object instanceof SendObjectPartMessage)
        {
            SendObjectPartMessage m = (SendObjectPartMessage)(object);
            if (m.getObjectPart() instanceof ImageSubBlock)
            {
                long pixels = ((ImageSubBlock) m.getObjectPart()).dimensions.getPixelSize();
                ExecutionTimeMeasurementEntry result = new ExecutionTimeMeasurementEntry(
                        0,
                        TimeData.FromMilliSeconds(durration),
                        sender + " -> " + receiver,
                        MeasuredType.TransportImageDataSend, pixels);
                return result;
            }
            else
            {
                //DebugHelper.BreakIntoDebug();
            }
        }
        return null;
    }


    private static long GetPixelCountFromInputs(Collection<Object> values)
    {
        long sum = 0;
        for (Object e :
                values)
        {
            if (e instanceof AbstractImageObject)
            {
                AbstractImageObject inputImage = (AbstractImageObject) e;
                // technical not the most correct way to figure out what was actually calculated
                try
                {
                    List<ImageSubSet> bounds = inputImage.getBoundsOfLocalParts();
                    for (ImageSubSet b : bounds)
                    {
                        sum += b.getPixelSize();
                    }
                }
                catch (Exception ex)
                {
                    DebugHelper.BreakIntoDebug();
                }

            }
        }

        return sum;
    }

    public long getInputPixelCount()
    {
        return inputPixelCount;
    }



    public enum MeasuredType
    {
        Calculation,

        /**
         * Block initialisation
         */
        Initialization,

        InputCollection,

        /**
         * The runtime of the node.
         */
        Running,
        MessageReceived,
        MessageSend,
        TransportLayerStatistic,
        TransportImageDataSend,
    }

    public ExecutionTimeMeasurementEntry(long startInMilliSeconds, long durationInMilliSeconds, String block, MeasuredType measured)
    {
        this.TimeNeeded = TimeData.FromMilliSeconds(durationInMilliSeconds);
        this.BlockExecuted = block;
        this.StartTime = startInMilliSeconds;
        this.Measured = measured;
    }

    public ExecutionTimeMeasurementEntry(long startInMilliSeconds, TimeData duration, String block, MeasuredType measured, long pixels)
    {
        this.TimeNeeded = duration;
        this.BlockExecuted = block;
        this.StartTime = startInMilliSeconds;
        this.Measured = measured;
        this.inputPixelCount = pixels;
    }

    public Map<String,Object> toJsonSerializableForm()
    {
        Map<String, Object> result = new HashMap<>();
        result.put("StartTime", this.getStartTime());
        result.put("BlockType", String.valueOf(this.getBlockExecuted()));
        result.put("Duration", this.getTimeNeeded().toString());
        result.put("Type", EnumToString(this.Measured));
        result.put("Pixels", String.valueOf(this.inputPixelCount));

        return result;
    }

    public static ExecutionTimeMeasurementEntry FromJson(JSONObject entry)
    {
        long startTime = 0;
        {
            Object foo = entry.get("StartTime");
            if (foo instanceof Long)
            {
                startTime = (long) foo;
            } else if (foo instanceof String)
            {
                startTime = Long.parseLong((String) foo);
            }
        }

        long pixels = 0;
        {
            Object foo = entry.get("Pixels");
            if (foo instanceof Long)
            {
                pixels = (long) foo;
            } else if (foo instanceof String)
            {
                pixels = Long.parseLong((String) foo);
            }
        }

        MeasuredType type= null;
        {
            Object foo = entry.get("Type");
            if (foo instanceof String)
            {
                type = StringToEnum((String) foo);
            }
        }

        String  blockType= null;
        {
            Object foo = entry.get("BlockType");
            if (foo instanceof String)
            {
                blockType =(String) foo;
            }
        }

        TimeData duration = null;
        {
            Object foo = entry.get("Duration");
            if (foo instanceof String)
            {
                duration = TimeData.FromString((String) foo);
            }
        }

        ExecutionTimeMeasurementEntry result = new ExecutionTimeMeasurementEntry(startTime,duration,blockType,type,pixels);

        return result;
    }

    static private String EnumToString(MeasuredType measured)
    {
        switch (measured)
        {
            case Calculation: return "Calculation";
            case Initialization: return "Initialization";
            default:
                return measured.toString();
        }
    }

    static private MeasuredType StringToEnum(String input)
    {
        switch (input)
        {
            case "Calculation" : return MeasuredType.Calculation;
            case "Initialization" : return MeasuredType.Initialization;
            default:
                for (MeasuredType v : MeasuredType.values())
                {
                    if (input.equals(v.toString()))
                        return v;
                }

                DebugHelper.BreakIntoDebug();
        }


        return MeasuredType.Calculation;
    }

    /**
     * Get the start time.
     * @return The time of start in [ms].
     */
    long getStartTime()
    {
        return StartTime;
    }

    /**
     * Get the duration of the measurement.
     * @return The duration needed.
     */
    public TimeData getTimeNeeded()
    {
        return TimeNeeded;
    }

    /**
     * Get the name of the block that was executed
     * @return The name of the block / activity.
     */
    String getBlockExecuted()
    {
        return BlockExecuted;
    }

    public MeasuredType getMeasured()
    {
        return Measured;
    }
}


