package com.stac.image.algorithms.generics;

import com.stac.image.utilities.*;
import java.awt.image.*;

public class CannyEdgeDetect
{
    private static final Kernel SobelV;
    private static final Kernel SobelH;
    
    private static BufferedImage getSobelH(final BufferedImage image) {
        return Convolve.convolve(image, CannyEdgeDetect.SobelH);
    }
    
    private static BufferedImage getSobelV(final BufferedImage image) {
        return Convolve.convolve(image, CannyEdgeDetect.SobelV);
    }
    
    private static BufferedImage getAngles(final BufferedImage Gx, final BufferedImage Gy) {
        final BufferedImage angles = new BufferedImage(Gx.getWidth(), Gx.getHeight(), 10);
        for (int i = 0; i < Gx.getWidth(); ++i) {
            for (int j = 0; j < Gx.getHeight(); ++j) {
                int gx = ARGB.rawB(Gx.getRGB(i, j));
                int gy = ARGB.rawB(Gy.getRGB(i, j));
                int tanpi8gx = 27146;
                int tan3pi8gx = 158218;
                if (gx != 0) {
                    if (gx < 0) {
                        gx = -gx;
                        gy = -gy;
                    }
                    gy <<= 16;
                    tanpi8gx *= gx;
                    tan3pi8gx *= gx;
                    if (gy > -tan3pi8gx && gy < -tanpi8gx) {
                        setExpandedValue(angles, i, j, Direction.UP45.ordinal());
                        continue;
                    }
                    if (gy > -tanpi8gx && gy < tanpi8gx) {
                        setExpandedValue(angles, i, j, Direction.HORIZONTAL.ordinal());
                        continue;
                    }
                    if (gy > tanpi8gx && gy < tan3pi8gx) {
                        setExpandedValue(angles, i, j, Direction.DOWN45.ordinal());
                        continue;
                    }
                }
                setExpandedValue(angles, i, j, Direction.VERTICAL.ordinal());
            }
        }
        return angles;
    }
    
    private static void setExpandedValue(final BufferedImage image, final int i, final int j, final int value) {
        image.setRGB(i, j, ARGB.toARGB(255, value, value, value));
    }
    
    private static BufferedImage getGradient(final BufferedImage Gx, final BufferedImage Gy) {
        final BufferedImage gradient = new BufferedImage(Gx.getWidth(), Gx.getHeight(), 10);
        for (int i = 0; i < Gx.getWidth(); ++i) {
            for (int j = 0; j < Gx.getHeight(); ++j) {
                final int gx = ARGB.rawB(Gx.getRGB(i, j));
                final int gy = ARGB.rawB(Gy.getRGB(i, j));
                setExpandedValue(gradient, i, j, (int)Math.sqrt(gy * gy + gx * gx));
            }
        }
        return gradient;
    }
    
    private static BufferedImage nonMaxSupression(final BufferedImage angles, final BufferedImage gradient) {
        final BufferedImage nms = new BufferedImage(angles.getWidth(), angles.getHeight(), angles.getType());
        nms.setData(gradient.copyData(null));
//    	BufferedImage nms = angles.getCopy();
        for (int i = 0; i < angles.getWidth(); ++i) {
            for (int j = 0; j < angles.getHeight(); ++j) {
                final int[] magnitudes = getMags(i, j, ARGB.rawB(angles.getRGB(i, j))/*Direction.getDirection(ARGB.rawB(angles.getRGB(i, j)))*/, gradient);
                if (Math.max(magnitudes[1], Math.max(magnitudes[0], magnitudes[2])) != magnitudes[1]) {
                    setExpandedValue(nms, i, j, 0);
                }
            }
        }
        return nms;
    }
    
    private static BufferedImage hysteresisThresholding(final BufferedImage input, final int min, final int max) {
        final BufferedImage nms = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());
        nms.setData(input.copyData(null));
//    	BufferedImage nms = input.getCopy();
        for (int i = 0; i < nms.getWidth(); ++i) {
            for (int j = 0; j < nms.getHeight(); ++j) {
                final int m11 = ARGB.rawB(nms.getRGB(bound(0, nms.getWidth(), i), bound(0, nms.getHeight(), j)));
                if (m11 >= max) {
                    hysterize(min, nms, i - 1, j - 1);
                    hysterize(min, nms, i, j - 1);
                    hysterize(min, nms, i + 1, j - 1);
                    hysterize(min, nms, i - 1, j);
                    hysterize(min, nms, i, j);
                    hysterize(min, nms, i + 1, j);
                    hysterize(min, nms, i - 1, j + 1);
                    hysterize(min, nms, i, j + 1);
                    hysterize(min, nms, i + 1, j + 1);
                }
                else if (m11 < min) {
                    setExpandedValue(nms, i, j, 0);
                }
            }
        }
        return nms;
    }
    
    private static void hysterize(final int min, final BufferedImage nms, int i, int j) {
        i = bound(0, nms.getWidth(), i);
        j = bound(0, nms.getHeight(), j);
        if (ARGB.rawB(nms.getRGB(i, j)) > min) {
            setExpandedValue(nms, i, j, 255);
        }
    }
    
    private static int[] getMags(final int i, final int j, /*final Direction angle*/int angle, final BufferedImage grad) {
        int bx = i;
        int by = j;
        int ax = i;
        int ay = j;
        switch (angle) {
            case 1/*DOWN45*/: {
                --bx;
                --by;
                ++ax;
                ++ay;
                break;
            }
            case 2/*UP45*/: {
                --bx;
                ++by;
                ++ax;
                --ay;
                break;
            }
            case 3/*VERTICAL*/: {
                --by;
                ++ay;
                break;
            }
            case 4/*HORIZONTAL*/: {
                --bx;
                ++ax;
                break;
            }
        }
        bx = bound(0, grad.getWidth(), bx);
        by = bound(0, grad.getHeight(), by);
        ax = bound(0, grad.getWidth(), ax);
        ay = bound(0, grad.getHeight(), ay);
        return new int[] { ARGB.rawB(grad.getRGB(bx, by)), ARGB.rawB(grad.getRGB(i, j)), ARGB.rawB(grad.getRGB(ax, ay)) };
    }
    
    private static int bound(final int min, final int max, int val) {
        if (val < min) {
            ++val;
        }
        if (val >= max) {
            --val;
        }
        return val;
    }
    
    public static BufferedImage detect(final BufferedImage image, final int min, final int thresh) {
        final BufferedImage blurred = Convolve.convolve(image, Convolve.Gausian5x5);
        final BufferedImage grey = ConvertImage.otherGray(blurred);
        final BufferedImage sobelH = getSobelH(grey);
        final BufferedImage sobelV = getSobelV(grey);
        final BufferedImage angle = getAngles(sobelH, sobelV);
        final BufferedImage grad = getGradient(sobelH, sobelV);
        final BufferedImage nms = nonMaxSupression(angle, grad);
        final BufferedImage output = hysteresisThresholding(nms, min, thresh);
        return output;
    }
    
    static {
        SobelV = new Kernel(3, 3, new float[] { 1.0f, 2.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, -2.0f, -1.0f });
        SobelH = new Kernel(3, 3, new float[] { 1.0f, 0.0f, -1.0f, 2.0f, 0.0f, -2.0f, 1.0f, 0.0f, -1.0f });
    }
}
