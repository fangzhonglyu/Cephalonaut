package edu.cornell.lilbiggames.cephalonaut.engine;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import edu.cornell.lilbiggames.cephalonaut.engine.model.LevelModel;


public class LevelLoader {

    protected TiledMap map;

    public LevelLoader(String fileName) {
        map = new TmxMapLoader().load(fileName);
        System.out.println("Loaded in " + fileName);
        loadLevel();
    }

    public MapLayer getLayer(int index) {
        return map.getLayers().get(index);
    }

    public TiledMapTileLayer getTileLayer() {
        try {
            return (TiledMapTileLayer) getLayer(0);
        } catch (RuntimeException err) {
            System.out.println("Unable to get the first tile layer at index 0: " + err);
            return null;
        }
    }

    public void loadLevel() {

        TiledMapTileLayer tiles = getTileLayer();
        if (tiles == null) {
            System.out.println("Unable to load level.");
            return;
        }

        // What to do with level?
        LevelModel level = new LevelModel(tiles);
    }
}
