package edu.cornell.lilbiggames.cephalonaut.engine.gameobject;

import com.badlogic.gdx.math.MathUtils;

public class LEBlackHole extends LevelElement {
    private static final float DEFAULT_ATTRACT_FACTOR = 9.81f;
    private static final float DEFAULT_RANGE = 5f;

    private float blackHoleAttractFactor;
    private float blackHoleRange;

    protected LEBlackHole(Def def) {
        super(def);
        this.blackHoleAttractFactor = def.properties.getFloat("blackHoleAttractFactor", DEFAULT_ATTRACT_FACTOR);
        this.blackHoleRange = def.properties.getFloat("blackHoleRange", DEFAULT_RANGE);
    }

    public float getBlackHoleAttractFactor() { return blackHoleAttractFactor; }
    public float getBlackHoleRange() { return blackHoleRange; }
}
