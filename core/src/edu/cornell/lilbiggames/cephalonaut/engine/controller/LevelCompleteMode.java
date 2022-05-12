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
import edu.cornell.lilbiggames.cephalonaut.engine.GameState;
import edu.cornell.lilbiggames.cephalonaut.util.Controllers;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;
import edu.cornell.lilbiggames.cephalonaut.util.XBoxController;


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
    private FilmStrip starStill;
    private Texture starFrame;

    /** Reference to the game canvas */
    protected GameCanvas canvas;

    private int timer;

    private String timeString;

    XBoxController xbox;
    private boolean prevUp;
    private boolean prevDown;
    private boolean prevExit;
    private boolean prevSelect;
    private boolean choiceMade;

    private int twoStars, threeStars;
    private String identifier;
    private GameState gamestate;

    /**
     * Creates a MainMenuMode with the default size and position.
     *
     * @param assets    The asset directory to use
     * @param canvas 	The game canvas to draw to
     */
    public LevelCompleteMode(AssetDirectory assets, GameCanvas canvas, ScreenListener listener, GameState state){
        super(assets, canvas, listener);
        this.canvas  = canvas;
        this.listener = listener;
        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();
        displayFont = assets.getEntry("retro", BitmapFont.class);

        frame = 0;
        starScoring = new FilmStrip(assets.getEntry("ui:star_scoring", Texture.class),1,20);
        starStill = new FilmStrip(assets.getEntry("ui:star_scoring", Texture.class),1,20);
        starFrame = assets.getEntry("ui:star", Texture.class);
        starScoring.setFrame(0);
        starStill.setFrame(19);

        background = assets.getEntry( "BG-1-teal.png", Texture.class);
        background.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        selectedOption = 0;
        Array<XBoxController> controllers = Controllers.get().getXBoxControllers();
        if (controllers.size > 0) {
            xbox = controllers.get( 0 );
        } else {
            xbox = null;
        }

        choiceMade = false;
        identifier = "";
        gamestate = state;
    }

    @Override
    public void show() {
        setState();
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

    public void setLevelIdentifier(String id) {
        identifier = id;
    }

    public void setSelectedOption(int option) {
        selectedOption = option;
    }

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }

    public void setTimer(int timer) { this.timer = timer; }

    public void setStars(int twoStars, int threeStars) {
        this.twoStars = twoStars <= 1 ? Integer.MAX_VALUE : twoStars;
        this.threeStars = threeStars <= 1 ? Integer.MAX_VALUE : threeStars;
    }

    private void setState() {
        if (timer <= threeStars) {
            gamestate.setStars(identifier, 3);
        } else if (timer <= twoStars) {
            gamestate.setStars(identifier, 2);
        } else {
            gamestate.setStars(identifier, 1);
        }
    }

    public void resetFrame() {
        starScoring.setFrame(0);
    }

    public void resetChoiceMade() { choiceMade = false; }

    public void setDefault(){
        super.setDefault();
        Gdx.input.setInputProcessor(menuInput); selectedOption = 0;
    }

    private void update(float delta){
        SoundController.setBlackHoleSound(false,1);
        SoundController.setInkSound(false);
        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                (xbox != null && xbox.isConnected() && xbox.getA() && prevSelect != xbox.getA())){
            SoundController.playSound(6,1);
            exitScreen();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
                (xbox != null && xbox.isConnected() && xbox.getB() && prevExit != xbox.getB())){
            listener.exitScreen(this, EXIT_LEVEL_CODE);
        } else if(Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W) ||
                (xbox != null && xbox.isConnected() && xbox.getLeftY() < -0.6f && prevUp != xbox.getLeftY() < -0.6f)){
            selectedOption = selectedOption == 0 ? options.length-1 : selectedOption-1;
            SoundController.playSound(4,1);
        } else if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S) ||
                (xbox != null && xbox.isConnected() && xbox.getLeftY() > 0.6f && prevDown != xbox.getLeftY() > 0.6f)){
            selectedOption = (selectedOption+1)%options.length;
            SoundController.playSound(4,1);
        }
        if(xbox != null && xbox.isConnected()) {
            prevUp = xbox.getLeftY() < -0.6f;
            prevDown = xbox.getLeftY() > 0.6f;
            prevExit = xbox.getB();
            prevSelect = xbox.getA();
        }

        frame += delta * 10f;
        if (frame > 1) {
            frame--;
            if (starScoring.getFrame() == starScoring.getSize() - 1) {
                return;
            }
            starScoring.setFrame(starScoring.getFrame() + 1);
        }

        canvas.setCameraPos(canvas.getWidth() / 2f, canvas.getHeight() / 2f);

    }

    public void exitScreen() {
        if (goToSettings){
            goToSettings = false;
            choiceMade = true;
            listener.exitScreen(this, MenuMode.OPTIONS_CODE);
        }

        if(!choiceMade) {
            if (selectedOption == 0) {
                listener.exitScreen(this, NEXT_LEVEL_CODE);
            } else if (selectedOption == 1) {
                listener.exitScreen(this, RESTART_LEVEL_CODE);
            } else if (selectedOption == 2) {
                listener.exitScreen(this, EXIT_LEVEL_CODE);
            }
            choiceMade = true;
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
        float height = canvas.getHeight();
        float width = canvas.getWidth();

        canvas.clear();
        canvas.begin();

        canvas.draw(background,
                0.5f*canvas.getWidth()-canvas.getCameraX(),
                0.5f*canvas.getHeight()-canvas.getCameraY(),
                0, 0, background.getWidth() * 10, background.getHeight() * 10,
                20,
                20);

        super.drawGoToSettings();
        displayFont.getData().setScale(Math.min(scale.x,scale.y));

        canvas.drawTextCentered("LEVEL COMPLETED", displayFont, canvas.getHeight()*0.4f);

        int stars = 1;
        if (timer <= threeStars) {
            stars = 3;
        } else if (timer <= twoStars) {
            stars = 2;
        }

        for (int i = 0; i < stars; i++) {
            if (stars == 3) {
                canvas.draw(starScoring, Color.WHITE,
                        starScoring.getFwidth() / 2f, starScoring.getFheight() / 2f,
                        width / 2f - 130 * scale.x + scale.x * (130 * i), height / 2f + scale.y * 150,
                        0, 0.5f * scale.x, 0.5f * scale.y);
            } else {
                canvas.draw(starStill, Color.WHITE,
                        starStill.getFwidth() / 2f, starStill.getFheight() / 2f,
                        width / 2f - 130 * scale.x + scale.x * (130 * i), height / 2f + scale.y * 150,
                        0, 0.5f * scale.x, 0.5f * scale.y);
            }
        }

        for (int i = 0; i < 3 - stars; i++) {
            canvas.draw(starFrame, Color.WHITE,
                    starFrame.getWidth() / 2f, starFrame.getHeight() / 2f,
                    width / 2f + 130 * scale.x - scale.x * (130 * i), height / 2f + scale.y * 150,
                    0, 0.5f * scale.x, 0.5f * scale.y);
        }

        canvas.drawTextCentered(timeString, displayFont, 0f);

        int minutes2 = (twoStars % 3600) / 60;
        int seconds2 = twoStars % 60;
        String timeString2 = String.format("%02d:%02d", minutes2, seconds2);

        int minutes3 = (threeStars % 3600) / 60;
        int seconds3 = threeStars % 60;
        String timeString3 = String.format("%02d:%02d", minutes3, seconds3);

        super.drawOptions(options, selectedOption, (int)(150*scale.y));

        float x = canvas.getWidth() * 0.40f + canvas.getCameraX() - 120 * scale.x;
        float y = canvas.getHeight() * 0.47f + canvas.getCameraY() - 5 * scale.y;
        for (int i = 0; i < 3; i++) {
                canvas.draw(starStill, Color.WHITE,
                        starStill.getFwidth() / 2f, starStill.getFheight() / 2f,
                        x + scale.x * (50 * i), y,
                        0, 0.2f * scale.x, 0.2f * scale.y);
                if (i > 0) {
                    canvas.draw(starStill, Color.WHITE,
                            starStill.getFwidth() / 2f, starStill.getFheight() / 2f,
                            x + scale.x * (50 * i), y - 60 * scale.y,
                            0, 0.2f * scale.x, 0.2f * scale.y);
                }
        }

        displayFont.getData().setScale(0.5f * Math.min(scale.x, scale.y));
        canvas.drawTextTopRight(timeString3, displayFont, (int) (-20 * scale.x), (int) (-5 * scale.y));
        canvas.drawTextTopRight(timeString2, displayFont, (int) (-20 * scale.x), (int) (55 * scale.y));
        displayFont.getData().setScale(1f);

        canvas.end();
    }
}

