/*
 * GDXRoot.java
 *
 * This is the primary class file for running the game.  It is the "static main" of
 * LibGDX.  In the first lab, we extended ApplicationAdapter.  In previous lab
 * we extended Game.  This is because of a weird graphical artifact that we do not
 * understand.  Transparencies (in 3D only) is failing when we use ApplicationAdapter. 
 * There must be some undocumented OpenGL code in setScreen.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
 package edu.cornell.lilbiggames.cephalonaut.engine;

import com.badlogic.gdx.*;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.controller.PlayMode;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;
import java.util.Map;
import edu.cornell.lilbiggames.cephalonaut.engine.controller.SoundController;
import edu.cornell.lilbiggames.cephalonaut.engine.parsing.LevelLoader;

/**
 * Root class for a LibGDX.  
 * 
 * This class is technically not the ROOT CLASS. Each platform has another class above
 * this (e.g. PC games use DesktopLauncher) which serves as the true root.  However, 
 * those classes are unique to each platform, while this class is the same across all 
 * plaforms. In addition, this functions as the root class all intents and purposes, 
 * and you would draw it as a root class in an architecture specification.  
 */
public class GDXRoot extends Game {
	/** AssetManager to load game assets (textures, sounds, etc.) */
	AssetDirectory directory;

	/** Drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas;

	private PlayMode playMode;
	private LevelLoader levelLoader;
	private final String[] levelNames = {"level_0", "level_1"};
	private Map<String, PlayMode> levels;

	/**
	 * Creates a new game from the configuration settings.
	 *
	 * This method configures the asset manager, but does not load any assets
	 * or assign any screen.
	 */
	public GDXRoot() {

	}

	/** 
	 * Called when the Application is first created.
	 * 
	 * This is method immediately loads assets for the loading screen, and prepares
	 * the asynchronous loader for all other assets.
	 */
	public void create() {
		canvas  = new GameCanvas();
		levelLoader = new LevelLoader();
		directory = levelLoader.getAssetDirectory();

		// TODO: This should be cleaner when we have a main menu
		// Load in levels
//		try{
//			levels = levelLoader.loadLevels(levelNames);
//		} catch(Exception e){
//			System.out.println(e);
//		}

		SoundController.gatherSoundAssets(directory);


		// Initialize the game world
		playMode = new PlayMode();
//		playMode = levelLoader.loadLevel("level_1");
		LevelElement.gatherAssets(directory);
		playMode.gatherAssets(directory);
		playMode.setCanvas(canvas);
//		playMode.reset(levelLoader.loadLevel("level_1"));
		playMode.setLevel("Oliver_levelt1");
		levelLoader.loadLevel("Oliver_levelt1", playMode);



		setScreen(playMode);

	}

	/** 
	 * Called when the Application is destroyed. 
	 *
	 * This is preceded by a call to pause().
	 */
	public void dispose() {
		// Call dispose on our children
		setScreen(null);
		playMode.dispose();

		canvas.dispose();
		canvas = null;
	
		// Unload all of the resources
		if (directory != null) {
			directory.unloadAssets();
			directory.dispose();
			directory = null;
		}
		super.dispose();
	}
	
	/**
	 * Called when the Application is resized. 
	 *
	 * This can happen at any point during a non-paused state but will never happen 
	 * before a call to create().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		canvas.resize();
		super.resize(width,height);
	}
}
