package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;

public class MenuMode implements Screen {
    public static final int LEVEL_SELECTED_CODE = 21;
    public static int EXIT_LEVEL_CODE = 30;
    public static int RESUME_LEVEL_CODE = 31;
    public static int RESTART_LEVEL_CODE = 32;
    public static int RETURN_TO_START_CODE = 33;
    public static int START_CODE = 40;
    public static int OPTIONS_CODE = 41;
    public static int CREDITS_CODE = 42;
    public static final int CHECKPOINT_SELECTED_CODE = 51;
    public static final int NESTED_MENU_EXIT_CODE = 52;
    public static final int NEXT_LEVEL_CODE = 53;
    public static final int EXIT_LOADING_CODE = 61;

    protected final Color YELLOW = new Color(255.0f/256.0f, 232.0f/256.0f, 132.0f/256.0f, 1.0f);
    protected final float ARROW_WIDTH = 20.0f;

    /** The font for giving messages to the player */
    private BitmapFont displayFont;

    /** Listener that will move to selected level when we are done */
    private ScreenListener listener;

    /** Background texture for start-up */
    private Texture background;

    /** Reference to the game canvas */
    protected GameCanvas canvas;

    private AssetDirectory assets;
    protected Texture arrow;

    protected Vector2 bounds,scale;

    /**
     * Creates a MainMenuMode with the default size and position.
     *
     * @param assets    The asset directory to use
     * @param canvas 	The game canvas to draw to
     */
    public MenuMode(AssetDirectory assets, GameCanvas canvas, ScreenListener listener){
        this.canvas  = canvas;
        this.listener = listener;
        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();
        displayFont = assets.getEntry("retro", BitmapFont.class);

        background = assets.getEntry( "main-menu:background", Texture.class);
        background.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        arrow = assets.getEntry("arrow", Texture.class);

        this.assets = assets;

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {
        float scaleMin = Math.min(canvas.getWidth()/bounds.x, canvas.getHeight()/bounds.y);
        scale.x = scaleMin;
        scale.y = scaleMin;
        canvas.setCameraPos(0.5f*width,0.5f*height);
    }

    private void update(float delta){

    }

    protected void drawOptions(String[] options, int selectedOption) {
        drawOptions(options, selectedOption, 0);
    }

    protected void drawOptions(String[] options, int selectedOption, int offset){
        float start = (options.length*displayFont.getLineHeight())/2 - offset;
        displayFont.setColor(Color.ORANGE);
        displayFont.getData().setScale(0.7f*scale.x);

        for(int i = 0; i < options.length; i++){
            if(selectedOption == i) {
                displayFont.setColor(YELLOW);
            }
            canvas.drawTextCentered(options[i], displayFont, start - 1.2f*displayFont.getLineHeight()*i - 2*displayFont.getLineHeight());
            displayFont.setColor(Color.ORANGE);
        }
        displayFont.setColor(Color.WHITE);
        displayFont.getData().setScale(scale.x);
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

    }
}
