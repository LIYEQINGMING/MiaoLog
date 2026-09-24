# 物品级规则绑定设计 v2

## 1. 目标

本版将规则系统从“属性定义携带规则绑定”重构为“物品持有规则实例”。

核心关系改为：

1. 物品-属性：物品是属性的集合。
2. 物品-规则：规则是物品内部属性之间的业务关系。
3. 属性-规则：属性可以绑定到规则槽位中，作为输入值，或者作为输出落点。

## 2. 新的用户心智

规则编辑页负责三件事：

1. 定义槽位。
2. 编辑公式。
3. 声明哪些槽位是输出。

这里的槽位只是占位符，不带具体值。

真正的属性绑定发生在物品新增/编辑时：

1. 用户先为物品配置属性。
2. 再为物品添加规则实例。
3. 在规则实例里把物品属性绑定到规则槽位上。

如果规则里仍有必填槽位没有绑定完成，则该规则实例处于“未完成”状态，需要在物品页给出明显提醒。

## 3. 页面结构

物品新增页和编辑页统一改成两个页签：

1. `属性`
   - 配置当前物品具备哪些属性。
   - 编辑属性值。
   - 展示规则推导出的只读输出。
2. `规则`
   - 为当前物品添加规则实例。
   - 为每条规则实例绑定输入/输出槽位。
   - 查看哪些规则仍缺少槽位绑定。

## 4. 数据建模

新增物品级规则实例持久化表：

- `item_rule_bindings`
  - `id`
  - `itemId`
  - `ruleId`
  - `entryAttributeId`
  - `entrySlotKey`
  - `slotBindingsJson`
  - `isEnabled`
  - `togglePlacementAttributeId`
  - `status`
  - `runtimeStateJson`
  - `creationSource`
  - `description`
  - `createdAt`
  - `updatedAt`

其中：

- `RuleDefinition` 继续作为“规则母版”。
- `RuleBindingInstance` 继续作为“规则实例领域模型”。
- `item_rule_bindings` 是 `RuleBindingInstance` 在物品维度上的持久化落点。

补充字段说明：

- `isEnabled` 表示该物品上的这条规则当前是否启用
- `togglePlacementAttributeId` 表示用户把该规则的操作开关挂在了哪个属性下方
- `runtimeStateJson` 用于保存最小运行时状态，例如：
  - `lastExecutedAt`
  - `nextRunAt`
  - `lastGeneratedEventAt`
  - `lastExecutionFingerprint`

## 5. 兼容策略

旧的 `AttributeDefinitionEntity.ruleBindingsJson` 暂时保留，但只作为兼容读取层，不再继续扩展语义。

后续新功能一律以 `item_rule_bindings` 为准：

1. 运行时输出计算。
2. 物品页规则提示。
3. 属性只读联动。
4. 规则缺失槽位校验。

## 6. 交互约束

1. 删除物品属性时，不再因为规则依赖而硬阻塞。
2. 如果某条规则因此失去完整绑定，则它在规则页变为红色提醒状态。
3. 输出槽位如果指向属性，则运行时自动回写属性值。
4. 只读输出则仅做展示，不写回属性。
5. 当规则定义包含 `SCHEDULED` 时，物品级规则实例还要负责持久化下一次调度时间与事件生成状态。

## 7. 当前落地顺序

1. 先落后端：表、DAO、Repository、缓存、运行时入口。
2. 再落前端：物品页双 Tab、规则实例编辑、缺失提醒。
3. 最后再逐步清理旧属性级规则绑定的遗留消费路径，并接入周期调度执行器。
