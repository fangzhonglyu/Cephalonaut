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

public class LevelCompleteMode implements Screen {
    public static final int EXit_LEVEL_CODE = 30;
    public static final int RESTART_LEVEL_CODE = 32;
    public static final int NEXT_LEVEL_CODE = 40;

    /** The font for giving messages to the player */
    private BitmapFont displayFont;

    /** Listener that will move to selected level when we are done */
    private ScreenListener listener;

    /** Background texture for start-up */
    private Texture background;

    private Texture replayIcon;

    private Texture homeIcon;

    private Texture starIcon;

    /** Reference to the game canvas */
    protected GameCanvas canvas;

    private AssetDirectory assets;

    private InputController inputController;

    private Vector2 bounds, scale;

    /**
     * Creates a MainMenuMode with the default size and position.
     *
     * @param assets    The asset directory to use
     * @param canvas 	The game canvas to draw to
     */
    public LevelCompleteMode(AssetDirectory assets, GameCanvas canvas, ScreenListener listener){
        this.canvas  = canvas;
        this.listener = listener;
        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();
        displayFont = assets.getEntry("retro", BitmapFont.class);

        background = assets.getEntry( "main-menu:background", Texture.class);
        background.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        this.assets = assets;

        replayIcon = assets.getEntry("replayicon", Texture.class);
        homeIcon = assets.getEntry("homeicon", Texture.class);
        starIcon = assets.getEntry("staricon", Texture.class);
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

    private void update(float delta){
        inputController = InputController.getInstance();
        inputController.readInput(new Rectangle(), new Vector2());
        if (clickedRestart()) {
            listener.exitScreen(this, RESTART_LEVEL_CODE);
        } else if (clickedHome()) {
            listener.exitScreen(this, EXit_LEVEL_CODE);
        }
        // else if next
            // switch to next level screen
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

    private boolean clickedRestart() {
        float mouseX = -10;
        float mouseY = -10;
        float height = canvas.getHeight();
        float width = canvas.getWidth();
        float rHeight = replayIcon.getHeight();
        float rWidth = replayIcon.getWidth();

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            mouseX = Gdx.input.getX();
            mouseY = height - Gdx.input.getY();
        }

        if (((width / 2f - 100) - rWidth / 2f <= mouseX && mouseX <= (width / 2f - 100) + rWidth / 2f ) &&
                ((height / 2f - 200) - rHeight / 2f <= mouseY && mouseY <= (height / 2f - 200) + rHeight / 2f )) {
            return true;
        }

        return false;
    }

    private boolean clickedHome() {
        float mouseX = -10;
        float mouseY = -10;
        float height = canvas.getHeight();
        float width = canvas.getWidth();
        float hHeight = homeIcon.getHeight();
        float hWidth = homeIcon.getWidth();

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            mouseX = Gdx.input.getX();
            mouseY = height - Gdx.input.getY();
        }

        if (((width / 2f + 100) - hWidth / 2f <= mouseX && mouseX <= (width / 2f + 100) + hWidth / 2f ) &&
                ((height / 2f - 200) - hHeight / 2f <= mouseY && mouseY <= (height / 2f - 200) + hHeight / 2f )) {
            return true;
        }

        return false;
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
                    width / 2f - 100 + 100 * i, height / 2f + 50,
                    0, scale.x, scale.y);
        }

        canvas.draw(replayIcon, Color.WHITE,
                replayIcon.getWidth() / 2f, replayIcon.getHeight() / 2f,
                width / 2f - 100, height / 2f - 200,
                0, scale.x, scale.y);

        canvas.draw(homeIcon, Color.WHITE,
                homeIcon.getWidth() / 2f, homeIcon.getHeight() / 2f,
                width / 2f + 100, height / 2f - 200,
                0, scale.x, scale.y);

        canvas.end();
    }
}

