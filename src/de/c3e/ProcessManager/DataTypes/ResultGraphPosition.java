package de.c3e.ProcessManager.DataTypes;

import java.io.Serializable;

/**
 * Specify where in the graph this value belongs
 *
 * Important:
 *  This overrides hashCode and equals to work with HashMap and other Container classes.
 *  It also overrides toString vor better debugging.
 */
public class ResultGraphPosition implements Serializable, Comparable<ResultGraphPosition>
{
    private final long uniqueScheduleId ;

    private final String PortName;

    final public String BlockId;


    public ResultGraphPosition(long blockScheduleId, String blockId, String PortName)
    {
        this.uniqueScheduleId = blockScheduleId;
        this.PortName = PortName;
        this.BlockId = blockId;
    }

    @Override
    public int hashCode()
    {
        return (int)this.uniqueScheduleId + this.PortName.hashCode();
    }

    @Override
    public boolean equals( Object o )
    {
        if (!( o instanceof ResultGraphPosition))
        {   return false;   }

        if ( o == this )
        {   return true;    }


        ResultGraphPosition that = (ResultGraphPosition) o;

        return  this.uniqueScheduleId == that.uniqueScheduleId   &&this.PortName.equals( that.PortName);
    }

    @Override
    public int compareTo(ResultGraphPosition o)
    {
        if (this.uniqueScheduleId == o.uniqueScheduleId)
        {

            return this.PortName.compareTo(o.PortName);

        }

        return (int)(this.uniqueScheduleId - o.uniqueScheduleId);
    }

    @Override
    public  String toString()
    {
        return "(" + this.BlockId + " Scheduled as Id("  +this.uniqueScheduleId + ") Port " + this.PortName;
    }
}
