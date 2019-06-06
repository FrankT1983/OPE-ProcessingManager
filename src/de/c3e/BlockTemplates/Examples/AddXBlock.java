package de.c3e.BlockTemplates.Examples;

import de.c3e.BlockTemplates.Templates.Annotations.InputParameter;
import de.c3e.BlockTemplates.Templates.Annotations.DefaultValueDouble;
import de.c3e.BlockTemplates.Templates.PointCalculator;

/**
 * Example block for using the Point calculator: Add a constant
 */

@InputParameter(Name = "Value" , Description = "TestParameter" , Typ = Double.class)
@DefaultValueDouble(Name = "Value", Value = 0)
public class AddXBlock extends PointCalculator
{
    @Override
    public double Calculate(double input)
    {
        return Math.max(input + this.getDoubleInput("Value"),0);
    }

    public byte Calculate(byte input)
    {
        return (byte)Math.max(input + this.getDoubleInput("Value").byteValue(),0);
    }
}

