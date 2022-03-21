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
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.GameObject;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.GameObjectJson;
import edu.cornell.lilbiggames.cephalonaut.engine.model.CephalonautModel;
import edu.cornell.lilbiggames.cephalonaut.engine.model.PlayMode;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.*;

import java.util.*;
import java.util.logging.Level;

/**
 * Gameplay specific controller for the gameplay prototype.
 */
public class SandboxController extends WorldController implements ContactListener {
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
	 * @param  level
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
		
		for(GameObject obj : objects) {
			obj.deactivatePhysics(world);
		}
		objects.clear();
		addQueue.clear();
		world.dispose();
		
		world = new World(gravity,false);
		setComplete(false);
		setFailure(false);
		world.setContactListener(this);
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

	private PolygonObstacle createPolygonObstacle(JsonValue objectJson) {
			float[] vertices = new float[10];
			Iterator<JsonValue> it = objectJson.get("polygon").iterator();
			int i = 0;
			JsonValue curr;
			while(it.hasNext()) {
				curr = it.next();
				vertices[i] = curr.getFloat("x");
				vertices[i + 1] = curr.getFloat("y");
				i += 1;
			}
			return new PolygonObstacle(vertices);
	}

	/** SHOULD BE DELETED OR REFACTORED FOR LEVEL ELEMENTS
	 * @param objectJson json of game object
	 * @param tileID id of game object in reference to texture
	 * @param x position for rendering
	 * @param y position for rendering
	 * @return SimpleObstacle representing created object
	 */
	private SimpleObstacle initializeObjectFromJsonOrig(JsonValue objectJson, int tileID, int x, int y){


		float width = objectJson.getFloat("width");
		float height = objectJson.getFloat("height");

		SimpleObstacle obstacle = new BoxObstacle(x, y,  1, 1);

		TextureRegion texture = textures.get(tileID);
		obstacle.setDrawScale(scale);
		obstacle.setTint(new Color(0.5f, 0.4f, 0.4f, 1));
		obstacle.setTexture(texture);
		obstacle.setTextureScaleX(16 * scale.x / (texture.getRegionWidth()*16));
		obstacle.setTextureScaleY(16 * scale.y / (texture.getRegionHeight()*16));
		for(JsonValue property : objectJson.get("properties")){
			switch (property.getString("name")) {
				case "canGrappleOn":
					obstacle.setGrapple(property.getBoolean("value"));
			}
		}
		return obstacle;
	}

	private LevelElement initializeObjectFromJson(JsonValue objectJson, int tileID, int x, int y){
		if(objectJson.get("polygon") != null) {
			float[] vertices = new float[2 * objectJson.get("polygon").size];
			for(int i = 0; i < objectJson.get("polygon").size; i++) {
				vertices[2 * i] = objectJson.get("polygon").get(i).getFloat("x") / 16;
				vertices[2 * i + 1] = -objectJson.get("polygon").get(i).getFloat("y") / 16;
			}

			LevelElement obstacle = new LevelElement(x, y, vertices, objectJson.getFloat("rotation"), scale, LevelElement.ELEMENT.MISC_POLY);

			TextureRegion texture = textures.get(tileID);
			obstacle.setTextureBottomLeft(texture);
			obstacle.setTextureScaleX(16 * scale.x / (texture.getRegionWidth()*16));
			obstacle.setTextureScaleY(16 * scale.y / (texture.getRegionHeight()*16));
			return obstacle;
		} else {
			LevelElement obstacle = new LevelElement(x, y, objectJson.getFloat("width") / 16,
					objectJson.getFloat("height") / 16, objectJson.getFloat("rotation"), scale,
					LevelElement.ELEMENT.MISC_POLY);

			TextureRegion texture = textures.get(tileID);
			obstacle.setTexture(texture);
			obstacle.setTextureScaleX(16 * scale.x / (texture.getRegionWidth()*16));
			obstacle.setTextureScaleY(16 * scale.y / (texture.getRegionHeight()*16));
			return obstacle;
		}

	}


	/**
	 * Parses the game objects from the LevelLoader
	 */
	private void initializeLevelInfo(){

		// queue of all the game objects
		Queue<GameObjectJson> gameObjectJsons = level.getGameObjectQueue();

		// map of all the textures, you use tileID to reference texture given game object
		textures = level.getTextures();

		boxObstacles = new LinkedList<>();

		// iterate through GameObjectJsons
		Iterator<GameObjectJson> it = gameObjectJsons.iterator();
		while(it.hasNext()){

			GameObjectJson gameObjectJson = it.next();

			// get relevant information from object
			JsonValue objectJson = gameObjectJson.getJsonObject();
			int tileID = gameObjectJson.getTileID();
			int x = gameObjectJson.getX();
			int y = gameObjectJson.getY();

			// MODIFY THE CODE BELOW


			// NOTE: There are multiple ways of doing this.
			/*
			If we want to see what type of shape the object is, then we need to check the following
			if(objectJson.has("polygon")) {
				1. run polygon relevant LevelElement creation
				2. have access to vertices array, but must be parsed as seen in createPolygonObstacle() above
			} else if(objectJson.has("ellipse")) {
				1. run ellipse relevant LevelElement creation
				2. has width and height property. no radius. width and height can be access like rectangle object.
			} else {
				1. run rectangle relevant LevelElement creation
				2. has width and height property.
			}
			*/

			// We can also check the name of the object. Name will allow us to create objects with the
			// same properties. However, this might be unnecessary since all objects should have the same properties
			// with different values, so parsing from the object to the LevelElement should be the same regardless.

			// here is where you can determine what type of object it is
			// there is a name field which allows us to treat the objects equally given they share the same name
			String name = (objectJson.get("name") != null) ? objectJson.getString("name") : "";

			if(name.equals("Rock")){
				// using a SimpleObstacle for now, when we merge with Oliver's code we can use gameobjects
				// TO BE CHANGED HERE
				LevelElement objectToInit = initializeObjectFromJson(objectJson, tileID, x, y);
				if(objectToInit != null) {
					addObject(objectToInit);
				}
//				addObject(initializeObjectFromJsonOrig(objectJson, tileID, x, y));
				// TO BE CHANGED HERE
			}

			// MODIFY THE CODE ABOVE
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

		cephalonaut.setForce(Vector2.Zero);
		for(GameObject object : objects) {
			if(object.getClass() == LevelElement.class) {
//				((LevelElement) object).updateElement();
//				object.update(cephalonaut);
				switch (((LevelElement) object).getElement()) {
					case BLACK_HOLE:
						attract(object);
						break;
					case FLYING_METEOR:
						updateFlyingMeteor((LevelElement) object);
						break;
					case BOOST_PAD:
						boost((LevelElement) object);
						break;
					case DOOR:
						if(((LevelElement) object).getActivated()) {
							openDoor((LevelElement) object);
						}
						break;
					default:
						break;
				}
			}
		}
		cephalonautController.update(grappleButton, crossHair, inking, rotation);
	}

	public void updateFlyingMeteor(LevelElement element) {
		if(element.getPosition().cpy().sub(element.getOriginalPos().cpy()).len() > 5) {
			element.setPosition(element.getOriginalPos());
		}
	}


	public void fly(LevelElement element) {
		switch (element.getDirection()) {
			case UP:
				element.setLinearVelocity(new Vector2(0, METEOR_SPEED));
				break;
			case LEFT:
				element.setLinearVelocity(new Vector2(-METEOR_SPEED, 0));
				break;
			case DOWN:
				element.setLinearVelocity(new Vector2(0, -METEOR_SPEED));
				break;
			case RIGHT:
				element.setLinearVelocity(new Vector2(METEOR_SPEED, 0));
				break;
			default:
				break;
		}
	}

	/** Force from cephalonaut attracted to obj */
	public void attract(GameObject obj) {
		if(Math.abs(obj.getBody().getPosition().cpy().sub(cephalonaut.getPosition().cpy()).len()) < ATTRACT_DIST) {
			Vector2 pos = obj.getBody().getWorldCenter();
			Vector2 objPos = cephalonaut.getBody().getWorldCenter();
			Vector2 force = pos.sub(objPos);

			force.clamp(1, 5);
			force.nor();
			float strength = (9.81f * 1 * cephalonaut.getBody().getMass()) / (force.len() * force.len());
			force.scl(strength);
			cephalonaut.addForce(force);
		}
	}

	public void boost(LevelElement obj) {
		if(!obj.getInContact()) {
			return;
		}
		switch(obj.getDirection()) {
			case UP:
				cephalonaut.addForce(new Vector2(0, BOOST_SPEED));
				break;
			case LEFT:
				cephalonaut.addForce(new Vector2(-BOOST_SPEED, 0));
				break;
			case DOWN:
				cephalonaut.addForce(new Vector2(0, -BOOST_SPEED));
				break;
			case RIGHT:
				cephalonaut.addForce(new Vector2(BOOST_SPEED, 0));
				break;
			default:
				break;
		}
	}

	public void openDoor(LevelElement element) {
		if(element.getOpened()) {
			element.setLinearVelocity(Vector2.Zero);
			return;
		}
		element.setLinearVelocity(new Vector2(0, 1));
		if(element.getBody().getPosition().y >= element.getOriginalPos().y + 1.4  * DOOR_SIZE) {
			element.setOpened(true);
		}
	}


	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when we first get a collision between two objects.  We use
	 * this method to test if it is the "right" kind of collision.
	 *
	 * @param contact The two bodies that collided
	 */
	public void beginContact(Contact contact) {
		Body body1 = contact.getFixtureA().getBody();
		Body body2 = contact.getFixtureB().getBody();

		SimpleObstacle bd1 = (SimpleObstacle)body1.getUserData();
		SimpleObstacle bd2 = (SimpleObstacle)body2.getUserData();

		try {
			if (bd1.getClass() == LevelElement.class && bd2.getName().equals("michael")) {
				((LevelElement) bd1).setInContact(true);
			}
			if (bd2.getClass() == LevelElement.class && bd1.getName().equals("michael")) {
				((LevelElement) bd2).setInContact(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void endContact(Contact contact) {
		Body body1 = contact.getFixtureA().getBody();
		Body body2 = contact.getFixtureB().getBody();

		SimpleObstacle bd1 = (SimpleObstacle)body1.getUserData();
		SimpleObstacle bd2 = (SimpleObstacle)body2.getUserData();

		try {
			if (bd1.getClass() == LevelElement.class && bd2.getName().equals("michael")) {
				((LevelElement) bd1).setInContact(false);
			}
			if (bd2.getClass() == LevelElement.class && bd1.getName().equals("michael")) {
				((LevelElement) bd2).setInContact(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) { }

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) { }

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