package com.stac.learning;

public class EuclideanVectorFactory extends VectorFactory
{
    @Override
    public Vector get(final int n) {
        return new EuclideanVector(n);
    }
}
