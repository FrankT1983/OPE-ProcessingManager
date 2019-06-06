package de.c3e.BlockTemplates.Examples;


import de.c3e.BlockTemplates.Templates.Annotations.InputParameter;
import de.c3e.BlockTemplates.Templates.Annotations.InputParameterDeclarations;
import de.c3e.BlockTemplates.Templates.PointCalculator;


/**
 * Example calls for Point calculator class.
 */

@InputParameterDeclarations({ @InputParameter(Name = "X" , Description = "TestParameter" , Typ = Double.class) , @InputParameter(Name = "Y" , Description = "TestParameter" , Typ = Double.class)})
public class AddXYBlock extends PointCalculator
{
    @Override
    public double Calculate(double input)
    {
        return input + this.getDoubleInput("X") + this.getDoubleInput("Y");
    }
}
