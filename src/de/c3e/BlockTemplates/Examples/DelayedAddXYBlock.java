package de.c3e.BlockTemplates.Examples;


import de.c3e.BlockTemplates.Templates.Annotations.InputParameter;
import de.c3e.BlockTemplates.Templates.Annotations.InputParameterDeclarations;
import de.c3e.BlockTemplates.Templates.PointCalculator;

/**
 * Test Block with time delay for unit tests
 */
@InputParameterDeclarations({ @InputParameter(Name = "X" , Description = "TestParameter" , Typ = Double.class) , @InputParameter(Name = "Y" , Description = "TestParameter" , Typ = Double.class)})
public class DelayedAddXYBlock extends PointCalculator
{
        @Override
        public double Calculate(double input)
        {
            try
            {
                // sleep a bit for testing purposes : Remember this will add to each unit test => for each pixel
                Thread.sleep(500 / 25);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return input + this.getDoubleInput("X") + this.getDoubleInput("Y");
        }
}
