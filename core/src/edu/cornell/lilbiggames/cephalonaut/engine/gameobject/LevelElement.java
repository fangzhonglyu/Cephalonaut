package edu.cornell.lilbiggames.cephalonaut.engine.gameobject;

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
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements.*;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.SimpleObstacle;
import edu.cornell.lilbiggames.cephalonaut.engine.parsing.Properties;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;


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
        ESPIKE,
        SPIKE,
        SPIKEBALL,
        REFILL,
        DIALOGUE_TRIGGER,
        SPARKLE,
        SPARKLEEND,
        ENGINE,
        BROKEN_ENGINE,
        BIG_BUTTON
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

    private static Texture sparksTexture, glassBarrierTexture,inkPackTexture;
    private static Texture wormholeTexture,blackHoleTexture,electricSpiketexture,boostPadTexture,spikeTexture,spikeBallTexture,engineTexture,brokenEngineTexture;
    private static Texture[] animationCache;
    private static Texture bigButtonTrigger;

    public static void collectAssets(AssetDirectory assetDirectory){
        wormholeTexture = assetDirectory.getEntry("A-wormhole-filmstrip.png",Texture.class);
        wormholeTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        blackHoleTexture = assetDirectory.getEntry("a:blackhole",Texture.class);
        blackHoleTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        boostPadTexture = assetDirectory.getEntry("GO-boostpad-filmstrip.png",Texture.class);
        boostPadTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        electricSpiketexture = assetDirectory.getEntry("electric-spikes.png",Texture.class);
        electricSpiketexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        spikeTexture = assetDirectory.getEntry("GO-spikes-film.png",Texture.class);
        spikeTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        spikeBallTexture = assetDirectory.getEntry("GO-spikeball-film.png",Texture.class);
        spikeBallTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        sparksTexture = assetDirectory.getEntry("UI-target-sparkle.png",Texture.class);
        sparksTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        engineTexture = assetDirectory.getEntry("engine_film.png",Texture.class);
        engineTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        brokenEngineTexture = assetDirectory.getEntry("engine_broken_film.png",Texture.class);
        brokenEngineTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        glassBarrierTexture = assetDirectory.getEntry("GO-glass-filmstrip.png",Texture.class);
        glassBarrierTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        inkPackTexture = assetDirectory.getEntry("inkPack-film.png",Texture.class);
        inkPackTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        bigButtonTrigger = assetDirectory.getEntry("big-button2.png",Texture.class);
        bigButtonTrigger.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        animationCache = new Texture[8];
        animationCache[0] = assetDirectory.getEntry("A-alex.png",Texture.class);
        animationCache[1] = assetDirectory.getEntry("A-angie.png",Texture.class);
        animationCache[2] = assetDirectory.getEntry("A-estelle.png",Texture.class);
        animationCache[3] = assetDirectory.getEntry("A-teddy.png",Texture.class);
        animationCache[4] = assetDirectory.getEntry("A-matias.png",Texture.class);
        animationCache[5] = assetDirectory.getEntry("A-oliver.png",Texture.class);
        animationCache[6] = assetDirectory.getEntry("A-barry.png",Texture.class);
        animationCache[7] = assetDirectory.getEntry("Arrow-sheet.png",Texture.class);
        for(int i = 0;i<8;i++){
            animationCache[i].setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
    }

    public static LevelElement create(Def def) {
        switch (def.element) {
            case BLACK_HOLE:
                return new LEBlackHole(def, new FilmStrip(blackHoleTexture, 1, 8));
            case BOOST_PAD:
                return new LEBoostPad(def, new FilmStrip(boostPadTexture,1,7));
            case DOOR:
                return new LETriggerable(def);
            case BUTTON:
                return new LETrigger(def);
            case BIG_BUTTON:
                return new LETrigger(def,new FilmStrip(bigButtonTrigger,1,1));
            case WORMHOLE:
                return new LEWormHole(def, new FilmStrip(wormholeTexture,1,24));
            case GLASS_BARRIER:
                return new LEGlassBarrier(def, new FilmStrip(glassBarrierTexture, 1, 13));
            case DIALOGUE_TRIGGER:
                return new LEDialogueTrigger(def);
            case START:
                return new LEStart(def);
            case ESPIKE:
                return new LEAnimated(def,new FilmStrip(electricSpiketexture,1,8),7, false);
            case SPIKE:
                return new LEAnimated(def,new FilmStrip(spikeTexture,1,9),7, false);
            case SPARKLE:
                return new LEAnimated(def,new FilmStrip(sparksTexture,1,6),5, true);
            case SPARKLEEND:
                return new LEAnimated(def,new FilmStrip(sparksTexture,1,6),4, false);
            case SPIKEBALL:
                return new LEAnimated(def,new FilmStrip(spikeBallTexture,1,7),7, false);
            case ENGINE:
                return new LEAnimated(def,new FilmStrip(engineTexture,1,6),5,false);
            case BROKEN_ENGINE:
                return new LEAnimated(def,new FilmStrip(brokenEngineTexture,1,6),5,false);
            case REFILL:
                return new LEInkPack(def,new FilmStrip(inkPackTexture,2,5));
            default:
                JsonValue anim = def.properties.get("animation");
                if(anim!=null)
                    return new LEAnimated(def, new FilmStrip(animationCache[anim.getInt("textureNum",0)],anim.getInt("rows",1),anim.getInt("cols",1)),anim.getFloat("update",7f),false);
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
//		displayFont = directory.getEntry( "retro", BitmapFont.class);
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