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
package edu.cornell.lilbiggames.cephalonaut.engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.CapsuleObstacle;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.OctopusObstacle;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.WheelObstacle;

/**
 * Player avatar for the gameplay prototype.
 */
public class CephalonautModel extends OctopusObstacle {
	/** Cache for internal force calculations */
	private final Vector2 forceCache = new Vector2();

	/** The cephalonaut's grapple tentacle */
	private GrappleModel grapple;

	/** The tentacle's texture */
	private Texture tentacleTexture;

	/** Cache object for transforming the force according the object angle */
	private final Affine2 affineCache = new Affine2();

	private final float MAX_SPEED = 8.0f;

	/** Magnitude of force to apply */
	private final float force = 4.0f;

	/** The direction of rotation */
	private float rotation;

	/** Whether or not the octopus is ink-thrusting */
	private boolean inking;

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
		this.forceCache.y = inking ? force : 0.0f;
	}

	/**
	 * Creates a new cephalonaut with the given physics data
	 *
	 * The size is expressed in physics units NOT pixels.  In order for
	 * drawing to work properly, you MUST set the drawScale. The drawScale
	 * converts the physics units to pixels.
	 *
	 */
	public CephalonautModel(float x, float y, float width, float height, Vector2 drawScale) {
		// The shrink factors fit the image to a tigher hitbox
		super(x, y, width, height);
		setName("michael");
		setDrawScale(drawScale);
		setDensity(1);
		setFriction(0);
		setRestitution(0.1f);
		setFixedRotation(false);

		origin.set(width / 2f, height / 2f);

		grapple = new GrappleModel(100000000, 100000000, drawScale);
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
		Vector2 position = getPosition();
		body.setAngularVelocity(10.0f*rotation);
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

		float speed = (float)Math.sqrt((body.getLinearVelocity().x*body.getLinearVelocity().x) + (body.getLinearVelocity().y*body.getLinearVelocity().y));
		if(speed < MAX_SPEED){
			// Orient the force with rotation and apply ink-thrust.
			System.out.println(getAngle());
			Vector2 temp = forceCache.cpy();
			affineCache.setToRotationRad(getAngle());
			affineCache.applyTo(forceCache);
			body.applyForce(forceCache,getPosition(),true);
			forceCache.set(temp);
		}
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
		// TODO: Stuff here probably
		
		super.update(dt);
	}

	/**
	 * Draws the physics object.
	 *
	 * @param canvas Drawing context
	 */
	public void draw(GameCanvas canvas) {
		if (grapple.isGrappling()) {
			float distance = getPosition().dst(grapple.getPosition());
			float angle = getPosition().cpy().sub(grapple.getPosition()).angleRad() + (float) Math.PI / 2f;
			Vector2 middle = getPosition().cpy().add(grapple.getPosition()).scl(0.5f);
			Color tint = grapple.isAnchored() ? Color.RED : Color.GREEN;
			int pixDiameter = (int) (getWidth() * Math.max(drawScale.x, drawScale.y));
			Pixmap pixmap = new Pixmap(pixDiameter, pixDiameter, Pixmap.Format.RGBA8888);
		    pixmap.setColor(Color.WHITE);
		    pixmap.fillCircle(pixDiameter / 2, pixDiameter / 2, pixDiameter / 2);
		    TextureRegion t = new TextureRegion(new Texture(pixmap));
			canvas.draw(t, tint, origin.x, origin.y, middle.x * drawScale.x, middle.y * drawScale.y,
					angle, 0.2f, distance);
		}

		canvas.draw(texture, Color.ORANGE, origin.x, origin.y,
				getX() * drawScale.x, getY() * drawScale.y,
				getAngle(), 1, 1);
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