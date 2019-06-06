package de.c3e.ProcessManager;

/**
 * Class for global variables. This is bad, but for some simple things the best way to do it.
 */
public class GlobalSettings
{
    public static String WorkFolder = "";
    public static String IcyFolder = "";

    /**
     * A global flag to enable or disable dependency distribution: This means should blocks be splittable according
     * to there defined dependencies. This is a legacy code used to test dependency distribution against a non splitting
     * Environment.
     * todo: could very likely be removed.
     * This is purely for unit testing, and will not work in the real environment, because no shared
     * memory => if I need that later, will have to properly initialize my workers.
     */
    public static Boolean EnableDependencyDistribution = true;

    /**
     * Define global, how many pixel chunks should have.
     * This is purely for unit testing, and will not work in the real environment, because no shared
     * memory => if I need that later, will have to properly initialize my workers.
     */
    public static long OverridePixelEstimate = -1;

    public static Boolean EnableMultiPassProjectionCalculations = true;
}
