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
    private GameOverPanel gameOverPanel;

    @FXML
    private Group groupPauseMenu;

    @FXML
    private PauseMenuPanel pauseMenuPanel;

    @FXML
    private Label highScoreValue;

    @FXML
    private Label scoreValue;

    @FXML
    private Label levelValue;

    @FXML
    private Label blocksRemainingValue;

    @FXML
    private StackPane rootStackPane;

    @FXML
    private ImageView backgroundImageView;

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

        // Set up pause menu button handlers
        pauseMenuPanel.setOnResume(event -> resumeGame());
        pauseMenuPanel.setOnQuit(event -> exitGame());

        // Set up responsive background image
        setupBackgroundImage();

        final Reflection reflection = new Reflection();
        reflection.setFraction(0.8);
        reflection.setTopOpacity(0.9);
        reflection.setTopOffset(-12);


        mainMenu.setVisible(true);

        isPause.setValue(Boolean.TRUE);
        gamePanel.setOpacity(0.5);


        HighScoreManager hsManager = new HighScoreManager();
        int currentHigh = hsManager.loadHighScore();
        highScoreValue.setText("High Score: " + currentHigh);
    }

    public void initGameView(int[][] boardMatrix, ViewData brick) {
        // Clear existing rectangles if they exist
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
                displayMatrix[i][j] = rectangle;
                gamePanel.add(rectangle, j, i - 2);
            }
        }

        rectangles = new Rectangle[brick.getBrickData().length][brick.getBrickData()[0].length];
        for (int i = 0; i < brick.getBrickData().length; i++) {
            for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(getFillColor(brick.getBrickData()[i][j]));
                rectangles[i][j] = rectangle;
                brickPanel.add(rectangle, j, i);
            }
        }
        brickPanel.setLayoutX(gamePanel.getLayoutX() + brick.getxPosition() * brickPanel.getVgap() + brick.getxPosition() * BRICK_SIZE);
        brickPanel.setLayoutY(-42 + gamePanel.getLayoutY() + brick.getyPosition() * brickPanel.getHgap() + brick.getyPosition() * BRICK_SIZE);


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

    public void bindScore(IntegerProperty integerProperty) {
        scoreValue.textProperty().bind(integerProperty.asString("Score: %d"));
    }
    
    public void bindLevelInfo(com.comp2042.logic.LevelManager levelManager) {
        if (levelValue != null && blocksRemainingValue != null) {
            levelValue.setVisible(true);
            blocksRemainingValue.setVisible(true);
            levelValue.textProperty().bind(levelManager.currentLevelProperty().asString("Level: %d"));
            
            // Bind blocks remaining and score requirement
            blocksRemainingValue.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                    () -> {
                        int blocksLeft = levelManager.getBlocksRemaining();
                        int scoreReq = levelManager.getScoreRequired();
                        return "Blocks Left: " + blocksLeft + " | Score Need: " + scoreReq;
                    },
                    levelManager.blocksPlacedProperty(),
                    levelManager.blocksRequiredProperty(),
                    levelManager.scoreRequiredProperty()
                )
            );
        }
    }
    
    public void levelComplete(int newLevel) {
        // Show level complete notification
        NotificationPanel notificationPanel = new NotificationPanel("LEVEL " + (newLevel - 1) + " COMPLETE! LEVEL " + newLevel + " START!");
        groupNotification.getChildren().add(notificationPanel);
        notificationPanel.showScore(groupNotification.getChildren());
        
        // Update timeline speed for new level
        if (eventListener != null && eventListener instanceof GameController) {
            GameController gc = (GameController) eventListener;
            updateTimelineSpeed(gc.getSpeedDelay());
        }
    }
    
    public void allLevelsComplete() {
        // Show all levels complete notification
        NotificationPanel notificationPanel = new NotificationPanel("ALL 5 LEVELS COMPLETE! YOU WIN!");
        groupNotification.getChildren().add(notificationPanel);
        notificationPanel.showScore(groupNotification.getChildren());
        
        // Stop the game or allow restart
        if (timeLine != null) {
            timeLine.pause();
        }
    }
    
    public void levelFailed(int level, int currentScore, int requiredScore) {
        // Game over - level failed
        if (timeLine != null) {
            timeLine.stop();
        }
        gameOverPanel.setVisible(true);
        isGameOver.setValue(Boolean.TRUE);
        
        // Show failure message
        NotificationPanel notificationPanel = new NotificationPanel("LEVEL " + level + " FAILED! Score: " + currentScore + "/" + requiredScore);
        groupNotification.getChildren().add(notificationPanel);
        notificationPanel.showScore(groupNotification.getChildren());
    }

    public void gameOver() {
        timeLine.stop();
        gameOverPanel.setVisible(true);
        isGameOver.setValue(Boolean.TRUE);
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
            return; // Don't pause if game is over
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
        mainMenu.setVisible(false);
        groupPauseMenu.setVisible(false);
        if (levelValue != null) levelValue.setVisible(false);
        if (blocksRemainingValue != null) blocksRemainingValue.setVisible(false);
        gamePanel.requestFocus();
        timeLine.play();
        isPause.setValue(Boolean.FALSE);
        gamePanel.setOpacity(1.0);
    }
    
    @FXML
    public void startLevelGame() {
        mainMenu.setVisible(false);
        groupPauseMenu.setVisible(false);
        gameOverPanel.setVisible(false);
        if (levelValue != null) levelValue.setVisible(true);
        if (blocksRemainingValue != null) blocksRemainingValue.setVisible(true);
        
        // Stop existing timeline
        if (timeLine != null) {
            timeLine.stop();
        }
        
        // Reinitialize game in level mode
        Board board = new SimpleBoard(25, 13);
        GameController newGameController = new GameController(this, board);
        setEventListener(newGameController);
        newGameController.initLevelGame();
        
        isPause.setValue(Boolean.FALSE);
        isGameOver.setValue(Boolean.FALSE);
        gamePanel.setOpacity(1.0);
        gamePanel.requestFocus();
    }

    @FXML
    public void exitGame() {
        System.exit(0);
    }

    private void setupBackgroundImage() {
        // Bind background image size to StackPane size so it scales with window
        if (backgroundImageView != null && rootStackPane != null) {
            backgroundImageView.fitWidthProperty().bind(rootStackPane.widthProperty());
            backgroundImageView.fitHeightProperty().bind(rootStackPane.heightProperty());
        }
    }
}
