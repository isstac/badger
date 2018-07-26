package com.stac.learning;

import java.util.*;

public class EuclideanVector extends com.stac.learning.Vector
{
    EuclideanVector(final int n) {
        super(n);
    }
    
    @Override
    public float compareTo(final Vector other) {
        Objects.requireNonNull(this.attributes);
        Objects.requireNonNull(other.attributes);
        if (this.attributes.length != other.attributes.length) {
            throw new IllegalArgumentException("Vector lengths do not match.");
        }
        if (!this.attributes.getClass().getComponentType().equals(other.attributes.getClass().getComponentType())) {
            throw new IllegalArgumentException("Arrays must be of the same type.");
        }
        double sum = 0.0;
        for (int i = 0; i < this.attributes.length; ++i) {
            sum += (this.attributes[i] - other.attributes[i]) * (this.attributes[i] - other.attributes[i]);
        }
        return (float)(1.0 - Math.sqrt(sum));
    }
}
