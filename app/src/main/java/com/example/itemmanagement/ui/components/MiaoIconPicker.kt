package com.example.itemmanagement.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSize
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * MiaoLog 通用图标选择器组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiaoIconPickerSheet(
    initialIcon: IconSource = IconSource.None,
    onDismiss: () -> Unit,
    onIconSelected: (IconSource) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = null
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            contentPadding = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 顶部 Tabs
                IconPickerTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onRemove = { onIconSelected(IconSource.None); onDismiss() }
                )

                // 搜索与操作条
                SearchBarRow(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onRandom = { 
                        val allEmojis = CATEGORIZED_EMOJIS.flatMap { it.emojis }
                        onIconSelected(IconSource.Emoji(allEmojis.random()))
                        onDismiss()
                    },
                    onConfirm = { onDismiss() }
                )

                // 主内容区
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> EmojiPickerContent(
                            searchQuery = searchQuery,
                            onEmojiSelected = { 
                                onIconSelected(IconSource.Emoji(it))
                                onDismiss()
                            }
                        )
                        1 -> PlaceholderContent("矢量图标功能开发中...")
                        2 -> PlaceholderContent("上传功能开发中...")
                    }
                }
            }
        }
    }
}

@Composable
private fun IconPickerTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("表情符号", "图标", "上传").forEachIndexed { index, title ->
                TextButton(
                    onClick = { onTabSelected(index) },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (selectedTab == index) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) 
                            else Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == index) 
                            MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Close, contentDescription = "移除图标", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun SearchBarRow(
    query: String,
    onQueryChange: (String) -> Unit,
    onRandom: () -> Unit,
    onConfirm: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("搜索表情或图标", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.2f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.1f)
            )
        )
        
        TextButton(onClick = onRandom) {
            Text("随机")
        }
        
        Button(
            onClick = onConfirm,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Text("确认")
        }
    }
}

@Composable
private fun EmojiPickerContent(
    searchQuery: String,
    onEmojiSelected: (String) -> Unit
) {
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    
    val filteredCategories = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            CATEGORIZED_EMOJIS
        } else {
            CATEGORIZED_EMOJIS.mapNotNull { category ->
                val filteredEmojis = category.emojis.filter { it.contains(searchQuery) }
                if (filteredEmojis.isNotEmpty()) {
                    category.copy(emojis = filteredEmojis)
                } else null
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 48.dp),
            state = gridState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filteredCategories.forEachIndexed { catIndex, category ->
                // 分类标题
                item(span = { GridItemSize.Fill }) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                
                // Emoji 网格
                items(category.emojis) { emoji ->
                    Surface(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { onEmojiSelected(emoji) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = emoji, fontSize = 24.sp)
                        }
                    }
                }
            }
        }

        // 锚点导航栏 (仅在非搜索状态显示)
        if (searchQuery.isBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.1f),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CATEGORIZED_EMOJIS.forEachIndexed { index, category ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable {
                                    scope.launch {
                                        // 计算跳转索引：每个分类前面的 item 数 (1个标题 + N个emoji)
                                        var targetIndex = 0
                                        for (i in 0 until index) {
                                            targetIndex += 1 // 标题
                                            targetIndex += CATEGORIZED_EMOJIS[i].emojis.size
                                        }
                                        gridState.animateScrollToItem(targetIndex)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = category.icon, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceholderContent(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
