# 校园社区平台优化方案

> 生成时间: 2026-06-06
> 状态: 待执行

---

## 一、项目现状概览

### 技术栈
- 前端: Vue 3 (Composition API + `<script setup>`) + Vite + Vue Router + Pinia + Axios
- 后端: Spring Boot 3 + MyBatis + MySQL + JWT 认证
- 数据库: MySQL 8.0 (campus_market_dev / campus_market)

### 核心功能模块

| 模块 | 状态 | 说明 |
|------|------|------|
| 用户认证 | 基本可用 | 注册/登录/JWT，但密码重置是空壳 |
| 信息流/推荐 | 可用 | 瀑布流+推荐算法，但推荐性能差(100+次DB查询) |
| 商品交易 | 基本可用 | CRUD+评论+收藏，但缺"已售出"状态流转 |
| 社区帖子 | 可用 | CRUD+评论+点赞+加精，但加精无权限控制 |
| 实时聊天 | 可用 | WebSocket+HTTP混合，但消息发送失败无反馈 |
| 活动 | 半成品 | 列表/详情可用，报名是空壳(只打日志不持久化) |
| 组织/社团 | 功能完整 | 邀请/申请/审批/角色/审计，但审批无权限控制 |
| 搜索 | 基本可用 | 全局搜索，但无标签搜索支持 |

---

## 二、问题清单（按严重度排序）

### P0 - 安全/功能缺陷

#### 2.1 SecurityConfig 全部 permitAll
- **位置**: `backend/src/main/java/com/campus/backend/config/SecurityConfig.java`
- **问题**: `/api/**` 和 `anyRequest()` 都是 `permitAll()`，Spring Security 授权检查完全不生效
- **影响**: 任何未登录用户都能调用需要认证的接口，仅因 `SecurityUtils.getCurrentUserId()` 抛异常才间接阻止
- **关联问题**: User 实体没有 `role` 字段，`CustomUserDetailsService` 硬编码 `ROLE_USER`，ADMIN 角色无法分配，`@PreAuthorize("hasRole('ADMIN')")` 永远不通过

#### 2.2 密码重置功能是空壳
- **位置**: `backend/src/main/java/com/campus/backend/service/impl/UserServiceImpl.java`
- **问题**: `sendResetCode()` 只打日志不发送验证码，`verifyAndResetPassword()` 不校验验证码正确性
- **影响**: 任何人知道用户名/手机号就能重置他人密码

#### 2.3 数据库测试接口暴露
- **位置**: `backend/src/main/java/com/campus/backend/controller/DatabaseTestController.java`
- **问题**: `/api/test/db/query` 直接拼接表名到 SQL，有 SQL 注入风险；暴露数据库元信息
- **影响**: 生产环境安全风险

#### 2.4 帖子详情 XSS 风险
- **位置**: `frontend/src/views/PostDetailPage.vue`
- **问题**: `v-html` 渲染未转义的帖子内容
- **影响**: 恶意用户可注入脚本

#### 2.5 权限控制缺失
- **位置**: PostController.toggleEssence()、OrganizationController.approveOrganization()/rejectOrganization()
- **问题**: 加精、组织审批等管理功能无权限校验，任何登录用户可操作
- **影响**: 功能滥用风险

### P1 - 用户体验明显受损

#### 2.6 活动报名是空壳
- **位置**: `backend/src/main/java/com/campus/backend/controller/ActivityController.java`
- **问题**: joinActivity() 和 cancelJoin() 只返回固定结果和打日志，不持久化
- **影响**: 用户以为报名成功但实际没有

#### 2.7 聊天消息发送失败无反馈
- **位置**: `frontend/src/views/ChatRoom.vue`
- **问题**: 消息发送失败无重试机制和错误提示
- **影响**: 用户不知道消息是否发送成功

#### 2.8 活动页 API 失败静默回退假数据
- **位置**: `frontend/src/views/Activities.vue`
- **问题**: API 请求失败时使用 `generateMockActivities()` 返回假数据，用户无法区分
- **影响**: 用户看到假数据以为是真的

#### 2.9 图片预览用 window.open
- **位置**: `frontend/src/views/ProductDetail.vue`、`PostDetailPage.vue`
- **问题**: 直接在新标签页打开图片，无缩放/滑动/手势关闭
- **影响**: 移动端体验差

### P2 - 功能补全

#### 2.10 商品缺"已售出/预约中"状态流转
- **位置**: `backend/src/main/java/com/campus/backend/controller/ProductController.java`
- **问题**: status 字段有 0下架/1在售/2已售出/3预约中，但 `toggleProductStatus` 只支持 0↔1 切换
- **影响**: 卖家无法标记商品已售出

#### 2.11 聊天表情面板/更多菜单无点击外部关闭
- **位置**: `frontend/src/views/ChatRoom.vue`
- **问题**: 表情面板和更多菜单缺少点击外部关闭逻辑
- **影响**: 需点其他按钮才能关闭

#### 2.12 Profile 统计数据加载期间显示 0
- **位置**: `frontend/src/views/Profile.vue`
- **问题**: 统计数据使用 `Promise.allSettled` 但没有 loading 状态
- **影响**: 数据加载期间显示 0，用户误以为没有数据

#### 2.13 分享功能无反馈
- **位置**: `ProductDetail.vue`、`PostDetailPage.vue`
- **问题**: `handleShare()` 使用 `navigator.clipboard` 但没有成功/失败 toast
- **影响**: 用户不知道是否复制成功

#### 2.14 CreateProduct 双 toast 系统
- **位置**: `frontend/src/views/CreateProduct.vue`
- **问题**: 自定义了 `showToast` 函数而非使用全局 `useToast`
- **影响**: 提示风格不统一

#### 2.15 NavBar 底部导航只有 3 个 Tab
- **位置**: `frontend/src/components/NavBar.vue`
- **问题**: 只有首页/发布/消息，缺少社区/活动入口
- **影响**: 移动端用户无法快速访问社区和活动

### P3 - 页面切换与展示体验

#### 2.16 路由过渡动画方向固定
- **位置**: `frontend/src/App.vue`
- **问题**: 使用固定 `translateY` 动画，无法区分前进/后退
- **影响**: 移动端用户期望进入详情页从右滑入，返回从左滑出

#### 2.17 $router.back() 无 fallback
- **位置**: ProductDetail.vue、PostDetailPage.vue、Products.vue、SearchPage.vue
- **问题**: 从外部链接进入详情页时，`$router.back()` 会跳出应用
- **影响**: 用户离开应用而非回到首页

#### 2.18 返回按钮样式不统一
- **位置**: ProductDetail.vue、PostDetailPage.vue、Products.vue、SearchPage.vue
- **问题**: 四个页面的返回按钮视觉风格完全不同
- **影响**: 视觉不一致

#### 2.19 骨架屏/空状态/加载指示器不统一
- **位置**: Home.vue、Products.vue、CommunityPage.vue、SearchPage.vue 等
- **问题**: 每个页面独立实现，视觉风格不统一，无可复用组件
- **影响**: 开发效率低，视觉不一致

#### 2.20 图片加载失败处理不统一
- **位置**: ProductDetail.vue、PostDetailPage.vue、ProductCard.vue、PostCard.vue
- **问题**: 5 种不同的图片加载失败处理方式
- **影响**: 用户体验不一致

#### 2.21 活动帖子跳转不统一
- **位置**: Home.vue vs CommunityPage.vue
- **问题**: Home 区分 ACTIVITY 类型跳转 `/activities/{id}`，CommunityPage 统一跳转 `/community/posts/{id}`
- **影响**: 同类型帖子不同入口跳转目标不一致

#### 2.22 ProductCard "聊一聊"按钮无登录检查
- **位置**: `frontend/src/components/ProductCard.vue`
- **问题**: 未登录用户点击后跳转聊天页，被路由守卫拦截到登录页
- **影响**: 体验割裂

### P4 - 代码质量与性能

#### 2.23 API 路径风格不统一
- **问题**: 商品用 `/api/products`，聊天用 `/api/chat`，收藏用 `/api/favorites`，其他用 `/api/v2/xxx`
- **影响**: 前端调用混乱，不利于版本管理

#### 2.24 推荐服务性能差
- **位置**: `backend/src/main/java/com/campus/backend/service/RecommendationService.java`
- **问题**: 一次推荐请求可能产生 100+ 次数据库查询
- **影响**: 接口响应慢

#### 2.25 Feed 分页越往后越慢
- **位置**: `backend/src/main/java/com/campus/backend/controller/FeedController.java`
- **问题**: 第 N 页查 N×size 条数据，查询量线性增长
- **影响**: 深度分页性能差

#### 2.26 硬编码颜色
- **位置**: PostDetailPage.vue、Settings.vue、NotFound.vue
- **问题**: 使用 #333、#999、#4CAF50 等硬编码颜色，与 design-system CSS 变量不一致
- **影响**: 主题切换困难，视觉不一致

#### 2.27 内存泄漏
- **位置**: Profile.vue (visibilitychange 未移除)、NavBar.vue (setInterval 未清除)
- **问题**: 事件监听和定时器未在组件销毁时清理
- **影响**: 长时间使用后内存占用增长

#### 2.28 console.log 残留
- **位置**: App.vue WebSocket 初始化
- **问题**: `console.log('收到新消息:', data)` 违反项目规则
- **影响**: 生产环境日志泄露

#### 2.29 FavoriteController/FeedController 直接注入 Mapper
- **问题**: 业务逻辑写在 Controller 中，违反分层原则
- **影响**: 代码可维护性差

#### 2.30 PostDetailPage 收藏失败回滚无提示
- **问题**: `isFavorited.value = !isFavorited.value` 回滚状态但无 toast
- **影响**: 用户不知道操作失败

---

## 三、优化方案（5 个批次）

### 批次 1: P0 安全缺陷

#### 1.1 SecurityConfig 权限修复
**修改文件:**
- `backend/src/main/java/com/campus/backend/entity/User.java` — 增加 `role` 字段 (String, 默认 "USER")
- `backend/src/main/java/com/campus/backend/config/SecurityConfig.java` — 将需要认证的接口改为 `authenticated()`
- `backend/src/main/java/com/campus/backend/service/CustomUserDetailsService.java` — 从数据库读取角色
- `backend/src/main/resources/data-mysql.sql` — 给管理用户添加 ADMIN 角色

**方案细节:**
- User 实体增加 `role` 字段，默认值 "USER"，管理员为 "ADMIN"
- SecurityConfig 按路径分组配置：公开路径 permitAll，认证路径 authenticated，管理路径 hasRole('ADMIN')
- CustomUserDetailsService 从 User 实体读取 role，构建 `GrantedAuthority`
- 白名单路径：`/api/v2/users/register`, `/api/v2/users/login`, `/api/v2/users/reset-password/**`, `/api/test/**` 等

#### 1.2 密码重置接入邮件服务
**修改文件:**
- `backend/pom.xml` — 添加 `spring-boot-starter-mail` 依赖
- `backend/src/main/resources/application.yml` — 配置 SMTP
- `backend/src/main/java/com/campus/backend/service/impl/UserServiceImpl.java` — 实现验证码生成、邮件发送、校验
- 新增 `backend/src/main/java/com/campus/backend/entity/VerificationCode.java` — 验证码实体
- 新增 `backend/src/main/java/com/campus/backend/mapper/VerificationCodeMapper.java` — 验证码 Mapper

**方案细节:**
- 生成 6 位随机验证码，存入数据库（关联用户、验证码、过期时间）
- 使用 JavaMailSender 发送验证码邮件
- 校验时检查验证码是否匹配且未过期（5 分钟有效期）
- 验证成功后删除已用验证码

#### 1.3 数据库测试接口保护
**修改文件:**
- `backend/src/main/java/com/campus/backend/controller/DatabaseTestController.java` — 添加 `@Profile("dev")` 注解

**方案细节:**
- 使用 Spring Profile 控制，只在 dev 环境加载此 Controller
- 生产环境不注册此 Bean，接口完全不可访问

#### 1.4 帖子详情 XSS 防护
**修改文件:**
- `frontend/package.json` — 添加 `dompurify` 依赖
- `frontend/src/views/PostDetailPage.vue` — 渲染前使用 DOMPurify 过滤

**方案细节:**
- 安装 `dompurify` 库
- `renderedContent` 计算属性中使用 `DOMPurify.sanitize()` 过滤危险标签和属性
- 保留安全的 HTML 标签（p, br, strong, em, a, img 等）

---

### 批次 2: P1 体验修复

#### 2.1 活动报名改为联系方式展示
**修改文件:**
- `backend/src/main/java/com/campus/backend/entity/Post.java` — 增加 `contactInfo` 字段（或复用现有字段）
- `backend/src/main/java/com/campus/backend/controller/ActivityController.java` — 报名接口返回联系方式
- `frontend/src/views/ActivityDetail.vue` — 报名按钮改为展示联系方式卡片
- `frontend/src/views/PostCreatePage.vue` — 发布活动时增加联系方式输入

**方案细节:**
- 发布活动时要求填写联系方式（微信/QQ/电话，至少一项）
- 活动详情页"我要报名"按钮点击后展示联系方式卡片，而非调用空壳报名接口
- 联系方式卡片包含复制功能

#### 2.2 聊天消息发送反馈
**修改文件:**
- `frontend/src/views/ChatRoom.vue`

**方案细节:**
- 消息发送中显示 loading 指示（时钟图标旋转）
- 发送失败显示红色感叹号，点击可重试
- 发送成功移除 loading 状态

#### 2.3 活动页假数据回退修复
**修改文件:**
- `frontend/src/views/Activities.vue`

**方案细节:**
- 移除 `generateMockActivities()` 函数
- API 失败时显示错误状态 + 重试按钮
- 空数据显示空状态提示

#### 2.4 图片预览弹窗
**修改文件:**
- 新增 `frontend/src/components/ImageViewer.vue`
- `frontend/src/views/ProductDetail.vue` — 使用 ImageViewer
- `frontend/src/views/PostDetailPage.vue` — 使用 ImageViewer

**方案细节:**
- 全屏遮罩层 + 图片居中展示
- 支持左右滑动切换图片
- 支持双指缩放（移动端）
- 点击遮罩层或下滑关闭
- 图片序号指示器 (1/5)

---

### 批次 3: P2 功能补全

#### 3.1 商品状态流转
**修改文件:**
- `backend/src/main/java/com/campus/backend/controller/ProductController.java` — 增加"标记已售出"接口
- `backend/src/main/java/com/campus/backend/service/impl/ProductServiceImpl.java` — 实现状态流转逻辑
- `frontend/src/views/ProductDetail.vue` — 卖家可看到"标记已售出"按钮
- `frontend/src/views/MyProducts.vue` — 商品列表显示状态标签

**方案细节:**
- 新增 `PUT /api/products/{id}/sold` 接口，将商品状态改为 2(已售出)
- 只有卖家本人可操作
- 已售出商品详情页显示"已售出"标签，购买按钮禁用

#### 3.2 表情面板/菜单点击外部关闭
**修改文件:**
- `frontend/src/views/ChatRoom.vue`

**方案细节:**
- 添加 `v-click-outside` 自定义指令
- 或使用 `@click.self` 在遮罩层关闭
- 表情面板和更多菜单打开时监听 document click 事件

#### 3.3 Profile 统计数据 loading 态
**修改文件:**
- `frontend/src/views/Profile.vue`

**方案细节:**
- 添加 `statsLoading` 状态变量
- 加载期间显示 skeleton 占位
- 加载完成后显示实际数据

#### 3.4 分享功能反馈
**修改文件:**
- `frontend/src/views/ProductDetail.vue`
- `frontend/src/views/PostDetailPage.vue`

**方案细节:**
- `handleShare()` 成功后调用 `showToast('链接已复制', 'success')`
- 失败时调用 `showToast('复制失败，请手动复制', 'error')`

#### 3.5 toast 系统统一
**修改文件:**
- `frontend/src/views/CreateProduct.vue`

**方案细节:**
- 移除自定义 `showToast` 函数
- 改用全局 `useToast()` 组合式函数

#### 3.6 导航完善
**修改文件:**
- `frontend/src/components/NavBar.vue` — 增加社区/活动入口
- `frontend/src/components/AppHeader.vue` — 桌面端增加消息/个人中心入口

**方案细节:**
- NavBar 从 3 Tab 扩展为 5 Tab：首页、社区、发布、消息、我的
- AppHeader 桌面端导航增加消息和个人中心链接

---

### 批次 4: 页面切换展示优化

#### 4.1 路由过渡动画
**修改文件:**
- `frontend/src/router/index.js` — 路由 meta 增加 `transition` 字段
- `frontend/src/App.vue` — 根据 meta.transition 选择不同动画

**方案细节:**
- 列表页 → 详情页：slide-left（从右滑入）
- 详情页 → 列表页：slide-right（从左滑出）
- 弹窗类页面：fade
- 首页 Tab 切换：fade

#### 4.2 $router.back() fallback
**修改文件:**
- `frontend/src/views/ProductDetail.vue`
- `frontend/src/views/PostDetailPage.vue`
- `frontend/src/views/Products.vue`
- `frontend/src/views/SearchPage.vue`

**方案细节:**
- 统一使用 `router.back()` + fallback 逻辑
- 封装为 `useBackNavigation` 组合式函数或直接内联判断
- `window.history.length > 1 ? router.back() : router.push('/')`

#### 4.3 返回按钮统一
**修改文件:**
- 新增 `frontend/src/components/BackButton.vue`
- 替换各详情页的返回按钮

**方案细节:**
- 统一视觉风格：圆形按钮 + 左箭头图标 + 半透明背景
- 内置 fallback 逻辑
- 支持 `fallback` prop 自定义回退路径

#### 4.4 骨架屏/空状态/加载指示器组件化
**修改文件:**
- 新增 `frontend/src/components/SkeletonCard.vue`
- 新增 `frontend/src/components/EmptyState.vue`
- 新增 `frontend/src/components/LoadingDots.vue`
- 各页面替换为统一组件

**方案细节:**
- SkeletonCard：支持不同类型（帖子/商品/详情）
- EmptyState：支持自定义图标、标题、副标题、操作按钮
- LoadingDots：统一的三点弹跳动画

#### 4.5 图片加载失败统一处理
**修改文件:**
- `frontend/src/components/PostCard.vue`
- `frontend/src/components/ProductCard.vue`
- `frontend/src/views/ProductDetail.vue`
- `frontend/src/views/PostDetailPage.vue`

**方案细节:**
- 统一使用 SVG 占位图（灰色背景 + 图片图标 + "加载失败"文字）
- 抽取 `onImageError` 为公共函数或组件内置

#### 4.6 活动帖子跳转统一
**修改文件:**
- `frontend/src/views/CommunityPage.vue`

**方案细节:**
- `goToPost` 中增加 ACTIVITY 类型判断，跳转 `/activities/{id}`
- 与 Home.vue 保持一致

---

### 批次 5: P3 代码质量与性能

#### 5.1 API 路径统一
**修改文件:**
- `backend/src/main/java/com/campus/backend/controller/ProductController.java` — `/api/products` → `/api/v2/products`
- `backend/src/main/java/com/campus/backend/controller/ChatController.java` — `/api/chat` → `/api/v2/chat`
- `backend/src/main/java/com/campus/backend/controller/FavoriteController.java` — `/api/favorites` → `/api/v2/favorites`
- `frontend/src/services/api.js` — 更新所有对应路径

**方案细节:**
- 后端 Controller 的 `@RequestMapping` 统一加 `/v2` 前缀
- 前端 api.js 同步更新
- 保持向后兼容：旧路径可保留重定向

#### 5.2 推荐服务性能优化
**修改文件:**
- `backend/src/main/java/com/campus/backend/service/RecommendationService.java`

**方案细节:**
- 批量查询替代 N+1 查询
- 一次查询获取所有需要的 Post/Product 和 User 信息
- 使用 IN 查询批量获取关联数据

#### 5.3 Feed 分页优化
**修改文件:**
- `backend/src/main/java/com/campus/backend/controller/FeedController.java`

**方案细节:**
- 限制最大页码（如最多 50 页）
- 或改为游标分页（基于最后一条数据的 ID/时间戳）
- 避免深度分页的线性查询增长

#### 5.4 硬编码颜色统一
**修改文件:**
- `frontend/src/views/PostDetailPage.vue`
- `frontend/src/views/Settings.vue`
- `frontend/src/views/NotFound.vue`

**方案细节:**
- 将硬编码颜色替换为 design-system.css 中定义的 CSS 变量
- #333 → var(--color-gray-800)
- #999 → var(--color-gray-500)
- #4CAF50 → var(--color-primary-500)

#### 5.5 内存泄漏修复
**修改文件:**
- `frontend/src/views/Profile.vue` — onUnmounted 中移除 visibilitychange 监听
- `frontend/src/components/NavBar.vue` — onUnmounted 中 clearInterval

#### 5.6 console.log 清理
**修改文件:**
- `frontend/src/App.vue` — 移除 WebSocket 调试日志

#### 5.7 Controller 层业务逻辑下沉
**修改文件:**
- `backend/src/main/java/com/campus/backend/controller/FavoriteController.java` — 逻辑移至 FavoriteService
- `backend/src/main/java/com/campus/backend/controller/FeedController.java` — 逻辑移至 RecommendationService

---

## 四、执行原则

1. **每批次拆分为小任务**：修改超过 3 个文件时先拆分
2. **先方案后代码**：每个小任务先确认方案再执行
3. **增量验证**：每完成一个小任务验证功能正常
4. **不破坏现有功能**：修改前确认基线，修改后验证回归
5. **遵循项目规则**：不删除已有文件、不修改配置文件（除非明确需要）、不留 console.log

---

## 五、关键文件索引

### 前端
| 文件 | 说明 |
|------|------|
| `frontend/src/App.vue` | 根组件，布局/路由过渡/WebSocket |
| `frontend/src/router/index.js` | 路由配置和守卫 |
| `frontend/src/services/api.js` | API 接口定义 |
| `frontend/src/store/auth.js` | 认证状态管理 |
| `frontend/src/styles/design-system.css` | CSS 变量定义 |
| `frontend/src/views/Home.vue` | 首页 |
| `frontend/src/views/Products.vue` | 商品列表 |
| `frontend/src/views/ProductDetail.vue` | 商品详情 |
| `frontend/src/views/CommunityPage.vue` | 社区页 |
| `frontend/src/views/PostDetailPage.vue` | 帖子详情 |
| `frontend/src/views/ChatRoom.vue` | 聊天室 |
| `frontend/src/views/Activities.vue` | 活动列表 |
| `frontend/src/views/ActivityDetail.vue` | 活动详情 |
| `frontend/src/views/Profile.vue` | 个人中心 |
| `frontend/src/views/Settings.vue` | 设置页 |
| `frontend/src/views/CreateProduct.vue` | 发布商品 |
| `frontend/src/views/PostCreatePage.vue` | 发布帖子 |
| `frontend/src/views/Messages.vue` | 消息列表 |
| `frontend/src/views/SearchPage.vue` | 搜索页 |
| `frontend/src/components/NavBar.vue` | 底部导航 |
| `frontend/src/components/PostCard.vue` | 帖子卡片 |
| `frontend/src/components/ProductCard.vue` | 商品卡片 |

### 后端
| 文件 | 说明 |
|------|------|
| `backend/src/main/java/com/campus/backend/config/SecurityConfig.java` | 安全配置 |
| `backend/src/main/java/com/campus/backend/service/CustomUserDetailsService.java` | 用户认证服务 |
| `backend/src/main/java/com/campus/backend/entity/User.java` | 用户实体 |
| `backend/src/main/java/com/campus/backend/service/impl/UserServiceImpl.java` | 用户业务逻辑 |
| `backend/src/main/java/com/campus/backend/controller/DatabaseTestController.java` | 数据库测试接口 |
| `backend/src/main/java/com/campus/backend/controller/ActivityController.java` | 活动接口 |
| `backend/src/main/java/com/campus/backend/controller/ProductController.java` | 商品接口 |
| `backend/src/main/java/com/campus/backend/controller/PostController.java` | 帖子接口 |
| `backend/src/main/java/com/campus/backend/controller/FavoriteController.java` | 收藏接口 |
| `backend/src/main/java/com/campus/backend/controller/FeedController.java` | Feed 接口 |
| `backend/src/main/java/com/campus/backend/controller/ChatController.java` | 聊天接口 |
| `backend/src/main/java/com/campus/backend/service/RecommendationService.java` | 推荐服务 |
| `backend/src/main/resources/data-mysql.sql` | 初始数据 |
| `backend/src/main/resources/application.yml` | 应用配置 |
