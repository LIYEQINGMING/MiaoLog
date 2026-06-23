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
    val defaultOptionSource: AttributeOptionSource,
    val defaultInputMode: AttributeInputMode,
    val defaultMultiValue: Boolean,
    val defaultOptionItems: List<String> = emptyList(),
    val defaultRuleBindings: List<RuleBinding> = emptyList(),
    val description: String? = null,
)
