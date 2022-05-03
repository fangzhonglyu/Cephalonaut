package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.ImageObject;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;

import static edu.cornell.lilbiggames.cephalonaut.engine.controller.MenuMode.LEVEL_SELECTED_CODE;

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

    private InputController inputController;

    private Vector2 bounds,scale;

    private boolean levelSelected;

    private Color tint;
    private Rectangle hitBox;

    protected InputAdapter mainMenuInput = new InputAdapter() {
        public boolean mouseMoved (int x, int screenY) {
            if(hitBox != null){
                float y = canvas.getHeight() - screenY;
                if(hitBox.x <= x && hitBox.x + hitBox.width >= x && hitBox.y >= y && hitBox.y - hitBox.height <= y ){
                    tint = Color.WHITE;
                } else {
                    tint = Color.GRAY;
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


        tint = Color.GRAY;

        background = assets.getEntry( "main-menu:background", Texture.class);
        background.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        leftArrow = assets.getEntry( "arrowLeft", Texture.class);
        rightArrow = assets.getEntry( "arrowRight", Texture.class);
        this.assets = assets;

        curLevel = DEFAULT_LEVEL;
        levelIcon = assets.getEntry("levelicon:level_" + curLevel, Texture.class);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if (levelSelected && listener != null) {
            levelSelected = false;
            listener.exitScreen(this, LEVEL_SELECTED_CODE);
        } else {
            update(delta);
            draw();
        }
    }

    public void setDefault(){
        Gdx.input.setInputProcessor(mainMenuInput);
        float x = Gdx.input.getX();
        float y = canvas.getHeight() - Gdx.input.getY();

        if(hitBox != null){
            if(hitBox.x <= x && hitBox.x + hitBox.width >= x && hitBox.y >= y && hitBox.y - hitBox.height <= y ){
                tint = Color.WHITE;
            } else {
                tint = Color.GRAY;
            }
        }
    }

    private void update(float delta){
        inputController = InputController.getInstance();
        inputController.readInput(new Rectangle(), new Vector2());
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || inputController.isSelectPressed()){
            levelSelected = true;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D) ||
                inputController.isNextPressed()){
            curLevel = (curLevel + 1) % NUM_LEVELS;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A) ||
                inputController.isPrevPressed()){
            curLevel = curLevel == 0 ? NUM_LEVELS - 1 : curLevel - 1;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || inputController.isBackPressed()){
            listener.exitScreen(this, RETURN_TO_START_CODE);
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

        canvas.draw(background,
                0.5f*canvas.getWidth()-canvas.getCameraX(),
                0.5f*canvas.getHeight()-canvas.getCameraY(),
                0, 0, background.getWidth() * 10, background.getHeight() * 10,
                20,
                20);


        float levelIconWidth = levelIcon.getWidth();
        float levelIconHeight = levelIcon.getHeight();

        canvas.draw(levelIcon, tint,
                    levelIconWidth / 2f, levelIconHeight / 2f,
                    width / 2f, height / 2f + 100,
                    0, 1.5f * scale.x, 1.5f * scale.y);

        // left arrow
        canvas.draw(leftArrow, Color.WHITE,
                leftArrow.getWidth() / 2f, leftArrow.getHeight() / 2f,
                width / 5f, height / 2f,
                0, 5f * scale.x, 5f * scale.y);

        //right arrow
        canvas.draw(rightArrow, Color.WHITE,
                rightArrow.getWidth() / 2f, rightArrow.getHeight() / 2f,
                width - width / 5f, height / 2f, 0, 5f * scale.x, 5f * scale.y);

        hitBox = new Rectangle(width / 2f - (1.5f*scale.x)*levelIconWidth/2f, (height / 2f + 100) + (1.5f*scale.y)*levelIconHeight/2f, (1.5f*scale.x)*levelIconWidth, (1.5f*scale.y)*levelIconHeight);

        canvas.drawTextCentered("WORLD " + curLevel, displayFont, -levelIconHeight / 4f * scale.y - 60f);

        canvas.end();
    }
}
