package com.comp2042.logic;

import com.comp2042.event.EventSource;
import com.comp2042.event.InputEventListener;
import com.comp2042.event.MoveEvent;
import com.comp2042.ui.GuiController;
import javafx.scene.paint.Paint;
import java.util.ArrayList;
import java.util.List;

public class GameController implements InputEventListener {

    private Board board;

    private final GuiController viewGuiController;
    private final HighScoreManager highScoreManager = new HighScoreManager();
    private LevelManager levelManager;
    private boolean levelMode = false;
    private static final int SOFT_DROP_SCORE = 1;
    private static final int LINE_SCORE_MULTIPLIER = 50;
    
    // Physics-Based Neon Glass Shatter System (High Impact)
    private List<Shard> activeShards = new ArrayList<>();
    private List<WhiteFlash> activeFlashes = new ArrayList<>();
    
    /**
     * WhiteFlash Class for Impact Flash Effect
     * Creates a blinding white flash when a row is cleared
     */
    public static class WhiteFlash {
        private int gridY;           // The row index that was cleared
        private double life;          // Life remaining (1.0 to 0.0)
        private double maxLife;       // Maximum life duration (0.1 seconds - very fast)
        
        public WhiteFlash(int gridY) {
            this.gridY = gridY;
            this.life = 1.0;
            this.maxLife = 0.1; // 0.1 seconds - blinding fast flash
        }
        
        /**
         * Update the flash: decrease life over time
         * @param deltaTime Time elapsed since last update (in seconds)
         * @return true if flash is still alive, false if dead
         */
        public boolean update(double deltaTime) {
            life -= deltaTime / maxLife;
            if (life < 0.0) {
                life = 0.0;
            }
            return life > 0.0;
        }
        
        public int getGridY() { return gridY; }
        public double getLife() { return life; }
    }
    
    /**
     * Shard Class for Physics-Based Debris (High Impact)
     * Fields: x, y, velocity X, velocity Y (gravity), rotation, rotationSpeed, color, opacity
     */
    public static class Shard {
        private double x;                // X position in grid coordinates
        private double y;                // Y position in grid coordinates
        private double velocityX;        // X velocity (explosive)
        private double velocityY;        // Y velocity (affected by gravity)
        private double rotation;         // Current rotation angle in degrees
        private double rotationSpeed;    // Rotation speed in degrees per second (high)
        private javafx.scene.paint.Color color;  // Shard color (white or very bright)
        private double opacity;           // Opacity (1.0 to 0.0)
        
        public Shard(double x, double y, double velocityX, double velocityY, 
                     double rotationSpeed, javafx.scene.paint.Color color) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.rotation = 0.0;
            this.rotationSpeed = rotationSpeed;
            this.color = color;
            this.opacity = 1.0;
        }
        
        /**
         * Update the shard physics: apply strong gravity, update position, rotation, and opacity
         * @param deltaTime Time elapsed since last update (in seconds)
         * @return true if shard is still alive, false if dead
         */
        public boolean update(double deltaTime) {
            // Add strong gravity to velocityY (vy += 0.8)
            velocityY += 0.8 * deltaTime * 60; // Strong gravity in grid units
            
            // Update position
            x += velocityX * deltaTime * 60; // Scale for 60 FPS
            y += velocityY * deltaTime * 60; // Scale for 60 FPS
            
            // Update rotation (high speed)
            rotation += rotationSpeed * deltaTime * 60; // Scale for 60 FPS
            if (rotation >= 360.0) {
                rotation -= 360.0;
            }
            if (rotation < 0.0) {
                rotation += 360.0;
            }
            
            // Decrease opacity slowly
            opacity -= deltaTime * 0.3; // Fade out over ~3 seconds
            if (opacity < 0.0) {
                opacity = 0.0;
            }
            
            // Shard is dead if opacity is 0 or it's fallen off screen
            return opacity > 0.0 && y < 1000; // Arbitrary screen bottom limit
        }
        
        public double getX() { return x; }
        public double getY() { return y; }
        public double getRotation() { return rotation; }
        public javafx.scene.paint.Color getColor() { return color; }
        public double getOpacity() { return opacity; }
    }
    public GameController(GuiController c, Board b) {
        viewGuiController = c;
        this.board = b;
    }
    public void initGame() {
        board.createNewBrick();
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());
        viewGuiController.bindScore(board.getScore().scoreProperty());
    }
    
    public void initLevelGame() {
        initLevelGame(1);
    }
    
    public void initLevelGame(int levelId) {
        levelManager = new LevelManager(levelId);
        levelMode = true;
        board.createNewBrick();
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());
        viewGuiController.bindScore(board.getScore().scoreProperty());
        viewGuiController.bindLevelInfo(levelManager);
    }

    @Override
    public DownData onDownEvent(MoveEvent event) {
        boolean canMove = board.moveBrickDown();
        ClearRow clearRow = null;
        int scoreBonus = 0;

        if (!canMove) {
            board.mergeBrickToBackground();
            
            if (levelMode && levelManager != null) {
                levelManager.incrementBlocksPlaced();
            }
            
            if (levelMode) {
                int[][] boardMatrix = board.getBoardMatrix();
                boolean topOutDetected = false;
                if (boardMatrix != null && boardMatrix.length > 0) {
                    int numRows = boardMatrix.length;
                    int numCols = boardMatrix[0] != null ? boardMatrix[0].length : 0;
                    for (int row = 0; row <= 2 && row < numRows; row++) {
                        if (boardMatrix[row] != null) {
                            for (int col = 0; col < numCols && col < boardMatrix[row].length; col++) {
                                if (boardMatrix[row][col] != 0) {
                                    topOutDetected = true;
                                    break;
                                }
                            }
                        }
                        if (topOutDetected) {
                            break;
                        }
                    }
                }
                
                if (topOutDetected) {
                    viewGuiController.gameOver();
                    highScoreManager.saveHighScore(board.getScore().scoreProperty().get());
                    return new DownData(null, board.getViewData(), 0);
                }
            }
            
            clearRow = board.clearRows();

            if (clearRow != null && clearRow.getLinesRemoved() > 0) {
                scoreBonus = LINE_SCORE_MULTIPLIER * clearRow.getLinesRemoved() * clearRow.getLinesRemoved();
                board.getScore().add(scoreBonus);
                
                // Trigger shatter animations for all cleared rows
                // CRITICAL: Capture board state BEFORE clearing to get block colors
                int[][] boardBeforeClear = null;
                if (board.getBoardMatrix() != null) {
                    boardBeforeClear = MatrixOperations.copy(board.getBoardMatrix());
                }
                
                if (clearRow.getClearedRowIndices() != null) {
                    for (Integer rowIndex : clearRow.getClearedRowIndices()) {
                        triggerShatter(rowIndex, boardBeforeClear);
                    }
                }
            }

            if (levelMode) {
                int[][] boardMatrix = board.getBoardMatrix();
                boolean topOutDetected = false;
                if (boardMatrix != null && boardMatrix.length > 0) {
                    int numRows = boardMatrix.length;
                    int numCols = boardMatrix[0] != null ? boardMatrix[0].length : 0;
                    for (int row = 0; row <= 2 && row < numRows; row++) {
                        if (boardMatrix[row] != null) {
                            for (int col = 0; col < numCols && col < boardMatrix[row].length; col++) {
                                if (boardMatrix[row][col] != 0) {
                                    topOutDetected = true;
                                    break;
                                }
                            }
                        }
                        if (topOutDetected) {
                            break;
                        }
                    }
                }
                
                if (topOutDetected) {
                    viewGuiController.gameOver();
                    highScoreManager.saveHighScore(board.getScore().scoreProperty().get());
                    return new DownData(clearRow, board.getViewData(), scoreBonus);
                }
            }

            boolean cannotCreateNewBrick = board.createNewBrick();
            if (cannotCreateNewBrick) {
                viewGuiController.gameOver();
                highScoreManager.saveHighScore(board.getScore().scoreProperty().get());
                return new DownData(clearRow, board.getViewData(), scoreBonus);
            }
            
            if (levelMode && levelManager != null) {
                int currentScore = board.getScore().scoreProperty().get();
                
                if (levelManager.checkLevelFailed(currentScore)) {
                    viewGuiController.levelFailed(levelManager.getCurrentLevel(), currentScore, levelManager.getScoreRequired());
                    return new DownData(clearRow, board.getViewData(), scoreBonus);
                }
                
                if (currentScore >= levelManager.getScoreRequired() && levelManager.getBlocksPlaced() >= levelManager.getBlocksRequired()) {
                    levelManager.unlockNextLevel();
                    
                    int completedLevel = levelManager.getCurrentLevel();
                    viewGuiController.levelWon(completedLevel, levelManager.isMaxLevel());
                    return new DownData(clearRow, board.getViewData(), scoreBonus);
                }
            }

                viewGuiController.refreshGameBackground(board.getBoardMatrix());

        } else {
            if (event.getEventSource() == EventSource.USER) {
                board.getScore().add(SOFT_DROP_SCORE);
            }
        }

        return new DownData(clearRow, board.getViewData(), scoreBonus);
    }

    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        board.moveBrickLeft();
        return board.getViewData();
    }

    @Override
    public ViewData onRightEvent(MoveEvent event) {
        board.moveBrickRight();
        return board.getViewData();
    }

    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        board.rotateLeftBrick();
        return board.getViewData();
    }


    @Override
    public void createNewGame() {
        board.newGame();
        if (levelMode && levelManager != null) {
            levelManager.reset();
        }
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
    }
    
    public LevelManager getLevelManager() {
        return levelManager;
    }
    
    public boolean isLevelMode() {
        return levelMode;
    }
    
    public int getSpeedDelay() {
        if (levelMode && levelManager != null) {
            return levelManager.getDropSpeed();
        }
        return 400;
    }
    
    public Board getBoard() {
        return board;
    }
    
    /**
     * Trigger a shatter animation for a cleared row (HIGH IMPACT)
     * Creates a white flash followed by massive explosion of shards
     * @param gridY The grid row index that was cleared
     * @param boardBeforeClear The board matrix BEFORE clearing (to get block colors)
     */
    public void triggerShatter(int gridY, int[][] boardBeforeClear) {
        if (boardBeforeClear == null || gridY < 0 || gridY >= boardBeforeClear.length) {
            return;
        }
        
        // WHITE FLASH: Create blinding white flash at the cleared row
        activeFlashes.add(new WhiteFlash(gridY));
        
        int[] row = boardBeforeClear[gridY];
        if (row == null) {
            return;
        }
        
        final int TILE_SIZE = 20; // BRICK_SIZE from GuiController
        
        // Loop through every block in the cleared row
        for (int col = 0; col < row.length; col++) {
            int blockColor = row[col];
            
            // Only spawn shards for non-zero blocks (occupied cells)
            if (blockColor != 0) {
                // Get the color of the block and make it brighter
                Paint blockPaint = viewGuiController.getFillColor(blockColor);
                javafx.scene.paint.Color blockColorObj;
                
                if (blockPaint instanceof javafx.scene.paint.Color) {
                    blockColorObj = ((javafx.scene.paint.Color) blockPaint).brighter();
                } else {
                    blockColorObj = javafx.scene.paint.Color.WHITE.brighter();
                }
                
                // Calculate block center position in grid coordinates
                // Store as grid coordinates (col, row) - will be converted to pixels in rendering
                double blockCenterX = col; // Grid column
                double blockCenterY = gridY - 2; // Grid row (account for 2-row offset in display)
                
                // HIGH IMPACT: Spawn 10-15 shards per block (massive explosion)
                int shardCount = 10 + (int)(Math.random() * 6); // 10-15 shards
                
                for (int i = 0; i < shardCount; i++) {
                    // EXPLOSIVE velocities: vx range -10 to +10, vy range -15 to -5 (blast upwards)
                    double velocityX = -10 + Math.random() * 20; // -10 to +10 (wide spread)
                    double velocityY = -15 + Math.random() * 10; // -15 to -5 (forceful upward blast)
                    
                    // HIGH rotation speed for visible spinning
                    double rotationSpeed = -720 + Math.random() * 1440; // -720 to +720 degrees per second (very fast)
                    
                    // Create shard with slight random offset from block center (in grid coordinates)
                    double shardX = blockCenterX + (Math.random() - 0.5) * 0.5; // Small offset
                    double shardY = blockCenterY + (Math.random() - 0.5) * 0.5; // Small offset
                    
                    // Use white or very bright color for maximum impact
                    javafx.scene.paint.Color shardColor = javafx.scene.paint.Color.WHITE;
                    if (Math.random() > 0.3) { // 70% white, 30% bright block color
                        shardColor = blockColorObj.brighter().brighter(); // Very bright version
                    }
                    
                    activeShards.add(new Shard(shardX, shardY, velocityX, velocityY, rotationSpeed, shardColor));
                }
            }
        }
    }
    
    /**
     * Get the list of active shards (for rendering)
     * @return List of active Shard objects
     */
    public List<Shard> getActiveShards() {
        return activeShards;
    }
    
    /**
     * Get the list of active white flashes (for rendering)
     * @return List of active WhiteFlash objects
     */
    public List<WhiteFlash> getActiveFlashes() {
        return activeFlashes;
    }
    
    /**
     * Update all active white flashes and remove dead ones
     * Should be called every frame
     * @param deltaTime Time elapsed since last update (in seconds)
     */
    public void updateFlashes(double deltaTime) {
        activeFlashes.removeIf(flash -> !flash.update(deltaTime));
    }
    
    /**
     * Update all active shards and remove dead ones
     * Should be called every frame
     * @param deltaTime Time elapsed since last update (in seconds)
     */
    public void updateShards(double deltaTime) {
        activeShards.removeIf(shard -> !shard.update(deltaTime));
    }
    
    /**
     * Clear all active shards and flashes (e.g., when starting a new game)
     */
    public void clearAllShards() {
        activeShards.clear();
        activeFlashes.clear();
    }
}
