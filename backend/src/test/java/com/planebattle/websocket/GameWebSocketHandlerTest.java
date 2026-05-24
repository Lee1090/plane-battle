package com.planebattle.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planebattle.game.rule.PlaneShapeService;
import com.planebattle.game.service.DeploymentValidator;
import com.planebattle.game.service.GameService;
import com.planebattle.util.JsonUtils;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

class GameWebSocketHandlerTest {

    @Test
    void broadcastsDeploymentUpdatesAndHidesOpponentPlanes() throws Exception {
        GameWebSocketHandler handler = createHandler();
        WebSocketSession playerA = session("player-a");
        WebSocketSession playerB = session("player-b");
        handler.afterConnectionEstablished(playerA);
        handler.afterConnectionEstablished(playerB);

        handle(handler, playerA, "{\"type\":\"JOIN\"}");
        handle(handler, playerB, "{\"type\":\"JOIN\"}");
        clearInvocations(playerA, playerB);

        handle(handler, playerA, "{\"type\":\"SIT_DOWN\",\"data\":{\"side\":\"A\"}}");
        handle(handler, playerB, "{\"type\":\"SIT_DOWN\",\"data\":{\"side\":\"B\"}}");

        String playerADeployingView = latestPayload(playerA);
        String playerBDeployingView = latestPayload(playerB);
        assertThat(playerADeployingView).contains("\"role\":\"PLAYER_A\"", "\"status\":\"DEPLOYING\"");
        assertThat(playerBDeployingView).contains("\"role\":\"PLAYER_B\"", "\"status\":\"DEPLOYING\"");
        clearInvocations(playerA, playerB);

        handle(handler, playerA, deploymentMessage());

        String playerAReadyView = latestPayload(playerA);
        String playerBReadyView = latestPayload(playerB);
        assertThat(playerAReadyView).contains("\"playerAReady\":true", "\"planes\":[{\"id\":\"P1\"");
        assertThat(playerBReadyView).contains("\"playerAReady\":true", "\"playerABoard\":{\"owner\":\"A\",\"planes\":[]");
        clearInvocations(playerA, playerB);

        handle(handler, playerB, deploymentMessage());

        String playerAPlayingView = latestPayload(playerA);
        String playerBPlayingView = latestPayload(playerB);
        assertThat(playerAPlayingView).contains("\"status\":\"PLAYING\"", "\"currentTurn\":\"A\"");
        assertThat(playerBPlayingView).contains("\"status\":\"PLAYING\"", "\"currentTurn\":\"A\"");
        assertThat(playerAPlayingView).contains("\"playerBBoard\":{\"owner\":\"B\",\"planes\":[]");
        assertThat(playerBPlayingView).contains("\"playerABoard\":{\"owner\":\"A\",\"planes\":[]");
    }

    private GameWebSocketHandler createHandler() {
        PlaneShapeService planeShapeService = new PlaneShapeService();
        DeploymentValidator deploymentValidator = new DeploymentValidator(planeShapeService);
        GameService gameService = new GameService(deploymentValidator);
        JsonUtils jsonUtils = new JsonUtils(new ObjectMapper());
        return new GameWebSocketHandler(gameService, jsonUtils);
    }

    private WebSocketSession session(String id) {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn(id);
        when(session.isOpen()).thenReturn(true);
        return session;
    }

    private void handle(GameWebSocketHandler handler, WebSocketSession session, String payload) throws Exception {
        handler.handleMessage(session, new TextMessage(payload));
    }

    private String latestPayload(WebSocketSession session) throws IOException {
        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, atLeastOnce()).sendMessage(captor.capture());
        return captor.getAllValues().get(captor.getAllValues().size() - 1).getPayload();
    }

    private String deploymentMessage() {
        return """
                {"type":"SUBMIT_DEPLOYMENT","data":{"planes":[
                {"id":"P1","head":{"row":0,"col":2},"direction":"UP"},
                {"id":"P2","head":{"row":6,"col":6},"direction":"DOWN"},
                {"id":"P3","head":{"row":6,"col":3},"direction":"RIGHT"}
                ]}}
                """;
    }
}
