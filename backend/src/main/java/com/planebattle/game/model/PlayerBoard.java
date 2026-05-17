package com.planebattle.game.model;

import java.util.ArrayList;
import java.util.List;

public class PlayerBoard {

    private PlayerSide owner;
    private List<Plane> planes = new ArrayList<>();
    private List<AttackRecord> receivedAttacks = new ArrayList<>();

    public PlayerSide getOwner() {
        return owner;
    }

    public void setOwner(PlayerSide owner) {
        this.owner = owner;
    }

    public List<Plane> getPlanes() {
        return planes;
    }

    public void setPlanes(List<Plane> planes) {
        this.planes = planes;
    }

    public List<AttackRecord> getReceivedAttacks() {
        return receivedAttacks;
    }

    public void setReceivedAttacks(List<AttackRecord> receivedAttacks) {
        this.receivedAttacks = receivedAttacks;
    }
}
