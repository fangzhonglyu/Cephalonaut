package edu.cornell.lilbiggames.cephalonaut.engine.gameobject;

import com.badlogic.gdx.math.MathUtils;

public class LEBoostPad extends LevelElement {
    private static final float DEFAULT_BOOST_FACTOR = 8f;

    private float boostPadFactor;
    private float boostPadAngle;

    protected LEBoostPad(Def def) {
        super(def);
        this.boostPadFactor = def.properties.getFloat("boostPadFactor", DEFAULT_BOOST_FACTOR);
        this.boostPadAngle = -MathUtils.degreesToRadians * def.properties.getFloat("boostPadAngle", 0f);
        setSensor(true);
    }

    public float getBoostPadFactor() { return boostPadFactor; }
    public float getBoostPadAngle() { return boostPadAngle; }
}
