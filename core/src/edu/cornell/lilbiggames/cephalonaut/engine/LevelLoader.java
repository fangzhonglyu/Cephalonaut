package edu.cornell.lilbiggames.cephalonaut.engine;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

import java.util.Iterator;

public class LevelLoader {
    public LevelLoader() {
        TiledMap map = new TmxMapLoader().load("Test.tmx");
        System.out.println("Loaded in tmx file");
        TiledMapTileLayer tiles = (TiledMapTileLayer) map.getLayers().get(0);
        System.out.println(tiles.getCell(0,0).getTile().getObjects().get(0).getProperties().get("canGrappleOn"));
    }
    public void parseFile() {

    }
}
