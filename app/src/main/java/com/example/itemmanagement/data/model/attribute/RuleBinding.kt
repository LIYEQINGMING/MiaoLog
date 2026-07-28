package com.example.itemmanagement.data.model.attribute

sealed class RuleSlotBinding {
    abstract val slotKey: String
    abstract val isRequired: Boolean

    data class AttributeInput(
        override val slotKey: String,
        val attributeId: String? = null,
        val attributeKeySnapshot: String? = null,
        val attributeNameSnapshot: String? = null,
        override val isRequired: Boolean = true,
    ) : RuleSlotBinding()

    data class ConfigInput(
        override val slotKey: String,
        val rawValue: String,
        override val isRequired: Boolean = true,
    ) : RuleSlotBinding()

    data class SystemInput(
        override val slotKey: String,
        val systemVariableKey: SystemVariableKey,
        override val isRequired: Boolean = true,
    ) : RuleSlotBinding()

    data class AttributeOutput(
        override val slotKey: String,
        val attributeId: String? = null,
        val attributeKeySnapshot: String? = null,
        val attributeNameSnapshot: String? = null,
        val updateMode: RuleOutputUpdateMode = RuleOutputUpdateMode.OVERWRITE,
        override val isRequired: Boolean = true,
    ) : RuleSlotBinding()

    data class ReadonlyOutput(
        override val slotKey: String,
        val displayLabel: String? = null,
        override val isRequired: Boolean = true,
    ) : RuleSlotBinding()

    data class SystemOutput(
        override val slotKey: String,
        val systemVariableKey: SystemVariableKey,
        val updateMode: RuleOutputUpdateMode = RuleOutputUpdateMode.OVERWRITE,
        override val isRequired: Boolean = true,
    ) : RuleSlotBinding()
}

data class RuleBindingInstance(
    val id: String,
    val ruleId: String,
    val entryAttributeId: String? = null,
    val entrySlotKey: String,
    val slotBindings: List<RuleSlotBinding>,
    val togglePlacementSlotKey: String? = null,
    val isEnabled: Boolean = true,
    val status: RuleBindingStatus = RuleBindingStatus.ACTIVE,
    val creationSource: RuleBindingCreationSource = RuleBindingCreationSource.ATTRIBUTE_MANAGEMENT,
    val description: String? = null,
) {
    val inputRole: String
        get() = entrySlotKey

    val requiredDependencies: List<String>
        get() = slotBindings
            .filterIsInstance<RuleSlotBinding.AttributeInput>()
            .filter { it.slotKey != entrySlotKey && it.isRequired }
            .mapNotNull { it.attributeNameSnapshot }

    val optionalDependencies: List<String>
        get() = slotBindings
            .filterIsInstance<RuleSlotBinding.AttributeInput>()
            .filter { it.slotKey != entrySlotKey && !it.isRequired }
            .mapNotNull { it.attributeNameSnapshot }

    val outputKeys: List<String>
        get() = slotBindings
            .filter {
                it is RuleSlotBinding.AttributeOutput ||
                    it is RuleSlotBinding.ReadonlyOutput ||
                    it is RuleSlotBinding.SystemOutput
            }
            .map { it.slotKey }

    val isRuntimeActive: Boolean
        get() = isEnabled && status == RuleBindingStatus.ACTIVE
}

typealias RuleBinding = RuleBindingInstance
