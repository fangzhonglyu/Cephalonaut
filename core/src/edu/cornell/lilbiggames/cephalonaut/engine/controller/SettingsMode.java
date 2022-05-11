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
    private Slider fxVolumeSlider;

    private int currentKey;
    private Vector2 startPosition;
    private boolean dragging;
    private boolean keybindingMode;

    private final float DEFAULT_VOLUME = 0.5f;
    private float musicVolume;
    private float fxVolume;
    private float KNOB_RADIUS = 20.0f;

    XBoxController xbox;
    private boolean prevUp;
    private boolean prevDown;
    private boolean prevExit;

    Rectangle[] bindingsHitBoxes;


    private InputAdapter settingsInput = new InputAdapter() {
        @Override
        public boolean keyDown(int i) {
            currentKey = i;
            return true;
        }

        @Override
        public boolean touchDown (int x, int screenY, int pointer, int button) {
            startPosition = new Vector2(x,getCanvas().getHeight()-screenY);
            float y = startPosition.y;
            dragging = true;
            if(musicVolumeSlider.inKnobBounds(startPosition.x, startPosition.y)){
                musicVolumeSlider.movedX(startPosition.x);
                musicVolume = musicVolumeSlider.getValue();
            }

            if(fxVolumeSlider.inKnobBounds(startPosition.x, startPosition.y)){
                fxVolumeSlider.movedX(startPosition.x);
                fxVolume = fxVolumeSlider.getValue();
            }

            if(bindingsHitBoxes != null  && bindingsHitBoxes[0] != null){
                for(int i = 0; i < bindingsHitBoxes.length; i++){
                    Rectangle rect = bindingsHitBoxes[i];
                    if(rect.x <= x && rect.x + rect.width >= x && rect.y >= y && rect.y - rect.height <= y ){
                        selectedOption = i;
                        keybindingMode = true;
                    }
                }
            }

            return true;
        }

        @Override
        public boolean mouseMoved(int x, int screenY){
            int y = canvas.getHeight() - screenY;
            if(bindingsHitBoxes != null && bindingsHitBoxes[0] != null){
                for(int i = 0; i < bindingsHitBoxes.length; i++){
                    Rectangle rect = bindingsHitBoxes[i];
                    if(rect.x <= x && rect.x + rect.width >= x && rect.y >= y && rect.y - rect.height <= y ){
                        if(i != selectedOption) keybindingMode = false;
                        selectedOption = i;
                    }
                }
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

            if(dragging && fxVolumeSlider.inKnobBounds(startPosition.x, startPosition.y)){
                fxVolumeSlider.movedX(screenX);
                startPosition.x = screenX;
                fxVolume = fxVolumeSlider.getValue();
                SoundController.setFXVolume(fxVolume);
            }

            return true;
        }
    };

    public SettingsMode(AssetDirectory assets, GameCanvas canvas, ScreenListener listener, Map<String,Integer> keyBindings){
        super(assets, canvas, listener);
        this.canvas = canvas;
        this.listener = listener;
        this.keyBindings = keyBindings;
        musicVolume = DEFAULT_VOLUME;
        fxVolume = DEFAULT_VOLUME;
        options = keyBindings.keySet().toArray(new String[0]);
        bindingsHitBoxes = new Rectangle[keyBindings.size()];

        background = assets.getEntry( "BG-1-teal.png", Texture.class );
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();
        displayFont = assets.getEntry("retro", BitmapFont.class);

        volumeDown = assets.getEntry("volume-down", Texture.class);
        volumeUp = assets.getEntry("volume-up", Texture.class);

        keybindingMode = false;
        dragging = false;
        musicVolumeSlider = new Slider(canvas, YELLOW,0.0f, 1.0f, musicVolume, false, canvas.getWidth()/3.0f, SLIDER_HEIGHT*scale.x, KNOB_RADIUS);
        fxVolumeSlider = new Slider(canvas, YELLOW,0.0f, 1.0f, fxVolume, false, canvas.getWidth()/3.0f, SLIDER_HEIGHT*scale.x, KNOB_RADIUS);

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
                SoundController.playSound(6,1);
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
                    SoundController.playSound(6,1);
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
        canvas.drawTextCentered("SETTINGS", displayFont, height/3);

        float subtitleHeight = (5.0f/6.0f)*height - displayFont.getLineHeight();
        displayFont.getData().setScale(0.7f*scale.x);
        canvas.drawText("KEYBINDINGS", displayFont, width*0.3f - musicVolumeSlider.getWidth()/2, subtitleHeight);

        float start = subtitleHeight - 1.5f * displayFont.getLineHeight();

        displayFont.getData().setScale(0.5f*scale.x);
        displayFont.setColor(Color.ORANGE);

        int i = 0;
        for(String binding : keyBindings.keySet()){
            float textHeight = start - 1.5f*displayFont.getLineHeight()*i;
            if(i == selectedOption){
                displayFont.setColor(YELLOW);
            }
            canvas.drawText(binding, displayFont, width*0.3f - musicVolumeSlider.getWidth()/2, textHeight);
            if(i == selectedOption) displayFont.setColor(YELLOW);
            if(i == selectedOption && keybindingMode) canvas.drawText("<Enter Key>", displayFont, 0.7f*width, textHeight);
            else canvas.drawText(Input.Keys.toString(keyBindings.get(binding)), displayFont, 0.75f * width, textHeight);
            bindingsHitBoxes[i] = new Rectangle(0.75f*width, textHeight + displayFont.getLineHeight()/2f,100f*scale.x, displayFont.getLineHeight());
            displayFont.setColor(Color.ORANGE);
            i++;
        }

        start = start - 2f*displayFont.getLineHeight()*i;

        displayFont.getData().setScale(0.7f*scale.x);
        displayFont.setColor(Color.ORANGE);
        canvas.drawText("VOLUME", displayFont, width*0.3f - musicVolumeSlider.getWidth()/2, start);
        start = start-1.2f*displayFont.getLineHeight();

        displayFont.getData().setScale(0.5f*scale.x);
        canvas.drawText("MUSIC", displayFont, width*0.3f - musicVolumeSlider.getWidth()/2, start);
        canvas.drawText("SOUND EFFECTS", displayFont, width*0.7f  - fxVolumeSlider.getWidth()/2, start);

        start = start-1.5f*displayFont.getLineHeight();
        float sliderHeight = SLIDER_HEIGHT*scale.y;

        musicVolumeSlider.updatePosition(width * 0.3f, start - sliderHeight/2);
        musicVolumeSlider.updateSize(width/4f, sliderHeight, scale.x*KNOB_RADIUS);
        musicVolumeSlider.draw();


        //soundfx
        fxVolumeSlider.updatePosition(width*0.7f, start - sliderHeight/2);
        fxVolumeSlider.updateSize(width/4f, sliderHeight, scale.x*KNOB_RADIUS);
        fxVolumeSlider.draw();
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
