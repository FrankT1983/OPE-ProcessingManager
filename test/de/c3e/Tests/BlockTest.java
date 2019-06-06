package de.c3e.Tests;

import de.c3e.BlockTemplates.Examples.AddXYBlock;
import de.c3e.BlockTemplates.Templates.PointCalculatorImpl;
import de.c3e.ProcessManager.BlockRepository.CollectResultsWorkBlock;
import de.c3e.ProcessManager.BlockRepository.IcyBlockRepository;
import de.c3e.ProcessManager.BlockRepository.InvertWorkBlock;
import de.c3e.ProcessManager.BlockRepository.LoadImageWorkBlock;
import de.c3e.ProcessManager.DataTypes.*;
import de.c3e.ProcessManager.Main;
import de.c3e.ProcessManager.ProcessingManager;
import de.c3e.ProcessManager.Utils.AbstractImageObject;
import de.c3e.ProcessManager.WorkerManager.MainThreadWorkManager;
import de.c3e.ProcessManager.WorkerManager.ThreadsWorkerManager;
import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * First uni test class to writen blocks and in combination with the processing manager
 */
public class BlockTest
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
    }

    static BlockGraph ConstructAddXGraph(int X, int Y, Sequence image, String blockType)
    {
        // load => addBlock1
        GraphBlock addBlock = new GraphBlock();
        addBlock.Type = blockType;
        PointCalculatorImpl.InitGraphBlock(addBlock,AddXYBlock.class);

        assertEquals("Should have only the image output", 1 ,addBlock.Outputs.size() );
        assertEquals("Should have image, x and y input", 3 ,addBlock.Inputs.size() );

        GraphBlock imageBlock = GraphRepository.CreateStandardImageLoadBlock();
        {
            imageBlock.InputByName(LoadImageWorkBlock.inputImage).SetValue(image);
            imageBlock.InputByName(LoadImageWorkBlock.inputName).SetValue(null);
        }

        addBlock.InputByName("X").SetValue(X);
        addBlock.InputByName("Y").SetValue(Y);

        BlockGraph graph = new BlockGraph();
        graph.AllBlocks.add(addBlock);
        graph.AllBlocks.add(imageBlock);

        BlockLink link = new BlockLink(imageBlock.OutputByName(LoadImageWorkBlock.outputName),imageBlock, addBlock.InputByName(PointCalculatorImpl.ImageInput), addBlock);
        graph.Links.add(link);

        return graph;
    }

    static BlockGraph ConstructDualAddXGraph(int X, int Y, Sequence image, String blockType)
    {
        // load => addBlock1 = > addBlock2
        GraphBlock imageBlock = GraphRepository.CreateStandardImageLoadBlock();
        {
            imageBlock.InputByName(LoadImageWorkBlock.inputImage).SetValue(image);
            imageBlock.InputByName(LoadImageWorkBlock.inputName).SetValue(null);
        }

        GraphBlock addBlock1 = new GraphBlock();
        {
            addBlock1.Type = blockType;
            addBlock1.Id = "Add1";
            PointCalculatorImpl.InitGraphBlock(addBlock1, AddXYBlock.class);

            assertEquals("Should have only the image output", 1, addBlock1.Outputs.size());
            assertEquals("Should have image, x and y input", 3, addBlock1.Inputs.size());

            addBlock1.InputByName("X").SetValue(X);
            addBlock1.InputByName("Y").SetValue(Y);
        }

        GraphBlock addBlock2 = new GraphBlock();
        {
            addBlock2.Type = blockType;
            addBlock2.Id = "Add2";
            PointCalculatorImpl.InitGraphBlock(addBlock2, AddXYBlock.class);

            assertEquals("Should have only the image output", 1, addBlock2.Outputs.size());
            assertEquals("Should have image, x and y input", 3, addBlock2.Inputs.size());

            addBlock2.InputByName("X").SetValue(X);
            addBlock2.InputByName("Y").SetValue(Y);
        }

        BlockGraph graph = new BlockGraph();
        graph.AllBlocks.add(addBlock1);
        graph.AllBlocks.add(addBlock2);
        graph.AllBlocks.add(imageBlock);

        BlockLink link1 = new BlockLink(imageBlock.OutputByName(LoadImageWorkBlock.outputName),imageBlock, addBlock1.InputByName(PointCalculatorImpl.ImageInput), addBlock1);
        BlockLink link2 = new BlockLink(addBlock1.OutputByName(PointCalculatorImpl.ImageOutput),addBlock1, addBlock2.InputByName(PointCalculatorImpl.ImageInput), addBlock2);
        graph.Links.add(link1);
        graph.Links.add(link2);

        return graph;
    }

    static BlockGraph ConstructDoualAddXWithResultsListGraph(int X, int Y, Object image, String blockType, String resultFilePath)
    {
        // load => addBlock1 = > addBlock2
        GraphBlock imageBlock = GraphRepository.CreateStandardImageLoadBlock();
        {
            imageBlock.InputByName(LoadImageWorkBlock.inputImage).SetValue(image);
            imageBlock.InputByName(LoadImageWorkBlock.inputName).SetValue(null);
        }

        GraphBlock addBlock1 = new GraphBlock();
        {
            addBlock1.Type = blockType;
            addBlock1.Id = "Add1";
            PointCalculatorImpl.InitGraphBlock(addBlock1, AddXYBlock.class);

            addBlock1.InputByName("X").SetValue(X);
            addBlock1.InputByName("Y").SetValue(Y);
        }

        GraphBlock addBlock2 = new GraphBlock();
        {
            addBlock2.Type = blockType;
            addBlock2.Id = "Add2";
            PointCalculatorImpl.InitGraphBlock(addBlock2, AddXYBlock.class);

            addBlock2.InputByName("X").SetValue(X);
            addBlock2.InputByName("Y").SetValue(Y);
        }

        GraphBlock resultSaveBlock = GraphRepository.CreateStandardResultSinkBlock();
        {
            resultSaveBlock.Id = "ResultSink";
            resultSaveBlock.InputByName(CollectResultsWorkBlock.ResultFilePath).SetValue(resultFilePath);
        }

        assertEquals("Should have only the image output", 1, addBlock1.Outputs.size());
        assertEquals("Should have image, x and y input", 3, addBlock1.Inputs.size());

        assertEquals("Should have only the image output", 1, addBlock2.Outputs.size());
        assertEquals("Should have image, x and y input", 3, addBlock2.Inputs.size());

        assertEquals( 0, resultSaveBlock.Outputs.size());
        assertEquals( 2, resultSaveBlock.Inputs.size());


        BlockGraph graph = new BlockGraph();
        graph.AllBlocks.add(addBlock1);
        graph.AllBlocks.add(addBlock2);
        graph.AllBlocks.add(imageBlock);
        graph.AllBlocks.add(resultSaveBlock);

        assertEquals( 4, graph.AllBlocks.size());

        BlockLink link1 = new BlockLink(imageBlock.OutputByName(LoadImageWorkBlock.outputName),imageBlock, addBlock1.InputByName(PointCalculatorImpl.ImageInput), addBlock1);
        BlockLink link2 = new BlockLink(addBlock1.OutputByName(PointCalculatorImpl.ImageOutput),addBlock1, addBlock2.InputByName(PointCalculatorImpl.ImageInput), addBlock2);
        BlockLink link3 = new BlockLink(addBlock2.OutputByName(PointCalculatorImpl.ImageOutput),addBlock2, resultSaveBlock.InputByName(CollectResultsWorkBlock.ResultInput), resultSaveBlock);
        graph.Links.add(link1);
        graph.Links.add(link2);
        graph.Links.add(link3);

        return graph;
    }


    static BlockGraph ConstructAddXWithThresholdResultsListGraph(int X, int Y, Object image, String blockType, String resultFilePath, int threashhold)
    {
        // load => addBlock1 = > addBlock2
        GraphBlock imageBlock = GraphRepository.CreateStandardImageLoadBlock();
        {
            imageBlock.InputByName(LoadImageWorkBlock.inputImage).SetValue(image);
            imageBlock.InputByName(LoadImageWorkBlock.inputName).SetValue(null);
        }

        GraphBlock addBlock1 = new GraphBlock();
        {
            addBlock1.Type = blockType;
            addBlock1.Id = "Add1";
            PointCalculatorImpl.InitGraphBlock(addBlock1, AddXYBlock.class);

            addBlock1.InputByName("X").SetValue(X);
            addBlock1.InputByName("Y").SetValue(Y);
        }

        GraphBlock thresholdBlock = IcyBlockRepository.ConstructThreasholdBlock();
        {
            thresholdBlock.InputByName("Manual thresholds").SetValue(threashhold);
        }

        GraphBlock resultSaveBlock = GraphRepository.CreateStandardResultSinkBlock();
        {
            resultSaveBlock.Id = "ResultSink";
            resultSaveBlock.InputByName(CollectResultsWorkBlock.ResultFilePath).SetValue(resultFilePath);
        }

        assertEquals("Should have only the image output", 1, addBlock1.Outputs.size());
        assertEquals("Should have image, x and y input", 3, addBlock1.Inputs.size());



        assertEquals( 0, resultSaveBlock.Outputs.size());
        assertEquals( 2, resultSaveBlock.Inputs.size());


        BlockGraph graph = new BlockGraph();
        graph.AllBlocks.add(addBlock1);
        graph.AllBlocks.add(thresholdBlock);
        graph.AllBlocks.add(imageBlock);
        graph.AllBlocks.add(resultSaveBlock);

        assertEquals( 4, graph.AllBlocks.size());

        BlockLink link1 = new BlockLink(imageBlock.OutputByName(LoadImageWorkBlock.outputName),imageBlock, addBlock1.InputByName(PointCalculatorImpl.ImageInput), addBlock1);
        BlockLink link2 = new BlockLink(addBlock1.OutputByName(PointCalculatorImpl.ImageOutput),addBlock1, thresholdBlock.InputByName("Input"), thresholdBlock);
        BlockLink link3 = new BlockLink(thresholdBlock.OutputByName("output"),thresholdBlock, resultSaveBlock.InputByName(CollectResultsWorkBlock.ResultInput), resultSaveBlock);
        graph.Links.add(link1);
        graph.Links.add(link2);
        graph.Links.add(link3);

        return graph;
    }


    static GraphBlock GetBlockOfType(BlockGraph graph, String type)
    {
        for (GraphBlock b :graph.AllBlocks)
        {
            if (b.Type.equals(type))
            {
                return b;
            }
        }
        return null;
    }

    static GraphBlock GetBlockWithId(BlockGraph graph, String id)
    {
        for (GraphBlock b :graph.AllBlocks)
        {
            if (b.Id.equals(id))
            {
                return b;
            }
        }
        return null;
    }

    @Test
    public void TestAddXBlock()
    {
        int sizeX = 5;
        int sizeY = 5;
        int X = 1;
        int Y = 2;

        Sequence image = new Sequence();
        IcyBufferedImage icy_img = new IcyBufferedImage(sizeX, sizeY, 1, DataType.BYTE);
        byte[] inputData  = new byte[sizeX * sizeY];
        for (byte i= 0;i < inputData.length ; i++)
        {
            inputData[i] = i;
        }
        Array1DUtil.byteArrayToSafeArray(inputData, icy_img.getDataXY(0),icy_img.isSignedDataType() , icy_img.isSignedDataType());
        image.setImage(0,0,icy_img);

        BlockGraph graph = ConstructAddXGraph(X,Y,image,"AddXYBlock");

        ProcessingManager manager = new ProcessingManager(new MainThreadWorkManager());
        manager.InitManager(graph);
        assertTrue("Should run successful" , manager.StartProcessingLoop());

        GraphBlock addBlock = GetBlockOfType(graph,"AddXYBlock");
        assertEquals("Should have only the image output", 1 ,addBlock.Outputs.size() );
        assertTrue(addBlock.Outputs.get(0).isValid());
        Object result = addBlock.Outputs.get(0).getValue();
        assertNotNull(result);
        assertTrue(result instanceof AbstractImageObject);

        double[] resultData = ((AbstractImageObject)result).getDataXYAsDoubleArray(0,0,0);
        assertEquals(inputData.length, resultData.length);
        for (int i =0;i< inputData.length; i++)
        {
            assertEquals(inputData[i] + X +Y, resultData[i] , 0.0001);
        }
    }

    @Test
    // this will test the loading from a directory
    public void TestAddXYBlock()
    {
        int sizeX = 5;
        int sizeY = 5;
        int X = 1;
        int Y = 2;

        Sequence image = new Sequence();
        IcyBufferedImage icy_img = new IcyBufferedImage(sizeX, sizeY, 1, DataType.BYTE);
        byte[] inputData  = new byte[sizeX * sizeY];
        for (byte i= 0;i < inputData.length ; i++)
        {
            inputData[i] = i;
        }
        Array1DUtil.byteArrayToSafeArray(inputData, icy_img.getDataXY(0),icy_img.isSignedDataType() , icy_img.isSignedDataType());
        image.setImage(0,0,icy_img);


        GraphBlock addBlock = new GraphBlock();
        addBlock.Type = "AddXYBlock";
        PointCalculatorImpl.InitGraphBlock(addBlock,AddXYBlock.class);

        assertEquals("Should have only the image output", 1 ,addBlock.Outputs.size() );
        assertEquals("Should have image, x and y input", 3 ,addBlock.Inputs.size() );

        GraphBlock imageBlock = GraphRepository.CreateStandardImageLoadBlock();
        {
            imageBlock.InputByName(LoadImageWorkBlock.inputImage).SetValue(image);
            imageBlock.InputByName(LoadImageWorkBlock.inputName).SetValue(null);
        }

        addBlock.InputByName("X").SetValue(X);
        addBlock.InputByName("Y").SetValue(Y);

        BlockGraph graph = new BlockGraph();
        graph.AllBlocks.add(addBlock);
        graph.AllBlocks.add(imageBlock);

        BlockLink link = new BlockLink(imageBlock.OutputByName(LoadImageWorkBlock.outputName),imageBlock, addBlock.InputByName(PointCalculatorImpl.ImageInput), addBlock);
        graph.Links.add(link);

        ProcessingManager manager = new ProcessingManager(new MainThreadWorkManager());
        manager.InitManager(graph);
        assertTrue("Should run successful" , manager.StartProcessingLoop());

        assertEquals("Should have only the image output", 1 ,addBlock.Outputs.size() );
        assertTrue(addBlock.Outputs.get(0).isValid());
        Object result = addBlock.Outputs.get(0).getValue();
        assertNotNull(result);
        assertTrue(result instanceof AbstractImageObject);

        double[] resultData = ((AbstractImageObject)result).getDataXYAsDoubleArray(0,0,0);
        assertEquals(inputData.length, resultData.length);
        for (int i =0;i< inputData.length; i++)
        {
            assertEquals(inputData[i] + X +Y, resultData[i] , 0.0001);
        }
    }

    @Test()
    public void UseAddXBlockToTestThreadWorker()
    {
        int sizeX = 5;
        int sizeY = 5;
        int X = 1;
        int Y = 2;

        Sequence image = new Sequence();
        IcyBufferedImage icy_img = new IcyBufferedImage(sizeX, sizeY, 1, DataType.BYTE);
        byte[] inputData  = new byte[sizeX * sizeY];
        for (byte i= 0;i < inputData.length ; i++)
        {
            inputData[i] = i;
        }
        Array1DUtil.byteArrayToSafeArray(inputData, icy_img.getDataXY(0),icy_img.isSignedDataType() , icy_img.isSignedDataType());
        image.setImage(0,0,icy_img);

        BlockGraph graph = ConstructAddXGraph(X,Y,image, "DelayedAddXYBlock");

        ProcessingManager manager = new ProcessingManager(new ThreadsWorkerManager(1));
        manager.InitManager(graph);

        assertTrue("Should run successful" , manager.StartProcessingLoop());

        GraphBlock addBlock = GetBlockOfType(graph,"DelayedAddXYBlock");
        assertEquals("Should have only the image output", 1 ,addBlock.Outputs.size() );
        assertTrue(addBlock.Outputs.get(0).isValid());
        Object result = addBlock.Outputs.get(0).getValue();
        assertNotNull(result);
        assertTrue(result instanceof AbstractImageObject);

        double[] resultData = ((AbstractImageObject)result).getDataXYAsDoubleArray(0,0,0);
        assertEquals(inputData.length, resultData.length);
        for (int i =0;i< inputData.length; i++)
        {
            assertEquals(inputData[i] + X +Y, resultData[i] , 0.0001);
        }
    }

    @Test()
    public void TestInvertBlock()
    {
        InvertWorkBlock res = new InvertWorkBlock();

        int[] input = new int[5];


        Map<String, Object> inputs = new HashMap<>();
        inputs.put(InvertWorkBlock.Input1Name,input);
        res.SetInputs(inputs);
        res.RunWork();
        assertTrue(res.IsFinished());

        Map<String, Object>results = res.GetResults();
    }
}
