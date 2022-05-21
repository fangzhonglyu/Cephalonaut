package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.engine.GameState;
import edu.cornell.lilbiggames.cephalonaut.util.Controllers;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;
import edu.cornell.lilbiggames.cephalonaut.util.XBoxController;

public class StartScreenMode extends MenuMode {

    /** The font for giving messages to the player */
    private BitmapFont displayFont;

    /** Listener that will move to selected level when we are done */
    private ScreenListener listener;

    /** Background texture for start-up */
    private Texture background;
    private FilmStrip banner;
    private float frame;
    private int resetDataCount;
    private boolean resetDataMode;

    private String[] options = {"START", "OPTIONS", "CREDITS" };

    private int selectedOption;

    /** Reference to the game canvas */
    protected GameCanvas canvas;

    XBoxController xbox;
    private boolean prevUp;
    private boolean prevDown;
    private boolean prevExit;
    private boolean prevSelect;
    private GameState gameState;


    public StartScreenMode(AssetDirectory assets, GameCanvas canvas, ScreenListener listener, GameState gameState){
        super(assets, canvas, listener);
        this.canvas = canvas;
        this.listener = listener;
        background = assets.getEntry( "BG-1-teal.png", Texture.class );
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        banner = new FilmStrip(assets.getEntry( "banner-filmstrip", Texture.class),1,7);
        frame = 0;
        banner.setFrame(0);
        this.gameState = gameState;

        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();
        displayFont = assets.getEntry("retro", BitmapFont.class);
        selectedOption = 0; //default is resume

        Array<XBoxController> controllers = Controllers.get().getXBoxControllers();
        if (controllers.size > 0) {
            xbox = controllers.get( 0 );
        } else {
            xbox = null;
        }
        resetDataCount = 0;
        resetDataMode = false;
    }

    public void setDefault(){
        super.setDefault();
        Gdx.input.setInputProcessor(menuInput); selectedOption = 0;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        frame = (frame+10f*delta)%7;
        banner.setFrame((int)frame);

        if(resetDataMode){
            if (Gdx.input.isKeyJustPressed(Input.Keys.Y) || (xbox != null && xbox.isConnected() && xbox.getA())) {
                gameState.clearSave();
                resetDataMode = false;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) || (xbox != null && xbox.isConnected() && xbox.getB())) {
                resetDataMode = false;
            }
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                    (xbox != null && xbox.isConnected() && xbox.getA() && prevSelect != xbox.getA())) {
                SoundController.playSound(6, 1);
                exitScreen();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W) ||
                    (xbox != null && xbox.isConnected() && xbox.getLeftY() < -0.6f && prevUp != xbox.getLeftY() < -0.6f)) {
                selectedOption = selectedOption == 0 ? options.length - 1 : selectedOption - 1;
                SoundController.playSound(4, 1);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S) ||
                    (xbox != null && xbox.isConnected() && xbox.getLeftY() > 0.6f && prevDown != xbox.getLeftY() > 0.6f)) {
                selectedOption = (selectedOption + 1) % options.length;
                SoundController.playSound(4, 1);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.R) || (xbox != null && xbox.isConnected() && xbox.getBack())) {
                resetDataCount++;
            } else {
                resetDataCount = 0;
            }

            if (resetDataCount >= 150) {
                resetDataMode = true;
            }
        }

        if(xbox != null && xbox.isConnected()) {
            prevUp = xbox.getLeftY() < -0.6f;
            prevDown = xbox.getLeftY() > 0.6f;
            prevSelect = xbox.getA();
        }
        draw();
    }

    public void draw(){
        canvas.clear();
        canvas.begin();

        int height = canvas.getHeight();
        int width = canvas.getWidth();
        canvas.draw(background,
                canvas.getWidth() / 2f - canvas.getCameraX(),
                canvas.getHeight() / 2f - canvas.getCameraY(),
                width, height,0, 0,
                background.getWidth() * 2, background.getWidth() * 2 * height / width,
                1, 1);

        displayFont.getData().setScale(scale.y);
        displayFont.setColor(Color.ORANGE);
        float start = (options.length*displayFont.getLineHeight())/2 - scale.y*80f;
        canvas.draw(banner, Color.WHITE, width/2 - scale.x*2.5f*banner.getRegionWidth()/2, height/2+start, scale.x*2.5f*banner.getRegionWidth(), scale.y*2.5f*banner.getRegionHeight());
        if(resetDataMode) {
            canvas.drawTextCentered("RESET DATA?  Y/N", displayFont, -(height / 3));
        } else {
            super.drawOptions(options, selectedOption, (int)(scale.y*80));
        }

        canvas.end();
    }

    @Override
    public void exitScreen(){
        if (selectedOption == 0) {
            listener.exitScreen(this, START_CODE);
        } else if (selectedOption == 1) {
            listener.exitScreen(this, OPTIONS_CODE);
        } else if (selectedOption == 2) {
            listener.exitScreen(this, CREDITS_CODE);
        }
    }


    @Override
    public void optionSelected(int i){
        selectedOption = i;
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
}
