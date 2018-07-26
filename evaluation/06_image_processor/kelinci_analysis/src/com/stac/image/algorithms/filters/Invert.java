package com.stac.image.algorithms.filters;

import com.stac.image.algorithms.*;
import java.awt.image.*;

public class Invert extends Filter
{
    private boolean invertAlpha;
    
    public Invert() {
        this.invertAlpha = false;
    }
    
    public Invert(final boolean invertAlpha) {
        this.invertAlpha = false;
        this.invertAlpha = invertAlpha;
    }
    
    @Override
    public void filter(final BufferedImage image) {
        final int width = image.getWidth();
        final int height = image.getHeight();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                final int argbIn = image.getRGB(x, y);
                int alpha = argbIn >> 24 & 0xFF;
                if (this.invertAlpha) {
                    alpha = 255 - alpha;
                }
                image.setRGB(x, y, alpha << 24 | 255 - (argbIn >> 16 & 0xFF) << 16 | 255 - (argbIn >> 8 & 0xFF) << 8 | 255 - (argbIn & 0xFF));
            }
        }
    }
}
