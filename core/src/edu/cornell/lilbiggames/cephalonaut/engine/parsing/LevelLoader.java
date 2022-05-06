package edu.cornell.lilbiggames.cephalonaut.engine.parsing;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.GameObject;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.ImageObject;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;

import java.util.HashMap;
import java.util.Map;


public class LevelLoader {

    enum TiledFile {
        METEOR_TILESET,
        SPACESHIP_TILESET,
        OBJECTS
    }

    private TiledFile stringToTiledFile(String str) {
        if (str.contains("space")) {
            return TiledFile.SPACESHIP_TILESET;
        } else if (str.contains("objects")) {
            return TiledFile.OBJECTS;
        } else if (str.contains("tileset")) {
            return TiledFile.METEOR_TILESET;
        } else {
            throw new IllegalArgumentException("Unknown tiled file '" + str + "' required\n");
        }
    }

    final private AssetDirectory assetDirectory;
    final private Map<TiledFile, Map<Integer, JsonValue>> map = new HashMap<>();
    final private Map<TiledFile, Map<Integer, TextureRegion>> textures = new HashMap<>();

    public LevelLoader(AssetDirectory assetDirectory) {
        this.assetDirectory = assetDirectory;
        LevelElement.collectAssets(assetDirectory);
        loadTileset("tile-tileset", TiledFile.METEOR_TILESET);
        loadTileset("space-tileset", TiledFile.SPACESHIP_TILESET);
        loadTileset("object-tileset", TiledFile.OBJECTS);
    }

    private void loadTileset(String asset, TiledFile tiledFile) {
        JsonValue tileset = assetDirectory.getEntry(asset, JsonValue.class);
        Map<Integer, JsonValue> fileMap = new HashMap<>();
        Map<Integer, TextureRegion> textureMap = new HashMap<>();

        for (JsonValue tile : tileset.get("tiles")) {
            fileMap.put(tile.getInt("id"), tile);
        }

        if (tileset.has("image")) {
            // Get atlas and set up texture regions
            Texture atlas = new Texture(tileset.getString("image"));
            atlas.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

            int tileSize = tileset.getInt("tilewidth");
            int columns = tileset.getInt("columns");
            for (JsonValue tile : tileset.get("tiles")) {
                int id = tile.getInt("id");
                int x = (id % columns)*tileSize;
                int y = (id / columns)*tileSize;
                textureMap.put(id, new TextureRegion(atlas, x, y, tileSize, tileSize));
            }
        } else {
            // Get each tile's individual textures at set up regions
            for (JsonValue tile : tileset.get("tiles")) {
                Texture fullTexture = assetDirectory.getEntry(tile.getString("image"), Texture.class);
                if (fullTexture == null) continue;
                fullTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                textureMap.put(tile.getInt("id"), new TextureRegion(fullTexture));
            }
        }

        map.put(tiledFile, fileMap);
        textures.put(tiledFile, textureMap);
    }

    public AssetDirectory getAssetDirectory() {
        return assetDirectory;
    }

    private LevelElement.Element stringToElementType(String element) {
        if (element == null) return LevelElement.Element.MISC;

        switch (element) {
            case "Wall":
                return LevelElement.Element.WALL;
            case "Black Hole":
                return LevelElement.Element.BLACK_HOLE;
            case "Boost Pad":
                return LevelElement.Element.BOOST_PAD;
            case "Button":
                return LevelElement.Element.BUTTON;
            case "Door":
                return LevelElement.Element.DOOR;
            case "Finish":
                return LevelElement.Element.FINISH;
            case "Wormhole":
                return LevelElement.Element.WORMHOLE;
            case "Start":
                return LevelElement.Element.START;
            case "Glass Barrier":
                return LevelElement.Element.GLASS_BARRIER;
            case "Spike":
                return LevelElement.Element.SPIKE;
            case "ESpike":
                return LevelElement.Element.ESPIKE;
            case "SpikeBall":
                return LevelElement.Element.SPIKEBALL;
            case "Refill":
                return LevelElement.Element.REFILL;
            case "Dialogue Trigger":
                return LevelElement.Element.DIALOGUE_TRIGGER;
            default:
                System.out.printf("WARNING: Unknown LevelElement type '%s'\n", element);
                return LevelElement.Element.WALL;
        }
    }

    private BodyDef.BodyType stringToBodyType(String bodyType) {
        if (bodyType == null) return BodyDef.BodyType.StaticBody;

        switch (bodyType) {
            case "Static":
                return BodyDef.BodyType.StaticBody;
            case "Dynamic":
                return BodyDef.BodyType.DynamicBody;
            case "Kinematic":
                return BodyDef.BodyType.KinematicBody;
            default:
                System.out.printf("ERROR: Unknown body type '%s'\n", bodyType);
                return null;
        }
    }

    private static Color argbToColor(String hex, Color color) {
        if (hex == null) return Color.WHITE;
        hex = hex.charAt(0) == '#' ? hex.substring(1) : hex;
        color.a = Integer.parseInt(hex.substring(0, 2), 16) / 255f;
        color.r = Integer.parseInt(hex.substring(2, 4), 16) / 255f;
        color.g = Integer.parseInt(hex.substring(4, 6), 16) / 255f;
        color.b = Integer.parseInt(hex.substring(6, 8), 16) / 255f;
        return color;
    }

    private JsonValue mergeProperties(JsonValue a, JsonValue b) {
        for (JsonValue bChild : b) {
            String bName = bChild.getString("name");

            boolean merged = false;
            for (JsonValue aChild : a) {
                merged = bName.equals(aChild.getString("name"));
                if (merged) {
                    mergeJsons(aChild.get("value"), bChild.get("value"));
                    break;
                }
            }

            if (!merged) {
                a.addChild(bChild);
            }
        }

        return a;
    }

    /** Merges json [b] into json [a]. **/
    private JsonValue mergeJsons(JsonValue a, JsonValue b) {
        if (a == null) return b;
        if (b == null) return a;
        if (!a.isObject() || !b.isObject()) return a;

        for (JsonValue bChild : b) {
            JsonValue aChild = a.get(bChild.name);
            if (aChild != null) {
                if (aChild.name.equals("properties")) {
                    mergeProperties(aChild, bChild);
                } else {
                    mergeJsons(aChild, bChild);
                }
            } else {
                a.addChild(bChild);
            }
        }

        return a;
    }

    private void loadCollider(LevelElement.Def def, JsonValue json) {
        float ox = json.getFloat("x");
        float oy = json.getFloat("y");

        float scaleX = def.width / def.texture.getRegionWidth();
        float scaleY = def.height / def.texture.getRegionHeight();

        if (json.has("polygon")) {
            // Create PolygonShape from Tiled polygon
            JsonValue polygon = json.get("polygon");

            def.vertices = new float[2 * polygon.size];
            for (int i = 0; i < polygon.size; i++) {
                def.vertices[2 * i]     = polygon.get(i).getFloat("x");
                def.vertices[2 * i + 1] = polygon.get(i).getFloat("y");
            }
        } else {
            // Create PolygonShape from Tiled rectangle
            float width  = json.getFloat("width");
            float height = json.getFloat("height");
            def.vertices = new float[] {0, 0, width, 0, height, width, 0, height};
        }

        for (int i = 0; i < def.vertices.length; i += 2) {
            def.vertices[i] = scaleX * (ox + def.vertices[i]) - def.width / 2;
            def.vertices[i + 1] = def.height / 2 - scaleY * (oy + def.vertices[i + 1]);
        }
    }

    private void loadTile(LevelElement.Def def, JsonValue json) {
        Properties properties = new Properties(json.get("properties"));
        JsonValue body = properties.get("body");
        if (body == null) body = new JsonValue(JsonValue.ValueType.object);

        def.x += def.width  / 2 - 0.5f;
        def.y += def.height / 2 - 0.5f;
        def.vx = body.getFloat("vx", 0);
        def.vy = body.getFloat("vy", 0);

        def.angle = 0;
        def.element = stringToElementType(body.getString("type", null));
        def.canGrapple = body.getBoolean("canGrappleOn", true);
        def.density = body.getFloat("density", 0);
        def.restitution = body.getFloat("restitution", 0.3f);
        def.isSensor = body.getBoolean("isSensor", false);
        def.bodyType = stringToBodyType(body.getString("bodyType", null));
        def.tint = argbToColor(body.getString("tint", null), new Color());

        def.properties = properties;

        // Currently the collider is just the first object in 'objectgroup'. We can change this later if we end up
        // having multiple objects in 'objectgroup'.
        if (json.has("objectgroup")) {
            loadCollider(def, json.get("objectgroup").get("objects").child);
        } else {
            def.vertices = null;
        }
    }

    private void loadObject(LevelElement.Def def, JsonValue json, float tileSize, int levelHeight) {
        def.name = json.getString("name");
        float x = json.getInt("x") / tileSize;
        float y = levelHeight - json.getInt("y") / tileSize;
        def.x = x;
        def.y = y;
        def.width = json.getFloat("width") / tileSize;
        def.height = json.getFloat("height") / tileSize;
        loadTile(def, json);

        // Need to account that rotation is around the bottom-left origin in Tiled instead of the center origin here
        def.angle = -MathUtils.degreesToRadians * json.getFloat("rotation", 0);
        Vector2 pos = new Vector2(def.x, def.y).rotateAroundRad(new Vector2(x-def.width/2f, y-def.height/2f), def.angle);
        def.x = pos.x;
        def.y = pos.y;
    }

    public static class LevelDef {
        // TODO: GameObject/LevelElement separation is kinda gross
        private final Queue<GameObject> objects;
        private final Map<Integer, LevelElement> idToObject;

        final public int width, height;
        final public int music;
        final public int twoStars, threeStars;

        public LevelDef(int width, int height, int music, int twoStars, int threeStars) {
            objects = new Queue<>();
            idToObject = new HashMap<>();
            this.width = width;
            this.height = height;
            this.music = music;
            this.twoStars = twoStars;
            this.threeStars = threeStars;
        }

        public void addObject(GameObject obj) {
            objects.addLast(obj);
        }

        public void addObject(int id, LevelElement obj) {
            addObject(obj);
            idToObject.put(id, obj);
        }

        public Iterable<GameObject> getObjects() {
            return objects;
        }

        public Map<Integer, LevelElement> getIdToObject() {
            return idToObject;
        }
    }

    static class Pair {
        public TiledFile tiledFile;
        public int firstgid;
        public Pair(TiledFile tiledFile, int firstgid) {
            this.tiledFile = tiledFile;
            this.firstgid = firstgid;
        }
    }

    private Pair getTileset(JsonValue level, int gid) {
        JsonValue ret = null;
        for (JsonValue tileset : level.get("tilesets")) {
            if (gid < tileset.getInt("firstgid")) {
                break;
            } else {
                ret = tileset;
            }
        }
        return new Pair(stringToTiledFile(ret.getString("source")), ret.getInt("firstgid"));
    }

    private JsonValue getTiledObj(JsonValue level, int gid) {
        Pair tileset = getTileset(level, gid);
        return map.get(tileset.tiledFile).get(gid - tileset.firstgid);
    }

    private TextureRegion getTexture(JsonValue level, int gid) {
        Pair tileset = getTileset(level, gid);
        return textures.get(tileset.tiledFile).get(gid - tileset.firstgid);
    }

    public LevelDef loadLevel(String levelName, String checkpointName) {
        JsonValue level = assetDirectory.getEntry(levelName+":"+checkpointName, JsonValue.class);
        LevelElement.Def levelElementDef = new LevelElement.Def();

        int levelWidth = level.getInt("width");
        int levelHeight = level.getInt("height");
        int tileSize = level.getInt("tilewidth");
        assert tileSize == level.getInt("tileheight");

        Properties levelProperties = new Properties(level.get("properties"));

        LevelDef levelDef = new LevelDef(levelWidth, levelHeight, levelProperties.getInt("music", 1),
                levelProperties.getInt("twoStars", 1), levelProperties.getInt("threeStars", 1));

        for (JsonValue layer : level.get("layers")) {
            String type = layer.getString("type");
            // TODO: Transfer coordinates from parallaxed layers from Tiled more accurately
            Vector2 parallax = new Vector2(
                    1 - layer.getFloat("parallaxx", 1),
                    1 - layer.getFloat("parallaxy", 1));

            switch (type) {
                case "tilelayer":
                    int[] data = layer.get("data").asIntArray();
                    int layerWidth = layer.getInt("width");
                    int layerHeight = layer.getInt("height");

                    // Tiles by definition have a width and height of 1
                    levelElementDef.width = 1;
                    levelElementDef.height = 1;

                    // NOTE: ID's in data array is 1-index, so subtract 1 to match tileset 0-index
                    for (int i = 0; i < data.length; i++) {
                        // need to convert to game object, for now, its JsonValue object
                        int id = data[i];
                        if (id == 0) continue;
                        JsonValue tile = getTiledObj(level, id);
                        if (tile == null) continue;
                        int tiledX = i % layerWidth;
                        int tiledY = i / layerWidth;
                        levelElementDef.name = String.format("Tile #%d (%d, %d)", id, tiledX, tiledY);
                        levelElementDef.x = tiledX;
                        levelElementDef.y = layerHeight - tiledY - 1;
                        levelElementDef.texture = getTexture(level, id);
                        loadTile(levelElementDef, tile);
                        LevelElement element = LevelElement.create(levelElementDef);

                        element.setParallaxFactor(parallax);
                        levelDef.addObject(element);
                    }
                    break;
                case "objectgroup":
                    for (JsonValue jsonObject : layer.get("objects")) {
                        int gid = jsonObject.getInt("gid");
                        mergeJsons(jsonObject, getTiledObj(level, gid));

                        levelElementDef.texture = getTexture(level, gid);
                        // TODO: Can we do this cleaner? Maybe in tiled?
                        levelElementDef.triggerTexture = textures.get(TiledFile.SPACESHIP_TILESET).get(57);

                        loadObject(levelElementDef, jsonObject, tileSize, levelHeight);
                        LevelElement newObject = LevelElement.create(levelElementDef);
                        newObject.setParallaxFactor(parallax);
                        levelDef.addObject(jsonObject.getInt("id"), newObject);
                    }
                    break;
                case "imagelayer":
                    String path = layer.getString("image");
                    String filename = path.substring(path.lastIndexOf("/") + 1);
                    Texture image = assetDirectory.getEntry(filename, Texture.class);
                    image.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                    ImageObject imageObject = new ImageObject(image);
                    imageObject.setParallaxFactor(parallax);
                    levelDef.addObject(imageObject);
                    break;
                default:
                    System.out.printf("ERROR: Cannot parse layer type '%s'\n", type);
                    break;
            }
        }

        return levelDef;
    }
}