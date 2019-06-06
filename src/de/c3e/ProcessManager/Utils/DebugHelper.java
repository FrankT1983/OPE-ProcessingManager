package de.c3e.ProcessManager.Utils;

import com.google.common.base.Joiner;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Helper class for debugging.
 */
public class DebugHelper
{
    /**
     * Helper function I can use any time I have a point, where I want to break while debugging
     *  => Places I should never reach, but want to check for anyways  => needs just on breakpoint here
     */
    static public void BreakIntoDebug()
    {
        assert false;
    }

    public static void PrintSysInfo(Logger logger)
    {
        File tmpFolder = new File("/tmp/");
        String[] tmpList = tmpFolder.list();
        if (tmpList != null)
        {
            logger.error("Problem was on host" + getComputerName() + " Space left in tmp: " + tmpFolder.getFreeSpace() + "bytes. TmpContent: \n " + Joiner.on("\n").join(tmpList));
        }
        else
        {
            logger.error("Problem was on host" + getComputerName() + " Space left in tmp: " + tmpFolder.getFreeSpace() + "bytes.");
        }
    }

    public static void PrintException(Throwable ex, Logger logger)
    {
        String error = ExceptionUtils.getStackTrace(ex);
        if (logger != null)
        {
            logger.error(error);
        }
        else
        {
            System.out.println(error);
        }
    }

    private static String getComputerName()
    {
        Map<String, String> env = System.getenv();
        if (env.containsKey("COMPUTERNAME"))
            return env.get("COMPUTERNAME");
        else if (env.containsKey("HOSTNAME"))
            return env.get("HOSTNAME");
        else
            try
            {
                InetAddress addr;
                addr = InetAddress.getLocalHost();
                return addr.getHostName();
            }
            catch (UnknownHostException ex)
            {
                System.out.println("Hostname can not be resolved");
            }

            return "Unknown Computer";
    }
}




