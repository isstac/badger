package com.stac.image.algorithms;

import com.stac.image.*;
import java.awt.image.*;

public abstract class Detector extends ImageAlgorithm
{
    private float value;
    
    public Detector() {
        this.value = 0.0f;
    }
    
    @Override
    public void runAlgorithm(final BufferedImage image) {
        this.value = this.detect(image);
    }
    
    @Override
    public boolean hasValue() {
        return true;
    }
    
    @Override
    public float getValue() {
        return this.value;
    }
    
    public abstract float detect(final BufferedImage p0);
}
