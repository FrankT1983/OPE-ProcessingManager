package de.c3e.ProcessManager.BlockRepository;

import de.c3e.ProcessManager.Utils.LogUtilities;
import de.c3e.ProcessingManager.Interfaces.IWorkBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LoggingWorkBlockBase implements IWorkBlock
{
    protected Logger logger;

    public LoggingWorkBlockBase()
    {
        this.logger = LoggerFactory.getLogger( this.getClass().getName());
        LogUtilities.ConfigureLogger(logger);
    }

    public boolean RunWork()
    {
        logger.info("Execute " + this.getClass().getName());
        return true;
    }

}
