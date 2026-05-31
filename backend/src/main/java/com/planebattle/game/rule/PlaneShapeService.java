package com.planebattle.game.rule;

import com.planebattle.game.model.Cell;
import com.planebattle.game.model.Direction;
import com.planebattle.game.model.PlanePart;
import com.planebattle.game.model.PlanePartType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PlaneShapeService {

    private static final List<RelativePlanePart> UP_SHAPE = List.of(
            new RelativePlanePart(PlanePartType.HEAD, 0, 0),
            new RelativePlanePart(PlanePartType.WING, 1, -2),
            new RelativePlanePart(PlanePartType.WING, 1, -1),
            new RelativePlanePart(PlanePartType.WING, 1, 0),
            new RelativePlanePart(PlanePartType.WING, 1, 1),
            new RelativePlanePart(PlanePartType.WING, 1, 2),
            new RelativePlanePart(PlanePartType.BODY, 2, 0),
            new RelativePlanePart(PlanePartType.TAIL, 3, -1),
            new RelativePlanePart(PlanePartType.TAIL, 3, 0),
            new RelativePlanePart(PlanePartType.TAIL, 3, 1));

    public List<PlanePart> buildParts(Cell head, Direction direction) {
        List<PlanePart> parts = new ArrayList<>();
        for (RelativePlanePart relativePart : UP_SHAPE) {
            int[] rotated = rotate(relativePart.rowOffset(), relativePart.colOffset(), direction);
            PlanePart part = new PlanePart();
            part.setType(relativePart.type());
            part.setRow(head.getRow() + rotated[0]);
            part.setCol(head.getCol() + rotated[1]);
            part.setHit(false);
            parts.add(part);
        }
        return parts;
    }

    private int[] rotate(int rowOffset, int colOffset, Direction direction) {
        return switch (direction) {
            case UP -> new int[] {rowOffset, colOffset};
            case DOWN -> new int[] {-rowOffset, -colOffset};
            case LEFT -> new int[] {colOffset, rowOffset};
            case RIGHT -> new int[] {-colOffset, -rowOffset};
        };
    }

    private record RelativePlanePart(PlanePartType type, int rowOffset, int colOffset) {
    }
}
