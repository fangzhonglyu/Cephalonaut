package edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements;

import com.badlogic.gdx.graphics.Color;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;

public class LETrigger extends LevelElement {
    private final int target;
    private final Color originalTint;

    private boolean activated = false;

    public LETrigger(Def def) {
        super(def);
        target = def.properties.getInt("target", -1);
        assert target >= 0;
        originalTint = getTint();
    }

    public int getTarget() { return target; }

    public void contacted() {
        // Kinda arbitrary to do this for a trigger. Maybe LETrigger could be an abstract class or an interface which
        // can be used by something like LEDoor which will define more specific functionality. Or we can encode
        // trigger behavior as data in this class, parsed from Tiled parameters. That might be nicer.
        activated = !activated;
        if (activated) {
            setTint(Color.GREEN);
        } else {
            setTint(originalTint);
        }
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
        if (this.activated) {
            setTint(Color.GREEN);
        } else {
            setTint(originalTint);
        }
    }

    public boolean isActivated() {
        return activated;
    }
}
