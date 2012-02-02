package com.choochotrain.derpcraft;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import java.util.HashMap;
import jme3tools.optimize.GeometryBatchFactory;

public class Main extends SimpleApplication implements ActionListener {

    private Factory factory;
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    private CharacterControl player;
    private Vector3f walkDirection = new Vector3f();
    private Node shootables;
    private Geometry mark;
    private CompoundCollisionShape blocks;
    private boolean left = false, right = false, up = false, down = false;
    
    public static float UNIT_EXTENT = 0.5f;
    
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        //bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        
        viewPort.setBackgroundColor(new ColorRGBA(0.4f, 0.8f, 1f, 1f));
        flyCam.setMoveSpeed(100);
        initKeys();
        initMark();
        
        factory = new Factory(assetManager);
        
        blocks = new CompoundCollisionShape();
        
        Vector3f floorVector = new Vector3f(0, -6f, 0);
        Geometry floor = factory.buildSimpleCube("floor", floorVector, 5f, 0.5f, 5, ColorRGBA.Brown);
        rootNode.attachChild(floor);
        BoxCollisionShape floorShape = new BoxCollisionShape(new Vector3f(5f, 0.5f, 5));
        blocks.addChildShape(floorShape, floorVector);
        
        Node shootablesUnoptimized = new Node("Shootables");
        
        for(int i = -5; i < 6; i++) {
            for(int j = -5; j < 6; j++) {
                for(int k = -5; k < 6; k++) {
                    if(Math.random() > 0) {
                        Vector3f location = new Vector3f(i,j,k);
                        Geometry geom = factory.buildSimpleCube("Box"+i+" "+j+" "+k, location, UNIT_EXTENT, UNIT_EXTENT, UNIT_EXTENT, ColorRGBA.randomColor());
                        shootablesUnoptimized.attachChild(geom);
                        
                        BoxCollisionShape collisionShape = new BoxCollisionShape(new Vector3f(UNIT_EXTENT,UNIT_EXTENT,UNIT_EXTENT));
                        blocks.addChildShape(collisionShape, location);
                    }
                }
            }
        }
        
        landscape = new RigidBodyControl(blocks, 0);
        
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(0.4f, 1.5f, 1);
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setJumpSpeed(20);
        player.setFallSpeed(30);
        player.setGravity(10);
        player.setPhysicsLocation(new Vector3f(0, 10, 0));
        
        shootables = GeometryBatchFactory.optimize(shootablesUnoptimized, false);
        rootNode.attachChild(shootables);
        bulletAppState.getPhysicsSpace().add(landscape);
        bulletAppState.getPhysicsSpace().add(player);
    }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f camDir = cam.getDirection().clone().multLocal(0.6f);
        Vector3f camLeft = cam.getLeft().clone().multLocal(0.4f);
        walkDirection.set(0, 0, 0);
        if (left)  { walkDirection.addLocal(camLeft); }
        if (right) { walkDirection.addLocal(camLeft.negate()); }
        if (up)    { walkDirection.addLocal(camDir); }
        if (down)  { walkDirection.addLocal(camDir.negate()); }
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getPhysicsLocation());
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    
    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Left")) {
            if (value) { left = true; } else { left = false; }
        } else if (binding.equals("Right")) {
            if (value) { right = true; } else { right = false; }
        } else if (binding.equals("Up")) {
            if (value) { up = true; } else { up = false; }
        } else if (binding.equals("Down")) {
            if (value) { down = true; } else { down = false; }
        } else if (binding.equals("Jump")) {
            player.jump();
        }
        else if (binding.equals("Shoot") && !value) {
            // 1. Reset results list.
            CollisionResults results = new CollisionResults();
            // 2. Aim the ray from cam loc to cam direction.
            Ray ray = new Ray(cam.getLocation(), cam.getDirection());
            // 3. Collect intersections between Ray and Shootables in results list.
            shootables.collideWith(ray, results);
            // 4. Print the results
            System.out.println("----- Collisions? " + results.size() + "-----");
            for (int i = 0; i < results.size(); i++) {
                // For each hit, we know distance, impact point, name of geometry.
                float dist = results.getCollision(i).getDistance();
                Vector3f pt = results.getCollision(i).getContactPoint();
                String hit = results.getCollision(i).getGeometry().getName();
                System.out.println("* Collision #" + i);
                System.out.println("  You shot " + hit + " at " + pt + ", " + dist + " wu away.");
            }
            // 5. Use the results (we mark the hit object)
            if (results.size() > 0) {
                // The closest collision point is what was truly hit:
                CollisionResult closest = results.getClosestCollision();
                // Let's interact - we mark the hit with a red dot.
                Geometry g = closest.getGeometry();
                shootables.detachChild(g);
                g.scale(10);
                guiNode.attachChild(g);
                g.move(100, 100, 100);
                mark.setLocalTranslation(closest.getContactPoint());
                rootNode.attachChild(mark);
            } else {
                // No hits? Then remove the red mark.
                rootNode.detachChild(mark);
            }
        }
    }
    
    private void initKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
        inputManager.addListener(this, "Jump");
        inputManager.addListener(this, "Shoot");
    }
    
    /** A red ball that marks the last spot that was "hit" by the "shot". */
    protected void initMark() {
        Sphere sphere = new Sphere(30, 30, 0.2f);
        mark = new Geometry("BOOM!", sphere);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", ColorRGBA.Red);
        mark.setMaterial(mark_mat);
    }
 
  /** A centred plus sign to help the player aim. */
  protected void initCrossHairs() {
    guiNode.detachAllChildren();
    guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
    BitmapText ch = new BitmapText(guiFont, false);
    ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
    ch.setText("+"); // crosshairs
    ch.setLocalTranslation( // center
      settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
      settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
    guiNode.attachChild(ch);
  }
}
