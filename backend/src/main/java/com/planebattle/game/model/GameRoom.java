package com.planebattle.game.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class GameRoom {

    private String roomId;
    private PlayerSession playerA;
    private PlayerSession playerB;
    private Map<String, PlayerSession> sessions = new LinkedHashMap<>();
    private GameState gameState;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public PlayerSession getPlayerA() {
        return playerA;
    }

    public void setPlayerA(PlayerSession playerA) {
        this.playerA = playerA;
    }

    public PlayerSession getPlayerB() {
        return playerB;
    }

    public void setPlayerB(PlayerSession playerB) {
        this.playerB = playerB;
    }

    public Map<String, PlayerSession> getSessions() {
        return sessions;
    }

    public void setSessions(Map<String, PlayerSession> sessions) {
        this.sessions = sessions;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
}
