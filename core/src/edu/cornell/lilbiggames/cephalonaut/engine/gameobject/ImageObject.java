package edu.cornell.lilbiggames.cephalonaut.engine.gameobject;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;

public class ImageObject extends GameObject {

    private Vector2 parallaxFactor;
    private Texture texture;

    public ImageObject (Texture texture) {
        this.texture = texture;
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        Paral
    }

    @Override
    public boolean activatePhysics(World world) { return true; }

    @Override
    public void deactivatePhysics(World world) {}

    @Override
    public void draw(GameCanvas canvas) {
        canvas.draw(texture, Color.WHITE, -100f, -100f, 10000f, 10000f);
    }

    @Override
    public void drawDebug(GameCanvas canvas) {}
}
