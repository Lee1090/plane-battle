package com.planebattle.game.model;

public class AttackRecord {

    private int row;
    private int col;
    private AttackResult result;

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

    public AttackResult getResult() {
        return result;
    }

    public void setResult(AttackResult result) {
        this.result = result;
    }
}
