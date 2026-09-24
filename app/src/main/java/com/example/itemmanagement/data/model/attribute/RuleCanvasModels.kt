package com.example.itemmanagement.data.model.attribute

enum class RuleCanvasLayoutMode {
    FLOW_ROW,
    DIRECTED_GRAPH,
}

enum class RuleCanvasNodeType {
    SLOT,
    OPERATOR,
    FUNCTION,
    LITERAL,
}

enum class RuleCanvasNodeStyleHint {
    DEFAULT,
    INPUT_SLOT,
    OUTPUT_SLOT,
    SYSTEM_SOURCE,
    FIXED_VALUE,
    NEEDS_CONFIGURATION,
    ERROR,
    SCHEDULE_REFERENCED,
}

enum class RuleCanvasSlotValueSource {
    SYSTEM_VARIABLE,
    EXTERNAL_INPUT,
    FIXED_VALUE,
}

data class RuleCanvasSlotConfig(
    val slotKey: String? = null,
    val displayName: String = "",
    val direction: RuleSlotDirection = RuleSlotDirection.INPUT,
    val valueType: RuleSlotValueType = RuleSlotValueType.TEXT,
    val valueSource: RuleCanvasSlotValueSource = RuleCanvasSlotValueSource.EXTERNAL_INPUT,
    val systemVariableKey: SystemVariableKey? = null,
    val fixedValue: String? = null,
    val isRequired: Boolean = true,
    val description: String? = null,
)

data class RuleCanvasNode(
    val id: String,
    val type: RuleCanvasNodeType,
    val row: Int = 0,
    val column: Int = 0,
    val x: Float? = null,
    val y: Float? = null,
    val label: String = "",
    val slotConfig: RuleCanvasSlotConfig? = null,
    val operatorKey: String? = null,
    val functionKey: String? = null,
    val literalValue: String? = null,
    val literalValueType: RuleSlotValueType? = null,
    val isPlaceholder: Boolean = false,
    val styleHint: RuleCanvasNodeStyleHint = RuleCanvasNodeStyleHint.DEFAULT,
)

data class RuleCanvasConnection(
    val fromNodeId: String,
    val toNodeId: String,
    val fromPort: String? = null,
    val toPort: String? = null,
)

data class RuleCanvasDefinition(
    val version: Int = 1,
    val layoutMode: RuleCanvasLayoutMode = RuleCanvasLayoutMode.FLOW_ROW,
    val nodes: List<RuleCanvasNode> = emptyList(),
    val connections: List<RuleCanvasConnection> = emptyList(),
)
