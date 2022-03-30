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

public class PauseMode implements Screen {
    public static int EXIT_LEVEL_CODE = 30;
    public static int RESUME_LEVEL_CODE = 31;
    public static int RESTART_LEVEL_CODE = 32;
    /** The font for giving messages to the player */
    private BitmapFont displayFont;

    /** Listener that will move to selected level when we are done */
    private ScreenListener listener;

    /** Background texture for start-up */
    private Texture background;

    private InputController inputController;

    private String[] options = {"RESUME PLAYING", "EXIT LEVEL", "RESTART LEVEL" };

    private int optionId;

    private Vector2 bounds,scale;

    /** Reference to the game canvas */
    protected GameCanvas canvas;

    public PauseMode(AssetDirectory assets, GameCanvas canvas, ScreenListener listener){
        this.canvas = canvas;
        this.listener = listener;
        background = assets.getEntry( "main-menu:background", Texture.class );
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        float height = canvas.getHeight();
        float width = canvas.getWidth();
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
            listener.exitScreen(this, RESUME_LEVEL_CODE);
        } else if(optionId == 1 && inputController.isSelectPressed()){
            listener.exitScreen(this, EXIT_LEVEL_CODE);
        } else if(optionId == 2 && inputController.isSelectPressed()){
            listener.exitScreen(this, RESTART_LEVEL_CODE);
        } else if(inputController.didExit()){
            listener.exitScreen(this, RESUME_LEVEL_CODE);
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
        displayFont.getData().setScale(0.5f);
        float start = (options.length* displayFont.getLineHeight())/2;

        for(int i = 0; i < options.length; i++){
            if(optionId == i) displayFont.setColor(Color.CYAN);
            canvas.drawTextCentered(options[i], displayFont, start - displayFont.getLineHeight()*i);
            if(optionId == i) displayFont.setColor(Color.WHITE);
        }
        displayFont.getData().setScale(1.0f);
    }

    @Override
    public void resize(int width, int height) {
        scale.x = canvas.getWidth()/bounds.x;
        scale.y = canvas.getHeight()/bounds.y;
        canvas.setCameraPos(0.5f*width,0.5f*height);
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
