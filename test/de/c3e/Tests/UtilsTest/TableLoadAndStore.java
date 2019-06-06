package de.c3e.Tests.UtilsTest;

import de.c3e.ProcessManager.Utils.AbstractTableObject;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;


public class TableLoadAndStore
{
    @Test
    public void TestLoad()
    {
        AbstractTableObject toTest = new AbstractTableObject();
        toTest.ReadFromCsv("C:\\PHD\\UnitTest\\testcsv.txt",true,",");

        CheckData(toTest);
    }


    private void CheckData(AbstractTableObject toTest)
    {
        List<String> h = toTest.getHeader();
        assertNotNull(h);
        assertEquals(4, h.size());
        assertEquals("foo1", h.get(0));
        assertEquals("foo2", h.get(1));
        assertEquals("foo3", h.get(2));
        assertEquals("foo4", h.get(3));


        assertEquals(3, toTest.getRowCount());
        assertEquals(4, toTest.getColumnCount());

        assertEquals("1", toTest.get(0,0));
        assertEquals("2", toTest.get(0,1));
        assertEquals("3", toTest.get(0,2));
        assertEquals("4", toTest.get(0,3));

        assertEquals("5", toTest.get(1,0));
        assertEquals("6", toTest.get(1,1));
        assertEquals("7", toTest.get(1,2));
        assertEquals("8", toTest.get(1,3));

        assertEquals("9", toTest.get(2,0));
        assertEquals("10", toTest.get(2,1));
        assertEquals("10", toTest.get(2,2));
        assertEquals(null, toTest.get(2,3));

        assertEquals(null, toTest.get(10,10));
    }


    @Test
    public void TestSave()
    {
        AbstractTableObject testData = new AbstractTableObject();
        testData.ReadFromCsv("C:\\PHD\\UnitTest\\testcsv.txt",true,",");

        Path tmpFile;
        try
        {
            tmpFile = Files.createTempFile("UnitTest", "bla");
        }catch (Exception e)
        { assertTrue(false); return;}

        testData.StoreInCsv(tmpFile.toAbsolutePath().toString(),true, ",");

        AbstractTableObject toTest = new AbstractTableObject();
        toTest.ReadFromCsv(tmpFile.toAbsolutePath().toString(),true, ",");
        CheckData(toTest);
    }
}
