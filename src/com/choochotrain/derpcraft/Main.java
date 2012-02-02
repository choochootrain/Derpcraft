package com.choochotrain.derpcraft;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

public class Main extends SimpleApplication {

    private Factory factory;
    public static float UNIT_EXTENT = 0.5f;
    
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        factory = new Factory(assetManager);
        
        for(int i = -5; i < 6; i++) {
            for(int j = -5; j < 6; j++) {
                for(int k = -5; k < 6; k++) {
                    if(Math.random() > 0.8) {
                        Geometry geom = factory.buildSimpleCube("Box", new Vector3f(i,j,k), UNIT_EXTENT, UNIT_EXTENT, UNIT_EXTENT, ColorRGBA.randomColor());
                        rootNode.attachChild(geom);
                    }
                }
            }
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
