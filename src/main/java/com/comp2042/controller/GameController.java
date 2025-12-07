package com.comp2042.controller;

import com.comp2042.event.EventSource;
import com.comp2042.event.InputEventListener;
import com.comp2042.event.MoveEvent;
import com.comp2042.ui.GuiController;
import com.comp2042.model.Board;
import com.comp2042.model.ClearRow;
import com.comp2042.model.DownData;
import com.comp2042.model.ViewData;
import com.comp2042.model.MatrixOperations;
import javafx.scene.paint.Paint;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates core Tetris gameplay, translating input events into board
 * updates, managing scoring and level progression, and notifying the UI
 * controller of state changes and game outcomes.
 */
public class GameController implements InputEventListener {

    private Board board;

    private final GuiController viewGuiController;
    private final HighScoreManager highScoreManager = new HighScoreManager();
    private LevelManager levelManager;
    private boolean levelMode = false;
    private static final int SOFT_DROP_SCORE = 1;
    private static final int LINE_SCORE_MULTIPLIER = 50;
    
    private List<Shard> activeShards = new ArrayList<>();
    private List<WhiteFlash> activeFlashes = new ArrayList<>();
    
    /**
     * Visual effect representing a brief white flash across a cleared row.
     */
    public static class WhiteFlash {
        private int gridY;
        private double life;
        private double maxLife;
        
        /**
         * Creates a flash effect aligned with a specific board row.
         *
         * @param gridY the Y index of the cleared row in board coordinates
         */
        public WhiteFlash(int gridY) {
            this.gridY = gridY;
            this.life = 1.0;
            this.maxLife = 0.1;
        }
        
        /**
         * Advances the internal lifetime of the flash.
         *
         * @param deltaTime time elapsed since the last update in seconds
         * @return {@code true} if the effect is still alive, {@code false} when it has finished
         */
        public boolean update(double deltaTime) {
            life -= deltaTime / maxLife;
            if (life < 0.0) {
                life = 0.0;
            }
            return life > 0.0;
        }
        
        /**
         * Returns the board row index where this flash is rendered.
         *
         * @return the grid Y position of the flash
         */
        public int getGridY() { return gridY; }

        /**
         * Returns the remaining normalized lifetime of the flash.
         *
         * @return remaining life in the range {@code 0.0}–{@code 1.0}
         */
        public double getLife() { return life; }
    }
    
    /**
     * Particle effect representing a single shard emitted from a cleared block.
     */
    public static class Shard {
        private double x;
        private double y;
        private double velocityX;
        private double velocityY;
        private javafx.scene.paint.Color startColor;
        private javafx.scene.paint.Color endColor;
        private double opacity;
        private double life;
        private double maxLife;
        private static final double DRAG = 0.85;
        
        /**
         * Creates a new shard with an initial position, velocity, and target color.
         *
         * @param x         initial X position in board coordinates
         * @param y         initial Y position in board coordinates
         * @param velocityX initial horizontal velocity
         * @param velocityY initial vertical velocity
         * @param endColor  color the shard will fade toward as it expires
         */
        public Shard(double x, double y, double velocityX, double velocityY,
                     javafx.scene.paint.Color endColor) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.startColor = javafx.scene.paint.Color.WHITE;
            this.endColor = endColor;
            this.opacity = 1.0;
            this.life = 1.0;
            this.maxLife = 0.5;
        }
        
        public boolean update(double deltaTime) {
            velocityX *= Math.pow(DRAG, deltaTime * 60);
            velocityY *= Math.pow(DRAG, deltaTime * 60);
            
            x += velocityX * deltaTime * 60;
            y += velocityY * deltaTime * 60;
            
            life -= deltaTime / maxLife;
            if (life < 0.0) {
                life = 0.0;
            }
            
            opacity = life;
            if (opacity < 0.0) {
                opacity = 0.0;
            }
            
            return life > 0.0 && opacity > 0.0;
        }
        
        /**
         * Returns the current X position of the shard in board-space units.
         *
         * @return the shard X coordinate
         */
        public double getX() { return x; }

        /**
         * Returns the current Y position of the shard in board-space units.
         *
         * @return the shard Y coordinate
         */
        public double getY() { return y; }

        /**
         * Returns the initial color from which the shard starts its fade.
         *
         * @return the start color of the shard
         */
        public javafx.scene.paint.Color getStartColor() { return startColor; }

        /**
         * Returns the final color toward which the shard fades over its lifetime.
         *
         * @return the target end color of the shard
         */
        public javafx.scene.paint.Color getEndColor() { return endColor; }

        /**
         * Returns the remaining normalized lifetime of the shard.
         *
         * @return remaining life in the range {@code 0.0}–{@code 1.0}
         */
        public double getLife() { return life; }

        /**
         * Returns the current opacity of the shard.
         *
         * @return opacity in the range {@code 0.0}–{@code 1.0}
         */
        public double getOpacity() { return opacity; }
    }
    
    /**
     * Constructs a game controller bound to a specific UI controller and board
     * implementation.
     *
     * @param c the {@link GuiController} responsible for rendering and input
     * @param b the {@link Board} implementation that maintains game state
     */
    public GameController(GuiController c, Board b) {
        viewGuiController = c;
        this.board = b;
    }
    
    /**
     * Initializes a standard endless game session, creating the first brick,
     * binding score updates, and preparing the view.
     */
    public void initGame() {
        board.createNewBrick();
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());
        viewGuiController.bindScore(board.getScore().scoreProperty());
    }
    
    /**
     * Initializes level-based gameplay starting from the first level.
     */
    public void initLevelGame() {
        initLevelGame(1);
    }
    
    /**
     * Initializes level-based gameplay for a specific level identifier, binding
     * level progress information to the UI.
     *
     * @param levelId identifier of the level configuration to load
     */
    public void initLevelGame(int levelId) {
        levelManager = new LevelManager(levelId);
        levelMode = true;
        board.createNewBrick();
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());
        viewGuiController.bindScore(board.getScore().scoreProperty());
        viewGuiController.bindLevelInfo(levelManager);
    }

    /**
     * Handles a request to move the active brick down, updating the board,
     * resolving merges, scoring, level progression, and visual effects.
     *
     * @param event the move event describing the source of the down action
     * @return a {@link DownData} bundle containing clear-row information,
     *         view updates, and any score bonus applied
     */
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
                
                SoundManager.getInstance().playClear();
                
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

    /**
     * Handles a request to move the active brick left and returns updated
     * view data for rendering.
     *
     * @param event the move event describing the source of the action
     * @return updated {@link ViewData} after applying the move
     */
    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        board.moveBrickLeft();
        return board.getViewData();
    }

    /**
     * Handles a request to move the active brick right and returns updated
     * view data for rendering.
     *
     * @param event the move event describing the source of the action
     * @return updated {@link ViewData} after applying the move
     */
    @Override
    public ViewData onRightEvent(MoveEvent event) {
        board.moveBrickRight();
        return board.getViewData();
    }

    /**
     * Handles a request to rotate the active brick and returns updated
     * view data for rendering.
     *
     * @param event the move event describing the source of the action
     * @return updated {@link ViewData} after applying the rotation
     */
    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        board.rotateLeftBrick();
        return board.getViewData();
    }

    /**
     * Resets the underlying board to start a new game and clears any
     * level-specific progress tracking.
     */
    @Override
    public void createNewGame() {
        board.newGame();
        if (levelMode && levelManager != null) {
            levelManager.reset();
        }
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
    }
    
    /**
     * Returns the level manager used for level-based gameplay.
     *
     * @return the current {@link LevelManager}, or {@code null} if not in level mode
     */
    public LevelManager getLevelManager() {
        return levelManager;
    }
    
    /**
     * Indicates whether the controller is currently running in level mode.
     *
     * @return {@code true} if level mode is active, {@code false} otherwise
     */
    public boolean isLevelMode() {
        return levelMode;
    }
    
    /**
     * Returns the current drop delay in milliseconds, either derived from
     * the active level configuration or a default value when not in level mode.
     *
     * @return the drop delay used by the game loop
     */
    public int getSpeedDelay() {
        if (levelMode && levelManager != null) {
            return levelManager.getDropSpeed();
        }
        return 400;
    }
    
    /**
     * Exposes the underlying board used by the controller.
     *
     * @return the active {@link Board} instance
     */
    public Board getBoard() {
        return board;
    }
    
    /**
     * Spawns particle effects for a cleared row using a snapshot of the board
     * state before the clear operation.
     *
     * @param gridY            the index of the cleared row in the board matrix
     * @param boardBeforeClear a snapshot of the board prior to row removal
     */
    public void triggerShatter(int gridY, int[][] boardBeforeClear) {
        if (boardBeforeClear == null || gridY < 0 || gridY >= boardBeforeClear.length) {
            return;
        }
        
        activeFlashes.add(new WhiteFlash(gridY));
        
        int[] row = boardBeforeClear[gridY];
        if (row == null) {
            return;
        }
        
        for (int col = 0; col < row.length; col++) {
            int blockColor = row[col];
            
            if (blockColor != 0) {
                Paint blockPaint = viewGuiController.getFillColor(blockColor);
                javafx.scene.paint.Color blockColorObj;
                
                if (blockPaint instanceof javafx.scene.paint.Color) {
                    blockColorObj = (javafx.scene.paint.Color) blockPaint;
                } else {
                    blockColorObj = javafx.scene.paint.Color.WHITE;
                }
                
                double blockCenterX = col;
                double blockCenterY = gridY - 2;
                
                int particleCount = 10;
                
                for (int i = 0; i < particleCount; i++) {
                    double velocityX = -20 + Math.random() * 40;
                    double velocityY = -0.5 + Math.random() * 1.0;
                    
                    double particleX = blockCenterX + (Math.random() - 0.5) * 0.3;
                    double particleY = blockCenterY + (Math.random() - 0.5) * 0.1;
                    
                    activeShards.add(new Shard(particleX, particleY, velocityX, velocityY, blockColorObj));
                }
            }
        }
    }
    
    /**
     * Returns the collection of currently active shard particles.
     *
     * @return list of active {@link Shard} instances
     */
    public List<Shard> getActiveShards() {
        return activeShards;
    }
    
    /**
     * Returns the collection of currently active white flash effects.
     *
     * @return list of active {@link WhiteFlash} instances
     */
    public List<WhiteFlash> getActiveFlashes() {
        return activeFlashes;
    }
    
    /**
     * Advances all flash effects by the specified time step, removing any that
     * have completed.
     *
     * @param deltaTime time elapsed since the last update in seconds
     */
    public void updateFlashes(double deltaTime) {
        activeFlashes.removeIf(flash -> !flash.update(deltaTime));
    }
    
    /**
     * Advances all shard particles by the specified time step, removing any that
     * have completed.
     *
     * @param deltaTime time elapsed since the last update in seconds
     */
    public void updateShards(double deltaTime) {
        activeShards.removeIf(shard -> !shard.update(deltaTime));
    }
    
    /**
     * Clears all currently active shard and flash effects.
     */
    public void clearAllShards() {
        activeShards.clear();
        activeFlashes.clear();
    }
}
