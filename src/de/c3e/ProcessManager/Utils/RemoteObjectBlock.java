package de.c3e.ProcessManager.Utils;

import de.c3e.ProcessManager.DataTypes.ResultGraphPosition;
import de.c3e.ProcessManager.WorkerManager.IRemoteObjectPart;

import java.util.ArrayList;
import java.util.List;

public class RemoteObjectBlock implements IRemoteObjectPart,  java.io.Serializable
{
    private int workerId;
    private ResultGraphPosition fullObjectPosition;

    @Override
    public int getWorkerId()
    {
        return workerId;
    }

    public void setWorkerId(int newId) { workerId = newId;}

    @Override
    public ResultGraphPosition getFullObjectId()
    {
        return fullObjectPosition;
    }


    public static void AddNewOnes(List<RemoteObjectBlock> list, List<RemoteObjectBlock> toAdd)
    {

        for(RemoteObjectBlock add : toAdd)
        {
            boolean doAdd = true;
            for(RemoteObjectBlock ex : list)
            {
                if (
                        ( add.getWorkerId() == ex.getWorkerId())&&
                        (add.fullObjectPosition.equals(ex.fullObjectPosition))
                        )
                {
                    doAdd=false;
                    break;
                }
            }

            if (doAdd)
            {
                list.add(add);
            }
        }
    }


    @Override
    public String getPartIdentification()
    {
        return "";
    }

    public void setFullObjectPosition(ResultGraphPosition fullObjectPosition)
    {
        this.fullObjectPosition = fullObjectPosition;
    }

    @Override
    public  String toString()
    {
        return "Worker " + workerId + " " + fullObjectPosition.toString();
    }
}
