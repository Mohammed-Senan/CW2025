package com.comp2042.logic;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class LevelManager {
    
    private final IntegerProperty currentLevel = new SimpleIntegerProperty(1);
    private final IntegerProperty blocksPlaced = new SimpleIntegerProperty(0);
    private final IntegerProperty blocksRequired = new SimpleIntegerProperty(0);
    private final IntegerProperty scoreRequired = new SimpleIntegerProperty(0);
    
    private LevelConfig currentLevelConfig;
    private LevelConfig[] allLevels;
    private LevelProgressManager progressManager;
    
    // Level configuration: specific requirements per level
    private static final int MAX_LEVEL = 10;
    
    public LevelManager() {
        this.progressManager = new LevelProgressManager();
        this.allLevels = LevelConfig.createDefaultLevels();
        updateLevelLockStatus();
        setLevel(1);
    }
    
    public LevelManager(int levelId) {
        this.progressManager = new LevelProgressManager();
        this.allLevels = LevelConfig.createDefaultLevels();
        updateLevelLockStatus();
        setLevel(levelId);
    }
    
    public void setLevel(int levelId) {
        if (levelId >= 1 && levelId <= MAX_LEVEL) {
            currentLevelConfig = allLevels[levelId - 1];
            if (!currentLevelConfig.isLocked()) {
                currentLevel.set(levelId);
                blocksPlaced.set(0);
                updateLevelRequirements();
            }
        }
    }
    
    public void reset() {
        blocksPlaced.set(0);
        updateLevelRequirements();
    }
    
    private void updateLevelLockStatus() {
        int highestUnlocked = progressManager.getHighestLevelUnlocked();
        for (int i = 0; i < allLevels.length; i++) {
            allLevels[i].setLocked(i + 1 > highestUnlocked);
        }
    }
    
    public void incrementBlocksPlaced() {
        blocksPlaced.set(blocksPlaced.get() + 1);
    }
    
    public boolean checkLevelComplete(int currentScore) {
        return blocksPlaced.get() >= blocksRequired.get() && currentScore >= scoreRequired.get();
    }
    
    public boolean checkLevelFailed(int currentScore) {
        // Check if player has placed all blocks but didn't reach required score
        if (blocksPlaced.get() >= blocksRequired.get() && currentScore < scoreRequired.get()) {
            return true;
        }
        return false;
    }
    
    public boolean advanceLevel() {
        if (currentLevel.get() < MAX_LEVEL) {
            currentLevel.set(currentLevel.get() + 1);
            blocksPlaced.set(0);
            updateLevelRequirements();
            return true;
        }
        return false; // Max level reached
    }
    
    public boolean isMaxLevel() {
        return currentLevel.get() >= MAX_LEVEL;
    }
    
    public int getMaxLevel() {
        return MAX_LEVEL;
    }
    
    private void updateLevelRequirements() {
        if (currentLevelConfig != null) {
            blocksRequired.set(currentLevelConfig.getBlocksRequired());
            scoreRequired.set(currentLevelConfig.getTargetScore());
        }
    }
    
    public LevelConfig getCurrentLevelConfig() {
        return currentLevelConfig;
    }
    
    public LevelConfig[] getAllLevels() {
        return allLevels;
    }
    
    public int getDropSpeed() {
        return currentLevelConfig != null ? currentLevelConfig.getDropSpeed() : 400;
    }
    
    public void unlockNextLevel() {
        int nextLevel = currentLevel.get() + 1;
        if (nextLevel <= MAX_LEVEL) {
            progressManager.unlockLevel(nextLevel);
            updateLevelLockStatus();
        }
    }
    
    public boolean isLevelLocked(int levelId) {
        if (levelId >= 1 && levelId <= MAX_LEVEL) {
            return allLevels[levelId - 1].isLocked();
        }
        return true;
    }
    
    public int getBlocksRemaining() {
        return Math.max(0, blocksRequired.get() - blocksPlaced.get());
    }
    
    public int getSpeedMultiplier() {
        // Each level makes the game faster (lower delay = faster)
        return Math.max(1, currentLevel.get());
    }
    
    public IntegerProperty currentLevelProperty() {
        return currentLevel;
    }
    
    public IntegerProperty blocksPlacedProperty() {
        return blocksPlaced;
    }
    
    public IntegerProperty blocksRequiredProperty() {
        return blocksRequired;
    }
    
    public IntegerProperty scoreRequiredProperty() {
        return scoreRequired;
    }
    
    public int getCurrentLevel() {
        return currentLevel.get();
    }
    
    public int getBlocksPlaced() {
        return blocksPlaced.get();
    }
    
    public int getBlocksRequired() {
        return blocksRequired.get();
    }
    
    public int getScoreRequired() {
        return scoreRequired.get();
    }
}

