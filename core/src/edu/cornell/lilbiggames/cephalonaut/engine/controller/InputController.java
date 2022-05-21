/*
 * InputController.java
 *
 * This class buffers in input from the devices and converts it into its
 * semantic meaning. If your game had an option that allows the player to
 * remap the control keys, you would store this information in this class.
 * That way, the main GameEngine does not have to keep track of the current
 * key mapping.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;

import com.badlogic.gdx.utils.Array;
import edu.cornell.lilbiggames.cephalonaut.util.Controllers;
import edu.cornell.lilbiggames.cephalonaut.util.XBoxController;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for reading player input. 
 *
 * This supports both a keyboard and X-Box controller. In previous solutions, we only 
 * detected the X-Box controller on start-up.  This class allows us to hot-swap in
 * a controller via the new XBox360Controller class.
 */
public class InputController {
	// Sensitivity for moving crosshair with gameplay
	private static final float GP_ACCELERATE = 1.0f;
	private static final float GP_MAX_SPEED  = 10.0f;
	private static final float GP_THRESHOLD  = 0.01f;

	/** The singleton instance of the input controller */
	private static InputController theController = null;
	
	// Fields to manage buttons
	/** Whether the reset button was pressed. */
	private boolean resetPressed;
	private boolean resetPrevious;
	/** Whether the primary action button was pressed. */
	private boolean primePressed;
	private boolean primePrevious;
	/** Whether the secondary action button was pressed. */
	private boolean secondaryPressed;
	private boolean secondaryPrevious;
	/** Whether the teritiary action button was pressed. */
	private boolean tertiaryPressed;
	private boolean tertiaryPrevious;

	/** Whether the debug toggle was pressed. */
	private boolean debugPressed;
	private boolean debugPrevious;
	/** Whether the exit button was pressed. */
	private boolean exitPressed;
	private boolean exitPrevious;

	private Vector2 stickDirec;

	/** The crosshair position (for raddoll) */
	private Vector2 crosshair;
	/** The crosshair cache (for using as a return value) */
	private Vector2 crosscache;

	/** Forward thrust applied */
	private boolean thrusterApplied;
	/** Rotation applied (-1 for counterclockwise, 0 for no rotation, 1 for clockwise */
	private float rotation;
	private Map<String, Integer> keyBindings;
	
	/** An X-Box controller (if it is connected) */
	XBoxController xbox;

	private void setDefaultBindings(){
		keyBindings.put("ink",Input.Keys.valueOf("W"));
		keyBindings.put("rotate-counterclockwise",Input.Keys.valueOf("A"));
		keyBindings.put("rotate-clockwise",Input.Keys.valueOf("D"));
		keyBindings.put("ungrapple",Input.Keys.valueOf("Space"));
	}

	/**
	 * Return the singleton instance of the input controller
	 *
	 * @return the singleton instance of the input controller
	 */
	public static InputController getInstance() {
		if (theController == null) {
			theController = new InputController();
			theController.setDefaultBindings();
		}
		return theController;
	}

	public void setBindings(Map<String,Integer> keyBindings){
		this.keyBindings = keyBindings;
	}
	
	/**
	 * Returns the current position of the crosshairs on the screen.
	 *
	 * This value does not return the actual reference to the crosshairs position.
	 * That way this method can be called multiple times without any fair that 
	 * the position has been corrupted.  However, it does return the same object
	 * each time.  So if you modify the object, the object will be reset in a
	 * subsequent call to this getter.
	 *
	 * @return the current position of the crosshairs on the screen. This should probably be used to help grapple.
	 */
	public Vector2 getCrossHair() {
		return crosscache.set(crosshair);
	}

	/**
	 * Returns true if the primary action button was pressed.
	 *
	 * This is a one-press button. It only returns true at the moment it was
	 * pressed, and returns false at any frame afterwards.
	 *
	 * @return true if the primary action button was pressed. This should be used for thrusting.
	 */
	public boolean didPrimary() {
		return primePressed && !primePrevious;
	}

	/**
	 * Returns true if the secondary action button was pressed.
	 *
	 * This is a one-press button. It only returns true at the moment it was
	 * pressed, and returns false at any frame afterwards.
	 *
	 * @return true if the secondary action button was pressed. This should be used for grappling.
	 */
	public boolean didSecondary() {
		return secondaryPressed && !secondaryPrevious;
	}

	/**
	 * Returns true if the tertiary action button was pressed.
	 *
	 * This is a one-press button. It only returns true at the moment it was
	 * pressed, and returns false at any frame afterwards.
	 *
	 * @return true if the tertiary action button was pressed. This should be used for switch grappling mode.
	 */
	public boolean didTertiary() {
		if(xbox != null && xbox.isConnected()) {
			return tertiaryPressed && !tertiaryPrevious;
		} else {
			return tertiaryPressed && !secondaryPrevious;
		}
	}

	/**
	 * Returns true if the reset button was pressed.
	 *
	 * @return true if the reset button was pressed.
	 */
	public boolean didReset() {
		return resetPressed && !resetPrevious;
	}
	
	/**
	 * Returns true if the player wants to go toggle the debug mode.
	 *
	 * @return true if the player wants to go toggle the debug mode.
	 */
	public boolean didDebug() {
		return debugPressed && !debugPrevious;
	}
	
	/**
	 * Returns true if the exit button was pressed.
	 *
	 * @return true if the exit button was pressed.
	 */
	public boolean didExit() {
		return exitPressed && !exitPrevious;
	}

	/**
	 * Returns true if the player is currently using thruster ink sacs
	 *
	 * @return true if the add thrust button was pressed
	 */
	public boolean isThrusterApplied(){
		return thrusterApplied;
	}




	public Vector2 getStickDirec(){
		return stickDirec;
	}

	/**
	 * Gets the rotation of the octopus
	 * Returns 1.0 for clockwise rotation, 0 for no rotation, and -1.0 for counterclockwise rotation
	 *
	 * @returns a float representing rotation direction
	 */
	public float getRotation(){
		return rotation;
	}

	/**
	 * Creates a new input controller
	 * 
	 * The input controller attempts to connect to the X-Box controller at device 0,
	 * if it exists.  Otherwise, it falls back to the keyboard control.
	 */
	public InputController() {
		// If we have a game-pad for id, then use it.
		Array<XBoxController> controllers = Controllers.get().getXBoxControllers();
		if (controllers.size > 0) {
			xbox = controllers.get( 0 );
		} else {
			xbox = null;
		}

		stickDirec = new Vector2();
		keyBindings = new HashMap<>();
		crosshair = new Vector2();
		crosscache = new Vector2();
	}

	/**
	 * Reads the input for the player and converts the result into game logic.
	 *
	 * The method provides both the input bounds and the drawing scale.  It needs
	 * the drawing scale to convert screen coordinates to world coordinates.  The
	 * bounds are for the crosshair.  They cannot go outside of this zone.
	 *
	 * @param bounds The input bounds for the crosshair.  
	 * @param scale  The drawing scale
	 */
	public void readInput(Rectangle bounds, Vector2 scale) {
		// Copy state from last animation frame
		// Helps us ignore buttons that are held down
		primePrevious = primePressed;
		secondaryPrevious = secondaryPressed;
		tertiaryPrevious = tertiaryPressed;
		resetPrevious  = resetPressed;
		debugPrevious  = debugPressed;
		exitPrevious = exitPressed;

		// Check to see if a GamePad is connected
		if (xbox != null && xbox.isConnected()) {
			readGamepad(bounds, scale);
			readKeyboard(bounds, scale, true); // Read as a back-up
		} else {
			readKeyboard(bounds, scale, false);
		}
	}

	/**
	 * Reads input from an X-Box controller connected to this computer.
	 *
	 * The method provides both the input bounds and the drawing scale.  It needs
	 * the drawing scale to convert screen coordinates to world coordinates.  The
	 * bounds are for the crosshair.  They cannot go outside of this zone.
	 *
	 * @param bounds The input bounds for the crosshair.  
	 * @param scale  The drawing scale
	 */
	private void readGamepad(Rectangle bounds, Vector2 scale) {
		resetPressed = xbox.getX();
		exitPressed  = xbox.getBack();
		debugPressed  = xbox.getY();
		primePressed = xbox.getRightTrigger() > 0.6f;
		tertiaryPressed = xbox.getLeftTrigger() > 0.6f;
//		tertiaryPressed = xbox.getLBumper();

		rotation = 0.0f;
		if (xbox.getLeftX() > 0.6f) {
			rotation += 1.0f;
		}
		if (xbox.getLeftX() < -0.6f){
			rotation -= 1.0f;
		}

		thrusterApplied = xbox.getRightTrigger() > 0.6f;

		if(stickDirec.x > .6f || stickDirec.x < -.6f || stickDirec.y > .6f || stickDirec.y < -.6f) {
			secondaryPressed = true;
		} else {
			secondaryPressed = false;
		}

		stickDirec.x = xbox.getRightX();
		stickDirec.y = -xbox.getRightY();

	}

	/**
	 * Reads input from the keyboard.
	 *
	 * This controller reads from the keyboard regardless of whether or not an X-Box
	 * controller is connected.  However, if a controller is connected, this method
	 * gives priority to the X-Box controller.
	 *
	 * @param secondary true if the keyboard should give priority to a gamepad
	 */
	private void readKeyboard(Rectangle bounds, Vector2 scale, boolean secondary) {
		// Give priority to gamepad results
		resetPressed = (secondary && resetPressed) || (Gdx.input.isKeyPressed(Input.Keys.R));
		debugPressed = (secondary && debugPressed) ||
				((Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT)) &&
						Gdx.input.isKeyPressed(Input.Keys.F12));
		primePressed = (secondary && primePressed) || (Gdx.input.isKeyPressed(Input.Keys.W));
		secondaryPressed = (secondary && secondaryPressed) || (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT));
		exitPressed  = (secondary && exitPressed) || (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE));
		tertiaryPressed = (secondary && tertiaryPressed) || (Gdx.input.isKeyJustPressed(keyBindings.get("ungrapple"))) ||
				(Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT));

		// Directional controls
		rotation = (secondary ? rotation : 0.0f);
		if (Gdx.input.isKeyPressed(keyBindings.get("rotate-clockwise"))) {
			rotation += 1.0f;
		}
		if (Gdx.input.isKeyPressed((keyBindings.get("rotate-counterclockwise")))){
			rotation -= 1.0f;
		}

		thrusterApplied = (secondary ? thrusterApplied : false);
		thrusterApplied = thrusterApplied || Gdx.input.isKeyPressed((keyBindings.get("ink")));

		// Mouse results
		crosshair.set(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
		crosshair.scl(1/scale.x,1/scale.y);
		// TODO: Clamp correctly
//		clampPosition(bounds);
	}
	
	/**
	 * Clamp the cursor position so that it does not go outside the window
	 *
	 * While this is not usually a problem with mouse control, this is critical 
	 * for the gamepad controls.
	 */
	private void clampPosition(Rectangle bounds) {
		crosshair.x = Math.max(bounds.x, Math.min(bounds.x+bounds.width, crosshair.x));
		crosshair.y = Math.max(bounds.y, Math.min(bounds.y+bounds.height, crosshair.y));
	}

}