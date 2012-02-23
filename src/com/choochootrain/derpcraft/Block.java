package com.choochootrain.derpcraft;

import com.jme3.math.ColorRGBA;

public class Block {
    
    public static final int GRASS = 0;
    public static final int DIRT = 1;
    public static final int STONE = 2;
    public static final int WATER = 3;
    public static final int WOOD = 4;
    public static final int BRICK = 5;
    public static final int GLASS = 6;
    
    public static ColorRGBA getColor(int type) {
        switch(type) {
            case GRASS:
                return new ColorRGBA(0.2f, 0.8f, 0.2f, 1.0f);
            case DIRT:
                return new ColorRGBA(0.3f, 0.3f, 0f, 1.0f);
            case STONE:
                return new ColorRGBA(0.6f, 0.6f, 0.6f, 1.0f);
            case WATER:
                return new ColorRGBA(0f, 0.1f, 1.0f, 0.6f);
            case WOOD:
                return new ColorRGBA(0.1f, 0.1f, 0f, 1.0f);
            case BRICK:
                return new ColorRGBA(0.8f, 0.1f, 0.1f, 1.0f);
            case GLASS:
                return new ColorRGBA(0.8f, 0.9f, 0.8f, 0.2f);                
            default:
                return ColorRGBA.Black;
        }
    }
}
