package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;

public class MainMenuMode implements Screen {
    private static final int NUM_LEVELS = 7;
    private static final int DEFAULT_LEVEL = 0;

    /** The font for giving messages to the player */
    private BitmapFont displayFont;

    /** Listener that will move to selected level when we are done */
    private ScreenListener listener;

    /** Background texture for start-up */
    private Texture background;

    private Texture levelIcon;

    /** Reference to the game canvas */
    protected GameCanvas canvas;

    protected float scale;

    private AssetDirectory assets;

    private int curLevel;

    private InputController inputController;

    private boolean levelSelected;

    /**
     * Creates a MainMenuMode with the default size and position.
     *
     * @param assets    The asset directory to use
     * @param canvas 	The game canvas to draw to
     */
    public MainMenuMode(AssetDirectory assets, GameCanvas canvas, ScreenListener listener){
        this.canvas  = canvas;
        this.listener = listener;
        displayFont = assets.getEntry("retro",BitmapFont.class);

        background = assets.getEntry( "main-menu:background", Texture.class );
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
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
            listener.exitScreen(this, 0);
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

    /**
     * Called when the Screen is resized.
     *
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {

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
        canvas.draw(background, 0, 0 , 0, 0, background.getWidth(), background.getHeight(), (float)width/(float)background.getWidth(), (float)height/(float)background.getHeight());

        float levelIconWidth = levelIcon.getWidth();
        float levelIconHeight = levelIcon.getHeight();

        canvas.draw(levelIcon, width/2 - 0.5f*levelIconWidth/2, height/2 - 0.5f*levelIconHeight/2, 0, 0, levelIcon.getWidth(), levelIcon.getHeight(), 0.5f, 0.5f);

        canvas.drawTextCentered("LEVEL: " + curLevel, displayFont, -height/4);

        canvas.end();
    }
}
