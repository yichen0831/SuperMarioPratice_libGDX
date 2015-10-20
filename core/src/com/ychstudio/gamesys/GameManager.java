package com.ychstudio.gamesys;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

/**
 * Created by yichen on 10/11/15.
 *
 * GameManager
 */
public class GameManager implements Disposable {

    public static GameManager instance;

    public static final float PPM = 16;

    public static final int WINDOW_WIDTH = Gdx.graphics.getWidth();
    public static final int WINDOW_HEIGHT = Gdx.graphics.getHeight();

    public static final float V_WIDTH = 20.0f;
    public static final float V_HEIGHT = 15.0f;

    public static final Vector2 GRAVITY = new Vector2(0.0f, -9.8f * 4);

    public static final float STEP = 1 / 60.0f;

    public static final short NOTHING_BIT = 0;
    public static final short GROUND_BIT = 1;
    public static final short MARIO_BIT = 1 << 1;
    public static final short MARIO_HEAD_BIT = 1 << 2;
    public static final short ENEMY_LETHAL_BIT = 1 << 3;
    public static final short ENEMY_WEAKNESS_BIT = 1 << 4;
    public static final short ENEMY_INTERACT_BIT = 1 << 5;
    public static final short ITEM_BIT = 1 << 6;
    public static final short WEAPON_BIT = 1 << 7;
    public static final short FLAGPOLE_BIT = 1 << 8;

    public static final String musicPath = "audio/music/";

    private AssetManager assetManager;

    private int score;
    private int coins;

    public static float timeScale = 1;

    public GameManager() {
        if (instance == null) {
            instance = this;
        }

        if (assetManager == null) {
            assetManager = new AssetManager();
        }

        loadAudio();

        score = 0;
        coins = 0;
    }

    private void loadAudio() {
        assetManager.load("audio/music/mario_music.ogg", Music.class);
        assetManager.load("audio/music/mario_music_hurry.ogg", Music.class);
        assetManager.load("audio/music/out_of_time.ogg", Music.class);
        assetManager.load("audio/music/game_over.ogg", Music.class);
        assetManager.load("audio/music/invincible.ogg", Music.class);
        assetManager.load("audio/music/stage_clear.ogg", Music.class);
        assetManager.load("audio/music/flagpole.ogg", Music.class);
        assetManager.load("audio/sfx/breakblock.wav", Sound.class);
        assetManager.load("audio/sfx/bump.wav", Sound.class);
        assetManager.load("audio/sfx/coin.wav", Sound.class);
        assetManager.load("audio/sfx/jump_small.wav", Sound.class);
        assetManager.load("audio/sfx/jump_super.wav", Sound.class);
        assetManager.load("audio/sfx/mariodie.wav", Sound.class);
        assetManager.load("audio/sfx/powerdown.wav", Sound.class);
        assetManager.load("audio/sfx/powerup.wav", Sound.class);
        assetManager.load("audio/sfx/powerup_spawn.wav", Sound.class);
        assetManager.load("audio/sfx/stomp.wav", Sound.class);
        assetManager.load("audio/sfx/fireball.ogg", Sound.class);
        assetManager.load("audio/sfx/kick.ogg", Sound.class);
        assetManager.finishLoading();
    }


    public void gameOver() {
        clearScore();
    }

    public int getScore() {
        return score;
    }

    public void clearScore() {
        score = 0;
    }

    public void addScore(int value) {
        score += value;
    }

    public void addCoin() {
        addCoin(1);
    }

    public void addCoin(int value) {
        coins += value;
    }

    public int getCoins() {
        return coins;
    }

    public static void setTimeScale(float value) {
        timeScale = MathUtils.clamp(value, 0.0f, 2.0f);
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    private String currentMusic = "";

    public void playMusic(String filename) {
        playMusic(filename, true);
    }

    public void playMusic(String filename, boolean loop) {
        if (!currentMusic.equals(filename)) {
            stopMusic();
            currentMusic = filename;
        }

        if (isPlayingMusic(currentMusic)) {
            return;
        }
        assetManager.get(musicPath + filename, Music.class).setLooping(loop);
        assetManager.get(musicPath + filename, Music.class).play();
    }

    public boolean isPlayingMusic() {
        return isPlayingMusic(currentMusic);
    }

    public void pauseMusic() {
        if (currentMusic.length() > 0) {
            assetManager.get(musicPath + currentMusic, Music.class).pause();
        }
    }

    public void resumeMusic() {
        if (currentMusic.length() > 0) {
            if (!isPlayingMusic(currentMusic)) {
                playMusic(currentMusic);
            }
        }
    }

    public void stopMusic() {
        if (currentMusic.length() > 0) {
            assetManager.get(musicPath + currentMusic, Music.class).stop();
        }
    }

    public boolean isPlayingMusic(String filename) {
        return assetManager.get(musicPath + filename, Music.class).isPlaying();
    }


    @Override
    public void dispose() {
        assetManager.dispose();
    }
}
