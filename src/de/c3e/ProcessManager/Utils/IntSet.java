package de.c3e.ProcessManager.Utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Small class to use a set of ints.
 */
public class IntSet
{
    private final Object accessLock = new Object();

    private int[] set = new int[0];

    public int getFirstNonContained()
    {
        synchronized (accessLock)
        {
            for (int i = 1; i< set.length + 10; i++)
            {
                i++;
                if (!this.contains(i))
                {
                    return i;
                }
            }
            return set.length + 10;
        }
    }


    public int getRandomNonContained()
    {
        synchronized (accessLock)
        {
            while(true)
            {
                int i = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
                if (!this.contains(i))
                {
                    return i;
                }
            }
        }
    }

    public void add(int toAdd)
    {
        synchronized (accessLock)
        {
            if (this.contains(toAdd))
            {
                return;
            }

            int[] resized = new int[this.set.length + 1];
            System.arraycopy(this.set, 0, resized, 0, this.set.length);
            resized[this.set.length] = toAdd;

            this.set = resized;
        }
    }

    public boolean contains(int x)
    {
        synchronized (accessLock)
        {
            for (int aMap : this.set)
            {
                if (aMap == x)
                {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean contains(long x)
    {
        return this.contains((int) x);
    }

    public void remove(int toRemove)
    {
        synchronized (accessLock)
        {
            int[] resized = new int[this.set.length - 1];
            int j=0;
            int removedCount = 0;
            for (int entry : this.set)
            {
                if (entry != toRemove)
                {
                    resized[j++] = entry;
                } else
                {
                    removedCount++;
                }
            }

            if (removedCount > 1)
            {
                System.out.println("removed to much");
            }

            this.set = resized;
        }
    }

    public int size()
    {
        synchronized (accessLock)
        {
            return this.set.length;
        }
    }
}

