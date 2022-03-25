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
import edu.cornell.lilbiggames.cephalonaut.engine.controller.PlayMode;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.GameObject;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.ImageObject;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;


public class LevelLoader {

    private AssetDirectory assetDirectory;
    private Map<Integer, JsonValue> map = new HashMap<>();
    private Map<Integer, TextureRegion> textures = new HashMap<>();
    private Texture tilesetTexture;
    private JsonValue tile_tileset;
    private JsonValue object_tileset;

    public LevelLoader() {
        assetDirectory = new AssetDirectory("assets.json");
        assetDirectory.loadAssets();
        assetDirectory.finishLoading();
        tilesetTexture = new Texture("TS-meteroid-space.png");
        tilesetTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        tile_tileset = assetDirectory.getEntry("tile-tileset", JsonValue.class);
        object_tileset = assetDirectory.getEntry("object-tileset", JsonValue.class);
        getTextures();

        for (JsonValue tile : tile_tileset.get("tiles")) {
            map.put(tile.getInt("id"), tile);
        }

        for (JsonValue tile : object_tileset.get("tiles")) {
            map.put(tile.getInt("id") + 128, tile);
        }
    }

    public AssetDirectory getAssetDirectory() {
        return assetDirectory;
    }

    // Maps tileset tiles to texture regions
    private void getTextures() {
        int tileSize = tile_tileset.getInt("tilewidth");
        int columns = tile_tileset.getInt("columns");
        for (JsonValue tile : tile_tileset.get("tiles")) {
            int id = tile.getInt("id");
            int x = (id % columns)*tileSize;
            int y = (id / columns)*tileSize;
            textures.put(id, new TextureRegion(tilesetTexture, x, y, tileSize, tileSize));
        }
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

    /** Merges json [b] into json [a]. **/
    private JsonValue mergeJsons(JsonValue a, JsonValue b) {
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
        Vector2 pos = new Vector2(def.x, def.y).rotateAroundRad(new Vector2(x, y), def.angle);
        def.x = pos.x;
        def.y = pos.y;
    }

    public void loadLevel(String levelName, PlayMode playMode) {
        LevelElement.Def levelElementDef = new LevelElement.Def();

        Map<Integer, LevelElement> objectIds = new HashMap<>();
        Queue<GameObject> objects = new Queue<>();
        JsonValue level = assetDirectory.getEntry(levelName, JsonValue.class);
        int levelHeight = level.getInt("height");
        int tileSize = level.getInt("tilewidth");
        assert tileSize == level.getInt("tileheight");

        for (JsonValue layer : level.get("layers")) {
            String type = layer.getString("type");
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
                        JsonValue tile = map.get(id - 1);
                        if (tile != null) {
                            int tiledX = i % layerWidth;
                            int tiledY = i / layerWidth;
                            levelElementDef.name = String.format("Tile #%d (%d, %d)", id, tiledX, tiledY);
                            levelElementDef.x = tiledX;
                            levelElementDef.y = layerHeight - tiledY - 1;
                            levelElementDef.texture = textures.get(id - 1);
                            loadTile(levelElementDef, tile);
                            objects.addLast(LevelElement.create(levelElementDef));
                        }
                    }
                    break;
                case "objectgroup":
                    for (JsonValue jsonObject : layer.get("objects")) {
                        int gid = jsonObject.getInt("gid") - 1;
                        mergeJsons(jsonObject, map.get(gid));

                        if (jsonObject.has("image")) {
                            Texture fullTexture = assetDirectory.getEntry(jsonObject.getString("image"), Texture.class);
                            fullTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                            levelElementDef.texture = new TextureRegion(fullTexture);
                        } else {
                            levelElementDef.texture = textures.get(gid);
                        }

                        loadObject(levelElementDef, jsonObject, tileSize, levelHeight);
                        LevelElement newObject = LevelElement.create(levelElementDef);
                        objects.addLast(newObject);
                        objectIds.put(jsonObject.getInt("id"), newObject);
                    }
                    break;
                case "imagelayer":
                    Texture image = assetDirectory.getEntry(layer.getString("image"), Texture.class);
                    image.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                    ImageObject imageObject = new ImageObject(image);
                    imageObject.setParallax(layer.getFloat("parallaxx"), layer.getFloat("parallaxy"));
                    objects.addLast(imageObject);
                    break;
                default:
                    System.out.printf("ERROR: Cannot parse layer type '%s'\n", type);
                    break;
            }
        }

        playMode.setObjectMap(objectIds);
        playMode.reset(objects);
    }
}
