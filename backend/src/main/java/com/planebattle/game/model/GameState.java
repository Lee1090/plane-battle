package com.planebattle.game.model;

public class GameState {

    private GameStatus status;
    private PlayerSide currentTurn;
    private PlayerSide winner;
    private PlayerBoard playerABoard;
    private PlayerBoard playerBBoard;
    private boolean playerAReady;
    private boolean playerBReady;

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public PlayerSide getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(PlayerSide currentTurn) {
        this.currentTurn = currentTurn;
    }

    public PlayerSide getWinner() {
        return winner;
    }

    public void setWinner(PlayerSide winner) {
        this.winner = winner;
    }

    public PlayerBoard getPlayerABoard() {
        return playerABoard;
    }

    public void setPlayerABoard(PlayerBoard playerABoard) {
        this.playerABoard = playerABoard;
    }

    public PlayerBoard getPlayerBBoard() {
        return playerBBoard;
    }

    public void setPlayerBBoard(PlayerBoard playerBBoard) {
        this.playerBBoard = playerBBoard;
    }

    public boolean isPlayerAReady() {
        return playerAReady;
    }

    public void setPlayerAReady(boolean playerAReady) {
        this.playerAReady = playerAReady;
    }

    public boolean isPlayerBReady() {
        return playerBReady;
    }

    public void setPlayerBReady(boolean playerBReady) {
        this.playerBReady = playerBReady;
    }
}
