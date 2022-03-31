package edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements;
import com.badlogic.gdx.graphics.Color;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;

import java.util.logging.Level;

public class LEGlassBarrier extends LevelElement {

    private static final float DEFAULT_HARDNESS = 10.0f;

    private float glassBarrierHardness;
    private float health;
    private Color tint;
    private Color indicator;

    public LEGlassBarrier(LevelElement.Def def) {
        super(def);
        this.glassBarrierHardness = def.properties.getFloat("glassBarrierHardness", DEFAULT_HARDNESS);
        this.tint = def.tint;
        this.indicator = new Color(tint.r + 50, tint.g, tint.b, tint.a);
        this.health = this.glassBarrierHardness;
    }

    public void hit(float damage) {
        health -= damage;
        tint.a -= 1 / glassBarrierHardness * damage;
        setTint(new Color(tint.r, tint.g, tint.b, tint.a));
        if(health <= 0) {
            this.markRemoved(true);
        }
    }

    public boolean willBreak(float damage) {
        if(health - damage <= 0) {
            setTint(indicator);
        }
        else {
            setTint(tint);
        }
        return health - damage <= 0;
    }

    public boolean isBroken() {
        return health <= 0;
    }
}


