package com.example.itemmanagement.ui.attribute

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 规则画布素材栏分类
 *
 * 每个分类用一个图标表示，点击图标后在下方展开一行可左右滑动的实际素材条目，
 * 避免长期占用画布左侧的纵向空间。
 */
private data class PaletteCategory(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val items: List<PaletteItem>,
)

/**
 * 规则画布素材栏（横向图标分类 + 下方可滑动素材条）
 */
@Composable
fun RuleCanvasPalette(
    onPaletteItemSelected: (PaletteItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories = remember {
        listOf(
            PaletteCategory(
                id = "operand",
                label = "操作数",
                icon = Icons.Filled.Extension,
                items = listOf(PaletteItem.Operand),
            ),
            PaletteCategory(
                id = "arithmetic",
                label = "算术运算",
                icon = Icons.Filled.Calculate,
                items = listOf(
                    PaletteItem.Operator("+", "加"),
                    PaletteItem.Operator("-", "减"),
                    PaletteItem.Operator("*", "乘"),
                    PaletteItem.Operator("/", "除"),
                    PaletteItem.Operator("%", "取余"),
                ),
            ),
            PaletteCategory(
                id = "comparison",
                label = "比较运算",
                icon = Icons.Filled.CompareArrows,
                items = listOf(
                    PaletteItem.Operator("=", "赋值"),
                    PaletteItem.Operator(">", "大于"),
                    PaletteItem.Operator("<", "小于"),
                    PaletteItem.Operator(">=", "大于等于"),
                    PaletteItem.Operator("<=", "小于等于"),
                    PaletteItem.Operator("==", "等于"),
                    PaletteItem.Operator("!=", "不等于"),
                ),
            ),
            PaletteCategory(
                id = "logic",
                label = "逻辑运算",
                icon = Icons.Filled.AccountTree,
                items = listOf(
                    PaletteItem.Operator("&&", "与"),
                    PaletteItem.Operator("||", "或"),
                    PaletteItem.Operator("!", "非"),
                ),
            ),
            PaletteCategory(
                id = "function",
                label = "函数",
                icon = Icons.Filled.Functions,
                items = listOf(
                    PaletteItem.Function("if", "条件"),
                    PaletteItem.Function("coalesce", "合并"),
                    PaletteItem.Function("dateDiff", "日期差"),
                    PaletteItem.Function("cycleCount", "周期数"),
                ),
            ),
            PaletteCategory(
                id = "grouping",
                label = "分组符",
                icon = Icons.Filled.Code,
                items = listOf(
                    PaletteItem.Grouping("(", "左圆括号"),
                    PaletteItem.Grouping(")", "右圆括号"),
                    PaletteItem.Grouping("{", "左花括号"),
                    PaletteItem.Grouping("}", "右花括号"),
                    PaletteItem.Grouping("[", "左方括号"),
                    PaletteItem.Grouping("]", "右方括号"),
                ),
            ),
        )
    }

    var selectedCategoryId by remember { mutableStateOf<String?>(categories.firstOrNull()?.id) }
    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "素材栏",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )

            // 分类图标横向排列，点击切换下方展开的素材条
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(categories, key = { it.id }) { category ->
                    val isSelected = category.id == selectedCategoryId
                    PaletteCategoryIcon(
                        icon = category.icon,
                        label = category.label,
                        isSelected = isSelected,
                        onClick = {
                            selectedCategoryId = if (isSelected) null else category.id
                        },
                    )
                }
            }

            AnimatedVisibility(
                visible = selectedCategory != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                selectedCategory?.let { category ->
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                    ) {
                        items(category.items) { item ->
                            PaletteItemCard(
                                item = item,
                                onClick = { onPaletteItemSelected(item) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 分类图标按钮：图标 + 下方文字标签，选中态高亮
 */
@Composable
private fun PaletteCategoryIcon(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
    ) {
        Column(
            modifier = Modifier
                .width(64.dp)
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                maxLines = 1,
            )
        }
    }
}

/**
 * 素材栏项目卡片（横向排列，宽度固定，配合 LazyRow 左右滑动）
 */
@Composable
private fun PaletteItemCard(
    item: PaletteItem,
    onClick: () -> Unit,
) {
    val (displayText, subtitle) = when (item) {
        PaletteItem.Operand -> "操作数" to "节点内配置"
        is PaletteItem.Operator -> item.symbol to item.label
        is PaletteItem.Function -> item.name to item.label
        is PaletteItem.Grouping -> item.symbol to item.label
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(96.dp)
            .height(60.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = displayText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
