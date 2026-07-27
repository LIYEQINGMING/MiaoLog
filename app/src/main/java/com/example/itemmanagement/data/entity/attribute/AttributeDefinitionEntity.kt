package com.example.itemmanagement.data.entity.attribute

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.itemmanagement.data.model.attribute.AttributeOwnerType
import com.example.itemmanagement.data.model.attribute.AttributeValueType
import com.example.itemmanagement.data.model.attribute.AttributeValueSource
import com.example.itemmanagement.data.model.attribute.AttributeOptionSource
import com.example.itemmanagement.data.model.attribute.AttributeInputMode

@Entity(tableName = "attribute_definitions")
data class AttributeDefinitionEntity(
    @PrimaryKey
    val id: String,
    val key: String,
    val name: String,
    val ownerType: AttributeOwnerType,
    val valueType: AttributeValueType,
    val valueSource: AttributeValueSource,
    val interactionMode: AttributeInputMode,
    val valuePropertiesJson: String,
    val optionSource: AttributeOptionSource,
    val inputMode: AttributeInputMode,
    val isMultiValue: Boolean,
    val optionItemsJson: String,
    val icon: String?,
    val templateId: String?,
    val ruleBindingsJson: String,
    val description: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
