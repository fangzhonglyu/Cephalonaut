package edu.cornell.lilbiggames.cephalonaut.engine.obstacle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.EllipseShapeBuilder;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;

import javax.xml.validation.Validator;
import java.awt.*;


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

    private float width;
    private float height;

    private void updateScale() {
        if (texture == null) return;
        setTextureScaleX(drawScale.x * width / texture.getRegionWidth());
        setTextureScaleY(drawScale.y * height / texture.getRegionHeight());
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        updateScale();
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

    private void setDefaultProperties() {
        element = ELEMENT.WALL;
        setDensity(0);
        setBodyType(BodyDef.BodyType.StaticBody);
        setRestitution(0.3f);
        setGrapple(true);
    }

    // TODO: Move this (along some other parsing code I guess) somewhere else?
    public static Color argbToColor (String hex, Color color) {
        hex = hex.charAt(0) == '#' ? hex.substring(1) : hex;
        color.a = Integer.parseInt(hex.substring(0, 2), 16) / 255f;
        color.r = Integer.parseInt(hex.substring(2, 4), 16) / 255f;
        color.g = Integer.parseInt(hex.substring(4, 6), 16) / 255f;
        color.b = Integer.parseInt(hex.substring(6, 8), 16) / 255f;
        return color;
    }

    private void setProperties(JsonValue json) {
        JsonValue properties = json.get("properties");
        if (properties == null) return;

        JsonValue go_properties = null;
        for (JsonValue property : properties.iterator()) {
            String name = property.getString("name", "");
            String propertyType = property.getString("propertytype", "");
            if (name.equals("body") && propertyType.equals("GameObject")) {
                go_properties = property.get("value");
            }
        }
        if (go_properties == null) return;

        if (go_properties.has("type"))
            setElement(go_properties.getString("type"));
        if (go_properties.has("canGrappleOn"))
            setGrapple(go_properties.getBoolean("canGrappleOn"));
        if (go_properties.has("density"))
            setDensity(go_properties.getFloat("density"));
        if (go_properties.has("restitution"))
            setRestitution(go_properties.getFloat("restitution"));
        if (go_properties.has("isStatic"))
            setBodyType(go_properties.getBoolean("isStatic") ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
        if (go_properties.has("tint"))
            argbToColor(go_properties.getString("tint"), tint);
    }

    private LevelElement(float x, float y, float width, float height, TextureRegion texture) {
        super(x, y);
        //        original_pos = new Vector2(x, y);
        setName("unnamed");
        setDefaultProperties();
        setSize(width, height);
        setTexture(texture);
        setPosition(x - 0.5f + width / 2f, y - 0.5f + height / 2f);
    }

    public LevelElement(int x, int y, float width, float height, TextureRegion texture, JsonValue tile) {
        this(x, y, width, height, texture);

        setProperties(tile);

        JsonValue collisionObject = tile.get("objectgroup").get("objects").get(0);
        JsonValue polygon = collisionObject.get("polygon");
        if (polygon != null) {
            setPolygon(collisionObject, polygon);
        } else {
            // TODO: Replace. This creates square polygon when no polygon is found

            float[] vertices = {-0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f};
            setVertices(vertices);
        }

        finishSetup();
    }

    public LevelElement(JsonValue object, JsonValue objectType, TextureRegion texture) {
        this(object.getInt("x") / 16f,
                50 - object.getInt("y") / 16f,
                object.getFloat("width") / 16f,
                object.getFloat("height") / 16f,
                texture);

        setProperties(objectType);
        setProperties(object);

        JsonValue collisionObject = objectType.get("objectgroup").get("objects").get(0);
        JsonValue polygon = collisionObject.get("polygon");

        if (polygon != null) {
            setPolygon(collisionObject, polygon);
        } else {
            // TODO: Replace. This creates square polygon when no polygon is found

//            float[] vertices = {-0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f};
            float[] vertices = {-0.5f, -3f, 0.5f, -3f, 0.5f, 3f, -0.5f, 3f};
            setVertices(vertices);
        }

        finishSetup();
    }

    private void finishSetup() {
        switch (element) {
            case BLACK_HOLE:
                setName("blackHole" + black_hole_count++);
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

    private void setPolygon(JsonValue collisionObject, JsonValue polygon) {
        // TODO: Replace 16 with less magic number

        float ox = collisionObject.getFloat("x");
        float oy = collisionObject.getFloat("y");
        float[] vertices = new float[2 * polygon.size];

        float scaleX = 16f * width / texture.getRegionWidth();
        float scaleY = 16f * height / texture.getRegionHeight();

        for (int i = 0; i < polygon.size; i++) {
            vertices[2 * i] = scaleX * ((ox + polygon.get(i).getFloat("x")) / 16f - width / 2f);
            vertices[2 * i + 1] = scaleY * (height / 2f - (oy + polygon.get(i).getFloat("y")) / 16f);
        }

        setVertices(vertices);
    }

    public void setTexture(TextureRegion value) {
        texture = value;
        origin.set(value.getRegionWidth() / 2f, value.getRegionHeight() / 2f);
        updateScale();
    }

    public void setDrawScale(Vector2 value) {
        super.setDrawScale(value);
        updateScale();
    }

    private void setVertices(float[] vertices) {
        PolygonShape temp = new PolygonShape();
        temp.set(vertices);
        shape = temp;
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
        setSensor(true);
//        createBoostPad(BOX_WIDTH, BOX_HEIGHT);
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
        if (shape instanceof PolygonShape) {
            canvas.drawPhysics((PolygonShape) shape,Color.YELLOW,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
            return;
        }

        switch(element) {
            case BLACK_HOLE:
                CircleShape circleShape = (CircleShape) shape;
                canvas.drawPhysics(circleShape,Color.YELLOW,
                                getX() + circleShape.getPosition().x,getY() + circleShape.getPosition().y,
                                   drawScale.x,drawScale.y);
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