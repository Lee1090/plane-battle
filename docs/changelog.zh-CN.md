# Changelog

本文档记录项目的重要实现变更、提交记录和设计调整。
详细设计仍以 `docs/design.md` 和各 Step 设计文档为准。

## Step 5 - 对战攻击阶段

### Commit: `current commit`

**Title:** `feat(step 5): add turn-based attack phase`  
**Date:** 2026-05-31  
**Branch:** `feature/step-5-attack`

#### Summary

实现 Step 5 对战阶段：双方完成部署后进入回合制攻击，后端负责攻击校验、命中判断、回合切换和胜负判定，前端在固定双棋盘布局中支持点击对方棋盘攻击并显示攻击结果。

#### Backend

- 新增 `AttackRequest` 和 `AttackResultResponse`。
- 新增 `GameService.attack`，处理 `MISS`、`HIT_PLANE`、`HIT_HEAD`。
- 攻击记录写入防守方 `receivedAttacks`。
- 命中飞机部位时更新 `PlanePart.hit`。
- 非当前回合、观战者、越界、重复攻击和非 `PLAYING` 状态攻击会被拒绝。
- 击中防守方全部 3 个机头后进入 `FINISHED`，并设置 `winner`。
- 对战中继续隐藏对方飞机位置，游戏结束后允许展示完整棋盘。

#### Frontend

- `PLAYING` 状态下，当前玩家可以点击对方棋盘格子发起攻击。
- 自己棋盘、非当前回合玩家和观战者不能发起攻击。
- 已攻击格子不再响应重复点击。
- 攻击记录在棋盘中显示为 `M`、`H`、`X`。
- 控制区在对战阶段显示回合提示、观战提示和胜者信息。
- 补充攻击相关中英文文案。

#### Docs

- 新增 `docs/step-5-attack-design.md`。
- 更新 `docs/design.md`，补充 Step 5 详细设计文档入口。
- 更新 `docs/project-structure.md`。
- 更新 `docs/changelog.zh-CN.md`。

#### Verification

- `backend`: `.\mvnw.cmd test`
- `frontend`: `npm run build`

---

## Documentation Workflow

### Commit: `bootstrap entry`

**Title:** `docs: add project changelog workflow`  
**Date:** 2026-05-31  
**Branch:** `feature/step-4-deployment`

#### Summary

统一项目变更日志规则，明确以后每次功能、修复、重构或 step 级提交都需要同步更新中文 changelog。

#### Docs

- 新增 `AGENTS.md`，记录提交前文档同步规则。
- 新增 `docs/changelog.zh-CN.md`，作为统一中文变更日志。
- 删除按 Step 拆分的临时调整日志：
  - `docs/step-4-adjustment-log.zh-CN.md`
  - `docs/step-4-adjustment-log.en-US.md`
- 约定 changelog 正文使用中文，commit title、branch、路径、命令和技术标识保留原文。
- 约定在相关变化发生时同步检查 `docs/design.md`、`docs/project-structure.md` 和 Step-specific design docs。

#### Verification

- Documentation-only change; tests were not run.

---

## Step 4 - Deployment UI and Validation

### Commit: `3ae4ddc`

**Title:** `feat(step 4): add deployment UI and validation`  
**Date:** 2026-05-24  
**Branch:** `feature/step-4-deployment`

#### Summary

实现 Step 4 部署阶段，包括后端部署校验、前端固定双棋盘部署 UI、WebSocket 提交部署流程。

#### Backend

- 新增部署提交消息 `SUBMIT_DEPLOYMENT`。
- 新增飞机部署 DTO。
- 新增飞机形状生成服务。
- 新增部署校验服务。
- 支持双方提交部署后进入 `PLAYING`。
- 对对手和观战者隐藏飞机位置。
- 提交部署后禁止重复提交。
- 提交部署后禁止起身清空棋盘。
- 收紧飞机 id 校验，要求精确为 `P1`、`P2`、`P3`。

#### Frontend

- 新增固定双棋盘布局。
- 左侧固定玩家 A，右侧固定玩家 B。
- 坐下 / 起身按钮固定在对应棋盘下方。
- 部署操作区固定在对应棋盘下方，避免棋盘随操作移动。
- 支持点击空格按顺序部署 `P1`、`P2`、`P3`。
- 支持点击机头切换当前焦点飞机。
- 支持方向键调整当前焦点飞机方向。
- 支持拖动机头移动飞机。
- 支持整架飞机拖动预览。
- 支持非法部署提示，区分超出棋盘和飞机重叠。
- 提示文字按状态区分颜色。
- 已占用飞机格取消 hover 背景变化。
- 中文方向选项本地化。

#### Docs

- 新增 `docs/step-4-ui-design.md`。
- 新增 Step 4 调整日志。
- 更新 `docs/design.md`。
- 更新 `docs/project-structure.md`。

#### Verification

- Backend tests passed.
- Frontend build passed.
- Browser layout check confirmed the fixed two-board layout fits a 1280 x 720 viewport without vertical scrolling during normal use.

---

### Commit: `365f21c`

**Title:** `fix(step 4): restore deployment state on refresh`  
**Date:** 2026-05-31  
**Branch:** `feature/step-4-deployment`

#### Summary

修复部署阶段刷新后玩家身份和部署状态丢失的问题，并整理部署交互结构，为后续扩展保留空间。

#### Backend

- 新增 `JoinRequest`，支持 `JOIN` 携带稳定 `clientId`。
- `PlayerSession` 增加 `clientId`。
- 后端通过 `clientId` 恢复玩家身份和座位。
- 刷新或 WebSocket 断开不再立即释放带 `clientId` 的玩家座位。
- 已提交部署继续从后端游戏状态恢复。
- 补充等待阶段、部署阶段、已提交部署后的刷新恢复测试。

#### Frontend

- 新增 `clientIdentity.ts`，在浏览器 `localStorage` 中保存稳定 `clientId`。
- `JOIN` 消息携带 `clientId`。
- 新增 `deploymentDraftStorage.ts`，保存未提交的部署草稿。
- 刷新后恢复未提交的部署草稿。
- 提交部署后清理本地草稿。
- 页面中文标题改为“飞机大战”。

#### Refactor

- 将部署交互规则拆到 `deploymentInteraction.ts`。
- 将飞机形状、方向旋转和拖拽预览逻辑拆到 `planeShape.ts`。
- `GameBoardArea.tsx` 保留组件编排、状态连接和渲染逻辑。
- 第一版不暴露操作习惯设置 UI，只在代码结构上为后续扩展保留空间。

#### Docs

- 更新 `docs/step-4-ui-design.md`，补充刷新恢复规则。
- 更新 `docs/design.md`，补充刷新恢复和草稿恢复规则。
- 更新 `docs/project-structure.md`，记录新增前端结构文件。

#### Verification

- Backend tests: 18 passed.
- Frontend build passed.
