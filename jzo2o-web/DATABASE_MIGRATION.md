# 数据库变更说明 - C端账号密码登录

## 一、表变更：common_user

### 新增字段

在 `common_user` 表中新增 `password` 字段，用于存储 BCrypt 加密后的密码（账号密码登录）：

```sql
-- MySQL
ALTER TABLE common_user ADD COLUMN password VARCHAR(255) DEFAULT NULL COMMENT '密码(BCrypt加密,账号密码登录用)' AFTER open_id;
```

### 字段说明

| 字段名   | 类型         | 允许空 | 说明                                        |
|----------|--------------|--------|---------------------------------------------|
| password | VARCHAR(255) | 是     | 密码，BCrypt 加密存储。微信登录用户可为空。 |

### 注意事项

1. **已有数据**：现有通过微信登录注册的用户 `password` 为 NULL，不影响原有逻辑。
2. **新注册用户**：通过 Web 端注册的用户会写入加密后的密码。
3. **open_id**：微信登录用户有 open_id；账号密码注册用户 open_id 可为 NULL。

## 二、后端已做修改

- 新增 `LoginForCustomerPasswordReqDTO`：账号密码登录请求
- 新增 `RegisterForCustomerReqDTO`：注册请求
- 新增接口：
  - `POST /customer/open/login/common/user/password` 账号密码登录
  - `POST /customer/open/login/common/user/register` 注册
- `CommonUser` 实体新增 `password` 字段
- `CommonUserMapper.xml` 新增 `password` 映射

## 三、网关白名单

已在 `jzo2o-gateway` 的 `bootstrap.yml` 中增加白名单：

- `/customer/open/login/common/user/password`
- `/customer/open/login/common/user/register`

## 四、执行顺序

1. 执行上述 SQL 变更
2. 重启 jzo2o-customer 服务
3. 重启 jzo2o-gateway 服务（如单独部署）
