package com.comp2042.logic;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HighScoreManager {

    private static final String HIGHSCORE_FILE = "highscore.txt";

    public int loadHighScore() {
        try {
            if (!Files.exists(Paths.get(HIGHSCORE_FILE))) {
                return 0;
            }
            String content = new String(Files.readAllBytes(Paths.get(HIGHSCORE_FILE)));
            return Integer.parseInt(content.trim());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void saveHighScore(int score) {
        try {
            int currentHigh = loadHighScore();
            if (score > currentHigh) {
                FileWriter writer = new FileWriter(HIGHSCORE_FILE);
                writer.write(String.valueOf(score));
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}