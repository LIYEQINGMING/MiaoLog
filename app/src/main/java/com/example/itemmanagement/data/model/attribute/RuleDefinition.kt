package com.example.itemmanagement.data.model.attribute

data class RuleSlotQuickCreatePreset(
    val ownerType: AttributeOwnerType = AttributeOwnerType.CUSTOM,
    val valueSource: AttributeValueSource = AttributeValueSource.INPUT,
    val interactionMode: AttributeInputMode? = null,
    val templateId: String? = null,
    val defaultValue: String? = null,
)

data class RuleSlotDefinition(
    val key: String,
    val name: String,
    val direction: RuleSlotDirection,
    val valueType: RuleSlotValueType,
    val sourceType: RuleSlotSourceType,
    val isRequired: Boolean = true,
    val allowQuickCreateAttribute: Boolean = false,
    val quickCreatePreset: RuleSlotQuickCreatePreset? = null,
    val systemVariableKey: SystemVariableKey? = null,
    val description: String? = null,
)

data class RuleExpressionDefinition(
    val expression: String,
    val referencedSlotKeys: List<String> = emptyList(),
)

data class RuleOutputStrategyDefinition(
    val slotKey: String,
    val targetType: RuleOutputTargetType,
    val updateMode: RuleOutputUpdateMode = RuleOutputUpdateMode.OVERWRITE,
    val systemVariableKey: SystemVariableKey? = null,
)

data class RuleDefinition(
    val id: String,
    val key: String,
    val name: String,
    val computationType: RuleComputationType,
    val triggerModes: List<RuleTriggerMode> = listOf(
        RuleTriggerMode.ON_VALUE_CHANGED,
        RuleTriggerMode.ON_SAVE,
    ),
    val activationMode: RuleActivationMode = RuleActivationMode.ALWAYS_ON,
    val toggleUiConfig: RuleToggleUiConfig? = null,
    val scheduleConfig: RuleScheduleConfig? = null,
    val scheduleEventConfig: RuleScheduleEventConfig? = null,
    val canvasDefinition: RuleCanvasDefinition? = null,
    val slots: List<RuleSlotDefinition>,
    val expressionDefinition: RuleExpressionDefinition? = null,
    val outputStrategies: List<RuleOutputStrategyDefinition> = emptyList(),
    val description: String? = null,
    val templateId: String? = null,
) {
    val inputRoles: List<String>
        get() = inputSlots().map { it.key }

    val requiredDependencies: List<String>
        get() = attributeInputSlots(requiredOnly = true).map { it.name }

    val optionalDependencies: List<String>
        get() = attributeInputSlots(requiredOnly = false).map { it.name }

    val outputKeys: List<String>
        get() = outputSlots().map { it.key }

    val systemInputs: List<RuleSystemInputDefinition>
        get() = inputSlots()
            .filter { it.sourceType == RuleSlotSourceType.SYSTEM_INPUT }
            .map { slot ->
                RuleSystemInputDefinition(
                    role = slot.key,
                    sourceType = RuleInputSourceType.SYSTEM_VARIABLE,
                    variableKey = slot.systemVariableKey,
                    required = slot.isRequired,
                )
            }

    val outputTargets: List<RuleOutputTargetDefinition>
        get() = outputSlots().map { slot ->
            val strategy = outputStrategies.firstOrNull { it.slotKey == slot.key }
            RuleOutputTargetDefinition(
                outputKey = slot.key,
                targetType = strategy?.targetType ?: slot.toLegacyOutputTargetType(),
                variableKey = strategy?.systemVariableKey ?: slot.systemVariableKey,
            )
        }

    val expression: String?
        get() = expressionDefinition?.expression
}

fun RuleDefinition.inputSlots(): List<RuleSlotDefinition> {
    return slots.filter { it.direction == RuleSlotDirection.INPUT }
}

fun RuleDefinition.outputSlots(): List<RuleSlotDefinition> {
    return slots.filter { it.direction == RuleSlotDirection.OUTPUT }
}

fun RuleDefinition.attributeInputSlots(requiredOnly: Boolean? = null): List<RuleSlotDefinition> {
    return inputSlots()
        .filter { it.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT }
        .filter { requiredOnly == null || it.isRequired == requiredOnly }
}

private fun RuleSlotDefinition.toLegacyOutputTargetType(): RuleOutputTargetType {
    return when (sourceType) {
        RuleSlotSourceType.ATTRIBUTE_OUTPUT -> RuleOutputTargetType.ATTRIBUTE_VALUE
        RuleSlotSourceType.SYSTEM_OUTPUT -> RuleOutputTargetType.SYSTEM_VARIABLE
        else -> RuleOutputTargetType.READONLY_RESULT
    }
}
