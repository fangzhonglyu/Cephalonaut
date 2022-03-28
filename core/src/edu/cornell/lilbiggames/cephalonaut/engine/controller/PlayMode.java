package edu.cornell.lilbiggames.cephalonaut.engine.controller;

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
import edu.cornell.lilbiggames.cephalonaut.engine.parsing.LevelLoader;

import java.util.Map;
import java.util.logging.Level;

/** Game mode for playing a level */
public class PlayMode extends WorldController {
    // Matias: We might want to think of making this not extend WorldController, or editing/making our own

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

    private LevelLoader levelLoader;


    /**
     * Creates and initialize a new instance of the sandbox
     */
    public PlayMode() {
        super(DEFAULT_WIDTH, DEFAULT_HEIGHT, 0);
        setDebug(false);
        setComplete(false);
        setFailure(false);
        directionalGrapple = true;
        levelLoader = new LevelLoader();
    }

    public void setObjectMap(Map<Integer, LevelElement> objectMap) {
        this.objectMap = objectMap;
    }

    public LevelElement getObject(int id) {
        return objectMap.get(id);
    }

    // TODO: Fix resetting, make this less jank
    public void reset() {
        levelLoader.loadLevel("wormhole_test", this);
    }

    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the world and creates a new one.
     */
    public void reset(Queue<GameObject> newObjects) {
        Vector2 gravity = new Vector2(world.getGravity());


        for(GameObject obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        addQueue.clear();
        world.dispose();

        world = new World(gravity,false);
        setComplete(false);
        setFailure(false);
        populateLevel(newObjects);

        levelController = new LevelController(cephalonaut, this);
        world.setContactListener(levelController);
        GrappleModel grapple = cephalonaut.getGrapple();
        grapple.reset();
        SoundController.switchTrack(1);
    }

    private void populateLevel(Queue<GameObject> newObjects) {
        for (GameObject object : newObjects) {
            object.setDrawScale(scale);
            addObject(object);
        }

        // Make the cephalonaut
        float dwidth  = octopusTexture.getRegionWidth()/scale.x;
        float dheight = octopusTexture.getRegionHeight()/scale.y;
        cephalonaut = new CephalonautModel(10, 10, dwidth, dheight, scale);
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
        octopusTexture = new TextureRegion(directory.getEntry( "octopus", Texture.class ));
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
        if (input.didTertiary()) {
            directionalGrapple = !directionalGrapple;
        }
        cephalonaut.setForce(Vector2.Zero);


        for(GameObject object : objects) {
            levelController.update(object, cephalonautController);
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

    /**
     * Draw the physics objects together with foreground and background
     *
     * This is completely overridden to support custom background and foreground art.
     *
     * @param dt Timing values from parent loop
     */
    public void draw(float dt) {
        canvas.clear();
        canvas.begin();
        for(GameObject obj : objects) {
            obj.draw(canvas);
        }

        selector.draw(canvas);
        cephalonaut.draw(canvas);
        canvas.end();

        if (isDebug()) {
            canvas.beginDebug();
            for(GameObject obj : objects) {
                obj.drawDebug(canvas);
            }
            canvas.endDebug();
        }
    }
}
