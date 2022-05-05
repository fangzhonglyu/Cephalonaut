package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.util.Controllers;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;
import edu.cornell.lilbiggames.cephalonaut.util.XBoxController;

public class StartScreenMode extends MenuMode {

    /**
     * Reference to the game canvas
     */
    protected GameCanvas canvas;
    XBoxController xbox;
    /**
     * The font for giving messages to the player
     */
    private final BitmapFont displayFont;
    /**
     * Listener that will move to selected level when we are done
     */
    private final ScreenListener listener;
    /**
     * Background texture for start-up
     */
    private final Texture background;
    private final Texture banner;
    private final String[] options = {"START", "OPTIONS", "CREDITS"};
    private int selectedOption;
    private boolean prevUp;
    private boolean prevDown;
    private boolean prevExit;
    private boolean prevSelect;


    public StartScreenMode(AssetDirectory assets, GameCanvas canvas, ScreenListener listener) {
        super(assets, canvas, listener);
        this.canvas = canvas;
        this.listener = listener;
        background = assets.getEntry("BG-1-teal.png", Texture.class);
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        banner = assets.getEntry("banner", Texture.class);


        this.scale = new Vector2(1, 1);
        this.bounds = canvas.getSize().cpy();
        displayFont = assets.getEntry("retro", BitmapFont.class);
        selectedOption = 0; //default is resume

        Array<XBoxController> controllers = Controllers.get().getXBoxControllers();
        if (controllers.size > 0) {
            xbox = controllers.get(0);
        } else {
            xbox = null;
        }
    }

    public void setDefault() {
        super.setDefault();
        Gdx.input.setInputProcessor(menuInput);
        selectedOption = 0;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

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
        if (xbox != null && xbox.isConnected()) {
            prevUp = xbox.getLeftY() < -0.6f;
            prevDown = xbox.getLeftY() > 0.6f;
            prevSelect = xbox.getA();
        }
        draw();
    }

    public void draw() {
        canvas.clear();
        canvas.begin();

        float height = canvas.getHeight();
        float width = canvas.getWidth();
        canvas.draw(background,
                0.5f * canvas.getWidth() - canvas.getCameraX(),
                0.5f * canvas.getHeight() - canvas.getCameraY(),
                0, 0, background.getWidth() * 10, background.getHeight() * 10,
                20,
                20);

        displayFont.getData().setScale(scale.x);
        displayFont.setColor(Color.ORANGE);
        float start = (options.length * displayFont.getLineHeight()) / 2;
        canvas.draw(banner, width / 2 - scale.x * 0.5f * banner.getWidth() / 2, height / 2 + start, 0, 0, banner.getWidth(), banner.getHeight(), scale.x * 0.5f, scale.y * 0.5f);
        super.drawOptions(options, selectedOption);

        canvas.end();
    }

    @Override
    public void exitScreen() {
        if (selectedOption == 0) {
            listener.exitScreen(this, START_CODE);
        } else if (selectedOption == 1) {
            listener.exitScreen(this, OPTIONS_CODE);
        } else if (selectedOption == 2) {
            listener.exitScreen(this, CREDITS_CODE);
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
}
