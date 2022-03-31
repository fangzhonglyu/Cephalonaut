package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.GameObject;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;
import edu.cornell.lilbiggames.cephalonaut.engine.model.CephalonautModel;
import edu.cornell.lilbiggames.cephalonaut.engine.model.GrappleModel;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.*;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;

import java.util.Map;

/** Game mode for playing a level */
public class PlayMode extends WorldController implements Screen {
    // Matias: We might want to think of making this not extend WorldController, or editing/making our own

    // for knowing we have exited a level
    public static int EXIT_LEVEL = 20;
    /** Player model */
    private CephalonautModel cephalonaut;
    private TextureRegion octopusTexture;

    /** Controller that handles cephalonaut movement (grappling and inking) */
    private CephalonautController cephalonautController;

    /** Sound controller */
    private SoundController soundController;

    /** Controller which handles levels and level elements **/
    private LevelController levelController;

    /** The grapple mechanic mode TODO: Shouldn't this be in CephalonautController? */
    private boolean directionalGrapple;

    /** Mouse selector to move the cephalonaut TODO: Can this be in CephalonautController too? */
    private ObstacleSelector selector;

    private Map<Integer, LevelElement> objectMap;

    /** Listener that will update the screen when we are done */
    private ScreenListener listener;

    boolean exiting = false;

    private int level;

    /** Default starting position for the cephalonaut */
    static private final float DEFAULT_STARTING_POS_X = 10.0f;
    static private final float DEFAULT_STARTING_POS_Y = 10.0f;

    // Matias: What's this variable for?
//    private Vector2 canvasO;
    // Matias: We shouldn't do this bc objects have state which change from loading to restarting
    // TODO: Change
    private Queue<GameObject> defaultObjects;

    /**
     * Creates and initialize a new instance of the sandbox
     */
    public PlayMode(ScreenListener listener, int level) {
        super(DEFAULT_WIDTH, DEFAULT_HEIGHT, 0);
        this.listener = listener;
        this.level = level;
        setDebug(false);
        setComplete(false);
        setFailure(false);
        directionalGrapple = true;
//        canvasO = new Vector2(1920,1080);
    }

    public void setObjectMap(Map<Integer, LevelElement> objectMap) {
        this.objectMap = objectMap;
    }

    public LevelElement getObject(int id) {
        return objectMap.get(id);
    }

    // TODO: Fix resetting, make this less jank
    public void reset() {
        reset(defaultObjects);
    }

    public void resume(){
        exiting = false;
    }


    public void cleanupLevel(){
        for(GameObject obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        addQueue.clear();
        world.dispose();
    }

    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the world and creates a new one.
     */
    public void reset(Queue<GameObject> newObjects) {
        defaultObjects = newObjects;
        Vector2 gravity = new Vector2(world.getGravity());
        cleanupLevel();

        world = new World(gravity,false);
        setComplete(false);
        setFailure(false);
        populateLevel(newObjects);

        levelController = new LevelController(listener, cephalonaut, this);
        world.setContactListener(levelController);
        GrappleModel grapple = cephalonaut.getGrapple();
        grapple.reset();
        SoundController.switchTrack(level);
    }

    private void populateLevel(Queue<GameObject> newObjects) {
        float startX = DEFAULT_STARTING_POS_X;
        float startY = DEFAULT_STARTING_POS_Y;
        for (GameObject object : newObjects) {
            if(object.getName() != null && object.getName().equals("start")) {
                startX = object.getX();
                startY = object.getY();
                continue;
            }
            object.setDrawScale(scale);
            addObject(object);
        }


        // Make the cephalonaut
        float dwidth  = octopusTexture.getRegionWidth()/scale.x;
        float dheight = octopusTexture.getRegionHeight()/scale.y;
        cephalonaut = new CephalonautModel(startX, startY, dwidth, dheight, scale);
        cephalonaut.setTexture(octopusTexture);
        cephalonautController = new CephalonautController(world, cephalonaut);

        addObject(cephalonaut);
        GrappleModel grapple = cephalonaut.getGrapple();
        grapple.setMaxLength(7.0f);
        addObject(grapple);

        selector = new ObstacleSelector(world);
        selector.setDrawScale(scale);
        world.setGravity(Vector2.Zero);
    }

    /**
     * Gather the assets for this controller.
     *
     * This method extracts the asset variables from the given asset directory. It
     * should only be called after the asset directory is completed.
     *
     * @param directory	Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory) {
        // Allocate the tiles
        earthTile = new TextureRegion(directory.getEntry( "earth", Texture.class ));
        octopusTexture = new TextureRegion(directory.getEntry( "octopus.png", Texture.class ));
//		displayFont = directory.getEntry( "shared:retro" ,BitmapFont.class);
    }

    /**
     * The core gameplay loop of this world.
     *
     * This method contains the specific update code for this mini-game. It does
     * not handle collisions, as those are managed by the parent class WorldController.
     * This method is called after input is read, but before collisions are resolved.
     * The very last thing that it should do is apply forces to the appropriate objects.
     *
     * @param dt 	Number of seconds since last animation frame
     */
    public void update(float dt) {
        // Move an object if touched
        InputController input = InputController.getInstance();

        if(input.didExit()){
            if (listener != null) {
                exiting = true;
                pause();
                listener.exitScreen(this, EXIT_LEVEL);
            } else {
                System.err.println("No listener! Did you correctly set the listener for this playmode?");
            }
        } else {
            if (input.didTertiary()) {
                directionalGrapple = !directionalGrapple;
            }
            cephalonaut.setForce(Vector2.Zero);


            for(GameObject object : objects) {
                levelController.update(object);
            }

            boolean grappleButton = input.didSecondary();
            boolean ungrappleButton = input.didTertiary();

            Vector2 crossHair = input.getCrossHair().add(
                    (canvas.getCameraX() - canvas.getWidth() / 2f) / scale.x,
                    (canvas.getCameraY() - canvas.getHeight() / 2f) / scale.y);

            boolean inking = input.isThrusterApplied();
            float rotation = input.getRotation();

            cephalonautController.update(grappleButton, ungrappleButton, crossHair, inking, rotation);
            canvas.setCameraPos(cephalonaut.getX() * scale.x, cephalonaut.getY() * scale.y);
        }


    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        for (GameObject object : objects) {
            object.setDrawScale(scale);
        }
    }

    /**
     * Draw the physics objects together with foreground and background
     *
     * This is completely overridden to support custom background and foreground art.
     * This is completely overridden to support custom background and foreground art.
     *
     * @param dt Timing values from parent loop
     */
    public void draw(float dt) {
        canvas.clear();

        if(!exiting) {
            canvas.begin();
            for (GameObject obj : objects) {
                obj.draw(canvas);
            }

            selector.draw(canvas);
            cephalonaut.draw(canvas);
            canvas.drawSimpleFuelBar(cephalonaut.getInk());
            canvas.end();

            if (isDebug()) {
                canvas.beginDebug();
                for (GameObject obj : objects) {
                    obj.drawDebug(canvas);
                }
                canvas.endDebug();
            }
        }
    }

}
