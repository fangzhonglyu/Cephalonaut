package edu.cornell.lilbiggames.cephalonaut.engine.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.GameObject;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.Obstacle;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.WheelObstacle;
import edu.cornell.lilbiggames.cephalonaut.util.PooledList;

import java.util.ArrayList;

public class GrappleModel extends WheelObstacle {
    /** Whether the grapple is out */
    private boolean isOut;
    /** Whether the grapple is taut */
    private boolean isGrappling;
    /** Whether the grapple is anchored */
    private boolean isAnchored;
    /** Whether the grapple is locked in place*/
    public boolean isLocked;
    /** The anchor location of the grapple */
    private String anchorLocation;
    /** The extension length of the grapple */
    private float extensionLength;
    /** The max extension length of the grapple */
    private float maxLength;
    /** Travelled Points*/
    private ArrayList<Vector2> trace;
    private Texture tex;

    public GrappleModel(float x, float y, Vector2 drawScale) {
        // The shrink factors fit the image to a tighter hitbox
        super(x, y, 0.1f);
        setName("grapple");
        setDrawScale(drawScale);
        setFixedRotation(true);
        setActive(false);
        setSensor(true);
        setBullet(true);

        int pixDiameter = 6;
        Pixmap pixmap = new Pixmap(pixDiameter, pixDiameter, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.ORANGE);
        pixmap.fillCircle(pixDiameter / 2, pixDiameter / 2, pixDiameter / 2);
        tex = (new Texture(pixmap));
        origin.set(pixDiameter / 2f, pixDiameter / 2f);

        isOut = false;
        isGrappling = false;
        isAnchored = false;
        isLocked = false;
        anchorLocation = "";
        extensionLength = 0;
        trace = new ArrayList<>();
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
        isLocked = false;
        anchorLocation = "";
        extensionLength = 0;
        trace = new ArrayList<>();
    }

    public void addTrace(Vector2 cephP){
        trace.add(new Vector2(getX(),getY()).sub(cephP));
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
     * Returns true if the grapple is taut.
     *
     * @return true if the grapple is taut.
     */
    public boolean isGrappling() {
        return isGrappling;
    }

    /**
     * Sets whether the grapple is taut.
     *
     * @param grappling whether the grapple is taut.
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
    public void setAnchored(boolean anchored) { isAnchored = anchored; }

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

//    /**
//     * Sets the grapple's linear velocity closest towards anchoring point.
//     *
//     */
//    public void closestAnchor(PooledList<GameObject> objects) {
//        Vector2 closest = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);
//        for (GameObject o : objects) {
//            float distance = getPosition().dst(o.getPosition());
//            if (!o.getName().equals("michael") && !o.getName().equals("grapple") &&
//                    distance < getPosition().dst(closest)) {
//                closest = o.getPosition();
//            }
//        }
//        setLinearVelocity(closest.cpy().sub(getPosition().cpy()).nor().scl(15));
//    }

    /**
     * Set the grapple's max length.
     *
     * @param length the grapple's max length.
     */
    public void setMaxLength(float length) {
        maxLength = length;
    }

    /**
     * Get the grapple's max length.
     *
     * @return the grapple's max length.
     */
    public float getMaxLength() {
        return maxLength;
    }

    /**
     * Whether the grapple's is fully extended.
     *
     * @return whether the grapple's is fully extended.
     */
    public boolean isFullyExtended() {
        return extensionLength >= maxLength;
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas, Vector2 cephP) {
        if (isOut) {
            Affine2 tr = new Affine2();
            tr.preTranslate(cephP.x, cephP.y);
            tr.rotate(getPosition().sub(cephP).angleDeg());
            float dist = getPosition().dst(cephP);
            for (float i = 0; i<getPosition().dst(cephP)/2; i+=1/drawScale.x){
                Vector2 t = new Vector2(i*2, (float) Math.sin(i*5)/dist);
                tr.applyTo(t);
                canvas.draw(tex, Color.ORANGE, 3f, 3f, t.x * drawScale.x, t.y * drawScale.y,
                        getAngle(), 1, 1);
            }
            canvas.draw(tex, Color.ORANGE, 3f, 3f, getX() * drawScale.x, getY() * drawScale.y,
                    getAngle(), 1, 1);
        }

    }

    /**
     * Draws the outline of the physics body.
     *
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        if (isOut) {
            super.drawDebug(canvas);
            canvas.drawPhysics(shape, Color.RED, getX(), getY(), drawScale.x, drawScale.y);
        }
    }

}
