package com.planebattle.game.service;

import com.planebattle.game.dto.PlaneDeploymentRequest;
import com.planebattle.game.model.Plane;
import com.planebattle.game.model.PlanePart;
import com.planebattle.game.rule.PlaneShapeService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class DeploymentValidator {

    private static final int BOARD_SIZE = 10;
    private static final int REQUIRED_PLANE_COUNT = 3;
    private static final Set<String> REQUIRED_PLANE_IDS = Set.of("P1", "P2", "P3");

    private final PlaneShapeService planeShapeService;

    public DeploymentValidator(PlaneShapeService planeShapeService) {
        this.planeShapeService = planeShapeService;
    }

    public List<Plane> validateAndBuildPlanes(List<PlaneDeploymentRequest> requests) {
        if (requests == null || requests.size() != REQUIRED_PLANE_COUNT) {
            throw new IllegalArgumentException("Deployment requires exactly 3 planes.");
        }

        List<Plane> planes = new ArrayList<>();
        Set<String> occupiedCells = new HashSet<>();
        Set<String> planeIds = new HashSet<>();

        for (PlaneDeploymentRequest request : requests) {
            validateRequest(request, planeIds);
            List<PlanePart> parts = planeShapeService.buildParts(request.getHead(), request.getDirection());
            if (parts.size() != 10) {
                throw new IllegalArgumentException("Invalid plane shape.");
            }

            for (PlanePart part : parts) {
                validateInBoard(part);
                String cellKey = part.getRow() + ":" + part.getCol();
                if (!occupiedCells.add(cellKey)) {
                    throw new IllegalArgumentException("Planes cannot overlap.");
                }
            }

            Plane plane = new Plane();
            plane.setId(request.getId());
            plane.setHead(request.getHead());
            plane.setDirection(request.getDirection());
            plane.setParts(parts);
            planes.add(plane);
        }
        if (!planeIds.equals(REQUIRED_PLANE_IDS)) {
            throw new IllegalArgumentException("Deployment requires plane ids P1, P2, and P3.");
        }

        return planes;
    }

    private void validateRequest(PlaneDeploymentRequest request, Set<String> planeIds) {
        if (request == null || request.getId() == null || request.getId().isBlank()
                || request.getHead() == null || request.getDirection() == null) {
            throw new IllegalArgumentException("Each plane requires id, head, and direction.");
        }
        if (!planeIds.add(request.getId())) {
            throw new IllegalArgumentException("Plane ids must be unique.");
        }
    }

    private void validateInBoard(PlanePart part) {
        if (part.getRow() < 0 || part.getRow() >= BOARD_SIZE || part.getCol() < 0 || part.getCol() >= BOARD_SIZE) {
            throw new IllegalArgumentException("Plane is out of board.");
        }
    }
}
