package com.example.itemmanagement.data.model.attribute

data class AttributeDefinition(
    val id: String,
    val key: String,
    val name: String,
    val ownerType: AttributeOwnerType,
    val valueType: AttributeValueType,
    val optionSource: AttributeOptionSource,
    val inputMode: AttributeInputMode,
    val isMultiValue: Boolean,
    val optionItems: List<String> = emptyList(),
    val icon: String? = null,
    val templateId: String? = null,
    val ruleBindings: List<RuleBinding> = emptyList(),
    val description: String? = null,
)
