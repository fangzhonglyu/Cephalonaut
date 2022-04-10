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

    private int currentKey;
    private Vector2 startPosition;
    private boolean dragging;

    private final float DEFAULT_VOLUME = 0.5f;
    private float musicVolume;

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
        displayFont = assets.getEntry("retro",BitmapFont.class);

        volumeDown = assets.getEntry("volume-down", Texture.class);
        volumeUp = assets.getEntry("volume-up", Texture.class);

        dragging = false;
        musicVolumeSlider = new Slider(canvas, YELLOW,0.0f, 1.0f, musicVolume, false, canvas.getWidth()/3.0f, SLIDER_HEIGHT*scale.x, 20.0f);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int i) {
                currentKey = Gdx.input.isKeyJustPressed(currentKey) ? currentKey : Input.Keys.ANY_KEY;;
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
        });
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
        selectedOption = 0;
    }

    @Override
    public void render(float delta) {
        inputController = InputController.getInstance();
        inputController.readInput(new Rectangle(), new Vector2());

        if(inputController.isUpPressed()){
            selectedOption = selectedOption == 0 ? options.length-1 : selectedOption -1;
        } else if(inputController.isDownPressed()){
            selectedOption = (selectedOption +1)%options.length;
        } else if(inputController.didExit()){
            listener.exitScreen(this, RETURN_TO_START_CODE);
        }

        draw();
    }

    public void draw(){
        canvas.clear();
        canvas.begin();

        float height = canvas.getHeight();
        float width = canvas.getWidth();

        canvas.draw(background, 0.5f*width-canvas.getCameraX()/scale.x, 0.5f*height-canvas.getCameraY()/scale.y , 0, 0, background.getWidth(), background.getHeight(), (float)width/(float)background.getWidth()/scale.x, (float)height/(float)background.getHeight()/scale.y);

        displayFont.getData().setScale(scale.x);
        displayFont.setColor(Color.ORANGE);
        canvas.drawTextCentered("SETTINGS", displayFont, height/4);

        float subtitleHeight = (3.0f/4.0f)*height - displayFont.getLineHeight();
        displayFont.getData().setScale(0.6f*scale.x);
        displayFont.setColor(YELLOW);
        canvas.drawText("KEYBINDINGS", displayFont, width/4, subtitleHeight);

        float start = subtitleHeight - 1.5f * displayFont.getLineHeight();

        displayFont.getData().setScale(0.4f*scale.x);

        int i = 0;
        for(String binding : keyBindings.keySet()){
            float textHeight = start - 2*displayFont.getLineHeight()*i;
            canvas.drawText(binding, displayFont, width/4, textHeight);

            if(i == selectedOption){
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
                displayFont.setColor(Color.CYAN);
            }
            canvas.drawText(Input.Keys.toString(keyBindings.get(binding)), displayFont, 0.75f*width, textHeight);
            displayFont.setColor(YELLOW);
            i++;
        }

        start = start - 2*displayFont.getLineHeight()*i;

        displayFont.getData().setScale(0.6f*scale.x);
        displayFont.setColor(YELLOW);
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
