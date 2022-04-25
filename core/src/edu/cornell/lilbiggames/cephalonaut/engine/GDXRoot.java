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
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;
import edu.cornell.lilbiggames.cephalonaut.engine.controller.*;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;
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
public class GDXRoot extends Game implements ScreenListener {
	/** AssetManager to load game assets (textures, sounds, etc.) */
	AssetDirectory directory;

	/** Drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas;
	List<Integer> numCheckpointsCompleted;

	Map<String, Integer> keyBindings;

	private PlayMode playMode;
	private MainMenuMode mainMenu;
	private MainMenuNestedMode mainMenuNestedMode;
	private PauseMode pauseMode;
	private LevelCompleteMode levelCompleteMode;
	private StartScreenMode startScreenMode;
	private SettingsMode settings;
	private LevelLoader levelLoader;

	/**
	 * Creates a new game from the configuration settings.
	 *
	 * This method configures the asset manager, but does not load any assets
	 * or assign any screen.
	 */
	public GDXRoot() {

	}

	private void initializeCheckpointsMap(){
		numCheckpointsCompleted = new ArrayList<>();
		for(int i = 0; i < 7; i++){
			numCheckpointsCompleted.add(0);
		}
	}

	/**
	 * directory must not be null when this is called
	 */
	private void initializeKeybindings(){
		JsonValue bindings = directory.getEntry("keybindings", JsonValue.class);
		keyBindings = new HashMap<>();
		keyBindings.put("thrust", Input.Keys.valueOf(bindings.getString("thrust","W")));
		keyBindings.put("rotate-counterclockwise", Input.Keys.valueOf(bindings.getString("rotate-counterclockwise","A")));
		keyBindings.put("rotate-clockwise", Input.Keys.valueOf(bindings.getString("rotate-clockwise","D")));
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

		canvas.resize();

		SoundController.gatherSoundAssets(directory);

		initializeCheckpointsMap();
		initializeKeybindings();

		// Initialize the game world
		mainMenu = new MainMenuMode(directory, canvas, this);
		mainMenuNestedMode = new MainMenuNestedMode(directory, canvas, 5,0, 0, this);
		startScreenMode = new StartScreenMode(directory, canvas, this);
		LevelElement.gatherAssets(directory);

		pauseMode = new PauseMode(directory, canvas, this);
		settings = new SettingsMode(directory, canvas, this, keyBindings);
		levelCompleteMode = new LevelCompleteMode(directory, canvas, this);

		SoundController.setMusicVolume(0.5f);
		SoundController.startMenuMusic();
		setScreen(startScreenMode);
	}

	private void initializeCheckpointSelect(){
		int curLevel = mainMenu.getCurLevelNumber();
		mainMenuNestedMode.setLevel(curLevel);
		mainMenuNestedMode.setNumCheckpoints(5);
		mainMenuNestedMode.setNumCompletedCheckpoints(numCheckpointsCompleted.get(curLevel));
	}

	private void completeCheckpoint(){
		playMode.setComplete(true);
		playMode.setFailure(false);
		int curLevel = mainMenu.getCurLevelNumber();
		numCheckpointsCompleted.set(curLevel, numCheckpointsCompleted.get(curLevel)+1);
		mainMenuNestedMode.setLevel(curLevel);
		mainMenuNestedMode.setNumCheckpoints(5);
		mainMenuNestedMode.setNumCompletedCheckpoints(numCheckpointsCompleted.get(curLevel));
	}

	public void selectLevel(){
		String levelName = mainMenu.getCurLevel();
		int curLevel = mainMenu.getCurLevelNumber();
		String checkpointName = "checkpoint_" + numCheckpointsCompleted.get(curLevel);
		playMode = new PlayMode(this, levelLoader, levelName, checkpointName, keyBindings);
		playMode.gatherAssets(directory);
		playMode.setCanvas(canvas);
		playMode.reset();
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
		mainMenu.dispose();

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

//		int new_width = Math.max(1280, width);
//		int new_height = Math.max(720, height);
//		if(width > 1280 || height > 720) {
//		if(new_width/1280 > new_height / 720) {
//
//		}
//		if(width > 1280) {
//			super.resize(new_width, new_height);
//			canvas.resize();
//		}
		super.resize(width, height);
		canvas.resize();
//		canvas.resize();
//		if(new_height == height && new_width == width) {
//			canvas.resize();
//		}
//		System.out.println(new_width);
//		System.out.println(new_height);
//		}
//		super.resize(width, height);
//		canvas.resize();
	}

	@Override
	public void exitScreen(Screen screen, int exitCode) {
		SoundController.killAllSound();
		if(exitCode == MenuMode.START_CODE){
			setScreen(mainMenu);
		} else if(exitCode == MenuMode.OPTIONS_CODE){
			settings.setDefault();
			setScreen(settings);
		} else if(exitCode == MenuMode.CREDITS_CODE){
			System.out.println("credits");
			setScreen(mainMenu);
		} else if(exitCode == MenuMode.LEVEL_SELECTED_CODE){
			initializeCheckpointSelect();
			setScreen(mainMenuNestedMode);
		} else if(exitCode == MenuMode.CHECKPOINT_SELECTED_CODE) {
			selectLevel();
		} else if (exitCode == MenuMode.EXIT_LEVEL_CODE) {
			SoundController.startMenuMusic();
			playMode.pause();
			SoundController.setPlaying(false);
			canvas.setCameraPos(canvas.getWidth()/2, canvas.getHeight()/2);
			int curLevel = mainMenu.getCurLevelNumber();
			mainMenuNestedMode.setLevel(curLevel);
			setScreen(mainMenuNestedMode);
		} else if(exitCode == MenuMode.NESTED_MENU_EXIT_CODE){
			setScreen(mainMenu);
		} else if(exitCode == PlayMode.EXIT_LEVEL){
			canvas.setCameraPos(canvas.getWidth()/2, canvas.getHeight()/2);
			pauseMode.setDefault();
			setScreen(pauseMode);
		} else if (exitCode == MenuMode.RESTART_LEVEL_CODE) {
			playMode.reset();
			playMode.resume();
			setScreen(playMode);
		} else if (exitCode == MenuMode.RESUME_LEVEL_CODE) {
			playMode.resume();
			setScreen(playMode);
		} else if (exitCode == LevelController.COMPLETE_LEVEL) {
			completeCheckpoint();
			playMode.pause();
			SoundController.killAllSound();
			canvas.setCameraPos(canvas.getWidth()/2, canvas.getHeight()/2);
			setScreen(levelCompleteMode);
		} else if (exitCode == MenuMode.NEXT_LEVEL_CODE) {
			selectLevel();
		} else if (exitCode == MenuMode.RETURN_TO_START_CODE){
			Gdx.input.setInputProcessor(new InputAdapter());
			setScreen(startScreenMode);
		}
	}

}
