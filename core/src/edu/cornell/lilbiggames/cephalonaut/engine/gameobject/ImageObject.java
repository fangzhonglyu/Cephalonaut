package edu.cornell.lilbiggames.cephalonaut.engine.gameobject;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;

public class ImageObject extends GameObject {

    private final Texture texture;

    public ImageObject (Texture texture) {
        this.texture = texture;
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
    }
    
    @Override
    public boolean activatePhysics(World world) { return true; }

    @Override
    public void deactivatePhysics(World world) {}

    @Override
    public void draw(GameCanvas canvas) {
        int offsetX = (int) (canvas.getCameraX() * parallaxFactor.x);
        int offsetY = (int) (canvas.getCameraY() * parallaxFactor.y);
        canvas.draw(texture, -5000 + offsetX, -5000 + offsetY, 0, 0, 10000, 10000, drawScale.x, drawScale.y);
    }

    @Override
    public void drawDebug(GameCanvas canvas) {}
}
