# UI/UX 设计演进方案：从 Material 3 到 Liquid Glass（液态玻璃）

## 1. 现状分析
当前 APP 基于 **Material Design 3 (M3)**，具有典型的 M3 特征：
- 纯色大面积色块。
- 标准的卡片阴影（Elevation）和圆角。
- 扁平化风格为主，缺乏视觉层次的通透感。

## 2. 目标设计语言：Liquid Glass（液态玻璃 / Glassmorphism）
为了达到类似于“极简记物”、“有数APP”或 Apple 现代设计中的视觉效果，我们将对整体风格进行全面重构。
- **核心理念**：通透、轻盈、色彩流转、扁平化基础上的层级感。
- **关键视觉元素**：
  1. **背景（Background）**：不再使用纯白或纯黑，而是带有微妙渐变色的背景，甚至带有几何形状的光晕（Gradient Meshes）。
  2. **卡片（Cards）**：使用带有毛玻璃（Blur）效果的半透明背景。使用 `RenderEffect.createBlurEffect` (Android 12+) 配合低透明度白色 `#40FFFFFF` 和极其纤细的纯白边框（`1dp`，`#80FFFFFF`）。
  3. **层级（Hierarchy）**：不依赖阴影（Shadow）来区分层级，而是依靠透明度的叠加和背景的透视（透视模糊）来区分。
  4. **文字与图标（Typography & Icons）**：高对比度的文字，部分核心数据使用粗体（Bold/Black）或圆润的字体（Rounded Fonts），图标使用高亮纯色（如霓虹蓝、珊瑚粉）作为点缀。

## 3. 具体改造步骤

### 3.1 颜色系统重构 (colors.xml)
- **主题背景**：使用渐变层（如 `bg_liquid_main.xml`）替换现有的 `?attr/colorSurface` 或 `?attr/backgroundColor`。
- **表面透明度**：
  - `glass_surface_heavy`: `#B3FFFFFF` (70%白，适用于表层操作面板)
  - `glass_surface_medium`: `#80FFFFFF` (50%白，适用于卡片主体)
  - `glass_surface_light`: `#40FFFFFF` (25%白，适用于列表项)
- *(夜间模式对应黑色半透明)*

### 3.2 核心组件改造
#### A. 底部导航栏 (Bottom Navigation)
- 去除背景色，改为高斯模糊（Blur）背景。
- 浮动式设计：与屏幕边缘留出 `16dp` 的间距，做成类似灵动岛的胶囊形状。

#### B. 数据卡片 (Data Cards - 首页统计、物品详情)
- 放弃 `MaterialCardView` 的 `app:cardElevation`。
- 自定义 `GlassCardView` 或直接在 XML 中使用带 Stroke 和半透明 Solid 的 Shape Drawable。
- 对其下方的视图应用模糊滤镜。

#### C. 列表项 (RecyclerView Items)
- 列表滑动时，物品项在渐变背景上滑动，半透明的背景能透出底层的色彩。
- 图片（Image）统一使用圆角（16dp - 24dp），并可能带有微弱的外发光。

### 3.3 交互与动画
- **弹簧动画（Spring Animation）**：按钮点击、卡片展开采用阻尼动画，类似 iOS 交互体验。
- **沉浸式体验**：强制全屏（Edge-to-Edge），状态栏和导航栏完全透明，内容直接绘制在系统栏下方。

## 4. 实施计划 (TODOs)
1. **基础样式搭建**：创建 `Glassmorphism` 相关的 XML Drawables (半透明背景、渐变背景)。
2. **主题替换**：在 `themes.xml` 中移除默认背景，设置为透明，并开启 Window Edge-to-Edge。
3. **关键页面重构**：优先重构 `HomeFragment` (首页仪表盘) 和 `ItemDetailFragment` (物品详情页)。
4. **自定义 View**：针对 Android 12+ 实现原生的 Blur 效果，针对低版本实现 fallback（半透明纯色）。