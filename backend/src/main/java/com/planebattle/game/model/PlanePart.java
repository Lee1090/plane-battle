package com.planebattle.game.model;

public class PlanePart {

    private PlanePartType type;
    private int row;
    private int col;
    private boolean hit;

    public PlanePartType getType() {
        return type;
    }

    public void setType(PlanePartType type) {
        this.type = type;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }
}
