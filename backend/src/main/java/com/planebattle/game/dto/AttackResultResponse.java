package com.planebattle.game.dto;

import com.planebattle.game.model.AttackResult;
import com.planebattle.game.model.PlayerSide;

public class AttackResultResponse {

    private PlayerSide attacker;
    private PlayerSide defender;
    private int row;
    private int col;
    private AttackResult result;

    public PlayerSide getAttacker() {
        return attacker;
    }

    public void setAttacker(PlayerSide attacker) {
        this.attacker = attacker;
    }

    public PlayerSide getDefender() {
        return defender;
    }

    public void setDefender(PlayerSide defender) {
        this.defender = defender;
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

    public AttackResult getResult() {
        return result;
    }

    public void setResult(AttackResult result) {
        this.result = result;
    }
}
