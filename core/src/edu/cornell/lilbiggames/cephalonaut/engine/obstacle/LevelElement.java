package edu.cornell.lilbiggames.cephalonaut.engine.obstacle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;

import javax.xml.validation.Validator;


public class LevelElement extends SimpleObstacle {
    /** Shape information for this circle */
    protected Shape shape;
    /** A cache value for the fixture (for resizing) */
    private Fixture geometry;

    private static final float BLACK_HOLE_RADIUS = .5f;
    private static final float METEOR_RADIUS = .5f;
    private static final float BOUNCY_WALL_RESTITUTION = 2.0f;
    private static final float BOX_HEIGHT = 1f;
    private static final float BOX_WIDTH = 1f;
    private static final float BUTTON_RADIUS = .25f;
    private static final float DOOR_HEIGHT = 1f;
    private static final float DOOR_WIDTH = .5f;

    private static TextureRegion earthTexture;
    private static TextureRegion octopusTexture;
    private static TextureRegion crosshairTexture;

    private static int black_hole_count = 0;
    private static int meteor_count = 0;
    private static int wall_count = 0;
    private static int boost_count = 0;
    private static int button_count = 0;
    private static int door_count = 0;
    private static int misc_count = 0;

    public enum ELEMENT {
        BLACK_HOLE,
        FLYING_METEOR,
        WALL,
        BOUNCY_WALL,
        BOOST_PAD,
        BUTTON,
        DOOR,
        MISC_POLY
    }

    public enum DIRECTION {
        UP,
        LEFT,
        DOWN,
        RIGHT
    }

    private ELEMENT element;
    private LevelElement activatee;
    private DIRECTION direction;
    private Vector2 original_pos;
    private boolean inContact;
    private boolean activated;
    private boolean opened;
    private float radius;
    private float width;
    private float height;
//    private float[] vertices;

    /**
     * Returns the radius of this circle
     *
     * @return the radius of this circle
     */
    public float getRadius() {
        return shape.getRadius();
    }

    /**
     * Sets the radius of this circle
     *
     * @param value  the radius of this circle
     */
    public void setRadius(float value) {
        shape.setRadius(value);
        markDirty(true);
    }

    public boolean getActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean getOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public boolean getInContact() {
        return inContact;
    }

    public void setInContact(boolean inContact) {
        this.inContact = inContact;
        if(inContact) {
            switch (element) {
                case BUTTON:
                    activate();
                    break;
                default:
                    break;
            }
        }
    }

    public void activate() {
        activated = true;
        switch(element) {
            case BUTTON:
                setTint(Color.GREEN);
                if(activatee != null) {
                    activatee.activate();
                }
                break;
            default:
                break;
        }
    }

    public Vector2 getOriginalPos() {return original_pos;}

    public static void gatherAssets(AssetDirectory directory) {
        // Allocate the tiles
        earthTexture = new TextureRegion(directory.getEntry( "earth", Texture.class ));
        octopusTexture = new TextureRegion(directory.getEntry( "octopus", Texture.class ));
        crosshairTexture = new TextureRegion(directory.getEntry("crosshair", Texture.class));
//		displayFont = directory.getEntry( "shared:retro" ,BitmapFont.class);
    }

    public ELEMENT getElement() {
        return element;
    }

    /**
     * Sets the radius of this circle
     *
     * @param element  the radius of this circle
     */
    public void setElement(ELEMENT element) {
        this.element = element;
    }

    /**
     * Creates a new circle at the origin.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param element	The wheel radius
     */


    public LevelElement(Vector2 scale, ELEMENT element) {
        this(0, 0, 0, scale, element);
    }


    public LevelElement(float x, float y, float[] vertices, float angle, Vector2 scale, ELEMENT element) {
        super(x,y);
        inContact = false;
        this.element = element;
        original_pos = new Vector2(x, y);
        setDrawScale(scale);
        setAngle(angle);
        geometry = null;
        activated = false;
        switch (element) {
            case MISC_POLY:
                createMiscPoly(scale, vertices);
            default:
                break;
        }
    }

    public LevelElement(float x, float y, float radius, float angle, Vector2 scale, ELEMENT element) {
        super(x,y);
        inContact = false;
        this.element = element;
        original_pos = new Vector2(x, y);
        setDrawScale(scale);
        setAngle(angle);
        geometry = null;
        activated = false;
        switch (element) {
            case BLACK_HOLE:
                createBlackHole(scale, radius);
                break;
            case FLYING_METEOR:
                createFlyingMeteor(scale, radius);
                break;
            case BUTTON:
                createButton(scale, radius);
                break;
            default:
                break;
        }
    }

    public LevelElement(float x, float y, float width, float height, float angle, Vector2 scale, ELEMENT element) {
        super(x,y);
        inContact = false;
        this.element = element;
        original_pos = new Vector2(x, y);
        setDrawScale(scale);
        setAngle(angle);
        geometry = null;
        activated = false;
        switch (element) {
            case WALL:
                createWall(scale, width, height);
                break;
            case BOUNCY_WALL:
                createBouncyWall(scale, width, height);
                break;
            case BOOST_PAD:
                createBoostPad(scale, width, height);
                break;
            case DOOR:
                createDoor(scale, width, height);
            default:
                break;
        }
    }


    /**
     * Creates a new circle object.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param x 		Initial x position of the circle center
     * @param y  		Initial y position of the circle center
     * @param element	The wheel radius
     */
    public LevelElement(float x, float y, float angle, Vector2 scale, ELEMENT element) {
        super(x,y);
        inContact = false;
        this.element = element;
        original_pos = new Vector2(x, y);
        setDrawScale(scale);
        setAngle(angle);
        geometry = null;
        activated = false;
        switch (element) {
            case BLACK_HOLE:
                createBlackHole(scale);
                break;
            case FLYING_METEOR:
                createFlyingMeteor(scale);
                break;
            case WALL:
                createWall(scale);
                break;
            case BOUNCY_WALL:
                createBouncyWall(scale);
                break;
            case BOOST_PAD:
                createBoostPad(scale);
                break;
            case BUTTON:
                createButton(scale);
                break;
            case DOOR:
                createDoor(scale);
            default:
                break;
        }
    }

    private void setVertices(float[] vertices) {
        // Make the box with the center in the center
        float xMin = vertices[0];
        float xMax = vertices[0];
        float yMin = vertices[1];
        float yMax = vertices[1];
        for(int i = 0; i < vertices.length; i += 2) {
            if(vertices[i] < xMin) {
                xMin = vertices[i];
            }
            if(vertices[i] > xMax) {
                xMax = vertices[i];
            }
        }
        for(int i = 1; i < vertices.length; i += 2) {
            if(vertices[i] < yMin) {
                yMin = vertices[i];
            }
            if(vertices[i] > yMax) {
                yMax = vertices[i];
            }
        }
        width = xMax - xMin;
        height = yMax - yMin;
        PolygonShape temp = new PolygonShape();
        temp.set(vertices);
        shape = temp;
    }

    private void createMiscPoly(Vector2 scale, float[] vertices) {
        geometry = null;
        setVertices(vertices);

        setGrapple(true);
        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(0);
        setFriction(0);
        setRestitution(0.3f);
        setDrawScale(scale);
        setTint(new Color(0.5f, 0.4f, 0.4f, 1));
//        setTexture(earthTexture);
//        setTextureScaleX(width * scale.x / earthTexture.getRegionWidth());
//        setTextureScaleY(height * scale.y / earthTexture.getRegionHeight());
        setName("misc"+misc_count);
        misc_count++;
    }

    public void setTextureBottomLeft(TextureRegion value) {
        texture = value;
        origin.set(0, 0);
    }

    private void createBlackHole(Vector2 scale) {
        createBlackHole(scale, BLACK_HOLE_RADIUS);
    }

    private void createBlackHole(Vector2 scale, float radius) {
        shape = new CircleShape();
        shape.setRadius(radius);
        setName("blackHole" + black_hole_count);
        setGrapple(false);
        setTexture(crosshairTexture);
        setBodyType(BodyDef.BodyType.StaticBody);
        setTextureScaleX(radius * 2 * scale.x / crosshairTexture.getRegionWidth());
        setTextureScaleY(radius * 2 * scale.y / crosshairTexture.getRegionHeight());
        setTint(Color.YELLOW);
        black_hole_count++;
    }

    private void createFlyingMeteor(Vector2 scale) {
        createFlyingMeteor(scale, METEOR_RADIUS);
    }

    private void createFlyingMeteor(Vector2 scale, float radius) {
        shape = new CircleShape();
        shape.setRadius(radius);
        setName("meteor" + meteor_count);
        setGrapple(true);
        setTexture(crosshairTexture);
        setBodyType(BodyDef.BodyType.KinematicBody);
        setDrawScale(scale);
        setTextureScaleX(radius * 2 * scale.x / crosshairTexture.getRegionWidth());
        setTextureScaleY(radius * 2 * scale.y / crosshairTexture.getRegionHeight());
        setTint(Color.PURPLE);
        meteor_count++;
        this.direction = DIRECTION.RIGHT;
    }

    private void createBoostPad(Vector2 scale) {
        createBoostPad(scale, BOX_WIDTH, BOX_HEIGHT);
    }

    private void createBoostPad(Vector2 scale, float width, float height) {
//        bodyinfo.position.set(original_pos.x + width / 2, original_pos.y + height / 2);
        geometry = null;
        boxResize(width, height);
        direction = DIRECTION.RIGHT;
        setGrapple(false);
        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(0);
        setFriction(0);
        setSensor(true);
        setDrawScale(scale);
        setTint(Color.YELLOW);
        setTexture(earthTexture);
        setTextureScaleX(width * scale.x / earthTexture.getRegionWidth());
        setTextureScaleY(height * scale.y / earthTexture.getRegionHeight());
        setName("boost"+boost_count);
        boost_count++;
    }

    private void boxResize(float width, float height) {
        // Make the box with the center in the center
        float[] vertices = new float[8];
        vertices[0] = -width/2.0f;
        vertices[1] = -height/2.0f;
        vertices[2] = -width/2.0f;
        vertices[3] =  height/2.0f;
        vertices[4] =  width/2.0f;
        vertices[5] =  height/2.0f;
        vertices[6] =  width/2.0f;
        vertices[7] = -height/2.0f;
        PolygonShape temp = new PolygonShape();
        temp.set(vertices);
        shape = temp;
    }

    private void createWall(Vector2 scale) {
        createWall(scale, BOX_WIDTH, BOX_HEIGHT);
    }

    private void createWall(Vector2 scale, float width, float height) {
//        bodyinfo.position.set(original_pos.x + width / 2, original_pos.y + height / 2);
        geometry = null;
        boxResize(width, height);

        setGrapple(true);
        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(0);
        setFriction(0);
        setRestitution(0.3f);
        setDrawScale(scale);
        setTint(new Color(0.5f, 0.4f, 0.4f, 1));
        setTexture(earthTexture);
        setTextureScaleX(width * scale.x / earthTexture.getRegionWidth());
        setTextureScaleY(height * scale.y / earthTexture.getRegionHeight());
        setName("wall"+wall_count);
        wall_count++;
    }

    private void createBouncyWall(Vector2 scale) {
        createBouncyWall(scale, BOX_WIDTH, BOX_HEIGHT);
    }

    private void createBouncyWall(Vector2 scale, float width, float height) {
        createWall(scale, width, height);
        setRestitution(BOUNCY_WALL_RESTITUTION);
        setTint(Color.CYAN);
        setName(getName() + "bouncy");
    }

    private void createButton(Vector2 scale) {
        createButton(scale, BUTTON_RADIUS);
    }

    private void createButton(Vector2 scale, float radius) {
        shape = new CircleShape();
        shape.setRadius(radius);
//        bodyinfo.position.set(BOX_SIZE * original_pos.x + BOX_SIZE / 2, BOX_SIZE * original_pos.y);
//        geometry = null;
//        boxResize(BOX_SIZE/2, BOX_SIZE/2);

        setGrapple(true);
        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(0);
        setFriction(0);
        setRestitution(0.5f);
        setDrawScale(scale);
        setTint(Color.RED);
        setTexture(earthTexture);
        setTextureScaleX(radius * 2 * scale.x / earthTexture.getRegionWidth());
        setTextureScaleY(radius * 2 * scale.y / earthTexture.getRegionWidth());
        setName("button"+button_count);
        button_count++;
    }

    private void createDoor(Vector2 scale) {
        createDoor(scale, DOOR_WIDTH, DOOR_HEIGHT);
    }

    private void createDoor(Vector2 scale, float width, float height) {
//        bodyinfo.position.set(20, 8);
//        bodyinfo.position.set(original_pos.x + width / 2, original_pos.y + height / 2);
        geometry = null;
        boxResize(width, height);
        setGrapple(true);
        setBodyType(BodyDef.BodyType.KinematicBody);
        setDensity(0);
        setFriction(0);
        setRestitution(0.3f);
        setDrawScale(scale);
        setTint(Color.PINK);
        setTexture(earthTexture);
        setTextureScaleX(width * scale.x / earthTexture.getRegionHeight());
        setTextureScaleY(height * scale.y / earthTexture.getRegionHeight());
        setName("door"+door_count);
        door_count++;
    }

    public void setDirection(DIRECTION direction) {
        this.direction = direction;
    }

    public DIRECTION getDirection() {
        return direction;
    }

    public void setActivatee(LevelElement element) {
        activatee = element;
    }

    public LevelElement getActivatee() {
        return activatee;
    }

    /**
     * Create new fixtures for this body, defining the shape
     *
     * This is the primary method to override for custom physics objects
     */
    protected void createFixtures() {
        if (body == null) {
            return;
        }

        releaseFixtures();

        // Create the fixture
        fixture.shape = shape;
        geometry = body.createFixture(fixture);
        markDirty(false);
    }

    /**
     * Release the fixtures for this body, reseting the shape
     *
     * This is the primary method to override for custom physics objects
     */
    protected void releaseFixtures() {
        if (geometry != null) {
            body.destroyFixture(geometry);
            geometry = null;
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
        switch(element) {
            case BLACK_HOLE:
                canvas.drawPhysics((CircleShape) shape,Color.YELLOW,getX(),getY(),drawScale.x,drawScale.y);
                break;
            case FLYING_METEOR:
                canvas.drawPhysics((CircleShape) shape,Color.YELLOW,getX(),getY(),drawScale.x,drawScale.y);
                break;
            case WALL:
                canvas.drawPhysics((PolygonShape) shape,Color.YELLOW,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
                break;
            case BOUNCY_WALL:
                canvas.drawPhysics((PolygonShape) shape,Color.YELLOW,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
                break;
            case BUTTON:
                canvas.drawPhysics((CircleShape) shape,Color.YELLOW,getX(),getY(),drawScale.x,drawScale.y);
                break;
            case DOOR:
                canvas.drawPhysics((PolygonShape) shape,Color.YELLOW,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
                break;
            case MISC_POLY:
                canvas.drawPhysics((PolygonShape) shape,Color.YELLOW,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
                break;
            default:
                break;
        }
//        canvas.drawPhysics(shape,Color.YELLOW,getX(),getY(),drawScale.x,drawScale.y);
    }
}