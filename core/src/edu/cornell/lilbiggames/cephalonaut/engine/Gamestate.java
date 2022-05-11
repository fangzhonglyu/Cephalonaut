package edu.cornell.lilbiggames.cephalonaut.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

public class Gamestate {
    public int[][] stars;

    public Gamestate() {
        stars = new int[7][10];
    }

    public int getStarsFromIdentifier(String identifier) {
        int world = Integer.parseInt("" + identifier.charAt(identifier.indexOf(":") - 1));
        int level = Integer.parseInt("" + identifier.charAt(identifier.length() - 1));
        return this.stars[world][level];
    }

    public void setStars(String identifier, int score) {
        int world = Integer.parseInt("" + identifier.charAt(identifier.indexOf(":") - 1));
        int level = Integer.parseInt("" + identifier.charAt(identifier.length() - 1));
        if(this.stars[world][level] < score) {
            this.stars[world][level] = score;
        }
    }

    public void save() {
        FileHandle file = Gdx.files.local("gamestate.json");
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        String txt = json.toJson(this);
        System.out.println(txt);
        file.writeString(json.prettyPrint(txt), false);
    }

}
