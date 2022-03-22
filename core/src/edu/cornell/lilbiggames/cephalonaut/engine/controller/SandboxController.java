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

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.GameObject;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.GameObjectJson;
import edu.cornell.lilbiggames.cephalonaut.engine.model.CephalonautModel;
import edu.cornell.lilbiggames.cephalonaut.engine.model.GrappleModel;
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

	/** The grapple mechanic mode */
	private boolean directionalGrapple;

	private TextureRegion octopusTexture;

	/** Texture asset for mouse crosshairs */
	private TextureRegion crosshairTexture;

	private CephalonautController cephalonautController;

	private static final float ATTRACT_DIST = 5f;
	private static final float METEOR_SPEED = 2f;
	private static final float BOOST_SPEED = 8f;
	private static final float DOOR_SIZE = 1f;

	/** Sound Controller */
	private SoundController soundController;

	/**
	 * Creates and initialize a new instance of the sandbox
	 */
	public SandboxController() {
		super(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_GRAVITY);
		setDebug(false);
		setComplete(false);
		setFailure(false);
		directionalGrapple = true;
	}

	public void reset() {
		reset(new Queue<GameObject>());
	}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset(Queue<GameObject> newObjects) {
		Vector2 gravity = new Vector2(world.getGravity());

		
		for(GameObject obj : objects) {
			obj.deactivatePhysics(world);
		}
		objects.clear();
		addQueue.clear();
		world.dispose();
		
		world = new World(gravity,false);
		world.setContactListener(this);
		setComplete(false);
		setFailure(false);
		world.setContactListener(this);
		populateLevel(newObjects);

		GrappleModel grapple = cephalonaut.getGrapple();
		grapple.reset();
		SoundController.switchTrack(1);
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
	 * Lays out the game geography.
	 */
	private void populateLevel(Queue<GameObject> newObjects) {
		for (GameObject object : newObjects) {
			object.setDrawScale(scale);
			addObject(object);
		}

		// Make the cephalonaut
		float dwidth  = octopusTexture.getRegionWidth()/scale.x;
		float dheight = octopusTexture.getRegionHeight()/scale.y;
		cephalonaut = new CephalonautModel(10, 10, dwidth, dheight, scale);
		cephalonaut.setTexture(octopusTexture);
		cephalonautController = new CephalonautController(world, cephalonaut);

		addObject(cephalonaut);
		GrappleModel grapple = cephalonaut.getGrapple();
		grapple.setMaxLength(7.0f);
		addObject(grapple);
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
		if (input.didTertiary()) {
			directionalGrapple = !directionalGrapple;
		}
		cephalonaut.setForce(Vector2.Zero);


		for(GameObject object : objects) {
			if (object.getClass() == LevelElement.class) {
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
						if (((LevelElement) object).getActivated()) {
							openDoor((LevelElement) object);
						}
						break;
					default:
						break;
				}
			}
		}

		boolean grappleButton = input.didSecondary();
		Vector2 crossHair = input.getCrossHair().add(
				(canvas.getCameraX() - canvas.getWidth() / 2f) / scale.x,
				(canvas.getCameraY() - canvas.getHeight() / 2f) / scale.y);
		boolean inking = input.isThrusterApplied();
		float rotation = input.getRotation();

		if (input.didTertiary()) {
			cephalonaut.setInk(1);
		}

		cephalonautController.update(grappleButton, directionalGrapple, objects, crossHair, inking, rotation);
		canvas.setCameraPos(cephalonaut.getX() * scale.x, cephalonaut.getY() * scale.y);
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

	public void finishLevel() {

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
			} else if (bd2.getClass() == LevelElement.class && bd1.getName().equals("michael")) {
				((LevelElement) bd2).setInContact(true);
			}

			GrappleModel grapple = cephalonaut.getGrapple();
			if (!grapple.isAnchored()) {
				if (bd1.getName().equals("grapple") && !bd2.getName().equals("michael")) {
					grapple.setAnchored(true);
					 grapple.setExtensionLength(1 + cephalonaut.getPosition().dst(bd2.getPosition()));
					grapple.setAnchorLocation(bd2.getName());
				}
				if (bd2.getName().equals("grapple") && !bd1.getName().equals("michael")) {
					grapple.setAnchored(true);
					 grapple.setExtensionLength(1 + cephalonaut.getPosition().dst(bd1.getPosition()));
					grapple.setAnchorLocation(bd1.getName());
				}
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
				if(bd1.getName().equals("finish")) {
					finishLevel();
				}
			}
			if (bd2.getClass() == LevelElement.class && bd1.getName().equals("michael")) {
				((LevelElement) bd2).setInContact(false);
				if(bd2.getName().equals("finish")) {
					finishLevel();
				}
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
		for(GameObject obj : objects) {
			obj.draw(canvas);
		}

		selector.draw(canvas);
		cephalonaut.draw(canvas);
		canvas.end();
		
		if (isDebug()) {
			canvas.beginDebug();
			for(GameObject obj : objects) {
				obj.drawDebug(canvas);
			}
			canvas.endDebug();
		}
	}

}