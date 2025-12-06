package com.comp2042.controller;

import com.comp2042.model.bricks.Brick;

public interface BrickGenerator {

    Brick getBrick();

    Brick getNextBrick();
}

