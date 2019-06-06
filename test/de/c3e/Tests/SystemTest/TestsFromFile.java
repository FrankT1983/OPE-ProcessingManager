package de.c3e.Tests.SystemTest;

import de.c3e.ProcessManager.Main;
import de.c3e.Tests.MultipleInstancesTest;
import de.c3e.Tests.TestTools;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * This are tests, that are as close to the real system as possible.
 * Using strings to pass the inputs, that would otherwise be located in input files.
 */
public class TestsFromFile
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
        TestTools.StandardUnitTestPreperatins();
    }

    @Test
    public void ThresholdScriptTest()
    {
        // fileIn => Threshold => Safe
        int sizeX=5;
        int sizeY=5;
        byte[] inputData = new byte[sizeX * sizeY];
        for (byte i = 0; i < inputData.length; i++)
        {
            inputData[i] = i;
        }

        File inputFile = null;
        File outputFile= null;
        try
        {
            inputFile = File.createTempFile("UnitTest", ".tiff");
            outputFile = File.createTempFile("UnitTest", ".tiff");
            inputFile.deleteOnExit();
            outputFile.deleteOnExit();
            TestTools.WriteImageToFile(inputFile,inputData,sizeX,sizeY);
        }
        catch (Exception e)
        {   assertTrue(false);}

        String graphFile =  SimpelThreasholdWorkflow();
        String parameterFile = ParametersForSimpelThreasholdWorkflow(inputFile,outputFile);
        MultipleInstancesTest.RunFromFiles(5,graphFile,parameterFile);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        double[] resultData = TestTools.DataFromFile(outputFile,0);
        assertNotNull(resultData);
        assertEquals(inputData.length, resultData.length);
        for (int i =0;i< inputData.length; i++)
        {
            assertEquals(inputData[i]  > 11 ? 1.0 : 0.0 , resultData[i] , 0.0001);
        }
    }

    @Test
    public void ThresholdScript_usingMain_Test()
    {
        List<String> args  = new ArrayList<>();
        args.add("-Single");
        args.add("-JsonFile");
        args.add("C:\\PHD\\UnitTest\\ExampleWorkflow.txt");
        Main.main(args.toArray(new String[0]));
    }


    @Test
    public void HistogramScriptTest()
    {
        int sizeX=5;
        int sizeY=5;
        byte[] inputData = new byte[sizeX * sizeY];
        for (byte i = 0; i < inputData.length; i++)
        {
            inputData[i] = i;
        }

        File inputFile = null;
        File outputFile= null;
        try
        {
            inputFile = File.createTempFile("UnitTest", ".tiff");
            outputFile = File.createTempFile("UnitTest", ".tiff");
            inputFile.deleteOnExit();
            outputFile.deleteOnExit();
            TestTools.WriteImageToFile(inputFile,inputData,sizeX,sizeY);
        }
        catch (Exception e)
        {   assertTrue(false);}

        String graphFile =  SimpleHistrogramLiniarisiation();
        String parameterFile = ParametersSimpleHistrogramLiniarisiation(inputFile,outputFile,0);
        MultipleInstancesTest.RunFromFiles(5,graphFile,parameterFile);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);


        double[] resultData = TestTools.DataFromFile(outputFile,0);
        assertNotNull(resultData);
        assertEquals(inputData.length, resultData.length);
    }

    @Test
    public void HistogramScriptTestMultiChannel()
    {
        int sizeX=5;
        int sizeY=5;
        byte[] ch1 = new byte[sizeX * sizeY];
        byte[] ch2 = new byte[sizeX * sizeY];
        byte[] ch3 = new byte[sizeX * sizeY];
        for (byte i = 0; i < sizeX * sizeY; i++)
        {
            ch1[i] = i;
            ch2[i] = (byte)( i+2);
            ch3[i] = (byte)( i+4);
        }

        File inputFile = null;
        File outputFile= null;
        try
        {
            inputFile = File.createTempFile("UnitTest", ".tiff");
            outputFile = File.createTempFile("UnitTest", ".tiff");
            inputFile.deleteOnExit();
            outputFile.deleteOnExit();
            TestTools.WriteMultiChannelToFile(inputFile,ch1,ch2,ch3,sizeX,sizeY);
        }
        catch (Exception e)
        {   assertTrue(false);}

        String graphFile =  SimpleHistrogramLiniarisiation();
        String parameterFile = ParametersSimpleHistrogramLiniarisiation(inputFile,outputFile,1);
        MultipleInstancesTest.RunFromFiles(5,graphFile,parameterFile);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);


        double[] resultCh1 = TestTools.DataFromFile(outputFile,0);
        double[] resultCh2 = TestTools.DataFromFile(outputFile,1);
        double[] resultCh3 = TestTools.DataFromFile(outputFile,2);
        assertNotNull(resultCh1);
        assertNotNull(resultCh2);
        assertNotNull(resultCh3);

        // check that channel 0 and 2 where not changed
        for (int i =0;i< resultCh1.length; i++)
        {
            assertEquals(ch1[i]  , resultCh1[i] , 0.0001);
            assertEquals(ch3[i]  , resultCh3[i] , 0.0001);
        }
    }

    @Test
    public void HistogramScriptTestFromConvalariaFile()
    {
        File inputFile = new File("C:\\PHD\\UnitTest\\Convalaria.jpg");
        File outputFile= null;
        try
        {
            outputFile = File.createTempFile("UnitTest", ".tiff");
            outputFile.deleteOnExit();
        }
        catch (Exception e)
        {   assertTrue(false);}

        String graphFile =  SimpleHistrogramLiniarisiation();
        String parameterFile = ParametersSimpleHistrogramLiniarisiation(inputFile,outputFile,1);
        MultipleInstancesTest.RunFromFiles(5,graphFile,parameterFile);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);


        double[] resultData = TestTools.DataFromFile(outputFile,0);
        assertNotNull(resultData);

        // can't really test more, than if something went through the whole pipeline.
        // todo: make reference image => test with that
    }

    @Test
    public void ChannelToAverageTest()
    {
        int sizeX=5;
        int sizeY=5;
        byte[] ch1 = new byte[sizeX * sizeY];
        byte[] ch2 = new byte[sizeX * sizeY];
        byte[] ch3 = new byte[sizeX * sizeY];
        for (byte i = 0; i < sizeX * sizeY; i++)
        {
            ch1[i] = (i);
            ch2[i] = (byte)( i+2);
            ch3[i] = (byte)( i+4);
        }

        File inputFile = null;
        File outputFile= null;
        try
        {
            inputFile = File.createTempFile("UnitTest", ".tiff");
            outputFile = File.createTempFile("UnitTest", ".tiff");
            inputFile.deleteOnExit();
            outputFile.deleteOnExit();
            TestTools.WriteMultiChannelToFile(inputFile,ch1,ch2,ch3,sizeX,sizeY);
        }
        catch (Exception e)
        {   assertTrue(false);}

        String graphFile =  SimpleSetChannelToAverage();
        // can use these parameters since I just replace the block type
        String parameterFile = ParametersSimpleHistrogramLiniarisiation(inputFile,outputFile,1);
        MultipleInstancesTest.RunFromFiles(5,graphFile,parameterFile);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);


        double[] resultCh1 = TestTools.DataFromFile(outputFile,0);
        double[] resultCh2 = TestTools.DataFromFile(outputFile,1);
        double[] resultCh3 = TestTools.DataFromFile(outputFile,2);
        assertNotNull(resultCh1);
        assertNotNull(resultCh2);
        assertNotNull(resultCh3);

        double average0to24 = (24.0*25.0/2.0)/25.0;
        for (int i =0;i< resultCh1.length; i++)
        {
            assertEquals(average0to24, resultCh1[i], 0.0001);
            assertEquals(average0to24 + 2  , resultCh2[i], 0.0001);
            assertEquals(average0to24 + 4 , resultCh3[i], 0.0001);
        }
    }


    /**
     * Test for checking mixed splitting types
     */
    @Test
    public void Add_Average_Add_Test()
    {
        int sizeX=5;
        int sizeY=5;
        byte[] ch1 = new byte[sizeX * sizeY];
        byte[] ch2 = new byte[sizeX * sizeY];
        byte[] ch3 = new byte[sizeX * sizeY];
        for (byte i = 0; i < sizeX * sizeY; i++)
        {
            ch1[i] = (i);
            ch2[i] = (byte)( i+2);
            ch3[i] = (byte)( i+4);
        }

        File inputFile = null;
        File outputFile= null;
        try
        {
            inputFile = File.createTempFile("UnitTest", ".tiff");
            outputFile = File.createTempFile("UnitTest", ".tiff");
            inputFile.deleteOnExit();
            outputFile.deleteOnExit();
            TestTools.WriteMultiChannelToFile(inputFile,ch1,ch2,ch3,sizeX,sizeY);
        }
        catch (Exception e)
        {   assertTrue(false);}


        int x1 = 1;
        int x2= 3;
        String graphFile =  Add_Average_Add();
        // can use these parameters since I just replace the block type
        String parameterFile = "";
        parameterFile += Parameter("1409786826", "out" , "Value" , "0"); // DataSet
        parameterFile += Parameter("1339674000", "out" , "Value" , inputFile.getAbsolutePath()); // input file
        parameterFile += Parameter("740544130", "out" , "Value" , outputFile.getAbsolutePath()); // output file
        parameterFile += Parameter("1", "in" , "Value" , String.valueOf(x1));
        parameterFile += Parameter("3", "in" , "Value" , String.valueOf(x2));

        MultipleInstancesTest.RunFromFiles(5, graphFile, parameterFile);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        double[] resultCh1 = TestTools.DataFromFile(outputFile,0);
        double[] resultCh2 = TestTools.DataFromFile(outputFile,1);
        double[] resultCh3 = TestTools.DataFromFile(outputFile,2);
        assertNotNull(resultCh1);
        assertNotNull(resultCh2);
        assertNotNull(resultCh3);

        double average0to24 = (24.0*25.0/2.0)/25.0;
        for (int i =0;i< resultCh1.length; i++)
        {
            assertEquals(average0to24 + x1 + x2, resultCh1[i], 0.0001);
            assertEquals(average0to24 + x1 + x2 + 2  , resultCh2[i], 0.0001);
            assertEquals(average0to24 + x1 + x2 + 4 , resultCh3[i], 0.0001);
        }
    }

    private String Parameter(String s, String out, String value, String i)
    {
        return s +"\t"+ out + "\t" + value +"\t" + i + "\n";
    }

    private String ParametersForSimpelThreasholdWorkflow(File inputFile, File outputFile)
    {
        return "1647119090\tout\tValue\t0\n" +          // channel input
                "1409786826\tout\tValue\t601\n" +       // datasetId
                "1339674000\tout\tValue\t"+ inputFile.getAbsolutePath()+"\n" +   // input file
                "740544130\tout\tValue\t"+  outputFile.getAbsolutePath()+"\n";     // outputfile
    }

    private String SimpelThreasholdWorkflow()
    {
        return "<protocol VERSION=\"4\">\n" +
                "<blocks>\n" +
                "<block ID=\"1339674000\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\" collapsed=\"false\" definedName=\"Omero image input\" height=\"90\" keepsResults=\"true\" width=\"234\" xLocation=\"52\" yLocation=\"22\">\n" +
                "<variables>\n" +
                "<input>\n" +
                "<variable ID=\"ParameterName\" name=\"ParameterName\" runtime=\"false\" value=\"InputImage\" visible=\"true\"/>\n" +
                "</input>\n" +
                "<output>\n" +
                "<variable ID=\"LoadedImage\" name=\"Loaded image\" runtime=\"false\" visible=\"true\"/>\n" +
                "</output>\n" +
                "</variables>\n" +
                "</block>\n" +
                "<block ID=\"807507987\" blockType=\"plugins.adufour.thresholder.KMeansThresholdBlock\" className=\"plugins.adufour.thresholder.Thresholder\" collapsed=\"false\" definedName=\"K means threshold\" height=\"133\" keepsResults=\"true\" width=\"205\" xLocation=\"295\" yLocation=\"438\">\n" +
                "<variables>\n" +
                "<input>\n" +
                "<variable ID=\"Input\" name=\"Input\" runtime=\"false\" visible=\"true\"/>\n" +
                "<variable ID=\"Channel\" name=\"Channel\" runtime=\"false\" value=\"0\" visible=\"true\"/>\n" +
                "<variable ID=\"Classes\" name=\"Classes\" runtime=\"false\" value=\"2\" visible=\"true\"/>\n" +
                "</input>\n" +
                "<output>\n" +
                "<variable ID=\"thresholds\" name=\"thresholds\" runtime=\"false\" visible=\"true\"/>\n" +
                "</output>\n" +
                "</variables>\n" +
                "</block>\n" +
                "<block ID=\"1409786826\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong\" collapsed=\"false\" definedName=\"Omero script parameter long\" height=\"137\" keepsResults=\"true\" width=\"318\" xLocation=\"5\" yLocation=\"151\">\n" +
                "<variables>\n" +
                "<input>\n" +
                "<variable ID=\"Value\" name=\"TestValue(ignored in Omero)\" runtime=\"false\" value=\"601\" visible=\"true\"/>\n" +
                "<variable ID=\"ParameterName\" name=\"ParameterName\" runtime=\"false\" value=\"DataSetId\" visible=\"true\"/>\n" +
                "</input>\n" +
                "<output>\n" +
                "<variable ID=\"Value\" name=\"Value\" runtime=\"false\" visible=\"true\"/>\n" +
                "</output>\n" +
                "</variables>\n" +
                "</block>\n" +
                "<block ID=\"1760892854\" blockType=\"plugins.adufour.thresholder.Thresholder\" className=\"plugins.adufour.thresholder.Thresholder\" collapsed=\"false\" definedName=\"Thresholder\" height=\"179\" keepsResults=\"true\" width=\"255\" xLocation=\"510\" yLocation=\"24\">\n" +
                "<variables>\n" +
                "<input>\n" +
                "<variable ID=\"Input\" name=\"Input\" runtime=\"false\" visible=\"true\"/>\n" +
                "<variable ID=\"channel\" name=\"channel\" runtime=\"false\" value=\"0\" visible=\"true\"/>\n" +
                "<variable ID=\"Manual thresholds\" name=\"Manual thresholds\" runtime=\"false\" visible=\"true\"/>\n" +
                "<variable ID=\"Treat as percentiles\" name=\"Treat as percentiles\" runtime=\"false\" value=\"false\" visible=\"true\"/>\n" +
                "</input>\n" +
                "<output>\n" +
                "<variable ID=\"output\" name=\"Binary output\" runtime=\"false\" visible=\"true\"/>\n" +
                "<variable ID=\"ROI\" name=\"ROI\" runtime=\"false\" visible=\"true\"/>\n" +
                "</output>\n" +
                "</variables>\n" +
                "</block>\n" +
                "<block ID=\"740544130\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\" collapsed=\"false\" definedName=\"Omero image save to data set\" height=\"230\" keepsResults=\"true\" width=\"268\" xLocation=\"781\" yLocation=\"220\">\n" +
                "<variables>\n" +
                "<input>\n" +
                "<variable ID=\"Image to save\" name=\"Image to save\" runtime=\"false\" type=\"icy.sequence.Sequence\" visible=\"true\"/>\n" +
                "<variable ID=\"DataSet Id\" name=\"DataSet Id\" runtime=\"false\" visible=\"true\"/>\n" +
                "</input>\n" +
                "<output/>\n" +
                "</variables>\n" +
                "</block>\n" +
                "</blocks>\n" +
                "<links>\n" +
                "<link dstBlockID=\"740544130\" dstVarID=\"DataSet Id\" srcBlockID=\"1409786826\" srcVarID=\"Value\"/>\n" +
                "<link dstBlockID=\"807507987\" dstVarID=\"Input\" srcBlockID=\"1339674000\" srcVarID=\"LoadedImage\"/>\n" +
                "<link dstBlockID=\"1760892854\" dstVarID=\"Manual thresholds\" srcBlockID=\"807507987\" srcVarID=\"thresholds\"/>\n" +
                "<link dstBlockID=\"1760892854\" dstVarID=\"Input\" srcBlockID=\"1339674000\" srcVarID=\"LoadedImage\"/>\n" +
                "<link dstBlockID=\"740544130\" dstVarID=\"Image to save\" srcBlockID=\"1760892854\" srcVarID=\"output\"/>\n" +
                "</links>\n" +
                "</protocol>";
    }

    private String ParametersSimpleHistrogramLiniarisiation(File inputFile, File outputFile,int channel)
    {
        return "1647119090\tout\tValue\t"+channel+"\n" +  // channel input
                "1409786826\tout\tValue\t601\n" +  // datasetId
                "1339674000\tout\tValue\t"+ inputFile.getAbsolutePath()+"\n" +   // input file
                "740544130\tout\tValue\t"+  outputFile.getAbsolutePath()+"\n";     // outputfile
    }

    private String SimpleHistrogramLiniarisiation()
    {
        return "<protocol VERSION=\"4\">\n" +
                "<blocks>\n" +
                "<block ID=\"1647119090\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong\" collapsed=\"false\" definedName=\"Omero script parameter long\" height=\"114\" keepsResults=\"true\" width=\"282\" xLocation=\"6\" yLocation=\"117\">\n" +
                "<variables>\n" +
                "<input>\n" +
                "<variable ID=\"Value\" name=\"TestValue(ignored in Omero)\" runtime=\"false\" value=\"0\" visible=\"true\"/>\n" +
                "<variable ID=\"ParameterName\" name=\"ParameterName\" runtime=\"false\" value=\"Channel Input\" visible=\"true\"/>\n" +
                "</input>\n" +
                "<output>\n" +
                "<variable ID=\"Value\" name=\"Value\" runtime=\"false\" visible=\"true\"/>\n" +
                "</output>\n" +
                "</variables>\n" +
                "</block>\n" +
                "<block ID=\"1409786826\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong\" collapsed=\"false\" definedName=\"Omero script parameter long\" height=\"137\" keepsResults=\"true\" width=\"318\" xLocation=\"14\" yLocation=\"254\">\n" +
                "<variables>\n" +
                "<input>\n" +
                "<variable ID=\"Value\" name=\"TestValue(ignored in Omero)\" runtime=\"false\" value=\"0\" visible=\"true\"/>\n" +
                "<variable ID=\"ParameterName\" name=\"ParameterName\" runtime=\"false\" value=\"DataSetId\" visible=\"true\"/>\n" +
                "</input>\n" +
                "<output>\n" +
                "<variable ID=\"Value\" name=\"Value\" runtime=\"false\" visible=\"true\"/>\n" +
                "</output>\n" +
                "</variables>\n" +
                "</block>\n" +
                "<block ID=\"1339674000\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\" collapsed=\"false\" definedName=\"Omero image input\" height=\"90\" keepsResults=\"true\" width=\"234\" xLocation=\"52\" yLocation=\"22\">\n" +
                "<variables>\n" +
                "<input>\n" +
                "<variable ID=\"ParameterName\" name=\"ParameterName\" runtime=\"false\" value=\"InputImage\" visible=\"true\"/>\n" +
                "</input>\n" +
                "<output>\n" +
                "<variable ID=\"LoadedImage\" name=\"Loaded image\" runtime=\"false\" visible=\"true\"/>\n" +
                "</output>\n" +
                "</variables>\n" +
                "</block>\n" +
                "<block ID=\"2087420046\" blockType=\"plugins.tlecomte.histogram.HistogramEqualization\" className=\"plugins.tlecomte.histogram.HistogramEqualization\" collapsed=\"false\" definedName=\"Histogram Equalization\" height=\"132\" keepsResults=\"true\" width=\"236\" xLocation=\"432\" yLocation=\"89\">\n" +
                "<variables>\n" +
                "<input>\n" +
                "<variable ID=\"Input\" name=\"Input\" runtime=\"false\" visible=\"true\"/>\n" +
                "<variable ID=\"Channel\" name=\"Channel\" runtime=\"false\" visible=\"true\"/>\n" +
                "<variable ID=\"In-place\" name=\"In-place\" runtime=\"false\" value=\"false\" visible=\"true\"/>\n" +
                "</input>\n" +
                "<output>\n" +
                "<variable ID=\"Equalized sequence\" name=\"Equalized sequence\" runtime=\"false\" visible=\"true\"/>\n" +
                "</output>\n" +
                "</variables>\n" +
                "</block>\n" +
                "<block ID=\"740544130\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\" collapsed=\"false\" definedName=\"Omero image save to data set\" height=\"230\" keepsResults=\"true\" width=\"268\" xLocation=\"756\" yLocation=\"88\">\n" +
                "<variables>\n" +
                "<input>\n" +
                "<variable ID=\"Image to save\" name=\"Image to save\" runtime=\"false\" type=\"icy.sequence.Sequence\" visible=\"true\"/>\n" +
                "<variable ID=\"DataSet Id\" name=\"DataSet Id\" runtime=\"false\" visible=\"true\"/>\n" +
                "</input>\n" +
                "<output/>\n" +
                "</variables>\n" +
                "</block>\n" +
                "</blocks>\n" +
                "<links>\n" +
                "<link dstBlockID=\"2087420046\" dstVarID=\"Input\" srcBlockID=\"1339674000\" srcVarID=\"LoadedImage\"/>\n" +
                "<link dstBlockID=\"740544130\" dstVarID=\"Image to save\" srcBlockID=\"2087420046\" srcVarID=\"Equalized sequence\"/>\n" +
                "<link dstBlockID=\"2087420046\" dstVarID=\"Channel\" srcBlockID=\"1647119090\" srcVarID=\"Value\"/>\n" +
                "<link dstBlockID=\"740544130\" dstVarID=\"DataSet Id\" srcBlockID=\"1409786826\" srcVarID=\"Value\"/>\n" +
                "</links>\n" +
                "</protocol>";
    }

    private String SimpleSetChannelToAverage()
    {
        return "<protocol VERSION=\"4\">\n" +
                "    <blocks>        \n" +
                "        <block ID=\"1409786826\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong\" collapsed=\"false\" definedName=\"Omero script parameter long\" height=\"137\" keepsResults=\"true\" width=\"318\" xLocation=\"14\" yLocation=\"254\">\n" +
                "            <variables>\n" +
                "                <input>\n" +
                "                    <variable ID=\"Value\" name=\"TestValue(ignored in Omero)\" runtime=\"false\" value=\"0\" visible=\"true\" />\n" +
                "                    <variable ID=\"ParameterName\" name=\"ParameterName\" runtime=\"false\" value=\"DataSetId\" visible=\"true\" />\n" +
                "                </input>\n" +
                "                <output>\n" +
                "                    <variable ID=\"Value\" name=\"Value\" runtime=\"false\" visible=\"true\" />\n" +
                "                </output>\n" +
                "            </variables>\n" +
                "        </block>\n" +
                "        <block ID=\"1339674000\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\" collapsed=\"false\" definedName=\"Omero image input\" height=\"90\" keepsResults=\"true\" width=\"234\" xLocation=\"52\" yLocation=\"22\">\n" +
                "            <variables>\n" +
                "                <input>\n" +
                "                    <variable ID=\"ParameterName\" name=\"ParameterName\" runtime=\"false\" value=\"InputImage\" visible=\"true\" />\n" +
                "                </input>\n" +
                "                <output>\n" +
                "                    <variable ID=\"LoadedImage\" name=\"Loaded image\" runtime=\"false\" visible=\"true\" />\n" +
                "                </output>\n" +
                "            </variables>\n" +
                "        </block>\n" +
                "        <block ID=\"2087420046\" blockType=\"de.c3e.BlockTemplates.Examples.SetChannelToAverage\" className=\"de.c3e.BlockTemplates.Examples.SetChannelToAverage\" collapsed=\"false\" definedName=\"SetChannelToAverage\" height=\"132\" keepsResults=\"true\" width=\"236\" xLocation=\"432\" yLocation=\"89\">\n" +
                "            <variables>\n" +
                "                <input>\n" +
                "                    <variable ID=\"Input\" name=\"Input\" runtime=\"false\" visible=\"true\" />                    \n" +
                "                </input>\n" +
                "                <output>\n" +
                "                    <variable ID=\"Output\" name=\"Output\" runtime=\"false\" visible=\"true\" />\n" +
                "                </output>\n" +
                "            </variables>\n" +
                "        </block>\n" +
                "        <block ID=\"740544130\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\" collapsed=\"false\" definedName=\"Omero image save to data set\" height=\"230\" keepsResults=\"true\" width=\"268\" xLocation=\"756\" yLocation=\"88\">\n" +
                "            <variables>\n" +
                "                <input>\n" +
                "                    <variable ID=\"Image to save\" name=\"Image to save\" runtime=\"false\" type=\"icy.sequence.Sequence\" visible=\"true\" />\n" +
                "                    <variable ID=\"DataSet Id\" name=\"DataSet Id\" runtime=\"false\" visible=\"true\" />\n" +
                "                </input>\n" +
                "                <output />\n" +
                "            </variables>\n" +
                "        </block>\n" +
                "    </blocks>\n" +
                "    <links>\n" +
                "        <link dstBlockID=\"2087420046\" dstVarID=\"Input\" srcBlockID=\"1339674000\" srcVarID=\"LoadedImage\" />\n" +
                "        <link dstBlockID=\"740544130\" dstVarID=\"Image to save\" srcBlockID=\"2087420046\" srcVarID=\"Output\" />        \n" +
                "        <link dstBlockID=\"740544130\" dstVarID=\"DataSet Id\" srcBlockID=\"1409786826\" srcVarID=\"Value\" />\n" +
                "    </links>\n" +
                "</protocol>";
    }

    private String Add_Average_Add()
    {
        return "<protocol VERSION=\"4\">\n" +
                "    <blocks>        \n" +
                "        <block ID=\"1409786826\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong\" collapsed=\"false\" definedName=\"Omero script parameter long\" height=\"137\" keepsResults=\"true\" width=\"318\" xLocation=\"14\" yLocation=\"254\">\n" +
                "            <variables>\n" +
                "                <input>\n" +
                "                    <variable ID=\"Value\" name=\"TestValue(ignored in Omero)\" runtime=\"false\" value=\"0\" visible=\"true\" />\n" +
                "                    <variable ID=\"ParameterName\" name=\"ParameterName\" runtime=\"false\" value=\"DataSetId\" visible=\"true\" />\n" +
                "                </input>\n" +
                "                <output>\n" +
                "                    <variable ID=\"Value\" name=\"Value\" runtime=\"false\" visible=\"true\" />\n" +
                "                </output>\n" +
                "            </variables>\n" +
                "        </block>\n" +
                "        <block ID=\"1339674000\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\" collapsed=\"false\" definedName=\"Omero image input\" height=\"90\" keepsResults=\"true\" width=\"234\" xLocation=\"52\" yLocation=\"22\">\n" +
                "            <variables>\n" +
                "                <input>\n" +
                "                    <variable ID=\"ParameterName\" name=\"ParameterName\" runtime=\"false\" value=\"InputImage\" visible=\"true\" />\n" +
                "                </input>\n" +
                "                <output>\n" +
                "                    <variable ID=\"LoadedImage\" name=\"Loaded image\" runtime=\"false\" visible=\"true\" />\n" +
                "                </output>\n" +
                "            </variables>\n" +
                "        </block>\n" +
                "\t\t\n" +
                "\t\t <block ID=\"1\" blockType=\"de.c3e.BlockTemplates.Examples.AddXBlock\" className=\"de.c3e.BlockTemplates.Examples.AddXBlock\" collapsed=\"false\" definedName=\"AddXBlock\" height=\"132\" keepsResults=\"true\" width=\"236\" xLocation=\"432\" yLocation=\"89\">\n" +
                "            <variables>\n" +
                "                <input>\n" +
                "                    <variable ID=\"Input\" name=\"Input\" runtime=\"false\" visible=\"true\" />                    \n" +
                "<variable ID=\"Value\" name=\"Value\" runtime=\"false\" value=\"0\" visible=\"true\"/>"+
                "                </input>\n" +
                "                <output>\n" +
                "                    <variable ID=\"Output\" name=\"Output\" runtime=\"false\" visible=\"true\" />\n" +
                "                </output>\n" +
                "            </variables>\n" +
                "        </block>\n" +
                "\t\t\n" +
                "        <block ID=\"2\" blockType=\"de.c3e.BlockTemplates.Examples.SetChannelToAverage\" className=\"de.c3e.BlockTemplates.Examples.SetChannelToAverage\" collapsed=\"false\" definedName=\"SetChannelToAverage\" height=\"132\" keepsResults=\"true\" width=\"236\" xLocation=\"432\" yLocation=\"89\">\n" +
                "            <variables>\n" +
                "                <input>\n" +
                "                    <variable ID=\"Input\" name=\"Input\" runtime=\"false\" visible=\"true\" />                    \n" +
                "                </input>\n" +
                "                <output>\n" +
                "                    <variable ID=\"Output\" name=\"Output\" runtime=\"false\" visible=\"true\" />\n" +
                "                </output>\n" +
                "            </variables>\n" +
                "        </block>\n" +
                "\t\t\n" +
                "\t\t <block ID=\"3\" blockType=\"de.c3e.BlockTemplates.Examples.AddXBlock\" className=\"de.c3e.BlockTemplates.Examples.AddXBlock\" collapsed=\"false\" definedName=\"AddXBlock\" height=\"132\" keepsResults=\"true\" width=\"236\" xLocation=\"432\" yLocation=\"89\">\n" +
                "            <variables>\n" +
                "                <input>\n" +
                "                    <variable ID=\"Input\" name=\"Input\" runtime=\"false\" visible=\"true\" />                    \n" +
                "<variable ID=\"Value\" name=\"Value\" runtime=\"false\" value=\"0\" visible=\"true\"/>"+
                "                </input>\n" +
                "                <output>\n" +
                "                    <variable ID=\"Output\" name=\"Output\" runtime=\"false\" visible=\"true\" />\n" +
                "                </output>\n" +
                "            </variables>\n" +
                "        </block>\n" +
                "        <block ID=\"740544130\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\" collapsed=\"false\" definedName=\"Omero image save to data set\" height=\"230\" keepsResults=\"true\" width=\"268\" xLocation=\"756\" yLocation=\"88\">\n" +
                "            <variables>\n" +
                "                <input>\n" +
                "                    <variable ID=\"Image to save\" name=\"Image to save\" runtime=\"false\" type=\"icy.sequence.Sequence\" visible=\"true\" />\n" +
                "                    <variable ID=\"DataSet Id\" name=\"DataSet Id\" runtime=\"false\" visible=\"true\" />\n" +
                "                </input>\n" +
                "                <output />\n" +
                "            </variables>\n" +
                "        </block>\n" +
                "    </blocks>\n" +
                "    <links>\n" +
                "        <link dstBlockID=\"1\" dstVarID=\"Input\" srcBlockID=\"1339674000\" srcVarID=\"LoadedImage\" />\t\t\n" +
                "\t\t<link dstBlockID=\"2\" dstVarID=\"Input\" srcBlockID=\"1\" srcVarID=\"Output\" />\t\t\n" +
                "\t\t<link dstBlockID=\"3\" dstVarID=\"Input\" srcBlockID=\"2\" srcVarID=\"Output\" />       \t\t\n" +
                "\t\t<link dstBlockID=\"740544130\" dstVarID=\"Image to save\" srcBlockID=\"3\" srcVarID=\"Output\" />        \n" +
                "        <link dstBlockID=\"740544130\" dstVarID=\"DataSet Id\" srcBlockID=\"1409786826\" srcVarID=\"Value\" />\n" +
                "    </links>\n" +
                "</protocol>";
    }
}
