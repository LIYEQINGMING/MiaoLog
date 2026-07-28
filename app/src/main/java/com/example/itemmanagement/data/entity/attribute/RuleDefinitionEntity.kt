package com.example.itemmanagement.data.entity.attribute

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.itemmanagement.data.model.attribute.RuleComputationType
import com.example.itemmanagement.data.model.attribute.RuleActivationMode

@Entity(tableName = "rule_definitions")
data class RuleDefinitionEntity(
    @PrimaryKey
    val id: String,
    val key: String,
    val name: String,
    val computationType: RuleComputationType,
    val triggerModesJson: String,
    val activationMode: RuleActivationMode = RuleActivationMode.ALWAYS_ON,
    val toggleUiJson: String? = null,
    val inputRolesJson: String,
    val requiredDependenciesJson: String,
    val optionalDependenciesJson: String,
    val outputKeysJson: String,
    val systemInputsJson: String,
    val outputTargetsJson: String,
    val expression: String?,
    val description: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
