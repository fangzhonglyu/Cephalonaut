package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.GDXRoot;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;

public class CreditsScreen extends MenuMode implements Screen {
    private Credit[] credits;
    private GameCanvas canvas;
    private Texture background;
    protected Vector2 bounds,scale;
    private ScreenListener listener;

    /** The font for giving messages to the player */
    private BitmapFont displayFont;

    private class Credit{
        public String name;
        public String[] roles;
        public Texture texture;

        public Credit(String name, String[] roles, Texture texture){
            this.name = name;
            this.roles = roles;
            this.texture = texture;
        }
    }

    public CreditsScreen(AssetDirectory assets, GameCanvas canvas, ScreenListener listener){
        super(assets, canvas, listener);
        this.canvas = canvas;
        this.listener = listener;

        background = assets.getEntry( "main-menu:background", Texture.class);
        background.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        this.scale = new Vector2(1,1);
        this.bounds = canvas.getSize().cpy();
        this.displayFont = assets.getEntry("retro", BitmapFont.class);

        // TODO: have this actually parse from a json
        credits = new Credit[8];
        credits[0] = new Credit("michael",new String[]{"programmer"},assets.getEntry("michael",Texture.class));
        credits[1] = new Credit("teddy",new String[]{"programmer"},assets.getEntry("teddy",Texture.class));
        credits[2] = new Credit("matias",new String[]{"programmer"},assets.getEntry("matias",Texture.class));
        credits[3] = new Credit("oliver",new String[]{"programmer"},assets.getEntry("oliver",Texture.class));
        credits[4] = new Credit("angie",new String[]{"programmer"},assets.getEntry("angie",Texture.class));
        credits[5] = new Credit("barry",new String[]{"programmer","designer"},assets.getEntry("barry",Texture.class));
        credits[6] = new Credit("estelle",new String[]{"designer"},assets.getEntry("estelle",Texture.class));
        credits[7] = new Credit("alex",new String[]{"music"},assets.getEntry("alex",Texture.class));

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float v) {
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            listener.exitScreen(this, MenuMode.RETURN_TO_START_CODE);
        }
        canvas.begin();
        float width = canvas.getWidth();
        float height = canvas.getHeight();
        canvas.draw(background, 0.5f*canvas.getWidth()-canvas.getCameraX()/scale.x, 0.5f*canvas.getHeight()-canvas.getCameraY()/scale.y , 0, 0, background.getWidth(), background.getHeight(), (float)width/(float)background.getWidth()/scale.x, (float)height/(float)background.getHeight()/scale.y);
        displayFont.getData().setScale(0.4f);

        float startX = canvas.getWidth()/2-scale.x*(100+20)*4;
        float yOdd = canvas.getHeight()  - canvas.getHeight()/3;
        float yEven = canvas.getHeight()/3;


        for(int i = 0; i < credits.length; i++){
            Texture creditTexture = credits[i].texture;
            float y = i%2 == 0 ? yEven : yOdd;

            canvas.draw(creditTexture,(startX+scale.x*(100+20)*i),y, 0, 0, creditTexture.getWidth(), creditTexture.getHeight(), scale.x*(100.0f/creditTexture.getWidth()), scale.x*(100.0f/creditTexture.getHeight()));
            displayFont.setColor(Color.ORANGE);
            canvas.drawText(credits[i].name, displayFont, (startX+scale.x*(100+20)*i), y - 20f);
            displayFont.setColor(Color.WHITE);
            for(int j = 0; j < credits[i].roles.length; j++){
                canvas.drawText(credits[i].roles[j], displayFont, (startX+scale.x*(100+20)*i), y - 20f - (j+1)*1.2f*displayFont.getLineHeight());
            }
        }
        canvas.end();
    }

    @Override
    public void resize(int width, int height) {
        float scaleMin = Math.min(canvas.getWidth()/bounds.x, canvas.getHeight()/bounds.y);
        scale.x = scaleMin;
        scale.y = scaleMin;
        canvas.setCameraPos(0.5f*width,0.5f*height);
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
