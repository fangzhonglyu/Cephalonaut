package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.util.Controllers;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;
import edu.cornell.lilbiggames.cephalonaut.util.XBoxController;

public class MainMenuNestedMode extends MenuMode {

    /**
     * Reference to the game canvas
     */
    protected GameCanvas canvas;
    XBoxController xbox;
    private int checkpoints;
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
    private Texture background;
    private Texture levelIcon;
    private final AssetDirectory assets;
    private int curLevel;
    private final Vector2 bounds;
    private final Vector2 scale;
    private boolean levelSelected;
    private final TextureRegion octopusTexture;
    private final Texture levelTexture;
    private final Texture levelCompletedTexture;
    private int completedCheckpoints;
    private boolean prevRight;
    private boolean prevLeft;
    private boolean prevExit;
    private boolean prevSelect;

    /**
     * Creates a MainMenuMode with the default size and position.
     *
     * @param assets The asset directory to use
     * @param canvas The game canvas to draw to
     */
    public MainMenuNestedMode(AssetDirectory assets, GameCanvas canvas, int checkpoints, int completedCheckpoints, int curLevel, ScreenListener listener) {
        super(assets, canvas, listener);
        this.canvas = canvas;
        this.listener = listener;
        this.scale = new Vector2(1, 1);
        this.bounds = canvas.getSize().cpy();
        displayFont = assets.getEntry("retro", BitmapFont.class);
        this.completedCheckpoints = completedCheckpoints;
        this.checkpoints = checkpoints;

        this.curLevel = curLevel;
        background = assets.getEntry("BG-1-teal.png", Texture.class);
        background.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        this.assets = assets;

        octopusTexture = new TextureRegion(assets.getEntry("octopus.png", Texture.class));
        levelTexture = assets.getEntry("level.png", Texture.class);
        levelCompletedTexture = assets.getEntry("completedLevel.png", Texture.class);
        Array<XBoxController> controllers = Controllers.get().getXBoxControllers();
        if (controllers.size > 0) {
            xbox = controllers.get(0);
        } else {
            xbox = null;
        }
    }

    public void setBackground() {
        background = assets.getEntry("BG-" + (curLevel + 1), Texture.class);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if (levelSelected && listener != null) {
            levelSelected = false;
            SoundController.playSound(6, 1);

            listener.exitScreen(this, CHECKPOINT_SELECTED_CODE);
        } else {
            update(delta);
            draw();
        }
    }

    private void update(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                (xbox != null && xbox.isConnected() && xbox.getA() && prevSelect != xbox.getA())) {
            levelSelected = true;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D) ||
                (xbox != null && xbox.isConnected() && xbox.getLeftX() > 0.6f && prevRight != xbox.getLeftX() > 0.6f)) {
            completedCheckpoints = (completedCheckpoints + 1) % checkpoints;
            SoundController.playSound(4, 1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A) ||
                (xbox != null && xbox.isConnected() && xbox.getLeftX() < -0.6f && prevLeft != xbox.getLeftX() < -0.6f)) {
            completedCheckpoints = completedCheckpoints == 0 ? checkpoints - 1 : completedCheckpoints - 1;
            SoundController.playSound(4, 1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
                (xbox != null && xbox.isConnected() && xbox.getB() && prevExit != xbox.getB())) {
            listener.exitScreen(this, NESTED_MENU_EXIT_CODE);
        }
        if (xbox != null && xbox.isConnected()) {
            prevLeft = xbox.getLeftX() < -0.6f;
            prevRight = xbox.getLeftX() > 0.6f;
            prevExit = xbox.getB();
            prevSelect = xbox.getA();
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

    public void setNumCheckpoints(int checkpoints) {
        this.checkpoints = checkpoints;
    }

    public int getNumCompletedCheckpoints() {
        return this.completedCheckpoints;
    }

    public void setNumCompletedCheckpoints(int completedCheckpoints) {
        this.completedCheckpoints = completedCheckpoints;
    }

    public void setLevel(int level) {
        this.curLevel = level;
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
        float diff = 100;
        float start = width / 2 - diff * (checkpoints / 2);
        for (int i = 0; i < checkpoints; i++) {
            if (i <= completedCheckpoints) {
                canvas.draw(levelCompletedTexture, i * diff + start, height / 2, 0, 0, levelTexture.getWidth(), levelTexture.getHeight(), 0.1f, 0.1f);
            } else {
                canvas.draw(levelTexture, i * diff + start, height / 2, 0, 0, levelTexture.getWidth(), levelTexture.getHeight(), 0.1f, 0.1f);
            }
        }

        canvas.draw(octopusTexture, start + completedCheckpoints * diff, height / 2 + levelTexture.getHeight() * 0.1f + 10);

        canvas.end();
    }
}
