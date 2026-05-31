package com.planebattle.game.rule;

import static org.assertj.core.api.Assertions.assertThat;

import com.planebattle.game.model.Cell;
import com.planebattle.game.model.Direction;
import com.planebattle.game.model.PlanePart;
import com.planebattle.game.model.PlanePartType;
import org.junit.jupiter.api.Test;

class PlaneShapeServiceTest {

    private final PlaneShapeService planeShapeService = new PlaneShapeService();

    @Test
    void leftDirectionPlacesBodyToTheRightOfHead() {
        PlanePart body = bodyPart(Direction.LEFT);

        assertThat(body.getRow()).isEqualTo(5);
        assertThat(body.getCol()).isEqualTo(7);
    }

    @Test
    void rightDirectionPlacesBodyToTheLeftOfHead() {
        PlanePart body = bodyPart(Direction.RIGHT);

        assertThat(body.getRow()).isEqualTo(5);
        assertThat(body.getCol()).isEqualTo(3);
    }

    private PlanePart bodyPart(Direction direction) {
        Cell head = new Cell();
        head.setRow(5);
        head.setCol(5);

        return planeShapeService.buildParts(head, direction).stream()
                .filter(part -> part.getType() == PlanePartType.BODY)
                .findFirst()
                .orElseThrow();
    }
}
