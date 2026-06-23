package com.example.itemmanagement.ui.attribute.model

enum class AttributeManagementTab(val displayName: String) {
    ATTRIBUTES("属性"),
    RULES("规则"),
    TEMPLATES("模板"),
}

enum class AttributeSourceFilter(val displayName: String) {
    ALL("全部来源"),
    SYSTEM("系统属性"),
    CUSTOM("自定义属性"),
}

enum class AttributeValueTypeFilter(val displayName: String) {
    ALL("全部类型"),
    TEXT("文本"),
    NUMBER("数值"),
    DATE("日期"),
    BOOLEAN("布尔"),
}

enum class AttributeOptionSourceFilter(val displayName: String) {
    ALL("全部方式"),
    INPUT("输入"),
    FIXED_OPTIONS("固定选项"),
}

enum class MultiValueFilter(val displayName: String) {
    ALL("全部值数"),
    SINGLE("单值"),
    MULTI("多值"),
}

enum class RuleBindingFilter(val displayName: String) {
    ALL("全部规则"),
    BOUND("已绑定规则"),
    UNBOUND("未绑定规则"),
}

enum class TemplateTypeFilter(val displayName: String) {
    ALL("全部模板"),
    ATTRIBUTE("属性模板"),
    RULE("规则模板"),
}

enum class TemplateCategoryFilter(val displayName: String) {
    ALL("全部分类"),
    FOUNDATION("基础属性模板"),
    CORNERSTONE("基石属性模板"),
    PRICE_FAMILY("价格家族属性模板"),
}

enum class RuleTypeFilter(val displayName: String) {
    ALL("全部规则类型"),
    DIFFERENCE("差值规则"),
    AVERAGE("平均值规则"),
    CYCLE("周期规则"),
    ACCUMULATION("累计规则"),
}

enum class TemplateUiType(val displayName: String) {
    ATTRIBUTE("属性模板"),
    RULE("规则模板"),
}

sealed class ListContentState {
    object Loading : ListContentState()
    object Empty : ListContentState()
    object SearchEmpty : ListContentState()
    object FilterEmpty : ListContentState()
    object Data : ListContentState()
    data class Error(val message: String) : ListContentState()
}

data class AttributeListFilters(
    val source: AttributeSourceFilter = AttributeSourceFilter.ALL,
    val valueType: AttributeValueTypeFilter = AttributeValueTypeFilter.ALL,
    val optionSource: AttributeOptionSourceFilter = AttributeOptionSourceFilter.ALL,
    val multiValue: MultiValueFilter = MultiValueFilter.ALL,
    val ruleBinding: RuleBindingFilter = RuleBindingFilter.ALL,
)

data class TemplateListFilters(
    val templateType: TemplateTypeFilter = TemplateTypeFilter.ALL,
    val templateCategory: TemplateCategoryFilter = TemplateCategoryFilter.ALL,
    val ruleType: RuleTypeFilter = RuleTypeFilter.ALL,
    val valueType: AttributeValueTypeFilter = AttributeValueTypeFilter.ALL,
)

data class AttributeListPaneState(
    val filters: AttributeListFilters = AttributeListFilters(),
    val contentState: ListContentState = ListContentState.Loading,
    val items: List<AttributeListItemUiModel> = emptyList(),
    val totalCount: Int = 0,
    val isRefreshing: Boolean = false,
)

data class TemplateListPaneState(
    val filters: TemplateListFilters = TemplateListFilters(),
    val contentState: ListContentState = ListContentState.Loading,
    val items: List<TemplateListItemUiModel> = emptyList(),
    val totalCount: Int = 0,
    val isRefreshing: Boolean = false,
)

data class RuleListPaneState(
    val contentState: ListContentState = ListContentState.Loading,
    val items: List<RuleListItemUiModel> = emptyList(),
    val totalCount: Int = 0,
    val isRefreshing: Boolean = false,
)

data class AttributeListItemUiModel(
    val id: String,
    val name: String,
    val icon: String,
    val sourceLabel: String,
    val valueTypeLabel: String,
    val optionSourceLabel: String,
    val multiValueLabel: String,
    val templateName: String?,
    val ruleSummary: String,
    val usageCountText: String,
    val isSystemBuiltIn: Boolean,
)

data class TemplateListItemUiModel(
    val id: String,
    val name: String,
    val icon: String,
    val templateType: TemplateUiType,
    val badgeText: String,
    val summaryLine1: String,
    val summaryLine2: String,
    val countText: String,
    val isSystemBuiltIn: Boolean,
)

data class RuleListItemUiModel(
    val id: String,
    val name: String,
    val icon: String,
    val ruleTypeLabel: String,
    val inputSummary: String,
    val outputSummary: String,
    val dependencySummary: String,
    val sourceLabel: String,
    val isSystemBuiltIn: Boolean,
)

data class AttributeValueDefinitionUiModel(
    val valueType: String,
    val optionSource: String,
    val inputMode: String,
    val multiValueLabel: String,
)

data class RuleBindingUiModel(
    val ruleName: String,
    val inputRole: String,
    val requiredDependencies: List<String>,
    val optionalDependencies: List<String>,
    val outputLabel: String,
)

data class DependencyHintUiModel(
    val triggerAttributeName: String,
    val autoFillInputs: List<String>,
    val readonlyOutputs: List<String>,
)

data class AttributeDetailUiModel(
    val id: String,
    val name: String,
    val icon: String,
    val sourceLabel: String,
    val templateName: String?,
    val usageCountText: String,
    val valueDefinition: AttributeValueDefinitionUiModel,
    val ruleBindings: List<RuleBindingUiModel>,
    val dependencyHints: List<DependencyHintUiModel>,
    val isSystemBuiltIn: Boolean,
    val isEditable: Boolean,
    val isDeletable: Boolean,
)

data class RuleDetailUiModel(
    val id: String,
    val name: String,
    val icon: String,
    val sourceLabel: String,
    val ruleType: String,
    val inputRoles: List<String>,
    val requiredDependencies: List<String>,
    val optionalDependencies: List<String>,
    val outputs: List<String>,
    val expression: String?,
    val description: String?,
    val isSystemBuiltIn: Boolean,
)

sealed class TemplateDetailUiModel {
    abstract val id: String
    abstract val name: String
    abstract val icon: String
    abstract val isSystemBuiltIn: Boolean

    data class AttributeTemplateDetail(
        override val id: String,
        override val name: String,
        override val icon: String,
        override val isSystemBuiltIn: Boolean,
        val categoryLabel: String,
        val defaultDefinition: AttributeValueDefinitionUiModel,
        val defaultRuleBindings: List<String>,
        val derivedAttributeCountText: String,
        val usageHint: String,
    ) : TemplateDetailUiModel()

    data class RuleTemplateDetail(
        override val id: String,
        override val name: String,
        override val icon: String,
        override val isSystemBuiltIn: Boolean,
        val ruleType: String,
        val inputRoles: List<String>,
        val requiredDependencies: List<String>,
        val optionalDependencies: List<String>,
        val outputs: List<String>,
        val referenceCountText: String,
    ) : TemplateDetailUiModel()
}

data class CreateAttributeFromTemplateUiModel(
    val templateId: String,
    val templateName: String,
    val templateIcon: String,
    val templateType: TemplateUiType,
    val initialAttributeName: String,
    val valueDefinition: AttributeValueDefinitionUiModel,
    val defaultRuleBindings: List<String>,
    val editableHints: List<String>,
)

sealed class AttributeManagementDialogState {
    data class ConfirmDeleteAttribute(
        val attributeId: String,
        val attributeName: String,
        val usageCount: Int,
    ) : AttributeManagementDialogState()
}

sealed class AttributeManagementRouteState {
    object List : AttributeManagementRouteState()

    data class AttributeDetail(
        val detail: AttributeDetailUiModel,
    ) : AttributeManagementRouteState()

    data class TemplateDetail(
        val detail: TemplateDetailUiModel,
    ) : AttributeManagementRouteState()

    data class RuleDetail(
        val detail: RuleDetailUiModel,
    ) : AttributeManagementRouteState()

    data class CreateFromTemplate(
        val draft: CreateAttributeFromTemplateUiModel,
    ) : AttributeManagementRouteState()

    object CreateAttribute : AttributeManagementRouteState()
}

data class AttributeManagementUiState(
    val selectedTab: AttributeManagementTab = AttributeManagementTab.ATTRIBUTES,
    val searchQuery: String = "",
    val attributePane: AttributeListPaneState = AttributeListPaneState(),
    val rulePane: RuleListPaneState = RuleListPaneState(),
    val templatePane: TemplateListPaneState = TemplateListPaneState(),
    val routeState: AttributeManagementRouteState = AttributeManagementRouteState.List,
    val dialogState: AttributeManagementDialogState? = null,
    val message: String? = null,
)
