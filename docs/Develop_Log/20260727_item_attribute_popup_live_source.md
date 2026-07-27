## 20260727 物品页添加属性弹窗接入属性管理实时数据源
### 本轮调整

- 确认并补强了“物品新增页 / 编辑页添加属性弹窗”的属性来源链路：弹窗不再只是页面进入时抓一次快照，而是改成和属性管理页共用同一个属性仓库实时流。
- `AttributeRepository` 新增了面向物品表单侧的 `getAllAttributeEntities()`，直接暴露 `AttributeDefinitionEntity` 的实时流；这让属性管理与物品表单继续共用同一张属性定义表，但各自可以按所需模型消费。
- `AddItemFragment` 改为在页面生命周期内持续订阅属性定义和规则定义。当属性管理里新增、修改或删除属性后，新增物品页的“添加属性”弹窗会随之刷新，`AddItemViewModel` 也会同步更新已选字段的属性引用。
- `EditItemFragment` 同样接入实时订阅，并增加“属性定义与规则定义都准备好后再首绑”的保护，确保编辑页第一次把已有物品属性映射回表单时，规则依赖也已经到位。

### 链路说明

- 属性管理页：`AttributeManagementViewModel -> AttributeRepository.getAllAttributes() -> AttributeDefinitionDao.getAllDefinitions()`
- 物品新增/编辑页弹窗：`AddItemFragment / EditItemFragment -> AttributeRepository.getAllAttributeEntities() -> AttributeDefinitionDao.getAllDefinitions() -> ItemFieldPickerSheet`
- 也就是说，现在这两条链路底层已经是同一份属性定义数据，只是上层分别消费为 `AttributeDefinition` 和 `AttributeDefinitionEntity`。

### 备注

- 本轮没有本地编译，继续由你本机验证。
- 后续如果你希望“属性管理页正在编辑某个属性时，物品页里已添加的该属性卡片也立即刷新显示名/类型说明”，现在这条实时链路已经把基础打通了。
