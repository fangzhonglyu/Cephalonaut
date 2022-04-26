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
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;

import java.util.ArrayList;
import java.util.Iterator;

import static edu.cornell.lilbiggames.cephalonaut.engine.controller.MenuMode.*;

public class DialogueMode {

    /** The font for giving messages to the player */
    private BitmapFont displayFont;

    private Texture nextIcon;

    /** Reference to the game canvas */
    protected GameCanvas canvas;

    private InputController inputController;

    private Vector2 scale;

    private ArrayList<ArrayList<String>> dialogue;

    /** The row index in dialogue. */
    private int part;

    /** The column index in dialogue. */
    private int index;

    private final AssetDirectory directory;

    final private int X_OFFSET = 280;
    final private int Y_OFFSET = 50;
    final private float ARROW_WIDTH = 7.5f;


    /**
     * Creates a DialogueMode with the default size and position.
     *
     * @param canvas 	The game canvas to draw to
     */

    public DialogueMode(GameCanvas canvas, AssetDirectory directory){
        this.canvas  = canvas;
        this.directory = directory;
        this.scale = new Vector2(1,1);
        this.nextIcon = directory.getEntry("arrow", Texture.class);
        this.displayFont = directory.getEntry("retro", BitmapFont.class);
        this.inputController = InputController.getInstance();
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
            System.out.println("Failed to load dialogue. Check to see there is a dialogue for " + levelName + ":" + checkpointName + ":::" + e);
        }
    }

    public boolean update(){
        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) ||
                Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) ||
                Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {

            index+=1;
            if(index >= dialogue.get(part).size()) {
                return false;
            }
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

    private void displayText(float cx, float cy) {
        displayFont.getData().setScale(.5f * scale.x);
        int cutoff = 40;
        float x_offset = 0.006f;
        int y_offset = (int)(310f*Gdx.graphics.getHeight()/1080f);

        String text = dialogue.get(part).get(index);
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
                (float)Math.PI / -2f, scale.x * ARROW_WIDTH, scale.y * ARROW_WIDTH);
    }
}

