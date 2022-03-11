package edu.cornell.lilbiggames.cephalonaut.engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.Obstacle;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.WheelObstacle;
import edu.cornell.lilbiggames.cephalonaut.util.PooledList;

public class GrappleModel extends WheelObstacle {
    /** Whether the grapple is out */
    private boolean isOut;
    /** Whether the grapple is tout */
    private boolean isGrappling;
    /** Whether the grapple is anchored */
    private boolean isAnchored;
    /** The anchor location of the grapple */
    private String anchorLocation;
    /** The extension length of the grapple */
    private float extensionLength;

    public GrappleModel(float x, float y, Vector2 drawScale) {
        // The shrink factors fit the image to a tighter hitbox
        super(x, y, 0.1f);
        setName("grapple");
        setDrawScale(drawScale);
        setFixedRotation(true);
        setActive(false);
        setSensor(true);
        setBullet(true);

        int pixDiameter = (int) (getRadius() * 2);
        Pixmap pixmap = new Pixmap(pixDiameter, pixDiameter, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fillCircle(pixDiameter / 2, pixDiameter / 2, pixDiameter / 2);
        texture = new TextureRegion(new Texture(pixmap));
        origin.set(pixDiameter / 2f, pixDiameter / 2f);

        isOut = false;
        isGrappling = true;
        isAnchored = false;
        anchorLocation = "";
        extensionLength = 0;
    }

    /**
     * Resets grapple to constructor values.
     *
     */
    public void reset() {
        setActive(false);
        setLinearVelocity(new Vector2());
        setBodyType(BodyDef.BodyType.DynamicBody);
        isOut = false;
        isGrappling = false;
        isAnchored = false;
        anchorLocation = "";
        extensionLength = 0;
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
     * Returns true if the grapple is out.
     *
     * @return true if the grapple is out.
     */
    public boolean isOut() {
        return isOut;
    }

    /**
     * Sets whether the grapple is out.
     *
     * @param out whether the grapple is out.
     */
    public void setOut(boolean out) {
        isOut = out;
    }

    /**
     * Returns true if the grapple is tout.
     *
     * @return true if the grapple is tout.
     */
    public boolean isGrappling() {
        return isGrappling;
    }

    /**
     * Sets whether the grapple is tout.
     *
     * @param grappling whether the grapple is tout.
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
     * Sets the grapple's anchor location.
     *
     * @param location the distance joint definition for the grapple.
     */
    public void setAnchorLocation(String location) {
        this.anchorLocation = location;
    }

    /**
     * Returns the grapple's anchor location.
     *
     * @return the grapple's anchor location.
     */
    public String getAnchorLocation() { return anchorLocation; }

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

    /**
     * Sets the grapple's linear velocity closest towards anchoring point.
     *
     */
    public void closestAnchor(PooledList<Obstacle> objects) {
        Vector2 closest = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);
        for (Obstacle o : objects) {
            float distance = getPosition().dst(o.getPosition());
            if (!o.getName().equals("michael") && !o.getName().equals("grapple") &&
                    distance < getPosition().dst(closest)) {
                closest = o.getPosition();
            }
        }
        setLinearVelocity(closest.cpy().sub(getPosition().cpy()).nor().scl(15));
    }

    public void draw(GameCanvas canvas) {
        if (isOut) {
            canvas.draw(texture, Color.ORANGE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y,
                    getAngle(), 1, 1);
        }
    }

    public void drawDebug(GameCanvas canvas) {
        if (isOut) {
            super.drawDebug(canvas);
            canvas.drawPhysics(shape, Color.RED, getX(), getY(), drawScale.x, drawScale.y);
        }
    }

}
