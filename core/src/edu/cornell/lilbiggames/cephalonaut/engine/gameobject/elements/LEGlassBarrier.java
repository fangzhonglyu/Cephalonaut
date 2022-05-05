package edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements;
import com.badlogic.gdx.graphics.Color;
import edu.cornell.lilbiggames.cephalonaut.engine.controller.SoundController;
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
        this.indicator = new Color(tint.r, tint.g + 10, tint.b, tint.a);
        this.health = this.glassBarrierHardness;
    }

    public void hit(float damage) {
        health -= damage;
        alpha -= 1 / glassBarrierHardness * damage;
        tint  = new Color(tint.r, tint.g, tint.b, Math.max(alpha, 0.33f));
        if(health <= 0) {
            SoundController.playSound(3,1);
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


