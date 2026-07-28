package com.example.itemmanagement.ui.components

import com.example.itemmanagement.data.entity.attribute.AttributeDefinitionEntity
import com.example.itemmanagement.data.model.attribute.RuleBindingCreationSource
import com.example.itemmanagement.data.model.attribute.RuleBindingInstance
import com.example.itemmanagement.data.model.attribute.RuleDefinition
import com.example.itemmanagement.data.model.attribute.RuleSlotBinding
import com.example.itemmanagement.data.model.attribute.RuleSlotSourceType
import com.example.itemmanagement.data.model.attribute.inputSlots
import java.util.UUID

fun itemBuildBoundRuleBinding(
    rule: RuleDefinition,
    slotAttributeDefinitionsBySlotKey: Map<String, AttributeDefinitionEntity?>,
    enabled: Boolean = rule.toggleUiConfig?.defaultEnabled ?: true,
    bindingId: String = "item_rule_${UUID.randomUUID()}",
): RuleBindingInstance {
    val slotBindings = rule.slots.mapNotNull { slot ->
        when (slot.sourceType) {
            RuleSlotSourceType.ATTRIBUTE_INPUT -> {
                val definition = slotAttributeDefinitionsBySlotKey[slot.key]
                RuleSlotBinding.AttributeInput(
                    slotKey = slot.key,
                    attributeId = definition?.id,
                    attributeKeySnapshot = definition?.key,
                    attributeNameSnapshot = definition?.name,
                    isRequired = slot.isRequired,
                )
            }

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

            RuleSlotSourceType.ATTRIBUTE_OUTPUT -> {
                val definition = slotAttributeDefinitionsBySlotKey[slot.key]
                RuleSlotBinding.AttributeOutput(
                    slotKey = slot.key,
                    attributeId = definition?.id,
                    attributeKeySnapshot = definition?.key,
                    attributeNameSnapshot = definition?.name,
                    isRequired = slot.isRequired,
                )
            }

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

    val entrySlot = rule.inputSlots()
        .firstOrNull { slot ->
            slot.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT &&
                slotAttributeDefinitionsBySlotKey[slot.key] != null
        }
        ?: rule.inputSlots().firstOrNull { it.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT }
        ?: rule.inputSlots().firstOrNull()

    return RuleBindingInstance(
        id = bindingId,
        ruleId = rule.id,
        entryAttributeId = entrySlot?.let { slot -> slotAttributeDefinitionsBySlotKey[slot.key]?.id },
        entrySlotKey = entrySlot?.key ?: "entry",
        slotBindings = slotBindings,
        togglePlacementSlotKey = rule.toggleUiConfig?.anchorSlotKey,
        isEnabled = enabled,
        creationSource = RuleBindingCreationSource.RULE_TEMPLATE,
    )
}
