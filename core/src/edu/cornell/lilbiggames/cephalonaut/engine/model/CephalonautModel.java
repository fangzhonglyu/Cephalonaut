/*
 * DudeModel.java
 *
 * You SHOULD NOT need to modify this file.  However, you may learn valuable lessons
 * for the rest of the lab by looking at it.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package edu.cornell.lilbiggames.cephalonaut.engine.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.lilbiggames.cephalonaut.engine.GameCanvas;
import edu.cornell.lilbiggames.cephalonaut.engine.controller.SoundController;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.OctopusObstacle;
import edu.cornell.lilbiggames.cephalonaut.util.FilmStrip;

/**
 * Player avatar for the gameplay prototype.
 */
public class CephalonautModel extends OctopusObstacle {
	/** Cache for internal force calculations */
	private final Vector2 forceCache = new Vector2();
	private final Vector2 miscForceCache = new Vector2();
	private boolean shouldTeleport = false;
	private Vector2 teleportLocation;

	/** The cephalonaut's grapple tentacle */
	private GrappleModel grapple;

	/** The tentacle's texture */
	private Texture tentacleTexture;

	/** The Filmstrip for the cephalonaut*/
	private FilmStrip filmstrip;

	/** Animation Counter*/
	private float frame;

	/** Cache object for transforming the force according the object angle */
	private final Affine2 affineCache = new Affine2();

	private final float MAX_SPEED = 8.0f;

	/** Magnitude of force to apply */
	private final float force = 8.0f;

	/** The direction of rotation */
	private float rotation;

	/** Whether or not the octopus is ink-thrusting */
	private boolean inking;

	/** How much ink the cephalonaut has left */
	private float ink;

	private boolean alive = true;

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	/**
	 * Returns true if the cephalonaut is actively inking.
	 *
	 * @return true if the cephalonaut is actively inking.
	 */
	public boolean isInking() {
		return inking;
	}

	/**
	 * Sets whether the cephalonaut is actively inking.
	 *
	 * @param inking whether the cephalonaut is actively inking.
	 */
	public void setInking(boolean inking) {
		this.inking = inking;
		this.forceCache.y = (inking && ink > 0) ? force : 0.0f;
		SoundController.setInkSound(inking && ink > 0);
	}

	public boolean getShouldTeleport() {
		return shouldTeleport;
	}

	public void setShouldTeleport(boolean shouldT) {
		shouldTeleport = shouldT;
	}

	public Vector2 getTeleportLocation() {
		return teleportLocation;
	}

	public void setTeleportLocation(Vector2 teleportLoc) {
		teleportLocation = teleportLoc;
	}


	/**
	 * Sets the amount of ink in the cephalonaut's sac.
	 *
	 * @param ink the amount of ink in the cephalonaut's sac.
	 */
	public void setInk(int ink) { this.ink = Math.min(1, ink); }

	/**
	 * Returns the amount of ink in the cephalonaut's sac.
	 *
	 * @returns the amount of ink in the cephalonaut's sac.
	 */
	public float getInk() { return ink; }

	/**
	 * Sets whether the cephalonaut is actively inking.
	 *
	 * @param new_force whether the cephalonaut is actively inking.
	 */
	public void setForce(Vector2 new_force) {
		miscForceCache.x = new_force.x;
		miscForceCache.y = new_force.y;
	}
	/**
	 * Sets whether the cephalonaut is actively inking.
	 *
	 * @param new_force whether the cephalonaut is actively inking.
	 */
	public void addForce(Vector2 new_force) {
		miscForceCache.x += new_force.x;
		miscForceCache.y += new_force.y;
	}

	/**
	 * Creates a new cephalonaut with the given physics data
	 *
	 * The size is expressed in physics units NOT pixels.  In order for
	 * drawing to work properly, you MUST set the drawScale. The drawScale
	 * converts the physics units to pixels.
	 *
	 */
	public CephalonautModel(float x, float y, float width, float height, Vector2 drawScale,FilmStrip filmstrip) {
		// The shrink factors fit the image to a tighter hitbox
		super(x, y, width, height);
		setName("michael");
		setDrawScale(drawScale);
		setDensity(1);
		setFriction(0);
		setRestitution(0.1f);
		setFixedRotation(false);
		this.filmstrip = filmstrip;
		this.filmstrip.setFrame(0);
		frame = 0;
		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.fillRectangle(0, 0, 1, 1);
		tentacleTexture = new Texture(pixmap);

		// Matias: I don't think this line of code matters bc it's being overwritten by the setTexture call
		// in the SandboxController.
		origin.set(width / 2f, height / 2f);
		ink = 1f;
		grapple = new GrappleModel(x, y, drawScale);
	}

	/**
	 * Returns the cephalonaut's grapple tentacle.
	 *
	 * @return the cephalonaut's grapple tentacle.
	 */
	public GrappleModel getGrapple() {
		return grapple;
	}

	/**
	 * Creates the physics Body(s) for this object, adding them to the world.
	 *
	 * This method overrides the base method to keep your ship from spinning.
	 *
	 * @param world Box2D world to store body
	 *
	 * @return true if object allocation succeeded
	 */
	public boolean activatePhysics(World world) {
		// create the box from our superclass
		if (!super.activatePhysics(world)) {
			return false;
		}

		// TODO: Stuff here probably

		return true;
	}

	/**
	 * Applies rotation to the octopus
	 *
	 * This method should be called after the rotation attribute is set.
	 */
	public void applyRotation(){
		body.setAngularVelocity(-5f * rotation);
	}
	

	/**
	 * Applies the force to the body of this dude
	 *
	 * This method should be called after the force attribute is set.
	 */
	public void applyForce() {
		if (!isActive()) {
			return;
		}

		// Orient the force with rotation and apply ink-thrust.
		Vector2 temp = forceCache.cpy();
		affineCache.setToRotationRad(getAngle());
		affineCache.applyTo(forceCache);
		forceCache.add(miscForceCache);
		body.applyForce(forceCache,getPosition(),true);
		forceCache.set(temp);
		setLinearVelocity(getLinearVelocity().clamp(0, MAX_SPEED));
	}

	/**
	 * Sets rotational force
	 *
	 * @param rotation The direction (clockwise or counterclockwise to rotate)
	 */
	public void setRotationalDirection(float rotation){
		this.rotation = rotation;
	}
	
	/**
	 * Updates the object's physics state (NOT GAME LOGIC).
	 *
	 * We use this method to reset cooldowns.
	 *
	 * @param dt	Number of seconds since last animation frame
	 */
	public void update(float dt) {
		super.update(dt);
		if(frame>8&&frame<18){
			frame += dt*10f;
			if(frame>=18)
				frame = 0;
		}
		else if(frame>=18&&frame<25){
			if(frame<23||(inking && ink > 0.0f))
				frame += dt*20f;
			else if (rotation>-0.8f){
				frame += dt*15f;
			}
			else if (frame>24 && rotation<-0.8f){
				frame -= dt*15f;
			}
			if(frame>=25)
				frame = 0;
		}
		else if(frame>=27&&frame<34){
			if(frame<32||(inking && ink > 0.0f))
				frame += dt*20f;
			else if (rotation<0.8f){
				frame += dt*15f;
			}
			else if (frame>33 && rotation>0.8f){
				frame -= dt*15f;
			}
			if(frame>=34)
				frame = 0;
		}
		else if(frame>=36){
			frame += dt*10f;
			if(frame>=filmstrip.getSize())
				frame = 0;
		}
		if(frame<5){
			frame += dt*5f;
			if(frame>=5)
				frame = 0;
			if(inking && ink > 0.0f)
				frame = 36;
			else if(rotation>0.8f)
				frame = 27;
			else if (rotation<-0.8f)
				frame = 18;
		}
		filmstrip.setFrame((int)frame);

		if (inking && ink > 0.0f) {
			ink -= 0.006f;
		} else if (!inking && ink < 1.0f) {
			ink += 0.004f;
		}
		ink = Math.min(ink, 1.0f);
	}

	/**
	 * Draws the physics object.
	 *
	 * @param canvas Drawing context
	 */
	public void draw(GameCanvas canvas) {
		grapple.draw(canvas, getPosition(), (float) Math.toDegrees(getAngle()));

		//animation

		float ox = 0.5f * filmstrip.getRegionWidth();
		float oy = 0.75f * filmstrip.getRegionHeight();
		canvas.draw(filmstrip, Color.WHITE, ox, oy,
				getX() * drawScale.x, getY() * drawScale.y,
				getAngle(), 0.052f* drawScale.x, 0.052f*drawScale.y);

		//fuel bar
		canvas.drawSimpleFuelBar(ink);
	}
	
	/**
	 * Draws the outline of the physics body.
	 *
	 * This method can be helpful for understanding issues with collisions.
	 *
	 * @param canvas Drawing context
	 */
	public void drawDebug(GameCanvas canvas) {
		super.drawDebug(canvas);
//		canvas.drawPhysics(circleShape, Color.RED, getX(), getY(), drawScale.x, drawScale.y);
//		canvas.drawPhysics(triangleShape, Color.RED, getX(), getY(), getAngle(), drawScale.x, drawScale.y);
	}
}