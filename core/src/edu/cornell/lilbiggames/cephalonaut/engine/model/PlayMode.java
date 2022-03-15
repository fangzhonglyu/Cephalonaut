package edu.cornell.lilbiggames.cephalonaut.engine.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class PlayMode {

    protected TiledMapTileLayer tiles;
    protected MapLayer gameElementLayer;
    protected int width;
    protected int height;

    public PlayMode(TiledMapTileLayer tiles, MapLayer gameElementLayer) {
        this.tiles = tiles;
        this.gameElementLayer = gameElementLayer;
        this.width = tiles.getWidth();
        this.height = tiles.getHeight();
    }

    public TiledMapTileLayer getTiles() {
        return tiles;
    }

    /**
     * getPropertyFromTile returns a property object given the property key from the passed tile
     */
    public Object getPropertyFromTile(TiledMapTile tile, String property) {
        if (tile.getObjects().getCount() == 0) {
            return null;
        }
        return tile.getObjects().get(0).getProperties().get(property);
    }

    /**
     * getTile returns a TiledMapTile object from the tile layer at index (x, y)
     */
    public TiledMapTile getTile(int x, int y) {
        return tiles.getCell(x, y).getTile();
    }

    public float getAngle(TiledMapTile tile) {
        Object val = getPropertyFromTile(tile, "angle");
        return val != null ? (float) val : -1;
    }

    public float getDensity(TiledMapTile tile) {
        Object val = getPropertyFromTile(tile, "density");
        return val != null ? (float) val : -1;
    }

    public boolean getCanGrappleOn(TiledMapTile tile) {
        Object val = getPropertyFromTile(tile, "canGrappleOn");
        return val != null && (boolean) val;
    }

    public boolean getIsStatic(TiledMapTile tile) {
        Object val = getPropertyFromTile(tile, "isStatic");
        return val != null && (boolean) val;
    }

    public float getRestitution(TiledMapTile tile) {
        Object val = getPropertyFromTile(tile, "restitution");
        return val != null ? (float) val : -1;
    }

    public Color getTint(TiledMapTile tile) {
        Object val = getPropertyFromTile(tile, "tint");
        return val != null ? (Color) val : new Color();
    }

    public String getName(TiledMapTile tile) {
        if(tile.getObjects().getCount() == 0) {
            return null;
        }
        return tile.getObjects().get(0).getName();
    }

    public float getObjectWidth(TiledMapTile tile) {
        Object val = getPropertyFromTile(tile, "width");
        return val != null ? (float) val : -1;
    }

    public float getObjectHeight(TiledMapTile tile) {
        Object val = getPropertyFromTile(tile, "height");
        return val != null ? (float) val : -1;
    }

    public void printTile(TiledMapTile tile, int x, int y) {
        System.out.println(getName(tile) + ":" + tile.getId() + " {");
        System.out.println("    position: (" + x + ", " + y + "),");
        System.out.println("    density: " + getDensity(tile) + ",");
        System.out.println("    angle: " + getAngle(tile) + ",");
        System.out.println("    canGrappleOn: " + getCanGrappleOn(tile) + ",");
        System.out.println("    restitution: " + getRestitution(tile) + ",");
        System.out.println("    isStatic: " + getIsStatic(tile) + ",");
        System.out.println("    tint: " + getTint(tile) + ",");
        System.out.println("    width: " + getObjectWidth(tile) + ",");
        System.out.println("    height: " + getObjectHeight(tile));
        System.out.println("}");
    }

    public MapObjects getGameElements(){
        MapObjects objects = gameElementLayer.getObjects();
        return objects;
    }
}
