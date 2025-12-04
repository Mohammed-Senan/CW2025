package com.comp2042.logic;

import java.util.List;

public final class ClearRow {

    private final int linesRemoved;
    private final int[][] newMatrix;
    private final List<Integer> clearedRowIndices;


    public ClearRow(int linesRemoved, int[][] newMatrix, List<Integer> clearedRowIndices) {
        this.linesRemoved = linesRemoved;
        this.newMatrix = newMatrix;
        this.clearedRowIndices = clearedRowIndices;
    }

    public int getLinesRemoved() {
        return linesRemoved;
    }

    public int[][] getNewMatrix() {
        return MatrixOperations.copy(newMatrix);
    }
    
    public List<Integer> getClearedRowIndices() {
        return clearedRowIndices;
    }
}
