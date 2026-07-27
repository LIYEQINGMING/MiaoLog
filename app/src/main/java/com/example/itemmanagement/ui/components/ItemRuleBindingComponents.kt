package com.example.itemmanagement.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.itemmanagement.data.entity.attribute.AttributeDefinitionEntity
import com.example.itemmanagement.data.model.attribute.AttributeInputMode
import com.example.itemmanagement.data.model.attribute.AttributeValueType
import com.example.itemmanagement.data.model.attribute.RuleBindingCreationSource
import com.example.itemmanagement.data.model.attribute.RuleBindingInstance
import com.example.itemmanagement.data.model.attribute.RuleDefinition
import com.example.itemmanagement.data.model.attribute.RuleOutputTargetType
import com.example.itemmanagement.data.model.attribute.RuleRuntimeEvaluationContext
import com.example.itemmanagement.data.model.attribute.RuleSlotBinding
import com.example.itemmanagement.data.model.attribute.RuleSlotDefinition
import com.example.itemmanagement.data.model.attribute.RuleSlotDirection
import com.example.itemmanagement.data.model.attribute.RuleSlotSourceType
import com.example.itemmanagement.data.model.attribute.RuleTriggerMode
import com.example.itemmanagement.data.model.attribute.evaluateRuleRuntimeOutput
import com.example.itemmanagement.data.model.attribute.inputSlots
import com.example.itemmanagement.data.model.attribute.outputSlots
import com.example.itemmanagement.data.model.attribute.resolveItemRuleOutputProjections
import com.example.itemmanagement.data.model.attribute.resolveRuleOutputTarget
import com.example.itemmanagement.ui.base.BaseItemViewModel
import java.util.Locale
import java.util.UUID

fun itemBuildReadonlyRuleOutputFieldsFromBindings(
    itemRuleBindings: List<RuleBindingInstance>,
    allDefinitions: List<AttributeDefinitionEntity>,
    ruleDefinitions: List<RuleDefinition> = emptyList(),
): List<String> {
    return resolveItemRuleOutputProjections(
        bindings = itemRuleBindings,
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
        bindings = itemRuleBindings,
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
        val projections = resolveItemRuleOutputProjections(
            bindings = itemRuleBindings,
            allAttributes = allDefinitions,
            fieldNameProvider = ::itemCustomFieldName,
            ruleDefinitions = ruleDefinitions,
        )
        val fieldUpdates = linkedMapOf<String, Any?>()
        val systemUpdates = linkedMapOf<com.example.itemmanagement.data.model.attribute.SystemVariableKey, Any?>()
        projections.forEach { projection ->
            val binding = itemRuleBindings.firstOrNull { it.id == projection.attributeId } ?: return@forEach
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
    var addMenuExpanded by remember { mutableStateOf(false) }

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
                Text(
                    text = "把当前物品的属性绑定到规则槽位里，未绑定完整的规则会高亮提醒。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                OutlinedButton(onClick = { addMenuExpanded = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("新增规则")
                }
                DropdownMenu(
                    expanded = addMenuExpanded,
                    onDismissRequest = { addMenuExpanded = false },
                ) {
                    ruleDefinitions
                        .sortedBy { it.name.lowercase(Locale.getDefault()) }
                        .forEach { rule ->
                            DropdownMenuItem(
                                text = { Text(rule.name) },
                                onClick = {
                                    viewModel.addOrUpdateItemRuleBinding(buildInitialItemRuleBinding(rule))
                                    addMenuExpanded = false
                                },
                            )
                        }
                }
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
                availableAttributeDefinitions = availableAttributeDefinitions,
                onBindingChanged = { updated ->
                    viewModel.addOrUpdateItemRuleBinding(normalizeBindingAfterSlotChange(updated, rule))
                },
                onDelete = { viewModel.removeItemRuleBinding(binding.id) },
            )
        }
    }
}

@Composable
private fun ItemRuleBindingCard(
    binding: RuleBindingInstance,
    rule: RuleDefinition?,
    missingSlots: List<RuleSlotDefinition>,
    availableAttributeDefinitions: List<AttributeDefinitionEntity>,
    onBindingChanged: (RuleBindingInstance) -> Unit,
    onDelete: () -> Unit,
) {
    val bindingByKey = binding.slotBindings.associateBy { it.slotKey }
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
                    if (missingSlots.isEmpty()) {
                        Text(
                            text = "槽位绑定完整",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            text = "还缺少：${missingSlots.joinToString(" / ") { it.name }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "删除规则")
                }
            }

            rule?.slots?.forEach { slot ->
                val slotBinding = bindingByKey[slot.key]
                when (slot.sourceType) {
                    RuleSlotSourceType.ATTRIBUTE_INPUT -> {
                        ItemRuleAttributeSlotEditor(
                            title = slot.name,
                            slot = slot,
                            currentAttributeId = (slotBinding as? RuleSlotBinding.AttributeInput)?.attributeId,
                            availableAttributeDefinitions = availableAttributeDefinitions,
                            onAttributeSelected = { definition ->
                                onBindingChanged(
                                    binding.copy(
                                        slotBindings = binding.slotBindings.replaceSlotBinding(
                                            RuleSlotBinding.AttributeInput(
                                                slotKey = slot.key,
                                                attributeId = definition?.id,
                                                attributeKeySnapshot = definition?.key,
                                                attributeNameSnapshot = definition?.name,
                                                isRequired = slot.isRequired,
                                            )
                                        )
                                    )
                                )
                            },
                        )
                    }

                    RuleSlotSourceType.ATTRIBUTE_OUTPUT -> {
                        ItemRuleAttributeSlotEditor(
                            title = "${slot.name}（输出）",
                            slot = slot,
                            currentAttributeId = (slotBinding as? RuleSlotBinding.AttributeOutput)?.attributeId,
                            availableAttributeDefinitions = availableAttributeDefinitions,
                            onAttributeSelected = { definition ->
                                onBindingChanged(
                                    binding.copy(
                                        slotBindings = binding.slotBindings.replaceSlotBinding(
                                            RuleSlotBinding.AttributeOutput(
                                                slotKey = slot.key,
                                                attributeId = definition?.id,
                                                attributeKeySnapshot = definition?.key,
                                                attributeNameSnapshot = definition?.name,
                                                isRequired = slot.isRequired,
                                            )
                                        )
                                    )
                                )
                            },
                        )
                    }

                    RuleSlotSourceType.CONFIG_INPUT -> {
                        OutlinedTextField(
                            value = (slotBinding as? RuleSlotBinding.ConfigInput)?.rawValue.orEmpty(),
                            onValueChange = { value ->
                                onBindingChanged(
                                    binding.copy(
                                        slotBindings = binding.slotBindings.replaceSlotBinding(
                                            RuleSlotBinding.ConfigInput(
                                                slotKey = slot.key,
                                                rawValue = value,
                                                isRequired = slot.isRequired,
                                            )
                                        )
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(slot.name) },
                            supportingText = { Text(slot.description ?: "配置常量输入") },
                            singleLine = true,
                        )
                    }

                    RuleSlotSourceType.SYSTEM_INPUT,
                    RuleSlotSourceType.SYSTEM_OUTPUT,
                    RuleSlotSourceType.READONLY_OUTPUT -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.08f),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = slot.name,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    text = buildSlotSupportText(slot),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            if (rule != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rule.triggerModes.ifEmpty { listOf(RuleTriggerMode.ON_VALUE_CHANGED) }.forEach { mode ->
                        AssistChip(
                            onClick = {},
                            label = { Text(mode.name) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemRuleAttributeSlotEditor(
    title: String,
    slot: RuleSlotDefinition,
    currentAttributeId: String?,
    availableAttributeDefinitions: List<AttributeDefinitionEntity>,
    onAttributeSelected: (AttributeDefinitionEntity?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val currentDefinition = availableAttributeDefinitions.firstOrNull { it.id == currentAttributeId }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.08f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = currentDefinition?.name ?: "未绑定属性",
                    color = if (currentDefinition == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                OutlinedButton(onClick = { expanded = true }) {
                    Text("选择属性")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("清空绑定") },
                        onClick = {
                            onAttributeSelected(null)
                            expanded = false
                        },
                    )
                    availableAttributeDefinitions.forEach { definition ->
                        DropdownMenuItem(
                            text = { Text(definition.name) },
                            onClick = {
                                onAttributeSelected(definition)
                                expanded = false
                            },
                        )
                    }
                }
            }
            Text(
                text = buildAttributeSlotSupportText(slot),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
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
