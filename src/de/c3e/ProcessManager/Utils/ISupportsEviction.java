package de.c3e.ProcessManager.Utils;

public interface ISupportsEviction
{
    void Evict();
    void DeEvict();

    boolean isEvicted();
}
