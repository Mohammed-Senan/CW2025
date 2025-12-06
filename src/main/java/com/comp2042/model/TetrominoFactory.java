package com.comp2042.model;

import com.comp2042.model.bricks.Brick;
import com.comp2042.model.bricks.IBrick;
import com.comp2042.model.bricks.JBrick;
import com.comp2042.model.bricks.LBrick;
import com.comp2042.model.bricks.OBrick;
import com.comp2042.model.bricks.SBrick;
import com.comp2042.model.bricks.TBrick;
import com.comp2042.model.bricks.ZBrick;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Factory class for creating random Tetromino (Brick) instances.
 * Implements the Factory Design Pattern to encapsulate the logic
 * for selecting and creating random block types.
 */
public class TetrominoFactory {
    
    /**
     * Creates a random Tetromino (Brick) instance.
     * Uses a random number generator to select one of the 7 standard Tetris pieces.
     * 
     * @return A new Brick instance representing a random Tetromino
     */
    public static Brick createRandomBlock() {
        // Generate random number from 0 to 6 (7 possible block types)
        int randomType = ThreadLocalRandom.current().nextInt(7);
        
        // Use switch statement to determine which block type to create
        switch (randomType) {
            case 0:
                return new IBrick();
            case 1:
                return new JBrick();
            case 2:
                return new LBrick();
            case 3:
                return new OBrick();
            case 4:
                return new SBrick();
            case 5:
                return new TBrick();
            case 6:
                return new ZBrick();
            default:
                // Fallback to I-Brick if somehow an invalid number is generated
                return new IBrick();
        }
    }
}

