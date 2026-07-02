# 校园外卖跑腿平台 (Campus Errand Running Platform)

一个基于 Spring Boot + Vue 3 + Uni-app 的多端校园跑腿/外卖配送综合服务平台，包含用户端小程序、骑手端小程序和管理端 Web 后台。

## 项目简介

本项目是一个完整的 O2O 校园跑腿与外卖配送系统，支持用户下单、商家接单、骑手抢单配送、平台管理等功能。系统包含四个核心模块：后端服务、用户端（微信小程序）、骑手端（微信小程序）和管理端（Web 后台）。

### 核心业务功能

- **用户端**：商品浏览、分类搜索、购物车、下单支付、收货地址管理、售后申请、跑腿服务
- **骑手端**：订单接单、配送导航、收益中心、位置实时上报
- **管理端**：数据仪表盘、用户/商家/骑手管理、订单管理、财务管理（发票/支付/结算）
- **平台能力**：商品 SKU 管理、优惠券、竞价策略、运费模板、OCR 识别、JWT 认证

## 目录结构

```
School/
├── 后端/                     # Spring Boot 后端服务
│   ├── src/main/java/com/project/platform/
│   │   ├── config/           # 配置类（CORS、JWT、拦截器、全局异常）
│   │   ├── controller/       # 22 个 REST 控制器
│   │   ├── dto/              # 数据传输对象
│   │   ├── entity/           # 数据库实体（26 张表）
│   │   ├── exception/        # 自定义异常与全局处理
│   │   ├── mapper/           # MyBatis Mapper 接口
│   │   ├── service/          # 服务接口与实现
│   │   ├── utils/            # 工具类
│   │   └── vo/               # 视图对象
│   ├── src/main/resources/
│   │   ├── mapper/           # MyBatis XML 映射文件（24 个）
│   │   ├── tessdata/         # Tesseract OCR 语言包（中/英）
│   │   └── application.yaml  # 主配置文件
│   └── pom.xml               # Maven 依赖配置
│
├── 用户端/                   # Uni-app 用户端小程序
│   ├── src/
│   │   ├── pages/            # 首页、分类、购物车、商品、搜索、登录
│   │   ├── pagesMember/      # 地址管理、个人资料、设置
│   │   ├── pagesOrder/       # 下单、订单列表、订单详情、支付
│   │   ├── services/         # API 服务层
│   │   ├── stores/           # Pinia 状态管理
│   │   ├── components/       # 轮播图、SKU 弹窗、数量选择器
│   │   └── types/            # TypeScript 类型定义
│   └── package.json
│
├── 骑手端/                   # Uni-app 骑手端小程序
│   ├── src/
│   │   ├── pages/            # 首页、订单列表、个人中心
│   │   ├── pages/pagesRider/ # 骑手资料、订单详情、完成配送、收益中心
│   │   ├── services/         # 骑手 API 服务
│   │   ├── stores/           # Pinia 状态管理
│   │   └── utils/            # HTTP 封装、腾讯地图 SDK
│   └── package.json
│
├── 管理端/                   # Vue 3 管理后台
│   ├── src/
│   │   ├── views/            # 仪表盘、数据统计、订单、商户、骑手、财务、用户
│   │   ├── router/           # Vue Router 路由
│   │   ├── utils/            # Axios 封装、认证
│   │   └── components/       # 上传组件
│   └── package.json
│
└── 数据库/
    └── School.sql            # MySQL 完整建库建表脚本
```

## 技术栈

| 模块 | 技术 | 版本 |
|------|------|------|
| **后端** | Java | 17 |
| | Spring Boot | 3.2.10 |
| | MyBatis + MyBatis-Spring-Boot-Starter | 3.0.4 |
| | MySQL Connector | 8.0.31 |
| | JWT (jjwt) | 0.11.5 |
| | PageHelper | 1.4.6 |
| | FastJSON | 2.0.53 |
| | Hutool | 5.7.20 |
| | Tess4j (OCR) | 5.4.0 |
| | Lombok | latest |
| **用户端** | Uni-app (Vue 3) | 3.0 |
| | TypeScript | 5.1 |
| | Pinia | 2.0.27 |
| | Vite | 5.2 |
| | Sass | 1.56 |
| **骑手端** | Uni-app (Vue 3) | 3.0 |
| | TypeScript | 5.1 |
| | Pinia | 2.0.27 |
| | 腾讯地图 SDK | - |
| **管理端** | Vue 3 | 3.5 |
| | Element Plus | 2.13 |
| | Vite | 8.0 |
| | Vue Router | 5.0 |
| | Axios | 1.13 |
| **数据库** | MySQL | 8.0+ |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Node.js 20.19+ / 22.12+
- pnpm（推荐）或 npm
- 微信开发者工具（用于小程序调试）

### 1. 数据库初始化

```bash
# 创建数据库并导入表结构
mysql -u root -p < 数据库/School.sql
```

默认数据库名：`run_errands`，可在后端 `application.yaml` 中修改连接信息。

### 2. 启动后端服务

```bash
cd 后端
mvn spring-boot:run
```

后端默认运行在 `http://localhost:1000`。

配置文件位于 `后端/src/main/resources/application.yaml`，可按需修改数据库连接、端口等参数。

### 3. 启动管理端（Web 后台）

```bash
cd 管理端
pnpm install
pnpm dev
```

管理端默认运行在 `http://localhost:5173`。

### 4. 启动小程序端（用户端 / 骑手端）

```bash
# 用户端
cd 用户端
pnpm install
pnpm dev:mp-weixin

# 骑手端
cd 骑手端
pnpm install
pnpm dev:mp-weixin
```

将编译产物 `dist/dev/mp-weixin` 导入微信开发者工具即可预览。

## 数据库表概览

| 表名 | 说明 |
|------|------|
| `user` | 用户表 |
| `admin` | 管理员表 |
| `merchant` | 商家表 |
| `rider` | 骑手表 |
| `address` | 收货地址 |
| `goods` / `goods_sku` / `goods_spec` / `goods_spec_item` | 商品、SKU、规格 |
| `category` | 商品分类 |
| `cart` | 购物车 |
| `order_master` / `order_item` | 订单主表 / 明细 |
| `after_sale` | 售后服务（退款/投诉/发票） |
| `comment` | 商品评价 |
| `coupon` | 优惠券 |
| `bid_strategy` | 竞价策略 |
| `errand` | 跑腿订单 |
| `freight_template` | 运费模板 |
| `peak_price` | 峰值定价 |
| `platform_commission_config` | 平台抽成配置 |
| `rider_balance_flow` | 骑手流水 |
| `rider_settlement` | 骑手结算 |
| `invoice_apply` | 发票申请 |

## 项目特点

- **多端支持**：用户端与骑手端基于 Uni-app 开发，一套代码可编译到微信小程序、App、H5 等多平台
- **完整业务流程**：覆盖下单 → 支付 → 接单 → 配送 → 完成 → 结算的全链路
- **OCR 识别**：集成 Tesseract OCR，支持身份证等图像文字识别
- **骑手位置追踪**：基于腾讯地图 SDK 实现骑手实时位置上报
- **竞价策略**：支持商家的灵活配送竞价配置
- **JWT 认证**：统一的无状态认证方案，前后端分离架构
```
