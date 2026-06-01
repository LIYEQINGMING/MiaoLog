package com.example.itemmanagement.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeRevealDeleteContainer(
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    if (!enabled) {
        Box(modifier = modifier.fillMaxWidth()) {
            content()
        }
        return
    }

    val actionWidth = 64.dp
    val actionWidthPx = with(LocalDensity.current) { actionWidth.toPx() }
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val revealFraction by remember {
        derivedStateOf {
            (-offsetX.value / actionWidthPx).coerceIn(0f, 1f)
        }
    }
    val actionVisibility by remember {
        derivedStateOf {
            when {
                revealFraction < 0.98f -> 0f
                else -> ((revealFraction - 0.98f) / 0.02f).coerceIn(0f, 1f)
            }
        }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Surface(
                modifier = Modifier.graphicsLayer {
                    alpha = actionVisibility
                    scaleX = 0.92f + 0.08f * actionVisibility
                    scaleY = 0.92f + 0.08f * actionVisibility
                },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                IconButton(
                    modifier = Modifier.size(width = 52.dp, height = 40.dp),
                    enabled = actionVisibility > 0.99f,
                    onClick = {
                        scope.launch {
                            offsetX.animateTo(0f)
                        }
                        onDeleteClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            offsetX.snapTo((offsetX.value + delta).coerceIn(-actionWidthPx, 0f))
                        }
                    },
                    onDragStopped = { velocity ->
                        val reveal = offsetX.value <= -actionWidthPx * 0.45f || velocity < -1200f
                        scope.launch {
                            offsetX.animateTo(if (reveal) -actionWidthPx else 0f)
                        }
                    }
                )
        ) {
            content()
        }
    }
}
