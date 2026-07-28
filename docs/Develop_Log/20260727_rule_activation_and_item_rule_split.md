## 20260727 规则启停模型与物品规则实例拆分
### 本轮调整

- 把“属性定义中的规则绑定”与“物品中的规则实例”正式拆开：属性定义继续只负责定义字段本身，不再持久化真实规则绑定实例；物品页里的规则实例继续负责槽位绑定、完整性校验与运行时启停。
- 规则模型新增了启用方式定义：
  - `ALWAYS_ON`
  - `USER_TOGGLE`
- 对 `USER_TOGGLE` 规则新增了开关 UI 配置：
  - 启用态文案
  - 停用态文案
  - 锚点槽位
  - 默认启用状态
- 物品规则实例新增 `isEnabled`，这样规则定义负责“可不可以切换”，物品规则实例负责“当前这个物品上开没开启”。

### 数据层

- `RuleDefinition` / `RuleTemplate` 增加了 `activationMode` 与 `toggleUiConfig`。
- `RuleBindingInstance` 增加了 `isEnabled` 与 `isRuntimeActive`。
- `RuleDefinitionEntity` 与 `ItemRuleBindingEntity` 同步扩展持久化字段。
- `AppDatabase` 版本提升到 `64`，并新增 `MIGRATION_63_64`：
  - `rule_definitions.activationMode`
  - `rule_definitions.toggleUiJson`
  - `item_rule_bindings.isEnabled`
- `AttributeRepository` 的规则读写映射已同步支持新字段。

### 规则定义与系统规则

- `计入总价` 系统规则模板已切到 `USER_TOGGLE`，并内置：
  - 启用：`计入总价值`
  - 停用：`不计入总价值`
  - 锚点槽位：`amount`
- `ensureSystemRules()` 改成对系统规则执行保存覆盖，而不是只补缺失项，保证现有库里的系统规则也能同步拿到新的启停配置。

### 属性管理页

- 属性编辑页中的规则区块改成“关联规则（快捷入口）”语义：
  - 允许先做规则草稿与槽位思考
  - 但不再把这些草稿持久化为属性定义上的真实规则绑定
- “保存属性”文案改为更明确的“保存属性定义”。
- 规则编辑页新增“运行方式”区块，可配置：
  - 始终运行 / 用户开关控制
  - 启用态文案
  - 停用态文案
  - 开关显示位置
  - 默认启用状态

### 物品页

- 物品规则卡支持显示规则启停开关。
- 运行时只会对 `isRuntimeActive` 的规则实例执行输出计算。
- 当规则被停用时：
  - 只读输出不再继续计算
  - 规则输出字段 / 系统变量会按活跃投影重新归并，避免保留过期运行结果
- 缺少必填槽位的浅红边框提醒仍保留。

### 备注

- 本轮没有本地编译，继续由你本机验证。
- 设计同步文档已新增：`docs/Item_Attribute_Rule_Activation_Design.md`
