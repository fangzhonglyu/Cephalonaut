package edu.cornell.lilbiggames.cephalonaut.engine;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.GameObjectJson;
import edu.cornell.lilbiggames.cephalonaut.engine.model.PlayMode;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.LevelElement;

import java.util.HashMap;
import java.util.Map;


public class LevelLoader {

    private AssetDirectory assetDirectory;
    private HashMap<Integer, JsonValue> map;
    private Texture tilesetTexture;
    private JsonValue tileset;


    public LevelLoader() {
        assetDirectory = new AssetDirectory("assets.json");
        assetDirectory.loadAssets();
        assetDirectory.finishLoading();
        tilesetTexture = new Texture("TS-meteroid-space.png");
        tileset = assetDirectory.getEntry("tileset", JsonValue.class);
        map = getTileMap();
    }

    public AssetDirectory getAssetDirectory() {
        return assetDirectory;
    }

    /** Creates a HashMap that maps tile ids to JsonValue game objects */
    private HashMap<Integer, JsonValue> getTileMap() {
        HashMap<Integer, JsonValue> map = new HashMap<>();
        JsonValue tiles = tileset.get("tiles");
         // iterate through each tile that has an object
         for(JsonValue t : tiles) {
             map.put(t.getInt("id"), t);
//             JsonValue objects = t.get("objectgroup").get("objects");
//             // get the first (and only) object on the tile
//             if(objects.iterator().hasNext()) {
//                 map.put(t.getInt("id"), objects.iterator().next());
//             } else {
//                 throw new RuntimeException("Can't find any objects");
//             }
         }
        return map;
    }

    // NOTE: Might need to be changed to Queue<GameObject>
    private Queue<GameObjectJson> getObjectQueue(JsonValue level) {
        Queue<GameObjectJson> objects = new Queue<>();
        JsonValue layer = level.get("layers").iterator().next();
        int[] data = layer.get("data").asIntArray();
        int width = layer.getInt("width");
        int height = layer.getInt("height");
        // NOTE: ID's in data array is 1-index, so subtract 1 to match tileset 0-index
        for(int i = 0; i < data.length; i++) {
            // need to convert to game object, for now, its JsonValue object
            int id = data[i];
            if(map.get(id-1) != null) {
                int x = i % width;
                int y = (data.length - i) / width;
                objects.addLast(new GameObjectJson(map.get(id-1), id-1, x, y));
            }
        }
        return objects;
    }

    // This method captures the tile textures for the relevant level by parsing the png
    private Map<Integer, TextureRegion> getTextures(JsonValue level) {
        JsonValue layer = level.get("layers").iterator().next();
        int[] data = layer.get("data").asIntArray();
        int tileSize = tileset.getInt("tilewidth");
        int columns = tileset.getInt("columns");
        Map<Integer, TextureRegion> tiles = new HashMap<Integer, TextureRegion>();
        // NOTE: ID's in data array is 1-index, so subtract 1 to match tileset 0-index
        for(int i = 0; i < data.length; i++) {
            int id = data[i] - 1;
            int x = (id % columns)*tileSize;
            int y = (id / columns)*tileSize;
            tiles.put(id, new TextureRegion(tilesetTexture, x, y, tileSize, tileSize));
        }
        return tiles;
    }

    public Map<String, PlayMode> loadLevels(String[] levelNames){
        Map<String, PlayMode> levels = new HashMap<>();
        //iterate over files in directory, adding them to level model list
        for(String levelName : levelNames){
            PlayMode level = loadLevel(levelName);
            levels.put(levelName,level);
            System.out.println("Loaded in " + levelName);
        }
        return levels;
    }

    public PlayMode loadLevel(String levelName) {
        JsonValue level = assetDirectory.getEntry(levelName, JsonValue.class);
        if(!level.get("layers").iterator().hasNext()) {
            throw new RuntimeException("No layer to parse.");
        }
        Queue<GameObjectJson> objects = getObjectQueue(level);
        Map<Integer, TextureRegion> textures = getTextures(level);
        return new PlayMode(objects, textures);
    }

    private LevelElement initializeObjectFromJson(JsonValue objectJson, int tileID, int x, int y) {
        if (!objectJson.getString("type").equals("GameObject")) return null;
//		JsonValue body = objectJson.get("body");
//		if (body == null) return null;

        JsonValue collisionObject = objectJson.get("objectgroup").get("objects").get(0);
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

            LevelElement obstacle = new LevelElement(x, y, vertices, 0, LevelElement.ELEMENT.MISC_POLY);

            TextureRegion texture = textures.get(tileID);
            obstacle.setTextureBottomLeft(texture);
            obstacle.setTextureScaleX(16 * scale.x / (texture.getRegionWidth() * 16));
            obstacle.setTextureScaleY(16 * scale.y / (texture.getRegionHeight() * 16));
            return obstacle;
        } else {
            LevelElement obstacle = new LevelElement(x, y, collisionObject.getFloat("width") / 16,
                    collisionObject.getFloat("height") / 16, collisionObject.getFloat("rotation"), scale,
                    LevelElement.ELEMENT.MISC_POLY);

            TextureRegion texture = textures.get(tileID);
            obstacle.setTexture(texture);
            obstacle.setTextureScaleX(16 * scale.x / (texture.getRegionWidth()*16));
            obstacle.setTextureScaleY(16 * scale.y / (texture.getRegionHeight()*16));
            return obstacle;
        }

    }

    public void loadLevel2(String levelName) {
        Queue<GameObject> objects = new Queue<>();
        JsonValue level = assetDirectory.getEntry(levelName, JsonValue.class);
        for (JsonValue layer : level.get("layers").iterator()) {
            String type = layer.getString("type");
            if (type.equals("tilelayer")) {
                int[] data = layer.get("data").asIntArray();
                int width = layer.getInt("width");
                int height = layer.getInt("height");
                // NOTE: ID's in data array is 1-index, so subtract 1 to match tileset 0-index
                for(int i = 0; i < data.length; i++) {
                    // need to convert to game object, for now, its JsonValue object
                    int id = data[i];
                    if(map.get(id-1) != null) {
                        int x = i % width;
                        int y = (data.length - i) / width;
                        objects.addLast(new LevelElement());
                    }
                }
            } else if (type.equals("imagelayer")) {

            } else {
                System.out.printf("ERROR: Cannot parse layer type '%s'\n", type);
            }
        }

    }
}
