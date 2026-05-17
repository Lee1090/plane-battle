# 飞机棋盘对战游戏 - MVP 设计文档

## 1. 项目目标

实现一个实时网页棋类对战游戏。第一版只做 Web，不需要数据库，不需要多房间。

第一版目标：

- 一个固定房间 / 一张桌子
- 页面显示一张桌子和两个椅子
- 用户访问页面后不会自动成为玩家
- 用户点击空椅子坐下后才成为玩家
- 未坐下的用户默认作为旁观者 / 观战者
- 多名观战者观看
- 双方先部署飞机
- 部署完成后轮流攻击
- 后端负责规则校验和游戏状态同步
- 前端只负责展示和发送用户操作

---

## 2. 技术栈

### 前端

- React
- TypeScript
- CSS Grid / HTML div 实现棋盘
- WebSocket 与后端通信

### 后端

- Java
- Spring Boot
- WebSocket
- 内存保存游戏状态

### 第一版不做

- 用户注册 / 登录
- 数据库存储
- 多个房间
- 排行榜
- 断线后长期恢复
- 移动端 App

---

## 3. 游戏概念

游戏是一个 10 x 10 棋盘上的飞机对战游戏。

每个玩家拥有自己的棋盘，并在部署阶段放置 3 架飞机。

对战阶段，双方轮流攻击对方棋盘中的一个格子。攻击后得到反馈：

- 没打中
- 打中飞机，但不是机头
- 打中机头

当某一方的 3 架飞机机头全部被击中时，对方获胜。

---

## 4. 游戏阶段

```text
WAITING        等待玩家加入
DEPLOYING      双方部署飞机
PLAYING        对战中
FINISHED       游戏结束
```

### 4.1 WAITING

规则：

- 用户打开页面后，先连接 WebSocket，但不会自动成为 Player A 或 Player B
- 服务端为连接用户分配临时 session
- 页面显示一张桌子和两个椅子
- 椅子 A 对应 Player A
- 椅子 B 对应 Player B
- 如果椅子为空，用户可以点击 Sit Down 坐下
- 用户坐下成功后才成为对应玩家
- 没有坐下的用户默认为 Spectator
- 当两个椅子都有人坐下后，游戏进入 DEPLOYING 阶段

### 4.2 DEPLOYING

规则：

- 每个玩家在自己的棋盘上部署 3 架飞机
- 每架飞机占 10 个格子
- 飞机不能超出棋盘
- 同一玩家的 3 架飞机不能有任何格子重叠
- 双方都提交合法部署后，进入 PLAYING 阶段

### 4.3 PLAYING

规则：

- 双方轮流攻击
- 只能攻击对方棋盘
- 每回合只能攻击 1 个格子
- 不能重复攻击同一个格子
- 攻击后后端返回攻击结果
- 如果攻击导致对方 3 个机头全部被击中，则游戏进入 FINISHED
- 否则切换回合

### 4.4 FINISHED

规则：

- 游戏结束
- 保存 winner
- 前端显示胜利方
- 第一版可以提供 Reset 按钮重新开始

---

## 5. 棋盘设计

棋盘大小固定为：

```text
10 x 10
```

坐标使用：

```ts
row: 0 - 9
col: 0 - 9
```

前端显示时可以展示为：

```text
A1 - J10
```

但前后端通信统一使用 row / col。

---

## 6. 飞机结构

每架飞机由 10 个格子组成：

```text
机头：1 格
机翼：5 格
机身：1 格
机尾：3 格
```

飞机是一个整体，有方向。

支持方向：

```text
UP
DOWN
LEFT
RIGHT
```

### 6.1 标准飞机形状

以飞机朝 UP 为例，机头在最上方：

```text
    H
W W W W W
    B
  T T T
```

说明：

```text
H = HEAD，机头
W = WING，机翼
B = BODY，机身
T = TAIL，机尾
```

如果用相对坐标表示，假设机头坐标是：

```text
head = (0, 0)
```

朝 UP 时，飞机占用格子为：

```text
HEAD: (0, 0)
WING: (1, -2), (1, -1), (1, 0), (1, 1), (1, 2)
BODY: (2, 0)
TAIL: (3, -1), (3, 0), (3, 1)
```

其他方向可以由这个形状旋转得到。

---

## 7. 核心数据模型

## 7.1 前端 TypeScript 类型

```ts
export type GameStatus = 'WAITING' | 'DEPLOYING' | 'PLAYING' | 'FINISHED';

export type PlayerRole = 'PLAYER_A' | 'PLAYER_B' | 'SPECTATOR';

export type PlayerSide = 'A' | 'B';

export type Direction = 'UP' | 'DOWN' | 'LEFT' | 'RIGHT';

export type PlanePartType = 'HEAD' | 'WING' | 'BODY' | 'TAIL';

export type AttackResult = 'MISS' | 'HIT_PLANE' | 'HIT_HEAD';

export interface Cell {
  row: number;
  col: number;
}

export interface PlanePart {
  type: PlanePartType;
  row: number;
  col: number;
  hit: boolean;
}

export interface Plane {
  id: string;
  head: Cell;
  direction: Direction;
  parts: PlanePart[];
}

export interface AttackRecord {
  row: number;
  col: number;
  result: AttackResult;
}

export interface PlayerBoard {
  owner: PlayerSide;
  planes: Plane[];
  receivedAttacks: AttackRecord[];
}

export interface GameState {
  status: GameStatus;
  currentTurn: PlayerSide | null;
  winner: PlayerSide | null;
  playerABoard: PlayerBoard | null;
  playerBBoard: PlayerBoard | null;
  playerAReady: boolean;
  playerBReady: boolean;
}

export interface ClientView {
  role: PlayerRole;
  side: PlayerSide | null;
  gameState: GameState;
}
```

---

## 7.2 后端 Java 模型

建议包结构：

```text
com.planebattle
  config
  websocket
  game
    model
    service
    rule
```

核心类：

```java
public enum GameStatus {
    WAITING,
    DEPLOYING,
    PLAYING,
    FINISHED
}
```

```java
public enum PlayerSide {
    A,
    B
}
```

```java
public enum PlayerRole {
    PLAYER_A,
    PLAYER_B,
    SPECTATOR
}
```

```java
public enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT
}
```

```java
public enum PlanePartType {
    HEAD,
    WING,
    BODY,
    TAIL
}
```

```java
public enum AttackResult {
    MISS,
    HIT_PLANE,
    HIT_HEAD
}
```

```java
public class Cell {
    private int row;
    private int col;
}
```

```java
public class PlanePart {
    private PlanePartType type;
    private int row;
    private int col;
    private boolean hit;
}
```

```java
public class Plane {
    private String id;
    private Cell head;
    private Direction direction;
    private List<PlanePart> parts;
}
```

```java
public class AttackRecord {
    private int row;
    private int col;
    private AttackResult result;
}
```

```java
public class PlayerBoard {
    private PlayerSide owner;
    private List<Plane> planes;
    private List<AttackRecord> receivedAttacks;
}
```

```java
public class GameState {
    private GameStatus status;
    private PlayerSide currentTurn;
    private PlayerSide winner;
    private PlayerBoard playerABoard;
    private PlayerBoard playerBBoard;
    private boolean playerAReady;
    private boolean playerBReady;
}
```

```java
public class PlayerSession {
    private String sessionId;
    private PlayerRole role;
    private PlayerSide side;
}
```

```java
public class GameRoom {
    private String roomId;
    private PlayerSession playerA;
    private PlayerSession playerB;
    private List<PlayerSession> spectators;
    private GameState gameState;
}
```

---

## 8. 部署阶段规则设计

### 8.1 玩家提交部署

前端发送：

```json
{
  "type": "SUBMIT_DEPLOYMENT",
  "data": {
    "planes": [
      {
        "id": "A-P1",
        "head": { "row": 2, "col": 4 },
        "direction": "UP"
      },
      {
        "id": "A-P2",
        "head": { "row": 7, "col": 2 },
        "direction": "RIGHT"
      },
      {
        "id": "A-P3",
        "head": { "row": 5, "col": 8 },
        "direction": "DOWN"
      }
    ]
  }
}
```

注意：

- 前端只提交每架飞机的 head 和 direction
- 后端根据 head + direction 计算 parts
- 后端校验是否合法
- 校验通过后保存到对应玩家的 PlayerBoard

---

### 8.2 部署校验

后端需要校验：

```text
1. 必须刚好提交 3 架飞机
2. 每架飞机必须有 id、head、direction
3. 每架飞机根据 head + direction 计算后必须刚好占 10 格
4. 所有格子必须在 10 x 10 棋盘内
5. 同一个玩家的 3 架飞机不能有重叠格子
6. 只有 DEPLOYING 阶段允许提交部署
7. 玩家只能提交自己的棋盘
8. 观战者不能提交部署
```

---

### 8.3 飞机形状生成服务

建议创建：

```java
public class PlaneShapeService {
    public List<PlanePart> buildParts(Cell head, Direction direction) {
        // 根据方向生成 10 个 PlanePart
    }
}
```

推荐先实现 UP 方向的相对坐标：

```java
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
    new RelativePlanePart(PlanePartType.TAIL, 3, 1)
);
```

然后根据方向做旋转：

```text
UP:    (row, col)
DOWN:  (-row, -col)
LEFT:  (col, -row)
RIGHT: (-col, row)
```

也可以为了简单，直接为四个方向分别写固定相对坐标。

第一版推荐：直接写四套固定相对坐标，方便调试。

---

## 9. 对战阶段规则设计

### 9.1 攻击请求

前端发送：

```json
{
  "type": "ATTACK",
  "data": {
    "row": 3,
    "col": 5
  }
}
```

---

### 9.2 攻击校验

后端需要校验：

```text
1. 游戏状态必须是 PLAYING
2. 请求者必须是玩家，不能是观战者
3. 必须是当前玩家的回合
4. 攻击坐标必须在 10 x 10 棋盘内
5. 只能攻击对方棋盘
6. 该坐标不能已经攻击过
```

---

### 9.3 攻击结果判断

逻辑：

```text
找到对方棋盘上 row / col 对应的 PlanePart

如果不存在：
  MISS

如果存在，并且 type == HEAD：
  HIT_HEAD

如果存在，并且 type != HEAD：
  HIT_PLANE
```

攻击后：

```text
1. 将对应 PlanePart 标记为 hit = true
2. 将攻击记录加入 defender.receivedAttacks
3. 判断 defender 是否所有 HEAD 都 hit = true
4. 如果是，设置 winner = attacker，status = FINISHED
5. 如果不是，切换 currentTurn
6. 广播最新状态
```

---

## 10. WebSocket 通信设计

### 10.1 连接地址

```text
ws://localhost:8080/ws/game
```

第一版使用原生 WebSocket + JSON 消息。

---

### 10.2 客户端发送消息格式

统一格式：

```ts
export interface ClientMessage<T = unknown> {
  type: ClientMessageType;
  data?: T;
}
```

消息类型：

```ts
export type ClientMessageType =
  | 'JOIN'
  | 'SIT_DOWN'
  | 'STAND_UP'
  | 'SUBMIT_DEPLOYMENT'
  | 'ATTACK'
  | 'RESET';
```

---

### 10.3 服务端发送消息格式

统一格式：

```ts
export interface ServerMessage<T = unknown> {
  type: ServerMessageType;
  data?: T;
  error?: string;
}
```

消息类型：

```ts
export type ServerMessageType =
  | 'CONNECTED'
  | 'STATE_UPDATE'
  | 'ATTACK_RESULT'
  | 'ERROR';
```

---

### 10.4 JOIN

客户端连接 WebSocket 后发送：

```json
{
  "type": "JOIN"
}
```

服务端不会直接分配玩家身份。

默认返回：

```json
{
  "type": "CONNECTED",
  "data": {
    "role": "SPECTATOR",
    "side": null,
    "gameState": {}
  }
}
```

然后广播当前桌子状态：

```json
{
  "type": "STATE_UPDATE",
  "data": {}
}
```

---

### 10.5 SIT_DOWN

用户点击椅子后发送：

```json
{
  "type": "SIT_DOWN",
  "data": {
    "side": "A"
  }
}
```

或者：

```json
{
  "type": "SIT_DOWN",
  "data": {
    "side": "B"
  }
}
```

服务端逻辑：

```text
1. 校验游戏状态是否允许坐下
2. 校验用户当前是否已经坐在椅子上
3. 校验目标椅子是否为空
4. 如果 side = A，占用 Player A 椅子
5. 如果 side = B，占用 Player B 椅子
6. 更新该 session 的 role / side
7. 广播 STATE_UPDATE
8. 如果 A 和 B 两个椅子都有人，则进入 DEPLOYING
```

如果椅子已被占用，返回：

```json
{
  "type": "ERROR",
  "error": "This seat is already taken."
}
```

---

### 10.6 STAND_UP

玩家可以在游戏开始前离开座位：

```json
{
  "type": "STAND_UP"
}
```

服务端逻辑：

```text
1. 只允许 WAITING 阶段或 DEPLOYING 阶段未提交部署前站起
2. 清空对应椅子
3. 将该用户 role 改回 SPECTATOR，side 改为 null
4. 如果游戏还未开始，状态保持 WAITING 或回到 WAITING
5. 广播 STATE_UPDATE
```

第一版也可以简单处理：

```text
游戏进入 PLAYING 后不允许 STAND_UP。
```

---

### 10.7 SUBMIT_DEPLOYMENT

客户端发送：

```json
{
  "type": "SUBMIT_DEPLOYMENT",
  "data": {
    "planes": [
      {
        "id": "P1",
        "head": { "row": 2, "col": 4 },
        "direction": "UP"
      }
    ]
  }
}
```

服务端：

```text
1. 校验玩家身份
2. 校验部署合法性
3. 保存玩家棋盘
4. 标记 playerAReady / playerBReady
5. 如果双方都 ready，进入 PLAYING，并设置 currentTurn = A
6. 广播 STATE_UPDATE
```

---

### 10.8 ATTACK

客户端发送：

```json
{
  "type": "ATTACK",
  "data": {
    "row": 4,
    "col": 6
  }
}
```

服务端返回攻击结果给所有人：

```json
{
  "type": "ATTACK_RESULT",
  "data": {
    "attacker": "A",
    "defender": "B",
    "row": 4,
    "col": 6,
    "result": "HIT_HEAD"
  }
}
```

然后广播完整状态：

```json
{
  "type": "STATE_UPDATE",
  "data": {}
}
```

---

## 11. 前端页面设计

### 11.0 多语言支持（i18n）

UI 必须支持：

```text
简体中文（zh-CN）
英文（en-US）
```

实现要求：

```text
1. 所有 UI 文本不得写死，必须通过 i18n 管理
2. 前端根据浏览器语言自动选择默认语言
3. 提供手动切换语言（可放在右上角）
4. 语言切换不影响游戏状态
```

推荐方案：

```text
使用 i18n 库（如 react-i18next）
```

示例：

```ts
// en-US
{
  "sitDown": "Sit Down",
  "attack": "Attack",
  "miss": "Miss",
  "hitPlane": "Hit Plane",
  "hitHead": "Hit Head",
  "yourTurn": "Your Turn"
}

// zh-CN
{
  "sitDown": "坐下",
  "attack": "攻击",
  "miss": "未命中",
  "hitPlane": "击中飞机",
  "hitHead": "击中机头",
  "yourTurn": "轮到你"
}
```

注意：

```text
后端只返回状态和结果代码（如 HIT_HEAD）
前端负责翻译显示
```

---

### 11.1 页面结构

```text
src/
  components/
    Board/
      Board.tsx
      Board.css
    Cell/
      Cell.tsx
    PlanePreview/
      PlanePreview.tsx
    StatusPanel/
      StatusPanel.tsx
    DeploymentPanel/
      DeploymentPanel.tsx
  hooks/
    useGameSocket.ts
  services/
    gameSocket.ts
  types/
    game.ts
  pages/
    GamePage.tsx
  App.tsx
```

---

### 11.2 GamePage

职责：

```text
1. 建立 WebSocket 连接
2. 接收 ClientView / GameState
3. 显示桌子和两个椅子
4. 根据用户是否坐下显示不同操作
5. 根据游戏状态显示不同 UI
```

展示逻辑：

```text
WAITING:
  显示桌子和两个椅子
  空椅子显示 Sit Down 按钮
  已占用椅子显示 Player A / Player B
  未坐下用户显示为 Spectator

DEPLOYING:
  玩家显示部署棋盘
  观战者显示等待部署完成

PLAYING:
  玩家显示自己的棋盘和对方攻击棋盘
  观战者显示双方公开攻击记录

FINISHED:
  显示胜利方
```

---

### 11.3 部署 UI

玩家操作：

```text
1. 选择飞机编号 P1 / P2 / P3
2. 选择方向 UP / DOWN / LEFT / RIGHT
3. 点击棋盘上的一个格子作为机头
4. 前端显示飞机预览
5. 如果本地检测越界或重叠，显示红色提示
6. 三架飞机合法后，点击 Submit Deployment
```

前端可以做本地校验，但不能只依赖前端。后端仍然必须校验。

---

### 11.4 对战 UI

玩家视图建议分成两个棋盘：

```text
左侧：自己的棋盘
- 显示自己的飞机
- 显示对方攻击过的位置

右侧：对方棋盘
- 不显示对方飞机
- 只显示自己攻击记录
- 点击格子发送 ATTACK
```

观战者视图：

```text
可以显示双方棋盘
第一版建议隐藏双方飞机，只显示攻击记录
以后可以增加“上帝视角”开关
```

---

## 12. 后端服务设计

### 12.1 GameWebSocketHandler

职责：

```text
1. 处理连接建立
2. 处理连接关闭
3. 接收客户端消息
4. 根据 type 调用 GameService
5. 向客户端发送消息
6. 广播状态
```

---

### 12.2 GameService

职责：

```text
1. 管理唯一 GameRoom
2. 分配玩家 / 观战者
3. 提交部署
4. 执行攻击
5. 重置游戏
6. 获取当前 GameState
```

建议方法：

```java
public ClientView join(String sessionId);

public void leave(String sessionId);

public void submitDeployment(String sessionId, List<PlaneDeploymentRequest> planes);

public AttackResultResponse attack(String sessionId, int row, int col);

public void reset();

public GameState getGameState();
```

---

### 12.3 DeploymentValidator

职责：

```text
1. 校验飞机数量
2. 生成飞机 parts
3. 校验越界
4. 校验重叠
5. 返回完整 Plane 列表
```

建议方法：

```java
public List<Plane> validateAndBuildPlanes(List<PlaneDeploymentRequest> requests);
```

---

### 12.4 AttackService / GameRuleService

职责：

```text
1. 校验是否当前回合
2. 校验攻击位置
3. 判断攻击结果
4. 更新棋盘
5. 判断胜负
6. 切换回合
```

---

## 13. 状态可见性设计

注意：不能把完整 GameState 无差别发给所有客户端，否则玩家可以在浏览器里看到对方飞机位置。

第一版建议后端生成 ClientView：

```java
public ClientView buildClientView(String sessionId) {
    // 根据当前 session 的角色，过滤对方飞机信息
}
```

### 13.1 玩家 A 能看到

```text
自己的完整飞机位置
自己的 receivedAttacks
对 B 的攻击记录
游戏状态
当前回合
胜负信息
```

不能看到：

```text
B 的飞机完整位置
```

### 13.2 玩家 B 能看到

同理，不能看到 A 的飞机完整位置。

### 13.3 观战者能看到

第一版建议：

```text
双方攻击记录
当前回合
胜负信息
```

不显示双方飞机完整位置。

---

## 14. 错误处理

服务端遇到非法操作时，发送：

```json
{
  "type": "ERROR",
  "error": "It is not your turn."
}
```

常见错误：

```text
Invalid message type.
Invalid game status.
Only players can perform this action.
It is not your turn.
Cell is out of board.
Cell has already been attacked.
Plane is out of board.
Planes cannot overlap.
Deployment requires exactly 3 planes.
```

---

## 15. 第一版开发顺序

建议 Codex 按下面顺序实现：

### Step 1: 后端基础 WebSocket

- Spring Boot 项目
- WebSocket endpoint: /ws/game
- 支持 JOIN
- 支持广播 STATE_UPDATE

### Step 2: 后端 GameRoom

- GameRoom
- PlayerSession
- GameState
- 玩家 / 观战者分配

### Step 3: 前端基础连接

- React + TypeScript 项目
- useGameSocket hook
- 能连接 ws://localhost:8080/ws/game
- 能显示 role / side / status

### Step 4: 部署阶段

- 前端 10 x 10 棋盘
- 选择飞机方向
- 点击机头位置
- 生成飞机预览
- 提交 3 架飞机
- 后端校验部署

### Step 5: 对战阶段

- 后端 ATTACK 逻辑
- 攻击记录
- 胜负判断
- 回合切换
- 前端显示攻击结果

### Step 6: 观战者

- 第三个及之后用户进入观战模式
- 显示当前公开状态

### Step 7: Reset

- 游戏结束后允许重置
- 清空棋盘和玩家 ready 状态

---

## 16. MVP 验收标准

### 16.1 加入游戏

- 浏览器打开后默认成为 Spectator
- 页面显示一张桌子和两个椅子
- 点击椅子 A 后成为 Player A
- 点击椅子 B 后成为 Player B
- 椅子已占用时不能重复坐下
- 没有坐下的用户保持 Spectator

### 16.2 部署

- 玩家 A 和 B 都能部署 3 架飞机
- 越界部署被拒绝
- 重叠部署被拒绝
- 双方部署完成后进入 PLAYING

### 16.3 对战

- 只有当前回合玩家可以攻击
- 攻击空格返回 MISS
- 攻击非机头飞机部位返回 HIT_PLANE
- 攻击机头返回 HIT_HEAD
- 重复攻击同一格被拒绝
- 攻击后自动切换回合

### 16.4 胜负

- 任意一方 3 个机头全部被击中后，游戏结束
- winner 设置为攻击方
- 前端显示胜利方

### 16.5 观战

- 观战者能看到游戏状态变化
- 观战者不能操作部署或攻击

---

## 17. 给 Codex 的实现提示

实现时请遵守：

```text
1. 游戏规则全部放在后端
2. 前端不决定攻击结果
3. 前端不保存最终状态，只根据服务端 STATE_UPDATE 渲染
4. 所有 WebSocket 消息使用统一 JSON 格式
5. 第一版状态存在内存，不需要数据库
6. 先做一个房间，不需要 roomId 输入
7. JOIN 只表示进入页面，不代表成为玩家
8. 用户必须通过 SIT_DOWN 占用椅子后才成为玩家
9. 注意不要把对手完整飞机位置发给玩家
10. 后端必须有部署和攻击校验
11. 代码尽量模块化，便于以后扩展多房间、登录和移动端
```

---

## 18. 部署方案（Vercel + Render）

### 18.1 总体架构

```text
浏览器
  ↓
Vercel（React 前端）
  ↓ WebSocket
Render（Spring Boot 后端）
```

### 18.2 前端部署（Vercel）

使用 entity["company","Vercel","frontend hosting platform"] 部署 React：

```text
Root Directory: frontend
Build Command: npm run build
Output Directory: dist
```

环境变量：

```text
VITE_WS_URL = wss://your-backend.onrender.com/ws/game
```

---

### 18.3 后端部署（Render）

使用 entity["company","Render","cloud application platform"] 部署 Spring Boot：

```text
Root Directory: backend
Build Command: ./mvnw clean package -DskipTests
Start Command: java -jar target/*.jar
```

配置端口：

```properties
server.port=${PORT:8080}
```

---

### 18.4 开发环境 vs 生产环境

```text
开发：
React: http://localhost:5173
WS: ws://localhost:8080/ws/game

生产：
React: https://xxx.vercel.app
WS: wss://xxx.onrender.com/ws/game
```

---

### 18.5 注意事项

```text
1. 前端不得写死 WebSocket 地址，必须通过环境变量读取（例如 VITE_WS_URL）
2. 后端需要允许 Vercel 域名跨域
3. Render 免费版会休眠，可能影响体验
4. 第一版允许使用内存存储游戏状态
```

---

## 19. 后续可扩展方向

第一版完成后可以扩展：

```text
1. 多房间
2. 用户登录
3. 游戏记录
4. 排行榜
5. 房间邀请链接
6. 断线重连
7. 移动端 React Native / Expo
8. 更好的动画效果
9. 上帝视角观战模式
10. AI 机器人对战
```


