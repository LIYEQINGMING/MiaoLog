package com.example.itemmanagement.ui.components

import com.example.itemmanagement.data.entity.attribute.AttributeDefinitionEntity
import com.example.itemmanagement.data.entity.unified.ItemCustomAttributeEntity
import com.example.itemmanagement.data.model.attribute.AttributeValueType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ItemCustomAttributeFieldState(
    val fieldValue: Any?,
    val unitValue: String? = null,
    val includeInTotal: Boolean = true,
)

fun itemDecodeCustomAttributeFieldState(
    definition: AttributeDefinitionEntity,
    attribute: ItemCustomAttributeEntity,
): ItemCustomAttributeFieldState {
    val fieldValue = when {
        definition.valueType == AttributeValueType.DATE -> {
            attribute.valueDate?.let { millis ->
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
            }
        }

        definition.valueType == AttributeValueType.BOOLEAN -> {
            attribute.valueNumber?.let { it != 0.0 }
        }

        definition.valueType == AttributeValueType.NUMBER -> {
            attribute.valueNumber?.let(::itemFormatNumericValue)
        }

        else -> attribute.valueText
    }

    return ItemCustomAttributeFieldState(
        fieldValue = fieldValue,
        unitValue = attribute.priceCurrency?.takeIf { it.isNotBlank() },
        includeInTotal = attribute.includeInTotal,
    )
}

fun itemBuildCustomAttributeEntities(
    itemId: Long,
    fieldValues: Map<String, Any?>,
    definitionsById: Map<String, AttributeDefinitionEntity>,
    fallbackCurrencyCode: String? = null,
): List<ItemCustomAttributeEntity> {
    return definitionsById.values.mapNotNull { definition ->
        val fieldName = itemCustomFieldName(definition)
        if (!fieldName.startsWith("custom_")) {
            return@mapNotNull null
        }

        val value = fieldValues[fieldName]
        val includeInTotal = itemBooleanFieldValue(
            fieldValues[itemCustomIncludeFieldName(definition.id)],
            defaultValue = true,
        )

        when {
            definition.valueType == AttributeValueType.DATE -> {
                val dateString = (value as? String)?.trim().orEmpty()
                if (dateString.isBlank()) {
                    null
                } else {
                    ItemCustomAttributeEntity(
                        itemId = itemId,
                        definitionId = definition.id,
                        valueDate = itemParseDate(dateString)?.time,
                        includeInTotal = includeInTotal,
                    )
                }
            }

            definition.valueType == AttributeValueType.BOOLEAN -> {
                ItemCustomAttributeEntity(
                    itemId = itemId,
                    definitionId = definition.id,
                    valueNumber = if (itemBooleanFieldValue(value)) 1.0 else 0.0,
                    includeInTotal = includeInTotal,
                )
            }

            definition.valueType == AttributeValueType.NUMBER -> {
                val numericValue = when (value) {
                    is Number -> value.toDouble()
                    is String -> value.toDoubleOrNull()
                    else -> null
                }
                if (numericValue == null) {
                    null
                } else {
                    val isPrice = itemIsPriceAttribute(definition)
                    ItemCustomAttributeEntity(
                        itemId = itemId,
                        definitionId = definition.id,
                        valueNumber = numericValue,
                        includeInTotal = includeInTotal,
                        priceAmount = if (isPrice) numericValue else null,
                        priceCurrency = if (isPrice) {
                            (fieldValues["${fieldName}_unit"] as? String)
                                ?.takeIf { it.isNotBlank() }
                                ?: fallbackCurrencyCode?.takeIf { it.isNotBlank() }
                        } else {
                            null
                        },
                    )
                }
            }

            else -> {
                val textValue = itemNormalizeTextValue(value)
                if (textValue == null) {
                    null
                } else {
                    ItemCustomAttributeEntity(
                        itemId = itemId,
                        definitionId = definition.id,
                        valueText = textValue,
                        includeInTotal = includeInTotal,
                    )
                }
            }
        }
    }
}

private fun itemFormatNumericValue(numericValue: Double): String {
    return if (numericValue == numericValue.toInt().toDouble()) {
        numericValue.toInt().toString()
    } else {
        numericValue.toString()
    }
}

private fun itemParseDate(dateString: String): Date? {
    return runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
    }.getOrNull()
}

private fun itemNormalizeTextValue(value: Any?): String? {
    return when (value) {
        null -> null
        is String -> value.trim().takeIf { it.isNotBlank() }
        is Collection<*> -> value
            .mapNotNull { entry -> entry?.toString()?.trim()?.takeIf { it.isNotBlank() } }
            .joinToString(",")
            .takeIf { it.isNotBlank() }
        else -> value.toString().trim().takeIf { it.isNotBlank() }
    }
}

private fun itemBooleanFieldValue(value: Any?, defaultValue: Boolean = false): Boolean {
    return when (value) {
        is Boolean -> value
        is Number -> value.toInt() != 0
        is String -> value.equals("true", ignoreCase = true) || value == "1"
        null -> defaultValue
        else -> defaultValue
    }
}
