package com.comp2042.ui;

import com.comp2042.event.EventSource;
import com.comp2042.event.EventType;
import com.comp2042.event.InputEventListener;
import com.comp2042.event.MoveEvent;
import com.comp2042.logic.DownData;
import com.comp2042.logic.ViewData;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.layout.StackPane;
import com.comp2042.logic.HighScoreManager;
import com.comp2042.logic.GameController;
import com.comp2042.logic.Board;
import com.comp2042.logic.SimpleBoard;
import javafx.scene.paint.ImagePattern;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.control.Button;
import com.comp2042.ui.LevelSelectionPanel;
import com.comp2042.ui.LevelCompletePanel;

public class GuiController implements Initializable {

    private static final int BRICK_SIZE = 20;

    @FXML
    private GridPane gamePanel;

    @FXML
    private Group groupNotification;

    @FXML
    private StackPane mainMenu;

    @FXML
    private GridPane brickPanel;

    @FXML
    private GridPane nextBlockPanel;

    @FXML
    private javafx.scene.layout.BorderPane gameBoard;

    @FXML
    private GameOverPanel gameOverPanel;

    @FXML
    private Group groupPauseMenu;

    @FXML
    private PauseMenuPanel pauseMenuPanel;

    @FXML
    private Group groupLevelSelection;

    @FXML
    private LevelSelectionPanel levelSelectionPanel;

    @FXML
    private Group groupLevelComplete;

    @FXML
    private Label highScoreValue;

    @FXML
    private Label scoreValue;

    @FXML
    private Label levelValue;

    @FXML
    private Label blocksRemainingValue;

    @FXML
    private Label scoreNeedValue;

    @FXML
    private StackPane rootStackPane;

    @FXML
    private VBox hudPanel;

    private Rectangle[][] displayMatrix;

    private InputEventListener eventListener;

    private Rectangle[][] rectangles;

    private Timeline timeLine;

    private final BooleanProperty isPause = new SimpleBooleanProperty();

    private final BooleanProperty isGameOver = new SimpleBooleanProperty();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Font.loadFont(getClass().getClassLoader().getResource("digital.ttf").toExternalForm(), 38);
        gamePanel.setFocusTraversable(true);
        gamePanel.requestFocus();
        gamePanel.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.P) {
                    if (isPause.getValue() == Boolean.FALSE) {
                        pauseGame();
                    } else {
                        resumeGame();
                    }
                    return;
                }

                if (isPause.getValue() == Boolean.TRUE) {
                    return;
                }
                if (isPause.getValue() == Boolean.FALSE && isGameOver.getValue() == Boolean.FALSE) {
                    if (keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.A) {
                        refreshBrick(eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.D) {
                        refreshBrick(eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.W) {
                        refreshBrick(eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.S) {
                        moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
                        keyEvent.consume();
                    }
                }
                if (keyEvent.getCode() == KeyCode.N) {
                    newGame(null);
                }
            }
        });
        gameOverPanel.setVisible(false);
        groupPauseMenu.setVisible(false);
        groupLevelSelection.setVisible(false);
        groupLevelComplete.setVisible(false);
        if (gameBoard != null) {
            gameBoard.setVisible(false);
        }
        if (gamePanel != null) {
            gamePanel.setVisible(false);
        }
        if (brickPanel != null) {
            brickPanel.setVisible(false);
        }

        pauseMenuPanel.setOnResume(event -> resumeGame());
        pauseMenuPanel.setOnBackToMenu(event -> {
            cleanupGame();
            mainMenu.setVisible(true);
        });
        pauseMenuPanel.setOnQuit(event -> exitGame());
        
        if (levelSelectionPanel != null) {
            levelSelectionPanel.setOnLevelSelected(event -> {
                if (event.getSource() instanceof Button) {
                    Button source = (Button) event.getSource();
                    Object userData = source.getUserData();
                    if (userData instanceof Integer) {
                        int levelId = (Integer) userData;
                        startLevelGame(levelId);
                    } else {
                        try {
                            int levelId = Integer.parseInt(source.getText());
                            startLevelGame(levelId);
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            });
            levelSelectionPanel.setOnBackToMenu(event -> {
                groupLevelSelection.setVisible(false);
                mainMenu.setVisible(true);
                if (gameBoard != null) {
                    gameBoard.setVisible(false);
                }
                if (gamePanel != null) {
                    gamePanel.setVisible(false);
                }
                if (brickPanel != null) {
                    brickPanel.setVisible(false);
                }
            });
        }

        final Reflection reflection = new Reflection();
        reflection.setFraction(0.8);
        reflection.setTopOpacity(0.9);
        reflection.setTopOffset(-12);


        mainMenu.setVisible(true);

        isPause.setValue(Boolean.TRUE);
        gamePanel.setOpacity(0.5);

        HighScoreManager hsManager = new HighScoreManager();
        int currentHigh = hsManager.loadHighScore();
        highScoreValue.setText(String.valueOf(currentHigh));
    }

    public void initGameView(int[][] boardMatrix, ViewData brick) {
        if (displayMatrix != null) {
            gamePanel.getChildren().clear();
        }
        if (rectangles != null) {
            brickPanel.getChildren().clear();
        }
        
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];
        for (int i = 2; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(Color.TRANSPARENT);
                // CRITICAL: Explicitly remove any effects to ensure flat, matte blocks
                rectangle.setEffect(null);
                displayMatrix[i][j] = rectangle;
                gamePanel.add(rectangle, j, i - 2);
            }
        }

        rectangles = new Rectangle[brick.getBrickData().length][brick.getBrickData()[0].length];
        for (int i = 0; i < brick.getBrickData().length; i++) {
            for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(getFillColor(brick.getBrickData()[i][j]));
                // CRITICAL: Explicitly remove any effects to ensure flat, matte blocks
                rectangle.setEffect(null);
                rectangles[i][j] = rectangle;
                brickPanel.add(rectangle, j, i);
            }
        }
        brickPanel.setLayoutX(gamePanel.getLayoutX() + brick.getxPosition() * brickPanel.getVgap() + brick.getxPosition() * BRICK_SIZE);
        brickPanel.setLayoutY(-42 + gamePanel.getLayoutY() + brick.getyPosition() * brickPanel.getHgap() + brick.getyPosition() * BRICK_SIZE);

        // Initialize next block display
        updateNextBlock(brick.getNextBrickData());

        updateTimelineSpeed(400);
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();
    }
    
    private void updateTimelineSpeed(int baseDelay) {
        if (timeLine != null) {
            timeLine.stop();
        }
        int delay = baseDelay;
        if (eventListener != null && eventListener instanceof GameController) {
            GameController gc = (GameController) eventListener;
            if (gc.isLevelMode()) {
                delay = gc.getSpeedDelay();
            }
        }
        timeLine = new Timeline(new KeyFrame(
                Duration.millis(delay),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        if (isPause.getValue() == Boolean.FALSE) {
            timeLine.play();
        }
    }

    private Paint getFillColor(int i) {
        Paint returnPaint;
        switch (i) {
            case 0:
                returnPaint = Color.TRANSPARENT;
                break;
            case 1:
                returnPaint = Color.AQUA;
                break;
            case 2:
                returnPaint = Color.BLUEVIOLET;
                break;
            case 3:
                returnPaint = Color.DARKGREEN;
                break;
            case 4:
                returnPaint = Color.YELLOW;
                break;
            case 5:
                returnPaint = Color.RED;
                break;
            case 6:
                returnPaint = Color.BEIGE;
                break;
            case 7:
                returnPaint = Color.BURLYWOOD;
                break;
            default:
                returnPaint = Color.WHITE;
                break;
        }
        return returnPaint;
    }


    private void refreshBrick(ViewData brick) {
        if (isPause.getValue() == Boolean.FALSE) {
            brickPanel.setLayoutX(gamePanel.getLayoutX() + brick.getxPosition() * brickPanel.getVgap() + brick.getxPosition() * BRICK_SIZE);
            brickPanel.setLayoutY(-42 + gamePanel.getLayoutY() + brick.getyPosition() * brickPanel.getHgap() + brick.getyPosition() * BRICK_SIZE);
            for (int i = 0; i < brick.getBrickData().length; i++) {
                for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                    setRectangleData(brick.getBrickData()[i][j], rectangles[i][j]);
                }
            }
            // Update next block display
            updateNextBlock(brick.getNextBrickData());
        }
    }

    private void updateNextBlock(int[][] nextBrickData) {
        if (nextBlockPanel == null || nextBrickData == null) {
            return;
        }
        
        // Clear existing rectangles
        nextBlockPanel.getChildren().clear();
        
        // Calculate center offset for better display
        int maxWidth = 0;
        int maxHeight = nextBrickData.length;
        for (int[] row : nextBrickData) {
            maxWidth = Math.max(maxWidth, row.length);
        }
        
        // Create rectangles for the next block
        int blockSize = 15; // Slightly smaller for preview
        for (int i = 0; i < nextBrickData.length; i++) {
            for (int j = 0; j < nextBrickData[i].length; j++) {
                if (nextBrickData[i][j] != 0) {
                    Rectangle rectangle = new Rectangle(blockSize, blockSize);
                    rectangle.setFill(getFillColor(nextBrickData[i][j]));
                    rectangle.setEffect(null);
                    rectangle.setArcHeight(5);
                    rectangle.setArcWidth(5);
                    // Center the block in the grid
                    int offsetX = (maxWidth - nextBrickData[i].length) / 2;
                    nextBlockPanel.add(rectangle, j + offsetX, i);
                }
            }
        }
    }

    public void refreshGameBackground(int[][] board) {
        for (int i = 2; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                setRectangleData(board[i][j], displayMatrix[i][j]);
            }
        }
    }

    private void setRectangleData(int color, Rectangle rectangle) {
        rectangle.setFill(getFillColor(color));
        rectangle.setArcHeight(9);
        rectangle.setArcWidth(9);
        // CRITICAL: Explicitly remove any effects to ensure flat, matte blocks
        rectangle.setEffect(null);
    }

    private void moveDown(MoveEvent event) {
        if (isPause.getValue() == Boolean.FALSE) {
            DownData downData = eventListener.onDownEvent(event);
            if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                NotificationPanel notificationPanel = new NotificationPanel("+" + downData.getScoreBonus());
                groupNotification.getChildren().add(notificationPanel);
                notificationPanel.showScore(groupNotification.getChildren());
            }
            refreshBrick(downData.getViewData());
        }
        gamePanel.requestFocus();
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }
    
    private void cleanupGame() {
        if (timeLine != null) {
            timeLine.stop();
        }
        
        if (eventListener != null) {
            eventListener.createNewGame();
        }
        
        isPause.setValue(Boolean.FALSE);
        isGameOver.setValue(Boolean.FALSE);
        
        if (groupNotification != null) {
            groupNotification.setVisible(false);
        }
        gameOverPanel.setVisible(false);
        groupPauseMenu.setVisible(false);
        groupLevelComplete.setVisible(false);
        
        if (gameBoard != null) {
            gameBoard.setVisible(false);
        }
        if (gamePanel != null) {
            gamePanel.setVisible(false);
        }
        if (brickPanel != null) {
            brickPanel.setVisible(false);
        }
        
        if (scoreValue != null) {
            scoreValue.setVisible(false);
        }
        if (highScoreValue != null) {
            highScoreValue.setVisible(false);
        }
        if (levelValue != null) {
            levelValue.setVisible(false);
        }
        if (blocksRemainingValue != null) {
            blocksRemainingValue.setVisible(false);
        }
        if (scoreNeedValue != null) {
            scoreNeedValue.setVisible(false);
        }
        
        if (groupNotification != null) {
            groupNotification.getChildren().clear();
        }
    }

    public void bindScore(IntegerProperty integerProperty) {
        scoreValue.textProperty().bind(integerProperty.asString("Score: %d"));
    }
    
    public void bindLevelInfo(com.comp2042.logic.LevelManager levelManager) {
        if (levelValue != null && blocksRemainingValue != null && scoreNeedValue != null) {
            levelValue.setVisible(true);
            blocksRemainingValue.setVisible(true);
            scoreNeedValue.setVisible(true);
            levelValue.textProperty().bind(levelManager.currentLevelProperty().asString("%d"));
            
            blocksRemainingValue.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                    () -> {
                        int blocksLeft = levelManager.getBlocksRemaining();
                        return String.valueOf(blocksLeft);
                    },
                    levelManager.blocksPlacedProperty(),
                    levelManager.blocksRequiredProperty()
                )
            );
            
            scoreNeedValue.textProperty().bind(
                levelManager.scoreRequiredProperty().asString("%d")
            );
        }
    }
    
    public void levelWon(int levelCompleted, boolean isLastLevel) {
        if (timeLine != null) {
            timeLine.stop();
        }
        isPause.setValue(Boolean.TRUE);
        
        groupLevelComplete.setVisible(true);
        groupLevelComplete.getChildren().clear();
        
        LevelCompletePanel newPanel = new LevelCompletePanel(levelCompleted, isLastLevel);
        if (!isLastLevel) {
            newPanel.setOnNextLevel(event -> {
                int nextLevel = levelCompleted + 1;
                startLevelGame(nextLevel);
            });
        }
        newPanel.setOnMenu(event -> {
            groupLevelComplete.setVisible(false);
            mainMenu.setVisible(true);
            if (levelSelectionPanel != null) {
                levelSelectionPanel.refreshLevels();
            }
        });
        VBox container = new VBox();
        container.setAlignment(javafx.geometry.Pos.CENTER);
        container.getChildren().add(newPanel);
        groupLevelComplete.getChildren().add(container);
    }
    
    public void levelComplete(int levelCompleted) {
        levelWon(levelCompleted, false);
    }
    
    public void allLevelsComplete() {
        levelWon(10, true);
    }
    
    public void levelFailed(int level, int currentScore, int requiredScore) {
        gameOver();
        
        NotificationPanel notificationPanel = new NotificationPanel("LEVEL " + level + " FAILED! Score: " + currentScore + "/" + requiredScore);
        groupNotification.getChildren().add(notificationPanel);
        notificationPanel.showScore(groupNotification.getChildren());
    }

    public void gameOver() {
        if (timeLine != null) {
            timeLine.stop();
        }
        isPause.setValue(Boolean.TRUE);
        isGameOver.setValue(Boolean.TRUE);
        
        if (groupLevelComplete != null) {
            groupLevelComplete.setVisible(false);
        }
        if (groupLevelSelection != null) {
            groupLevelSelection.setVisible(false);
        }
        if (groupPauseMenu != null) {
            groupPauseMenu.setVisible(false);
        }
        
        if (groupNotification != null) {
            groupNotification.setVisible(true);
            if (rootStackPane != null && rootStackPane.getChildren().contains(groupNotification)) {
                rootStackPane.getChildren().remove(groupNotification);
                rootStackPane.getChildren().add(groupNotification);
            }
            groupNotification.toFront();
        }
        if (gameOverPanel != null) {
            gameOverPanel.setVisible(true);
            gameOverPanel.toFront();
        }
        
        if (rootStackPane != null && gameOverPanel != null) {
            rootStackPane.getChildren().remove(gameOverPanel);
            rootStackPane.getChildren().add(gameOverPanel);
        }
        
        gamePanel.requestFocus();
        
        gameOverPanel.setOnRestart(event -> {
            if (eventListener != null && eventListener instanceof GameController) {
                GameController gc = (GameController) eventListener;
                if (gc.isLevelMode() && gc.getLevelManager() != null) {
                    int currentLevel = gc.getLevelManager().getCurrentLevel();
                    startLevelGame(currentLevel);
                } else {
                    newGame(null);
                }
            } else {
                newGame(null);
            }
        });
        
        gameOverPanel.setOnBackToMenu(event -> {
            cleanupGame();
            if (groupNotification != null) {
                groupNotification.setVisible(false);
            }
            gameOverPanel.setVisible(false);
            mainMenu.setVisible(true);
            if (levelSelectionPanel != null) {
                levelSelectionPanel.refreshLevels();
            }
        });
    }

    public void newGame(ActionEvent actionEvent) {
        timeLine.stop();
        gameOverPanel.setVisible(false);
        groupPauseMenu.setVisible(false);
        eventListener.createNewGame();
        gamePanel.requestFocus();
        timeLine.play();
        isPause.setValue(Boolean.FALSE);
        isGameOver.setValue(Boolean.FALSE);
        gamePanel.setOpacity(1.0);
    }

    public void pauseGame() {
        if (isGameOver.getValue() == Boolean.TRUE) {
            return;
        }
        timeLine.pause();
        isPause.setValue(Boolean.TRUE);
        gamePanel.setOpacity(0.5);
        groupPauseMenu.setVisible(true);
        gamePanel.requestFocus();
    }

    public void resumeGame() {
        timeLine.play();
        isPause.setValue(Boolean.FALSE);
        gamePanel.setOpacity(1.0);
        groupPauseMenu.setVisible(false);
        gamePanel.requestFocus();
    }

    @FXML
    public void startGame() {
        cleanupGame();
        
        mainMenu.setVisible(false);
        groupPauseMenu.setVisible(false);
        groupLevelSelection.setVisible(false);
        groupLevelComplete.setVisible(false);
        
        if (groupNotification != null) {
            groupNotification.setVisible(false);
            groupNotification.getChildren().clear();
        }
        if (gameOverPanel != null) {
            gameOverPanel.setVisible(false);
        }
        
        isPause.setValue(Boolean.FALSE);
        isGameOver.setValue(Boolean.FALSE);
        
        if (gameBoard != null) {
            gameBoard.setVisible(true);
        }
        if (gamePanel != null) {
            gamePanel.setVisible(true);
        }
        if (brickPanel != null) {
            brickPanel.setVisible(true);
        }
        if (scoreValue != null) {
            scoreValue.setVisible(true);
        }
        if (highScoreValue != null) {
            highScoreValue.setVisible(true);
        }
        if (levelValue != null) {
            levelValue.setVisible(false);
        }
        if (blocksRemainingValue != null) {
            blocksRemainingValue.setVisible(false);
        }
        if (scoreNeedValue != null) {
            scoreNeedValue.setVisible(false);
        }
        gamePanel.requestFocus();
        if (timeLine != null) {
            timeLine.play();
        }
        isPause.setValue(Boolean.FALSE);
        gamePanel.setOpacity(1.0);
    }
    
    @FXML
    public void showLevelSelection() {
        mainMenu.setVisible(false);
        groupLevelSelection.setVisible(true);
        if (gameBoard != null) {
            gameBoard.setVisible(false);
        }
        if (gamePanel != null) {
            gamePanel.setVisible(false);
        }
        if (brickPanel != null) {
            brickPanel.setVisible(false);
        }
        if (levelSelectionPanel != null) {
            levelSelectionPanel.refreshLevels();
        }
    }
    
    public void startLevelGame(int levelId) {
        cleanupGame();
        
        mainMenu.setVisible(false);
        groupLevelSelection.setVisible(false);
        groupPauseMenu.setVisible(false);
        groupLevelComplete.setVisible(false);
        
        if (groupNotification != null) {
            groupNotification.setVisible(false);
            groupNotification.getChildren().clear();
        }
        if (gameOverPanel != null) {
            gameOverPanel.setVisible(false);
        }
        
        isPause.setValue(Boolean.FALSE);
        isGameOver.setValue(Boolean.FALSE);
        
        if (gameBoard != null) {
            gameBoard.setVisible(true);
        }
        if (gamePanel != null) {
            gamePanel.setVisible(true);
        }
        if (brickPanel != null) {
            brickPanel.setVisible(true);
        }
        if (scoreValue != null) {
            scoreValue.setVisible(true);
        }
        if (highScoreValue != null) {
            highScoreValue.setVisible(true);
        }
        if (levelValue != null) {
            levelValue.setVisible(true);
        }
        if (blocksRemainingValue != null) {
            blocksRemainingValue.setVisible(true);
        }
        if (scoreNeedValue != null) {
            scoreNeedValue.setVisible(true);
        }
        
        if (timeLine != null) {
            timeLine.stop();
        }
        
        Board board = new SimpleBoard(25, 13);
        GameController newGameController = new GameController(this, board);
        setEventListener(newGameController);
        newGameController.initLevelGame(levelId);
        
        isPause.setValue(Boolean.FALSE);
        isGameOver.setValue(Boolean.FALSE);
        gamePanel.setOpacity(1.0);
        gamePanel.requestFocus();
    }
    
    @FXML
    public void startLevelGame() {
        startLevelGame(1);
    }

    @FXML
    public void exitGame() {
        System.exit(0);
    }

}
