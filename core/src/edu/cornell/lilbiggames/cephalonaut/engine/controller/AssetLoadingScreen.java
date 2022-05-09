package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;

public class AssetLoadingScreen implements Screen {
    private GameCanvas canvas;
    private ScreenListener listener;
    private AssetDirectory assets;


    private Texture background;

    private FilmStrip filmStrip;
    private float frame;
    private float loadingTime;
    private float totalLoadingTime;

    protected Vector2 bounds,scale;
    private AssetDirectory loading;

    private int budget = 15;
    /**
     * Creates a MainMenuMode with the default size and position.
     *
     * @param canvas   The game canvas to draw to
     * @param listener
     */
    public AssetLoadingScreen(GameCanvas canvas, ScreenListener listener) {
        this.canvas = canvas;
        this.listener = listener;
        this.loadingTime = totalLoadingTime;
        this.totalLoadingTime = totalLoadingTime;
        this.assets = new AssetDirectory("assets.json");
        loading = new AssetDirectory("loading.json");

        loading.loadAssets();
        loading.finishLoading();

        background = loading.getEntry( "background", Texture.class );
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();

        Texture film = loading.getEntry("animation", Texture.class);
        this.filmStrip = new FilmStrip(film,1,7);
        frame = 0;
        filmStrip.setFrame(0);

        assets.loadAssets();
    }

    public AssetDirectory getAssetDirectory(){
        return assets;
    }

    @Override
    public void show() {

    }

    public void render(float delta){
        this.assets.update(this.budget);

        if (assets.getProgress() >= 1.0F) {
            listener.exitScreen(this,MenuMode.DONE_LOADING_ASSETS);
        }
        loadingTime -= 1;
        frame = (frame+delta*10f)%7;
        filmStrip.setFrame((int)(frame));

        canvas.clear();
        canvas.begin();

        float height = canvas.getHeight();
        float width = canvas.getWidth();
        canvas.draw(background,
                0.5f*canvas.getWidth()-canvas.getCameraX(),
                0.5f*canvas.getHeight()-canvas.getCameraY(),
                0, 0, background.getWidth() * 10, background.getHeight() * 10,
                20,
                20);
        float ox = 0.5f * filmStrip.getRegionWidth();
        float oy = 0.5f * filmStrip.getRegionHeight();
        canvas.draw(filmStrip, Color.WHITE, ox, oy,
        canvas.getWidth()/2f, canvas.getHeight()/2f, 0, 0.5f, 0.5f);
        canvas.end();

    }

    @Override
    public void resize(int i, int i1) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
