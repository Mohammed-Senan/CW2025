package com.comp2042.controller;

import com.comp2042.model.bricks.Brick;

/**
 * Strategy interface for supplying tetromino instances to the board.
 * Implementations may use random selection or more advanced queue logic.
 */
public interface BrickGenerator {

    /**
     * Returns the next {@link Brick} to be spawned as the active piece,
     * advancing any internal queue or random generator.
     *
     * @return the next brick to place on the board
     */
    Brick getBrick();

    /**
     * Returns, without consuming it, the brick that will follow the current
     * active piece. This is typically used to power the "Next" preview UI.
     *
     * @return the upcoming brick, or {@code null} if none is queued
     */
    Brick getNextBrick();
}

