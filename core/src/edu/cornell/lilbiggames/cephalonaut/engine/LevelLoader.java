package edu.cornell.lilbiggames.cephalonaut.engine;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.GameObject;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.GameObjectJson;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.ImageObject;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.LevelElement;

import java.util.HashMap;
import java.util.Map;


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
        tile_tileset = assetDirectory.getEntry("tile-tileset", JsonValue.class);
        object_tileset = assetDirectory.getEntry("object-tileset", JsonValue.class);
        getTextures();

        for (JsonValue tile : tile_tileset.get("tiles")) {
            map.put(tile.getInt("id"), tile);
        }

        for (JsonValue tile : object_tileset.get("tiles")) {
            map.put(tile.getInt("id") + 129, tile);
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

    public Queue<GameObject> loadLevel(String levelName) {
        Queue<GameObject> objects = new Queue<>();
        JsonValue level = assetDirectory.getEntry(levelName, JsonValue.class);
        for (JsonValue layer : level.get("layers")) {
            String type = layer.getString("type");
            if (type.equals("tilelayer")) {
                int[] data = layer.get("data").asIntArray();
                int width = layer.getInt("width");
                int height = layer.getInt("height");
                // NOTE: ID's in data array is 1-index, so subtract 1 to match tileset 0-index
                for (int i = 0; i < data.length; i++) {
                    // need to convert to game object, for now, its JsonValue object
                    int id = data[i];
                    JsonValue tile = map.get(id - 1);
                    TextureRegion texture = textures.get(id - 1);
                    if (tile != null && texture != null) {
                        int x = i % width;
                        int y = height - i / width - 1;
                        LevelElement obstacle = new LevelElement(x, y, texture,1, 1, tile);
                        objects.addLast((GameObject) obstacle);
                    }
                }
            } else if (type.equals("imagelayer")) {
                Texture image = assetDirectory.getEntry(layer.getString("image"), Texture.class);
                ImageObject object = new ImageObject(image);
                object.setParallax( layer.getFloat("parallaxx"), layer.getFloat("parallaxy") );
                objects.addLast(object);
            } else if (type.equals("objectgroup")) {
                for (JsonValue jsonObject : layer.get("objects")) {
                    JsonValue jsonObjectType = map.get(jsonObject.getInt("gid"));
                    Texture texture = assetDirectory.getEntry(jsonObjectType.getString("image"), Texture.class);
                    LevelElement object = new LevelElement(jsonObject, jsonObjectType, new TextureRegion(texture));
                    objects.addLast(object);
                }
            } else {
                System.out.printf("ERROR: Cannot parse layer type '%s'\n", type);
            }
        }

        return objects;
    }
}
