package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;

public class StartScreenMode extends MenuMode {

    /** The font for giving messages to the player */
    private BitmapFont displayFont;

    /** Listener that will move to selected level when we are done */
    private ScreenListener listener;

    /** Background texture for start-up */
    private Texture background;
    private Texture banner;

    private InputController inputController;

    private String[] options = {"START", "OPTIONS", "CREDITS" };

    private int selectedOption;

    /** Reference to the game canvas */
    protected GameCanvas canvas;

    public StartScreenMode(AssetDirectory assets, GameCanvas canvas, ScreenListener listener){
        super(assets, canvas, listener);
        this.canvas = canvas;
        this.listener = listener;
        background = assets.getEntry( "main-menu:background", Texture.class );
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        banner = assets.getEntry( "banner", Texture.class );

        
        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();
        displayFont = assets.getEntry("retro", BitmapFont.class);
        selectedOption = 0; //default is resume


    }

    public void setDefault(){
        super.setDefault();
        Gdx.input.setInputProcessor(menuInput); selectedOption = 0;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            exitScreen();
        } else if(Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)){
            selectedOption = selectedOption == 0 ? options.length-1 : selectedOption-1;
        } else if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.A)){
            selectedOption = (selectedOption+1)%options.length;
        }

        draw();
    }

    public void draw(){
        canvas.clear();
        canvas.begin();

        float height = canvas.getHeight();
        float width = canvas.getWidth();
        canvas.draw(background, 0.5f*canvas.getWidth()-canvas.getCameraX()/scale.x, 0.5f*canvas.getHeight()-canvas.getCameraY()/scale.y , 0, 0, background.getWidth(), background.getHeight(), (float)width/(float)background.getWidth()/scale.x, (float)height/(float)background.getHeight()/scale.y);

        displayFont.getData().setScale(scale.x);
        displayFont.setColor(Color.ORANGE);
        float start = (options.length*displayFont.getLineHeight())/2;
        canvas.draw(banner, width/2 - scale.x*0.5f*banner.getWidth()/2, height/2+start, 0,0, banner.getWidth(), banner.getHeight(), scale.x*0.5f, scale.y*0.5f);
        super.drawOptions(options, selectedOption);

        canvas.end();
    }

    @Override
    public void exitScreen(){
        if (selectedOption == 0) {
            listener.exitScreen(this, START_CODE);
        } else if (selectedOption == 1) {
            listener.exitScreen(this, OPTIONS_CODE);
        } else if (selectedOption == 2) {
            listener.exitScreen(this, CREDITS_CODE);
        }
    }


    @Override
    public void optionSelected(int i){
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
