# 属性管理页面设计文档

## 1. 文档目的

本文档用于固定“属性管理页”的产品方案，作为后续以下模块的统一依据：

- “我的”页面中的属性管理入口与信息架构
- 属性管理页的双视图设计：属性视图与模板视图
- 属性定义与模板对象的浏览、搜索、筛选、创建、编辑、删除交互
- 新增页、编辑页中“轻量使用属性”和管理页中“完整治理属性”的分工
- 基于 `docs/Attribute_Modeling_Spec.md` 与 `docs/Attribute_Template_Spec.md` 的页面落地方式

本文档重点回答：

- `属性管理页本身怎么设计`
- `属性视图和模板视图各自负责什么`
- `属性管理页和新增/编辑页怎么协同`
- `模板视图在不单独建立规则视图时如何承接规则模板`

属性模型与模板边界统一以下列文档为准：

- [Attribute_Modeling_Spec.md](file:///D:/BaiduSyncdisk/CodeSpace/MiaoLog/docs/Attribute_Modeling_Spec.md)
- [Attribute_Template_Spec.md](file:///D:/BaiduSyncdisk/CodeSpace/MiaoLog/docs/Attribute_Template_Spec.md)

---

## 2. 页面定位

### 2.1 为什么需要独立页面

虽然新增页和编辑页已经存在属性补充与属性选择需求，但“使用属性”和“治理属性”是两件不同的事。

当前需要独立属性管理页，主要因为：

1. 用户需要统一查看整个系统中可复用的属性定义，而不是只在某个物品表单里被动看到。
2. 用户需要理解系统当前提供了哪些模板，以及这些模板分别适合创建属性还是创建规则。
3. 新增页和编辑页的职责应是“快速录入和编辑物品”，不应承担完整的属性治理工作。
4. 分类、标签、属性都属于物品体系的全局基础能力，应该统一收口在“我的”页面里。

因此固定一条原则：

- `属性管理属于全局治理能力，不属于某个单一物品的局部编辑能力`

### 2.2 入口位置

“我的”页面中应增加：

- `属性管理`

并与以下入口并列：

- `分类管理`
- `标签管理`

推荐结构：

```text
我的
- 分类管理
- 标签管理
- 属性管理
```

### 2.3 页面职责

属性管理页负责：

- 查看全部属性定义
- 查看全部属性模板与规则模板
- 搜索属性和模板
- 按来源、值类型、模板类型、模板分类等维度筛选
- 查看属性的模板来源、规则绑定摘要、使用数量
- 从模板创建属性
- 查看规则模板边界与依赖输入
- 编辑自定义属性
- 删除自定义属性
- 作为新增页、编辑页中的“去管理属性”全局入口

属性管理页不负责：

- 直接编辑某件物品上的属性值
- 在这里替代新增页或编辑页的表单录入流程
- 让用户自由创建任意新规则类型
- 把属性系统做成任意公式引擎或低代码平台

---

## 3. 核心设计原则

### 3.1 管理对象是“属性定义 + 模板”

属性管理页的核心治理对象有两类：

1. 属性定义
2. 模板

其中模板在当前阶段又分成：

- 属性模板
- 规则模板

这意味着属性管理页必须同时回答三类问题：

- 我当前有哪些属性正在被使用
- 系统当前提供了哪些属性模板可以创建字段
- 系统当前提供了哪些规则模板可以承接跨字段计算

### 3.2 第一阶段固定为双视图

属性管理页第一阶段固定为两个视图：

1. `属性视图`
2. `模板视图`

当前不单独扩展第三个“规则视图”。

原因是：

1. 当前最重要的是先把属性治理和模板治理跑通。
2. 规则模板本质上仍属于模板体系，可先放在模板视图内治理。
3. 双视图结构更适合移动端复杂度控制。

### 3.3 模板视图统一承接两类模板

由于当前不单独建立规则视图，因此模板视图必须同时承接：

- 属性模板
- 规则模板

但在产品表达上不直接把它做成第三层深架构，而是通过：

- 模板类型标签
- 模板类型筛选
- 模板详情中的差异化信息块

来帮助用户区分两类模板。

### 3.4 与分类管理页保持同一视觉语法

属性管理页应尽量沿用分类管理页的视觉与交互语言：

- 顶部标题
- 搜索
- 列表卡片
- 更多操作菜单
- 底部新增入口
- 空状态与搜索状态分离

这样用户更容易理解：

- `分类管理`
- `标签管理`
- `属性管理`

属于同一组全局治理页面。

### 3.5 不把内部建模概念直接暴露给用户

属性系统内部存在：

- 属性模型
- 规则模型
- 属性模板
- 规则模板

但在用户界面中，仍应优先使用更自然的表达：

- `属性`
- `模板`
- `从模板创建`
- `规则绑定`
- `高级设置`

而不是直接暴露抽象模型术语或开放等级。

---

## 4. 页面总体结构

### 4.1 顶部区域

页面顶部建议包含：

- 页面标题：`属性管理`
- 视图切换：
  - `属性`
  - `模板`
- 搜索入口

说明：

- 视图切换是第一阶段页面结构中的核心变化
- 搜索内容应随当前视图切换

### 4.2 主内容区

主内容区根据当前视图不同，展示不同列表：

- 属性视图 -> 属性定义列表
- 模板视图 -> 模板列表

两种视图都应支持：

- 筛选状态栏
- 列表内容
- 空状态
- 搜索无结果状态

### 4.3 底部主操作

页面底部或悬浮按钮建议根据当前视图显示不同主操作：

- 属性视图：
  - `新建属性`

- 模板视图：
  - `从模板创建`

其中模板视图中的 `从模板创建` 进入后，再让用户选择：

- 从属性模板创建属性
- 查看规则模板详情

第一阶段不直接在此页开放“新建规则”主流程。

---

## 5. 属性视图设计

### 5.1 视图职责

属性视图负责治理“属性定义”。

这里看的是：

- 系统中有哪些属性定义
- 每个属性来自哪个属性模板
- 该属性当前被多少物品使用
- 该属性绑定了哪些规则
- 该属性是否会引入额外依赖属性

### 5.2 列表项建议展示的信息

每个属性卡片建议展示：

- 属性图标
- 属性名称
- 来源标签：
  - 系统
  - 自定义
- 值类型标签：
  - 文本
  - 数值
  - 日期
  - 布尔
- 取值方式标签：
  - 输入
  - 固定选项
- 多选状态：
  - 单值
  - 多值
- 模板来源
- 规则绑定摘要
- 使用数量

### 5.3 卡片示意

系统属性示意：

```text
购入价格      [系统] [数值] [输入] [单值]
来源模板：购入价格属性模板
规则：计入总价 / 平均价值
使用：42 个物品
```

用户自定义属性示意：

```text
收藏编号      [自定义] [文本] [输入] [单值]
来源模板：文本模板
规则：无
使用：8 个物品
```

### 5.4 规则绑定摘要规则

属性视图中的摘要不展示规则计算过程，只展示当前属性绑定了哪些规则，以及是否存在依赖补齐。

例如：

- `规则：计入总价`
- `规则：待付尾款（依赖总价）`
- `规则：无`

这样可以帮助用户快速理解字段会如何参与系统行为，而不会让卡片过重。

### 5.5 点击行为

点击属性卡片时：

- 默认进入属性详情或属性编辑页

第一版也可采用更轻量的方式：

- 点击弹出属性编辑对话框

但对于绑定规则较多的属性，更推荐独立详情/编辑页。

### 5.6 更多操作

每个属性卡片应提供“更多操作”入口，推荐包括：

- 查看详情
- 编辑属性
- 删除属性

其中：

- 系统内置属性不显示删除
- 系统内置属性默认不允许编辑核心定义

---

## 6. 模板视图设计

### 6.1 视图职责

模板视图负责治理“模板体系”。

这里看的是：

- 系统中有哪些属性模板
- 系统中有哪些规则模板
- 模板属于哪一类模板
- 模板的默认边界是什么
- 模板能派生出什么对象

### 6.2 列表项建议展示的信息

每个模板卡片建议展示：

- 模板图标
- 模板名称
- 模板类型：
  - 属性模板
  - 规则模板
- 模板分类或规则类型
- 默认值类型或输入角色摘要
- 默认规则绑定或输出定义摘要
- 派生数量

说明：

- 当模板是属性模板时，重点展示默认值类型、选项来源、多选开关、默认规则绑定。
- 当模板是规则模板时，重点展示规则类型、必需输入角色、输出定义、触发方式。

### 6.3 卡片示意

属性模板示意：

```text
购入价格属性模板      [属性模板] [价格家族]
默认：数值 / 输入 / 单值
默认规则：计入总价 / 平均价值
派生属性：12 个
```

规则模板示意：

```text
待付尾款规则模板      [规则模板] [差值规则]
输入：totalPrice / deposit
输出：待付尾款
引用属性：6 个
```

### 6.4 点击行为

点击模板卡片时：

- 默认进入模板详情页

模板详情页建议重点展示：

- 模板基本信息
- 模板类型
- 默认边界
- 典型用途
- 相关依赖说明
- 可创建对象

当模板为属性模板时，重点展示：

- 默认属性名
- 默认值类型
- 默认选项来源
- 默认输入方式 / 选择方式
- 默认多选开关
- 默认规则绑定

当模板为规则模板时，重点展示：

- 规则类型
- 输入角色
- 必需依赖输入
- 可选依赖输入
- 输出定义
- 默认触发方式

### 6.5 更多操作

第一阶段模板视图的更多操作建议保守收口。

推荐包括：

- 查看模板详情
- 从属性模板创建属性

对于规则模板，第一阶段建议仅支持：

- 查看详情
- 查看它会依赖哪些属性
- 查看哪些属性模板默认绑定了该规则模板

第一阶段不建议开放：

- 用户编辑系统模板结构
- 用户删除系统模板
- 用户新建任意模板

---

## 7. 搜索与筛选

### 7.1 搜索行为

搜索应跟随当前视图切换：

- 在属性视图中，按属性名称、值类型、模板名、规则名搜索
- 在模板视图中，按模板名称、模板类型、模板分类、规则类型、输出定义搜索

例如：

- 搜索 `价格`
  - 属性视图应命中购入价格、总价、自定义价格等属性
  - 模板视图应命中购入价格属性模板、总价属性模板、平均价值规则模板等模板

### 7.2 属性视图筛选

属性视图建议支持以下筛选：

- 来源筛选：
  - 全部
  - 系统属性
  - 自定义属性

- 值类型筛选：
  - 全部
  - 文本
  - 数值
  - 日期
  - 布尔

- 取值方式筛选：
  - 全部
  - 输入
  - 固定选项

- 多选状态筛选：
  - 全部
  - 单值
  - 多值

- 规则绑定筛选：
  - 全部
  - 已绑定规则
  - 未绑定规则

- 模板来源筛选：
  - 全部模板
  - 基础属性模板
  - 价格家族属性模板
  - 指定模板

### 7.3 模板视图筛选

模板视图建议支持以下筛选：

- 模板类型：
  - 全部
  - 属性模板
  - 规则模板

- 模板分类：
  - 全部
  - 基础属性模板
  - 基石属性模板
  - 价格家族属性模板

- 规则类型：
  - 全部
  - 差值规则
  - 平均值规则
  - 周期规则
  - 累计规则

- 值类型：
  - 全部
  - 文本
  - 数值
  - 日期
  - 布尔

说明：

- 当模板类型为属性模板时，优先使用模板分类和值类型筛选。
- 当模板类型为规则模板时，优先使用规则类型筛选。

---

## 8. 创建流程

### 8.1 属性视图中的创建入口

属性视图中的 `新建属性` 是完整治理入口。

它和新增页、编辑页中的“轻量创建属性”不是同一层能力。

固定分工如下：

- 新增页 / 编辑页
  - 负责“轻量使用属性”
  - 支持快速创建

- 属性管理页
  - 负责“完整定义属性”
  - 支持通过模板完成标准化创建

### 8.2 新建属性推荐流程

推荐流程如下：

1. 选择属性模板
2. 配置属性名、图标、值类型、选项来源、输入方式 / 选择方式、多选开关
3. 查看模板默认规则绑定
4. 如需启用规则绑定，则展示依赖属性说明
5. 保存属性定义

### 8.3 模板视图中的创建入口

模板视图中的主操作为：

- `从模板创建`

这条路径的优势是：

- 用户先看到模板边界
- 再决定是否基于该模板创建
- 更适合价格等带默认规则绑定的复杂属性

### 8.4 从模板创建属性流程

推荐流程如下：

1. 进入属性模板详情
2. 点击 `从模板创建属性`
3. 系统带出模板默认值
4. 用户微调属性名、图标与字段配置
5. 用户确认是否保留默认规则绑定
6. 保存属性定义

### 8.5 规则模板在第一阶段的承接方式

规则模板当前主要承担：

- 告诉用户某类计算规则需要什么输入
- 告诉用户该规则会输出什么结果
- 告诉用户哪些属性模板会默认绑定该规则

因此第一阶段规则模板更偏向：

- 可浏览
- 可理解
- 可对齐

而不是直接在属性管理页里作为完整“新建规则”主流程暴露给用户。

---

## 9. 编辑与删除

### 9.1 属性定义

#### 9.1.1 系统属性

系统属性默认：

- 不可删除
- 不可修改核心定义

第一阶段建议采用完全只读方案。

#### 9.1.2 自定义属性

允许编辑：

- 名称
- 图标
- 值类型
- 选项来源
- 输入方式 / 选择方式
- 多选开关
- 模板允许范围内的规则绑定

但必须注意：

- 不支持把一个已有属性跨主值类型自由转换
- 不支持越过模板边界添加模板未支持的规则绑定

#### 9.1.3 删除规则

删除自定义属性时，遵循以下规则：

1. 未被使用
   - 可以直接删除

2. 已被使用
   - 必须二次确认
   - 需要明确提示影响范围

建议提示文案：

- `该属性已被 12 个物品使用，删除后这些物品中的该属性值也会被移除，是否继续？`

### 9.2 模板

第一阶段模板以系统内置模板为主。

固定规则如下：

- 系统模板不可删除
- 系统模板不可直接编辑结构
- 系统模板可查看
- 属性模板可用于创建属性
- 规则模板可用于理解规则边界与依赖关系

说明：

- 第一阶段不做用户自定义模板治理
- 模板视图主要承担“查看模板边界”和“从属性模板创建属性”的职责

---

## 10. 与新增页和编辑页的关系

### 10.1 新增页 / 编辑页中的属性入口

新增页和编辑页中，属性相关入口应继续保留在：

- 基础信息区 `+`
- 补充信息区 `+`

这些入口的职责是：

- 为当前表单补充属性
- 轻量选择属性
- 必要时快速创建属性

### 10.2 从表单跳转到属性管理页

当用户在表单中遇到以下场景时，应支持：

- `去属性管理`

典型场景包括：

- 需要统一整理属性体系
- 需要查看属性是否来自合适的模板
- 需要完整配置带规则绑定的复杂属性
- 需要理解某个规则为什么会补出其他依赖属性

### 10.3 表单层自动补齐与管理页协同

根据当前属性模型与规则模型，新增页和编辑页在用户添加属性时，应遵循：

1. 用户选择一个属性
2. 系统将该属性加入当前物品填写清单
3. 系统检查该属性绑定的规则
4. 对每条规则读取必需依赖属性
5. 若依赖属性尚未存在，则自动补齐
6. 若依赖属性已存在，则复用不重复添加

因此管理页需要让用户看得见：

- 一个属性绑定了哪些规则
- 每条规则是否依赖其他属性
- 自动补齐发生的原因是什么

### 10.4 轻量创建与完整治理分工

固定分工如下：

- 轻量创建
  - 放在新增页 / 编辑页的属性选择面板里
  - 创建后自动选中

- 完整治理
  - 放在属性管理页里
  - 用于完整配置属性定义和查看模板边界

### 10.5 数据同步要求

属性管理页中发生以下操作后，新增页与编辑页应立即感知：

- 新建属性
- 编辑属性名称
- 编辑属性配置
- 编辑规则绑定
- 删除属性

这样可以避免：

- 管理页已更新
- 表单里还是旧定义

---

## 11. 页面信息架构拆解

### 11.1 页面层级

属性管理页建议拆成以下层级，而不是把所有交互都塞进一个页面：

#### L0 全局入口

- 我的
  - 属性管理

#### L1 主页面

- `属性管理主页面`
  - 顶部标题
  - 视图切换
  - 搜索
  - 筛选区
  - 列表区
  - 主操作入口

#### L2 详情与创建页面

- `属性详情页`
- `属性编辑页`
- `模板详情页`
- `从模板创建属性页`

#### L3 轻量弹层

- 删除确认弹窗
- 筛选面板
- 规则绑定说明弹窗
- 依赖属性说明弹窗
- 操作结果 Snackbar

这样拆层的原因是：

1. 主页面只负责浏览、搜索、筛选和进入下一层。
2. 复杂结构说明放到详情页，避免列表页过重。
3. 轻量确认与解释型信息放弹层，避免频繁跳页。

### 11.2 主页面信息架构

属性管理主页面建议稳定为以下信息区块：

1. 页面头部
2. 搜索与视图切换区
3. 当前筛选摘要区
4. 主列表区
5. 底部主操作区

推荐结构：

```text
属性管理
- 顶部栏：返回 / 标题 / 主操作
- 视图切换：属性 / 模板
- 搜索框
- 筛选 Chips / 筛选入口
- 列表状态区
  - Loading / Empty / Search Empty / Error / Data
- 底部主操作
```

### 11.3 属性视图信息优先级

属性视图中的信息优先级建议固定如下：

1. 属性名称
2. 值类型 + 取值方式 + 多选状态
3. 模板来源
4. 规则绑定摘要
5. 使用数量
6. 更多操作

这意味着列表卡片要优先让用户快速回答：

- 这是什么属性
- 它怎么填
- 它会参与什么规则
- 它目前被多少物品使用

### 11.4 模板视图信息优先级

模板视图中的信息优先级建议固定如下：

1. 模板名称
2. 模板类型
3. 模板分类或规则类型
4. 默认边界摘要
5. 派生数量或引用数量
6. 更多操作

这意味着模板卡片要优先让用户快速回答：

- 这是属性模板还是规则模板
- 它适合拿来创建什么
- 它默认带什么边界

### 11.5 导航关系

页面之间的推荐跳转关系如下：

```text
我的
-> 属性管理
   -> 属性详情
      -> 属性编辑
   -> 模板详情
      -> 从模板创建属性
新增页 / 编辑页
-> 去属性管理
```

固定原则如下：

- 列表页负责“发现对象”
- 详情页负责“理解对象”
- 编辑页负责“修改对象”
- 创建页负责“生成对象”

---

## 12. 列表状态设计

### 12.1 通用列表状态

属性视图和模板视图都应支持以下通用状态：

1. 初始加载中
2. 首屏空状态
3. 搜索无结果
4. 筛选无结果
5. 正常列表
6. 刷新中
7. 操作失败

不建议只保留一个简单的 `isLoading`。

更推荐区分：

- 首屏加载
- 已有数据时的局部刷新
- 空数据
- 有条件但无命中

这样 UI 提示才不会混乱。

### 12.2 属性视图状态拆解

属性视图建议至少支持以下状态：

#### 12.2.1 首屏加载

展示：

- 骨架列表或居中加载态

说明：

- 用于首次进入页面
- 不显示“暂无属性”，避免误导用户

#### 12.2.2 首屏空状态

触发条件：

- 当前系统中没有任何可展示属性定义

展示建议：

- 标题：`还没有属性`
- 描述：`可以先从基础模板或价格模板创建一个属性。`
- 主操作：`新建属性`

#### 12.2.3 搜索无结果

触发条件：

- 用户输入了搜索词
- 当前视图没有匹配项

展示建议：

- 标题：`没有找到匹配属性`
- 描述：`可以调整关键词，或从模板创建新属性。`

#### 12.2.4 筛选无结果

触发条件：

- 用户选择了筛选条件
- 当前数据存在，但没有命中项

展示建议：

- 标题：`当前筛选条件下没有属性`
- 描述：`可以清空筛选，或切换到其他模板来源查看。`
- 次操作：`清空筛选`

#### 12.2.5 正常列表

展示：

- 属性卡片列表
- 顶部筛选摘要
- 当前结果数量

#### 12.2.6 局部刷新

触发条件：

- 编辑属性后返回列表
- 删除属性后刷新
- 从模板创建属性后刷新

展示建议：

- 保留旧列表
- 顶部或局部显示轻量进度
- 不要整页闪回全屏 loading

#### 12.2.7 操作失败

触发条件：

- 拉取失败
- 删除失败
- 保存失败后回到列表

展示建议：

- 全屏错误态仅用于首屏拉取失败
- 其他失败优先使用 Snackbar 或轻量错误卡片

### 12.3 模板视图状态拆解

模板视图建议至少支持以下状态：

#### 12.3.1 首屏加载

展示：

- 骨架卡片

#### 12.3.2 首屏空状态

理论上第一阶段不应频繁出现，但仍建议保留兜底：

- 标题：`暂无模板`
- 描述：`当前没有可用模板，请稍后重试。`

#### 12.3.3 搜索无结果

- 标题：`没有找到匹配模板`
- 描述：`可以调整关键词，或切换模板类型后再试。`

#### 12.3.4 筛选无结果

- 标题：`当前条件下没有模板`
- 描述：`可以切换模板类型、规则类型或值类型筛选。`

#### 12.3.5 正常列表

展示：

- 属性模板与规则模板混合列表
- 通过标签明确区分两类对象

#### 12.3.6 类型切换态

这是模板视图特有状态：

- 当筛选为 `属性模板` 时，卡片只显示属性模板信息块
- 当筛选为 `规则模板` 时，卡片只显示规则模板信息块

这样可以避免同一张卡片同时承载两套信息结构。

### 12.4 操作反馈状态

除了列表内容状态，还需要统一定义页面动作反馈：

- `Snackbar`
  - 用于成功提示、轻量失败提示

- `Confirm Dialog`
  - 用于删除确认

- `Info Sheet / Dialog`
  - 用于解释规则绑定、依赖属性、模板边界

固定原则如下：

- 破坏性操作走确认弹窗
- 解释型信息走轻量弹层
- 成功和轻量失败走 Snackbar

---

## 13. 详情页结构设计

### 13.1 属性详情页

属性详情页建议拆成以下信息块：

1. 头部摘要
2. 字段定义
3. 规则绑定
4. 依赖补齐说明
5. 使用统计
6. 来源与操作

推荐结构：

```text
属性详情
- 属性名 / 图标 / 来源 / 使用数量
- 值类型 / 选项来源 / 输入方式 / 多选状态
- 规则绑定列表
- 每条规则的依赖属性说明
- 模板来源
- 编辑 / 删除
```

#### 13.1.1 头部摘要

建议展示：

- 属性图标
- 属性名称
- 来源标签
- 模板来源
- 使用数量

#### 13.1.2 字段定义区

建议展示：

- 值类型
- 选项来源
- 输入方式 / 选择方式
- 多选开关

#### 13.1.3 规则绑定区

建议逐条展示：

- 规则名
- 当前属性在规则中的输入角色
- 是否存在必需依赖属性
- 是否存在可选依赖属性

#### 13.1.4 依赖补齐说明区

这一块专门服务于表单联动逻辑。

建议展示：

- 当用户在物品表单中添加该属性时
- 系统会自动补哪些必需依赖属性
- 哪些是只读输出，不会补成输入框

#### 13.1.5 来源与操作区

建议展示：

- 来源模板
- 是否系统内置
- 可执行操作

操作规则：

- 系统属性：查看为主
- 自定义属性：允许编辑、删除

### 13.2 属性编辑页

属性编辑页建议采用分组表单结构，而不是长列表平铺。

推荐分组如下：

1. 基本信息
2. 值定义
3. 规则绑定
4. 保存区

#### 13.2.1 基本信息

- 属性名
- 图标

#### 13.2.2 值定义

- 值类型
- 选项来源
- 输入方式 / 选择方式
- 多选开关

#### 13.2.3 规则绑定

- 已绑定规则列表
- 每条规则的依赖说明
- 模板允许范围提示

固定边界：

- 不允许越过模板边界随意添加规则绑定
- 不允许把已有属性改造成完全不同的主值结构

### 13.3 模板详情页

模板详情页需要一套共享骨架，再按模板类型展示不同内容块。

共享结构建议如下：

1. 头部摘要
2. 模板边界
3. 典型用途
4. 关联对象
5. 主操作

#### 13.3.1 属性模板详情

建议重点展示：

- 默认属性名
- 默认值类型
- 默认选项来源
- 默认输入方式 / 选择方式
- 默认多选开关
- 默认规则绑定
- 已派生属性数量

#### 13.3.2 规则模板详情

建议重点展示：

- 规则类型
- 输入角色
- 必需依赖输入
- 可选依赖输入
- 输出定义
- 默认触发方式
- 当前被多少个属性引用

### 13.4 从模板创建属性页

这一页是模板视图到属性创建的桥梁，建议拆成以下几个块：

1. 模板摘要
2. 可编辑字段
3. 默认规则绑定确认
4. 保存操作

用户在这一页中应当能清楚区分：

- 哪些内容是模板默认带出的
- 哪些内容是自己当前允许修改的
- 哪些规则绑定只是可选开启，不是强制启用

---

## 14. UI 与数据结构落地建议

### 14.1 UI 结构分层

参考现有 [CategoryManagementScreen.kt](file:///D:/BaiduSyncdisk/CodeSpace/MiaoLog/app/src/main/java/com/example/itemmanagement/ui/category/CategoryManagementScreen.kt) 的做法，属性管理页也应拆成：

1. Route / Screen 层
2. Header 层
3. List Pane 层
4. Card 层
5. Detail / Editor 层
6. Dialog / Sheet 层

推荐组件划分如下：

```text
AttributeManagementRoute
-> AttributeManagementScreen
   -> AttributeManagementHeader
   -> AttributeListPane
   -> TemplateListPane
   -> AttributeCard
   -> TemplateCard
   -> EmptyStateCard
   -> FilterSheet
   -> DeleteConfirmDialog
```

### 14.2 顶层 UI State 建议

属性管理页不建议像分类管理那样只用一个较扁平的状态包承接全部信息。

因为这里天然有：

- 双视图
- 多种筛选
- 多类详情页
- 多类弹层

更推荐拆成以下结构：

```kotlin
data class AttributeManagementUiState(
    val isLoading: Boolean = true,
    val selectedTab: AttributeManagementTab = AttributeManagementTab.ATTRIBUTES,
    val searchQuery: String = "",
    val attributePane: AttributeListPaneState = AttributeListPaneState(),
    val templatePane: TemplateListPaneState = TemplateListPaneState(),
    val dialogState: AttributeManagementDialogState? = null,
    val message: String? = null,
)

enum class AttributeManagementTab {
    ATTRIBUTES,
    TEMPLATES,
}
```

### 14.3 列表 Pane 状态建议

推荐把每个视图的列表状态独立出来，而不是把所有字段平铺在根状态里。

```kotlin
data class AttributeListPaneState(
    val filters: AttributeListFilters = AttributeListFilters(),
    val contentState: ListContentState = ListContentState.Loading,
    val items: List<AttributeListItemUiModel> = emptyList(),
    val totalCount: Int = 0,
)

data class TemplateListPaneState(
    val filters: TemplateListFilters = TemplateListFilters(),
    val contentState: ListContentState = ListContentState.Loading,
    val items: List<TemplateListItemUiModel> = emptyList(),
    val totalCount: Int = 0,
)

sealed interface ListContentState {
    data object Loading : ListContentState
    data object Empty : ListContentState
    data object SearchEmpty : ListContentState
    data object FilterEmpty : ListContentState
    data object Data : ListContentState
    data class Error(val message: String) : ListContentState
}
```

### 14.4 列表项 UI Model 建议

属性列表项建议至少包含：

```kotlin
data class AttributeListItemUiModel(
    val id: String,
    val name: String,
    val icon: String,
    val sourceLabel: String,
    val valueTypeLabel: String,
    val optionSourceLabel: String,
    val multiValueLabel: String,
    val templateName: String?,
    val ruleSummary: String,
    val usageCountText: String,
    val isSystemBuiltIn: Boolean,
)
```

模板列表项建议至少包含：

```kotlin
data class TemplateListItemUiModel(
    val id: String,
    val name: String,
    val icon: String,
    val templateType: TemplateUiType,
    val badgeText: String,
    val summaryLine1: String,
    val summaryLine2: String,
    val countText: String,
    val isSystemBuiltIn: Boolean,
)

enum class TemplateUiType {
    ATTRIBUTE,
    RULE,
}
```

### 14.5 详情页 UI Model 建议

属性详情页建议使用独立模型，不直接复用列表项：

```kotlin
data class AttributeDetailUiModel(
    val id: String,
    val name: String,
    val icon: String,
    val sourceLabel: String,
    val templateName: String?,
    val usageCount: Int,
    val valueDefinition: AttributeValueDefinitionUiModel,
    val ruleBindings: List<RuleBindingUiModel>,
    val dependencyHints: List<DependencyHintUiModel>,
    val isEditable: Boolean,
    val isDeletable: Boolean,
)

data class AttributeValueDefinitionUiModel(
    val valueType: String,
    val optionSource: String,
    val inputMode: String,
    val isMultiValue: Boolean,
)

data class RuleBindingUiModel(
    val ruleId: String,
    val ruleName: String,
    val inputRole: String?,
    val requiredDependencies: List<String>,
    val optionalDependencies: List<String>,
)
```

模板详情页建议按类型拆模型：

```kotlin
sealed interface TemplateDetailUiModel {
    val id: String
    val name: String
    val icon: String
    val isSystemBuiltIn: Boolean
}

data class AttributeTemplateDetailUiModel(
    override val id: String,
    override val name: String,
    override val icon: String,
    override val isSystemBuiltIn: Boolean,
    val categoryLabel: String,
    val defaultDefinition: AttributeValueDefinitionUiModel,
    val defaultRuleBindings: List<String>,
    val derivedAttributeCount: Int,
) : TemplateDetailUiModel

data class RuleTemplateDetailUiModel(
    override val id: String,
    override val name: String,
    override val icon: String,
    override val isSystemBuiltIn: Boolean,
    val ruleType: String,
    val inputRoles: List<String>,
    val requiredDependencies: List<String>,
    val optionalDependencies: List<String>,
    val outputs: List<String>,
    val referencedByAttributeCount: Int,
) : TemplateDetailUiModel
```

### 14.6 筛选与弹层状态建议

建议把筛选条件和弹层状态单独结构化，避免后续 if/else 爆炸。

```kotlin
data class AttributeListFilters(
    val source: AttributeSourceFilter = AttributeSourceFilter.ALL,
    val valueType: AttributeValueTypeFilter = AttributeValueTypeFilter.ALL,
    val optionSource: AttributeOptionSourceFilter = AttributeOptionSourceFilter.ALL,
    val multiValue: MultiValueFilter = MultiValueFilter.ALL,
    val ruleBinding: RuleBindingFilter = RuleBindingFilter.ALL,
)

data class TemplateListFilters(
    val templateType: TemplateTypeFilter = TemplateTypeFilter.ALL,
    val templateCategory: TemplateCategoryFilter = TemplateCategoryFilter.ALL,
    val ruleType: RuleTypeFilter = RuleTypeFilter.ALL,
    val valueType: AttributeValueTypeFilter = AttributeValueTypeFilter.ALL,
)

sealed interface AttributeManagementDialogState {
    data class ConfirmDeleteAttribute(
        val attributeId: String,
        val attributeName: String,
        val usageCount: Int,
    ) : AttributeManagementDialogState

    data class RuleBindingInfo(
        val attributeId: String,
        val ruleName: String,
    ) : AttributeManagementDialogState

    data class DependencyInfo(
        val attributeId: String,
        val requiredDependencies: List<String>,
        val optionalDependencies: List<String>,
    ) : AttributeManagementDialogState
}
```

### 14.7 数据层对象建议

为了让 UI 不直接依赖底层存储细节，建议数据层至少拆成：

- 属性定义摘要对象
- 属性定义详情对象
- 模板摘要对象
- 模板详情对象
- 规则绑定对象
- 使用统计对象

推荐接口方向如下：

```kotlin
data class AttributeDefinitionSummary(
    val id: String,
    val name: String,
    val icon: String,
    val valueType: String,
    val optionSource: String,
    val isMultiValue: Boolean,
    val templateId: String?,
    val isSystemBuiltIn: Boolean,
)

data class AttributeDefinitionDetail(
    val summary: AttributeDefinitionSummary,
    val inputMode: String,
    val ruleBindings: List<RuleBindingDefinition>,
    val usageCount: Int,
)

data class TemplateSummary(
    val id: String,
    val name: String,
    val type: String,
    val category: String?,
    val ruleType: String?,
    val isSystemBuiltIn: Boolean,
)
```

### 14.8 ViewModel 组织建议

参考 [CategoryManagementViewModel.kt](file:///D:/BaiduSyncdisk/CodeSpace/MiaoLog/app/src/main/java/com/example/itemmanagement/ui/category/CategoryManagementViewModel.kt)，属性管理页也建议保持：

- 一个主 ViewModel 对外暴露 `StateFlow<AttributeManagementUiState>`
- 由 ViewModel 负责拉取摘要数据、转换 UI Model、处理删除与刷新

但与分类管理不同的是，属性管理页更适合：

1. 将属性视图和模板视图的筛选逻辑拆到独立方法。
2. 将详情页数据拉取和列表页数据拉取分离。
3. 将模板详情与属性详情拆成单独的 detail state，而不是塞回列表 state。

---

## 15. 数据建模建议

属性管理页应基于以下两份文档协同实现：

- [Attribute_Modeling_Spec.md](file:///D:/BaiduSyncdisk/CodeSpace/MiaoLog/docs/Attribute_Modeling_Spec.md)
- [Attribute_Template_Spec.md](file:///D:/BaiduSyncdisk/CodeSpace/MiaoLog/docs/Attribute_Template_Spec.md)

当前页面至少需要承接以下对象：

### 11.1 属性定义对象

建议字段至少包括：

- `id`
- `name`
- `valueType`
- `optionSource`
- `inputMode`
- `isMultiValue`
- `iconName`
- `templateId`
- `isSystemBuiltIn`
- `ruleBindings`
- `ruleBindingSummary`
- `usageCount`

### 11.2 模板对象

建议字段至少包括：

- `templateId`
- `templateName`
- `templateType`
- `templateCategory`
- `isSystemBuiltIn`
- `defaultValueType`
- `defaultOptionSource`
- `defaultInputMode`
- `defaultIsMultiValue`
- `defaultRuleBindings`
- `ruleType`
- `inputRoles`
- `requiredDependencies`
- `optionalDependencies`
- `outputDefinitions`
- `derivedCount`

### 11.3 统计口径

属性视图需要展示：

- 该属性定义被多少个物品实例引用

模板视图需要展示：

- 该模板派生出了多少个属性定义
- 该规则模板当前被多少个属性引用

---

## 16. 第一阶段落地范围

第一阶段建议完成：

1. “我的”页面增加 `属性管理` 入口
2. 属性管理页顶部双视图切换：
   - 属性
   - 模板
3. 属性视图基础列表
4. 模板视图基础列表
5. 搜索属性
6. 搜索模板
7. 属性视图的来源、值类型、取值方式、规则绑定筛选
8. 模板视图的模板类型、模板分类、规则类型筛选
9. 从属性模板创建属性
10. 编辑自定义属性
11. 删除自定义属性
12. 展示属性使用数量
13. 展示模板派生数量或引用数量
14. 展示属性的规则绑定摘要与依赖说明

第一阶段暂不做：

- 用户自定义模板
- 编辑系统模板结构
- 独立规则视图
- 任意规则编辑器
- 批量迁移属性值
- 写回型规则治理

---

## 17. 与现有设计的一致性

属性管理页设计遵循以下原则，与现有系统保持一致：

1. 页面位置一致：与分类管理、标签管理一样放在“我的”页面
2. 视觉语言一致：复用分类管理页的搜索、卡片、更多操作、底部主操作入口
3. 分工一致：新增/编辑页负责使用属性，管理页负责治理属性
4. 模型一致：以“属性负责字段、规则负责跨字段计算、模板只是母板”为主线
5. 命名一致：使用中文界面，必要时在文档中保留英文字段名用于技术对齐

---

## 18. 最终固定方案

当前可固定的属性管理页方案如下：

1. 属性管理放在“我的”页面中，作为全局基础治理能力。
2. 属性管理页第一阶段固定为两个视图：`属性视图` 与 `模板视图`。
3. 属性视图治理属性定义，模板视图统一治理属性模板与规则模板。
4. 属性视图重点展示属性来源、值类型、取值方式、多选状态、模板来源、规则绑定摘要与使用数量。
5. 模板视图重点展示模板类型、模板分类、默认边界、输入角色、输出定义与派生数量或引用数量。
6. 用户创建属性应优先从属性模板出发，尤其是带默认规则绑定的复杂属性。
7. 规则模板第一阶段主要用于帮助用户理解规则边界，不单独扩展为规则视图。
8. 系统内置模板和系统内置属性第一阶段都以只读为主。
9. 新增页和编辑页中的轻量创建，不替代属性管理页的完整治理能力。
10. 表单层的规则依赖自动补齐逻辑，需要在属性管理页中有可见的规则绑定与依赖说明作为支撑。
