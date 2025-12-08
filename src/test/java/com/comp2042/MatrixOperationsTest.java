package com.comp2042;

import com.comp2042.model.ClearRow;
import com.comp2042.model.MatrixOperations;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MatrixOperationsTest {

    @Test
    public void testIntersectReturnsTrueForOverlappingMatrices() {
        int[][] board = new int[4][4];
        board[2][2] = 1;

        int[][] shape = {
                {0, 1},
                {0, 0}
        };

        boolean result = MatrixOperations.intersect(board, shape, 1, 2);

        assertTrue(result, "intersect should return true when shape overlaps a filled board cell");
    }

    @Test
    public void testCheckRemovingDetectsFullRow() {
        int[][] board = new int[4][4];

        board[3][0] = 1;
        board[3][1] = 1;
        board[3][2] = 1;
        board[3][3] = 1;

        ClearRow clearRow = MatrixOperations.checkRemoving(board);

        assertTrue(clearRow.getLinesRemoved() >= 1,
                "checkRemoving should report at least one cleared row when a full row exists");
    }
}
