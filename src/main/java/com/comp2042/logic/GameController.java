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
}
