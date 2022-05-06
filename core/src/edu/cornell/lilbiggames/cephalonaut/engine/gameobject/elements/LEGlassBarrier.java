package edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements;
import com.badlogic.gdx.graphics.Color;
import edu.cornell.lilbiggames.cephalonaut.engine.controller.SoundController;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;

import java.util.logging.Level;

public class LEGlassBarrier extends LevelElement {

    private float glassBarrierHardness;
    private Color tint;
    private Color indicator;
    private float alpha;
    private final float INDICATOR_DISTANCE = 5.0f;
    private final float GLASS_BARRIER_HARDNESS = 9.0f;

    private boolean didBreak;

    public LEGlassBarrier(LevelElement.Def def) {
        super(def);
        this.glassBarrierHardness = GLASS_BARRIER_HARDNESS;
        this.tint = def.tint;
        this.alpha = this.tint.a;
        this.indicator = new Color(tint.r, tint.g + 10, tint.b, tint.a);
        this.didBreak = false;
    }

    public void hit(float damage) {
        // alpha -= 1 / glassBarrierHardness * damage;
        // tint  = new Color(tint.r, tint.g, tint.b, Math.max(alpha, 0.33f));
        if(glassBarrierHardness - damage <= 0) {
            SoundController.playSound(3,1);
            this.markRemoved(true);
            didBreak = true;
        }
    }

    public void willBreak(float damage, float distance) {
        if(distance < INDICATOR_DISTANCE && GLASS_BARRIER_HARDNESS - damage <= 0) {
            setTint(indicator);
        }
        else {
            setTint(tint);
        }
    }

    public boolean isBroken() {
        return didBreak;
    }

    public void reset() {
        this.glassBarrierHardness = GLASS_BARRIER_HARDNESS;
        didBreak = false;
        this.markRemoved(false);
    }
}


