package com.example.itemmanagement.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiaoIconPickerSheet(
    initialIcon: IconSource = IconSource.None,
    onDismiss: () -> Unit,
    onIconSelected: (IconSource) -> Unit
) {
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
            MiaoIconPickerPanel(
                initialIcon = initialIcon,
                modifier = Modifier.fillMaxSize(),
                onIconSelected = {
                    onIconSelected(it)
                    onDismiss()
                },
                onRemove = {
                    onIconSelected(IconSource.None)
                    onDismiss()
                },
                onConfirm = onDismiss
            )
        }
    }
}

@Composable
fun MiaoCompactDialog(
    title: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    confirmText: String,
    dismissText: String,
    modifier: Modifier = Modifier,
    confirmEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp)
                .padding(horizontal = 6.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp)
            ) {
                Text(
                    modifier = Modifier.padding(bottom = 8.dp),
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    content()
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(dismissText)
                    }
                    TextButton(
                        enabled = confirmEnabled,
                        onClick = onConfirm
                    ) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}

@Composable
fun MiaoIconPickerPanel(
    initialIcon: IconSource = IconSource.None,
    modifier: Modifier = Modifier,
    onIconSelected: (IconSource) -> Unit,
    onRemove: (() -> Unit)? = null,
    onConfirm: (() -> Unit)? = null,
    embedded: Boolean = false
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(if (initialIcon is IconSource.Emoji || initialIcon is IconSource.None) 0 else 1) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    Column(modifier = modifier) {
        IconPickerTabs(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            onRemove = onRemove,
            embedded = embedded
        )

        SearchBarRow(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = when (selectedTab) {
                1 -> "搜索 Material Symbols"
                2 -> "上传功能开发中"
                else -> "搜索表情或图标"
            },
            onRandom = when (selectedTab) {
                0 -> ({
                    CATEGORIZED_EMOJIS
                        .flatMap { it.emojis }
                        .randomOrNull()
                        ?.let { onIconSelected(IconSource.Emoji(it)) }
                })
                1 -> ({
                    MATERIAL_SYMBOL_CATEGORIES
                        .flatMap { it.icons }
                        .randomOrNull()
                        ?.let { onIconSelected(IconSource.Vector(it.name)) }
                })
                else -> null
            },
            onConfirm = onConfirm,
            embedded = embedded
        )

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> EmojiPickerContent(
                    searchQuery = searchQuery,
                    onEmojiSelected = { onIconSelected(IconSource.Emoji(it)) },
                    embedded = embedded
                )
                1 -> MaterialSymbolPickerContent(
                    searchQuery = searchQuery,
                    selectedName = (initialIcon as? IconSource.Vector)?.name,
                    onIconSelected = { onIconSelected(IconSource.Vector(it)) },
                    embedded = embedded
                )
                2 -> PlaceholderContent("上传功能开发中...")
            }
        }
    }
}

@Composable
private fun IconPickerTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onRemove: (() -> Unit)?,
    embedded: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (embedded) 0.dp else 16.dp, vertical = if (embedded) 4.dp else 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(if (embedded) 4.dp else 8.dp)) {
            listOf("表情符号", "图标", "上传").forEachIndexed { index, title ->
                TextButton(
                    onClick = { onTabSelected(index) },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (selectedTab == index) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) 
                            else Color.Transparent
                    ),
                    shape = RoundedCornerShape(if (embedded) 10.dp else 12.dp)
                ) {
                    Text(
                        text = title,
                        style = if (embedded) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == index) 
                            MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (onRemove != null) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(if (embedded) 32.dp else 40.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "移除图标",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(if (embedded) 16.dp else 20.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchBarRow(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    onRandom: (() -> Unit)?,
    onConfirm: (() -> Unit)?,
    embedded: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (embedded) 0.dp else 16.dp,
                end = if (embedded) 0.dp else 16.dp,
                bottom = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .height(if (embedded) 48.dp else 52.dp),
            placeholder = {
                Text(
                    placeholder,
                    style = if (embedded) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodySmall
                )
            },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(if (embedded) 18.dp else 20.dp)) },
            singleLine = true,
            shape = RoundedCornerShape(if (embedded) 14.dp else 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.2f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.1f)
            )
        )
        
        if (onRandom != null) {
            TextButton(
                onClick = onRandom,
                contentPadding = PaddingValues(horizontal = if (embedded) 10.dp else 12.dp, vertical = 0.dp)
            ) {
                Text(
                    "随机",
                    style = if (embedded) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge
                )
            }
        }
        if (onConfirm != null) {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text("确认")
            }
        }
    }
}

@Composable
private fun MaterialSymbolPickerContent(
    searchQuery: String,
    selectedName: String?,
    onIconSelected: (String) -> Unit,
    embedded: Boolean
) {
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    var clickedCategoryKey by remember { mutableStateOf<String?>(null) }
    val normalizedQuery = searchQuery.trim()
    val filteredCategories = remember(normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            MATERIAL_SYMBOL_CATEGORIES
        } else {
            MATERIAL_SYMBOL_CATEGORIES.mapNotNull { category ->
                val filteredIcons = category.icons.filter { icon ->
                    icon.name.contains(normalizedQuery, ignoreCase = true) ||
                        materialSymbolDisplayName(icon.name).contains(normalizedQuery, ignoreCase = true) ||
                        icon.tags.any { tag -> tag.contains(normalizedQuery, ignoreCase = true) }
                }
                if (filteredIcons.isNotEmpty()) {
                    category.copy(icons = filteredIcons)
                } else {
                    null
                }
            }
        }
    }
    val itemIndexByCategory = remember(filteredCategories) {
        buildMap {
            var currentIndex = 0
            filteredCategories.forEach { category ->
                put(category.key, currentIndex)
                currentIndex += 1 + category.icons.size
            }
        }
    }
    val categoryRangeByKey = remember(filteredCategories, itemIndexByCategory) {
        buildMap {
            filteredCategories.forEach { category ->
                val startIndex = itemIndexByCategory[category.key] ?: return@forEach
                put(category.key, startIndex..(startIndex + category.icons.size))
            }
        }
    }
    val scrollDrivenCategoryKey by remember(filteredCategories, itemIndexByCategory, categoryRangeByKey, gridState) {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                filteredCategories.firstOrNull()?.key
                    ?: MATERIAL_SYMBOL_CATEGORIES.firstOrNull()?.key
                    ?: "home"
            } else {
                val lastVisibleIndex = visibleItems.maxOf { it.index }
                if (lastVisibleIndex >= layoutInfo.totalItemsCount - 1) {
                    filteredCategories.lastOrNull()?.key
                        ?: MATERIAL_SYMBOL_CATEGORIES.lastOrNull()?.key
                        ?: "home"
                } else {
                    val firstVisibleIndex = visibleItems.minOf { it.index }
                    filteredCategories
                        .lastOrNull { category ->
                            val categoryIndex = itemIndexByCategory[category.key] ?: Int.MIN_VALUE
                            categoryIndex <= firstVisibleIndex
                        }
                        ?.key
                        ?: filteredCategories.firstOrNull()?.key
                        ?: MATERIAL_SYMBOL_CATEGORIES.firstOrNull()?.key
                        ?: "home"
                }
            }
        }
    }
    val visibleCategoryKeys by remember(categoryRangeByKey, gridState) {
        derivedStateOf {
            val visibleIndices = gridState.layoutInfo.visibleItemsInfo.map { it.index }.toSet()
            categoryRangeByKey
                .filterValues { range -> visibleIndices.any { it in range } }
                .keys
        }
    }
    val activeCategoryKey by remember(clickedCategoryKey, scrollDrivenCategoryKey) {
        derivedStateOf { clickedCategoryKey ?: scrollDrivenCategoryKey }
    }

    LaunchedEffect(clickedCategoryKey, visibleCategoryKeys) {
        val targetKey = clickedCategoryKey ?: return@LaunchedEffect
        if (targetKey !in visibleCategoryKeys) {
            clickedCategoryKey = null
        }
    }

    if (filteredCategories.isEmpty()) {
        PlaceholderContent("没有找到匹配图标")
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = if (embedded) 42.dp else 50.dp),
            state = gridState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = if (embedded) 4.dp else 16.dp,
                end = if (embedded) 4.dp else 16.dp,
                top = if (embedded) 4.dp else 16.dp,
                bottom = if (embedded) 12.dp else 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(if (embedded) 8.dp else 10.dp),
            verticalArrangement = Arrangement.spacedBy(if (embedded) 8.dp else 10.dp)
        ) {
            filteredCategories.forEach { category ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = materialSymbolCategoryLabel(category.key),
                        style = if (embedded) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = if (embedded) 8.dp else 16.dp, bottom = if (embedded) 4.dp else 8.dp)
                    )
                }

                items(category.icons, key = { it.name }) { icon ->
                    val isSelected = icon.name == selectedName
                    Surface(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { onIconSelected(icon.name) },
                        shape = RoundedCornerShape(if (embedded) 12.dp else 14.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = if (embedded) 0.16f else 0.18f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (embedded) 0.4f else 0.55f)
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            }
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            MaterialSymbolGlyph(
                                name = icon.name,
                                fontSize = if (embedded) 22.sp else 24.sp,
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                axes = materialSymbolAxes(
                                    renderMode = MaterialSymbolRenderMode.Picker,
                                    fontSize = if (embedded) 22.sp else 24.sp,
                                    filled = isSelected
                                )
                            )
                        }
                    }
                }
            }
        }

        if (normalizedQuery.isBlank()) {
            val navScrollState = rememberScrollState()
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(if (embedded) 2.dp else 0.dp)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                Row(
                    modifier = Modifier
                        .padding(
                            top = if (embedded) 2.dp else 8.dp,
                            bottom = if (embedded) 0.dp else 8.dp,
                            start = if (embedded) 4.dp else 16.dp,
                            end = if (embedded) 4.dp else 16.dp
                        )
                        .fillMaxWidth()
                        .horizontalScroll(navScrollState),
                    horizontalArrangement = Arrangement.spacedBy(if (embedded) 6.dp else 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MATERIAL_SYMBOL_NAV_ITEMS.forEach { navItem ->
                        val isActive = navItem.categoryKey == activeCategoryKey
                        Box(
                            modifier = Modifier
                                .size(if (embedded) 30.dp else 36.dp)
                                .clip(CircleShape)
                                .clickable {
                                    clickedCategoryKey = navItem.categoryKey
                                    val targetIndex = itemIndexByCategory[navItem.categoryKey] ?: return@clickable
                                    scope.launch {
                                        gridState.scrollToItem(targetIndex)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            MaterialSymbolGlyph(
                                name = navItem.iconName,
                                fontSize = if (embedded) 18.sp else 20.sp,
                                tint = if (isActive) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                axes = materialSymbolAxes(
                                    renderMode = MaterialSymbolRenderMode.Picker,
                                    fontSize = if (embedded) 18.sp else 20.sp,
                                    filled = isActive
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmojiPickerContent(
    searchQuery: String,
    onEmojiSelected: (String) -> Unit,
    embedded: Boolean
) {
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    var clickedCategoryName by remember { mutableStateOf<String?>(null) }
    val normalizedQuery = searchQuery.trim()
    val filteredCategories = remember(searchQuery) {
        if (normalizedQuery.isBlank()) {
            CATEGORIZED_EMOJIS
        } else {
            CATEGORIZED_EMOJIS.mapNotNull { category ->
                val filteredEmojis = category.emojis.filter { it.contains(normalizedQuery) }
                if (filteredEmojis.isNotEmpty()) {
                    category.copy(emojis = filteredEmojis)
                } else null
            }
        }
    }
    val itemIndexByCategory = remember(filteredCategories) {
        buildMap {
            var currentIndex = 0
            filteredCategories.forEach { category ->
                put(category.name, currentIndex)
                currentIndex += 1 + category.emojis.size
            }
        }
    }
    val categoryRangeByName = remember(filteredCategories, itemIndexByCategory) {
        buildMap {
            filteredCategories.forEach { category ->
                val startIndex = itemIndexByCategory[category.name] ?: return@forEach
                put(category.name, startIndex..(startIndex + category.emojis.size))
            }
        }
    }
    val scrollDrivenCategoryName by remember(filteredCategories, itemIndexByCategory, categoryRangeByName, gridState) {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                filteredCategories.firstOrNull()?.name
                    ?: CATEGORIZED_EMOJIS.firstOrNull()?.name
                    ?: "最近使用"
            } else {
                val lastVisibleIndex = visibleItems.maxOf { it.index }
                if (lastVisibleIndex >= layoutInfo.totalItemsCount - 1) {
                    filteredCategories.lastOrNull()?.name
                        ?: CATEGORIZED_EMOJIS.lastOrNull()?.name
                        ?: "最近使用"
                } else {
                    val firstVisibleIndex = visibleItems.minOf { it.index }
                    filteredCategories
                        .lastOrNull { category ->
                            val categoryIndex = itemIndexByCategory[category.name] ?: Int.MIN_VALUE
                            categoryIndex <= firstVisibleIndex
                        }
                        ?.name
                        ?: filteredCategories.firstOrNull()?.name
                        ?: CATEGORIZED_EMOJIS.firstOrNull()?.name
                        ?: "最近使用"
                }
            }
        }
    }
    val visibleCategoryNames by remember(categoryRangeByName, gridState) {
        derivedStateOf {
            val visibleIndices = gridState.layoutInfo.visibleItemsInfo.map { it.index }.toSet()
            categoryRangeByName
                .filterValues { range -> visibleIndices.any { it in range } }
                .keys
        }
    }
    val activeCategoryName by remember(clickedCategoryName, scrollDrivenCategoryName) {
        derivedStateOf { clickedCategoryName ?: scrollDrivenCategoryName }
    }

    LaunchedEffect(clickedCategoryName, visibleCategoryNames) {
        val targetName = clickedCategoryName ?: return@LaunchedEffect
        if (targetName !in visibleCategoryNames) {
            clickedCategoryName = null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = if (embedded) 40.dp else 48.dp),
            state = gridState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = if (embedded) 4.dp else 16.dp,
                end = if (embedded) 4.dp else 16.dp,
                top = if (embedded) 4.dp else 16.dp,
                bottom = if (embedded) 12.dp else 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(if (embedded) 6.dp else 8.dp),
            verticalArrangement = Arrangement.spacedBy(if (embedded) 6.dp else 8.dp)
        ) {
            filteredCategories.forEachIndexed { _, category ->
                // 分类标题
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = category.name,
                        style = if (embedded) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = if (embedded) 8.dp else 16.dp, bottom = if (embedded) 4.dp else 8.dp)
                    )
                }
                
                // Emoji 网格
                items(category.emojis) { emoji ->
                    Surface(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { onEmojiSelected(emoji) },
                        shape = RoundedCornerShape(if (embedded) 10.dp else 12.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = emoji, fontSize = if (embedded) 20.sp else 24.sp)
                        }
                    }
                }
            }
        }

        // 锚点导航栏 (仅在非搜索状态显示)
        if (normalizedQuery.isBlank()) {
            val navScrollState = rememberScrollState()
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(if (embedded) 2.dp else 0.dp)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                Row(
                    modifier = Modifier
                        .padding(
                            top = if (embedded) 2.dp else 8.dp,
                            bottom = if (embedded) 0.dp else 8.dp,
                            start = if (embedded) 4.dp else 16.dp,
                            end = if (embedded) 4.dp else 16.dp
                        )
                        .fillMaxWidth()
                        .horizontalScroll(navScrollState),
                    horizontalArrangement = Arrangement.spacedBy(if (embedded) 6.dp else 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EMOJI_NAV_ITEMS.forEach { navItem ->
                        val isActive = navItem.categoryName == activeCategoryName
                        Box(
                            modifier = Modifier
                                .size(if (embedded) 30.dp else 36.dp)
                                .clip(CircleShape)
                                .clickable {
                                    clickedCategoryName = navItem.categoryName
                                    val targetIndex = itemIndexByCategory[navItem.categoryName] ?: return@clickable
                                    scope.launch {
                                        gridState.scrollToItem(targetIndex)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            MaterialSymbolGlyph(
                                name = navItem.iconName,
                                fontSize = if (embedded) 18.sp else 20.sp,
                                tint = if (isActive) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                axes = materialSymbolAxes(
                                    renderMode = MaterialSymbolRenderMode.Picker,
                                    fontSize = if (embedded) 18.sp else 20.sp,
                                    filled = isActive
                                )
                            )
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
