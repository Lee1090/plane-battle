package com.planebattle.game.service;

import com.planebattle.game.dto.ClientView;
import com.planebattle.game.dto.PlaneDeploymentRequest;
import com.planebattle.game.dto.AttackResultResponse;
import com.planebattle.game.model.AttackRecord;
import com.planebattle.game.model.AttackResult;
import com.planebattle.game.model.GameRoom;
import com.planebattle.game.model.GameState;
import com.planebattle.game.model.GameStatus;
import com.planebattle.game.model.Plane;
import com.planebattle.game.model.PlanePart;
import com.planebattle.game.model.PlanePartType;
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
        return join(sessionId, null);
    }

    public synchronized ClientView join(String sessionId, String clientId) {
        PlayerSession restoredSession = findPlayerByClientId(clientId);
        if (restoredSession != null) {
            room.getSessions().remove(restoredSession.getSessionId());
            restoredSession.setSessionId(sessionId);
            restoredSession.setClientId(clientId);
            room.getSessions().put(sessionId, restoredSession);
            return buildClientView(restoredSession);
        }

        PlayerSession playerSession = room.getSessions().computeIfAbsent(sessionId, PlayerSession::new);
        playerSession.setClientId(clientId);
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

    public synchronized AttackResultResponse attack(String sessionId, int row, int col) {
        if (room.getGameState().getStatus() != GameStatus.PLAYING) {
            throw new IllegalArgumentException("Invalid game status.");
        }
        if (!isInBoard(row, col)) {
            throw new IllegalArgumentException("Attack target is out of board.");
        }

        PlayerSession playerSession = room.getSessions().get(sessionId);
        if (playerSession == null || playerSession.getRole() == PlayerRole.SPECTATOR || playerSession.getSide() == null) {
            throw new IllegalArgumentException("Only players can perform this action.");
        }
        if (room.getGameState().getCurrentTurn() != playerSession.getSide()) {
            throw new IllegalArgumentException("It is not your turn.");
        }

        PlayerSide attacker = playerSession.getSide();
        PlayerSide defender = opponentOf(attacker);
        PlayerBoard defenderBoard = boardOf(defender);
        if (defenderBoard == null) {
            throw new IllegalArgumentException("Opponent board is not ready.");
        }
        if (hasAlreadyAttacked(defenderBoard, row, col)) {
            throw new IllegalArgumentException("This cell has already been attacked.");
        }

        PlanePart hitPart = findPart(defenderBoard, row, col);
        AttackResult result = resolveAttackResult(hitPart);
        if (hitPart != null) {
            hitPart.setHit(true);
        }
        defenderBoard.getReceivedAttacks().add(attackRecord(row, col, result));

        if (allHeadsHit(defenderBoard)) {
            room.getGameState().setStatus(GameStatus.FINISHED);
            room.getGameState().setWinner(attacker);
            room.getGameState().setCurrentTurn(null);
        } else {
            room.getGameState().setCurrentTurn(defender);
        }

        AttackResultResponse response = new AttackResultResponse();
        response.setAttacker(attacker);
        response.setDefender(defender);
        response.setRow(row);
        response.setCol(col);
        response.setResult(result);
        return response;
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

        boolean revealAllPlanes = source.getStatus() == GameStatus.FINISHED;
        boolean canSeeAPlanes = revealAllPlanes || playerSession.getSide() == PlayerSide.A;
        boolean canSeeBPlanes = revealAllPlanes || playerSession.getSide() == PlayerSide.B;
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
        if (playerSession.getClientId() != null && !playerSession.getClientId().isBlank()) {
            return false;
        }
        if (room.getGameState().getStatus() == GameStatus.WAITING) {
            return true;
        }
        return false;
    }

    private PlayerSession findPlayerByClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return null;
        }
        if (hasClientId(room.getPlayerA(), clientId)) {
            return room.getPlayerA();
        }
        if (hasClientId(room.getPlayerB(), clientId)) {
            return room.getPlayerB();
        }
        return null;
    }

    private boolean hasClientId(PlayerSession playerSession, String clientId) {
        return playerSession != null && clientId.equals(playerSession.getClientId());
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

    private boolean isInBoard(int row, int col) {
        return row >= 0 && row < 10 && col >= 0 && col < 10;
    }

    private PlayerSide opponentOf(PlayerSide side) {
        return side == PlayerSide.A ? PlayerSide.B : PlayerSide.A;
    }

    private PlayerBoard boardOf(PlayerSide side) {
        return side == PlayerSide.A ? room.getGameState().getPlayerABoard() : room.getGameState().getPlayerBBoard();
    }

    private boolean hasAlreadyAttacked(PlayerBoard board, int row, int col) {
        return board.getReceivedAttacks().stream()
                .anyMatch(record -> record.getRow() == row && record.getCol() == col);
    }

    private PlanePart findPart(PlayerBoard board, int row, int col) {
        return board.getPlanes().stream()
                .map(Plane::getParts)
                .flatMap(List::stream)
                .filter(part -> part.getRow() == row && part.getCol() == col)
                .findFirst()
                .orElse(null);
    }

    private AttackResult resolveAttackResult(PlanePart part) {
        if (part == null) {
            return AttackResult.MISS;
        }
        return part.getType() == PlanePartType.HEAD ? AttackResult.HIT_HEAD : AttackResult.HIT_PLANE;
    }

    private AttackRecord attackRecord(int row, int col, AttackResult result) {
        AttackRecord record = new AttackRecord();
        record.setRow(row);
        record.setCol(col);
        record.setResult(result);
        return record;
    }

    private boolean allHeadsHit(PlayerBoard board) {
        return board.getPlanes().stream()
                .map(Plane::getParts)
                .flatMap(List::stream)
                .filter(part -> part.getType() == PlanePartType.HEAD)
                .allMatch(PlanePart::isHit);
    }
}
