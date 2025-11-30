package com.comp2042.logic;

public class LevelConfig {
    private final int levelId;
    private final int targetScore;
    private final int dropSpeed; // milliseconds between drops
    private final int blocksRequired;
    private boolean isLocked;
    
    public LevelConfig(int levelId, int targetScore, int dropSpeed, int blocksRequired, boolean isLocked) {
        this.levelId = levelId;
        this.targetScore = targetScore;
        this.dropSpeed = dropSpeed;
        this.blocksRequired = blocksRequired;
        this.isLocked = isLocked;
    }
    
    public int getLevelId() {
        return levelId;
    }
    
    public int getTargetScore() {
        return targetScore;
    }
    
    public int getDropSpeed() {
        return dropSpeed;
    }
    
    public int getBlocksRequired() {
        return blocksRequired;
    }
    
    public boolean isLocked() {
        return isLocked;
    }
    
    public void setLocked(boolean locked) {
        this.isLocked = locked;
    }
    
    // Create default level configurations
    public static LevelConfig[] createDefaultLevels() {
        LevelConfig[] levels = new LevelConfig[10];
        
        // Level 1: Easy, slow
        levels[0] = new LevelConfig(1, 500, 400, 50, false);
        
        // Level 2-10: Increasing difficulty
        levels[1] = new LevelConfig(2, 800, 380, 50, true);
        levels[2] = new LevelConfig(3, 1000, 360, 50, true);
        levels[3] = new LevelConfig(4, 1500, 340, 50, true);
        levels[4] = new LevelConfig(5, 3000, 320, 50, true);
        levels[5] = new LevelConfig(6, 4000, 300, 50, true);
        levels[6] = new LevelConfig(7, 5000, 280, 50, true);
        levels[7] = new LevelConfig(8, 6000, 260, 50, true);
        levels[8] = new LevelConfig(9, 7000, 240, 50, true);
        levels[9] = new LevelConfig(10, 8000, 200, 50, true);
        
        return levels;
    }
}

