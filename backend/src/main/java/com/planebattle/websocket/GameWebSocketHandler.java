package com.planebattle.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.planebattle.game.dto.ClientMessage;
import com.planebattle.game.dto.ClientView;
import com.planebattle.game.dto.ServerMessage;
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
    private static final String CONNECTED = "CONNECTED";
    private static final String STATE_UPDATE = "STATE_UPDATE";

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
            ClientView clientView = gameService.join(session.getId());
            send(session, ServerMessage.data(CONNECTED, clientView));
            broadcast(ServerMessage.data(STATE_UPDATE, gameService.getGameState()));
            return;
        }

        send(session, ServerMessage.error("Invalid message type."));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
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
