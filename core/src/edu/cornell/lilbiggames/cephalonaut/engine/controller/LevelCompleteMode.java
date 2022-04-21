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
        this.canvas  = canvas;
        this.listener = listener;
        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();
        displayFont = assets.getEntry("retro", BitmapFont.class);

        background = assets.getEntry( "main-menu:background", Texture.class);
        background.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        this.assets = assets;

        starIcon = assets.getEntry("staricon", Texture.class);
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

    private void update(float delta){
        SoundController.killAllSound();
        inputController = InputController.getInstance();
        inputController.readInput(new Rectangle(), new Vector2());
        if (selectedOption == 0 && inputController.isSelectPressed()){
            listener.exitScreen(this, NEXT_LEVEL_CODE);
        } else if(selectedOption == 1 && inputController.isSelectPressed()){
            listener.exitScreen(this, RESTART_LEVEL_CODE);
        } else if(selectedOption == 2 && inputController.isSelectPressed() || inputController.didExit()){
            listener.exitScreen(this, EXIT_LEVEL_CODE);
        } else if(inputController.isUpPressed()){
            selectedOption = selectedOption == 0 ? options.length-1 : selectedOption -1;
        } else if(inputController.isDownPressed()){
            selectedOption = (selectedOption + 1 ) % options.length;
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

    public void draw() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        canvas.clear();
        canvas.begin();

        float height = canvas.getHeight();
        float width = canvas.getWidth();

        canvas.setCameraPos(width / 2, height / 2);
        canvas.drawOverlay(background, true);

        canvas.drawTextCentered("LEVEL COMPLETED", displayFont, 300f);

        for (int i = 0; i < 3; i++) {
            canvas.draw(starIcon, Color.GOLD,
                    starIcon.getWidth() / 2f, starIcon.getHeight() / 2f,
                    width / 2f - 100 + 100 * i, height / 2f + 150,
                    0, scale.x, scale.y);
        }

        canvas.drawTextCentered(timeString, displayFont, 0f);

        super.drawOptions(options, selectedOption, 200);

        canvas.end();
    }
}

