package com.example.itemmanagement.ui.category

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.itemmanagement.ui.main.LiquidBackground
import com.example.itemmanagement.ui.components.GlassCard
import com.example.itemmanagement.ui.theme.LiquidGlassTheme
import com.example.itemmanagement.utils.DEFAULT_CATEGORY_ICONS
import com.example.itemmanagement.utils.buildCategoryChildNodes
import com.example.itemmanagement.utils.categoryPathDisplayName
import com.example.itemmanagement.utils.categoryPathParent
import com.example.itemmanagement.utils.defaultCategoryIcon
import com.example.itemmanagement.utils.normalizeCategoryPath
import com.example.itemmanagement.utils.splitCategoryPath

private data class CategoryListEntry(
    val path: String,
    val name: String,
    val icon: String,
    val childCount: Int,
    val itemCount: Int,
    val hasChildren: Boolean,
)

private sealed interface CategoryDialogState {
    data class Create(
        val parentPath: String,
        val initialName: String = "",
        val initialIcon: String = "",
        val shouldScroll: Boolean = false,
    ) : CategoryDialogState

    data class Rename(
        val path: String,
        val initialName: String,
        val initialIcon: String,
    ) : CategoryDialogState

    data class Delete(
        val path: String,
        val hasChildren: Boolean,
        val itemCount: Int,
    ) : CategoryDialogState
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryManagementScreen(
    uiState: CategoryManagementUiState,
    onCreateCategory: (parentPath: String, name: String, icon: String) -> Unit,
    onRenameCategory: (path: String, name: String, icon: String) -> Unit,
    onDeleteCategory: (path: String) -> Unit,
    onClose: () -> Unit,
    onMessageConsumed: () -> Unit,
) {
    var currentPath by rememberSaveable { mutableStateOf("") }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var dialogState by remember { mutableStateOf<CategoryDialogState?>(null) }
    var lastCreatedPath by rememberSaveable { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val normalizedSearchQuery = searchQuery.trim()

    val normalizedCurrentPath = remember(currentPath, uiState.allPaths) {
        val normalized = normalizeCategoryPath(currentPath)
        normalized.takeIf { it.isBlank() || uiState.allPaths.contains(it) }.orEmpty()
    }
    val childEntries = remember(uiState.allPaths, uiState.iconMap, uiState.usageCounts, normalizedCurrentPath) {
        buildCategoryChildNodes(uiState.allPaths, normalizedCurrentPath).map { node ->
            CategoryListEntry(
                path = node.path,
                name = node.name,
                icon = uiState.iconMap[node.path] ?: defaultCategoryIcon(node.name),
                childCount = uiState.allPaths.count {
                    categoryPathParent(it) == node.path
                },
                itemCount = uiState.usageCounts[node.path] ?: 0,
                hasChildren = node.hasChildren,
            )
        }
    }
    val searchResults = remember(uiState.allPaths, uiState.iconMap, uiState.usageCounts, searchQuery) {
        val keyword = searchQuery.trim()
        if (keyword.isBlank()) {
            emptyList()
        } else {
            uiState.allPaths
                .filter {
                    it.contains(keyword, ignoreCase = true) ||
                        categoryPathDisplayName(it).contains(keyword, ignoreCase = true)
                }
                .map { path ->
                    CategoryListEntry(
                        path = path,
                        name = categoryPathDisplayName(path),
                        icon = uiState.iconMap[path] ?: defaultCategoryIcon(categoryPathDisplayName(path)),
                        childCount = uiState.allPaths.count { categoryPathParent(it) == path },
                        itemCount = uiState.usageCounts[path] ?: 0,
                        hasChildren = uiState.allPaths.any {
                            normalizeCategoryPath(it) != path &&
                                normalizeCategoryPath(it).startsWith("$path / ")
                        },
                    )
                }
        }
    }

    LaunchedEffect(uiState.message) {
        if (!uiState.message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(uiState.message)
            onMessageConsumed()
        }
    }

    // 处理新建分类后的自动滚动
    LaunchedEffect(uiState.allPaths) {
        val targetPath = lastCreatedPath
        if (targetPath != null && uiState.allPaths.contains(targetPath)) {
            // 确保在当前显示的列表中找到它
            val indexInList = if (normalizedSearchQuery.isBlank()) {
                childEntries.indexOfFirst { it.path == targetPath }
            } else {
                searchResults.indexOfFirst { it.path == targetPath }
            }

            if (indexInList != -1) {
                // 延迟一小会儿等待 UI 渲染完成
                delay(100)
                listState.animateScrollToItem(indexInList)
                lastCreatedPath = null
            }
        }
    }

    BackHandler {
        when {
            dialogState != null -> dialogState = null
            normalizedCurrentPath.isBlank() -> onClose()
            else -> currentPath = categoryPathParent(normalizedCurrentPath)
        }
    }

    LiquidGlassTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // 使用统一的液态背景
            LiquidBackground(modifier = Modifier.fillMaxSize())

            Column(modifier = Modifier.fillMaxSize()) {
                CategoryManagementHeader(
                    currentPath = normalizedCurrentPath,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onPathSelected = { currentPath = it },
                    onClose = onClose,
                    onCreateCategory = {
                        dialogState = CategoryDialogState.Create(
                            parentPath = normalizedCurrentPath,
                            shouldScroll = true // 仅右上角新建按钮触发滚动
                        )
                    }
                )

                Box(modifier = Modifier.weight(1f)) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 18.dp,
                                bottom = 24.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                            ),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            if (normalizedSearchQuery.isBlank()) {
                                if (childEntries.isEmpty()) {
                                    item {
                                        CategoryEmptyCard(
                                            title = if (normalizedCurrentPath.isBlank()) "还没有可浏览的分类" else "当前分类下还没有子分类",
                                            description = if (normalizedCurrentPath.isBlank()) {
                                                "可以先创建一级分类，也可以继续使用默认分类。"
                                            } else {
                                                "可以在当前层级继续新建子分类，或者将当前节点直接用于物品分类。"
                                            }
                                        )
                                    }
                                } else {
                                    items(childEntries, key = { it.path }) { entry ->
                                        CategoryEntryCard(
                                            entry = entry,
                                            onOpen = {
                                                if (entry.hasChildren) {
                                                    currentPath = entry.path
                                                }
                                            },
                                            onRename = {
                                                dialogState = CategoryDialogState.Rename(
                                                    path = entry.path,
                                                    initialName = entry.name,
                                                    initialIcon = entry.icon,
                                                )
                                            },
                                            onCreateChild = {
                                                dialogState = CategoryDialogState.Create(parentPath = entry.path)
                                            },
                                            onDelete = {
                                                dialogState = CategoryDialogState.Delete(
                                                    path = entry.path,
                                                    hasChildren = entry.hasChildren,
                                                    itemCount = entry.itemCount,
                                                )
                                            }
                                        )
                                    }
                                }
                            } else {
                                if (searchResults.isEmpty()) {
                                    item {
                                        CategoryEmptyCard(
                                            title = "没有找到匹配分类",
                                            description = "可以切换到当前层级后直接新建，或调整关键词继续搜索。"
                                        )
                                    }
                                } else {
                                    items(searchResults, key = { it.path }) { entry ->
                                        CategorySearchCard(
                                            entry = entry,
                                            onOpen = {
                                                currentPath = entry.path
                                                searchQuery = ""
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    )
            )
        }
    }

    when (val activeDialog = dialogState) {
        is CategoryDialogState.Create -> {
            CategoryEditorDialog(
                title = if (activeDialog.parentPath.isBlank()) "新建一级分类" else "新建子分类",
                initialName = activeDialog.initialName,
                initialIcon = activeDialog.initialIcon,
                parentPath = activeDialog.parentPath,
                onDismiss = { dialogState = null },
                onConfirm = { name, icon ->
                    if (activeDialog.shouldScroll) {
                        lastCreatedPath = if (activeDialog.parentPath.isBlank()) name else "${activeDialog.parentPath} / $name"
                    }
                    onCreateCategory(activeDialog.parentPath, name, icon)
                    dialogState = null
                }
            )
        }

        is CategoryDialogState.Rename -> {
            CategoryEditorDialog(
                title = "编辑分类",
                initialName = activeDialog.initialName,
                initialIcon = activeDialog.initialIcon,
                parentPath = categoryPathParent(activeDialog.path),
                onDismiss = { dialogState = null },
                onConfirm = { name, icon ->
                    onRenameCategory(activeDialog.path, name, icon)
                    dialogState = null
                }
            )
        }

        is CategoryDialogState.Delete -> {
            AlertDialog(
                onDismissRequest = { dialogState = null },
                title = { Text("删除分类") },
                text = {
                    Text(
                        when {
                            activeDialog.hasChildren -> "该分类下还有子分类，当前版本不支持直接删除。"
                            activeDialog.itemCount > 0 -> "删除后，该分类下的 ${activeDialog.itemCount} 个物品会统一移动到“未分类”。"
                            else -> "确认删除“${categoryPathDisplayName(activeDialog.path)}”？"
                        }
                    )
                },
                confirmButton = {
                    TextButton(
                        enabled = !activeDialog.hasChildren,
                        onClick = {
                            onDeleteCategory(activeDialog.path)
                            dialogState = null
                        }
                    ) {
                        Text("删除")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { dialogState = null }) {
                        Text("取消")
                    }
                }
            )
        }

        null -> Unit
    }
}

@Composable
private fun CategoryManagementHeader(
    currentPath: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onPathSelected: (String) -> Unit,
    onClose: () -> Unit,
    onCreateCategory: () -> Unit,
) {
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val headerTopPadding = (statusBarTopPadding - 8.dp).coerceAtLeast(0.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = headerTopPadding,
                start = 16.dp,
                end = 16.dp,
                bottom = 12.dp
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            blurRadius = 28.dp,
            contentPadding = 10.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        modifier = Modifier.size(36.dp),
                        onClick = onClose
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "分类管理",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        modifier = Modifier.size(36.dp),
                        onClick = onCreateCategory
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "新建分类")
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text("输入关键词") },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            blurRadius = 24.dp,
            contentPadding = 14.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CategoryPathChips(
                    currentPath = currentPath,
                    onPathSelected = onPathSelected
                )
                if (currentPath.isNotBlank()) {
                    Text(
                        text = "当前路径：$currentPath",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryPathChips(
    currentPath: String,
    onPathSelected: (String) -> Unit,
) {
    val pathSegments = splitCategoryPath(currentPath)

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = currentPath.isBlank(),
            onClick = { onPathSelected("") },
            label = { Text("全部分类") }
        )
        pathSegments.forEachIndexed { index, segment ->
            val partialPath = pathSegments.take(index + 1).joinToString(" / ")
            FilterChip(
                selected = partialPath == currentPath,
                onClick = { onPathSelected(partialPath) },
                label = { Text(segment) }
            )
        }
    }
}

@Composable
private fun CategoryEntryCard(
    entry: CategoryListEntry,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onCreateChild: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = entry.hasChildren, onClick = onOpen),
        shape = RoundedCornerShape(22.dp),
        contentPadding = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                    Text(
                        text = entry.icon,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (entry.childCount > 0) {
                            "${entry.childCount} 个子分类 · ${entry.itemCount} 个物品"
                        } else {
                            "${entry.itemCount} 个物品直接引用"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (entry.hasChildren) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "进入下一级",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多操作")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("编辑分类") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onRename()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("新建子分类") },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onCreateChild()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("删除分类") },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun CategorySearchCard(
    entry: CategoryListEntry,
    onOpen: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(22.dp),
        contentPadding = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                    Text(
                        text = entry.icon,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Text(entry.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            }
            Text(
                text = entry.path,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${entry.itemCount} 个物品 · ${entry.childCount} 个子分类",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategoryEmptyCard(
    title: String,
    description: String,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        contentPadding = 18.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryEditorDialog(
    title: String,
    initialName: String,
    initialIcon: String,
    parentPath: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String) -> Unit,
) {
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var selectedIcon by rememberSaveable(initialIcon) { mutableStateOf(initialIcon.ifBlank { defaultCategoryIcon(initialName) }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (parentPath.isNotBlank()) {
                    Text(
                        text = "当前层级：$parentPath",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("请输入分类名称") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DEFAULT_CATEGORY_ICONS.forEach { icon ->
                        FilterChip(
                            selected = selectedIcon == icon,
                            onClick = { selectedIcon = icon },
                            label = { Text(icon) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), selectedIcon) }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
