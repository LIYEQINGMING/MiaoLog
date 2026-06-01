package com.example.itemmanagement.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.ManageSearch
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.itemmanagement.R
import com.example.itemmanagement.ui.components.GlassCard
import com.example.itemmanagement.ui.components.springClick

data class NavItem(
    val id: Int,
    val icon: ImageVector,
    val label: String,
    val isAddButton: Boolean = false
)

@Composable
fun CapsuleBottomNavigation(
    currentSelectionId: Int,
    onItemSelected: (Int) -> Unit,
    onItemReselected: (Int) -> Unit,
    onAddLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem(R.id.navigation_home, Icons.Outlined.Home, "首页"),
        NavItem(R.id.navigation_warehouse, Icons.Outlined.ManageSearch, "检索"),
        NavItem(R.id.navigation_add_item, Icons.Outlined.Add, "记录", isAddButton = true),
        NavItem(R.id.navigation_inventory_analysis, Icons.Outlined.Insights, "统计"),
        NavItem(R.id.navigation_profile, Icons.Outlined.Person, "我的")
    )
    val leftItems = items.take(2)
    val addItem = items[2]
    val rightItems = items.takeLast(2)
    val navShape = RoundedCornerShape(34.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 28.dp, end = 28.dp, bottom = 32.dp, top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = navShape,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
            shape = navShape,
            blurRadius = 24.dp,
            contentPadding = 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.26f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.18f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.32f)
                            )
                        ),
                        shape = navShape
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.65f),
                                Color.White.copy(alpha = 0.18f)
                            )
                        ),
                        shape = navShape
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        leftItems.forEach { item ->
                            NavCapsuleItem(
                                item = item,
                                isSelected = currentSelectionId == item.id,
                                onClick = {
                                    if (currentSelectionId == item.id) onItemReselected(item.id) else onItemSelected(item.id)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(56.dp))

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rightItems.forEach { item ->
                            NavCapsuleItem(
                                item = item,
                                isSelected = currentSelectionId == item.id,
                                onClick = {
                                    if (currentSelectionId == item.id) onItemReselected(item.id) else onItemSelected(item.id)
                                }
                            )
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .size(58.dp)
                .springClick(onLongClick = onAddLongClick) {
                    if (currentSelectionId == addItem.id) {
                        onItemReselected(addItem.id)
                    } else {
                        onItemSelected(addItem.id)
                    }
                },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.94f),
            shadowElevation = 14.dp,
            tonalElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.24f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = addItem.icon,
                    contentDescription = addItem.label,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun NavCapsuleItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(56.dp)
            .clip(RoundedCornerShape(22.dp))
            .springClick(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                    } else {
                        Color.Transparent
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
