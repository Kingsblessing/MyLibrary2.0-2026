# 图书管理系统 (Library Management System)

一个面向书店的全栈图书进销存管理系统，支持图书进货、销售、库存管理和财务核算。

**主要用户**：超级管理员（SUPER）可管理用户和全部业务；普通管理员（NORMAL）可管理图书、进货和财务。

---

## 技术栈

| 层级 | 技术 |
|------|------|
| **后端** | Java 21, Spring Boot 3.4.5, Spring JDBC (JdbcTemplate), PostgreSQL |
| **前端** | Vue 3, Vite, Element Plus, Axios, ECharts, Vue Router |
| **构建** | Maven (后端) / npm (前端) |
| **开发工具** | VS Code |

---

## 快速开始

### 环境准备

- JDK 17+
- Node.js 16+
- PostgreSQL 运行于 `localhost:5432`

### 数据库

1. 创建数据库：
   ```sql
   CREATE DATABASE "MyLibrary";
   ```

2. 启动后 `schema.sql` 和 `data.sql` 将自动初始化表结构和种子数据。

3. 默认超级管理员账户：
   ```
   用户名：admin
   密码：  admin123
   ```
   密码以 MD5 密文存储于 `users` 表。

### 后端

```bash
cd library
# 配置 application.properties 中的数据库连接（数据库名、用户名、密码）
./mvnw spring-boot:run
```

后端启动在 `http://localhost:8080`。

### 前端

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器启动在 `http://localhost:5173`，对 `/api` 的请求自动代理到后端 `8080` 端口，无需额外 CORS 配置。

> 后端已内置 `CorsFilter`，允许所有来源的跨域请求，分离部署时无需额外配置。

---

## 主要功能模块

| 模块 | 超级管理员 | 普通管理员 | 说明 |
|------|:---:|:---:|------|
| 用户管理 | 增删改查全部用户 | 仅修改自身信息/密码 | 登录、角色控制 |
| 图书管理 | 增删改查、出售 | 增删改查、出售 | 库存书籍维护 |
| 图书查询 | 按 ISBN/书名/作者/出版社搜索 | 同左 | 模糊匹配，分页 100 条/页 |
| 图书进货 | 创建进货单、付款、退货 | 同左 | 支持从库存选择或录入新书 |
| 新书入库 | 已付款进货转为库存 | 同左 | 需填写零售价 |
| 图书出售 | 按零售价出售并扣库存 | 同左 | 自动生成财务收入记录 |
| 财务管理 | 账单列表、财务报表 | 同左 | ECharts 展示收支趋势 |

### 业务状态流转

```
进货: UNPAID → (付款) → PAID → (入库) → STOCKED
              → (退货) → RETURNED (仅 UNPAID 可退)

出售: 库存 books.stock_quantity 减少，生成 INCOME 财务记录
付款: 生成 EXPENSE 财务记录
```

---

## API 接口概要

- **Base URL**: `/api`
- **统一响应格式**:
  ```json
  { "code": 200, "message": "success", "data": {} }
  ```
  HTTP 状态码统一为 200，成功/失败由 `code` 区分。`code=200` 表示成功。
- **身份验证**: 请求头 `X-Token` 携带登录后获取的 MD5 token，`AuthInterceptor` 拦截除 `/api/user/login` 外的所有 `/api/**` 请求。

### 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或登录已过期 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 1001 | 用户名或密码错误 |
| 1002 | 用户名已存在 |
| 1003 | 旧密码错误 |
| 2001 | 图书不存在 |
| 2002 | ISBN 已存在 |
| 2003 | 库存不足 |
| 3001 | 进货记录不存在 |
| 3002 | 进货状态不允许此操作 |
| 3003 | 已付款的进货才能入库 |

### 接口列表

#### 用户接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/user/login` | 用户登录 | 公开 |
| POST | `/api/user/logout` | 登出 | 登录用户 |
| GET | `/api/user/info` | 获取当前用户信息 | 登录用户 |
| GET | `/api/user/{id}` | 获取指定用户信息 | 登录用户 |
| PUT | `/api/user/password` | 修改密码 | 登录用户 |
| GET | `/api/user/list` | 用户列表（支持 keyword 搜索） | SUPER |
| POST | `/api/user/add` | 创建用户 | SUPER |
| PUT | `/api/user/{id}` | 更新用户信息 | SUPER |
| DELETE | `/api/user/{id}` | 删除用户 | SUPER |

#### 图书接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/book/list` | 图书列表（支持 isbn/title/author/publisher 搜索，page/size 分页） | 登录用户 |
| GET | `/api/book/{id}` | 图书详情 | 登录用户 |
| POST | `/api/book/add` | 添加图书（从已付款进货转库存） | 登录用户 |
| PUT | `/api/book/{id}` | 更新图书信息 | 登录用户 |
| DELETE | `/api/book/{id}` | 删除图书 | 登录用户 |
| POST | `/api/book/stock-in` | 新书入库（已付款进货） | 登录用户 |
| POST | `/api/book/sell` | 出售图书（减库存 + 生成收入记录） | 登录用户 |

#### 进货接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/purchase/list` | 进货列表（支持 status 筛选） | 登录用户 |
| GET | `/api/purchase/{id}` | 进货详情 | 登录用户 |
| POST | `/api/purchase/add` | 新增进货 | 登录用户 |
| PUT | `/api/purchase/{id}/pay` | 付款（UNPAID → PAID + 生成支出记录） | 登录用户 |
| PUT | `/api/purchase/{id}/return` | 退货（UNPAID → RETURNED） | 登录用户 |

#### 财务接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/finance/bills` | 查询账单（支持 type/日期范围筛选） | 登录用户 |
| GET | `/api/finance/report` | 财务报表（按年统计，月度收支） | 登录用户 |

#### 仪表盘接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/dashboard/stats` | 首页统计数据（总图书数、今日进货、本月收入、待处理数） | 登录用户 |

---

## 数据库设计

系统使用 PostgreSQL，启动时由 `schema.sql` 自动建表。

### 核心表

**users** — 用户表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | SERIAL | PK | 用户 ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 用户名 |
| password | VARCHAR(32) | NOT NULL | MD5 加密密码 |
| real_name | VARCHAR(50) | | 真实姓名 |
| employee_id | VARCHAR(20) | UNIQUE | 工号 |
| gender | VARCHAR(10) | | 性别 |
| age | INTEGER | | 年龄 |
| role | VARCHAR(20) | NOT NULL, DEFAULT 'NORMAL' | 角色 (SUPER/NORMAL) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**books** — 库存图书表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | SERIAL | PK | 图书 ID |
| isbn | VARCHAR(20) | UNIQUE, NOT NULL | ISBN 号 |
| title | VARCHAR(200) | NOT NULL | 书名 |
| author | VARCHAR(100) | | 作者 |
| publisher | VARCHAR(100) | | 出版社 |
| retail_price | DECIMAL(10,2) | | 零售价格 |
| stock_quantity | INTEGER | DEFAULT 0 | 库存数量 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**purchases** — 进货表（冗余存储图书信息，避免级联影响）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | SERIAL | PK | 进货 ID |
| book_id | INTEGER | REFERENCES books(id) | 关联库存图书 |
| isbn | VARCHAR(20) | NOT NULL | ISBN |
| title | VARCHAR(200) | NOT NULL | 书名 |
| author | VARCHAR(100) | | 作者 |
| publisher | VARCHAR(100) | | 出版社 |
| purchase_price | DECIMAL(10,2) | NOT NULL | 进货价格 |
| quantity | INTEGER | NOT NULL | 进货数量 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'UNPAID' | 状态 (UNPAID/PAID/RETURNED/STOCKED) |
| operator_id | INTEGER | REFERENCES users(id) | 操作人 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | | 更新时间 |

**finances** — 财务表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | SERIAL | PK | 账单 ID |
| type | VARCHAR(20) | NOT NULL | 类型 (INCOME/EXPENSE) |
| amount | DECIMAL(10,2) | NOT NULL | 金额 |
| description | VARCHAR(200) | | 描述 |
| related_id | INTEGER | | 关联 ID（进货 ID 或图书 ID） |
| operator_id | INTEGER | REFERENCES users(id) | 操作人 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

---

## 项目目录结构

```
MyLibrary2.0/
├── library/                        # Spring Boot 后端
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd
│   └── src/
│       ├── main/java/com/example/library/
│       │   ├── LibraryApplication.java       # 启动类
│       │   ├── common/                       # ApiResponse, ErrorCodes, Md5Util
│       │   ├── config/                       # AuthInterceptor, CorsConfig, WebMvcConfig
│       │   ├── controller/                   # Book, User, Purchase, Finance, Dashboard
│       │   ├── entity/                       # Book, User, Purchase, Finance
│       │   ├── exception/                    # BizException, GlobalExceptionHandler
│       │   ├── repository/                   # JdbcTemplate 数据访问层
│       │   └── service/                      # 接口
│       │       └── impl/                     # 实现
│       └── main/resources/
│           ├── application.properties        # 数据库连接配置
│           ├── schema.sql                    # 建表 DDL
│           └── data.sql                      # 种子数据
│
├── frontend/                        # Vue 3 前端
│   ├── package.json
│   ├── vite.config.js               # 开发代理 /api → :8080
│   ├── index.html
│   └── src/
│       ├── main.js                  # 入口，注册 Element Plus + Router
│       ├── App.vue
│       ├── api/                     # Axios 封装 + 各模块 API 函数
│       │   ├── request.js           # 统一拦截器（X-Token 注入、401 跳转）
│       │   ├── book.js / user.js / purchase.js / finance.js / dashboard.js
│       ├── components/
│       │   └── MainLayout.vue       # 侧边栏 + 顶栏 + 主内容区布局
│       ├── composables/
│       │   └── useResponsive.js     # 移动端响应式断点 composable
│       ├── assets/
│       │   └── mobile.css           # 移动端全局样式
│       ├── pages/                   # 页面组件（按模块）
│       │   ├── LoginPage.vue
│       │   ├── IndexPage.vue
│       │   ├── UserManagerPage.vue / UserFormPage.vue
│       │   ├── BookListPage.vue / BookDetailPage.vue / BookEditPage.vue
│       │   ├── PurchaseListPage.vue / PurchaseAddPage.vue / PurchaseDetailPage.vue
│       │   └── FinanceBillsPage.vue / FinanceReportPage.vue
│       └── router/
│           └── index.js             # 路由配置 + 导航守卫
│
└── README.md                        # 本文件
```

---

## 部署说明

### 开发环境

```bash
# 1. 启动 PostgreSQL，创建 MyLibrary 数据库
# 2. 修改 library/src/main/resources/application.properties 中的数据库连接
# 3. 启动后端
cd library && ./mvnw spring-boot:run
# 4. 启动前端（另一个终端）
cd frontend && npm run dev
```

### 生产部署

```bash
# 1. 构建前端
cd frontend && npm run build
# 产物位于 frontend/dist/，可放到 Nginx 或后端 static 目录

# 2. 构建后端
cd library && ./mvnw package -DskipTests
# 产物位于 library/target/library-0.0.1-SNAPSHOT.jar

# 3. 运行
java -jar library/target/library-0.0.1-SNAPSHOT.jar
```

> 前后端分离部署时，后端需确保 `CorsConfig` 允许前端域名；前端需将 API baseURL 指向后端实际地址。

---

## 常见问题与注意事项

- **密码安全**：所有用户密码使用 MD5 加密存储于数据库，明文从不落盘。登录 token 也是 MD5 生成，存储在服务端 `ConcurrentHashMap` 中，重启后失效。
- **角色权限**：前端路由通过 `meta.role: 'SUPER'` 限制用户管理页面仅超级管理员可见；后端 `UserController` 未额外校验角色（依赖前端隐藏 + 用户 ID 来源），如需更严格的后端鉴权可自行补充。
- **状态流转约束**：只有 `UNPAID` 状态的进货可退货（→ `RETURNED`）或付款（→ `PAID`）；只有 `PAID` 状态的进货可入库（→ `STOCKED`）。后端通过 `BizException` 拦截非法操作。
- **财务自动记账**：进货付款时生成 `EXPENSE` 账单；图书出售时生成 `INCOME` 账单，均通过 `@Transactional` 保证数据一致性。
- **图书信息冗余**：进货表 `purchases` 冗余存储了图书的 ISBN、书名、作者、出版社，目的是避免图书信息变更后历史进货记录受影响。
- **跨域处理**：后端 `CorsConfig` 允许所有来源 (`*`)，开发环境额外通过 Vite 代理处理。

---

## 后续扩展计划

- [ ] 数据导出（Excel / CSV）
- [ ] 邮件通知（库存预警、账单汇总）
- [ ] 供应商管理
- [ ] 操作日志审计
- [ ] 更完善的后端角色鉴权（接口级 @RoleRequired 注解）
- [ ] 移动端 PWA 支持
