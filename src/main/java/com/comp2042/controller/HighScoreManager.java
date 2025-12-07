package com.comp2042.controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Manages persistent storage and retrieval of the player's high score
 * using a simple text file in the local file system.
 */
public class HighScoreManager {

    private static final String HIGHSCORE_FILE = "highscore.txt";

    /**
     * Loads the current high score from the high score file.
     * If the file is missing or cannot be parsed, this method returns {@code 0}.
     *
     * @return the stored high score, or {@code 0} if no valid score is found
     */
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

    /**
     * Persists a new high score if it exceeds the currently stored value.
     * The score is written as plain text to the high score file.
     *
     * @param score the latest score achieved by the player
     */
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

