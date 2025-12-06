package com.comp2042.ui;

import com.comp2042.model.LevelConfig;
import com.comp2042.controller.LevelManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LevelSelectionPanel extends BorderPane {
    
    private LevelManager levelManager;
    private EventHandler<ActionEvent> onLevelSelected;
    private EventHandler<ActionEvent> onBackToMenu;
    private GridPane levelGrid;
    private ImageView backgroundImageView;
    
    public LevelSelectionPanel() {
        levelManager = new LevelManager();
        createUI();
        updateLevelButtons();
    }
    
    private void createUI() {
        StackPane stackPane = new StackPane();
        
        try {
            Image backgroundImage = new Image(getClass().getClassLoader().getResource("level.png").toExternalForm());
            backgroundImageView = new ImageView(backgroundImage); 
            backgroundImageView.fitWidthProperty().bind(this.widthProperty());
            backgroundImageView.fitHeightProperty().bind(this.heightProperty());
            backgroundImageView.setPreserveRatio(false);
            backgroundImageView.setSmooth(true);
            backgroundImageView.setPickOnBounds(true);
            backgroundImageView.setMouseTransparent(true);
            stackPane.getChildren().add(backgroundImageView);
        } catch (Exception e) {
            System.out.println("Background image not found: " + e.getMessage());
        }
        
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(30));
        
        Label titleLabel = new Label("SELECT LEVEL");
        titleLabel.setStyle("-fx-font-family: 'Let's go Digital'; -fx-font-size: 36px; -fx-text-fill: #00ffff; -fx-font-weight: bold;");
        
        levelGrid = new GridPane();
        levelGrid.setHgap(15);
        levelGrid.setVgap(15);
        levelGrid.setAlignment(Pos.CENTER);
        
        LevelConfig[] levels = levelManager.getAllLevels();
        for (int i = 0; i < levels.length; i++) {
            int levelId = i + 1;
            Button levelButton = createLevelButton(levelId, levels[i].isLocked());
            int col = i % 5;
            int row = i / 5;
            levelGrid.add(levelButton, col, row);
        }
        
        Button backButton = new Button("BACK TO MENU");
        backButton.getStyleClass().add("menuButton");
        backButton.setOnAction(event -> {
            if (onBackToMenu != null) {
                onBackToMenu.handle(event);
            }
        });
        
        mainContainer.getChildren().addAll(titleLabel, levelGrid, backButton);
        stackPane.getChildren().add(mainContainer);
        setCenter(stackPane);
    }
    
    private Button createLevelButton(int levelId, boolean isLocked) {
        Button button = new Button();
        button.setPrefWidth(60);
        button.setPrefHeight(60);
        
        if (isLocked) {
            button.setStyle("-fx-background-color: #333333; -fx-text-fill: #666666; -fx-font-size: 20px; -fx-font-weight: bold; -fx-border-color: #555555; -fx-border-width: 2px;");
            button.setText("🔒");
            button.setDisable(true);
        } else {
            button.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(0, 150, 200, 0.7), rgba(0, 100, 150, 0.8)); -fx-text-fill: #ffffff; -fx-font-size: 24px; -fx-font-weight: bold; -fx-border-color: #00ffff; -fx-border-width: 2px; -fx-cursor: hand;");
            button.setText(String.valueOf(levelId));
            final int finalLevelId = levelId;
            button.setOnAction(event -> {
                if (onLevelSelected != null) {
                    button.setUserData(finalLevelId);
                    onLevelSelected.handle(event);
                }
            });
            
            button.setOnMouseEntered(e -> {
                if (!isLocked) {
                    button.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(0, 200, 255, 0.9), rgba(0, 150, 200, 1.0)); -fx-text-fill: #ffff00; -fx-font-size: 24px; -fx-font-weight: bold; -fx-border-color: #00ffff; -fx-border-width: 3px; -fx-cursor: hand;");
                }
            });
            
            button.setOnMouseExited(e -> {
                if (!isLocked) {
                    button.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(0, 150, 200, 0.7), rgba(0, 100, 150, 0.8)); -fx-text-fill: #ffffff; -fx-font-size: 24px; -fx-font-weight: bold; -fx-border-color: #00ffff; -fx-border-width: 2px; -fx-cursor: hand;");
                }
            });
        }
        
        return button;
    }
    
    public void updateLevelButtons() {
        levelGrid.getChildren().clear();
        LevelConfig[] levels = levelManager.getAllLevels();
        for (int i = 0; i < levels.length; i++) {
            int levelId = i + 1;
            boolean isLocked = levelManager.isLevelLocked(levelId);
            Button levelButton = createLevelButton(levelId, isLocked);
            int col = i % 5;
            int row = i / 5;
            levelGrid.add(levelButton, col, row);
        }
    }
    
    public void setOnLevelSelected(EventHandler<ActionEvent> handler) {
        this.onLevelSelected = handler;
    }
    
    public void setOnBackToMenu(EventHandler<ActionEvent> handler) {
        this.onBackToMenu = handler;
    }
    
    public void refreshLevels() {
        levelManager = new LevelManager();
        updateLevelButtons();
    }
}

