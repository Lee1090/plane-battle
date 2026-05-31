package com.planebattle.game.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.planebattle.game.dto.ClientView;
import com.planebattle.game.dto.PlaneDeploymentRequest;
import com.planebattle.game.model.Cell;
import com.planebattle.game.model.Direction;
import com.planebattle.game.model.GameStatus;
import com.planebattle.game.model.PlayerRole;
import com.planebattle.game.model.PlayerSide;
import com.planebattle.game.rule.PlaneShapeService;
import java.util.List;
import org.junit.jupiter.api.Test;

class GameServiceTest {

    @Test
    void joinCreatesSpectatorView() {
        GameService gameService = createGameService();

        ClientView view = gameService.join("session-1");

        assertThat(view.getRole()).isEqualTo(PlayerRole.SPECTATOR);
        assertThat(view.getSide()).isNull();
        assertThat(view.getGameState().getStatus()).isEqualTo(GameStatus.WAITING);
    }

    @Test
    void sitDownAssignsSeatAndMovesToDeployingWhenBothSeatsAreTaken() {
        GameService gameService = createGameService();
        gameService.join("session-a");
        gameService.join("session-b");

        ClientView playerAView = gameService.sitDown("session-a", PlayerSide.A);
        ClientView playerBView = gameService.sitDown("session-b", PlayerSide.B);

        assertThat(playerAView.getRole()).isEqualTo(PlayerRole.PLAYER_A);
        assertThat(playerAView.getSide()).isEqualTo(PlayerSide.A);
        assertThat(playerAView.getGameState().isPlayerASeated()).isTrue();
        assertThat(playerBView.getRole()).isEqualTo(PlayerRole.PLAYER_B);
        assertThat(playerBView.getSide()).isEqualTo(PlayerSide.B);
        assertThat(playerBView.getGameState().isPlayerBSeated()).isTrue();
        assertThat(gameService.getGameState().getStatus()).isEqualTo(GameStatus.DEPLOYING);
    }

    @Test
    void sitDownRejectsTakenSeat() {
        GameService gameService = createGameService();
        gameService.sitDown("session-a", PlayerSide.A);

        assertThatThrownBy(() -> gameService.sitDown("session-b", PlayerSide.A))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This seat is already taken.");
    }

    @Test
    void standUpClearsSeatAndReturnsToWaiting() {
        GameService gameService = createGameService();
        gameService.sitDown("session-a", PlayerSide.A);
        gameService.sitDown("session-b", PlayerSide.B);

        ClientView view = gameService.standUp("session-a");

        assertThat(view.getRole()).isEqualTo(PlayerRole.SPECTATOR);
        assertThat(view.getSide()).isNull();
        assertThat(gameService.getGameState().isPlayerASeated()).isFalse();
        assertThat(gameService.getGameState().isPlayerBSeated()).isTrue();
        assertThat(gameService.getGameState().getStatus()).isEqualTo(GameStatus.WAITING);
    }

    @Test
    void leaveClearsSeatBeforePlayingStarts() {
        GameService gameService = createGameService();
        gameService.sitDown("session-a", PlayerSide.A);

        gameService.leave("session-a");

        assertThat(gameService.getGameState().isPlayerASeated()).isFalse();
        assertThat(gameService.getGameState().getStatus()).isEqualTo(GameStatus.WAITING);
    }

    @Test
    void submitDeploymentStoresOwnBoardAndHidesItFromOpponent() {
        GameService gameService = createGameService();
        gameService.sitDown("session-a", PlayerSide.A);
        gameService.sitDown("session-b", PlayerSide.B);

        ClientView playerAView = gameService.submitDeployment("session-a", validDeployment());
        ClientView playerBView = gameService.getClientView("session-b");
        ClientView spectatorView = gameService.getClientView("unknown-session");

        assertThat(playerAView.getGameState().isPlayerAReady()).isTrue();
        assertThat(playerAView.getGameState().getPlayerABoard().getPlanes()).hasSize(3);
        assertThat(playerBView.getGameState().getPlayerABoard().getPlanes()).isEmpty();
        assertThat(spectatorView.getGameState().getPlayerABoard().getPlanes()).isEmpty();
    }

    @Test
    void submitDeploymentMovesToPlayingWhenBothPlayersReady() {
        GameService gameService = createGameService();
        gameService.sitDown("session-a", PlayerSide.A);
        gameService.sitDown("session-b", PlayerSide.B);

        gameService.submitDeployment("session-a", validDeployment());
        gameService.submitDeployment("session-b", validDeployment());

        assertThat(gameService.getGameState().getStatus()).isEqualTo(GameStatus.PLAYING);
        assertThat(gameService.getGameState().getCurrentTurn()).isEqualTo(PlayerSide.A);
    }

    @Test
    void submitDeploymentRejectsOutOfBoardPlane() {
        GameService gameService = createGameService();
        gameService.sitDown("session-a", PlayerSide.A);
        gameService.sitDown("session-b", PlayerSide.B);

        List<PlaneDeploymentRequest> deployment = List.of(
                plane("P1", 0, 0, Direction.UP),
                plane("P2", 6, 6, Direction.DOWN),
                plane("P3", 6, 0, Direction.RIGHT));

        assertThatThrownBy(() -> gameService.submitDeployment("session-a", deployment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Plane is out of board.");
    }

    @Test
    void submitDeploymentRejectsOverlappingPlanes() {
        GameService gameService = createGameService();
        gameService.sitDown("session-a", PlayerSide.A);
        gameService.sitDown("session-b", PlayerSide.B);

        List<PlaneDeploymentRequest> deployment = List.of(
                plane("P1", 0, 2, Direction.UP),
                plane("P2", 0, 2, Direction.UP),
                plane("P3", 4, 1, Direction.RIGHT));

        assertThatThrownBy(() -> gameService.submitDeployment("session-a", deployment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Planes cannot overlap.");
    }

    @Test
    void submitDeploymentRejectsDuplicateSubmission() {
        GameService gameService = createGameService();
        gameService.sitDown("session-a", PlayerSide.A);
        gameService.sitDown("session-b", PlayerSide.B);
        gameService.submitDeployment("session-a", validDeployment());

        assertThatThrownBy(() -> gameService.submitDeployment("session-a", validDeployment()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Deployment has already been submitted.");
    }

    @Test
    void leaveKeepsSubmittedDeploymentDuringDeploying() {
        GameService gameService = createGameService();
        gameService.join("session-a", "client-a");
        gameService.join("session-b", "client-b");
        gameService.sitDown("session-a", PlayerSide.A);
        gameService.sitDown("session-b", PlayerSide.B);
        gameService.submitDeployment("session-a", validDeployment());

        gameService.leave("session-a");

        assertThat(gameService.getGameState().getStatus()).isEqualTo(GameStatus.DEPLOYING);
        assertThat(gameService.getGameState().isPlayerASeated()).isTrue();
        assertThat(gameService.getGameState().isPlayerAReady()).isTrue();
        assertThat(gameService.getGameState().getPlayerABoard()).isNotNull();
    }

    @Test
    void joinRestoresPlayerByClientIdDuringDeploying() {
        GameService gameService = createGameService();
        gameService.join("session-a-old", "client-a");
        gameService.join("session-b", "client-b");
        gameService.sitDown("session-a-old", PlayerSide.A);
        gameService.sitDown("session-b", PlayerSide.B);
        gameService.submitDeployment("session-a-old", validDeployment());

        gameService.leave("session-a-old");
        ClientView restoredView = gameService.join("session-a-new", "client-a");

        assertThat(restoredView.getRole()).isEqualTo(PlayerRole.PLAYER_A);
        assertThat(restoredView.getSide()).isEqualTo(PlayerSide.A);
        assertThat(restoredView.getGameState().isPlayerASeated()).isTrue();
        assertThat(restoredView.getGameState().isPlayerAReady()).isTrue();
        assertThat(restoredView.getGameState().getPlayerABoard().getPlanes()).hasSize(3);
    }

    @Test
    void joinRestoresUnsubmittedPlayerSeatByClientIdDuringDeploying() {
        GameService gameService = createGameService();
        gameService.join("session-a-old", "client-a");
        gameService.join("session-b", "client-b");
        gameService.sitDown("session-a-old", PlayerSide.A);
        gameService.sitDown("session-b", PlayerSide.B);

        gameService.leave("session-a-old");
        ClientView restoredView = gameService.join("session-a-new", "client-a");

        assertThat(restoredView.getRole()).isEqualTo(PlayerRole.PLAYER_A);
        assertThat(restoredView.getSide()).isEqualTo(PlayerSide.A);
        assertThat(restoredView.getGameState().isPlayerASeated()).isTrue();
        assertThat(restoredView.getGameState().isPlayerAReady()).isFalse();
    }

    @Test
    void joinRestoresPlayerSeatByClientIdWhileWaiting() {
        GameService gameService = createGameService();
        gameService.join("session-a-old", "client-a");
        gameService.sitDown("session-a-old", PlayerSide.A);

        gameService.leave("session-a-old");
        ClientView restoredView = gameService.join("session-a-new", "client-a");

        assertThat(restoredView.getRole()).isEqualTo(PlayerRole.PLAYER_A);
        assertThat(restoredView.getSide()).isEqualTo(PlayerSide.A);
        assertThat(restoredView.getGameState().getStatus()).isEqualTo(GameStatus.WAITING);
        assertThat(restoredView.getGameState().isPlayerASeated()).isTrue();
    }

    @Test
    void submitDeploymentRejectsUnexpectedPlaneIds() {
        GameService gameService = createGameService();
        gameService.sitDown("session-a", PlayerSide.A);
        gameService.sitDown("session-b", PlayerSide.B);
        List<PlaneDeploymentRequest> deployment = List.of(
                plane("P1", 0, 2, Direction.UP),
                plane("P2", 6, 6, Direction.DOWN),
                plane("PX", 6, 3, Direction.RIGHT));

        assertThatThrownBy(() -> gameService.submitDeployment("session-a", deployment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Deployment requires plane ids P1, P2, and P3.");
    }

    private GameService createGameService() {
        PlaneShapeService planeShapeService = new PlaneShapeService();
        return new GameService(new DeploymentValidator(planeShapeService));
    }

    private List<PlaneDeploymentRequest> validDeployment() {
        return List.of(
                plane("P1", 0, 2, Direction.UP),
                plane("P2", 6, 6, Direction.DOWN),
                plane("P3", 6, 3, Direction.RIGHT));
    }

    private PlaneDeploymentRequest plane(String id, int row, int col, Direction direction) {
        PlaneDeploymentRequest request = new PlaneDeploymentRequest();
        request.setId(id);
        request.setHead(cell(row, col));
        request.setDirection(direction);
        return request;
    }

    private Cell cell(int row, int col) {
        Cell cell = new Cell();
        cell.setRow(row);
        cell.setCol(col);
        return cell;
    }
}
