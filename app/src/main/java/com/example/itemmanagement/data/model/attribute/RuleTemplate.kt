package com.example.itemmanagement.data.model.attribute

data class RuleTemplate(
    val id: String,
    val key: String,
    val name: String,
    val category: String,
    val kind: TemplateKind = TemplateKind.RULE,
    val computationType: RuleComputationType,
    val inputRoles: List<String>,
    val requiredDependencies: List<String> = emptyList(),
    val optionalDependencies: List<String> = emptyList(),
    val outputKeys: List<String> = emptyList(),
    val defaultExpression: String? = null,
    val description: String? = null,
)
