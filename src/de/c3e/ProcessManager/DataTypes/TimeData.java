package de.c3e.ProcessManager.DataTypes;

import de.c3e.ProcessManager.Utils.DebugHelper;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * class for holding time
 */
public class TimeData implements Serializable
{
    private TimeUnit storageUnit;
    private long value;

    public TimeData(long value, TimeUnit unit)
    {
        this.storageUnit = unit;
        this.value = value;
    }

    public static TimeData FromMilliSeconds(long value)
    {
        return new TimeData(value,TimeUnit.MILLISECONDS);
    }

    @Override
    public String toString()
    {
        return  String.valueOf(this.toUnit(TimeUnit.MILLISECONDS)) + "\tmillis";
    }

    public static TimeData FromString(String foo)
    {
        String millisecends= foo.split("\t")[0];
        return FromMilliSeconds(Long.parseLong(millisecends));
    }

    public void addTime(TimeData timeNeeded)
    {
        this.value += timeNeeded.toUnit(this.storageUnit);
    }

    public long toUnit(TimeUnit destinatinUnit)
    {
        switch (destinatinUnit)
        {
            case MILLISECONDS:
                return this.storageUnit.toMillis(this.value);

            case SECONDS:
                return this.storageUnit.toSeconds(this.value);

            default:
                DebugHelper.BreakIntoDebug();
        }

        return 0;
    }


}
