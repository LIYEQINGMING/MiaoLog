package com.example.itemmanagement.data.model.attribute

data class RuleDefinition(
    val id: String,
    val key: String,
    val name: String,
    val computationType: RuleComputationType,
    val inputRoles: List<String>,
    val requiredDependencies: List<String> = emptyList(),
    val optionalDependencies: List<String> = emptyList(),
    val outputKeys: List<String> = emptyList(),
    val expression: String? = null,
    val description: String? = null,
)
