package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;

import static edu.cornell.lilbiggames.cephalonaut.engine.controller.MenuMode.*;

public class LevelCompleteMode extends MenuMode {

    /** The font for giving messages to the player */
    private BitmapFont displayFont;

    /** Listener that will move to selected level when we are done */
    private ScreenListener listener;

    /** Background texture for start-up */
    private Texture background;

    private String[] options = {"NEXT LEVEL", "REPLAY LEVEL", "LEVEL SELECT" };

    private int selectedOption;

    private float frame;

    private FilmStrip starScoring;

    private Texture starIcon;

    /** Reference to the game canvas */
    protected GameCanvas canvas;

    private AssetDirectory assets;

    private InputController inputController;

    private Vector2 bounds, scale;

    private String timeString;

    /**
     * Creates a MainMenuMode with the default size and position.
     *
     * @param assets    The asset directory to use
     * @param canvas 	The game canvas to draw to
     */
    public LevelCompleteMode(AssetDirectory assets, GameCanvas canvas, ScreenListener listener){
        super(assets, canvas, listener);
        this.assets = assets;
        this.canvas  = canvas;
        this.listener = listener;
        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();
        displayFont = assets.getEntry("retro", BitmapFont.class);

        frame = 0;
        starScoring = new FilmStrip(assets.getEntry("ui:star_scoring", Texture.class),1,20);
        starScoring.setFrame(0);
        starIcon = assets.getEntry("ui:star", Texture.class);

        background = assets.getEntry( "main-menu:background", Texture.class);
        background.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        selectedOption = 0;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    @Override
    public void resize(int width, int height) {
        scale.x = canvas.getWidth() / bounds.x;
        scale.y = scale.x;
        canvas.setCameraPos(0.5f * width,0.5f * height);
    }

    public void setSelectedOption(int option) {
        selectedOption = option;
    }

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }

    public void resetFrame() {
        starScoring.setFrame(0);
    }

    public void setDefault(){
        super.setDefault();
        Gdx.input.setInputProcessor(menuInput); selectedOption = 0;
    }

    private void update(float delta){
        SoundController.setBlackHoleSound(false,1);
        SoundController.setInkSound(false);
        inputController = InputController.getInstance();
        inputController.readInput(new Rectangle(), new Vector2());
        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || inputController.isSelectPressed()){
            SoundController.playSound(6,1);
            exitScreen();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || inputController.isBackPressed()){
            listener.exitScreen(this, EXIT_LEVEL_CODE);
        } else if(Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W) ||
                inputController.isUpPressed()){
            selectedOption = selectedOption == 0 ? options.length-1 : selectedOption-1;
        } else if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S) ||
                inputController.isDownPressed()){
            selectedOption = (selectedOption+1)%options.length;
        }

        frame += delta * 10f;
        if (frame > 1) {
            frame--;
            if (starScoring.getFrame() == starScoring.getSize() - 1) {
                return;
            }
            starScoring.setFrame(starScoring.getFrame() + 1);
        }
    }

    public void exitScreen() {
        if (selectedOption == 0) {
            listener.exitScreen(this, NEXT_LEVEL_CODE);
        } else if (selectedOption == 1) {
            listener.exitScreen(this, RESTART_LEVEL_CODE);
        } else if (selectedOption == 2) {
            listener.exitScreen(this, EXIT_LEVEL_CODE);
        }
    }

    @Override
    public void optionSelected(int i) {
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

    public void draw() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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

        canvas.setCameraPos(width / 2, height / 2);
//        canvas.drawOverlay(background, true);

        canvas.drawTextCentered("LEVEL COMPLETED", displayFont, 300f);

        for (int i = 0; i < 3; i++) {
            canvas.draw(starScoring, Color.WHITE,
                    starScoring.getFwidth() / 2f, starScoring.getFheight() / 2f,
                    width / 2f - 110 + 110 * i, height / 2f + 150,
                    0, 0.5f * scale.x, 0.5f * scale.y);
        }

        canvas.drawTextCentered(timeString, displayFont, 0f);

        super.drawOptions(options, selectedOption, 150);

        canvas.end();
    }
}

