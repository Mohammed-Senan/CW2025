package com.comp2042.logic;

import com.comp2042.event.EventSource;
import com.comp2042.event.InputEventListener;
import com.comp2042.event.MoveEvent;
import com.comp2042.ui.GuiController;

public class GameController implements InputEventListener {

    private Board board;

    private final GuiController viewGuiController;
    private final HighScoreManager highScoreManager = new HighScoreManager();
    private LevelManager levelManager;
    private boolean levelMode = false;
    private static final int SOFT_DROP_SCORE = 1;
    private static final int LINE_SCORE_MULTIPLIER = 50;
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
            
            // Track block placement in level mode
            if (levelMode && levelManager != null) {
                levelManager.incrementBlocksPlaced();
                int currentScore = board.getScore().scoreProperty().get();
                
                // Check if level failed (placed all blocks but didn't reach score)
                if (levelManager.checkLevelFailed(currentScore)) {
                    viewGuiController.levelFailed(levelManager.getCurrentLevel(), currentScore, levelManager.getScoreRequired());
                    return new DownData(clearRow, board.getViewData(), scoreBonus);
                }
                
                // Check if level is complete (score reached target AND blocks placed)
                if (currentScore >= levelManager.getScoreRequired() && levelManager.getBlocksPlaced() >= levelManager.getBlocksRequired()) {
                    // Unlock next level
                    levelManager.unlockNextLevel();
                    
                    int completedLevel = levelManager.getCurrentLevel();
                    // Notify GUI to pause and show completion screen
                    viewGuiController.levelWon(completedLevel, levelManager.isMaxLevel());
                    return new DownData(clearRow, board.getViewData(), scoreBonus);
                }
            }
            
            clearRow = board.clearRows();

            if (clearRow != null && clearRow.getLinesRemoved() > 0) {
                scoreBonus = LINE_SCORE_MULTIPLIER * clearRow.getLinesRemoved() * clearRow.getLinesRemoved();
                board.getScore().add(scoreBonus);
                
                // Check level completion after score update (in level mode)
                if (levelMode && levelManager != null) {
                    int currentScore = board.getScore().scoreProperty().get();
                    if (currentScore >= levelManager.getScoreRequired() && levelManager.getBlocksPlaced() >= levelManager.getBlocksRequired()) {
                        levelManager.unlockNextLevel();
                        int completedLevel = levelManager.getCurrentLevel();
                        viewGuiController.levelWon(completedLevel, levelManager.isMaxLevel());
                        return new DownData(clearRow, board.getViewData(), scoreBonus);
                    }
                }
            }

            if (board.createNewBrick()) {
                viewGuiController.gameOver();
                highScoreManager.saveHighScore(board.getScore().scoreProperty().get());
            } else {
                viewGuiController.refreshGameBackground(board.getBoardMatrix());
            }

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
}
