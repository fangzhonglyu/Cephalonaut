package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;

public class MenuMode implements Screen {
    public static final int LEVEL_SELECTED_CODE = 21;
    public static int EXIT_LEVEL_CODE = 30;
    public static int RESUME_LEVEL_CODE = 31;
    public static int RESTART_LEVEL_CODE = 32;
    public static int RETURN_TO_START_CODE = 33;
    public static int START_CODE = 40;
    public static int OPTIONS_CODE = 41;
    public static int CREDITS_CODE = 42;
    public static final int CHECKPOINT_SELECTED_CODE = 51;
    public static final int NESTED_MENU_EXIT_CODE = 52;
    public static final int NEXT_LEVEL_CODE = 53;
    public static final int EXIT_LOADING_CODE = 61;
    public static final int DONE_LOADING_ASSETS = 71;
    public static final int EXIT_SETTINGS_CODE = 81;
    public static final int GO_BACK_CODE = 91;

    protected final Color YELLOW = new Color(255.0f/256.0f, 232.0f/256.0f, 132.0f/256.0f, 1.0f);

    /** The font for giving messages to the player */
    private BitmapFont displayFont;

    /** Listener that will move to selected level when we are done */
    private ScreenListener listener;

    /** Background texture for start-up */
    private Texture background;

    private Texture settingsIcon;
    protected Rectangle settingsIconHitbox;
    protected boolean goToSettings;

    private Texture backIcon;
    protected Rectangle backIconHitbox;
    protected boolean goBack;

    /** Reference to the game canvas */
    protected GameCanvas canvas;

    private AssetDirectory assets;

    protected Vector2 bounds,scale;

    private Rectangle[] optionsHitBoxes;

    protected int selectedOption;

    protected InputAdapter menuInput = new InputAdapter() {
        public boolean mouseMoved (int x, int screenY) {
            float y = canvas.getHeight() - screenY;

            if(optionsHitBoxes != null){
                for(int i = 0; i < optionsHitBoxes.length; i++){
                    Rectangle hitBox = optionsHitBoxes[i];
                    if(hitBox.x <= x && hitBox.x + hitBox.width >= x && hitBox.y <= y && hitBox.y + hitBox.height >= y ){
                        if(i!=selectedOption)
                            SoundController.playSound(4,1);
                        selectedOption = i;
                        optionSelected(i);
                    }
                }
            }
            return true;
        }

        public boolean touchDown (int x, int screenY, int pointer, int button) {
            float y = canvas.getHeight() - screenY;
            if (optionsHitBoxes != null){
                for (int i = 0; i < optionsHitBoxes.length; i++){
                    Rectangle hitBox = optionsHitBoxes[i];
                    if (hitBox.x <= x && hitBox.x + hitBox.width >= x && hitBox.y <= y && hitBox.y + hitBox.height >= y ){
                        selectedOption = i;
                        SoundController.playSound(6,1);
                        exitScreen();
                    }
                }
            }

            if(settingsIconHitbox != null){
                if(settingsIconHitbox.x <= x && settingsIconHitbox.x + settingsIconHitbox.width >= x && settingsIconHitbox.y >= y && settingsIconHitbox.y - settingsIconHitbox.height <= y ){
                    goToSettings = true;
                    SoundController.playSound(6,1);
                    exitScreen();
                }
            }

            if(backIconHitbox != null){
                if(backIconHitbox.x <= x && backIconHitbox.x + backIconHitbox.width >= x && backIconHitbox.y >= y && backIconHitbox.y - backIconHitbox.height <= y ){
                    goBack = true;
                    SoundController.playSound(6,1);
                    exitScreen();
                }
            }
            return true;
        }
    };

    public void exitScreen(){

    }

    public void optionSelected(int i){

    }

    /**
     * Creates a MainMenuMode with the default size and position.
     *
     * @param assets    The asset directory to use
     * @param canvas 	The game canvas to draw to
     */
    public MenuMode(AssetDirectory assets, GameCanvas canvas, ScreenListener listener){
        this.canvas  = canvas;
        this.listener = listener;
        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();
        displayFont = assets.getEntry("retro", BitmapFont.class);

        background = assets.getEntry( "BG-1-teal.png", Texture.class);
        background.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        this.assets = assets;

        settingsIcon = assets.getEntry("settings-icon", Texture.class);
        backIcon =  assets.getEntry("arrowLeft", Texture.class);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        canvas.begin();
        canvas.draw(settingsIcon, 20, canvas.getHeight()*0.9f, 100, 100, 100, 100, 100, 100);
        canvas.end();
    }

    @Override
    public void resize(int width, int height) {
        float scaleMin = Math.min(canvas.getWidth()/bounds.x, canvas.getHeight()/bounds.y);
        scale.x = scaleMin;
        scale.y = scaleMin;
        canvas.setCameraPos(0.5f*width,0.5f*height);
    }

    private void update(float delta){

    }

    protected void drawOptions(String[] options, int selectedOption) {
        drawOptions(options, selectedOption, 0);
    }

    protected void drawOptions(String[] options, int selectedOption, int offset){
        optionsHitBoxes = new Rectangle[options.length];

        float start = (options.length*displayFont.getLineHeight())/2 - offset;
        displayFont.setColor(Color.ORANGE);
        displayFont.getData().setScale(0.7f*scale.x);

        for(int i = 0; i < options.length; i++){
            if(selectedOption == i) {
                displayFont.setColor(YELLOW);
            }
            canvas.drawTextCentered(options[i], displayFont, start - 1.2f*displayFont.getLineHeight()*i - 2*displayFont.getLineHeight());
            displayFont.setColor(Color.ORANGE);
            GlyphLayout layout = new GlyphLayout(displayFont, options[i]);
            float x = (canvas.getWidth()  - layout.width) / 2.0f;
            optionsHitBoxes[i] = new Rectangle(x,canvas.getHeight() / 2f + start - 1.2f*displayFont.getLineHeight() * i - 2 * displayFont.getLineHeight() - .6f*displayFont.getLineHeight(), 1.2f*layout.width, 1.2f*displayFont.getLineHeight());

        }
        displayFont.setColor(Color.WHITE);
        displayFont.getData().setScale(scale.x);
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

    public void draw(){

    }

    public void drawGoBack(){
        canvas.draw(backIcon, Color.WHITE,
                backIcon.getWidth() / 2f, backIcon.getHeight() / 2f,
                canvas.getWidth()*0.05f, canvas.getHeight()*0.9f,
                0, scale.x*0.03f, scale.y*0.03f);

        backIconHitbox = new Rectangle(canvas.getWidth()*0.05f-scale.x*backIcon.getWidth()/2*0.03f,canvas.getHeight()*0.9f+scale.y*backIcon.getHeight()/2*0.03f,scale.x*backIcon.getWidth()*0.03f,scale.y*backIcon.getHeight()*0.03f);
    }

    public void drawBackSettings(){
        canvas.draw(backIcon, Color.WHITE,
                backIcon.getWidth() / 2f, backIcon.getHeight() / 2f,
                canvas.getWidth()*0.05f, canvas.getHeight()*0.9f,
                0, scale.x*0.03f, scale.y*0.03f);

        float settingsX = canvas.getWidth()*0.03f + 0.2f*scale.x*0.03f*backIcon.getWidth() + scale.x*settingsIcon.getWidth()/2*0.1f + 100*scale.x;
        canvas.draw(settingsIcon, Color.WHITE,
                settingsIcon.getWidth() / 2f, settingsIcon.getHeight() / 2f,
                settingsX, canvas.getHeight()*0.9f,
                0, scale.x*0.1f, scale.y*0.1f);

        backIconHitbox = new Rectangle(canvas.getWidth()*0.05f-scale.x*backIcon.getWidth()/2*0.03f,canvas.getHeight()*0.9f+scale.y*backIcon.getHeight()/2*0.03f,scale.x*backIcon.getWidth()*0.03f,scale.y*backIcon.getHeight()*0.03f);
        settingsIconHitbox = new Rectangle(settingsX-scale.x*settingsIcon.getWidth()/2*0.1f,canvas.getHeight()*0.9f+scale.y*settingsIcon.getHeight()/2*0.1f,scale.x*settingsIcon.getWidth()*0.1f,scale.y*settingsIcon.getHeight()*0.1f);
    }

    public void drawGoToSettings(){
        canvas.draw(settingsIcon, Color.WHITE,
                settingsIcon.getWidth() / 2f, settingsIcon.getHeight() / 2f,
                canvas.getWidth()*0.1f, canvas.getHeight()*0.9f,
                0, scale.x*0.1f, scale.y*0.1f);

        settingsIconHitbox = new Rectangle(canvas.getWidth()*0.1f-scale.x*settingsIcon.getWidth()/2*0.1f,canvas.getHeight()*0.9f+scale.y*settingsIcon.getHeight()/2*0.1f,scale.x*settingsIcon.getWidth()*0.1f,scale.y*settingsIcon.getHeight()*0.1f);
    }

    public void setDefault(){
        float x = Gdx.input.getX();
        float y = canvas.getHeight() - Gdx.input.getY();

        if(optionsHitBoxes != null){
            for(int i = 0; i < optionsHitBoxes.length; i++){
                Rectangle hitBox = optionsHitBoxes[i];
                if(hitBox.x <= x && hitBox.x + hitBox.width >= x
                        && hitBox.y <= y && hitBox.y + hitBox.height >= y ){
                    selectedOption = i;
                    optionSelected(i);
                }
            }
        }
    }
}
