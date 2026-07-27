## 20260723 规则实例上浮到物品层

### 后端落点

- 新增了 `item_rule_bindings` 持久化表，专门承接物品级规则实例，不再把“当前物品用了哪些规则”继续塞回属性定义。
- 新增 `ItemRuleBindingEntity` 与 `ItemRuleBindingDao`，并把 Room 版本升级到 `63`，补上 `62 -> 63` 迁移。
- `UnifiedItemRepository` 现在已经能按 `itemId` 读取和替换整组 `RuleBindingInstance`，物品保存链正式具备“属性值 + 规则实例”一起持久化的能力。

### 状态链路

- `BaseItemViewModel` 新增了 `itemRuleBindings` 状态，并提供增删改入口。
- `ItemStateCacheViewModel` 的新增/编辑缓存都补进了 `ruleBindings` 字段，规则页编辑后的结果已经能随页面切换保留下来。
- `AddItemViewModel`、`EditItemViewModel` 已经开始在保存时同步落库物品级规则实例，编辑页也会回读现有规则实例。

## 20260723 物品页切到 属性/规则 双页签

### 交互调整

- 物品新增页、编辑页开始按新的心智拆成 `属性` / `规则` 两个页签。
- 规则页不再从某个属性出发去拼“属性绑定规则”，而是直接管理“当前物品有哪些规则实例”。
- 每条规则实例都支持面向槽位做属性绑定，未绑定完整的必填槽位会走红色边框提醒。

### 运行时入口

- 新增了物品级规则辅助组件，开始让只读输出和自动写回基于 `itemRuleBindings` 运行，而不是继续依赖属性定义里的旧 `ruleBindingsJson`。
- 当前这层仍保留和旧运行时的兼容边界，但新的物品页已经优先消费物品级规则实例。

### 备注

- 本轮编译校验被本机 Gradle wrapper 锁文件 `gradle-8.11.1-bin.zip.lck` 的访问拒绝拦住，尚未拿到完整 Kotlin 编译结果。
