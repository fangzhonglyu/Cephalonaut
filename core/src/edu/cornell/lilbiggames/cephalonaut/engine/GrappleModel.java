package edu.cornell.lilbiggames.cephalonaut.engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.CapsuleObstacle;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.WheelObstacle;

public class GrappleModel extends edu.cornell.lilbiggames.cephalonaut.engine.obstacle.WheelObstacle {

    public GrappleModel(float x, float y, Vector2 drawScale) {
        // The shrink factors fit the image to a tigher hitbox
        super(x, y, 0.1f);
        setDrawScale(drawScale);
        setDensity(1);
        setFriction(0);
        setRestitution(1);
        setFixedRotation(true);
        setBodyType(BodyDef.BodyType.StaticBody);
        setActive(true);
        setName("hook");
        int pixDiameter = (int) (getRadius() * 2);
        Pixmap pixmap = new Pixmap(pixDiameter, pixDiameter, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fillCircle(pixDiameter / 2, pixDiameter / 2, pixDiameter / 2);
        texture = new TextureRegion(new Texture(pixmap));
        origin.set(pixDiameter / 2f, pixDiameter / 2f);

        setName("Hook");
    }

    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }

        // TODO: Stuff here probably

        return true;
    }

    public void draw(GameCanvas canvas) {
        canvas.draw(texture, Color.ORANGE, origin.x, origin.y,
                getX() * drawScale.x, getY() * drawScale.y,
                getAngle(), 1, 1);
    }

    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        canvas.drawPhysics(shape, Color.RED, getX(), getY(), drawScale.x, drawScale.y);
    }

}
