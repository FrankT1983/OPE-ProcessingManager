package de.c3e.Tests.SystemTest;

import de.c3e.ProcessManager.Utils.AbstractImageObject;
import de.c3e.ProcessManager.Utils.ImageSubBlock;
import de.c3e.ProcessManager.Utils.ImageSubSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AbstractImageTests
{
    @Test
    public void TestEvictin()
    {
        ImageSubBlock foo = new ImageSubBlock();
        foo.dimensions = new ImageSubSet(10, 5);
        byte[] data = new byte[50];
        for(int i=0;i<50;i++) { data[i]=(byte)i;}
        foo.data = data;
        foo.type = byte.class;

        AbstractImageObject img = AbstractImageObject.fromSubBlock(foo);
        img.Evict();

        assertTrue(img.isEvicted());

        img.DeEvict();



    }
}
