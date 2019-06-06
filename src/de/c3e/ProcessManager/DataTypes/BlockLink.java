package de.c3e.ProcessManager.DataTypes;

/**
 * Representation of a link between to Graph blocks.
 */
public class BlockLink
{
    enum LinkStatus
    {
        noTransfer,
        partialTransferred,
        finishedTransfer,
        finishedNeedsLooping,
    }

    public final BlockIO OriginPort;
    public final GraphBlock  OriginBlock;
    public final BlockIO DestinationPort;
    public final GraphBlock  DestinationBlock;

    LinkStatus Status = LinkStatus.noTransfer;

    public BlockLink(BlockIO originPort, GraphBlock originBlock, BlockIO destinationPort, GraphBlock destinationBlock)
    {
        OriginPort = originPort;
        OriginBlock = originBlock;
        DestinationPort = destinationPort;
        DestinationBlock = destinationBlock;
    }


    /**
     * Ge a string representation of this Link.
     * @return The String representation.
     */
    String toDebutString()
    {
        String result = "";
        result += this.OriginBlock != null ? this.OriginBlock.Id + " " +this.OriginBlock.Type : " ";
        result += " : ";

        result += this.OriginPort != null ? this.OriginPort.Name  : " ";
        result += " => ";

        result += this.DestinationBlock != null ? this.DestinationBlock.Id + " " +this.DestinationBlock.Type : " ";
        result += " : ";

        result += this.DestinationPort != null ? this.DestinationPort.Name  : " ";

        return result;
    }
}
