package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.engine.ui.Slider;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;

import java.util.Map;

public class SettingsMode extends MenuMode {
    /** The font for giving messages to the player */
    private BitmapFont displayFont;

    /** Listener that will move to selected level when we are done */
    private ScreenListener listener;

    /** Background texture for start-up */
    private Texture background;

    private InputController inputController;

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

            if(fxVolumeSlider.inKnobBounds(startPosition.x, startPosition.y)){
                fxVolumeSlider.movedX(startPosition.x);
                fxVolume = fxVolumeSlider.getValue();
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
        this.musicVolume = DEFAULT_VOLUME;
        options = keyBindings.keySet().toArray(new String[0]);

        background = assets.getEntry( "main-menu:background", Texture.class );
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();
        displayFont = assets.getEntry("retro", BitmapFont.class);

        volumeDown = assets.getEntry("volume-down", Texture.class);
        volumeUp = assets.getEntry("volume-up", Texture.class);

        keybindingMode = false;
        dragging = false;
        musicVolumeSlider = new Slider(canvas, Color.PURPLE,0.0f, 1.0f, musicVolume, false, canvas.getWidth()/3.0f, SLIDER_HEIGHT*scale.x, 20.0f);
        fxVolumeSlider = new Slider(canvas, Color.PURPLE,0.0f, 1.0f, fxVolume, false, canvas.getWidth()/3.0f, SLIDER_HEIGHT*scale.x, 20.0f);

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
        inputController = InputController.getInstance();
        inputController.readInput(new Rectangle(), new Vector2());

        if(keybindingMode) {
            if(Gdx.input.isKeyJustPressed(currentKey)) {
                keyBindings.put(options[selectedOption], currentKey);
                keybindingMode = false;
            }
        } else {
            if (inputController.isUpPressed()) {
                selectedOption = selectedOption == 0 ? options.length - 1 : selectedOption - 1;
            } else if (inputController.isDownPressed()) {
                selectedOption = (selectedOption + 1) % options.length;
            } else if (inputController.didExit() || inputController.isBackPressed()) {
                listener.exitScreen(this, RETURN_TO_START_CODE);
            } else {
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    keybindingMode = true;

                    //keyBindings.put(options[selectedOption], currentKey);
                }
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
        canvas.drawText("KEYBINDINGS", displayFont, width/4, subtitleHeight);

        float start = subtitleHeight - 1.5f * displayFont.getLineHeight();

        displayFont.getData().setScale(0.5f*scale.x);
        displayFont.setColor(Color.ORANGE);

        int i = 0;
        for(String binding : keyBindings.keySet()){
            float textHeight = start - 1.5f*displayFont.getLineHeight()*i;
            if(i == selectedOption){
                displayFont.setColor(YELLOW);
            }
            canvas.drawText(binding, displayFont, width/4, textHeight);
            if(i == selectedOption) displayFont.setColor(Color.PURPLE);
            if(i == selectedOption && keybindingMode) canvas.drawText("<Enter Key>", displayFont, 0.75f*width, textHeight);
            else canvas.drawText(Input.Keys.toString(keyBindings.get(binding)), displayFont, 0.75f * width, textHeight);
            displayFont.setColor(Color.ORANGE);
            i++;
        }

        start = start - 1.5f*displayFont.getLineHeight()*i;

        displayFont.getData().setScale(0.7f*scale.x);
        displayFont.setColor(Color.ORANGE);
        canvas.drawText("VOLUME", displayFont, width/4, start);

        float sliderHeight = SLIDER_HEIGHT*scale.x;

        start = start-1.5f*displayFont.getLineHeight();

        // music volume slider
        displayFont.getData().setScale(0.5f*scale.x);
        start += 10;
        canvas.drawText("MUSIC", displayFont, width/4, start);
        start -= 0.5f*displayFont.getLineHeight();

        musicVolumeSlider.updatePosition(width * 0.75f, start);
        musicVolumeSlider.updateSize(width/4f, sliderHeight);
        musicVolumeSlider.draw();

        float halfSliderWidth = width/4f * 0.5f;

        // draw volume icons
        canvas.draw(volumeDown, Color.WHITE, 0, sliderHeight/2, width*0.75f - halfSliderWidth - 2*sliderHeight - 10, start, 2*sliderHeight, 2*sliderHeight);
        canvas.draw(volumeUp, Color.WHITE, 0, sliderHeight/2, width*0.75f + halfSliderWidth + 10, start, 2*sliderHeight, 2*sliderHeight);

        canvas.drawText("SOUND EFFECTS", displayFont, width/4, start - sliderHeight - 40);
        start -= 0.5f*displayFont.getLineHeight();

        //soundfx
        fxVolumeSlider.updatePosition(width*0.75f, start - sliderHeight - 40);
        fxVolumeSlider.updateSize(width/4f, sliderHeight);
        fxVolumeSlider.draw();

        // draw volume icons
        canvas.draw(volumeDown, Color.WHITE, 0, sliderHeight/2, width*0.75f - halfSliderWidth - 2*sliderHeight -10, start  - sliderHeight - 40, 2*sliderHeight, 2*sliderHeight);
        canvas.draw(volumeUp, Color.WHITE, 0, sliderHeight/2, width*0.75f + halfSliderWidth + 10, start  - sliderHeight - 40, 2*sliderHeight, 2*sliderHeight);


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
