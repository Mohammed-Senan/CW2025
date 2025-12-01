package com.comp2042.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class PauseMenuPanel extends BorderPane {

    private Button resumeButton;
    private Button backToMenuButton;
    private Button quitButton;
    private EventHandler<ActionEvent> onResume;
    private EventHandler<ActionEvent> onBackToMenu;
    private EventHandler<ActionEvent> onQuit;

    public PauseMenuPanel() {
        VBox mainContainer = new VBox(30);
        mainContainer.setStyle("-fx-alignment: center;");

        Label pausedLabel = new Label("PAUSED");
        pausedLabel.getStyleClass().add("gameOverStyle");
        pausedLabel.setStyle("-fx-font-family: 'Let's go Digital'; -fx-font-size: 48px; -fx-text-fill: #00ffff;");

        VBox buttonContainer = new VBox(20);
        buttonContainer.setStyle("-fx-alignment: center;");

        resumeButton = new Button("RESUME");
        resumeButton.getStyleClass().add("menuButton");
        resumeButton.setOnAction(event -> {
            if (onResume != null) {
                onResume.handle(event);
            }
        });

        backToMenuButton = new Button("BACK TO MENU");
        backToMenuButton.getStyleClass().add("menuButton");
        backToMenuButton.setOnAction(event -> {
            if (onBackToMenu != null) {
                onBackToMenu.handle(event);
            }
        });

        quitButton = new Button("QUIT");
        quitButton.getStyleClass().add("menuButton");
        quitButton.setOnAction(event -> {
            if (onQuit != null) {
                onQuit.handle(event);
            }
        });

        buttonContainer.getChildren().addAll(resumeButton, backToMenuButton, quitButton);
        mainContainer.getChildren().addAll(pausedLabel, buttonContainer);
        setCenter(mainContainer);
    }

    public void setOnResume(EventHandler<ActionEvent> handler) {
        this.onResume = handler;
    }

    public void setOnBackToMenu(EventHandler<ActionEvent> handler) {
        this.onBackToMenu = handler;
    }

    public void setOnQuit(EventHandler<ActionEvent> handler) {
        this.onQuit = handler;
    }
}

