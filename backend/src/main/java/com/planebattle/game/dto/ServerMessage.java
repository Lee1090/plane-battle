package com.planebattle.game.dto;

public class ServerMessage<T> {

    private String type;
    private T data;
    private String error;

    public ServerMessage() {
    }

    public ServerMessage(String type, T data, String error) {
        this.type = type;
        this.data = data;
        this.error = error;
    }

    public static <T> ServerMessage<T> data(String type, T data) {
        return new ServerMessage<>(type, data, null);
    }

    public static ServerMessage<Void> error(String error) {
        return new ServerMessage<>("ERROR", null, error);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
