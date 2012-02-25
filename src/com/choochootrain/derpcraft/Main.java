package com.choochootrain.derpcraft;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import java.util.StringTokenizer;

public class Main extends SimpleApplication implements ActionListener {

    private Factory factory;
    private Inventory inventory;
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    private CharacterControl player;
    private Vector3f walkDirection = new Vector3f();
    private Node shootables;
    private CompoundCollisionShape blocks;
    private boolean left = false, right = false, up = false, down = false;
    private ParticleEmitter debris;
    private Node debrisNode;
    private Node inventoryNode;
    
    public static float UNIT_EXTENT = 5.0f;
    
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        bulletAppState.getPhysicsSpace().setAccuracy(0.01f);
        
        viewPort.setBackgroundColor(new ColorRGBA(0.4f, 0.8f, 1f, 1f));
        flyCam.setMoveSpeed(100);
        initKeys();
        initDebris();
        initCrosshairs();
        renderManager.preloadScene(rootNode);
        
        factory = new Factory(assetManager);
        
        inventoryNode = new Node("inventory");
        inventory = new Inventory(inventoryNode, factory, 
                guiFont, settings.getWidth(), settings.getHeight());
        guiNode.attachChild(inventoryNode);
        
        blocks = new CompoundCollisionShape();
        
        Vector3f floorVector = new Vector3f(0, -6f, 0);
        Geometry floor = factory.buildSimpleCube("floor", floorVector, 50f, 0.5f, 50f, ColorRGBA.Black);
        rootNode.attachChild(floor);
        BoxCollisionShape floorShape = new BoxCollisionShape(new Vector3f(50f, 0.5f, 50f));
        blocks.addChildShape(floorShape, floorVector);
        
        BoxCollisionShape s = new BoxCollisionShape(new Vector3f(5f, 0.5f, 5f));
        blocks.addChildShape(s, new Vector3f(0,-6,0));
        
        shootables = new Node("shootables");
        
        for(int i = -15; i < 16; i++) {
            for(int k = -15; k < 16; k++) {
                for(int j = 4; j < 6; j++) {
                    if(Math.random() > 0) {
                        Vector3f location = new Vector3f(i*10,j*10,k*10);
                        int type = (int)(Math.random() * 7);
                        ColorRGBA c = Block.getColor(type);
                        Geometry geom = factory.buildSimpleCube("Box"+i+" "+j+" "+k, location,
                                UNIT_EXTENT, UNIT_EXTENT, UNIT_EXTENT, c);
                        shootables.attachChild(geom);
                        geom.setUserData("color", c);
                        geom.setUserData("block type", type);
                        
                        if(Block.isTransparent(type))
                            geom.setQueueBucket(Bucket.Transparent);
                        
                        if(j >= 5) {
                            BoxCollisionShape collisionShape = new BoxCollisionShape(new Vector3f(UNIT_EXTENT,UNIT_EXTENT,UNIT_EXTENT));
                            blocks.addChildShape(collisionShape, location);
                        }
                    }
                }
            }
        }       

        landscape = new RigidBodyControl(blocks, 0);
        
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(4f, 7f, 1);
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setJumpSpeed(75);
        player.setFallSpeed(40);
        player.setGravity(10);
        player.setPhysicsLocation(new Vector3f(0, 75, 0));
        
        rootNode.attachChild(shootables);
        rootNode.attachChild(debrisNode);
        bulletAppState.getPhysicsSpace().add(landscape);
        bulletAppState.getPhysicsSpace().add(player);
    }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f camDir = cam.getDirection().clone();
        Vector3f camLeft = cam.getLeft().clone();
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
            // 5. Use the results (we mark the hit object)
            if (results.size() > 0) {
                CollisionResult closest = results.getClosestCollision();
                Geometry g = closest.getGeometry();
                shootables.detachChild(g);
                debris.killAllParticles();
                debris.setStartColor((ColorRGBA)g.getUserData("color"));
                debris.setEndColor((ColorRGBA)g.getUserData("color"));
                float[] coords = getCoordinates(g.getName().substring(3));
                debris.setLocalTranslation(coords[0]*10, coords[1]*10, coords[2]*10);
                debris.emitAllParticles();
                
                inventory.addBlock(g);
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
    
    private void initDebris() {
        debrisNode = new Node("debris node");
        debris = new ParticleEmitter("Debris", Type.Triangle, 15);
        debris.setSelectRandomImage(true);
        debris.setRandomAngle(true);
        debris.setRotateSpeed(FastMath.TWO_PI / 4);
        debris.setStartColor(new ColorRGBA(1f, 0.59f, 0.28f, 1));
        debris.setEndColor(new ColorRGBA(0.7f, 0.7f, 0.7f, 1.0f));
        debris.setStartSize(4f);
        debris.setEndSize(3f);

        debris.setShape(new EmitterSphereShape(Vector3f.ZERO, 1.5f));
        debris.setParticlesPerSec(0);
        debris.setGravity(0, 5f, 0);
        debris.setLowLife(0.6f);
        debris.setHighLife(0.8f);
        debris.setImagesX(3);
        debris.setImagesY(3);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/Debris.png"));
        debris.setMaterial(mat);
        debrisNode.attachChild(debris);
    }
    
    protected void initCrosshairs() {
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation( // center
                settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
                settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
    }
        
    private float[] getCoordinates(String name) {
        StringTokenizer st = new StringTokenizer(name);
        float coords[] = new float[3];
        try {
            coords[0] = (float)Integer.parseInt(st.nextToken());
            coords[1] = (float)Integer.parseInt(st.nextToken());
            coords[2] = (float)Integer.parseInt(st.nextToken());
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return coords;
    }
    
    public BitmapFont getGuiFont() {
        return guiFont;
    }
}
