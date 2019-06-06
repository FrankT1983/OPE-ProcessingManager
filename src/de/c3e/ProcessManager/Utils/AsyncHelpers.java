package de.c3e.ProcessManager.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Helper class for providing some functionalism's when working with async stuff.
 */
public class AsyncHelpers
{
    public static <T> List<T> FuturesToList(List<Future<T>> futures) throws Exception
    {
        List<T> result = new ArrayList<>();
        for(Future<T> fut : futures)
        {
            result.add(fut.get());
        }
        return result;
    }

}
