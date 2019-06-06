package de.c3e.BlockTemplates.Examples;


import de.c3e.BlockTemplates.Templates.Annotations.InputParameter;
import de.c3e.BlockTemplates.Templates.SinglePointCalculator;

/// Simple example for a plugin to calculate the threshold of an image .
/// note , Bytes are signed in Java => −128 . . 127

// define needed parameter
@InputParameter( Name ="Threshold", Typ = Byte.class ,
         Description = "The value over which the result pixel should be 1 not 0" )

// class will be found through reflection => can suppress this warning
@SuppressWarnings("unused")

public class Threshold extends SinglePointCalculator<Byte>
{
    @Override
    public Byte Calculate(Byte data)
    {
        Byte threshold = getInput ( "Threshold" ) ;
        return ( data < threshold ) ? ( byte ) 0 : ( byte ) 1;
    }
}
