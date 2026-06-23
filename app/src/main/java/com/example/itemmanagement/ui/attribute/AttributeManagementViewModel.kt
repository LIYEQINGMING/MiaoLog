package com.example.itemmanagement.ui.attribute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itemmanagement.data.model.attribute.*
import com.example.itemmanagement.data.repository.AttributeRepository
import com.example.itemmanagement.ui.attribute.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class AttributeManagementViewModel(
    private val repository: AttributeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttributeManagementUiState())
    val uiState: StateFlow<AttributeManagementUiState> = _uiState.asStateFlow()

    private var currentAttributes: List<AttributeDefinition> = emptyList()
    private var currentRules: List<RuleDefinition> = emptyList()
    
    private val templateCatalog: List<TemplateCatalogEntry> = buildTemplateCatalog()

    init {
        viewModelScope.launch {
            repository.getAllAttributes().collect { attributes ->
                currentAttributes = attributes
                if (attributes.isEmpty()) {
                    seedSystemAttributes()
                } else {
                    refresh()
                }
            }
        }
        viewModelScope.launch {
            repository.getAllRules().collect { rules ->
                currentRules = rules
                if (rules.isEmpty()) {
                    seedSystemRules()
                } else {
                    refresh()
                }
            }
        }
    }

    private suspend fun seedSystemAttributes() {
        // Init some built-in attributes based on templates if empty
        val defaultPriceAttr = AttributeDefinition(
            id = "attr_system_purchase_price",
            key = "purchase_price",
            name = "购入价格",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.NUMBER,
            optionSource = AttributeOptionSource.INPUT,
            inputMode = AttributeInputMode.PRICE_INPUT,
            isMultiValue = false,
            icon = "payments",
            templateId = "template_attr_purchase_price",
            ruleBindings = listOf(
                RuleBinding(
                    ruleId = "rule_system_include_total_price",
                    inputRole = "amount",
                    outputKeys = listOf("计入总价")
                )
            )
        )
        val defaultDateAttr = AttributeDefinition(
            id = "attr_system_purchase_date",
            key = "purchase_date",
            name = "购买日期",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.DATE,
            optionSource = AttributeOptionSource.INPUT,
            inputMode = AttributeInputMode.DATE_PICKER,
            isMultiValue = false,
            icon = "event",
            templateId = "template_attr_date"
        )
        repository.saveAttribute(defaultPriceAttr)
        repository.saveAttribute(defaultDateAttr)
    }

    private suspend fun seedSystemRules() {
        repository.systemRuleTemplates.forEach { template ->
            repository.saveRule(
                RuleDefinition(
                    id = template.key,
                    key = template.key,
                    name = template.name.removeSuffix("模板"),
                    computationType = template.computationType,
                    inputRoles = template.inputRoles,
                    requiredDependencies = template.requiredDependencies,
                    optionalDependencies = template.optionalDependencies,
                    outputKeys = template.outputKeys,
                    expression = template.defaultExpression,
                    description = template.description,
                )
            )
        }
    }

    private fun buildTemplateCatalog(): List<TemplateCatalogEntry> {
        val attrTemplates = repository.systemAttributeTemplates.map { template ->
            TemplateCatalogEntry(
                id = template.id,
                name = template.name,
                icon = template.defaultIcon ?: "notes",
                templateType = TemplateUiType.ATTRIBUTE,
                templateCategory = TemplateCategoryFilter.valueOf(template.category),
                ruleType = RuleTypeFilter.ALL,
                valueType = mapValueType(template.defaultValueType),
                optionSource = mapOptionSource(template.defaultOptionSource),
                inputMode = template.defaultInputMode.name,
                isMultiValue = template.defaultMultiValue,
                badgeText = "属性模板",
                summaryLine1 = "默认：${template.defaultValueType.name} / ${template.defaultOptionSource.name} / ${if(template.defaultMultiValue) "多值" else "单值"}",
                summaryLine2 = template.description ?: "",
                countText = "派生属性：0 个",
                usageHint = "适合快速创建通用字段",
                defaultRuleBindings = template.defaultRuleBindings.map { it.ruleId },
                inputRoles = emptyList(),
                requiredDependencies = emptyList(),
                optionalDependencies = emptyList(),
                outputs = emptyList(),
                isSystemBuiltIn = true
            )
        }

        val ruleTemplates = repository.systemRuleTemplates.map { template ->
            TemplateCatalogEntry(
                id = template.id,
                name = template.name,
                icon = "rule",
                templateType = TemplateUiType.RULE,
                templateCategory = TemplateCategoryFilter.ALL,
                ruleType = mapRuleType(template.computationType),
                valueType = AttributeValueTypeFilter.ALL,
                optionSource = AttributeOptionSourceFilter.ALL,
                inputMode = "",
                isMultiValue = false,
                badgeText = "规则模板",
                summaryLine1 = "输入：${template.inputRoles.joinToString(" / ")}",
                summaryLine2 = "输出：${template.outputKeys.joinToString(" / ")}",
                countText = "引用属性：0 个",
                usageHint = template.description ?: "",
                defaultRuleBindings = emptyList(),
                inputRoles = template.inputRoles,
                requiredDependencies = template.requiredDependencies,
                optionalDependencies = template.optionalDependencies,
                outputs = template.outputKeys,
                isSystemBuiltIn = true
            )
        }

        return attrTemplates + ruleTemplates
    }

    private fun mapValueType(type: AttributeValueType): AttributeValueTypeFilter = when(type) {
        AttributeValueType.TEXT -> AttributeValueTypeFilter.TEXT
        AttributeValueType.NUMBER -> AttributeValueTypeFilter.NUMBER
        AttributeValueType.DATE -> AttributeValueTypeFilter.DATE
        AttributeValueType.BOOLEAN -> AttributeValueTypeFilter.BOOLEAN
    }

    private fun mapOptionSource(source: AttributeOptionSource): AttributeOptionSourceFilter = when(source) {
        AttributeOptionSource.INPUT -> AttributeOptionSourceFilter.INPUT
        else -> AttributeOptionSourceFilter.FIXED_OPTIONS
    }

    private fun mapRuleType(type: RuleComputationType): RuleTypeFilter = when(type) {
        RuleComputationType.DIFFERENCE -> RuleTypeFilter.DIFFERENCE
        RuleComputationType.AVERAGE -> RuleTypeFilter.AVERAGE
        RuleComputationType.ACCUMULATION -> RuleTypeFilter.ACCUMULATION
        RuleComputationType.CYCLE -> RuleTypeFilter.CYCLE
        else -> RuleTypeFilter.ALL
    }

    fun refresh() {
        val current = _uiState.value
        _uiState.value = current.copy(
            attributePane = buildAttributePaneState(
                searchQuery = current.searchQuery,
                filters = current.attributePane.filters,
            ),
            rulePane = buildRulePaneState(
                searchQuery = current.searchQuery,
            ),
            templatePane = buildTemplatePaneState(
                searchQuery = current.searchQuery,
                filters = current.templatePane.filters,
            ),
        )
    }

    fun selectTab(tab: AttributeManagementTab) {
        _uiState.value = _uiState.value.copy(
            selectedTab = tab,
            routeState = AttributeManagementRouteState.List,
        )
    }

    fun updateSearchQuery(query: String) {
        val current = _uiState.value
        _uiState.value = current.copy(
            searchQuery = query,
            routeState = AttributeManagementRouteState.List,
            attributePane = buildAttributePaneState(
                searchQuery = query,
                filters = current.attributePane.filters,
            ),
            rulePane = buildRulePaneState(
                searchQuery = query,
            ),
            templatePane = buildTemplatePaneState(
                searchQuery = query,
                filters = current.templatePane.filters,
            ),
        )
    }

    fun cycleAttributeSourceFilter() {
        val filters = _uiState.value.attributePane.filters
        updateAttributeFilters(filters.copy(source = nextEnum(filters.source, AttributeSourceFilter.values())))
    }

    fun cycleAttributeValueTypeFilter() {
        val filters = _uiState.value.attributePane.filters
        updateAttributeFilters(filters.copy(valueType = nextEnum(filters.valueType, AttributeValueTypeFilter.values())))
    }

    fun cycleAttributeRuleBindingFilter() {
        val filters = _uiState.value.attributePane.filters
        updateAttributeFilters(filters.copy(ruleBinding = nextEnum(filters.ruleBinding, RuleBindingFilter.values())))
    }

    fun clearAttributeFilters() {
        updateAttributeFilters(AttributeListFilters())
    }

    fun cycleTemplateTypeFilter() {
        val filters = _uiState.value.templatePane.filters
        updateTemplateFilters(filters.copy(templateType = nextEnum(filters.templateType, TemplateTypeFilter.values())))
    }

    fun cycleTemplateCategoryFilter() {
        val filters = _uiState.value.templatePane.filters
        updateTemplateFilters(filters.copy(templateCategory = nextEnum(filters.templateCategory, TemplateCategoryFilter.values())))
    }

    fun cycleRuleTypeFilter() {
        val filters = _uiState.value.templatePane.filters
        updateTemplateFilters(filters.copy(ruleType = nextEnum(filters.ruleType, RuleTypeFilter.values())))
    }

    fun clearTemplateFilters() {
        updateTemplateFilters(TemplateListFilters())
    }

    fun openAttributeDetail(item: AttributeListItemUiModel) {
        val def = currentAttributes.firstOrNull { it.id == item.id } ?: return
        _uiState.value = _uiState.value.copy(
            routeState = AttributeManagementRouteState.AttributeDetail(toDetailUiModel(def))
        )
    }

    fun openTemplateDetail(item: TemplateListItemUiModel) {
        val entry = templateCatalog.firstOrNull { it.id == item.id } ?: return
        _uiState.value = _uiState.value.copy(
            routeState = AttributeManagementRouteState.TemplateDetail(entry.toDetailUiModel())
        )
    }

    fun openRuleDetail(item: RuleListItemUiModel) {
        val rule = currentRules.firstOrNull { it.id == item.id } ?: return
        _uiState.value = _uiState.value.copy(
            routeState = AttributeManagementRouteState.RuleDetail(toRuleDetailUiModel(rule))
        )
    }

    fun openCreateAttribute() {
        _uiState.value = _uiState.value.copy(
            routeState = AttributeManagementRouteState.CreateAttribute
        )
    }

    fun openCreateFromTemplate() {
        val draftTemplate = templateCatalog.firstOrNull { it.templateType == TemplateUiType.ATTRIBUTE }
        if (draftTemplate == null) {
            postMessage("当前没有可用于创建属性的模板")
            return
        }
        _uiState.value = _uiState.value.copy(
            routeState = AttributeManagementRouteState.CreateFromTemplate(draftTemplate.toCreateDraft())
        )
    }

    fun createFromTemplate(templateId: String) {
        val template = templateCatalog.firstOrNull { it.id == templateId && it.templateType == TemplateUiType.ATTRIBUTE }
        if (template == null) {
            postMessage("该模板当前不可用于创建属性")
            return
        }
        _uiState.value = _uiState.value.copy(
            routeState = AttributeManagementRouteState.CreateFromTemplate(template.toCreateDraft())
        )
    }

    fun requestDeleteAttribute(item: AttributeListItemUiModel) {
        if (item.isSystemBuiltIn) {
            postMessage("系统属性当前阶段不允许删除")
            return
        }
        _uiState.value = _uiState.value.copy(
            dialogState = AttributeManagementDialogState.ConfirmDeleteAttribute(
                attributeId = item.id,
                attributeName = item.name,
                usageCount = 0, // Placeholder
            )
        )
    }

    fun confirmDeleteAttribute() {
        val dialogState = _uiState.value.dialogState
        if (dialogState !is AttributeManagementDialogState.ConfirmDeleteAttribute) {
            return
        }
        viewModelScope.launch {
            val attr = currentAttributes.find { it.id == dialogState.attributeId }
            if (attr != null && attr.ownerType != AttributeOwnerType.SYSTEM) {
                repository.deleteAttribute(attr)
                postMessage("已删除属性：${dialogState.attributeName}")
            }
            dismissDialog()
            _uiState.value = _uiState.value.copy(routeState = AttributeManagementRouteState.List)
        }
    }

    fun navigateBackWithinAttributeManagement() {
        val current = _uiState.value
        when (current.routeState) {
            AttributeManagementRouteState.List -> Unit
            else -> _uiState.value = current.copy(routeState = AttributeManagementRouteState.List)
        }
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(dialogState = null)
    }

    fun consumeMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun saveAttributeDraft() {
        // Just mock saving a draft for now
        viewModelScope.launch {
            val newAttr = AttributeDefinition(
                id = "attr_${UUID.randomUUID()}",
                key = "custom_${System.currentTimeMillis()}",
                name = "新属性",
                ownerType = AttributeOwnerType.CUSTOM,
                valueType = AttributeValueType.TEXT,
                optionSource = AttributeOptionSource.INPUT,
                inputMode = AttributeInputMode.TEXT_INPUT,
                isMultiValue = false
            )
            repository.saveAttribute(newAttr)
            _uiState.value = _uiState.value.copy(routeState = AttributeManagementRouteState.List)
            postMessage("已保存新属性")
        }
    }

    private fun updateAttributeFilters(filters: AttributeListFilters) {
        val current = _uiState.value
        _uiState.value = current.copy(
            routeState = AttributeManagementRouteState.List,
            attributePane = buildAttributePaneState(
                searchQuery = current.searchQuery,
                filters = filters,
            )
        )
    }

    private fun updateTemplateFilters(filters: TemplateListFilters) {
        val current = _uiState.value
        _uiState.value = current.copy(
            routeState = AttributeManagementRouteState.List,
            templatePane = buildTemplatePaneState(
                searchQuery = current.searchQuery,
                filters = filters,
            )
        )
    }

    private fun buildAttributePaneState(
        searchQuery: String,
        filters: AttributeListFilters,
    ): AttributeListPaneState {
        val hasSearch = searchQuery.isNotBlank()
        val hasFilter = filters != AttributeListFilters()
        
        val catalogEntries = currentAttributes.map { mapToCatalogEntry(it) }
        val filtered = catalogEntries.filter { entry ->
            entry.matches(filters, searchQuery)
        }

        val contentState = when {
            filtered.isNotEmpty() -> ListContentState.Data
            hasSearch -> ListContentState.SearchEmpty
            hasFilter -> ListContentState.FilterEmpty
            catalogEntries.isEmpty() -> ListContentState.Empty
            else -> ListContentState.Empty
        }

        return AttributeListPaneState(
            filters = filters,
            contentState = contentState,
            items = filtered.map { it.toListItemUiModel() },
            totalCount = filtered.size,
            isRefreshing = false,
        )
    }

    private fun buildTemplatePaneState(
        searchQuery: String,
        filters: TemplateListFilters,
    ): TemplateListPaneState {
        val hasSearch = searchQuery.isNotBlank()
        val hasFilter = filters != TemplateListFilters()
        val filtered = templateCatalog.filter { entry ->
            entry.matches(filters, searchQuery)
        }

        val contentState = when {
            filtered.isNotEmpty() -> ListContentState.Data
            hasSearch -> ListContentState.SearchEmpty
            hasFilter -> ListContentState.FilterEmpty
            templateCatalog.isEmpty() -> ListContentState.Empty
            else -> ListContentState.Empty
        }

        return TemplateListPaneState(
            filters = filters,
            contentState = contentState,
            items = filtered.map { it.toListItemUiModel() },
            totalCount = filtered.size,
            isRefreshing = false,
        )
    }

    private fun buildRulePaneState(
        searchQuery: String,
    ): RuleListPaneState {
        val hasSearch = searchQuery.isNotBlank()
        val catalogEntries = currentRules.map { mapToRuleCatalogEntry(it) }
        val filtered = catalogEntries.filter { entry ->
            entry.matches(searchQuery)
        }

        val contentState = when {
            filtered.isNotEmpty() -> ListContentState.Data
            hasSearch -> ListContentState.SearchEmpty
            catalogEntries.isEmpty() -> ListContentState.Empty
            else -> ListContentState.Empty
        }

        return RuleListPaneState(
            contentState = contentState,
            items = filtered.map { it.toListItemUiModel() },
            totalCount = filtered.size,
            isRefreshing = false,
        )
    }

    private fun postMessage(message: String) {
        _uiState.value = _uiState.value.copy(message = message)
    }

    private fun <T : Enum<T>> nextEnum(current: T, values: Array<T>): T {
        val nextIndex = (current.ordinal + 1) % values.size
        return values[nextIndex]
    }

    private fun mapToCatalogEntry(def: AttributeDefinition): AttributeCatalogEntry {
        val ruleBindings = def.ruleBindings.map { binding ->
            val ruleTemplate = repository.systemRuleTemplates.find {
                it.id == binding.ruleId || it.key == binding.ruleId
            }
            val ruleDefinition = currentRules.find { it.id == binding.ruleId }
            val ruleName = ruleDefinition?.name ?: ruleTemplate?.name ?: binding.ruleId
            CatalogRuleBinding(
                ruleName = ruleName,
                inputRole = binding.inputRole,
                requiredDependencies = binding.requiredDependencies,
                optionalDependencies = binding.optionalDependencies,
                outputLabel = binding.outputKeys.joinToString(" / ")
            )
        }
        
        return AttributeCatalogEntry(
            id = def.id,
            name = def.name,
            icon = def.icon ?: "sell",
            source = if (def.ownerType == AttributeOwnerType.SYSTEM) AttributeSourceFilter.SYSTEM else AttributeSourceFilter.CUSTOM,
            valueType = mapValueType(def.valueType),
            optionSource = mapOptionSource(def.optionSource),
            inputMode = def.inputMode.name,
            isMultiValue = def.isMultiValue,
            templateName = repository.systemAttributeTemplates.find { it.id == def.templateId }?.name ?: "无",
            usageCount = 0,
            isSystemBuiltIn = def.ownerType == AttributeOwnerType.SYSTEM,
            ruleBindings = ruleBindings,
            dependencyHints = emptyList() // For simplicity
        )
    }
    
    private fun toDetailUiModel(def: AttributeDefinition): AttributeDetailUiModel {
        val ruleBindings = def.ruleBindings.map { binding ->
            val ruleTemplate = repository.systemRuleTemplates.find {
                it.id == binding.ruleId || it.key == binding.ruleId
            }
            val ruleDefinition = currentRules.find { it.id == binding.ruleId }
            val ruleName = ruleDefinition?.name ?: ruleTemplate?.name ?: binding.ruleId
            RuleBindingUiModel(
                ruleName = ruleName,
                inputRole = binding.inputRole,
                requiredDependencies = binding.requiredDependencies,
                optionalDependencies = binding.optionalDependencies,
                outputLabel = binding.outputKeys.joinToString(" / ")
            )
        }
        return AttributeDetailUiModel(
            id = def.id,
            name = def.name,
            icon = def.icon ?: "sell",
            sourceLabel = if (def.ownerType == AttributeOwnerType.SYSTEM) "系统属性" else "自定义属性",
            templateName = repository.systemAttributeTemplates.find { it.id == def.templateId }?.name ?: "无",
            usageCountText = "当前被 0 个物品使用",
            valueDefinition = AttributeValueDefinitionUiModel(
                valueType = mapValueType(def.valueType).displayName,
                optionSource = mapOptionSource(def.optionSource).displayName,
                inputMode = def.inputMode.name,
                multiValueLabel = if (def.isMultiValue) "多值" else "单值",
            ),
            ruleBindings = ruleBindings,
            dependencyHints = emptyList(),
            isSystemBuiltIn = def.ownerType == AttributeOwnerType.SYSTEM,
            isEditable = def.ownerType != AttributeOwnerType.SYSTEM,
            isDeletable = def.ownerType != AttributeOwnerType.SYSTEM,
        )
    }

    private fun mapToRuleCatalogEntry(rule: RuleDefinition): RuleCatalogEntry {
        val isSystemBuiltIn = repository.systemRuleTemplates.any {
            it.key == rule.key || it.id == rule.id
        }
        val required = if (rule.requiredDependencies.isEmpty()) "无" else rule.requiredDependencies.joinToString(" / ")
        val optional = if (rule.optionalDependencies.isEmpty()) "无" else rule.optionalDependencies.joinToString(" / ")
        return RuleCatalogEntry(
            id = rule.id,
            name = rule.name,
            icon = "rule",
            ruleType = mapRuleType(rule.computationType),
            inputSummary = if (rule.inputRoles.isEmpty()) "无" else rule.inputRoles.joinToString(" / "),
            outputSummary = if (rule.outputKeys.isEmpty()) "无" else rule.outputKeys.joinToString(" / "),
            dependencySummary = "必需：$required；可选：$optional",
            sourceLabel = if (isSystemBuiltIn) "系统规则" else "自定义规则",
            isSystemBuiltIn = isSystemBuiltIn,
            expression = rule.expression,
            description = rule.description,
            inputRoles = rule.inputRoles,
            requiredDependencies = rule.requiredDependencies,
            optionalDependencies = rule.optionalDependencies,
            outputs = rule.outputKeys,
        )
    }

    private fun toRuleDetailUiModel(rule: RuleDefinition): RuleDetailUiModel {
        val isSystemBuiltIn = repository.systemRuleTemplates.any {
            it.key == rule.key || it.id == rule.id
        }
        return RuleDetailUiModel(
            id = rule.id,
            name = rule.name,
            icon = "rule",
            sourceLabel = if (isSystemBuiltIn) "系统规则" else "自定义规则",
            ruleType = mapRuleType(rule.computationType).displayName,
            inputRoles = rule.inputRoles,
            requiredDependencies = rule.requiredDependencies,
            optionalDependencies = rule.optionalDependencies,
            outputs = rule.outputKeys,
            expression = rule.expression,
            description = rule.description,
            isSystemBuiltIn = isSystemBuiltIn,
        )
    }
}

private data class AttributeCatalogEntry(
    val id: String,
    val name: String,
    val icon: String,
    val source: AttributeSourceFilter,
    val valueType: AttributeValueTypeFilter,
    val optionSource: AttributeOptionSourceFilter,
    val inputMode: String,
    val isMultiValue: Boolean,
    val templateName: String?,
    val usageCount: Int,
    val isSystemBuiltIn: Boolean,
    val ruleBindings: List<CatalogRuleBinding>,
    val dependencyHints: List<CatalogDependencyHint>,
) {
    fun matches(filters: AttributeListFilters, searchQuery: String): Boolean {
        val search = searchQuery.trim()
        val ruleSummary = ruleSummary()
        val searchMatches = search.isBlank() ||
            name.contains(search, ignoreCase = true) ||
            (templateName?.contains(search, ignoreCase = true) == true) ||
            ruleSummary.contains(search, ignoreCase = true)

        val sourceMatches = filters.source == AttributeSourceFilter.ALL || filters.source == source
        val valueTypeMatches = filters.valueType == AttributeValueTypeFilter.ALL || filters.valueType == valueType
        val optionMatches = filters.optionSource == AttributeOptionSourceFilter.ALL || filters.optionSource == optionSource
        val multiMatches = when (filters.multiValue) {
            MultiValueFilter.ALL -> true
            MultiValueFilter.SINGLE -> !isMultiValue
            MultiValueFilter.MULTI -> isMultiValue
        }
        val ruleMatches = when (filters.ruleBinding) {
            RuleBindingFilter.ALL -> true
            RuleBindingFilter.BOUND -> ruleBindings.isNotEmpty()
            RuleBindingFilter.UNBOUND -> ruleBindings.isEmpty()
        }

        return searchMatches && sourceMatches && valueTypeMatches && optionMatches && multiMatches && ruleMatches
    }

    fun toListItemUiModel(): AttributeListItemUiModel {
        return AttributeListItemUiModel(
            id = id,
            name = name,
            icon = icon,
            sourceLabel = if (source == AttributeSourceFilter.SYSTEM) "系统" else "自定义",
            valueTypeLabel = valueType.displayName,
            optionSourceLabel = optionSource.displayName,
            multiValueLabel = if (isMultiValue) "多值" else "单值",
            templateName = templateName,
            ruleSummary = ruleSummary(),
            usageCountText = "使用：$usageCount 个物品",
            isSystemBuiltIn = isSystemBuiltIn,
        )
    }

    private fun ruleSummary(): String {
        return if (ruleBindings.isEmpty()) "无" else ruleBindings.joinToString(" / ") { it.ruleName }
    }
}

private data class CatalogRuleBinding(
    val ruleName: String,
    val inputRole: String,
    val requiredDependencies: List<String>,
    val optionalDependencies: List<String>,
    val outputLabel: String,
)

private data class CatalogDependencyHint(
    val autoFillInputs: List<String>,
    val readonlyOutputs: List<String>,
)

private data class RuleCatalogEntry(
    val id: String,
    val name: String,
    val icon: String,
    val ruleType: RuleTypeFilter,
    val inputSummary: String,
    val outputSummary: String,
    val dependencySummary: String,
    val sourceLabel: String,
    val isSystemBuiltIn: Boolean,
    val expression: String?,
    val description: String?,
    val inputRoles: List<String>,
    val requiredDependencies: List<String>,
    val optionalDependencies: List<String>,
    val outputs: List<String>,
) {
    fun matches(searchQuery: String): Boolean {
        val search = searchQuery.trim()
        return search.isBlank() ||
            name.contains(search, ignoreCase = true) ||
            ruleType.displayName.contains(search, ignoreCase = true) ||
            inputSummary.contains(search, ignoreCase = true) ||
            outputSummary.contains(search, ignoreCase = true) ||
            dependencySummary.contains(search, ignoreCase = true) ||
            (description?.contains(search, ignoreCase = true) == true)
    }

    fun toListItemUiModel(): RuleListItemUiModel {
        return RuleListItemUiModel(
            id = id,
            name = name,
            icon = icon,
            ruleTypeLabel = ruleType.displayName,
            inputSummary = inputSummary,
            outputSummary = outputSummary,
            dependencySummary = dependencySummary,
            sourceLabel = sourceLabel,
            isSystemBuiltIn = isSystemBuiltIn,
        )
    }
}

private data class TemplateCatalogEntry(
    val id: String,
    val name: String,
    val icon: String,
    val templateType: TemplateUiType,
    val templateCategory: TemplateCategoryFilter,
    val ruleType: RuleTypeFilter,
    val valueType: AttributeValueTypeFilter,
    val optionSource: AttributeOptionSourceFilter,
    val inputMode: String,
    val isMultiValue: Boolean,
    val badgeText: String,
    val summaryLine1: String,
    val summaryLine2: String,
    val countText: String,
    val usageHint: String,
    val defaultRuleBindings: List<String>,
    val inputRoles: List<String>,
    val requiredDependencies: List<String>,
    val optionalDependencies: List<String>,
    val outputs: List<String>,
    val isSystemBuiltIn: Boolean,
) {
    fun matches(filters: TemplateListFilters, searchQuery: String): Boolean {
        val search = searchQuery.trim()
        val searchMatches = search.isBlank() ||
            name.contains(search, ignoreCase = true) ||
            badgeText.contains(search, ignoreCase = true) ||
            summaryLine1.contains(search, ignoreCase = true) ||
            summaryLine2.contains(search, ignoreCase = true)

        val typeMatches = when (filters.templateType) {
            TemplateTypeFilter.ALL -> true
            TemplateTypeFilter.ATTRIBUTE -> templateType == TemplateUiType.ATTRIBUTE
            TemplateTypeFilter.RULE -> templateType == TemplateUiType.RULE
        }

        val categoryMatches = when {
            filters.templateCategory == TemplateCategoryFilter.ALL -> true
            templateType == TemplateUiType.RULE -> true
            else -> filters.templateCategory == templateCategory
        }

        val ruleTypeMatches = when {
            filters.ruleType == RuleTypeFilter.ALL -> true
            templateType == TemplateUiType.ATTRIBUTE -> true
            else -> filters.ruleType == ruleType
        }

        val valueTypeMatches = when {
            filters.valueType == AttributeValueTypeFilter.ALL -> true
            templateType == TemplateUiType.RULE -> true
            else -> filters.valueType == valueType
        }

        return searchMatches && typeMatches && categoryMatches && ruleTypeMatches && valueTypeMatches
    }

    fun toListItemUiModel(): TemplateListItemUiModel {
        return TemplateListItemUiModel(
            id = id,
            name = name,
            icon = icon,
            templateType = templateType,
            badgeText = badgeText,
            summaryLine1 = summaryLine1,
            summaryLine2 = summaryLine2,
            countText = countText,
            isSystemBuiltIn = isSystemBuiltIn,
        )
    }

    fun toDetailUiModel(): TemplateDetailUiModel {
        return when (templateType) {
            TemplateUiType.ATTRIBUTE -> {
                TemplateDetailUiModel.AttributeTemplateDetail(
                    id = id,
                    name = name,
                    icon = icon,
                    isSystemBuiltIn = isSystemBuiltIn,
                    categoryLabel = templateCategory.displayName,
                    defaultDefinition = AttributeValueDefinitionUiModel(
                        valueType = valueType.displayName,
                        optionSource = optionSource.displayName,
                        inputMode = inputMode,
                        multiValueLabel = if (isMultiValue) "多值" else "单值",
                    ),
                    defaultRuleBindings = defaultRuleBindings,
                    derivedAttributeCountText = countText,
                    usageHint = usageHint,
                )
            }

            TemplateUiType.RULE -> {
                TemplateDetailUiModel.RuleTemplateDetail(
                    id = id,
                    name = name,
                    icon = icon,
                    isSystemBuiltIn = isSystemBuiltIn,
                    ruleType = ruleType.displayName,
                    inputRoles = inputRoles,
                    requiredDependencies = requiredDependencies,
                    optionalDependencies = optionalDependencies,
                    outputs = outputs,
                    referenceCountText = countText,
                )
            }
        }
    }

    fun toCreateDraft(): CreateAttributeFromTemplateUiModel {
        return CreateAttributeFromTemplateUiModel(
            templateId = id,
            templateName = name,
            templateIcon = icon,
            templateType = templateType,
            initialAttributeName = name.removeSuffix("属性模板").removeSuffix("模板"),
            valueDefinition = AttributeValueDefinitionUiModel(
                valueType = valueType.displayName,
                optionSource = optionSource.displayName,
                inputMode = inputMode.ifBlank { "待绑定输入方式" },
                multiValueLabel = if (isMultiValue) "多值" else "单值",
            ),
            defaultRuleBindings = defaultRuleBindings,
            editableHints = listOf(
                "允许调整属性名与图标",
                "允许在模板边界内调整字段配置",
                "规则绑定默认带出，但不是全部强制启用",
            ),
        )
    }
}
