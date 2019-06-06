package de.c3e.Tests.UtilsTest;


import de.c3e.ProcessManager.Main;
import de.c3e.Tests.TestTools;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Test, because it is easier to run stuff from here, than create a second main program
 */
public class FileCreationTest
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
        TestTools.StandardUnitTestPreperatins();
    }

    /**
     * This test is more to generate test data, than to acutally run a test
     */
    @Test
    public void CreateFile()
    {
        int sizeX=100;
        int sizeY=100;
        int sizeC=100;
        int sizeZ=100;
        int sizeT=100;


        for (int i_z = 1; i_z  < sizeC ; i_z*=10 )
        {
            for (int i_c = 1; i_c < sizeZ; i_c*=10)
            {
                CreateFile(sizeX, sizeY, i_c, i_z, sizeT);
            }
        }
    }

    private void CreateFile(int sizeX, int sizeY, int sizeC, int sizeZ, int sizeT)
    {
        String fileName =   String.valueOf(sizeX)+"_" +
                String.valueOf(sizeY)+"_" +
                String.valueOf(sizeC)+"_" +
                String.valueOf(sizeZ)+"_" +
                String.valueOf(sizeT);
        File outputFile = new File("C:\\Tmp\\ImageCreation\\"+fileName+ ".tiff");

        if (outputFile.exists())
        {return;}

        byte[] inputData = new byte[sizeX * sizeY * sizeC * sizeZ * sizeT];
        int current = 0;
        for (int t = 0; t <sizeT ; t++)
        {
            for (int z = 0; z < sizeZ; z++)
            {
                for (int c = 0; c < sizeC; c++)
                {
                    for (int i = 0; i < sizeX * sizeY; i++)
                    {
                        inputData[current] = (byte) (i + z * 13 + c * 7 + t*5);
                        current++;
                    }
                }
            }
        }


        TestTools.WriteImageToFile(outputFile,inputData,sizeX,sizeY,sizeC, sizeZ, sizeT);
    }
}
