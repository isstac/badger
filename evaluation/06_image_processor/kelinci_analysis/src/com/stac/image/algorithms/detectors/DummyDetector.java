package com.stac.image.algorithms.detectors;

import com.stac.image.algorithms.*;
import java.awt.image.*;

//import gov.nasa.jpf.symbc.Debug;

public class DummyDetector extends Detector
{
	static int INDEX = 0;
	
    @Override
    public float detect(final BufferedImage image) {
        return 0.5f;//(float) Debug.makeSymbolicReal("feat"+INDEX++);
    }
}
