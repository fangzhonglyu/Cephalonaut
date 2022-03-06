package edu.cornell.lilbiggames.cephalonaut.engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.CapsuleObstacle;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.WheelObstacle;

public class GrappleModel extends WheelObstacle {
    /** Whether the grapple is actively extending */
    private boolean isGrappling;
    /** Whether the grapple is anchored */
    private boolean isAnchored;
    /** The joint anchor for the grapple */
    private DistanceJointDef anchor;
    /** The extension length of the grapple */
    private float extensionLength;

    public GrappleModel(float x, float y, Vector2 drawScale) {
        // The shrink factors fit the image to a tighter hitbox
        super(x, y, 0.1f);
        setName("hook");
        setDrawScale(drawScale);
        setFixedRotation(true);
        setActive(false);
        setSensor(true);
        setBodyType(BodyDef.BodyType.StaticBody);

        int pixDiameter = (int) (getRadius() * 2);
        Pixmap pixmap = new Pixmap(pixDiameter, pixDiameter, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fillCircle(pixDiameter / 2, pixDiameter / 2, pixDiameter / 2);
        texture = new TextureRegion(new Texture(pixmap));
        origin.set(pixDiameter / 2f, pixDiameter / 2f);
    }

    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }

        // TODO: Stuff here probably

        return true;
    }

    /**
     * Returns true if the grapple is actively extending.
     *
     * @return true if the grapple is actively extending.
     */
    public boolean isGrappling() {
        return isGrappling;
    }

    /**
     * Sets whether the grapple is actively extending.
     *
     * @param grappling whether the grapple is actively extending.
     */
    public void setGrappling(boolean grappling) {
        isGrappling = grappling;
    }

    /**
     * Returns true if the grapple is anchored.
     *
     * @return true if the grapple is anchored
     */
    public boolean isAnchored() {
        return isAnchored;
    }

    /**
     * Sets whether the grapple is anchored.
     *
     * @param anchored whether the grapple is anchored.
     */
    public void setAnchored(boolean anchored) {
        isAnchored = anchored;
    }

    /**
     * Sets the grapple's anchor.
     *
     * @param anchor the distance joint definition for the grapple.
     */
    public void setAnchor(DistanceJointDef anchor) {
        this.anchor = anchor;
    }

    /**
     * Returns the grapple's anchor.
     *
     * @return the grapple's anchor.
     */
    public DistanceJointDef getAnchor() {
        return anchor;
    }

    /**
     * Sets the grapple's extension length.
     *
     * @param length the distance the grapple has extended from the cephalonaut.
     */
    public void setExtensionLength(float length) {
        extensionLength = length;
    }

    /**
     * Returns the grapple's extension length.
     *
     * @return the grapple's extension length.
     */
    public float getExtensionLength() {
        return extensionLength;
    }

    public void draw(GameCanvas canvas) {
        if (isGrappling || isAnchored) {
            canvas.draw(texture, Color.ORANGE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y,
                    getAngle(), 1, 1);
        }
    }

    public void drawDebug(GameCanvas canvas) {
        if (isGrappling || isAnchored) {
            super.drawDebug(canvas);
            canvas.drawPhysics(shape, Color.RED, getX(), getY(), drawScale.x, drawScale.y);
        }
    }

}
