package com.comp2042.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class LevelFailedPanel extends BorderPane {
    
    private Button restartButton;
    private Button backToMenuButton;
    private EventHandler<ActionEvent> onRestart;
    private EventHandler<ActionEvent> onBackToMenu;
    
    public LevelFailedPanel(int level, int currentScore, int requiredScore) {
        VBox mainContainer = new VBox(30);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(40));
        
        Label titleLabel = new Label("LEVEL " + level + " FAILED!");
        titleLabel.setStyle("-fx-font-family: 'Let's go Digital'; -fx-font-size: 32px; -fx-text-fill: #ff0000; -fx-font-weight: bold;");
        
        Label scoreLabel = new Label("Score: " + currentScore + " / " + requiredScore);
        scoreLabel.setStyle("-fx-font-family: 'Let's go Digital'; -fx-font-size: 20px; -fx-text-fill: #ffff00; -fx-font-weight: bold;");
        
        Label messageLabel = new Label("You didn't reach the required score!");
        messageLabel.setStyle("-fx-font-family: 'Let's go Digital'; -fx-font-size: 18px; -fx-text-fill: #ffffff;");
        
        VBox buttonContainer = new VBox(20);
        buttonContainer.setAlignment(Pos.CENTER);
        
        restartButton = new Button("RESTART");
        restartButton.getStyleClass().add("menuButton");
        restartButton.setOnAction(event -> {
            if (onRestart != null) {
                onRestart.handle(event);
            }
        });
        
        backToMenuButton = new Button("BACK TO MENU");
        backToMenuButton.getStyleClass().add("menuButton");
        backToMenuButton.setOnAction(event -> {
            if (onBackToMenu != null) {
                onBackToMenu.handle(event);
            }
        });
        
        buttonContainer.getChildren().addAll(restartButton, backToMenuButton);
        mainContainer.getChildren().addAll(titleLabel, scoreLabel, messageLabel, buttonContainer);
        setCenter(mainContainer);
    }
    
    public void setOnRestart(EventHandler<ActionEvent> handler) {
        this.onRestart = handler;
    }
    
    public void setOnBackToMenu(EventHandler<ActionEvent> handler) {
        this.onBackToMenu = handler;
    }
}







