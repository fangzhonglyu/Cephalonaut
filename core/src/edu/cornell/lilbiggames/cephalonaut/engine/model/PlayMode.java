package edu.cornell.lilbiggames.cephalonaut.engine.model;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonValue;

import com.badlogic.gdx.utils.Queue;

public class PlayMode {

    private Queue<JsonValue> gameObjectQueue;
    private TextureRegion[] textures;

    public PlayMode(Queue<JsonValue> q, TextureRegion[] t) {
       gameObjectQueue = q;
       textures = t;
    }

    public TextureRegion[] getTextures() {
        return textures;
    }

    public Queue<JsonValue> getGameObjectQueue() {
        return gameObjectQueue;
    }
}
