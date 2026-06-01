# 数据库实体（Entity）演进与扩充设计方案

## 1. 现状分析
目前项目已有一套基于 `UnifiedItemEntity` 的统一架构：
- **`UnifiedItemEntity`**: 记录基础的物品元数据（名称、价格、日期、地点、备注等）。
- **`ItemStateEntity`**: 记录物品的状态流转，但目前仅依赖枚举 `ItemStateType`（SHOPPING, INVENTORY, DELETED），属于“硬编码”的业务状态。
- 其他辅助表：`LocationEntity`、`TagEntity`、`ItemTagCrossRef` 等已初步具备。

## 2. 核心扩充需求
根据新的产品愿景，我们需要对数据库进行以下几方面的扩展设计：

### 2.1 统计与筛选标识扩充（UnifiedItemEntity）
为了支持“是否纳入总价值/数量统计”的功能，需在 `UnifiedItemEntity` 中新增控制字段。
- `excludeFromTotalValue: Boolean` (默认 false，即默认纳入价值统计)
- `excludeFromTotalCount: Boolean` (默认 false，即默认纳入数量统计)
- *扩展*：针对订阅制，需新增 `isSubscription: Boolean` 和 `autoRenew: Boolean` 等属性。

### 2.2 自定义状态支持（Custom States）
现有的 `ItemStateType` 枚举无法满足“用户自定义状态”（如：外借中、挂闲鱼、已送人、维修中）以及将“未购买”作为一个普通状态（不绑定愿望单）的需求。
- **演进方向**：
  将状态抽象为独立的数据表 `ItemStatusDefinitionEntity`。
  系统预置几个基础状态（不可删除）：
  - 1: 服役中 (In Service)
  - 2: 未购买 (Not Purchased) - *可作为愿望单的筛选条件*
  - 3: 已退役 (Retired)
  - 4: 已过期 (Expired)
  用户可增加自定义状态：如 "借出"。
- **修改 `ItemStateEntity`**：
  将原本依赖的枚举类型 `ItemStateType` 更改为关联状态定义的 ID (`statusId: Long`)。

### 2.3 展厅/图谱功能支持（Gallery）
为了实现“Gallery”功能，我们需要新建实体来管理合集以及合集与物品的映射。
- **`GalleryEntity`**: 展厅定义（ID, Name, Description, CoverImage, CreatedAt）。
- **`GalleryItemCrossRef`**: 多对多关系表（GalleryId, ItemId, AddedAt）。

### 2.4 标签与分类分组支持
为了支持“标签分组”（如：按场景、颜色），需要引入分组表。
- **`TagGroupEntity`**: (ID, Name, Color)。
- **`TagEntity`**: 增加外键 `groupId: Long?`。

### 2.5 货币与汇率记录
- **`CurrencyEntity`**: 记录币种信息（Code, Symbol, ExchangeRateToDefault, LastUpdated）。
- **`UnifiedItemEntity`**: 增加 `currencyCode: String` 字段（默认为本币）。

## 3. 实体(Entity) 具体设计

### 3.1 核心表更新：UnifiedItemEntity
```kotlin
@Entity(tableName = "unified_items")
data class UnifiedItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    // ... 现有字段 ...

    // [新增] 货币单位
    val currencyCode: String = "CNY",

    // [新增] 统计控制
    val excludeFromTotalValue: Boolean = false,
    val excludeFromTotalCount: Boolean = false,

    // [新增] 订阅制属性
    val isSubscription: Boolean = false,
    val autoRenew: Boolean = false,
    val subscriptionCycle: String? = null // DAY, MONTH, QUARTER, YEAR
)
```

### 3.2 状态定义表（新增）：ItemStatusDefinitionEntity
```kotlin
@Entity(tableName = "item_status_definitions")
data class ItemStatusDefinitionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,               // 状态名称（服役中、未购买、挂闲鱼）
    val isSystemBuiltIn: Boolean,   // 是否为系统内置（不可删除）
    val colorHex: String? = null,   // 状态对应的 UI 颜色
    val iconName: String? = null    // 状态对应的 UI 图标
)
```

### 3.3 展厅表（新增）：GalleryEntity & 关系表
```kotlin
@Entity(tableName = "galleries")
data class GalleryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String? = null,
    val coverImagePath: String? = null,
    val createdDate: Date = Date()
)

@Entity(
    tableName = "gallery_item_cross_ref",
    primaryKeys = ["galleryId", "itemId"],
    // 外键约束...
)
data class GalleryItemCrossRef(
    val galleryId: Long,
    val itemId: Long,
    val addedDate: Date = Date()
)
```

## 4. 实施步骤 (Migration Plan)
由于涉及到数据库结构的变更，需要通过 Room Migration 来平滑升级：
1. **Migration 1 (V52 -> V53)**: 在 `unified_items` 中增加 `excludeFromTotalValue`, `excludeFromTotalCount`, `currencyCode`, `isSubscription`, `autoRenew` 列。
2. **Migration 2 (V53 -> V54)**: 创建 `item_status_definitions`，`galleries`，`gallery_item_cross_ref`，`tag_groups` 表。
3. **Migration 3 (V54 -> V55)**: 预置系统内置的状态数据（服役中、未购买等），将现有的枚举状态映射迁移到新的状态表中，修改 `ItemStateEntity`。
4. **DAO 层适配**：新增针对 Gallery 和 StatusDefinition 的 DAO。升级 `UnifiedItemDao` 以支持高速检索（利用 Room 的 FTS 或者复杂的 WHERE LIKE 查询配合索引）。