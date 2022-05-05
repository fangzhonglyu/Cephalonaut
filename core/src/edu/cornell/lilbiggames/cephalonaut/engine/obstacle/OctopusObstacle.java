/*
 * BoxObject.java
 *
 * Given the name Box2D, this is your primary model class.  Most of the time,
 * unless it is a player controlled avatar, you do not even need to subclass
 * BoxObject.  Look through the code and see how many times we use this class.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.lilbiggames.cephalonaut.engine.obstacle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;

/**
 * Box-shaped model to support collisions.
 * <p>
 * Unless otherwise specified, the center of mass is as the center.
 */
public class OctopusObstacle extends SimpleObstacle {
    /**
     * Shape information for this box
     */
    protected CircleShape circleShape;
    /**
     * Shape information for this box
     */
    protected PolygonShape triangleShape;
    /**
     * Stores the fixture information for this shape
     */
    protected FixtureDef fixtureTri;
    /**
     * The width and height of the box
     */
    private final Vector2 dimension;
    /**
     * A cache value for when the user wants to access the dimensions
     */
    private final Vector2 sizeCache;
    /**
     * A cache value for the fixture (for resizing)
     */
    private Fixture geometry;
    /**
     * Cache of the polygon vertices (for resizing)
     */
    private final float[] vertices;
    /**
     * A cache value for the fixture (for resizing)
     */
    private Fixture geometryTri;

    /**
     * Creates a new box at the origin.
     * <p>
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param width  The object width in physics units
     * @param height The object width in physics units
     */
    public OctopusObstacle(float width, float height) {
        this(0, 0, width, height);
    }

    /**
     * Creates a new box object.
     * <p>
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param x      Initial x position of the box center
     * @param y      Initial y position of the box center
     * @param width  The object width in physics units
     * @param height The object width in physics units
     */
    public OctopusObstacle(float x, float y, float width, float height) {
        super(x, y);
        fixtureTri = new FixtureDef();
        dimension = new Vector2(width, height);
        sizeCache = new Vector2();
        circleShape = new CircleShape();
        triangleShape = new PolygonShape();
        vertices = new float[6];
        geometry = null;

        // Initialize
        resize(width, height);
    }

    /**
     * Returns the dimensions of this box
     * <p>
     * This method does NOT return a reference to the dimension vector. Changes to this
     * vector will not affect the shape.  However, it returns the same vector each time
     * its is called, and so cannot be used as an allocator.
     *
     * @return the dimensions of this box
     */
    public Vector2 getDimension() {
        return sizeCache.set(dimension);
    }

    /**
     * Sets the dimensions of this box
     * <p>
     * This method does not keep a reference to the parameter.
     *
     * @param value the dimensions of this box
     */
    public void setDimension(Vector2 value) {
        setDimension(value.x, value.y);
    }

    /**
     * Sets the dimensions of this box
     *
     * @param width  The width of this box
     * @param height The height of this box
     */
    public void setDimension(float width, float height) {
        dimension.set(width, height);
        markDirty(true);
        resize(width, height);
    }

    /**
     * Returns the box width
     *
     * @return the box width
     */
    public float getWidth() {
        return dimension.x;
    }

    /**
     * Sets the box width
     *
     * @param value the box width
     */
    public void setWidth(float value) {
        sizeCache.set(value, dimension.y);
        setDimension(sizeCache);
    }

    /**
     * Returns the box height
     *
     * @return the box height
     */
    public float getHeight() {
        return dimension.y;
    }

    /**
     * Sets the box height
     *
     * @param value the box height
     */
    public void setHeight(float value) {
        sizeCache.set(dimension.x, value);
        setDimension(sizeCache);
    }

    @Override
    public void setTexture(TextureRegion value) {
        super.setTexture(value);
        updateScale();
    }

    /**
     * Reset the polygon vertices in the shape to match the dimension.
     */
    private void resize(float width, float height) {
        updateScale();
        // Make the box with the center in the center
        vertices[0] = -width / 2.0f;
        vertices[1] = -height / 3.0f;
        vertices[2] = 0.0f;
        vertices[3] = height / 4.0f;
        vertices[4] = width / 2.0f;
        vertices[5] = -height / 3.0f;
//		vertices[6] =  width/2.0f;
//		vertices[7] = -height/2.0f;
        triangleShape.set(vertices);
        circleShape.setPosition(new Vector2(0.0f, width / 4.0f));
        circleShape.setRadius(width / 3.0f);
    }

    @Override
    public void setDrawScale(float x, float y) {
        super.setDrawScale(x, y);
        updateScale();
    }

    @Override
    public void setDrawScale(Vector2 value) {
        super.setDrawScale(value);
        updateScale();
    }

    private void updateScale() {
        if (texture != null) {
            System.out.println("HUH " + sx + ", " + sy);

            setTextureScaleX(drawScale.x * dimension.x / texture.getRegionWidth());
            setTextureScaleY(drawScale.y * dimension.y / texture.getRegionHeight());
        }
    }

    /**
     * Create new fixtures for this body, defining the shape
     * <p>
     * This is the primary method to override for custom physics objects
     */
    protected void createFixtures() {
        if (body == null) {
            return;
        }

        releaseFixtures();

        // Create the fixture
        fixture.shape = circleShape;
        fixtureTri.shape = triangleShape;
        geometry = body.createFixture(fixture);
        geometryTri = body.createFixture(fixtureTri);
        markDirty(false);
    }

    /**
     * Release the fixtures for this body, reseting the shape
     * <p>
     * This is the primary method to override for custom physics objects
     */
    protected void releaseFixtures() {
        if (geometry != null) {
            body.destroyFixture(geometry);
            body.destroyFixture(geometryTri);
            geometry = null;
            geometryTri = null;
        }
    }


    /**
     * Draws the outline of the physics body.
     * <p>
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        canvas.drawPhysics(circleShape, Color.YELLOW, getX() - (float) Math.sin(getAngle()) * getHeight() / 4.0f,
                getY() + (float) Math.cos(getAngle()) * getHeight() / 4.0f, drawScale.x, drawScale.y);
        canvas.drawPhysics(triangleShape, Color.YELLOW, getX(), getY(), getAngle(), drawScale.x, drawScale.y);
    }


}