package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;

public class MainMenuMode implements Screen {
    private static final int NUM_LEVELS = 7;


    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** Background texture for start-up */
    private Texture background;

    private Texture levelIcon;

    /** Reference to the game canvas */
    protected GameCanvas canvas;

    protected float scale;

    private int width, height;

    private AssetDirectory assets;

    private String curLevel;

    private InputController inputController;

    private boolean levelSelected;

    /**
     * Creates a MainMenuMode with the default size and position.
     *
     * @param assets    The asset directory to use
     * @param canvas 	The game canvas to draw to
     */
    public MainMenuMode(AssetDirectory assets, GameCanvas canvas){
        this.canvas  = canvas;

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());

        background = assets.getEntry( "main-menu:background", Texture.class );
        background.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);

        levelIcon = assets.getEntry("levelicon:level_1", Texture.class);

        curLevel = "level_1";
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }


    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if (levelSelected && listener != null) {
            System.out.println("exiting menu");
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
            System.out.println("select pressed");
            levelSelected = true;
        }
    }

    public String getCurLevel(){
        return curLevel;
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
        canvas.begin();
        canvas.draw(background, 0, 0);

        int levelIconWidth = levelIcon.getWidth();
        int levelIconHeight = levelIcon.getHeight();
        int width = canvas.getWidth();;
        int height = canvas.getHeight();

        canvas.draw(levelIcon, width/2 - levelIconWidth/2, height/2 - levelIconHeight/2, 0, 0, levelIcon.getWidth(), levelIcon.getHeight(), 1, 1);

        canvas.end();
    }
}
