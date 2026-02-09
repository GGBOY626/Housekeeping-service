# jzo2o-web

云岚到家 Web 端（浏览器版），由 jzo2o-consumer 小程序改造而来。

## 技术栈

- Vue 3
- Vite 5
- Vue Router 4
- Vuex 4
- Axios

## 功能说明

- **登录方式**：账号密码登录（手机号 + 密码）
- **注册**：支持新用户注册
- **首页**：服务展示、城市选择
- **服务**：服务分类、服务列表、服务详情
- **搜索**：服务搜索
- **我的**：用户信息、退出登录

## 开发

```bash
npm install
npm run dev
```

默认端口：5174

## 构建

```bash
npm run build
```

## 后端配置

- 后端服务：jzo2o-code 下的 jzo2o-customer、jzo2o-gateway、jzo2o-foundations 等
- 开发时通过 vite proxy 将 `/api` 代理到 `http://localhost:11500`
- 如需修改代理地址，编辑 `vite.config.js` 中的 `server.proxy`

## 数据库变更

执行数据库变更前，请参考 [DATABASE_MIGRATION.md](./DATABASE_MIGRATION.md)。
