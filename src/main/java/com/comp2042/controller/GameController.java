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
    
    public static class WhiteFlash {
        private int gridY;
        private double life;
        private double maxLife;
        
        public WhiteFlash(int gridY) {
            this.gridY = gridY;
            this.life = 1.0;
            this.maxLife = 0.1;
        }
        
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
        
        public double getX() { return x; }
        public double getY() { return y; }
        public javafx.scene.paint.Color getStartColor() { return startColor; }
        public javafx.scene.paint.Color getEndColor() { return endColor; }
        public double getLife() { return life; }
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
    
    public List<Shard> getActiveShards() {
        return activeShards;
    }
    
    public List<WhiteFlash> getActiveFlashes() {
        return activeFlashes;
    }
    
    public void updateFlashes(double deltaTime) {
        activeFlashes.removeIf(flash -> !flash.update(deltaTime));
    }
    
    public void updateShards(double deltaTime) {
        activeShards.removeIf(shard -> !shard.update(deltaTime));
    }
    
    public void clearAllShards() {
        activeShards.clear();
        activeFlashes.clear();
    }
}
