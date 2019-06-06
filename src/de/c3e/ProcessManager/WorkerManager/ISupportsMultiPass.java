package de.c3e.ProcessManager.WorkerManager;

/**
 * Interface for objects, that need to run through a block multiple times
 */
public interface ISupportsMultiPass
{
    boolean NeedsMorePasses();
}
