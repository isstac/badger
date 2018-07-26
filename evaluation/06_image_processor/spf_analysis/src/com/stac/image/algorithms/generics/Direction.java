package com.stac.image.algorithms.generics;

public enum Direction
{
    UP45, 
    DOWN45, 
    HORIZONTAL, 
    VERTICAL;
    
    public static Direction getDirection(final int ord) {
        for (final Direction direction : values()) {
            if (direction.ordinal() == ord) {
                return direction;
            }
        }
        return null;
    }
}
