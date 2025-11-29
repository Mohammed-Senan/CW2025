package com.comp2042.logic;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class LevelManager {
    
    private final IntegerProperty currentLevel = new SimpleIntegerProperty(1);
    private final IntegerProperty blocksPlaced = new SimpleIntegerProperty(0);
    private final IntegerProperty blocksRequired = new SimpleIntegerProperty(0);
    private final IntegerProperty scoreRequired = new SimpleIntegerProperty(0);
    
    // Level configuration: specific requirements per level
    private static final int MAX_LEVEL = 5;
    private static final int BLOCKS_PER_LEVEL = 50;
    
    // Score requirements for each level
    private static final int[] SCORE_REQUIREMENTS = {500, 800, 1000, 1500, 3000};
    
    public LevelManager() {
        updateLevelRequirements();
    }
    
    public void reset() {
        currentLevel.set(1);
        blocksPlaced.set(0);
        updateLevelRequirements();
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
        int level = currentLevel.get();
        blocksRequired.set(BLOCKS_PER_LEVEL);
        
        if (level >= 1 && level <= MAX_LEVEL) {
            scoreRequired.set(SCORE_REQUIREMENTS[level - 1]);
        } else {
            scoreRequired.set(SCORE_REQUIREMENTS[MAX_LEVEL - 1]);
        }
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

