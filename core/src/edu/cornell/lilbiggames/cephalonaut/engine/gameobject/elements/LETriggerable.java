package edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;

public class LETriggerable extends LevelElement {
    // TODO: Clean this up a bit. I just copied code over and it could be a bit nicer.

    static final float MOVE_SPEED = 20f;

    final protected Vector2 originalPos;
    final protected Vector2 activatedPos;
    final protected float dx;
    final protected float dy;


    protected Vector2 targetPos;

    public LETriggerable(Def def) {
        super(def);

        dx = def.properties.getFloat("dx", 0);
        dy = def.properties.getFloat("dy", 3);
        setBodyType(BodyDef.BodyType.KinematicBody);
        originalPos = getPosition().cpy();
        // TODO: Make these adjustable parameters
        activatedPos = getPosition().cpy().add(dx, dy);
        targetPos = originalPos;
    }

    public void checkPos() {
        if (getLinearVelocity().len2() > 0 && getPosition().dst(targetPos) < 0.1f) {
            setLinearVelocity(Vector2.Zero);
            setPosition(targetPos.cpy());
        }
    }

    public void setActivated(boolean activated) {
        targetPos = activated ? activatedPos : originalPos;
        setLinearVelocity(targetPos.cpy().sub(getPosition()).setLength(MOVE_SPEED));
    }

}
