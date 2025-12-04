package com.comp2042.logic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

public class MatrixOperations {


    private MatrixOperations(){

    }

    /**
     * Per-Cell Collision Detection
     * 
     * This method implements precise per-cell collision checking.
     * 
     * IMPORTANT: The brick shapes are stored as shape[row][col], but the original code
     * accessed them as shape[col][row] (transposed). This method maintains that behavior
     * to ensure compatibility with the existing coordinate system.
     * 
     * @param matrix The game board matrix (matrix[row][col] or matrix[y][x])
     * @param shape The block's shape array (shape[row][col] in storage, but accessed as shape[col][row])
     * @param currentX The X position of the block's top-left corner
     * @param currentY The Y position of the block's top-left corner
     * @return true if there's a collision, false if the move is safe
     */
    public static boolean intersect(final int[][] matrix, final int[][] shape, int currentX, int currentY) {
        if (matrix == null || shape == null || matrix.length == 0) {
            return false;
        }
        
        int BOARD_HEIGHT = matrix.length; // Number of rows
        int BOARD_WIDTH = matrix[0] != null ? matrix[0].length : 0; // Number of columns
        
        // Iterate through every cell of the block's 2D shape array
        // Note: The original code used transposed indexing (shape[col][row] instead of shape[row][col])
        // We maintain this to match the existing coordinate system
        for (int i = 0; i < shape.length; i++) {  // i loops through shape rows
            for (int j = 0; j < shape[i].length; j++) {  // j loops through shape columns
                // Access shape transposed: shape[j][i] means shape[col][row]
                // This matches the original implementation's coordinate system
                if (shape[j][i] != 0) {
                    // Calculate board coordinates
                    // Original code used: targetX = x + i, targetY = y + j
                    // This means: i (shape row) maps to X offset, j (shape col) maps to Y offset
                    int boardX = currentX + i;  // i (shape row index) is X offset
                    int boardY = currentY + j;  // j (shape col index) is Y offset
                    
                    // 1. Check Wall Boundaries
                    if (boardX < 0 || boardX >= BOARD_WIDTH || boardY >= BOARD_HEIGHT) {
                        return true; // Collision with wall or floor!
                    }
                    
                    // 2. Check Existing Blocks (only if within valid Y range)
                    if (boardY >= 0 && matrix[boardY][boardX] != 0) {
                        return true; // Collision with existing block!
                    }
                }
            }
        }
        
        return false; // Safe to move - no collisions detected
    }

    private static boolean checkOutOfBound(int[][] matrix, int targetX, int targetY) {
        boolean returnValue = true;
        if (targetX >= 0 && targetY < matrix.length && targetX < matrix[targetY].length) {
            returnValue = false;
        }
        return returnValue;
    }

    public static int[][] copy(int[][] original) {
        int[][] myInt = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            int[] aMatrix = original[i];
            int aLength = aMatrix.length;
            myInt[i] = new int[aLength];
            System.arraycopy(aMatrix, 0, myInt[i], 0, aLength);
        }
        return myInt;
    }

    /**
     * Merge the brick into the board matrix using per-cell logic
     * Uses the same transposed indexing as intersect() for consistency
     * 
     * CRITICAL: This function MUST never write outside board boundaries.
     * Any cell that would be outside bounds is silently skipped (not merged).
     */
    public static int[][] merge(int[][] filledFields, int[][] shape, int currentX, int currentY) {
        if (filledFields == null || shape == null || filledFields.length == 0) {
            return filledFields;
        }
        
        int[][] copy = copy(filledFields);
        int BOARD_HEIGHT = copy.length; // Number of rows
        int BOARD_WIDTH = copy[0] != null ? copy[0].length : 0; // Number of columns
        
        // Iterate through every cell of the block's 2D shape array
        // Use the same transposed indexing as intersect() to maintain consistency
        for (int i = 0; i < shape.length; i++) {  // i loops through shape rows
            for (int j = 0; j < shape[i].length; j++) {  // j loops through shape columns
                // Access shape transposed: shape[j][i] means shape[col][row]
                if (shape[j][i] != 0) {
                    // Calculate board coordinates (same as intersect)
                    int boardX = currentX + i;  // i (shape row index) is X offset
                    int boardY = currentY + j;  // j (shape col index) is Y offset
                    
                    // STRICT boundary checking - NEVER write outside board boundaries
                    // Check all boundaries: X must be 0 to BOARD_WIDTH-1, Y must be 0 to BOARD_HEIGHT-1
                    if (boardX >= 0 && boardX < BOARD_WIDTH && 
                        boardY >= 0 && boardY < BOARD_HEIGHT &&
                        boardY < copy.length && 
                        boardX < copy[boardY].length) {
                        // All checks passed - safe to merge this cell
                        copy[boardY][boardX] = shape[j][i];
                    }
                    // If any boundary check fails, skip this cell (don't merge it)
                    // This prevents blocks from being placed beyond the border
                }
            }
        }
        return copy;
    }

    public static ClearRow checkRemoving(final int[][] matrix) {
        int[][] tmp = new int[matrix.length][matrix[0].length];
        Deque<int[]> newRows = new ArrayDeque<>();
        List<Integer> clearedRows = new ArrayList<>();

        for (int i = 0; i < matrix.length; i++) {
            int[] tmpRow = new int[matrix[i].length];
            boolean rowToClear = true;
            for (int j = 0; j < matrix[0].length; j++) {
                if (matrix[i][j] == 0) {
                    rowToClear = false;
                }
                tmpRow[j] = matrix[i][j];
            }
            if (rowToClear) {
                clearedRows.add(i);
            } else {
                newRows.add(tmpRow);
            }
        }
        for (int i = matrix.length - 1; i >= 0; i--) {
            int[] row = newRows.pollLast();
            if (row != null) {
                tmp[i] = row;
            } else {
                break;
            }
        }

        return new ClearRow(clearedRows.size(), tmp, clearedRows);
    }

    public static List<int[][]> deepCopyList(List<int[][]> list){
        return list.stream().map(MatrixOperations::copy).collect(Collectors.toList());
    }

}
