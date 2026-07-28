package com.example.itemmanagement.ui.attribute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itemmanagement.data.model.attribute.AppSystemSourceValue
import com.example.itemmanagement.data.model.attribute.AttributeDefinition
import com.example.itemmanagement.data.model.attribute.AttributeInputMode
import com.example.itemmanagement.data.model.attribute.AttributeNumberFormat
import com.example.itemmanagement.data.model.attribute.AttributeOwnerType
import com.example.itemmanagement.data.model.attribute.AttributeSelectionOptionSource
import com.example.itemmanagement.data.model.attribute.AttributeTemplate
import com.example.itemmanagement.data.model.attribute.AttributeValueProperties
import com.example.itemmanagement.data.model.attribute.AttributeValueSource
import com.example.itemmanagement.data.model.attribute.AttributeValueType
import com.example.itemmanagement.data.model.attribute.RuleBinding
import com.example.itemmanagement.data.model.attribute.RuleBindingInstance
import com.example.itemmanagement.data.model.attribute.RuleBindingStatus
import com.example.itemmanagement.data.model.attribute.RuleBindingCreationSource
import com.example.itemmanagement.data.model.attribute.RuleActivationMode
import com.example.itemmanagement.data.model.attribute.RuleComputationType
import com.example.itemmanagement.data.model.attribute.RuleDefinition
import com.example.itemmanagement.data.model.attribute.RuleExpressionDefinition
import com.example.itemmanagement.data.model.attribute.RuleInputSourceType
import com.example.itemmanagement.data.model.attribute.RuleOutputStrategyDefinition
import com.example.itemmanagement.data.model.attribute.RuleOutputTargetDefinition
import com.example.itemmanagement.data.model.attribute.RuleOutputTargetType
import com.example.itemmanagement.data.model.attribute.RuleOutputUpdateMode
import com.example.itemmanagement.data.model.attribute.RuleSlotBinding
import com.example.itemmanagement.data.model.attribute.RuleSlotDefinition
import com.example.itemmanagement.data.model.attribute.RuleSlotDirection
import com.example.itemmanagement.data.model.attribute.RuleSlotSourceType
import com.example.itemmanagement.data.model.attribute.RuleSlotValueType
import com.example.itemmanagement.data.model.attribute.RuleToggleUiConfig
import com.example.itemmanagement.data.model.attribute.RuleTemplate
import com.example.itemmanagement.data.model.attribute.RuleSystemInputDefinition
import com.example.itemmanagement.data.model.attribute.RuleTriggerMode
import com.example.itemmanagement.data.model.attribute.SystemVariableImplementationStatus
import com.example.itemmanagement.data.model.attribute.SystemVariableKey
import com.example.itemmanagement.data.model.attribute.inputSlots
import com.example.itemmanagement.data.model.attribute.outputSlots
import com.example.itemmanagement.data.model.attribute.builtInSystemVariableDefinitions
import com.example.itemmanagement.data.model.attribute.findSystemVariableDefinition
import com.example.itemmanagement.data.model.attribute.findSystemVariableKey
import com.example.itemmanagement.data.model.attribute.resolveAttributeRuleDependencyHints
import com.example.itemmanagement.data.model.attribute.resolveEffectiveRuleOutputTargets
import com.example.itemmanagement.data.model.attribute.resolveEffectiveRuleSystemInputs
import com.example.itemmanagement.data.model.attribute.resolveEffectiveRuleTriggerModes
import com.example.itemmanagement.data.model.attribute.ruleRuntimeMetadata
import com.example.itemmanagement.data.model.attribute.toLegacyOptionSource
import com.example.itemmanagement.data.repository.AppSystemSourceRepository
import com.example.itemmanagement.data.repository.AttributeRepository
import com.example.itemmanagement.ui.attribute.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale
import java.util.UUID

class AttributeManagementViewModel(
    private val repository: AttributeRepository,
    private val appSystemSourceRepository: AppSystemSourceRepository,
) : ViewModel() {
    private val attributeNameCollator: Collator = Collator.getInstance(Locale.CHINA)

    private val _uiState = MutableStateFlow(AttributeManagementUiState())
    val uiState: StateFlow<AttributeManagementUiState> = _uiState.asStateFlow()

    private var currentAttributes: List<AttributeDefinition> = emptyList()
    private var currentRules: List<RuleDefinition> = emptyList()
    private var currentSystemVariables: List<SystemVariableOptionUiModel> = buildFallbackSystemVariableOptions()
    private val templateCatalog: List<TemplateCatalogEntry>
        get() = buildTemplateCatalog()

    init {
        viewModelScope.launch {
            loadSystemVariableCatalog()
        }
        viewModelScope.launch {
            repository.ensureItemSystemAttributes()
            repository.getAllAttributes().collect { attributes ->
                currentAttributes = attributes
                refresh()
            }
        }
        viewModelScope.launch {
            repository.getAllRules().collect { rules ->
                currentRules = rules
                if (rules.isEmpty()) {
                    repository.ensureSystemRules()
                } else {
                    refresh()
                }
            }
        }
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
                filters = current.rulePane.filters,
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
            attributePane = buildAttributePaneState(query, current.attributePane.filters),
            rulePane = buildRulePaneState(query, current.rulePane.filters),
            templatePane = buildTemplatePaneState(query, current.templatePane.filters),
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

    fun cycleRuleSourceFilter() {
        val filters = _uiState.value.rulePane.filters
        updateRuleFilters(filters.copy(source = nextEnum(filters.source, RuleSourceFilter.values())))
    }

    fun cycleRuleManagementTypeFilter() {
        val filters = _uiState.value.rulePane.filters
        updateRuleFilters(filters.copy(ruleType = nextEnum(filters.ruleType, RuleTypeFilter.values())))
    }

    fun cycleRuleUsageFilter() {
        val filters = _uiState.value.rulePane.filters
        updateRuleFilters(filters.copy(usage = nextEnum(filters.usage, RuleUsageFilter.values())))
    }

    fun clearRuleFilters() {
        updateRuleFilters(RuleListFilters())
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
        val attribute = currentAttributes.firstOrNull { it.id == item.id } ?: return
        _uiState.value = _uiState.value.copy(
            routeState = AttributeManagementRouteState.AttributeDetail(toAttributeDetail(attribute))
        )
    }

    fun openRuleDetail(item: RuleListItemUiModel) {
        val rule = currentRules.firstOrNull { it.id == item.id } ?: return
        _uiState.value = _uiState.value.copy(
            routeState = AttributeManagementRouteState.RuleDetail(toRuleDetail(rule))
        )
    }

    fun openTemplateDetail(item: TemplateListItemUiModel) {
        val template = templateCatalog.firstOrNull { it.id == item.id } ?: return
        _uiState.value = _uiState.value.copy(
            routeState = AttributeManagementRouteState.TemplateDetail(template.toDetailUiModel())
        )
    }

    fun openCreateAttribute() {
        _uiState.value = _uiState.value.copy(
            routeState = AttributeManagementRouteState.AttributeEditor(buildAttributeEditorDraft())
        )
    }

    fun openEditAttribute(attributeId: String) {
        val attribute = currentAttributes.firstOrNull { it.id == attributeId } ?: return
        if (attribute.ownerType == AttributeOwnerType.SYSTEM) {
            postMessage("系统属性当前阶段不允许编辑")
            return
        }
        _uiState.value = _uiState.value.copy(
            routeState = AttributeManagementRouteState.AttributeEditor(buildAttributeEditorDraft(attribute))
        )
    }

    fun openRuleBindingWizard(
        attributeDraft: AttributeEditorDraftUiModel,
        editingRuleId: String? = null,
    ) {
        val attributeName = attributeDraft.name.trim()
        if (attributeName.isBlank()) {
            postMessage("请先填写属性名称，再配置规则绑定")
            return
        }
        _uiState.value = _uiState.value.copy(
            routeState = AttributeManagementRouteState.RuleBindingWizard(
                buildRuleBindingWizardDraft(attributeDraft, editingRuleId)
            )
        )
    }

    fun saveRuleBindingWizard(draft: RuleBindingWizardDraftUiModel) {
        val selectedRuleId = draft.selectedRuleId
        if (selectedRuleId.isNullOrBlank()) {
            postMessage("请先选择规则")
            return
        }
        val selectedEntrySlotKey = draft.selectedEntrySlotKey
        if (selectedEntrySlotKey.isNullOrBlank()) {
            postMessage("请先选择入口槽位")
            return
        }
        val missingRequiredSlot = draft.slotBindings.firstOrNull { slot ->
            if (!slot.isRequired || slot.slotKey == selectedEntrySlotKey) {
                false
            } else {
                when (slot.sourceType) {
                    RuleSlotSourceType.ATTRIBUTE_INPUT,
                    RuleSlotSourceType.ATTRIBUTE_OUTPUT -> slot.attributeName.isBlank()
                    RuleSlotSourceType.CONFIG_INPUT -> slot.configValue.isBlank()
                    RuleSlotSourceType.SYSTEM_INPUT,
                    RuleSlotSourceType.SYSTEM_OUTPUT -> findSystemVariableKey(slot.systemVariableKey) == null
                    RuleSlotSourceType.READONLY_OUTPUT -> false
                }
            }
        }
        if (missingRequiredSlot != null) {
            postMessage("槽位“${missingRequiredSlot.slotName}”尚未完成配置")
            return
        }
        val selectedRule = currentRules.firstOrNull { it.id == selectedRuleId } ?: return
        val resolvedSlotBindings = resolveRuleBindingSlotDraftsForSave(
            attributeDraft = draft.attributeDraft,
            selectedEntrySlotKey = selectedEntrySlotKey,
            slotBindings = draft.slotBindings,
        ) ?: return
        val existingCandidates = draft.attributeDraft.ruleCandidates.filterNot { it.ruleId == selectedRuleId }
        val updatedCandidate = buildConfiguredRuleBindingCandidate(
            rule = selectedRule,
            selectedEntrySlotKey = selectedEntrySlotKey,
            slotBindings = resolvedSlotBindings,
        )
        val updatedCandidates = (existingCandidates + updatedCandidate).sortedBy { it.ruleName }
        _uiState.value = _uiState.value.copy(
            routeState = AttributeManagementRouteState.AttributeEditor(
                draft.attributeDraft.copy(
                    ruleCandidates = updatedCandidates,
                    pendingCreatedAttributes = collectPendingCreatedAttributes(updatedCandidates),
                )
            )
        )
    }

    fun openCreateFromTemplate() {
        val template = repository.systemAttributeTemplates.firstOrNull()
        if (template == null) {
            postMessage("当前没有可用于创建属性的模板")
            return
        }
        createFromTemplate(template.id)
    }

    fun createFromTemplate(templateId: String) {
        val template = repository.systemAttributeTemplates.firstOrNull { it.id == templateId }
        if (template == null) {
            postMessage("该模板当前不可用于创建属性")
            return
        }
        _uiState.value = _uiState.value.copy(
            routeState = AttributeManagementRouteState.AttributeEditor(buildAttributeEditorDraft(template = template))
        )
    }

    fun openCreateRule() {
        _uiState.value = _uiState.value.copy(
            routeState = AttributeManagementRouteState.RuleEditor(buildRuleEditorDraft())
        )
    }

    fun openEditRule(ruleId: String) {
        val rule = currentRules.firstOrNull { it.id == ruleId } ?: return
        if (isSystemRule(rule)) {
            postMessage("系统规则当前阶段不允许编辑")
            return
        }
        _uiState.value = _uiState.value.copy(
            routeState = AttributeManagementRouteState.RuleEditor(buildRuleEditorDraft(rule = rule))
        )
    }

    fun createRuleFromTemplate(templateId: String) {
        val template = repository.systemRuleTemplates.firstOrNull { it.id == templateId }
        if (template == null) {
            postMessage("该模板当前不可用于创建规则")
            return
        }
        _uiState.value = _uiState.value.copy(
            routeState = AttributeManagementRouteState.RuleEditor(buildRuleEditorDraft(template = template))
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
                usageCount = 0,
            )
        )
    }

    fun requestDeleteRule(item: RuleListItemUiModel) {
        if (item.isSystemBuiltIn) {
            postMessage("系统规则当前阶段不允许删除")
            return
        }
        _uiState.value = _uiState.value.copy(
            dialogState = AttributeManagementDialogState.ConfirmDeleteRule(
                ruleId = item.id,
                ruleName = item.name,
                affectedAttributeCount = item.affectedAttributeCount,
                affectedAttributeNames = item.affectedAttributeNames,
            )
        )
    }

    fun requestDeleteRule(ruleId: String) {
        val rule = currentRules.firstOrNull { it.id == ruleId } ?: return
        requestDeleteRule(mapToRuleCatalogEntry(rule).toListItemUiModel())
    }

    fun confirmDeleteAttribute() {
        val dialog = _uiState.value.dialogState as? AttributeManagementDialogState.ConfirmDeleteAttribute ?: return
        viewModelScope.launch {
            val attribute = currentAttributes.firstOrNull { it.id == dialog.attributeId }
            if (attribute != null && attribute.ownerType != AttributeOwnerType.SYSTEM) {
                repository.deleteAttribute(attribute)
                postMessage("已删除属性：${dialog.attributeName}")
            }
            dismissDialog()
            _uiState.value = _uiState.value.copy(routeState = AttributeManagementRouteState.List)
        }
    }

    fun confirmDeleteRule() {
        val dialog = _uiState.value.dialogState as? AttributeManagementDialogState.ConfirmDeleteRule ?: return
        viewModelScope.launch {
            val rule = currentRules.firstOrNull { it.id == dialog.ruleId }
            if (rule != null && !isSystemRule(rule)) {
                val affectedAttributes = findAttributesBoundToRule(rule)
                affectedAttributes.forEach { attribute ->
                    val updatedBindings = attribute.ruleBindings.filterNot { bindingMatchesRule(it.ruleId, rule) }
                    if (updatedBindings.size != attribute.ruleBindings.size) {
                        repository.saveAttribute(attribute.copy(ruleBindings = updatedBindings))
                    }
                }
                repository.deleteRule(rule)
                val detachedCount = affectedAttributes.size
                postMessage(
                    if (detachedCount > 0) {
                        "已删除规则：${dialog.ruleName}，并从 $detachedCount 个属性中移除绑定"
                    } else {
                        "已删除规则：${dialog.ruleName}"
                    }
                )
            }
            dismissDialog()
            _uiState.value = _uiState.value.copy(routeState = AttributeManagementRouteState.List)
        }
    }

    fun saveAttributeDraft(draft: AttributeEditorDraftUiModel) {
        val name = draft.name.trim()
        if (name.isBlank()) {
            postMessage("属性名不能为空")
            return
        }
        val existingByName = currentAttributes.firstOrNull {
            it.name.equals(name, ignoreCase = true) && it.id != draft.id
        }
        if (existingByName != null) {
            postMessage("已存在同名属性，请调整名称后再保存")
            return
        }
        val optionItems = parseLineBasedList(draft.optionItemsText)
        if (draft.valueSource == AttributeValueSource.FIXED && draft.defaultValue.isBlank()) {
            postMessage("固定值不能为空")
            return
        }
        val selectedSystemVariable = draft.systemVariableKey.trim()
        if (draft.valueSource == AttributeValueSource.SYSTEM) {
            if (selectedSystemVariable.isBlank()) {
                postMessage("系统来源需要先选择系统级变量")
                return
            }
            val systemVariableOption = findSystemVariableOption(selectedSystemVariable)
            if (systemVariableOption == null) {
                postMessage("所选系统级变量不存在，请重新选择")
                return
            }
            if (!systemVariableOption.isSelectable) {
                postMessage(systemVariableOption.availabilityMessage ?: "该系统级变量当前未开放属性绑定")
                return
            }
            if (systemVariableOption.valueType != draft.valueType) {
                postMessage("系统级变量的值类型与当前属性不一致，请重新调整")
                return
            }
        }
        if (
            draft.valueType == AttributeValueType.SELECT &&
            optionItems.isEmpty()
        ) {
            postMessage("选择类型至少需要配置一个选项")
            return
        }
        if (
            draft.valueType == AttributeValueType.NUMBER &&
            draft.numberFormat == AttributeNumberFormat.WITH_UNIT &&
            draft.unitCategory.isBlank()
        ) {
            postMessage("量值类型需要先选择单位类型")
            return
        }
        if (
            draft.valueType == AttributeValueType.NUMBER &&
            draft.numberFormat == AttributeNumberFormat.WITH_UNIT &&
            draft.defaultUnit.isBlank()
        ) {
            postMessage("量值类型需要配置具体单位")
            return
        }

        val pendingCreatedAttributes = collectPendingCreatedAttributes(draft.ruleCandidates)
        val duplicatePendingName = pendingCreatedAttributes
            .groupBy { it.name.trim().lowercase(Locale.ROOT) }
            .entries
            .firstOrNull { it.key.isNotBlank() && it.value.size > 1 }
            ?.value
            ?.firstOrNull()
        if (duplicatePendingName != null) {
            postMessage("快捷创建属性“${duplicatePendingName.name}”重复，请调整后再保存")
            return
        }
        val conflictingPendingName = pendingCreatedAttributes.firstOrNull { pending ->
            pending.name.equals(name, ignoreCase = true)
        }
        if (conflictingPendingName != null) {
            postMessage("快捷创建属性名不能与当前属性同名，请调整“${conflictingPendingName.name}”")
            return
        }

        viewModelScope.launch {
            val existing = draft.id?.let { id -> currentAttributes.firstOrNull { it.id == id } }
            val valueProperties = buildValueProperties(draft)
            val attributeId = existing?.id ?: generateAttributeId()
            val attributeKey = existing?.key ?: buildAttributeKey(name)
            pendingCreatedAttributes.forEach { pendingAttribute ->
                val alreadyExists = currentAttributes.any { it.id == pendingAttribute.id }
                if (!alreadyExists) {
                    repository.saveAttribute(buildQuickCreatedAttributeDefinition(pendingAttribute))
                }
            }
            val requestedInputMode = when (draft.valueType) {
                AttributeValueType.BOOLEAN -> draft.booleanInputMode
                AttributeValueType.SELECT -> if (draft.isMultiSelect) {
                    AttributeInputMode.MULTI_SELECT
                } else {
                    AttributeInputMode.SINGLE_SELECT
                }
                AttributeValueType.DATE -> AttributeInputMode.DATE_PICKER
                AttributeValueType.NUMBER -> if (draft.numberFormat == AttributeNumberFormat.WITH_UNIT) {
                    if (draft.unitCategory.trim() == "价格") AttributeInputMode.PRICE_INPUT else AttributeInputMode.NUMBER_INPUT
                } else {
                    AttributeInputMode.NUMBER_INPUT
                }
                AttributeValueType.TEXT -> AttributeInputMode.TEXT_INPUT
            }
            val interactionMode = sanitizeInputMode(
                valueType = draft.valueType,
                valueSource = draft.valueSource,
                inputMode = requestedInputMode,
                valueProperties = valueProperties,
            )
            val attribute = AttributeDefinition(
                id = attributeId,
                key = attributeKey,
                name = name,
                ownerType = existing?.ownerType ?: AttributeOwnerType.CUSTOM,
                valueType = draft.valueType,
                optionSource = draft.valueSource.toLegacyOptionSource(valueProperties),
                inputMode = interactionMode,
                isMultiValue = valueProperties.isMultiValue,
                optionItems = valueProperties.optionItems,
                valueProperties = valueProperties,
                valueSource = draft.valueSource,
                interactionMode = interactionMode,
                icon = existing?.icon ?: iconForValueType(draft.valueType, valueProperties),
                templateId = draft.templateId,
                ruleBindings = emptyList(),
                description = draft.description.trim().ifBlank { null },
            )
            repository.saveAttribute(attribute)
            _uiState.value = _uiState.value.copy(routeState = AttributeManagementRouteState.List)
            postMessage(if (existing == null) "已创建属性：$name" else "已更新属性：$name")
        }
    }

    fun saveRuleDraft(draft: RuleEditorDraftUiModel) {
        val name = draft.name.trim()
        if (name.isBlank()) {
            postMessage("规则名不能为空")
            return
        }
        val existingByName = currentRules.firstOrNull {
            it.name.equals(name, ignoreCase = true) && it.id != draft.id
        }
        if (existingByName != null) {
            postMessage("已存在同名规则，请调整名称后再保存")
            return
        }
        val triggerModes = draft.triggerModes.ifEmpty { listOf(RuleTriggerMode.ON_VALUE_CHANGED) }
        val slots = sanitizeRuleSlotDrafts(draft.slots)
        if (slots.isEmpty()) {
            postMessage("规则至少需要一个槽位")
            return
        }
        val inputSlots = slots.filter { it.direction == RuleSlotDirection.INPUT }
        val outputSlots = slots.filter { it.direction == RuleSlotDirection.OUTPUT }
        if (inputSlots.isEmpty()) {
            postMessage("规则至少需要一个输入槽位")
            return
        }
        if (outputSlots.isEmpty()) {
            postMessage("规则至少需要一个输出槽位")
            return
        }
        val emptyKeySlot = slots.firstOrNull { it.key.trim().isBlank() }
        if (emptyKeySlot != null) {
            postMessage("槽位 key 不能为空")
            return
        }
        val duplicateKey = slots.groupBy { it.key.trim() }.entries.firstOrNull { it.key.isNotBlank() && it.value.size > 1 }?.key
        if (duplicateKey != null) {
            postMessage("槽位 key“$duplicateKey”重复，请调整后再保存")
            return
        }
        val missingSystemVariableSlot = slots.firstOrNull {
            (it.sourceType == RuleSlotSourceType.SYSTEM_INPUT || it.sourceType == RuleSlotSourceType.SYSTEM_OUTPUT) &&
                findSystemVariableKey(it.systemVariableKey) == null
        }
        if (missingSystemVariableSlot != null) {
            postMessage("槽位“${missingSystemVariableSlot.name.ifBlank { missingSystemVariableSlot.key }}”缺少系统变量")
            return
        }
        val toggleAnchorSlotKey = draft.toggleAnchorSlotKey.trim()
        if (draft.activationMode == RuleActivationMode.USER_TOGGLE) {
            if (draft.toggleLabelWhenEnabled.isBlank() || draft.toggleLabelWhenDisabled.isBlank()) {
                postMessage("用户开关控制模式需要同时填写启用/停用文案")
                return
            }
            if (
                toggleAnchorSlotKey.isNotBlank() &&
                inputSlots.none { slot ->
                    slot.key == toggleAnchorSlotKey &&
                        slot.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT
                }
            ) {
                postMessage("开关锚点必须绑定到一个属性输入槽位")
                return
            }
        }
        val expression = draft.expression.trim()

        viewModelScope.launch {
            val existing = draft.id?.let { id -> currentRules.firstOrNull { it.id == id } }
            val slotDefinitions = slots.map { slot -> slot.toRuleSlotDefinition() }
            val outputStrategies = slots
                .filter { it.direction == RuleSlotDirection.OUTPUT }
                .map { slot -> slot.toRuleOutputStrategyDefinition() }
            val rule = RuleDefinition(
                id = existing?.id ?: generateRuleId(),
                key = existing?.key ?: buildRuleKey(name),
                name = name,
                computationType = draft.computationType,
                triggerModes = triggerModes,
                activationMode = draft.activationMode,
                toggleUiConfig = if (draft.activationMode == RuleActivationMode.USER_TOGGLE) {
                    RuleToggleUiConfig(
                        labelWhenEnabled = draft.toggleLabelWhenEnabled.trim(),
                        labelWhenDisabled = draft.toggleLabelWhenDisabled.trim(),
                        anchorSlotKey = toggleAnchorSlotKey.ifBlank { null },
                        defaultEnabled = draft.toggleDefaultEnabled,
                    )
                } else {
                    null
                },
                slots = slotDefinitions,
                expressionDefinition = expression.ifBlank { null }?.let {
                    RuleExpressionDefinition(
                        expression = it,
                        referencedSlotKeys = slotDefinitions.map { slot -> slot.key },
                    )
                },
                outputStrategies = outputStrategies,
                description = draft.description.trim().ifBlank { null },
                templateId = draft.templateId,
            )
            repository.saveRule(rule)
            _uiState.value = _uiState.value.copy(routeState = AttributeManagementRouteState.List)
            postMessage(if (existing == null) "已创建规则：$name" else "已更新规则：$name")
        }
    }

    fun navigateBackWithinAttributeManagement() {
        _uiState.value = _uiState.value.copy(routeState = AttributeManagementRouteState.List)
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(dialogState = null)
    }

    fun consumeMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    private fun buildAttributeEditorDraft(
        attribute: AttributeDefinition? = null,
        template: AttributeTemplate? = null,
    ): AttributeEditorDraftUiModel {
        val valueType = attribute?.valueType ?: template?.defaultValueType ?: AttributeValueType.TEXT
        val valueProperties = attribute?.valueProperties
            ?: template?.defaultValueProperties
            ?: AttributeValueProperties()
        return AttributeEditorDraftUiModel(
            id = attribute?.id,
            name = attribute?.name ?: template?.defaultName.orEmpty(),
            description = attribute?.description.orEmpty(),
            templateId = attribute?.templateId ?: template?.id,
            templateName = template?.name ?: repository.systemAttributeTemplates.firstOrNull { it.id == attribute?.templateId }?.name,
            valueType = valueType,
            valueSource = attribute?.valueSource ?: template?.defaultValueSource ?: AttributeValueSource.INPUT,
            numberFormat = editorNumberFormat(valueProperties.numberFormat),
            unitCategory = valueProperties.unitCategory
                ?: if (valueProperties.numberFormat == AttributeNumberFormat.PRICE) "价格" else "",
            defaultUnit = valueProperties.defaultUnit.orEmpty(),
            decimalPlacesText = valueProperties.decimalPlaces?.toString().orEmpty(),
            allowNegative = valueProperties.allowNegative,
            booleanInputMode = when (attribute?.interactionMode ?: template?.defaultInteractionMode) {
                AttributeInputMode.TEXT_INPUT -> AttributeInputMode.TEXT_INPUT
                else -> AttributeInputMode.BOOLEAN_SWITCH
            },
            trueLabel = valueProperties.trueLabel.orEmpty(),
            falseLabel = valueProperties.falseLabel.orEmpty(),
            optionItemsText = valueProperties.optionItems.joinToString("\n"),
            isMultiSelect = valueProperties.isMultiValue,
            systemVariableKey = if ((attribute?.valueSource ?: template?.defaultValueSource) == AttributeValueSource.SYSTEM) {
                valueProperties.systemVariableKey.orEmpty()
            } else {
                ""
            },
            systemVariableOptions = availableSystemVariableOptions(),
            defaultValue = if ((attribute?.valueSource ?: template?.defaultValueSource) == AttributeValueSource.FIXED) {
                valueProperties.defaultValue.orEmpty()
            } else {
                ""
            },
            ruleCandidates = emptyList(),
            pendingCreatedAttributes = emptyList(),
            title = when {
                attribute != null -> "编辑属性"
                template != null -> "从模板创建属性"
                else -> "新建属性"
            },
            saveButtonText = if (attribute != null) "保存属性定义" else "保存属性定义",
            isEditMode = attribute != null,
        )
    }

    private fun buildRuleEditorDraft(
        rule: RuleDefinition? = null,
        template: RuleTemplate? = null,
    ): RuleEditorDraftUiModel {
        val baseSlots = rule?.slots ?: template?.slots ?: emptyList()
        return RuleEditorDraftUiModel(
            id = rule?.id,
            name = rule?.name ?: template?.name
                ?.removeSuffix("规则模板")
                ?.removeSuffix("模板")
                .orEmpty(),
            description = rule?.description ?: template?.description.orEmpty(),
            templateId = template?.id,
            templateName = template?.name,
            computationType = rule?.computationType ?: template?.computationType ?: RuleComputationType.CUSTOM,
            triggerModes = rule?.let(::resolveEffectiveRuleTriggerModes)
                ?: template?.triggerModes
                ?: listOf(RuleTriggerMode.ON_VALUE_CHANGED),
            activationMode = rule?.activationMode ?: template?.activationMode ?: RuleActivationMode.ALWAYS_ON,
            toggleLabelWhenEnabled = rule?.toggleUiConfig?.labelWhenEnabled
                ?: template?.toggleUiConfig?.labelWhenEnabled
                .orEmpty(),
            toggleLabelWhenDisabled = rule?.toggleUiConfig?.labelWhenDisabled
                ?: template?.toggleUiConfig?.labelWhenDisabled
                .orEmpty(),
            toggleAnchorSlotKey = rule?.toggleUiConfig?.anchorSlotKey
                ?: template?.toggleUiConfig?.anchorSlotKey
                .orEmpty(),
            toggleDefaultEnabled = rule?.toggleUiConfig?.defaultEnabled
                ?: template?.toggleUiConfig?.defaultEnabled
                ?: true,
            slots = if (baseSlots.isEmpty()) {
                listOf(
                    newRuleSlotDraft(RuleSlotDirection.INPUT),
                    newRuleSlotDraft(RuleSlotDirection.OUTPUT),
                )
            } else {
                baseSlots.map { slot ->
                    RuleSlotDraftUiModel(
                        id = "slot_${UUID.randomUUID().toString().replace("-", "")}",
                        key = slot.key,
                        name = slot.name,
                        direction = slot.direction,
                        valueType = slot.valueType,
                        sourceType = slot.sourceType,
                        isRequired = slot.isRequired,
                        allowQuickCreateAttribute = slot.allowQuickCreateAttribute,
                        systemVariableKey = slot.systemVariableKey?.storageKey.orEmpty(),
                        outputTargetType = resolveEffectiveRuleOutputTargets(rule ?: templateToRuleDefinition(template))
                            .firstOrNull { it.outputKey == slot.key }
                            ?.targetType
                            ?: when (slot.sourceType) {
                                RuleSlotSourceType.ATTRIBUTE_OUTPUT -> RuleOutputTargetType.ATTRIBUTE_VALUE
                                RuleSlotSourceType.SYSTEM_OUTPUT -> RuleOutputTargetType.SYSTEM_VARIABLE
                                else -> RuleOutputTargetType.READONLY_RESULT
                            },
                        outputUpdateMode = rule?.outputStrategies
                            ?.firstOrNull { it.slotKey == slot.key }
                            ?.updateMode
                            ?: template?.outputStrategies
                                ?.firstOrNull { it.slotKey == slot.key }
                                ?.updateMode
                            ?: RuleOutputUpdateMode.OVERWRITE,
                        description = slot.description.orEmpty(),
                    )
                }
            },
            systemVariableOptions = availableSystemVariableOptions(),
            expression = rule?.expression ?: template?.defaultExpression.orEmpty(),
            title = when {
                rule != null -> "编辑规则"
                template != null -> "从模板创建规则"
                else -> "新建规则"
            },
            saveButtonText = if (rule != null) "保存修改" else "保存规则",
            isEditMode = rule != null,
        )
    }

    private fun buildRuleCandidates(selectedBindings: List<RuleBinding>): List<RuleBindingCandidateUiModel> {
        return currentRules.map { rule ->
            val binding = selectedBindings.firstOrNull { it.ruleId == rule.id }
            val inputSlots = rule.inputSlots()
            val outputSlots = rule.outputSlots()
            val slotBindings = if (binding != null) {
                buildRuleBindingSlotDrafts(
                    rule = rule,
                    existingBinding = binding,
                    entryAttributeName = binding.slotBindings
                        .filterIsInstance<RuleSlotBinding.AttributeInput>()
                        .firstOrNull { it.slotKey == binding.entrySlotKey }
                        ?.attributeNameSnapshot
                        .orEmpty(),
                    entryAttributeId = binding.entryAttributeId,
                    pendingCreatedAttributes = emptyList(),
                )
            } else {
                emptyList()
            }
            RuleBindingCandidateUiModel(
                ruleId = rule.id,
                ruleName = rule.name,
                ruleTypeLabel = mapRuleType(rule.computationType).displayName,
                triggerSummary = buildRuleTriggerSummary(rule),
                slotCountSummary = buildRuleSlotCountSummary(rule),
                inputSlotSummary = summarizeRuleSlots(inputSlots),
                outputSlotSummary = summarizeRuleSlots(outputSlots),
                entrySlotOptions = inputSlots.map(::toRuleBindingSlotOption),
                selectedEntrySlotKey = binding?.inputRole ?: "",
                slotBindings = slotBindings,
                isComplete = binding != null && slotBindings.all { slotDraft ->
                    !slotDraft.isRequired || isRuleBindingSlotDraftSatisfied(slotDraft)
                },
            )
        }
    }

    private fun sanitizeRuleSlotDrafts(slots: List<RuleSlotDraftUiModel>): List<RuleSlotDraftUiModel> {
        return slots.map { slot ->
            val trimmedKey = slot.key.trim()
            val normalizedSource = normalizeSourceType(slot.direction, slot.sourceType)
            val normalizedRequired = when {
                normalizedSource == RuleSlotSourceType.ATTRIBUTE_INPUT -> true
                else -> slot.isRequired
            }
            slot.copy(
                key = trimmedKey,
                name = slot.name.trim(),
                description = slot.description.trim(),
                sourceType = normalizedSource,
                isRequired = normalizedRequired,
                outputTargetType = normalizeOutputTargetType(normalizedSource, slot.outputTargetType),
            )
        }
    }

    private fun normalizeSourceType(
        direction: RuleSlotDirection,
        sourceType: RuleSlotSourceType,
    ): RuleSlotSourceType {
        return when (direction) {
            RuleSlotDirection.INPUT -> when (sourceType) {
                RuleSlotSourceType.ATTRIBUTE_INPUT,
                RuleSlotSourceType.CONFIG_INPUT,
                RuleSlotSourceType.SYSTEM_INPUT -> sourceType
                else -> RuleSlotSourceType.ATTRIBUTE_INPUT
            }
            RuleSlotDirection.OUTPUT -> when (sourceType) {
                RuleSlotSourceType.ATTRIBUTE_OUTPUT,
                RuleSlotSourceType.READONLY_OUTPUT,
                RuleSlotSourceType.SYSTEM_OUTPUT -> sourceType
                else -> RuleSlotSourceType.READONLY_OUTPUT
            }
        }
    }

    private fun normalizeOutputTargetType(
        sourceType: RuleSlotSourceType,
        outputTargetType: RuleOutputTargetType,
    ): RuleOutputTargetType {
        return when (sourceType) {
            RuleSlotSourceType.ATTRIBUTE_OUTPUT -> RuleOutputTargetType.ATTRIBUTE_VALUE
            RuleSlotSourceType.SYSTEM_OUTPUT -> RuleOutputTargetType.SYSTEM_VARIABLE
            else -> RuleOutputTargetType.READONLY_RESULT
        }
    }

    private fun RuleSlotDraftUiModel.toRuleSlotDefinition(): RuleSlotDefinition {
        val systemVariable = findSystemVariableKey(systemVariableKey)
        val normalizedSource = normalizeSourceType(direction, sourceType)
        return RuleSlotDefinition(
            key = key.trim(),
            name = name.trim().ifBlank { key.trim() },
            direction = direction,
            valueType = valueType,
            sourceType = normalizedSource,
            isRequired = when {
                normalizedSource == RuleSlotSourceType.ATTRIBUTE_INPUT -> true
                else -> isRequired
            },
            allowQuickCreateAttribute = allowQuickCreateAttribute,
            systemVariableKey = systemVariable,
            description = description.trim().ifBlank { null },
        )
    }

    private fun RuleSlotDraftUiModel.toRuleOutputStrategyDefinition(): RuleOutputStrategyDefinition {
        return RuleOutputStrategyDefinition(
            slotKey = key.trim(),
            targetType = normalizeOutputTargetType(sourceType, outputTargetType),
            updateMode = outputUpdateMode,
            systemVariableKey = findSystemVariableKey(systemVariableKey),
        )
    }

    private fun templateToRuleDefinition(template: RuleTemplate?): RuleDefinition {
        return RuleDefinition(
            id = template?.id.orEmpty(),
            key = template?.key.orEmpty(),
            name = template?.name.orEmpty(),
            computationType = template?.computationType ?: RuleComputationType.CUSTOM,
            triggerModes = template?.triggerModes ?: listOf(RuleTriggerMode.ON_VALUE_CHANGED),
            activationMode = template?.activationMode ?: RuleActivationMode.ALWAYS_ON,
            toggleUiConfig = template?.toggleUiConfig,
            slots = template?.slots ?: emptyList(),
            expressionDefinition = template?.defaultExpressionDefinition,
            outputStrategies = template?.outputStrategies ?: emptyList(),
            description = template?.description,
            templateId = template?.id,
        )
    }

    private fun newRuleSlotDraft(
        direction: RuleSlotDirection,
    ): RuleSlotDraftUiModel {
        val sourceType = when (direction) {
            RuleSlotDirection.INPUT -> RuleSlotSourceType.ATTRIBUTE_INPUT
            RuleSlotDirection.OUTPUT -> RuleSlotSourceType.READONLY_OUTPUT
        }
        return RuleSlotDraftUiModel(
            id = "slot_${UUID.randomUUID().toString().replace("-", "")}",
            direction = direction,
            sourceType = sourceType,
            outputTargetType = normalizeOutputTargetType(sourceType, RuleOutputTargetType.READONLY_RESULT),
        )
    }

    private fun buildRuleBindingWizardDraft(
        attributeDraft: AttributeEditorDraftUiModel,
        editingRuleId: String? = null,
    ): RuleBindingWizardDraftUiModel {
        val compatibleRules = currentRules.mapNotNull { rule ->
            val entrySlotOptions = rule.inputSlots()
                .filter { slot ->
                    slot.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT &&
                        isAttributeValueTypeCompatibleWithRuleSlot(attributeDraft.valueType, slot.valueType)
                }
                .map(::toRuleBindingSlotOption)
            if (entrySlotOptions.isEmpty()) {
                null
            } else {
                val defaultEntrySlotKey = entrySlotOptions.firstOrNull()?.key
                RuleBindingWizardRuleCandidateUiModel(
                    ruleId = rule.id,
                    ruleName = rule.name,
                    ruleTypeLabel = mapRuleType(rule.computationType).displayName,
                    triggerSummary = buildRuleTriggerSummary(rule),
                    inputSlotSummary = summarizeRuleSlots(rule.inputSlots()),
                    outputSlotSummary = summarizeRuleSlots(rule.outputSlots()),
                    slotCountSummary = buildRuleSlotCountSummary(rule),
                    entrySlotOptions = entrySlotOptions,
                        allSlots = rule.slots.map { slot ->
                            toRuleBindingSlotOption(
                                slot = slot,
                                currentAttributeValueType = attributeDraft.valueType,
                            )
                        },
                    defaultSlotBindings = buildRuleBindingSlotDrafts(
                        rule = rule,
                        existingBinding = null,
                        entryAttributeName = attributeDraft.name,
                        entryAttributeId = attributeDraft.id,
                            pendingCreatedAttributes = attributeDraft.pendingCreatedAttributes,
                        forcedEntrySlotKey = defaultEntrySlotKey,
                    ),
                )
            }
        }
        val existingCandidate = attributeDraft.ruleCandidates.firstOrNull { it.ruleId == editingRuleId }
        val selectedRuleId = editingRuleId ?: existingCandidate?.ruleId ?: compatibleRules.firstOrNull()?.ruleId
        val selectedRule = currentRules.firstOrNull { it.id == selectedRuleId }
        val selectedEntrySlotKey = existingCandidate?.selectedEntrySlotKey
            ?.takeIf { it.isNotBlank() }
            ?: compatibleRules.firstOrNull { it.ruleId == selectedRuleId }
                ?.entrySlotOptions
                ?.firstOrNull()
                ?.key
        val slotBindings = when {
            existingCandidate != null && existingCandidate.slotBindings.isNotEmpty() -> {
                existingCandidate.slotBindings.map { draftSlot ->
                    draftSlot.copy(
                        attributeOptions = buildAttributeSelectorOptions(
                            slotValueType = draftSlot.valueType,
                            currentAttributeName = attributeDraft.name,
                            currentAttributeId = attributeDraft.id,
                            pendingCreatedAttributes = attributeDraft.pendingCreatedAttributes,
                        )
                    )
                }
            }
            selectedRule != null -> {
                buildRuleBindingSlotDrafts(
                    rule = selectedRule,
                    existingBinding = null,
                    entryAttributeName = attributeDraft.name,
                    entryAttributeId = attributeDraft.id,
                    pendingCreatedAttributes = attributeDraft.pendingCreatedAttributes,
                    forcedEntrySlotKey = selectedEntrySlotKey,
                )
            }
            else -> emptyList()
        }
        return RuleBindingWizardDraftUiModel(
            attributeDraft = attributeDraft,
            attributeName = attributeDraft.name,
            attributeValueType = attributeDraft.valueType,
            compatibleRules = compatibleRules,
            selectedRuleId = selectedRuleId,
            selectedEntrySlotKey = selectedEntrySlotKey,
            slotBindings = slotBindings,
        )
    }

    private fun buildConfiguredRuleBindingCandidate(
        rule: RuleDefinition,
        selectedEntrySlotKey: String,
        slotBindings: List<RuleBindingSlotDraftUiModel>,
    ): RuleBindingCandidateUiModel {
        return RuleBindingCandidateUiModel(
            ruleId = rule.id,
            ruleName = rule.name,
            ruleTypeLabel = mapRuleType(rule.computationType).displayName,
            triggerSummary = buildRuleTriggerSummary(rule),
            slotCountSummary = buildRuleSlotCountSummary(rule),
            inputSlotSummary = summarizeRuleSlots(rule.inputSlots()),
            outputSlotSummary = summarizeRuleSlots(rule.outputSlots()),
            entrySlotOptions = rule.inputSlots().map(::toRuleBindingSlotOption),
            selectedEntrySlotKey = selectedEntrySlotKey,
            slotBindings = slotBindings,
            isComplete = slotBindings.all { slotDraft ->
                !slotDraft.isRequired || isRuleBindingSlotDraftSatisfied(slotDraft)
            },
        )
    }

    private fun buildRuleBindingSlotDrafts(
        rule: RuleDefinition,
        existingBinding: RuleBinding?,
        entryAttributeName: String,
        entryAttributeId: String?,
        pendingCreatedAttributes: List<PendingCreatedAttributeUiModel> = emptyList(),
        forcedEntrySlotKey: String? = null,
    ): List<RuleBindingSlotDraftUiModel> {
        val entrySlotKey = forcedEntrySlotKey ?: existingBinding?.entrySlotKey
        val existingByKey = existingBinding?.slotBindings?.associateBy { it.slotKey }.orEmpty()
        return rule.slots.map { slot ->
            val existing = existingByKey[slot.key]
            val isEntrySlot = slot.key == entrySlotKey
            when (slot.direction) {
                RuleSlotDirection.INPUT -> when (slot.sourceType) {
                    RuleSlotSourceType.ATTRIBUTE_INPUT -> {
                        val attributeBinding = existing as? RuleSlotBinding.AttributeInput
                        RuleBindingSlotDraftUiModel(
                            slotKey = slot.key,
                            slotName = slot.name,
                            direction = slot.direction,
                            valueType = slot.valueType,
                            sourceType = slot.sourceType,
                            isRequired = slot.isRequired,
                            allowQuickCreateAttribute = slot.allowQuickCreateAttribute,
                            description = slot.description.orEmpty(),
                            attributeId = if (isEntrySlot) entryAttributeId else attributeBinding?.attributeId,
                            attributeName = if (isEntrySlot) entryAttributeName else attributeBinding?.attributeNameSnapshot.orEmpty(),
                            isQuickCreatedAttribute = pendingCreatedAttributes.any { pending ->
                                pending.id == attributeBinding?.attributeId
                            },
                            attributeOptions = buildAttributeSelectorOptions(
                                slotValueType = slot.valueType,
                                currentAttributeName = entryAttributeName,
                                currentAttributeId = entryAttributeId,
                                pendingCreatedAttributes = pendingCreatedAttributes,
                            ),
                        )
                    }
                    RuleSlotSourceType.CONFIG_INPUT -> {
                        val configBinding = existing as? RuleSlotBinding.ConfigInput
                        RuleBindingSlotDraftUiModel(
                            slotKey = slot.key,
                            slotName = slot.name,
                            direction = slot.direction,
                            valueType = slot.valueType,
                            sourceType = slot.sourceType,
                            isRequired = slot.isRequired,
                            description = slot.description.orEmpty(),
                            configValue = configBinding?.rawValue.orEmpty(),
                        )
                    }
                    RuleSlotSourceType.SYSTEM_INPUT -> {
                        val systemBinding = existing as? RuleSlotBinding.SystemInput
                        RuleBindingSlotDraftUiModel(
                            slotKey = slot.key,
                            slotName = slot.name,
                            direction = slot.direction,
                            valueType = slot.valueType,
                            sourceType = slot.sourceType,
                            isRequired = slot.isRequired,
                            description = slot.description.orEmpty(),
                            systemVariableKey = systemBinding?.systemVariableKey?.storageKey
                                ?: slot.systemVariableKey?.storageKey
                                .orEmpty(),
                        )
                    }
                    else -> {
                        RuleBindingSlotDraftUiModel(
                            slotKey = slot.key,
                            slotName = slot.name,
                            direction = slot.direction,
                            valueType = slot.valueType,
                            sourceType = slot.sourceType,
                            isRequired = slot.isRequired,
                            description = slot.description.orEmpty(),
                        )
                    }
                }
                RuleSlotDirection.OUTPUT -> when (slot.sourceType) {
                    RuleSlotSourceType.ATTRIBUTE_OUTPUT -> {
                        val outputBinding = existing as? RuleSlotBinding.AttributeOutput
                        RuleBindingSlotDraftUiModel(
                            slotKey = slot.key,
                            slotName = slot.name,
                            direction = slot.direction,
                            valueType = slot.valueType,
                            sourceType = slot.sourceType,
                            isRequired = slot.isRequired,
                            allowQuickCreateAttribute = slot.allowQuickCreateAttribute,
                            description = slot.description.orEmpty(),
                            attributeId = outputBinding?.attributeId,
                            attributeName = outputBinding?.attributeNameSnapshot.orEmpty(),
                            isQuickCreatedAttribute = pendingCreatedAttributes.any { pending ->
                                pending.id == outputBinding?.attributeId
                            },
                            attributeOptions = buildAttributeSelectorOptions(
                                slotValueType = slot.valueType,
                                currentAttributeName = entryAttributeName,
                                currentAttributeId = entryAttributeId,
                                pendingCreatedAttributes = pendingCreatedAttributes,
                            ),
                            outputTargetType = RuleOutputTargetType.ATTRIBUTE_VALUE,
                            outputUpdateMode = outputBinding?.updateMode
                                ?: rule.outputStrategies.firstOrNull { it.slotKey == slot.key }?.updateMode
                                ?: RuleOutputUpdateMode.OVERWRITE,
                        )
                    }
                    RuleSlotSourceType.SYSTEM_OUTPUT -> {
                        val outputBinding = existing as? RuleSlotBinding.SystemOutput
                        RuleBindingSlotDraftUiModel(
                            slotKey = slot.key,
                            slotName = slot.name,
                            direction = slot.direction,
                            valueType = slot.valueType,
                            sourceType = slot.sourceType,
                            isRequired = slot.isRequired,
                            description = slot.description.orEmpty(),
                            systemVariableKey = outputBinding?.systemVariableKey?.storageKey
                                ?: rule.outputStrategies.firstOrNull { it.slotKey == slot.key }?.systemVariableKey?.storageKey
                                ?: slot.systemVariableKey?.storageKey
                                .orEmpty(),
                            outputTargetType = RuleOutputTargetType.SYSTEM_VARIABLE,
                            outputUpdateMode = outputBinding?.updateMode
                                ?: rule.outputStrategies.firstOrNull { it.slotKey == slot.key }?.updateMode
                                ?: RuleOutputUpdateMode.OVERWRITE,
                        )
                    }
                    RuleSlotSourceType.READONLY_OUTPUT -> {
                        RuleBindingSlotDraftUiModel(
                            slotKey = slot.key,
                            slotName = slot.name,
                            direction = slot.direction,
                            valueType = slot.valueType,
                            sourceType = slot.sourceType,
                            isRequired = slot.isRequired,
                            description = slot.description.orEmpty(),
                            outputTargetType = RuleOutputTargetType.READONLY_RESULT,
                            outputUpdateMode = rule.outputStrategies.firstOrNull { it.slotKey == slot.key }?.updateMode
                                ?: RuleOutputUpdateMode.OVERWRITE,
                        )
                    }
                    else -> {
                        RuleBindingSlotDraftUiModel(
                            slotKey = slot.key,
                            slotName = slot.name,
                            direction = slot.direction,
                            valueType = slot.valueType,
                            sourceType = slot.sourceType,
                            isRequired = slot.isRequired,
                            description = slot.description.orEmpty(),
                        )
                    }
                }
            }
        }
    }

    private fun buildRuleSlotBindingFromDraft(
        slotDraft: RuleBindingSlotDraftUiModel,
        rule: RuleDefinition?,
        entrySlotKey: String,
        currentAttributeId: String,
        currentAttributeKey: String,
        currentAttributeName: String,
    ): RuleSlotBinding? {
        val slotDefinition = rule?.slots?.firstOrNull { it.key == slotDraft.slotKey }
        return when (slotDraft.sourceType) {
            RuleSlotSourceType.ATTRIBUTE_INPUT -> {
                val isEntrySlot = slotDraft.slotKey == entrySlotKey
                val attributeName = if (isEntrySlot) currentAttributeName else slotDraft.attributeName.trim()
                if (attributeName.isBlank()) {
                    null
                } else {
                    RuleSlotBinding.AttributeInput(
                        slotKey = slotDraft.slotKey,
                        attributeId = if (isEntrySlot) currentAttributeId else slotDraft.attributeId,
                        attributeKeySnapshot = if (isEntrySlot) currentAttributeKey else null,
                        attributeNameSnapshot = attributeName,
                        isRequired = slotDefinition?.isRequired ?: slotDraft.isRequired,
                    )
                }
            }
            RuleSlotSourceType.CONFIG_INPUT -> {
                RuleSlotBinding.ConfigInput(
                    slotKey = slotDraft.slotKey,
                    rawValue = slotDraft.configValue.trim(),
                    isRequired = slotDefinition?.isRequired ?: slotDraft.isRequired,
                )
            }
            RuleSlotSourceType.SYSTEM_INPUT -> {
                findSystemVariableKey(slotDraft.systemVariableKey)?.let { systemVariableKey ->
                    RuleSlotBinding.SystemInput(
                        slotKey = slotDraft.slotKey,
                        systemVariableKey = systemVariableKey,
                        isRequired = slotDefinition?.isRequired ?: slotDraft.isRequired,
                    )
                }
            }
            RuleSlotSourceType.ATTRIBUTE_OUTPUT -> {
                val attributeName = slotDraft.attributeName.trim()
                if (attributeName.isBlank()) {
                    null
                } else {
                    RuleSlotBinding.AttributeOutput(
                        slotKey = slotDraft.slotKey,
                        attributeId = slotDraft.attributeId,
                        attributeNameSnapshot = attributeName,
                        updateMode = slotDraft.outputUpdateMode,
                        isRequired = slotDefinition?.isRequired ?: slotDraft.isRequired,
                    )
                }
            }
            RuleSlotSourceType.READONLY_OUTPUT -> {
                RuleSlotBinding.ReadonlyOutput(
                    slotKey = slotDraft.slotKey,
                    displayLabel = slotDraft.slotName,
                    isRequired = slotDefinition?.isRequired ?: slotDraft.isRequired,
                )
            }
            RuleSlotSourceType.SYSTEM_OUTPUT -> {
                findSystemVariableKey(slotDraft.systemVariableKey)?.let { systemVariableKey ->
                    RuleSlotBinding.SystemOutput(
                        slotKey = slotDraft.slotKey,
                        systemVariableKey = systemVariableKey,
                        updateMode = slotDraft.outputUpdateMode,
                        isRequired = slotDefinition?.isRequired ?: slotDraft.isRequired,
                    )
                }
            }
        }
    }

    private fun isRuleBindingSlotDraftSatisfied(slotDraft: RuleBindingSlotDraftUiModel): Boolean {
        return when (slotDraft.sourceType) {
            RuleSlotSourceType.ATTRIBUTE_INPUT,
            RuleSlotSourceType.ATTRIBUTE_OUTPUT -> slotDraft.attributeName.isNotBlank()
            RuleSlotSourceType.CONFIG_INPUT -> slotDraft.configValue.isNotBlank()
            RuleSlotSourceType.SYSTEM_INPUT,
            RuleSlotSourceType.SYSTEM_OUTPUT -> slotDraft.systemVariableKey.isNotBlank()
            RuleSlotSourceType.READONLY_OUTPUT -> true
        }
    }

    private fun resolveRuleBindingSlotDraftsForSave(
        attributeDraft: AttributeEditorDraftUiModel,
        selectedEntrySlotKey: String,
        slotBindings: List<RuleBindingSlotDraftUiModel>,
    ): List<RuleBindingSlotDraftUiModel>? {
        val pendingByName = attributeDraft.pendingCreatedAttributes.associateBy { it.name.trim().lowercase(Locale.ROOT) }
            .toMutableMap()
        val createdPending = attributeDraft.pendingCreatedAttributes.toMutableList()
        val resolved = mutableListOf<RuleBindingSlotDraftUiModel>()
        slotBindings.forEach { slot ->
            if (slot.sourceType != RuleSlotSourceType.ATTRIBUTE_INPUT && slot.sourceType != RuleSlotSourceType.ATTRIBUTE_OUTPUT) {
                resolved += slot
                return@forEach
            }
            if (slot.slotKey == selectedEntrySlotKey && slot.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT) {
                resolved += slot.copy(
                    attributeId = attributeDraft.id,
                    attributeName = attributeDraft.name,
                    isQuickCreatedAttribute = false,
                )
                return@forEach
            }
            val attributeName = slot.attributeName.trim()
            if (attributeName.isBlank()) {
                resolved += slot.copy(attributeId = null, attributeName = "", isQuickCreatedAttribute = false)
                return@forEach
            }
            val existingAttribute = currentAttributes.firstOrNull { definition ->
                definition.name.equals(attributeName, ignoreCase = true) &&
                    isAttributeValueTypeCompatibleWithRuleSlot(definition.valueType, slot.valueType)
            }
            if (existingAttribute != null) {
                resolved += slot.copy(
                    attributeId = existingAttribute.id,
                    attributeName = existingAttribute.name,
                    isQuickCreatedAttribute = false,
                )
                return@forEach
            }
            val existingPending = pendingByName[attributeName.lowercase(Locale.ROOT)]
            if (existingPending != null) {
                resolved += slot.copy(
                    attributeId = existingPending.id,
                    attributeName = existingPending.name,
                    isQuickCreatedAttribute = true,
                )
                return@forEach
            }
            if (!slot.allowQuickCreateAttribute) {
                postMessage("槽位“${slot.slotName}”只能绑定已有属性，当前未找到“$attributeName”")
                return null
            }
            val quickCreated = PendingCreatedAttributeUiModel(
                id = generateAttributeId(),
                key = buildAttributeKey(attributeName),
                name = attributeName,
                valueType = inferAttributeValueTypeForRuleSlot(slot.valueType),
                valueTypeLabel = mapValueType(inferAttributeValueTypeForRuleSlot(slot.valueType)).displayName,
                slotValueType = slot.valueType,
            )
            pendingByName[attributeName.lowercase(Locale.ROOT)] = quickCreated
            createdPending += quickCreated
            resolved += slot.copy(
                attributeId = quickCreated.id,
                attributeName = quickCreated.name,
                isQuickCreatedAttribute = true,
            )
        }
        return resolved.map { slot ->
            if (slot.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT || slot.sourceType == RuleSlotSourceType.ATTRIBUTE_OUTPUT) {
                slot.copy(
                    attributeOptions = buildAttributeSelectorOptions(
                        slotValueType = slot.valueType,
                        currentAttributeName = attributeDraft.name,
                        currentAttributeId = attributeDraft.id,
                        pendingCreatedAttributes = createdPending,
                    ),
                )
            } else {
                slot
            }
        }
    }

    private fun collectPendingCreatedAttributes(
        ruleCandidates: List<RuleBindingCandidateUiModel>,
    ): List<PendingCreatedAttributeUiModel> {
        val referencedSlots = ruleCandidates
            .flatMap { candidate -> candidate.slotBindings }
            .filter { slot ->
                slot.isQuickCreatedAttribute &&
                    (slot.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT || slot.sourceType == RuleSlotSourceType.ATTRIBUTE_OUTPUT)
            }
            .mapNotNull { slot ->
                val attributeId = slot.attributeId ?: return@mapNotNull null
                val attributeName = slot.attributeName.trim().takeIf { it.isNotBlank() } ?: return@mapNotNull null
                Triple(attributeId, attributeName, slot)
            }
            .distinctBy { it.first }
        return referencedSlots.map { (attributeId, attributeName, sampleSlot) ->
            PendingCreatedAttributeUiModel(
                id = attributeId,
                key = buildAttributeKey(attributeName),
                name = attributeName,
                valueType = inferAttributeValueTypeForRuleSlot(sampleSlot.valueType),
                valueTypeLabel = mapValueType(inferAttributeValueTypeForRuleSlot(sampleSlot.valueType)).displayName,
                slotValueType = sampleSlot.valueType,
            )
        }
    }

    private fun buildAttributeSelectorOptions(
        slotValueType: RuleSlotValueType,
        currentAttributeName: String,
        currentAttributeId: String?,
        pendingCreatedAttributes: List<PendingCreatedAttributeUiModel> = emptyList(),
    ): List<AttributeSelectorOptionUiModel> {
        val mappedCurrent = currentAttributes
            .filter { definition -> isAttributeValueTypeCompatibleWithRuleSlot(definition.valueType, slotValueType) }
            .map { definition ->
                AttributeSelectorOptionUiModel(
                    id = definition.id,
                    name = definition.name,
                    valueType = definition.valueType,
                    valueTypeLabel = mapValueType(definition.valueType).displayName,
                    isCurrentAttribute = currentAttributeId != null && definition.id == currentAttributeId,
                )
            }
        val mappedPending = pendingCreatedAttributes
            .filter { pending ->
                isAttributeValueTypeCompatibleWithRuleSlot(pending.valueType, slotValueType)
            }
            .map { pending ->
                AttributeSelectorOptionUiModel(
                    id = pending.id,
                    name = pending.name,
                    valueType = pending.valueType,
                    valueTypeLabel = pending.valueTypeLabel,
                    isCurrentAttribute = currentAttributeId != null && pending.id == currentAttributeId,
                )
            }
        val hasCurrent = mappedCurrent.any { it.isCurrentAttribute || it.name == currentAttributeName }
        val mergedOptions = (mappedCurrent + mappedPending).distinctBy { it.id ?: it.name.lowercase(Locale.ROOT) }
        return if (hasCurrent || currentAttributeName.isBlank()) {
            mergedOptions
        } else {
            listOf(
                AttributeSelectorOptionUiModel(
                    id = currentAttributeId,
                    name = currentAttributeName,
                    valueType = AttributeValueType.TEXT,
                    valueTypeLabel = mapValueType(AttributeValueType.TEXT).displayName,
                    isCurrentAttribute = true,
                )
            ) + mergedOptions
        }
    }

    private fun isAttributeValueTypeCompatibleWithRuleSlot(
        attributeValueType: AttributeValueType,
        slotValueType: RuleSlotValueType,
    ): Boolean {
        return when (slotValueType) {
            RuleSlotValueType.TEXT -> attributeValueType == AttributeValueType.TEXT
            RuleSlotValueType.NUMBER -> attributeValueType == AttributeValueType.NUMBER
            RuleSlotValueType.DATE,
            RuleSlotValueType.SYSTEM_DATE_TIME -> attributeValueType == AttributeValueType.DATE
            RuleSlotValueType.BOOLEAN -> attributeValueType == AttributeValueType.BOOLEAN
            RuleSlotValueType.SELECT -> attributeValueType == AttributeValueType.SELECT
            RuleSlotValueType.CYCLE_UNIT -> {
                attributeValueType == AttributeValueType.TEXT || attributeValueType == AttributeValueType.SELECT
            }
        }
    }

    private fun toRuleBindingSlotOption(
        slot: RuleSlotDefinition,
        currentAttributeValueType: AttributeValueType? = null,
    ): RuleBindingSlotOptionUiModel {
        val isCompatibleWithCurrentAttribute = currentAttributeValueType != null &&
            slot.direction == RuleSlotDirection.INPUT &&
            slot.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT &&
            isAttributeValueTypeCompatibleWithRuleSlot(currentAttributeValueType, slot.valueType)
        return RuleBindingSlotOptionUiModel(
            key = slot.key,
            name = slot.name,
            direction = slot.direction,
            valueType = slot.valueType,
            sourceType = slot.sourceType,
            valueTypeLabel = ruleSlotValueTypeLabel(slot.valueType),
            sourceTypeLabel = ruleSlotSourceTypeLabel(slot.sourceType),
            isRequired = slot.isRequired,
            description = slot.description.orEmpty(),
            isCompatibleWithCurrentAttribute = isCompatibleWithCurrentAttribute,
            compatibilityHint = if (isCompatibleWithCurrentAttribute) {
                "当前属性可直接绑定到该槽位"
            } else {
                ""
            },
        )
    }

    private fun toRuleSlotSummaryUiModel(slot: RuleSlotDefinition): RuleSlotSummaryUiModel {
        return RuleSlotSummaryUiModel(
            key = slot.key,
            name = slot.name,
            directionLabel = ruleSlotDirectionLabel(slot.direction),
            valueTypeLabel = ruleSlotValueTypeLabel(slot.valueType),
            sourceTypeLabel = ruleSlotSourceTypeLabel(slot.sourceType),
            requiredLabel = if (slot.isRequired) "必填" else "可选",
            description = slot.description.orEmpty(),
        )
    }

    private fun buildRuleTriggerSummary(rule: RuleDefinition): String {
        return resolveEffectiveRuleTriggerModes(rule)
            .map(::ruleTriggerModeLabel)
            .joinToString(" / ")
            .ifBlank { "无触发" }
    }

    private fun buildRuleSlotCountSummary(rule: RuleDefinition): String {
        return "输入 ${rule.inputSlots().size} · 输出 ${rule.outputSlots().size}"
    }

    private fun summarizeRuleSlots(slots: List<RuleSlotDefinition>): String {
        return slots.joinToString(" / ") { slot ->
            buildString {
                append(slot.name.ifBlank { slot.key })
                append(" · ")
                append(ruleSlotValueTypeLabel(slot.valueType))
                append(" · ")
                append(ruleSlotSourceTypeLabel(slot.sourceType))
                if (!slot.isRequired) {
                    append(" · 可选")
                }
            }
        }.ifBlank { "无" }
    }

    private fun updateAttributeFilters(filters: AttributeListFilters) {
        val current = _uiState.value
        _uiState.value = current.copy(
            routeState = AttributeManagementRouteState.List,
            attributePane = buildAttributePaneState(current.searchQuery, filters),
        )
    }

    private fun updateRuleFilters(filters: RuleListFilters) {
        val current = _uiState.value
        _uiState.value = current.copy(
            routeState = AttributeManagementRouteState.List,
            rulePane = buildRulePaneState(current.searchQuery, filters),
        )
    }

    private fun updateTemplateFilters(filters: TemplateListFilters) {
        val current = _uiState.value
        _uiState.value = current.copy(
            routeState = AttributeManagementRouteState.List,
            templatePane = buildTemplatePaneState(current.searchQuery, filters),
        )
    }

    private fun buildAttributePaneState(
        searchQuery: String,
        filters: AttributeListFilters,
    ): AttributeListPaneState {
        val catalogEntries = currentAttributes.map { mapToCatalogEntry(it) }
        val filtered = catalogEntries
            .filter { entry -> entry.matches(filters, searchQuery) }
            .sortedWith { left, right ->
                attributeNameCollator.compare(left.name, right.name)
            }
        val contentState = when {
            filtered.isNotEmpty() -> ListContentState.Data
            searchQuery.isNotBlank() -> ListContentState.SearchEmpty
            filters != AttributeListFilters() -> ListContentState.FilterEmpty
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

    private fun buildRulePaneState(
        searchQuery: String,
        filters: RuleListFilters,
    ): RuleListPaneState {
        val catalogEntries = currentRules.map { mapToRuleCatalogEntry(it) }
        val filtered = catalogEntries.filter { entry -> entry.matches(filters, searchQuery) }
        val contentState = when {
            filtered.isNotEmpty() -> ListContentState.Data
            searchQuery.isNotBlank() -> ListContentState.SearchEmpty
            filters != RuleListFilters() -> ListContentState.FilterEmpty
            catalogEntries.isEmpty() -> ListContentState.Empty
            else -> ListContentState.Empty
        }
        return RuleListPaneState(
            filters = filters,
            contentState = contentState,
            items = filtered.map { it.toListItemUiModel() },
            totalCount = filtered.size,
            systemCount = catalogEntries.count { it.source == RuleSourceFilter.SYSTEM },
            customCount = catalogEntries.count { it.source == RuleSourceFilter.CUSTOM },
            boundCount = catalogEntries.count { it.affectedAttributeCount > 0 },
            isRefreshing = false,
        )
    }

    private fun buildTemplatePaneState(
        searchQuery: String,
        filters: TemplateListFilters,
    ): TemplateListPaneState {
        val filtered = templateCatalog.filter { entry -> entry.matches(filters, searchQuery) }
        val contentState = when {
            filtered.isNotEmpty() -> ListContentState.Data
            searchQuery.isNotBlank() -> ListContentState.SearchEmpty
            filters != TemplateListFilters() -> ListContentState.FilterEmpty
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

    private fun buildTemplateCatalog(): List<TemplateCatalogEntry> {
        val attributeTemplates = repository.systemAttributeTemplates.map { template ->
            TemplateCatalogEntry(
                id = template.id,
                name = template.name,
                icon = template.defaultIcon ?: "notes",
                templateType = TemplateUiType.ATTRIBUTE,
                templateCategory = TemplateCategoryFilter.valueOf(template.category),
                ruleType = RuleTypeFilter.ALL,
                valueType = mapValueType(template.defaultValueType),
                valueSource = mapValueSource(template.defaultValueSource),
                interactionMode = "",
                propertySummary = buildValuePropertySummary(
                    valueType = template.defaultValueType,
                    valueSource = template.defaultValueSource,
                    interactionMode = template.defaultInteractionMode,
                    valueProperties = template.defaultValueProperties,
                ),
                badgeText = "属性模板",
                summaryLine1 = "默认：${mapValueType(template.defaultValueType).displayName} / ${mapValueSource(template.defaultValueSource).displayName}",
                summaryLine2 = template.description.orEmpty(),
                countText = "派生属性：${currentAttributes.count { it.templateId == template.id }} 个",
                usageHint = "适合快速创建标准字段",
                defaultRuleBindings = template.defaultRuleBindings.map { binding -> binding.ruleId },
                triggerModes = emptyList(),
                inputSlots = emptyList(),
                outputSlots = emptyList(),
                outputStrategies = emptyList(),
                expression = null,
                description = null,
                isSystemBuiltIn = true,
            )
        }

        val ruleTemplates = repository.systemRuleTemplates.map { template ->
            val templateRule = templateToRuleDefinition(template)
            TemplateCatalogEntry(
                id = template.id,
                name = template.name,
                icon = "rule",
                templateType = TemplateUiType.RULE,
                templateCategory = TemplateCategoryFilter.ALL,
                ruleType = mapRuleType(template.computationType),
                valueType = AttributeValueTypeFilter.ALL,
                valueSource = AttributeValueSourceFilter.ALL,
                interactionMode = "",
                propertySummary = "",
                badgeText = "规则模板",
                summaryLine1 = "输入：${summarizeRuleSlots(templateRule.inputSlots())}",
                summaryLine2 = "输出：${summarizeRuleSlots(templateRule.outputSlots())}",
                countText = "规则定义：${currentRules.count { it.key == template.key || it.id == template.key }} 个",
                usageHint = template.description.orEmpty(),
                defaultRuleBindings = emptyList(),
                triggerModes = template.triggerModes.map(::ruleTriggerModeLabel),
                inputSlots = templateRule.inputSlots().map(::toRuleSlotSummaryUiModel),
                outputSlots = templateRule.outputSlots().map(::toRuleSlotSummaryUiModel),
                outputStrategies = resolveEffectiveRuleOutputTargets(templateRule).map { output ->
                    RuleOutputStrategySummaryUiModel(
                        slotKey = output.outputKey,
                        targetLabel = when (output.targetType) {
                            RuleOutputTargetType.READONLY_RESULT -> "只读结果"
                            RuleOutputTargetType.ATTRIBUTE_VALUE -> "属性值"
                            RuleOutputTargetType.SYSTEM_VARIABLE -> output.variableKey?.displayName ?: "系统变量"
                        },
                        updateModeLabel = template.outputStrategies
                            .firstOrNull { it.slotKey == output.outputKey }
                            ?.updateMode
                            ?.let(::ruleOutputUpdateModeLabel)
                            ?: ruleOutputUpdateModeLabel(RuleOutputUpdateMode.OVERWRITE),
                    )
                },
                expression = template.defaultExpression,
                description = template.description,
                isSystemBuiltIn = true,
            )
        }
        return attributeTemplates + ruleTemplates
    }

    private fun mapToCatalogEntry(def: AttributeDefinition): AttributeCatalogEntry {
        val dependencyHints = resolveAttributeRuleDependencyHints(
            attribute = def,
            allAttributes = currentAttributes,
            ruleDefinitions = currentRules,
        )
        val ruleBindings = def.ruleBindings.map { binding ->
            val ruleTemplate = repository.systemRuleTemplates.firstOrNull {
                it.id == binding.ruleId || it.key == binding.ruleId
            }
            val ruleDefinition = currentRules.firstOrNull { it.id == binding.ruleId }
            val ruleName = ruleDefinition?.name ?: ruleTemplate?.name ?: binding.ruleId
            CatalogRuleBinding(
                ruleName = ruleName,
                inputRole = binding.inputRole,
                requiredDependencies = binding.requiredDependencies,
                optionalDependencies = binding.optionalDependencies,
                outputLabel = binding.outputKeys.joinToString(" / "),
            )
        }
        return AttributeCatalogEntry(
            id = def.id,
            name = def.name,
            icon = def.icon ?: "sell",
            source = if (def.ownerType == AttributeOwnerType.SYSTEM) AttributeSourceFilter.SYSTEM else AttributeSourceFilter.CUSTOM,
            valueType = mapValueType(def.valueType),
            valueSource = mapValueSource(def.valueSource),
            interactionMode = "",
            propertySummary = buildValuePropertySummary(
                valueType = def.valueType,
                valueSource = def.valueSource,
                interactionMode = def.interactionMode,
                valueProperties = def.valueProperties,
            ),
            templateName = repository.systemAttributeTemplates.firstOrNull { it.id == def.templateId }?.name ?: "无",
            usageCount = 0,
            isSystemBuiltIn = def.ownerType == AttributeOwnerType.SYSTEM,
            ruleBindings = ruleBindings,
            dependencyHints = dependencyHints.map { hint ->
                CatalogDependencyHint(
                    autoFillInputs = hint.autoFillInputs,
                    readonlyOutputs = hint.readonlyOutputs,
                )
            },
        )
    }

    private fun mapToRuleCatalogEntry(rule: RuleDefinition): RuleCatalogEntry {
        val isSystemBuiltIn = isSystemRule(rule)
        val affectedAttributes = findAttributesBoundToRule(rule)
        return RuleCatalogEntry(
            id = rule.id,
            name = rule.name,
            icon = "rule",
            ruleType = mapRuleType(rule.computationType),
            source = if (isSystemBuiltIn) RuleSourceFilter.SYSTEM else RuleSourceFilter.CUSTOM,
            triggerSummary = buildRuleTriggerSummary(rule),
            slotCountSummary = buildRuleSlotCountSummary(rule),
            inputSummary = summarizeRuleSlots(rule.inputSlots()),
            outputSummary = summarizeRuleSlots(rule.outputSlots()),
            searchSummary = buildString {
                append(summarizeRuleSlots(rule.inputSlots()))
                append(" ")
                append(summarizeRuleSlots(rule.outputSlots()))
                append(" ")
                append(buildRuleTriggerSummary(rule))
            },
            sourceLabel = if (isSystemBuiltIn) "系统规则" else "自定义规则",
            isSystemBuiltIn = isSystemBuiltIn,
            description = rule.description,
            affectedAttributeCount = affectedAttributes.size,
            affectedAttributeNames = affectedAttributes.map { it.name },
        )
    }

    private fun toAttributeDetail(def: AttributeDefinition): AttributeDetailUiModel {
        val dependencyHints = resolveAttributeRuleDependencyHints(
            attribute = def,
            allAttributes = currentAttributes,
            ruleDefinitions = currentRules,
        )
        val ruleBindings = def.ruleBindings.map { binding ->
            val ruleTemplate = repository.systemRuleTemplates.firstOrNull {
                it.id == binding.ruleId || it.key == binding.ruleId
            }
            val ruleDefinition = currentRules.firstOrNull { it.id == binding.ruleId }
            val ruleName = ruleDefinition?.name ?: ruleTemplate?.name ?: binding.ruleId
            RuleBindingUiModel(
                ruleName = ruleName,
                inputRole = binding.inputRole,
                requiredDependencies = binding.requiredDependencies,
                optionalDependencies = binding.optionalDependencies,
                outputLabel = binding.outputKeys.joinToString(" / "),
            )
        }
        return AttributeDetailUiModel(
            id = def.id,
            name = def.name,
            icon = def.icon ?: "sell",
            description = def.description,
            sourceLabel = if (def.ownerType == AttributeOwnerType.SYSTEM) "系统属性" else "自定义属性",
            templateName = repository.systemAttributeTemplates.firstOrNull { it.id == def.templateId }?.name ?: "无",
            usageCountText = "当前被 0 个物品使用",
            valueDefinition = buildValueDefinitionUiModel(
                valueType = def.valueType,
                valueSource = def.valueSource,
                interactionMode = def.interactionMode,
                valueProperties = def.valueProperties,
            ),
            ruleBindings = ruleBindings,
            dependencyHints = dependencyHints.map { hint ->
                DependencyHintUiModel(
                    triggerAttributeName = hint.triggerAttributeName,
                    autoFillInputs = hint.autoFillInputs,
                    readonlyOutputs = hint.readonlyOutputs,
                )
            },
            isSystemBuiltIn = def.ownerType == AttributeOwnerType.SYSTEM,
            isEditable = def.ownerType != AttributeOwnerType.SYSTEM,
            isDeletable = def.ownerType != AttributeOwnerType.SYSTEM,
        )
    }

    private fun toRuleDetail(rule: RuleDefinition): RuleDetailUiModel {
        val isSystemBuiltIn = isSystemRule(rule)
        return RuleDetailUiModel(
            id = rule.id,
            name = rule.name,
            icon = "rule",
            sourceLabel = if (isSystemBuiltIn) "系统规则" else "自定义规则",
            ruleType = mapRuleType(rule.computationType).displayName,
            triggerModes = resolveEffectiveRuleTriggerModes(rule).map(::ruleTriggerModeLabel),
            inputSlots = rule.inputSlots().map(::toRuleSlotSummaryUiModel),
            outputSlots = rule.outputSlots().map(::toRuleSlotSummaryUiModel),
            outputStrategies = resolveEffectiveRuleOutputTargets(rule).map { output ->
                RuleOutputStrategySummaryUiModel(
                    slotKey = output.outputKey,
                    targetLabel = when (output.targetType) {
                        RuleOutputTargetType.READONLY_RESULT -> "只读结果"
                        RuleOutputTargetType.ATTRIBUTE_VALUE -> {
                            "属性值${output.attributeKey?.let { "($it)" }.orEmpty()}"
                        }
                        RuleOutputTargetType.SYSTEM_VARIABLE -> {
                            output.variableKey
                                ?.let(::findSystemVariableDefinition)
                                ?.key
                                ?.displayName
                                ?: output.variableKey?.displayName
                                ?: "系统变量"
                        }
                    },
                    updateModeLabel = rule.outputStrategies
                        .firstOrNull { it.slotKey == output.outputKey }
                        ?.updateMode
                        ?.let(::ruleOutputUpdateModeLabel)
                        ?: ruleOutputUpdateModeLabel(RuleOutputUpdateMode.OVERWRITE),
                )
            },
            expression = rule.expression,
            description = rule.description,
            boundAttributes = findAttributesBoundToRule(rule).map { it.name },
            boundAttributeCountText = "当前绑定 ${findAttributesBoundToRule(rule).size} 个属性",
            isSystemBuiltIn = isSystemBuiltIn,
            isEditable = !isSystemBuiltIn,
            isDeletable = !isSystemBuiltIn,
        )
    }

    private suspend fun syncRuleBindingsForRule(rule: RuleDefinition) {
        currentAttributes
            .filter { attribute -> attribute.ruleBindings.any { bindingMatchesRule(it.ruleId, rule) } }
            .forEach { attribute ->
                val updatedBindings = attribute.ruleBindings.map { binding ->
                    if (bindingMatchesRule(binding.ruleId, rule)) {
                        syncBindingInstanceWithRule(binding, rule)
                    } else {
                        binding
                    }
                }
                if (updatedBindings != attribute.ruleBindings) {
                    repository.saveAttribute(attribute.copy(ruleBindings = updatedBindings))
                }
            }
    }

    private fun syncBindingInstanceWithRule(
        binding: RuleBinding,
        rule: RuleDefinition,
    ): RuleBinding {
        val slotDefinitionsByKey = rule.slots.associateBy { it.key }
        val existingByKey = binding.slotBindings.associateBy { it.slotKey }
        val resolvedEntrySlotKey = slotDefinitionsByKey[binding.entrySlotKey]?.key
            ?: rule.inputSlots().firstOrNull()?.key
            ?: binding.entrySlotKey
        val synchronizedBindings = rule.slots.mapNotNull { slot ->
            when (slot.direction) {
                RuleSlotDirection.INPUT -> when (slot.sourceType) {
                    RuleSlotSourceType.ATTRIBUTE_INPUT -> {
                        val existing = existingByKey[slot.key] as? RuleSlotBinding.AttributeInput
                        existing?.copy(slotKey = slot.key, isRequired = slot.isRequired)
                            ?: RuleSlotBinding.AttributeInput(
                                slotKey = slot.key,
                                attributeNameSnapshot = slot.name,
                                isRequired = slot.isRequired,
                            )
                    }

                    RuleSlotSourceType.CONFIG_INPUT -> {
                        val existing = existingByKey[slot.key] as? RuleSlotBinding.ConfigInput
                        existing?.copy(slotKey = slot.key, isRequired = slot.isRequired)
                            ?: RuleSlotBinding.ConfigInput(
                                slotKey = slot.key,
                                rawValue = "",
                                isRequired = slot.isRequired,
                            )
                    }

                    RuleSlotSourceType.SYSTEM_INPUT -> {
                        val systemVariableKey = slot.systemVariableKey ?: return@mapNotNull null
                        val existing = existingByKey[slot.key] as? RuleSlotBinding.SystemInput
                        existing?.copy(
                            slotKey = slot.key,
                            systemVariableKey = systemVariableKey,
                            isRequired = slot.isRequired,
                        ) ?: RuleSlotBinding.SystemInput(
                            slotKey = slot.key,
                            systemVariableKey = systemVariableKey,
                            isRequired = slot.isRequired,
                        )
                    }

                    else -> null
                }

                RuleSlotDirection.OUTPUT -> when (slot.sourceType) {
                    RuleSlotSourceType.ATTRIBUTE_OUTPUT -> {
                        val updateMode = rule.outputStrategies
                            .firstOrNull { it.slotKey == slot.key }
                            ?.updateMode
                            ?: RuleOutputUpdateMode.OVERWRITE
                        val existing = existingByKey[slot.key] as? RuleSlotBinding.AttributeOutput
                        existing?.copy(
                            slotKey = slot.key,
                            updateMode = updateMode,
                            isRequired = slot.isRequired,
                        ) ?: RuleSlotBinding.AttributeOutput(
                            slotKey = slot.key,
                            attributeNameSnapshot = slot.name,
                            updateMode = updateMode,
                            isRequired = slot.isRequired,
                        )
                    }

                    RuleSlotSourceType.SYSTEM_OUTPUT -> {
                        val systemVariableKey = rule.outputStrategies
                            .firstOrNull { it.slotKey == slot.key }
                            ?.systemVariableKey
                            ?: slot.systemVariableKey
                            ?: return@mapNotNull null
                        val updateMode = rule.outputStrategies
                            .firstOrNull { it.slotKey == slot.key }
                            ?.updateMode
                            ?: RuleOutputUpdateMode.OVERWRITE
                        val existing = existingByKey[slot.key] as? RuleSlotBinding.SystemOutput
                        existing?.copy(
                            slotKey = slot.key,
                            systemVariableKey = systemVariableKey,
                            updateMode = updateMode,
                            isRequired = slot.isRequired,
                        ) ?: RuleSlotBinding.SystemOutput(
                            slotKey = slot.key,
                            systemVariableKey = systemVariableKey,
                            updateMode = updateMode,
                            isRequired = slot.isRequired,
                        )
                    }

                    RuleSlotSourceType.READONLY_OUTPUT -> {
                        val existing = existingByKey[slot.key] as? RuleSlotBinding.ReadonlyOutput
                        existing?.copy(
                            slotKey = slot.key,
                            displayLabel = slot.name,
                            isRequired = slot.isRequired,
                        ) ?: RuleSlotBinding.ReadonlyOutput(
                            slotKey = slot.key,
                            displayLabel = slot.name,
                            isRequired = slot.isRequired,
                        )
                    }

                    else -> null
                }
            }
        }
        return binding.copy(
            ruleId = rule.id,
            entrySlotKey = resolvedEntrySlotKey,
            slotBindings = synchronizedBindings,
        )
    }

    private fun findAttributesBoundToRule(rule: RuleDefinition): List<AttributeDefinition> {
        return currentAttributes.filter { attribute ->
            attribute.ruleBindings.any { bindingMatchesRule(it.ruleId, rule) }
        }
    }

    private fun bindingMatchesRule(bindingRuleId: String, rule: RuleDefinition): Boolean {
        return bindingRuleId == rule.id || bindingRuleId == rule.key
    }

    private fun mapValueType(type: AttributeValueType): AttributeValueTypeFilter = when (type) {
        AttributeValueType.TEXT -> AttributeValueTypeFilter.TEXT
        AttributeValueType.NUMBER -> AttributeValueTypeFilter.NUMBER
        AttributeValueType.DATE -> AttributeValueTypeFilter.DATE
        AttributeValueType.BOOLEAN -> AttributeValueTypeFilter.BOOLEAN
        AttributeValueType.SELECT -> AttributeValueTypeFilter.SELECT
    }

    private fun mapValueSource(source: AttributeValueSource): AttributeValueSourceFilter = when (source) {
        AttributeValueSource.INPUT -> AttributeValueSourceFilter.INPUT
        AttributeValueSource.FIXED -> AttributeValueSourceFilter.FIXED
        AttributeValueSource.SYSTEM -> AttributeValueSourceFilter.SYSTEM
    }

    private fun mapRuleType(type: RuleComputationType): RuleTypeFilter = when (type) {
        RuleComputationType.SUM -> RuleTypeFilter.SUM
        RuleComputationType.DIFFERENCE -> RuleTypeFilter.DIFFERENCE
        RuleComputationType.AVERAGE -> RuleTypeFilter.AVERAGE
        RuleComputationType.CYCLE -> RuleTypeFilter.CYCLE
        RuleComputationType.ACCUMULATION -> RuleTypeFilter.ACCUMULATION
        RuleComputationType.CUSTOM -> RuleTypeFilter.CUSTOM
    }

    private fun sanitizeInputMode(
        valueType: AttributeValueType,
        valueSource: AttributeValueSource,
        inputMode: AttributeInputMode,
        valueProperties: AttributeValueProperties,
    ): AttributeInputMode {
        val supportedModes = supportedInputModes(
            valueType = valueType,
            valueSource = valueSource,
            valueProperties = valueProperties,
        )
        return if (inputMode in supportedModes) inputMode else supportedModes.first()
    }

    private fun isSystemRule(rule: RuleDefinition): Boolean {
        return repository.systemRuleTemplates.any { it.key == rule.key || it.id == rule.id || it.key == rule.id }
    }

    private fun buildValueProperties(draft: AttributeEditorDraftUiModel): AttributeValueProperties {
        val optionItems = parseLineBasedList(draft.optionItemsText)
        val normalizedDefaultValue = draft.defaultValue.trim().ifBlank { null }
        val defaultValue = if (draft.valueSource == AttributeValueSource.FIXED) normalizedDefaultValue else null
        val systemVariableKey = if (draft.valueSource == AttributeValueSource.SYSTEM) {
            draft.systemVariableKey.trim().ifBlank { null }
        } else {
            null
        }
        val decimalPlaces = draft.decimalPlacesText.trim().toIntOrNull()

        return when (draft.valueType) {
            AttributeValueType.TEXT -> AttributeValueProperties(
                systemVariableKey = systemVariableKey,
                defaultValue = defaultValue,
            )

            AttributeValueType.NUMBER -> AttributeValueProperties(
                numberFormat = draft.numberFormat,
                unitCategory = draft.unitCategory.trim().ifBlank { null },
                defaultUnit = draft.defaultUnit.trim().ifBlank { null },
                allowedUnits = draft.defaultUnit.trim().takeIf { it.isNotBlank() }?.let(::listOf) ?: emptyList(),
                decimalPlaces = decimalPlaces,
                allowNegative = draft.allowNegative,
                systemVariableKey = systemVariableKey,
                defaultValue = defaultValue,
            )

            AttributeValueType.DATE -> AttributeValueProperties(
                systemVariableKey = systemVariableKey,
                defaultValue = defaultValue,
            )

            AttributeValueType.BOOLEAN -> AttributeValueProperties(
                trueLabel = draft.trueLabel.trim().ifBlank { null },
                falseLabel = draft.falseLabel.trim().ifBlank { null },
                systemVariableKey = systemVariableKey,
                defaultValue = defaultValue,
            )

            AttributeValueType.SELECT -> AttributeValueProperties(
                optionItems = optionItems,
                selectionOptionSource = AttributeSelectionOptionSource.USER,
                isMultiValue = draft.isMultiSelect,
                systemVariableKey = systemVariableKey,
                defaultValue = defaultValue,
            )
        }
    }

    private fun supportedInputModes(
        valueType: AttributeValueType,
        valueSource: AttributeValueSource,
        valueProperties: AttributeValueProperties,
    ): List<AttributeInputMode> {
        return when (valueType) {
            AttributeValueType.TEXT -> listOf(AttributeInputMode.TEXT_INPUT)
            AttributeValueType.NUMBER -> if (isPriceQuantity(valueProperties)) {
                listOf(AttributeInputMode.PRICE_INPUT, AttributeInputMode.NUMBER_INPUT)
            } else {
                listOf(AttributeInputMode.NUMBER_INPUT)
            }
            AttributeValueType.DATE -> listOf(AttributeInputMode.DATE_PICKER)
            AttributeValueType.BOOLEAN -> listOf(AttributeInputMode.BOOLEAN_SWITCH, AttributeInputMode.TEXT_INPUT)
            AttributeValueType.SELECT -> if (valueProperties.isMultiValue) {
                listOf(AttributeInputMode.MULTI_SELECT)
            } else {
                listOf(AttributeInputMode.SINGLE_SELECT)
            }
        }
    }

    private fun parseLineBasedList(raw: String): List<String> {
        return raw.split('\n', ',', '，', ';', '；')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    private fun parseRuleSystemInputs(raw: String): List<RuleSystemInputDefinition> {
        return raw.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { line ->
                val segments = line.split("=", limit = 2)
                if (segments.size != 2) {
                    return@mapNotNull null
                }
                val role = segments[0].trim()
                val key = findSystemVariableKey(segments[1].trim()) ?: return@mapNotNull null
                RuleSystemInputDefinition(
                    role = role,
                    sourceType = RuleInputSourceType.SYSTEM_VARIABLE,
                    variableKey = key,
                    required = true,
                )
            }
            .distinctBy { it.role to it.variableKey }
            .toList()
    }

    private fun formatRuleSystemInputs(inputs: List<RuleSystemInputDefinition>): String {
        return inputs.joinToString("\n") { input ->
            "${input.role}=${input.variableKey?.storageKey.orEmpty()}"
        }
    }

    private fun parseRuleOutputTargets(raw: String): List<RuleOutputTargetDefinition> {
        return raw.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { line ->
                val segments = line.split("=", limit = 2)
                if (segments.size != 2) {
                    return@mapNotNull null
                }
                val outputKey = segments[0].trim()
                val targetSpec = segments[1].trim()
                when {
                    targetSpec == "READONLY_RESULT" -> RuleOutputTargetDefinition(
                        outputKey = outputKey,
                        targetType = RuleOutputTargetType.READONLY_RESULT,
                    )

                    targetSpec.startsWith("SYSTEM_VARIABLE:", ignoreCase = true) -> RuleOutputTargetDefinition(
                        outputKey = outputKey,
                        targetType = RuleOutputTargetType.SYSTEM_VARIABLE,
                        variableKey = findSystemVariableKey(
                            targetSpec.substringAfter(":", "").trim()
                        ),
                    )

                    targetSpec.startsWith("ATTRIBUTE_VALUE:", ignoreCase = true) -> RuleOutputTargetDefinition(
                        outputKey = outputKey,
                        targetType = RuleOutputTargetType.ATTRIBUTE_VALUE,
                        attributeKey = targetSpec.substringAfter(":", "").trim().ifBlank { null },
                    )

                    else -> null
                }
            }
            .distinctBy { it.outputKey to it.targetType to it.variableKey to it.attributeKey }
            .toList()
    }

    private fun formatRuleOutputTargets(targets: List<RuleOutputTargetDefinition>): String {
        return targets.joinToString("\n") { target ->
            when (target.targetType) {
                RuleOutputTargetType.READONLY_RESULT -> "${target.outputKey}=READONLY_RESULT"
                RuleOutputTargetType.ATTRIBUTE_VALUE ->
                    "${target.outputKey}=ATTRIBUTE_VALUE:${target.attributeKey.orEmpty()}"

                RuleOutputTargetType.SYSTEM_VARIABLE ->
                    "${target.outputKey}=SYSTEM_VARIABLE:${target.variableKey?.storageKey.orEmpty()}"
            }
        }
    }

    private fun buildValueDefinitionUiModel(
        valueType: AttributeValueType,
        valueSource: AttributeValueSource,
        interactionMode: AttributeInputMode,
        valueProperties: AttributeValueProperties,
    ): AttributeValueDefinitionUiModel {
        return AttributeValueDefinitionUiModel(
            valueType = mapValueType(valueType).displayName,
            valueSource = mapValueSource(valueSource).displayName,
            interactionMode = "",
            properties = buildValuePropertyUiModels(valueType, valueSource, interactionMode, valueProperties),
        )
    }

    private fun buildValuePropertySummary(
        valueType: AttributeValueType,
        valueSource: AttributeValueSource,
        interactionMode: AttributeInputMode,
        valueProperties: AttributeValueProperties,
    ): String {
        val summary = buildValuePropertyUiModels(valueType, valueSource, interactionMode, valueProperties)
            .take(2)
            .joinToString(" / ") { "${it.label}：${it.value}" }
        return summary.ifBlank { "无特殊配置" }
    }

    private fun buildValuePropertyUiModels(
        valueType: AttributeValueType,
        valueSource: AttributeValueSource,
        interactionMode: AttributeInputMode,
        valueProperties: AttributeValueProperties,
    ): List<AttributeValuePropertyUiModel> {
        val items = mutableListOf<AttributeValuePropertyUiModel>()
        val systemVariableOption = findSystemVariableOption(valueProperties.systemVariableKey)

        fun addIfNotBlank(label: String, value: String?) {
            val normalized = value?.trim().orEmpty()
            if (normalized.isNotEmpty()) {
                items += AttributeValuePropertyUiModel(label, normalized)
            }
        }

        when (valueType) {
            AttributeValueType.TEXT -> Unit

            AttributeValueType.NUMBER -> {
                addIfNotBlank("数值格式", valueProperties.numberFormat?.let { numberFormatLabel(it) })
                if (valueProperties.numberFormat == AttributeNumberFormat.WITH_UNIT || valueProperties.numberFormat == AttributeNumberFormat.PRICE) {
                    addIfNotBlank("单位类型", valueProperties.unitCategory ?: if (valueProperties.numberFormat == AttributeNumberFormat.PRICE) "价格" else null)
                    addIfNotBlank("具体单位", valueProperties.defaultUnit)
                }
                valueProperties.decimalPlaces?.let {
                    items += AttributeValuePropertyUiModel("小数位", it.toString())
                }
                if (!valueProperties.allowNegative) {
                    items += AttributeValuePropertyUiModel("允许负数", "否")
                }
            }

            AttributeValueType.DATE -> Unit

            AttributeValueType.BOOLEAN -> {
                items += AttributeValuePropertyUiModel("交互方式", booleanInputModeLabel(interactionMode))
                addIfNotBlank("真值文案", valueProperties.trueLabel)
                addIfNotBlank("假值文案", valueProperties.falseLabel)
            }

            AttributeValueType.SELECT -> {
                items += AttributeValuePropertyUiModel(
                    "选择方式",
                    if (valueProperties.isMultiValue) "多选" else "单选",
                )
                if (valueProperties.optionItems.isNotEmpty()) {
                    items += AttributeValuePropertyUiModel(
                        "选项",
                        valueProperties.optionItems.joinToString(" / "),
                    )
                }
            }
        }

        if (valueSource == AttributeValueSource.FIXED) {
            addIfNotBlank("固定值", valueProperties.defaultValue)
        }
        if (valueSource == AttributeValueSource.SYSTEM) {
            addIfNotBlank("系统变量", systemVariableOption?.displayName ?: valueProperties.systemVariableKey)
            addIfNotBlank("变量状态", systemVariableOption?.statusLabel)
            addIfNotBlank("绑定说明", systemVariableOption?.availabilityMessage)
        }
        return items
    }

    private suspend fun loadSystemVariableCatalog() {
        val snapshotVariables = runCatching {
            appSystemSourceRepository.getSnapshot().variables
        }.getOrElse { emptyList() }
        currentSystemVariables = buildSystemVariableOptions(snapshotVariables)
        refresh()
    }

    private fun availableSystemVariableOptions(): List<SystemVariableOptionUiModel> {
        return currentSystemVariables.ifEmpty { buildFallbackSystemVariableOptions() }
    }

    private fun buildFallbackSystemVariableOptions(): List<SystemVariableOptionUiModel> {
        return buildSystemVariableOptions(emptyList())
    }

    private fun buildSystemVariableOptions(
        snapshotVariables: List<AppSystemSourceValue>,
    ): List<SystemVariableOptionUiModel> {
        val snapshotByKey = snapshotVariables.associateBy { it.variableKey.storageKey }
        return builtInSystemVariableDefinitions()
            .filter { it.implementationStatus != SystemVariableImplementationStatus.PLANNED }
            .map { definition ->
                val snapshot = snapshotByKey[definition.key.storageKey]
                val isImplemented = definition.implementationStatus == SystemVariableImplementationStatus.IMPLEMENTED
                val availabilityMessage = when {
                    snapshot?.availabilityMessage?.isNotBlank() == true -> snapshot.availabilityMessage
                    definition.implementationStatus == SystemVariableImplementationStatus.DEGRADED ->
                        "当前为降级能力，暂不开放在属性管理页直接绑定"
                    !isImplemented -> "该系统级变量当前未开放属性绑定"
                    else -> null
                }
                SystemVariableOptionUiModel(
                    key = definition.key.storageKey,
                    displayName = definition.key.displayName,
                    valueType = definition.valueType,
                    valueTypeLabel = mapValueType(definition.valueType).displayName,
                    statusLabel = when (definition.implementationStatus) {
                        SystemVariableImplementationStatus.IMPLEMENTED -> "已实现"
                        SystemVariableImplementationStatus.DEGRADED -> "降级能力"
                        SystemVariableImplementationStatus.PLANNED -> "规划中"
                    },
                    description = definition.description.orEmpty(),
                    availabilityMessage = availabilityMessage,
                    isSelectable = isImplemented && (snapshot?.isAvailable != false),
                )
            }
    }

    private fun findSystemVariableOption(storageKey: String?): SystemVariableOptionUiModel? {
        val normalizedKey = storageKey?.trim().orEmpty()
        if (normalizedKey.isBlank()) {
            return null
        }
        return availableSystemVariableOptions().firstOrNull { it.key == normalizedKey }
    }

    private fun inferAttributeValueTypeForRuleSlot(slotValueType: RuleSlotValueType): AttributeValueType {
        return when (slotValueType) {
            RuleSlotValueType.TEXT -> AttributeValueType.TEXT
            RuleSlotValueType.NUMBER -> AttributeValueType.NUMBER
            RuleSlotValueType.DATE,
            RuleSlotValueType.SYSTEM_DATE_TIME -> AttributeValueType.DATE
            RuleSlotValueType.BOOLEAN -> AttributeValueType.BOOLEAN
            RuleSlotValueType.SELECT -> AttributeValueType.SELECT
            RuleSlotValueType.CYCLE_UNIT -> AttributeValueType.SELECT
        }
    }

    private fun buildQuickCreatedAttributeDefinition(
        pendingAttribute: PendingCreatedAttributeUiModel,
    ): AttributeDefinition {
        val valueProperties = when (pendingAttribute.slotValueType) {
            RuleSlotValueType.CYCLE_UNIT -> AttributeValueProperties(
                optionItems = listOf("天", "周", "月", "季", "年"),
                selectionOptionSource = AttributeSelectionOptionSource.USER,
                isMultiValue = false,
            )
            RuleSlotValueType.SELECT -> AttributeValueProperties(
                optionItems = emptyList(),
                selectionOptionSource = AttributeSelectionOptionSource.USER,
                isMultiValue = false,
            )
            RuleSlotValueType.NUMBER -> AttributeValueProperties(
                numberFormat = AttributeNumberFormat.PLAIN,
                allowNegative = true,
            )
            RuleSlotValueType.BOOLEAN -> AttributeValueProperties()
            RuleSlotValueType.TEXT,
            RuleSlotValueType.DATE,
            RuleSlotValueType.SYSTEM_DATE_TIME -> AttributeValueProperties()
        }
        val inputMode = supportedInputModes(
            valueType = pendingAttribute.valueType,
            valueSource = AttributeValueSource.INPUT,
            valueProperties = valueProperties,
        ).first()
        return AttributeDefinition(
            id = pendingAttribute.id,
            key = pendingAttribute.key,
            name = pendingAttribute.name,
            ownerType = AttributeOwnerType.CUSTOM,
            valueType = pendingAttribute.valueType,
            optionSource = AttributeValueSource.INPUT.toLegacyOptionSource(valueProperties),
            inputMode = inputMode,
            isMultiValue = valueProperties.isMultiValue,
            optionItems = valueProperties.optionItems,
            valueProperties = valueProperties,
            valueSource = AttributeValueSource.INPUT,
            interactionMode = inputMode,
            icon = iconForValueType(pendingAttribute.valueType, valueProperties),
            templateId = null,
            ruleBindings = emptyList(),
            description = "由规则绑定快捷创建",
        )
    }

    private fun attributeInputModeLabel(mode: AttributeInputMode): String = when (mode) {
        AttributeInputMode.TEXT_INPUT -> "文本输入"
        AttributeInputMode.NUMBER_INPUT -> "数值输入"
        AttributeInputMode.PRICE_INPUT -> "价格输入"
        AttributeInputMode.DATE_PICKER -> "日期选择"
        AttributeInputMode.BOOLEAN_SWITCH -> "布尔开关"
        AttributeInputMode.SINGLE_SELECT -> "单选"
        AttributeInputMode.MULTI_SELECT -> "多选"
        AttributeInputMode.TAG_INPUT -> "标签输入"
    }

    private fun numberFormatLabel(format: AttributeNumberFormat): String = when (format) {
        AttributeNumberFormat.PLAIN -> "普通数值"
        AttributeNumberFormat.PRICE -> "量值"
        AttributeNumberFormat.PERCENTAGE -> "百分比"
        AttributeNumberFormat.WITH_UNIT -> "量值"
    }

    private fun booleanInputModeLabel(mode: AttributeInputMode): String = when (mode) {
        AttributeInputMode.BOOLEAN_SWITCH -> "按钮"
        AttributeInputMode.TEXT_INPUT -> "输入"
        else -> attributeInputModeLabel(mode)
    }

    private fun ruleTriggerModeLabel(mode: RuleTriggerMode): String = when (mode) {
        RuleTriggerMode.ON_ATTRIBUTE_SELECTED -> "添加属性时"
        RuleTriggerMode.ON_VALUE_CHANGED -> "属性值变更时"
        RuleTriggerMode.ON_FORM_OPENED -> "表单打开时"
        RuleTriggerMode.ON_SAVE -> "保存物品时"
        RuleTriggerMode.ON_SYSTEM_INPUT_CHANGED -> "系统输入变化时"
    }

    private fun ruleSlotDirectionLabel(direction: RuleSlotDirection): String = when (direction) {
        RuleSlotDirection.INPUT -> "输入"
        RuleSlotDirection.OUTPUT -> "输出"
    }

    private fun ruleSlotValueTypeLabel(type: RuleSlotValueType): String = when (type) {
        RuleSlotValueType.TEXT -> "文本"
        RuleSlotValueType.NUMBER -> "数值"
        RuleSlotValueType.DATE -> "日期"
        RuleSlotValueType.BOOLEAN -> "布尔"
        RuleSlotValueType.SELECT -> "选择"
        RuleSlotValueType.CYCLE_UNIT -> "周期单位"
        RuleSlotValueType.SYSTEM_DATE_TIME -> "系统时间"
    }

    private fun ruleSlotSourceTypeLabel(type: RuleSlotSourceType): String = when (type) {
        RuleSlotSourceType.ATTRIBUTE_INPUT -> "属性输入"
        RuleSlotSourceType.CONFIG_INPUT -> "配置输入"
        RuleSlotSourceType.SYSTEM_INPUT -> "系统输入"
        RuleSlotSourceType.ATTRIBUTE_OUTPUT -> "属性输出"
        RuleSlotSourceType.READONLY_OUTPUT -> "只读输出"
        RuleSlotSourceType.SYSTEM_OUTPUT -> "系统输出"
    }

    private fun ruleOutputUpdateModeLabel(mode: RuleOutputUpdateMode): String = when (mode) {
        RuleOutputUpdateMode.OVERWRITE -> "覆盖"
        RuleOutputUpdateMode.ACCUMULATE -> "累计"
    }

    private fun editorNumberFormat(format: AttributeNumberFormat?): AttributeNumberFormat {
        return when (format) {
            AttributeNumberFormat.PRICE -> AttributeNumberFormat.WITH_UNIT
            null -> AttributeNumberFormat.PLAIN
            else -> format
        }
    }

    private fun isPriceQuantity(valueProperties: AttributeValueProperties): Boolean {
        val unitCategory = valueProperties.unitCategory.orEmpty()
        return valueProperties.numberFormat == AttributeNumberFormat.PRICE ||
            (valueProperties.numberFormat == AttributeNumberFormat.WITH_UNIT && unitCategory == "价格")
    }

    private fun iconForValueType(
        valueType: AttributeValueType,
        valueProperties: AttributeValueProperties,
    ): String {
        return when (valueType) {
            AttributeValueType.TEXT -> "notes"
            AttributeValueType.NUMBER -> if (isPriceQuantity(valueProperties)) "payments" else "straighten"
            AttributeValueType.DATE -> "event"
            AttributeValueType.BOOLEAN -> "toggle_on"
            AttributeValueType.SELECT -> "checklist"
        }
    }

    private fun buildAttributeKey(name: String): String {
        val normalized = name.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "attr" }
        return "${normalized}_${System.currentTimeMillis()}"
    }

    private fun buildRuleKey(name: String): String {
        val normalized = name.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "rule" }
        return "${normalized}_${System.currentTimeMillis()}"
    }

    private fun generateAttributeId(): String {
        return "attr_${UUID.randomUUID().toString().replace("-", "")}"
    }

    private fun generateRuleId(): String {
        return "rule_${UUID.randomUUID().toString().replace("-", "")}"
    }

    private fun inferRuleSlotValueType(
        token: String,
        systemInput: Boolean = false,
    ): RuleSlotValueType {
        val normalized = token.trim().lowercase(Locale.ROOT)
        if (systemInput) {
            return RuleSlotValueType.SYSTEM_DATE_TIME
        }
        return when {
            normalized.contains("date") || normalized.contains("time") || normalized.contains("day") -> RuleSlotValueType.DATE
            normalized.contains("count") || normalized.contains("amount") || normalized.contains("price") ||
                normalized.contains("total") || normalized.contains("deposit") || normalized.contains("value") ||
                normalized.contains("step") || normalized.contains("paid") -> RuleSlotValueType.NUMBER
            normalized.contains("unit") || normalized.contains("cycleunit") -> RuleSlotValueType.CYCLE_UNIT
            normalized.startsWith("is") || normalized.contains("flag") || normalized.contains("enabled") -> RuleSlotValueType.BOOLEAN
            else -> RuleSlotValueType.TEXT
        }
    }

    private fun postMessage(message: String) {
        _uiState.value = _uiState.value.copy(message = message)
    }

    private fun <T : Enum<T>> nextEnum(current: T, values: Array<T>): T {
        return values[(current.ordinal + 1) % values.size]
    }
}

private data class AttributeCatalogEntry(
    val id: String,
    val name: String,
    val icon: String,
    val source: AttributeSourceFilter,
    val valueType: AttributeValueTypeFilter,
    val valueSource: AttributeValueSourceFilter,
    val interactionMode: String,
    val propertySummary: String,
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
        val valueSourceMatches = filters.valueSource == AttributeValueSourceFilter.ALL || filters.valueSource == valueSource
        val ruleMatches = when (filters.ruleBinding) {
            RuleBindingFilter.ALL -> true
            RuleBindingFilter.BOUND -> ruleBindings.isNotEmpty()
            RuleBindingFilter.UNBOUND -> ruleBindings.isEmpty()
        }
        return searchMatches && sourceMatches && valueTypeMatches && valueSourceMatches && ruleMatches
    }

    fun toListItemUiModel(): AttributeListItemUiModel {
        return AttributeListItemUiModel(
            id = id,
            name = name,
            icon = icon,
            sourceLabel = if (source == AttributeSourceFilter.SYSTEM) "系统" else "自定义",
            valueTypeLabel = valueType.displayName,
            valueSourceLabel = valueSource.displayName,
            interactionModeLabel = interactionMode,
            propertySummary = propertySummary,
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
    val source: RuleSourceFilter,
    val triggerSummary: String,
    val slotCountSummary: String,
    val inputSummary: String,
    val outputSummary: String,
    val searchSummary: String,
    val sourceLabel: String,
    val isSystemBuiltIn: Boolean,
    val description: String?,
    val affectedAttributeCount: Int,
    val affectedAttributeNames: List<String>,
) {
    fun matches(filters: RuleListFilters, searchQuery: String): Boolean {
        val search = searchQuery.trim()
        val searchMatches = search.isBlank() ||
            name.contains(search, ignoreCase = true) ||
            ruleType.displayName.contains(search, ignoreCase = true) ||
            triggerSummary.contains(search, ignoreCase = true) ||
            slotCountSummary.contains(search, ignoreCase = true) ||
            inputSummary.contains(search, ignoreCase = true) ||
            outputSummary.contains(search, ignoreCase = true) ||
            searchSummary.contains(search, ignoreCase = true) ||
            sourceLabel.contains(search, ignoreCase = true) ||
            affectedAttributeNames.any { it.contains(search, ignoreCase = true) } ||
            (description?.contains(search, ignoreCase = true) == true)
        val sourceMatches = filters.source == RuleSourceFilter.ALL || filters.source == source
        val ruleTypeMatches = filters.ruleType == RuleTypeFilter.ALL || filters.ruleType == ruleType
        val usageMatches = when (filters.usage) {
            RuleUsageFilter.ALL -> true
            RuleUsageFilter.BOUND -> affectedAttributeCount > 0
            RuleUsageFilter.UNBOUND -> affectedAttributeCount == 0
        }
        return searchMatches && sourceMatches && ruleTypeMatches && usageMatches
    }

    fun toListItemUiModel(): RuleListItemUiModel {
        return RuleListItemUiModel(
            id = id,
            name = name,
            icon = icon,
            ruleTypeLabel = ruleType.displayName,
            triggerSummary = triggerSummary,
            slotCountSummary = slotCountSummary,
            inputSummary = inputSummary,
            outputSummary = outputSummary,
            sourceLabel = sourceLabel,
            isSystemBuiltIn = isSystemBuiltIn,
            affectedAttributeCount = affectedAttributeCount,
            affectedAttributeCountText = "绑定属性：$affectedAttributeCount 个",
            affectedAttributeNames = affectedAttributeNames,
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
    val valueSource: AttributeValueSourceFilter,
    val interactionMode: String,
    val propertySummary: String,
    val badgeText: String,
    val summaryLine1: String,
    val summaryLine2: String,
    val countText: String,
    val usageHint: String,
    val defaultRuleBindings: List<String>,
    val triggerModes: List<String>,
    val inputSlots: List<RuleSlotSummaryUiModel>,
    val outputSlots: List<RuleSlotSummaryUiModel>,
    val outputStrategies: List<RuleOutputStrategySummaryUiModel>,
    val expression: String?,
    val description: String?,
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
            TemplateUiType.ATTRIBUTE -> TemplateDetailUiModel.AttributeTemplateDetail(
                id = id,
                name = name,
                icon = icon,
                isSystemBuiltIn = isSystemBuiltIn,
                categoryLabel = templateCategory.displayName,
                defaultDefinition = AttributeValueDefinitionUiModel(
                    valueType = valueType.displayName,
                    valueSource = valueSource.displayName,
                    interactionMode = interactionMode,
                    properties = if (propertySummary.isBlank()) {
                        emptyList()
                    } else {
                        listOf(AttributeValuePropertyUiModel("值属性", propertySummary))
                    },
                ),
                defaultRuleBindings = defaultRuleBindings,
                derivedAttributeCountText = countText,
                usageHint = usageHint,
            )

            TemplateUiType.RULE -> TemplateDetailUiModel.RuleTemplateDetail(
                id = id,
                name = name,
                icon = icon,
                isSystemBuiltIn = isSystemBuiltIn,
                ruleType = ruleType.displayName,
                triggerModes = triggerModes,
                inputSlots = inputSlots,
                outputSlots = outputSlots,
                outputStrategies = outputStrategies,
                expression = expression,
                description = description ?: usageHint,
                referenceCountText = countText,
            )
        }
    }
}
