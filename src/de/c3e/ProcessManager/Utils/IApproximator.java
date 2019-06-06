package de.c3e.ProcessManager.Utils;

public interface IApproximator
{
    void AddDataPoints(double x, double y);

    double Approximate(double x);
}
