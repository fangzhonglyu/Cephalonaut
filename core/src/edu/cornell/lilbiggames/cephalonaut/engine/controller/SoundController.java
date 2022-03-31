package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Timer;
import edu.cornell.lilbiggames.cephalonaut.assets.AssetDirectory;

public class SoundController {

    /** The Music Object for a bgm that is actively playing */
    private static Music bgmPlaying;

    private static final int MENU_MUSIC_INDEX = 7;

    /** The cache for level musics */
    private static Music[] musicCache = new Music[10];

    /** SFX cache*/
    private static Sound[] soundCache = new Sound[10];

    /** Ink sound object */
    private static Sound inkSound;

    /** Whether the ink sound is playing */
    private static boolean inkPlaying = false;

    /** Gather assets. NEEDS TO BE CALLED BEFORE USE*/
    public synchronized static void gatherSoundAssets(AssetDirectory directory) {
        for (int i = 0; i < MENU_MUSIC_INDEX; i++) {
            musicCache[i] = directory.getEntry("level" + i, Music.class);
        }
        musicCache[MENU_MUSIC_INDEX] = directory.getEntry("mainMenu", Music.class);
        inkSound = directory.getEntry("ink", Sound.class);
        //TODO load the sound effects into the cache
    }

    /**
     * Toggle whether the ink sound is playing
     *
     * @param thrust whether to play the sound or not
     */
    public synchronized static void setInkSound(boolean thrust) {
        if (inkPlaying && !thrust) {
            inkPlaying = false;
            inkSound.stop();
        }
        if (!inkPlaying && thrust) {
            inkPlaying = true;
            inkSound.loop();
        }
    }

    /**
     * Play a bgm. This will not work when there is a bgm in play already
     *
     * @param level the index of the bgm to play
     */
    public synchronized static void playBGM(int level) {
        if (bgmPlaying != null || level >= musicCache.length || level < 0)
            return;
        bgmPlaying = musicCache[level];
        bgmPlaying.setLooping(true);
        bgmPlaying.setVolume(1);
        bgmPlaying.play();

    }

    /**
     * Play a bgm on delay.
     */
    private synchronized static void playBGMDelay(final int level, final float delay) {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                playBGM(level);
            }
        }, delay);
    }

    /**
     * Fade out the current bgm. The bgm will continue to play momentarily but bgmPlaying will be set to null.
     */
    public synchronized static void fadeOutBGM() {
        if (bgmPlaying == null)
            return;
        final Music fading = bgmPlaying;
        bgmPlaying = null;
        Timer.schedule(new Timer.Task() {
            int i = 10;

            @Override
            public void run() {
                if (i <= 0) {
                    if(bgmPlaying!=fading)
                        fading.stop();
                } else {
                    i--;
                    if(bgmPlaying!=fading)
                        fading.setVolume((float) i / 10);
                }
            }
        }, 0f, 0.1f, 10);
    }

    /**
     * Switch a track. The current bgm will fade and the new one will play after the fade.
     */
    public synchronized static void switchTrack(int level) {
        if (bgmPlaying == null) {
            playBGM(level);
            return;
        }
        fadeOutBGM();
        playBGMDelay(level, 1.2f);
    }

    /**
     *
     *
     *
     * Play the start menu music.
     */
    public synchronized static void startMenuMusic() {
        switchTrack(MENU_MUSIC_INDEX);
    }

    /**
     * Stops and empties the current bgm
     */
    public synchronized static void pauseBGM() {
        if (bgmPlaying == null)
            return;
        bgmPlaying.stop();
        bgmPlaying = null;
    }

    /** Play a Sound by index. The sound effect will play once and stop */
    public static void playSound(int i,float volume){
        if (i >= soundCache.length || i < 0 || soundCache[i]==null)
            return;
        soundCache[i].play(volume);
    }

    /** Kill all sounds related of that index */
    public static void killSound(int i){
        if (i >= soundCache.length || i < 0 || soundCache[i]==null)
            return;
        soundCache[i].stop();
    }

    /** Kill all one-time sound effects*/
    public static void killAllSound(){
        for(int i = 0; i<soundCache.length;i++){
            killSound(i);
        }
    }
}
