## 20260731 规则画布与日历调度后端基础落地
### 本轮调整

- 在不打断现有规则链路的前提下，先把“画布式规则编辑器”和“日历规则调度”所需的底层正式建起来。
- 本轮重点不是把最终 UI 一次性做完，而是先完成：
  - 数据模型
  - 持久化字段
  - 仓储兼容映射
  - 规则编辑草稿结构
  - 当前规则编辑页中最明显的旧语义清理

### 数据层

- 新增规则画布模型：
  - `RuleCanvasDefinition`
  - `RuleCanvasNode`
  - `RuleCanvasConnection`
  - `RuleCanvasSlotConfig`
  - 以及节点类型、样式提示、布局模式等辅助枚举
- `RuleDefinition` / `RuleTemplate` 新增：
  - `canvasDefinition`
- `RuleScheduleConfig` 扩展为兼容新日历规则模型：
  - `sourceMode`
  - `manualRule`
  - `slotDrivenConfig`
- 新增日历规则相关枚举与模型：
  - `RuleScheduleSourceMode`
  - `RuleScheduleFrequency`
  - `RuleScheduleEndType`
  - `RuleScheduleWeekday`
  - `RuleScheduleMonthlyPatternType`
  - `RuleManualScheduleRule`
  - `RuleSlotDrivenScheduleConfig`
- 增加了调度配置辅助函数：
  - `resolveSourceMode()`
  - `allReferencedSlotKeys()`

### 持久化

- `RuleDefinitionEntity` 新增：
  - `canvasDefinitionJson`
- `AppDatabase` 版本从 `66` 升到 `67`
- 新增迁移：
  - `MIGRATION_66_67`
  - 为 `rule_definitions` 增加 `canvasDefinitionJson`

### 仓储与兼容

- `AttributeRepository` 已接通：
  - `canvasDefinition` 的 JSON 读写
  - 新日历规则模型的读写
- 规则归一化逻辑已补兼容：
  - 老的 `FIXED / SLOT` 调度配置会自动推断为新的 `MANUAL_CALENDAR_RULE / SLOT_DRIVEN`
  - 周期规则归一化后，会自动整理所有被调度引用的槽位 key
  - 周期规则的事件配置在归一化后统一视为生成日历事件
- 系统内置“周期扣费规则模板”已升级为新的槽位驱动调度结构。

### 规则编辑草稿

- `RuleEditorDraftUiModel` 新增：
  - `scheduleSourceMode`
  - `manualScheduleRule`
  - `slotDrivenScheduleConfig`
  - `canvasDefinition`
- `AttributeManagementViewModel` 已接通上述新字段：
  - 构建规则编辑草稿时回显
  - 保存规则时写入正式模型
  - 调度摘要已能区分：
    - 手动日历规则
    - 槽位驱动调度
- 为了兼容当前还未完全重做的旧编辑页，还补了一层“自动推断调度来源模式”：
  - 如果旧 UI 仍然只写 `scheduleTimeSourceType = SLOT`，保存时仍会自动转成 `SLOT_DRIVEN`
  - 避免新旧结构并行期间把调度数据写坏

### 当前规则编辑页的过渡清理

- 先移除了当前页中几处已经不符合新设计的内容：
  - 去掉了“规则类型”编辑入口
  - 去掉了“运行时机 当前编辑页先用默认触发时机……”说明
  - “启用控制”改成更接近正式语义的单一能力开关
  - “周期调度”中不再展示“是否生成日历事件”的旧开关，直接收口为事件配置

### 备注

- 本轮没有在本地执行编译，继续由你本机验证。
- 下一阶段会继续往前推进：
  - 规则定义页的真正画布 UI
  - 槽位拖拽与配置弹窗
  - 手动日历规则编辑器
