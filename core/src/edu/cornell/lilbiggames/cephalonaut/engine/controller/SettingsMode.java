package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
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

    public SettingsMode(AssetDirectory assets, GameCanvas canvas, ScreenListener listener, Map<String,Integer> keyBindings){
        super(assets, canvas, listener);
        this.canvas = canvas;
        this.listener = listener;
        this.keyBindings = keyBindings;
        options = keyBindings.keySet().toArray(new String[0]);

        background = assets.getEntry( "main-menu:background", Texture.class );
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();
        displayFont = assets.getEntry("retro",BitmapFont.class);

        volumeDown = assets.getEntry("volume-down", Texture.class);
        volumeUp = assets.getEntry("volume-up", Texture.class);
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
        } else {
            if(inputController.getCurrentKey() != Input.Keys.ANY_KEY) {
                keyBindings.put(options[selectedOption], inputController.getCurrentKey());
            }
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

        float sliderHeight = SLIDER_HEIGHT * scale.x;

        canvas.drawSlider(YELLOW,0.0f, 1.0f, 0.5f, width/2, start - 2f*displayFont.getLineHeight(), width/3.0f, sliderHeight);
        float halfSliderWidth = width/3.0f * 0.5f;

        // draw volume icons
        canvas.draw(volumeDown, Color.WHITE, 0, sliderHeight/2, width/2 - halfSliderWidth - 3*sliderHeight, start - 2f*displayFont.getLineHeight(), 2*sliderHeight, 2*sliderHeight);
        canvas.draw(volumeUp, Color.WHITE, 0, sliderHeight/2, width/2 + halfSliderWidth + 2*sliderHeight, start - 2f*displayFont.getLineHeight(), 2*sliderHeight, 2*sliderHeight);

        canvas.end();
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
