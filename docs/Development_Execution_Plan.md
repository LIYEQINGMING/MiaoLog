# 个人物品与资产管理 APP - 开发执行计划 (Development Execution Plan)

## 概述
本文档基于产品设计、UI/UX 设计、数据库架构及技术演进路线图，将整个重构与新功能开发过程拆解为**可独立执行、可验证的原子化步骤**。我们将严格按照此计划逐步推进代码开发。

---

## 阶段一：底层数据基建 (Database & Core Entities)
*目标：不改变现有 UI 的前提下，完成所有底层表结构的升级、数据迁移和 DAO 层建设，为后续高级业务铺平道路。*

- [ ] **Step 1.1: 升级 UnifiedItemEntity**
  - 新增统计控制开关（`excludeFromTotalValue`, `excludeFromTotalCount`）。
  - 新增订阅制属性（`isSubscription`, `autoRenew`, `subscriptionCycle`）。
  - 新增 `currencyCode` 和 `templateId` 关联。
  - 编写 Room Migration 脚本 (V52 -> V53)，保证现有数据不丢失。
  - 编写并运行单元测试验证数据迁移。

- [ ] **Step 1.2: 建立状态定义体系 (Status Definition)**
  - 创建 `ItemStatusDefinitionEntity` 表及对应的 DAO。
  - 创建预置数据填充脚本（服役中、未购买、已退役、已过期、待补款）。
  - 修改现有的 `ItemStateEntity`，将其原本基于 Enum 的状态字段迁移为关联 `statusId`。
  - 编写 Room Migration 脚本 (V53 -> V54)。

- [ ] **Step 1.3: 高级自定义属性系统 (Advanced Custom Attributes)**
  - 创建 `CustomAttributeDefinitionEntity` 及其 DAO（定义强类型的 PRICE, DATE, TEXT 属性）。
  - 创建 `ItemCustomAttributeEntity` 及其 DAO（引入 `includeInTotal` 特有配置）。
  - 编写数据迁移脚本，将旧的简单键值对 `field_custom_values` 数据迁移至新的双表结构。
  - 编写 Room Migration 脚本 (V54 -> V55)。

- [ ] **Step 1.4: 完善 Gallery、Template 与 Tag Group 实体**
  - 创建 `GalleryEntity` 与多对多关系表 `GalleryItemCrossRef`。
  - 完善 `ItemTemplateEntity` 表结构及 DAO。
  - 创建 `TagGroupEntity` 并修改 `TagEntity` 关联。
  - 编写 Room Migration 脚本 (V55 -> V56)。
  - 更新 `UnifiedItemDao` 中的联合查询，支持上述新增结构的高速检索。

---

## 阶段二：UI/UX 基建与 Compose 引入 (UI Foundation)
*目标：搭建 Liquid Glass 设计语言的底层代码规范，引入 Jetpack Compose 支持。*

- [ ] **Step 2.1: 引入 Compose 与依赖升级**
  - 在 `build.gradle` 开启 Compose 支持，添加相关依赖。
  - 引入 `kotlinx.serialization` 替代 Gson。
  - 清理无用依赖，确保编译通过。

- [ ] **Step 2.2: 构建 Liquid Glass 设计系统 (Compose Theme)**
  - 编写 `LiquidGlassTheme.kt`，定义全局 Color Palette（柔和渐变色系）。
  - 定义 Typography（粗体主标题、圆润数字字体）。
  - 实现自定义的 `GlassCard` Composable 组件：
    - 封装 `RenderEffect.createBlurEffect`（Android 12+）及向下兼容方案。
    - 封装 1dp 高光边框（`#80FFFFFF`）和内阴影逻辑。
    
- [ ] **Step 2.3: 基础交互动画封装**
  - 封装通用的 `Modifier.springClick()` 扩展，实现按下缩小至 95%、抬起回弹的阻尼交互。
  - 开启全应用 Edge-to-Edge，处理沉浸式状态栏与透明导航栏的 WindowInsets 兼容。

---

## 阶段三：核心业务逻辑下沉 (Business Logic & Repository)
*目标：在 Repository 层封装复杂的聚合逻辑，为前端 ViewModel 提供干净的数据流。*

- [ ] **Step 3.1: 资产统计逻辑重构**
  - 在 Repository 层实现全新的资产统计流（Flow）。
  - 整合 `UnifiedItemEntity` 的 `purchasePrice` 与 `PRICE` 类自定义属性（如定金），并应用 `excludeFromTotalValue` 与 `includeInTotal` 开关逻辑。
  
- [ ] **Step 3.2: 汇率与多币种计算逻辑**
  - 引入 `Ktor` 网络库。
  - 实现一个 `CurrencyConverter` 工具类，支持联网获取汇率并缓存本地。
  - 在资产统计 Flow 中无缝嵌入多币种换算。

- [ ] **Step 3.3: 订阅与到期推算逻辑**
  - 实现基于 `purchaseDate`、`subscriptionCycle` 的下一次扣费日计算引擎。
  - 挂载日历视图的数据源装配器。

---

## 阶段四：核心界面重构 (Screens Rebuild via Compose)
*目标：将旧版 XML 页面逐个替换为基于 Compose 的 Liquid Glass 界面。*

- [ ] **Step 4.1: 底部导航栏与主框架改造**
  - 将主 Activity 的底部导航栏重构为悬浮胶囊形状的 Compose 组件。
  - 设置带光晕的全局动态渐变背景。

- [ ] **Step 4.2: 首页仪表盘 (Home Dashboard)**
  - 使用 Compose 重写首页：顶部的 GlassCard 总资产卡片。
  - 增加横向滚动的快捷操作 Pill Buttons。
  - 将原有的 RecyclerView 替换为 Compose `LazyVerticalStaggeredGrid`，实现带有轻度磨砂效果的物品瀑布流。

- [ ] **Step 4.3: 物品详情页 (Item Detail)**
  - 重写详情页：实现顶部大图沉浸式渐变消失效果。
  - 构建悬浮的“重度磨砂”核心信息卡。
  - 构建类似控制中心的 2x2 或 3x3 “属性网格 (Attributes Grid)”，展示高级自定义属性。

- [ ] **Step 4.4: 物品录入与模板选择流程**
  - 开发模板选择的底部弹窗（BottomSheet）。
  - 改造 AddItem 流程，支持根据 `ItemTemplateEntity` 动态渲染高级属性输入表单。

---

## 阶段五：高级功能拓展 (Advanced Features)
*目标：开发基于新底层架构衍生出的扩展功能。*

- [ ] **Step 5.1: 发现与日历页 (Discovery & Calendar)**
  - 实现基于时间维度的日历视图，标注录入/补款/到期等事件点。
  - 日期点击后的毛玻璃卡片弹窗交互。

- [ ] **Step 5.2: 展厅管理 (Gallery)**
  - 开发 Gallery 合集的创建与管理页。
  - 开发横向大图画廊展示模式。

- [ ] **Step 5.3: 数据图表 (Statistics Charts)**
  - 引入现代图表库或使用 Compose Canvas 绘制带有光影效果的折线图与分类占比环。
  - 接入多维交叉筛选组件。

---

## 后续规划 (KMP & 多端)
当阶段一至阶段五在 Android 端完全跑通且稳定后，将正式启动**技术演进路线图**中的“KMP 模块化”与“Desktop/Web 适配”步骤。

---

## 阶段六：旧库存语义清理 (Legacy Inventory Semantics Cleanup)
*目标：将所有用户可见层中残留的“库存/缺货/补货/仓库”语义彻底移除，使产品表达重新对齐“个人物品与资产管理”。*

- [ ] **Step 6.1: 用户可见文案与入口统一替换**
  - 全量排查底部导航、首页、详情页、筛选页、提醒页中的“仓库 / 库存 / 缺货 / 补货”等旧文案。
  - 将用户可见层统一替换为“物品 / 资产 / 检索 / 未购买 / 待补款 / 状态”等符合新产品语义的表达。
  - 清理旧图标、旧说明文案与旧空状态提示，避免继续传达仓储软件心智。

- [ ] **Step 6.2: 旧交互动作退场**
  - 下线或隐藏“低库存提醒”“补货建议”“转入库存”等旧业务动作入口。
  - 重新定义与产品文档一致的动作集合，如“标记未购买”“标记服役中”“标记已退役”“加入愿望单”“加入展厅”等。
  - 对需要保留的数据迁移逻辑添加兼容层，确保旧数据不因入口移除而丢失。

- [ ] **Step 6.3: 用户可见页面职责重构**
  - 重新检查首页、检索页、统计页、日历页的信息结构，移除面向库存管理的页面职责。
  - 将首页聚焦于资产总览、动态提醒、物品流；将检索页聚焦于元数据搜索与筛选。
  - 为后续“愿望单 / 展厅 / 订阅”模块预留新的入口位置与信息层级。

---

## 阶段七：愿望单与状态体系重构 (Wishlist & Status Reframing)
*目标：将旧“购物清单/补货清单”逻辑升级为围绕“未购买、待补款、愿望单”的新状态与新模块体系。*

- [ ] **Step 7.1: 购物清单语义迁移为愿望单**
  - 将当前 `ShoppingList` 相关用户可见模块重新定义为“愿望单 / 想买 / 待购”体系。
  - 区分“未购买状态”与“愿望单模块”：未购买是底层状态，愿望单是组织和管理方式。
  - 调整列表标题、空态文案、创建流程与导航结构，避免继续使用“购物清单管理”的旧产品概念。

- [ ] **Step 7.2: 待补款与预定场景建模**
  - 将“待补款”从旧补货语义中独立出来，作为预定类物品的正式状态进行建模。
  - 衔接价格类自定义属性（如定金、尾款）与日期类属性（如补款日期），形成完整的预定物品流程。
  - 在日历、提醒、统计中接入“待补款”场景，替代旧的补货逻辑。

- [ ] **Step 7.3: 状态流转体验重做**
  - 基于 `ItemStatusDefinitionEntity` 设计统一的状态切换面板。
  - 明确“未购买 → 服役中 → 已退役 / 已过期 / 自定义状态”的流转体验。
  - 为“愿望单转已购买”“预定中转待补款”“待补款转服役中”等关键路径提供专门交互。

---

## 阶段八：领域命名与架构收口 (Domain Naming & Architecture Consolidation)
*目标：从代码结构、命名体系、导航标识到 ViewModel 职责，逐步摆脱旧库存产品骨架，完成面向“个人物品资产管理”的架构收口。*

- [ ] **Step 8.1: 导航与页面命名收口**
  - 将 `navigation_warehouse`、`WarehouseFragment` 等旧命名逐步迁移为与新产品一致的命名，如 `Search`、`Items`、`Assets`、`Wishlist`。
  - 清理导航图中残留的旧 ID 和过渡映射，减少“表面换皮、底层仍旧”的维护成本。
  - 保持迁移阶段的兼容性，避免一次性大规模重命名带来回归风险。

- [ ] **Step 8.2: ViewModel / Repository / Model 语义校正**
  - 审查 `WarehouseItem`、`ShoppingItem`、`TransferToInventory` 等旧领域对象和方法命名。
  - 逐步将其重构为围绕 `Item / Asset / Wishlist / Status / Gallery` 的新语义。
  - 将临时兼容方法集中收口，避免新代码继续依赖旧命名扩散。

- [ ] **Step 8.3: 模块边界与后续演进对齐**
  - 基于新产品定位，明确首页、检索、统计、日历、愿望单、展厅、模板的模块边界。
  - 为后续 KMP、多端适配与 AI 元数据识别预留稳定的数据接口与领域层抽象。
  - 在文档与代码中同步更新术语表，确保设计、开发、测试对产品语言达成一致。
