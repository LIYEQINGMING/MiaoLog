package com.example.itemmanagement.ui.attribute

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.itemmanagement.data.model.attribute.RuleSlotDirection
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val MinCanvasScale = 0.15f
private const val MaxCanvasScale = 2f
private const val SnapThresholdDp = 30f
private const val SnapCrossAxisToleranceDp = 44f
private const val InsertChainGapToleranceDp = 20f

private data class NodeSnapResult(
    val position: Offset,
    val targetNodeIds: Set<String>,
    val score: Float,
    val insertBeforeNodeId: String? = null,
)

/**
 * 可自由排布的规则画布。画布本身可以在视窗内平移和缩放，节点仅在彼此边缘
 * 足够接近时磁吸；公式顺序由上层按照节点的空间位置解析。
 */
@Composable
fun DraggableRuleCanvas(
    canvasState: CanvasState,
    onNodesChange: (List<CanvasNode>) -> Unit,
    onViewportChange: (scale: Float, offset: Offset) -> Unit,
    onNodeSelected: (String) -> Unit,
    onNodeDeleted: (String) -> Unit,
    onToggleFullscreen: () -> Unit,
    isFullscreen: Boolean,
    modifier: Modifier = Modifier,
) {
    var draggedNodeId by remember { mutableStateOf<String?>(null) }
    var draggedNodePosition by remember { mutableStateOf<Offset?>(null) }
    var activeSnapResult by remember { mutableStateOf<NodeSnapResult?>(null) }
    var viewportScaleState by remember { mutableStateOf(canvasState.scale) }
    var viewportOffsetState by remember { mutableStateOf(canvasState.offset) }
    var isNavigatorExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(canvasState.scale, canvasState.offset) {
        if (canvasState.scale != viewportScaleState || canvasState.offset != viewportOffsetState) {
            viewportScaleState = canvasState.scale
            viewportOffsetState = canvasState.offset
        }
    }

    LaunchedEffect(viewportScaleState, viewportOffsetState) {
        delay(100)
        if (canvasState.scale != viewportScaleState || canvasState.offset != viewportOffsetState) {
            onViewportChange(viewportScaleState, viewportOffsetState)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .clipToBounds()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f))
    ) {
        val density = LocalDensity.current
        val viewportWidthDp = maxWidth.value
        val viewportHeightDp = maxHeight.value
        val draggedNode = canvasState.nodes.firstOrNull { it.id == draggedNodeId }
        val draggedPosition = activeSnapResult?.position ?: draggedNodePosition
        val draggedWidthExtent = if (draggedNode != null && draggedPosition != null) {
            draggedPosition.x + draggedNode.widthDp() + RULE_CANVAS_PADDING_DP
        } else {
            0f
        }
        val draggedHeightExtent = if (draggedPosition != null) {
            draggedPosition.y + RULE_CANVAS_NODE_HEIGHT_DP + RULE_CANVAS_PADDING_DP
        } else {
            0f
        }
        val canvasWidthDp = maxOf(
            RULE_CANVAS_WIDTH_DP,
            canvasState.nodes.maxOfOrNull { it.position.x + it.widthDp() + RULE_CANVAS_PADDING_DP } ?: 0f,
            draggedWidthExtent,
        ).coerceAtMost(RULE_CANVAS_MAX_WIDTH_DP)
        val canvasHeightDp = maxOf(
            RULE_CANVAS_HEIGHT_DP,
            canvasState.nodes.maxOfOrNull { it.position.y + RULE_CANVAS_NODE_HEIGHT_DP + RULE_CANVAS_PADDING_DP } ?: 0f,
            draggedHeightExtent,
        ).coerceAtMost(RULE_CANVAS_MAX_HEIGHT_DP)
        val viewportOffset = clampViewportOffset(
            offset = viewportOffsetState,
            scale = viewportScaleState,
            viewportWidth = viewportWidthDp,
            viewportHeight = viewportHeightDp,
            canvasWidth = canvasWidthDp,
            canvasHeight = canvasHeightDp,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(canvasWidthDp, canvasHeightDp) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val currentScale = viewportScaleState
                        val currentOffset = clampViewportOffset(
                            offset = viewportOffsetState,
                            scale = currentScale,
                            viewportWidth = viewportWidthDp,
                            viewportHeight = viewportHeightDp,
                            canvasWidth = canvasWidthDp,
                            canvasHeight = canvasHeightDp,
                        )
                        val nextScale = (currentScale * zoom).coerceIn(MinCanvasScale, MaxCanvasScale)
                        val centroidDp = Offset(
                            x = centroid.x / density.density,
                            y = centroid.y / density.density,
                        )
                        val panDp = Offset(
                            x = pan.x / density.density,
                            y = pan.y / density.density,
                        )
                        val logicalPoint = Offset(
                            x = (centroidDp.x - currentOffset.x) / currentScale,
                            y = (centroidDp.y - currentOffset.y) / currentScale,
                        )
                        val nextOffset = Offset(
                            x = centroidDp.x - logicalPoint.x * nextScale + panDp.x,
                            y = centroidDp.y - logicalPoint.y * nextScale + panDp.y,
                        )
                        viewportScaleState = nextScale
                        viewportOffsetState = clampViewportOffset(
                            offset = nextOffset,
                            scale = nextScale,
                            viewportWidth = viewportWidthDp,
                            viewportHeight = viewportHeightDp,
                            canvasWidth = canvasWidthDp,
                            canvasHeight = canvasHeightDp,
                        )
                    }
                }
        )

        Box(
            modifier = Modifier
                .offset(
                    x = viewportOffset.x.dp,
                    y = viewportOffset.y.dp,
                )
                .size(
                    width = (canvasWidthDp * viewportScaleState).dp,
                    height = (canvasHeightDp * viewportScaleState).dp,
                )
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )
        )

        canvasState.nodes.forEach { node ->
            val rawPosition = if (draggedNodeId == node.id) {
                draggedNodePosition ?: node.position
            } else {
                node.position
            }
            val displayedPosition = if (draggedNodeId == node.id) {
                activeSnapResult?.position ?: rawPosition
            } else {
                rawPosition
            }
            val screenPosition = Offset(
                x = viewportOffset.x + displayedPosition.x * viewportScaleState,
                y = viewportOffset.y + displayedPosition.y * viewportScaleState,
            )
            val nodeWidthDp = node.widthDp()

            Box(
                modifier = Modifier
                    .offset(
                        x = screenPosition.x.dp,
                        y = screenPosition.y.dp,
                    )
                    .size(
                        width = (nodeWidthDp * viewportScaleState).dp,
                        height = (RULE_CANVAS_NODE_HEIGHT_DP * viewportScaleState).dp,
                    )
                    .zIndex(if (draggedNodeId == node.id) 2f else 1f)
                    .pointerInput(node.id, canvasState.nodes) {
                        detectDragGestures(
                            onDragStart = {
                                draggedNodeId = node.id
                                draggedNodePosition = node.position
                                activeSnapResult = null
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val currentPosition = draggedNodePosition ?: node.position
                                val nextPosition = clampNodePosition(
                                    position = currentPosition + Offset(
                                        x = dragAmount.x / density.density / viewportScaleState,
                                        y = dragAmount.y / density.density / viewportScaleState,
                                    ),
                                    node = node,
                                )
                                val snapResult = findNodeSnap(
                                    movingNode = node,
                                    proposedPosition = nextPosition,
                                    nodes = canvasState.nodes,
                                )
                                draggedNodePosition = nextPosition
                                activeSnapResult = snapResult
                            },
                            onDragEnd = {
                                val snapResult = activeSnapResult
                                val finalPosition = snapResult?.position ?: draggedNodePosition ?: node.position
                                if (finalPosition != node.position || snapResult?.insertBeforeNodeId != null) {
                                    val nextNodes = if (snapResult?.insertBeforeNodeId != null) {
                                        insertNodeIntoChain(
                                            nodes = canvasState.nodes,
                                            movingNode = node,
                                            position = finalPosition,
                                            insertBeforeNodeId = snapResult.insertBeforeNodeId,
                                        )
                                    } else {
                                        canvasState.nodes.map { current ->
                                            if (current.id == node.id) current.copy(position = finalPosition) else current
                                        }
                                    }
                                    onNodesChange(nextNodes)
                                }
                                draggedNodeId = null
                                draggedNodePosition = null
                                activeSnapResult = null
                            },
                            onDragCancel = {
                                draggedNodeId = null
                                draggedNodePosition = null
                                activeSnapResult = null
                            },
                        )
                    }
            ) {
                CanvasNodeCard(
                    node = node,
                    isSelected = canvasState.selectedNodeId == node.id,
                    isSnapTarget = node.id in activeSnapResult?.targetNodeIds.orEmpty(),
                    onClick = { onNodeSelected(node.id) },
                )
                if (canvasState.selectedNodeId == node.id) {
                    Surface(
                        onClick = { onNodeDeleted(node.id) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer,
                    ) {
                        Text(
                            text = "×",
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        if (canvasState.nodes.isEmpty()) {
            Text(
                text = "从素材栏添加操作数、运算符、函数或分组符",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (isNavigatorExpanded) {
            CanvasViewportNavigator(
                nodes = canvasState.nodes,
                canvasWidth = canvasWidthDp,
                canvasHeight = canvasHeightDp,
                viewportWidth = viewportWidthDp,
                viewportHeight = viewportHeightDp,
                viewportScale = viewportScaleState,
                viewportOffset = viewportOffset,
                onViewportChange = { scale, offset ->
                    viewportScaleState = scale
                    viewportOffsetState = offset
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 76.dp),
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)),
            shadowElevation = 6.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        viewportScaleState = 1f
                        viewportOffsetState = Offset.Zero
                        onViewportChange(1f, Offset.Zero)
                    },
                    modifier = Modifier.size(44.dp),
                ) {
                    Icon(Icons.Default.CenterFocusStrong, contentDescription = "重置画布视窗")
                }
                IconButton(
                    onClick = { isNavigatorExpanded = !isNavigatorExpanded },
                    modifier = Modifier.size(44.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = if (isNavigatorExpanded) "收起缩放与导航" else "展开缩放与导航",
                        tint = if (isNavigatorExpanded) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                IconButton(
                    onClick = {
                        isNavigatorExpanded = false
                        onViewportChange(viewportScaleState, viewportOffsetState)
                        onToggleFullscreen()
                    },
                    modifier = Modifier.size(44.dp),
                ) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = if (isFullscreen) "退出全屏" else "横屏全屏",
                    )
                }
            }
        }
    }
}

@Composable
private fun CanvasViewportNavigator(
    nodes: List<CanvasNode>,
    canvasWidth: Float,
    canvasHeight: Float,
    viewportWidth: Float,
    viewportHeight: Float,
    viewportScale: Float,
    viewportOffset: Offset,
    onViewportChange: (Float, Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val canvasColor = MaterialTheme.colorScheme.surfaceVariant
    val nodeColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
    val viewportColor = MaterialTheme.colorScheme.primary

    fun updateScale(nextScale: Float) {
        val logicalCenter = Offset(
            x = (viewportWidth / 2f - viewportOffset.x) / viewportScale,
            y = (viewportHeight / 2f - viewportOffset.y) / viewportScale,
        )
        val nextOffset = Offset(
            x = viewportWidth / 2f - logicalCenter.x * nextScale,
            y = viewportHeight / 2f - logicalCenter.y * nextScale,
        )
        onViewportChange(
            nextScale,
            clampViewportOffset(
                offset = nextOffset,
                scale = nextScale,
                viewportWidth = viewportWidth,
                viewportHeight = viewportHeight,
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight,
            ),
        )
    }

    Surface(
        modifier = modifier.size(width = 148.dp, height = 120.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Canvas(
                modifier = Modifier
                    .size(width = 132.dp, height = 76.dp)
                    .pointerInput(
                        canvasWidth,
                        canvasHeight,
                        viewportWidth,
                        viewportHeight,
                        viewportScale,
                        viewportOffset,
                    ) {
                        detectTapGestures { tapPosition ->
                            val fitScale = min(
                                size.width.toFloat() / canvasWidth,
                                size.height.toFloat() / canvasHeight,
                            )
                            val contentWidth = canvasWidth * fitScale
                            val contentHeight = canvasHeight * fitScale
                            val origin = Offset(
                                x = (size.width - contentWidth) / 2f,
                                y = (size.height - contentHeight) / 2f,
                            )
                            val logicalPosition = Offset(
                                x = ((tapPosition.x - origin.x) / fitScale).coerceIn(0f, canvasWidth),
                                y = ((tapPosition.y - origin.y) / fitScale).coerceIn(0f, canvasHeight),
                            )
                            val nextOffset = Offset(
                                x = viewportWidth / 2f - logicalPosition.x * viewportScale,
                                y = viewportHeight / 2f - logicalPosition.y * viewportScale,
                            )
                            onViewportChange(
                                viewportScale,
                                clampViewportOffset(
                                    offset = nextOffset,
                                    scale = viewportScale,
                                    viewportWidth = viewportWidth,
                                    viewportHeight = viewportHeight,
                                    canvasWidth = canvasWidth,
                                    canvasHeight = canvasHeight,
                                ),
                            )
                        }
                    }
            ) {
                val fitScale = min(size.width / canvasWidth, size.height / canvasHeight)
                val contentWidth = canvasWidth * fitScale
                val contentHeight = canvasHeight * fitScale
                val origin = Offset(
                    x = (size.width - contentWidth) / 2f,
                    y = (size.height - contentHeight) / 2f,
                )

                drawRect(
                    color = canvasColor,
                    topLeft = origin,
                    size = androidx.compose.ui.geometry.Size(contentWidth, contentHeight),
                )
                nodes.forEach { node ->
                    drawRect(
                        color = nodeColor,
                        topLeft = Offset(
                            x = origin.x + node.position.x * fitScale,
                            y = origin.y + node.position.y * fitScale,
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            width = max(2f, node.widthDp() * fitScale),
                            height = max(2f, RULE_CANVAS_NODE_HEIGHT_DP * fitScale),
                        ),
                    )
                }

                val logicalViewportLeft = -viewportOffset.x / viewportScale
                val logicalViewportTop = -viewportOffset.y / viewportScale
                val logicalViewportWidth = viewportWidth / viewportScale
                val logicalViewportHeight = viewportHeight / viewportScale
                val viewportMapWidth = min(logicalViewportWidth, canvasWidth) * fitScale
                val viewportMapHeight = min(logicalViewportHeight, canvasHeight) * fitScale
                val viewportTopLeft = Offset(
                    x = origin.x + logicalViewportLeft
                        .coerceIn(0f, max(0f, canvasWidth - logicalViewportWidth)) * fitScale,
                    y = origin.y + logicalViewportTop
                        .coerceIn(0f, max(0f, canvasHeight - logicalViewportHeight)) * fitScale,
                )
                drawRect(
                    color = viewportColor.copy(alpha = 0.12f),
                    topLeft = viewportTopLeft,
                    size = androidx.compose.ui.geometry.Size(viewportMapWidth, viewportMapHeight),
                )
                drawRect(
                    color = viewportColor,
                    topLeft = viewportTopLeft,
                    size = androidx.compose.ui.geometry.Size(viewportMapWidth, viewportMapHeight),
                    style = Stroke(width = 2.dp.toPx()),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { updateScale((viewportScale / 1.2f).coerceAtLeast(MinCanvasScale)) },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "缩小画布视窗")
                }
                Text(
                    text = "${(viewportScale * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                IconButton(
                    onClick = { updateScale((viewportScale * 1.2f).coerceAtMost(MaxCanvasScale)) },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "放大画布视窗")
                }
            }
        }
    }
}

private fun clampViewportOffset(
    offset: Offset,
    scale: Float,
    viewportWidth: Float,
    viewportHeight: Float,
    canvasWidth: Float,
    canvasHeight: Float,
): Offset {
    fun clampAxis(value: Float, viewportSize: Float, contentSize: Float): Float {
        val scaledContentSize = contentSize * scale
        if (scaledContentSize <= viewportSize) return (viewportSize - scaledContentSize) / 2f
        return value.coerceIn(viewportSize - scaledContentSize - 24f, 24f)
    }

    return Offset(
        x = clampAxis(offset.x, viewportWidth, canvasWidth),
        y = clampAxis(offset.y, viewportHeight, canvasHeight),
    )
}

private fun clampNodePosition(
    position: Offset,
    node: CanvasNode,
): Offset {
    return Offset(
        x = position.x.coerceIn(
            RULE_CANVAS_PADDING_DP,
            RULE_CANVAS_MAX_WIDTH_DP - node.widthDp() - RULE_CANVAS_PADDING_DP,
        ),
        y = position.y.coerceIn(
            RULE_CANVAS_PADDING_DP,
            RULE_CANVAS_MAX_HEIGHT_DP - RULE_CANVAS_NODE_HEIGHT_DP - RULE_CANVAS_PADDING_DP,
        ),
    )
}

private fun findNodeSnap(
    movingNode: CanvasNode,
    proposedPosition: Offset,
    nodes: List<CanvasNode>,
): NodeSnapResult? {
    val movingWidth = movingNode.widthDp()
    val otherNodes = nodes.filterNot { it.id == movingNode.id }
    val insertionCandidates = buildList {
        otherNodes.forEach { left ->
            val right = otherNodes
                .asSequence()
                .filter { candidate ->
                    candidate.position.x > left.position.x &&
                        abs(candidate.position.y - left.position.y) <= SnapCrossAxisToleranceDp
                }
                .minByOrNull { it.position.x }
                ?: return@forEach
            val gap = right.position.x - (left.position.x + left.widthDp())
            val maximumGap = movingWidth + InsertChainGapToleranceDp + RULE_CANVAS_NODE_GAP_DP * 2f
            if (gap < -SnapThresholdDp || gap > maximumGap) return@forEach

            val insertionSeamX = (left.position.x + left.widthDp() + right.position.x) / 2f
            val movingCenterX = proposedPosition.x + movingWidth / 2f
            val distanceToSeam = abs(movingCenterX - insertionSeamX)
            val distanceToRow = abs(proposedPosition.y - left.position.y)
            if (distanceToSeam <= movingWidth / 2f + SnapThresholdDp &&
                distanceToRow <= SnapCrossAxisToleranceDp
            ) {
                add(
                    NodeSnapResult(
                        position = Offset(
                            x = left.position.x + left.widthDp() + RULE_CANVAS_NODE_GAP_DP,
                            y = left.position.y,
                        ),
                        targetNodeIds = setOf(left.id, right.id),
                        score = distanceToSeam + distanceToRow,
                        insertBeforeNodeId = right.id,
                    )
                )
            }
        }
    }
    insertionCandidates.minByOrNull { it.score }?.let { return it }

    val candidates = buildList {
        otherNodes.forEach { target ->
            val targetWidth = target.widthDp()

            if (abs(proposedPosition.y - target.position.y) <= SnapCrossAxisToleranceDp) {
                val positionAfterTarget = Offset(
                    x = target.position.x + targetWidth + RULE_CANVAS_NODE_GAP_DP,
                    y = target.position.y,
                )
                if (abs(proposedPosition.x - positionAfterTarget.x) <= SnapThresholdDp) {
                    add(
                        NodeSnapResult(
                            position = positionAfterTarget,
                            targetNodeIds = setOf(target.id),
                            score = abs(proposedPosition.x - positionAfterTarget.x) +
                                abs(proposedPosition.y - positionAfterTarget.y),
                        )
                    )
                }
                val positionBeforeTarget = Offset(
                    x = target.position.x - movingWidth - RULE_CANVAS_NODE_GAP_DP,
                    y = target.position.y,
                )
                if (abs(proposedPosition.x - positionBeforeTarget.x) <= SnapThresholdDp) {
                    add(
                        NodeSnapResult(
                            position = positionBeforeTarget,
                            targetNodeIds = setOf(target.id),
                            score = abs(proposedPosition.x - positionBeforeTarget.x) +
                                abs(proposedPosition.y - positionBeforeTarget.y),
                        )
                    )
                }
            }

            if (abs(proposedPosition.x - target.position.x) <= SnapCrossAxisToleranceDp) {
                val positionBelowTarget = Offset(
                    x = target.position.x,
                    y = target.position.y + RULE_CANVAS_NODE_HEIGHT_DP + RULE_CANVAS_NODE_GAP_DP,
                )
                if (abs(proposedPosition.y - positionBelowTarget.y) <= SnapThresholdDp) {
                    add(
                        NodeSnapResult(
                            position = positionBelowTarget,
                            targetNodeIds = setOf(target.id),
                            score = abs(proposedPosition.x - positionBelowTarget.x) +
                                abs(proposedPosition.y - positionBelowTarget.y),
                        )
                    )
                }
                val positionAboveTarget = Offset(
                    x = target.position.x,
                    y = target.position.y - RULE_CANVAS_NODE_HEIGHT_DP - RULE_CANVAS_NODE_GAP_DP,
                )
                if (abs(proposedPosition.y - positionAboveTarget.y) <= SnapThresholdDp) {
                    add(
                        NodeSnapResult(
                            position = positionAboveTarget,
                            targetNodeIds = setOf(target.id),
                            score = abs(proposedPosition.x - positionAboveTarget.x) +
                                abs(proposedPosition.y - positionAboveTarget.y),
                        )
                    )
                }
            }
        }
    }

    return candidates
        .asSequence()
        .filter { candidate ->
            candidate.score <= SnapThresholdDp + SnapCrossAxisToleranceDp &&
                candidate.position.x >= RULE_CANVAS_PADDING_DP &&
                candidate.position.y >= RULE_CANVAS_PADDING_DP &&
                candidate.position.x + movingWidth <= RULE_CANVAS_MAX_WIDTH_DP - RULE_CANVAS_PADDING_DP &&
                candidate.position.y + RULE_CANVAS_NODE_HEIGHT_DP <= RULE_CANVAS_MAX_HEIGHT_DP - RULE_CANVAS_PADDING_DP
        }
        .minByOrNull { it.score }
}

private fun insertNodeIntoChain(
    nodes: List<CanvasNode>,
    movingNode: CanvasNode,
    position: Offset,
    insertBeforeNodeId: String,
): List<CanvasNode> {
    val insertBeforeNode = nodes.firstOrNull { it.id == insertBeforeNodeId } ?: return nodes.map { node ->
        if (node.id == movingNode.id) node.copy(position = position) else node
    }
    val rowNodes = nodes
        .filter { node ->
            node.id != movingNode.id &&
                abs(node.position.y - insertBeforeNode.position.y) <= SnapCrossAxisToleranceDp
        }
        .sortedBy { it.position.x }
    val startIndex = rowNodes.indexOfFirst { it.id == insertBeforeNodeId }
    if (startIndex < 0) return nodes

    val shiftedNodes = mutableListOf<CanvasNode>()
    var previousNode: CanvasNode? = null
    for (index in startIndex until rowNodes.size) {
        val currentNode = rowNodes[index]
        val gapFromPrevious = previousNode?.let { previous ->
            currentNode.position.x - (previous.position.x + previous.widthDp())
        }
        if (gapFromPrevious != null && gapFromPrevious > InsertChainGapToleranceDp) break
        shiftedNodes += currentNode
        previousNode = currentNode
    }

    val requiredShift = position.x + movingNode.widthDp() + RULE_CANVAS_NODE_GAP_DP - insertBeforeNode.position.x
    val maximumShift = shiftedNodes.minOfOrNull { node ->
        RULE_CANVAS_MAX_WIDTH_DP - RULE_CANVAS_PADDING_DP - node.position.x - node.widthDp()
    } ?: 0f
    val shift = requiredShift.coerceIn(0f, max(0f, maximumShift))
    val shiftedNodeIds = shiftedNodes.mapTo(mutableSetOf()) { it.id }
    return nodes.map { node ->
        when {
            node.id == movingNode.id -> node.copy(position = position)
            node.id in shiftedNodeIds -> node.copy(position = node.position + Offset(shift, 0f))
            else -> node
        }
    }
}

@Composable
private fun CanvasNodeCard(
    node: CanvasNode,
    isSelected: Boolean,
    isSnapTarget: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = when (node.type) {
        CanvasNodeType.OPERAND -> when {
            node.operandConfig == null -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
            node.operandConfig.direction == RuleSlotDirection.INPUT -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f)
            else -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.78f)
        }
        CanvasNodeType.OPERATOR -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
        CanvasNodeType.FUNCTION -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        CanvasNodeType.GROUPING -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        CanvasNodeType.LITERAL -> MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
    }

    val borderColor = when {
        isSnapTarget -> MaterialTheme.colorScheme.tertiary
        isSelected -> MaterialTheme.colorScheme.primary
        node.type == CanvasNodeType.OPERAND && node.operandConfig == null -> MaterialTheme.colorScheme.error
        else -> Color.Transparent
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(if (isSnapTarget) 3.dp else 2.dp, borderColor),
        shadowElevation = if (isSnapTarget) 8.dp else 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (node.type) {
                CanvasNodeType.OPERAND -> {
                    val operandConfig = node.operandConfig
                    val title = operandConfig?.key
                        ?.ifBlank { node.displayText ?: "操作数" }
                        ?: (node.displayText ?: "待配置")
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Text(
                        text = when {
                            node.operandConfig == null -> "待配置操作数"
                            node.operandConfig.direction == RuleSlotDirection.INPUT -> "输入"
                            else -> "输出"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                CanvasNodeType.OPERATOR -> {
                    Text(
                        text = node.operatorSymbol ?: node.displayText ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                CanvasNodeType.FUNCTION -> {
                    Text(
                        text = node.functionName ?: node.displayText ?: "func",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "函数",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                CanvasNodeType.GROUPING -> {
                    Text(
                        text = node.groupingSymbol ?: node.displayText ?: "(",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                CanvasNodeType.LITERAL -> {
                    Text(
                        text = node.literalValue ?: node.displayText ?: "值",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "字面量",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
