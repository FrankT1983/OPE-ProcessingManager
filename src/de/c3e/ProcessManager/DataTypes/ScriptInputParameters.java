package de.c3e.ProcessManager.DataTypes;

/**
 * Data structure to hold input values that can be used for blocks.
 */
public class ScriptInputParameters
{
    /**
     * Id of the Block this value should go to.
     */
    public String BlockId;

    /**
     * Is this an value for an input or an output: "in", "out"
     */
    public String Direction;

    /**
     * Name of the port.
     */
    public String PortName;

    /**
     * Value
     */
    public String Value;
}
