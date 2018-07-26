package com.stac.image.algorithms;

import com.stac.image.*;
import java.awt.image.*;

public abstract class Filter extends ImageAlgorithm
{
    @Override
    public void runAlgorithm(final BufferedImage image) {
        this.filter(image);
    }
    
    public abstract void filter(final BufferedImage p0);
}
