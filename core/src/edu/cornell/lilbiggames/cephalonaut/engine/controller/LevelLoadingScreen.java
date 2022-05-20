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

    private AssetDirectory assets;

    private Texture background;

    private FilmStrip[] filmStrips;
    private int numFrames;
    private int totalFilmstripSize;
    private float frame;
    private float loadingTime;
    private float totalLoadingTime;
    private int curLevel;

    /**
     * Creates a MainMenuMode with the default size and position.
     *
     * @param assets   The asset directory to use
     * @param canvas   The game canvas to draw to
     * @param listener
     */
    public LevelLoadingScreen(AssetDirectory assets, GameCanvas canvas, ScreenListener listener, float totalLoadingTime, FilmStrip[] filmStrips, int numFrames, int totalFilmstripSize) {
        super(assets, canvas, listener);
        this.canvas = canvas;
        this.listener = listener;
        this.loadingTime = totalLoadingTime;
        this.totalLoadingTime = totalLoadingTime;
        this.assets = assets;
        this.curLevel = 0;
        this.setNewFilm(filmStrips, numFrames, totalFilmstripSize);


        background = assets.getEntry( "BG-1-teal.png", Texture.class );
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();
    }

    public void setNewFilm(FilmStrip[] filmStrips, int numFrames, int totalFilmstripSize){
        this.numFrames = numFrames;
        this.totalFilmstripSize = totalFilmstripSize;
        this.filmStrips = filmStrips;
        frame = 0;
        this.filmStrips[0].setFrame(0);
    }

    public void setLoadingTime(float time){
        totalLoadingTime = time;
        loadingTime = time;
    }

    public void setDefault(){
        setBackground();
        setLoadingTime(100);
    }

    public void setCurLevel(int level){
        this.curLevel = level;
    }

    public void setBackground() {

        background = assets.getEntry( "BG-" + (curLevel + 1), Texture.class);
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
    }

    public void render(float delta){
        SoundController.setBlackHoleSound(false,1);
        SoundController.setInkSound(false);
        if(loadingTime == 0){
            loadingTime = totalLoadingTime;
            listener.exitScreen(this,MenuMode.EXIT_LOADING_CODE);
        }
        loadingTime -= 1;
        frame = (frame+delta*10f)%numFrames;

        int currentFilmStripId = (int)(frame/totalFilmstripSize);

        FilmStrip filmStrip = filmStrips[currentFilmStripId];

        filmStrip.setFrame((int)frame - (int)(frame/totalFilmstripSize)*totalFilmstripSize);

        canvas.clear();
        canvas.begin();

        int width = canvas.getWidth();
        int height = canvas.getHeight();
        canvas.draw(background,
                canvas.getWidth() / 2f - canvas.getCameraX(),
                canvas.getHeight() / 2f - canvas.getCameraY(),
                width, height,0, 0,
                background.getWidth() * 2, background.getWidth() * 2 * height / width,
                1, 1);
        float ox = 0.5f * filmStrip.getRegionWidth();
        float oy = 0.5f * filmStrip.getRegionHeight();
        canvas.draw(filmStrip, Color.WHITE, ox, oy,
        canvas.getWidth()/2f, canvas.getHeight()/2f, 0, 0.5f, 0.5f);
        canvas.end();
    }
}
