package de.c3e.ProcessManager.BlockRepository;


import de.c3e.BlockTemplates.Templates.Interfaces.ISupportsSplitting;
import de.c3e.ProcessManager.Utils.IcyHelpers;

import de.c3e.ProcessingManager.Types.SplitType;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.Var;
import plugins.adufour.vars.lang.VarROIArray;
import plugins.adufour.vars.lang.VarSequence;

import java.util.*;

/**
 * Wrapper class for using icy blocks in this manager.
 * Note:
 * // Icy has a design flaw here:
 * // ports have a name and an id
 * // When I call  this.icyBlock.declareOutput(this.outPutList)
 * //                  it will give me a bunch of objects, from which I can only get the name
 * // Links how ever are declared using the ID of the port
 * // => At runtime, without the xml description, I can not map with declared output goes to what link
 * // just in case I forget thi detail again
 **/
public class IcyBlockWrapper extends LoggingWorkBlockBase implements ISupportsSplitting
{
    Block icyBlock;
    boolean isFinished = false;
    VarList outPutList = new VarList();
    List<Var<?>>  tmpList = new ArrayList<>();

    public IcyBlockWrapper(Object myObject)
    {
        if (myObject instanceof  Block)
        {
            this.icyBlock = (Block) myObject;
        }
    }

    @Override
    public boolean RunWork()
    {
        if (this.icyBlock == null)
        {   return false; }

        logger.info("Execute " + this.getClass().getName() + " " + this.icyBlock.getClass().getName());
        this.icyBlock.declareOutput(this.outPutList);

        ReferenceOutputs();

        this.icyBlock.run();
        this.isFinished = true;
        logger.info("Execute " + this.getClass().getName() + " " + this.icyBlock.getClass().getName() + " finished");
        return true;
    }

    @Override
    public SplitType getSplitType()
    {
        return  SplitType.cantSplit;
    }

    /**
     * Some icy blocks calcualte only outputs that are used.
     * Since I currently don't tell a block wich output is used, I have to reference all of them. Just in case.
     */
    private void ReferenceOutputs()
    {
        for (Var<?> out : outPutList )
        {
            if (out instanceof VarSequence)
            {
                VarSequence tmp = new VarSequence("tmp out" , null);
                tmp.setReference((VarSequence)out);
                this.tmpList.add(tmp);
            }

            if (out instanceof VarROIArray)
            {
                VarROIArray tmp = new VarROIArray("tmp out");
                tmp.setReference((VarROIArray)out);
                this.tmpList.add(tmp);
            }
        }
    }

    @Override
    public void SetInputs(Map<String, Object> inputs)
    {
        VarList inputList = new VarList();
        this.icyBlock.declareInput(inputList);
        Set<String> allreadySet = new HashSet<>();

        for (Var<?> input : inputList )
        {
            String inputName = input.getName();
            if (inputs.containsKey(inputName))
            {
                IcyHelpers.BoxAndSetToIcyVar(input, inputs.get(inputName));
                allreadySet.add(inputName);
            }
        }

        for (String key : inputs.keySet())
        {
            Var<?> input = inputList.get(key);
            if ((input != null))
            {
                // already set , potentially dangerous ?
                if (allreadySet.contains(key))
                {   continue;   }

                IcyHelpers.BoxAndSetToIcyVar(input, inputs.get(key));
            }
        }
    }

    @Override
    public Map<String, Object> GetResults()
    {
        Map<String, Object> result = new HashMap<>();
        for (Var<?> output : this.outPutList )
        {
            //System.out.println(output);
            Object unBoxed = IcyHelpers.UnboxIcyVar(output);
            if (unBoxed == null)
            {   continue;   }

            result.put(output.getName(), unBoxed);

            // hack: see class description
            result.put(this.outPutList.getID(output), unBoxed);
        }
        return result;
    }

    @Override
    public boolean IsFinished()
    {
        return this.isFinished;
    }
}
