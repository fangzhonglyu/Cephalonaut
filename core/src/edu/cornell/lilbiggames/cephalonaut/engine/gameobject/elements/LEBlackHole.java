package edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements;

import com.badlogic.gdx.math.MathUtils;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;

public class LEBlackHole extends LevelElement {
    private static final float DEFAULT_ATTRACT_FACTOR = 9.81f;
    private static final float DEFAULT_RANGE = 5f;
    private float blackHoleAttractFactor;
    private float blackHoleRange;
    private float time;
    private FilmStrip filmStrip;

    public LEBlackHole(Def def, FilmStrip filmStrip) {
        super(def);
        this.blackHoleAttractFactor = def.properties.getFloat("blackHoleAttractFactor", DEFAULT_ATTRACT_FACTOR);
        this.blackHoleRange = def.properties.getFloat("blackHoleRange", DEFAULT_RANGE);
        time = 0;
        texture = filmStrip;
        this.filmStrip = filmStrip;
        filmStrip.setFrame(0);
    }

    public float getBlackHoleAttractFactor() { return blackHoleAttractFactor; }
    public float getBlackHoleRange() { return blackHoleRange; }

    @Override
    public void update(float delta) {
        time += 7.0f * delta;
        if (time >= 1) {
            time = 0;
            filmStrip.setFrame((filmStrip.getFrame() + 1) % filmStrip.getSize());
        }
    }

    @Override
    public void draw(GameCanvas canvas) {
        super.draw(canvas);
        canvas.setBH(getX() * drawScale.x, getY() * drawScale.y, blackHoleRange * drawScale.x);
    }
}
