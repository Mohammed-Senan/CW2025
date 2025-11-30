package com.comp2042.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class LevelCompletePanel extends BorderPane {
    
    private Button nextLevelButton;
    private Button menuButton;
    private EventHandler<ActionEvent> onNextLevel;
    private EventHandler<ActionEvent> onMenu;
    
    public LevelCompletePanel(int levelCompleted, boolean isLastLevel) {
        VBox mainContainer = new VBox(30);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(40));
        
        Label titleLabel;
        if (isLastLevel) {
            titleLabel = new Label("ALL LEVELS COMPLETE!");
            titleLabel.setStyle("-fx-font-family: 'Let's go Digital'; -fx-font-size: 32px; -fx-text-fill: #00ff00; -fx-font-weight: bold;");
        } else {
            titleLabel = new Label("LEVEL " + levelCompleted + " COMPLETE!");
            titleLabel.setStyle("-fx-font-family: 'Let's go Digital'; -fx-font-size: 32px; -fx-text-fill: #00ffff; -fx-font-weight: bold;");
        }
        
        VBox buttonContainer = new VBox(20);
        buttonContainer.setAlignment(Pos.CENTER);
        
        if (!isLastLevel) {
            nextLevelButton = new Button("NEXT LEVEL");
            nextLevelButton.getStyleClass().add("menuButton");
            nextLevelButton.setOnAction(event -> {
                if (onNextLevel != null) {
                    onNextLevel.handle(event);
                }
            });
            buttonContainer.getChildren().add(nextLevelButton);
        }
        
        menuButton = new Button("MENU");
        menuButton.getStyleClass().add("menuButton");
        menuButton.setOnAction(event -> {
            if (onMenu != null) {
                onMenu.handle(event);
            }
        });
        buttonContainer.getChildren().add(menuButton);
        
        mainContainer.getChildren().addAll(titleLabel, buttonContainer);
        setCenter(mainContainer);
    }
    
    public void setOnNextLevel(EventHandler<ActionEvent> handler) {
        this.onNextLevel = handler;
    }
    
    public void setOnMenu(EventHandler<ActionEvent> handler) {
        this.onMenu = handler;
    }
}

