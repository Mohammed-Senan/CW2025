package com.comp2042.controller;

import com.comp2042.model.LevelConfig;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Coordinates level-based gameplay, tracking the active level, block and
 * score requirements, and integrating with {@link LevelProgressManager}
 * to honor which levels are unlocked.
 */
public class LevelManager {
    
    private final IntegerProperty currentLevel = new SimpleIntegerProperty(1);
    private final IntegerProperty blocksPlaced = new SimpleIntegerProperty(0);
    private final IntegerProperty blocksRequired = new SimpleIntegerProperty(0);
    private final IntegerProperty scoreRequired = new SimpleIntegerProperty(0);
    
    private LevelConfig currentLevelConfig;
    private LevelConfig[] allLevels;
    private LevelProgressManager progressManager;
    
    private static final int MAX_LEVEL = 10;
    
    /**
     * Creates a level manager starting from level 1 using the default
     * level configuration set.
     */
    public LevelManager() {
        this.progressManager = new LevelProgressManager();
        this.allLevels = LevelConfig.createDefaultLevels();
        updateLevelLockStatus();
        setLevel(1);
    }
    
    /**
     * Creates a level manager starting from a specific level using the
     * default level configuration set.
     *
     * @param levelId initial level to load
     */
    public LevelManager(int levelId) {
        this.progressManager = new LevelProgressManager();
        this.allLevels = LevelConfig.createDefaultLevels();
        updateLevelLockStatus();
        setLevel(levelId);
    }
    
    /**
     * Selects the current level configuration if the given identifier is
     * within bounds and the level is not locked.
     *
     * @param levelId level to make active
     */
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
    
    /**
     * Resets per-level counters such as blocks placed and recomputes the
     * current level requirements.
     */
    public void reset() {
        blocksPlaced.set(0);
        updateLevelRequirements();
    }
    
    /**
     * Refreshes the lock status of all levels based on the highest
     * unlocked level recorded in the progress manager.
     */
    private void updateLevelLockStatus() {
        int highestUnlocked = progressManager.getHighestLevelUnlocked();
        for (int i = 0; i < allLevels.length; i++) {
            allLevels[i].setLocked(i + 1 > highestUnlocked);
        }
    }
    
    /**
     * Increments the count of blocks placed in the current level.
     */
    public void incrementBlocksPlaced() {
        blocksPlaced.set(blocksPlaced.get() + 1);
    }
    
    /**
     * Determines whether the level is complete based on blocks placed
     * and the player's current score.
     *
     * @param currentScore the player's current score
     * @return {@code true} if all requirements are met, {@code false} otherwise
     */
    public boolean checkLevelComplete(int currentScore) {
        return blocksPlaced.get() >= blocksRequired.get() && currentScore >= scoreRequired.get();
    }
    
    /**
     * Determines whether the level has failed because the block quota has
     * been reached without hitting the target score.
     *
     * @param currentScore the player's current score
     * @return {@code true} if the level is failed, {@code false} otherwise
     */
    public boolean checkLevelFailed(int currentScore) {
        if (blocksPlaced.get() >= blocksRequired.get() && currentScore < scoreRequired.get()) {
            return true;
        }
        return false;
    }
    
    /**
     * Advances to the next level if one exists, resetting per-level
     * counters.
     *
     * @return {@code true} if a higher level was activated, {@code false} if
     *         the current level is already the maximum
     */
    public boolean advanceLevel() {
        if (currentLevel.get() < MAX_LEVEL) {
            currentLevel.set(currentLevel.get() + 1);
            blocksPlaced.set(0);
            updateLevelRequirements();
            return true;
        }
        return false;
    }
    
    /**
     * Indicates whether the current level is the highest supported level.
     *
     * @return {@code true} if at the maximum level, {@code false} otherwise
     */
    public boolean isMaxLevel() {
        return currentLevel.get() >= MAX_LEVEL;
    }
    
    /**
     * Returns the maximum level index configured for the game.
     *
     * @return the maximum level number
     */
    public int getMaxLevel() {
        return MAX_LEVEL;
    }
    
    /**
     * Updates the cached blocks and score requirements from the active
     * {@link LevelConfig}.
     */
    private void updateLevelRequirements() {
        if (currentLevelConfig != null) {
            blocksRequired.set(currentLevelConfig.getBlocksRequired());
            scoreRequired.set(currentLevelConfig.getTargetScore());
        }
    }
    
    /**
     * Returns the configuration object for the current level.
     *
     * @return active {@link LevelConfig}, or {@code null} if none is set
     */
    public LevelConfig getCurrentLevelConfig() {
        return currentLevelConfig;
    }
    
    /**
     * Returns the full array of level configurations.
     *
     * @return all configured levels
     */
    public LevelConfig[] getAllLevels() {
        return allLevels;
    }
    
    /**
     * Returns the drop speed in milliseconds for the current level, or a
     * sensible default if no level is active.
     *
     * @return the drop speed for the current level
     */
    public int getDropSpeed() {
        return currentLevelConfig != null ? currentLevelConfig.getDropSpeed() : 400;
    }
    
    /**
     * Unlocks the next level in progression, if it exists, and updates
     * lock states accordingly.
     */
    public void unlockNextLevel() {
        int nextLevel = currentLevel.get() + 1;
        if (nextLevel <= MAX_LEVEL) {
            progressManager.unlockLevel(nextLevel);
            updateLevelLockStatus();
        }
    }
    
    /**
     * Indicates whether a level is currently locked.
     *
     * @param levelId level index to query
     * @return {@code true} if the level is locked, {@code false} otherwise
     */
    public boolean isLevelLocked(int levelId) {
        if (levelId >= 1 && levelId <= MAX_LEVEL) {
            return allLevels[levelId - 1].isLocked();
        }
        return true;
    }
    
    /**
     * Returns how many blocks remain to be placed before the level's
     * block requirement is satisfied.
     *
     * @return non-negative number of remaining blocks
     */
    public int getBlocksRemaining() {
        return Math.max(0, blocksRequired.get() - blocksPlaced.get());
    }
    
    /**
     * Returns a simple multiplier derived from the current level, useful
     * for scaling difficulty-related parameters.
     *
     * @return the current level value as a multiplier (at least 1)
     */
    public int getSpeedMultiplier() {
        return Math.max(1, currentLevel.get());
    }
    
    /**
     * Exposes the current level as a bindable JavaFX property.
     *
     * @return property representing the current level
     */
    public IntegerProperty currentLevelProperty() {
        return currentLevel;
    }
    
    /**
     * Exposes the blocks-placed counter as a bindable JavaFX property.
     *
     * @return property tracking blocks placed this level
     */
    public IntegerProperty blocksPlacedProperty() {
        return blocksPlaced;
    }
    
    /**
     * Exposes the blocks-required target as a bindable JavaFX property.
     *
     * @return property for block requirement this level
     */
    public IntegerProperty blocksRequiredProperty() {
        return blocksRequired;
    }
    
    /**
     * Exposes the score-required target as a bindable JavaFX property.
     *
     * @return property for score requirement this level
     */
    public IntegerProperty scoreRequiredProperty() {
        return scoreRequired;
    }
    
    /**
     * Returns the numeric identifier of the current level.
     *
     * @return current level index
     */
    public int getCurrentLevel() {
        return currentLevel.get();
    }
    
    /**
     * Returns how many blocks have been placed so far in this level.
     *
     * @return blocks placed counter
     */
    public int getBlocksPlaced() {
        return blocksPlaced.get();
    }
    
    /**
     * Returns the number of blocks that must be placed to satisfy the
     * level's block requirement.
     *
     * @return total blocks required for this level
     */
    public int getBlocksRequired() {
        return blocksRequired.get();
    }
    
    /**
     * Returns the score that must be achieved to complete the current level.
     *
     * @return target score for this level
     */
    public int getScoreRequired() {
        return scoreRequired.get();
    }
}

