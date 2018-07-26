package com.stac;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

class MapCompare implements Comparator<Map.Entry<String, Integer>>
{
    @Override
    public int compare(final Map.Entry<String, Integer> o1, final Map.Entry<String, Integer> o2) {
        return Objects.requireNonNull(o2.getValue()) - Objects.requireNonNull(o1.getValue());
    }
}