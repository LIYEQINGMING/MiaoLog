## 20260730 规则启用控制与周期调度正式落地
### 本轮调整

- 按最新确认方案，把“规则是否允许用户启停”和“规则启用后在何时运行”彻底拆开：
  - `USER_TOGGLE` 只表示是否向物品侧暴露启停开关。
  - 规则启用后的基础触发器统一回到默认计算链路：`ON_VALUE_CHANGED + ON_SAVE`。
  - 对“周期扣费”“到期提醒”这类规则，额外补充 `SCHEDULED` 调度能力。
- 规则定义页同步引入“周期调度”正式配置区，允许规则定义：
  - 调度时间来自固定值
  - 调度时间来自槽位
  - 是否生成日历事件
  - 事件标题模板、事件日期槽位、事件去重策略
- 这轮先把数据模型、持久化与规则管理页打通，不在本地编译，继续由你本机验证。

### 数据层

- `RuleDefinition` / `RuleTemplate` 新增：
  - `scheduleConfig`
  - `scheduleEventConfig`
- `RuleSystemModels` 新增：
  - `RuleTriggerMode.SCHEDULED`
  - `RuleScheduleTimeSourceType`
  - `RuleScheduleConfig`
  - `RuleScheduleEventConfig`
  - `RuleRuntimeState`
- `RuleBindingInstance` 新增 `runtimeState`，为后续记录最近执行状态、下一次计划执行时间预留正式落点。
- 规则默认有效触发器统一调整为：
  - `ON_VALUE_CHANGED`
  - `ON_SAVE`
- Room 持久化同步扩展：
  - `rule_definitions.scheduleConfigJson`
  - `rule_definitions.scheduleEventConfigJson`
  - `item_rule_bindings.runtimeStateJson`
- `AppDatabase` 版本升级到 `66`，新增 `65 -> 66` 迁移。

### 仓储与系统规则

- `AttributeRepository` 的规则读写映射已接通调度字段：
  - 保存规则时写入 `scheduleConfigJson / scheduleEventConfigJson`
  - 读取规则时反序列化调度配置
- 规则归一化逻辑新增兜底：
  - 当规则声明了 `SCHEDULED`，但没有完整调度配置时，会补最小默认结构，避免旧数据直接失效。
- 系统内置“周期扣费规则模板”已升级为正式调度规则：
  - 触发器包含 `SCHEDULED`
  - 调度时间来源默认走槽位
  - 已补事件生成配置
  - 旧版“先不做调度”的过渡描述已移除

### 规则管理页

- 规则列表、规则详情、模板详情补充了新的摘要信息：
  - 启用控制摘要
  - 周期调度摘要
  - 事件生成摘要
- 规则工作台 `RuleWorkbenchPage` 已开始承接新配置：
  - “运行方式”语义改为“启用控制”
  - 新增“周期调度”区块
  - 支持固定值 / 槽位来源两类调度时间
  - 支持日历事件相关字段录入
- 本轮补齐了规则工作台里最后几个关键交互：
  - 周期槽位改为真正可点击的多选 `FilterChip`
  - 槽位删除或变更后，会自动清理失效的调度槽位 key
  - 保存规则时，周期槽位会回写到 `scheduleSlotKeys`
  - `RuleTriggerMode.SCHEDULED` 的中文标签已补为“周期调度”
- `RuleEditorDraftUiModel` 的默认触发器也同步改成了：
  - `ON_VALUE_CHANGED + ON_SAVE`
  - 保证新建规则草稿与最终保存逻辑语义一致

### 备注

- 这轮还没有实现真正的“周期调度执行器”与“日历事件同步器”，当前先完成规则定义、存储结构和管理页编辑能力。
- 本轮没有在本地执行编译，继续由你本机验证。
- 文件继续按 `UTF-8` 维护，避免再次引入中文文案乱码。
