package edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements;

import com.badlogic.gdx.math.MathUtils;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;

public class LEBoostPad extends LevelElement {
    private static final float DEFAULT_BOOST_FACTOR = 8f;

    private float boostPadFactor;
    private float boostPadAngle;
    private FilmStrip filmStrip;
    private float frame;

    public LEBoostPad(Def def,FilmStrip filmStrip) {
        super(def);
        this.filmStrip = filmStrip;
        filmStrip.setFrame(0);
        setTexture(filmStrip);
        this.boostPadFactor = def.properties.getFloat("boostPadFactor", DEFAULT_BOOST_FACTOR);
        this.boostPadAngle = -MathUtils.degreesToRadians * 270;
        setSensor(true);
    }

    public float getBoostPadFactor() { return boostPadFactor; }
    public float getBoostPadAngle() { return boostPadAngle; }

    @Override
    public void update(float delta){
        frame += delta*7f;
        if(frame>=filmStrip.getSize())
            frame =0;
        filmStrip.setFrame((int)frame);
    }
}
