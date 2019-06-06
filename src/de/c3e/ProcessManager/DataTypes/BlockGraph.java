package de.c3e.ProcessManager.DataTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * A Graph of blocks representing the flow of data through the system.
 */
public class BlockGraph
{
    public List<GraphBlock> AllBlocks = new ArrayList<>();
    public List<BlockLink> Links = new ArrayList<>();
}
