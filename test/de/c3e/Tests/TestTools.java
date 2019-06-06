package de.c3e.Tests;

import de.c3e.ProcessManager.DataTypes.BlockGraph;
import de.c3e.ProcessManager.DataTypes.BlockLink;
import de.c3e.ProcessManager.DataTypes.GraphBlock;
import de.c3e.ProcessManager.GlobalSettings;
import de.c3e.ProcessManager.Utils.AbstractImageObject;
import icy.image.IcyBufferedImage;
import icy.imagej.ImageJUtil;
import icy.sequence.Sequence;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import ij.ImagePlus;
import ij.io.FileSaver;
import loci.plugins.in.ImagePlusReader;
import loci.plugins.in.ImportProcess;
import loci.plugins.in.ImporterOptions;
import org.slf4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Helper class for tests.
 */
public class TestTools
{
    public static boolean HasType(List<GraphBlock> input,String type)
    {
        for (GraphBlock i : input)
        {
            if (i.Type.equals(type))
            {
                return true;
            }
        }

        return false;
    }

    public static boolean HasId(List<GraphBlock> input,String type)
    {
        for (GraphBlock i : input)
        {
            if (i.Id.equals(type))
            {
                return true;
            }
        }

        return false;
    }

    public static int Count(List<GraphBlock> input,String type)
    {
        int count=0;
        for (GraphBlock i : input)
        {
            if (i.Type.equals(type))
            {
                count++;
            }
        }

        return count;
    }

    public static boolean HasLink(BlockGraph graph
                                   ,String SourceBlock, String SourcePort, String DestinationBlock, String DestinationPort)
    {
        for (BlockLink link : graph.Links)
        {
            if (    link.OriginBlock.Type.equals(SourceBlock) &&
                    link.OriginPort.Name.equals(SourcePort) &&
                    link.DestinationBlock.Type.equals(DestinationBlock) &&
                    link.DestinationPort.Name.equals(DestinationPort))
            {
                return true;
            }
        }

        return false;
    }

    public static double[] DataFromFile(File outputFile, int channel, int z, int t)
    {
        try
        {
            final ImporterOptions options = new ImporterOptions();
            options.setId(outputFile.getAbsolutePath());
            final ImportProcess process = new ImportProcess(options);
            if (!process.execute()) throw new IllegalStateException("Process failed");
            final ImagePlusReader reader = new ImagePlusReader(process);
            final ImagePlus[] imps = reader.openImagePlus();

            Sequence seq =  ImageJUtil.convertToIcySequence(imps[0],null);
            AbstractImageObject abst = AbstractImageObject.fromSequence(seq);
            return  abst.getDataXYAsDoubleArray(channel,z,t);

        }catch (Exception e)
        {
            e.printStackTrace();
            assertTrue(false);
            return null;
        }
    }

    public static short[] DataFromFileShort(File outputFile, int channel, int z, int t)
    {
        try
        {
            final ImporterOptions options = new ImporterOptions();
            options.setId(outputFile.getAbsolutePath());
            final ImportProcess process = new ImportProcess(options);
            if (!process.execute()) throw new IllegalStateException("Process failed");
            final ImagePlusReader reader = new ImagePlusReader(process);
            final ImagePlus[] imps = reader.openImagePlus();

            Sequence seq =  ImageJUtil.convertToIcySequence(imps[0],null);
            AbstractImageObject abst = AbstractImageObject.fromSequence(seq);
            return  abst.getDataXYAsShortArray(0,abst.getFullSizeX(),0,abst.getFullSizeY(),channel,z,t);

        }catch (Exception e)
        {
            e.printStackTrace();
            assertTrue(false);
            return null;
        }
    }

    public static double[] DataFromFile(File outputFile, int channel)
    {
        return DataFromFile(outputFile,channel,0,0);
    }

    public static short[] DataFromFileShort(File outputFile, int channel)
    {
        return DataFromFileShort(outputFile,channel,0,0);
    }


    public static void WriteImageToFile(File inputFile, byte[] inputData, int sizeX , int sizeY  )
    {
        WriteImageToFile(inputFile,inputData,sizeX,sizeY,1);
    }

    // this does not work for c > 1
    public static void WriteImageToFile(File inputFile, byte[] inputData, int sizeX , int sizeY , int sizeC )
    {
        Sequence image = new Sequence();

        IcyBufferedImage icy_img = new IcyBufferedImage(sizeX, sizeY, sizeC, DataType.BYTE);
        Array1DUtil.byteArrayToSafeArray(inputData, icy_img.getDataXY(0), icy_img.isSignedDataType(), icy_img.isSignedDataType());
        image.setImage(0, 0, icy_img);

        ImagePlus ijImagePlus = ImageJUtil.convertToImageJImage(image,null);
        FileSaver saver = new FileSaver(ijImagePlus);
        saver.saveAsTiff(inputFile.getAbsolutePath());
    }

    public static void WriteMultiChannelToFile(File inputFile, byte[] ch1, byte[] ch2, byte[] ch3, int sizeX , int sizeY  )
    {
        Sequence image = new Sequence();
        IcyBufferedImage icy_img = new IcyBufferedImage(sizeX, sizeY, 3, DataType.BYTE);

        Array1DUtil.byteArrayToSafeArray(ch1, icy_img.getDataXY(0), icy_img.isSignedDataType(), icy_img.isSignedDataType());
        Array1DUtil.byteArrayToSafeArray(ch2, icy_img.getDataXY(1), icy_img.isSignedDataType(), icy_img.isSignedDataType());
        Array1DUtil.byteArrayToSafeArray(ch3, icy_img.getDataXY(2), icy_img.isSignedDataType(), icy_img.isSignedDataType());
        image.setImage(0, 0, icy_img);

        ImagePlus ijImagePlus = ImageJUtil.convertToImageJImage(image,null);
        FileSaver saver = new FileSaver(ijImagePlus);
        saver.saveAsTiff(inputFile.getAbsolutePath());
    }

    public static void WriteImageToFile(File inputFile, byte[] inputData, int sizeX, int sizeY, int sizeC, int sizeZ, int sizeT)
    {
        Sequence image = new Sequence();
        int planeSize= sizeX*sizeY;
        int current = 0;
        for (int t = 0; t <sizeT ; t++)
        {
            for (int z = 0; z < sizeZ; z++)
            {
                IcyBufferedImage icy_img = new IcyBufferedImage(sizeX, sizeY, sizeC, DataType.BYTE);

                for (int c = 0; c < sizeC; c++)
                {
                    byte[] subData = Arrays.copyOfRange(inputData, current, current + planeSize);
                    Array1DUtil.byteArrayToSafeArray(subData, icy_img.getDataXY(c), icy_img.isSignedDataType(), icy_img.isSignedDataType());
                    current += planeSize;
                }
                image.setImage(t, z, icy_img);
            }
        }

        ImagePlus ijImagePlus = ImageJUtil.convertToImageJImage(image,null);
        FileSaver saver = new FileSaver(ijImagePlus);
        saver.saveAsTiff(inputFile.getAbsolutePath());
    }

    public static void WriteImageToFile(File inputFile, byte[] inputData, int sizeX, int sizeY, int sizeC, int sizeZ)
    {
        WriteImageToFile(inputFile,inputData,sizeX,sizeY,sizeC,sizeZ,1);
    }

    public static void StandardUnitTestPreperatins()
    {
        GlobalSettings.IcyFolder = "C:\\PHD\\Git\\omero-parallel-processing\\ProcessingScheduler\\";
    }
}


