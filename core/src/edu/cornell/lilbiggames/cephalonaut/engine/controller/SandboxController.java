/*
 * RagdollController.java
 *
 * You are not expected to modify this file at all.  You are free to look at it, however,
 * and determine how it works.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.GameObject;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.GameObjectJson;
import edu.cornell.lilbiggames.cephalonaut.engine.model.CephalonautModel;
import edu.cornell.lilbiggames.cephalonaut.engine.model.PlayMode;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.BoxObstacle;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.Obstacle;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.ObstacleSelector;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Gameplay specific controller for the gameplay prototype.
 */
public class SandboxController extends WorldController {
	/** Reference to the cephalonaut's model */
	private CephalonautModel cephalonaut;


	private Queue<GameObject> gameObjectQueue;
	private List<BoxObstacle> boxObstacles;
	private Map<Integer, TextureRegion> textures;

	/** Mouse selector to move the cephalonaut */
	private ObstacleSelector selector;

	private Texture earthTexture;

	private TextureRegion octopusTexture;
	/** Texture asset for mouse crosshairs */
	private TextureRegion crosshairTexture;

	private CephalonautController cephalonautController;

	private PlayMode level;

	/**
	 * Creates and initialize a new instance of the sandbox
	 */
	public SandboxController() {
		super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY);
		setDebug(false);
		setComplete(false);
		setFailure(false);

	}

	/**
	 * Sets the current level
	 * @param PlayMode level
	 */
	public void setLevel(PlayMode level){
		this.level = level;
	}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
		Vector2 gravity = new Vector2(world.getGravity());
		
		for(Obstacle obj : objects) {
			obj.deactivatePhysics(world);
		}
		objects.clear();
		addQueue.clear();
		world.dispose();
		
		world = new World(gravity,false);
		setComplete(false);
		setFailure(false);
		populateLevel();
	}

	/**
	 * Gather the assets for this controller.
	 *
	 * This method extracts the asset variables from the given asset directory. It
	 * should only be called after the asset directory is completed.
	 *
	 * @param directory	Reference to global asset manager.
	 */
	public void gatherAssets(AssetDirectory directory) {
		// Allocate the tiles
		earthTile = new TextureRegion(directory.getEntry( "earth", Texture.class ));
		octopusTexture = new TextureRegion(directory.getEntry( "octopus", Texture.class ));
//		displayFont = directory.getEntry( "shared:retro" ,BitmapFont.class);
	}

	/**
	 *
	 * @param objectJson
	 * @return BoxObstacle representing created object
	 */
	private BoxObstacle initializeObjectFromJson(JsonValue objectJson, int tileID){
		// just to show what I'm currently thinking for object initialization based on json
		// question: how to deal with location relative to tile?
		float width = objectJson.getFloat("width");
		float height = objectJson.getFloat("height");
		float x = objectJson.getFloat("x");
		float y = objectJson.getFloat("y");

		BoxObstacle cur = new BoxObstacle(x, y, width/32, height/32);
		TextureRegion texture = textures.get(tileID);

		cur.setDrawScale(scale);
		cur.setTint(new Color(0.5f, 0.4f, 0.4f, 1));
		cur.setTexture(texture);
		cur.setTextureScaleX(width * scale.x / (texture.getRegionWidth()*32));
		cur.setTextureScaleY(height * scale.y / (texture.getRegionHeight()*32));

		for(JsonValue property : objectJson.get("properties")){
			switch (property.getString("name")) {
				case "canGrappleOn":
					cur.setGrapple(property.getBoolean("value"));
			}
		}
		return cur;
	}


	/**
	 * Parses the game objects from the
	 */
	private void initializeLevelInfo(){
		Queue<GameObjectJson> gameObjectJsons = level.getGameObjectQueue();
		textures = level.getTextures();
		boxObstacles = new LinkedList<BoxObstacle>();

		Iterator<GameObjectJson> it = gameObjectJsons.iterator();
		while(it.hasNext()){
			GameObjectJson gameObjectJson = it.next();
			JsonValue objectJson = gameObjectJson.getJsonObject();
			int tileID = gameObjectJson.getTileID();

			String name = (objectJson.get("name") != null) ? objectJson.getString("name") : "";

			if(name.equals("Rock")){
				// using a BoxObstacle for now, when we merge with Oliver's code we can use gameobjects
				addObject(initializeObjectFromJson(objectJson, tileID));
			}
		}

	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel() {
		// Make the cephalonaut
		initializeLevelInfo();

		float dwidth  = octopusTexture.getRegionWidth()/scale.x;
		float dheight = octopusTexture.getRegionHeight()/scale.y;
		cephalonaut = new CephalonautModel(10, 10, dwidth, dheight, scale);
		cephalonaut.setTexture(octopusTexture);
		cephalonautController = new CephalonautController(world, cephalonaut);

		addObject(cephalonaut);
		addObject(cephalonaut.getGrapple());
		setDebug(true);

		selector = new ObstacleSelector(world);
		selector.setDrawScale(scale);
		world.setGravity(Vector2.Zero);
	}

	/**
	 * The core gameplay loop of this world.
	 *
	 * This method contains the specific update code for this mini-game. It does
	 * not handle collisions, as those are managed by the parent class WorldController.
	 * This method is called after input is read, but before collisions are resolved.
	 * The very last thing that it should do is apply forces to the appropriate objects.
	 *
	 * @param dt 	Number of seconds since last animation frame
	 */
	public void update(float dt) {
	    // Move an object if touched
		InputController input = InputController.getInstance();

		boolean grappleButton = input.didSecondary();
		Vector2 crossHair = input.getCrossHair();
		boolean inking = input.isThrusterApplied();
		float rotation = input.getRotation();

		cephalonautController.update(grappleButton, crossHair, inking, rotation);
	}
	
	/**
	 * Draw the physics objects together with foreground and background
	 *
	 * This is completely overridden to support custom background and foreground art.
	 *
	 * @param dt Timing values from parent loop
	 */
	public void draw(float dt) {
		canvas.clear();
		canvas.begin();
		for(Obstacle obj : objects) {
			obj.draw(canvas);
		}

		selector.draw(canvas);
		canvas.end();
		
		if (isDebug()) {
			canvas.beginDebug();
			for(Obstacle obj : objects) {
				obj.drawDebug(canvas);
			}
			canvas.endDebug();
		}
	}

}