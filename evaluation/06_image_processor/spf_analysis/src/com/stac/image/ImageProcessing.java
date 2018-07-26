package com.stac.image;

import java.awt.image.*;
import javax.imageio.*;
import java.util.*;
import java.io.*;
import com.stac.learning.*;
import com.stac.learning.Vector;

public class ImageProcessing
{
    private static final VectorFactory VECTOR_FACTORY;
    private final ArrayList<ImageAlgorithm> ALGORITHMS;
    private final BufferedImage image;
    
    public ImageProcessing(final File imageFile, final String processingChain) throws IOException {
        this.ALGORITHMS = new ArrayList<ImageAlgorithm>();
        this.image = ImageIO.read(imageFile);
        final String[] arr$;
        final String[] algorithms = arr$ = processingChain.split(",\\s*");
        for (final String algorithm : arr$) {
            final ImageAlgorithm ia = ImageAlgorithmStore.getAlgorithm(algorithm);
            if (ia == null) {
                throw new RuntimeException("Unknown algorithm: " + algorithm);
            }
            this.ALGORITHMS.add(ia);
        }
    }
    
    private int countDetectors() {
        int c = 0;
        for (final ImageAlgorithm algorithm : this.ALGORITHMS) {
            if (algorithm.hasValue()) {
                ++c;
            }
        }
        return c;
    }
    
    private Vector featureExtract() throws InvalidObjectException {
        final Vector.VectorBuilder vectorBuilder = new Vector.VectorBuilder(ImageProcessing.VECTOR_FACTORY, this.countDetectors());
        for (final ImageAlgorithm imageAlgorithm : this.ALGORITHMS) {
            imageAlgorithm.runAlgorithm(this.image);
            if (imageAlgorithm.hasValue()) {
                vectorBuilder.add(imageAlgorithm.getValue());
            }
        }
        return vectorBuilder.build();
    }
    
    public static Vector getAttributeVector(final File imageFile, final String processingChain) throws IOException {
        return new ImageProcessing(imageFile, processingChain).featureExtract();
    }
    
    static {
        VECTOR_FACTORY = new EuclideanVectorFactory();
    }
}
