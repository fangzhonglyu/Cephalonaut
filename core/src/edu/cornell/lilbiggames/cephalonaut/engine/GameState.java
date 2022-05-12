package edu.cornell.lilbiggames.cephalonaut.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

public class GameState {
    public int[][] stars;

    public GameState() {
        stars = new int[7][10];
    }

    public int getStarsFromIdentifier(String identifier) {
        int world = parseWorld(identifier);
        int level = parseLevel(identifier);
        return stars[world][level];
    }

    private int parseWorld(String identifier) {
        return Integer.parseInt(identifier.substring(identifier.indexOf('_') + 1, identifier.indexOf(':')));
    }

    private int parseLevel(String identifier) {
        return Integer.parseInt(identifier.substring(identifier.lastIndexOf('_') + 1));
    }

    public void setStars(String identifier, int new_stars) {
        int world = parseWorld(identifier);
        int level = parseLevel(identifier);
        if(new_stars > stars[world][level]) {
            stars[world][level] = new_stars;
            save();
        }
    }

    public void save() {
        try {
            FileHandle file = Gdx.files.local("gamestate.json");
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            String txt = json.toJson(this);
            file.writeString(json.prettyPrint(txt), false);
        } catch (RuntimeException e) {
            System.out.println("Failed to save game state. \n" + e);
        }
    }
}
