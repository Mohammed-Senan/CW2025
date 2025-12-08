package com.comp2042;

import com.comp2042.controller.LevelManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LevelManagerTest {

    @Test
    public void testCheckLevelCompleteReturnsTrueWhenRequirementsMet() {
        LevelManager levelManager = new LevelManager(1);

        int blocksRequired = levelManager.getBlocksRequired();
        int scoreRequired = levelManager.getScoreRequired();

        // Simulate placing all required blocks
        for (int i = 0; i < blocksRequired; i++) {
            levelManager.incrementBlocksPlaced();
        }

        boolean complete = levelManager.checkLevelComplete(scoreRequired);

        assertTrue(complete, "Level should be complete when both blocks and score requirements are met");
    }

    @Test
    public void testCheckLevelFailedWhenBlocksRunOutAndScoreLow() {
        LevelManager levelManager = new LevelManager(1);

        int blocksRequired = levelManager.getBlocksRequired();
        int scoreRequired = levelManager.getScoreRequired();

        // Simulate placing all required blocks
        for (int i = 0; i < blocksRequired; i++) {
            levelManager.incrementBlocksPlaced();
        }

        int lowScore = Math.max(0, scoreRequired - 1);

        boolean failed = levelManager.checkLevelFailed(lowScore);

        assertTrue(failed, "Level should be failed when block quota is reached but score is below target");
    }

    @Test
    public void testUnlockNextLevelUnlocksHigherLevel() {
        LevelManager levelManager = new LevelManager(1);
        int initialLevel = levelManager.getCurrentLevel();
        int nextLevel = initialLevel + 1;

        // Try to unlock next level, ignoring JavaFX audio / Toolkit errors
        try {
            levelManager.unlockNextLevel();
        } catch (Exception e) {
            // Ignore "Toolkit not initialized" errors from SoundManager
            // This is expected during unit tests without a GUI
        }

        // Verify that the next level is not locked anymore
        assertFalse(levelManager.isLevelLocked(nextLevel),
                "Next level should be unlocked after calling unlockNextLevel()");
    }
}
