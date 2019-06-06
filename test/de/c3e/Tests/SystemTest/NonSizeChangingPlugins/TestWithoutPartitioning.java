package de.c3e.Tests.SystemTest.NonSizeChangingPlugins;

import de.c3e.ProcessManager.GlobalSettings;
import de.c3e.ProcessManager.Main;
import de.c3e.Tests.TestTools;
import org.junit.Before;
import org.junit.Test;

/**
 * Test to ensure that the plugins work with disabled data distribution in the same way as with.
 */
public class TestWithoutPartitioning
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
        TestTools.StandardUnitTestPreperatins();
        GlobalSettings.EnableDependencyDistribution = false;
    }

    @Test
    public void TestPluginsWithoutPartitioning()
    {
        TestCubeToMaxXYC t1 = new TestCubeToMaxXYC();
        t1.CubeMaxXYC_Test();
        t1.CubeMaxXYZ_Test();

        TestCustomThreshold t2 = new TestCustomThreshold();
        t2.CustomThreshold_Test();

        TestHyperCubeToMax t3 = new TestHyperCubeToMax();
        t3.HyperCubeToMaxXYCT_Test();
        t3.HyperCubeToMaxXYCZ_Test();

        TestPlaneToMaxXZ t4 = new TestPlaneToMaxXZ();
        t4.PlaneToMaxXY_Test();
        t4.PlaneToMaxXZ_Test();
    }
}
