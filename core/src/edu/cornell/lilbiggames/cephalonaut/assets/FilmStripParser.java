/*
 * FilmStripParser.java
 *
 * This is an interface for parsing a JSON entry into a FilmStrip. It allows you to
 * keep your sprite sheets data driven. However, film strips are not freely reusable
 * like textures and texture regions. You should be careful to make sure that you
 * only have one version of the asset at a time.
 *
 * @author Walker M. White
 * @data   04/20/2020
 */
package edu.cornell.lilbiggames.cephalonaut.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;

/**
 * This class parses a JSON entry into a {@link FilmStrip}.
 * <p>
 * Film strips are defined inside of a texture entry.  That texture entry is the source
 * of the film strip. Film strips are defined inside of a subobject called "sprites".
 * They have the following entries:
 * <p>
 * * "rows": The number of rows in the sprite sheet
 * * "cols": The number of columns in the sprite sheet
 * * "size": The number of frames in the sprite sheet
 * * "region": The subregion of the texture defining the sprite sheet
 * <p>
 * The region has the same format as {@link TextureRegionParser}. The values size
 * and region are optional.
 */
public class FilmStripParser implements AssetParser<FilmStrip> {
    /**
     * The separating character between the file name and the alias
     */
    public static char ALIAS_SEP = ':';
    /**
     * The substitution character for when an alias separation is in the file
     */
    private static final char ALIAS_MASK = '∀';
    /**
     * The parent texture of the current film strip
     */
    private JsonValue root;
    /**
     * The current film strip entry in the JSON directory
     */
    private JsonValue sprite;

    /**
     * Returns the asset type generated by this parser
     *
     * @return the asset type generated by this parser
     */
    public Class<FilmStrip> getType() {
        return FilmStrip.class;
    }

    /**
     * Resets the parser iterator for the given directory.
     * <p>
     * The value directory is assumed to be the root of a larger JSON structure.
     * The individual assets are defined by subtrees in this structure.
     *
     * @param directory The JSON representation of the asset directory
     */
    public void reset(JsonValue directory) {
        root = directory;
        root = root.getChild("textures");
        sprite = null;
        advance();
    }

    /**
     * Returns true if there are still assets left to generate
     *
     * @return true if there are still assets left to generate
     */
    public boolean hasNext() {
        return sprite != null;
    }

    /**
     * Processes the next available film strip, loading it into the asset manager
     * <p>
     * The parser converts JSON entries into {@link FilmStripLoader.FilmStripParameters}
     * values of the same name. The keys match the field names of that object.
     * <p>
     * This method fails silently if there are no available assets to process.
     *
     * @param manager The asset manager to load an asset
     * @param keymap  The mapping of JSON keys to asset file names
     */
    public void processNext(AssetManager manager, ObjectMap<String, String> keymap) {
        String file = root.getString("file", null);
        if (file == null) {
            advance();
            return;
        }
        FilmStripLoader.FilmStripParameters params = new FilmStripLoader.FilmStripParameters(file);

        params.rows = sprite.getInt("rows", 1);
        params.cols = sprite.getInt("cols", 1);
        params.size = sprite.getInt("size", params.rows * params.cols);
        if (sprite.hasChild("region")) {
            JsonValue region = sprite.get("region");
            if (region.size < 4) {
                throw new GdxRuntimeException("Rectangle " + region + " is not valid");
            }
            params.x = region.getInt(0);
            params.y = region.getInt(1);
            params.width = region.getInt(2);
            params.height = region.getInt(3);
            params.width = params.width == -1 ? -1 : params.width - params.x;
            params.height = params.height == -1 ? -1 : params.height - params.y;
        }

        String region = ParserUtils.safeConcatenate(file, sprite.name(), ALIAS_SEP, ALIAS_MASK);
        keymap.put(root.name() + "." + sprite.name(), region);
        manager.load(region, FilmStrip.class, params);
        advance();
    }

    /**
     * Returns true if o is another FilmStripParser
     *
     * @return true if o is another FilmStripParser
     */
    public boolean equals(Object o) {
        return o instanceof FilmStripParser;
    }

    /**
     * Advances the read position forward to find the next film strip
     * <p>
     * Filmstrips are defined inside of a parent texture. As not all textures
     * have a film strip, we need to scan forward to find the next film strip
     */
    private void advance() {
        if (sprite != null) {
            sprite = sprite.next();
            if (sprite == null) {
                root = root.next();
            }
        }
        while (sprite == null && root != null) {
            if (root.hasChild("sprites")) {
                sprite = root.getChild("sprites");
            } else {
                root = root.next();
            }
        }
    }
}
