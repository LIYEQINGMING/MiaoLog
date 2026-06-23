# 属性管理开发日志
## 20260623 17:05 第九轮开发结果
### 开发要求
1. 属性管理这个页面现在和分类管理页面完全不一样啊，属性管理也顶上还有个状态栏。也是返回和标题。这一点和分类管理不一样。你完整地，仔细的，查看分类管理也，对于顶部栏和底部栏的沉浸式管理，调整属性管理页。
2. 打开属性管理页，app的底部操作栏应该不显示才对，像分类管理页面一样。
3. 属性管理也中对于规则的管理怎么没有了，现在只有属性和模板，这里应该是是三个标签页。
4. 清单显示区域大小不一。图标应该统一大小，排版统一

### 完成内容
已调整

- 属性管理页现在按分类管理页的方式走沉浸式页面壳：系统 TopBar 和底部导航在进入 nav_attribute 时都会隐藏，页面只保留内部自绘头部，处理逻辑已加到 MainActivity.kt 。
- 属性管理页头部改成和分类管理页一致的结构：返回、标题、右侧主操作按钮都放在页内头部卡片里，不再依赖底部浮动按钮，见 AttributeManagementScreen.kt 。
- 属性管理现在补成了 3 个标签页： 属性 、 规则 、 模板 ，对应 UI 模型已扩展到 AttributeManagementUiModels.kt 。
- 规则页签已经接入真实 RuleDefinition 数据流，不再只是模板里隐含存在；规则列表和规则详情都已接通，见 AttributeManagementViewModel.kt 。
- 三类清单卡片的图标容器、尺寸和文本排版已统一，属性/规则/模板都改成同一套 ListItemIcon 和一致的卡片节奏，见 AttributeManagementScreen.kt 。
具体变化

- 底部导航隐藏：
  - 给 nav_attribute 补进了 MainActivity 的底部导航隐藏列表和 TopBar 隐藏分支。
  - 同时把它归到“我的”标签的底部导航选中态解析里，和分类管理保持一致。
- 头部栏对齐分类管理：
  - 保留页内沉浸式头部卡片。
  - 列表模式下右上角使用头部 + 按钮，而不是底部 FAB。
  - 搜索框占位文案按当前页签切换。
- 规则管理补齐：
  - 新增 RuleListPaneState 、 RuleListItemUiModel 、 RuleDetailUiModel 。
  - 新增 RuleDetail 路由态和 openRuleDetail() 。
  - 为规则列表增加 RuleCard 和 RuleDetailPage() 。
- 规则数据初始化：
  - 当本地规则表为空时，会基于系统规则模板自动播种系统规则定义，避免规则页签打开就是空白。
- 清单排版统一：
  - 所有卡片图标都固定为统一圆形容器和统一内部图标尺寸。
  - 列表底部留白从为底部导航预留的较大间距，收敛成和分类管理一致的普通沉浸式留白。
代码位置

- 页面结构与列表样式调整： AttributeManagementScreen.kt
- 属性管理状态与规则页签数据接入： AttributeManagementViewModel.kt
- UI 模型扩展为三标签页： AttributeManagementUiModels.kt
- Fragment 事件绑定补齐： AttributeFragment.kt
- Activity 级顶部栏/底部栏隐藏规则： MainActivity.kt

## 20260623 17:00 第八轮开发结果
### 内容
已修复

- ExperimentalLayoutApi 的导入包写错了，已从 androidx.compose.foundation.ExperimentalLayoutApi 改为 androidx.compose.foundation.layout.ExperimentalLayoutApi ，并直接引入 FlowRow ；同时补回了 Edit 图标导入，修掉了 Info/Edit 相关的未解析引用，见 AttributeManagementScreen.kt 。
- 系统内置模板 ID 做了明确命名空间区分，属性模板统一为 template_attr_* ，规则模板统一为 template_rule_* ，见 AttributeRepository.kt 。
- 补上了缺失的系统“日期模板” template_attr_date ，这样 AttributeManagementViewModel.kt 里默认播种的“购买日期”属性不再引用一个不存在的模板。
- 系统播种属性 ID 也做了唯一化，改成了 attr_system_purchase_price 、 attr_system_purchase_date ，见 AttributeManagementViewModel.kt 。
- 规则绑定标识同步规范为 rule_system_* ，并把规则模板匹配逻辑调整为同时支持按 id 和 key 查找，避免“模板 ID 唯一化之后，规则名称反而显示不出来”的问题，见 AttributeRepository.kt 和 AttributeManagementViewModel.kt 。

## 20260623 16:42 第七轮开发结果
### 内容
### 1. 修复模板和属性的 ID 冲突
在 AttributeRepository.kt 中，由于属性模板和规则模板的 ID 存在重复或容易混淆的命名（比如之前规则模板和其实例属性都用了类似 rule_remaining_payment 的 ID），我已经将它们的 id 添加了明确的前缀以保证全局唯一：

- 属性模板 ID ：统一加上了 template_attr_ 前缀（例如： template_attr_purchase_price 、 template_attr_deposit ）。
- 规则模板 ID ：统一加上了 template_rule_ 前缀（例如： template_rule_remaining_payment 、 template_rule_average_value ）。
  同时，我也同步更新了 AttributeManagementViewModel.kt 中默认播种属性 ( seedSystemAttributes ) 引用这些模板时的硬编码 ID。
### 2. 修复 AttributeManagementScreen.kt 编译错误
在 AttributeManagementScreen.kt 中：

- 修复了 Unresolved reference: Info ：导入了缺失的 androidx.compose.material.icons.filled.Info 图标资源。
- 修复了 ExperimentalLayoutApi 注解报错 ：之前的方法注解位置或者嵌套存在问题，我已经把 @OptIn(ExperimentalLayoutApi::class) 注解精确地加到了使用了 FlowRow 的组件函数上（即 AttributeFilterSection 和 TemplateFilterSection ），并从主组件的注解中移除了该标记。

## 20260623 16:20 第六轮开发结果
### 内容
### 1. 数据库与实体层落地
- 新实体 RuleDefinitionEntity ：创建了 RuleDefinitionEntity.kt ，严格对应了规范中规则应包含的信息，包括：规则计算类型（ computationType ）、输入角色 ( inputRoles )、必需/可选依赖以及输出键 ( outputKeys )。
- 新数据访问对象 RuleDefinitionDao ：创建了 RuleDefinitionDao.kt ，提供针对规则定义的增删改查流支持。
- 数据库升级 ：更新了 AppDatabase ，将版本从 58 升至 59 ，并成功编写和挂载了 MIGRATION_58_59 建表脚本。
### 2. 仓储层（Repository）能力扩展
- 在 AttributeRepository.kt 中引入了 RuleDefinitionDao 。
- 新增了针对规则数据的 getAllRules() 、 getRuleById() 、 saveRule() 以及 deleteRule() 的完整操作接口。
- 添加了实体和业务模型间的转换方法 ( RuleDefinitionEntity.toModel() 和 RuleDefinition.toEntity() )。
- 在 ItemManagementApplication.kt 中更新了注入逻辑。
### 3. ViewModel 层的数据串联
- 在 AttributeManagementViewModel.kt 中，通过 viewModelScope 开启了新的流监听 repository.getAllRules() 。
- 双向数据解析与展示修复 ：在原本展示“规则绑定 (RuleBinding)”的地方，原本只能强依赖系统预置的规则模板名称；现在已经修改为优先从持久化层查找到对应的自定义 RuleDefinition 名称，兜底才会回退去找模板或显示 ID。

## 20260623 16:00 第五轮开发结果
### 内容 
### 1. 数据库实体与 DAO 落地 (Room Database)
- 创建新实体 ：新建了 AttributeDefinitionEntity ，该实体完整对应了最新规范中收敛后的 6 项核心字段（名称、值类型、取值方式、输入方式、单/多选、绑定的规则）。复杂对象（如下拉选项、规则绑定关系）直接映射为了 JSON 字符串，以保证结构轻量。
- 创建 DAO ：新建了 AttributeDefinitionDao 接口，提供标准的插入、查询流 ( Flow ) 和删除等操作。
- 更新数据库版本 ：在 AppDatabase 中注册了新的表，并将数据库版本从 57 升级至 58，同时编写了无缝迁移的 MIGRATION_57_58 脚本。
### 2. 仓储层落地 (AttributeRepository)
- 新建了 AttributeRepository 统一管理属性和模板数据。
- 系统内置模板治理 ：将设计文档中定义的基础模板（文本模板、单选模板）和基石模板（购入价格模板、定金模板），以及规则模板（待付尾款、平均价值、订阅等），都统一移交至仓储层作为系统内置数据提供。
- 提供了实体对象 ( AttributeDefinitionEntity ) 与业务模型 ( AttributeDefinition ) 之间的 Mapper 转换能力。
### 3. 属性管理 ViewModel 改造
- 接入响应式流 ：重写了 AttributeManagementViewModel 。之前它是直接写死的 mock 列表，现在改为通过 viewModelScope 收集 repository.getAllAttributes() 的数据流。
- 系统数据自动兜底 ：当检测到数据库中没有任何属性定义时，会自动播种 (Seed) 默认的系统属性（如“购入价格”、“购买日期”），与预置的系统模板相对应。
- 增删改查贯通 ：原本只起 UI 变化作用的 saveAttributeDraft() 和 confirmDeleteAttribute() 等操作，现已全部桥接到 Repository 去真实操作 Room 数据库。
- 依赖注入 ：通过在 ItemManagementApplication 暴露 repository，并新建 AttributeManagementViewModelFactory ，使得 AttributeFragment 能够正常拉起重构后的 ViewModel。

## 20260623 15:12 第四轮开发结果
### 内容
已整理

- 已保留 Item.kt ，它是当前全局主模型，不属于这轮要清掉的属性旧设计。
- 已保留 TemplateFieldDefaults.kt ，并补充说明：它只是现有模板默认值 JSON 的兼容结构，不是新的属性模板主模型。
- 已把旧的价格属性兼容模型迁到 LegacyPriceFieldModels.kt ，让它归到 data/model/attribute 。
- 已删除旧文件 data/model/PriceAttributeModels.kt ，避免属性相关模型继续散落在 data/model 根目录。
保留与删除

- 保留 ： PriceActionConfig 、 PriceRecurrenceConfig 、 PriceAttributeValue
  - 原因： ItemFormComponents 里的旧价格字段表单还在真实使用这些结构。
- 保留 ： parseSupportedActions()
  - 原因：旧表单仍需要从 CustomAttributeDefinitionEntity.supportedActions 解析支持的动作开关。
- 删除 ： parseDefaultActions() 、 toSupportedActionsJson() 、 toDefaultActionsJson()
  - 原因：当前代码里没有引用，且它们服务的是上一轮“价格属性自带动作配置”的旧思路。
- 删除 ： createBuiltInPurchasePriceDefinition() 、 createBuiltInTotalPriceDefinition() 、 createBuiltInSubscriptionPriceDefinition()
  - 原因：当前代码无引用，而且这类“内建价格模板工厂”已经不符合现在的“属性模型 + 规则模型 + 模板模型”分层。
同步调整

- 已更新 ItemFormComponents.kt 的导入和调用，从旧的 PriceAttributeHelper 切到新的 LegacyPriceFieldSupport 。
- 新的兼容文件已明确标注为“旧表单兼容层”，避免后续继续把“动作/周期能力”当成正式属性模型扩写。
我对当前 data/model 的判断

- 现在 data/model 里真正和旧属性设计强相关的，主要就是原来的 PriceAttributeModels.kt ，这次已经做了收缩和归位。
- 更深层的旧设计其实还在 CustomAttributeDefinitionEntity.kt 和围绕它的表单/仓储逻辑里，比如：
  - TYPE_PRICE
  - priceRole
  - supportedActions
  - supportsRecurrence
  - defaultActions
- 这些我这次没有直接动，因为你这次点名的是 data/model ，而且它们已经牵连到表单、仓储、数据库迁移。

## 20260623 14:57 第三轮开发结果
### 效果
已整理

- 已把原来的 UI 模型文件迁移为 AttributeManagementUiModels.kt ，目录现在是 ui/attribute/model 。
- 已删除旧文件 ui/attribute/AttributeManagementModels.kt ，避免同类模型继续散落在页面目录根下。
- 已同步更新 AttributeManagementScreen.kt 和 AttributeManagementViewModel.kt 的引用。
新增业务模型目录

- 已新增 data/model/attribute 目录，并补了第一版正式业务模型：
- AttributeValueModels.kt
- RuleBinding.kt
- AttributeDefinition.kt
- RuleDefinition.kt
- AttributeTemplate.kt
- RuleTemplate.kt
现在的分层

- ui/attribute/
  - 放 Fragment 、 Screen 、 ViewModel
- ui/attribute/model/
  - 放 UiState 、 UiModel 、 RouteState 、筛选状态
- data/model/attribute/
  - 放正式业务模型：属性定义、规则定义、模板、规则绑定、共享枚举
 

## 20260623 14:31 第二轮开发结果
### 效果
已继续推进

- 已把属性管理页从“只有列表”推进到“列表 + 二级页”结构。
- 已补上内部路由状态和详情模型，核心在 AttributeManagementModels.kt 。
- 已扩展 AttributeManagementViewModel.kt ，让它支持：
  - 打开属性详情
  - 打开模板详情
  - 打开新建属性页
  - 从模板创建属性页
  - 页面内返回
  - 模拟保存
- 已重构 AttributeManagementScreen.kt ，现在页面内已经有：
  - 属性详情页
  - 模板详情页
  - 新建属性页
  - 从模板创建属性页
- 已同步更新 AttributeFragment.kt ，把新的页面事件和 ViewModel 接口全部接上。
这轮落下来的结构

- AttributeManagementRouteState
  - 统一承接列表页和二级页切换。
- AttributeDetailUiModel
  - 承接属性详情里的字段定义、规则绑定、依赖补齐说明。
- TemplateDetailUiModel
  - 分成属性模板详情和规则模板详情两类。
- CreateAttributeFromTemplateUiModel
  - 承接从模板创建属性页的默认值和可编辑提示。
当前页面效果

- 列表页点击属性卡片，会进入属性详情页。
- 列表页点击模板卡片，会进入模板详情页。
- 模板详情页里如果是属性模板，可以继续进入“从模板创建属性页”。
- 底部主操作现在不再只是提示，而是会进入：
  - 新建属性页
  - 从模板创建属性页

### 提出问题
1. 属性、规则、模板的模型都有了么，放到当前目录是不是不好

## 20260623 第一轮开发结果
### 效果
已推进

- 已新增属性管理页基础代码骨架，核心文件是 AttributeManagementModels.kt 、 AttributeManagementViewModel.kt 、 AttributeManagementScreen.kt 、 AttributeFragment.kt 。
- 页面已按前面文档拆成双视图骨架： 属性 / 模板 ，支持搜索、基础筛选、空状态、规则说明弹窗、自定义属性删除确认。
- ViewModel 里先接了样例数据和本地筛选逻辑，保证现在就能看到完整页面结构，不需要先等仓储层落地。
入口接线

- 已把“我的”页入口接上，在 ProfileViewModel.kt 中加入 属性管理 菜单项。
- 已在 ProfileFragment.kt 中补上点击跳转逻辑。
- 已在 mobile_navigation.xml 中新增 action_profile_to_attribute_management 和 nav_attribute 目的地。
当前页面能力

- 属性视图 ：展示属性来源、值类型、取值方式、多值状态、模板来源、规则摘要、使用数量。
- 模板视图 ：混合展示属性模板和规则模板，通过模板类型、摘要和计数区分。
- 交互 ：支持切换 tab、搜索、循环筛选、查看规则/模板摘要、删除自定义属性、底部主操作提示。
- 样例数据 ：已经覆盖系统属性、自定义属性、属性模板、规则模板四类典型对象。
