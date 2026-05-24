package com.planebattle.game.service;

import com.planebattle.game.dto.ClientView;
import com.planebattle.game.dto.PlaneDeploymentRequest;
import com.planebattle.game.model.GameRoom;
import com.planebattle.game.model.GameState;
import com.planebattle.game.model.GameStatus;
import com.planebattle.game.model.PlayerBoard;
import com.planebattle.game.model.PlayerRole;
import com.planebattle.game.model.PlayerSession;
import com.planebattle.game.model.PlayerSide;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    private final GameRoom room = createRoom();
    private final DeploymentValidator deploymentValidator;

    public GameService(DeploymentValidator deploymentValidator) {
        this.deploymentValidator = deploymentValidator;
    }

    public synchronized ClientView join(String sessionId) {
        PlayerSession playerSession = room.getSessions()
                .computeIfAbsent(sessionId, PlayerSession::new);
        return buildClientView(playerSession);
    }

    public synchronized ClientView getClientView(String sessionId) {
        PlayerSession playerSession = room.getSessions().get(sessionId);
        if (playerSession == null) {
            return buildClientView(new PlayerSession(sessionId));
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
        if (playerSession.getSide() == PlayerSide.A && room.getGameState().isPlayerAReady()
                || playerSession.getSide() == PlayerSide.B && room.getGameState().isPlayerBReady()) {
            throw new IllegalArgumentException("Players cannot stand up after submitting deployment.");
        }

        clearSeat(playerSession);
        return buildClientView(playerSession);
    }

    public synchronized ClientView submitDeployment(String sessionId, List<PlaneDeploymentRequest> requests) {
        if (room.getGameState().getStatus() != GameStatus.DEPLOYING) {
            throw new IllegalArgumentException("Invalid game status.");
        }

        PlayerSession playerSession = room.getSessions().get(sessionId);
        if (playerSession == null || playerSession.getRole() == PlayerRole.SPECTATOR || playerSession.getSide() == null) {
            throw new IllegalArgumentException("Only players can perform this action.");
        }
        if (playerSession.getSide() == PlayerSide.A && room.getGameState().isPlayerAReady()
                || playerSession.getSide() == PlayerSide.B && room.getGameState().isPlayerBReady()) {
            throw new IllegalArgumentException("Deployment has already been submitted.");
        }

        PlayerBoard playerBoard = new PlayerBoard();
        playerBoard.setOwner(playerSession.getSide());
        playerBoard.setPlanes(deploymentValidator.validateAndBuildPlanes(requests));
        playerBoard.setReceivedAttacks(new ArrayList<>());

        if (playerSession.getSide() == PlayerSide.A) {
            room.getGameState().setPlayerABoard(playerBoard);
            room.getGameState().setPlayerAReady(true);
        } else {
            room.getGameState().setPlayerBBoard(playerBoard);
            room.getGameState().setPlayerBReady(true);
        }

        moveToPlayingWhenReady();
        return buildClientView(playerSession);
    }

    public synchronized void leave(String sessionId) {
        PlayerSession playerSession = room.getSessions().remove(sessionId);
        if (playerSession == null) {
            return;
        }
        if (shouldClearSeatOnLeave(playerSession)) {
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
        return new ClientView(playerSession.getRole(), playerSession.getSide(), buildVisibleGameState(playerSession));
    }

    private GameState buildVisibleGameState(PlayerSession playerSession) {
        GameState source = room.getGameState();
        GameState visible = new GameState();
        visible.setStatus(source.getStatus());
        visible.setCurrentTurn(source.getCurrentTurn());
        visible.setWinner(source.getWinner());
        visible.setPlayerASeated(source.isPlayerASeated());
        visible.setPlayerBSeated(source.isPlayerBSeated());
        visible.setPlayerAReady(source.isPlayerAReady());
        visible.setPlayerBReady(source.isPlayerBReady());

        boolean canSeeAPlanes = playerSession.getSide() == PlayerSide.A;
        boolean canSeeBPlanes = playerSession.getSide() == PlayerSide.B;
        visible.setPlayerABoard(copyBoard(source.getPlayerABoard(), canSeeAPlanes));
        visible.setPlayerBBoard(copyBoard(source.getPlayerBBoard(), canSeeBPlanes));
        return visible;
    }

    private PlayerBoard copyBoard(PlayerBoard board, boolean includePlanes) {
        if (board == null) {
            return null;
        }
        PlayerBoard copy = new PlayerBoard();
        copy.setOwner(board.getOwner());
        copy.setReceivedAttacks(new ArrayList<>(board.getReceivedAttacks()));
        copy.setPlanes(includePlanes ? board.getPlanes() : List.of());
        return copy;
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

    private boolean shouldClearSeatOnLeave(PlayerSession playerSession) {
        if (room.getGameState().getStatus() == GameStatus.WAITING) {
            return true;
        }
        if (room.getGameState().getStatus() != GameStatus.DEPLOYING) {
            return false;
        }
        return playerSession.getSide() == PlayerSide.A && !room.getGameState().isPlayerAReady()
                || playerSession.getSide() == PlayerSide.B && !room.getGameState().isPlayerBReady();
    }

    private void moveToDeployingWhenReady() {
        if (room.getPlayerA() != null && room.getPlayerB() != null) {
            room.getGameState().setStatus(GameStatus.DEPLOYING);
        }
    }

    private void moveToPlayingWhenReady() {
        if (room.getGameState().isPlayerAReady() && room.getGameState().isPlayerBReady()) {
            room.getGameState().setStatus(GameStatus.PLAYING);
            room.getGameState().setCurrentTurn(PlayerSide.A);
        }
    }
}
