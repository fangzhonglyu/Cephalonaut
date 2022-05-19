package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;
import edu.cornell.lilbiggames.cephalonaut.util.Controllers;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;
import edu.cornell.lilbiggames.cephalonaut.util.XBoxController;

public class MainMenuMode extends MenuMode {
    private static final int DEFAULT_LEVEL = 0;
    public static final int NUM_LEVELS = 7;

    /** The font for giving messages to the player */
    private BitmapFont displayFont;

    /** Listener that will move to selected level when we are done */
    private ScreenListener listener;

//    ImageObject background;

    /** Background texture for start-up */
    private Texture background;

    private Texture levelIcon;
    private Texture leftArrow;
    private Texture rightArrow;

    /** Reference to the game canvas */
    protected GameCanvas canvas;

    private AssetDirectory assets;

    private int curLevel;

    private boolean levelSelected;

    private Rectangle hitBox;
    XBoxController xbox;
    private boolean prevRight;
    private boolean prevLeft;
    private boolean prevExit;
    private boolean prevSelect;

    private boolean shouldAnimate;
    private FilmStrip[] filmStrips;
    private float frame;

    private Rectangle left;
    private Rectangle right;

    protected InputAdapter mainMenuInput = new InputAdapter() {
        public boolean mouseMoved (int x, int screenY) {
            if(hitBox != null){
                float y = canvas.getHeight() - screenY;
                if(hitBox.x <= x && hitBox.x + hitBox.width >= x && hitBox.y >= y && hitBox.y - hitBox.height <= y ){
                   shouldAnimate = false;
                } else {
                   shouldAnimate = true;
                }
            }

            return true;
        }

        public boolean touchDown (int x, int screenY, int pointer, int button) {
            float y = canvas.getHeight() - screenY;
            if(hitBox != null){
                if(hitBox.x <= x && hitBox.x + hitBox.width >= x && hitBox.y >= y && hitBox.y - hitBox.height <= y ){
                    levelSelected = true;
                }
            }

            if(left != null){
                if(left.x <= x && left.x + left.width >= x && left.y >= y && left.y - left.height <= y ){
                    curLevel = curLevel == 0 ? NUM_LEVELS - 1 : curLevel - 1;
                    frame = 0;
                    SoundController.playSound(4, 1);
                }
            }

            if(right != null){
                if(right.x <= x && right.x + right.width >= x && right.y >= y && right.y - right.height <= y ){
                    curLevel = (curLevel + 1) % NUM_LEVELS;
                    frame = 0;
                    SoundController.playSound(4,1);
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


    /**
     * Creates a MainMenuMode with the default size and position.
     *
     * @param assets    The asset directory to use
     * @param canvas 	The game canvas to draw to
     */
    public MainMenuMode(AssetDirectory assets, GameCanvas canvas, ScreenListener listener){
        super(assets, canvas, listener);
        this.canvas  = canvas;
        this.listener = listener;
        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();
        displayFont = assets.getEntry("retro", BitmapFont.class);

        shouldAnimate = true;

        background = assets.getEntry( "BG-1-teal.png", Texture.class);
        background.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        leftArrow = assets.getEntry( "arrowLeft", Texture.class);
        rightArrow = assets.getEntry( "arrowRight", Texture.class);
        this.assets = assets;

        curLevel = DEFAULT_LEVEL;
        levelIcon = assets.getEntry("levelicon:level_" + curLevel, Texture.class);
        filmStrips = new FilmStrip[7];

        for (int i = 0; i < 7; i++) {
            int cols = i == 0 ? 6 : 5;
            filmStrips[i] = new FilmStrip(assets.getEntry("levelidle:level_" + i, Texture.class), 1, cols);
            filmStrips[i].setFrame(0);
        }
        Array<XBoxController> controllers = Controllers.get().getXBoxControllers();
        if (controllers.size > 0) {
            xbox = controllers.get( 0 );
        } else {
            xbox = null;
        }
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        frame = (frame+delta*5f)%5;
        if (levelSelected && listener != null) {
            SoundController.playSound(6,1);
            levelSelected = false;
            listener.exitScreen(this, LEVEL_SELECTED_CODE);
        } else if (goToSettings){
            goToSettings = false;
            listener.exitScreen(this, MenuMode.OPTIONS_CODE);
        } else if (goBack){
            goBack = false;
            listener.exitScreen(this, MenuMode.GO_BACK_CODE);
        } else {
            update(delta);
            draw();
        }
    }

    public void setDefault(){
        Gdx.input.setInputProcessor(mainMenuInput);
        float x = Gdx.input.getX();
        float y = canvas.getHeight() - Gdx.input.getY();
    }

    private void update(float delta){
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                (xbox != null && xbox.isConnected() && xbox.getA() && prevSelect != xbox.getA())){
            levelSelected = true;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D) ||
                (xbox != null && xbox.isConnected() && xbox.getLeftX() > 0.6f && prevRight != xbox.getLeftX() > 0.6f)){
            curLevel = (curLevel + 1) % NUM_LEVELS;
            frame = 0;
            SoundController.playSound(4,1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A) ||
                (xbox != null && xbox.isConnected() && xbox.getLeftX() < -0.6f && prevLeft != xbox.getLeftX() < -0.6f)){
            curLevel = curLevel == 0 ? NUM_LEVELS - 1 : curLevel - 1;
            frame = 0;
            SoundController.playSound(4, 1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
                (xbox != null && xbox.isConnected() && xbox.getB() && prevExit != xbox.getB())){
            listener.exitScreen(this, RETURN_TO_START_CODE);
        }
        if(xbox != null && xbox.isConnected()) {
            prevLeft = xbox.getLeftX() < -0.6f;
            prevRight = xbox.getLeftX() > 0.6f;
            prevExit = xbox.getB();
            prevSelect = xbox.getA();
        }
        levelIcon = assets.getEntry("levelicon:level_" + curLevel, Texture.class);
    }

    public String getCurLevel(){
        return "level_" + curLevel;
    }

    public int getCurLevelNumber(){
        return curLevel;
    }

    public void nextLevel() {
        curLevel = (curLevel + 1) % NUM_LEVELS;
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

    public void draw(){
        canvas.clear();
        canvas.begin();

        float height = canvas.getHeight();
        float width = canvas.getWidth();

        displayFont.getData().setScale(scale.x);

        canvas.draw(background,
                0.5f*canvas.getWidth()-canvas.getCameraX(),
                0.5f*canvas.getHeight()-canvas.getCameraY(),
                0, 0, background.getWidth() * 10, background.getHeight() * 10,
                20,
                20);

        float levelIconWidth = filmStrips[curLevel].getRegionWidth();
        float levelIconHeight = filmStrips[curLevel].getRegionHeight();

        float imageScale = shouldAnimate ? 1.5f : 2f;

        if(!shouldAnimate) {
            levelIconWidth = levelIcon.getWidth();
            levelIconHeight = levelIcon.getHeight();

            canvas.draw(levelIcon, Color.WHITE,
                    levelIconWidth / 2f, levelIconHeight / 2f,
                    width / 2f, height / 2f + 100,
                    0, imageScale * scale.x, imageScale * scale.y);
        } else {
            filmStrips[curLevel].setFrame((int)frame);
            canvas.draw(filmStrips[curLevel], Color.WHITE,
                     levelIconWidth/ 2f,  levelIconHeight/ 2f,
                    width / 2f, height / 2f + 100,
                    0, imageScale * scale.x, imageScale * scale.y);
        }

        float textHeight = Math.min(1.5f*filmStrips[curLevel].getRegionHeight(), 2f*levelIcon.getWidth())*scale.y;
        displayFont.setColor(YELLOW);
        canvas.drawTextCentered("WORLD " + (curLevel+1), displayFont, -textHeight+100f);

        // left arrow
        canvas.draw(leftArrow, Color.WHITE,
                leftArrow.getWidth() / 2f, leftArrow.getHeight() / 2f,
                width / 5f, height/2-textHeight+100f,
                0, 0.05f * scale.x, 0.05f * scale.y);

        //right arrow
        canvas.draw(rightArrow, Color.WHITE,
                rightArrow.getWidth() / 2f, rightArrow.getHeight() / 2f,
                width - width / 5f, height/2 -textHeight+100f, 0, 0.05f * scale.x, 0.05f * scale.y);

        hitBox = new Rectangle(width / 2f - scale.x*imageScale*levelIconWidth/2f, (height / 2f + 100) + scale.y*imageScale*levelIconHeight/2f, scale.x*imageScale*levelIconWidth, scale.y*imageScale*levelIconHeight);
        left = new Rectangle(width/5f - 0.1f*leftArrow.getWidth()/2f, height/2-textHeight+100f + 0.1f*scale.x* leftArrow.getHeight()/2f, 0.1f*leftArrow.getWidth(), 0.1f*leftArrow.getHeight());
        right = new Rectangle(width - width/5f - 0.1f*rightArrow.getWidth()/2f, height/2-textHeight+100f + 0.1f*scale.x* rightArrow.getHeight()/2f, 0.1f*rightArrow.getWidth(), 0.1f*rightArrow.getHeight());

        super.drawBackSettings();
        canvas.end();
    }
}
