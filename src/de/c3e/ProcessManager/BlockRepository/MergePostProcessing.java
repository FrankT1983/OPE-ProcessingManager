package de.c3e.ProcessManager.BlockRepository;

import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.ProcessManager.Utils.ImageSubSet;

import java.util.Collection;

public abstract class MergePostProcessing
{
    abstract public void DoModifications(Object toModify, ImageSubSet dimensions, Collection<ImageDimension> trackingDimensions);
}
