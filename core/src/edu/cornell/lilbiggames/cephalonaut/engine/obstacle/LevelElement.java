package edu.cornell.lilbiggames.cephalonaut.engine.obstacle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
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
        MISC_POLY,
        FINISH
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
    private boolean inContact = false;
    private boolean activated = false;
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
        if (inContact) {
            if (element == ELEMENT.BUTTON) {
                activate();
            }
        }
    }

    public void activate() {
        activated = true;
        if (element == ELEMENT.BUTTON) {
            setTint(Color.GREEN);
            if (activatee != null) {
                activatee.activate();
            }
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

    public void setElement(String element) {
        switch (element) {
            case "Wall":
                this.element = ELEMENT.WALL;
                break;
            case "Black Hole":
                this.element = ELEMENT.BLACK_HOLE;
                break;
            case "Boost Pad":
                this.element = ELEMENT.BOOST_PAD;
                break;
            case "Button":
                this.element = ELEMENT.BUTTON;
                break;
            case "Door":
                this.element = ELEMENT.DOOR;
                break;
            case "Finish":
                this.element = ELEMENT.FINISH;
                break;
            default:
                System.out.printf("WARNING: Unknown LevelElement type '%s'\n", element);
                this.element = ELEMENT.WALL;
                break;
        }
    }

    private void setProperties(JsonValue properties) {
        if (properties == null) {
            element = ELEMENT.WALL;
            setDensity(0);
            setBodyType(BodyDef.BodyType.StaticBody);
            setRestitution(0.3f);
            setGrapple(true);
        } else {
            setElement(properties.getString("type", "Wall"));
            setGrapple(properties.getBoolean("canGrappleOn", true));
            setDensity(properties.getFloat("density", 0));
            setRestitution(properties.getFloat("restitution", 0.3f));
            boolean isStatic = properties.getBoolean("isStatic", true);
            setBodyType(isStatic ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
        }
    }

    public LevelElement(int x, int y, JsonValue tile) {
        super(x,y);
        setName("unnamed");
        original_pos = new Vector2(x, y);

        JsonValue properties = tile.get("properties");
        JsonValue elementProperties = null;
        if (properties != null) {
            for (JsonValue property : properties.iterator()) {
                String name = property.getString("name", "");
                String propertyType = property.getString("propertytype", "");
                if (name.equals("body") && propertyType.equals("GameObject")) {
                    elementProperties = property.get("value");
                }
            }
        }
        setProperties(elementProperties);

        JsonValue collisionObject = tile.get("objectgroup").get("objects").get(0);
        JsonValue polygon = collisionObject.get("polygon");
        if (polygon != null) {
            // TODO: Replace 16 with less magic number

            float ox = collisionObject.getFloat("x");
            float oy = collisionObject.getFloat("y");
            float[] vertices = new float[2 * polygon.size];
            for (int i = 0; i < polygon.size; i++) {
                vertices[2 * i] = (ox + polygon.get(i).getFloat("x")) / 16;
                vertices[2 * i + 1] = 1f - (oy + polygon.get(i).getFloat("y")) / 16;
            }
            setVertices(vertices);
        } else {
            // TODO: Replace. This creates square polygon when no polygon is found

            float[] vertices = {0, 0, 1, 0, 1, 1, 0, 1};
            setVertices(vertices);
        }

        switch (element) {
            case BLACK_HOLE:
                createBlackHole();
                break;
            case FLYING_METEOR:
                createFlyingMeteor();
                break;
            case BOOST_PAD:
                createBoostPad();
                break;
            case BUTTON:
                createButton();
                break;
            case DOOR:
                createDoor();
                break;
            case FINISH:
                createFinish();
                break;
            default:
                break;
        }
    }

    public void setTexture(TextureRegion value) {
        texture = value;
        origin.set(0, 0);
//        origin.set(texture.getRegionWidth() / 2.0f, texture.getRegionHeight() / 2.0f);
//        setTextureScaleX(drawScale.x / texture.getRegionWidth());
//        setTextureScaleY(drawScale.y / texture.getRegionHeight());
    }

    public void setDrawScale(Vector2 value) {
        super.setDrawScale(value);
        setTextureScaleX(drawScale.x / texture.getRegionWidth());
        setTextureScaleY(drawScale.y / texture.getRegionHeight());
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

    public void setTextureBottomLeft(TextureRegion value) {
        texture = value;
        origin.set(0, 0);
    }

    private void createBlackHole() {
        createBlackHole(BLACK_HOLE_RADIUS);
    }

    private void createBlackHole(float radius) {
        shape = new CircleShape();
        shape.setRadius(radius);
        setName("blackHole" + black_hole_count);
        setGrapple(false);
        setTexture(crosshairTexture);
        setBodyType(BodyDef.BodyType.StaticBody);
//        setTextureScaleX(radius * 2 * scale.x / crosshairTexture.getRegionWidth());
//        setTextureScaleY(radius * 2 * scale.y / crosshairTexture.getRegionHeight());
        setTint(Color.YELLOW);
        black_hole_count++;
    }

    private void createFlyingMeteor() {
        createFlyingMeteor(METEOR_RADIUS);
    }

    private void createFlyingMeteor(float radius) {
        shape = new CircleShape();
        shape.setRadius(radius);
        setName("meteor" + meteor_count);
        setGrapple(true);
        setTexture(crosshairTexture);
        setBodyType(BodyDef.BodyType.KinematicBody);
//        setDrawScale(scale);
//        setTextureScaleX(radius * 2 * scale.x / crosshairTexture.getRegionWidth());
//        setTextureScaleY(radius * 2 * scale.y / crosshairTexture.getRegionHeight());
        setTint(Color.PURPLE);
        meteor_count++;
        this.direction = DIRECTION.RIGHT;
    }

    private void createBoostPad() {
        createBoostPad(BOX_WIDTH, BOX_HEIGHT);
    }

    private void createBoostPad(float width, float height) {
//        bodyinfo.position.set(original_pos.x + width / 2, original_pos.y + height / 2);
        geometry = null;
        boxResize(width, height);
        direction = DIRECTION.RIGHT;
        setGrapple(false);
        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(0);
        setFriction(0);
        setSensor(true);
//        setDrawScale(scale);
        setTint(Color.YELLOW);
        setTexture(earthTexture);
//        setTextureScaleX(width * scale.x / earthTexture.getRegionWidth());
//        setTextureScaleY(height * scale.y / earthTexture.getRegionHeight());
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
        bodyinfo.position.set(original_pos.x + width / 2, original_pos.y + height / 2);
    }

    private void createButton() {
        createButton(BUTTON_RADIUS);
    }

    private void createButton(float radius) {
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
//        setDrawScale(scale);
        setTint(Color.RED);
        setTexture(earthTexture);
//        setTextureScaleX(radius * 2 * scale.x / earthTexture.getRegionWidth());
//        setTextureScaleY(radius * 2 * scale.y / earthTexture.getRegionWidth());
        setName("button"+button_count);
        button_count++;
    }

    private void createDoor() {
        createDoor(DOOR_WIDTH, DOOR_HEIGHT);
    }

    private void createDoor(float width, float height) {
//        bodyinfo.position.set(20, 8);
//        bodyinfo.position.set(original_pos.x + width / 2, original_pos.y + height / 2);
        geometry = null;
        boxResize(width, height);
        setGrapple(true);
        setBodyType(BodyDef.BodyType.KinematicBody);
        setDensity(0);
        setFriction(0);
        setRestitution(0.3f);
//        setDrawScale(scale);
        setTint(Color.PINK);
        setTexture(earthTexture);
//        setTextureScaleX(width * scale.x / earthTexture.getRegionHeight());
//        setTextureScaleY(height * scale.y / earthTexture.getRegionHeight());
        setName("door"+door_count);
        door_count++;
    }

    private void createFinish() {
        createBoostPad(BOX_WIDTH, BOX_HEIGHT);
    }

    private void createFinish(float width, float height) {
//        bodyinfo.position.set(original_pos.x + width / 2, original_pos.y + height / 2);
        geometry = null;
        boxResize(width, height);
        setGrapple(false);
        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(0);
        setFriction(0);
        setSensor(true);
//        setDrawScale(scale);
        setTint(Color.YELLOW);
        setTexture(earthTexture);
//        setTextureScaleX(width * scale.x / earthTexture.getRegionWidth());
//        setTextureScaleY(height * scale.y / earthTexture.getRegionHeight());
        setName("finish");
    }

    private void createFinish(float[] vertices) {
//        bodyinfo.position.set(original_pos.x + width / 2, original_pos.y + height / 2);
        geometry = null;
        setVertices(vertices);
        setGrapple(false);
        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(0);
        setFriction(0);
        setSensor(true);
//        setDrawScale(scale);
        setTint(Color.YELLOW);
        setTexture(earthTexture);
//        setTextureScaleX(width * scale.x / earthTexture.getRegionWidth());
//        setTextureScaleY(height * scale.y / earthTexture.getRegionHeight());
        setName("finish");
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