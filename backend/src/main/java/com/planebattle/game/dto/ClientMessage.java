package com.planebattle.game.dto;

import com.fasterxml.jackson.databind.JsonNode;

public class ClientMessage {

    private String type;
    private JsonNode data;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }
}
