package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.engine.GameState;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;
import edu.cornell.lilbiggames.cephalonaut.util.Controllers;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;
import edu.cornell.lilbiggames.cephalonaut.util.XBoxController;

import java.util.List;
import java.util.Map;

public class MainMenuNestedMode extends MenuMode {

    private int checkpoints;

    /** The font for giving messages to the player */
    private BitmapFont displayFont;

    /** Listener that will move to selected level when we are done */
    private ScreenListener listener;

    /** Background texture for start-up */
    private Texture background;

    private Texture levelIcon;

    /** Reference to the game canvas */
    protected GameCanvas canvas;

    private AssetDirectory assets;

    private int curLevel;

    private boolean levelSelected;

    private TextureRegion octopusTexture;
    private Texture levelTexture;
    private Texture levelCompletedTexture_3,levelCompletedTexture_0, levelCompletedTexture_1, levelCompletedTexture_2;

    private Rectangle[] checkpointHitBoxes;
    private int completedCheckpoints;

    XBoxController xbox;
    private boolean prevRight;
    private boolean prevLeft;
    private boolean prevExit;
    private boolean prevSelect;

    private FilmStrip filmstrip;
    /** Animation Counter*/
    private float frame;
    private float maxFrame;

    private Texture[] silhouettes;

    private Map<Integer, List<TextureRegion>> winTextures;

    private GameState gamestate;

    private final boolean UNLOCKED_MODE = true;

    protected InputAdapter menuNestedInput = new InputAdapter() {
        public boolean mouseMoved (int x, int screenY) {
            float y = canvas.getHeight() - screenY;

            if (checkpointHitBoxes != null) {
                for (int i = 0; i < checkpointHitBoxes.length; i++){
                    Rectangle hitBox = checkpointHitBoxes[i];
                    if (hitBox.x <= x && hitBox.x + hitBox.width >= x && hitBox.y <= y && hitBox.y + hitBox.height >= y ){
                        if (i != completedCheckpoints)
                            SoundController.playSound(4,1);
                        completedCheckpoints = i;
                    }
                }
            }
            return true;
        }

        public boolean touchDown (int x, int screenY, int pointer, int button) {
            float y = canvas.getHeight() - screenY;
            if (checkpointHitBoxes != null){
                for (int i = 0; i < checkpointHitBoxes.length; i++){
                    Rectangle hitBox = checkpointHitBoxes[i];
                    if (hitBox.x <= x && hitBox.x + hitBox.width >= x && hitBox.y <= y && hitBox.y + hitBox.height >= y ){
                        completedCheckpoints = i;
                        SoundController.playSound(6,1);
                        if(levelIsUnlocked(i)) {
                            levelSelected = true;
                        }
                    }
                }
            }

            if(settingsIconHitbox != null){
                if(settingsIconHitbox.x <= x && settingsIconHitbox.x + settingsIconHitbox.width >= x && settingsIconHitbox.y >= y && settingsIconHitbox.y - settingsIconHitbox.height <= y ){
                    goToSettings = true;
                    SoundController.playSound(6,1);
                }
            }

            if(backIconHitbox != null){
                if(backIconHitbox.x <= x && backIconHitbox.x + backIconHitbox.width >= x && backIconHitbox.y >= y && backIconHitbox.y - backIconHitbox.height <= y ){
                    goBack = true;
                    SoundController.playSound(6,1);
                    exitScreen();
                }
            }

            return true;
        }
    };

    public void exitScreen(){
        if(goBack){
            listener.exitScreen(this, MenuMode.GO_BACK_CODE);
        }
    }

    /**
     * Creates a MainMenuMode with the default size and position.
     *
     * @param assets    The asset directory to use
     * @param canvas 	The game canvas to draw to
     */
    public MainMenuNestedMode(AssetDirectory assets, GameCanvas canvas, int checkpoints, int completedCheckpoints, int curLevel, ScreenListener listener, Map<Integer, List<TextureRegion>> winTextures, GameState state){
        super(assets, canvas, listener);
        this.canvas  = canvas;
        this.listener = listener;
        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();
        displayFont = assets.getEntry("retro", BitmapFont.class);
        this.completedCheckpoints = completedCheckpoints;
        this.checkpoints = checkpoints;
        this.winTextures = winTextures;

        this.curLevel = curLevel;
        this.assets = assets;

        setBackground();
        background.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        octopusTexture = new TextureRegion(assets.getEntry( "octopus.png", Texture.class ));
        levelTexture = assets.getEntry( "level-incomplete", Texture.class );
        levelTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        levelCompletedTexture_0 = assets.getEntry( "level-incomplete", Texture.class );
        levelCompletedTexture_0.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        levelCompletedTexture_1 = assets.getEntry( "level-complete-1-star", Texture.class );
        levelCompletedTexture_1.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        levelCompletedTexture_2 = assets.getEntry( "level-complete-2-star", Texture.class );
        levelCompletedTexture_2.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        levelCompletedTexture_3 = assets.getEntry( "level-complete-3-star", Texture.class );
        levelCompletedTexture_3.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        filmstrip = new FilmStrip(assets.getEntry("octopus",Texture.class), 5, 9);
        frame = 0;
        maxFrame = 4;

        gamestate = state;

        populateIcons();
    }

    private void populateIcons(){
        Texture sil = assets.getEntry( "alex-sil", Texture.class );
        silhouettes = new Texture[checkpoints];

        for (int i = 0; i < checkpoints; i++) {
            silhouettes[i] = sil;
        }
        Array<XBoxController> controllers = Controllers.get().getXBoxControllers();
        if (controllers.size > 0) {
            xbox = controllers.get( 0 );
        } else {
            xbox = null;
        }
    }

    private void printStarForLevel(int checkpoint) {
        System.out.println(curLevel + ":" + (checkpoint) + ":" +getStarForLevel(checkpoint));
    }

    private int getStarForLevel(int checkpoint) {
        if(checkpoint < 0 || checkpoint >= gamestate.stars[0].length) {
            return -1;
        }
        return gamestate.stars[curLevel][checkpoint];
    }


    private boolean levelIsUnlocked(int checkpoint) {
        return UNLOCKED_MODE || checkpoint == 0 || getStarForLevel(checkpoint-1) > 0;
    }

    public void setBackground() {
        background = assets.getEntry( "BG-" + (curLevel + 1), Texture.class);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        if (levelSelected && listener != null) {
            levelSelected = false;
            SoundController.playSound(6,1);
            listener.exitScreen(this, CHECKPOINT_SELECTED_CODE);
        } else if(goToSettings){
            goToSettings = false;
            listener.exitScreen(this, MenuMode.OPTIONS_CODE);
        } else {
            update(delta);
            draw();
        }
    }

    private void update(float delta){

        frame = (frame+delta*5f)%maxFrame;
        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                (xbox != null && xbox.isConnected() && xbox.getA() && prevSelect != xbox.getA())){
            if(levelIsUnlocked(completedCheckpoints)) {
                levelSelected = true;
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D) ||
                (xbox != null && xbox.isConnected() && xbox.getLeftX() > 0.6f && prevRight != xbox.getLeftX() > 0.6f)){
            completedCheckpoints = (completedCheckpoints+1)%checkpoints;
            SoundController.playSound(4,1);
        } else if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A) ||
                (xbox != null && xbox.isConnected() && xbox.getLeftX() < -0.6f && prevLeft != xbox.getLeftX() < -0.6f)){
            completedCheckpoints = completedCheckpoints == 0 ? checkpoints - 1 : completedCheckpoints - 1;
            SoundController.playSound(4,1);
        } else if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
                (xbox != null && xbox.isConnected() && xbox.getB() && prevExit != xbox.getB())){
            listener.exitScreen(this, NESTED_MENU_EXIT_CODE);
        }
        if(xbox != null && xbox.isConnected()) {
            prevLeft = xbox.getLeftX() < -0.6f;
            prevRight = xbox.getLeftX() > 0.6f;
            prevExit = xbox.getB();
            prevSelect = xbox.getA();
        }
    }


    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    public void setDefault() {
        populateIcons();
        checkpointHitBoxes = new Rectangle[checkpoints];
        float diff = scale.x * 200;
        float start = canvas.getWidth()/2 - diff * (checkpoints/2) + levelTexture.getWidth();
        for (int i = 0; i < checkpoints; i++) {
            checkpointHitBoxes[i] = new Rectangle(i*diff+start,canvas.getHeight() / 2, 3f * levelTexture.getWidth(), 3f * levelTexture.getHeight());
        }
        Gdx.input.setInputProcessor(menuNestedInput);
        setBackground();
    }

    public void setNumCheckpoints(int checkpoints){
        this.checkpoints = checkpoints;
    }

    public int getNumCompletedCheckpoints() {return this.completedCheckpoints; }

    public void setLevel(int level){
        this.curLevel = level;
    }

    public void setNumCompletedCheckpoints(int completedCheckpoints){
        this.completedCheckpoints = completedCheckpoints;
    }

    public void draw(){
        canvas.clear();
        canvas.begin();

        float height = canvas.getHeight();
        float width = canvas.getWidth();


        List<TextureRegion> winTexturesCurLevel = winTextures.get(curLevel);
        float bgImageScale = Math.max(scale.x*canvas.getWidth()/ background.getWidth(), scale.y*canvas.getHeight()/ background.getHeight());
        canvas.draw(background,
                0.5f*canvas.getWidth()-canvas.getCameraX(),
                0.5f*canvas.getHeight()-canvas.getCameraY(),
                0, 0, background.getWidth(), background.getHeight(),
                bgImageScale,
                bgImageScale);
        float diff = scale.x * 200;
        float start = width/2 - diff * ((checkpoints-1)/2f);

        for(int i = 0; i < checkpoints; i++) {

            float imageScale = scale.x*((0.5f*3f*levelTexture.getWidth())/(winTexturesCurLevel.get(i).getRegionWidth()));
            int level_stars = getStarForLevel(i);
            Texture levelCompletedTexture =
                    level_stars == 0 ? levelCompletedTexture_0
                    : level_stars == 1 ? levelCompletedTexture_1
                    : level_stars == 2 ? levelCompletedTexture_2
                    : levelCompletedTexture_3;

            Color WHITE_SELECTED =  new Color(1,1,1, completedCheckpoints == i ? .8f : 1f);

            if (level_stars > 0) {
                canvas.draw(levelCompletedTexture, WHITE_SELECTED, levelCompletedTexture.getWidth()/2, levelCompletedTexture.getHeight()/2, i * diff + start, height/2, 0, scale.x*3f, scale.y*3f);
                canvas.draw(winTexturesCurLevel.get(i), Color.WHITE, winTexturesCurLevel.get(i).getRegionWidth()/2, 0, i * diff + start, height/2, 0, imageScale, imageScale);
            } else if(levelIsUnlocked(i)) {
                canvas.draw(levelTexture, WHITE_SELECTED, levelTexture.getWidth()/2, levelTexture.getHeight()/2, i * diff + start, height/2, 0, scale.x*3f, scale.y*3f);
                canvas.draw(winTexturesCurLevel.get(i),  new Color(0,0,0, completedCheckpoints == i ? .8f : 1f), winTexturesCurLevel.get(i).getRegionWidth()/2, 0, i * diff + start, height/2, 0, imageScale, imageScale);
            } else {
                canvas.draw(levelTexture, new Color(1,1,1, .5f), levelTexture.getWidth()/2, levelTexture.getHeight()/2, i * diff + start, height/2, 0, scale.x*3f, scale.y*3f);
                canvas.draw(winTexturesCurLevel.get(i),  new Color(0,0,0, .5f), winTexturesCurLevel.get(i).getRegionWidth()/2, 0, i * diff + start, height/2, 0, imageScale, imageScale);
            }

            checkpointHitBoxes[i] = new Rectangle(i*diff+start - scale.x*3f * levelTexture.getWidth()/2f,canvas.getHeight() / 2, scale.x*3f * levelTexture.getWidth(), scale.x*3f * levelTexture.getHeight());
        }

        filmstrip.setFrame((int)frame);

        float ox = 0.5f * filmstrip.getRegionWidth();
        float oy = 0.75f * filmstrip.getRegionHeight();
        canvas.draw(filmstrip, Color.WHITE, ox, 0,
                start + completedCheckpoints*diff, height/2 + 30,
                0, scale.x*2f, scale.y*2f);

        displayFont.setColor(YELLOW);
        displayFont.getData().setScale(0.6f*scale.x);
        canvas.drawText("WORLD " + (curLevel+1), displayFont, width*0.2f, height*0.9f);
        displayFont.setColor(Color.WHITE);
        super.drawBackSettings();

        canvas.end();
    }
}
