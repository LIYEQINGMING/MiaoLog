package com.example.itemmanagement.ui.attribute

import androidx.compose.ui.geometry.Offset
import com.example.itemmanagement.ui.attribute.model.RuleSlotDraftUiModel
import java.util.UUID

internal const val RULE_CANVAS_WIDTH_DP = 2400f
internal const val RULE_CANVAS_HEIGHT_DP = 1600f
internal const val RULE_CANVAS_MAX_WIDTH_DP = 6000f
internal const val RULE_CANVAS_MAX_HEIGHT_DP = 4000f
internal const val RULE_CANVAS_PADDING_DP = 32f
internal const val RULE_CANVAS_NODE_HEIGHT_DP = 64f
internal const val RULE_CANVAS_NODE_GAP_DP = 4f

internal fun CanvasNode.widthDp(): Float = when (type) {
    CanvasNodeType.OPERAND -> 104f
    CanvasNodeType.OPERATOR -> 52f
    CanvasNodeType.FUNCTION -> 88f
    CanvasNodeType.GROUPING -> 40f
    CanvasNodeType.LITERAL -> 80f
}

/**
 * 画布节点类型
 */
enum class CanvasNodeType {
    OPERAND,    // 操作数节点，方向和值来源由节点配置决定
    OPERATOR,   // 运算符节点
    FUNCTION,   // 函数节点
    GROUPING,   // 分组符节点
    LITERAL     // 字面量节点
}

/**
 * 画布节点
 */
data class CanvasNode(
    val id: String = "node_${UUID.randomUUID().toString().replace("-", "")}",
    val type: CanvasNodeType,
    val position: Offset,
    val operandConfig: RuleSlotDraftUiModel? = null, // 仅操作数节点有
    val operatorSymbol: String? = null,             // 仅运算符节点有
    val functionName: String? = null,               // 仅函数节点有
    val groupingSymbol: String? = null,              // 仅分组符节点有
    val literalValue: String? = null,               // 仅字面量节点有
    val displayText: String? = null,                 // 用于未配置操作数或解析态展示
)

/**
 * 画布连接线
 */
data class CanvasConnection(
    val id: String = "conn_${UUID.randomUUID().toString().replace("-", "")}",
    val fromNodeId: String,
    val toNodeId: String,
)

/**
 * 素材栏项目类型
 */
sealed class PaletteItem {
    object Operand : PaletteItem()
    data class Operator(val symbol: String, val label: String) : PaletteItem()
    data class Function(val name: String, val label: String) : PaletteItem()
    data class Grouping(val symbol: String, val label: String) : PaletteItem()
}

/**
 * 画布状态
 */
data class CanvasState(
    val nodes: List<CanvasNode> = emptyList(),
    val connections: List<CanvasConnection> = emptyList(),
    val scale: Float = 1.0f,
    val offset: Offset = Offset.Zero,
    val selectedNodeId: String? = null,
)
