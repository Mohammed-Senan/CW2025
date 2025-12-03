package com.comp2042.logic;

import java.io.*;
import java.util.Properties;

public class LevelProgressManager {
    private static final String PROGRESS_FILE = "level_progress.properties";
    private static final String HIGHEST_LEVEL_KEY = "highestLevelUnlocked";
    private int highestLevelUnlocked = 1; // Default: Level 1 unlocked
    
    public LevelProgressManager() {
        loadProgress();
    }
    
    public void loadProgress() {
        Properties props = new Properties();
        File file = new File(PROGRESS_FILE);
        
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
                String levelStr = props.getProperty(HIGHEST_LEVEL_KEY, "1");
                highestLevelUnlocked = Integer.parseInt(levelStr);
            } catch (IOException e) {
                System.err.println("Error loading progress: " + e.getMessage());
                highestLevelUnlocked = 1;
            }
        }
    }
    
    public void saveProgress(int highestLevel) {
        highestLevelUnlocked = highestLevel;
        Properties props = new Properties();
        props.setProperty(HIGHEST_LEVEL_KEY, String.valueOf(highestLevel));
        
        try (FileOutputStream fos = new FileOutputStream(PROGRESS_FILE)) {
            props.store(fos, "Level Progress");
        } catch (IOException e) {
            System.err.println("Error saving progress: " + e.getMessage());
        }
    }
    
    public int getHighestLevelUnlocked() {
        return highestLevelUnlocked;
    }
    
    public void unlockLevel(int level) {
        if (level > highestLevelUnlocked) {
            saveProgress(level);
        }
    }
}







