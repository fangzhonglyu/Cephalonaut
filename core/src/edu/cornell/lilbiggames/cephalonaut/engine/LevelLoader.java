package edu.cornell.lilbiggames.cephalonaut.engine;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.assets.JsonValueParser;
import edu.cornell.lilbiggames.cephalonaut.engine.model.PlayMode;

import java.util.HashMap;
import java.util.Map;


public class LevelLoader {

    protected TiledMap map;
    protected AssetDirectory assetDirectory;
    protected AssetDirectory tilesetDirectory;

    public LevelLoader() {
        assetDirectory = new AssetDirectory("assets.json");
        tilesetDirectory = new AssetDirectory("jb-32-Tileset..tsj");
    }

    public Map<String, PlayMode> loadLevels(String[] levelNames){
        //get directory
        JsonValue objects = new AssetDirectory("").getEntry("")

        Map<String, PlayMode> levels = new HashMap<String, PlayMode>();

        //iterate over files in directory, adding them to level model list
        for(String levelName : levelNames){
//            map = mapLoader.load(levelName + ".tsj");

            PlayMode level = loadLevel();
            levels.put(levelName,level);
            System.out.println("Loaded in " + levelName);
        }
        return levels;
    }

    // expects game elements to be on the layer with index 1
    public MapLayer getGameElementLayer() {
        return getLayer(1);
    }

    public PlayMode loadLevel() {
        TiledMapTileLayer tiles = getTileLayer();
        MapLayer gameElementLayer = getGameElementLayer();

        if (tiles == null || gameElementLayer == null) {
            throw new RuntimeException("Unable to load level.");
        }

        PlayMode level = new PlayMode(tiles, gameElementLayer);
        return level;
    }
}
