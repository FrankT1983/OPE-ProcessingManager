package de.c3e.BlockTemplates.Examples;

import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.BlockTemplates.Templates.Annotations.InputParameter;
import de.c3e.BlockTemplates.Templates.IndexedPointProjector;
import de.c3e.BlockTemplates.Templates.PointProjector;

@InputParameter(Name = "TimeIndex" , Description = "The time point to select" , Typ = int.class)
public class TimePointSelection extends IndexedPointProjector<Double>
{
    public TimePointSelection()
    {
        super(ImageDimension.T);
    }


    @Override
    public Double Aggregate(Double data1, Double data2, int posTData1, int posTData2)
    {
        int desiredT = this.getInput("TimeIndex");

        if (posTData1 == desiredT)
        {
            return data1;
        }

        if (posTData2 == desiredT)
        {
            return data2;
        }

        return 0.0;
    }
}

