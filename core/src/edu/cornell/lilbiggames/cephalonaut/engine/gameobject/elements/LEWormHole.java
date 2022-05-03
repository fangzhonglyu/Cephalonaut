package edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;

public class LEWormHole extends LevelElement {
    private final int target;
    private final Color originalTint;
    private FilmStrip filmStrip;
    private float frame;
    private int cooldown;
    private final int WORMHOLE_COOLDOWN = 100;

    private boolean activated = false;

    public LEWormHole(Def def,FilmStrip filmstrip) {
        super(def);
        texture = filmstrip;
        this.filmStrip = filmstrip;
        filmstrip.setFrame(0);
        frame = 0;
        setBodyType(BodyDef.BodyType.KinematicBody);
        target = def.properties.getInt("target", -1);
        assert target >= 0;
        originalTint = getTint();
        cooldown = 0;
        setAngularVelocity(-0.4f);
    }

    public int getTarget() { return target; }

    public void setCooldown(int cooldown) { this.cooldown = cooldown; }

    public int getCooldown() { return cooldown; }

    public int getWormHoleCooldown() { return WORMHOLE_COOLDOWN; }

    @Override
    public void update(float delta){
        frame += delta*7f;
        if(frame>=filmStrip.getSize())
            frame =0;
        filmStrip.setFrame((int)frame);
    }

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

