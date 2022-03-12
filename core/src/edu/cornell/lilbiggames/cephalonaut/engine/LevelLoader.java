package edu.cornell.lilbiggames.cephalonaut.engine;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import edu.cornell.lilbiggames.cephalonaut.engine.model.LevelModel;

import java.util.HashMap;
import java.util.Map;


public class LevelLoader {

    protected TiledMap map;

    public LevelLoader() {

    }

    public Map<String,LevelModel> loadLevels(String[] levelNames){
        //get directory
        TmxMapLoader mapLoader = new TmxMapLoader();
        Map<String,LevelModel> levels = new HashMap<String,LevelModel>();

        //iterate over files in directory, adding them to level model list
        for(String levelName : levelNames){
            map = mapLoader.load(levelName + ".tmx");
            LevelModel level = loadLevel();
            levels.put(levelName,level);
            System.out.println("Loaded in " + levelName);
        }
        return levels;
    }

    public MapLayer getLayer(int index) {
        try {
            return map.getLayers().get(index);
        } catch (RuntimeException err) {
            System.out.println("Unable to get the layer at index " + index + ": " + err);
            return null;
        }
    }

    public TiledMapTileLayer getTileLayer() {
        return (TiledMapTileLayer) getLayer(0);
    }

    // expects game elements to be on the layer with index 1
    public MapLayer getGameElementLayer() {
        return getLayer(1);
    }

    public LevelModel loadLevel() {
        TiledMapTileLayer tiles = getTileLayer();
        MapLayer gameElementLayer = getGameElementLayer();

        if (tiles == null || gameElementLayer == null) {
            throw new RuntimeException("Unable to load level.");
        }

        LevelModel level = new LevelModel(tiles, gameElementLayer);
        return level;
    }
}
