# 众妙录 MiaoLog

<div align="center">

![众妙录 Logo](app/src/main/ic_launcher-playstore.png)

**一款面向个人物品与资产管理的 Android 应用**

[![Latest Release](https://img.shields.io/badge/release-v1.0.2-blue.svg)](https://github.com/LIYEQINGMING/MiaoLog/releases)
[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com)
[![Min API](https://img.shields.io/badge/API-24%2B-orange.svg)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/license-MIT-red.svg)](LICENSE)

[📥 下载 APK](https://github.com/LIYEQINGMING/MiaoLog/releases) · [📖 设计文档](docs/) · [🐛 反馈问题](https://github.com/LIYEQINGMING/MiaoLog/issues)

</div>

---

## 应用简介

**众妙录（MiaoLog）** 是一款围绕“物品”展开的个人记录与资产管理应用。

它不再把产品定位成“仓库”或“库存”系统，而是把现实物品、收藏、消耗品、耐用品、订阅型支出与价格记录，统一纳入一套更灵活的物品体系里。你可以用它记录自己拥有什么、东西放在哪里、价值如何变化、哪些项目即将到期，以及如何更高效地管理这些信息。

当前产品设计以以下方向为核心：

- `首页`：总览、提醒、快捷进入和推荐物品流
- `统计`：个人物品数据分析中心
- `我的`：全局管理中心与应用设置中心
- `新增/编辑`：基于模板、系统属性和自定义属性的统一录入体验
- `分类 / 标签 / 属性管理`：物品体系治理能力统一收口

---

## 核心能力

### 物品记录

- 支持记录名称、分类、标签、数量、位置、备注、品牌、规格等基础信息
- 支持多图上传与图片查看
- 支持地点与地图位置记录
- 支持购买日期、保质期、保修期、价格日期等时间信息

### 价格属性体系

- 支持 `购入价格`、`总价` 等内置价格字段
- 支持类型化价格属性块，逐步统一价格记录方式
- 支持价格动作配置，例如是否计入总价值
- 支持订阅型价格信息、自动续费、周期规则等扩展能力

### 分类、标签、属性治理

- 支持多层级分类体系
- 支持多标签和标签分组
- 支持系统属性与自定义属性并存
- 后续统一通过“我的”页面中的管理入口治理整个物品体系

### 首页与统计

- 首页以资产总览、高优先事项、快捷入口和物品流为核心
- 统计页以总价值、日均价值、分类分布、标签分布、趋势分析为核心
- 首页负责摘要，统计页负责深度分析

### 模板与高效录入

- 支持模板创建与模板驱动的新增流程
- 模板可预设字段组合、默认分类、默认标签
- 适合数码、收藏、日用品、消耗品等不同场景快速录入

### 提醒与回顾

- 支持到期提醒、时间相关事件展示
- 支持在日历和统计视角回顾物品数据
- 支持回收站恢复误删内容

---

## 当前页面结构

根据当前设计文档，产品的主要页面结构如下：

```text
首页
├── 资产总览
├── 高优先事项
├── 快捷入口
└── 推荐物品流

统计
├── 核心指标
├── 结构分析
├── 趋势分析
└── 深入分析

我的
├── 我的物品体系
│   ├── 分类管理
│   ├── 标签管理
│   └── 属性管理
├── 数据与安全
├── 应用偏好
└── 支持与关于
```

更详细的产品设计可参考：

- [Product_Design_Document.md](docs/Product_Design_Document.md)
- [Home_Page_Design.md](docs/Home_Page_Design.md)
- [Statistics_Page_Design.md](docs/Statistics_Page_Design.md)
- [My_Page_Design.md](docs/My_Page_Design.md)
- [Category_Management_Design.md](docs/Category_Management_Design.md)
- [Tag_Management_Design.md](docs/Tag_Management_Design.md)
- [Attribute_Management_Design.md](docs/Attribute_Management_Design.md)

---

## 功能概览

### 首页

- 资产总览：展示总价值、日均价值、物品数量、分类数量、标签数量
- 高优先事项：承接即将到期、已过期、低数量提醒、购物清单等内容
- 快捷入口：快速进入添加物品、日历、搜索、统计等高频功能
- 物品流：支持智能推荐、轻量分类筛选、排序与视图切换

### 统计

- 核心指标：总价值、日均价值、物品数量、分类数量、标签数量等
- 结构分析：分类占比、标签分布、存放位置分布、价值贡献排行
- 趋势分析：月度录入趋势、月度价值变化
- 后续支持图表钻取到具体物品列表

### 我的

- 个人信息与使用摘要
- 分类管理、标签管理、属性管理
- 数据导出、回收站、应用设置
- 关于应用、版本信息与支持入口

### 录入与编辑

- 新增页与修改页采用统一的物品表单思路
- 支持系统字段、自定义属性、价格属性块
- 支持模板带入默认字段
- 支持图片、位置、日期、价格、备注等复合录入

---

## 设计原则

### 产品术语

当前产品统一使用以下术语：

- `物品`
- `所有物品`
- `分类`
- `标签`
- `属性`
- `统计`

不再将以下旧命名作为产品层正式语义：

- `库存`
- `仓库`
- `库存分析`

需要注意：

- 代码里仍可能保留 `Inventory`、`Warehouse` 等历史技术命名
- 这些属于技术债或兼容层，不代表当前产品概念

### UI 方向

当前 UI 设计方向为：

- 轻氛围背景
- 稳定主容器
- 局部轻玻璃点缀
- 信息结构优先于装饰表现

首页、统计页和“我的”页都已在 `docs/` 中补充了结构稿与页面设计文档。

---

## 技术栈

### 开发语言与平台

- **Kotlin**
- **Android SDK**，`minSdk 24`，`targetSdk 34`
- **Java 17**

### 核心架构

- **MVVM**
- **Room**
- **Navigation**
- **ViewBinding / DataBinding**
- **Jetpack Compose**
- **KSP**

### 主要依赖

- **Material 3**
- **Coil Compose**
- **Glide**
- **Ktor**
- **kotlinx.serialization**
- **AAInfographics / AAChartCore-Kotlin**
- **高德地图 SDK**
- **SmartRefreshLayout**
- **Flexbox**
- **PhotoView**

---

## 项目结构

```text
ItemManagement/
├── app/                          # 主应用模块
│   ├── src/main/
│   │   ├── java/com/example/itemmanagement/
│   │   │   ├── data/             # 数据层
│   │   │   ├── ui/               # UI 层
│   │   │   ├── utils/            # 工具与辅助逻辑
│   │   │   └── MainActivity.kt
│   │   └── res/                  # 资源文件
│   ├── schemas/                  # Room schema
│   └── build.gradle.kts
├── docs/                         # 产品、页面、数据库与 UI 设计文档
├── libs/AAChartCore-Kotlin-7.4.0 # 本地图表库依赖
├── version.json                  # 自动更新配置
├── build.gradle.kts              # 根构建配置
├── settings.gradle.kts
└── README.md
```

---

## 快速开始

### 方式一：直接下载 APK

1. 打开 [Releases 页面](https://github.com/LIYEQINGMING/MiaoLog/releases)
2. 下载最新版本 APK
3. 安装到 Android 设备

### 方式二：从源码运行

```bash
git clone https://github.com/LIYEQINGMING/MiaoLog.git
cd MiaoLog
./gradlew assembleDebug
```

Debug APK 默认输出路径：

```text
app/build/outputs/apk/debug/
```

### 环境要求

- Android Studio Koala 或更高版本
- Android SDK 24-34
- JDK 17

### 本地配置

- `local.properties` 用于本机 SDK 路径配置，不应提交到版本控制
- 地图功能需要配置高德地图 API Key
- Release 构建需要本地签名配置

---

## 数据库与演进

- 当前 Room 数据库版本：`57`
- Schema 文件位于 `app/schemas/`
- 数据结构正在向“统一物品架构 + 类型化属性 + 管理页治理能力”持续演进

相关文档可参考：

- [Database_Design_Document.md](docs/Database_Design_Document.md)
- [Database_Evolution_Design.md](docs/Database_Evolution_Design.md)
- [Built_In_Item_Attributes.md](docs/Built_In_Item_Attributes.md)
- [Price_Attribute_Design.md](docs/Price_Attribute_Design.md)

---

## 开发说明

### 代码规范

- 遵循 Kotlin 官方编码规范
- 产品文案统一使用“物品”体系术语
- 新功能优先补设计文档，再推进实现
- 页面改动优先与 `docs/` 中现有设计保持一致

### 提交建议

- Bug 修复：描述清楚复现路径和修复范围
- 页面重构：同步更新相关设计文档
- 数据结构调整：同步更新 schema、迁移和设计说明

---

## 应用截图

### 首页
![首页](screenshots/home.png)

### 物品详情
![物品详情](screenshots/detail.png)

### 地图选点
![地图选点](screenshots/map.png)

### 购物清单
![购物清单](screenshots/shopping.png)

### 统计
![统计](screenshots/analysis.png)

---

## 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。

---

## 联系方式

- **作者**：Ash Lee
- **项目地址**：[ItemManagement](https://github.com/LIYEQINGMING/MiaoLog)
- **问题反馈**：[Issues](https://github.com/LIYEQINGMING/MiaoLog/issues)

---

## 致谢

感谢以下开源项目和服务：

- [Android Jetpack](https://developer.android.com/jetpack)
- [Material Design 3](https://m3.material.io/)
- [高德地图](https://lbs.amap.com/)
- [Glide](https://github.com/bumptech/glide)
- [AAChartCore-Kotlin](https://github.com/AAChartModel/AAChartCore-Kotlin)
- [GitHub](https://github.com/)

---

<div align="center">

**众妙之门，记录万象。**


</div>
