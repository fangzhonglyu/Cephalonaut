package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;

import static edu.cornell.lilbiggames.cephalonaut.engine.controller.MenuMode.*;

public class StartScreenMode extends MenuMode {

    /** The font for giving messages to the player */
    private BitmapFont displayFont;

    /** Listener that will move to selected level when we are done */
    private ScreenListener listener;

    /** Background texture for start-up */
    private Texture background;

    private InputController inputController;

    private String[] options = {"START", "OPTIONS", "CREDITS" };

    private int optionId;

    private Vector2 bounds,scale;

    /** Reference to the game canvas */
    protected GameCanvas canvas;

    public StartScreenMode(AssetDirectory assets, GameCanvas canvas, ScreenListener listener){
        super(assets, canvas, listener);
        this.canvas = canvas;
        this.listener = listener;
        background = assets.getEntry( "main-menu:background", Texture.class );
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        
        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();
        displayFont = assets.getEntry("retro",BitmapFont.class);
        optionId = 0; //default is resume
    }

    public void setDefault(){
        optionId = 0;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        inputController = InputController.getInstance();
        inputController.readInput(new Rectangle(), new Vector2());

        if(optionId == 0 && inputController.isSelectPressed()){
            listener.exitScreen(this, START_CODE);
        } else if(optionId == 1 && inputController.isSelectPressed()){
            listener.exitScreen(this, OPTIONS_CODE);
        } else if(optionId == 2 && inputController.isSelectPressed()){
            listener.exitScreen(this, CREDITS_CODE);
        } else if(inputController.isUpPressed()){
            optionId = optionId == 0 ? options.length-1 : optionId-1;
        } else if(inputController.isDownPressed()){
            optionId = (optionId+1)%options.length;
        }

        draw();
    }

    public void draw(){
        canvas.clear();
        canvas.begin();

        float height = canvas.getHeight();
        float width = canvas.getWidth();
        canvas.draw(background, 0.5f*canvas.getWidth()-canvas.getCameraX()/scale.x, 0.5f*canvas.getHeight()-canvas.getCameraY()/scale.y , 0, 0, background.getWidth(), background.getHeight(), (float)width/(float)background.getWidth()/scale.x, (float)height/(float)background.getHeight()/scale.y);

        drawOptions();

        canvas.end();
    }

    private void drawOptions(){
        float start = (options.length*0.5f*displayFont.getLineHeight())/2;
        displayFont.getData().setScale(1.0f);
        displayFont.setColor(Color.ORANGE);
        canvas.drawTextCentered("CEPHALONAUT", displayFont, start);
        displayFont.setColor(Color.WHITE);
        displayFont.getData().setScale(0.5f);

        for(int i = 0; i < options.length; i++){
            if(optionId == i) displayFont.setColor(Color.CYAN);
            canvas.drawTextCentered(options[i], displayFont, start - displayFont.getLineHeight()*i - 2*displayFont.getLineHeight());
            if(optionId == i) displayFont.setColor(Color.WHITE);
        }
        displayFont.getData().setScale(1.0f);
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
