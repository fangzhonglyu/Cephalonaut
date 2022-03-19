package edu.cornell.lilbiggames.cephalonaut.engine.model;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonValue;

import com.badlogic.gdx.utils.Queue;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.GameObjectJson;

import java.util.Map;

public class PlayMode {

    private Queue<GameObjectJson> gameObjectQueue;
    private Map<Integer, TextureRegion> textures;

    public PlayMode(Queue<GameObjectJson> q, Map<Integer, TextureRegion> t) {
       gameObjectQueue = q;
       textures = t;
    }

    public Map<Integer, TextureRegion> getTextures() {
        return textures;
    }

    public Queue<GameObjectJson> getGameObjectQueue() {
        return gameObjectQueue;
    }
}
