package com.example.itemmanagement.data.model.attribute

data class AttributeTemplate(
    val id: String,
    val key: String,
    val name: String,
    val category: String,
    val kind: TemplateKind = TemplateKind.ATTRIBUTE,
    val defaultName: String,
    val defaultIcon: String? = null,
    val defaultValueType: AttributeValueType,
    val defaultOptionSource: AttributeOptionSource = AttributeOptionSource.INPUT,
    val defaultInputMode: AttributeInputMode = AttributeInputMode.TEXT_INPUT,
    val defaultMultiValue: Boolean = false,
    val defaultOptionItems: List<String> = emptyList(),
    val defaultValueProperties: AttributeValueProperties = legacyValueProperties(
        valueType = defaultValueType,
        optionSource = defaultOptionSource,
        isMultiValue = defaultMultiValue,
        optionItems = defaultOptionItems,
        inputMode = defaultInputMode,
    ),
    val defaultValueSource: AttributeValueSource = defaultOptionSource.toValueSource(),
    val defaultInteractionMode: AttributeInputMode = defaultInputMode,
    val defaultRuleBindings: List<RuleBinding> = emptyList(),
    val description: String? = null,
)
