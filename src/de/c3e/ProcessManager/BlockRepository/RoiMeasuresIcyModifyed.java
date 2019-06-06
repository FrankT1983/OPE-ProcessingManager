package de.c3e.ProcessManager.BlockRepository;

import de.c3e.ProcessManager.DataTypes.UnBoxedIcyRoi;
import de.c3e.ProcessManager.DataTypes.UnBoxedWorkBook;
import icy.file.FileUtil;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.roi.ROI3D;
import icy.roi.ROI5D;
import icy.sequence.Sequence;
import icy.sequence.SequenceDataIterator;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle5D;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.WorkbookUtil;
import plugins.adufour.roi.*;
import plugins.adufour.vars.lang.VarROIArray;
import plugins.adufour.vars.lang.VarSequence;
import plugins.adufour.vars.lang.VarWorkbook;

import org.apache.poi.ss.usermodel.Workbook;
import plugins.adufour.workbooks.IcySpreadSheet;
import plugins.adufour.workbooks.Workbooks;
import plugins.kernel.roi.descriptor.measure.ROIMassCenterDescriptorsPlugin;
import plugins.kernel.roi.roi2d.ROI2DArea;
import plugins.kernel.roi.roi3d.ROI3DArea;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;


/**
 * Since the Original Icy RoiMeasures class need swing stuff to even call the constructor, this is a
 * copy and paste non ui version.
 *
 * Changes: IsHeadless is always true : remove that code
 */
public class RoiMeasuresIcyModifyed extends LoggingWorkBlockBase
{
    private final VarROIArray rois                    = new VarROIArray("Regions of interest");
    private final VarWorkbook book                    = new VarWorkbook("Workbook", (Workbook) null);
    private final VarSequence sequence                = new VarSequence("Sequence", null);

    private Object imageObject;

    private boolean           measureSelectionChanged = false;

    private final String measureInput = "measures";
    private final String roiInput = "Regions of interest";
    private final String sequenceInput = "Sequence";
    private final String workbookOutput = "Workbook";

    boolean finished = false;


    @Override
    public void SetInputs(Map<String, Object> inputs)
    {
        if (inputs.containsKey(measureInput))
        {

        }

        if (inputs.containsKey(roiInput))
        {
            Object input = inputs.get(roiInput);
            if (input instanceof UnBoxedIcyRoi)
            {
                this.rois.setValue(((UnBoxedIcyRoi) input).ReBox());
            }
            else
            {
                this.rois.setValue((ROI[]) input);
            }
        }

        if (inputs.containsKey(sequenceInput))
        {
            imageObject = inputs.get(sequenceInput);
        }
    }


    public boolean RunWork()
    {
        super.RunWork();

        Workbook wb = book.getValue();

        // Create the workbook anew if:
        // - it did not exist
        // - we are in block mode (forget previous results)
        if (wb == null)
        {
            book.setValue(wb = Workbooks.createEmptyWorkbook());
        }

        updateStatistics(sequence.getValue());
        this.finished = true;
        return true;
    }

    @Override
    public Map<String, Object> GetResults()
    {
        Map<String, Object> result = new HashMap<>();
        result.put(this.workbookOutput , new UnBoxedWorkBook(this.book.getValue()));
        return result;
    }

    @Override
    public boolean IsFinished()
    {
        return this.finished;
    }

    private void updateStatistics(final Sequence sequence)
    {
        final Workbook wb = book.getValue();

        String sheetName = "ROI Statistics";

        final Sheet sheet;
        Row header = null;

        if (wb.getSheet(sheetName) != null)
        {
            sheet = wb.getSheet(sheetName);
        } else
        {
            // create the sheet
            sheet = wb.createSheet(sheetName);
            measureSelectionChanged = true;
        }

        if (measureSelectionChanged)
        {
            measureSelectionChanged = false;
            header = sheet.createRow(0);

            // create the header row
            ROIMeasures.Measures[] measures = ROIMeasures.Measures.values();

            int nbIntensityStats = 0;
            for (ROIMeasures.Measures measure : measures)
                if (measure.name().startsWith("INTENSITY") && measure.isSelected()) nbIntensityStats++;

            for (ROIMeasures.Measures measure : measures)
            {
                if (!measure.isSelected()) continue;

                int col = measure.getColumnIndex();
                String name = measure.toString();

                if (measure.name().startsWith("INTENSITY") && sequence != null)
                {
                    for (int c = 0; c < sequence.getSizeC(); c++)
                    {
                        header.getCell(col + nbIntensityStats * c).setCellValue(name + sequence.getChannelName(c));
                    }
                } else
                {
                    header.getCell(col).setCellValue(name);
                }
            }
        }

        final List<ROI> rois2Update = Arrays.asList(this.rois.getValue());
        try
        {
            IcySpreadSheet icySheet = new IcySpreadSheet(sheet);

            int rowID = 1;
            icySheet.removeRows(rowID);

            List<List<Object>> results = new ArrayList<>(rois2Update.size());

            for (ROI roi : rois2Update)
            {
                results.add(update(sequence, roi));
            }

            for (List<Object> result : results)
                updateWorkbook(wb, icySheet, rowID++, result);
        } catch (Exception e)
        {
            return;
        }

        book.valueChanged(book, null, book.getValue());
    }

    private List<Object> update(final Sequence sequence, final ROI roi2Update)
    {
        ArrayList measures = new ArrayList();
        Object roi = roi2Update;
        if(roi instanceof ROI2D && !(roi instanceof ROI2DArea)) {
            ROI2D var21 = (ROI2D)roi;
            ROI2DArea var23 = new ROI2DArea(var21.getBooleanMask(false));
            var23.setZ(var21.getZ());
            var23.setT(var21.getT());
            var23.setName(((ROI)roi).getName());
            var23.setColor(((ROI)roi).getColor());
            roi = var23;
        } else if(roi instanceof ROI3D && !(roi instanceof ROI3DArea)) {
            ROI3D center = (ROI3D)roi;
            ROI3DArea nbStats = new ROI3DArea(center.getBooleanMask(false));
            nbStats.setT(center.getT());
            nbStats.setName(((ROI)roi).getName());
            nbStats.setColor(((ROI)roi).getColor());
            roi = nbStats;
        }


        {
            String var22;
            if(ROIMeasures.Measures.FULLPATH.isSelected()) {
                var22 = "--";
                if(sequence != null && sequence.getFilename() != null) {
                    var22 = FileUtil.getDirectory(sequence.getFilename(), false);
                }

                measures.add(var22);
            }

            if(ROIMeasures.Measures.FOLDER.isSelected()) {
                var22 = "--";
                if(sequence != null && sequence.getFilename() != null) {
                    var22 = FileUtil.getDirectory(sequence.getFilename(), false);
                    var22 = var22.substring(1 + var22.lastIndexOf(File.separator));
                }

                measures.add(var22);
            }

            if(ROIMeasures.Measures.DATASET.isSelected()) {
                measures.add(RoiMeasuresIcyModifyed.this.getDataSetName(sequence));
            }

            if(ROIMeasures.Measures.NAME.isSelected()) {
                measures.add(((ROI)roi).getName());
            }

            if(ROIMeasures.Measures.COLOR.isSelected()) {
                measures.add(((ROI)roi).getColor());
            }

            Point5D var28 = ROIMassCenterDescriptorsPlugin.computeMassCenter((ROI)roi);
            if(ROIMeasures.Measures.X.isSelected()) {
                measures.add(Double.valueOf(var28.getX()));
            }

            if(ROIMeasures.Measures.Y.isSelected()) {
                measures.add(Double.valueOf(var28.getY()));
            }

            if(ROIMeasures.Measures.Z.isSelected()) {
                measures.add(roi instanceof ROI2D && var28.getZ() == -1.0D?"ALL":Double.valueOf(var28.getZ()));
            }

            if(ROIMeasures.Measures.T.isSelected()) {
                measures.add(var28.getT() == -1.0D?"ALL":Double.valueOf(var28.getT()));
            }

            if(ROIMeasures.Measures.C.isSelected()) {
                measures.add(var28.getC() == -1.0D?"ALL":Double.valueOf(var28.getC()));
            }

            if(ROIMeasures.Measures.BOX_WIDTH.isSelected() || ROIMeasures.Measures.BOX_HEIGHT.isSelected() || ROIMeasures.Measures.BOX_DEPTH.isSelected()) {
                Rectangle5D var24 = ((ROI)roi).getBounds5D();
                if(ROIMeasures.Measures.BOX_WIDTH.isSelected()) {
                    measures.add(Double.valueOf(var24.getSizeX()));
                }

                if(ROIMeasures.Measures.BOX_HEIGHT.isSelected()) {
                    measures.add(Double.valueOf(var24.getSizeY()));
                }

                if(ROIMeasures.Measures.BOX_DEPTH.isSelected()) {
                    measures.add(roi instanceof ROI3D?Double.valueOf(var24.getSizeZ()):"N/A");
                }

                if(Thread.currentThread().isInterrupted()) {
                    return measures;
                }
            }

            if(ROIMeasures.Measures.CONTOUR.isSelected()) {
                measures.add(Double.valueOf(((ROI)roi).getNumberOfContourPoints()));
            }

            if(ROIMeasures.Measures.INTERIOR.isSelected()) {
                measures.add(Double.valueOf(((ROI)roi).getNumberOfPoints()));
            }

            if(Thread.currentThread().isInterrupted())
            {   return measures;    }

            if(ROIMeasures.Measures.SPHERICITY.isSelected()) {
                measures.add(Double.valueOf(ROISphericityDescriptor.ROISphericity.computeSphericity((ROI)roi)));
                if(Thread.currentThread().isInterrupted()) {
                    return measures;
                }
            }

            if(ROIMeasures.Measures.ROUNDNESS.isSelected()) {
                measures.add(Double.valueOf(((ROI)roi).getNumberOfPoints() == 1.0D?100.0D: ROIRoundnessDescriptor.ROIRoundness.computeRoundness((ROI)roi)));
                if(Thread.currentThread().isInterrupted()) {
                    return measures;
                }
            }


            if(ROIMeasures.Measures.CONVEXITY.isSelected()) {
                measures.add(Double.valueOf(ROIConvexHullDescriptor.ROIConvexity.computeConvexity((ROI)roi)));
                if(Thread.currentThread().isInterrupted()) {
                    return measures;
                }
            }

            if(ROIMeasures.Measures.MAX_FERET.isSelected()) {
                measures.add(Double.valueOf(ROIMeasures.computeMaxFeret((ROI)roi)));
                if(Thread.currentThread().isInterrupted()) {
                    return measures;
                }
            }

            if(ROIMeasures.Measures.ELLIPSE_A.isSelected() || ROIMeasures.Measures.ELLIPSE_B.isSelected() || ROIMeasures.Measures.ELLIPSE_C.isSelected() || ROIMeasures.Measures.YAW.isSelected() || ROIMeasures.Measures.PITCH.isSelected() || ROIMeasures.Measures.ROLL.isSelected() || ROIMeasures.Measures.ELONGATION.isSelected() || ROIMeasures.Measures.FLATNESS3D.isSelected()) {
                double[] var25 = ROIEllipsoidFittingDescriptor.computeOrientation((ROI)roi, (Sequence)null);
                if(ROIMeasures.Measures.ELLIPSE_A.isSelected()) {
                    measures.add(var25 != null?Double.valueOf(var25[0]):"N/A");
                }

                if(ROIMeasures.Measures.ELLIPSE_B.isSelected()) {
                    measures.add(var25 != null?Double.valueOf(var25[1]):"N/A");
                }

                if(ROIMeasures.Measures.ELLIPSE_C.isSelected()) {
                    measures.add(roi instanceof ROI3D && var25 != null?Double.valueOf(var25[2]):"N/A");
                }

                if(ROIMeasures.Measures.YAW.isSelected()) {
                    measures.add(Double.valueOf(Math.toDegrees(Math.acos(-var25[3]))));
                }

                if(ROIMeasures.Measures.PITCH.isSelected()) {
                    measures.add(Double.valueOf(Math.toDegrees(Math.asin(var25[5]))));
                }

                if(ROIMeasures.Measures.ROLL.isSelected()) {
                    measures.add(Double.valueOf(Math.toDegrees(Math.atan2(var25[8], var25[11]))));
                }

                if(ROIMeasures.Measures.ELONGATION.isSelected()) {
                    measures.add(var25 != null && var25[1] != 0.0D?Double.valueOf(var25[0] / var25[1]):"N/A");
                }

                if(ROIMeasures.Measures.FLATNESS3D.isSelected()) {
                    measures.add(roi instanceof ROI3D && var25 != null && var25[2] != 0.0D?Double.valueOf(var25[1] / var25[2]):"N/A");
                }
            }

            int var26 = 0;
            ROIMeasures.Measures[] var8;
            int min = (var8 = ROIMeasures.Measures.values()).length;

            for(int iterator = 0; iterator < min; ++iterator) {
                ROIMeasures.Measures c = var8[iterator];
                if(c.name().startsWith("INTENSITY") && c.isSelected()) {
                    ++var26;
                }
            }

            if(var26 > 0 && sequence != null && !(roi instanceof ROI5D)) {
                for(int var27 = 0; var27 < sequence.getSizeC(); ++var27) {
                    SequenceDataIterator var29 = new SequenceDataIterator(sequence, (ROI)roi, false, -1, -1, var27);
                    double var30 = 1.7976931348623157E308D;
                    double max = 0.0D;
                    double sum = 0.0D;
                    double cpt = 0.0D;

                    double avg;
                    while(!var29.done()) {
                        avg = var29.get();
                        if(avg > max) {
                            max = avg;
                        }

                        if(avg < var30) {
                            var30 = avg;
                        }

                        sum += avg;
                        ++cpt;
                        var29.next();
                    }

                    avg = sum / cpt;
                    double std = 0.0D;
                    if(ROIMeasures.Measures.INTENSITY_STD.isSelected()) {
                        var29.reset();

                        while(!var29.done()) {
                            double dev = var29.get() - avg;
                            std += dev * dev;
                            var29.next();
                        }

                        std = Math.sqrt(std / cpt);
                    }

                    if(ROIMeasures.Measures.INTENSITY_MIN.isSelected()) {
                        measures.add(Double.valueOf(var30));
                    }

                    if(ROIMeasures.Measures.INTENSITY_AVG.isSelected()) {
                        measures.add(Double.valueOf(avg));
                    }

                    if(ROIMeasures.Measures.INTENSITY_MAX.isSelected()) {
                        measures.add(Double.valueOf(max));
                    }

                    if(ROIMeasures.Measures.INTENSITY_SUM.isSelected()) {
                        measures.add(Double.valueOf(sum));
                    }

                    if(ROIMeasures.Measures.INTENSITY_STD.isSelected()) {
                        measures.add(Double.valueOf(std));
                    }
                }
            }
            return measures;
        }
    }



    private String getDataSetName(Sequence sequence) {
        String dataSetName = "ROI Statistics";
        if(sequence == null) {
            ArrayList sequences = ((ROI[])this.rois.getValue())[0].getSequences();
            if(sequences.size() > 0) {
                sequence = (Sequence)sequences.get(0);
            }
        }

        if(sequence == null) {
            dataSetName = "--";
        } else {
            dataSetName = FileUtil.getFileName(sequence.getFilename());
            if(dataSetName.isEmpty()) {
                dataSetName = sequence.getName();
            }
        }

        return WorkbookUtil.createSafeSheetName(dataSetName);
    }

    private void updateWorkbook(Workbook wb, IcySpreadSheet sheet, int rowID, List<Object> measures) {
        for(int colID = 0; colID < measures.size(); ++colID) {
            Object value = measures.get(colID);
            if(value instanceof Color) {
                sheet.setFillColor(rowID, colID, (Color)value);
            } else {
                sheet.setValue(rowID, colID, value);
            }
        }
    }



}
