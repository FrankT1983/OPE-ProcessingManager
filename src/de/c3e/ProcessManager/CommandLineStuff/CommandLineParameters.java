package de.c3e.ProcessManager.CommandLineStuff;

import com.google.common.base.Joiner;
import de.c3e.ProcessManager.Utils.LogUtilities;
import org.slf4j.Logger;
import org.apache.commons.cli.*;

import java.lang.invoke.MethodHandles;

/**
 * Helper class to parse the parameters passed to this application.
 */
public class CommandLineParameters
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public static Options BuildCommandLineOptions()
    {
        Options options = new Options();
        Option opt1 =new Option("param",ParameterFileArg ,true,"A file Specifying all parameters in a file");
        opt1.setRequired(false);
        options.addOption(opt1);


        Option opt2 =new Option("protocol",ProtocolFileArg ,true,"A file specifying the protocol in a file");
        opt2.setRequired(false);
        options.addOption(opt2);


        Option opt3 =new Option("json",WorkingFolderArg ,true,"A json file specifying both parameters an protocol");
        opt3.setRequired(false);
        options.addOption(opt3);

        Option opt4 =new Option("wf",JsonFileArg ,true,"The path to the working folder");
        opt4.setRequired(false);
        options.addOption(opt4);

        Option opt5 =new Option("b",BalancerArg ,true,"The used load balancer.");
        opt5.setRequired(false);
        options.addOption(opt5);

        Option opt6 =new Option("bc",BalancerSizeArg ,true,"The desired pixel count, for constant size balancer.");
        opt6.setRequired(false);
        options.addOption(opt6);

        Option opt7 =new Option("d",DelayArg ,true,"A factor by which the transfer speed should be decreased");
        opt7.setRequired(false);
        options.addOption(opt7);

        Option opt8 =new Option("s",SingleArg ,false,"Run in single node mode.");
        opt8.setRequired(false);
        options.addOption(opt8);

        return options;
    }

    private static final String ParameterFileArg = "ParamFile";
    private static final String ProtocolFileArg = "ProtocolFile";
    private static final String WorkingFolderArg = "WorkingFolder";
    private static final String JsonFileArg = "JsonFile";
    private static final String BalancerArg = "Balancer";
    private static final String BalancerSizeArg = "BalancerSize";
    private static final String DelayArg = "Delay";
    private static final String SingleArg = "SingleNode";

    public final String ParameterFile;
    public final String ProtocolFile;
    public final String WorkingFolder;
    public final String JsonFile;
    public final String Balancer;
    public String BalancerSize;
    public String TransferDelay;
    public final boolean RunInSingleNodeMode;

    public CommandLineParameters(String[] args)
    {
        Options options = BuildCommandLineOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        System.out.println("ParseCommandLine");
        System.out.println(Joiner.on(";").join(args));

        CommandLine cmd;
        String parmFile = null;
        String protFile = null;
        String workFolder = null;
        String jsonFile = null;
        String balanc = null;
        Boolean singleNode = false;


        try
        {
            cmd = parser.parse(options, args);
            parmFile= cmd.hasOption(ParameterFileArg) ? cmd.getOptionValue(ParameterFileArg) : null;
            protFile = cmd.hasOption(ProtocolFileArg) ? cmd.getOptionValue(ProtocolFileArg) : null;
            workFolder = cmd.hasOption(WorkingFolderArg) ? cmd.getOptionValue(WorkingFolderArg) : null;
            jsonFile = cmd.hasOption(JsonFileArg) ? cmd.getOptionValue(JsonFileArg) : null;
            balanc = cmd.hasOption(BalancerArg) ? cmd.getOptionValue(BalancerArg) : null;
            singleNode = cmd.hasOption(SingleArg);

            this.BalancerSize = cmd.hasOption(BalancerSizeArg) ? cmd.getOptionValue(BalancerSizeArg) : null;
            this.TransferDelay = cmd.hasOption(DelayArg) ? cmd.getOptionValue(DelayArg) : null;

        }
        catch (ParseException e)
        {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
        }

        this.ParameterFile = parmFile;
        this.ProtocolFile = protFile;
        this.WorkingFolder = workFolder;
        this.JsonFile = jsonFile;
        this.Balancer = balanc;
        this.RunInSingleNodeMode = singleNode;

        if (this.ParameterFile != null)
        {
            logger.info("Paramfile found:  " + this.ParameterFile);
        }

        if (this.ProtocolFile != null)
        {
            logger.info("Protocol found:  " + this.ProtocolFile);
        }

        if (this.WorkingFolder != null)
        {
            logger.info("Workingfolder found:  " + this.WorkingFolder);
        }

        if (this.JsonFile != null)
        {
            logger.info("Json File found:  " + this.JsonFile);
        }

        if (this.Balancer != null)
        {
            logger.info("Balancer found:  " + this.Balancer);
        }
    }
}
