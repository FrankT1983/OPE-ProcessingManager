package de.c3e.ProcessManager.BlockRepository;

import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.ProcessManager.Utils.AbstractTableObject;
import de.c3e.ProcessManager.Utils.DebugHelper;
import de.c3e.ProcessManager.Utils.ImageSubSet;
import de.c3e.ProcessManager.Utils.LogUtilities;
import de.c3e.ProcessingManager.Types.IMergePostProcessing;
import de.c3e.ProcessingManager.Types.TableMergePostprocessing;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Collection;

class TableMergeModifierImpl extends MergePostProcessing
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private String ColumnPrefix = "";
    private final TableMergePostprocessing.Mode mode;

    public TableMergeModifierImpl(TableMergePostprocessing mergePostprocessing)
    {
        super();
        this.mode = mergePostprocessing.getMode();
    }

    @Override
    public void DoModifications(Object toModify, ImageSubSet dimensions, Collection<ImageDimension> trackingDimensions)
    {
        if (toModify instanceof AbstractTableObject)
        {
            AbstractTableObject table = (AbstractTableObject)toModify;

            for (ImageDimension dim : trackingDimensions)
            {
                if (dimensions.getSize(dim) != 1)
                {
                    DebugHelper.BreakIntoDebug();
                }

                String columnName = ColumnPrefix + dim.name();
                int value= dimensions.getStart(dim);
                if (table.getHeader() != null)
                {
                    if (!table.getHeader().contains(columnName))
                    {
                        table.getHeader().add(columnName);
                    }
                }

                int insertColumn = table.getColumnIndex(columnName);

                for (int i = 0; i < table.getRowCount(); i++)
                {
                    table.set(i, insertColumn, value);
                }
            }
        }
    }
}


public class MergeModifierImplementationFactory
{
    static public MergePostProcessing getImplementation(IMergePostProcessing mergePostProcessing)
    {
        if (mergePostProcessing instanceof TableMergePostprocessing)
        {
                return new TableMergeModifierImpl((TableMergePostprocessing) mergePostProcessing);
        }

        return null;
    }
}
