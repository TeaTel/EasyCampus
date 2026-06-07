# 密码重置功能配置指南

> 本文档说明如何配置和使用校园社区平台的密码重置功能。

---

## 一、功能概述

密码重置流程：
1. 用户输入账号（用户名/手机号/邮箱）
2. 系统生成6位数字验证码，发送到用户绑定的邮箱
3. 用户输入验证码 + 新密码
4. 系统校验验证码，通过后重置密码

**前提条件：** 用户必须绑定了邮箱才能使用密码重置功能。

---

## 二、数据库变更

### 2.1 users 表增加 role 列

```sql
ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT 'USER' COMMENT '角色:USER,ADMIN' AFTER status;
```

### 2.2 设置管理员账号

```sql
-- 将 user01 设为管理员
UPDATE users SET role = 'ADMIN' WHERE username = 'user01';
```

### 2.3 创建 verification_codes 表

```sql
CREATE TABLE IF NOT EXISTS verification_codes (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '验证码ID',
    account         VARCHAR(100) NOT NULL COMMENT '关联的邮箱或手机号',
    code            VARCHAR(6)   NOT NULL COMMENT '验证码',
    type            VARCHAR(30)  NOT NULL DEFAULT 'RESET_PASSWORD' COMMENT '类型:RESET_PASSWORD',
    used            TINYINT(1)   DEFAULT 0 COMMENT '是否已使用:0未使用,1已使用',
    expire_at       DATETIME     NOT NULL COMMENT '过期时间',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_account_type (account, type),
    INDEX idx_expire_at (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证码表';
```

### 2.4 清理过期验证码（可选，建议定时执行）

```sql
DELETE FROM verification_codes WHERE expire_at < CURRENT_TIMESTAMP;
```

---

## 三、邮件服务配置

### 3.1 环境变量

密码重置依赖邮件发送服务，需配置以下环境变量：

| 环境变量 | 说明 | 示例值 |
|---------|------|--------|
| `MAIL_HOST` | SMTP 服务器地址 | `smtp.qq.com` |
| `MAIL_PORT` | SMTP 端口 | `587` |
| `MAIL_USERNAME` | 发件邮箱账号 | `your_email@qq.com` |
| `MAIL_PASSWORD` | 邮箱授权码（非登录密码） | `abcdefghijklmnop` |

### 3.2 常见邮箱 SMTP 配置

#### QQ 邮箱（默认）

1. 登录 QQ 邮箱 → 设置 → 账户
2. 找到「POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务」
3. 开启「POP3/SMTP服务」或「IMAP/SMTP服务」
4. 按提示生成**授权码**（16位字母，不是QQ密码）
5. 配置环境变量：

```bash
export MAIL_HOST=smtp.qq.com
export MAIL_PORT=587
export MAIL_USERNAME=你的QQ邮箱@qq.com
export MAIL_PASSWORD=生成的16位授权码
```

#### 163 邮箱

```bash
export MAIL_HOST=smtp.163.com
export MAIL_PORT=465
export MAIL_USERNAME=你的邮箱@163.com
export MAIL_PASSWORD=客户端授权密码
```

#### Gmail

```bash
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=你的邮箱@gmail.com
export MAIL_PASSWORD=应用专用密码
```

### 3.3 启动后端时设置环境变量

**方式一：命令行直接设置**

```bash
MAIL_HOST=smtp.qq.com MAIL_PORT=587 MAIL_USERNAME=xxx@qq.com MAIL_PASSWORD=xxx ./mvnw spring-boot:run
```

**方式二：创建 .env 文件**

在 `backend/` 目录下创建 `.env` 文件（不要提交到 Git）：

```env
MAIL_HOST=smtp.qq.com
MAIL_PORT=587
MAIL_USERNAME=your_email@qq.com
MAIL_PASSWORD=your_auth_code
```

然后通过 IDE 或脚本加载环境变量后启动。

**方式三：直接修改 application-dev.yml**

仅限本地开发使用，不要提交到版本控制：

```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 587
    username: your_email@qq.com
    password: your_auth_code
```

---

## 四、API 接口说明

### 4.1 发送验证码

```
POST /api/v2/users/reset-password/send-code
Content-Type: application/json

{
  "account": "user01"       // 用户名、手机号或邮箱
}
```

**成功响应：**
```json
{
  "code": 200,
  "message": "验证码已发送"
}
```

**错误响应：**
| 场景 | message |
|------|---------|
| 用户不存在 | 用户不存在: xxx |
| 用户无邮箱 | 该用户未设置邮箱，无法发送验证码 |
| 邮件发送失败 | 验证码发送失败，请稍后重试 |

### 4.2 验证并重置密码

```
POST /api/v2/users/reset-password/verify
Content-Type: application/json

{
  "account": "user01",       // 用户名、手机号或邮箱
  "verifyCode": "123456",    // 6位验证码
  "newPassword": "newPass123" // 新密码
}
```

**成功响应：**
```json
{
  "code": 200,
  "message": "密码重置成功"
}
```

**错误响应：**
| 场景 | message |
|------|---------|
| 验证码格式错误 | 验证码格式错误 |
| 验证码过期/未发送 | 验证码已过期或未发送，请重新获取 |
| 验证码错误 | 验证码错误 |
| 用户无邮箱 | 该用户未设置邮箱，无法验证 |

---

## 五、验证码规则

| 规则 | 说明 |
|------|------|
| 格式 | 6位纯数字 |
| 有效期 | 5分钟 |
| 使用次数 | 一次性，使用后标记为已使用 |
| 存储 | 数据库 `verification_codes` 表 |
| 发送频率 | 无限制（建议前端做60秒倒计时） |

---

## 六、测试数据说明

项目初始数据中，所有用户（user01-user80）均绑定了邮箱，格式为 `userXX@hstc.edu.cn`。

这些是虚拟邮箱，实际无法收到验证码邮件。如需测试密码重置功能，请：

1. 将某个用户的邮箱改为你的真实邮箱：
```sql
UPDATE users SET email = 'your_real_email@qq.com' WHERE username = 'user01';
```

2. 配置好邮件服务环境变量后启动后端

3. 在忘记密码页面输入 `user01`，验证码将发送到你的真实邮箱

---

## 七、涉及文件清单

| 文件 | 变更类型 | 说明 |
|------|---------|------|
| `backend/pom.xml` | 修改 | 添加 spring-boot-starter-mail 依赖 |
| `backend/src/main/resources/application-dev.yml` | 修改 | 添加 SMTP 配置 |
| `backend/src/main/resources/schema-mysql.sql` | 修改 | 添加 verification_codes 表 |
| `backend/src/main/java/com/campus/backend/entity/VerificationCode.java` | 新增 | 验证码实体 |
| `backend/src/main/java/com/campus/backend/mapper/VerificationCodeMapper.java` | 新增 | 验证码 Mapper |
| `backend/src/main/java/com/campus/backend/service/impl/UserServiceImpl.java` | 修改 | 实现验证码生成/发送/校验逻辑 |
