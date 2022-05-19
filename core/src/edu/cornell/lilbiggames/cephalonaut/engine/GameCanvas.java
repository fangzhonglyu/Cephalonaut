/*
 * GameCanvas.java
 *
 * To properly follow the model-view-controller separation, we should not have
 * any specific drawing code in GameMode. All of that code goes here.  As
 * with GameEngine, this is a class that you are going to want to copy for
 * your own projects.
 *
 * An important part of this canvas design is that it is loosely coupled with
 * the model classes. All of the drawing methods are abstracted enough that
 * it does not require knowledge of the interfaces of the model classes.  This
 * important, as the model classes are likely to change often.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.lilbiggames.cephalonaut.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.lilbiggames.cephalonaut.engine.ui.Slider;

import java.sql.Time;
import java.util.Date;

/**
 * Primary view class for the game, abstracting the basic graphics calls.
 * 
 * This version of GameCanvas only supports both rectangular and polygonal Sprite
 * drawing.  It also supports a debug mode that draws polygonal outlines.  However,
 * that mode must be done in a separate begin/end pass.
 */
public class GameCanvas {
	/** Enumeration to track which pass we are in */
	private enum DrawPass {
		/** We are not drawing */
		INACTIVE,
		/** We are drawing sprites */
		STANDARD,
		/** We are drawing outlines */
		DEBUG
	}
	
	/**
	 * Enumeration of supported BlendStates.
	 *
	 * For reasons of convenience, we do not allow user-defined blend functions.
	 * 99% of the time, we find that the following blend modes are sufficient
	 * (particularly with 2D games).
	 */
	public enum BlendState {
		/** Alpha blending on, assuming the colors have pre-multipled alpha (DEFAULT) */
		ALPHA_BLEND,
		/** Alpha blending on, assuming the colors have no pre-multipled alpha */
		NO_PREMULT,
		/** Color values are added together, causing a white-out effect */
		ADDITIVE,
		/** Color values are draw on top of one another with no transparency support */
		OPAQUE
	}	

	
	/** Drawing context to handle textures AND POLYGONS as sprites */
	private PolygonSpriteBatch spriteBatch;
	
	/** Rendering context for the debug outlines */
	private ShapeRenderer debugRender;
	
	/** Track whether or not we are active (for error checking) */
	private DrawPass active;
	
	/** The current color blending mode */
	private BlendState blend;
	
	/** Camera for the underlying SpriteBatch */
	private OrthographicCamera camera;

	/** ShapeRenderer for the fuel bar*/
	private ShapeRenderer shapeRen;
	
	/** Value to cache window width (if we are currently full screen) */
	int width;
	/** Value to cache window height (if we are currently full screen) */
	int height;

	// CACHE OBJECTS
	/** Affine cache for current sprite to draw */
	private Affine2 local;
	/** Affine cache for all sprites this drawing pass */
	private Matrix4 global;
	private Vector2 vertex;
	/** Cache object to handle raw textures */
	private TextureRegion holder;

	private final ShaderProgram shaderProgram;
	private final ShaderProgram accretionShader;

	private FrameBuffer bgFrame;
	private FrameBuffer fgFrame;
	private FrameBuffer temp;

	private final float[] blackHoles = new float[60];
	private int blackHoleCount;

	/**
	 * Creates a new GameCanvas determined by the application configuration.
	 * 
	 * Width, height, and fullscreen are taken from the LWGJApplicationConfig
	 * object used to start the application.  This constructor initializes all
	 * of the necessary graphics objects.
	 */
	public GameCanvas() {
		active = DrawPass.INACTIVE;
		spriteBatch = new PolygonSpriteBatch();
		debugRender = new ShapeRenderer();
		shapeRen = new ShapeRenderer();

		// Set the projection matrix (for proper scaling)
		camera = new OrthographicCamera(getWidth(),getHeight());
		camera.setToOrtho(false);
		spriteBatch.setProjectionMatrix(camera.combined);
		debugRender.setProjectionMatrix(camera.combined);

		spriteBatch.enableBlending();

		// Initialize the cache objects
		holder = new TextureRegion();
		local  = new Affine2();
		global = new Matrix4();
		vertex = new Vector2();

		String vertexShader = Gdx.files.internal("shaders/vertex.glsl").readString();
		String fragmentShader = Gdx.files.internal("shaders/fragment.glsl").readString();
		String fragmentAccretionShader = Gdx.files.internal("shaders/fragment_accretion.glsl").readString();

		shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
		accretionShader = new ShaderProgram(vertexShader, fragmentAccretionShader);

		resize();
	}

	public void setCameraPos(float x, float y) {
		camera.position.x = (int)x;
		camera.position.y = (int)y;
		camera.update();
	}

	public void setCameraPos(Rectangle bounds, Vector2 scale, float x, float y) {
		float bounds_width = (bounds.width - 0.5f) * scale.x;
		float bounds_height = (bounds.height - 0.5f) * scale.x;
		float bounds_x = (bounds.x - 0.5f) * scale.y;
		float bounds_y = (bounds.y - 0.5f) * scale.y;


		if (bounds_width <= camera.viewportWidth) {
			camera.position.x = bounds_x + bounds_width / 2;
		} else if (x + camera.viewportWidth / 2 > bounds_width) {
			camera.position.x = bounds_width - camera.viewportWidth / 2;
		} else if (x - camera.viewportWidth / 2 < bounds_x) {
			camera.position.x = bounds_x + camera.viewportWidth / 2;
		} else {
			camera.position.x = x;
		}

		if (bounds_height <= camera.viewportHeight) {
			camera.position.y = bounds_y + bounds_height / 2;
		} else if (y + camera.viewportHeight / 2 > bounds_height) {
			camera.position.y = bounds_height - camera.viewportHeight / 2;
		} else if (y - camera.viewportHeight / 2 < bounds_y) {
			camera.position.y = bounds_y + camera.viewportHeight / 2;
		} else {
			camera.position.y = y;
		}
		camera.position.x = (int)camera.position.x;
		camera.position.y = (int)camera.position.y;
		camera.update();
	}

	public float getCameraX() {
		return camera.position.x;
	}

	public float getCameraY() {
		return camera.position.y;
	}
		
    /**
     * Eliminate any resources that should be garbage collected manually.
     */
    public void dispose() {
		if (active != DrawPass.INACTIVE) {
			Gdx.app.error("GameCanvas", "Cannot dispose while drawing active", new IllegalStateException());
			return;
		}
		spriteBatch.dispose();
    	spriteBatch = null;
    	local  = null;
    	global = null;
    	vertex = null;
    	holder = null;
    }

	/**
	 * Returns the width of this canvas
	 *
	 * This currently gets its value from Gdx.graphics.getWidth()
	 *
	 * @return the width of this canvas
	 */
	public int getWidth() {
		return Gdx.graphics.getWidth();
	}
	
	/**
	 * Changes the width of this canvas
	 *
	 * This method raises an IllegalStateException if called while drawing is
	 * active (e.g. in-between a begin-end pair).
	 *
	 * @param width the canvas width
	 */
	public void setWidth(int width) {
		if (active != DrawPass.INACTIVE) {
			Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		this.width = width;
		if (!isFullscreen()) {
			Gdx.graphics.setWindowedMode(width, getHeight());
		}
		resize();
	}
	
	/**
	 * Returns the height of this canvas
	 *
	 * This currently gets its value from Gdx.graphics.getHeight()
	 *
	 * @return the height of this canvas
	 */
	public int getHeight() {
		return Gdx.graphics.getHeight();
	}
	
	/**
	 * Changes the height of this canvas
	 *
	 * This method raises an IllegalStateException if called while drawing is
	 * active (e.g. in-between a begin-end pair).
	 *
	 * @param height the canvas height
	 */
	public void setHeight(int height) {
		if (active != DrawPass.INACTIVE) {
			Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		this.height = height;
		if (!isFullscreen()) {
			Gdx.graphics.setWindowedMode(getWidth(), height);	
		}
		resize();
	}
	
	/**
	 * Returns the dimensions of this canvas
	 *
	 * @return the dimensions of this canvas
	 */
	public Vector2 getSize() {
		return new Vector2(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
	}
	
	/**
	 * Changes the width and height of this canvas
	 *
	 * This method raises an IllegalStateException if called while drawing is
	 * active (e.g. in-between a begin-end pair).
	 *
	 * @param width the canvas width
	 * @param height the canvas height
	 */
	public void setSize(int width, int height) {
		if (active != DrawPass.INACTIVE) {
			Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		this.width = width;
		this.height = height;
		if (!isFullscreen()) {
			Gdx.graphics.setWindowedMode(width, height);
		}
		resize();
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		shapeRen.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
	}
	
	/**
	 * Returns whether this canvas is currently fullscreen.
	 *
	 * @return whether this canvas is currently fullscreen.
	 */	 
	public boolean isFullscreen() {
		return Gdx.graphics.isFullscreen(); 
	}
	
	/**
	 * Sets whether or not this canvas should change to fullscreen.
	 *
	 * If desktop is true, it will use the current desktop resolution for
	 * fullscreen, and not the width and height set in the configuration
	 * object at the start of the application. This parameter has no effect
	 * if fullscreen is false.
	 *
	 * This method raises an IllegalStateException if called while drawing is
	 * active (e.g. in-between a begin-end pair).
	 *
	 * @param desktop 	 Whether to use the current desktop resolution
	 */	 
	public void setFullscreen(boolean value, boolean desktop) {
		if (active != DrawPass.INACTIVE) {
			Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		if (value) {
			Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		} else {
			Gdx.graphics.setWindowedMode(width, height);
		}
	}
	
	/**
	 * Resets the SpriteBatch camera when this canvas is resized.
	 *
	 * If you do not call this when the window is resized, you will get
	 * weird scaling issues.
	 */
	 public void resize() {
//		shaderProgram.setUniformf("u_res", getWidth(), getHeight());
		// Resizing screws up the spriteBatch projection matrix
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());
		shapeRen.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());
		camera.setToOrtho(false, getWidth(), getHeight());

		bgFrame = new FrameBuffer(Pixmap.Format.RGB888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
		fgFrame = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
		temp = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
	}
	
	/**
	 * Returns the current color blending state for this canvas.
	 *
	 * Textures draw to this canvas will be composited according
	 * to the rules of this blend state.
	 *a
	 * @return the current color blending state for this canvas
	 */
	public BlendState getBlendState() {
		return blend;
	}
	
	/**
	 * Sets the color blending state for this canvas.
	 *
	 * Any texture draw subsequent to this call will use the rules of this blend 
	 * state to composite with other textures.  Unlike the other setters, if it is 
	 * perfectly safe to use this setter while  drawing is active (e.g. in-between 
	 * a begin-end pair).  
	 *
	 * @param state the color blending rule
	 */
	public void setBlendState(BlendState state) {
		if (state == blend) {
			return;
		}
		switch (state) {
		case NO_PREMULT:
			spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case ALPHA_BLEND:
			spriteBatch.setBlendFunction(GL20.GL_ONE,GL20.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case ADDITIVE:
			spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA,GL20.GL_ONE);
			break;
		case OPAQUE:
			spriteBatch.setBlendFunction(GL20.GL_ONE,GL20.GL_ZERO);
			break;
		}
		blend = state;
	}

	private void switchToSprite() {
		if (!spriteBatch.isDrawing()) {
			shapeRen.end();
			fgFrame.end();
			bgFrame.begin();
			spriteBatch.begin();
		}
	}

	private void switchToShape() {
		if (!shapeRen.isDrawing()) {
			spriteBatch.end();
			bgFrame.end();
			fgFrame.begin();
		}
	}
	
	/**
	 * Clear the screen so we can start a new animation frame
	 */
	public void clear() {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		bgFrame.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		bgFrame.end();
		fgFrame.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		fgFrame.end();
		temp.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		temp.end();

		Gdx.gl.glClearColor(0.047f, 0.086f, 0.31f, 1.0f);  // Homage to the XNA years
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}

	/**
	 * Start a standard drawing sequence.
	 *
	 * Nothing is flushed to the graphics card until the method end() is called.
	 *
	 * @param affine the global transform apply to the camera
	 */
    public void begin(Affine2 affine) {
		global.setAsAffine(affine);
    	global.mulLeft(camera.combined);
		spriteBatch.setProjectionMatrix(global);
		setBlendState(BlendState.NO_PREMULT);
		spriteBatch.begin();
    	active = DrawPass.STANDARD;
    }

	/**
	 * Start a standard drawing sequence.
	 *
	 * Nothing is flushed to the graphics card until the method end() is called.
	 *
	 * @param sx the amount to scale the x-axis
	 * @param sy the amount to scale the y-axis
	 */
    public void begin(float sx, float sy) {
		global.idt();
		global.scl(sx,sy,1.0f);
    	global.mulLeft(camera.combined);
		spriteBatch.setProjectionMatrix(global);
		
    	spriteBatch.begin();
    	active = DrawPass.STANDARD;
    }
    
	/**
	 * Start a standard drawing sequence.
	 *
	 * Nothing is flushed to the graphics card until the method end() is called.
	 */
    public void begin() {
		blackHoleCount = 0;

		Gdx.gl.glClearColor(0, 0, 0, 0);
		fgFrame.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		fgFrame.end();
		temp.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		temp.end();
		bgFrame.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		bgFrame.end();

		bgFrame.begin();
		spriteBatch.setProjectionMatrix(camera.combined);
    	spriteBatch.begin();
    	shapeRen.setProjectionMatrix(camera.combined);
    	active = DrawPass.STANDARD;
    }

	public void setBH(float x, float y, float radius) {
		blackHoles[3 * blackHoleCount] = x - (camera.position.x - camera.viewportWidth / 2f);
		blackHoles[3 * blackHoleCount + 1] = y - (camera.position.y - camera.viewportHeight / 2f);
		blackHoles[3 * blackHoleCount + 2] = radius;
		blackHoleCount++;
	}

	/**
	 * Ends a drawing sequence, flushing textures to the graphics card.
	 */
    public void end() {
		spriteBatch.flush();
		bgFrame.end();

		float x = camera.position.x - camera.viewportWidth / 2f;
		float y = camera.position.y - camera.viewportHeight / 2f;

		int width = getWidth();
		int height = getHeight();

		// Draw accretion disk from bgFrame onto temp
		temp.begin();
		// COMMENT FOLLOWING LINE TO DISABLE ACCRETION SHADERS:
//		spriteBatch.setShader(accretionShader);
//		accretionShader.setUniformf("u_radius", 16 );
		accretionShader.setUniform3fv("u_bh", blackHoles, 0, 3 * blackHoleCount);
		accretionShader.setUniformi("u_bh_count", blackHoleCount);
		accretionShader.setUniformf("u_res", width, height);
		accretionShader.setUniformf("u_time", (System.currentTimeMillis() % 1000000) / 1000f);

		//		shaderProgram.setUniformMatrix("u_projTrans", spriteBatch.getProjectionMatrix());
		spriteBatch.draw(bgFrame.getColorBufferTexture(), x, y, width, height, 0, 0, width, height, false, true);
		spriteBatch.flush();
		temp.end();

		// Draw black hole warping from temp onto screen
		spriteBatch.setShader(shaderProgram);
		shaderProgram.setUniform3fv("u_bh", blackHoles, 0, 3 * blackHoleCount);
		shaderProgram.setUniformi("u_bh_count", blackHoleCount);
		shaderProgram.setUniformf("u_res", width, height);
		spriteBatch.draw(temp.getColorBufferTexture(), x, y, width, height, 0, 0, width, height, false, true);

		// Draw fgFrame onto screen
		spriteBatch.setShader(null);
		spriteBatch.draw(fgFrame.getColorBufferTexture(), x, y, width, height, 0, 0, width, height, false, true);

		spriteBatch.end();
		active = DrawPass.INACTIVE;
    }

	public void drawFade(float fadeOut) {
		if (active == DrawPass.STANDARD) {
			spriteBatch.end();
			bgFrame.end();
		}

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		shapeRen.begin(ShapeRenderer.ShapeType.Filled);
		shapeRen.setColor(0, 0, 0, fadeOut);
		shapeRen.rect(-10000, -10000, 1000000, 1000000); // warning: jank
		shapeRen.end();

		if (active == DrawPass.STANDARD) {
			spriteBatch.begin();
			bgFrame.begin();
		}
	}

	public void drawDialogueBox(float fade) {
		switchToShape();
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		// dim background
		shapeRen.begin(ShapeRenderer.ShapeType.Filled);
		shapeRen.setColor(0, 0, 0, fade);
		shapeRen.rect(getCameraX() - getWidth() * .5f, getCameraY() - getHeight() / 2 , getWidth(), getHeight());
		shapeRen.end();

		// draw box outline
		shapeRen.begin(ShapeRenderer.ShapeType.Line);
		Gdx.gl.glLineWidth(10f);
		shapeRen.setColor(255, 255, 255, 2f * fade);
		shapeRen.rect(getCameraX() - getWidth() * .4f, getCameraY() - getHeight() / 2 , getWidth() * .8f, 300f*getHeight()/1080f);
		shapeRen.end();

		// draw actual box
		shapeRen.begin(ShapeRenderer.ShapeType.Filled);
		shapeRen.setColor(0, 0, 0,  1.5f * fade);
		shapeRen.rect(getCameraX() - getWidth() * .4f, getCameraY() - getHeight() / 2 , getWidth() * .8f, 300f*getHeight()/1080f);
		shapeRen.end();
		Gdx.gl.glLineWidth(1f);

		switchToSprite();
	}


	/**
	 * Draws the tinted texture at the given position.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * Unless otherwise transformed by the global transform (@see begin(Affine2)),
	 * the texture will be unscaled.  The bottom left of the texture will be positioned
	 * at the given coordinates.
	 *
	 * @param image The texture to draw
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 */
	public void draw(Texture image, float x, float y) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		// Unlike Lab 1, we can shortcut without a master drawing method
    	spriteBatch.setColor(Color.WHITE);
		spriteBatch.draw(image, x,  y);
	}

	public void draw(Texture image, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight, float sx, float sy) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

		spriteBatch.setColor(Color.WHITE);
		spriteBatch.draw(image, x, y, 0, 0, image.getWidth(), image.getHeight(), sx, sy, 0,
						srcX, srcY, srcWidth, srcHeight, false, false);
	}

	public void draw(Texture image, float x, float y, float width, float height, int srcX, int srcY, int srcWidth, int srcHeight, float sx, float sy) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

		spriteBatch.setColor(Color.WHITE);
		spriteBatch.draw(image, x, y, 0, 0, width, height, sx, sy, 0,
				srcX, srcY, srcWidth, srcHeight, false, false);
	}
	
	/**
	 * Draws the tinted texture at the given position.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * Unless otherwise transformed by the global transform (@see begin(Affine2)),
	 * the texture will be unscaled.  The bottom left of the texture will be positioned
	 * at the given coordinates.
	 *
	 * @param image The texture to draw
	 * @param tint  The color tint
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 * @param width	The texture width
	 * @param height The texture height
	 */
	public void draw(Texture image, Color tint, float x, float y, float width, float height) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		// Unlike Lab 1, we can shortcut without a master drawing method
    	spriteBatch.setColor(tint);
		spriteBatch.draw(image, x,  y, width, height);
	}
	
	/**
	 * Draws the tinted texture at the given position.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * Unless otherwise transformed by the global transform (@see begin(Affine2)),
	 * the texture will be unscaled.  The bottom left of the texture will be positioned
	 * at the given coordinates.
	 *
	 * @param image The texture to draw
	 * @param tint  The color tint
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the texture origin (on screen)
	 * @param y 	The y-coordinate of the texture origin (on screen)
	 * @param width	The texture width
	 * @param height The texture height
	 */
	public void draw(Texture image, Color tint, float ox, float oy, float x, float y, float width, float height) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		// Call the master drawing method (more efficient that base method)
		holder.setRegion(image);
		draw(holder, tint, x-ox, y-oy, width, height);
	}


	/**
	 * Draws the tinted texture with the given transformations
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * The transformations are BEFORE after the global transform (@see begin(Affine2)).  
	 * As a result, the specified texture origin will be applied to all transforms 
	 * (both the local and global).
	 *
	 * The local transformations in this method are applied in the following order: 
	 * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
	 *
	 * @param image The texture to draw
	 * @param tint  The color tint
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the texture origin (on screen)
	 * @param y 	The y-coordinate of the texture origin (on screen)
	 * @param angle The rotation angle (in degrees) about the origin.
	 * @param sx 	The x-axis scaling factor
	 * @param sy 	The y-axis scaling factor
	 */	
	public void draw(Texture image, Color tint, float ox, float oy, 
					float x, float y, float angle, float sx, float sy) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		// Call the master drawing method (more efficient that base method)
		holder.setRegion(image);
		draw(holder,tint,ox,oy,x,y,angle,sx,sy);
	}

	public void drawFg(Texture image, Color tint, float ox, float oy,
					 float x, float y, float angle, float sx, float sy) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

		// Call the master drawing method (more efficient that base method)
		spriteBatch.flush();
		fgFrame.begin();
		holder.setRegion(image);
		draw(holder,tint,ox,oy,x,y,angle,sx,sy);
		spriteBatch.flush();
		bgFrame.begin();
	}
	
	/**
	 * Draws the tinted texture with the given transformations
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * The transformations are BEFORE after the global transform (@see begin(Affine2)).  
	 * As a result, the specified texture origin will be applied to all transforms 
	 * (both the local and global).
	 *
	 * The local transformations in this method are applied in the following order: 
	 * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
	 *
	 * @param image The texture to draw
	 * @param tint  The color tint
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param transform  The image transform
	 */	
	public void draw(Texture image, Color tint, float ox, float oy, Affine2 transform) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		// Call the master drawing method (we have to for transforms)
		holder.setRegion(image);
		draw(holder,tint,ox,oy,transform);
	}
	
	/**
	 * Draws the tinted texture region (filmstrip) at the given position.
	 *
	 * A texture region is a single texture file that can hold one or more textures.
	 * It is used for filmstrip animation.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * Unless otherwise transformed by the global transform (@see begin(Affine2)),
	 * the texture will be unscaled.  The bottom left of the texture will be positioned
	 * at the given coordinates.
	 *
	 * @param region The texture to draw
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 */
	public void draw(TextureRegion region, float x, float y) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		// Unlike Lab 1, we can shortcut without a master drawing method
    	spriteBatch.setColor(Color.WHITE);
		spriteBatch.draw(region, x,  y);
	}

	/**
	 * Draws the tinted texture at the given position.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * Unless otherwise transformed by the global transform (@see begin(Affine2)),
	 * the texture will be unscaled.  The bottom left of the texture will be positioned
	 * at the given coordinates.
	 *region
	 * @param tint  The color tint
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 * @param width	The texture width
	 * @param height The texture height
	 */
	public void draw(TextureRegion region, Color tint, float x, float y, float width, float height) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		// Unlike Lab 1, we can shortcut without a master drawing method
    	spriteBatch.setColor(tint);
		spriteBatch.draw(region, x,  y, width, height);
	}
	
	/**
	 * Draws the tinted texture at the given position.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * Unless otherwise transformed by the global transform (@see begin(Affine2)),
	 * the texture will be unscaled.  The bottom left of the texture will be positioned
	 * at the given coordinates.
	 *
	 * @param region The texture to draw
	 * @param tint  The color tint
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the texture origin (on screen)
	 * @param y 	The y-coordinate of the texture origin (on screen)
	 * @param width	The texture width
	 * @param height The texture height
	 */	
	public void draw(TextureRegion region, Color tint, float ox, float oy, float x, float y, float width, float height) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		// Unlike Lab 1, we can shortcut without a master drawing method
    	spriteBatch.setColor(tint);
		spriteBatch.draw(region, x-ox, y-oy, width, height);
	}

	/**
	 * Draws the tinted texture region (filmstrip) with the given transformations
	 *
	 * A texture region is a single texture file that can hold one or more textures.
	 * It is used for filmstrip animation.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * The transformations are BEFORE after the global transform (@see begin(Affine2)).  
	 * As a result, the specified texture origin will be applied to all transforms 
	 * (both the local and global).
	 *
	 * The local transformations in this method are applied in the following order: 
	 * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
	 *
	 * @param region The texture to draw
	 * @param tint  The color tint
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the texture origin (on screen)
	 * @param y 	The y-coordinate of the texture origin (on screen)
	 * @param angle The rotation angle (in degrees) about the origin.
	 * @param sx 	The x-axis scaling factor
	 * @param sy 	The y-axis scaling factor
	 */	
	public void draw(TextureRegion region, Color tint, float ox, float oy, 
					 float x, float y, float angle, float sx, float sy) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

		// BUG: The draw command for texture regions does not work properly.
		// There is a workaround, but it will break if the bug is fixed.
		// For now, it is better to set the affine transform directly.
		computeTransform(ox,oy,x,y,angle,sx,sy);
		spriteBatch.setColor(tint);
		spriteBatch.draw(region, region.getRegionWidth(), region.getRegionHeight(), local);
	}

	public void drawFg(TextureRegion region, Color tint, float ox, float oy,
					 float x, float y, float angle, float sx, float sy) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

		spriteBatch.flush();
		bgFrame.end();
		fgFrame.begin();

		// BUG: The draw command for texture regions does not work properly.
		// There is a workaround, but it will break if the bug is fixed.
		// For now, it is better to set the affine transform directly.
		computeTransform(ox,oy,x,y,angle,sx,sy);
		spriteBatch.setColor(tint);
		spriteBatch.draw(region, region.getRegionWidth(), region.getRegionHeight(), local);

		spriteBatch.flush();
		fgFrame.end();
		bgFrame.begin();
	}

	public void drawSimpleFuelBar(float ink, float maxInk, float x, float y) {
		float percent = ink / maxInk;
		switchToShape();
		shapeRen.begin(ShapeRenderer.ShapeType.Filled);

		float width = getWidth() / 18f;
		float height = getHeight() * 15f / 1080f;
		float left = x - width / 2f;
		float bottom = y;

		// Draw container
		shapeRen.setColor(Color.BLACK);
		shapeRen.rect(left - 1, bottom - 1, width + 2, height + 2);
		shapeRen.setColor(.85f, .85f, .85f, 1f);
		shapeRen.rect(left + 1, bottom + 1, width - 2, height - 2);

		// Draw fuel indicator
		Color color = percent > 0.6  ? Color.PURPLE : percent > 0.3 ? Color.ORANGE : Color.RED;
		shapeRen.setColor(color);
		shapeRen.rect(left + 1, bottom + 1, width * percent - 2, height - 2);
		shapeRen.setColor(color.cpy().mul(0.7f));
		shapeRen.rect(left + 1, bottom + 1, width * percent - 2, (height - 2) * 0.4f);

		// Draw lines
		shapeRen.setColor(Color.BLACK);
		final float INK_PER_LINE = .25f;
		for (int i = 1; i < maxInk / INK_PER_LINE; i++) {
			float lineX = left + INK_PER_LINE / maxInk * width * i;
			shapeRen.rectLine(lineX, bottom, lineX, bottom + height * .7f, 3);
		}

		switchToSprite();
	}

	public void drawBlackHoleOutline(float x, float y, float radius){
		switchToShape();

		shapeRen.begin(ShapeRenderer.ShapeType.Line);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glLineWidth(4f);
		shapeRen.setColor(Color.valueOf("ff7c21A0"));
		shapeRen.circle(x, y, radius, 200);

		switchToSprite();
	}

	public void drawLevelEndGlow(float x, float y){
		switchToShape();

		shapeRen.begin(ShapeRenderer.ShapeType.Filled);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glLineWidth(4f);
		shapeRen.setColor(Color.valueOf("000000FF"));
		shapeRen.circle(x, y, 50f, 200);

		switchToSprite();
	}


	public void drawSlider(Slider slider){
		float x = slider.getPosition().x;
		float y = slider.getPosition().y;

		switchToShape();

		shapeRen.begin(ShapeRenderer.ShapeType.Line);
		shapeRen.setColor(slider.getColor());
		shapeRen.rect(x - slider.getWidth()/2.0f, y, slider.getWidth(), slider.getHeight());
		shapeRen.end();
		shapeRen.begin(ShapeRenderer.ShapeType.Filled);
		shapeRen.rect(slider.getKnobPosition().x, y - slider.getHeight()/2f, slider.getKnobRadius(), 2*slider.getKnobRadius());

		switchToSprite();
	}

	/**
	 * Draws the tinted texture with the given transformations
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * The transformations are BEFORE after the global transform (@see begin(Affine2)).  
	 * As a result, the specified texture origin will be applied to all transforms 
	 * (both the local and global).
	 *
	 * The local transformations in this method are applied in the following order: 
	 * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
	 *
	 * @param tint  The color tint
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 */	
	public void draw(TextureRegion region, Color tint, float ox, float oy, Affine2 affine) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

		local.set(affine);
		local.translate(-ox,-oy);				
		spriteBatch.setColor(tint);
		spriteBatch.draw(region, region.getRegionWidth(), region.getRegionHeight(), local);
	}

	/**
	 * Draws the polygonal region with the given transformations
	 *
	 * A polygon region is a texture region with attached vertices so that it draws a
	 * textured polygon. The polygon vertices are relative to the texture file.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * The transformations are BEFORE after the global transform (@see begin(Affine2)).  
	 * As a result, the specified texture origin will be applied to all transforms 
	 * (both the local and global).
	 *
	 * The local transformations in this method are applied in the following order: 
	 * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
	 *
	 * @param region The polygon to draw
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 */	
	public void draw(PolygonRegion region, float x, float y) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		// Unlike Lab 1, we can shortcut without a master drawing method
    	spriteBatch.setColor(Color.WHITE);
		spriteBatch.draw(region, x,  y);
	}
	
	/**
	 * Draws the polygonal region with the given transformations
	 *
	 * A polygon region is a texture region with attached vertices so that it draws a
	 * textured polygon. The polygon vertices are relative to the texture file.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * The transformations are BEFORE after the global transform (@see begin(Affine2)).  
	 * As a result, the specified texture origin will be applied to all transforms 
	 * (both the local and global).
	 *
	 * The local transformations in this method are applied in the following order: 
	 * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
	 *
	 * @param region The polygon to draw
	 * @param tint  The color tint
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 * @param width	The texture width
	 * @param height The texture height
	 */	
	public void draw(PolygonRegion region, Color tint, float x, float y, float width, float height) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		// Unlike Lab 1, we can shortcut without a master drawing method
    	spriteBatch.setColor(tint);
		spriteBatch.draw(region, x,  y, width, height);
	}
	
	/**
	 * Draws the polygonal region with the given transformations
	 *
	 * A polygon region is a texture region with attached vertices so that it draws a
	 * textured polygon. The polygon vertices are relative to the texture file.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * The transformations are BEFORE after the global transform (@see begin(Affine2)).  
	 * As a result, the specified texture origin will be applied to all transforms 
	 * (both the local and global).
	 *
	 * The local transformations in this method are applied in the following order: 
	 * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
	 *
	 * @param region The polygon to draw
	 * @param tint  The color tint
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the texture origin (on screen)
	 * @param y 	The y-coordinate of the texture origin (on screen)
	 * @param width	The texture width
	 * @param height The texture height
	 */	
	public void draw(PolygonRegion region, Color tint, float ox, float oy, float x, float y, float width, float height) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		// Unlike Lab 1, we can shortcut without a master drawing method
    	spriteBatch.setColor(tint);
		spriteBatch.draw(region, x-ox, y-oy, width, height);
	}
	
	/**
	 * Draws the polygonal region with the given transformations
	 *
	 * A polygon region is a texture region with attached vertices so that it draws a
	 * textured polygon. The polygon vertices are relative to the texture file.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * The transformations are BEFORE after the global transform (@see begin(Affine2)).  
	 * As a result, the specified texture origin will be applied to all transforms 
	 * (both the local and global).
	 *
	 * The local transformations in this method are applied in the following order: 
	 * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
	 *
	 * @param region The polygon to draw
	 * @param tint  The color tint
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the texture origin (on screen)
	 * @param y 	The y-coordinate of the texture origin (on screen)
	 * @param angle The rotation angle (in degrees) about the origin.
	 * @param sx 	The x-axis scaling factor
	 * @param sy 	The y-axis scaling factor
	 */	
	public void draw(PolygonRegion region, Color tint, float ox, float oy, 
					 float x, float y, float angle, float sx, float sy) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		TextureRegion bounds = region.getRegion();
		spriteBatch.setColor(tint);
		spriteBatch.draw(region, x, y, ox, oy, 
						 bounds.getRegionWidth(), bounds.getRegionHeight(), 
						 sx, sy, 180.0f*angle/(float)Math.PI);
	}

	/**
	 * Draws the polygonal region with the given transformations
	 *
	 * A polygon region is a texture region with attached vertices so that it draws a
	 * textured polygon. The polygon vertices are relative to the texture file.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * The transformations are BEFORE after the global transform (@see begin(Affine2)).  
	 * As a result, the specified texture origin will be applied to all transforms 
	 * (both the local and global).
	 *
	 * The local transformations in this method are applied in the following order: 
	 * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
	 *
	 * @param region The polygon to draw
	 * @param tint  The color tint
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 */	
	public void draw(PolygonRegion region, Color tint, float ox, float oy, Affine2 affine) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

		local.set(affine);
		local.translate(-ox,-oy);
		computeVertices(local,region.getVertices());

		spriteBatch.setColor(tint);
		spriteBatch.draw(region, 0, 0);
		
		// Invert and restore
		local.inv();
		computeVertices(local,region.getVertices());
	}
	
	/**
	 * Transform the given vertices by the affine transform
	 */
	private void computeVertices(Affine2 affine, float[] vertices) {
		for(int ii = 0; ii < vertices.length; ii += 2) {
			vertex.set(vertices[2*ii], vertices[2*ii+1]);
			affine.applyTo(vertex);
			vertices[2*ii  ] = vertex.x;
			vertices[2*ii+1] = vertex.y;
		}
	}

	/**
	 * Draw an unscaled overlay image.
	 *
	 * An overlay image is one that is not scaled by the global transform
	 * This is ideal for backgrounds, foregrounds and uniform HUDs that do not
	 * track the camera.
	 *
	 * @param image Texture to draw as an overlay
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 */
	public void drawOverlay(Texture image, float x, float y) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		drawOverlay(image,Color.WHITE,x,y);
	}

	/**
	 * Draw an unscaled overlay image tinted by the given color.
	 *
	 * An overlay image is one that is not scaled by the global transform
	 * This is ideal for backgrounds, foregrounds and uniform HUDs that do not
	 * track the camera.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * @param image Texture to draw as an overlay
	 * @param tint  The color tint
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 */
	public void drawOverlay(Texture image, Color tint, float x, float y) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		spriteBatch.setColor(tint);
		spriteBatch.draw(image, x, y);
	}

	/**
	 * Draw an stretched overlay image.
	 *
	 * An overlay image is one that is not scaled by the global transform
	 * This is ideal for backgrounds, foregrounds and uniform HUDs that do not
	 * track the camera.
	 *
	 * The image will be drawn starting at the bottom right corner, and will
	 * be stretched to fill the whole screen if appropriate.
	 *
	 * @param image Texture to draw as an overlay
	 * @param fill	Whether to stretch the image to fill the screen
	 */
	public void drawOverlay(Texture image, boolean fill) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		drawOverlay(image,Color.WHITE,fill);
	}

	/**
	 * Draw an stretched overlay image tinted by the given color.
	 *
	 * An overlay image is one that is not scaled by the global transform
	 * This is ideal for backgrounds, foregrounds and uniform HUDs that do not
	 * track the camera.
	 *
	 * The image will be drawn starting at the bottom right corner, and will
	 * be stretched to fill the whole screen if appropriate.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * @param image Texture to draw as an overlay
	 * @param tint  The color tint
	 * @param fill	Whether to stretch the image to fill the screen
	 */
	public void drawOverlay(Texture image, Color tint, boolean fill) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		float w, h;
		if (fill) {
			w = getWidth();
			h = getHeight();
		} else {
			w = image.getWidth();
			h = image.getHeight();
		}
		spriteBatch.setColor(tint);
		spriteBatch.draw(image, 0, 0, w, h);
	}

    /**
     * Draws text on the screen.
     *
     * @param text The string to draw
     * @param font The font to use
     * @param x The x-coordinate of the lower-left corner
     * @param y The y-coordinate of the lower-left corner
     */
    public void drawText(String text, BitmapFont font, float x, float y) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		spriteBatch.flush();
		fgFrame.begin();
		GlyphLayout layout = new GlyphLayout(font,text);
		font.draw(spriteBatch, layout, x, y);
		spriteBatch.flush();
		bgFrame.begin();
    }

	/**
	 * Draws text in top left of the screen.
	 *
	 * @param text The string to draw
	 * @param font The font to use
	 */
	public void drawTextTopRight(String text, BitmapFont font, int xOffset, int yOffset) {
		float x = getWidth() * 0.40f + getCameraX() - xOffset;
		float y = getHeight() * 0.47f + getCameraY() - yOffset;
		drawText(text, font, x, y);
	}

	/**
	 * Draws text in top left of the screen.
	 *
	 * @param text The string to draw
	 * @param font The font to use
	 */
	public void drawTextTopRight(String text, BitmapFont font) {
		drawTextTopRight(text, font, 0, 0);
	}

    /**
     * Draws text centered on the screen.
     *
     * @param text The string to draw
     * @param font The font to use
     * @param offset The y-value offset from the center of the screen.
     */
    public void drawTextCentered(String text, BitmapFont font, float offset) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

		spriteBatch.flush();
		fgFrame.begin();
		GlyphLayout layout = new GlyphLayout(font,text);
		float x = (getWidth()  - layout.width) / 2.0f;
		float y = (getHeight() + layout.height) / 2.0f;
		font.draw(spriteBatch, layout, x, y+offset);
		spriteBatch.flush();
		bgFrame.begin();
    }
    
	/**
	 * Start the debug drawing sequence.
	 *
	 * Nothing is flushed to the graphics card until the method end() is called.
	 *
	 * @param affine the global transform apply to the camera
	 */
    public void beginDebug(Affine2 affine) {
		global.setAsAffine(affine);
    	global.mulLeft(camera.combined);
    	debugRender.setProjectionMatrix(global);
		
    	debugRender.begin(ShapeRenderer.ShapeType.Line);
    	active = DrawPass.DEBUG;
    }
    
	/**
	 * Start the debug drawing sequence.
	 *
	 * Nothing is flushed to the graphics card until the method end() is called.
	 *
	 * @param sx the amount to scale the x-axis
	 * @param sy the amount to scale the y-axis
	 */    
    public void beginDebug(float sx, float sy) {
		global.idt();
		global.scl(sx,sy,1.0f);
    	global.mulLeft(camera.combined);
    	debugRender.setProjectionMatrix(global);
		
    	debugRender.begin(ShapeRenderer.ShapeType.Line);
    	active = DrawPass.DEBUG;
    }

	/**
	 * Start the debug drawing sequence.
	 *
	 * Nothing is flushed to the graphics card until the method end() is called.
	 */
    public void beginDebug() {
    	debugRender.setProjectionMatrix(camera.combined);
    	debugRender.begin(ShapeRenderer.ShapeType.Filled);
    	debugRender.setColor(Color.RED);
    	debugRender.circle(0, 0, 10);
    	debugRender.end();
    	
    	debugRender.begin(ShapeRenderer.ShapeType.Line);
    	active = DrawPass.DEBUG;
    }

	/**
	 * Ends the debug drawing sequence, flushing textures to the graphics card.
	 */
    public void endDebug() {
    	debugRender.end();
    	active = DrawPass.INACTIVE;
    }
    
    /**
     * Draws the outline of the given shape in the specified color
     *
     * @param shape The Box2d shape
     * @param color The outline color
     * @param x  The x-coordinate of the shape position
     * @param y  The y-coordinate of the shape position
     */
    public void drawPhysics(PolygonShape shape, Color color, float x, float y) {
		if (active != DrawPass.DEBUG) {
			Gdx.app.error("GameCanvas", "Cannot draw without active beginDebug()", new IllegalStateException());
			return;
		}
		
    	float x0, y0, x1, y1;
    	debugRender.setColor(color);
    	for(int ii = 0; ii < shape.getVertexCount()-1; ii++) {
    		shape.getVertex(ii  ,vertex);
    		x0 = x+vertex.x; y0 = y+vertex.y;
    		shape.getVertex(ii+1,vertex);
    		x1 = x+vertex.x; y1 = y+vertex.y;
    		debugRender.line(x0, y0, x1, y1);
    	}
    	// Close the loop
		shape.getVertex(shape.getVertexCount()-1,vertex);
		x0 = x+vertex.x; y0 = y+vertex.y;
		shape.getVertex(0,vertex);
		x1 = x+vertex.x; y1 = y+vertex.y;
		debugRender.line(x0, y0, x1, y1);
    }

    /**
     * Draws the outline of the given shape in the specified color
     *
     * @param shape The Box2d shape
     * @param color The outline color
     * @param x  The x-coordinate of the shape position
     * @param y  The y-coordinate of the shape position
     * @param angle  The shape angle of rotation
     */
    public void drawPhysics(PolygonShape shape, Color color, float x, float y, float angle) {
		if (active != DrawPass.DEBUG) {
			Gdx.app.error("GameCanvas", "Cannot draw without active beginDebug()", new IllegalStateException());
			return;
		}
		
		local.setToTranslation(x,y);
		local.rotateRad(angle);
		
    	float x0, y0, x1, y1;
    	debugRender.setColor(color);
    	for(int ii = 0; ii < shape.getVertexCount()-1; ii++) {
    		shape.getVertex(ii  ,vertex);
    		local.applyTo(vertex);
    		x0 = vertex.x; y0 = vertex.y;
    		shape.getVertex(ii+1,vertex);
    		local.applyTo(vertex);
    		x1 = vertex.x; y1 = vertex.y;
    		debugRender.line(x0, y0, x1, y1);
    	}
    	// Close the loop
		shape.getVertex(shape.getVertexCount()-1,vertex);
		local.applyTo(vertex);
		x0 = vertex.x; y0 = vertex.y;
		shape.getVertex(0,vertex);
		local.applyTo(vertex);
		x1 = vertex.x; y1 = vertex.y;
		debugRender.line(x0, y0, x1, y1);
    }

    /**
     * Draws the outline of the given shape in the specified color
     *
     * @param shape The Box2d shape
     * @param color The outline color
     * @param x  The x-coordinate of the shape position
     * @param y  The y-coordinate of the shape position
     * @param angle  The shape angle of rotation
     * @param sx The amount to scale the x-axis
     * @param sx The amount to scale the y-axis
     */
    public void drawPhysics(PolygonShape shape, Color color, float x, float y, float angle, float sx, float sy) {
		if (active != DrawPass.DEBUG) {
			Gdx.app.error("GameCanvas", "Cannot draw without active beginDebug()", new IllegalStateException());
			return;
		}
		
		local.setToScaling(sx,sy);
		local.translate(x,y);
		local.rotateRad(angle);
		
    	float x0, y0, x1, y1;
    	debugRender.setColor(color);
    	for(int ii = 0; ii < shape.getVertexCount()-1; ii++) {
    		shape.getVertex(ii  ,vertex);
    		local.applyTo(vertex);
    		x0 = vertex.x; y0 = vertex.y;
    		shape.getVertex(ii+1,vertex);
    		local.applyTo(vertex);
    		x1 = vertex.x; y1 = vertex.y;
    		debugRender.line(x0, y0, x1, y1);
    	}
    	// Close the loop
		shape.getVertex(shape.getVertexCount()-1,vertex);
		local.applyTo(vertex);
		x0 = vertex.x; y0 = vertex.y;
		shape.getVertex(0,vertex);
		local.applyTo(vertex);
		x1 = vertex.x; y1 = vertex.y;
		debugRender.line(x0, y0, x1, y1);
    }
    
    /** 
     * Draws the outline of the given shape in the specified color
     *
     * The position of the circle is ignored.  Only the radius is used. To move the
     * circle, change the x and y parameters.
     * 
     * @param shape The Box2d shape
     * @param color The outline color
     * @param x  The x-coordinate of the shape position
     * @param y  The y-coordinate of the shape position
     */
    public void drawPhysics(CircleShape shape, Color color, float x, float y) {
		if (active != DrawPass.DEBUG) {
			Gdx.app.error("GameCanvas", "Cannot draw without active beginDebug()", new IllegalStateException());
			return;
		}
		
    	debugRender.setColor(color);
    	debugRender.circle(x, y, shape.getRadius(),12);
    }
    
    /** 
     * Draws the outline of the given shape in the specified color
     *
     * The position of the circle is ignored.  Only the radius is used. To move the
     * circle, change the x and y parameters.
     * 
     * @param shape The Box2d shape
     * @param color The outline color
     * @param x  The x-coordinate of the shape position
     * @param y  The y-coordinate of the shape position
     * @param sx The amount to scale the x-axis
     * @param sx The amount to scale the y-axis
     */
    public void drawPhysics(CircleShape shape, Color color, float x, float y, float sx, float sy) {
		if (active != DrawPass.DEBUG) {
			Gdx.app.error("GameCanvas", "Cannot draw without active beginDebug()", new IllegalStateException());
			return;
		}
		
		float x0 = x*sx;
		float y0 = y*sy;
		float w = shape.getRadius()*sx;
		float h = shape.getRadius()*sy;
    	debugRender.setColor(color);
    	debugRender.ellipse(x0-w, y0-h, 2*w, 2*h, 12);
    }
    
	/**
	 * Compute the affine transform (and store it in local) for this image.
	 * 
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the texture origin (on screen)
	 * @param y 	The y-coordinate of the texture origin (on screen)
	 * @param angle The rotation angle (in degrees) about the origin.
	 * @param sx 	The x-axis scaling factor
	 * @param sy 	The y-axis scaling factor
	 */
	private void computeTransform(float ox, float oy, float x, float y, float angle, float sx, float sy) {
		local.setToTranslation(x,y);
		local.rotate(180.0f*angle/(float)Math.PI);
		local.scale(sx,sy);
		local.translate(-ox,-oy);
	}
}