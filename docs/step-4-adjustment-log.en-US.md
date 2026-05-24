# Step 4 Adjustment Log

This document records UI and interaction refinements made during Step 4 deployment implementation.
The canonical design rules remain in `docs/step-4-ui-design.md`; this file mainly preserves the adjustment history and decision context.

## Scope

Step 4 focuses on the deployment stage and the fixed two-board game table.
The goal is to make deployment clear, stable, and easy to operate without changing board positions during player actions.

## Layout Refinements

- Use a fixed two-board table layout.
- The left board is always Player A.
- The right board is always Player B.
- Players operate the board attached to their seat.
- Sit / stand buttons stay directly below the corresponding board.
- Deployment controls stay in reserved areas so board positions do not move when controls appear or change.
- The normal desktop browser view should show the full usable surface without vertical scrolling.
- When opponent or spectator visibility hides planes, the full 10 x 10 board frame still renders with an overlay instead of leaving blank space.

## Plane Direction Rules

- Direction labels describe the plane head direction.
- `UP` means the head is above the body and the body extends downward.
- `DOWN` means the head is below the body and the body extends upward.
- `LEFT` means the head is on the left and the body extends to the right.
- `RIGHT` means the head is on the right and the body extends to the left.
- Frontend and backend rotation logic were aligned to this rule.
- Backend tests were added for left and right direction behavior.

## Plane Color Rules

- All plane heads use one shared color so heads remain easy to identify.
- Non-head cells use one uniform body color within the same plane.
- `P1`, `P2`, and `P3` use different body colors.
- The `P2` body color was adjusted to a blue tone for better visual balance.

## Deployment Click Behavior

- Clicking an empty cell no longer moves the currently focused plane.
- Empty-cell clicks now place the next undeployed plane in order:
  - first placement is `P1`
  - second placement is `P2`
  - third placement is `P3`
- After all three planes are placed, empty-cell clicks do nothing.
- Clicking an already placed plane head selects that plane as the current deployment focus.
- Clicking an occupied non-head cell does not move a plane or place a new one.

## Keyboard Direction Controls

- The direction dropdown remains available.
- Arrow keys were added during deployment to quickly update the focused plane direction.
- Left arrow sets direction to `RIGHT`, meaning the head is on the right and the body extends left.
- Right arrow sets direction to `LEFT`, meaning the head is on the left and the body extends right.
- Up arrow sets direction to `DOWN`, meaning the head is below the body and the body extends up.
- Down arrow sets direction to `UP`, meaning the head is above the body and the body extends down.
- Keyboard shortcuts only work while the player is deploying on their own board and has not submitted deployment.

## Drag Interaction

- Players can drag an already placed plane head to another cell.
- Dragging moves that plane while keeping its direction.
- Dragging is only available during the player's own deployment phase before submission.
- The drag preview was changed from head-only to whole-plane preview.
- The drag preview keeps the plane shape, direction, body color, and head label.
- The drag preview head keeps a clear border so the head position remains visible while dragging.

## Hover Behavior

- Empty cells keep hover feedback.
- Cells occupied by any plane part no longer change color on hover.
- This avoids hover feedback overriding plane colors and keeps board state visually stable.

## Localization

- The language selector label uses bilingual text: `语言 / Language`.
- Direction options are localized.
- English keeps `UP`, `RIGHT`, `DOWN`, `LEFT`.
- Chinese displays explicit head-direction labels:
  - `机头向上`
  - `机头向右`
  - `机头向下`
  - `机头向左`

## Verification

- Frontend build passed after deployment interaction refinements.
- Backend tests passed after deployment validation and direction rule changes.
- Browser layout check confirmed the fixed two-board layout fits a 1280 x 720 viewport without vertical scrolling during normal use.
