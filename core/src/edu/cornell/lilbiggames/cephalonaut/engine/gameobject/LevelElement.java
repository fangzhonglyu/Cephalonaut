package edu.cornell.lilbiggames.cephalonaut.engine.gameobject;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.ShortArray;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements.*;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.SimpleObstacle;
import edu.cornell.lilbiggames.cephalonaut.engine.parsing.Properties;


public class LevelElement extends SimpleObstacle {
    /** Triangles for this element */
    private PolygonShape[] triangles;

    protected boolean inContact = false;

    public enum Element {
        GLASS_BARRIER,
        BLACK_HOLE,
        FLYING_METEOR,
        WALL,
        BOUNCE_PAD,
        BOOST_PAD,
        BUTTON,
        DOOR,
        FINISH,
        WORMHOLE,
        MISC,
        START,
        SPIKE,
        REFILL
    }

    /** Type of element **/
    private final Element element;

    /** Size of element, used for texture and polygon resizing purposes **/
    protected float width;
    protected float height;

    public static class Def {
        public String name;
        public float x, y, vx, vy;
        public float width, height;
        public float angle;

        public Element element;
        public float density;
        public BodyDef.BodyType bodyType;
        public float restitution;
        public boolean isSensor;
        public boolean canGrapple;
        public Color tint;

        public float[] vertices;
        public TextureRegion texture;
        public TextureRegion triggerTexture;
        public Properties properties;
    }

    protected LevelElement(Def def) {
        super(def.x, def.y);
        setVX(def.vx);
        setVY(def.vy);
        setName(def.name);
        setSize(def.width, def.height);
        setAngle(def.angle);

        element = def.element;
        setDensity(def.density);
        setBodyType(def.bodyType);
        setRestitution(def.restitution);
        setSensor(def.isSensor);
        setGrapple(def.canGrapple);
        setTint(def.tint);

        setVertices(def.vertices);
        setTexture(def.texture);
    }

    public static LevelElement create(Def def) {
        switch (def.element) {
            case BLACK_HOLE:
                return new LEBlackHole(def);
            case BOOST_PAD:
                return new LEBoostPad(def);
            case DOOR:
                return new LETriggerable(def);
            case BUTTON:
                return new LETrigger(def);
            case WORMHOLE:
                return new LEWormHole(def);
            case GLASS_BARRIER:
                return new LEGlassBarrier(def);
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

    // TODO: Clean these next few functions up a bit.
    public boolean getInContact() {
        return inContact;
    }

    public void setInContact(boolean inContact) {
        boolean wasInContact = this.inContact;
        this.inContact = inContact;
        if (!wasInContact && this.inContact) contacted();
    }

    protected void contacted() {}

    public static void gatherAssets(AssetDirectory directory) {
        // Allocate the tiles
//		displayFont = directory.getEntry( "shared:retro" ,BitmapFont.class);
    }

    public Element getElement() {
        return element;
    }

    final static EarClippingTriangulator triangulator = new EarClippingTriangulator();
    private void setVertices(float[] vertices) {
        if (vertices == null) {
            triangles = new PolygonShape[0];
            return;
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
        float offsetX = canvas.getCameraX() * parallaxFactor.x / drawScale.x;
        float offsetY = canvas.getCameraY() * parallaxFactor.y / drawScale.y;
        for (PolygonShape tri : triangles) {
            canvas.drawPhysics(tri,Color.YELLOW,getX() + offsetX,getY() + offsetY,getAngle(),drawScale.x,drawScale.y);
        }
    }
}