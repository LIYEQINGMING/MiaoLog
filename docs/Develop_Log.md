# 总体开发日志
## 20260722 16:37 规则管理页体验升级：新建/编辑规则切到变量工作台 + 公式编辑器
已完成

- 规则管理页的 `RuleEditor` 入口已经切到新的 `RuleWorkbenchPage`，不再以“槽位表单堆叠 + 单个表达式输入框”为主视图，而是改成“基本信息 -> 变量管理 -> 公式编辑器 -> 保存”的编辑流程，见 `AttributeManagementScreen.kt` 。
- 变量管理区补了快捷建变量入口，支持直接创建 `属性输入 / 配置输入 / 系统输入 / 结果变量` 四类变量；同时把变量卡片改成“摘要 + 展开编辑”的工作台样式，默认先展示角色、来源、值类型和更新方式，只有展开后才进入细项配置，降低了用户第一次建规则时需要同时理解的底层概念密度。
- 公式编辑区新增了可插入式公式体验：表达式输入框上方现在会先展示实时公式预览，下方再提供 `快捷片段 / 变量引用 / 函数模板 / 规则预设` 四组插入芯片，点击后会按当前光标位置插入到表达式中，不再只能手敲整段公式。
- 同页新增了一块“公式解码”区域，会即时解析当前表达式里的变量引用、函数调用、未使用变量和未定义变量引用，帮助用户在保存前快速知道“这条规则现在到底搭成了什么样”，见 `AttributeManagementScreen.kt` 。

本次收口结果

- 规则页的用户心智已经从“编辑 slot 元数据”切到了“先搭变量，再搭公式”，更贴近你前面要的可视化公式编辑/解码器方向。
- 这轮仍然复用了现有 `RuleEditorDraftUiModel / RuleDefinition / saveRuleDraft()` 持久化链路，没有再翻规则主模型，因此和前面已经打通的“属性系统 -> 规则系统 -> 物品运行时”链路保持兼容。
- 本地验证继续推进后，当前仍然卡在 Gradle 生成依赖访问器阶段的 `GeneratedClassCompilationException`，根因还是 `AccessDeniedException(gradle-core-8.11.1.jar)`，尚未进入业务 Kotlin 代码编译阶段。

## 20260722 15:41 物品主模型 + 属性系统 + 规则系统链路收口：规则解析切到属性定义优先
已完成

- 规则解析层新增了 `selectedAttributeIds` 维度，属性选择展开、删除依赖校验、规则输出投影三条核心链路现在都会优先按 `attributeId` 命中节点，再回退到旧的字段名 / 属性名 / 属性 key 兼容路径，见 `AttributeRuleResolution.kt` 。
- 规则输出目标现在会把 `RuleSlotBinding.AttributeOutput.attributeId` 透传到运行时投影，`AttributeRuleOutputProjection` 也补上了 `targetAttributeId`，这样规则把结果写回目标属性时，不再只能靠 `targetFieldName` 做字符串反查，见 `RuleSystemModels.kt` 、 `AttributeRuleResolution.kt` 、 `ItemFormComponents.kt` 。
- 新增页 / 编辑页的规则相关消费链路已经同步接入这套定义驱动解析：只读派生字段、函数区规则输出、依赖删除阻塞、运行时自动写回都会把“当前已选属性 id 集合”传入规则解析层，见 `AddItemScreen.kt` 、 `EditItemScreen.kt` 、 `EditItemViewModel.kt` 。
- 为了让本地离线验证能尽量走通，本轮还给 `settings.gradle.kts` 补了 foojay 插件的模块映射，并在工作区内落了一份最小本地 Maven 仓库，避免验证阶段继续被外网插件解析直接卡死。

本次收口结果

- 现在“物品主模型 -> 属性选择态 -> 规则依赖展开 -> 规则输出投影 -> 运行时写回”这条主链路里，自定义属性已经整体切到“定义 id 优先、字符串兼容兜底”的模式。
- 旧字符串耦合没有一次性粗暴删光，但已经被压缩到兼容层；后续如果要继续清理，可以逐步删除旧的 `fieldNameProvider` 兼容入口，而不需要再反复重做页面逻辑。
- 本地验证已继续推进到 Gradle 生成版本目录访问器阶段；当前阻塞已从插件解析转为 Gradle/Windows 的 `GeneratedClassCompilationException + AccessDeniedException(gradle-core-8.11.1.jar)` 文件锁问题，尚未进入业务 Kotlin 代码编译失败阶段。

## 20260722 15:27 物品主模型 + 属性系统链路收口：表单字段切到稳定 attributeId 引用
已完成

- `Field` 结构补上了稳定引用信息，新增 `customAttributeId / ruleOutputDefinitionId / ruleOutputKey`，让表单态开始具备“显示名之外的可追踪身份”，见 `Field.kt` 。
- `ItemFormComponents.kt` 新增一组字段补全 helper：统一负责从 `fieldName + definitions` 生成带稳定引用的字段、给旧缓存字段补全引用、以及从 `selectedFields` 反查当前真正选中的属性定义，不再只能靠 `field.name` 做字符串比对。
- 新增页和编辑页都改成优先根据 `selectedFields` 里的 `customAttributeId` 计算“已选自定义属性”和“还可补充的属性”，只有老缓存/老字段对象没有 id 时才回退到字段名匹配，见 `AddItemScreen.kt` 和 `EditItemScreen.kt` 。
- 模板应用、新增页 definitions 注册、编辑页现有物品属性回填这几条入口也已经开始补写稳定引用，确保模板预选、缓存恢复、编辑回显三条链路都能逐步迁到“定义驱动”，见 `AddItemFragment.kt` 、 `AddItemViewModel.kt` 、 `EditItemViewModel.kt` 。

本次收口结果

- 自定义属性的“是否已选 / 是否可再添加 / 属于哪个定义”开始脱离显示名字符串，后续就算字段展示文案变化，也不会立刻把表单选择态和函数区联动拖坏。
- 这一轮还没有重写规则运行时内部的 `fieldNameProvider` 机制，但已经把最外层表单选择和属性识别切到稳定引用优先，为下一阶段继续压缩字符串耦合打好了底。
- 本地编译验证尝试已执行；当前被 `settings.gradle.kts` 中未缓存的 `org.gradle.toolchains.foojay-resolver-convention:0.10.0` 插件解析阻塞，尚未进入业务代码编译阶段。

## 20260722 15:01 物品主模型 + 属性系统链路收口：统一自定义属性值编解码
已完成

- 新增一层统一的自定义属性值 codec，正式接管 `AttributeDefinitionEntity + fieldValues + itemId -> ItemCustomAttributeEntity` 的转换，同时补了反向回填表单值的能力，见 `ItemCustomAttributeCodec.kt` 。
- 新增页保存自定义属性时不再继续走本地手写的 `when (rawType)` 分支，而是改为调用统一 codec；这样后续值类型扩展时，只需要维护一套保存链路，见 `AddItemViewModel.kt` 。
- 编辑页保存链路同样切到统一 codec，并把“数据库值 -> 表单字段”的回填也改成复用同一套解码逻辑，减少新增页 / 编辑页在数值、日期、布尔、价格货币上的漂移风险，见 `EditItemViewModel.kt` 。
- 新增页在加载属性定义目录时，会同步把 definitions 注册给 ViewModel；编辑页在 `bindAttributeDefinitions()` 时也会同步建立 definitionId -> definition 的稳定映射，为后续继续从“字段显示名驱动”迁到“定义驱动”做准备，见 `AddItemFragment.kt` 和 `EditItemViewModel.kt` 。

本次收口结果

- 自定义属性值的保存和回填不再由新增页、编辑页各自维护两套实现。
- 当前仍保留旧的本地实现作为过渡，但主保存入口已经切到统一 codec，后续可以继续安全删除旧分支。
- 这一步还没有重做表单状态模型，但已经先把最容易漂移、最影响后续规则链路稳定性的“属性值编解码”统一起来。

## 20260709 09:00 规则V1落地：属性和规则关联，必填属性
已收口

- 规则编辑页已经不再允许把 ATTRIBUTE_INPUT 设成可选，切到该来源后会固定为必填，并在 UI 上显示“属性输入固定必填”，见 AttributeManagementScreen.kt 。
- 保存约束也已经同步收口：规则草稿在 sanitizeRuleSlotDrafts() 和 toRuleSlotDefinition() 两层都会把 ATTRIBUTE_INPUT 规范成 isRequired = true ，见 AttributeManagementViewModel.kt 。
- 系统规则模板里原先遗留的几个“可选属性输入”也已经改成必填，只保留系统输入按自身语义控制，见 AttributeRepository.kt 。
迁移处理

- 开发期数据迁移已经接到仓储初始化里：进入属性管理/物品添加/物品编辑时，会把当前库里的属性绑定和规则定义统一重写成新结构，见 AttributeRepository.kt 。
- 旧 ruleBindingsJson 的 fallback 仍保留了最小读取能力，但迁移后会把所有旧 optionalDependencies 直接并入新的必填属性输入槽位，并统一归一化为新模型，见 AttributeRepository.kt 。
- 旧规则定义读取时，也会把老的 requiredDependencies/optionalDependencies 全部折叠成新的 slots 结构，并强制属性输入为必填，见 AttributeRepository.kt 。

已修复

- 补回了 RuleDefinition 的扩展函数导入，解决 inputSlots / outputSlots 大量 Unresolved reference ，见 AttributeManagementViewModel.kt 。
- 把仓储迁移里的 forEach(attributeDefinitionDao::insert) / forEach(ruleDefinitionDao::insert) 改成显式 lambda，修掉 suspend 函数引用类型不匹配，见 AttributeRepository.kt 。
- 给绑定向导相关 FlowRow 使用点补上 ExperimentalLayoutApi ，并在页面里补了本地版 isRuleBindingSlotDraftSatisfied() ，修掉实验 API 和未解析引用，见 AttributeManagementScreen.kt 和 AttributeManagementScreen.kt 。
收口结果

- 规则编辑页已经固定 ATTRIBUTE_INPUT 为必填，不再允许切成可选，见 AttributeManagementScreen.kt 。
- 保存约束也同步强制 ATTRIBUTE_INPUT -> isRequired = true ，见 AttributeManagementViewModel.kt 。
- 开发期迁移已接入仓储初始化，会把当前库里的规则/绑定统一重写为新结构，减少旧兼容长期存在，见 AttributeRepository.kt 和 AttributeRepository.kt 。
- 给 RuleSlotEditorCard() 补上了 @OptIn(ExperimentalLayoutApi::class) ，解决了 FlowRow 的最后一个实验性 API 编译报错，见 AttributeManagementScreen.kt 。

## 20260708 14:00 规则V1落地：属性绑定规则、属性快捷创建、规则绑定向导
已完成

- 规则绑定向导现在会显式高亮“当前属性可进入的槽位”，并在规则卡片和入口槽位步骤里给出可占据说明，见 AttributeManagementScreen.kt 。
- “快捷创建属性”已经从纯文本入口接成真实流程：保存绑定时会识别不存在但允许创建的属性名，生成待创建属性并在最终保存属性时一并落库，见 AttributeManagementViewModel.kt 和 AttributeManagementViewModel.kt 。
- 规则模板详情页已经切到槽位语义，改为展示输入槽位、输出槽位、输出策略、默认表达式，不再停留在旧依赖摘要，见 AttributeManagementScreen.kt 和 AttributeManagementViewModel.kt 。
- 绑定实例的编辑/删除提示已补齐：属性编辑页会显示依赖属性、输出影响、快捷创建项，移除绑定时也会弹出影响确认，见 AttributeManagementScreen.kt 。
关键实现

- 新增了绑定高亮与快捷创建所需的 UI 状态字段，包括 isCompatibleWithCurrentAttribute 、 PendingCreatedAttributeUiModel 、 isQuickCreatedAttribute 等，见 AttributeManagementUiModels.kt 。
- 绑定草稿保存前会统一解析槽位：优先匹配已有属性，其次复用已待创建属性，最后在允许时自动生成新属性草稿，见 AttributeManagementViewModel.kt 。
- 自动创建的属性会按槽位值类型推导基础属性定义，例如周期单位默认走 SELECT ，数值槽位默认走 NUMBER ，见 AttributeManagementViewModel.kt 。
- 模板目录模型也同步切到槽位语义，避免模板页和规则详情页继续保留两套心智，见 AttributeManagementViewModel.kt 。

## 20260708 10:40 规则V1落地：把规则详情页和规则列表摘要也换成纯槽位语义，并开始做“规则绑定页”的实际配置 UI
已推进

- 规则列表摘要已切到槽位语义，卡片现在展示触发方式、输入槽位摘要、输出槽位摘要和槽位数量，不再以旧的依赖字段为主，入口在 AttributeManagementScreen.kt 和 AttributeManagementViewModel.kt 。
- 规则详情页已改成纯槽位语义，页面现在拆成输入槽位、输出槽位、输出策略、表达式和绑定属性几个区块，核心实现见 RuleDetailPage 和 toRuleDetail 。
- 规则绑定页的实际配置 UI 已开始落地，并接入属性编辑流程：属性编辑页里现在可以进入“规则绑定向导”，在向导里完成规则选择、入口槽位选择、剩余槽位补齐和保存回属性草稿，代码在 AttributeEditorPage 、 RuleBindingWizardPage 、 openRuleBindingWizard 、 saveRuleBindingWizard 。
- 绑定保存链路已切到真实 slotBindings ，属性保存不再依赖旧的 requiredDependencies / outputs 文本推导，核心逻辑在 saveAttributeDraft 。
- UI 模型已经补齐槽位化结构和绑定向导草稿模型，见 AttributeManagementUiModels.kt ；Fragment 也已接入新的两个回调，见 AttributeFragment.kt 。
关于单表达式

- 已按你的约束继续保持“单条规则只保留一个 expressionDefinition ”。
- 当前实现允许一条规则有多个输出槽位，但设计上仍只有一个逻辑表达式；这和你希望的“用户只理解一条规则逻辑”是一致的。
- 运行时现阶段仍优先对“单表达式可直接覆盖的场景”生效，多输出场景先保留同一逻辑表达式的设计边界，不再往“多表达式”方向扩。

当前边界

- 绑定向导已经是可操作 UI，但“快捷创建属性”目前还是以直接输入属性名作为前端入口，真正自动落库创建属性这一步还没接。
- 绑定向导目前保存后回到属性草稿页，再随属性一起保存；这是有意保持“绑定页使用规则、属性页持有最终草稿”的流程。
- 规则模板详情页还保留部分旧摘要语义，这一块我这轮没动。

## 20260708 10:20 规则V1落地
已完成

- 规则编辑页已改成真正的 V1 槽位编辑器：用“输入槽位 / 输出槽位”卡片替换旧文本框，支持方向、值类型、来源类型、系统变量、输出更新方式和快捷建属性开关编辑，入口在 AttributeManagementScreen.kt 。
- 规则草稿回填和保存已完全对齐槽位模型：新建规则会默认带一个输入槽位和一个输出槽位，保存时校验槽位 key、输入输出完整性、系统变量绑定，并写入 slots + expressionDefinition + outputStrategies ，见 buildRuleEditorDraft 和 saveRuleDraft 。
- 规则更新后的绑定实例同步也补上了：已有属性上的 RuleBindingInstance 会按新槽位结构重建输入/输出绑定，避免规则改完后绑定实例仍残留旧结构，见 syncBindingInstanceWithRule 。
- 运行时已接入表达式执行入口： RuleRuntimeEvaluator 现在会优先尝试执行表达式，支持变量取值、四则、比较、逻辑、 if(cond,a,b) 、 coalesce(...) 、 dateDiff(...) 、 count/paymentCount(...) 、 cycleCount(...) ，入口和解析器在 RuleRuntimeEvaluator.kt 、 evaluateExpressionRule 和 RuleExpressionParser 。
- 为了不打断现有系统规则，当前策略是“表达式优先，旧特例兜底”：单输出规则优先走表达式，多输出模板如“订阅已支付”仍保留旧分支兜底。



## 20260708 10:00 规则V1落地
已完成
- 不再保留独立 V2 并存模型文件，已删除独立的 RuleModelsV2.kt
- 现在是直接在原有 RuleDefinition 、 RuleBinding 、 RuleTemplate 上升成 V1 结构
- Kotlin 编译已通过： ./gradlew :app:compileDebugKotlin -x :app:processDebugResources
这轮完成的核心替换

- 同步总文档到 V1 术语： Attribute_Management_Design.md
- 直接升级规则定义模型： RuleDefinition.kt
- 直接升级规则绑定模型： RuleBinding.kt
- 直接升级规则模板模型： RuleTemplate.kt
- 补齐新规则基础枚举与状态： RuleSystemModels.kt
现在的模型结构

- RuleDefinition 已改为以 slots + expressionDefinition + outputStrategies 为核心
- RuleBinding 已改为 typealias RuleBinding = RuleBindingInstance
- RuleBindingInstance 现在持有：
  - entrySlotKey
  - slotBindings
  - status
  - creationSource
- RuleTemplate 也已改为围绕 slots 和 defaultExpressionDefinition 建模
- 旧的 inputRoles / requiredDependencies / optionalDependencies / outputKeys / systemInputs / outputTargets 现在是从新结构推导出来的兼容访问器，不再是主存储结构
仓储与旧数据处理

- 已把系统内置规则模板和系统规则定义改成原生 V1 结构： AttributeRepository.kt
- 旧库里已有 JSON 数据目前通过仓储转换层读取并转成新模型
- 当前策略是：
  - 业务模型直接替换为新结构
  - Room 旧字段名暂时不动
  - 读写时由仓储负责新旧结构转换
- 这不再是“双模型并存”，而是“单模型替换 + 存储层过渡”
运行时与依赖推导

- 依赖推导已优先从新 slotBindings 取值： AttributeRuleResolution.kt
- 运行时输入解析已开始切到 slotBindings ： RuleRuntimeEvaluator.kt
- 属性管理页保存属性时，生成的绑定对象也已经是新 RuleBindingInstance ： AttributeManagementViewModel.kt
## 20260708 09:40 规则的调整以及文档落成
已完成
- 形成新版的设计文档 Rule_Binding_Page_Design_v1.md
- 已将 V1 的核心结论反向更新到 Attribute_Rule_Model_Draft.md
- 已把规则相关页面先落成 4 份 V1 页面文档，覆盖规则管理、详情、定义编辑、绑定向导
- 已检查编辑器诊断，当前无新增问题
反向更新内容

- 顶层草案已从旧的“规则名 + 输入角色 + 依赖属性”表述，升级为 V1 的统一语义：
- 规则模型收敛为：规则定义、槽位定义、规则绑定实例、规则输出
- 明确规则本质是可执行表达式，绑定对象是“绑定实例”而不是简单规则名引用
- 明确依赖关系从绑定实例推导，不单独建立关系模型
- 明确与物品编辑页、删除属性影响分析、模板体系的联动边界
新增文档

- Rule_Management_Page_Design_v1.md
- Rule_Detail_Page_Design_v1.md
- Rule_Editor_Page_Design_v1.md
- Rule_Binding_Page_Design_v1.md
页面文档范围

- 规则管理页 ：规则列表、搜索、筛选、影响范围、删除治理
- 规则详情页 ：规则定义、槽位定义、表达式、输出策略、绑定范围
- 规则定义编辑页 ：规则母版编辑，重点是槽位、表达式、输出策略
- 规则绑定页 ：从属性出发选择规则、过滤可用槽位、补齐剩余槽位、保存绑定实例

## 20260707 14:20 属性管理页面中的规则
已完成

- 规则 Tab 现在补成了真正可管理的闭环，不再只是列表展示。
- 新增规则筛选与统计：支持按来源、规则类型、绑定状态筛选，并显示系统/自定义/已绑定数量，落在 AttributeManagementUiModels.kt 和 AttributeManagementScreen.kt 。
- 新增规则绑定可视化：规则卡片展示“绑定属性数量”和部分属性名，规则详情页展示完整绑定属性列表，落在 AttributeManagementScreen.kt 和 AttributeManagementScreen.kt 。
- 删除规则改成真实安全删除：弹窗会显示受影响属性；确认后先从这些属性里移除绑定，再删除规则本身，避免残留脏绑定，逻辑在 AttributeManagementViewModel.kt 。
- 编辑规则后会同步刷新已绑定属性里的绑定快照字段，避免规则定义变了但属性还保留旧依赖/旧输出，逻辑在 AttributeManagementViewModel.kt 。
- 页面接线已补齐， Fragment -> Screen -> ViewModel 的规则筛选和管理动作都已接通，见 AttributeFragment.kt 。

规则逻辑
- 规则列表数据现在会计算真实“被哪些属性绑定”，并据此生成筛选、统计、删除提示和详情信息，见 AttributeManagementViewModel.kt 。
- 删除规则时不只是 deleteRule() ，还会批量更新受影响属性的 ruleBindings ，避免后续表单解析时引用到已不存在的规则。
- 保存规则时会同步更新所有已绑定属性中的 requiredDependencies 、 optionalDependencies 、 outputKeys ，保证属性侧绑定元数据与规则定义一致。
- 系统规则仍保持只读，不允许编辑和删除；自定义规则支持新建、编辑、删除、筛选和查看绑定范围。

## 20260707 13:51 规则的实现第二阶段
已完成
- 新增页已接入规则重算触发，字段变化后会自动执行 itemApplyRuleRuntimeOutputs() ，并计算派生只读字段集合，见 AddItemScreen
- 编辑页也接入了同样的规则重算与派生只读逻辑，保证新增/编辑行为一致，见 EditItemScreen
- 补充属性区现在会把规则输出到 ATTRIBUTE_VALUE 的目标字段作为只读派生字段展示，并锁定删除入口，避免用户手改覆盖规则结果，见 AddItemScreen 和 EditItemScreen
- 表单组件层已经具备派生字段只读展示和规则运行时写回能力，包括 ATTRIBUTE_VALUE 写回属性、 SYSTEM_VARIABLE 写回运行时系统变量，见 ItemCustomAttributeField 、 itemResolveDerivedAttributeFieldNames 、 itemApplyRuleRuntimeOutputs
- BaseItemViewModel 已承接规则运行时状态批量写入，支持字段和值班系统变量一并更新，见 applyRuleRuntimeState

当前效果
- 添加一个带规则的属性后，不仅会自动补齐依赖输入，还会把规则目标属性字段自动带进表单
- 当输入值变化时，规则结果会真正落到目标属性字段或运行时系统变量，而不再只是显示只读卡片
- 被规则驱动的目标属性会以“派生结果”方式展示，用户不能直接编辑，避免和规则冲突
## 20260703 10:00 规则的实现
已完成
- 规则定义模型已正式扩成可承载 触发方式 / 系统输入 / 输出目标 ，不再只是旧的输入角色和输出键，见 RuleDefinition.kt 、 RuleTemplate.kt 、 RuleSystemModels.kt 、 RuleSystemModels.kt 。
- Room 持久化和迁移已补上， rule_definitions 现在会保存这些新结构，并新增了 61 -> 62 迁移，见 RuleDefinitionEntity.kt 、 AttributeRepository.kt 、 AppDatabase.kt 、 AppDatabase.kt 。
- 系统规则模板也同步升级了，现在模板本身就自带触发方式、系统输入和输出目标，不会在“从模板创建规则”时丢配置，见 AttributeRepository.kt 。
- 规则管理页编辑草稿和保存逻辑已接上新结构，支持配置多触发方式、系统输入映射、输出目标映射，并在保存时校验角色和输出是否一致，见 AttributeManagementUiModels.kt 、 AttributeManagementUiModels.kt 、 AttributeManagementViewModel.kt 、 AttributeManagementViewModel.kt 、 AttributeManagementViewModel.kt 。
- 规则详情页、规则模板详情页、规则编辑页都已展示并编辑这些新能力，见 AttributeManagementScreen.kt 、 AttributeManagementScreen.kt 、 AttributeManagementScreen.kt 。
- 规则详情生成已切到“优先读规则定义自身配置，缺失时再回退到内置运行时元数据”，这样系统规则和后续自定义规则都能共用一套展示与执行入口，见 AttributeManagementViewModel.kt 和 RuleRuntimeEvaluator.kt 。

## 20260702 15:53 物品页的加属性的规则关联
已接上
- 新增了“按最新规则定义补齐依赖”的解析入口， 规则依赖自动补齐 、 删除依赖保护 、 只读结果输出投影 现在都支持传入当前规则列表，不再只依赖属性里那份旧绑定快照，见 AttributeRuleResolution.kt 。
- 物品页的加属性链路已经按你说的流程走通：确认添加某个属性后，会立刻检查该属性绑定规则的必需输入，并把缺失属性一并加入表单，见 ItemFormComponents.kt 。
- 新增页已接这套逻辑，包括添加属性、删除属性阻拦、只读规则输出展示，见 AddItemScreen.kt 、 AddItemScreen.kt 、 AddItemScreen.kt 、 AddItemScreen.kt 。
- 编辑页也同步接上了同一套逻辑，所以“编辑已有物品时再加一个带多输入规则的属性”也会自动补齐缺失输入，见 EditItemScreen.kt 、 EditItemScreen.kt 、 EditItemScreen.kt 、 EditItemScreen.kt 。
- 编辑态恢复也改成按最新规则补齐，避免已有物品打开时漏掉多输入规则依赖，见 EditItemViewModel.kt 。
- 模板应用入口也统一了，所以从模板带入属性时，如果其中某个属性绑定了多输入规则，也会自动把依赖属性一起展开，见 AddItemFragment.kt 和 AddItemFragment.kt 。
- 属性管理页里的依赖提示也开始按当前规则定义生成，和物品页行为保持一致，见 AttributeManagementViewModel.kt 和 AttributeManagementViewModel.kt 。

当前行为
- 流程现在是：添加属性 -> 读取该属性绑定规则 -> 检查规则必需输入 -> 自动把缺失依赖属性加入补充信息区 -> 规则只读输出同步出现在功能区。
- 如果用户尝试删除一个“被其他已选规则属性依赖”的输入字段，页面会继续阻止删除并提示依赖来源。

## 20260701 16:28 规则的实现
### 开发结果
本轮进展

- 我先把规则从“页面内硬编码”推进成了统一运行层：新增了 RuleRuntimeEvaluator.kt ，现在会基于 规则定义 + 规则绑定 + 系统输入元数据 统一解析输入角色并计算输出。
- 现有物品页里的只读规则结果已经切到这套执行器，不再分别在页面里手写 待付尾款 / 平均价值 逻辑，见 ItemFormComponents.kt:L1534-L1588 和 ItemFormComponents.kt:L2657-L2707 。
- 规则仓储补了 ensureSystemRules() ，这样系统规则不再只靠属性管理页顺带播种，新增页、编辑页也能稳定拿到完整规则定义，见 AttributeRepository.kt:L572-L606 。
- 新增页和编辑页现在都会加载规则定义并传入表单运行层，见 AddItemFragment.kt:L76-L103 、 AddItemFragment.kt:L519-L524 、 EditItemFragment.kt:L58-L76 、 EditItemFragment.kt:L167-L172 。
- 规则详情页也补上了运行时信息展示，能直接看到系统输入和输出目标，而不只是抽象的输入角色/输出键，见 AttributeManagementUiModels.kt:L197-L214 、 AttributeManagementViewModel.kt:L833-L876 、 AttributeManagementScreen.kt:L1293-L1324 。

## 20260701 16:00 系统级变量接入到属性的系统源
### 开发结果
已完成

- 属性编辑草稿已补上系统变量目录与当前选中键，属性管理页现在有正式的“系统来源”承接结构了，见 AttributeManagementUiModels.kt:L258-L289 。
- 属性管理 ViewModel 已接入 AppSystemSourceRepository ，会加载这批已实现/降级系统变量，并在保存时校验“必须已选系统变量、值类型必须一致、降级变量不可绑定”，同时把绑定键写入 valueProperties.systemVariableKey ，见 AttributeManagementViewModel.kt:L65-L82 、 AttributeManagementViewModel.kt:L319-L355 、 AttributeManagementViewModel.kt:L505-L540 、 AttributeManagementViewModel.kt:L911-L958 、 AttributeManagementViewModel.kt:L1082-L1137 。
- 属性编辑页已开放 值来源 = 系统 ，并且会按当前值类型展示可绑定系统变量；降级变量会显示原因，但不会作为正常可选项，见 AttributeManagementScreen.kt:L1449-L1780 与 AttributeManagementScreen.kt:L1920-L1970 。
- 属性详情和列表摘要现在会带出系统变量名、状态和绑定说明，所以系统来源不再只是“存了个 key”，见 AttributeManagementViewModel.kt:L1016-L1079 。
- 属性管理页入口依赖也已补通， Factory 和 Fragment 会把系统变量仓储注入进去，见 AttributeManagementViewModelFactory.kt 和 AttributeFragment.kt:L18-L23 。

## 20260701 15:36 实现部分系统级变量
### 开发结果
已实现

- 按“最佳实践 + 当前代码可行性”先落了一批 稳定可读 的系统级变量，并把“是否真实可用”编码进模型，见 RuleSystemModels.kt 。
- 新增了系统级变量实现状态 IMPLEMENTED / DEGRADED / PLANNED ，同时给系统变量快照补了 isAvailable 和 availabilityMessage ，后面属性页和规则页可以直接复用这层状态。
- 真实接通的内置变量有：
  - 账户创建时间
  - 已加入 App 时长
  - 当前日期
  - 当前时间
  - 统一日历时间
  - 当前时区
  - 当前用户标识 见 AppSystemSourceRepository.kt 。
- 真实接通的业务变量有：
  - 全局总价值
  - 全局总数量 见 UnifiedItemRepository.kt 。
- 当前定位 这次没有再做伪实现，而是明确标记为 降级可用 ：变量定义保留，但当前值不可用，并返回原因说明，避免文档说支持、实际拿到的是空壳。

## 20260701 15:06 调整系统级变量
### 开发结果
已整理

已把“系统级变量分层”正式写进 System_Variable_Model_Draft.md。
分层已经明确为：
内置变量
业务变量
规则衍生变量
也同步在 Develop_Log.md 补了一条本次收敛记录。
文档里现在的结构

在 System_Variable_Model_Draft.md 里，我把原来单一的“当前建议的内置系统级变量”升级成了三层建议：
10.1 内置变量
10.2 业务变量
10.3 规则衍生变量
同时新增了一个更落地的推进建议：
11.1 第一批先做
11.2 第二批再做
11.3 第三批按规则逐步沉淀
当前收敛结果

内置变量
承接稳定基础上下文
例如：账户创建时间、已加入 App 时长、当前日期、当前时间、统一日历时间、当前定位
业务变量
承接明确业务口径
例如：全局总价值、全局总数量、当前订阅已支付金额、当前订阅已支付次数、下一次提醒时间
规则衍生变量

## 20260701 16:00 系统级变量可实现范围
### 开发结果
已完成

- 结合最佳实践与当前代码现状，已收敛“先实现确定性强、只读、来源清晰”的系统级变量。
- 当前已落地或可稳定提供真实值的变量包括：账户创建时间、已加入 App 时长、当前日期、当前时间、统一日历时间、当前时区、当前用户标识、全局总价值、全局总数量。
- 当前定位已保留为系统级变量，但明确标记为依赖权限与运行态上下文，当前先不承诺完整能力。
- 规则衍生变量暂不提前做成伪全局变量，仍先保留在规则输出层。
- 已同步补充 `System_Variable_Model_Draft.md` 中“当前可先实现的变量”章节。
承接规则输出沉淀
例如：平均价值、待付尾款、周期状态 

## 20260701 11:26 
### 疑问和建议
一些疑问和建议：
1. 不太明白你刚刚做的开发和我们刚刚说的有什么关联，这里其实涉及到系统变量、属性、规则这三者，而系统变量是属性值来源和规则的输入输出的可选源之一而已。
2. 我觉得应该是系统级变量，然后这些变量有一些可以调整的配置，这些配置可以在app的设置中进行调整
3. 属性的值来源为系统来源和规则的输入输出中的系统来源，来源于系统变量

## 20260701 11:45 收敛系统级变量定义
### 开发结果
已完成

- 新增 `System_Variable_Model_Draft.md`，把系统级变量单独抽成一层模型，明确它是 App 内部可被全局读取、部分可被规则写入的全局状态。
- 明确系统级变量不等于设置项；设置项是系统级变量的配置承接层，而不是与系统级变量平级的值来源。
- 明确属性的 `系统来源` 与规则输入输出中的 `系统来源`，本质上都应指向同一套系统级变量。
- 第一批建议的内置系统级变量包括：账户创建时间、已加入 App 时长、当前日期、当前时间、统一日历时间、当前定位、全局总价值。

## 20260701 12:05 系统级变量分层
### 开发结果
已完成

- 已将系统级变量分为三层：`内置变量 / 业务变量 / 规则衍生变量`。
- `内置变量` 主要承接稳定的基础上下文，例如账户创建时间、已加入 App 时长、当前日期、当前时间、统一日历时间、当前定位。
- `业务变量` 主要承接项目业务口径明确的系统变量，例如全局总价值、全局总数量、当前订阅已支付金额、当前订阅已支付次数、下一次提醒时间。
- `规则衍生变量` 主要承接规则输出沉淀下来的变量，例如平均价值、待付尾款、周期状态。
- 这套分层已经整理进 `System_Variable_Model_Draft.md`，后续属性系统、规则系统和设置承接都按这个分层推进。

## 20260701 11:21 系统级变量、系统设置
### 开发结果
已完成

- 新增了系统来源基础模型 RuleSystemModels.kt ，正式把这些概念落成代码：
  - 系统设置 ：如 DEFAULT_CURRENCY
  - 系统级变量 ：如 GLOBAL_TOTAL_VALUE 、 CURRENT_DATE
  - 规则输入来源类型
  - 规则输出目标类型
- 新增了系统来源仓储 AppSystemSourceRepository.kt ，现在可以统一读取：
  - 默认币种
  - 当前日期/时间/统一日历时间
  - 全局总价值
- 在 UnifiedItemRepository.kt 补了最小闭环能力：
  - 读取当前基准币种
  - 保存基准币种
  - 产出 全局总价值 的系统变量快照
- 规则解析层现在不再只认识“字符串输出名”，而是会识别输出到底是：
  - 只读结果
  - 系统变量写入 见 AttributeRuleResolution.kt
- 计入总价 已被明确收口为写入 GLOBAL_TOTAL_VALUE ，不再和只读输出混在一起；物品页文案也同步改成了“计入全局总价值” ItemFormComponents.kt 、 ItemFormComponents.kt
- 设置层已经接上这套系统来源仓储， AppSettingsViewModel 现在具备默认币种与系统来源快照的读写入口 AppSettingsViewModel.kt

## 20260701 10:10 补充系统来源文档定义
已补充

- 在 `Attribute_Modeling_Spec.md` 中明确：属性值来源里的 `来自系统`，指的不是模糊的“系统自动值”，而是 App 的系统级来源，包含 `系统设置` 与 `系统级变量` 两层。
- 在 `Attribute_Rule_Model_Draft.md` 中补充：规则输入可以来自属性值，也可以来自系统来源；规则输出除只读结果外，后续还应支持写入系统级变量。
- 在 `Attribute_Template_Spec.md` 中同步：属性模板与规则模板中的系统来源，也统一指向 App 的系统设置与系统级变量，而不是某个具体物品自己的属性。
- 这样后续实现时，属性的系统来源、规则的系统来源输入、规则写入全局变量三者会共用同一套语义边界。

## 20260701 09:20 规则输出落位
已落位

- 规则输出现在有正式承接层了：新增 AttributeRuleOutputProjection ，把“已选属性 -> 规则输出”映射成可落到表单的输出项 AttributeRuleResolution.kt 。
- 物品页功能区现在会自动显示规则输出，不再只停留在 outputKeys 元数据里；新增/编辑页都已接上 AddItemScreen.kt 、 EditItemScreen.kt 。
- 功能区新增了只读结果卡片 ItemReadonlyRuleOutputCard() ，用来承接第一阶段“只读输出” ItemFormComponents.kt 。
- 当前已落地的输出规则：
  - 计入总价 ：继续收口到已有“计入总价值”开关，不单独生成新输入框。
  - 待付尾款 ：在功能区显示只读结果，按 总价 - 当前属性金额 计算。
  - 平均价值 ：在功能区显示只读结果，按 当前属性金额 / 时间跨度 计算，默认按“天”展示。
    见 ItemFormComponents.kt 。
- 保存层已排除 custom_rule_output_* ，不会把只读规则输出误存成真实属性值 AddItemViewModel.kt 、 EditItemViewModel.kt 。
当前效果

- 当属性绑定了 平均价值 或 待付尾款 这类输出后，用户在新增/编辑物品时，功能区能直接看到只读结果卡片。
- 输出不会被当成新的可填写属性，也不会参与自动补齐输入，符合我们前面定的“第一阶段只做只读输出”的边界。
- 价格类规则的“计入总价”继续复用现有的动作开关，不和只读输出混在一起。


## 20260701 09:20 属性的规则相关配置开发
### 开发结果
- 新增一层通用规则解析器 AttributeRuleResolution.kt ，现在能统一做 3 件事：解析属性绑定规则、展开必需依赖字段、判断某个字段是否仍被其他已选属性依赖。
- 属性详情页已经接上“依赖补齐说明”， dependencyHints 不再是空值，来源于真实规则绑定解析 AttributeManagementViewModel.kt 。
- 新增物品页在用户添加属性时，会自动把规则要求的必需依赖属性一起补进表单；删除时如果该字段仍被其他已选属性依赖，会直接拦住并提示原因 ItemFormComponents.kt 、 AddItemScreen.kt 。
- 编辑物品页也接上了同样的依赖补齐与删除保护，同时在绑定已有属性值时会自动把缺失的依赖字段补出来 EditItemScreen.kt 、 EditItemViewModel.kt 。
- 模板应用时也会自动展开规则依赖，不再只加载模板里显式点名的属性，这样后面做旧属性迁移时可以直接复用这层展开能力 AddItemFragment.kt 。

## 20260629 17:00 调整新增属性
### 建议
针对刚刚的开发结果，对于新增属性页面有一些建议。
1. 顶部的返回按钮以及新建属性的标题上方到app顶部边界的边距小一些，现在呈现出一个“大额头“的情况。
2. 对于值来源，我们先仅保留输入和固定值选项，系统来源的数据我们留到未来再做。
3. 交互方式，我觉得这个属性应该不太需要开放给用户。因为用户添加一个文本属性、一个价格属性、一个日期属性，一个布尔属性，其实都是需要用户输入。唯一表现不同的只不过是UI的交互方式。我觉得这部分的设置可以统一到值属性的那部分。比如布尔类型的按钮输入或者是输入框输入的选择，我们可以在值属性中进行配置。又比如选择类型的单选或者多选，也可以在值属性中进行配置。

这样就新增属性的逻辑就变成了，值类型（五类） → 值来源（两个源）→值属性配置 →规则绑定 
4. 属性不需要图标
5. 文本类型的值属性中不需要单行/多行这样的配置，准确的来说文本类型我们暂时不给他配置值属性配置。新增属性-文本值类型，应该就是配置值来源、规则绑定。
6.数值类型的值属性中的数值格式配置，价格和带单位应该是同一个，价格是带单位下的一种。调整为“量值”。下属有单位类型、具体单位。配置单位类型，里面有价格、面积、长度、什么什么的，选中后显示具体单位配置下拉框（输入可检索），选择当前属性的单位值。
7. 日期类型的属性，仅需要配置值来源即可。日期类型的值的输入方式就是一个输入框，点击后弹出日期选择器，日期选择器中可以选择或者输入日期。所以日期类型其实本质是一个文本类型。
8. 布尔类型的属性，值属性配置这里，就要有交互方式的配置了，交互方式可以是按钮、输入。
9. 选择类型的属性，值属性配置里应该要支持单选/多选的配置；值属性配置里还可以配置此属性的选项。
10. 优化一下此页面的视觉效果和交互体验。可以找找最佳实践来帮助改进。
### 开发结果
已完成
- 已按 Develop_Log.md 这组建议继续收敛“新建属性”页，核心流程改成： 值类型 -> 值来源 -> 值属性配置 -> 规则绑定
- 已收紧二级页顶部留白，减轻返回按钮和标题上方的“大额头”感
- 已把值来源限制为 输入 和 固定值 ，创建页不再开放 系统 来源
- 已移除属性图标编辑入口，属性图标改为按值类型自动给默认图标，不再让用户配置
- 已去掉独立的“交互方式”总入口，改为收进各自值类型的值属性配置里

类型调整
- 文本 ：不再提供单行/多行、占位提示、最大长度等值属性配置
- 日期 ：仅保留值来源配置，页面文案也明确为统一日期输入/日期选择器
- 布尔 ：在值属性配置中支持 按钮 / 输入 两种交互方式，并保留真值/假值文案
- 选择 ：在值属性配置中支持 单选 / 多选 ，并直接配置选项列表
- 数值 ：把原来的“价格/带单位”收敛成 量值 口径，新增 单位类型 和 具体单位 ，并补了推荐单位类型与常用单位快捷选择
数据同步

- 保存逻辑已同步更新：固定值现在必须填写，选择类型必须配置选项，量值必须配置单位类型和具体单位
- 列表页/详情页/模板详情里的值属性摘要已同步切换到新口径，不再单独强调旧的交互方式行
- 新增了 unitCategory 到值属性模型，后续可以继续在这个基础上做更完整的单位选择器
涉及文件

- AttributeManagementScreen.kt
- AttributeManagementViewModel.kt
- AttributeManagementUiModels.kt
- AttributeValueModels.kt

## 20260629 15:55 根据属性开发步骤进行开发
### 开发结果
已完成

- 属性管理页的 UI 草稿模型已经切到新结构，编辑草稿和详情定义现在都围绕 值类型 / 值来源 / 交互方式 / 值属性 展开，见 AttributeManagementUiModels.kt 、 AttributeValueDefinitionUiModel 、 AttributeEditorDraftUiModel
- 属性保存、模板回填、详情展示已经改成走新字段，保存时会组装 AttributeValueProperties 、校验选择型属性、并根据配置收敛交互方式，见 saveAttributeDraft 、 buildAttributeEditorDraft 、 buildValueProperties
- 属性管理页编辑器已经改成新流程：先选值类型，再配类型专属值属性，再选值来源和交互方式； TEXT / NUMBER / DATE / BOOLEAN / SELECT 都有对应配置 UI，见 AttributeEditorPage
- 属性列表、属性详情、模板详情的展示文案已经从旧的“选项来源 / 单多值”切到新的“值来源 / 交互方式 / 值属性摘要”，同样在 AttributeManagementScreen.kt 中
- Room 层补了 AttributeValueSource 的 converter，并把数据库版本升到 61 ，新增 60 -> 61 迁移来落 valueSource / interactionMode / valuePropertiesJson ，见 Converters.kt 、 AppDatabase.kt 、 MIGRATION_60_61

## 20260629 14:31 明确开发步骤
- 值类型 还少了“选择”，现在枚举只有 TEXT / NUMBER / DATE / BOOLEAN ，见 AttributeValueModels.kt 。
- 属性定义 还是旧扁平结构： optionSource / inputMode / isMultiValue / optionItems ，还没有正式的“值属性”层，见 AttributeDefinition.kt 。
- Room 实体也还是同一套旧字段，没有把“值属性”独立承接出来，见 AttributeDefinitionEntity.kt 。
- 属性编辑页现在展示的还是“选项来源 / 输入方式 / 单值多值”，还不是你文档里的“值属性 / 值来源 / 交互方式”，见 AttributeManagementScreen.kt 。
- 保存逻辑也还是围绕旧字段在组装模型，没有按新结构校验，见 AttributeManagementViewModel.kt 。
- 你文档里已经明确要求“选择”成为独立值类型、“价格/百分比/单位”属于数值的值属性，这部分现在代码里还没真正落下来，见 Attribute_Modeling_Spec.md 。
正确的开发顺序

- 先改属性模型。
- 再改属性管理页的编辑和展示。
- 再改模板与系统属性播种。
- 然后改新增/编辑物品页对新属性结构的消费。
- 最后才轮到规则执行。

接下来会这样做
- 第 1 步：把属性模型改成新结构，至少补上：
  - 选择 值类型
  - 值来源
  - 值属性
  - 交互方式
- 第 2 步：改 AttributeDefinition 、 AttributeDefinitionEntity 、Repository 映射和数据库迁移。
- 第 3 步：重做属性管理编辑页，让它按“值类型 -> 值属性 -> 值来源 -> 交互方式”联动。
- 第 4 步：把现有系统属性和模板改到新口径，比如“状态”应优先落到“选择”而不是“文本 + 单选”。
- 第 5 步：等属性层稳定后，再继续规则执行。

## 20260629 11:25 调整属性设计
### 设计收敛
1. 属性定义中的“选项来源”重构为“值来源”。
2. 属性定义的核心结构收敛为：属性名、值类型、值属性、值来源、交互方式、规则绑定。
3. 值类型调整为：文本 / 数值 / 日期 / 布尔 / 选择。
4. “选择”成为独立值类型，统一承接单选、多选、下拉、标签选择等场景；文本、数值、日期、布尔不再在值类型层直接承接单选和多选。
5. 数值类型支持通过值属性配置来表达普通数值、价格、百分比、带单位数值等场景。
6. 价格不是新的值类型，而是“数值”类型下的一种格式；其币种、默认单位、可选单位、是否允许切换单位等信息属于值属性，不属于规则模型。
7. 交互方式成为可配置项，但必须受“值类型 + 值来源 + 值属性”约束，只能在当前支持集合中选择。

### 文档同步结果
- 已更新 `docs/Attribute_Modeling_Spec.md`，将正式建模规范切换到“值类型 / 值属性 / 值来源 / 交互方式”的新结构，并补充价格、百分比、带单位数值等场景的边界定义。
- 已更新 `docs/Attribute_Rule_Model_Draft.md`，同步属性与规则边界，明确价格单位等格式信息属于属性值属性，不属于规则模型。
- 已更新 `docs/Attribute_Template_Spec.md`，将属性模板的默认边界同步为“默认值类型 / 默认值属性 / 默认值来源 / 默认交互方式”。
- 已更新 `docs/Attribute_Management_Design.md`，同步属性管理页中的用户可见术语与信息结构，改为展示值类型、值属性、值来源、交互方式等信息。

## 20260626 14:44 物品的属性调整
### 修改建议
1. 新增物品、编辑物品的物品信息页，现在是基础信息区和补充信息区，两个区都可以添加属性。但是当前我们设计下，旧属性变成了现在的属性、规则设计。所以物品这里，我们需要调整下，物品的基础信息区，是物品的固有属性。也就是可以填写，但是无法删去基础信息区的属性。然后补充信息区，就是我们属性管理中设计的属性了。
然后物品模板，也可以指定基础信息区的属性，也可以指定补充信息区的属性。
基础信息区的属性不一定显示在属性管理页中，但是补充信息区中的属性，都是属性管理页中的属性。

2. 当前物品的补充信息中，还有很多旧属性的选项。将这些旧属性，迁移到我们新的属性设计吧。

### 结果
- 基础信息区已收敛为“固有属性区”，页面里不再支持删除基础区字段；当前保留基础区固有字段为 名称/分类/数量/品牌/规格 ，补充区则改为由新属性定义驱动： AddItemScreen.kt EditItemScreen.kt
- 补充信息区不再依赖旧硬编码字段列表，而是统一从属性定义表生成；同时为“旧补充字段”建立了系统属性定义，并保留和现有物品主模型字段的兼容映射，不需要改 Item 主结构： ItemFormComponents.kt AttributeRepository.kt
- 系统属性已补种覆盖原补充区常用字段，包括 状态/标签/总价/币种/购买日期/购买渠道/商家名称/备注/位置/地点/序列号/容量/评分/生产日期/保质期/保修期/订阅制/自动续费/扣费周期/开封状态/季节 等： AttributeRepository.kt
- 新增页已改为加载“全量属性定义”，模板只负责“预选哪些基础字段和补充属性”，不再决定补充属性的来源；旧模板里写在 selectedFields 的旧补充字段名，也会自动映射到新的系统属性定义： AddItemFragment.kt
- 编辑页也会在加载时确保系统属性存在，并按新的补充属性定义体系显示可编辑属性： EditItemFragment.kt
- 属性管理页初始化时也会补齐这些系统属性，避免“物品页用到但属性管理页里没有”的情况： AttributeManagementViewModel.kt
- 模板选择里的默认“通用模板”也已改成新结构：基础字段走 selectedFields ，补充属性走 customAttributeIds ： TemplateSelectionBottomSheet.kt
