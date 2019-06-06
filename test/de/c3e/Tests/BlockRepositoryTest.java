package de.c3e.Tests;

import de.c3e.ProcessManager.BlockRepository.*;

import static org.junit.Assert.*;

/**
 * Unit test for testing the block repo.
 */
public class BlockRepositoryTest
{
    @org.junit.Test
    public void getLongParameterBlock() throws Exception
    {
        BlockRepository toTest = new BlockRepository();
        Object result =  toTest.getWorkBlockFromType("LongParameter");
        assertTrue("Should return LongParameterWorkBlock", result instanceof LongParameterWorkBlock);
    }

    @org.junit.Test
    public void getLoadImageBlock() throws Exception
    {
        BlockRepository toTest = new BlockRepository();
        Object result =  toTest.getWorkBlockFromType("loadImage");
        assertTrue("Should return LongParameterWorkBlock", result instanceof LoadImageWorkBlock);
    }

    @org.junit.Test
    public void getInvertBlock() throws Exception
    {
        BlockRepository toTest = new BlockRepository();
        Object result =  toTest.getWorkBlockFromType("invert");
        assertTrue("Should return InvertWorkBlock", result instanceof InvertWorkBlock);
    }

    @org.junit.Test
    public void getOutBlock() throws Exception
    {
        BlockRepository toTest = new BlockRepository();
        Object result =  toTest.getWorkBlockFromType( OutputWorkBlock.typeName);
        assertTrue("Should return OutputWorkBlock", result instanceof OutputWorkBlock);
    }

}