package edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements;
import com.badlogic.gdx.graphics.Color;
import edu.cornell.lilbiggames.cephalonaut.engine.controller.SoundController;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;

import java.util.logging.Level;

public class LEGlassBarrier extends LevelElement {

    private float glassBarrierHardness;
    private Color tint;
    private Color indicator;
    private final float INDICATOR_DISTANCE = 5.0f;
    private final float GLASS_BARRIER_HARDNESS = 9.0f;
    private FilmStrip filmStrip;
    private float frame;

    private boolean didBreak;
    private boolean isAnimating;

    public LEGlassBarrier(LevelElement.Def def, FilmStrip filmStrip) {
        super(def);
        this.filmStrip = filmStrip;
        filmStrip.setFrame(0);
        setTexture(filmStrip);
        this.glassBarrierHardness = GLASS_BARRIER_HARDNESS;
        this.tint = def.tint;
        this.indicator = new Color(tint.r, tint.g + 10, tint.b, tint.a);
        this.didBreak = false;
        this.isAnimating = false;
    }

    public void hit(float damage) {
        if(glassBarrierHardness - damage <= 0) {
            SoundController.playSound(3,1);
            didBreak = true;
            isAnimating = true;
        }
    }

    @Override
    public void update(float delta){
        if(isAnimating) {
            frame += delta * 28f;
            if (frame >= filmStrip.getSize()) {
                isAnimating = false;
                this.markRemoved(true);
                return;
            }
            filmStrip.setFrame((int) frame);
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


