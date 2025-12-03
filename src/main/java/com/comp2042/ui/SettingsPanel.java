package com.comp2042.ui;

import com.comp2042.logic.GameConfig;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class SettingsPanel extends BorderPane {

    private Button backToMenuButton;
    private CheckBox ghostModeCheckBox;
    private EventHandler<ActionEvent> onBackToMenu;

    public SettingsPanel() {
        VBox mainContainer = new VBox(30);
        mainContainer.setStyle("-fx-alignment: center;");

        Label settingsLabel = new Label("SETTINGS");
        settingsLabel.getStyleClass().add("gameOverStyle");
        settingsLabel.setStyle("-fx-font-family: 'Let's go Digital'; -fx-font-size: 48px; -fx-text-fill: #00ffff;");

        VBox buttonContainer = new VBox(20);
        buttonContainer.setStyle("-fx-alignment: center;");

        // Ghost Mode Toggle Section
        Label ghostModeLabel = new Label("GHOST MODE");
        ghostModeLabel.setStyle("-fx-font-family: 'Let's go Digital'; -fx-font-size: 20px; -fx-text-fill: #00ffff;");
        
        ghostModeCheckBox = new CheckBox("Enable Ghost Mode");
        ghostModeCheckBox.setStyle("-fx-font-family: 'Let's go Digital'; -fx-font-size: 16px; -fx-text-fill: #ffffff;");
        ghostModeCheckBox.setOnAction(event -> {
            // Update GameConfig immediately when checkbox is toggled
            GameConfig.setGhostModeEnabled(ghostModeCheckBox.isSelected());
        });
        
        VBox ghostModeSection = new VBox(10);
        ghostModeSection.setStyle("-fx-alignment: center;");
        ghostModeSection.getChildren().addAll(ghostModeLabel, ghostModeCheckBox);

        backToMenuButton = new Button("BACK TO MENU");
        backToMenuButton.getStyleClass().add("menuButton");
        backToMenuButton.setOnAction(event -> {
            if (onBackToMenu != null) {
                onBackToMenu.handle(event);
            }
        });

        buttonContainer.getChildren().addAll(ghostModeSection, backToMenuButton);
        mainContainer.getChildren().addAll(settingsLabel, buttonContainer);
        setCenter(mainContainer);
    }

    public void setOnBackToMenu(EventHandler<ActionEvent> handler) {
        this.onBackToMenu = handler;
    }

    /**
     * Initialize the ghost mode checkbox with the current GameConfig value.
     * This should be called after the panel is created to sync with GameConfig.
     */
    public void initializeGhostMode() {
        if (ghostModeCheckBox != null) {
            // Set initial state from GameConfig
            ghostModeCheckBox.setSelected(GameConfig.isGhostModeEnabled());
        }
    }
}

