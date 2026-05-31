# Step 5 Attack Phase Design

## Goal

Step 5 adds the turn-based battle phase after both players submit valid deployments.

This step focuses on attack rules, attack result visibility, turn switching, and win detection. It does not add room management, replay, rematch, matchmaking, animations, or configurable battle rules.

## Game Flow

1. Both players sit down.
2. Both players submit valid deployments.
3. The game enters `PLAYING`.
4. `currentTurn` starts from player A.
5. The current player attacks one cell on the opponent board.
6. The backend validates and resolves the attack.
7. If the defender still has at least one unhit head, the turn switches.
8. If all defender heads are hit, the game enters `FINISHED` and `winner` is set to the attacker.

## Backend Rules

The backend handles `ATTACK` messages with this payload:

```json
{
  "type": "ATTACK",
  "data": {
    "row": 3,
    "col": 5
  }
}
```

Validation rules:

- Game status must be `PLAYING`.
- The sender must be player A or player B.
- The sender must match `currentTurn`.
- `row` and `col` must be inside the 10x10 board.
- The target cell must be on the opponent board.
- The target cell must not have been attacked before.

Resolution rules:

- No plane part at target cell: `MISS`.
- Plane part at target cell and part type is `HEAD`: `HIT_HEAD`.
- Plane part at target cell and part type is not `HEAD`: `HIT_PLANE`.

After a valid attack:

- The attack is appended to the defender board's `receivedAttacks`.
- If a plane part is hit, its `hit` flag is set to `true`.
- If all defender heads are hit, status becomes `FINISHED`, `winner` becomes the attacker, and `currentTurn` becomes `null`.
- Otherwise, `currentTurn` switches to the defender.

## WebSocket Messages

On valid attack, the server broadcasts:

1. `ATTACK_RESULT`
2. `STATE_UPDATE`

`ATTACK_RESULT` data:

```json
{
  "attacker": "A",
  "defender": "B",
  "row": 3,
  "col": 5,
  "result": "MISS"
}
```

Invalid attacks return `ERROR` only to the requester.

## Frontend Behavior

The Step 4 fixed two-board layout remains unchanged:

- Player A board stays on the left.
- Player B board stays on the right.
- Players operate from their seated side.
- Spectators see both board positions but cannot operate.

During `PLAYING`:

- A player attacks by clicking a cell on the opponent board.
- A player cannot attack their own board.
- A player cannot attack when it is not their turn.
- A player cannot attack a previously attacked cell.
- Own board shows own planes and opponent attack records.
- Opponent board hides planes and shows public attack records.
- Spectators see public attack records on both boards but no hidden planes.

During `FINISHED`:

- The winner is shown in the side control area.
- Full boards may be revealed because the game is over.

## Test Focus

Backend tests should cover:

- Reject attack before `PLAYING`.
- Reject spectator attack.
- Reject attack from the non-current player.
- Reject out-of-board attack.
- Reject repeated attack.
- `MISS` records attack and switches turn.
- `HIT_PLANE` marks a non-head part as hit and switches turn.
- `HIT_HEAD` marks the head as hit.
- Hitting the third defender head finishes the game and sets `winner`.
- Opponent plane positions remain hidden before `FINISHED`.

Frontend verification should cover:

- Build succeeds.
- Opponent board click sends `ATTACK`.
- Current-turn player can attack.
- Waiting player and spectator cannot attack.
- Attack result markers render on the correct board.
