package de.c3e.ProcessManager.DataTypes;

import de.c3e.ProcessManager.BlockRepository.*;

/**
 * Helper class to generate standard test graphs.
 */
public class GraphRepository
{
    public static GraphBlock CreateStandardOutBlock()
    {
        GraphBlock output = new GraphBlock();
        output.Type = OutputWorkBlock.typeName;

        BlockIO inValue1 = new BlockIO();
        inValue1.Name = OutputWorkBlock.ImageInput;
        output.Inputs.add(inValue1);

        BlockIO inValue2 = new BlockIO();
        inValue2.Name = OutputWorkBlock.PathInput;
        output.Inputs.add(inValue2);
        return output;
    }

    public static GraphBlock CreateStandardDataToFileBlock()
    {
        GraphBlock output = new GraphBlock();
        output.Type = DataToCsvFileBlock.TypeName;

        BlockIO inValue1 = new BlockIO();
        inValue1.Name = DataToCsvFileBlock.dataName;
        output.Inputs.add(inValue1);

        BlockIO inValue2 = new BlockIO();
        inValue2.Name = DataToCsvFileBlock.fileName;
        output.Inputs.add(inValue2);
        return output;
    }

    public static GraphBlock CreateStandardStringBlock(String id)
    {
        GraphBlock inputImageId = new GraphBlock();
        inputImageId.Type = StringParameterWorkBlock.typeName;
        inputImageId.Id = id;

        BlockIO outValue = new BlockIO();
        outValue.Name = StringParameterWorkBlock.outputName;

        inputImageId.Outputs.add(outValue);

        return inputImageId;
    }

    public static GraphBlock CreateStandardImageLoadBlock()
    {return CreateStandardImageLoadBlock(null,LoadImageWorkBlock.typeName,LoadImageWorkBlock.inputName,LoadImageWorkBlock.inputImage,LoadImageWorkBlock.outputName);}


    static GraphBlock CreateStandardImageLoadBlock(String id, String loadBlockName, String loadInputName, String loadInputFile, String loadOutputName)
    {
        GraphBlock loadImage = new GraphBlock();
        loadImage.Id = id;
        loadImage.Type = loadBlockName;
        BlockIO input1 = new BlockIO();
        input1.Name = loadInputName;
        input1.Id = loadInputName;
        loadImage.Inputs.add(input1);

        BlockIO input2 = new BlockIO();
        input2.Name =loadInputFile;
        input2.Id = loadInputFile;
        input2.SetValue(null);  // optional parameter, start it with valid.
        loadImage.Inputs.add(input2);

        BlockIO outValue = new BlockIO();
        outValue.Name = loadOutputName;
        outValue.Id =loadOutputName;
        loadImage.Outputs.add(outValue);

        return loadImage;
    }

    public static GraphBlock CreateStandardResultSinkBlock()
    {
        GraphBlock resultSink = new GraphBlock();
        resultSink.Type = CollectResultsWorkBlock.typeName;


        BlockIO input1 = new BlockIO();
        input1.Name = CollectResultsWorkBlock.ResultFilePath;
        input1.Id = CollectResultsWorkBlock.ResultFilePath;
        resultSink.Inputs.add(input1);

        BlockIO input2 = new BlockIO();
        input2.Name = CollectResultsWorkBlock.ResultInput;
        input2.Id = CollectResultsWorkBlock.ResultInput;
        resultSink.Inputs.add(input2);

        return resultSink;
    }
}
