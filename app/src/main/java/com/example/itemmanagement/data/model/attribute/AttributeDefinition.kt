package com.example.itemmanagement.data.model.attribute

data class AttributeDefinition(
    val id: String,
    val key: String,
    val name: String,
    val ownerType: AttributeOwnerType,
    val valueType: AttributeValueType,
    val optionSource: AttributeOptionSource = AttributeOptionSource.INPUT,
    val inputMode: AttributeInputMode = AttributeInputMode.TEXT_INPUT,
    val isMultiValue: Boolean = false,
    val optionItems: List<String> = emptyList(),
    val valueProperties: AttributeValueProperties = legacyValueProperties(
        valueType = valueType,
        optionSource = optionSource,
        isMultiValue = isMultiValue,
        optionItems = optionItems,
        inputMode = inputMode,
    ),
    val valueSource: AttributeValueSource = optionSource.toValueSource(),
    val interactionMode: AttributeInputMode = inputMode,
    val icon: String? = null,
    val templateId: String? = null,
    val ruleBindings: List<RuleBinding> = emptyList(),
    val description: String? = null,
)
