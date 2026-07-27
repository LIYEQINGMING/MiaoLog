package com.example.itemmanagement.ui.attribute.model

import com.example.itemmanagement.data.model.attribute.AttributeInputMode
import com.example.itemmanagement.data.model.attribute.AttributeNumberFormat
import com.example.itemmanagement.data.model.attribute.AttributeValueSource
import com.example.itemmanagement.data.model.attribute.AttributeValueType
import com.example.itemmanagement.data.model.attribute.RuleComputationType
import com.example.itemmanagement.data.model.attribute.RuleOutputTargetType
import com.example.itemmanagement.data.model.attribute.RuleOutputUpdateMode
import com.example.itemmanagement.data.model.attribute.RuleSlotDirection
import com.example.itemmanagement.data.model.attribute.RuleSlotSourceType
import com.example.itemmanagement.data.model.attribute.RuleSlotValueType
import com.example.itemmanagement.data.model.attribute.RuleTriggerMode

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
    SELECT("选择"),
}

enum class AttributeValueSourceFilter(val displayName: String) {
    ALL("全部值来源"),
    INPUT("输入"),
    FIXED("固定值"),
    SYSTEM("系统"),
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
    SUM("求和规则"),
    DIFFERENCE("差值规则"),
    AVERAGE("平均值规则"),
    CYCLE("周期规则"),
    ACCUMULATION("累计规则"),
    CUSTOM("自定义规则"),
}

enum class RuleSourceFilter(val displayName: String) {
    ALL("全部来源"),
    SYSTEM("系统规则"),
    CUSTOM("自定义规则"),
}

enum class RuleUsageFilter(val displayName: String) {
    ALL("全部绑定"),
    BOUND("已绑定属性"),
    UNBOUND("未绑定属性"),
}

enum class TemplateUiType(val displayName: String) {
    ATTRIBUTE("属性模板"),
    RULE("规则模板"),
}

sealed class ListContentState {
    data object Loading : ListContentState()
    data object Empty : ListContentState()
    data object SearchEmpty : ListContentState()
    data object FilterEmpty : ListContentState()
    data object Data : ListContentState()
    data class Error(val message: String) : ListContentState()
}

data class AttributeListFilters(
    val source: AttributeSourceFilter = AttributeSourceFilter.ALL,
    val valueType: AttributeValueTypeFilter = AttributeValueTypeFilter.ALL,
    val valueSource: AttributeValueSourceFilter = AttributeValueSourceFilter.ALL,
    val ruleBinding: RuleBindingFilter = RuleBindingFilter.ALL,
)

data class TemplateListFilters(
    val templateType: TemplateTypeFilter = TemplateTypeFilter.ALL,
    val templateCategory: TemplateCategoryFilter = TemplateCategoryFilter.ALL,
    val ruleType: RuleTypeFilter = RuleTypeFilter.ALL,
    val valueType: AttributeValueTypeFilter = AttributeValueTypeFilter.ALL,
)

data class RuleListFilters(
    val source: RuleSourceFilter = RuleSourceFilter.ALL,
    val ruleType: RuleTypeFilter = RuleTypeFilter.ALL,
    val usage: RuleUsageFilter = RuleUsageFilter.ALL,
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
    val filters: RuleListFilters = RuleListFilters(),
    val contentState: ListContentState = ListContentState.Loading,
    val items: List<RuleListItemUiModel> = emptyList(),
    val totalCount: Int = 0,
    val systemCount: Int = 0,
    val customCount: Int = 0,
    val boundCount: Int = 0,
    val isRefreshing: Boolean = false,
)

data class AttributeListItemUiModel(
    val id: String,
    val name: String,
    val icon: String,
    val sourceLabel: String,
    val valueTypeLabel: String,
    val valueSourceLabel: String,
    val interactionModeLabel: String,
    val propertySummary: String,
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
    val triggerSummary: String,
    val slotCountSummary: String,
    val inputSummary: String,
    val outputSummary: String,
    val sourceLabel: String,
    val isSystemBuiltIn: Boolean,
    val affectedAttributeCount: Int = 0,
    val affectedAttributeCountText: String = "",
    val affectedAttributeNames: List<String> = emptyList(),
)

data class AttributeValuePropertyUiModel(
    val label: String,
    val value: String,
)

data class AttributeValueDefinitionUiModel(
    val valueType: String,
    val valueSource: String,
    val interactionMode: String,
    val properties: List<AttributeValuePropertyUiModel>,
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
    val triggerModes: List<String>,
    val inputSlots: List<RuleSlotSummaryUiModel>,
    val outputSlots: List<RuleSlotSummaryUiModel>,
    val outputStrategies: List<RuleOutputStrategySummaryUiModel>,
    val expression: String?,
    val description: String?,
    val boundAttributes: List<String>,
    val boundAttributeCountText: String,
    val isSystemBuiltIn: Boolean,
    val isEditable: Boolean,
    val isDeletable: Boolean,
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
        val triggerModes: List<String>,
        val inputSlots: List<RuleSlotSummaryUiModel>,
        val outputSlots: List<RuleSlotSummaryUiModel>,
        val outputStrategies: List<RuleOutputStrategySummaryUiModel>,
        val expression: String?,
        val description: String?,
        val referenceCountText: String,
    ) : TemplateDetailUiModel()
}

data class RuleBindingCandidateUiModel(
    val ruleId: String,
    val ruleName: String,
    val ruleTypeLabel: String,
    val triggerSummary: String,
    val slotCountSummary: String,
    val inputSlotSummary: String,
    val outputSlotSummary: String,
    val entrySlotOptions: List<RuleBindingSlotOptionUiModel>,
    val selectedEntrySlotKey: String = "",
    val slotBindings: List<RuleBindingSlotDraftUiModel> = emptyList(),
    val isComplete: Boolean = false,
)

data class RuleSlotSummaryUiModel(
    val key: String,
    val name: String,
    val directionLabel: String,
    val valueTypeLabel: String,
    val sourceTypeLabel: String,
    val requiredLabel: String,
    val description: String = "",
)

data class RuleOutputStrategySummaryUiModel(
    val slotKey: String,
    val targetLabel: String,
    val updateModeLabel: String,
)

data class RuleBindingSlotOptionUiModel(
    val key: String,
    val name: String,
    val direction: RuleSlotDirection,
    val valueType: RuleSlotValueType,
    val sourceType: RuleSlotSourceType,
    val valueTypeLabel: String,
    val sourceTypeLabel: String,
    val isRequired: Boolean,
    val description: String = "",
    val isCompatibleWithCurrentAttribute: Boolean = false,
    val compatibilityHint: String = "",
)

data class AttributeSelectorOptionUiModel(
    val id: String? = null,
    val name: String,
    val valueType: AttributeValueType,
    val valueTypeLabel: String,
    val isCurrentAttribute: Boolean = false,
)

data class PendingCreatedAttributeUiModel(
    val id: String,
    val key: String,
    val name: String,
    val valueType: AttributeValueType,
    val valueTypeLabel: String,
    val slotValueType: RuleSlotValueType,
)

data class RuleBindingSlotDraftUiModel(
    val slotKey: String,
    val slotName: String,
    val direction: RuleSlotDirection,
    val valueType: RuleSlotValueType,
    val sourceType: RuleSlotSourceType,
    val isRequired: Boolean,
    val allowQuickCreateAttribute: Boolean = false,
    val description: String = "",
    val attributeId: String? = null,
    val attributeName: String = "",
    val isQuickCreatedAttribute: Boolean = false,
    val attributeOptions: List<AttributeSelectorOptionUiModel> = emptyList(),
    val configValue: String = "",
    val systemVariableKey: String = "",
    val outputTargetType: RuleOutputTargetType = RuleOutputTargetType.READONLY_RESULT,
    val outputUpdateMode: RuleOutputUpdateMode = RuleOutputUpdateMode.OVERWRITE,
)

data class RuleBindingWizardRuleCandidateUiModel(
    val ruleId: String,
    val ruleName: String,
    val ruleTypeLabel: String,
    val triggerSummary: String,
    val inputSlotSummary: String,
    val outputSlotSummary: String,
    val slotCountSummary: String,
    val entrySlotOptions: List<RuleBindingSlotOptionUiModel>,
    val allSlots: List<RuleBindingSlotOptionUiModel>,
    val defaultSlotBindings: List<RuleBindingSlotDraftUiModel> = emptyList(),
)

data class RuleBindingWizardDraftUiModel(
    val attributeDraft: AttributeEditorDraftUiModel,
    val attributeName: String,
    val attributeValueType: AttributeValueType,
    val compatibleRules: List<RuleBindingWizardRuleCandidateUiModel> = emptyList(),
    val selectedRuleId: String? = null,
    val selectedEntrySlotKey: String? = null,
    val slotBindings: List<RuleBindingSlotDraftUiModel> = emptyList(),
    val title: String = "规则绑定",
    val saveButtonText: String = "保存绑定",
)

data class SystemVariableOptionUiModel(
    val key: String,
    val displayName: String,
    val valueType: AttributeValueType,
    val valueTypeLabel: String,
    val statusLabel: String,
    val description: String = "",
    val availabilityMessage: String? = null,
    val isSelectable: Boolean = true,
)

data class AttributeEditorDraftUiModel(
    val id: String? = null,
    val name: String = "",
    val description: String = "",
    val templateId: String? = null,
    val templateName: String? = null,
    val valueType: AttributeValueType = AttributeValueType.TEXT,
    val valueSource: AttributeValueSource = AttributeValueSource.INPUT,
    val numberFormat: AttributeNumberFormat = AttributeNumberFormat.PLAIN,
    val unitCategory: String = "",
    val defaultUnit: String = "",
    val decimalPlacesText: String = "",
    val allowNegative: Boolean = true,
    val booleanInputMode: AttributeInputMode = AttributeInputMode.BOOLEAN_SWITCH,
    val trueLabel: String = "",
    val falseLabel: String = "",
    val optionItemsText: String = "",
    val isMultiSelect: Boolean = false,
    val systemVariableKey: String = "",
    val systemVariableOptions: List<SystemVariableOptionUiModel> = emptyList(),
    val defaultValue: String = "",
    val ruleCandidates: List<RuleBindingCandidateUiModel> = emptyList(),
    val pendingCreatedAttributes: List<PendingCreatedAttributeUiModel> = emptyList(),
    val title: String = "新建属性",
    val saveButtonText: String = "保存属性",
    val isEditMode: Boolean = false,
)

data class RuleEditorDraftUiModel(
    val id: String? = null,
    val name: String = "",
    val description: String = "",
    val templateId: String? = null,
    val templateName: String? = null,
    val computationType: RuleComputationType = RuleComputationType.CUSTOM,
    val triggerModes: List<com.example.itemmanagement.data.model.attribute.RuleTriggerMode> = listOf(
        com.example.itemmanagement.data.model.attribute.RuleTriggerMode.ON_VALUE_CHANGED
    ),
    val slots: List<RuleSlotDraftUiModel> = emptyList(),
    val systemVariableOptions: List<SystemVariableOptionUiModel> = emptyList(),
    val expression: String = "",
    val title: String = "新建规则",
    val saveButtonText: String = "保存规则",
    val isEditMode: Boolean = false,
)

data class RuleSlotDraftUiModel(
    val id: String,
    val key: String = "",
    val name: String = "",
    val direction: com.example.itemmanagement.data.model.attribute.RuleSlotDirection =
        com.example.itemmanagement.data.model.attribute.RuleSlotDirection.INPUT,
    val valueType: com.example.itemmanagement.data.model.attribute.RuleSlotValueType =
        com.example.itemmanagement.data.model.attribute.RuleSlotValueType.TEXT,
    val sourceType: com.example.itemmanagement.data.model.attribute.RuleSlotSourceType =
        com.example.itemmanagement.data.model.attribute.RuleSlotSourceType.ATTRIBUTE_INPUT,
    val isRequired: Boolean = true,
    val allowQuickCreateAttribute: Boolean = false,
    val systemVariableKey: String = "",
    val outputTargetType: com.example.itemmanagement.data.model.attribute.RuleOutputTargetType =
        com.example.itemmanagement.data.model.attribute.RuleOutputTargetType.READONLY_RESULT,
    val outputUpdateMode: com.example.itemmanagement.data.model.attribute.RuleOutputUpdateMode =
        com.example.itemmanagement.data.model.attribute.RuleOutputUpdateMode.OVERWRITE,
    val description: String = "",
)

sealed class AttributeManagementDialogState {
    data class ConfirmDeleteAttribute(
        val attributeId: String,
        val attributeName: String,
        val usageCount: Int,
    ) : AttributeManagementDialogState()

    data class ConfirmDeleteRule(
        val ruleId: String,
        val ruleName: String,
        val affectedAttributeCount: Int,
        val affectedAttributeNames: List<String>,
    ) : AttributeManagementDialogState()
}

sealed class AttributeManagementRouteState {
    data object List : AttributeManagementRouteState()

    data class AttributeDetail(
        val detail: AttributeDetailUiModel,
    ) : AttributeManagementRouteState()

    data class TemplateDetail(
        val detail: TemplateDetailUiModel,
    ) : AttributeManagementRouteState()

    data class RuleDetail(
        val detail: RuleDetailUiModel,
    ) : AttributeManagementRouteState()

    data class AttributeEditor(
        val draft: AttributeEditorDraftUiModel,
    ) : AttributeManagementRouteState()

    data class RuleEditor(
        val draft: RuleEditorDraftUiModel,
    ) : AttributeManagementRouteState()

    data class RuleBindingWizard(
        val draft: RuleBindingWizardDraftUiModel,
    ) : AttributeManagementRouteState()
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
