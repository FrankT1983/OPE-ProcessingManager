package de.c3e.ProcessManager.Utils;

/***
 * This is somewhat of a hack. Mark objects that will receive data during runtime to ensure the are cloned, before they
 * are serialized ... this should ConcurrentModificationException exception.
 */
public interface IIsPartOfActiveCommunication
{
    Object CreateShallowClone();
}

