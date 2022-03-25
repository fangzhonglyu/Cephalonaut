package edu.cornell.lilbiggames.cephalonaut.engine.parsing;

import com.badlogic.gdx.utils.JsonValue;

public class Properties {
    private final JsonValue properties;

    public Properties(JsonValue properties) {
        assert properties.isArray();
        this.properties = properties;
    }

    public JsonValue get(String name) {
        if (properties == null) return null;

        for (JsonValue property : properties) {
            if (property.getString("name").equals(name)) {
                return property.require("value");
            }
        }

        return null;
    }

    public int getInt(String name, int defaultValue) {
        JsonValue value = get(name);
        return value == null ? defaultValue : value.asInt();
    }

    public float getFloat(String name, float defaultValue) {
        JsonValue value = get(name);
        return value == null ? defaultValue : value.asFloat();
    }

    public String getString(String name, String defaultValue) {
        JsonValue value = get(name);
        return value == null ? defaultValue : value.asString();
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        JsonValue value = get(name);
        return value == null ? defaultValue : value.asBoolean();
    }
}
