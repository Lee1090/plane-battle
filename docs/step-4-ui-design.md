# Step 4 UI Design: Fixed Two-Board Table

## Goal

Step 4 should establish a stable game-table layout.
The full usable game surface should fit in one browser viewport without requiring vertical scrolling during normal play.

The board positions are fixed:

```text
left board:  Player A board
right board: Player B board
```

This is a table view, not a player-relative view.
The layout must not swap boards based on who is viewing the page.

## Layout Model

```text
Header
  title, language, connection/status summary

Game Table
  Player A Board                Player B Board
  Player A seat/action area     Player B seat/action area
  Player A controls area        Player B controls area
```

Board ordering:

| Viewer | Left Board | Right Board |
|---|---|---|
| Player A | Player A board, own board | Player B board, opponent board |
| Player B | Player A board, opponent board | Player B board, own board |
| Spectator | Player A board | Player B board |

Each player operates the board attached to their seat:

- Player A sits on the left and deploys/acts on the left board.
- Player B sits on the right and deploys/acts on the right board.
- Spectators see the same fixed A/B table as everyone else.

## Fixed Layout Rules

The UI must avoid layout shifts when the game state changes.

1. The two boards keep the same positions and dimensions in `WAITING`, `DEPLOYING`, `PLAYING`, and `FINISHED`.
2. Seat buttons live directly below their corresponding board.
3. Action controls live in reserved areas below or near their corresponding board.
4. Showing deployment controls, ready text, attack controls, or spectator text must not move the boards.
5. The page should use a `100vh` layout and responsive board sizing so the main content fits without scrolling.
6. Board size should be computed with stable constraints such as `min()`, `clamp()`, or container-based sizing.

## State Display

| Game Status | Player A Board | Player B Board | Seat/Control Areas |
|---|---|---|---|
| `WAITING` | Empty/read-only board | Empty/read-only board | Sit-down button under each board |
| `DEPLOYING` | Editable only for Player A | Editable only for Player B | Deployment controls appear in each player's reserved area |
| `PLAYING` | Shows A's own planes to A, public info to B/spectators | Shows B's own planes to B, public info to A/spectators | Attack controls appear in the current player's reserved area |
| `FINISHED` | Full/replay board | Full/replay board | Result and reset controls |

Visibility rules:

- A player can see their own plane positions.
- A player cannot see the opponent's hidden plane positions.
- Spectators cannot see hidden plane positions during `DEPLOYING` or `PLAYING`.
- Hidden boards still render the full 10 x 10 grid/frame with a light overlay. They should not collapse into blank space.
- After `FINISHED`, full board reveal is allowed for replay/review.

## Deployment Interaction

During `DEPLOYING`, a player deploys directly on the fixed board attached to their seat.

Required behavior:

1. Player A places planes on the left board.
2. Player B places planes on the right board.
3. Select plane `P1`, `P2`, or `P3`.
4. Rotate or choose direction: `UP`, `RIGHT`, `DOWN`, `LEFT`.
5. Click an empty board cell to place the next undeployed plane head in order: first `P1`, then `P2`, then `P3`.
6. Show the plane shape preview on the same fixed board.
7. Mark invalid placement, such as out-of-board or overlap, with an error style and a reason-specific message.
8. Enable submit only when all 3 planes are placed legally.
9. After submit, the player's board becomes read-only.
10. During deployment, keyboard arrow keys can update the selected plane direction after a head is placed.
11. During deployment, a player can drag an already placed plane head to another cell to move that plane while keeping its direction. The drag preview should show the whole plane, not only the head cell.
12. Clicking an already placed plane head should select that plane as the current deployment focus instead of moving another plane there.
13. Clicking an empty cell after all three planes are already placed should not move the focused plane or change the board.

Direction semantics:

- `UP` means the head is above the body.
- `DOWN` means the head is below the body.
- `LEFT` means the head is on the left and the body extends to the right.
- `RIGHT` means the head is on the right and the body extends to the left.

Keyboard direction shortcuts:

- Left arrow sets direction to `RIGHT`, meaning the head is on the right and the body extends left.
- Right arrow sets direction to `LEFT`, meaning the head is on the left and the body extends right.
- Up arrow sets direction to `DOWN`, meaning the head is below the body and the body extends up.
- Down arrow sets direction to `UP`, meaning the head is above the body and the body extends down.

Plane color rules:

- Plane heads use one shared head color so head cells stay easy to identify.
- Non-head cells use one uniform body color within the same plane.
- `P1`, `P2`, and `P3` use different body colors.

Deployment controls should not contain a separate board.
They should only contain current-stage controls such as plane selection, direction/rotate, status text, and submit.

## Seat Buttons

Seat actions are part of the fixed board layout:

```text
Player A board
Sit as Player A / Stand up / Ready state

Player B board
Sit as Player B / Stand up / Ready state
```

The sit-down button for a side should be directly under that side's board.
The action area should keep a stable height even when the button changes to status text or deployment controls.

## Spectator Logic

Spectators can see:

- Player A / Player B seat state.
- Player A / Player B deployment ready state.
- Current game status.
- Public attack records in later steps.

Spectators cannot:

- Deploy planes.
- Attack cells.
- See plane positions during `DEPLOYING`.
- See hidden plane positions during `PLAYING`.

## Recommended Components

The implementation can evolve toward:

```text
GameTable
  BoardSlot side=A
    BoardPanel
    SeatAction
    SideControlPanel

  BoardSlot side=B
    BoardPanel
    SeatAction
    SideControlPanel
```

Notes:

- `BoardGrid` is the reusable 10 x 10 grid.
- `BoardSlot` owns the fixed side-specific layout.
- `SeatAction` owns sit/stand/ready state for that side.
- `SideControlPanel` owns deployment or attack controls for that side.
- Controls can be hidden or disabled, but their reserved space should remain stable.

## Design Decision

The final Step 4 UI direction is:

> Use a fixed two-board table. The left board is always Player A, and the right board is always Player B. Players operate the board attached to their seat. The board positions and reserved control areas must not move as the game state changes.
