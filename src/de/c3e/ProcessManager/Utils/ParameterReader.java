package de.c3e.ProcessManager.Utils;

import de.c3e.ProcessManager.DataTypes.ScriptInputParameters;
import de.c3e.ProcessManager.GlobalSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for reading in parameters from different types of files.
 */
public class ParameterReader
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public static List<ScriptInputParameters> ParametersFromParameterString(String parameterString)
    {
        List<ScriptInputParameters> result = new ArrayList<>();
        try
        {
            BufferedReader reader = new BufferedReader(new StringReader(parameterString));
            String line;
            while ((line = reader.readLine()) != null)
            {
                String[] splits = line.split("\t");
                if (splits.length > 3)
                {
                    ScriptInputParameters parameter = new ScriptInputParameters();
                    parameter.BlockId = splits[0].trim();
                    parameter.Direction = splits[1];
                    parameter.PortName = splits[2];
                    parameter.Value = splits[3];

                    logger.info("Parameter: " + parameter.BlockId + " | " + parameter.Direction + " | " + parameter.PortName + " | " + parameter.Value + " | "  );
                    result.add(parameter);
                }
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return result;
    }

    public static List<ScriptInputParameters> ParametersFromParameterFile(String parameterFilePath)
    {
        parameterFilePath = GlobalSettings.WorkFolder + parameterFilePath;
        File par = new File(parameterFilePath);

        return ParametersFromParameterString(TextFileUtil.FileToString(par));
    }

    public static List<ScriptInputParameters> ParametersFromParameterJsonFile(String parameterFilePath)
    {
        parameterFilePath = GlobalSettings.WorkFolder + parameterFilePath;
        File par = new File(parameterFilePath);
        if (!par.exists())
        {
            logger.error("could not find file " + par.getAbsolutePath());
            return null;
        }

        return ParametersFromJsonString(TextFileUtil.FileToString(par));
    }

    public static boolean DumpIntermediatesParameterFromJsonFile(String parameterFilePath)
    {
        parameterFilePath = GlobalSettings.WorkFolder + parameterFilePath;
        File par = new File(parameterFilePath);
        if (!par.exists())
        {
            return false;
        }

        return DumpIntermediatesParameterFromJsonString(TextFileUtil.FileToString(par));
    }

    public static boolean DumpIntermediatesParameterFromJsonString(String jsonString)
    {
        JSONObject obj;
        try{
            obj = new JSONObject(jsonString);
        }
        catch (Exception e)
        {
            logger.error("Could not parse parameters from json \n" + jsonString + " \n" + e.toString());
            throw e;
        }

        try
        {
            return obj.getBoolean("intermediates");
        }
        catch (JSONException e)
        {
            return false;
        }
    }

    public static List<ScriptInputParameters> ParametersFromJsonString(String jsonString)
    {
        List<ScriptInputParameters> result = new ArrayList<>();

        JSONObject obj;
        try{
            obj = new JSONObject(jsonString);
        }
        catch (Exception e)
        {
            logger.error("Could not parse parameters from json \n" + jsonString + " \n" + e.toString());
            throw e;
        }

        JSONArray array = obj.getJSONArray("parameters");
        if (array != null)
        {
            for(int p=0;p<array.length();p++)
            {
                JSONArray parameterObject = array.getJSONArray(p);

                if (parameterObject.length() < 4)
                {
                    logger.error("Parameter has unexpected length");
                    continue;
                }

                String elementId = parameterObject.getString(0);
                String portId = parameterObject.getString(1);
                String value = parameterObject.getString(2);
                String direction = parameterObject.getString(3);

                ScriptInputParameters parameter = new ScriptInputParameters();
                parameter.BlockId = elementId;
                parameter.Direction = direction;
                parameter.PortName = portId;
                parameter.Value = value;

                logger.info("Parameter: " + parameter.BlockId + " | " + parameter.Direction + " | " + parameter.PortName + " | " + parameter.Value + " | "  );
                result.add(parameter);
            }
        }

        return result;
    }
}