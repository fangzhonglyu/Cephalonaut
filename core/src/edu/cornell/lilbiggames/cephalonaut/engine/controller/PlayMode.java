package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.GameObject;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements.*;
import edu.cornell.lilbiggames.cephalonaut.engine.model.CephalonautModel;
import edu.cornell.lilbiggames.cephalonaut.engine.model.GrappleModel;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.*;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;
import edu.cornell.lilbiggames.cephalonaut.engine.parsing.LevelLoader;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;
import java.util.Map;

/** Game mode for playing a level */
public class PlayMode extends WorldController implements Screen {
    /** For knowing we have exited a level */
    public static int EXIT_LEVEL = 20;
    public static int WON_LEVEL = 100;
    /** Player model */
    private CephalonautModel cephalonaut;
    private TextureRegion octopusTexture;
    private Texture octopusInkStrip, octopusStrip, nextIcon;

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

    /** Maps from Tiled object IDs to their corresponding Java objects */
    private Map<Integer, LevelElement> objectMap;

    /** Listener that will update the screen when we are done */
    private ScreenListener listener;

    /** Object which loads the level data */
    private LevelLoader loader;

    /** Current level name */
    private String level;
    private String checkpoint;

    private float deathRotationCount;
    private float fadeInCount;

    boolean exiting = false;

    /** Default starting position for the cephalonaut */
    static private final float DEFAULT_STARTING_POS_X = 10.0f;
    static private final float DEFAULT_STARTING_POS_Y = 10.0f;

    private String timeString;
    private float timeCount;
    private int timer;

    // Matias: We shouldn't do this bc objects have state which change from loading to restarting.
    // Honestly I wouldn't be opposed to just reloading a level from scratch every time...
    // TODO: Change
    private Queue<GameObject> defaultObjects;
    private boolean won;

    private DialogueMode dialogueMode;
    private boolean paused;
    private float dialogueFade;
    private int prev_music = -1;


    /**
     * Creates and initialize a new instance of the sandbox
     */
    public PlayMode(ScreenListener listener, LevelLoader loader, String level, String checkpoint, Map<String, Integer> keyBindings ,DialogueMode dialogueMode) {
        super(DEFAULT_WIDTH, DEFAULT_HEIGHT, 0);
        this.listener = listener;
        this.level = level;
        this.checkpoint = checkpoint;
        this.loader = loader;
        this.dialogueMode = dialogueMode;

        InputController.getInstance().setBindings(keyBindings);
        setDebug(false);
        setComplete(false);
        setFailure(false);

        displayFont = this.loader.getAssetDirectory().getEntry("retro", BitmapFont.class);
        directionalGrapple = true;
        deathRotationCount = 0;
        fadeInCount = 1;
        won = false;
        timeCount = 0;
        timer = 0;
        paused = false;
        dialogueFade = 0;
    }

    public void nextDialogue(int part) {
        dialogueMode.nextDialogue(part);
        paused = true;
        dialogueFade = 0;
    }

    public void setObjectMap(Map<Integer, LevelElement> objectMap) {
        this.objectMap = objectMap;
    }

    public LevelElement getObject(int id) {
        return objectMap.get(id);
    }

    public void setLevel(String level) {
        this.level = level;
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
    public void reset() {
        LevelLoader.LevelDef levelDef = loader.loadLevel(level, checkpoint);

        this.bounds.set(0, 0, levelDef.width, levelDef.height);

        Vector2 gravity = new Vector2(world.getGravity());
        cleanupLevel();

        world = new World(gravity,false);
        setComplete(false);
        setFailure(false);

        populateLevel(levelDef.getObjects());
        objectMap = levelDef.getIdToObject();

        levelController = new LevelController(listener, cephalonaut, this);
        world.setContactListener(levelController);
        GrappleModel grapple = cephalonaut.getGrapple();
        grapple.reset();
        // TODO: Switch track to a map property based off Tiled
        if(prev_music != levelDef.music) {
            SoundController.switchTrack(levelDef.music);
        }
        prev_music = levelDef.music;
        deathRotationCount = 0;
        cephalonaut.setDeathScale(1);
        fadeInCount = 1;
        timeCount = 0;
        timer = 0;
        dialogueMode.load(level, checkpoint);
        paused = false;
    }

    private void populateLevel(Iterable<GameObject> newObjects) {
        float startX = DEFAULT_STARTING_POS_X;
        float startY = DEFAULT_STARTING_POS_Y;
        float startInk = 1f;
        for (GameObject object : newObjects) {


            if (object instanceof LEStart){
                startX = object.getX();
                startY = object.getY();
                startInk = ((LEStart) object).getInk();
                continue;
            }
            else if (object instanceof LETrigger) {
                ((LETrigger) object).setActivated(false);
            } else if (object instanceof LETriggerable) {
                ((LETriggerable) object).setActivated(false);
            } else   if(object instanceof LEGlassBarrier) {
                ((LEGlassBarrier)object).reset();
            }
            object.setDrawScale(scale);
            addObject(object);
        }


        // Make the cephalonaut
        float dwidth  = octopusTexture.getRegionWidth()/scale.x*1.2f;
        float dheight = octopusTexture.getRegionHeight()/scale.y*1.6f;
        //FilmStrip cephInkFilm = new FilmStrip(octopusInkStrip,1,7);
        FilmStrip cephFilm = new FilmStrip(octopusStrip,5,9);
        cephalonaut = new CephalonautModel(startX, startY, dwidth, dheight,startInk, scale, cephFilm);
        cephalonautController = new CephalonautController(world, cephalonaut);

        addObject(cephalonaut);
        GrappleModel grapple = cephalonaut.getGrapple();
        grapple.setMaxLength(7.0f);
        addObject(grapple);

        selector = new ObstacleSelector(world);
        selector.setDrawScale(scale);
        world.setGravity(Vector2.Zero);
    }

    public String getTimeString() {
        return timeString;
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
        octopusInkStrip = directory.getEntry("octopusInk",Texture.class);
        octopusStrip = directory.getEntry("octopus",Texture.class);
        octopusStrip.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
//		displayFont = directory.getEntry( "retro", BitmapFont.class);
    }

    private void exitPauseMode() {
        for (GameObject obj : objects) {
            obj.setActive(true);
        }
    }

    private void enterPauseMode() {
        //canvas.setCameraPos(cephalonaut.getX() * scale.x, cephalonaut.getY() * scale.y);
        for (GameObject obj : objects) {
            obj.setActive(false);
        }
        cephalonaut.setInking(false);
    }


    private boolean isDialogueMode(float dt) {
        if(paused) {
            // don't freeze world until fade is done
            if (dialogueFade < .5f) {
                dialogueFade += .05f;
                return false;
            }
            if(fadeInCount >= .1) { return false; }

            // freeze world here
            enterPauseMode();
            paused  = dialogueMode.update(dt);

            // unfreeze world if paused is false
            if(!paused) {
                exitPauseMode();
            }
            return true;
        }
        return false;
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
        if(isDialogueMode(dt)) return;

        timeCount += dt;
        if (timeCount >= 1) {
            timer += 1;
            timeCount = 0;
        }

        if (input.didExit()) {
            if (listener != null) {
                exiting = true;
                pause();
                listener.exitScreen(this, EXIT_LEVEL);
                return;
            } else {
                System.err.println("No listener! Did you correctly set the listener for this playmode?");
            }
        }

        if (input.didTertiary()) {
            directionalGrapple = !directionalGrapple;
        }
        cephalonaut.setForce(Vector2.Zero);

        for (GameObject object : objects) {
            levelController.update(object, cephalonautController);
        }

        boolean grappleButton = input.didSecondary();
        boolean ungrappleButton = input.didTertiary();
        boolean inking = input.isThrusterApplied();
        float rotation = input.getRotation();

        Vector2 crossHair = input.getCrossHair().add(
                (canvas.getCameraX() - canvas.getWidth() / 2f) / scale.x,
                (canvas.getCameraY() - canvas.getHeight() / 2f) / scale.y);

        cephalonautController.update(grappleButton, ungrappleButton, crossHair, inking, rotation);
        canvas.setCameraPos(bounds, scale, MathUtils.roundPositive(cephalonaut.getX()* scale.x), MathUtils.roundPositive(cephalonaut.getY()* scale.y));

        if (fadeInCount > 0) {
            fadeInCount -= .05f;
        }
        if (!cephalonaut.isAlive()) {
            cephalonaut.setLinearVelocity(Vector2.Zero);
            cephalonaut.setDeathScale((float)((4 * Math.PI - deathRotationCount) / (4 * Math.PI)));
            //(float)((4 * Math.PI - deathRotationCount) / 4 * Math.PI)

            final float BLACK_HOLE_DEATH_SPINNY_CONSTANT = 5f;
            cephalonaut.getBody().applyTorque(BLACK_HOLE_DEATH_SPINNY_CONSTANT, false);
            deathRotationCount += Math.PI / 16;
            if (deathRotationCount >= 4 * Math.PI) {
                reset();
            }
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
     *
     * @param dt Timing values from parent loop
     */
    public void draw(float dt) {

        if (exiting) return;
      
        canvas.clear();
        canvas.begin();

        for (GameObject obj : objects) {
            obj.draw(canvas);
            if(obj instanceof LEBlackHole) {
                canvas.drawBlackHoleOutline(obj.getX() * scale.x, obj.getY() * scale.y,
                        ((LEBlackHole) obj).getBlackHoleRange() * scale.x);
            }
        }

        selector.draw(canvas);
        cephalonaut.draw(canvas);

        int minutes = (timer % 3600) / 60;
        int seconds = timer % 60;
        timeString = String.format("%02d:%02d", minutes, seconds);
        displayFont.getData().setScale(0.4f);
        canvas.drawTextTopLeft(timeString, displayFont);
        canvas.drawFade(fadeInCount);
      
        if (!cephalonaut.isAlive()) {
            canvas.drawFade(deathRotationCount / (float) (4 * Math.PI));
        }

        if(paused) {
          dialogueMode.draw(canvas.getCameraX(), canvas.getCameraY(), dialogueFade);
        }

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
