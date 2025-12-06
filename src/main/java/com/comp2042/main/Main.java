package com.comp2042.main;

import com.comp2042.model.Board;
import com.comp2042.controller.GameController;
import com.comp2042.model.SimpleBoard;
import com.comp2042.ui.GuiController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        URL location = getClass().getClassLoader().getResource("gameLayout.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(location);
        Parent root = fxmlLoader.load();
        GuiController c = fxmlLoader.getController();
        primaryStage.setTitle("Tetris NEW VERSION");
        Scene scene = new Scene(root, 700, 600);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(600);
        primaryStage.show();
        Board board = new SimpleBoard(25, 13);
        GameController gameController = new GameController(c, board);
        gameController.initGame();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

