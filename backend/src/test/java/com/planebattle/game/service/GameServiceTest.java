package com.planebattle.game.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.planebattle.game.dto.ClientView;
import com.planebattle.game.model.GameStatus;
import com.planebattle.game.model.PlayerRole;
import com.planebattle.game.model.PlayerSide;
import org.junit.jupiter.api.Test;

class GameServiceTest {

    @Test
    void joinCreatesSpectatorView() {
        GameService gameService = new GameService();

        ClientView view = gameService.join("session-1");

        assertThat(view.getRole()).isEqualTo(PlayerRole.SPECTATOR);
        assertThat(view.getSide()).isNull();
        assertThat(view.getGameState().getStatus()).isEqualTo(GameStatus.WAITING);
    }

    @Test
    void sitDownAssignsSeatAndMovesToDeployingWhenBothSeatsAreTaken() {
        GameService gameService = new GameService();
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
        GameService gameService = new GameService();
        gameService.sitDown("session-a", PlayerSide.A);

        assertThatThrownBy(() -> gameService.sitDown("session-b", PlayerSide.A))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This seat is already taken.");
    }

    @Test
    void standUpClearsSeatAndReturnsToWaiting() {
        GameService gameService = new GameService();
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
        GameService gameService = new GameService();
        gameService.sitDown("session-a", PlayerSide.A);

        gameService.leave("session-a");

        assertThat(gameService.getGameState().isPlayerASeated()).isFalse();
        assertThat(gameService.getGameState().getStatus()).isEqualTo(GameStatus.WAITING);
    }
}
