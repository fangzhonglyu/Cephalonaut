package edu.cornell.lilbiggames.cephalonaut.engine;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.model.PlayMode;

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
        tilesetTexture = new Texture("jb-32-Tileset.png");
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
             JsonValue objects = t.get("objectgroup").get("objects");
             // get the first (and only) object on the tile
             if(objects.iterator().hasNext()) {
                 map.put(t.getInt("id"), objects.iterator().next());
             } else {
                 throw new RuntimeException("Can't find any objects");
             }
         }
        return map;
    }

    // NOTE: Might need to be changed to Queue<GameObject>
    private Queue<JsonValue> getObjectQueue(JsonValue level) {
        Queue<JsonValue> objects = new Queue<>();
        JsonValue layer = level.get("layers").iterator().next();
        int[] data = layer.get("data").asIntArray();
        // NOTE: ID's in data array is 1-index, so subtract 1 to match tileset 0-index
        for(int id : data) {
            // need to convert to game object, for now, its JsonValue object
            if(map.get(id-1) != null) {
                objects.addLast(map.get(id-1));
            }
        }
        return objects;
    }

    // This method captures the tile textures for the relevant level by parsing the png
    private TextureRegion[] getTextures(JsonValue level) {
        JsonValue layer = level.get("layers").iterator().next();
        int[] data = layer.get("data").asIntArray();
        int tileSize = tileset.getInt("tilewidth");
        int columns = tileset.getInt("columns");
        TextureRegion[] tiles = new TextureRegion[data.length];
        // NOTE: ID's in data array is 1-index, so subtract 1 to match tileset 0-index
        for(int i = 0; i < tiles.length; i++) {
            int id = data[i] - 1;
            int x = id % columns;
            int y = id / columns;
            tiles[i] = new TextureRegion(tilesetTexture, x, y, tileSize, tileSize);;
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
        Queue<JsonValue> objects = getObjectQueue(level);
        TextureRegion[] textures = getTextures(level);
        return new PlayMode(objects, textures);
    }
}
