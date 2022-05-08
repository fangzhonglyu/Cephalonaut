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
	private LoadingScreen loadingScreen;
	private SettingsMode settings;
	private CreditsScreen credits;
	private LevelLoader levelLoader;
	private DialogueMode dialogueMode;

	private float alpha;
	private boolean fadeDirection;
	private boolean transitioning;
	private Screen nextScreen;
	private Screen postLoadingScreen;

	private boolean fakeLoadingAssets;
	private Map<Integer,Integer> numCheckpointsPerLevel;

	/**
	 * Creates a new game from the configuration settings.
	 *
	 * This method configures the asset manager, but does not load any assets
	 * or assign any screen.
	 */
	public GDXRoot() {

	}

	private void initializeCheckpointsMap(){
		numCheckpointsPerLevel = new HashMap<>();
		JsonValue bindings = directory.getEntry("worldCheckpoints", JsonValue.class);

		for(int i = 0; i < 7; i++){
			int checkpoints = bindings.getInt(String.valueOf(i));
			numCheckpointsPerLevel.put(i,checkpoints);
		}

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


	private void initializeDialogue() {
		dialogueMode = new DialogueMode(canvas, directory);
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

		fakeLoadingAssets = true;

		canvas.resize();
		loadingScreen = new LoadingScreen(directory, canvas, this,200f);
		postLoadingScreen = startScreenMode;
		setScreen(loadingScreen);

		SoundController.gatherSoundAssets(directory);

		initializeCheckpointsMap();
		initializeKeybindings();
		initializeDialogue();

		transitioning = false;

		// Initialize the game world
		mainMenu = new MainMenuMode(directory, canvas, this);
		mainMenuNestedMode = new MainMenuNestedMode(directory, canvas, numCheckpointsPerLevel.get(0),0, 0, this);
		startScreenMode = new StartScreenMode(directory, canvas, this);

		LevelElement.gatherAssets(directory);

		pauseMode = new PauseMode(directory, canvas, this);
		settings = new SettingsMode(directory, canvas, this, keyBindings);
		credits = new CreditsScreen(directory, canvas, this);
		levelCompleteMode = new LevelCompleteMode(directory, canvas, this);

		SoundController.setMusicVolume(0.5f);
		SoundController.startMenuMusic();
		PlayMode.resetMusic();
		postLoadingScreen = startScreenMode;
	}

	private void initializeCheckpointSelect(){
		int curLevel = mainMenu.getCurLevelNumber();
		mainMenuNestedMode.setLevel(curLevel);
		mainMenuNestedMode.setNumCheckpoints(numCheckpointsPerLevel.get(curLevel));
		mainMenuNestedMode.setNumCompletedCheckpoints(numCheckpointsCompleted.get(curLevel));
	}

	private void completeCheckpoint(){
		playMode.setComplete(true);
		playMode.setFailure(false);
		int curLevel = mainMenu.getCurLevelNumber();
		numCheckpointsCompleted.set(curLevel, numCheckpointsCompleted.get(curLevel)+1);
		mainMenuNestedMode.setLevel(curLevel);
		mainMenuNestedMode.setNumCheckpoints(numCheckpointsPerLevel.get(curLevel));
		mainMenuNestedMode.setNumCompletedCheckpoints(numCheckpointsCompleted.get(curLevel));
	}

	public void selectLevel(){
		String levelName = mainMenu.getCurLevel();
		int curLevel = mainMenu.getCurLevelNumber();
		String checkpointName = "checkpoint_" + mainMenuNestedMode.getNumCompletedCheckpoints();
		playMode = new PlayMode(this, levelLoader, levelName, checkpointName, keyBindings, dialogueMode);
		playMode.gatherAssets(directory);
		playMode.setCanvas(canvas);
		playMode.reset();
		startScreenTransition(playMode);
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
		Gdx.input.setInputProcessor(new InputAdapter());
		//
		SoundController.setBlackHoleSound(false,1);
		SoundController.setInkSound(false);
		if(exitCode == MenuMode.START_CODE){
			startScreenTransition(mainMenu);
		} else if(exitCode == MenuMode.OPTIONS_CODE){
			settings.setDefault();
			startScreenTransition(settings);
		} else if(exitCode == MenuMode.CREDITS_CODE){
			startScreenTransition(credits);
		} else if(exitCode == MenuMode.LEVEL_SELECTED_CODE){
			initializeCheckpointSelect();
			startScreenTransition(mainMenuNestedMode);
		} else if(exitCode == MenuMode.CHECKPOINT_SELECTED_CODE) {
			selectLevel();
		} else if (exitCode == MenuMode.EXIT_LEVEL_CODE) {
			SoundController.startMenuMusic();
			PlayMode.resetMusic();
			playMode.pause();
			SoundController.setPlaying(false);
			canvas.setCameraPos(canvas.getWidth()/2, canvas.getHeight()/2);
			int curLevel = mainMenu.getCurLevelNumber();
			mainMenuNestedMode.setLevel(curLevel);
			startScreenTransition(mainMenuNestedMode);
		} else if(exitCode == MenuMode.NESTED_MENU_EXIT_CODE){
			startScreenTransition(mainMenu);
		} else if(exitCode == PlayMode.EXIT_LEVEL){
			canvas.setCameraPos(canvas.getWidth()/2, canvas.getHeight()/2);
			pauseMode.setDefault();
			startScreenTransition(pauseMode);
		} else if (exitCode == MenuMode.RESTART_LEVEL_CODE) {
			playMode.reset();
			playMode.resume();
			startScreenTransition(playMode);
		} else if (exitCode == MenuMode.RESUME_LEVEL_CODE) {
			playMode.resume();
			startScreenTransition(playMode);
		} else if (exitCode == LevelController.COMPLETE_LEVEL) {
			playMode.pause();
			SoundController.killAllSound();
			canvas.setCameraPos(canvas.getWidth()/2, canvas.getHeight()/2);
			levelCompleteMode.resetFrame();
			levelCompleteMode.resetChoiceMade();
			levelCompleteMode.setSelectedOption(0);
			levelCompleteMode.setTimer(playMode.getTimer());
			levelCompleteMode.setTimeString(playMode.getTimeString());
			levelCompleteMode.setStars(playMode.getTwoStars(), playMode.getThreeStars());
			loadingScreenTransition(levelCompleteMode);
		} else if (exitCode == MenuMode.NEXT_LEVEL_CODE) {
			if(mainMenuNestedMode.getNumCompletedCheckpoints() == numCheckpointsPerLevel.get(mainMenu.getCurLevelNumber())-1){
				mainMenu.nextLevel();
				mainMenuNestedMode.setNumCompletedCheckpoints(0);
				mainMenuNestedMode.setNumCheckpoints(numCheckpointsPerLevel.get(mainMenu.getCurLevelNumber()));
			} else {
				mainMenuNestedMode.setNumCompletedCheckpoints(mainMenuNestedMode.getNumCompletedCheckpoints()
				+1);
			}
			selectLevel();
		} else if (exitCode == MenuMode.RETURN_TO_START_CODE){
			startScreenMode.setDefault();
			startScreenTransition(startScreenMode);
		} else if(exitCode == MenuMode.EXIT_LOADING_CODE){
			startScreenTransition(postLoadingScreen);
		}
	}

	private void loadingScreenTransition(Screen nextScreen){
		postLoadingScreen = nextScreen;
		loadingScreen.setLoadingTime(100);
		startScreenTransition(loadingScreen);
	}

	private void startScreenTransition(Screen nextScreen){
		transitioning = true;
		this.nextScreen = nextScreen;
		fadeDirection = true;
	}

	private void performScreenTransition(Screen nextScreen){
		if (alpha >= 1) {
			if(nextScreen == startScreenMode || nextScreen == settings || nextScreen == levelCompleteMode || nextScreen == mainMenu || nextScreen == mainMenuNestedMode){
				((MenuMode)nextScreen).setDefault();
			}
			setScreen(nextScreen);
			fadeDirection = false;
		}
		else if (alpha <= 0 && fadeDirection == false) {
			transitioning = false;
		}
		alpha += fadeDirection == true ? 0.05 : -0.05;
	}

	public void render(){
		super.render();
		if(transitioning){
			performScreenTransition(nextScreen);
			canvas.begin();
			canvas.drawFade(alpha);
			canvas.end2();
		}

	}

}
