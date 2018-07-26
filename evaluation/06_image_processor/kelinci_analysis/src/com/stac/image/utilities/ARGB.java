package com.stac.image.utilities;

public class ARGB
{
    public static float getA(final int argb) {
        return (argb >> 24 & 0xFF) / 255.0f;
//    	return (float) Debug.makeSymbolicReal(Debug.getSymbolicIntegerValue(argb)+"f");
    }
    
    public static int rawA(final int argb) {
        return argb >> 24 & 0xFF;
//    	return argb;
    }
    
    public static float getR(final int argb) {
        return (argb >> 16 & 0xFF) / 255.0f;
//    	return (float) Debug.makeSymbolicReal(Debug.getSymbolicIntegerValue(argb)+"f");
    }
    
    public static int rawR(final int argb) {
        return argb >> 16 & 0xFF;
//    	return argb;
    }
    
    public static float getG(final int argb) {
        return (argb >> 8 & 0xFF) / 255.0f;
//    	return (float) Debug.makeSymbolicReal(Debug.getSymbolicIntegerValue(argb)+"f");
    }
    
    public static int rawG(final int argb) {
        return argb >> 8 & 0xFF;
//    	return argb;
    }
    
    public static float getB(final int argb) {
        return (argb & 0xFF) / 255.0f;
//    	return (float) Debug.makeSymbolicReal(Debug.getSymbolicIntegerValue(argb)+"f");
    }
    
    public static int rawB(final int argb) {
        return argb & 0xFF;
//    	return argb;
    }
    
    public static int toARGB(final float a, final float r, final float g, final float b) {
        final int A = (int)a * 255 << 24;
        final int R = (int)r * 255 << 16;
        final int G = (int)g * 255 << 8;
        final int B = (int)b * 255;
        return A + R + G + B;
    }
    
    public static int toARGB(final int a, final int r, final int g, final int b) {
        final int A = a << 24;
        final int R = r << 16;
        final int G = g << 8;
        return A + R + G + b;
    }
    
    public static float[] toHSVA(final int argb) {
        final float[] hsva = new float[4];
        final float b = getB(argb);
        final float g = getG(argb);
        final float r = getR(argb);
        hsva[3] = getA(argb);
        hsva[2] = Math.max(r, Math.max(g, b));
        final float Cmin = Math.min(r, Math.min(g, b));
        final float Cdelt = hsva[2] - Cmin;
//        hsva[1] = (float) Debug.makeSymbolicReal("hsva1-"+Debug.getSymbolicIntegerValue(argb));//((hsva[2] == 0.0f) ? 0.0f : (Cdelt / hsva[2])); //yield div by 0 with no_solver option...
        hsva[1] = ((hsva[2] == 0.0f) ? 0.0f : (Cdelt / hsva[2]));
        hsva[0] = 60.0f;
        if (Cdelt == 0.0f) {
            hsva[0] = 0.0f;
        }
        else if (hsva[2] == r) {
            final float[] array = hsva;
            final int n = 0;
            array[n] *= (g - b) / Cdelt % 6.0f;
        }
        else if (hsva[2] == g) {
            final float[] array2 = hsva;
            final int n2 = 0;
            array2[n2] *= (b - r) / Cdelt + 2.0f;
        }
        else {
            final float[] array3 = hsva;
            final int n3 = 0;
            array3[n3] *= (r - g) / Cdelt + 4.0f;
        } 
        return hsva;
    }
}
