package com.choochootrain.derpcraft;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

public class Inventory {
    
    private int[] blocks;
    private Node inventory;
    private Geometry[] geoms;
    private BitmapText[] counts;
    private Factory factory;
    private BitmapFont guiFont;
    private int width;
    private int height;
    private int widthMargin;
    private int heightMargin;
    
    public Inventory(Node inv, Factory f, BitmapFont g, int w, int h) {
        width = w/12 * 10;
        widthMargin = w/12;
        height = h/12 * 10;
        heightMargin = h/12;
        blocks = new int[Block.NUM_BLOCKS];
        geoms = new Geometry[Block.NUM_BLOCKS];
        counts = new BitmapText[Block.NUM_BLOCKS];
        inventory = inv;
        factory = f;
        guiFont = g;
        
        Vector3f center = new Vector3f(width/2, height/12, -1);
        ColorRGBA color = ColorRGBA.Gray;
        Geometry background = factory.buildSimpleCube("Inventory Background", center,
                width/2, height/12, 1, color);
        background.setUserData("color", color); 
        
        for(int i = 0; i < geoms.length; i++) {
            Vector3f location = new Vector3f(width/Block.NUM_BLOCKS * i + widthMargin, height/6, 0);
            ColorRGBA c = Block.getColor(i);
            geoms[i] = factory.buildSimpleCube("Inventory "+i, location,
                    30, 30, 30, c);
            geoms[i].setUserData("color", c); 
        }
        
        for(int i = 0; i < counts.length; i++) {
            counts[i] = new BitmapText(guiFont, false);
            counts[i].setSize(guiFont.getCharSet().getRenderedSize() * 2);
            counts[i].setText("0");
            counts[i].setLocalTranslation(width/Block.NUM_BLOCKS * i + widthMargin + 15, height/6 + 10, 0);
        }
    }
    
    public void addBlock(Geometry block) {
        int type = block.getUserData("block type");
        
        blocks[type]++;
        counts[type].setText("" + blocks[type]);
        updateInventory();
    }
    
    public void removeBlock(Geometry block) {
        int type = block.getUserData("block type");
        blocks[type]--;
        counts[type].setText("" + blocks[type]);
        updateInventory();
    }
    
    private void updateInventory() {
        for(int i = 0; i < blocks.length; i++) {
            int b = blocks[i];
            if (b == 0) {
                inventory.detachChild(geoms[i]);
                inventory.detachChild(counts[i]);
            }
            else {
                inventory.attachChild(geoms[i]);
                inventory.attachChild(counts[i]);
            }
        }
    }
}
