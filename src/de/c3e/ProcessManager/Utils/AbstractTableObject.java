package de.c3e.ProcessManager.Utils;

import de.c3e.ProcessManager.DataTypes.ResultGraphPosition;
import de.c3e.ProcessManager.WorkerManager.ICommunicationInfrastructure;
import de.c3e.ProcessManager.WorkerManager.IPartialResult;
import de.c3e.ProcessManager.WorkerManager.IRemoteObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/***
 * Simple class for holding a data table ... such as a csv file.
 *
 * todo: not thread safe ,... do I need this thread safe?
 */
public class AbstractTableObject implements INeedsComInfrastructe, IPartialResult, IRemoteObject, Serializable, IIsPartOfActiveCommunication
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public AbstractTableObject(){}

    private AbstractTableObject(List<String> header, List<List<String>> data)
    {
        if (header !=null)
        {
            this.header = new ArrayList<>(header);
        }

        int max = 0;
        for(List<String> d : data)
        {
            List<String> nd = new ArrayList<>(d);
            max = Math.max(max,d.size());
            this.data.add(nd);
        }
        this.maxColumnCount = max;
    }

    public List<RemoteObjectBlock> getRemoteBlocks()
    {
        return remoteBlocks;
    }

    private List<String> header;
    // todo: maybe give each data fragment an id
    private final List<List<String>> data = new ArrayList<>();
    private int maxColumnCount = 0;
    private ICommunicationInfrastructure comInfrastructure ;
    private ResultGraphPosition graphPosition;

    final private List<RemoteObjectBlock> remoteBlocks = new ArrayList<>();



    public AbstractTableObject(String path)
    {
        this.ReadFromCsv(path,true,",");
    }

    public List<String> getHeader()
    {
        return header;
    }

    public int getRowCount()
    {
        return data.size();
    }

    public int getColumnCount()
    {
        return maxColumnCount;
    }

    public Object get(int row, int column)
    {
        if (this.data.size() <= row)
        {   return null;}


        if (this.data.get(row).size() <= column)
        {   return null;}


        return this.data.get(row).get(column);
    }

    public static AbstractTableObject fromSubTables(List<AbstractTableObject> partialTables)
    {
        AbstractTableObject result = new AbstractTableObject();
        if (partialTables.size() > 0)
        {
            result.header = partialTables.get(0).header;
            for (AbstractTableObject t : partialTables)
            {
                result.data.addAll(t.data);
            }
        }
        return  result;
    }

    @Override
    public void setCommunicationInfrastructure(ICommunicationInfrastructure infrastructure)
    {   this.comInfrastructure = infrastructure;    }



    @Override
    public synchronized void AddPart(Object value)
    {
        synchronized (this.data)
        {
            //logger.info("Add Parts");
            if (value instanceof AbstractTableObject)
            {
                synchronized (this.remoteBlocks)
                {
                    AbstractTableObject tab = (AbstractTableObject) value;
                    if (this.graphPosition.equals(tab.graphPosition))
                    {
                        RemoteObjectBlock.AddNewOnes(this.remoteBlocks,tab.getRemoteBlocks());
                    }

                    if (tab.data.size()>0)
                    {
                        this.data.addAll(tab.data);
                    }
                }
            }else
            if (value instanceof TablePart)
            {
                TablePart part = (TablePart) value;
                synchronized (this.remoteBlocks)
                {
                    if (this.header == null && (part.header != null))
                    {
                        this.header = part.header;
                    }

                    this.data.addAll(part.data);
                    this.RemoveRemoteObject(part.graphPosition, part.onWorker);
                }
            }
        }
    }

    public int getColumnIndex(String nameOfColumn)
    {
        synchronized (this.data)
        {
            if (this.header == null)
            {
                return -1;
            }

            for (int i = 0; i < this.header.size(); i++)
            {
                if (this.header.get(i).equals(nameOfColumn))
                {
                    return i;
                }
            }
        }
        return -1;
    }

    public List<String> getColumn(int i)
    {
        synchronized (this.data)
        {
            List<String> c = new ArrayList<>();
            for (List<String> r : data)
            {
                c.add(r.get(i));
            }
            return c;
        }
    }

    @Override
    public Object CreateShallowClone()
    {
        AbstractTableObject res = new AbstractTableObject(this.header,this.data);
        res.setObjectGraphPosition(this.graphPosition);
        res.getRemoteBlocks().addAll(this.getRemoteBlocks());
        return res;
    }

    private void RemoveRemoteObject(ResultGraphPosition graphPosition, int onWorker)
    {
        synchronized (this.remoteBlocks)
        {
            List<RemoteObjectBlock> toDelete = new ArrayList<>();
            for (RemoteObjectBlock block : this.remoteBlocks)
            {
                if (block.getWorkerId() == onWorker)
                {
                    if ((graphPosition == null ) || block.getFullObjectId().equals(graphPosition))
                    {
                        toDelete.add(block);
                    }
                }
            }
            this.remoteBlocks.removeAll(toDelete);
            //logger.info(this + "Removed " + toDelete.size() + " parts at " + graphPosition.toString() + " from worker " + onWorker + " "  + this.remoteBlocks.size() + " remote blocks remaining");
        }
    }

    @Override
    public Object GetLocalParts(int myRank)
    {
        TablePart res = new TablePart();
        for (List<String> row: this.data)
        {
            res.data.add(new ArrayList<>(row));
        }

        res.header = new ArrayList<>(this.header);
        res.graphPosition = this.graphPosition;

        if (this.comInfrastructure != null)
        {   res.onWorker = this.comInfrastructure.getMyRank();}
        else
        { res.onWorker = myRank;}
        return res;
    }

    @Override
    public boolean isComplete()
    {
        return true;
    }

    @Override
    public ResultGraphPosition getObjectGraphPosition()
    {
        return this.graphPosition;
    }

    @Override
    public void setObjectGraphPosition(ResultGraphPosition position)
    {
        this.graphPosition = position;
    }

    @Override
    public int getWorkerId()
    {
        return 0;
    }

    @Override
    public Object CreateEmptyRepresentation(int localWorkerId)
    {
        AbstractTableObject result = new AbstractTableObject();
        result.setObjectGraphPosition(this.getObjectGraphPosition());
        RemoteObjectBlock remotePart = new RemoteObjectBlock();
        remotePart.setWorkerId(localWorkerId);
        remotePart.setFullObjectPosition(this.getObjectGraphPosition());
        result.getRemoteBlocks().add(remotePart);
        return result;
    }

    @Override
    public void PullAllRemoteParts()
    {
        logger.info("Pull all remote table parts: " + this.remoteBlocks);
        // local temp to prevent java.util.ConcurrentModificationException

        if (this.comInfrastructure != null)
        {
            RemoveRemoteObject(null, this.comInfrastructure.getMyRank());
        }

        List<RemoteObjectBlock> localTemp = new ArrayList<>(this.remoteBlocks);
        for (RemoteObjectBlock b :localTemp)
        {
            this.comInfrastructure.RequestPartOfPartialObject(new RemoteObjectPartRequest(b));
        }

        while(this.remoteBlocks.size()>0)
        {
            try
            {
                // sleep while waiting for the results.
                Thread.sleep(100);
            }
            catch (Exception e)
            {
                DebugHelper.PrintException(e,logger);
                e.printStackTrace();
            }
        }

        logger.info("PullAllRemoteParts finished");
    }

    public void set(int row, int column, Object toAdd)
    {
        while(this.data.size()<row)
        {data.add(new ArrayList<String>());}

        List<String> curRow = this.data.get(row);
        while(curRow.size()<=column)
        {curRow.add(null);}
        curRow.set(column,toAdd.toString());
    }

    public void StoreInCsv(String pathToCsv, boolean firstLineAsHeader, String separator)
    {
        synchronized (this.data)
        {
            try (PrintWriter out = new PrintWriter(pathToCsv))
            {
                if (firstLineAsHeader && this.header != null && this.header.size() > 0)
                {
                    out.println(StringUtils.join(this.header, separator));
                }

                for (List<String> line : this.data)
                {
                    out.println(StringUtils.join(line, separator));
                }

            } catch (Exception ex)
            {
                logger.error("Exception writing line:\n" + ex.toString());
                DebugHelper.PrintException(ex, logger);
                DebugHelper.BreakIntoDebug();
            }
        }
    }

    // todo: return some kind of error code
    public void ReadFromCsv(String pathToCsv, boolean firstLineAsHeader, String separator)
    {
        // Open the file
        try( BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(pathToCsv))))
        {
            String line = br.readLine();
            if (line == null)
            {   return;}

            this.header = null;
            if (firstLineAsHeader)
            {
                ParseHeader(line, separator);
                line = br.readLine();
            }

            this.data.clear();
            while (line != null)
            {
                line = line.trim();
                if (line.length() ==0) {continue;}  // skip empty lines ... todo: do I really want that?
                ParseAndAddData(line,separator);
                line = br.readLine();
            }
        }
        catch (Exception ex)
        {
            DebugHelper.BreakIntoDebug();
        }
    }

    private void ParseAndAddData(String line, String separator)
    {
        if (line== null)
        {return;}

        String[]  parts = line.split(separator);
        this.maxColumnCount = Math.max(this.maxColumnCount, parts.length);
        this.data.add(new ArrayList<>(Arrays.asList(parts)));
    }

    private void ParseHeader(String line, String separator)
    {
        if (line== null)
        {return;}

        String[]  parts = line.split(separator);
        this.header = new ArrayList<>(Arrays.asList(parts));
    }
}



class TablePart implements Serializable
{
    public List<String> header;
    public List<List<String>> data = new ArrayList<>();
    public ResultGraphPosition graphPosition;
    public int onWorker;
}