## 20260723 物品规则链路 ViewModel 恢复与重接

### 本轮调整

- 按确认方案先把 `AddItemViewModel.kt` 和 `EditItemViewModel.kt` 恢复到仓库基线，再基于当前 `UTF-8` 项目编码和现有规则架构重新补改，避免继续在损坏文件上增量修补。
- 新增页的 `AddItemViewModel` 重新接回了属性定义注册能力，补上 `ruleBindings` 缓存读写，并在保存物品时同步落库 `item_rule_bindings`，让“物品属性 + 物品规则实例”能一起持久化。
- 编辑页的 `EditItemViewModel` 改成直接对接当前的 `AttributeDefinitionEntity / RuleDefinition / RuleBindingInstance` 体系，补上规则绑定加载、缓存恢复、保存落库，以及自定义属性统一编解码。
- 为了让“只改规则也算未保存修改”成立，`BaseItemViewModel` 的规则绑定更新入口已开放为可覆写，编辑页现在会把规则新增、编辑、删除和规则输出自动写回一并纳入 `hasUnsavedChanges` 判断。

### 备注

- 本轮未执行编译，仍由用户本地继续验证。
- 这次修复以“恢复可维护结构 + 接回当前规则架构”为主，没有额外改动你未授权回滚的其他业务文件。
