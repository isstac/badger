package com.stac.learning;

import java.util.*;

public class VectorMap extends HashMap<Vector, String>
{
    public String get(final Vector key) {
        return super.get(key);
    }
    
    public boolean containsKey(final Vector key) {
        return super.containsKey(key);
    }
    
    public String remove(final Vector key) {
        return super.remove(key);
    }
    
    public boolean containsValue(final Vector value) {
        return super.containsValue(value);
    }
}
