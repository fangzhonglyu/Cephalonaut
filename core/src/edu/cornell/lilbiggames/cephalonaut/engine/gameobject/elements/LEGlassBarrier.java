package edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements;
import com.badlogic.gdx.graphics.Color;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;

import java.util.logging.Level;

public class LEGlassBarrier extends LevelElement {

    private static final float DEFAULT_HARDNESS = 10.0f;

    private float glassBarrierHardness;
    private float health;
    private Color tint;
    private int hitCoolDown;

    public LEGlassBarrier(LevelElement.Def def) {
        super(def);
        this.glassBarrierHardness = def.properties.getFloat("glassBarrierHardness", DEFAULT_HARDNESS);
        this.tint = def.tint;
        this.health = this.glassBarrierHardness;
        this.hitCoolDown = 0;
    }

    public float getHealth() { return health; }

    public void hit(float damage) {
        if(hitCoolDown > 0) {
            hitCoolDown -= 1;
            return;
        }
        health -= damage;
        tint.a -= 1 / glassBarrierHardness * damage;
        setTint(new Color(tint.r, tint.g, tint.b, tint.a));
        hitCoolDown = 20;
    }
}


