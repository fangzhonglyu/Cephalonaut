package edu.cornell.lilbiggames.cephalonaut.engine.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;

public class Slider {
    private Color color;
    private float min;
    private float max;
    private float value;
    private float x;
    private float y;
    private float sliderWidth;
    private float sliderHeight;
    private GameCanvas canvas;
    private boolean isActive;

    private Vector2 knobPosition;
    private float knobRadius;

    public Slider(GameCanvas canvas, Color color, float min, float max, float value, boolean isActive, float sliderWidth, float sliderHeight, float knobRadius){
        this.canvas = canvas;
        this.color = color;
        this.min = min;
        this.max = max;
        this.value = value;
        this.isActive = isActive;
        this.sliderWidth = sliderWidth;
        this.sliderHeight = sliderHeight;
        this.knobRadius = knobRadius;
    }

    public Vector2 getKnobPosition(){
        return knobPosition;
    }

    public Vector2 getPosition(){
        return new Vector2(x, y);
    }

    public float getWidth(){
        return sliderWidth;
    }

    public float getHeight(){
        return sliderHeight;
    }

    public Color getColor(){
        return color;
    }

    public float getValue(){
        return value;
    }

    public boolean inKnobBounds(float x, float y){
        return (Math.abs(x - knobPosition.x) <= 2*knobRadius && Math.abs(y - knobPosition.y) <= 2*knobRadius);
    }
    public void updatePosition(float x, float y){
        this.x = x;
        this.y = y;
        float knobX = (x-sliderWidth/2.0f) + sliderWidth*value;
        knobPosition = new Vector2(knobX, y);
    }

    public void updateSize(float sliderWidth, float sliderHeight){
        this.sliderWidth = sliderWidth;
        this.sliderHeight = sliderHeight;
    }

    public void movedX(float newX){
        knobPosition = new Vector2(newX, y);
        value = 0.95f * Math.min(1,Math.max(0,(knobPosition.x - (x-sliderWidth/2.0f))/ sliderWidth));
        knobPosition = new Vector2((x-sliderWidth/2.0f) + value*sliderWidth, y);
    }


    public float getKnobRadius(){
        return knobRadius;
    }

    public void draw(){
        canvas.drawSlider(this);
    }

}
