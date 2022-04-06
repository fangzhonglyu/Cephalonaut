package edu.cornell.lilbiggames.cephalonaut.engine.controller;

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

    /** Reference to the game canvas */
    protected GameCanvas canvas;

    private AssetDirectory assets;

    private int curLevel;

    private InputController inputController;

    private Vector2 bounds,scale;

    private boolean levelSelected;

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
        displayFont = assets.getEntry("retro",BitmapFont.class);

        background = assets.getEntry( "main-menu:background", Texture.class);
        background.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        this.assets = assets;

        curLevel = DEFAULT_LEVEL;
        levelIcon = assets.getEntry("levelicon:level_"+curLevel, Texture.class);

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

    private void update(float delta){
        inputController = InputController.getInstance();
        inputController.readInput(new Rectangle(), new Vector2());
        if(inputController.isSelectPressed()){
            levelSelected = true;
        } else if (inputController.isNextPressed()){
            curLevel = (curLevel + 1)%NUM_LEVELS;
            levelIcon = assets.getEntry("levelicon:level_"+curLevel, Texture.class);
        } else if (inputController.isPrevPressed()){
            curLevel = curLevel == 0 ? NUM_LEVELS - 1 : curLevel - 1;
            levelIcon = assets.getEntry("levelicon:level_"+curLevel, Texture.class);
        }
    }

    public String getCurLevel(){
        return "level_"+curLevel;
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
//        background.draw(canvas);

        canvas.draw(background,
                0.5f*canvas.getWidth()-canvas.getCameraX(),
                0.5f*canvas.getHeight()-canvas.getCameraY(),
                0, 0, background.getWidth() * 10, background.getHeight() * 10,
                20,
                20);

        float levelIconWidth = levelIcon.getWidth();
        float levelIconHeight = levelIcon.getHeight();

        canvas.draw(levelIcon, Color.WHITE,
                    levelIconWidth / 2f, levelIconHeight / 2f,
                    width / 2f, height / 2f,
                    0, scale.x / 2f, scale.y / 2f);

        canvas.drawTextCentered("LEVEL: " + curLevel, displayFont, -levelIconHeight / 4f * scale.y - 60f);

        canvas.end();
    }
}
