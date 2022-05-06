package edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.lilbiggames.cephalonaut.engine.controller.SoundController;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;

public class LEBoostPad extends LevelElement {
    private static final float DEFAULT_BOOST_FACTOR = 8f;

    private float boostPadFactor;
    private float boostPadAngle;
    private FilmStrip filmStrip;
    private float frame;
    private int cooldown;
    private final int BOOST_COOLDOWN = 25;

    private final float BOOST_MULTIPLIER = 15f;

    public LEBoostPad(Def def,FilmStrip filmStrip) {
        super(def);
        this.filmStrip = filmStrip;
        filmStrip.setFrame(0);
        setTexture(filmStrip);
        this.boostPadFactor = def.properties.getFloat("boostPadFactor", DEFAULT_BOOST_FACTOR);
        this.boostPadAngle = -MathUtils.degreesToRadians * 270;
        setSensor(true);
        this.cooldown = 0;
    }

    public float getBoostPadFactor() { return boostPadFactor; }
    public float getBoostPadAngle() { return boostPadAngle; }

    @Override
    public void update(float delta){
        frame += delta*7f;
        if(frame>=filmStrip.getSize())
            frame =0;
        filmStrip.setFrame((int)frame);
    }

    public Vector2 boost() {
        SoundController.playSound(2,1);
        Vector2 force = new Vector2(0, getBoostPadFactor() * BOOST_MULTIPLIER).setAngleRad(getAngle() + getBoostPadAngle());
        return force;
    }

    public void setCooldown(int cooldown) { this.cooldown = cooldown; }

    public int getCooldown() { return cooldown; }

    public int getBOOST_COOLDOWN() { return BOOST_COOLDOWN; }



}
