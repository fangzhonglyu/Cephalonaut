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

	private PlayMode playMode;
	private MainMenuMode menuMode;
	private MainMenuNestedMode mainMenuNestedMode;
	private PauseMode pauseMode;
	private LevelCompleteMode levelCompleteMode;
	private StartScreenMode startScreenMode;
	private LevelLoader levelLoader;

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

		canvas.resize();

		SoundController.gatherSoundAssets(directory);

		numCheckpointsCompleted = new ArrayList<>();
		for(int i = 0; i < 7; i++){
			numCheckpointsCompleted.add(0);
		}

		// Initialize the game world
		menuMode = new MainMenuMode(directory, canvas, this);
		mainMenuNestedMode = new MainMenuNestedMode(directory, canvas, 5,0, 0, this);
		startScreenMode = new StartScreenMode(directory, canvas, this);
		LevelElement.gatherAssets(directory);
//		playMode.gatherAssets(directory);
//		playMode.setCanvas(canvas);
//		playMode.reset(levelLoader.loadLevel("level_1"));
//		playMode.setLevel("Oliver_level2");
//		levelLoader.loadLevel("Oliver_level2", playMode);

		pauseMode = new PauseMode(directory, canvas, this);
		levelCompleteMode = new LevelCompleteMode(directory, canvas, this);

		SoundController.startMenuMusic();
		setScreen(startScreenMode);
	}

	public void selectLevel(){
		String levelName = menuMode.getCurLevel();
		int curLevel = menuMode.getCurLevelNumber();
		String checkpointName = "checkpoint_" + numCheckpointsCompleted.get(curLevel);
		playMode = new PlayMode(this, levelLoader, levelName, checkpointName);
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
		menuMode.dispose();

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

	@Override
	public void exitScreen(Screen screen, int exitCode) {
		SoundController.killAllSound();
		if(exitCode == StartScreenMode.START_CODE){
			setScreen(menuMode);
		} else if(exitCode == StartScreenMode.OPTIONS_CODE){
			System.out.println("options");
			setScreen(menuMode);
		} else if(exitCode == StartScreenMode.CREDITS_CODE){
			System.out.println("credits");
			setScreen(menuMode);
		} else if(exitCode == MainMenuMode.LEVEL_SELECTED_CODE){
			int curLevel = menuMode.getCurLevelNumber();
			mainMenuNestedMode.setLevel(curLevel);
			mainMenuNestedMode.setNumCheckpoints(5);
			mainMenuNestedMode.setNumCompletedCheckpoints(numCheckpointsCompleted.get(curLevel));
			setScreen(mainMenuNestedMode);
		} else if(exitCode == MainMenuNestedMode.LEVEL_SELECTED_CODE) {
			selectLevel();
		} else if (exitCode == PauseMode.EXIT_LEVEL_CODE || exitCode == LevelCompleteMode.EXit_LEVEL_CODE) {
			SoundController.startMenuMusic();
			SoundController.setPlaying(false);
			canvas.setCameraPos(canvas.getWidth()/2, canvas.getHeight()/2);
			int curLevel = menuMode.getCurLevelNumber();
			mainMenuNestedMode.setLevel(curLevel);
			setScreen(mainMenuNestedMode);
		} else if(exitCode == MainMenuNestedMode.NESTED_MENU_EXIT_CODE){
			setScreen(menuMode);
		} else if(exitCode == PlayMode.EXIT_LEVEL){
			canvas.setCameraPos(canvas.getWidth()/2, canvas.getHeight()/2);
			pauseMode.setDefault();
			setScreen(pauseMode);
		} else if(exitCode == PlayMode.WON_LEVEL){
			int curLevel = menuMode.getCurLevelNumber();
			numCheckpointsCompleted.set(curLevel, numCheckpointsCompleted.get(curLevel)+1);
			canvas.setCameraPos(canvas.getWidth()/2, canvas.getHeight()/2);
			mainMenuNestedMode.setLevel(curLevel);
			mainMenuNestedMode.setNumCheckpoints(5);
			mainMenuNestedMode.setNumCompletedCheckpoints(numCheckpointsCompleted.get(curLevel));
			setScreen(mainMenuNestedMode);
		} else if (exitCode == PauseMode.RESTART_LEVEL_CODE || exitCode == LevelCompleteMode.RESTART_LEVEL_CODE) {
			SoundController.setPlaying(true);
			playMode.reset();
			playMode.resume();
			setScreen(playMode);
		} else if (exitCode == LevelController.COMPLETE_LEVEL) {
			SoundController.setPlaying(false);
			playMode.setComplete(true);
			playMode.setFailure(false);
			setScreen(levelCompleteMode);
		} else if (exitCode == LevelCompleteMode.NEXT_LEVEL_CODE) {
			SoundController.setPlaying(true);
			menuMode.nextLevel();
			selectLevel();
		}
	}

}
