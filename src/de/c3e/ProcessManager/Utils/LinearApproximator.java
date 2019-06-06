package de.c3e.ProcessManager.Utils;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.ArrayList;
import java.util.List;


public class LinearApproximator implements IApproximator
{
    public LinearApproximator()
    {}

    private double bucketSize = -1;
    public LinearApproximator(double bucketSize)
    {
        this.bucketSize = bucketSize;
    }

    private SimpleRegression regression = new  SimpleRegression();

    private List<double[]> values = new ArrayList<>();

    @Override
    public void AddDataPoints(double x, double y)
    {
        this.regression.addData(x,y);
        this.values.add(new double[]{x,y});
    }


    @Override
    public double Approximate(double x)
    {
        double prediction =  this.regression.predict(x);

        if (!Double.isNaN(prediction))
        {
            return Math.max(prediction,0);
        }

        List<double[]> inBucket = new ArrayList<>();
        // find buckets around x;
        for (double[] v : this.values)
        {
            if (InRange(v[0], x - 0.5*this.bucketSize, x + 0.5*this.bucketSize))
            {
                inBucket.add(v);
            }
        }

        {
            double average = 0.0;
            for (double[] v : this.values)
            {
                average += ( v[1] / v[0]) / (double)this.values.size();
            }
            return Math.max(average * x,0);
        }


        //return prediction;
        // todo: if nan => bucket
    }

    static  boolean InRange(double value, double lower, double upper)
    {
        return value>lower && value < upper;

    }
}
