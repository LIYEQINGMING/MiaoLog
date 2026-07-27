# 技术演进路线图 (Tech Evolution Plan)

## 1. 背景与目标
当前项目是一个基于 **XML 布局**、**Android 原生组件（Fragment/ViewModel）**、**Room** 和 **Gson** 的传统单体 Android 应用。
为了达成我们在《技术选型文档》中制定的目标——**全平台跨端（KMP + Compose Multiplatform）** 和 **现代 UI（Liquid Glass）**，我们需要进行循序渐进的重构，保证在演进过程中应用始终可用。

## 2. 演进原则
- **小步快跑，逐步替换**：不要一次性推翻现有代码，采用“绞杀者模式（Strangler Fig Pattern）”，新页面用新技术，老页面逐步迁移。
- **自底向上剥离逻辑**：先将与 Android 平台无关的业务逻辑剥离出来，再进行 UI 层的跨端改造。
- **数据安全第一**：数据库的迁移必须极其谨慎，确保本地数据不丢失。

## 3. 阶段实施计划

### 阶段一：现代化 UI 与基础设施重塑 (Android 内部演进)
*目标：在 Android 原生工程内落地 Liquid Glass 设计语言，引入 Compose。*

1. **引入 Jetpack Compose**
   - 在 `build.gradle` 中开启 Compose 支持。
   - 保留现有的 Activity 和 Navigation Graph。
2. **混合开发模式（Interop）**
   - 现有的 XML Fragment 内部通过 `ComposeView` 嵌入新的 UI 组件。
   - 优先使用 Compose 重写“首页统计卡片”、“物品详情信息流”等视觉核心组件，利用 Compose 轻松实现磨砂玻璃（Blur）和动画效果。
3. **依赖清理与升级**
   - 将 `Gson` 替换为 `kotlinx.serialization`。
   - 将 `SharedPreferences` 替换为 Jetpack DataStore 或 Multiplatform Settings。
   - 按照《Database Design Document》的要求，编写 Room Migration 升级现有的数据库表结构。

### 阶段二：逻辑剥离与 KMP 模块化 (准备跨端)
*目标：将工程改造为标准的 Kotlin Multiplatform 项目结构，分离 UI 与业务逻辑。*

1. **重构项目结构**
   - 建立 `shared` (KMP) 模块和 `androidApp` 模块。
   - 将 `androidApp` 中现有的数据库实体（Entities）、DAO 接口、Repository、网络请求模型移动到 `shared` 模块的 `commonMain` 中。
2. **替换平台强相关依赖**
   - 数据库：升级至 **Room KMP** 版本，调整底层配置使其支持多端。
   - 逻辑：确保所有的时间处理、文件读写不再依赖 `java.*` 或 `android.*` API，改用 Kotlin 标准库或 `kotlinx-datetime`。
3. **架构分层优化**
   - 在 `shared` 模块中实现跨端的 `ViewModel` 或 MVI 架构（如使用 Decompose），确保所有的业务状态（State）都在共享模块中生成。

### 阶段三：全面拥抱 Compose Multiplatform (UI 跨端)
*目标：废弃 XML，实现 Android 和 Desktop 的 UI 共享。*

1. **移除 XML 碎片**
   - 彻底干掉 `androidApp` 中的 XML 文件，将 Navigation Component 替换为支持 Compose 的路由框架（如 Voyager）。
2. **引入 Compose Multiplatform**
   - 将 UI 代码从 `androidApp` 迁移到 `shared` 模块的 `commonMain` 中。
3. **搭建 Desktop (JVM) 平台**
   - 创建 `desktopApp` 模块，依赖 `shared` 模块。
   - 编写针对宽屏的自适应布局（Adaptive Layout）：当屏幕宽度变大时，自动将底部导航栏切换为侧边栏，将瀑布流列数从 2 列扩展到 4-5 列。
   - 打包发布 Windows/macOS 客户端，验证跨端可行性。

### 阶段四：拓展生态 (iOS 与 Web)
*目标：实现真正意义上的全平台覆盖。*

1. **iOS 适配**
   - 创建 iOS 工程，链接 `shared` 模块生成的 framework。
   - 对于 Liquid Glass 效果，如果 Compose iOS 性能不达标，通过 `expect/actual` 机制调用 iOS 原生的 `UIVisualEffectView` 以保证丝滑体验。
2. **Web 适配**
   - 创建 `wasmJs` 或 `js` target。
   - 实现轻量级的“Web 展厅（Gallery）”页面，方便用户通过链接分享自己的物品合集给他人查看。

## 4. 总结
整个演进过程从 **UI 渐进式翻新 (Compose in XML)** 开始，过渡到 **核心逻辑下沉 (KMP Shared Module)**，最终实现 **全平台 UI 统一 (Compose Multiplatform)**。这一路线图不仅降低了重构风险，还为未来产品的生态扩张打下了坚实基础。