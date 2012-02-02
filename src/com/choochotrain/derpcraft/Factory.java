package com.choochotrain.derpcraft;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

public class Factory {
 
  private AssetManager assetManager;
    
  public Factory(AssetManager assetManager) {
      this.assetManager = assetManager;
  }
    
  public Geometry buildCube(String name, float x, float y, float z, 
          float xExtent, float yExtent, float zExtent, ColorRGBA color) {
    Box box = new Box(new Vector3f(x, y, z), xExtent, yExtent, zExtent);
    Geometry cube = new Geometry(name, box);
    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    mat.setColor("Color", color);
    cube.setMaterial(mat);
    return cube;
  }
}
