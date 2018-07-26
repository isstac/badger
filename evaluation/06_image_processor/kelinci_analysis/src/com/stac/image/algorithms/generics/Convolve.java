package com.stac.image.algorithms.generics;

import java.awt.image.*;
import java.awt.*;

public class Convolve
{
    public static final Kernel Gausian5x5;
//    public static final Kernel Gausian3x3;
    
    public static BufferedImage convolve(final BufferedImage image, final Kernel kernel) {
    	// removed library call
    	return image;
//        return new ConvolveOp(kernel, 1, null).filter(image, null);
    }
    
    static {
        Gausian5x5 = new Kernel(5, 5, new float[] { 0.003021148f, 0.012084592f, 0.021148037f, 0.012084592f, 0.003021148f, 0.012084592f, 0.06042296f, 0.09969789f, 0.06042296f, 0.012084592f, 0.021148037f, 0.09969789f, 0.16616315f, 0.09969789f, 0.021148037f, 0.012084592f, 0.06042296f, 0.09969789f, 0.06042296f, 0.012084592f, 0.003021148f, 0.012084592f, 0.021148037f, 0.012084592f, 0.003021148f });
//        Gausian3x3 = new Kernel(3, 3, new float[] { 0.06666667f, 0.13333334f, 0.06666667f, 0.13333334f, 0.2f, 0.13333334f, 0.06666667f, 0.13333334f, 0.06666667f });
    }
}
