package com.example.itemmanagement.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.itemmanagement.data.entity.attribute.AttributeDefinitionEntity
import com.example.itemmanagement.data.model.attribute.AttributeInputMode
import com.example.itemmanagement.data.model.attribute.AttributeValueType
import com.example.itemmanagement.data.model.attribute.RuleActivationMode
import com.example.itemmanagement.data.model.attribute.RuleBindingCreationSource
import com.example.itemmanagement.data.model.attribute.RuleBindingInstance
import com.example.itemmanagement.data.model.attribute.RuleDefinition
import com.example.itemmanagement.data.model.attribute.RuleOutputTargetType
import com.example.itemmanagement.data.model.attribute.RuleRuntimeEvaluationContext
import com.example.itemmanagement.data.model.attribute.RuleSlotBinding
import com.example.itemmanagement.data.model.attribute.RuleSlotDefinition
import com.example.itemmanagement.data.model.attribute.RuleSlotDirection
import com.example.itemmanagement.data.model.attribute.RuleSlotSourceType
import com.example.itemmanagement.data.model.attribute.RuleSlotValueType
import com.example.itemmanagement.data.model.attribute.SystemVariableKey
import com.example.itemmanagement.data.model.attribute.builtInSystemVariableDefinitions
import com.example.itemmanagement.data.model.attribute.evaluateRuleRuntimeOutput
import com.example.itemmanagement.data.model.attribute.inputSlots
import com.example.itemmanagement.data.model.attribute.outputSlots
import com.example.itemmanagement.data.model.attribute.resolveItemRuleOutputProjections
import com.example.itemmanagement.data.model.attribute.resolveRuleOutputTarget
import com.example.itemmanagement.ui.base.BaseItemViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

data class ItemAnchoredRuleToggleState(
    val binding: RuleBindingInstance,
    val ruleName: String,
    val label: String,
    val supportText: String? = null,
)

fun itemBuildReadonlyRuleOutputFieldsFromBindings(
    itemRuleBindings: List<RuleBindingInstance>,
    allDefinitions: List<AttributeDefinitionEntity>,
    ruleDefinitions: List<RuleDefinition> = emptyList(),
): List<String> {
    return resolveItemRuleOutputProjections(
        bindings = itemRuleBindings.filter(RuleBindingInstance::isRuntimeActive),
        allAttributes = allDefinitions,
        fieldNameProvider = ::itemCustomFieldName,
        ruleDefinitions = ruleDefinitions,
    ).mapNotNull { projection ->
        if (projection.targetType == RuleOutputTargetType.ATTRIBUTE_VALUE) {
            null
        } else {
            itemRuleOutputFieldName(projection.attributeId, projection.outputKey)
        }
    }
}

fun itemResolveDerivedAttributeFieldNamesFromBindings(
    itemRuleBindings: List<RuleBindingInstance>,
    allDefinitions: List<AttributeDefinitionEntity>,
    ruleDefinitions: List<RuleDefinition> = emptyList(),
): Set<String> {
    return resolveItemRuleOutputProjections(
        bindings = itemRuleBindings.filter(RuleBindingInstance::isRuntimeActive),
        allAttributes = allDefinitions,
        fieldNameProvider = ::itemCustomFieldName,
        ruleDefinitions = ruleDefinitions,
    ).mapNotNull { projection ->
        if (projection.targetType == RuleOutputTargetType.ATTRIBUTE_VALUE) {
            projection.targetFieldName
        } else {
            null
        }
    }.toSet()
}

fun itemApplyRuleRuntimeOutputsFromBindings(
    viewModel: BaseItemViewModel,
    itemRuleBindings: List<RuleBindingInstance>,
    allDefinitions: List<AttributeDefinitionEntity>,
    ruleDefinitions: List<RuleDefinition>,
) {
    if (itemRuleBindings.isEmpty() || allDefinitions.isEmpty() || ruleDefinitions.isEmpty()) {
        return
    }
    repeat(3) {
        val activeBindings = itemRuleBindings.filter(RuleBindingInstance::isRuntimeActive)
        val allProjections = resolveItemRuleOutputProjections(
            bindings = itemRuleBindings,
            allAttributes = allDefinitions,
            fieldNameProvider = ::itemCustomFieldName,
            ruleDefinitions = ruleDefinitions,
        )
        val activeProjections = resolveItemRuleOutputProjections(
            bindings = activeBindings,
            allAttributes = allDefinitions,
            fieldNameProvider = ::itemCustomFieldName,
            ruleDefinitions = ruleDefinitions,
        )
        val fieldUpdates = linkedMapOf<String, Any?>()
        val systemUpdates = linkedMapOf<com.example.itemmanagement.data.model.attribute.SystemVariableKey, Any?>()
        val activeFieldTargets = activeProjections.mapNotNull { it.targetFieldName }.toSet()
        val allFieldTargets = allProjections.mapNotNull { it.targetFieldName }.toSet()
        allFieldTargets
            .filterNot(activeFieldTargets::contains)
            .forEach { fieldName -> fieldUpdates[fieldName] = null }
        val activeSystemTargets = activeProjections.mapNotNull { it.systemVariableKey }.toSet()
        val allSystemTargets = allProjections.mapNotNull { it.systemVariableKey }.toSet()
        allSystemTargets
            .filterNot(activeSystemTargets::contains)
            .forEach { key -> systemUpdates[key] = null }
        activeProjections.forEach { projection ->
            val binding = activeBindings.firstOrNull { it.id == projection.attributeId } ?: return@forEach
            val rule = ruleDefinitions.firstOrNull { it.id == binding.ruleId || it.key == binding.ruleId } ?: return@forEach
            val triggerDefinition = itemResolveTriggerDefinition(binding, allDefinitions) ?: return@forEach
            val runtimeOutput = evaluateRuleRuntimeOutput(
                RuleRuntimeEvaluationContext(
                    triggerDefinition = triggerDefinition,
                    rule = rule,
                    binding = binding,
                    outputKey = projection.outputKey,
                    allDefinitions = allDefinitions,
                    fieldNameProvider = ::itemCustomFieldName,
                    fieldValueProvider = viewModel::getFieldValue,
                    systemValueProvider = { key -> viewModel.getRuntimeSystemValue(key) },
                )
            )
            when (projection.targetType) {
                RuleOutputTargetType.READONLY_RESULT -> Unit
                RuleOutputTargetType.ATTRIBUTE_VALUE -> {
                    val targetDefinition = projection.targetAttributeId
                        ?.let { targetId -> allDefinitions.firstOrNull { it.id == targetId } }
                    val targetFieldName = projection.targetFieldName ?: targetDefinition?.let(::itemCustomFieldName)
                    targetFieldName?.let {
                        itemBuildAttributeTargetUpdates(
                            definition = targetDefinition,
                            fieldName = it,
                            runtimeOutput = runtimeOutput,
                        ).forEach { (key, value) ->
                            fieldUpdates[key] = value
                        }
                    }
                }

                RuleOutputTargetType.SYSTEM_VARIABLE -> {
                    projection.systemVariableKey?.let { key ->
                        systemUpdates[key] = itemBuildSystemTargetValue(key, runtimeOutput.value)
                    }
                }
            }
        }
        if (!viewModel.applyRuleRuntimeState(fieldUpdates, systemUpdates)) {
            return
        }
    }
}

fun itemResolveRuleOutputStateFromBindings(
    bindingId: String,
    outputKey: String,
    viewModel: BaseItemViewModel,
    itemRuleBindings: List<RuleBindingInstance>,
    allDefinitions: List<AttributeDefinitionEntity>,
    ruleDefinitions: List<RuleDefinition>,
): ItemReadonlyRuleOutputState {
    val binding = itemRuleBindings.firstOrNull { it.id == bindingId }
        ?: return ItemReadonlyRuleOutputState(
            title = outputKey,
            value = "",
            placeholder = "未找到对应的物品规则实例",
            supportText = bindingId,
        )
    val rule = ruleDefinitions.firstOrNull { it.id == binding.ruleId || it.key == binding.ruleId }
        ?: return ItemReadonlyRuleOutputState(
            title = outputKey,
            value = "",
            placeholder = "未找到规则定义，当前无法计算",
            supportText = binding.ruleId,
        )
    if (!binding.isRuntimeActive) {
        return ItemReadonlyRuleOutputState(
            title = outputKey,
            value = "",
            placeholder = "当前规则已停用",
            supportText = rule.name,
        )
    }
    val triggerDefinition = itemResolveTriggerDefinition(binding, allDefinitions)
        ?: return ItemReadonlyRuleOutputState(
            title = outputKey,
            value = "",
            placeholder = "规则入口槽位还未绑定属性",
            supportText = rule.name,
        )
    val runtimeOutput = evaluateRuleRuntimeOutput(
        RuleRuntimeEvaluationContext(
            triggerDefinition = triggerDefinition,
            rule = rule,
            binding = binding,
            outputKey = outputKey,
            allDefinitions = allDefinitions,
            fieldNameProvider = ::itemCustomFieldName,
            fieldValueProvider = viewModel::getFieldValue,
            systemValueProvider = { key -> viewModel.getRuntimeSystemValue(key) },
        )
    )
    val target = resolveRuleOutputTarget(rule.id, outputKey)
    val supportText = buildList {
        runtimeOutput.supportText?.takeIf { it.isNotBlank() }?.let(::add)
        when (target.targetType) {
            RuleOutputTargetType.READONLY_RESULT -> Unit
            RuleOutputTargetType.ATTRIBUTE_VALUE -> add("结果会自动写入目标属性")
            RuleOutputTargetType.SYSTEM_VARIABLE -> add("结果会写入系统变量")
        }
    }.joinToString(" / ").ifBlank { null }
    return ItemReadonlyRuleOutputState(
        title = runtimeOutput.title,
        value = runtimeOutput.value,
        placeholder = runtimeOutput.placeholder,
        supportText = supportText,
    )
}

fun buildInitialItemRuleBinding(rule: RuleDefinition): RuleBindingInstance {
    val slotBindings = rule.slots.mapNotNull { slot ->
        when (slot.sourceType) {
            RuleSlotSourceType.ATTRIBUTE_INPUT -> RuleSlotBinding.AttributeInput(
                slotKey = slot.key,
                isRequired = slot.isRequired,
            )

            RuleSlotSourceType.CONFIG_INPUT -> RuleSlotBinding.ConfigInput(
                slotKey = slot.key,
                rawValue = "",
                isRequired = slot.isRequired,
            )

            RuleSlotSourceType.SYSTEM_INPUT -> slot.systemVariableKey?.let { key ->
                RuleSlotBinding.SystemInput(
                    slotKey = slot.key,
                    systemVariableKey = key,
                    isRequired = slot.isRequired,
                )
            }

            RuleSlotSourceType.ATTRIBUTE_OUTPUT -> RuleSlotBinding.AttributeOutput(
                slotKey = slot.key,
                isRequired = slot.isRequired,
            )

            RuleSlotSourceType.READONLY_OUTPUT -> RuleSlotBinding.ReadonlyOutput(
                slotKey = slot.key,
                displayLabel = slot.name,
                isRequired = slot.isRequired,
            )

            RuleSlotSourceType.SYSTEM_OUTPUT -> slot.systemVariableKey?.let { key ->
                RuleSlotBinding.SystemOutput(
                    slotKey = slot.key,
                    systemVariableKey = key,
                    isRequired = slot.isRequired,
                )
            }
        }
    }
    val defaultEntrySlot = rule.inputSlots()
        .firstOrNull { it.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT }
        ?.key
        ?: rule.inputSlots().firstOrNull()?.key
        ?: "entry"
    return RuleBindingInstance(
        id = "item_rule_${UUID.randomUUID()}",
        ruleId = rule.id,
        entrySlotKey = defaultEntrySlot,
        slotBindings = slotBindings,
        togglePlacementSlotKey = rule.toggleUiConfig?.anchorSlotKey,
        isEnabled = rule.toggleUiConfig?.defaultEnabled ?: true,
        creationSource = RuleBindingCreationSource.RULE_TEMPLATE,
    )
}

fun itemRuleMissingRequiredSlots(
    binding: RuleBindingInstance,
    rule: RuleDefinition?,
): List<RuleSlotDefinition> {
    if (rule == null) {
        return emptyList()
    }
    val bindingByKey = binding.slotBindings.associateBy { it.slotKey }
    return rule.slots.filter { slot ->
        if (!slot.isRequired) {
            return@filter false
        }
        when (val slotBinding = bindingByKey[slot.key]) {
            is RuleSlotBinding.AttributeInput -> slotBinding.attributeId.isNullOrBlank()
            is RuleSlotBinding.ConfigInput -> slotBinding.rawValue.isBlank()
            is RuleSlotBinding.SystemInput -> slotBinding.systemVariableKey == null
            is RuleSlotBinding.AttributeOutput -> slotBinding.attributeId.isNullOrBlank()
            is RuleSlotBinding.ReadonlyOutput -> false
            is RuleSlotBinding.SystemOutput -> slotBinding.systemVariableKey == null
            null -> slot.sourceType != RuleSlotSourceType.READONLY_OUTPUT
        }
    }
}

private fun itemResolveTriggerDefinition(
    binding: RuleBindingInstance,
    allDefinitions: List<AttributeDefinitionEntity>,
): AttributeDefinitionEntity? {
    return binding.entryAttributeId
        ?.let { entryId -> allDefinitions.firstOrNull { it.id == entryId } }
        ?: binding.slotBindings
            .filterIsInstance<RuleSlotBinding.AttributeInput>()
            .firstOrNull { it.slotKey == binding.entrySlotKey && !it.attributeId.isNullOrBlank() }
            ?.attributeId
            ?.let { entryId -> allDefinitions.firstOrNull { it.id == entryId } }
        ?: binding.slotBindings
            .filterIsInstance<RuleSlotBinding.AttributeInput>()
            .firstOrNull { !it.attributeId.isNullOrBlank() }
            ?.attributeId
            ?.let { entryId -> allDefinitions.firstOrNull { it.id == entryId } }
}

private fun normalizeBindingAfterSlotChange(
    binding: RuleBindingInstance,
    rule: RuleDefinition?,
): RuleBindingInstance {
    val entrySlotKey = rule?.inputSlots()
        ?.firstOrNull { slot ->
            slot.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT &&
                binding.slotBindings.filterIsInstance<RuleSlotBinding.AttributeInput>()
                    .any { it.slotKey == slot.key && !it.attributeId.isNullOrBlank() }
        }
        ?.key
        ?: rule?.inputSlots()?.firstOrNull { it.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT }?.key
        ?: binding.entrySlotKey
    val entryAttributeId = binding.slotBindings
        .filterIsInstance<RuleSlotBinding.AttributeInput>()
        .firstOrNull { it.slotKey == entrySlotKey }
        ?.attributeId
    return binding.copy(
        entryAttributeId = entryAttributeId,
        entrySlotKey = entrySlotKey,
    )
}

@Composable
fun ItemRuleBindingSection(
    viewModel: BaseItemViewModel,
    itemRuleBindings: List<RuleBindingInstance>,
    ruleDefinitions: List<RuleDefinition>,
    availableAttributeDefinitions: List<AttributeDefinitionEntity>,
) {
    var showRulePickerDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "规则配置",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                OutlinedButton(onClick = { showRulePickerDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("新增规则")
                }
            }
        }

        if (availableAttributeDefinitions.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.08f),
            ) {
                Text(
                    text = "当前物品还没有可参与规则的属性。先回到属性页把属性加到这个物品上，再回来绑定规则。",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (itemRuleBindings.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.08f),
            ) {
                Text(
                    text = "还没有添加规则。先在规则库里定义槽位与公式，再把它加入到当前物品。",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        itemRuleBindings.forEach { binding ->
            val rule = ruleDefinitions.firstOrNull { it.id == binding.ruleId || it.key == binding.ruleId }
            val missingSlots = itemRuleMissingRequiredSlots(binding, rule)
            ItemRuleBindingCard(
                binding = binding,
                rule = rule,
                missingSlots = missingSlots,
                viewModel = viewModel,
                availableAttributeDefinitions = availableAttributeDefinitions,
                onBindingChanged = { updated ->
                    viewModel.addOrUpdateItemRuleBinding(normalizeBindingAfterSlotChange(updated, rule))
                },
                onDelete = { viewModel.removeItemRuleBinding(binding.id) },
            )
        }
    }

    if (showRulePickerDialog) {
        ItemRuleDefinitionPickerDialog(
            ruleDefinitions = ruleDefinitions,
            onDismiss = { showRulePickerDialog = false },
            onSelected = { rule ->
                viewModel.addOrUpdateItemRuleBinding(buildInitialItemRuleBinding(rule))
                showRulePickerDialog = false
            },
        )
    }
}

@Composable
private fun ItemRuleBindingCard(
    binding: RuleBindingInstance,
    rule: RuleDefinition?,
    missingSlots: List<RuleSlotDefinition>,
    viewModel: BaseItemViewModel,
    availableAttributeDefinitions: List<AttributeDefinitionEntity>,
    onBindingChanged: (RuleBindingInstance) -> Unit,
    onDelete: () -> Unit,
) {
    val toggleUiConfig = rule?.toggleUiConfig
    var pickerSlotKey by remember(binding.id) { mutableStateOf<String?>(null) }
    var togglePlacementPickerVisible by remember(binding.id) { mutableStateOf(false) }
    val pickerSlot = remember(pickerSlotKey, rule) {
        rule?.slots?.firstOrNull { it.key == pickerSlotKey }
    }
    val togglePlacementDefinitions = remember(binding, availableAttributeDefinitions) {
        itemResolveTogglePlacementDefinitions(binding, availableAttributeDefinitions)
    }
    val togglePlacementLabel = remember(binding, rule, availableAttributeDefinitions) {
        itemResolveTogglePlacementLabel(binding, rule, availableAttributeDefinitions)
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.10f),
        border = BorderStroke(
            width = 1.dp,
            color = if (missingSlots.isEmpty()) {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.error.copy(alpha = 0.45f)
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                    text = rule?.name ?: "缺失规则定义",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "删除规则")
                }
            }

            rule?.description?.takeIf { it.isNotBlank() }?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (missingSlots.isNotEmpty()) {
                Text(
                    text = "还缺少：${missingSlots.joinToString(" / ") { it.name }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "计算公式",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ItemRuleFormulaEditor(
                    binding = binding,
                    rule = rule,
                    availableAttributeDefinitions = availableAttributeDefinitions,
                    onSlotClick = { slotKey -> pickerSlotKey = slotKey },
                )
            }

            if (rule?.activationMode == RuleActivationMode.USER_TOGGLE && toggleUiConfig != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "运行开关位置",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ItemRuleTogglePlacementRow(
                        toggleLabel = toggleUiConfig.labelWhenEnabled.ifBlank { rule.name },
                        placementLabel = togglePlacementLabel,
                        onClick = { togglePlacementPickerVisible = true },
                    )
                }
            }
        }
    }

    pickerSlot?.let { slot ->
        ItemRuleSlotPickerDialog(
            viewModel = viewModel,
            slot = slot,
            availableAttributeDefinitions = availableAttributeDefinitions,
            onDismiss = { pickerSlotKey = null },
            onAttributeSelected = { definition ->
                val updatedBinding = when (slot.direction) {
                    RuleSlotDirection.INPUT -> RuleSlotBinding.AttributeInput(
                        slotKey = slot.key,
                        attributeId = definition.id,
                        attributeKeySnapshot = definition.key,
                        attributeNameSnapshot = definition.name,
                        isRequired = slot.isRequired,
                    )
                    RuleSlotDirection.OUTPUT -> RuleSlotBinding.AttributeOutput(
                        slotKey = slot.key,
                        attributeId = definition.id,
                        attributeKeySnapshot = definition.key,
                        attributeNameSnapshot = definition.name,
                        isRequired = slot.isRequired,
                    )
                }
                onBindingChanged(binding.copy(slotBindings = binding.slotBindings.replaceSlotBinding(updatedBinding)))
                pickerSlotKey = null
            },
            onSystemVariableSelected = { key ->
                onBindingChanged(
                    binding.copy(
                        slotBindings = binding.slotBindings.replaceSlotBinding(
                            RuleSlotBinding.SystemInput(
                                slotKey = slot.key,
                                systemVariableKey = key,
                                isRequired = slot.isRequired,
                            )
                        )
                    )
                )
                pickerSlotKey = null
            },
            onClearBinding = {
                val clearedBinding = when (slot.direction) {
                    RuleSlotDirection.INPUT -> RuleSlotBinding.AttributeInput(
                        slotKey = slot.key,
                        isRequired = slot.isRequired,
                    )
                    RuleSlotDirection.OUTPUT -> RuleSlotBinding.AttributeOutput(
                        slotKey = slot.key,
                        isRequired = slot.isRequired,
                    )
                }
                onBindingChanged(binding.copy(slotBindings = binding.slotBindings.replaceSlotBinding(clearedBinding)))
                pickerSlotKey = null
            },
            onQuickCreateCreated = { definition ->
                val updatedBinding = RuleSlotBinding.AttributeInput(
                    slotKey = slot.key,
                    attributeId = definition.id,
                    attributeKeySnapshot = definition.key,
                    attributeNameSnapshot = definition.name,
                    isRequired = slot.isRequired,
                )
                onBindingChanged(binding.copy(slotBindings = binding.slotBindings.replaceSlotBinding(updatedBinding)))
                pickerSlotKey = null
            },
        )
    }

    if (togglePlacementPickerVisible && toggleUiConfig != null) {
        val currentPlacementDefinitionId = binding.slotBindings.firstOrNull { it.slotKey == (binding.togglePlacementSlotKey ?: toggleUiConfig.anchorSlotKey) }
            ?.let { slotBinding ->
                when (slotBinding) {
                    is RuleSlotBinding.AttributeInput -> slotBinding.attributeId
                    is RuleSlotBinding.AttributeOutput -> slotBinding.attributeId
                    else -> null
                }
            }
        ItemRuleTogglePlacementDialog(
            currentAttributeId = currentPlacementDefinitionId,
            toggleLabel = toggleUiConfig.labelWhenEnabled.ifBlank { rule?.name.orEmpty() },
            availableDefinitions = togglePlacementDefinitions,
            onDismiss = { togglePlacementPickerVisible = false },
            onSelected = { definition ->
                val selectedSlotKey = binding.slotBindings.firstOrNull { slotBinding ->
                    when (slotBinding) {
                        is RuleSlotBinding.AttributeInput -> slotBinding.attributeId == definition.id
                        is RuleSlotBinding.AttributeOutput -> slotBinding.attributeId == definition.id
                        else -> false
                    }
                }?.slotKey
                onBindingChanged(binding.copy(togglePlacementSlotKey = selectedSlotKey))
                togglePlacementPickerVisible = false
            },
        )
    }
}

private sealed class ItemRuleFormulaToken {
    data class AdjustableSlot(
        val slot: RuleSlotDefinition,
        val slotBinding: RuleSlotBinding?,
    ) : ItemRuleFormulaToken()

    data class StaticSlot(
        val label: String,
    ) : ItemRuleFormulaToken()

    data class Operator(
        val symbol: String,
    ) : ItemRuleFormulaToken()
}

@Composable
private fun ItemRuleFormulaEditor(
    binding: RuleBindingInstance,
    rule: RuleDefinition?,
    availableAttributeDefinitions: List<AttributeDefinitionEntity>,
    onSlotClick: (String) -> Unit,
) {
    if (rule == null) {
        Text(
            text = "规则定义缺失，当前无法展示公式。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    val formulaTokens = remember(binding, rule) {
        itemBuildFormulaTokens(binding, rule)
    }
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        formulaTokens.forEach { token ->
            when (token) {
                is ItemRuleFormulaToken.AdjustableSlot -> {
                    ItemRuleFormulaSlotChip(
                        slot = token.slot,
                        slotBinding = token.slotBinding,
                        availableAttributeDefinitions = availableAttributeDefinitions,
                        onClick = { onSlotClick(token.slot.key) },
                    )
                }

                is ItemRuleFormulaToken.StaticSlot -> {
                    ItemRuleFormulaStaticChip(token.label)
                }

                is ItemRuleFormulaToken.Operator -> {
                    Text(
                        text = token.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemRuleFormulaStaticChip(label: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ItemRuleFormulaSlotChip(
    slot: RuleSlotDefinition,
    slotBinding: RuleSlotBinding?,
    availableAttributeDefinitions: List<AttributeDefinitionEntity>,
    onClick: () -> Unit,
) {
    val isUnboundRequired = when (slotBinding) {
        is RuleSlotBinding.AttributeInput -> slotBinding.attributeId.isNullOrBlank() && slot.isRequired
        is RuleSlotBinding.AttributeOutput -> slotBinding.attributeId.isNullOrBlank() && slot.isRequired
        is RuleSlotBinding.SystemInput -> false
        else -> false
    }
    val label = itemResolveSlotPreviewLabel(slot, slotBinding, availableAttributeDefinitions)
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isUnboundRequired) {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
        } else {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (isUnboundRequired) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            },
        ),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ItemRuleTogglePlacementRow(
    toggleLabel: String,
    placementLabel: String?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = toggleLabel,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Surface(
            modifier = Modifier.clickable(onClick = onClick),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.10f),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
            ),
        ) {
            Text(
                text = placementLabel ?: "请选择属性",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                color = if (placementLabel == null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

private data class ItemRulePickerAttributeOption(
    val definition: AttributeDefinitionEntity,
    val valuePreview: String?,
)

private data class ItemRulePickerSystemOption(
    val key: SystemVariableKey,
    val label: String,
    val valuePreview: String?,
)

private enum class ItemRuleSlotPickerTab {
    ITEM_ATTRIBUTES,
    SYSTEM_VARIABLES,
}

@Composable
private fun ItemRuleDefinitionPickerDialog(
    ruleDefinitions: List<RuleDefinition>,
    onDismiss: () -> Unit,
    onSelected: (RuleDefinition) -> Unit,
) {
    val ruleNameCollator = remember { java.text.Collator.getInstance(Locale.CHINA) }
    val sortedRules = remember(ruleDefinitions) {
        ruleDefinitions.sortedWith { left, right ->
            ruleNameCollator.compare(left.name, right.name)
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择规则") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (sortedRules.isEmpty()) {
                    Text(
                        text = "当前还没有可选规则。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    sortedRules.forEach { rule ->
                        ItemRulePickerOptionRow(
                            title = rule.name,
                            valuePreview = rule.description ?: "未填写规则说明",
                            onClick = { onSelected(rule) },
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
    )
}

@Composable
private fun ItemRuleSlotPickerDialog(
    viewModel: BaseItemViewModel,
    slot: RuleSlotDefinition,
    availableAttributeDefinitions: List<AttributeDefinitionEntity>,
    onDismiss: () -> Unit,
    onAttributeSelected: (AttributeDefinitionEntity) -> Unit,
    onSystemVariableSelected: (SystemVariableKey) -> Unit,
    onClearBinding: () -> Unit,
    onQuickCreateCreated: (AttributeDefinitionEntity) -> Unit,
) {
    val fieldVersion by viewModel.fieldVersion.observeAsState(0)
    var showQuickCreateDialog by remember(slot.key) { mutableStateOf(false) }
    var selectedTab by remember(slot.key) { mutableStateOf(ItemRuleSlotPickerTab.ITEM_ATTRIBUTES) }
    val attributeOptions = remember(slot, availableAttributeDefinitions, fieldVersion) {
        availableAttributeDefinitions.map { definition ->
            ItemRulePickerAttributeOption(
                definition = definition,
                valuePreview = itemDisplayAttributeSelectorValue(viewModel, definition),
            )
        }
    }
    val systemOptions = remember(slot, fieldVersion) {
        if (slot.direction != RuleSlotDirection.INPUT) {
            emptyList()
        } else {
            builtInSystemVariableDefinitions()
                .filter { option -> itemRuleSlotMatchesSystemValueType(slot, option.valueType) }
                .map { option ->
                    ItemRulePickerSystemOption(
                        key = option.key,
                        label = itemSystemFormulaLabel(option.key, asCurrent = true) ?: option.key.displayName,
                        valuePreview = itemDisplaySystemSelectorValue(viewModel, option.key, slot),
                    )
                }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择属性") },
        text = {
            Column(
                modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp)
                .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "槽位：${slot.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TabRow(selectedTabIndex = selectedTab.ordinal) {
                    Tab(
                        selected = selectedTab == ItemRuleSlotPickerTab.ITEM_ATTRIBUTES,
                        onClick = { selectedTab = ItemRuleSlotPickerTab.ITEM_ATTRIBUTES },
                        text = { Text("当前物品属性") },
                    )
                    Tab(
                        selected = selectedTab == ItemRuleSlotPickerTab.SYSTEM_VARIABLES,
                        onClick = { selectedTab = ItemRuleSlotPickerTab.SYSTEM_VARIABLES },
                        text = { Text("系统变量") },
                    )
                }
                when (selectedTab) {
                    ItemRuleSlotPickerTab.ITEM_ATTRIBUTES -> {
                        if (attributeOptions.isEmpty()) {
                            Text(
                                text = "当前物品还没有可选属性。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            attributeOptions.forEach { option ->
                                ItemRulePickerOptionRow(
                                    title = option.definition.name,
                                    valuePreview = option.valuePreview,
                                    onClick = { onAttributeSelected(option.definition) },
                                )
                            }
                        }
                        if (slot.direction == RuleSlotDirection.INPUT) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showQuickCreateDialog = true },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.08f),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.20f),
                                ),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "创建新属性",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    ItemRuleSlotPickerTab.SYSTEM_VARIABLES -> {
                        if (systemOptions.isEmpty()) {
                            Text(
                                text = "当前槽位暂时没有可用的系统变量。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            systemOptions.forEach { option ->
                                ItemRulePickerOptionRow(
                                    title = option.label,
                                    valuePreview = option.valuePreview,
                                    onClick = { onSystemVariableSelected(option.key) },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClearBinding) {
                Text("清空绑定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
    )

    if (showQuickCreateDialog) {
        ItemRuleQuickCreateAttributeDialog(
            slot = slot,
            viewModel = viewModel,
            onDismiss = { showQuickCreateDialog = false },
            onCreated = { definition ->
                showQuickCreateDialog = false
                onQuickCreateCreated(definition)
            },
        )
    }
}

@Composable
private fun ItemRulePickerOptionRow(
    title: String,
    valuePreview: String?,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = valuePreview ?: "当前还没有值",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ItemRuleQuickCreateAttributeDialog(
    slot: RuleSlotDefinition,
    viewModel: BaseItemViewModel,
    onDismiss: () -> Unit,
    onCreated: (AttributeDefinitionEntity) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var name by remember(slot.key) { mutableStateOf("") }
    var valueTypeMenuExpanded by remember(slot.key) { mutableStateOf(false) }
    var valueType by remember(slot.key) {
        mutableStateOf(itemDefaultAttributeValueTypeForSlot(slot))
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建新属性") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "属性名称",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(0.68f),
                        singleLine = true,
                        placeholder = { Text("请输入属性名称") },
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "值类型",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Box {
                        OutlinedButton(onClick = { valueTypeMenuExpanded = true }) {
                            Text(itemAttributeValueTypeLabel(valueType))
                        }
                        DropdownMenu(
                            expanded = valueTypeMenuExpanded,
                            onDismissRequest = { valueTypeMenuExpanded = false },
                        ) {
                            itemQuickCreateAttributeTypeOptions(slot).forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(itemAttributeValueTypeLabel(option)) },
                                    onClick = {
                                        valueType = option
                                        valueTypeMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.trim().isNotBlank(),
                onClick = {
                    scope.launch {
                        viewModel.createQuickAttribute(name, valueType)?.let(onCreated)
                    }
                },
            ) {
                Text("创建并绑定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun ItemRuleTogglePlacementDialog(
    currentAttributeId: String?,
    toggleLabel: String,
    availableDefinitions: List<AttributeDefinitionEntity>,
    onDismiss: () -> Unit,
    onSelected: (AttributeDefinitionEntity) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("运行开关位置") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "为“$toggleLabel”选择它要挂载到哪个属性下方。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (availableDefinitions.isEmpty()) {
                    Text(
                        text = "当前公式里还没有绑定到当前物品的属性，暂时无法放置开关。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    availableDefinitions.forEach { definition ->
                        ItemRulePickerOptionRow(
                            title = definition.name,
                            valuePreview = if (definition.id == currentAttributeId) "当前已挂载" else null,
                            onClick = { onSelected(definition) },
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
    )
}

private fun List<RuleSlotBinding>.replaceSlotBinding(updated: RuleSlotBinding): List<RuleSlotBinding> {
    return filterNot { it.slotKey == updated.slotKey } + updated
}

private fun buildSlotSupportText(slot: RuleSlotDefinition): String {
    return when (slot.sourceType) {
        RuleSlotSourceType.SYSTEM_INPUT -> slot.systemVariableKey?.displayName ?: "系统输入"
        RuleSlotSourceType.SYSTEM_OUTPUT -> slot.systemVariableKey?.displayName ?: "系统输出"
        RuleSlotSourceType.READONLY_OUTPUT -> slot.description ?: "只读输出，不需要再绑定属性"
        else -> slot.description ?: slot.key
    }
}

private fun buildAttributeSlotSupportText(slot: RuleSlotDefinition): String {
    val direction = when (slot.direction) {
        RuleSlotDirection.INPUT -> "输入槽位"
        RuleSlotDirection.OUTPUT -> "输出槽位"
    }
    val valueType = when (slot.valueType) {
        com.example.itemmanagement.data.model.attribute.RuleSlotValueType.DATE -> "日期"
        com.example.itemmanagement.data.model.attribute.RuleSlotValueType.BOOLEAN -> "布尔"
        com.example.itemmanagement.data.model.attribute.RuleSlotValueType.NUMBER -> "数字"
        else -> "文本"
    }
    return "$direction / $valueType${if (slot.isRequired) " / 必填" else " / 可选"}"
}

private fun itemResolveBindingAttributeName(
    slotBinding: RuleSlotBinding,
    availableAttributeDefinitions: List<AttributeDefinitionEntity>,
): String? {
    return when (slotBinding) {
        is RuleSlotBinding.AttributeInput -> {
            availableAttributeDefinitions.firstOrNull { it.id == slotBinding.attributeId }?.name
                ?: slotBinding.attributeNameSnapshot
        }
        is RuleSlotBinding.AttributeOutput -> {
            availableAttributeDefinitions.firstOrNull { it.id == slotBinding.attributeId }?.name
                ?: slotBinding.attributeNameSnapshot
        }
        else -> null
    }?.takeIf { it.isNotBlank() }
}

private fun itemResolveSlotPreviewLabel(
    slot: RuleSlotDefinition,
    slotBinding: RuleSlotBinding?,
    availableAttributeDefinitions: List<AttributeDefinitionEntity>,
): String {
    return when (slotBinding) {
        is RuleSlotBinding.AttributeInput,
        is RuleSlotBinding.AttributeOutput -> {
            itemResolveBindingAttributeName(slotBinding, availableAttributeDefinitions) ?: slot.name
        }
        is RuleSlotBinding.SystemInput -> itemSystemFormulaLabel(slotBinding.systemVariableKey, asCurrent = true) ?: slot.name
        is RuleSlotBinding.SystemOutput -> itemSystemFormulaLabel(slotBinding.systemVariableKey, asCurrent = false) ?: slot.name
        is RuleSlotBinding.ConfigInput -> slotBinding.rawValue.takeIf { it.isNotBlank() } ?: slot.name
        is RuleSlotBinding.ReadonlyOutput -> slotBinding.displayLabel ?: slot.name
        null -> when (slot.sourceType) {
            RuleSlotSourceType.SYSTEM_INPUT -> itemSystemFormulaLabel(slot.systemVariableKey, asCurrent = true) ?: slot.name
            RuleSlotSourceType.SYSTEM_OUTPUT -> itemSystemFormulaLabel(slot.systemVariableKey, asCurrent = false) ?: slot.name
            else -> slot.name
        }
    }
}

private fun itemBuildFormulaTokens(
    binding: RuleBindingInstance,
    rule: RuleDefinition,
): List<ItemRuleFormulaToken> {
    val bindingByKey = binding.slotBindings.associateBy { it.slotKey }
    when (rule.id) {
        "rule_system_include_total_count" -> {
            return listOf(
                ItemRuleFormulaToken.AdjustableSlot(
                    slot = rule.inputSlots().firstOrNull { it.key == "quantity" } ?: rule.inputSlots().first(),
                    slotBinding = bindingByKey["quantity"],
                ),
                ItemRuleFormulaToken.Operator("+"),
                ItemRuleFormulaToken.StaticSlot("当前总数量"),
                ItemRuleFormulaToken.Operator("="),
                ItemRuleFormulaToken.StaticSlot("总数量"),
            )
        }

        "rule_system_include_total_price" -> {
            return listOf(
                ItemRuleFormulaToken.AdjustableSlot(
                    slot = rule.inputSlots().firstOrNull { it.key == "amount" } ?: rule.inputSlots().first(),
                    slotBinding = bindingByKey["amount"],
                ),
                ItemRuleFormulaToken.Operator("+"),
                ItemRuleFormulaToken.StaticSlot("当前总价值"),
                ItemRuleFormulaToken.Operator("="),
                ItemRuleFormulaToken.StaticSlot("总价值"),
            )
        }
    }
    val tokens = mutableListOf<ItemRuleFormulaToken>()
    rule.inputSlots().forEachIndexed { index, slot ->
        tokens += when (slot.sourceType) {
            RuleSlotSourceType.ATTRIBUTE_INPUT,
            RuleSlotSourceType.ATTRIBUTE_OUTPUT -> ItemRuleFormulaToken.AdjustableSlot(
                slot = slot,
                slotBinding = bindingByKey[slot.key],
            )
            RuleSlotSourceType.SYSTEM_INPUT,
            RuleSlotSourceType.SYSTEM_OUTPUT,
            RuleSlotSourceType.READONLY_OUTPUT -> ItemRuleFormulaToken.StaticSlot(
                itemSystemFormulaLabel(slot.systemVariableKey, asCurrent = slot.direction == RuleSlotDirection.INPUT)
                    ?: slot.name
            )
            RuleSlotSourceType.CONFIG_INPUT -> ItemRuleFormulaToken.StaticSlot(
                (bindingByKey[slot.key] as? RuleSlotBinding.ConfigInput)?.rawValue?.takeIf { it.isNotBlank() }
                    ?: slot.name
            )
        }
        if (index < rule.inputSlots().lastIndex) {
            tokens += ItemRuleFormulaToken.Operator("+")
        }
    }
    if (rule.outputSlots().isNotEmpty()) {
        tokens += ItemRuleFormulaToken.Operator("=")
    }
    rule.outputSlots().forEachIndexed { index, slot ->
        tokens += when (slot.sourceType) {
            RuleSlotSourceType.ATTRIBUTE_INPUT,
            RuleSlotSourceType.ATTRIBUTE_OUTPUT -> ItemRuleFormulaToken.AdjustableSlot(
                slot = slot,
                slotBinding = bindingByKey[slot.key],
            )
            RuleSlotSourceType.SYSTEM_INPUT,
            RuleSlotSourceType.SYSTEM_OUTPUT,
            RuleSlotSourceType.READONLY_OUTPUT -> ItemRuleFormulaToken.StaticSlot(
                itemSystemFormulaLabel(slot.systemVariableKey, asCurrent = false) ?: slot.name
            )
            RuleSlotSourceType.CONFIG_INPUT -> ItemRuleFormulaToken.StaticSlot(
                (bindingByKey[slot.key] as? RuleSlotBinding.ConfigInput)?.rawValue?.takeIf { it.isNotBlank() }
                    ?: slot.name
            )
        }
        if (index < rule.outputSlots().lastIndex) {
            tokens += ItemRuleFormulaToken.Operator("/")
        }
    }
    return tokens
}

private fun itemSystemFormulaLabel(
    key: com.example.itemmanagement.data.model.attribute.SystemVariableKey?,
    asCurrent: Boolean,
): String? {
    return when (key) {
        com.example.itemmanagement.data.model.attribute.SystemVariableKey.GLOBAL_TOTAL_COUNT -> {
            if (asCurrent) "当前总数量" else "总数量"
        }
        com.example.itemmanagement.data.model.attribute.SystemVariableKey.GLOBAL_TOTAL_VALUE -> {
            if (asCurrent) "当前总价值" else "总价值"
        }
        else -> key?.displayName
    }
}

private fun itemResolveAnchorAttributeSummary(
    binding: RuleBindingInstance,
    rule: RuleDefinition?,
    availableAttributeDefinitions: List<AttributeDefinitionEntity>,
): String? {
    val anchorSlotKey = binding.togglePlacementSlotKey ?: rule?.toggleUiConfig?.anchorSlotKey ?: return null
    val anchorBinding = binding.slotBindings.firstOrNull { it.slotKey == anchorSlotKey } ?: return null
    val anchorName = itemResolveBindingAttributeName(anchorBinding, availableAttributeDefinitions) ?: return null
    return "这条开关当前和“$anchorName”这一组属性关系一起预览。"
}

fun itemResolveAttributeToggleStates(
    fieldName: String,
    itemRuleBindings: List<RuleBindingInstance>,
    ruleDefinitions: List<RuleDefinition>,
    allDefinitions: List<AttributeDefinitionEntity>,
): List<ItemAnchoredRuleToggleState> {
    return itemRuleBindings.mapNotNull { binding ->
        val rule = ruleDefinitions.firstOrNull { it.id == binding.ruleId || it.key == binding.ruleId } ?: return@mapNotNull null
        if (rule.activationMode != RuleActivationMode.USER_TOGGLE) {
            return@mapNotNull null
        }
        val anchorSlotKey = binding.togglePlacementSlotKey ?: rule.toggleUiConfig?.anchorSlotKey ?: return@mapNotNull null
        val anchorBinding = binding.slotBindings.firstOrNull { it.slotKey == anchorSlotKey } ?: return@mapNotNull null
        val anchorFieldName = itemResolveAnchorFieldName(anchorBinding, allDefinitions) ?: return@mapNotNull null
        if (anchorFieldName != fieldName) {
            return@mapNotNull null
        }
        ItemAnchoredRuleToggleState(
            binding = binding,
            ruleName = rule.name,
            label = if (binding.isEnabled) {
                rule.toggleUiConfig?.labelWhenEnabled.orEmpty()
            } else {
                rule.toggleUiConfig?.labelWhenDisabled.orEmpty()
            },
            supportText = rule.description,
        )
    }
}

private fun itemResolveSlotValuePreview(
    viewModel: BaseItemViewModel,
    slotBinding: RuleSlotBinding?,
    availableAttributeDefinitions: List<AttributeDefinitionEntity>,
): String? {
    return when (slotBinding) {
        is RuleSlotBinding.AttributeInput -> {
            slotBinding.attributeId
                ?.let { id -> availableAttributeDefinitions.firstOrNull { it.id == id } }
                ?.let { definition -> itemDisplayAttributeSelectorValue(viewModel, definition) }
        }
        is RuleSlotBinding.AttributeOutput -> {
            slotBinding.attributeId
                ?.let { id -> availableAttributeDefinitions.firstOrNull { it.id == id } }
                ?.let { definition -> itemDisplayAttributeSelectorValue(viewModel, definition) }
        }
        is RuleSlotBinding.SystemInput -> itemDisplaySystemSelectorValue(viewModel, slotBinding.systemVariableKey)
        else -> null
    }
}

private fun itemDisplayAttributeSelectorValue(
    viewModel: BaseItemViewModel,
    definition: AttributeDefinitionEntity,
): String? {
    val fieldName = itemCustomFieldName(definition)
    val rawValue = viewModel.getFieldValue(fieldName)
    return when {
        itemIsPriceAttribute(definition) -> {
            val amount = itemStringValue(rawValue)
            val unit = itemStringValue(viewModel.getFieldValue("${fieldName}_unit"))
            listOf(amount, unit).filter { it.isNotBlank() }.joinToString(" ").ifBlank { "当前还没有值" }
        }
        definition.valueType == AttributeValueType.BOOLEAN -> {
            if (itemBooleanValue(rawValue)) "已开启" else "已关闭"
        }
        else -> itemStringValue(rawValue).ifBlank { "当前还没有值" }
    }
}

private fun itemDisplaySystemSelectorValue(
    viewModel: BaseItemViewModel,
    key: SystemVariableKey,
    slot: RuleSlotDefinition? = null,
): String? {
    val value = viewModel.getRuntimeSystemValue(key)
    return when (value) {
        null -> {
            when (key) {
                SystemVariableKey.GLOBAL_TOTAL_COUNT -> {
                    itemNumericValueForRuleBinding(viewModel.getFieldValue("数量"))
                        ?.let(::itemFormatNumericInputForRuleBinding)
                        ?: "当前还没有值"
                }
                SystemVariableKey.GLOBAL_TOTAL_VALUE -> {
                    val amount = itemStringValue(viewModel.getFieldValue("单价"))
                    val unit = itemStringValue(viewModel.getFieldValue("单价_unit"))
                    listOf(amount, unit).filter { it.isNotBlank() }.joinToString(" ").ifBlank { "当前还没有值" }
                }
                SystemVariableKey.CURRENT_DATE -> {
                    itemStringValue(viewModel.getFieldValue("添加日期")).ifBlank {
                        if (slot?.valueType == RuleSlotValueType.SYSTEM_DATE_TIME || slot?.valueType == RuleSlotValueType.DATE) {
                            java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())
                        } else {
                            "当前还没有值"
                        }
                    }
                }
                else -> "当前还没有值"
            }
        }
        is Number -> value.toString()
        else -> itemStringValue(value).ifBlank { "当前还没有值" }
    }
}

private fun itemDefaultAttributeValueTypeForSlot(slot: RuleSlotDefinition): AttributeValueType {
    return when (slot.valueType) {
        RuleSlotValueType.NUMBER, RuleSlotValueType.CYCLE_UNIT -> AttributeValueType.NUMBER
        RuleSlotValueType.DATE, RuleSlotValueType.SYSTEM_DATE_TIME -> AttributeValueType.DATE
        RuleSlotValueType.BOOLEAN -> AttributeValueType.BOOLEAN
        RuleSlotValueType.SELECT -> AttributeValueType.SELECT
        RuleSlotValueType.TEXT -> AttributeValueType.TEXT
    }
}

private fun itemQuickCreateAttributeTypeOptions(slot: RuleSlotDefinition): List<AttributeValueType> {
    val preferred = itemDefaultAttributeValueTypeForSlot(slot)
    return listOf(
        preferred,
        AttributeValueType.TEXT,
        AttributeValueType.NUMBER,
        AttributeValueType.DATE,
        AttributeValueType.BOOLEAN,
        AttributeValueType.SELECT,
    ).distinct()
}

private fun itemAttributeValueTypeLabel(type: AttributeValueType): String {
    return when (type) {
        AttributeValueType.TEXT -> "文本"
        AttributeValueType.NUMBER -> "数字"
        AttributeValueType.DATE -> "日期"
        AttributeValueType.BOOLEAN -> "开关"
        AttributeValueType.SELECT -> "选项"
    }
}

private fun itemResolveTogglePlacementDefinitions(
    binding: RuleBindingInstance,
    availableAttributeDefinitions: List<AttributeDefinitionEntity>,
): List<AttributeDefinitionEntity> {
    val attributeIds = binding.slotBindings.mapNotNull { slotBinding ->
        when (slotBinding) {
            is RuleSlotBinding.AttributeInput -> slotBinding.attributeId
            else -> null
        }
    }.toSet()
    return availableAttributeDefinitions.filter { it.id in attributeIds }
}

private fun itemResolveTogglePlacementLabel(
    binding: RuleBindingInstance,
    rule: RuleDefinition?,
    availableAttributeDefinitions: List<AttributeDefinitionEntity>,
): String? {
    val slotKey = binding.togglePlacementSlotKey ?: rule?.toggleUiConfig?.anchorSlotKey ?: return null
    val slotBinding = binding.slotBindings.firstOrNull { it.slotKey == slotKey } ?: return null
    return itemResolveBindingAttributeName(slotBinding, availableAttributeDefinitions)
}

private fun itemRuleSlotMatchesSystemValueType(
    slot: RuleSlotDefinition,
    valueType: AttributeValueType,
): Boolean {
    return when (slot.valueType) {
        RuleSlotValueType.NUMBER -> valueType == AttributeValueType.NUMBER
        RuleSlotValueType.DATE,
        RuleSlotValueType.SYSTEM_DATE_TIME -> valueType == AttributeValueType.DATE
        RuleSlotValueType.BOOLEAN -> valueType == AttributeValueType.BOOLEAN
        RuleSlotValueType.SELECT,
        RuleSlotValueType.TEXT -> valueType == AttributeValueType.TEXT || valueType == AttributeValueType.SELECT
        RuleSlotValueType.CYCLE_UNIT -> valueType == AttributeValueType.TEXT || valueType == AttributeValueType.SELECT
    }
}

private fun itemResolveAnchorFieldName(
    slotBinding: RuleSlotBinding,
    allDefinitions: List<AttributeDefinitionEntity>,
): String? {
    return when (slotBinding) {
        is RuleSlotBinding.AttributeInput -> {
            val definition = allDefinitions.firstOrNull { it.id == slotBinding.attributeId }
            definition?.let(::itemCustomFieldName) ?: slotBinding.attributeNameSnapshot
        }
        is RuleSlotBinding.AttributeOutput -> {
            val definition = allDefinitions.firstOrNull { it.id == slotBinding.attributeId }
            definition?.let(::itemCustomFieldName) ?: slotBinding.attributeNameSnapshot
        }
        else -> null
    }
}

private fun itemBuildAttributeTargetUpdates(
    definition: AttributeDefinitionEntity?,
    fieldName: String,
    runtimeOutput: com.example.itemmanagement.data.model.attribute.RuleRuntimeOutputState,
): Map<String, Any?> {
    if (definition == null) {
        return mapOf(fieldName to runtimeOutput.value.ifBlank { null })
    }
    return when {
        itemIsPriceAttribute(definition) -> {
            val (amount, unit) = itemSplitAmountAndUnitForRuleBinding(runtimeOutput.value)
            mapOf(
                fieldName to amount,
                "${fieldName}_unit" to unit,
            )
        }

        definition.valueType == AttributeValueType.NUMBER -> {
            mapOf(fieldName to itemNumericValueForRuleBinding(runtimeOutput.value)?.let(::itemFormatNumericInputForRuleBinding))
        }

        definition.valueType == AttributeValueType.BOOLEAN -> {
            mapOf(fieldName to itemBooleanValueFromTextForRuleBinding(runtimeOutput.value))
        }

        else -> mapOf(fieldName to runtimeOutput.value.ifBlank { null })
    }
}

private fun itemBuildSystemTargetValue(
    key: com.example.itemmanagement.data.model.attribute.SystemVariableKey,
    rawValue: String,
): Any? {
    return when (com.example.itemmanagement.data.model.attribute.findSystemVariableDefinition(key)?.valueType) {
        AttributeValueType.NUMBER -> itemNumericValueForRuleBinding(rawValue)
        AttributeValueType.BOOLEAN -> itemBooleanValueFromTextForRuleBinding(rawValue)
        else -> rawValue.ifBlank { null }
    }
}

private fun itemSplitAmountAndUnitForRuleBinding(rawValue: String): Pair<String?, String?> {
    val normalized = rawValue.trim()
    if (normalized.isBlank()) {
        return null to null
    }
    val match = Regex("^([-+]?\\d+(?:\\.\\d+)?)\\s*(.*)$").find(normalized)
    val amount = match?.groupValues?.getOrNull(1)?.trim().orEmpty().ifBlank { normalized }
    val unit = match?.groupValues?.getOrNull(2)?.trim().orEmpty().ifBlank { null }
    return amount to unit
}

private fun itemFormatNumericInputForRuleBinding(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format(Locale.getDefault(), "%.2f", value)
    }
}

private fun itemBooleanValueFromTextForRuleBinding(rawValue: String): Boolean {
    return rawValue.trim().lowercase(Locale.ROOT) in setOf("true", "1", "yes", "y", "是", "对")
}

private fun itemNumericValueForRuleBinding(value: Any?): Double? {
    return when (value) {
        is Number -> value.toDouble()
        is String -> value.trim().toDoubleOrNull()
        is Pair<*, *> -> value.first?.toString()?.trim()?.toDoubleOrNull()
        else -> null
    }
}
