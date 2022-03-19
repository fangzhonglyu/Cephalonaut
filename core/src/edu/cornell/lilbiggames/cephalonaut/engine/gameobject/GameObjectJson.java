package edu.cornell.lilbiggames.cephalonaut.engine.gameobject;

import com.badlogic.gdx.utils.JsonValue;

public class GameObjectJson {
    private JsonValue jsonObject;
    private int tileID;

    public GameObjectJson(JsonValue jsonObject, int tileID){
        this.jsonObject = jsonObject;
        this.tileID = tileID;
    }

    public JsonValue getJsonObject(){
        return  jsonObject;
    }

    public int getTileID(){
        return tileID;
    }
}
