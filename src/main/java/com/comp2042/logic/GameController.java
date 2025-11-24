package com.comp2042.logic;

import com.comp2042.event.EventSource;
import com.comp2042.event.InputEventListener;
import com.comp2042.event.MoveEvent;
import com.comp2042.ui.GuiController;

public class GameController implements InputEventListener {

    private Board board;

    private final GuiController viewGuiController;
    private final HighScoreManager highScoreManager = new HighScoreManager();
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

    @Override
    public DownData onDownEvent(MoveEvent event) {
        boolean canMove = board.moveBrickDown();
        ClearRow clearRow = null;
        int scoreBonus = 0;

        if (!canMove) {
            board.mergeBrickToBackground();
            clearRow = board.clearRows();

            if (clearRow.getLinesRemoved() > 0) {
                scoreBonus = LINE_SCORE_MULTIPLIER * clearRow.getLinesRemoved() * clearRow.getLinesRemoved();
                board.getScore().add(scoreBonus);
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
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
    }
}
