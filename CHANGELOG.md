# 修改日志（CHANGELOG）

> 本文档记录「易校 EasyCampus」前端代码的迭代升级内容。本次更新聚焦于 **Vue 3 语法现代化** 与 **JavaScript → TypeScript 迁移**，遵循「只做更新与迭代、不做功能变更」的原则。

## [未发布] - 2026-08-23

### ✨ TypeScript 迁移（JS → TS）

将前端源码目录下的全部独立 JavaScript 模块迁移为 TypeScript 文件，并为请求/状态/工具层补充了基础类型标注：

| 原文件 | 新文件 | 说明 |
|--------|--------|------|
| `frontend/src/main.js` | `frontend/src/main.ts` | 应用入口 |
| `frontend/src/router/index.js` | `frontend/src/router/index.ts` | 路由配置，新增 `RouteMeta` 类型增强 |
| `frontend/src/services/api.js` | `frontend/src/services/api.ts` | API 服务层，声明 `ApiClient` 类型并类型化各模块方法 |
| `frontend/src/store/auth.js` | `frontend/src/store/auth.ts` | 认证 Store，新增 `User`、`AuthResult` 接口 |
| `frontend/src/store/notification.js` | `frontend/src/store/notification.ts` | 通知/未读数 Store，类型化轮询定时器 |
| `frontend/src/use/usePullRefresh.js` | `frontend/src/use/usePullRefresh.ts` | 下拉刷新组合式函数，类型化触摸事件 |
| `frontend/src/use/useToast.js` | `frontend/src/use/useToast.ts` | Toast 注入组合式函数，声明 `ToastApi` 接口 |
| `frontend/vite.config.js` | `frontend/vite.config.ts` | Vite 构建配置 |

### 🧰 工程化配置

- 新增 `frontend/tsconfig.json`：启用 `moduleResolution: bundler`、`isolatedModules`、`skipLibCheck` 等现代配置。
- 新增 `frontend/src/vite-env.d.ts`：声明 `vite/client` 类型与 `*.vue` 模块类型。
- 更新 `frontend/package.json`：
  - 新增开发依赖 `typescript`、`vue-tsc`、`@types/node`。
  - 新增 `type-check` 脚本（`vue-tsc --noEmit`），在不影响构建的前提下提供类型检查。
- 更新 `frontend/index.html`：入口由 `main.js` 指向 `main.ts`。

### 🔗 构建与部署联动

- 更新 `Dockerfile`：前端构建阶段复制 `vite.config.ts` 与 `tsconfig.json`，适配新的文件结构。
- 更新 `frontend/scripts/verify-build.js`：Vite 配置检测由 `vite.config.js` 调整为 `vite.config.ts`。

### 🔧 代码修正与清理

- 修复 `src/views/FollowList.vue` 用户主页跳转路径错误（`/user/` → `/users/`），避免误入 404 页面。
- 删除未被任何模块引用的死代码组件 `EmptyState.vue`、`SkeletonCard.vue`。

### 📝 文档优化

- 优化 `README.md`：
  - 技术栈补充 **TypeScript** 与 `vue-tsc` 类型检查。
  - 修正状态管理描述（由 "Pinia" 更正为组合式 Store `useAuthStore` / `useNotificationStore`）。
  - 目录结构与模块清单统一更新为 `.ts` 文件。
  - 修正页面清单：移除不存在的「校区设置页」，补录「关注列表」「通知」「我的邀请」三个真实页面，页面/路由数量由 33 更正为 35。
  - 组件清单补充 `BackButton`、`ImageViewer`，可复用组件数量由 12 更正为 14。

### ✅ 验证结果

- `npm run build`：构建通过（182 个模块转换成功）。
- `npm run type-check`：类型检查通过（无错误）。

---

## 说明

- 本次迁移保持原有功能与接口行为不变，仅进行文件格式与类型标注升级。
- `.vue` 组件均使用 `<script setup>` 语法，并已采用 `defineModel` 等 Vue 3.4+ 现代写法；本次未改动组件内的业务逻辑。
- `.vue` 组件内部的 `<script>` 仍保持 JavaScript，后续可按需逐步迁移为 `<script setup lang="ts">`。

---

## [未发布] - 2026-09-05

> 本次更新包含两部分：**前端可靠性/工程化升级** 与 **后端 WebSocket 实时聊天端点落地**，并完成前后端 WS 协议全链路联调。

### 🔌 后端新增：WebSocket 实时聊天端点 `/ws/chat`

| 文件 | 说明 |
|------|------|
| `backend/pom.xml` | 新增 `spring-boot-starter-websocket` 依赖 |
| `backend/.../config/WebSocketConfig.java` | 注册 `/ws/chat` 端点（`@EnableWebSocket`） |
| `backend/.../websocket/WsAuthInterceptor.java` | 握手拦截器：从 `?token=<JWT>` 查询参数校验身份（原生 WS 无法自定义 Authorization 头），token 缺失/无效直接拒绝握手 |
| `backend/.../websocket/ChatWebSocketHandler.java` | 聊天消息处理器 |

核心能力：

- **心跳保活**：收到 `{type:"ping"}` 回 `{type:"pong"}`，配合前端心跳超时检测维持长连接。
- **消息持久化 + ACK 回执**：`{type:"chat", clientMsgId, content, receiverId, productId}` 复用 `ChatService.sendMessage` 落库（与 HTTP `POST /api/v2/chat/messages` 同一入口，会话管理/未读数逻辑完全一致）；成功回 `{type:"ack", clientMsgId}`，失败回 `{type:"error", clientMsgId, message}`；WS 路径补充 content 非空校验，与 HTTP 层 `@Valid` 对齐。
- **实时推送**：消息落库后向接收者的在线会话推送 `{type:"chat_message", ...消息VO, clientMsgId, timestamp}`，`clientMsgId` 供接收端去重；接收方离线时无推送，由未读数轮询兜底。
- **在线会话表**：`ConcurrentHashMap<userId, Set<WebSocketSession>>` 支持同一用户多标签页同时在线；发送前对 session 加锁串行化，避免并发写帧异常。

### ✨ 前端：WebSocketManager 可靠性增强

`frontend/src/services/api.ts`：

- **心跳保活**：25s 间隔 ping，连续 3 个周期无任何服务端流量判定为死链（NAT 超时/代理断连），主动关闭触发重连。
- **指数退避重连**：基础 1s、上限 30s、含随机抖动，最多 10 次后停止；页面切回前台（visibilitychange）立即重置计数重连；主动关闭（登出）不触发重连。
- **消息 ACK 确认**：`clientMsgId` + 5s 超时定时器；超时按已发送宽容处理（不自动重发，避免重复消息）。
- **离线消息队列**：断线期间消息进入 localStorage 持久化队列（上限 50 条，超出丢弃最旧），重连成功后按序自动补发。
- **消息去重**：优先 `clientMsgId`、其次服务端 `id`，200 条滑动窗口，防止重连/补发导致重复展示。

### ✨ 前端：Pinia 状态管理迁移

- `auth` / `notification` Store 迁移为 **Pinia setup store**，注册 `pinia-plugin-persistedstate` 实现状态持久化。
- 自定义 `splitStorage`：持久化时拆分写入 localStorage 的 `token` / `user` 两个独立 key，与 axios 请求拦截器的既有读取约定保持兼容（避免 services ↔ store 循环依赖）。
- 保留 `useAuthStore()` / `useNotificationStore()` 兼容层（基于 `storeToRefs`），组件层零改动完成迁移。
- 启动时校验持久化登录态：拒绝演示模式假 token、数据不完整与已过期的 token。

### 🧰 工程化配置

- 新增 `frontend/eslint.config.js`（ESLint 10 Flat Config）：Vue + TypeScript 推荐规则，格式化交由 Prettier 避免冲突；存量技术债（prop 突变、空 catch 等）降级为 warning 不阻断提交。
- 新增 `.prettierrc.json` / `.prettierignore`。
- `package.json` 新增脚本：`lint` / `lint:fix` / `format`。
- 移除 husky / lint-staged 预提交钩子及依赖（保留 ESLint / Prettier 供手动使用）。

### ✅ 验证结果

- 后端 `mvnw compile`：编译通过。
- WebSocket 端点五场景实测：无 token / 假 token 握手被拒绝 ✓；合法 token 握手成功 + ping/pong ✓；A→B 发消息，发送方收到 ACK、接收方实时收到 chat_message 推送 ✓；向不存在用户发消息收到 error 回执 ✓。
- 前端 `npm run type-check`：0 错误。
- 前端 `npx eslint .`：0 errors（82 warnings 为存量技术债，不阻断）。
- 前端 `npm run build`：构建通过。