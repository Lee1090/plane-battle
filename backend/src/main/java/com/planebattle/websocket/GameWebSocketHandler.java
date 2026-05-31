package com.planebattle.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.planebattle.game.dto.AttackRequest;
import com.planebattle.game.dto.AttackResultResponse;
import com.planebattle.game.dto.ClientMessage;
import com.planebattle.game.dto.ClientView;
import com.planebattle.game.dto.JoinRequest;
import com.planebattle.game.dto.ServerMessage;
import com.planebattle.game.dto.SubmitDeploymentRequest;
import com.planebattle.game.model.PlayerSide;
import com.planebattle.game.service.GameService;
import com.planebattle.util.JsonUtils;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private static final String JOIN = "JOIN";
    private static final String SIT_DOWN = "SIT_DOWN";
    private static final String STAND_UP = "STAND_UP";
    private static final String SUBMIT_DEPLOYMENT = "SUBMIT_DEPLOYMENT";
    private static final String ATTACK = "ATTACK";
    private static final String CONNECTED = "CONNECTED";
    private static final String STATE_UPDATE = "STATE_UPDATE";
    private static final String ATTACK_RESULT = "ATTACK_RESULT";

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final GameService gameService;
    private final JsonUtils jsonUtils;

    public GameWebSocketHandler(GameService gameService, JsonUtils jsonUtils) {
        this.gameService = gameService;
        this.jsonUtils = jsonUtils;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        ClientMessage clientMessage;
        try {
            clientMessage = jsonUtils.fromJson(message.getPayload(), ClientMessage.class);
        } catch (JsonProcessingException exception) {
            send(session, ServerMessage.error("Invalid JSON message."));
            return;
        }

        if (JOIN.equals(clientMessage.getType())) {
            ClientView clientView = gameService.join(session.getId(), parseJoinRequest(clientMessage).getClientId());
            send(session, ServerMessage.data(CONNECTED, clientView));
            broadcastClientViews();
            return;
        }

        if (SIT_DOWN.equals(clientMessage.getType())) {
            try {
                PlayerSide side = parsePlayerSide(clientMessage);
                gameService.sitDown(session.getId(), side);
                broadcastClientViews();
            } catch (IllegalArgumentException exception) {
                send(session, ServerMessage.error(exception.getMessage()));
            }
            return;
        }

        if (STAND_UP.equals(clientMessage.getType())) {
            try {
                gameService.standUp(session.getId());
                broadcastClientViews();
            } catch (IllegalArgumentException exception) {
                send(session, ServerMessage.error(exception.getMessage()));
            }
            return;
        }

        if (SUBMIT_DEPLOYMENT.equals(clientMessage.getType())) {
            try {
                SubmitDeploymentRequest deploymentRequest = parseDeploymentRequest(clientMessage);
                gameService.submitDeployment(session.getId(), deploymentRequest.getPlanes());
                broadcastClientViews();
            } catch (JsonProcessingException exception) {
                send(session, ServerMessage.error("Invalid deployment payload."));
            } catch (IllegalArgumentException exception) {
                send(session, ServerMessage.error(exception.getMessage()));
            }
            return;
        }

        if (ATTACK.equals(clientMessage.getType())) {
            try {
                AttackRequest attackRequest = parseAttackRequest(clientMessage);
                AttackResultResponse attackResult = gameService.attack(
                        session.getId(),
                        attackRequest.getRow(),
                        attackRequest.getCol());
                broadcast(ServerMessage.data(ATTACK_RESULT, attackResult));
                broadcastClientViews();
            } catch (JsonProcessingException exception) {
                send(session, ServerMessage.error("Invalid attack payload."));
            } catch (IllegalArgumentException exception) {
                send(session, ServerMessage.error(exception.getMessage()));
            }
            return;
        }

        send(session, ServerMessage.error("Invalid message type."));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        gameService.leave(session.getId());
        broadcastClientViews();
    }

    private PlayerSide parsePlayerSide(ClientMessage clientMessage) {
        if (clientMessage.getData() == null || clientMessage.getData().get("side") == null) {
            throw new IllegalArgumentException("Player side is required.");
        }
        try {
            return PlayerSide.valueOf(clientMessage.getData().get("side").asText());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid player side.");
        }
    }

    private SubmitDeploymentRequest parseDeploymentRequest(ClientMessage clientMessage) throws JsonProcessingException {
        if (clientMessage.getData() == null) {
            throw new IllegalArgumentException("Deployment payload is required.");
        }
        return jsonUtils.fromJsonNode(clientMessage.getData(), SubmitDeploymentRequest.class);
    }

    private AttackRequest parseAttackRequest(ClientMessage clientMessage) throws JsonProcessingException {
        if (clientMessage.getData() == null) {
            throw new IllegalArgumentException("Attack payload is required.");
        }
        return jsonUtils.fromJsonNode(clientMessage.getData(), AttackRequest.class);
    }

    private JoinRequest parseJoinRequest(ClientMessage clientMessage) throws JsonProcessingException {
        if (clientMessage.getData() == null) {
            return new JoinRequest();
        }
        return jsonUtils.fromJsonNode(clientMessage.getData(), JoinRequest.class);
    }

    private void broadcastClientViews() {
        sessions.removeIf(session -> !session.isOpen());
        for (WebSocketSession session : sessions) {
            try {
                ClientView clientView = gameService.getClientView(session.getId());
                send(session, ServerMessage.data(STATE_UPDATE, clientView));
            } catch (IOException ignored) {
                sessions.remove(session);
            }
        }
    }

    private void broadcast(ServerMessage<?> message) {
        sessions.removeIf(session -> !session.isOpen());
        for (WebSocketSession session : sessions) {
            try {
                send(session, message);
            } catch (IOException ignored) {
                sessions.remove(session);
            }
        }
    }

    private void send(WebSocketSession session, ServerMessage<?> message) throws IOException {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(jsonUtils.toJson(message)));
        }
    }
}
