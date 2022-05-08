package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.util.Controllers;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;
import edu.cornell.lilbiggames.cephalonaut.util.XBoxController;

import java.util.ArrayList;
import java.util.Iterator;

import static edu.cornell.lilbiggames.cephalonaut.engine.controller.MenuMode.*;

public class DialogueMode {

    /** The font for giving messages to the player */
    private BitmapFont displayFont;

    private Texture nextIcon, AD_KEY, W_KEY, SPACE_KEY, RIGHT_CLICK, LEFT_CLICK;

    private FilmStrip W_KEY_strip;

    /** Reference to the game canvas */
    protected GameCanvas canvas;

    private Vector2 scale;

    private ArrayList<ArrayList<String>> dialogue;

    /** The row index in dialogue. */
    private int part;

    /** The column index in dialogue. */
    private int index;

    private final AssetDirectory directory;

    final private int X_OFFSET = 260;
    final private int Y_OFFSET = 25;
    final private float ARROW_WIDTH = 5f;
    final private float KEY_WIDTH = 5f;

    private float frame;
    XBoxController xbox;
    private boolean prevSelect;

    /**
     * Creates a DialogueMode with the default size and position.
     *
     * @param canvas 	The game canvas to draw to
     */

    public DialogueMode(GameCanvas canvas, AssetDirectory directory){
        this.canvas  = canvas;
        this.directory = directory;
        this.scale = new Vector2(1,1);
        this.nextIcon = directory.getEntry("white-arrow", Texture.class);

        this.W_KEY = directory.getEntry("W-key", Texture.class);
        this.AD_KEY = directory.getEntry("AD-key", Texture.class);
        this.SPACE_KEY = directory.getEntry("SPACE-key", Texture.class);
        this.RIGHT_CLICK = directory.getEntry("RIGHT-click", Texture.class);
        this.LEFT_CLICK = directory.getEntry("LEFT-click", Texture.class);

        nextIcon.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        W_KEY.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        AD_KEY.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        SPACE_KEY.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        RIGHT_CLICK.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        LEFT_CLICK.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);


        W_KEY_strip = new FilmStrip(this.W_KEY, 1, 2);

        W_KEY_strip.setFrame(0);
        frame = 0;

        this.displayFont = directory.getEntry("retro", BitmapFont.class);
        Array<XBoxController> controllers = Controllers.get().getXBoxControllers();
        if (controllers.size > 0) {
            xbox = controllers.get( 0 );
        } else {
            xbox = null;
        }
    }

    public void load(String levelName, String checkpointName) {
        JsonValue dialogueDirectory = directory.getEntry("dialogue", JsonValue.class);
        dialogue = null;
        part = -1;
        index = 0;
        try {
            JsonValue level = dialogueDirectory.get(levelName + ":" + checkpointName);
            dialogue = new ArrayList<>();
            Iterator<JsonValue> part_itr = level.iterator();
            // get each dialogue part in a single level
            while(part_itr.hasNext()) {
                // get part
                JsonValue part = part_itr.next();
                Iterator<JsonValue> itr = part.iterator();
                ArrayList<String> part_dialogue = new ArrayList<>();
                while (itr.hasNext()) {
                    String s = itr.next().toString();
                    part_dialogue.add(s);
                }
                dialogue.add(part_dialogue);
            }
        } catch (Exception e) {
            //System.out.println("Failed to load dialogue. Check to see there is a dialogue for " + levelName + ":" + checkpointName + ":::" + e);
        }
    }

    public boolean update(float dt){
        frame += 2.5 * dt;
        if (frame >= 1) {
            frame--;
            W_KEY_strip.setFrame((W_KEY_strip.getFrame() + 1) % W_KEY_strip.getSize());
        }

        InputController input = InputController.getInstance();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) ||
                Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) ||
                Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT) ||
                (xbox != null && xbox.isConnected() && xbox.getA() && prevSelect != xbox.getA())) {
            index+=1;
            if(xbox != null && xbox.isConnected()) {
                prevSelect = xbox.getA();
            }
            SoundController.playSound(6,1);
            if(index >= dialogue.get(part).size()) {
                return false;
            }
        }
        if(xbox != null && xbox.isConnected()) {
            prevSelect = xbox.getA();
        }
        return true;


    }

    public void nextDialogue(int part) {
        this.part = part;
        index = 0;
        if(part >= dialogue.size()) {
            throw new RuntimeException("Dialogue does not exist.");
        }
    }

    private void drawVisual(Texture texture, float cx, float cy) {
        float Y_OFFSET = 395f;
        float X_OFFSET = 410f;
        canvas.draw(texture, Color.WHITE,
                texture.getWidth() / 2f, texture.getHeight() / 2f,
                cx - canvas.getWidth() / 2 + X_OFFSET * scale.x, cy - Y_OFFSET * scale.y,
                0, scale.x * KEY_WIDTH, scale.y * KEY_WIDTH);
    }

    private void drawVisual(FilmStrip filmStrip, float cx, float cy){
        float Y_OFFSET = 395f;
        float X_OFFSET = 410f;
        canvas.draw(filmStrip, Color.WHITE,
                filmStrip.getFwidth() / 2f, filmStrip.getFheight() / 2f,
                cx - canvas.getWidth() / 2 + X_OFFSET * scale.x, cy - Y_OFFSET * scale.y,
                0, scale.x * KEY_WIDTH, scale.y * KEY_WIDTH);
    }

    private String displayVisual(String text, float cx, float cy) {
        String visualText = text.substring(0, text.indexOf(' '));
        if(visualText.equals("[W]")) {
//            drawVisual(W_KEY, cx, cy);
            drawVisual(W_KEY_strip, cx, cy);
            return text.substring(text.indexOf(' ') + 1);
        }
        else if(visualText.equals("[AD]")) {
            drawVisual(AD_KEY, cx, cy);
            return text.substring(text.indexOf(' ') + 1);
        }
        else if(visualText.equals("[SPACE]")) {
            drawVisual(SPACE_KEY, cx, cy);
            return text.substring(text.indexOf(' ') + 1);
        }
        else if(visualText.equals("[LEFT_CLICK]")) {
            drawVisual(LEFT_CLICK, cx, cy);
            return text.substring(text.indexOf(' ') + 1);
        }
        else if(visualText.equals("[RIGHT_CLICK]")) {
            drawVisual(RIGHT_CLICK, cx, cy);
            return text.substring(text.indexOf(' ') + 1);
        }
        return text;
    }

    private void displayText(float cx, float cy) {
        displayFont.getData().setScale(.5f * scale.x);
        int cutoff = 40;
        float x_offset = 0.006f;
        int y_offset = (int)(310f*Gdx.graphics.getHeight()/1080f);

        String text = dialogue.get(part).get(index);
        text = displayVisual(text, cx, cy);

        int text_length = text.length();

        while(text_length / cutoff > 0) {
            cutoff = text.substring(0, cutoff + 1).lastIndexOf(' ');
            float text_pos_x = cx - canvas.getWidth() * x_offset * cutoff;
            canvas.drawText(text.substring(0, cutoff), displayFont, text_pos_x, cy - y_offset);
            y_offset += (int)(50*Gdx.graphics.getHeight()/720f);
            text = text.substring(cutoff);
            text_length = text.length();
        }

        float text_pos_x = cx - canvas.getWidth() * x_offset * text_length;
        canvas.drawText(text, displayFont, text_pos_x, cy - y_offset);
    }

    public void draw(float cx, float cy, float fade) {
        scale.x = Gdx.graphics.getWidth()/1920f;
        scale.y = Gdx.graphics.getHeight()/1080f;

        canvas.drawDialogueBox(fade);
        displayText(cx, cy);

        canvas.draw(nextIcon, Color.WHITE,
                nextIcon.getWidth(), nextIcon.getHeight() / 2f,
                cx + canvas.getWidth() / 2f - X_OFFSET*scale.x, cy - canvas.getHeight() / 2f + Y_OFFSET*scale.y,
                (float)Math.PI, scale.x * ARROW_WIDTH, scale.y * ARROW_WIDTH);
    }
}

