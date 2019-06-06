package de.c3e.Tests.SystemTest;

import de.c3e.ProcessManager.Utils.DebugHelper;
import de.c3e.ProcessManager.Utils.LogUtilities;
import org.junit.Test;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public class ToolTests
{
    @Test
    public  void TestSysInfo()
    {
        Logger log = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());
        DebugHelper.PrintSysInfo(log);
    }
}
