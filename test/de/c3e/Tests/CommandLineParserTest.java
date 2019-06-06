package de.c3e.Tests;


import de.c3e.ProcessManager.CommandLineStuff.CommandLineParameters;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CommandLineParserTest
{
    @Test
    public void TestSingleNodeCommandLine()
    {
        String[] par = new String[1];
        par[0] = "--Single";
        CommandLineParameters params = new CommandLineParameters(par);
        assertTrue(params.RunInSingleNodeMode);
    }

    @Test
    public void TestSingleNodeCommandLineNone()
    {
        String[] par = new String[0];
        CommandLineParameters params = new CommandLineParameters(par);
        assertFalse(params.RunInSingleNodeMode);
    }
}
