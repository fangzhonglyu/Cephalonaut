package edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements;

import com.badlogic.gdx.math.MathUtils;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;

public class LEStart extends LevelElement{
    private float ink;

    public LEStart(LevelElement.Def def) {
        super(def);
        this.ink = def.properties.getFloat("ink",1f);
    }

    public float getInk(){
        return ink;
    }
}
