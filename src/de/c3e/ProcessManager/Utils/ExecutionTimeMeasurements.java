package de.c3e.ProcessManager.Utils;

import java.io.Serializable;
import java.util.*;

public class ExecutionTimeMeasurements implements Serializable
{
    private final List<ExecutionTimeMeasurementEntry> entries = Collections.synchronizedList(new ArrayList<ExecutionTimeMeasurementEntry>());
    private long startTime;
    private int workerId;

    public  ExecutionTimeMeasurements(){}
    public  ExecutionTimeMeasurements( int worker)
    {
        this.workerId = worker;
    }

    public void add(ExecutionTimeMeasurementEntry entry)
    {
        this.entries.add(entry);
    }

    public List<ExecutionTimeMeasurementEntry> get() {return  this.entries; }

    public void setStartTime(long startTime)
    {
        this.startTime = startTime;
    }

    public int getWorkerId()
    {   return this.workerId;}

    public void setWorkerId(int workerId)
    {
        this.workerId = workerId;
    }

    public Map<String, Object> toJsonSerializableForm()
    {
        Map<String, Object> result = new HashMap();
        result.put("Worker", this.workerId);
        result.put("Start", this.startTime);

        List<Map<String,Object>> es = new ArrayList<>();

        for (ExecutionTimeMeasurementEntry e : this.entries)
        {
            es.add(e.toJsonSerializableForm());
        }

        result.put("Entries", es);
        return result;
    }

    public String[][] toCsvStringArray()
    {
        String[][] array = new String[this.entries.size()+1][];

        int entryCount = 6;
        // header
        {
            String[] entry = new String[entryCount];
            entry[0] = "WorkerId";
            entry[1] = "Measured";
            entry[2] = "BlockType";
            entry[3] = "StartTime";
            entry[4] = "TimeNeeded";
            entry[5] = "Pixels Computed";
            array[0] = entry;
        }

        // data
        int i = 1;
        for (ExecutionTimeMeasurementEntry e : this.entries)
        {
            String[] entry = new String[entryCount];
            entry[0] = String.valueOf(this.workerId);
            entry[1] = e.getBlockExecuted();
            entry[2] = e.getMeasured().name();
            entry[3] = String.valueOf(e.getStartTime());
            entry[4] = String.valueOf(e.getTimeNeeded());
            entry[5] = String.valueOf(e.getInputPixelCount());
            array[i] = entry;
            i++;
        }

        return array;
    }

    public List<ExecutionTimeMeasurementEntry> EntriesOfMeasurementTyp(ExecutionTimeMeasurementEntry.MeasuredType measuredType)
    {
        List<ExecutionTimeMeasurementEntry> result =  new ArrayList<>();
        synchronized (entries)
        {
            for (ExecutionTimeMeasurementEntry e : entries)
            {
                if (e.getMeasured() == measuredType)
                {
                    result.add(e);
                }
            }
        }

        return result;

    }

    public List<ExecutionTimeMeasurementEntry> EntriesOfBlockType(String type)
    {
        List<ExecutionTimeMeasurementEntry> result =  new ArrayList<>();
        synchronized (entries)
        {
            for (ExecutionTimeMeasurementEntry e : entries)
            {
                if (e.getBlockExecuted().equals(type))
                {
                    result.add(e);
                }
            }
        }

        return result;
    }
}
