package com.comp2042.logic;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    
    private static SoundManager instance;
    
    private MediaPlayer menuMusicPlayer;
    private MediaPlayer gameMusicPlayer;
    private Map<String, MediaPlayer> sfxPlayers;
    
    private SoundManager() {
        sfxPlayers = new HashMap<>();
        loadAudioResources();
    }
    
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }
    
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
            
        } catch (Exception e) {
            System.err.println("Error loading audio resources: " + e.getMessage());
        }
    }
    
    private void loadSFX(String name, String resourcePath) {
        try {
            URL sfxUrl = getClass().getClassLoader().getResource(resourcePath);
            if (sfxUrl != null) {
                Media sfxMedia = new Media(sfxUrl.toExternalForm());
                MediaPlayer sfxPlayer = new MediaPlayer(sfxMedia);
                sfxPlayer.setVolume(GameConfig.getSfxVolume() / 100.0);
                sfxPlayers.put(name, sfxPlayer);
            }
        } catch (Exception e) {
            System.err.println("Error loading SFX " + name + ": " + e.getMessage());
        }
    }
    
    public void playMenuMusic() {
        stopMusic();
        if (menuMusicPlayer != null) {
            menuMusicPlayer.setVolume(GameConfig.getMusicVolume() / 100.0);
            menuMusicPlayer.play();
        }
    }
    
    public void playGameMusic() {
        stopMusic();
        if (gameMusicPlayer != null) {
            gameMusicPlayer.setVolume(GameConfig.getMusicVolume() / 100.0);
            gameMusicPlayer.play();
        }
    }
    
    public void stopMusic() {
        if (menuMusicPlayer != null) {
            menuMusicPlayer.stop();
        }
        if (gameMusicPlayer != null) {
            gameMusicPlayer.stop();
        }
    }
    
    public void playMove() {
        playSFX("move");
    }
    
    public void playRotate() {
        playSFX("rotate");
    }
    
    public void playClear() {
        playSFX("clear");
    }
    
    public void playGameOver() {
        playSFX("gameover");
    }
    
    public void playCountBlip() {
        playSFX("count");
    }
    
    private void playSFX(String name) {
        MediaPlayer player = sfxPlayers.get(name);
        if (player != null) {
            player.setVolume(GameConfig.getSfxVolume() / 100.0);
            player.seek(Duration.ZERO);
            player.play();
        }
    }
    
    public void updateMusicVolume() {
        if (menuMusicPlayer != null) {
            menuMusicPlayer.setVolume(GameConfig.getMusicVolume() / 100.0);
        }
        if (gameMusicPlayer != null) {
            gameMusicPlayer.setVolume(GameConfig.getMusicVolume() / 100.0);
        }
    }
    
    public void updateSfxVolume() {
        for (MediaPlayer player : sfxPlayers.values()) {
            player.setVolume(GameConfig.getSfxVolume() / 100.0);
        }
    }
}

