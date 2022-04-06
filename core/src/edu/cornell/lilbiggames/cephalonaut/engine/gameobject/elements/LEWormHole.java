package edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements;

import com.badlogic.gdx.graphics.Color;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;

public class LEWormHole extends LevelElement {
    private final int target;
    private final Color originalTint;
    private int cooldown;
    private final int WORMHOLE_COOLDOWN = 100;

    private boolean activated = false;

    public LEWormHole(Def def) {
        super(def);
        target = def.properties.getInt("target", -1);
        assert target >= 0;
        originalTint = getTint();
        cooldown = 0;
    }

    public int getTarget() { return target; }

    public void setCooldown(int cooldown) { this.cooldown = cooldown; }

    public int getCooldown() { return cooldown; }

    public int getWormHoleCooldown() { return WORMHOLE_COOLDOWN; }

//    public void contacted() {
//        // Kinda arbitrary to do this for a trigger. Maybe LETrigger could be an abstract class or an interface which
//        // can be used by something like LEDoor which will define more specific functionality. Or we can encode
//        // trigger behavior as data in this class, parsed from Tiled parameters. That might be nicer.
//        activated = !activated;
//        if (activated) {
//            setTint(Color.GREEN);
//        } else {
//            setTint(originalTint);
//        }
//    }

//    public boolean isActivated() {
//        return activated;
//    }
}

