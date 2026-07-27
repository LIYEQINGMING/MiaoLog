package com.example.itemmanagement.data.model.attribute

enum class AttributeOwnerType {
    SYSTEM,
    CUSTOM,
}

enum class AttributeValueType {
    TEXT,
    NUMBER,
    DATE,
    BOOLEAN,
    SELECT,
}

enum class AttributeValueSource {
    INPUT,
    FIXED,
    SYSTEM,
}

enum class AttributeNumberFormat {
    PLAIN,
    PRICE,
    PERCENTAGE,
    WITH_UNIT,
}

enum class AttributeSelectionOptionSource {
    SYSTEM,
    USER,
    MIXED,
}

enum class AttributeOptionSource {
    INPUT,
    FIXED_OPTIONS_SYSTEM,
    FIXED_OPTIONS_USER,
}

enum class AttributeInputMode {
    TEXT_INPUT,
    NUMBER_INPUT,
    PRICE_INPUT,
    DATE_PICKER,
    BOOLEAN_SWITCH,
    SINGLE_SELECT,
    MULTI_SELECT,
    TAG_INPUT,
}

enum class RuleComputationType {
    SUM,
    DIFFERENCE,
    AVERAGE,
    CYCLE,
    ACCUMULATION,
    CUSTOM,
}

enum class TemplateKind {
    ATTRIBUTE,
    RULE,
}

data class AttributeValueProperties(
    val textMultiline: Boolean = false,
    val placeholder: String? = null,
    val maxLength: Int? = null,
    val numberFormat: AttributeNumberFormat? = null,
    val unitCategory: String? = null,
    val defaultUnit: String? = null,
    val allowedUnits: List<String> = emptyList(),
    val allowUnitSwitch: Boolean = false,
    val decimalPlaces: Int? = null,
    val allowNegative: Boolean = true,
    val includeTime: Boolean = false,
    val trueLabel: String? = null,
    val falseLabel: String? = null,
    val optionItems: List<String> = emptyList(),
    val allowCustomOptions: Boolean = false,
    val selectionOptionSource: AttributeSelectionOptionSource? = null,
    val isMultiValue: Boolean = false,
    val systemVariableKey: String? = null,
    val defaultValue: String? = null,
)

fun defaultValuePropertiesFor(type: AttributeValueType): AttributeValueProperties {
    return when (type) {
        AttributeValueType.TEXT -> AttributeValueProperties()
        AttributeValueType.NUMBER -> AttributeValueProperties(
            numberFormat = AttributeNumberFormat.PLAIN,
            allowNegative = true,
        )
        AttributeValueType.DATE -> AttributeValueProperties(includeTime = false)
        AttributeValueType.BOOLEAN -> AttributeValueProperties(
            trueLabel = "是",
            falseLabel = "否",
        )
        AttributeValueType.SELECT -> AttributeValueProperties(
            selectionOptionSource = AttributeSelectionOptionSource.USER,
            isMultiValue = false,
        )
    }
}

fun legacyValueProperties(
    valueType: AttributeValueType,
    optionSource: AttributeOptionSource,
    isMultiValue: Boolean,
    optionItems: List<String>,
    inputMode: AttributeInputMode,
): AttributeValueProperties {
    val base = defaultValuePropertiesFor(valueType)
    return when (valueType) {
        AttributeValueType.TEXT -> base.copy(
            textMultiline = inputMode == AttributeInputMode.TAG_INPUT,
            optionItems = optionItems,
            isMultiValue = isMultiValue,
        )
        AttributeValueType.NUMBER -> base.copy(
            numberFormat = when (inputMode) {
                AttributeInputMode.PRICE_INPUT -> AttributeNumberFormat.PRICE
                else -> AttributeNumberFormat.PLAIN
            },
            optionItems = optionItems,
            isMultiValue = isMultiValue,
        )
        AttributeValueType.DATE -> base.copy(
            includeTime = false,
            optionItems = optionItems,
            isMultiValue = isMultiValue,
        )
        AttributeValueType.BOOLEAN -> base.copy(
            optionItems = optionItems,
            isMultiValue = isMultiValue,
        )
        AttributeValueType.SELECT -> base.copy(
            optionItems = optionItems,
            isMultiValue = isMultiValue,
            selectionOptionSource = when (optionSource) {
                AttributeOptionSource.FIXED_OPTIONS_SYSTEM -> AttributeSelectionOptionSource.SYSTEM
                AttributeOptionSource.FIXED_OPTIONS_USER -> AttributeSelectionOptionSource.USER
                AttributeOptionSource.INPUT -> AttributeSelectionOptionSource.USER
            },
        )
    }
}

fun AttributeValueSource.toLegacyOptionSource(
    valueProperties: AttributeValueProperties
): AttributeOptionSource {
    return when (this) {
        AttributeValueSource.INPUT -> AttributeOptionSource.INPUT
        AttributeValueSource.FIXED -> when (valueProperties.selectionOptionSource) {
            AttributeSelectionOptionSource.SYSTEM -> AttributeOptionSource.FIXED_OPTIONS_SYSTEM
            AttributeSelectionOptionSource.MIXED,
            AttributeSelectionOptionSource.USER,
            null -> AttributeOptionSource.FIXED_OPTIONS_USER
        }
        AttributeValueSource.SYSTEM -> AttributeOptionSource.INPUT
    }
}

fun AttributeOptionSource.toValueSource(): AttributeValueSource {
    return when (this) {
        AttributeOptionSource.INPUT -> AttributeValueSource.INPUT
        AttributeOptionSource.FIXED_OPTIONS_SYSTEM,
        AttributeOptionSource.FIXED_OPTIONS_USER -> AttributeValueSource.FIXED
    }
}

fun AttributeOptionSource.toSelectionOptionSource(): AttributeSelectionOptionSource? {
    return when (this) {
        AttributeOptionSource.INPUT -> null
        AttributeOptionSource.FIXED_OPTIONS_SYSTEM -> AttributeSelectionOptionSource.SYSTEM
        AttributeOptionSource.FIXED_OPTIONS_USER -> AttributeSelectionOptionSource.USER
    }
}
