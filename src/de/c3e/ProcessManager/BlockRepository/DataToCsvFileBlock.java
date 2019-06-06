package de.c3e.ProcessManager.BlockRepository;

import de.c3e.ProcessManager.DataTypes.UnBoxedIcyRoi;
import de.c3e.ProcessManager.DataTypes.UnBoxedWorkBook;
import de.c3e.ProcessManager.DataTypes.WorkbookToCsv;
import de.c3e.ProcessManager.GlobalSettings;
import de.c3e.ProcessManager.Utils.AbstractTableObject;
import icy.roi.ROI;
import org.apache.poi.ss.usermodel.Workbook;
import plugins.adufour.vars.lang.VarWorkbook;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



/**
 * Block for writing some kind of data to a file.
 * Should be used to annotate a file with that data
 */
public class DataToCsvFileBlock extends LoggingWorkBlockBase
{
    public static final String TypeName = "DataToCsvFileBlock";
    public static final String dataName = "data";
    public static final String fileName = "destinationFile";

    private Object outputObject;

    private String destinationPath;

    @Override
    public void SetInputs(Map<String, Object> inputs)
    {
        if (inputs.containsKey(dataName))
        {
            this.outputObject = inputs.get(dataName);
        }

        if (inputs.containsKey(fileName))
        {
            this.destinationPath = GlobalSettings.WorkFolder + inputs.get(fileName);
        }
    }

    @Override
    public Map<String, Object> GetResults()
    {
        // does not generate outputs
        return new HashMap<>();
    }

    @Override
    public boolean RunWork()
    {
        super.RunWork();
        if (this.outputObject instanceof VarWorkbook)
        {
            this.outputObject = ((VarWorkbook) this.outputObject).getValue();
        }

        if (this.outputObject instanceof UnBoxedWorkBook)
        {
            this.outputObject = ((UnBoxedWorkBook) this.outputObject).ReBox();
        }

        if (this.outputObject instanceof UnBoxedIcyRoi)
        {
            this.outputObject = ((UnBoxedIcyRoi) this.outputObject).ReBox();
        }

        if (this.outputObject instanceof Workbook)
        {
            WorkbookToFile((Workbook) this.outputObject , this.destinationPath);
            return true;
        }

        if (this.outputObject instanceof  ROI[] )
        {
            RoisToFile(( ROI[] ) this.outputObject , this.destinationPath);
            return true;
        }

        if (this.outputObject instanceof AbstractTableObject)
        {
            AbstractTableObject tab = (AbstractTableObject) this.outputObject;
            tab.StoreInCsv( this.destinationPath , true, ",");
            logger.info("Wrote " + this.destinationPath + " : " + tab.getRowCount() + " lines");
            return true;
        }


        logger.error("Could not write object " + this.outputObject + " to file");
        return false;
    }

    private void RoisToFile(ROI[] outputObject, String destinationPath)
    {
        File file = new File(destinationPath);
        FileWriter fw = null;
        BufferedWriter bw = null;
        ArrayList<String> line = null;
        StringBuffer buffer = null;
        try
        {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);

            buffer = new StringBuffer();
            buffer.append("Roi ").append(WorkbookToCsv.DEFAULT_SEPARATOR);
            buffer.append("Number of Points " ).append( WorkbookToCsv.DEFAULT_SEPARATOR);
            buffer.append("Bounds5D " ).append( WorkbookToCsv.DEFAULT_SEPARATOR);
            bw.write(buffer.toString().trim());
            bw.newLine();

            for (int i = 0; i < outputObject.length; i++)
            {

                buffer = new StringBuffer();
                ROI current = outputObject[i];

                buffer.append(i).append(WorkbookToCsv.DEFAULT_SEPARATOR);
                buffer.append(current.getNumberOfPoints()).append(WorkbookToCsv.DEFAULT_SEPARATOR);
                buffer.append(current.getBounds5D().toString().replace(WorkbookToCsv.DEFAULT_SEPARATOR, ":")).append(WorkbookToCsv.DEFAULT_SEPARATOR);

                bw.write(buffer.toString().trim());
                if(i < (outputObject.length - 1)) {
                    bw.newLine();
                }

            }
        }
        catch (Exception e)
        {
            logger.error("Error writing Roi[] to csv. " + e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (bw != null)
                {
                    bw.flush();
                    bw.close();
                }
            }
            catch (Exception e)
            {
                logger.error("Error closing stream while writing Roi[] to csv. " + e.toString());
                e.printStackTrace();
            }
        }
    }

    private static void WorkbookToFile(Workbook outputObject, String destinationPath)
    {
        WorkbookToCsv converter = new WorkbookToCsv(WorkbookToCsv.DEFAULT_SEPARATOR);
        converter.convertToCSV(outputObject);
        try
        {
            converter.saveCSVFile(new File(destinationPath));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean IsFinished()
    {
        return true;
    }
}
