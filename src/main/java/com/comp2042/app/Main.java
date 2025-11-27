package com.comp2042.app;

import com.comp2042.logic.Board;
import com.comp2042.logic.GameController;
import com.comp2042.logic.SimpleBoard;
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
        Scene scene = new Scene(root, 360, 610);
        primaryStage.setScene(scene);
        primaryStage.show();
        Board board = new SimpleBoard(25, 13);
        GameController gameController = new GameController(c, board);
        gameController.initGame();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
