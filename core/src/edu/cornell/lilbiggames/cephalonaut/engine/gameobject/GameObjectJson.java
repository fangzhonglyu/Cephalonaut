package edu.cornell.lilbiggames.cephalonaut.engine.gameobject;

import com.badlogic.gdx.utils.JsonValue;

public class GameObjectJson {
    private JsonValue jsonObject;
    private int tileID;
    private int x;
    private int y;

    public GameObjectJson(JsonValue jsonObject, int tileID, int x, int y){
        this.jsonObject = jsonObject;
        this.tileID = tileID;
        this.x = x;
        this.y = y;
    }

    public JsonValue getJsonObject(){
        return  jsonObject;
    }

    public int getTileID(){
        return tileID;
    }

    public int getX() {return x;}

    public int getY() {return y;}
}
