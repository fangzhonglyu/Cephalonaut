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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.BoxObstacle;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.Obstacle;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.ObstacleSelector;

/**
 * Gameplay specific controller for the gameplay prototype.
 */
public class SandboxController extends WorldController implements ContactListener {
	/** Reference to the cephalonaut's model */
	private CephalonautModel cephalonaut;

	/** Reference to the player's thruster controller */
	private ThrusterController thrusterController;

	/** Mouse selector to move the cephalonaut */
	private ObstacleSelector selector;

	private Texture earthTexture;

	/** The joint of the grapple */
	Joint joint;

	private boolean directionalGrapple;

	private TextureRegion octopusTexture;

	/** Texture asset for mouse crosshairs */
	private TextureRegion crosshairTexture;

	/**
	 * Creates and initialize a new instance of the sandbox
	 */
	public SandboxController() {
		super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY);
		setDebug(false);
		setComplete(false);
		setFailure(false);
		world.setContactListener(this);
		directionalGrapple = true;
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
		world.setContactListener(this);
		setComplete(false);
		setFailure(false);
		populateLevel();
		GrappleModel grapple = cephalonaut.getGrapple();
		grapple.reset();
	}

	private void addWall(float x, float y, float angle, String name) {
		final float boxSize = 1;
		BoxObstacle wall = new BoxObstacle(boxSize * x + boxSize / 2, boxSize * y + boxSize / 2, boxSize, boxSize);
		wall.setAngle(angle);
		wall.setGrapple(true);
		wall.setBodyType(BodyDef.BodyType.StaticBody);
		wall.setDensity(0);
		wall.setFriction(0);
		wall.setRestitution(0.3f);
		wall.setDrawScale(scale);
		wall.setTint(new Color(0.5f, 0.4f, 0.4f, 1));
		wall.setTexture(earthTile);
		wall.setTextureScaleX(boxSize * scale.x / earthTile.getRegionWidth());
		wall.setTextureScaleY(boxSize * scale.y / earthTile.getRegionHeight());
		wall.setName(name);
		addObject(wall);
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
	private void populateLevel() {
		// Make the cephalonaut
		float dwidth  = octopusTexture.getRegionWidth()/scale.x;
		float dheight = octopusTexture.getRegionHeight()/scale.y;
		cephalonaut = new CephalonautModel(10, 10, dwidth, dheight, scale);
		thrusterController = new ThrusterController(cephalonaut);
		cephalonaut.setTexture(octopusTexture);

		addObject(cephalonaut);
		GrappleModel grapple = cephalonaut.getGrapple();
		grapple.setMaxLength(7.0f);
		addObject(grapple);
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
		if (input.didTertiary()) {
			directionalGrapple = !directionalGrapple;
		}

		GrappleModel grapple = cephalonaut.getGrapple();
		if (input.didSecondary()) {
			grapple.setOut(!grapple.isOut());
			if (grapple.isOut()) {
				grapple.setPosition(cephalonaut.getPosition().cpy());
				// grapple travels 15 units/time in direction of mouse
				if (directionalGrapple) {
					grapple.setLinearVelocity(input.getCrossHair().cpy().sub(grapple.getPosition().cpy()).nor().scl(15));
				} else {
					grapple.closestAnchor(objects);
				}
				grapple.setActive(true);
			}
		}

		float distance = cephalonaut.getPosition().cpy().dst(grapple.getPosition());
		if (grapple.isAnchored()) {
			grapple.setBodyType(BodyDef.BodyType.StaticBody);
			if (distance > grapple.getExtensionLength() && !grapple.isGrappling()) {
				Vector2 swing = cephalonaut.getPosition().cpy().sub(grapple.getPosition()).rotate90(0);

				float dot = swing.dot(cephalonaut.getLinearVelocity());
				if (dot != 0) {
					// Experimental: Conserve velocity when rotating around point behind cephalonaut
					float newAngle = swing.angleRad() + (dot < 0 ? (float) Math.PI : 0);
					cephalonaut.setLinearVelocity(cephalonaut.getLinearVelocity().setAngleRad(newAngle));
				}

				DistanceJointDef anchor = new DistanceJointDef();
				anchor.bodyA = grapple.getBody();
				anchor.bodyB = cephalonaut.getBody();
				anchor.collideConnected = false;
				anchor.length = distance;
				joint = world.createJoint(anchor);
				grapple.setGrappling(true);
			}
		}
		grapple.setExtensionLength(distance);

		// "pull in" the grapple if requested, or if it has stretched its max length
		// and still hasn't anchored
		if ((input.didSecondary() && !grapple.isOut()) ||
				(grapple.isOut() && grapple.isFullyExtended() && !grapple.isAnchored())) {
			if (joint != null) {
				world.destroyJoint(joint);
				joint = null;
			}
			grapple.reset();
			grapple.setPosition(cephalonaut.getPosition().cpy());
		}

		if (input.isThrusterApplied()){
			thrusterController.startInking();
		} else {
			thrusterController.stopInking();
		}

		thrusterController.setRotation(input.getRotation());
		cephalonaut.applyRotation();
		cephalonaut.applyForce();
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

		Obstacle bd1 = (Obstacle)body1.getUserData();
		Obstacle bd2 = (Obstacle)body2.getUserData();

		try {
			GrappleModel grapple = cephalonaut.getGrapple();
			if (!grapple.isAnchored()) {
				if (bd1.getName().equals("grapple") && !bd2.getName().equals("michael")) {
					grapple.setAnchored(true);
					// grapple.setExtensionLength(cephalonaut.getPosition().dst(bd2.getPosition()));
					grapple.setAnchorLocation(bd2.getName());
				}
				if (bd2.getName().equals("grapple") && !bd1.getName().equals("michael")) {
					grapple.setAnchored(true);
					// grapple.setExtensionLength(cephalonaut.getPosition().dst(bd1.getPosition()));
					grapple.setAnchorLocation(bd1.getName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void endContact(Contact contact) { }

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
		cephalonaut.draw(canvas);
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