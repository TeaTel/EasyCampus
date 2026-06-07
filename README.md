# Campus-Community-V1.2.0

校园图文交流平台 1.2.0 交接版本

---

## 前端页面清单

### 技术栈

- **框架**: Vue 3 (Composition API + `<script setup>`)
- **路由**: Vue Router 4 (History 模式)
- **构建**: Vite 5
- **状态管理**: Pinia (auth store)
- **HTTP**: Axios
- **实时通信**: WebSocket (聊天)

### 目录结构

```
frontend/src/
├── App.vue                    # 根组件
├── main.js                    # 入口文件
├── assets/css/
│   └── design-system.css      # 全局设计系统样式
├── components/                # 可复用组件 (12个)
├── views/                     # 页面视图 (33个)
├── router/
│   └── index.js               # 路由配置
├── services/
│   └── api.js                 # API 服务层 + WebSocket
├── store/
│   └── auth.js                # 认证状态管理
└── use/
    └── useToast.js            # Toast 组合式函数
```

---

## 一、核心页面 (TabBar 主导航)

以下页面在底部 TabBar 中有入口，属于应用的核心页面。

### 1. 首页 / 发现页

| 属性 | 值 |
|------|-----|
| **路由** | `/` |
| **路由名** | `Home` |
| **是否需登录** | 否 |
| **显示TabBar** | 是 |
| **显示Header** | 是 |

**功能描述**: 应用首页，展示信息流（帖子+商品混合），支持标签筛选（圈子标签、校区标签），推荐内容，无限滚动加载。

**关联文件**:
- 页面: `frontend/src/views/Home.vue`
- 组件: `PostCard.vue`, `ProductCard.vue`
- API: `feedApi` (services/api.js)
- Store: `auth.js`

**导航出口**:
- → `/activities/:id` (点击活动卡片)
- → `/community/posts/:id` (点击帖子卡片)
- → `/products/:id` (点击商品卡片)
- → `/login` (未登录访问)

---

### 2. 商品列表页

| 属性 | 值 |
|------|-----|
| **路由** | `/products` |
| **路由名** | `Products` |
| **是否需登录** | 否 |
| **显示TabBar** | 是 |
| **显示Header** | 是 |

**功能描述**: 商品列表页，支持搜索、筛选（综合/价格/最新/分类）、分类标签切换、无限滚动加载，骨架屏加载态。

**关联文件**:
- 页面: `frontend/src/views/Products.vue`
- API: `productApi` (services/api.js)

**导航出口**:
- → `/products/:id` (点击商品卡片)

---

### 3. 分类浏览页

| 属性 | 值 |
|------|-----|
| **路由** | `/categories` |
| **路由名** | `Categories` |
| **是否需登录** | 否 |
| **显示TabBar** | 是 |
| **显示Header** | 是 |

**功能描述**: 商品分类浏览页，展示分类列表（含图标、描述、商品数量）和子分类，支持点击分类跳转商品列表。

**关联文件**:
- 页面: `frontend/src/views/Categories.vue`
- API: `categoryApi`, `productApi` (services/api.js)

**导航出口**:
- → `/products?categoryId=xxx` (选择分类后)

---

### 4. 社区页

| 属性 | 值 |
|------|-----|
| **路由** | `/community` |
| **路由名** | `Community` |
| **是否需登录** | 否 |
| **显示TabBar** | 是 |
| **显示Header** | 是 |

**功能描述**: 社区页面，含推荐/帖子/热门三个 Tab，展示帖子列表和商品列表，浮动发布按钮。

**关联文件**:
- 页面: `frontend/src/views/CommunityPage.vue`
- 组件: `PostCard.vue`
- API: `feedApi`, `postApi` (services/api.js)

**导航出口**:
- → `/community/posts/:id` (点击帖子)
- → `/products/:id` (点击商品)
- → `/community/posts/create` (点击发布按钮)

---

### 5. 消息列表页

| 属性 | 值 |
|------|-----|
| **路由** | `/messages` |
| **路由名** | `Messages` |
| **是否需登录** | 是 |
| **显示TabBar** | 是 |
| **显示Header** | 是 |

**功能描述**: 消息列表页，展示会话列表（头像、用户名、最后消息、未读数、在线状态），支持商品预览卡片，骨架屏加载。

**关联文件**:
- 页面: `frontend/src/views/Messages.vue`
- API: `messageApi` (services/api.js)

**导航出口**:
- → `/chat/:userId?productId=xxx` (打开聊天)
- → `/` (空状态去逛逛)

---

### 6. 个人中心页

| 属性 | 值 |
|------|-----|
| **路由** | `/profile` |
| **路由名** | `Profile` |
| **是否需登录** | 否 (未登录显示登录引导) |
| **显示TabBar** | 是 |
| **显示Header** | 是 |

**功能描述**: 个人中心页，展示用户头像/昵称/ID/简介/发布和收藏统计，支持头像上传更换、发布入口、菜单列表（商品与帖子/收藏/设置/关于/退出登录），未登录显示登录引导。

**关联文件**:
- 页面: `frontend/src/views/Profile.vue`
- 组件: `PublishActionSheet.vue`
- API: `productApi`, `postApi`, `favoriteApi`, `uploadApi`, `userApi` (services/api.js)
- Store: `auth.js`
- Composable: `useToast.js`

**导航出口**:
- → `/my-products` (商品与帖子)
- → `/favorites` (我的收藏)
- → `/settings` (设置)
- → `/login?redirect=/profile` (未登录)
- → `/register` (注册)

---

### 7. 兴趣圈子页

| 属性 | 值 |
|------|-----|
| **路由** | `/boards` |
| **路由名** | `BoardsDiscover` |
| **是否需登录** | 否 |
| **显示TabBar** | 是 |
| **显示Header** | 是 |

**功能描述**: 兴趣圈子发现页，以网格展示所有圈子分类。

**关联文件**:
- 页面: `frontend/src/views/BoardsDiscoverPage.vue`
- API: `categoryApi` (services/api.js)

**导航出口**:
- → `/boards/:id` (点击圈子)

---

### 8. 活动列表页

| 属性 | 值 |
|------|-----|
| **路由** | `/activities` |
| **路由名** | `Activities` |
| **是否需登录** | 否 |
| **显示TabBar** | 是 |
| **显示Header** | 是 |

**功能描述**: 校园活动列表页，含即将开始/进行中/已结束三个 Tab，使用 ActivityCard 组件展示活动卡片，支持加载更多和骨架屏。

**关联文件**:
- 页面: `frontend/src/views/Activities.vue`
- 组件: `ActivityCard.vue`
- API: `activityApi` (services/api.js)

**导航出口**:
- → `/activities/:id` (点击活动卡片)

---

## 二、认证页面

### 9. 登录页

| 属性 | 值 |
|------|-----|
| **路由** | `/login` |
| **路由名** | `Login` |
| **是否需登录** | 否 |
| **显示TabBar** | 否 |

**功能描述**: 登录页面，含品牌展示、表单输入、记住我、第三方登录入口（微信、校园认证）。

**关联文件**:
- 页面: `frontend/src/views/Login.vue`
- API: `userApi` (services/api.js)
- Store: `auth.js`

**导航出口**:
- → `/register` (点击注册链接)
- → `/forgot-password` (点击忘记密码)
- → `/` 或 redirect 参数 (登录成功)

---

### 10. 注册页

| 属性 | 值 |
|------|-----|
| **路由** | `/register` |
| **路由名** | `Register` |
| **是否需登录** | 否 |
| **显示TabBar** | 否 |

**功能描述**: 注册页面，含用户名/昵称/邮箱/密码表单，协议勾选。

**关联文件**:
- 页面: `frontend/src/views/Register.vue`
- Store: `auth.js`

**导航出口**:
- → `/login?registered=true&username=xxx` (注册成功)

---

### 11. 找回密码页

| 属性 | 值 |
|------|-----|
| **路由** | `/forgot-password` |
| **路由名** | `ForgotPassword` |
| **是否需登录** | 否 |
| **显示TabBar** | 否 |

**功能描述**: 找回密码页面，三步骤流程：输入邮箱/手机号 → 输入验证码和新密码 → 重置成功，支持验证码倒计时重发。

**关联文件**:
- 页面: `frontend/src/views/ForgotPassword.vue`
- API: `userApi` (services/api.js)

**导航出口**:
- → `/login` (返回登录/重置成功)

---

## 三、商品相关页面

### 12. 商品详情页

| 属性 | 值 |
|------|-----|
| **路由** | `/products/:id` |
| **路由名** | `ProductDetail` |
| **是否需登录** | 否 |
| **显示TabBar** | 否 |

**功能描述**: 商品详情页，展示图片画廊、价格、描述、标签、卖家信息、关注卖家、点赞、收藏、评论，底部操作栏含评论输入和聊天入口。

**关联文件**:
- 页面: `frontend/src/views/ProductDetail.vue`
- 组件: `LikeButton.vue`, `CommentSection.vue`
- API: `productApi`, `followApi`, `favoriteApi` (services/api.js)
- Store: `auth.js`

**导航出口**:
- → `/users/:sellerId` (查看卖家主页)
- → `/chat/:sellerId?productId=:id` (联系卖家)
- → `/login?redirect=...` (未登录)

---

### 13. 发布商品页

| 属性 | 值 |
|------|-----|
| **路由** | `/products/create` |
| **路由名** | `CreateProduct` |
| **是否需登录** | 是 |
| **显示TabBar** | 否 |

**功能描述**: 发布商品页面，支持图片上传、标题/描述/价格/分类/成色/交付方式/交易地点/标签。

**关联文件**:
- 页面: `frontend/src/views/CreateProduct.vue`
- 组件: `TagInput.vue`, `ImageUploader.vue`
- API: `productApi`, `categoryApi` (services/api.js)
- Store: `auth.js`

**导航出口**:
- → `/products` (发布成功)
- → `/login?redirect=/products/create` (未登录)

---

## 四、社区帖子相关页面

### 14. 帖子详情页

| 属性 | 值 |
|------|-----|
| **路由** | `/community/posts/:id` |
| **路由名** | `PostDetail` |
| **是否需登录** | 否 |
| **显示TabBar** | 否 |

**功能描述**: 帖子详情页，展示帖子内容、图片画廊、标签、评论、点赞、收藏、关注作者。

**关联文件**:
- 页面: `frontend/src/views/PostDetailPage.vue`
- 组件: `LikeButton.vue`, `CommentSection.vue`
- API: `postApi`, `followApi`, `favoriteApi` (services/api.js)
- Store: `auth.js`

**导航出口**:
- → `/users/:userId` (点击作者头像/用户名)

---

### 15. 发布帖子页

| 属性 | 值 |
|------|-----|
| **路由** | `/community/posts/create` |
| **路由名** | `PostCreate` |
| **是否需登录** | 是 |
| **显示TabBar** | 否 |

**功能描述**: 发布帖子页面，支持图片上传、帖子类型选择（讨论/展示/求助/活动）、标签输入、校区标签选择。

**关联文件**:
- 页面: `frontend/src/views/PostCreatePage.vue`
- 组件: `TagInput.vue`, `ImageUploader.vue`
- API: `postApi` (services/api.js)
- Store: `auth.js`

**导航出口**:
- → `/community/posts/:id` (发布成功)

---

## 五、聊天相关页面

### 16. 聊天室页

| 属性 | 值 |
|------|-----|
| **路由** | `/chat/:userId` |
| **路由名** | `ChatRoom` |
| **是否需登录** | 是 |
| **显示TabBar** | 否 |

**功能描述**: 聊天室页面，实时消息（WebSocket），表情选择器，商品卡片内联展示，消息已读/未读状态。

**关联文件**:
- 页面: `frontend/src/views/ChatRoom.vue`
- API: `messageApi`, `productApi`, `userApi` (services/api.js)
- Store: `auth.js`
- Composable: `useToast.js`
- WebSocket: `wsManager` (services/api.js)

**导航出口**:
- → 返回上一页
- → `/users/:userId` (查看联系人资料)

---

## 六、用户相关页面

### 17. 用户主页

| 属性 | 值 |
|------|-----|
| **路由** | `/users/:id` |
| **路由名** | `UserProfile` |
| **是否需登录** | 否 |
| **显示TabBar** | 否 |

**功能描述**: 用户主页，展示头像/昵称/学校/关注粉丝统计/帖子列表，支持关注/私聊。

**关联文件**:
- 页面: `frontend/src/views/UserProfilePage.vue`
- 组件: `PostCard.vue`
- API: `userApi`, `postApi`, `followApi` (services/api.js)
- Store: `auth.js`

**导航出口**:
- → `/profile` (编辑资料)
- → `/chat/:userId` (私聊)
- → `/community/posts/:id` (点击帖子)
- → `/login` (未登录)

---

### 18. 商品与帖子管理页

| 属性 | 值 |
|------|-----|
| **路由** | `/my-products` |
| **路由名** | `MyProducts` |
| **是否需登录** | 是 |
| **显示TabBar** | 否 |

**功能描述**: 商品与帖子管理页，双 Tab 切换（商品/帖子），支持商品上架/下架/删除、帖子删除，含确认删除弹窗。

**关联文件**:
- 页面: `frontend/src/views/MyProducts.vue`
- API: `productApi`, `postApi` (services/api.js)
- Store: `auth.js`
- Composable: `useToast.js`

**导航出口**:
- → `/products/:id` (商品详情)
- → `/community/posts/:id` (帖子详情)
- → `/products/create` (发布商品)
- → `/community/posts/create` (发布帖子)

---

### 19. 我的收藏页

| 属性 | 值 |
|------|-----|
| **路由** | `/favorites` |
| **路由名** | `Favorites` |
| **是否需登录** | 是 |
| **显示TabBar** | 否 |

**功能描述**: 我的收藏页，展示收藏的商品列表（封面/名称/价格/状态/收藏时间），支持取消收藏。

**关联文件**:
- 页面: `frontend/src/views/Favorites.vue`
- API: `favoriteApi` (services/api.js)

**导航出口**:
- → `/products/:id` (商品详情)
- → `/products` (空状态逛一逛)

---

### 20. 我的活动页

| 属性 | 值 |
|------|-----|
| **路由** | `/my-activities` |
| **路由名** | `MyActivities` |
| **是否需登录** | 是 |
| **显示TabBar** | 否 |

**功能描述**: 我的活动管理页，展示已创建的活动列表（含状态标签：即将开始/进行中/已结束），支持创建新活动（弹窗表单含标题/介绍/地点/时间/联系方式）。

**关联文件**:
- 页面: `frontend/src/views/MyActivities.vue`
- API: `activityApi` (services/api.js)
- Store: `auth.js`
- Composable: `useToast.js`

**导航出口**:
- → `/activities/:id` (查看活动详情)

---

### 21. 设置页

| 属性 | 值 |
|------|-----|
| **路由** | `/settings` |
| **路由名** | `Settings` |
| **是否需登录** | 是 |
| **显示TabBar** | 否 |

**功能描述**: 设置页面，含账户安全（修改密码）、通知设置、隐私设置、注销账户。

**关联文件**:
- 页面: `frontend/src/views/Settings.vue`
- 组件: `NavBar.vue`
- Store: `auth.js`
- Composable: `useToast.js`

---

### 22. 收货地址页

| 属性 | 值 |
|------|-----|
| **路由** | `/address` |
| **路由名** | `Address` |
| **是否需登录** | 是 |
| **显示TabBar** | 否 |

**功能描述**: 收货地址管理页，支持新增/编辑/删除/设为默认地址，数据存储在 localStorage。

**关联文件**:
- 页面: `frontend/src/views/Address.vue`
- Composable: `useToast.js`

**导航出口**:
- → 返回上一页

---

### 23. 校区设置页

| 属性 | 值 |
|------|-----|
| **路由** | `/campus` |
| **路由名** | `Campus` |
| **是否需登录** | 是 |
| **显示TabBar** | 否 |

**功能描述**: 校区设置页，选择学校（韩山师范学院）和校区（南三区/南二区/南一区/中区/东区/西区），保存到用户资料。

**关联文件**:
- 页面: `frontend/src/views/CampusPage.vue`
- API: `userApi` (services/api.js)
- Store: `auth.js`
- Composable: `useToast.js`

**导航出口**:
- → 返回上一页

---

## 七、活动相关页面

### 24. 活动详情页

| 属性 | 值 |
|------|-----|
| **路由** | `/activities/:id` |
| **路由名** | `ActivityDetail` |
| **是否需登录** | 否 |
| **显示TabBar** | 否 |

**功能描述**: 活动详情页，展示活动封面/时间/地点/联系方式/标签/组织者，支持报名/取消报名、点赞、评论。

**关联文件**:
- 页面: `frontend/src/views/ActivityDetail.vue`
- 组件: `LikeButton.vue`, `CommentSection.vue`
- API: `activityApi` (services/api.js)

**导航出口**:
- → `/users/:userId` (点击组织者)

---

## 八、组织相关页面

### 25. 创建组织页

| 属性 | 值 |
|------|-----|
| **路由** | `/orgs/create` |
| **路由名** | `CreateOrg` |
| **是否需登录** | 否 |
| **显示TabBar** | 否 |

**功能描述**: 创建组织页面，三步骤表单（基本信息 → 详细资料 → 确认提交），支持组织类型/加入方式选择。

**关联文件**:
- 页面: `frontend/src/views/CreateOrgPage.vue`
- API: `organizationApi` (services/api.js)
- Composable: `useToast.js`

**导航出口**:
- → `/orgs/my` (创建成功)

---

### 26. 我的组织页

| 属性 | 值 |
|------|-----|
| **路由** | `/orgs/my` |
| **路由名** | `MyOrgs` |
| **是否需登录** | 否 |
| **显示TabBar** | 否 |

**功能描述**: 我的组织列表页，展示已加入的组织和审核状态，提供创建/发现组织入口。

**关联文件**:
- 页面: `frontend/src/views/MyOrgsPage.vue`
- API: `organizationApi` (services/api.js)
- Composable: `useToast.js`

**导航出口**:
- → `/orgs/create` (创建组织)
- → `/orgs/discover` (发现组织)
- → `/orgs/:id` (点击组织)

---

### 27. 发现组织页

| 属性 | 值 |
|------|-----|
| **路由** | `/orgs/discover` |
| **路由名** | `OrgDiscover` |
| **是否需登录** | 否 |
| **显示TabBar** | 否 |

**功能描述**: 发现组织页，支持搜索组织，展示组织列表（Logo/名称/简介/类型/成员数/加入方式），支持申请加入。

**关联文件**:
- 页面: `frontend/src/views/OrgDiscoverPage.vue`
- API: `organizationApi` (services/api.js)
- Composable: `useToast.js`

**导航出口**:
- → `/orgs/:id` (查看组织详情)

---

### 28. 组织详情页

| 属性 | 值 |
|------|-----|
| **路由** | `/orgs/:id` |
| **路由名** | `OrgDetail` |
| **是否需登录** | 否 |
| **显示TabBar** | 否 |

**功能描述**: 组织详情页，展示组织信息、成员统计、申请加入/管理面板（申请列表/成员管理/邀请/操作日志）。

**关联文件**:
- 页面: `frontend/src/views/OrgDetailPage.vue`
- API: `organizationApi` (services/api.js)
- Composable: `useToast.js`

**导航出口**:
- → 返回上一页

---

## 九、其他页面

### 29. 搜索页

| 属性 | 值 |
|------|-----|
| **路由** | `/search` |
| **路由名** | `Search` |
| **是否需登录** | 否 |
| **显示TabBar** | 否 |

**功能描述**: 全局搜索页，支持搜索帖子/商品/用户，含筛选 Tab（全部/帖子/商品/用户），搜索关键词高亮显示，支持 URL query 参数触发搜索。

**关联文件**:
- 页面: `frontend/src/views/SearchPage.vue`
- API: `searchApi` (services/api.js)

**导航出口**:
- → `/users/:userId` (点击用户)
- → `/community/posts/:id` (点击帖子)
- → `/activities/:id` (点击活动帖子)
- → `/products/:productId` (点击商品)

---

### 30. 圈子详情页

| 属性 | 值 |
|------|-----|
| **路由** | `/boards/:id` |
| **路由名** | `BoardDetail` |
| **是否需登录** | 否 |
| **显示TabBar** | 否 |

**功能描述**: 圈子详情页（建设中），仅展示圈子名称和占位提示。

**关联文件**:
- 页面: `frontend/src/views/BoardPage.vue`
- API: `categoryApi` (services/api.js)

**导航出口**:
- → 返回上一页

---

### 31. 广告编辑页

| 属性 | 值 |
|------|-----|
| **路由** | `/ads/create` |
| **路由名** | `AdCreate` |
| **是否需登录** | 是 |
| **显示TabBar** | 否 |

**功能描述**: 广告编辑/发布页面，支持图片上传、标签、推流套餐选择（体验/基础/热门/爆款）、模拟支付。

**关联文件**:
- 页面: `frontend/src/views/AdEditPage.vue`
- 组件: `TagInput.vue`, `ImageUploader.vue`
- API: `adApi` (services/api.js)

**导航出口**:
- → `/community/posts/:id` (支付成功)

---

### 32. 404 页面

| 属性 | 值 |
|------|-----|
| **路由** | `/:pathMatch(.*)*` |
| **路由名** | `NotFound` |
| **是否需登录** | 否 |
| **显示TabBar** | 否 |

**功能描述**: 404 页面未找到，展示错误提示和返回首页/浏览商品按钮，含帮助建议列表。

**关联文件**:
- 页面: `frontend/src/views/NotFound.vue`
- 组件: `NavBar.vue`

**导航出口**:
- → `/` (返回首页)
- → `/products` (浏览商品)

---

### 33. 数据库测试页

| 属性 | 值 |
|------|-----|
| **路由** | `/db-test` |
| **路由名** | `DatabaseTest` |
| **是否需登录** | 否 |
| **显示TabBar** | 否 |

**功能描述**: 数据库连接测试工具页，展示连接状态、数据库信息、表数据量，支持自定义 SQL 查询测试（开发调试用）。

**关联文件**:
- 页面: `frontend/src/views/DatabaseTest.vue`

**导航出口**:
- → `/` (返回首页)

---

## 十、可复用组件清单

| 组件名 | 文件路径 | 功能描述 | 被引用页面 |
|--------|---------|---------|-----------|
| AppHeader | `components/AppHeader.vue` | 顶部导航栏，含关注/发现/校区 Tab 切换和搜索框 | App.vue |
| NavBar | `components/NavBar.vue` | 底部 TabBar 导航（首页/发布/消息），消息 Tab 显示未读数徽章 | App.vue, Settings.vue, NotFound.vue |
| SideMenu | `components/SideMenu.vue` | 侧边抽屉菜单，展示用户信息/功能菜单 | App.vue |
| PostCard | `components/PostCard.vue` | 帖子卡片组件，展示封面图/标题/标签/用户信息/点赞 | Home.vue, CommunityPage.vue, UserProfilePage.vue |
| ProductCard | `components/ProductCard.vue` | 商品卡片组件，展示封面图/价格/标题/标签/用户信息 | Home.vue |
| LikeButton | `components/LikeButton.vue` | 点赞按钮组件，支持点赞/取消点赞，显示点赞数 | PostCard.vue, ProductCard.vue, PostDetailPage.vue, ProductDetail.vue, ActivityDetail.vue, CommentSection.vue |
| CommentSection | `components/CommentSection.vue` | 评论组件，支持评论列表/回复/删除/点赞 | PostDetailPage.vue, ProductDetail.vue, ActivityDetail.vue |
| TagInput | `components/TagInput.vue` | 标签输入组件，支持预设建议/逗号分隔/XSS过滤 | PostCreatePage.vue, CreateProduct.vue, AdEditPage.vue |
| ImageUploader | `components/ImageUploader.vue` | 图片上传组件，支持多图/格式验证/大文件分片上传 | PostCreatePage.vue, CreateProduct.vue, AdEditPage.vue |
| ActivityCard | `components/ActivityCard.vue` | 活动卡片组件，展示封面/标题/组织者/时间/地点/状态 | Activities.vue |
| PublishActionSheet | `components/PublishActionSheet.vue` | 发布类型选择底部弹窗（发布帖子/发布交易） | NavBar.vue, SideMenu.vue, Profile.vue |
| ToastProvider | `components/ToastProvider.vue` | Toast 提示和确认弹窗提供者，全局注入 | App.vue |

---

## 十一、核心模块清单

| 模块名 | 文件路径 | 功能描述 |
|--------|---------|---------|
| auth store | `store/auth.js` | 认证状态管理，token/user 响应式状态，login/register/logout 方法，localStorage 持久化 |
| api service | `services/api.js` | API 服务层，含 axios 实例配置、JWT 拦截、WebSocket 管理器、16 个 API 模块 |
| useToast | `use/useToast.js` | Toast 组合式函数，通过 inject 获取全局 showToast/showConfirm 方法 |
| design-system | `assets/css/design-system.css` | 全局设计系统样式变量和基础样式 |
| router | `router/index.js` | 路由配置，含 33 个路由、全局守卫、白名单、认证检查 |

---

## 十二、API 模块清单

| API 模块 | 功能 | 主要使用页面 |
|---------|------|-------------|
| userApi | 用户注册/登录/资料/密码 | Login, Register, ForgotPassword, Profile, Settings, CampusPage, UserProfilePage |
| productApi | 商品 CRUD/列表/搜索 | Products, ProductDetail, CreateProduct, MyProducts, Home, Profile |
| postApi | 帖子 CRUD/列表/评论 | CommunityPage, PostDetailPage, PostCreatePage, MyProducts, UserProfilePage |
| messageApi | 聊天会话/消息 | Messages, ChatRoom |
| favoriteApi | 收藏/取消收藏 | Favorites, ProductDetail, PostDetailPage, Profile |
| categoryApi | 分类查询 | Categories, BoardsDiscoverPage, BoardPage, CreateProduct |
| searchApi | 全局搜索 | SearchPage |
| productCommentApi | 商品评论 | ProductDetail (通过 CommentSection) |
| likeApi | 点赞/取消点赞 | LikeButton |
| followApi | 关注/取消关注 | UserProfilePage, PostDetailPage, ProductDetail |
| feedApi | 信息流/推荐 | Home, CommunityPage |
| activityApi | 活动 CRUD/报名 | Activities, ActivityDetail, MyActivities |
| storyApi | 商品故事 | ProductDetail |
| adApi | 广告创建/支付 | AdEditPage |
| uploadApi | 图片上传 | ImageUploader, Profile |
| organizationApi | 组织 CRUD/成员/申请 | CreateOrgPage, OrgDetailPage, OrgDiscoverPage, MyOrgsPage |

---

## 十三、页面导航关系图

```
┌─────────────────────────────────────────────────────────────┐
│                      TabBar 主导航                           │
│  ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐         │
│  │ 首页 │  │ 分类 │  │ 社区 │  │ 消息 │  │ 我的 │         │
│  │  /   │  │/cats │  │/comm │  │/msg  │  │/prof │         │
│  └──┬───┘  └──┬───┘  └──┬───┘  └──┬───┘  └──┬───┘         │
│     │         │         │         │         │               │
│     ▼         ▼         ▼         ▼         ▼               │
│  ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐         │
│  │信息流│  │分类列表│  │帖子列表│  │会话列表│  │个人中心│       │
│  └──────┘  └──────┘  └──────┘  └──────┘  └──────┘         │
└─────────────────────────────────────────────────────────────┘

首页 ──→ 商品详情 ──→ 聊天室
 │         │    └──→ 卖家主页 ──→ 私聊
 │         └──→ 联系卖家
 ├──→ 帖子详情 ──→ 作者主页
 ├──→ 活动详情 ──→ 组织者主页
 └──→ 搜索页 ──→ 用户/帖子/商品/活动

社区 ──→ 发布帖子 ──→ 帖子详情
 │
 └──→ 帖子详情 ──→ 作者主页

消息 ──→ 聊天室 ──→ 联系人主页

我的 ──→ 商品与帖子管理 ──→ 发布商品/帖子
 ├──→ 我的收藏 ──→ 商品详情
 ├──→ 设置
 ├──→ 我的活动 ──→ 活动详情
 └──→ (侧边菜单) ──→ 组织管理/校区/广告

认证流程: 登录 ←→ 注册
          登录 ──→ 找回密码 ──→ 登录
```

---

## 十四、全局布局组件

| 组件 | 位置 | 显示条件 | 功能 |
|------|------|---------|------|
| AppHeader | 页面顶部 | `route.meta.showTabBar === true` | 关注/发现/校区 Tab + 搜索框 |
| NavBar (TabBar) | 页面底部 | 始终渲染（内部根据路由控制显隐） | 首页/发布/消息 三个 Tab |
| SideMenu | 左侧抽屉 | 点击汉堡按钮触发 | 侧边导航菜单 |
| ToastProvider | 全局浮层 | 始终渲染 | Toast 提示和确认弹窗 |
