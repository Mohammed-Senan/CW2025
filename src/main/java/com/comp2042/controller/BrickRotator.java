package com.comp2042.controller;

import com.comp2042.event.NextShapeInfo;
import com.comp2042.model.bricks.Brick;

/**
 * Manages rotation state for a single tetromino, exposing the
 * current orientation and the next rotation as view data.
 */
public class BrickRotator {

    private Brick brick;
    private int currentShape = 0;

    /**
     * Computes the next rotation state for the current brick without
     * mutating the internal rotation index.
     *
     * @return a {@link NextShapeInfo} describing the rotated shape and index
     */
    public NextShapeInfo getNextShape() {
        int nextShape = currentShape;
        nextShape = (++nextShape) % brick.getShapeMatrix().size();
        return new NextShapeInfo(brick.getShapeMatrix().get(nextShape), nextShape);
    }

    /**
     * Returns the matrix for the brick in its current rotation.
     *
     * @return 2D array representing the active brick shape
     */
    public int[][] getCurrentShape() {
        return brick.getShapeMatrix().get(currentShape);
    }

    /**
     * Sets the active rotation index for the current brick.
     *
     * @param currentShape index into the brick's shape matrix list
     */
    public void setCurrentShape(int currentShape) {
        this.currentShape = currentShape;
    }

    /**
     * Assigns a new brick to be rotated and resets the rotation to its
     * default orientation.
     *
     * @param brick the tetromino whose rotations will be managed
     */
    public void setBrick(Brick brick) {
        this.brick = brick;
        currentShape = 0;
    }


}
