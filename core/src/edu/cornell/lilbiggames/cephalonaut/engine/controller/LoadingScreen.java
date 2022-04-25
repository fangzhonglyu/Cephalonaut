package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;

public class LoadingScreen extends MenuMode implements Screen {
    private GameCanvas canvas;
    private ScreenListener listener;


    private Texture loadingAnimation;
    private Texture background;
    private FilmStrip filmStrip;
    private int frame;
    private float loadingTime;
    private float totalLoadingTime;
    /**
     * Creates a MainMenuMode with the default size and position.
     *
     * @param assets   The asset directory to use
     * @param canvas   The game canvas to draw to
     * @param listener
     */
    public LoadingScreen(AssetDirectory assets, GameCanvas canvas, ScreenListener listener, float totalLoadingTime) {
        super(assets, canvas, listener);
        this.canvas = canvas;
        this.listener = listener;
        this.loadingTime = totalLoadingTime;
        this.totalLoadingTime = totalLoadingTime;

        background = assets.getEntry( "main-menu:background", Texture.class );
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();

        loadingAnimation = assets.getEntry("loadingAnimation",Texture.class);
        System.out.println(loadingAnimation);
        filmStrip = new FilmStrip(loadingAnimation, 1, 15, 15,
        0, 0, loadingAnimation.getWidth(), loadingAnimation.getHeight());

        frame = 0;
        filmStrip.setFrame(0);
    }

    public void setLoadingTime(float time){
        totalLoadingTime = time;
        loadingTime = time;
    }

    public void render(float delta){
        if(loadingTime == 0){
            loadingTime = totalLoadingTime;
            listener.exitScreen(this,MenuMode.EXIT_LOADING_CODE);
        }
        loadingTime -= 1;
        frame = frame+1;

        filmStrip.setFrame((int)(frame/5)%15);

        canvas.clear();
        canvas.begin();

        float height = canvas.getHeight();
        float width = canvas.getWidth();
        canvas.draw(background, 0.5f*canvas.getWidth()-canvas.getCameraX()/scale.x, 0.5f*canvas.getHeight()-canvas.getCameraY()/scale.y , 0, 0, background.getWidth(), background.getHeight(), (float)width/(float)background.getWidth()/scale.x, (float)height/(float)background.getHeight()/scale.y);

        float ox = 0.5f * filmStrip.getRegionWidth();
        float oy = 0.5f * filmStrip.getRegionHeight();
        canvas.draw(filmStrip, Color.WHITE, ox, oy,
        canvas.getWidth()/2f, canvas.getHeight()/2f, 0, 0.5f, 0.5f);
        canvas.end();

    }
}
