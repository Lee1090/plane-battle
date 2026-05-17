package com.planebattle.game.dto;

import com.planebattle.game.model.GameState;
import com.planebattle.game.model.PlayerRole;
import com.planebattle.game.model.PlayerSide;

public class ClientView {

    private PlayerRole role;
    private PlayerSide side;
    private GameState gameState;

    public ClientView() {
    }

    public ClientView(PlayerRole role, PlayerSide side, GameState gameState) {
        this.role = role;
        this.side = side;
        this.gameState = gameState;
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

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
}
