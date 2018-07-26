package com.stac.image.algorithms.detectors;

import com.stac.image.algorithms.*;
import java.awt.image.*;
import com.stac.image.algorithms.generics.*;

public class EdgingDetector extends Detector
{
    @Override
    public float detect(final BufferedImage image) {
        final BufferedImage cannied = CannyEdgeDetect.detect(image, 125, 220);
        return new WhiteDetector().detect(cannied);
    }
}
