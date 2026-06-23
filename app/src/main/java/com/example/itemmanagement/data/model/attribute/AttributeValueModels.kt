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
