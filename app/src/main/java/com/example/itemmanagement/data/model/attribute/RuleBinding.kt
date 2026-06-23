package com.example.itemmanagement.data.model.attribute

data class RuleBinding(
    val ruleId: String,
    val inputRole: String,
    val requiredDependencies: List<String> = emptyList(),
    val optionalDependencies: List<String> = emptyList(),
    val outputKeys: List<String> = emptyList(),
)
