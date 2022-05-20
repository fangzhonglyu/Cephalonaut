package edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.BodyDef;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;

public class LEInkPack extends LevelElement {
    private FilmStrip filmStrip;
    private float frame;
    private float updateFactor=7.0f;

    public LEInkPack(Def def, FilmStrip filmStrip) {
        super(def);
        this.filmStrip = filmStrip;
        filmStrip.setFrame(0);
        setTexture(filmStrip);
    }

    public void trigger(){
        frame = 6;
        this.markRemoved(true);
    }

    @Override
    public void update(float delta) {
        if (frame<6f) {
            frame += delta * updateFactor;
            if (frame >= 6) {
                frame = 0;
            }
        }
        else{
            frame += delta * updateFactor;
            if(frame>=filmStrip.getSize())
                frame=0;
        }
        filmStrip.setFrame((int) frame);
    }
}
