package com.planebattle.game.model;

public class PlayerSession {

    private String sessionId;
    private PlayerRole role = PlayerRole.SPECTATOR;
    private PlayerSide side;

    public PlayerSession() {
    }

    public PlayerSession(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public PlayerRole getRole() {
        return role;
    }

    public void setRole(PlayerRole role) {
        this.role = role;
    }

    public PlayerSide getSide() {
        return side;
    }

    public void setSide(PlayerSide side) {
        this.side = side;
    }
}
