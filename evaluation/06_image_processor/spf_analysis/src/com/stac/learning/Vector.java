package com.stac.learning;

import java.util.*;
import java.io.*;

public abstract class Vector
{
    final float[] attributes;
    
    Vector(final int n) {
        this.attributes = new float[n];
    }
    
    public final int size() {
        return this.attributes.length;
    }
    
    final float[] getAttributes() {
        return this.attributes;
    }
    
    public abstract float compareTo(final Vector p0);
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Vector [");
        int i;
        for (i = 0; i < this.attributes.length - 1; ++i) {
            sb.append(this.attributes[i]).append(", ");
        }
        sb.append(this.attributes[i]);
        sb.append("]");
        return sb.toString();
    }
    
    public static class VectorBuilder
    {
        private int trackedIndex;
        private final VectorFactory ctor;
        private Vector vector;
        
        public VectorBuilder(final VectorFactory vectorFactory, final int size) {
            this.trackedIndex = 0;
            this.ctor = Objects.requireNonNull(vectorFactory, "Vector Supplier must not be null");
            this.vector = this.ctor.get(size);
        }
        
        public VectorBuilder add(final float attr) {
            if (attr >= 0.0f && attr <= 1.0f) {
                this.vector.attributes[this.trackedIndex++] = attr;
                return this;
            }
            throw new IllegalArgumentException("Attribute 'attr': must be in the range (0,1)");
        }
        
        public Vector build() throws InvalidObjectException {
            if (this.vector == null) {
                throw new InvalidObjectException("This vector has been built.");
            }
            final Vector v = this.vector;
            this.vector = null;
            return v;
        }
    }
}
