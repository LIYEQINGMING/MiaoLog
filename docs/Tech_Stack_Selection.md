# 技术选型文档 (Technology Stack Selection)

## 1. 愿景与目标
为打造一款兼具现代化 UI（Liquid Glass）和极致性能的个人物品资产管理应用，并且为了未来能够顺畅地将服务从单一的移动端扩展至**桌面端（Windows/macOS）**和**Web端**，我们需要在技术栈上做出具备前瞻性的选择。

## 2. 跨端框架选型最佳实践：Kotlin Multiplatform (KMP) + Compose Multiplatform
考虑到当前项目已经是一个纯 Kotlin 的 Android 工程，我们采用 **KMP (Kotlin Multiplatform)** 结合 **Compose Multiplatform** 作为最终的跨端解决方案，这是目前 Android 生态向全平台演进的最佳实践。

### 2.1 为什么选择 KMP + Compose？
1. **语言一致性**：整个前端（iOS/Android/Desktop/Web）均使用 Kotlin，与现有代码无缝衔接，降低学习与重写成本。
2. **逻辑复用（KMP）**：可以将数据库操作、网络请求、业务逻辑（如资产平均价值计算、订阅周期推算等）抽象到 `shared` 模块中，实现核心业务逻辑的 100% 复用。
3. **UI 复用（Compose Multiplatform）**：基于声明式的 UI 框架，不仅在 Android 上表现优异，如今已完全支持 Desktop（JVM）和 iOS，Web 端也正在迅速成熟。我们可以用一套 Compose 代码绘制出复杂的 Liquid Glass 模糊效果和弹簧动画。
4. **原生性能**：相较于 Flutter 或 React Native，KMP 允许在需要时随时退回到平台原生代码（如使用原生 iOS 的 Metal 渲染特效，或 Windows 的底层 API），不牺牲任何性能。

## 3. 核心技术栈明细

### 3.1 表现层 (UI/UX Layer)
- **框架**：Jetpack Compose (Android) -> 逐步过渡到 **Compose Multiplatform**。
- **UI 状态管理**：`StateFlow` & `ViewModel` (KMP 支持的多端 ViewModel)。
- **动画库**：Compose 内置的 `SpringSpec` (弹簧动画) 及 `AnimatedVisibility`。
- **图像加载**：**Kamel** (专为 KMP 打造的图片加载库，支持缓存和网络图片)，逐步替换现有的 Glide。

### 3.2 逻辑与数据层 (Shared Business Logic)
- **依赖注入**：**Koin** (支持 KMP 环境的轻量级 DI 框架)。
- **响应式编程**：**Kotlin Coroutines** & **Flow** (全平台通用的异步处理标准)。

### 3.3 数据持久层 (Local Storage)
- **关系型数据库**：**Room** (Google 最近已正式支持 Room 的 KMP 版本，这是目前最平滑的过渡方案)。或者备选方案 **SQLDelight** (KMP 社区老牌 SQL 框架)。
- **键值对缓存（KV）**：**Multiplatform Settings** (替代现有的 SharedPreferences，用于存储用户偏好、单位设置等)。

### 3.4 网络与云端同步 (Networking & Cloud)
- **网络请求**：**Ktor** (纯 Kotlin 编写的多端网络客户端，用于后续的汇率抓取、云端同步接口)。
- **JSON 序列化**：**kotlinx.serialization** (KMP 官方支持的序列化库，替代现有的 Gson)。

## 4. 各端适配策略
- **Android**：作为主阵地，优先落地所有新特性和 UI 规范。
- **Desktop (Windows/macOS)**：通过 Compose for Desktop 打包，充分利用大屏空间，将底部导航栏转化为左侧侧边栏（Navigation Rail），瀑布流卡片显示更多列数。
- **iOS**：通过 Compose for iOS 复用 UI，底层数据使用 KMP 共享。对于极端追求性能的毛玻璃效果，可编写 `expect/actual` 函数调用 iOS 原生的 `UIVisualEffectView`。
- **Web**：采用 Compose for Web (Wasm) 打包，作为轻量级的展厅分享页（Gallery Share）或轻量管理后台。