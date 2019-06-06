package de.c3e.ProcessManager.Utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Since I have to use the logging from bio formats (thanks java for beeing difficult to use => Helper Calls for that
 */
public class LogUtilities
{
    private static  final String ProcessingManagerLog = "ProccessingManagerLogAppender";
    private static List< ch.qos.logback.classic.Logger> loggers = new ArrayList<>();

    public static Logger ConstructLogger(String className)
    {
        Logger logger = LoggerFactory.getLogger(className);
        LogUtilities.ConfigureLogger(logger);
        return logger;
    }

    public static void ConfigureLogger(Logger logger)
    {
        Object loggerfactory = LoggerFactory.getILoggerFactory();
        if ((loggerfactory instanceof ch.qos.logback.classic.LoggerContext) && (logger instanceof ch.qos.logback.classic.Logger))
        {
            ch.qos.logback.classic.LoggerContext context = (ch.qos.logback.classic.LoggerContext)loggerfactory;
            ch.qos.logback.classic.Logger test = (ch.qos.logback.classic.Logger) logger;

            // add appender only once
            Appender exists = test.getAppender(ProcessingManagerLog);
            if (exists != null)
            {   return; }

            PatternLayoutEncoder ple = new PatternLayoutEncoder();
            ple.setPattern("%date %level [%file:%line] %msg%n");
            ple.setContext(context);
            ple.start();

            {
                FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
                fileAppender.setName(ProcessingManagerLog);
                fileAppender.setFile("log.log");
                fileAppender.setEncoder(ple);
                fileAppender.setContext(context);
                fileAppender.start();

                test.setLevel(Level.DEBUG);
                test.addAppender(fileAppender);
            }


            loggers.add(test);
        }
    }

    public static void ChangeLogLevel(Level newLevel)
    {
        for(ch.qos.logback.classic.Logger l : loggers)
        {
            l.setLevel(newLevel);
        }

    }
}
