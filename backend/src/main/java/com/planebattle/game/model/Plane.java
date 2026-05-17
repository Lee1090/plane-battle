package com.planebattle.game.model;

import java.util.ArrayList;
import java.util.List;

public class Plane {

    private String id;
    private Cell head;
    private Direction direction;
    private List<PlanePart> parts = new ArrayList<>();

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

    public List<PlanePart> getParts() {
        return parts;
    }

    public void setParts(List<PlanePart> parts) {
        this.parts = parts;
    }
}
