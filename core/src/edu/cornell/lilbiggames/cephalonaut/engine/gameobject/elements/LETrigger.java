package edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import edu.cornell.lilbiggames.cephalonaut.engine.controller.SoundController;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;

import java.util.logging.SocketHandler;

public class LETrigger extends LevelElement {
    private final int target;
    private final TextureRegion originalTexture;
    private final TextureRegion triggerTexture;

    private boolean activated = false;


    public LETrigger(Def def) {
        super(def);
        target = def.properties.getInt("target", -1);
        assert target >= 0;
        originalTexture = getTexture();
        this.triggerTexture = def.triggerTexture;
    }

    public int getTarget() { return target; }

    public void contacted() {
        // Kinda arbitrary to do this for a trigger. Maybe LETrigger could be an abstract class or an interface which
        // can be used by something like LEDoor which will define more specific functionality. Or we can encode
        // trigger behavior as data in this class, parsed from Tiled parameters. That might be nicer.
        if(!activated) {
            activated = true;
            setTexture(triggerTexture);
            SoundController.playSound(8,1);
        }
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
        if (this.activated) {
            setTexture(triggerTexture);
        } else {
            setTexture(originalTexture);
        }
    }

    public boolean isActivated() {
        return activated;
    }
}
