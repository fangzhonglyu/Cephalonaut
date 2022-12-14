package edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements;

import com.badlogic.gdx.math.MathUtils;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;

public class LEAnimated extends LevelElement {

    private FilmStrip filmStrip;
    private float frame;
    private float updateFactor;

    public LEAnimated(Def def, FilmStrip filmStrip,float updateFactor, boolean randStart) {
        super(def);
        this.filmStrip = filmStrip;
        if(randStart) {
            frame = (float) (Math.random() * filmStrip.getCols());
        }
        filmStrip.setFrame(0);
        setTexture(filmStrip);
        this.updateFactor = updateFactor;
    }

    @Override
    public void update(float delta) {
        frame += delta * updateFactor;
        if (frame >= filmStrip.getSize()) {
            frame = 0;
        }
        filmStrip.setFrame((int) frame);
    }
}
