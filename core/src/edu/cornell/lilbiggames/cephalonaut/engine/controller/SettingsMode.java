package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;

public class SettingsMode extends MenuMode {
    /** The font for giving messages to the player */
    private BitmapFont displayFont;

    /** Listener that will move to selected level when we are done */
    private ScreenListener listener;

    /** Background texture for start-up */
    private Texture background;

    private InputController inputController;

    private String[] options = {"RESUME PLAYING", "EXIT LEVEL", "RESTART LEVEL" };

    private int selectedOption;

    private Vector2 bounds,scale;

    /** Reference to the game canvas */
    protected GameCanvas canvas;

    public SettingsMode(AssetDirectory assets, GameCanvas canvas, ScreenListener listener){
        super(assets, canvas, listener);
        this.canvas = canvas;
        this.listener = listener;
        background = assets.getEntry( "main-menu:background", Texture.class );
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();
        displayFont = assets.getEntry("retro",BitmapFont.class);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        inputController = InputController.getInstance();
        inputController.readInput(new Rectangle(), new Vector2());

        draw();
    }

    public void draw(){
        canvas.clear();
        canvas.begin();

        float height = canvas.getHeight();
        float width = canvas.getWidth();

        canvas.draw(background, 0.5f*canvas.getWidth()-canvas.getCameraX()/scale.x, 0.5f*canvas.getHeight()-canvas.getCameraY()/scale.y , 0, 0, background.getWidth(), background.getHeight(), (float)width/(float)background.getWidth()/scale.x, (float)height/(float)background.getHeight()/scale.y);

        canvas.drawTextCentered("SETTINGS", displayFont, canvas.getHeight()/4);

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
