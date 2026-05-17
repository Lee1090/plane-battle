package com.planebattle.game.service;

import com.planebattle.game.dto.ClientView;
import com.planebattle.game.model.GameState;
import com.planebattle.game.model.GameStatus;
import com.planebattle.game.model.PlayerRole;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    private final GameState gameState = createInitialState();

    public ClientView join(String sessionId) {
        return new ClientView(PlayerRole.SPECTATOR, null, gameState);
    }

    public GameState getGameState() {
        return gameState;
    }

    private GameState createInitialState() {
        GameState state = new GameState();
        state.setStatus(GameStatus.WAITING);
        state.setCurrentTurn(null);
        state.setWinner(null);
        state.setPlayerABoard(null);
        state.setPlayerBBoard(null);
        state.setPlayerAReady(false);
        state.setPlayerBReady(false);
        return state;
    }
}
