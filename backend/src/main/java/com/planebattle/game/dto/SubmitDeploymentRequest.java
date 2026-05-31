package com.planebattle.game.dto;

import java.util.ArrayList;
import java.util.List;

public class SubmitDeploymentRequest {

    private List<PlaneDeploymentRequest> planes = new ArrayList<>();

    public List<PlaneDeploymentRequest> getPlanes() {
        return planes;
    }

    public void setPlanes(List<PlaneDeploymentRequest> planes) {
        this.planes = planes;
    }
}
