package edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements;

import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;

public class LEBlackHole extends LevelElement {
    private static final float DEFAULT_ATTRACT_FACTOR = 9.81f;
    private static final float DEFAULT_RANGE = 5f;
    private final float blackHoleAttractFactor;
    private final float blackHoleRange;
    private FilmStrip filmStrip;

    public LEBlackHole(Def def) {
        super(def);
        this.blackHoleAttractFactor = def.properties.getFloat("blackHoleAttractFactor", DEFAULT_ATTRACT_FACTOR);
        this.blackHoleRange = def.properties.getFloat("blackHoleRange", DEFAULT_RANGE);
    }

    public float getBlackHoleAttractFactor() {
        return blackHoleAttractFactor;
    }

    public float getBlackHoleRange() {
        return blackHoleRange;
    }

    @Override
    public void update(float delta) {

    }
}
