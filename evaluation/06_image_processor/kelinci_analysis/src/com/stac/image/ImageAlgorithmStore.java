package com.stac.image;

import java.util.*;
import com.stac.image.algorithms.detectors.*;
import com.stac.image.algorithms.filters.*;

public class ImageAlgorithmStore
{
    private static Map<String, ImageAlgorithm> store;
    
    private static void addAlgorithm(final Class<? extends ImageAlgorithm> algo) {
        try {
            ImageAlgorithmStore.store.put(algo.getSimpleName(), (ImageAlgorithm)algo.newInstance());
        }
        catch (InstantiationException | IllegalAccessException ex2) {
//            final ReflectiveOperationException ex;
//            final ReflectiveOperationException e = ex;
            throw new RuntimeException("Fatal initialization error has occurred");
        }
    }
    
    public static ImageAlgorithm getAlgorithm(final String name) {
        return ImageAlgorithmStore.store.get(name);
    }
    
    static {
        ImageAlgorithmStore.store = new HashMap<String, ImageAlgorithm>();
        addAlgorithm(BlackDetector.class);
        addAlgorithm(BlueDetector.class);
        addAlgorithm(EdgingDetector.class);
        addAlgorithm(GreenDetector.class);
        addAlgorithm(RedDetector.class);
        addAlgorithm(WhiteDetector.class);
        addAlgorithm(Intensify.class);
        addAlgorithm(Invert.class);
        addAlgorithm(DummyDetector.class);
    }
}
