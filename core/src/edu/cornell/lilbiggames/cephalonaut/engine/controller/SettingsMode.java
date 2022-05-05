package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.engine.ui.Slider;
import edu.cornell.lilbiggames.cephalonaut.util.Controllers;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;
import edu.cornell.lilbiggames.cephalonaut.util.XBoxController;

import java.util.Map;

public class SettingsMode extends MenuMode {
    /** The font for giving messages to the player */
    private BitmapFont displayFont;

    /** Listener that will move to selected level when we are done */
    private ScreenListener listener;

    /** Background texture for start-up */
    private Texture background;

    /** Reference to the game canvas */
    protected GameCanvas canvas;

    private Map<String,Integer> keyBindings;
    Texture volumeDown, volumeUp;

    private int selectedOption;
    private String[] options;

    private int SLIDER_HEIGHT = 18;
    private Slider musicVolumeSlider;

    private int currentKey;
    private Vector2 startPosition;
    private boolean dragging;
    private boolean keybindingMode;

    private final float DEFAULT_VOLUME = 0.5f;
    private float musicVolume;
    XBoxController xbox;
    private boolean prevUp;
    private boolean prevDown;
    private boolean prevExit;

    private InputAdapter settingsInput = new InputAdapter() {
        @Override
        public boolean keyDown(int i) {
            currentKey = i;
            return true;
        }

        @Override
        public boolean touchDown (int x, int y, int pointer, int button) {
            startPosition = new Vector2(x,getCanvas().getHeight()-y);
            dragging = true;
            if(musicVolumeSlider.inKnobBounds(startPosition.x, startPosition.y)){
                musicVolumeSlider.movedX(startPosition.x);
                musicVolume = musicVolumeSlider.getValue();
            }
            return true;
        }

        @Override
        public boolean touchUp (int x, int y, int pointer, int button) {
            dragging = false;
            return true;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int button){
            if(dragging && musicVolumeSlider.inKnobBounds(startPosition.x, startPosition.y)){
                musicVolumeSlider.movedX(screenX);
                startPosition.x = screenX;
                musicVolume = musicVolumeSlider.getValue();
                SoundController.setMusicVolume(musicVolume);
            }

            return true;
        }
    };

    public SettingsMode(AssetDirectory assets, GameCanvas canvas, ScreenListener listener, Map<String,Integer> keyBindings){
        super(assets, canvas, listener);
        this.canvas = canvas;
        this.listener = listener;
        this.keyBindings = keyBindings;
        this.musicVolume = DEFAULT_VOLUME;
        options = keyBindings.keySet().toArray(new String[0]);

        background = assets.getEntry( "BG-1-teal.png", Texture.class );
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();
        displayFont = assets.getEntry("retro", BitmapFont.class);

        volumeDown = assets.getEntry("volume-down", Texture.class);
        volumeUp = assets.getEntry("volume-up", Texture.class);

        keybindingMode = false;
        dragging = false;
        musicVolumeSlider = new Slider(canvas, YELLOW,0.0f, 1.0f, musicVolume, false, canvas.getWidth()/3.0f, SLIDER_HEIGHT*scale.x, 20.0f);
        Array<XBoxController> controllers = Controllers.get().getXBoxControllers();
        if (controllers.size > 0) {
            xbox = controllers.get( 0 );
        } else {
            xbox = null;
        }
    }

    public GameCanvas getCanvas(){
        return canvas;
    }

    public float getMusicVolume(){
        return  musicVolume;
    }

    @Override
    public void show() {

    }

    public void setDefault(){
        super.setDefault();
        Gdx.input.setInputProcessor(settingsInput); selectedOption = 0;
    }

    @Override
    public void render(float delta) {

        if(keybindingMode) {
            if(Gdx.input.isKeyJustPressed(currentKey)) {
                keyBindings.put(options[selectedOption], currentKey);
                keybindingMode = false;
            }
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W) ||
                    (xbox != null && xbox.isConnected() && xbox.getLeftY() < -0.6f && prevUp != xbox.getLeftY() < -0.6f)) {
                selectedOption = selectedOption == 0 ? options.length - 1 : selectedOption - 1;
                SoundController.playSound(4,1);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S) ||
                    (xbox != null && xbox.isConnected() && xbox.getLeftY() > 0.6f && prevDown != xbox.getLeftY() > 0.6f)) {
                selectedOption = (selectedOption + 1) % options.length;
                SoundController.playSound(4,1);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
                    (xbox != null && xbox.isConnected() && xbox.getB() && prevExit != xbox.getB())) {
                listener.exitScreen(this, RETURN_TO_START_CODE);
            } else {
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    keybindingMode = true;

                    //keyBindings.put(options[selectedOption], currentKey);
                }
            }
            if(xbox != null && xbox.isConnected()) {
                prevUp = xbox.getLeftY() < -0.6f;
                prevDown = xbox.getLeftY() > 0.6f;
                prevExit = xbox.getB();
            }
        }

        draw();
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
        displayFont.getData().setScale(scale.x);
        displayFont.setColor(Color.ORANGE);
        canvas.drawTextCentered("SETTINGS", displayFont, height/4);

        float subtitleHeight = (3.0f/4.0f)*height - displayFont.getLineHeight();
        displayFont.getData().setScale(0.7f*scale.x);
        canvas.drawText("KEYBINDINGS", displayFont, width/4, subtitleHeight);

        float start = subtitleHeight - 1.5f * displayFont.getLineHeight();

        displayFont.getData().setScale(0.5f*scale.x);
        displayFont.setColor(Color.ORANGE);

        int i = 0;
        for(String binding : keyBindings.keySet()){
            float textHeight = start - 2*displayFont.getLineHeight()*i;
            if(i == selectedOption){
                displayFont.setColor(YELLOW);
                canvas.draw(
                        arrow,
                        YELLOW,
                        ARROW_WIDTH/2,
                        ARROW_WIDTH/2,
                        width*0.2f,
                        textHeight,
                        ARROW_WIDTH,
                        ARROW_WIDTH
                );
            }
            canvas.drawText(binding, displayFont, width/4, textHeight);
            if(i == selectedOption) displayFont.setColor(Color.CYAN);
            if(i == selectedOption && keybindingMode) canvas.drawText("<Enter Key>", displayFont, 0.75f*width, textHeight);
            else canvas.drawText(Input.Keys.toString(keyBindings.get(binding)), displayFont, 0.75f * width, textHeight);
            displayFont.setColor(Color.ORANGE);
            i++;
        }

        start = start - 2*displayFont.getLineHeight()*i;

        displayFont.getData().setScale(0.7f*scale.x);
        displayFont.setColor(Color.ORANGE);
        canvas.drawText("VOLUME", displayFont, width/4, start);

        float sliderHeight = SLIDER_HEIGHT*scale.x;

        musicVolumeSlider.updatePosition(width/2f, start-2*displayFont.getLineHeight());
        musicVolumeSlider.updateSize(width/3f, sliderHeight);
        musicVolumeSlider.draw();

        float halfSliderWidth = width/3.0f * 0.5f;

        // draw volume icons
        canvas.draw(volumeDown, Color.WHITE, 0, sliderHeight/2, width/2 - halfSliderWidth - 3*sliderHeight, start - 2f*displayFont.getLineHeight(), 2*sliderHeight, 2*sliderHeight);
        canvas.draw(volumeUp, Color.WHITE, 0, sliderHeight/2, width/2 + halfSliderWidth + 2*sliderHeight, start - 2f*displayFont.getLineHeight(), 2*sliderHeight, 2*sliderHeight);

        canvas.end();
    }

    @Override
    public void resize(int w, int h){
        super.resize(w,h);
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
