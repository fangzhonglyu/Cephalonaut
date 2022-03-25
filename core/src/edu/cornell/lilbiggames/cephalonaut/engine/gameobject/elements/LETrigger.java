package edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements;

import com.badlogic.gdx.graphics.Color;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;

public class LETrigger extends LevelElement {
    private int target;

    public LETrigger(Def def) {
        super(def);
        target = def.properties.getInt("target", -1);
        assert target >= 0;
    }

    public int getTarget() { return target; }

    public void contacted() {
        setTint(Color.GREEN);
    }
}
