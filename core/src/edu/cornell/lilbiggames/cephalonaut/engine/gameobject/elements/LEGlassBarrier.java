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
    private float alpha;
    private final float INDICATOR_DISTANCE = 5.0f;

    public LEGlassBarrier(LevelElement.Def def) {
        super(def);
        this.glassBarrierHardness = def.properties.getFloat("glassBarrierHardness", DEFAULT_HARDNESS);
        this.tint = def.tint;
        this.alpha = this.tint.a;
        this.indicator = new Color(tint.r + 10, tint.g, tint.b, tint.a);
        this.health = this.glassBarrierHardness;
    }

    public void hit(float damage) {
        health -= damage;
        alpha -= 1 / glassBarrierHardness * damage;
        tint  = new Color(tint.r, tint.g, tint.b, alpha);
        if(health <= 0) {
            this.markRemoved(true);
        }
    }

    public void willBreak(float damage, float distance) {
        if(distance < INDICATOR_DISTANCE && health - damage <= 0) {
            setTint(indicator);
        }
        else {
            setTint(tint);
        }
    }

    public boolean isBroken() {
        return health <= 0;
    }

    public void reset() {
        health = glassBarrierHardness;
        alpha = 1.0f;
        this.markRemoved(false);
    }
}


