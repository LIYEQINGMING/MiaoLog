# 个人物品与资产管理 APP - 数据库设计文档

## 1. 架构概览
数据库架构设计的核心目标是“极高的扩展性”和“极速的检索能力”。系统将“物品”视为一个核心的信息锚点（Entity），所有的功能（统计、愿望单、Gallery、图谱）均围绕该元数据展开。

本系统采用 Room 数据库构建，主要分为以下几个数据域：
1. **物品元数据域（Item Core）**：定义物品的基础属性、状态和扩展字段。
2. **字典与配置域（Dictionaries）**：定义分类、自定义状态、货币汇率等全局配置。
3. **关系域（Relations）**：管理标签、Gallery 合集与物品之间的多对多映射。

## 2. 核心数据表设计 (Tables & Entities)

### 2.1 物品核心表：`UnifiedItemEntity` (Table: `unified_items`)
这是整个系统最核心的表，记录了物品的所有基础属性。

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | Long (PK, Auto) | 唯一标识符 |
| `name` | String | 物品名称 |
| `category` | String | 物品主分类 |
| `subCategory` | String? | 物品子分类 |
| `brand` | String? | 品牌信息 |
| `purchasePrice` | Double? | 购买价格 |
| `currencyCode` | String | 货币代码（如 CNY, USD），默认 CNY |
| `purchaseDate` | Date? | 购买日期 |
| `expirationDate` | Date? | 有效期/到期日 |
| `isSubscription` | Boolean | **[标识]** 是否为订阅制服务 |
| `autoRenew` | Boolean | **[标识]** 是否自动续费（针对订阅制） |
| `subscriptionCycle`| String? | 付款周期（DAY, MONTH, QUARTER, YEAR） |
| `excludeFromTotalValue`| Boolean | **[控制]** 是否不纳入总价值统计（默认 false） |
| `excludeFromTotalCount`| Boolean | **[控制]** 是否不纳入物品总数统计（默认 false） |
| `templateId` | Long? | **[关联]** 物品关联的模板 ID，用于模板维度筛选 |
| `locationAddress` | String? | 存放位置（文本描述） |
| `createdDate` | Date | 记录创建日期 |
| `updatedDate` | Date | 记录最后修改日期 |

### 2.2 状态定义表：`ItemStatusDefinitionEntity` (Table: `item_status_definitions`)
取代代码中的硬编码枚举，将状态抽取为字典表，支持用户自定义扩展。

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | Long (PK, Auto) | 状态唯一标识 |
| `name` | String | 状态名称（如：服役中、未购买、外借中） |
| `isSystemBuiltIn`| Boolean | 是否为系统内置状态（内置状态不可被用户删除） |
| `colorHex` | String? | 该状态在 UI 上的标识颜色 |
| `iconName` | String? | 该状态的图标引用名 |

*系统初始化时需预置的数据*：
- ID=1: 服役中 (In Service)
- ID=2: 未购买 (Not Purchased) -> 用于支撑愿望单业务
- ID=3: 已退役 (Retired)
- ID=4: 已过期 (Expired)
- ID=5: 待补款 (Pending Final Payment) -> 用于支撑预定类物品业务

### 2.3 物品状态关联表：`ItemStateEntity` (Table: `item_states`)
记录物品在特定时间点进入的某种状态。

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | Long (PK, Auto) | 记录唯一标识 |
| `itemId` | Long (FK) | 关联的物品 ID |
| `statusId` | Long (FK) | 关联的状态定义 ID |
| `isActive` | Boolean | 该状态记录当前是否处于激活中 |
| `activatedDate` | Date | 进入该状态的时间 |
| `notes` | String? | 状态流转备注 |

### 2.4 展厅/合集表：`GalleryEntity` (Table: `galleries`)
用于管理用户创建的物品合集视图。

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | Long (PK, Auto) | 展厅唯一标识 |
| `title` | String | 展厅标题（如：“我的摄影器材”） |
| `description` | String? | 展厅描述 |
| `coverImagePath`| String? | 展厅封面图本地路径 |
| `createdDate` | Date | 创建时间 |

### 2.5 展厅与物品映射表：`GalleryItemCrossRef` (Table: `gallery_item_cross_ref`)
多对多关系表，记录哪些物品被加入了哪些展厅。

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `galleryId` | Long (PK, FK) | 关联的展厅 ID |
| `itemId` | Long (PK, FK) | 关联的物品 ID |
| `addedDate` | Date | 物品加入展厅的时间 |

### 2.6 标签分组表：`TagGroupEntity` (Table: `tag_groups`)
对零散的标签进行结构化管理。

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | Long (PK, Auto) | 分组唯一标识 |
| `name` | String | 分组名称（如：场景、重要程度） |
| `colorHex` | String? | 分组专属颜色 |

*(注：原有的 `TagEntity` 需增加 `groupId: Long?` 字段指向此表)*

### 2.7 高级自定义属性系统表：`CustomAttributeDefinitionEntity` & `ItemCustomAttributeEntity`
为了支持强类型的自定义属性（如带有统计开关的“价格类”定金、自动接轨日历的“时间类”属性），对现有的 `field_custom_values` 进行升级，引入属性定义表与实例映射表。

**属性定义表：`CustomAttributeDefinitionEntity`** (Table: `custom_attribute_definitions`)

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | Long (PK, Auto) | 属性定义唯一标识 |
| `name` | String | 属性名称（如：定金、维修费、生产日期） |
| `attributeType`| String | 属性类型枚举（`PRICE`, `DATE`, `TEXT`, `BOOLEAN`） |
| `defaultValue`| String? | 默认值（序列化为字符串） |

**物品自定义属性实例表：`ItemCustomAttributeEntity`** (Table: `item_custom_attributes`)

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | Long (PK, Auto) | 实例唯一标识 |
| `itemId` | Long (FK) | 关联的物品 ID |
| `definitionId` | Long (FK) | 关联的属性定义 ID |
| `value` | String | 实际填写的值（如 "500.00" 或 "2024-01-01"） |
| `includeInTotal` | Boolean | **[特有配置]** 针对 `PRICE` 类型，是否将此值计入总价值统计 |

### 2.8 物品模板系统表：`ItemTemplateEntity`
用于实现物品模板功能，让用户可以预先配置好属性组合、默认分类和默认标签，以便在新建物品时快速带入。

**物品模板表：`ItemTemplateEntity`** (Table: `item_templates`)

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | Long (PK, Auto) | 模板唯一标识 |
| `name` | String | 模板名称（如：数码产品模板、手办模板） |
| `description` | String? | 模板描述 |
| `defaultCategoryId`| Long? | 从此模板创建物品时的默认分类 |
| `systemAttributesConfig`| String | JSON 格式，配置启用了哪些基础系统属性及默认值 |
| `customAttributeIds` | String | JSON 格式 (Long Array)，配置关联了哪些自定义属性定义的 ID |
| `defaultTags` | String | JSON 格式，配置默认附带的标签名称或 ID 列表 |
| `createdDate` | Date | 创建时间 |
| `updatedDate` | Date | 修改时间 |

## 3. 索引与检索优化 (Indices & Optimization)
为了支撑核心能力——**高速物品检索与多维筛选**，数据库层面必须建立完善的索引机制。

### 3.1 复合索引 (Composite Indices)
在 `UnifiedItemEntity` 表上建立以下索引：
- `Index(value = ["category", "subCategory"])`：加速分类维度的聚合与筛选。
- `Index(value = ["isSubscription"])`：加速筛选所有订阅制服务。
- `Index(value = ["templateId"])`：加速按照“物品模板”维度进行检索与筛选。
- `Index(value = ["excludeFromTotalValue", "excludeFromTotalCount"])`：在计算首页统计面板总值时，快速排除无效数据。

### 3.2 FTS (Full-Text Search) 规划
如果物品名称 (`name`)、品牌 (`brand`)、备注 (`customNote`) 文本较长，可考虑引入 Room 的 `@FTS4` 虚拟表映射，以实现毫秒级的全局关键字模糊搜索。

## 4. 业务逻辑在 DB 层的体现
1. **愿望单逻辑**：通过查询 `ItemStateEntity` 中 `statusId = 2` (未购买) 且 `isActive = true` 的所有 `itemId`，关联查询出物品列表。
2. **预定补款逻辑**：通过查询 `statusId = 5` (待补款) 获取预定列表；通过关联 `ItemCustomAttributeEntity` 读取类型为 `PRICE` 且名称为“定金”的属性值。
3. **总价值统计逻辑**：
   `总价值 = (SUM(UnifiedItemEntity.purchasePrice) WHERE excludeFromTotalValue = 0) + (SUM(ItemCustomAttributeEntity.value) WHERE definition.attributeType = 'PRICE' AND includeInTotal = true)`
   *(实际运算时还需根据 `currencyCode` 进行汇率换算)*。
4. **订阅制付款周期**：对于 `isSubscription = true` 的物品，读取其 `subscriptionCycle`，配合 `purchaseDate` 和当前系统时间动态推算下一次扣费日，用于触发到期提醒。