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

    final private int X_OFFSET = 250;
    final private int Y_OFFSET = 100;


    /**
     * Creates a DialogueMode with the default size and position.
     *
     * @param canvas 	The game canvas to draw to
     */

    public DialogueMode(GameCanvas canvas, AssetDirectory directory){
        this.canvas  = canvas;
        this.directory = directory;
        this.scale = new Vector2(1,1);
        this.nextIcon = directory.getEntry("nexticon", Texture.class);
        this.displayFont = directory.getEntry("retro", BitmapFont.class);
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
            while(part_itr.hasNext()) {
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
        inputController = InputController.getInstance();
        inputController.readInput(new Rectangle(), new Vector2());
        if (clickedBack()) {
            index = index > 0 ? index - 1 : 0;
        } else if (clickedNext() || inputController.isNextPressed()) {
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

    public void nextDialogue() {
        part += 1;
        index = 0;
        if(part >= dialogue.size()) {
            throw new RuntimeException("Dialogue does not exist.");
        }
    }

    private boolean clickedBack() {
        return checkClicked(X_OFFSET, canvas.getHeight() - Y_OFFSET);
    }

    private boolean clickedNext() {
        return checkClicked(canvas.getWidth() - X_OFFSET, canvas.getHeight() - Y_OFFSET);
    }

    private boolean checkClicked(float posX, float posY) {
        float mouseX = -10;
        float mouseY = -10;
        float rHeight = nextIcon.getHeight();
        float rWidth = nextIcon.getWidth();

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            mouseX = Gdx.input.getX();
            mouseY = Gdx.input.getY();
        }

        if (posX - rWidth / 2f <= mouseX && mouseX <= posX + rWidth / 2f
                && posY - rHeight / 2f <= mouseY && mouseY <= posY + rHeight / 2f ) {
            return true;
        }
        return false;
    }

    public void draw(float cx, float cy, float fade) {

        canvas.drawDialogueBox(fade);
        displayFont.getData().setScale(.5f * scale.x);
        float text_pos_x = cx - canvas.getWidth() * .008f * dialogue.get(part).get(index).length();
        //cx - canvas.getWidth() * .33f
        canvas.drawText(dialogue.get(part).get(index), displayFont, text_pos_x, cy - 200);

        canvas.draw(nextIcon, Color.WHITE,
                nextIcon.getWidth() / 2f, nextIcon.getHeight() / 2f,
                cx + canvas.getWidth() / 2 - X_OFFSET, cy - canvas.getHeight() / 2 + Y_OFFSET,
                0, scale.x, scale.y);

        canvas.draw(nextIcon, Color.WHITE,
                nextIcon.getWidth() / 2f, nextIcon.getHeight() / 2f,
                cx - canvas.getWidth() / 2 + X_OFFSET, cy - canvas.getHeight() / 2 + Y_OFFSET,
                3.14f, scale.x, scale.y);
    }
}

