package edu.cornell.lilbiggames.cephalonaut.engine.controller;

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
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.ImageObject;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements.*;
import edu.cornell.lilbiggames.cephalonaut.engine.model.CephalonautModel;
import edu.cornell.lilbiggames.cephalonaut.engine.model.GrappleModel;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.*;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;
import edu.cornell.lilbiggames.cephalonaut.engine.parsing.LevelLoader;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/** Game mode for playing a level */
public class PlayMode extends WorldController implements Screen {
    /** For knowing we have exited a level */
    public static int EXIT_LEVEL = 20;
    public static int WON_LEVEL = 100;
    /** Player model */
    private CephalonautModel cephalonaut;
    private TextureRegion octopusTexture;
    private Texture octopusInkStrip, octopusStrip, clock;

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
    private static int prev_music;

    private int twoStars, threeStars;

    public static int NUM_SPARKLES = 12;
    private FilmStrip [] sparkles;
    private int [][] sparkleX;
    private int [][] sparkleY;

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

        clock = this.loader.getAssetDirectory().getEntry("ui:clock", Texture.class);
        displayFont = this.loader.getAssetDirectory().getEntry("retro", BitmapFont.class);
        directionalGrapple = true;
        deathRotationCount = 0;
        fadeInCount = 1;
        won = false;
        timeCount = 0;
        timer = 0;
        paused = false;
        dialogueFade = 0;

        sparkles = new FilmStrip[NUM_SPARKLES];
        for (int i = 0; i < NUM_SPARKLES; i++) {
            if (i < 2 * (NUM_SPARKLES / 3)) {
                sparkles[i] = new FilmStrip(this.loader.getAssetDirectory().getEntry("bg:Mstar", Texture.class), 1, 11);
            } else {
                sparkles[i] = new FilmStrip(this.loader.getAssetDirectory().getEntry("bg:Sstar", Texture.class), 1, 11);
            }
        }
        sparkleX = new int[NUM_SPARKLES][NUM_SPARKLES];
        sparkleY = new int[NUM_SPARKLES][NUM_SPARKLES];
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

    public int getTwoStars() {
        return twoStars;
    }

    public int getThreeStars() {
        return threeStars;
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

        bounds.set(0, 0, levelDef.width, levelDef.height);

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
        cephalonaut.setHasMoved(false);
        fadeInCount = 1;
        timeCount = 0;
        timer = 0;
        dialogueMode.load(level, checkpoint);
        paused = false;
        twoStars = levelDef.twoStars;
        threeStars = levelDef.threeStars;

        for (int i = 0; i < NUM_SPARKLES; i++) {
            for (int j = 0; j < NUM_SPARKLES; j++) {
                sparkleX[i][j] = ThreadLocalRandom.current().nextInt(0, ((int) (bounds.getWidth() + 1)));
            }
        }

        int minY = 0;
        if (level.equals("level_3") && checkpoint.equals("checkpoint_0")) {
            minY = -((int) (bounds.getHeight() + 1));
        }
        for (int i = 0; i < NUM_SPARKLES; i++) {
            for (int j = 0; j < NUM_SPARKLES; j++) {
                sparkleY[i][j] = ThreadLocalRandom.current().nextInt(minY, ((int) (bounds.getHeight() + 1)));
            }
        }
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
//        float dwidth  = octopusTexture.getRegionWidth()/scale.x*1.2f;
//        float dheight = octopusTexture.getRegionHeight()/scale.y*1.6f;
        float dwidth = octopusTexture.getRegionWidth() * .02625f;
        float dheight = octopusTexture.getRegionHeight() * .035f;
        //FilmStrip cephInkFilm = new FilmStrip(octopusInkStrip,1,7);
        FilmStrip cephFilm = new FilmStrip(octopusStrip,5,9);
        cephalonaut = new CephalonautModel(startX, startY, dwidth, dheight, startInk, scale, cephFilm);
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

    public int getTimer() {
        return timer;
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

    public static void resetMusic() {
        prev_music = -1;
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
        if (isDialogueMode(dt)) return;

        int sparklesIdx = ThreadLocalRandom.current().nextInt(0, NUM_SPARKLES);
        FilmStrip sparkle = sparkles[sparklesIdx];
        sparkle.setFrame((sparkle.getFrame() + 1) % sparkle.getSize());

        if (cephalonaut.getHasMoved()) {
            timeCount += dt;
            if (timeCount >= 1) {
                timer += 1;
                timeCount = 0;
            }
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
        levelController.resetBlackHoleRange();
        for (GameObject object : objects) {
            levelController.update(object, cephalonautController);
        }
        if(!levelController.blackHoleSound())
            SoundController.setBlackHoleSound(false,0);

        boolean grappleButton = input.didSecondary();
        boolean ungrappleButton = input.didTertiary();
        if (level.equals("level_0") && checkpoint.equals("checkpoint_0")) {
            grappleButton = false;
            ungrappleButton = false;
        }
        boolean inking = input.isThrusterApplied();
        float rotation = input.getRotation();

        Vector2 crossHair = input.getCrossHair().add(
                (canvas.getCameraX() - canvas.getWidth() / 2f) / scale.x,
                (canvas.getCameraY() - canvas.getHeight() / 2f) / scale.y);

        if(input.xbox != null && input.xbox.isConnected() &&
                (Math.abs(input.getStickDirec().x) > .6f || Math.abs(input.getStickDirec().y) > .6f)) {
            crossHair.x = 100 * input.getStickDirec().x + cephalonaut.getPosition().x;
            crossHair.y = 100 * input.getStickDirec().y + cephalonaut.getPosition().y;
        }

        cephalonautController.update(grappleButton, ungrappleButton, crossHair, inking, rotation);

        Vector2 center = cephalonaut.getBody().getWorldCenter();
        canvas.setCameraPos(bounds, scale,
                MathUtils.roundPositive(center.x * scale.x),
                MathUtils.roundPositive(center.y * scale.y));

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

        // kill michael when out of bounds
        if(cephalonaut.getX() < -cephalonaut.getHeight() ||  cephalonaut.getX() >= bounds.getWidth() + cephalonaut.getHeight()
                || cephalonaut.getY() < -cephalonaut.getHeight()|| cephalonaut.getY() >= bounds.getHeight() + cephalonaut.getHeight()) {
            cephalonaut.setAlive(false);
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
//            if(obj instanceof  LevelElement && ((LevelElement) obj).getElement() == LevelElement.Element.FINISH) {
//                canvas.drawLevelEndGlow(obj.getX() * scale.x, obj.getY() * scale.y);
//            }
            obj.draw(canvas);
            if (obj instanceof LEBlackHole) {
                canvas.drawBlackHoleOutline(obj.getX() * scale.x, obj.getY() * scale.y,
                        ((LEBlackHole) obj).getBlackHoleRange() * scale.x);
            }
            if (obj instanceof ImageObject) {
                Vector2 parallaxFactor = ((ImageObject) obj).getParallaxFactor();
                float offsetX = canvas.getCameraX() * parallaxFactor.x;
                float offsetY = canvas.getCameraY() * parallaxFactor.y;
                for (int i = 0; i < NUM_SPARKLES; i++) {
                    for (int j = 0; j < NUM_SPARKLES; j++) {
                        canvas.draw(sparkles[i], Color.GRAY,
                                sparkles[i].getFwidth() / 2f, sparkles[i].getFheight() / 2f,
                                scale.x * sparkleX[i][j] + offsetX, scale.y * sparkleY[i][j] + offsetY,
                                0, 0.1f * scale.x, 0.1f * scale.y);
                    }
                }
            }
        }

        selector.draw(canvas);
        cephalonaut.draw(canvas);

        float x = canvas.getWidth() * 0.40f + canvas.getCameraX() + 56;
        float y = canvas.getHeight() * 0.47f + canvas.getCameraY() - 12;
        canvas.drawFg(clock, Color.WHITE, clock.getWidth() / 2f, clock.getHeight() / 2f, x, y, 0, 0.9f, 0.9f);

        int minutes = (timer % 3600) / 60;
        int seconds = timer % 60;
        timeString = String.format("%02d:%02d", minutes, seconds);
        displayFont.getData().setScale(0.5f);
        displayFont.setColor(Color.ORANGE);
        canvas.drawTextTopRight(timeString, displayFont);
        displayFont.setColor(Color.WHITE);
        canvas.drawFade(fadeInCount);
      
        if (!cephalonaut.isAlive()) {
            canvas.drawFade(deathRotationCount / (float) (4 * Math.PI));
        }

        if (paused) {
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
