package com.comp2042.controller;

import com.comp2042.model.GameConfig;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton manager responsible for loading and controlling background music
 * and sound effects for the game. Volumes are driven by {@link GameConfig}.
 */
public class SoundManager {

    private static SoundManager instance;

    private MediaPlayer menuMusicPlayer;
    private MediaPlayer gameMusicPlayer;
    private Map<String, MediaPlayer> sfxPlayers;

    /**
     * Creates a new sound manager instance and loads all configured audio resources.
     * Use {@link #getInstance()} to access the shared singleton.
     */
    private SoundManager() {
        sfxPlayers = new HashMap<>();
        loadAudioResources();
    }

    /**
     * Returns the singleton instance of the sound manager, creating it on first use.
     *
     * @return the global {@code SoundManager} instance
     */
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
     * Loads all configured audio resources, including background music and sound effects.
     */
    private void loadAudioResources() {
        try {
            URL menuMusicUrl = getClass().getClassLoader().getResource("Sounds/bgm_menu.mp3.mp3");
            URL gameMusicUrl = getClass().getClassLoader().getResource("Sounds/bgm_game.mp3.mp3");

            if (menuMusicUrl != null) {
                Media menuMusic = new Media(menuMusicUrl.toExternalForm());
                menuMusicPlayer = new MediaPlayer(menuMusic);
                menuMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                menuMusicPlayer.setVolume(GameConfig.getMusicVolume() / 100.0);
            }
            
            if (gameMusicUrl != null) {
                Media gameMusic = new Media(gameMusicUrl.toExternalForm());
                gameMusicPlayer = new MediaPlayer(gameMusic);
                gameMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                gameMusicPlayer.setVolume(GameConfig.getMusicVolume() / 100.0);
            }
            
            loadSFX("move", "Sounds/sfx_move.mp3.mp3");
            loadSFX("rotate", "Sounds/sfx_rotate.mp3.mp3");
            loadSFX("clear", "Sounds/sfx_clear.mp3.mp3");
            loadSFX("gameover", "Sounds/sfx_gameover.mp3.mp3");
            loadSFX("count", "Sounds/sfx_count.mp3.mp3");
            loadSFX("harddrop", "Sounds/sfx_harddrop.mp3.mp3");
            
        } catch (Exception e) {
            System.err.println("Error loading audio resources: " + e.getMessage());
        }
    }
    
    /**
     * Loads a single sound effect resource and registers it under the given name.
     *
     * @param name         logical identifier for the sound effect
     * @param resourcePath classpath-relative path to the audio resource
     */
    private void loadSFX(String name, String resourcePath) {
        try {
            URL sfxUrl = getClass().getClassLoader().getResource(resourcePath);
            if (sfxUrl != null) {
                Media sfxMedia = new Media(sfxUrl.toExternalForm());
                MediaPlayer sfxPlayer = new MediaPlayer(sfxMedia);
                sfxPlayer.setVolume(GameConfig.getSfxVolume() / 100.0);
                sfxPlayers.put(name, sfxPlayer);
            } else {
                System.err.println("SFX resource not found: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading SFX " + name + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Starts playback of the menu background music, stopping any currently
     * playing game music.
     */
    public void playMenuMusic() {
        stopMusic();
        if (menuMusicPlayer != null) {
            menuMusicPlayer.setVolume(GameConfig.getMusicVolume() / 100.0);
            menuMusicPlayer.play();
        }
    }
    
    /**
     * Starts playback of the in-game background music, stopping any currently
     * playing menu music.
     */
    public void playGameMusic() {
        stopMusic();
        if (gameMusicPlayer != null) {
            gameMusicPlayer.setVolume(GameConfig.getMusicVolume() / 100.0);
            gameMusicPlayer.play();
        }
    }
    
    /**
     * Stops all currently playing background music tracks.
     */
    public void stopMusic() {
        if (menuMusicPlayer != null) {
            menuMusicPlayer.stop();
        }
        if (gameMusicPlayer != null) {
            gameMusicPlayer.stop();
        }
    }
    
    /**
     * Plays the sound effect associated with a piece move action.
     */
    public void playMove() {
        playSFX("move");
    }
    
    /**
     * Plays the sound effect associated with a piece rotation action.
     */
    public void playRotate() {
        playSFX("rotate");
    }
    
    /**
     * Plays the sound effect triggered when one or more lines are cleared.
     */
    public void playClear() {
        playSFX("clear");
    }
    
    /**
     * Plays the sound effect triggered when the game ends.
     */
    public void playGameOver() {
        playSFX("gameover");
    }
    
    /**
     * Plays the sound effect used for countdown or UI tick feedback.
     */
    public void playCountBlip() {
        playSFX("count");
    }
    
    /**
     * Plays the sound effect associated with a hard drop action.
     */
    public void playHardDrop() {
        playSFX("harddrop");
    }
    
    /**
     * Plays a named sound effect if it has been successfully loaded.
     * Resets playback to the beginning before playing.
     *
     * @param name logical identifier of the sound effect to play
     */
    private void playSFX(String name) {
        MediaPlayer player = sfxPlayers.get(name);
        if (player != null) {
            player.setVolume(GameConfig.getSfxVolume() / 100.0);
            player.seek(Duration.ZERO);
            player.play();
        } else {
            System.err.println("SFX player not found for: " + name);
        }
    }
    
    /**
     * Applies the current music volume from {@link GameConfig} to all
     * background music players.
     */
    public void updateMusicVolume() {
        if (menuMusicPlayer != null) {
            menuMusicPlayer.setVolume(GameConfig.getMusicVolume() / 100.0);
        }
        if (gameMusicPlayer != null) {
            gameMusicPlayer.setVolume(GameConfig.getMusicVolume() / 100.0);
        }
    }
    
    /**
     * Applies the current sound-effects volume from {@link GameConfig} to
     * all loaded sound effect players.
     */
    public void updateSfxVolume() {
        for (MediaPlayer player : sfxPlayers.values()) {
            player.setVolume(GameConfig.getSfxVolume() / 100.0);
        }
    }
}

