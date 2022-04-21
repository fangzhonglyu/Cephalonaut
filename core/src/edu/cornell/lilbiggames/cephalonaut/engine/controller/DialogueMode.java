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
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;

import static edu.cornell.lilbiggames.cephalonaut.engine.controller.MenuMode.*;

public class DialogueMode {

    /** The font for giving messages to the player */
    private BitmapFont displayFont;

    private Texture nextIcon;

    /** Reference to the game canvas */
    protected GameCanvas canvas;

    private InputController inputController;

    private Vector2 scale;

    private String[][] dialogue;

    /** The row index in dialogue. */
    private int part;

    /** The column index in dialogue. */
    private int index;


    /**
     * Creates a DialogueMode with the default size and position.
     *
     * @param canvas 	The game canvas to draw to
     */
    public DialogueMode(BitmapFont displayFont, Texture nextIcon, GameCanvas canvas, String[][] dialogue){
        this.canvas  = canvas;
        this.scale = new Vector2(1,1);
        this.displayFont = displayFont;
        this.nextIcon = nextIcon;
        this.dialogue = dialogue;
        this.part = 0;
        this.index = 0;

        this.displayFont.getData().setScale(.33f);
    }

    public boolean update(float delta){
        inputController = InputController.getInstance();
        inputController.readInput(new Rectangle(), new Vector2());
        if (clickedBack()) {
            index = index > 0 ? index - 1 : 0;
        } else if (clickedNext() || inputController.isNextPressed()) {
            index+=1;
            if(index >= dialogue[part].length) {
                return false;
            }
        }
        return true;
    }

    public void nextDialogue(int part) {
        this.part = part;
        index = 0;
        if(part >= dialogue.length) {
            throw new RuntimeException("Dialogue does not exists.");
        }
    }

    public void nextDialogue() {
        part += 1;
        index = 0;
        if(part >= dialogue.length) {
            throw new RuntimeException("Dialogue does not exists.");
        }
    }

    private boolean clickedBack() {
        float mouseX = -10;
        float mouseY = -10;
        float rHeight = nextIcon.getHeight();
        float rWidth = nextIcon.getWidth();
        float posX = canvas.getWidth();
        float posY = canvas.getHeight();

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            mouseX = Gdx.input.getX();
            mouseY = Gdx.input.getY();
            posX = 100;
            posY = posY - 100;
            System.out.println(posX + ":" + posY + ":" + mouseX + ":" + mouseY);
        }

        if (posX - rWidth / 2f <= mouseX && mouseX <= posX + rWidth / 2f
                && posY - rHeight / 2f <= mouseY && mouseY <= posY + rHeight / 2f ) {
            return true;
        }

        return false;
    }

    private boolean clickedNext() {
        float mouseX = -10;
        float mouseY = -10;
        float rHeight = nextIcon.getHeight();
        float rWidth = nextIcon.getWidth();
        float posX = canvas.getWidth();
        float posY = canvas.getHeight();

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            mouseX = Gdx.input.getX();
            mouseY = Gdx.input.getY();
            posX = posX - 100;
            posY = posY - 100;
        }

        if (posX - rWidth / 2f <= mouseX && mouseX <= posX + rWidth / 2f
                && posY - rHeight / 2f <= mouseY && mouseY <= posY + rHeight / 2f ) {
            return true;
        }

        return false;
    }

    public void draw(float cx, float cy) {


        canvas.drawDialogueBox();
        //canvas.drawText(dialogue[part][index], displayFont, cx- 400, cy - 200);
        canvas.drawText(dialogue[part][index], displayFont, cx - canvas.getWidth() / 2 + 200, cy - 200);


        canvas.draw(nextIcon, Color.WHITE,
                nextIcon.getWidth() / 2f, nextIcon.getHeight() / 2f,
                cx + canvas.getWidth() / 2 - 100, cy - canvas.getHeight() / 2 + 100,
                0, scale.x, scale.y);

        canvas.draw(nextIcon, Color.WHITE,
                nextIcon.getWidth() / 2f, nextIcon.getHeight() / 2f,
                cx - canvas.getWidth() / 2 + 100, cy - canvas.getHeight() / 2 + 100,
                3.14f, scale.x, scale.y);
    }


}

