package edu.cornell.lilbiggames.cephalonaut.engine.ui;

import com.badlogic.gdx.graphics.Color;
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

    public Slider(GameCanvas canvas, Color color, float min, float max, float value, boolean isActive, float sliderWidth, float sliderHeight){
        this.canvas = canvas;
        this.color = color;
        this.min = min;
        this.max = max;
        this.value = value;
        this.isActive = isActive;
        this.sliderWidth = sliderWidth;
        this.sliderHeight = sliderHeight;
    }

    public void updatePosition(float x, float y){
        this.x = x;
        this.y = y;
    }

    public void updateSize(float sliderWidth, float sliderHeight){
        this.sliderWidth = sliderWidth;
        this.sliderHeight = sliderHeight;
    }

    public void draw(){
        canvas.drawSlider(color, min, max, value, x, y, sliderWidth, sliderHeight);
    }

    public void updateValue(){
        this.value = value;
    }
}
