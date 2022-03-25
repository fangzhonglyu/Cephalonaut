package edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;

public class LETriggerable extends LevelElement {
    // TODO: Clean this up a bit. I just copied code over and it could be a bit nicer.

    protected Vector2 originalPos;
//    protected LevelElement activatee;
    protected boolean inContact = false;
    protected boolean activated = false;
    protected boolean opened;

    public LETriggerable(Def def) {
        super(def);
        setBodyType(BodyDef.BodyType.KinematicBody);
        originalPos = getPosition();
    }

    public void trigger() {
        if (getOpened()) {
            setLinearVelocity(Vector2.Zero);
        } else {
            setLinearVelocity(new Vector2(0, 1));
            if (getY() >= getOriginalPos().y + 1.4f * height) {
                setOpened(true);
            }
        }
    }

    public Vector2 getOriginalPos() {
        return originalPos;
    }

    public boolean getActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean getOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }
}
