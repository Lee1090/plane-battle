package com.planebattle.game.service;

import com.planebattle.game.dto.ClientView;
import com.planebattle.game.model.GameRoom;
import com.planebattle.game.model.GameState;
import com.planebattle.game.model.GameStatus;
import com.planebattle.game.model.PlayerRole;
import com.planebattle.game.model.PlayerSession;
import com.planebattle.game.model.PlayerSide;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    private final GameRoom room = createRoom();

    public synchronized ClientView join(String sessionId) {
        PlayerSession playerSession = room.getSessions()
                .computeIfAbsent(sessionId, PlayerSession::new);
        return buildClientView(playerSession);
    }

    public synchronized ClientView getClientView(String sessionId) {
        PlayerSession playerSession = room.getSessions().get(sessionId);
        if (playerSession == null) {
            return new ClientView(PlayerRole.SPECTATOR, null, room.getGameState());
        }
        return buildClientView(playerSession);
    }

    public synchronized ClientView sitDown(String sessionId, PlayerSide side) {
        if (room.getGameState().getStatus() != GameStatus.WAITING) {
            throw new IllegalArgumentException("Players can only sit down while waiting.");
        }

        PlayerSession playerSession = room.getSessions()
                .computeIfAbsent(sessionId, PlayerSession::new);
        if (playerSession.getRole() != PlayerRole.SPECTATOR) {
            throw new IllegalArgumentException("You are already seated.");
        }

        if (side == PlayerSide.A) {
            occupySeatA(playerSession);
        } else if (side == PlayerSide.B) {
            occupySeatB(playerSession);
        } else {
            throw new IllegalArgumentException("Invalid player side.");
        }

        moveToDeployingWhenReady();
        return buildClientView(playerSession);
    }

    public synchronized ClientView standUp(String sessionId) {
        PlayerSession playerSession = room.getSessions().get(sessionId);
        if (playerSession == null || playerSession.getRole() == PlayerRole.SPECTATOR) {
            throw new IllegalArgumentException("Only seated players can stand up.");
        }
        if (room.getGameState().getStatus() == GameStatus.PLAYING
                || room.getGameState().getStatus() == GameStatus.FINISHED) {
            throw new IllegalArgumentException("Players cannot stand up after the game starts.");
        }

        clearSeat(playerSession);
        return buildClientView(playerSession);
    }

    public synchronized void leave(String sessionId) {
        PlayerSession playerSession = room.getSessions().remove(sessionId);
        if (playerSession == null) {
            return;
        }
        if (room.getGameState().getStatus() == GameStatus.WAITING
                || room.getGameState().getStatus() == GameStatus.DEPLOYING) {
            clearSeat(playerSession);
        }
    }

    public synchronized GameState getGameState() {
        return room.getGameState();
    }

    private GameState createInitialState() {
        GameState state = new GameState();
        state.setStatus(GameStatus.WAITING);
        state.setCurrentTurn(null);
        state.setWinner(null);
        state.setPlayerABoard(null);
        state.setPlayerBBoard(null);
        state.setPlayerASeated(false);
        state.setPlayerBSeated(false);
        state.setPlayerAReady(false);
        state.setPlayerBReady(false);
        return state;
    }

    private GameRoom createRoom() {
        GameRoom gameRoom = new GameRoom();
        gameRoom.setRoomId("default");
        gameRoom.setGameState(createInitialState());
        return gameRoom;
    }

    private ClientView buildClientView(PlayerSession playerSession) {
        return new ClientView(playerSession.getRole(), playerSession.getSide(), room.getGameState());
    }

    private void occupySeatA(PlayerSession playerSession) {
        if (room.getPlayerA() != null) {
            throw new IllegalArgumentException("This seat is already taken.");
        }
        playerSession.setRole(PlayerRole.PLAYER_A);
        playerSession.setSide(PlayerSide.A);
        room.setPlayerA(playerSession);
        room.getGameState().setPlayerASeated(true);
    }

    private void occupySeatB(PlayerSession playerSession) {
        if (room.getPlayerB() != null) {
            throw new IllegalArgumentException("This seat is already taken.");
        }
        playerSession.setRole(PlayerRole.PLAYER_B);
        playerSession.setSide(PlayerSide.B);
        room.setPlayerB(playerSession);
        room.getGameState().setPlayerBSeated(true);
    }

    private void clearSeat(PlayerSession playerSession) {
        if (playerSession.getSide() == PlayerSide.A) {
            room.setPlayerA(null);
            room.getGameState().setPlayerASeated(false);
            room.getGameState().setPlayerAReady(false);
            room.getGameState().setPlayerABoard(null);
        } else if (playerSession.getSide() == PlayerSide.B) {
            room.setPlayerB(null);
            room.getGameState().setPlayerBSeated(false);
            room.getGameState().setPlayerBReady(false);
            room.getGameState().setPlayerBBoard(null);
        }

        playerSession.setRole(PlayerRole.SPECTATOR);
        playerSession.setSide(null);
        if (room.getGameState().getStatus() == GameStatus.DEPLOYING) {
            room.getGameState().setStatus(GameStatus.WAITING);
        }
    }

    private void moveToDeployingWhenReady() {
        if (room.getPlayerA() != null && room.getPlayerB() != null) {
            room.getGameState().setStatus(GameStatus.DEPLOYING);
        }
    }
}
