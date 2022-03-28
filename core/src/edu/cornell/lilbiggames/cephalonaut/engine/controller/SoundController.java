package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Timer;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;

public class SoundController {
    private static Music bgmPlaying;
    private static final int MENU_MUSIC_INDEX = 7;
    private static Music[] musicCache = new Music[10];
    private static Sound inkSound;
    private static boolean locked = false;
    private static boolean inkPlaying = false;

    public static void gatherSoundAssets(AssetDirectory directory){
        for(int i = 0; i < MENU_MUSIC_INDEX; i++){
            musicCache[i] = directory.getEntry("level"+i,Music.class);
        }
        musicCache[MENU_MUSIC_INDEX] = directory.getEntry("mainMenu",Music.class);
        inkSound = directory.getEntry("ink",Sound.class);
    }

    public static void setInkSound(boolean thrust){
        if(inkPlaying&&!thrust){
            inkPlaying = false;
            inkSound.stop();
        }
        if(!inkPlaying&&thrust){
            inkPlaying = true;
            inkSound.loop();
        }
    }

    public static void playBGM(int level){
        if(bgmPlaying!=null||locked||level>=musicCache.length||level<=0)
            pauseBGM();
        bgmPlaying = musicCache[level];
        bgmPlaying.setLooping(true);
        bgmPlaying.setVolume(1);
        bgmPlaying.play();

    }

    private static void playBGMDelay(final int level, final float delay){
        if(locked)
            return;
        Timer.schedule(new Timer.Task(){
            @Override
            public void run() {
                bgmPlaying = musicCache[level];
                bgmPlaying.setLooping(true);
                bgmPlaying.play();
                locked = false;
                bgmPlaying.setVolume(1);
            }
        },delay);
    }

    public static void fadeOutBGM(){
        if(bgmPlaying==null||locked)
            return;
        final Music fading = bgmPlaying;
        bgmPlaying = null;
        Timer.schedule(new Timer.Task() {
            int i = 10;
            @Override
            public void run() {
                if(i<=0) {
                    fading.stop();
                }
                else {
                    i--;
                    fading.setVolume((float)i/10);
                }
            }
        }, 0f,0.2f,10);
    }

    public static void switchTrack(int level){
        if(locked)
            return;
        if(bgmPlaying == null) {
            playBGM(level);
            return;
        }
        fadeOutBGM();
        playBGMDelay(level,2.5f);
        locked = true;
    }

    public static void startMenuMusic(){
        switchTrack(MENU_MUSIC_INDEX);
    }

    public static void pauseBGM(){
        if(bgmPlaying==null||locked)
            return;
        bgmPlaying.stop();
        bgmPlaying = null;
    }
}
