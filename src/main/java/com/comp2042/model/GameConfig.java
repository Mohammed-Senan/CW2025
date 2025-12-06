package com.comp2042.model;

/**
 * GameConfig - Static configuration class for game settings.
 * Stores persistent game configuration that persists across sessions.
 */
public class GameConfig {
    
    /**
     * Ghost Mode setting - determines if the ghost/shadow piece is displayed.
     * Default: true (enabled)
     */
    private static boolean ghostModeEnabled = true;
    
    /**
     * Music Volume setting - volume level from 0 to 100.
     * Default: 50 (50%)
     */
    private static double musicVolume = 50.0;
    
    /**
     * SFX Volume setting - volume level from 0 to 100.
     * Default: 70 (70%)
     */
    private static double sfxVolume = 70.0;
    
    /**
     * Check if Ghost Mode is enabled.
     * @return true if ghost mode is enabled, false otherwise
     */
    public static boolean isGhostModeEnabled() {
        return ghostModeEnabled;
    }
    
    /**
     * Set the Ghost Mode setting.
     * @param enabled true to enable ghost mode, false to disable
     */
    public static void setGhostModeEnabled(boolean enabled) {
        ghostModeEnabled = enabled;
    }
    
    /**
     * Get the Music Volume setting.
     * @return volume level from 0.0 to 100.0
     */
    public static double getMusicVolume() {
        return musicVolume;
    }
    
    /**
     * Set the Music Volume setting.
     * @param volume volume level from 0.0 to 100.0
     */
    public static void setMusicVolume(double volume) {
        if (volume < 0.0) {
            musicVolume = 0.0;
        } else if (volume > 100.0) {
            musicVolume = 100.0;
        } else {
            musicVolume = volume;
        }
    }
    
    /**
     * Get the SFX Volume setting.
     * @return volume level from 0.0 to 100.0
     */
    public static double getSfxVolume() {
        return sfxVolume;
    }
    
    /**
     * Set the SFX Volume setting.
     * @param volume volume level from 0.0 to 100.0
     */
    public static void setSfxVolume(double volume) {
        if (volume < 0.0) {
            sfxVolume = 0.0;
        } else if (volume > 100.0) {
            sfxVolume = 100.0;
        } else {
            sfxVolume = volume;
        }
    }
}
