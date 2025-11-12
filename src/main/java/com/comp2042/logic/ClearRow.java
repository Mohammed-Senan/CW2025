package com.comp2042.logic;

public final class ClearRow {

    private final int linesRemoved;
    private final int[][] newMatrix;


    public ClearRow(int linesRemoved, int[][] newMatrix ) {
        this.linesRemoved = linesRemoved;
        this.newMatrix = newMatrix;

    }

    public int getLinesRemoved() {
        return linesRemoved;
    }

    public int[][] getNewMatrix() {
        return MatrixOperations.copy(newMatrix);
    }
}
