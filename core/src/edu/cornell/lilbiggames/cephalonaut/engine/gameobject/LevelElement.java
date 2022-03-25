package edu.cornell.lilbiggames.cephalonaut.engine.gameobject;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ShortArray;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.SimpleObstacle;
import edu.cornell.lilbiggames.cephalonaut.engine.parsing.Properties;

import java.util.logging.Level;


public class LevelElement extends SimpleObstacle {
    /** Triangles for this element */
    private PolygonShape[] triangles;

    // TODO: We probably don't need shape and geometry? What's this about a resizing cache?
    protected Shape shape;
    /** A cache value for the fixture (for resizing) */
    private Fixture geometry;

    /** Door **/
    private Vector2 originalPos;
    private LevelElement activatee;
    private boolean inContact = false;
    private boolean activated = false;
    private boolean opened;

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

    /** Element counts for naming **/
    // TODO: Can probably replace by Tiled object names. Tiled can be named by their coordinates.
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
        BOUNCE_PAD,
        BOOST_PAD,
        BUTTON,
        DOOR,
        FINISH,
        MISC
    }

    /** Type of element **/
    private Element element;

    /** Size of element, used for texture and polygon resizing purposes **/
    private float width;
    private float height;

    public static class Def {
        public String name;
        public float x, y;
        public float width, height;
        public float angle;

        public Element element;
        public float density;
        public BodyDef.BodyType bodyType;
        public float restitution;
        public boolean canGrapple;
        public Color tint;

        public float[] vertices;
        public TextureRegion texture;
        public Properties properties;
    }

    protected LevelElement(Def def) {
        super(def.x, def.y);
        setName(def.name);
        setSize(def.width, def.height);
        setAngle(def.angle);

        element = def.element;
        setDensity(def.density);
        setBodyType(def.bodyType);
        setRestitution(def.restitution);
        setGrapple(def.canGrapple);
        setTint(def.tint);

        setVertices(def.vertices);
        setTexture(def.texture);
    }

    public static LevelElement create(Def def) {
        switch (def.element) {
            case BOOST_PAD:
                return new LEBoostPad(def);
            default:
                return new LevelElement(def);
        }
    }

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

//    private void finishSetup(JsonValue tile) {
//        JsonValue properties = tile.get("properties");
//
//        switch (element) {
//            case BLACK_HOLE:
//                // TODO: Get parameters like strength and attraction radius from JSON
//                setName("blackHole" + black_hole_count++);
//                break;
//            case FLYING_METEOR:
//                // TODO: Make work in level editor properly. Maybe you should make configurable spawners?
//                createFlyingMeteor();
//                break;
//            case BOOST_PAD:
//                createBoostPad(properties);
//                break;
//            case BUTTON:
//                // TODO: Make work in level editor properly
//                createButton();
//                break;
//            case DOOR:
//                // TODO: Make work in level editor properly
//                createDoor();
//                break;
//            case FINISH:
//                setName("finish");
//                setSensor(true);
//                break;
//            default:
//                break;
//        }
//    }

    final static EarClippingTriangulator triangulator = new EarClippingTriangulator();
    private void setVertices(float[] vertices) {
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
//        boxResize(width, height);
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