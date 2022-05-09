package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;

public class LevelLoadingScreen extends MenuMode implements Screen {
    private GameCanvas canvas;
    private ScreenListener listener;


    private Texture background;

    private final int NUM_FRAMES = 42;
    private final int FILM_STRIP_SIZE = 15;
    private FilmStrip[] filmStrips;
    private float frame;
    private float loadingTime;
    private float totalLoadingTime;
    /**
     * Creates a MainMenuMode with the default size and position.
     *
     * @param assets   The asset directory to use
     * @param canvas   The game canvas to draw to
     * @param listener
     */
    public LevelLoadingScreen(AssetDirectory assets, GameCanvas canvas, ScreenListener listener, float totalLoadingTime) {
        super(assets, canvas, listener);
        this.canvas = canvas;
        this.listener = listener;
        this.loadingTime = totalLoadingTime;
        this.totalLoadingTime = totalLoadingTime;

        background = assets.getEntry( "BG-1-teal.png", Texture.class );
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();

        filmStrips = new FilmStrip[NUM_FRAMES/FILM_STRIP_SIZE + 1];
        for(int i = 0; i <= NUM_FRAMES/FILM_STRIP_SIZE; i++) {
            Texture loadingAnimation = assets.getEntry("loadingAnimation"+(i+1), Texture.class);
            filmStrips[i] = new FilmStrip(loadingAnimation, 1, Math.min(FILM_STRIP_SIZE, NUM_FRAMES-FILM_STRIP_SIZE*i+1), Math.min(FILM_STRIP_SIZE, NUM_FRAMES-FILM_STRIP_SIZE*i+1),
                    0, 0, loadingAnimation.getWidth(), loadingAnimation.getHeight());
        }

        frame = 0;
        filmStrips[0].setFrame(0);
    }

    public void setLoadingTime(float time){
        totalLoadingTime = time;
        loadingTime = time;
    }

    public void render(float delta){
        SoundController.setBlackHoleSound(false,1);
        SoundController.setInkSound(false);
        if(loadingTime == 0){
            loadingTime = totalLoadingTime;
            listener.exitScreen(this,MenuMode.EXIT_LOADING_CODE);
        }
        loadingTime -= 1;
        frame = (frame+delta*10f)%NUM_FRAMES;

        int currentFilmStripId = (int)(frame/FILM_STRIP_SIZE);

        FilmStrip filmStrip = filmStrips[currentFilmStripId];

        filmStrip.setFrame((int)frame - (int)(frame/FILM_STRIP_SIZE)*FILM_STRIP_SIZE);

        canvas.clear();
        canvas.begin();

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
}
