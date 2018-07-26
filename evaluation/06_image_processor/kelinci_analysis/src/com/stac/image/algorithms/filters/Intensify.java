package com.stac.image.algorithms.filters;

import com.stac.image.algorithms.*;
import java.awt.image.*;
import com.stac.image.utilities.*;
import com.stac.mathematics.*;

public class Intensify extends Filter
{
    @Override
    public void filter(final BufferedImage image) {
        for (int i = 0; i < image.getWidth(); ++i) {
            for (int j = 0; j < image.getHeight(); ++j) {
                final int rgb00 = image.getRGB(this.bound(0, image.getWidth(), i - 1), this.bound(0, image.getHeight(), j - 1));
                final int rgb2 = image.getRGB(this.bound(0, image.getWidth(), i - 1), this.bound(0, image.getHeight(), j));
                final int rgb3 = image.getRGB(this.bound(0, image.getWidth(), i - 1), this.bound(0, image.getHeight(), j + 1));
                final int rgb4 = image.getRGB(i, j);
                final int rgb5 = image.getRGB(this.bound(0, image.getWidth(), i + 1), this.bound(0, image.getHeight(), j - 1));
                final int rgb6 = image.getRGB(this.bound(0, image.getWidth(), i + 1), this.bound(0, image.getHeight(), j));
                final int rgb7 = image.getRGB(this.bound(0, image.getWidth(), i + 1), this.bound(0, image.getHeight(), j + 1));
                final int m = Mathematics.intensify(ARGB.rawA(rgb4), ARGB.rawR(rgb00), ARGB.rawG(rgb4), ARGB.rawB(rgb7));
                final int n = Mathematics.intensify(ARGB.rawA(rgb4), ARGB.rawR(rgb2), ARGB.rawG(rgb4), ARGB.rawB(rgb6));
                final int o = Mathematics.intensify(ARGB.rawA(rgb4), ARGB.rawR(rgb3), ARGB.rawG(rgb4), ARGB.rawB(rgb5));
                final long avg = m + m + m + n + n + o + o + o;
                image.setRGB(i, j, (int)avg >> 3 | 0xFF000000);
            }
        }
    }
    
    private int bound(final int min, final int max, final int i) {
        if (i < min) {
            return 0;
        }
        if (i < max) {
            return i;
        }
        return max - 1;
    }
}
