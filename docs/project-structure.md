# Project Structure

```text
plane-battle/
|-- frontend/                         # React frontend
|   |-- src/
|   |   |-- components/
|   |   |   |-- DeploymentPanel/
|   |   |   |   `-- DeploymentPanel.tsx
|   |   |   |-- GameBoardArea/
|   |   |   |   |-- GameBoardArea.tsx
|   |   |   |   |-- deploymentDraftStorage.ts
|   |   |   |   |-- deploymentInteraction.ts
|   |   |   |   `-- planeShape.ts
|   |   |   |-- Seat/
|   |   |   |   `-- Seat.tsx
|   |   |   `-- StatusPanel/
|   |   |       `-- StatusPanel.tsx
|   |   |-- hooks/
|   |   |   `-- useGameSocket.ts
|   |   |-- i18n/
|   |   |   `-- messages.ts
|   |   |-- pages/
|   |   |   `-- GamePage.tsx
|   |   |-- services/
|   |   |   |-- clientIdentity.ts
|   |   |   `-- gameSocket.ts
|   |   |-- styles/
|   |   |   `-- global.css
|   |   |-- types/
|   |   |   `-- game.ts
|   |   |-- App.tsx
|   |   `-- main.tsx
|   |-- .env
|   |-- .env.production.example
|   |-- package.json
|   |-- tsconfig.json
|   `-- vite.config.ts
|
|-- backend/                          # Spring Boot backend
|   |-- src/main/java/com/planebattle/
|   |   |-- config/
|   |   |   `-- WebSocketConfig.java
|   |   |-- game/
|   |   |   |-- dto/
|   |   |   |   |-- AttackRequest.java
|   |   |   |   |-- AttackResultResponse.java
|   |   |   |   |-- ClientMessage.java
|   |   |   |   |-- ClientView.java
|   |   |   |   |-- JoinRequest.java
|   |   |   |   |-- PlaneDeploymentRequest.java
|   |   |   |   |-- ServerMessage.java
|   |   |   |   `-- SubmitDeploymentRequest.java
|   |   |   |-- model/
|   |   |   |   |-- AttackRecord.java
|   |   |   |   |-- AttackResult.java
|   |   |   |   |-- Cell.java
|   |   |   |   |-- Direction.java
|   |   |   |   |-- GameRoom.java
|   |   |   |   |-- GameState.java
|   |   |   |   |-- GameStatus.java
|   |   |   |   |-- Plane.java
|   |   |   |   |-- PlanePart.java
|   |   |   |   |-- PlanePartType.java
|   |   |   |   |-- PlayerBoard.java
|   |   |   |   |-- PlayerRole.java
|   |   |   |   |-- PlayerSession.java
|   |   |   |   `-- PlayerSide.java
|   |   |   |-- rule/
|   |   |   |   `-- PlaneShapeService.java
|   |   |   `-- service/
|   |   |       |-- DeploymentValidator.java
|   |   |       `-- GameService.java
|   |   |-- util/
|   |   |   `-- JsonUtils.java
|   |   |-- websocket/
|   |   |   `-- GameWebSocketHandler.java
|   |   `-- PlaneBattleApplication.java
|   |-- src/main/resources/
|   |   `-- application.yml
|   |-- src/test/java/com/planebattle/
|   |   |-- game/rule/PlaneShapeServiceTest.java
|   |   |-- game/service/GameServiceTest.java
|   |   `-- websocket/GameWebSocketHandlerTest.java
|   |-- pom.xml
|   |-- mvnw
|   `-- mvnw.cmd
|
|-- docs/
|   |-- changelog.zh-CN.md
|   |-- design.md
|   |-- step-4-ui-design.md
|   |-- step-5-attack-design.md
|   `-- project-structure.md
|
|-- .gitignore
|-- .editorconfig
|-- AGENTS.md
`-- README.md
```
