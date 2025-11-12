package com.comp2042.logic;

public final class DownData {
    private final ClearRow clearRow;
    private final ViewData viewData;
    private final int scoreBonus;

    public DownData(ClearRow clearRow, ViewData viewData, int scoreBonus) {
        this.clearRow = clearRow;
        this.viewData = viewData;
        this.scoreBonus = scoreBonus;
    }

    public ClearRow getClearRow() {
        return clearRow;
    }

    public ViewData getViewData() {
        return viewData;
    }

    public int getScoreBonus() {
        return scoreBonus;
    }
}
