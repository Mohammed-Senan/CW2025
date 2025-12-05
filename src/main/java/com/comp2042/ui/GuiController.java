package com.comp2042.ui;

import com.comp2042.event.EventSource;
import com.comp2042.event.EventType;
import com.comp2042.event.InputEventListener;
import com.comp2042.event.MoveEvent;
import com.comp2042.logic.DownData;
import com.comp2042.logic.ViewData;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
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
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.BlurType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.control.Slider;
import com.comp2042.logic.HighScoreManager;
import com.comp2042.logic.GameController;
import com.comp2042.logic.Board;
import com.comp2042.logic.SimpleBoard;
import com.comp2042.logic.MatrixOperations;
import com.comp2042.logic.GameConfig;
import javafx.scene.paint.ImagePattern;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import com.comp2042.ui.LevelSelectionPanel;
import com.comp2042.ui.LevelCompletePanel;
import com.comp2042.ui.SettingsPanel;

public class GuiController implements Initializable {

    private static final int BRICK_SIZE = 20;

    /**
     * Unified coordinate conversion: Converts grid coordinates to pixel coordinates.
     * This method ensures that both active blocks and ghost blocks use the EXACT same
     * formula for positioning, guaranteeing perfect alignment.
     * 
     * @param gridX The X grid coordinate (column index)
     * @param gridY The Y grid coordinate (row index)
     * @return A Point2D containing the pixel X and Y coordinates
     */
    private javafx.geometry.Point2D gridToPixel(int gridX, int gridY) {
        if (brickPanel == null || gamePanel == null) {
            return new javafx.geometry.Point2D(0, 0);
        }
        // CRITICAL: Use the exact same formula for both X and Y conversion
        // This ensures that if ghostX == activeX, they will be pixel-perfectly aligned
        double pixelX = gamePanel.getLayoutX() + gridX * brickPanel.getVgap() + gridX * BRICK_SIZE;
        double pixelY = -42 + gamePanel.getLayoutY() + gridY * brickPanel.getHgap() + gridY * BRICK_SIZE;
        return new javafx.geometry.Point2D(pixelX, pixelY);
    }

    /**
     * Positions a GridPane (brickPanel or ghostPanel) at the specified grid coordinates.
     * Uses the unified gridToPixel() method to ensure perfect alignment.
     * 
     * @param panel The GridPane to position (brickPanel or ghostPanel)
     * @param gridX The X grid coordinate (column index)
     * @param gridY The Y grid coordinate (row index)
     */
    private void positionPanelAtGrid(GridPane panel, int gridX, int gridY) {
        if (panel == null) {
            return;
        }
        javafx.geometry.Point2D pixelPos = gridToPixel(gridX, gridY);
        panel.setLayoutX(pixelPos.getX());
        panel.setLayoutY(pixelPos.getY());
    }

    @FXML
    private GridPane gamePanel;

    @FXML
    private Group groupNotification;

    @FXML
    private StackPane mainMenu;

    @FXML
    private GridPane brickPanel;

    @FXML
    private GridPane ghostPanel;

    @FXML
    private GridPane nextBlockPanel;

    @FXML
    private javafx.scene.layout.Pane laserContainer;

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
    private Group groupSettings;

    @FXML
    private SettingsPanel settingsPanel;

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

    @FXML
    private VBox hudVboxLeft;

    @FXML
    private VBox hudVboxRight;

    @FXML
    private VBox menuContainer;

    @FXML
    private StackPane settingsContainer;

    @FXML
    private StackPane tutorialContainer;

    @FXML
    private HBox ghostModeSwitch;
    
    @FXML
    private StackPane ghostModeTrack;
    
    @FXML
    private Circle ghostModeThumb; // Defined in FXML, centered in StackPane
    
    @FXML
    private Label ghostModeOffLabel;
    
    @FXML
    private Label ghostModeOnLabel;
    
    @FXML
    private Slider musicVolumeSlider;

    private Rectangle[][] displayMatrix;

    private int[][] currentBoardMatrix;

    private InputEventListener eventListener;

    private Rectangle[][] rectangles;

    private Timeline timeLine;
    private Timeline laserTimeline;

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
                    if (keyEvent.getCode() == KeyCode.SPACE) {
                        hardDrop();
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
        if (groupSettings != null) {
            groupSettings.setVisible(false);
        }
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
        pauseMenuPanel.setOnSettings(event -> showPauseSettings());
        pauseMenuPanel.setOnBackToMenu(event -> {
            cleanupGame();
            mainMenu.setVisible(true);
            // Ensure menu container is visible when returning to main menu
            if (menuContainer != null) {
                menuContainer.setVisible(true);
            }
            if (settingsContainer != null) {
                settingsContainer.setVisible(false);
            }
        });
        pauseMenuPanel.setOnQuit(event -> exitGame());
        
        // Wire up settings panel (legacy, for pause menu settings)
        if (settingsPanel != null) {
            settingsPanel.setOnBackToMenu(event -> {
                if (groupSettings != null) {
                    groupSettings.setVisible(false);
                }
                if (mainMenu != null) {
                    mainMenu.setVisible(true);
                }
                // Ensure menu container is visible when returning to main menu
                if (menuContainer != null) {
                    menuContainer.setVisible(true);
                }
                if (settingsContainer != null) {
                    settingsContainer.setVisible(false);
                }
            });
            settingsPanel.initializeGhostMode();
        }
        
        // Wire up main menu ghost mode switch
        if (ghostModeTrack != null && ghostModeThumb != null) {
            // Ensure track has proper alignment for centered circle
            ghostModeTrack.setAlignment(javafx.geometry.Pos.CENTER);
            
            // Set circle properties (already defined in FXML, but ensure they're set)
            ghostModeThumb.setRadius(10);
            ghostModeThumb.setFill(Color.rgb(0, 255, 255)); // Bright cyan
            ghostModeThumb.setEffect(new DropShadow(
                BlurType.THREE_PASS_BOX,
                Color.rgb(0, 255, 255),
                15, 0.8, 0, 0
            ));
            ghostModeThumb.setOpacity(1.0);
            ghostModeThumb.setVisible(true);
            
            // Initialize switch state - this will set the initial position using translateX
            updateGhostModeSwitch();
            
            // Make the track clickable
            ghostModeTrack.setOnMouseClicked(event -> toggleGhostMode());
        }
        
        // Initialize ghost mode switch labels with proper style classes
        if (ghostModeOffLabel != null) {
            ghostModeOffLabel.getStyleClass().add("toggle-off");
        }
        if (ghostModeOnLabel != null) {
            // Default to ON state (enabled by default)
            if (GameConfig.isGhostModeEnabled()) {
                ghostModeOnLabel.getStyleClass().add("toggle-on");
            } else {
                ghostModeOnLabel.getStyleClass().add("toggle-off");
            }
        }
        
        // Wire up music volume slider
        if (musicVolumeSlider != null) {
            musicVolumeSlider.setValue(GameConfig.getMusicVolume());
            musicVolumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                GameConfig.setMusicVolume(newVal.doubleValue());
            });
        }
        
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
                // Ensure menu container is visible when returning to main menu
                if (menuContainer != null) {
                    menuContainer.setVisible(true);
                }
                if (settingsContainer != null) {
                    settingsContainer.setVisible(false);
                }
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
        // Ensure menu container is visible when returning to main menu
        if (menuContainer != null) {
            menuContainer.setVisible(true);
        }
        if (settingsContainer != null) {
            settingsContainer.setVisible(false);
        }

        isPause.setValue(Boolean.TRUE);
        gamePanel.setOpacity(0.5);

        HighScoreManager hsManager = new HighScoreManager();
        int currentHigh = hsManager.loadHighScore();
        highScoreValue.setText(String.valueOf(currentHigh));
        
        initializeShatterSystem();
    }
    
    private void initializeShatterSystem() {
        if (laserContainer == null) {
            return;
        }
        
        laserContainer.getChildren().clear();
        
        laserTimeline = new Timeline(new KeyFrame(Duration.millis(16), ae -> updateAndRenderShards()));
        laserTimeline.setCycleCount(Timeline.INDEFINITE);
        laserTimeline.play();
    }
    
    private void updateAndRenderShards() {
        if (laserContainer == null || isPause.getValue() == Boolean.TRUE) {
            return;
        }
        
        if (!(eventListener instanceof GameController)) {
            return;
        }
        
        GameController gameController = (GameController) eventListener;
        
        double deltaTime = 0.016;
        gameController.updateFlashes(deltaTime);
        gameController.updateShards(deltaTime);
        
        laserContainer.getChildren().clear();
        
        for (GameController.WhiteFlash flash : gameController.getActiveFlashes()) {
            renderWhiteFlash(flash);
        }
        
        for (GameController.Shard shard : gameController.getActiveShards()) {
            renderShard(shard);
        }
    }
    
    private void renderShard(GameController.Shard shard) {
        if (laserContainer == null || gamePanel == null || brickPanel == null) {
            return;
        }
        
        double x = shard.getX();
        double y = shard.getY();
        double life = shard.getLife();
        javafx.scene.paint.Color startColor = shard.getStartColor();
        javafx.scene.paint.Color endColor = shard.getEndColor();
        double opacity = shard.getOpacity();
        
        if (opacity <= 0) {
            return;
        }
        
        int gridCol = (int)Math.floor(x);
        int gridRow = (int)Math.floor(y);
        
        javafx.geometry.Point2D basePixelPos = gridToPixel(gridCol, gridRow);
        
        double offsetX = (x - gridCol) * BRICK_SIZE;
        double offsetY = (y - gridRow) * BRICK_SIZE;
        
        double screenX = basePixelPos.getX() + offsetX + BRICK_SIZE / 2.0;
        double screenY = basePixelPos.getY() + offsetY + BRICK_SIZE / 2.0;
        
        double width = 8.0;
        double height = 2.0;
        
        Rectangle particleRect = new Rectangle(width, height);
        
        double colorInterpolation = 1.0 - life;
        javafx.scene.paint.Color currentColor = startColor.interpolate(endColor, colorInterpolation);
        
        particleRect.setFill(currentColor);
        particleRect.setOpacity(opacity);
        
        DropShadow glow = new DropShadow(
            BlurType.GAUSSIAN,
            currentColor,
            15,
            1.0,
            0,
            0
        );
        particleRect.setEffect(glow);
        
        particleRect.setX(screenX - width / 2.0);
        particleRect.setY(screenY - height / 2.0);
        
        laserContainer.getChildren().add(particleRect);
    }
    
    private void renderWhiteFlash(GameController.WhiteFlash flash) {
        if (laserContainer == null || gamePanel == null || brickPanel == null) {
            return;
        }
        
        int gridY = flash.getGridY();
        double life = flash.getLife();
        
        if (life <= 0) {
            return;
        }
        
        int displayRow = gridY - 2;
        if (displayRow < 0) {
            return;
        }
        
        Board board = null;
        if (eventListener instanceof GameController) {
            board = ((GameController) eventListener).getBoard();
        }
        if (board == null) {
            return;
        }
        
        int[][] boardMatrix = board.getBoardMatrix();
        if (boardMatrix == null || boardMatrix.length == 0) {
            return;
        }
        
        int boardWidth = boardMatrix[0] != null ? boardMatrix[0].length : 0;
        
        javafx.geometry.Point2D leftPixelPos = gridToPixel(0, displayRow);
        javafx.geometry.Point2D rightPixelPos = gridToPixel(boardWidth - 1, displayRow);
        double rowX = leftPixelPos.getX();
        double rowY = leftPixelPos.getY();
        double rowWidth = rightPixelPos.getX() + BRICK_SIZE - rowX;
        double rowHeight = BRICK_SIZE;
        
        Rectangle flashRect = new Rectangle(rowWidth, rowHeight);
        flashRect.setFill(Color.WHITE);
        flashRect.setOpacity(life);
        flashRect.setX(rowX);
        flashRect.setY(rowY);
        
        DropShadow whiteGlow = new DropShadow(
            BlurType.GAUSSIAN,
            Color.WHITE,
            30,
            1.0,
            0,
            0
        );
        flashRect.setEffect(whiteGlow);
        
        laserContainer.getChildren().add(flashRect);
    }

    public void initGameView(int[][] boardMatrix, ViewData brick) {
        if (eventListener instanceof GameController) {
            ((GameController) eventListener).clearAllShards();
        }
        if (laserContainer != null) {
            laserContainer.getChildren().clear();
        }
        currentBoardMatrix = boardMatrix; // Store board matrix for ghost piece calculation
        if (displayMatrix != null) {
            gamePanel.getChildren().clear();
        }
        if (rectangles != null) {
            brickPanel.getChildren().clear();
        }
        if (ghostPanel != null) {
            ghostPanel.getChildren().clear();
            // CRITICAL: Ensure ghost panel has the same gap settings as brickPanel from the start
            // This guarantees perfect alignment between ghost and active blocks
            if (brickPanel != null) {
                ghostPanel.setVgap(brickPanel.getVgap());
                ghostPanel.setHgap(brickPanel.getHgap());
            }
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
        // Use unified positioning method to ensure perfect alignment
        positionPanelAtGrid(brickPanel, brick.getxPosition(), brick.getyPosition());

        // Initialize ghost piece and next block display
        drawGhost(boardMatrix, brick);
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

    public Paint getFillColor(int i) {
        Paint returnPaint;
        switch (i) {
            case 0:
                returnPaint = Color.TRANSPARENT;
                break;
            case 1:
                // Blue/Cyan - Use solid, vibrant cyan
                returnPaint = Color.rgb(0, 255, 255); // Bright cyan
                break;
            case 2:
                // Purple - Use solid, vibrant purple
                returnPaint = Color.rgb(138, 43, 226); // Blue violet
                break;
            case 3:
                // Green - Use solid, vibrant green
                returnPaint = Color.rgb(0, 200, 0); // Bright green
                break;
            case 4:
                // Yellow - Use solid, vibrant yellow
                returnPaint = Color.rgb(255, 255, 0); // Bright yellow
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
            // CRUCIAL: Draw ghost BEFORE the active block so it appears behind it
            // Update ghost piece first - Get the latest board matrix directly from the board to ensure accuracy
            if (eventListener instanceof GameController) {
                GameController gc = (GameController) eventListener;
                Board board = gc.getBoard();
                if (board != null) {
                    int[][] latestBoardMatrix = board.getBoardMatrix();
                    if (latestBoardMatrix != null) {
                        drawGhost(latestBoardMatrix, brick);
                    }
                }
            } else if (currentBoardMatrix != null) {
                // Fallback to stored matrix if not using GameController
                drawGhost(currentBoardMatrix, brick);
            }
            
            // Now update the active block (drawn after ghost, so it appears on top)
            // Use unified positioning method to ensure perfect alignment with ghost
            positionPanelAtGrid(brickPanel, brick.getxPosition(), brick.getyPosition());
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
        currentBoardMatrix = board; // Store board matrix for ghost piece calculation
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

    /**
     * Helper method to calculate the ghost Y position.
     * The Ghost is NOT an object - it's just a calculation.
     * 
     * Algorithm:
     * 1. Start with ghostY = currentBlock.getY()
     * 2. While loop: Check if board.isValidMove(currentBlock.getX(), ghostY + 1, currentBlock.getRotation())
     * 3. Inside loop: ghostY++
     * 4. This simulates dropping the block until it hits the floor or another block
     * 
     * CRITICAL: Uses the exact same X position and rotation as the active block.
     * The collision detection uses transposed indexing, but this method correctly
     * calculates where the block would land using the same logic as the game movement.
     */
    private int getGhostY(int[][] boardMatrix, ViewData currentBlock) {
        if (boardMatrix == null || currentBlock == null || boardMatrix.length == 0) {
            return currentBlock != null ? currentBlock.getyPosition() : 0;
        }
        
        // Get the current block's position and shape (rotation) - MUST use exact same X and rotation
        int currentX = currentBlock.getxPosition();  // Use exact same X - CRITICAL for alignment
        int currentY = currentBlock.getyPosition();
        int[][] brickData = currentBlock.getBrickData(); // Use exact same rotation - CRITICAL for alignment
        
        if (brickData == null || brickData.length == 0) {
            return currentY;
        }
        
        // Start with ghostY = currentBlock.getY()
        int ghostY = currentY;
        
        // While loop: Check if the move is valid (no collision)
        // MatrixOperations.intersect returns true if there's a collision or out of bounds
        // So !intersect means the move is valid
        // CRITICAL: Use the exact same X position (currentX) throughout the calculation
        while (!MatrixOperations.intersect(boardMatrix, brickData, currentX, ghostY + 1)) {
            // Move is valid, increment ghostY
            ghostY++;
            
            // Safety check: prevent infinite loop
            if (ghostY >= boardMatrix.length) {
                break;
            }
        }
        
        // Loop exits when ghostY + 1 would cause a collision or go out of bounds
        // So ghostY is the last valid position (where the block would land)
        // This position is guaranteed to be safe (no collision) and aligned with currentX
        return ghostY;
    }

    /**
     * Draw the ghost piece (shadow piece) showing where the block will land.
     * This method is called in the draw loop every frame.
     * 
     * CRUCIAL: Ghost must be drawn BEFORE the active block so it appears behind it.
     * 
     * The ghost uses:
     * - X: currentBlock.getX() (MUST match active block exactly)
     * - Y: The calculated ghostY from getGhostY()
     * - Shape/Rotation: currentBlock.getShape() (MUST match active block exactly)
     * - Style: Transparent fill or outline style
     */
    private void drawGhost(int[][] boardMatrix, ViewData currentBlock) {
        if (ghostPanel == null || boardMatrix == null || currentBlock == null || brickPanel == null) {
            return;
        }
        
        // Clear existing ghost rectangles
        ghostPanel.getChildren().clear();
        
        // CRITICAL: Ensure ghost panel has the same gap settings as brickPanel for perfect alignment
        ghostPanel.setVgap(brickPanel.getVgap());
        ghostPanel.setHgap(brickPanel.getHgap());
        
        // Get the current block's position and shape (rotation) - MUST match active block exactly
        int currentX = currentBlock.getxPosition();  // X: currentBlock.getX() (MUST match active block)
        int currentY = currentBlock.getyPosition();
        int[][] brickData = currentBlock.getBrickData(); // Shape/Rotation: currentBlock.getShape() (MUST match active block)
        
        if (brickData == null || brickData.length == 0) {
            return;
        }
        
        // Call getGhostY() at the very start of the draw frame to find the drop point
        // CRITICAL: This uses the exact same X position and rotation as the active block
        int ghostY = getGhostY(boardMatrix, currentBlock);
        
        // Only show ghost if:
        // 1. It's different from current position (ghostY > currentY)
        // 2. The position is valid (ghostY >= 0)
        // NOTE: getGhostY() already validates the position using intersect(), so if it returns
        // a valid ghostY, that position is guaranteed to be safe (no collision, within bounds).
        // We trust getGhostY()'s validation rather than double-checking, which could cause
        // false negatives when blocks are rotated near borders.
        // Check if ghost mode is enabled in GameConfig
        if (!GameConfig.isGhostModeEnabled()) {
            return; // Ghost mode disabled, don't draw ghost
        }
        
        // Only show ghost if:
        // 1. It's different from current position (ghostY > currentY)
        // 2. The position is valid (ghostY >= 0)
        boolean isValidGhost = ghostY > currentY && ghostY >= 0;
        
        if (isValidGhost) {
            // Draw the ghost block using the EXACT same shape data and rendering logic as the active block
            // CRITICAL: The collision detection uses transposed indexing (shape[j][i]), but rendering
            // uses normal indexing (brickData[i][j]). We must match the active block rendering exactly.
            // NOTE: The ghost position is already validated by getGhostY() and isGhostPositionSafe,
            // so we can safely render all cells without additional boundary checks.
            // The validation ensures the entire block (at ghostY position) is within bounds.
            for (int i = 0; i < brickData.length; i++) {
                for (int j = 0; j < brickData[i].length; j++) {
                    if (brickData[i][j] != 0) { // Use [i][j] to match active block rendering
                        Rectangle ghostRect = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                        
                        // Style: Use transparent fill or outline style so it looks like a shadow
                        Paint blockColor = getFillColor(brickData[i][j]);
                        if (blockColor instanceof Color) {
                            Color c = (Color) blockColor;
                            // Transparent fill (ghost appears behind active block)
                            Color ghostColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), 0.3);
                            ghostRect.setFill(ghostColor);
                            // Also add a stroke for better visibility
                            ghostRect.setStroke(c.deriveColor(0, 1, 1, 0.5));
                            ghostRect.setStrokeWidth(2.0);
                        } else {
                            ghostRect.setFill(Color.WHITE.deriveColor(0, 1, 1, 0.3));
                            ghostRect.setStroke(Color.WHITE.deriveColor(0, 1, 1, 0.5));
                            ghostRect.setStrokeWidth(2.0);
                        }
                        ghostRect.setEffect(null);
                        ghostRect.setArcHeight(9);
                        ghostRect.setArcWidth(9);
                        
                        // Add rectangle to grid using EXACT same indexing as brickPanel
                        // brickPanel uses (j, i) - column j, row i - to match active block
                        ghostPanel.add(ghostRect, j, i);
                    }
                }
            }
            
            // Position ghost panel using EXACT same calculation as brickPanel
            // CRITICAL: Use the exact same X position as the active block (currentX)
            // The ghost should be at the same X position, only Y changes to ghostY
            // Use unified positioning method to guarantee pixel-perfect alignment
            positionPanelAtGrid(ghostPanel, currentX, ghostY);
        }
    }

    /**
     * Hard drop: instantly drop the block to the EXACT ghost position
     */
    private void hardDrop() {
        if (eventListener == null || currentBoardMatrix == null || rectangles == null) {
            return;
        }
        
        // Get current brick position from event listener
        // We need to get ViewData from the board, not from onDownEvent
        if (eventListener instanceof GameController) {
            GameController gc = (GameController) eventListener;
            Board board = gc.getBoard();
            if (board == null) {
                return;
            }
            ViewData currentBrick = board.getViewData();
            if (currentBrick == null) {
                return;
            }
            
            // Calculate ghost position (this is the EXACT landing position)
            // Use the new getGhostY method which calculates dynamically based on currentBlock
            int ghostY = getGhostY(currentBoardMatrix, currentBrick);
            
            int currentY = currentBrick.getyPosition();
            
            // Drop the block instantly to the EXACT ghost position
            if (ghostY > currentY) {
                // Keep moving down until we reach the exact ghost Y position
                // The ghost Y is the last safe position, so we need to reach it exactly
                while (currentY < ghostY) {
                    int previousY = currentY;
                    DownData downData = eventListener.onDownEvent(new MoveEvent(EventType.DOWN, EventSource.USER));
                    if (downData == null) {
                        break;
                    }
                    ViewData newBrick = downData.getViewData();
                    if (newBrick == null) {
                        break;
                    }
                    int newY = newBrick.getyPosition();
                    
                    // If block can't move down anymore (locked at current position), stop
                    if (newY == previousY) {
                        // Block locked - should be at ghost position if calculation is correct
                        break;
                    }
                    
                    // Update current position
                    currentY = newY;
                    refreshBrick(newBrick);
                    
                    // If block merged (clearRow is not null), we're done
                    if (downData.getClearRow() != null) {
                        break;
                    }
                    
                    // If we've reached the ghost position exactly, we're done
                    if (currentY >= ghostY) {
                        break;
                    }
                }
            }
        }
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
            // Ensure menu container is visible when returning to main menu
            if (menuContainer != null) {
                menuContainer.setVisible(true);
            }
            if (settingsContainer != null) {
                settingsContainer.setVisible(false);
            }
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
            // Ensure menu container is visible when returning to main menu
            if (menuContainer != null) {
                menuContainer.setVisible(true);
            }
            if (settingsContainer != null) {
                settingsContainer.setVisible(false);
            }
            if (levelSelectionPanel != null) {
                levelSelectionPanel.refreshLevels();
            }
        });
    }

    public void newGame(ActionEvent actionEvent) {
        if (eventListener instanceof GameController) {
            ((GameController) eventListener).clearAllShards();
        }
        if (laserContainer != null) {
            laserContainer.getChildren().clear();
        }
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
        // Refresh pause menu (ghost mode is now in settings)
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
        
        // Hide HUD panels (score boxes) when showing level selection
        if (hudVboxLeft != null) {
            hudVboxLeft.setVisible(false);
        }
        if (hudVboxRight != null) {
            hudVboxRight.setVisible(false);
        }
        
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

    @FXML
    public void showSettings() {
        // Hide main menu buttons and show settings container
        if (menuContainer != null) {
            menuContainer.setVisible(false);
        }
        if (tutorialContainer != null) {
            tutorialContainer.setVisible(false);
        }
        if (settingsContainer != null) {
            settingsContainer.setVisible(true);
            
            // Ensure the circle is properly initialized when settings are shown
            if (ghostModeTrack != null && ghostModeThumb != null) {
                // Ensure track has proper alignment
                ghostModeTrack.setAlignment(javafx.geometry.Pos.CENTER);
                
                // Force re-initialization of circle properties
                ghostModeThumb.setRadius(10);
                ghostModeThumb.setFill(Color.rgb(0, 255, 255));
                ghostModeThumb.setEffect(new DropShadow(
                    BlurType.THREE_PASS_BOX,
                    Color.rgb(0, 255, 255),
                    15, 0.8, 0, 0
                ));
                ghostModeThumb.setOpacity(1.0);
                ghostModeThumb.setVisible(true);
            }
            
            // Initialize ghost mode switch with current GameConfig value
            updateGhostModeSwitch();
            // Initialize music volume slider
            if (musicVolumeSlider != null) {
                musicVolumeSlider.setValue(GameConfig.getMusicVolume());
            }
        }
    }

    @FXML
    public void showTutorial() {
        if (menuContainer != null) {
            menuContainer.setVisible(false);
        }
        if (settingsContainer != null) {
            settingsContainer.setVisible(false);
        }
        if (tutorialContainer != null) {
            tutorialContainer.setVisible(true);
        }
    }
    
    /**
     * Show settings from the pause menu.
     * Hides the pause menu and shows the settings container.
     */
    public void showPauseSettings() {
        // Hide pause menu
        if (groupPauseMenu != null) {
            groupPauseMenu.setVisible(false);
        }
        
        // Show settings container (same as main menu settings)
        if (settingsContainer != null) {
            settingsContainer.setVisible(true);
            
            // Ensure the circle is properly initialized when settings are shown
            if (ghostModeTrack != null && ghostModeThumb != null) {
                // Ensure track has proper alignment
                ghostModeTrack.setAlignment(javafx.geometry.Pos.CENTER);
                
                // Force re-initialization of circle properties
                ghostModeThumb.setRadius(10);
                ghostModeThumb.setFill(Color.rgb(0, 255, 255));
                ghostModeThumb.setEffect(new DropShadow(
                    BlurType.THREE_PASS_BOX,
                    Color.rgb(0, 255, 255),
                    15, 0.8, 0, 0
                ));
                ghostModeThumb.setOpacity(1.0);
                ghostModeThumb.setVisible(true);
            }
            
            // Initialize ghost mode switch with current GameConfig value
            updateGhostModeSwitch();
            // Initialize music volume slider
            if (musicVolumeSlider != null) {
                musicVolumeSlider.setValue(GameConfig.getMusicVolume());
            }
        }
    }

    @FXML
    public void backToMainMenu() {
        // Hide settings container
        if (settingsContainer != null) {
            settingsContainer.setVisible(false);
        }
        if (tutorialContainer != null) {
            tutorialContainer.setVisible(false);
        }
        
        // Show main menu buttons if we're in the main menu
        if (menuContainer != null && mainMenu != null && mainMenu.isVisible()) {
            menuContainer.setVisible(true);
        }
        
        // Show pause menu if we came from pause menu
        if (groupPauseMenu != null && isPause.getValue() == Boolean.TRUE) {
            groupPauseMenu.setVisible(true);
        }
    }

    @FXML
    public void hideTutorial() {
        if (tutorialContainer != null) {
            tutorialContainer.setVisible(false);
        }
        if (menuContainer != null && mainMenu != null && mainMenu.isVisible()) {
            menuContainer.setVisible(true);
        }
    }

    @FXML
    public void toggleTutorialOverlay() {
        if (tutorialContainer == null) {
            return;
        }
        boolean nextState = !tutorialContainer.isVisible();
        tutorialContainer.setVisible(nextState);
        if (!nextState && menuContainer != null && mainMenu != null && mainMenu.isVisible()) {
            menuContainer.setVisible(true);
        }
    }
    
    @FXML
    public void toggleGhostMode() {
        // Toggle ghost mode
        boolean newValue = !GameConfig.isGhostModeEnabled();
        GameConfig.setGhostModeEnabled(newValue);
        updateGhostModeSwitch();
    }
    
    private void updateGhostModeSwitch() {
        if (ghostModeTrack == null || ghostModeThumb == null || 
            ghostModeOffLabel == null || ghostModeOnLabel == null) {
            return;
        }
        
        boolean enabled = GameConfig.isGhostModeEnabled();
        
        // Precise positioning: Circle is centered in StackPane (at 0,0)
        // Track is 60px wide, circle radius is 10px (diameter 20px)
        // Center of track is at 30px, so:
        // OFF: Center - 15px = -15px translateX (circle from 15px to 35px, left side)
        // ON: Center + 15px = +15px translateX (circle from 25px to 45px, right side)
        double onPosition = 15.0;   // Right side: +15px from center
        double offPosition = -15.0; // Left side: -15px from center
        
        // Get current position to determine if we need to animate
        double currentX = ghostModeThumb.getTranslateX();
        double targetX = enabled ? onPosition : offPosition;
        
        // Only animate if position actually needs to change
        if (Math.abs(currentX - targetX) > 0.1) {
            // Animate thumb position with smooth transition
            TranslateTransition transition = new TranslateTransition(
                Duration.millis(250), ghostModeThumb);
            transition.setFromX(currentX);
            transition.setToX(targetX);
            transition.play();
        } else {
            // Position already correct, just set it directly
            ghostModeThumb.setTranslateX(targetX);
        }
        
        // Update label styles
        if (enabled) {
            ghostModeOffLabel.getStyleClass().clear();
            ghostModeOffLabel.getStyleClass().add("toggle-off");
            ghostModeOnLabel.getStyleClass().clear();
            ghostModeOnLabel.getStyleClass().add("toggle-on");
        } else {
            ghostModeOffLabel.getStyleClass().clear();
            ghostModeOffLabel.getStyleClass().add("toggle-off");
            ghostModeOnLabel.getStyleClass().clear();
            ghostModeOnLabel.getStyleClass().add("toggle-off"); // Dimmed when off
        }
    }
    
    public void startLevelGame(int levelId) {
        cleanupGame();
        
        mainMenu.setVisible(false);
        groupLevelSelection.setVisible(false);
        groupPauseMenu.setVisible(false);
        
        // Show HUD panels when game starts
        if (hudVboxLeft != null) {
            hudVboxLeft.setVisible(true);
        }
        if (hudVboxRight != null) {
            hudVboxRight.setVisible(true);
        }
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
