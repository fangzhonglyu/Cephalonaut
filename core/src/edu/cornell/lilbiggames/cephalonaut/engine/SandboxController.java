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
package edu.cornell.lilbiggames.cephalonaut.engine;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.BoxObstacle;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.Obstacle;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.ObstacleSelector;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.PolygonObstacle;
import edu.cornell.lilbiggames.cephalonaut.util.RandomController;

/**
 * Gameplay specific controller for the gameplay prototype.
 */
public class SandboxController extends WorldController {
	/** Reference to the cephalonaut's model */
	private CephalonautModel cephalonaut;

	/** Mouse selector to move the cephalonaut */
	private ObstacleSelector selector;

	private GrappleModel grapple;

	private Texture earthTexture;

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

	private void addWall(float x, float y, float angle, String name) {
		final float boxSize = 1;
		BoxObstacle wall = new BoxObstacle(boxSize * x + boxSize / 2, boxSize * y + boxSize / 2, boxSize, boxSize);
		wall.setAngle(angle);
		wall.setGrapple(true);
		wall.setBodyType(BodyDef.BodyType.StaticBody);
		wall.setDensity(0);
		wall.setFriction(0);
		wall.setRestitution(1);
		wall.setDrawScale(scale);
		wall.setTexture(earthTile);
		wall.setTextureScaleX(boxSize * scale.x / earthTile.getRegionWidth());
		wall.setTextureScaleY(boxSize * scale.y / earthTile.getRegionHeight());
		wall.setName(name);
		addObject(wall);
	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel() {
		// Make the cephalonaut
		cephalonaut = new CephalonautModel(10, 10, scale);
		cephalonaut.setVX(5);

		addObject(cephalonaut);
		setDebug(true);

		selector = new ObstacleSelector(world);
		selector.setDrawScale(scale);
		world.setGravity(Vector2.Zero);

		final int boxesY = (int) bounds.getHeight() - 1;
		final int boxesX = (int) bounds.getWidth() - 1;
		// Left and right walls
		for (int i = 0; i <= boxesY; i++) {
			addWall(0, i, 0, "border_left" + i);
			addWall(boxesX, i, 0, "border_right" + i);
		}
		// Bottom and top walls
		for (int i = 0; i <= boxesX; i++) {
			addWall(i, 0, 0, "border_bottom" + i);
			addWall(i, boxesY, 0, "border_top" + i);
		}
		// U shape
		for (int i = 20; i < 25; i++) {
			addWall(i, 4, 0, "u_bottom" + i);
			addWall(i, 9, 0, "u_top" + i);
		}
		for (int i = 5; i < 9; i++) {
			addWall(24, i, 0, "u_right" + i);
		}

		// Random boxes
		addWall(15, 14, 0, "box1");
		addWall(16, 14, 0, "box2");

		// Thick box on bottom left
		addWall(8, 8, 0, "box_thicc1");
		addWall(7, 8, 0, "box_thicc2");
		addWall(8, 7, 0, "box_thicc3");
		addWall(7, 7, 0, "box_thicc4");

		// Diagonal wall on top right
		addWall(27, 14, (float) Math.toRadians(45), "box4");
		addWall(26.5f, 14.5f, (float) Math.toRadians(45), "box5");
		addWall(27.5f, 13.5f, (float) Math.toRadians(45), "box6");
		addWall(28f, 13f, (float) Math.toRadians(45), "box7");

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
		if(input.didTertiary()){
			grapple =  new GrappleModel(input.getCrossHair().x,input.getCrossHair().y,scale);
			addObject(grapple);
			DistanceJointDef j = new DistanceJointDef();
			j.bodyA = grapple.getBody();
			j.bodyB = cephalonaut.getBody();
			j.length = 2;
			Joint jj = world.createJoint(j);
		}
		// TODO
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