package edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements;

import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;

public class LEDialogueTrigger extends LevelElement {
    private final int target;
    private boolean active = true;

    public LEDialogueTrigger(Def def) {
        super(def);
        target = def.properties.getInt("target", -1);
        assert target >= 0;
    }

    public int getTarget() {
        return target;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isActive() {
        return active;
    }
}
