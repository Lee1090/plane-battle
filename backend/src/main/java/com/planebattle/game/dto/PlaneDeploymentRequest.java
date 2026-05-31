package com.planebattle.game.dto;

import com.planebattle.game.model.Cell;
import com.planebattle.game.model.Direction;

public class PlaneDeploymentRequest {

    private String id;
    private Cell head;
    private Direction direction;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Cell getHead() {
        return head;
    }

    public void setHead(Cell head) {
        this.head = head;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
