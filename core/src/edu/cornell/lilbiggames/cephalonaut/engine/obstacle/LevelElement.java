package edu.cornell.lilbiggames.cephalonaut.engine.obstacle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ShortArray;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;


public class LevelElement extends SimpleObstacle {
    /** Shape information for this circle */
    protected Shape shape;
    private PolygonShape[] triangles;
    /** A cache value for the fixture (for resizing) */
    private Fixture geometry;

    /** Boost pad **/
    private static final float DEFAULT_BOOST_FACTOR = 8f;
    private float boostPadFactor = 1;
    private float boostPadAngle = 0;

    /** Door **/
    private Vector2 originalPos;

    /** Misc. element stuff **/
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

    public enum Element {
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

    private Element element;
    private LevelElement activatee;
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

    public float getBoostPadFactor() {
        return boostPadFactor;
    }

    public float getBoostPadAngle() {
        return boostPadAngle;
    }

    public Vector2 getOriginalPos() {
        return originalPos;
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
            if (element == Element.BUTTON) {
                activate();
            }
        }
    }

    public void activate() {
        activated = true;
        if (element == Element.BUTTON) {
            setTint(Color.GREEN);
            if (activatee != null) {
                activatee.activate();
            }
        }
    }

    public static void gatherAssets(AssetDirectory directory) {
        // Allocate the tiles
        earthTexture = new TextureRegion(directory.getEntry( "earth", Texture.class ));
        octopusTexture = new TextureRegion(directory.getEntry( "octopus", Texture.class ));
        crosshairTexture = new TextureRegion(directory.getEntry("crosshair", Texture.class));
//		displayFont = directory.getEntry( "shared:retro" ,BitmapFont.class);
    }

    public Element getElement() {
        return element;
    }

    public void setElement(String element) {
        switch (element) {
            case "Wall":
                this.element = Element.WALL;
                break;
            case "Black Hole":
                this.element = Element.BLACK_HOLE;
                break;
            case "Boost Pad":
                this.element = Element.BOOST_PAD;
                break;
            case "Button":
                this.element = Element.BUTTON;
                break;
            case "Door":
                this.element = Element.DOOR;
                break;
            case "Finish":
                this.element = Element.FINISH;
                break;
            default:
                System.out.printf("WARNING: Unknown LevelElement type '%s'\n", element);
                this.element = Element.WALL;
                break;
        }
    }

    private void setDefaultProperties() {
        element = Element.WALL;
        setDensity(0);
        setBodyType(BodyDef.BodyType.StaticBody);
        setRestitution(0.3f);
        setGrapple(true);
    }

    // TODO: Move this (along some other parsing code I guess) somewhere else?
    public static Color argbToColor(String hex, Color color) {
        hex = hex.charAt(0) == '#' ? hex.substring(1) : hex;
        color.a = Integer.parseInt(hex.substring(0, 2), 16) / 255f;
        color.r = Integer.parseInt(hex.substring(2, 4), 16) / 255f;
        color.g = Integer.parseInt(hex.substring(4, 6), 16) / 255f;
        color.b = Integer.parseInt(hex.substring(6, 8), 16) / 255f;
        return color;
    }

    // Merges json [b] into json [a].
    private static JsonValue mergeJsons(JsonValue a, JsonValue b) {
        if (a == null) return b;
        if (b == null) return a;
        if (!a.isObject() || !b.isObject()) return a;

        for (JsonValue bChild : b) {
            JsonValue aChild = a.get(bChild.name);
            if (aChild != null) {
                mergeJsons(aChild, bChild);
            } else {
                a.addChild(bChild);
            }
        }

        return a;
    }

    private JsonValue findProperty(JsonValue properties, String name) {
        for (JsonValue property : properties) {
            if (property.getString("name").equals(name))
                return property.get("value");
        }
        return null;
    }

    private float findProperty(JsonValue properties, String name, float defaultValue) {
        JsonValue property = findProperty(properties, name);
        if (property == null) {
            return defaultValue;
        } else {
            return property.asFloat();
        }
    }

    private void setProperties(JsonValue json) {
        JsonValue properties = json.get("properties");
        if (properties == null) return;

        JsonValue go_properties = findProperty(properties, "body");
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
        setName("unnamed");
        setDefaultProperties();
        setSize(width, height);
        setTexture(texture);
        // Tiled coordinates origins are at the bottom-left, while we want centered origins
        setPosition(x - 0.5f + width / 2f, y - 0.5f + height / 2f);
        originalPos = new Vector2(getPosition());
    }

    public LevelElement(float x, float y, float width, float height, TextureRegion texture, JsonValue object) {
        this(x, y, width, height, texture);

        setProperties(object);

        JsonValue collisionObject = object.get("objectgroup").get("objects").get(0);
        JsonValue polygon = collisionObject.get("polygon");
        if (polygon != null) {
            // Create PolygonShape from Tiled polygon
            setPolygon(collisionObject, polygon);
        } else {
            // Create PolygonShape from Tiled rectangle
            float cWidth = collisionObject.getFloat("width");
            float cHeight = collisionObject.getFloat("height");
            float[] vertices = {0, 0, cWidth, 0, cHeight, cWidth, 0, cHeight};
            setPolygon(collisionObject, vertices);
        }

        finishSetup(object);
    }

    public LevelElement(JsonValue object, JsonValue objectType, TextureRegion texture) {
        this(object.getInt("x") / 16f,
                50 - object.getInt("y") / 16f, // TODO: This 50 is the map height... Should be more flexibile.
                object.getFloat("width") / 16f,
                object.getFloat("height") / 16f,
                texture,
                mergeJsons(object, objectType));

        // Need to account that rotation is around the bottom-left origin in Tiled instead of the center origin here
        float rotation = -MathUtils.degreesToRadians * object.getFloat("rotation", 0);
        setAngle(rotation);
        Vector2 origin = new Vector2(object.getInt("x") / 16f, 50 - object.getInt("y") / 16f);
        setPosition(getPosition().rotateAroundRad(origin, rotation));
    }

    private void finishSetup(JsonValue tile) {
        JsonValue properties = tile.get("properties");

        switch (element) {
            case BLACK_HOLE:
                setName("blackHole" + black_hole_count++);
                break;
            case FLYING_METEOR:
                // TODO: Make work in level editor properly. Maybe you should make configurable spawners?
                createFlyingMeteor();
                break;
            case BOOST_PAD:
                createBoostPad(properties);
                break;
            case BUTTON:
                // TODO: Make work in level editor properly
                createButton();
                break;
            case DOOR:
                // TODO: Make work in level editor properly
                createDoor();
                break;
            case FINISH:
                setName("finish");
                setSensor(true);
                break;
            default:
                break;
        }
    }

    private void setPolygon(JsonValue collisionObject, JsonValue polygon) {
        float[] vertices = new float[2 * polygon.size];
        for (int i = 0; i < polygon.size; i++) {
            vertices[2 * i] = polygon.get(i).getFloat("x");
            vertices[2 * i + 1] = polygon.get(i).getFloat("y");
        }
        setPolygon(collisionObject, vertices);
    }

    final static EarClippingTriangulator triangulator = new EarClippingTriangulator();
    private void setPolygon(JsonValue collisionObject, float[] vertices) {
        float ox = collisionObject.getFloat("x");
        float oy = collisionObject.getFloat("y");

        float scaleX = width / texture.getRegionWidth();
        float scaleY = height / texture.getRegionHeight();

        // Scale and center vertices. The y-axis is flipped since Tiled has origin on top-left instead of bottom-left.
        for (int i = 0; i < vertices.length; i += 2) {
            vertices[i] = scaleX * (ox + vertices[i]) - width / 2f;
            vertices[i + 1] = height / 2f - scaleY * (oy + vertices[i + 1]);
        }

        // Triangulation of n-vertex polygons into 3-vertex triangles
        // For performance reasons and because box2d throws a hissy fit whenever we use polygons with over 8 vertices
        ShortArray tris = triangulator.computeTriangles(vertices);
        triangles = new PolygonShape[tris.size / 3];
        for (int i = 0; i < tris.size; i += 3) {
            float[] tri_vertices = new float[] {
                    vertices[2 * tris.get(i    )], vertices[2 * tris.get(i    ) + 1],
                    vertices[2 * tris.get(i + 1)], vertices[2 * tris.get(i + 1) + 1],
                    vertices[2 * tris.get(i + 2)], vertices[2 * tris.get(i + 2) + 1]
            };
            PolygonShape poly = new PolygonShape();
            poly.set(tri_vertices);
            triangles[i / 3] = poly;
        }
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
//        this.direction = DIRECTION.RIGHT;
    }


    private void createBoostPad(JsonValue properties) {
        this.boostPadFactor = findProperty(properties, "boostPadFactor", DEFAULT_BOOST_FACTOR);
        this.boostPadAngle = -MathUtils.degreesToRadians * findProperty(properties, "boostPadAngle", 0f);
        setSensor(true);
        setName("boost" + boost_count);
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
//        bodyinfo.position.set(original_pos.x + width / 2, original_pos.y + height / 2);
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

        // Create the fixtures
        for (Shape shape : triangles) {
            fixture.shape = shape;
            body.createFixture(fixture);
        }
        markDirty(false);
    }

    /**
     * Release the fixtures for this body, reseting the shape
     *
     * This is the primary method to override for custom physics objects
     */
    protected void releaseFixtures() {
        for (Fixture fixture : body.getFixtureList()) {
            body.destroyFixture(fixture);
        }
//        if (geometry != null) {
//            body.destroyFixture(geometry);
//            geometry = null;
//        }
    }

    /**
     * Draws the outline of the physics body.
     *
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        for (PolygonShape tri : triangles) {
            canvas.drawPhysics(tri,Color.YELLOW,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
        }
    }
}