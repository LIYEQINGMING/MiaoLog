package com.example.itemmanagement.data.model.attribute

data class RuleTemplate(
    val id: String,
    val key: String,
    val name: String,
    val category: String,
    val kind: TemplateKind = TemplateKind.RULE,
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
    val defaultExpressionDefinition: RuleExpressionDefinition? = null,
    val outputStrategies: List<RuleOutputStrategyDefinition> = emptyList(),
    val description: String? = null,
) {
    val inputRoles: List<String>
        get() = slots.filter { it.direction == RuleSlotDirection.INPUT }.map { it.key }

    val requiredDependencies: List<String>
        get() = slots.filter {
            it.direction == RuleSlotDirection.INPUT &&
                it.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT &&
                it.isRequired
        }.map { it.name }

    val optionalDependencies: List<String>
        get() = slots.filter {
            it.direction == RuleSlotDirection.INPUT &&
                it.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT &&
                !it.isRequired
        }.map { it.name }

    val outputKeys: List<String>
        get() = slots.filter { it.direction == RuleSlotDirection.OUTPUT }.map { it.key }

    val systemInputs: List<RuleSystemInputDefinition>
        get() = slots.filter {
            it.direction == RuleSlotDirection.INPUT &&
                it.sourceType == RuleSlotSourceType.SYSTEM_INPUT
        }.map { slot ->
            RuleSystemInputDefinition(
                role = slot.key,
                sourceType = RuleInputSourceType.SYSTEM_VARIABLE,
                variableKey = slot.systemVariableKey,
                required = slot.isRequired,
            )
        }

    val outputTargets: List<RuleOutputTargetDefinition>
        get() = slots.filter { it.direction == RuleSlotDirection.OUTPUT }.map { slot ->
            val strategy = outputStrategies.firstOrNull { it.slotKey == slot.key }
            RuleOutputTargetDefinition(
                outputKey = slot.key,
                targetType = strategy?.targetType ?: when (slot.sourceType) {
                    RuleSlotSourceType.ATTRIBUTE_OUTPUT -> RuleOutputTargetType.ATTRIBUTE_VALUE
                    RuleSlotSourceType.SYSTEM_OUTPUT -> RuleOutputTargetType.SYSTEM_VARIABLE
                    else -> RuleOutputTargetType.READONLY_RESULT
                },
                variableKey = strategy?.systemVariableKey ?: slot.systemVariableKey,
            )
        }

    val defaultExpression: String?
        get() = defaultExpressionDefinition?.expression
}
