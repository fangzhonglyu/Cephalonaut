package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;

import static edu.cornell.lilbiggames.cephalonaut.engine.controller.MenuMode.CHECKPOINT_SELECTED_CODE;
import static edu.cornell.lilbiggames.cephalonaut.engine.controller.MenuMode.NESTED_MENU_EXIT_CODE;

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

    private InputController inputController;

    private Vector2 bounds,scale;

    private boolean levelSelected;

    private TextureRegion octopusTexture;
    private Texture levelTexture;
    private Texture levelCompletedTexture;

    private int completedCheckpoints;

    private FilmStrip filmstrip;
    /** Animation Counter*/
    private float frame;
    private float maxFrame;

    /**
     * Creates a MainMenuMode with the default size and position.
     *
     * @param assets    The asset directory to use
     * @param canvas 	The game canvas to draw to
     */
    public MainMenuNestedMode(AssetDirectory assets, GameCanvas canvas, int checkpoints, int completedCheckpoints, int curLevel, ScreenListener listener){
        super(assets, canvas, listener);
        this.canvas  = canvas;
        this.listener = listener;
        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();
        displayFont = assets.getEntry("retro", BitmapFont.class);
        this.completedCheckpoints = completedCheckpoints;
        this.checkpoints = checkpoints;

        background = assets.getEntry( "main-menu:background", Texture.class);
        background.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        this.assets = assets;

        this.curLevel = curLevel;
        octopusTexture = new TextureRegion(assets.getEntry( "octopus.png", Texture.class ));
        levelTexture = assets.getEntry( "level.png", Texture.class );
        levelCompletedTexture = assets.getEntry( "completedLevel.png", Texture.class );

        filmstrip = new FilmStrip(assets.getEntry("octopus",Texture.class), 5, 9);
        frame = 0;
        maxFrame = 4;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if (levelSelected && listener != null) {
            levelSelected = false;
            listener.exitScreen(this, CHECKPOINT_SELECTED_CODE);
        } else {
            update(delta);
            draw();
        }
    }

    private void update(float delta){
        frame = (frame+delta*5f)%maxFrame;

        inputController = InputController.getInstance();
        inputController.readInput(new Rectangle(), new Vector2());
        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
            levelSelected = true;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)){
            completedCheckpoints = (completedCheckpoints+1)%checkpoints;
        } else if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)){
            completedCheckpoints = completedCheckpoints == 0 ? checkpoints - 1 : completedCheckpoints - 1;
        } else if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || inputController.isBackPressed()){
            listener.exitScreen(this, NESTED_MENU_EXIT_CODE);
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
        canvas.draw(background,
                0.5f*canvas.getWidth()-canvas.getCameraX(),
                0.5f*canvas.getHeight()-canvas.getCameraY(),
                0, 0, background.getWidth() * 10, background.getHeight() * 10,
                20,
                20);
        float diff = 100;
        float start = width/2 - diff * (checkpoints/2);
        for(int i = 0; i < checkpoints; i++) {
            if (i <= completedCheckpoints) {
                canvas.draw(levelCompletedTexture, i * diff + start, height / 2, 0, 0, levelTexture.getWidth(), levelTexture.getHeight(), 0.1f, 0.1f);
            } else {
                canvas.draw(levelTexture, i * diff + start, height / 2, 0, 0, levelTexture.getWidth(), levelTexture.getHeight(), 0.1f, 0.1f);
            }
        }

        filmstrip.setFrame((int)frame);

        float ox = 0.5f * filmstrip.getRegionWidth();
        float oy = 0.75f * filmstrip.getRegionHeight();
        canvas.draw(filmstrip, Color.WHITE, 0, 0,
                start + completedCheckpoints*diff, height/2 + filmstrip.getRegionHeight()*0.1f,
                0, scale.x*2, scale.y*2);

        canvas.end();
    }
}
