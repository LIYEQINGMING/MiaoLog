package com.example.itemmanagement.ui.components

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.itemmanagement.data.entity.unified.CustomAttributeDefinitionEntity
import com.example.itemmanagement.data.model.PriceActionConfig
import com.example.itemmanagement.data.model.PriceAttributeHelper
import com.example.itemmanagement.data.model.PriceAttributeValue
import com.example.itemmanagement.ui.add.Field
import com.example.itemmanagement.ui.base.BaseItemViewModel
import com.example.itemmanagement.ui.main.LiquidBackground
import com.example.itemmanagement.utils.buildCategoryChildNodes
import com.example.itemmanagement.utils.categoryPathDisplayName
import com.example.itemmanagement.utils.categoryPathParent
import com.example.itemmanagement.utils.defaultCategoryIcon
import com.example.itemmanagement.utils.joinCategoryPath
import com.example.itemmanagement.utils.normalizeCategoryPath
import com.example.itemmanagement.utils.splitCategoryPath
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemFormScaffold(
    headerContent: (@Composable () -> Unit)? = null,
    imageSection: @Composable () -> Unit,
    baseSection: @Composable ColumnScope.() -> Unit,
    supplementSection: @Composable ColumnScope.() -> Unit,
    functionSection: (@Composable ColumnScope.() -> Unit)? = null,
    saveButtonText: String,
    isSaveEnabled: Boolean,
    onSave: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
                bottom = 112.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            headerContent?.let { content ->
                item {
                    content()
                }
            }

            item {
                imageSection()
            }

            item {
                ItemFormSectionCard(title = "基础信息区", content = baseSection)
            }

            item {
                ItemFormSectionCard(title = "补充信息区", content = supplementSection)
            }

            functionSection?.let { content ->
                item {
                    ItemFormSectionCard(title = "功能区", content = content)
                }
            }
        }

        ItemSaveBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
            buttonText = saveButtonText,
            isSaveEnabled = isSaveEnabled,
            onSave = onSave
        )
    }
}

@Composable
fun ItemFormSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        contentPadding = 18.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemImageSection(
    photoUris: List<String>,
    onPickPhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    onRemovePhoto: (Int) -> Unit
) {
    ItemFormSectionCard(title = "图片") {
        if (photoUris.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.16f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📦",
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }
        } else {
            FlowRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                photoUris.forEachIndexed { index, uri ->
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp),
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.45f)
                        ) {
                            IconButton(
                                modifier = Modifier.size(28.dp),
                                onClick = { onRemovePhoto(index) }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "删除图片",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onPickPhoto) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("从相册添加")
            }
            OutlinedButton(onClick = onTakePhoto) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("拍照")
            }
        }
    }
}

@Composable
fun ItemCompactTextRow(
    label: String,
    value: String,
    placeholder: String,
    required: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.width(74.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontWeight = FontWeight.Medium)
            if (required) {
                Text(text = "*", color = MaterialTheme.colorScheme.error)
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(placeholder) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ItemCompactSelectRow(
    label: String,
    value: String,
    placeholder: String,
    options: List<String>,
    onValueSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                modifier = Modifier.width(74.dp),
                fontWeight = FontWeight.Medium
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = options.isNotEmpty()) { expanded = true },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = value.ifBlank { placeholder },
                        color = if (value.isBlank()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ItemQuantityRow(viewModel: BaseItemViewModel) {
    val quantity = itemStringValue(viewModel.getFieldValue("数量")).ifBlank { "1" }
    val unit = itemStringValue(viewModel.getFieldValue("数量_unit")).ifBlank { "个" }
    val units = viewModel.getFieldProperties("数量").unitOptions.orEmpty().ifEmpty { listOf("个") }
    var expanded by remember { mutableStateOf(false) }

    ItemQuantityRow(
        viewModel = viewModel,
        showExcludeFromTotalCountAction = false,
        excludeFromTotalCount = false,
        onExcludeFromTotalCountChange = {}
    )
}

@Composable
fun ItemQuantityRow(
    viewModel: BaseItemViewModel,
    showExcludeFromTotalCountAction: Boolean,
    excludeFromTotalCount: Boolean,
    onExcludeFromTotalCountChange: (Boolean) -> Unit
) {
    val quantity = itemStringValue(viewModel.getFieldValue("数量")).ifBlank { "1" }
    val unit = itemStringValue(viewModel.getFieldValue("数量_unit")).ifBlank { "个" }
    val units = viewModel.getFieldProperties("数量").unitOptions.orEmpty().ifEmpty { listOf("个") }
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "数量", modifier = Modifier.width(74.dp), fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = quantity,
                onValueChange = { viewModel.saveFieldValue("数量", it.filter { ch -> ch.isDigit() || ch == '.' }) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("1") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp)
            )
            Box {
                Surface(
                    modifier = Modifier.clickable { expanded = true },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(unit)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    units.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.saveFieldValue("数量_unit", option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        if (showExcludeFromTotalCountAction) {
            ItemAttributeActionRow(
                title = "不计入物品总数量",
                checked = excludeFromTotalCount,
                onCheckedChange = onExcludeFromTotalCountChange
            )
        }
    }
}

@Composable
fun ItemCategoryPickerRow(
    label: String,
    value: String,
    iconText: String? = null,
    onClick: () -> Unit
) {
    val displayValue = categoryPathDisplayName(value).ifBlank { value }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(74.dp),
            fontWeight = FontWeight.Medium
        )

        Surface(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.12f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = iconText ?: defaultCategoryIcon(displayValue.ifBlank { "其他" }),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    Text(
                        text = displayValue.ifBlank { "选择分类" },
                        color = if (value.isBlank()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemCategoryPickerSheet(
    currentValue: String,
    options: List<String>,
    iconMap: Map<String, String> = emptyMap(),
    onDismiss: () -> Unit,
    onCreateCategory: (String) -> Unit,
    onSelectCategory: (String) -> Unit
) {
    val normalizedCurrentValue = remember(currentValue) { normalizeCategoryPath(currentValue) }
    val normalizedOptions = remember(options, normalizedCurrentValue) {
        (options + listOf(normalizedCurrentValue))
            .map(::normalizeCategoryPath)
            .filter { it.isNotBlank() }
            .distinct()
            .sortedBy { it.length }
    }
    val expandablePaths = remember(normalizedOptions) {
        normalizedOptions.filter { option ->
            normalizedOptions.any { candidate ->
                candidate != option && candidate.startsWith("$option / ")
            }
        }.toSet()
    }

    var searchQuery by rememberSaveable(normalizedCurrentValue) { mutableStateOf("") }
    var browsingPath by rememberSaveable(normalizedCurrentValue) { mutableStateOf(categoryPathParent(normalizedCurrentValue)) }
    var selectedPath by rememberSaveable(normalizedCurrentValue) { mutableStateOf(normalizedCurrentValue) }
    var createParentPath by rememberSaveable(normalizedCurrentValue) { mutableStateOf(browsingPath) }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    val searchResults = remember(searchQuery, normalizedOptions) {
        val keyword = searchQuery.trim()
        if (keyword.isBlank()) {
            emptyList()
        } else {
            normalizedOptions.filter {
                it.contains(keyword, ignoreCase = true) ||
                    categoryPathDisplayName(it).contains(keyword, ignoreCase = true)
            }
        }
    }
    val childNodes = remember(normalizedOptions, browsingPath) {
        buildCategoryChildNodes(normalizedOptions, browsingPath)
    }
    val pathSegments = remember(browsingPath) { splitCategoryPath(browsingPath) }
    val isSearching = searchQuery.trim().isNotBlank()
    val density = LocalDensity.current

    BackHandler {
        when {
            showCreateDialog -> showCreateDialog = false
            browsingPath.isBlank() -> onDismiss()
            else -> browsingPath = categoryPathParent(browsingPath)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LiquidBackground(modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxSize()) {
            ItemCategoryPickerHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                browsingPath = browsingPath,
                pathSegments = pathSegments,
                selectedPath = selectedPath,
                onPathSelected = { browsingPath = it },
                onBack = {
                    if (browsingPath.isBlank()) {
                        onDismiss()
                    } else {
                        browsingPath = categoryPathParent(browsingPath)
                    }
                },
                onCreateCategory = {
                    createParentPath = browsingPath
                    showCreateDialog = true
                }
            )

            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 18.dp,
                        bottom = 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (!isSearching) {
                        if (childNodes.isEmpty()) {
                            item {
                                ItemCategoryPickerEmptyState(
                                    title = if (browsingPath.isBlank()) "还没有可浏览的分类" else "当前分类下还没有子分类",
                                    description = if (browsingPath.isBlank()) {
                                        "可以先创建一级分类。"
                                    } else {
                                        "可以直接选中当前节点，也可以继续新建子分类。"
                                    }
                                )
                            }
                        } else {
                            items(childNodes, key = { it.path }) { node ->
                                ItemCategoryPickerEntryCard(
                                    path = node.path,
                                    name = node.name,
                                    icon = iconMap[node.path] ?: defaultCategoryIcon(node.name.ifBlank { "其他" }),
                                    selected = selectedPath == node.path,
                                    hasChildren = node.hasChildren,
                                    onToggleSelected = {
                                        selectedPath = if (selectedPath == node.path) "" else node.path
                                    },
                                    onOpenChild = { browsingPath = node.path },
                                    onCreateChild = {
                                        createParentPath = node.path
                                        showCreateDialog = true
                                    }
                                )
                            }
                        }
                    } else {
                        if (searchResults.isEmpty()) {
                            item {
                                ItemCategoryPickerEmptyState(
                                    title = "没有找到匹配分类",
                                    description = "调整关键词继续搜索，或先返回当前层级。"
                                )
                            }
                        } else {
                            items(searchResults, key = { it }) { path ->
                                ItemCategoryPickerSearchResultCard(
                                    path = path,
                                    icon = iconMap[path] ?: defaultCategoryIcon(categoryPathDisplayName(path)),
                                    selected = selectedPath == path,
                                    hasChildren = expandablePaths.contains(path),
                                    onToggleSelected = {
                                        selectedPath = if (selectedPath == path) "" else path
                                    },
                                    onOpenChild = {
                                        browsingPath = path
                                        searchQuery = ""
                                    },
                                    onCreateChild = {
                                        createParentPath = path
                                        showCreateDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            ItemCategoryPickerBottomBar(
                enabled = selectedPath.isNotBlank(),
                onCancel = onDismiss,
                onConfirm = { onSelectCategory(selectedPath) }
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(28.dp)
                .pointerInput(browsingPath) {
                    val triggerDistancePx = with(density) { 72.dp.toPx() }
                    var totalDrag = 0f

                    detectHorizontalDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            if (dragAmount > 0f) {
                                totalDrag += dragAmount
                            }
                            change.consume()
                        },
                        onDragEnd = {
                            if (totalDrag >= triggerDistancePx) {
                                if (browsingPath.isBlank()) {
                                    onDismiss()
                                } else {
                                    browsingPath = categoryPathParent(browsingPath)
                                }
                            }
                            totalDrag = 0f
                        },
                        onDragCancel = {
                            totalDrag = 0f
                        }
                    )
                }
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(28.dp)
                .pointerInput(browsingPath) {
                    val triggerDistancePx = with(density) { 72.dp.toPx() }
                    var totalDrag = 0f

                    detectHorizontalDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            if (dragAmount < 0f) {
                                totalDrag += -dragAmount
                            }
                            change.consume()
                        },
                        onDragEnd = {
                            if (totalDrag >= triggerDistancePx) {
                                if (browsingPath.isBlank()) {
                                    onDismiss()
                                } else {
                                    browsingPath = categoryPathParent(browsingPath)
                                }
                            }
                            totalDrag = 0f
                        },
                        onDragCancel = {
                            totalDrag = 0f
                        }
                    )
                }
        )
    }

    if (showCreateDialog) {
        ItemCategoryCreateDialog(
            parentPath = createParentPath,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                val createdPath = joinCategoryPath(createParentPath, name)
                if (createdPath.isNotBlank()) {
                    onCreateCategory(createdPath)
                    browsingPath = createParentPath
                    selectedPath = createdPath
                    showCreateDialog = false
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ItemCategoryPickerHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    browsingPath: String,
    pathSegments: List<String>,
    selectedPath: String,
    onPathSelected: (String) -> Unit,
    onBack: () -> Unit,
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
                        onClick = onBack
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "选择分类",
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
                    placeholder = { Text("搜索分类名称或路径") },
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
                ItemCategoryPickerPathChips(
                    browsingPath = browsingPath,
                    pathSegments = pathSegments,
                    onPathSelected = onPathSelected
                )
                if (selectedPath.isNotBlank()) {
                    Text(
                        text = "当前选中：$selectedPath",
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
private fun ItemCategoryPickerPathChips(
    browsingPath: String,
    pathSegments: List<String>,
    onPathSelected: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = browsingPath.isBlank(),
            onClick = { onPathSelected("") },
            label = { Text("全部分类") }
        )
        pathSegments.forEachIndexed { index, segment ->
            val partialPath = pathSegments.take(index + 1).joinToString(" / ")
            FilterChip(
                selected = partialPath == browsingPath,
                onClick = { onPathSelected(partialPath) },
                label = { Text(segment) }
            )
        }
    }
}

@Composable
private fun ItemCategoryPickerEmptyState(
    title: String,
    description: String,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        contentPadding = 18.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ItemCategoryPickerEntryCard(
    path: String,
    name: String,
    icon: String,
    selected: Boolean,
    hasChildren: Boolean,
    onToggleSelected: () -> Unit,
    onOpenChild: () -> Unit,
    onCreateChild: () -> Unit,
) {
    var menuExpanded by remember(path) { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleSelected),
        shape = RoundedCornerShape(22.dp),
        contentPadding = 0.dp
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            border = if (selected) BorderStroke(1.5.dp, Color(0xFF34C759)) else null,
            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else Color.Transparent
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
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = icon,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (selected) "已选中" else path,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasChildren) {
                        IconButton(
                            modifier = Modifier.size(36.dp),
                            onClick = onOpenChild
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "进入下一级"
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
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
                                text = { Text("新建子分类") },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onCreateChild()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemCategoryPickerSearchResultCard(
    path: String,
    icon: String,
    selected: Boolean,
    hasChildren: Boolean,
    onToggleSelected: () -> Unit,
    onOpenChild: () -> Unit,
    onCreateChild: () -> Unit,
) {
    var menuExpanded by remember(path) { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleSelected),
        shape = RoundedCornerShape(22.dp),
        contentPadding = 0.dp
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            border = if (selected) BorderStroke(1.5.dp, Color(0xFF34C759)) else null,
            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else Color.Transparent
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = icon,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = categoryPathDisplayName(path),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (selected) "已选中" else path,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (hasChildren) {
                            IconButton(
                                modifier = Modifier.size(36.dp),
                                onClick = onOpenChild
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "进入下一级"
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
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
                                    text = { Text("新建子分类") },
                                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        onCreateChild()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemCategoryPickerBottomBar(
    enabled: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 12.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            ),
        shape = RoundedCornerShape(28.dp),
        blurRadius = 28.dp,
        contentPadding = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onCancel
                ) {
                    Text("取消")
                }
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    onClick = onConfirm
                ) {
                    Text("确认分类")
                }
            }
        }
    }
}

@Composable
private fun ItemCategoryCreateDialog(
    parentPath: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (parentPath.isBlank()) "新建分类" else "新建子分类") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    shape = RoundedCornerShape(18.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.trim().isNotEmpty(),
                onClick = { onConfirm(name.trim()) }
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun ItemSupplementFieldItem(
    fieldName: String,
    viewModel: BaseItemViewModel,
    customAttributeDefinitions: List<CustomAttributeDefinitionEntity>,
    locked: Boolean = false,
    onDelete: () -> Unit
) {
    val content: @Composable () -> Unit = {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.10f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                ItemRenderSupplementField(fieldName, viewModel, customAttributeDefinitions)
            }
        }
    }

    if (locked) {
        content()
    } else {
        SwipeRevealDeleteContainer(onDeleteClick = onDelete, content = content)
    }
}

@Composable
fun ItemRenderSupplementField(
    fieldName: String,
    viewModel: BaseItemViewModel,
    customAttributeDefinitions: List<CustomAttributeDefinitionEntity>
) {
    val customDefinition = customAttributeDefinitions.firstOrNull { itemCustomFieldName(it) == fieldName }
    if (customDefinition != null) {
        ItemCustomAttributeField(definition = customDefinition, viewModel = viewModel, showFunctionControl = true)
        return
    }

    when (fieldName) {
        "单价" -> ItemPriceField(
            label = "购入价格",
            value = itemStringValue(viewModel.getFieldValue("单价")),
            currency = itemStringValue(viewModel.getFieldValue("币种"))
                .ifBlank { itemStringValue(viewModel.getFieldValue("单价_unit")).ifBlank { "CNY" } },
            onValueChange = { viewModel.saveFieldValue("单价", it) },
            onCurrencyChange = {
                viewModel.saveFieldValue("币种", it)
                viewModel.saveFieldValue("单价_unit", it)
            },
            actionTitle = "不计入总价",
            actionChecked = itemBooleanValue(viewModel.getFieldValue("不计入总价值")),
            onActionCheckedChange = { viewModel.saveFieldValue("不计入总价值", it) }
        )
        "总价" -> ItemPriceField(
            label = "总价",
            value = itemStringValue(viewModel.getFieldValue("总价")),
            currency = itemStringValue(viewModel.getFieldValue("币种"))
                .ifBlank { itemStringValue(viewModel.getFieldValue("总价_unit")).ifBlank { "CNY" } },
            onValueChange = { viewModel.saveFieldValue("总价", it) },
            onCurrencyChange = {
                viewModel.saveFieldValue("币种", it)
                viewModel.saveFieldValue("总价_unit", it)
            }
        )
        "币种" -> ItemCompactSelectRow(
            label = "币种",
            value = itemStringValue(viewModel.getFieldValue("币种")),
            placeholder = "选择币种",
            options = itemCurrencyOptions(),
            onValueSelected = {
                viewModel.saveFieldValue("币种", it)
                if (itemStringValue(viewModel.getFieldValue("单价_unit")).isBlank()) {
                    viewModel.saveFieldValue("单价_unit", it)
                }
                if (itemStringValue(viewModel.getFieldValue("总价_unit")).isBlank()) {
                    viewModel.saveFieldValue("总价_unit", it)
                }
            }
        )
        "购买渠道" -> ItemCompactSelectRow(
            label = "购买渠道",
            value = itemStringValue(viewModel.getFieldValue("购买渠道")),
            placeholder = "选择渠道",
            options = viewModel.getFieldProperties("购买渠道").options.orEmpty(),
            onValueSelected = { viewModel.saveFieldValue("购买渠道", it) }
        )
        "购买日期", "生产日期", "保质过期时间", "保修到期时间" -> ItemDateField(
            label = fieldName,
            value = itemStringValue(viewModel.getFieldValue(fieldName)),
            onValueChange = { viewModel.saveFieldValue(fieldName, it) }
        )
        "商家名称", "备注", "地点", "序列号" -> ItemTextValueField(
            label = fieldName,
            value = itemStringValue(viewModel.getFieldValue(fieldName)),
            hint = "请输入$fieldName",
            minLines = if (fieldName == "备注") 3 else 1,
            onValueChange = { viewModel.saveFieldValue(fieldName, it) }
        )
        "位置" -> ItemStructuredLocationFields(viewModel)
        "容量" -> ItemNumberWithUnitField(
            label = "容量",
            value = itemStringValue(viewModel.getFieldValue("容量")),
            unit = itemStringValue(viewModel.getFieldValue("容量_unit")),
            units = viewModel.getFieldProperties("容量").unitOptions.orEmpty(),
            hint = "请输入容量",
            onValueChange = { viewModel.saveFieldValue("容量", it) },
            onUnitChange = { viewModel.saveFieldValue("容量_unit", it) }
        )
        "评分" -> ItemRatingField(
            rating = itemStringValue(viewModel.getFieldValue("评分")).toIntOrNull()
                ?: (viewModel.getFieldValue("评分") as? Number)?.toInt()
                ?: 0,
            onRatingChange = { viewModel.saveFieldValue("评分", it.toString()) }
        )
        "保质期", "保修期" -> ItemPeriodField(
            label = fieldName,
            value = itemPeriodValue(viewModel.getFieldValue(fieldName)),
            unit = itemStringValue(viewModel.getFieldValue("${fieldName}_unit")).ifBlank { "月" },
            units = viewModel.getFieldProperties(fieldName).periodUnits.orEmpty(),
            onValueChange = { viewModel.saveFieldValue(fieldName, it) },
            onUnitChange = { viewModel.saveFieldValue("${fieldName}_unit", it) }
        )
        "订阅制" -> ItemFunctionSwitchRow(
            title = "订阅制",
            checked = itemBooleanValue(viewModel.getFieldValue("订阅制")),
            onCheckedChange = { viewModel.saveFieldValue("订阅制", it) }
        )
        "自动续费" -> ItemFunctionSwitchRow(
            title = "自动续费",
            checked = itemBooleanValue(viewModel.getFieldValue("自动续费")),
            onCheckedChange = { viewModel.saveFieldValue("自动续费", it) }
        )
        "扣费周期" -> ItemSegmentedChoiceField(
            label = "扣费周期",
            value = itemStringValue(viewModel.getFieldValue("扣费周期")).ifBlank { "MONTH" },
            options = listOf("DAY", "MONTH", "QUARTER", "YEAR"),
            optionLabels = mapOf("DAY" to "日", "MONTH" to "月", "QUARTER" to "季度", "YEAR" to "年"),
            onValueChange = { viewModel.saveFieldValue("扣费周期", it) }
        )
        "开封状态" -> ItemSegmentedChoiceField(
            label = "开封状态",
            value = itemStringValue(viewModel.getFieldValue("开封状态")),
            options = listOf("未开封", "已开封"),
            onValueChange = { viewModel.saveFieldValue("开封状态", it) }
        )
        "季节" -> ItemSeasonField(viewModel)
    }
}

@Composable
fun ItemFunctionFieldItem(
    fieldName: String,
    viewModel: BaseItemViewModel,
    customDefinitions: List<CustomAttributeDefinitionEntity>,
    onDelete: () -> Unit
) {
    val customDefinition = customDefinitions.firstOrNull { itemCustomIncludeFieldName(it) == fieldName }
    if (customDefinition != null) {
        ItemFunctionSwitchRow(
            title = "${customDefinition.name}计入总价值",
            checked = when (val value = viewModel.getFieldValue(fieldName)) {
                is Boolean -> value
                else -> true
            },
            onCheckedChange = { viewModel.saveFieldValue(fieldName, it) }
        )
        return
    }

    when (fieldName) {
        "订阅制" -> SwipeRevealDeleteContainer(onDeleteClick = onDelete) {
            ItemFunctionSwitchRow(
                title = "订阅制",
                checked = itemBooleanValue(viewModel.getFieldValue("订阅制")),
                onCheckedChange = { viewModel.saveFieldValue("订阅制", it) }
            )
        }
        "自动续费" -> SwipeRevealDeleteContainer(onDeleteClick = onDelete) {
            ItemFunctionSwitchRow(
                title = "自动续费",
                checked = itemBooleanValue(viewModel.getFieldValue("自动续费")),
                onCheckedChange = { viewModel.saveFieldValue("自动续费", it) }
            )
        }
        "扣费周期" -> SwipeRevealDeleteContainer(onDeleteClick = onDelete) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.10f)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("扣费周期", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    ItemSegmentedChoiceField(
                        label = "",
                        value = itemStringValue(viewModel.getFieldValue("扣费周期")).ifBlank { "MONTH" },
                        options = listOf("DAY", "MONTH", "QUARTER", "YEAR"),
                        optionLabels = mapOf("DAY" to "日", "MONTH" to "月", "QUARTER" to "季度", "YEAR" to "年"),
                        onValueChange = { viewModel.saveFieldValue("扣费周期", it) }
                    )
                }
            }
        }
    }
}

@Composable
fun ItemFunctionSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.10f)
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun ItemAttributeActionRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun ItemPriceField(
    label: String,
    value: String,
    currency: String,
    onValueChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    actionTitle: String? = null,
    actionChecked: Boolean = false,
    onActionCheckedChange: ((Boolean) -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ItemNumberWithUnitField(
            label = label,
            value = value,
            unit = currency,
            units = itemCurrencyOptions(),
            hint = "请输入金额",
            onValueChange = onValueChange,
            onUnitChange = onCurrencyChange
        )
        if (!actionTitle.isNullOrBlank() && onActionCheckedChange != null) {
            ItemAttributeActionRow(
                title = actionTitle,
                checked = actionChecked,
                onCheckedChange = onActionCheckedChange
            )
        }
    }
}

@Composable
fun ItemPriceAttributeBlock(
    definition: CustomAttributeDefinitionEntity,
    value: PriceAttributeValue,
    onValueChange: (PriceAttributeValue) -> Unit
) {
    val supportedActions = remember(definition) {
        PriceAttributeHelper.parseSupportedActions(definition.supportedActions)
    }
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = definition.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        ItemNumberWithUnitField(
            label = "金额",
            value = value.amount?.toString() ?: "",
            unit = value.currency ?: definition.defaultCurrency ?: "CNY",
            units = itemCurrencyOptions(),
            hint = "请输入金额",
            onValueChange = { 
                onValueChange(value.copy(amount = it.toDoubleOrNull())) 
            },
            onUnitChange = { 
                onValueChange(value.copy(currency = it)) 
            }
        )
        
        val context = LocalContext.current
        val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
        val dateText = value.date?.let { dateFormat.format(Date(it)) } ?: ""
        
        ItemFieldLabel(label = "日期")
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val calendar = Calendar.getInstance().apply {
                        value.date?.let { timeInMillis = it }
                    }
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val cal = Calendar.getInstance()
                            cal.set(year, month, dayOfMonth)
                            onValueChange(value.copy(date = cal.timeInMillis))
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.12f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (dateText.isBlank()) "点击选择日期" else dateText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (dateText.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
            }
        }
        
        supportedActions.forEach { action ->
            when (action) {
                CustomAttributeDefinitionEntity.ACTION_INCLUDE_IN_TOTAL -> {
                    ItemAttributeActionRow(
                        title = "计入总价",
                        checked = value.actions.includeInTotal ?: false,
                        onCheckedChange = { 
                            onValueChange(
                                value.copy(actions = value.actions.copy(includeInTotal = it))
                            ) 
                        }
                    )
                }
                CustomAttributeDefinitionEntity.ACTION_INCLUDE_IN_AVERAGE -> {
                    ItemAttributeActionRow(
                        title = "参与均价计算",
                        checked = value.actions.includeInAverage ?: false,
                        onCheckedChange = { 
                            onValueChange(
                                value.copy(actions = value.actions.copy(includeInAverage = it))
                            ) 
                        }
                    )
                }
                CustomAttributeDefinitionEntity.ACTION_INCLUDE_IN_DAILY_VALUE -> {
                    ItemAttributeActionRow(
                        title = "参与日均价值计算",
                        checked = value.actions.includeInDailyValue ?: false,
                        onCheckedChange = { 
                            onValueChange(
                                value.copy(actions = value.actions.copy(includeInDailyValue = it))
                            ) 
                        }
                    )
                }
            }
        }
        
        if (definition.supportsRecurrence == true) {
            ItemFunctionSwitchRow(
                title = "周期性价格",
                checked = value.recurrence.isRecurring ?: false,
                onCheckedChange = { 
                    onValueChange(
                        value.copy(recurrence = value.recurrence.copy(isRecurring = it))
                    ) 
                }
            )
            
            if (value.recurrence.isRecurring == true) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ItemSegmentedChoiceField(
                        label = "周期类型",
                        value = value.recurrence.recurrenceType ?: "MONTH",
                        options = listOf("DAY", "WEEK", "MONTH", "QUARTER", "YEAR"),
                        optionLabels = mapOf(
                            "DAY" to "日",
                            "WEEK" to "周",
                            "MONTH" to "月",
                            "QUARTER" to "季度",
                            "YEAR" to "年"
                        ),
                        onValueChange = { 
                            onValueChange(
                                value.copy(recurrence = value.recurrence.copy(recurrenceType = it))
                            ) 
                        }
                    )
                    
                    ItemFunctionSwitchRow(
                        title = "自动续费",
                        checked = value.recurrence.autoRenew ?: false,
                        onCheckedChange = { 
                            onValueChange(
                                value.copy(recurrence = value.recurrence.copy(autoRenew = it))
                            ) 
                        }
                    )
                    
                    if (value.recurrence.autoRenew == true) {
                        val nextChargeDateText = value.recurrence.nextChargeDate?.let { 
                            dateFormat.format(Date(it)) 
                        } ?: ""
                        
                        ItemFieldLabel(label = "下次扣费日")
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val calendar = Calendar.getInstance().apply {
                                        value.recurrence.nextChargeDate?.let { timeInMillis = it }
                                    }
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            val cal = Calendar.getInstance()
                                            cal.set(year, month, dayOfMonth)
                                            onValueChange(
                                                value.copy(
                                                    recurrence = value.recurrence.copy(
                                                        nextChargeDate = cal.timeInMillis
                                                    )
                                                )
                                            )
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (nextChargeDateText.isBlank()) "点击选择日期" else nextChargeDateText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (nextChargeDateText.isBlank()) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun itemCurrencyOptions(): List<String> {
    return listOf("CNY", "USD", "EUR", "JPY", "GBP", "HKD", "TWD")
}

@Composable
fun ItemTextValueField(
    label: String,
    value: String,
    hint: String,
    required: Boolean = false,
    minLines: Int = 1,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ItemFieldLabel(label = label, required = required)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = minLines,
            placeholder = { Text(hint) },
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
fun ItemNumberWithUnitField(
    label: String,
    value: String,
    unit: String,
    units: List<String>,
    hint: String,
    onValueChange: (String) -> Unit,
    onUnitChange: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ItemFieldLabel(label = label)
            OutlinedTextField(
                value = value,
                onValueChange = { onValueChange(it.filter { ch -> ch.isDigit() || ch == '.' }) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                placeholder = { Text(hint) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp)
            )
        }
        Column(
            modifier = Modifier.width(120.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ItemFieldLabel(label = "单位")
            ItemCompactSelectRow(
                label = "",
                value = unit,
                placeholder = "单位",
                options = if (units.isEmpty()) listOf(unit.ifBlank { "-" }) else units,
                onValueSelected = onUnitChange
            )
        }
    }
}

@Composable
fun ItemDateField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ItemFieldLabel(label = label)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val calendar = Calendar.getInstance().apply {
                        itemParseDate(value)?.let { time = it }
                    }
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            onValueChange(
                                String.format(
                                    Locale.getDefault(),
                                    "%04d-%02d-%02d",
                                    year,
                                    month + 1,
                                    dayOfMonth
                                )
                            )
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.12f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (value.isBlank()) "点击选择日期" else value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (value.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
            }
        }
    }
}

@Composable
fun ItemPeriodField(
    label: String,
    value: String,
    unit: String,
    units: List<String>,
    onValueChange: (String) -> Unit,
    onUnitChange: (String) -> Unit
) {
    ItemNumberWithUnitField(
        label = label,
        value = value,
        unit = unit,
        units = if (units.isEmpty()) listOf("月") else units,
        hint = "例如 12",
        onValueChange = onValueChange,
        onUnitChange = onUnitChange
    )
}

@Composable
fun ItemRatingField(
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ItemFieldLabel(label = "评分")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(5) { index ->
                val selected = index < rating
                Surface(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onRatingChange(index + 1) },
                    shape = CircleShape,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.18f)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (selected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSegmentedChoiceField(
    label: String,
    value: String,
    options: List<String>,
    optionLabels: Map<String, String> = emptyMap(),
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (label.isNotBlank()) {
            ItemFieldLabel(label = label)
        }
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = value == option,
                    onClick = { onValueChange(option) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    label = {
                        Text(optionLabels[option] ?: option, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                )
            }
        }
    }
}

@Composable
fun ItemFieldLabel(label: String, required: Boolean = false) {
    if (label.isBlank()) return
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
        if (required) {
            Text(
                text = "*",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun ItemStructuredLocationFields(viewModel: BaseItemViewModel) {
    val area = itemStringValue(viewModel.getFieldValue("位置_area"))
    val container = itemStringValue(viewModel.getFieldValue("位置_container"))
    val sublocation = itemStringValue(viewModel.getFieldValue("位置_sublocation"))

    ItemTextValueField(
        label = "区域",
        value = area,
        hint = "例如书房 / 客厅 / 数码柜",
        onValueChange = {
            viewModel.saveFieldValue("位置_area", it)
            syncItemStructuredLocation(viewModel, it, container, sublocation)
        }
    )
    ItemTextValueField(
        label = "容器",
        value = container,
        hint = "例如抽屉 / 收纳箱 / 展示柜",
        onValueChange = {
            viewModel.saveFieldValue("位置_container", it)
            syncItemStructuredLocation(viewModel, area, it, sublocation)
        }
    )
    ItemTextValueField(
        label = "具体位置",
        value = sublocation,
        hint = "例如左侧第二层 / 相机层",
        onValueChange = {
            viewModel.saveFieldValue("位置_sublocation", it)
            syncItemStructuredLocation(viewModel, area, container, it)
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemSeasonField(viewModel: BaseItemViewModel) {
    val selected = when (val seasonValue = viewModel.getFieldValue("季节")) {
        is Set<*> -> seasonValue.filterIsInstance<String>().toSet()
        is String -> seasonValue.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
        else -> emptySet()
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("春", "夏", "秋", "冬", "全年").forEach { season ->
            FilterChip(
                selected = selected.contains(season),
                onClick = {
                    val next = if (selected.contains(season)) selected - season else selected + season
                    viewModel.saveFieldValue("季节", next)
                },
                label = { Text(season) },
                leadingIcon = if (selected.contains(season)) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else {
                    null
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemTagEditorField(
    label: String,
    selectedTags: Set<String>,
    suggestions: List<String>,
    onCreateTag: (String) -> Unit,
    onTagsChange: (Set<String>) -> Unit
) {
    var input by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val filteredSuggestions = remember(input, suggestions, selectedTags) {
        val keyword = input.trim()
        suggestions
            .filter { it.isNotBlank() && !selectedTags.contains(it) }
            .filter { keyword.isBlank() || it.contains(keyword, ignoreCase = true) }
            .take(6)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ItemFieldLabel(label = label)
        Box {
            OutlinedTextField(
                value = input,
                onValueChange = {
                    input = it
                    expanded = true
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索、输入或新建标签") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val tag = input.trim()
                            if (tag.isNotBlank()) {
                                onCreateTag(tag)
                                onTagsChange(selectedTags + tag)
                                input = ""
                                expanded = false
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "添加标签")
                    }
                },
                shape = RoundedCornerShape(18.dp)
            )

            DropdownMenu(
                expanded = expanded && (filteredSuggestions.isNotEmpty() || input.trim().isNotBlank()),
                onDismissRequest = { expanded = false }
            ) {
                filteredSuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            onTagsChange(selectedTags + suggestion)
                            input = ""
                            expanded = false
                        }
                    )
                }
                val newTag = input.trim()
                if (newTag.isNotBlank() && !suggestions.contains(newTag) && !selectedTags.contains(newTag)) {
                    DropdownMenuItem(
                        text = { Text("新建标签 \"$newTag\"") },
                        onClick = {
                            onCreateTag(newTag)
                            onTagsChange(selectedTags + newTag)
                            input = ""
                            expanded = false
                        }
                    )
                }
            }
        }

        if (selectedTags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedTags.forEach { tag ->
                    AssistChip(
                        onClick = { onTagsChange(selectedTags - tag) },
                        label = { Text(tag) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "删除标签",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ItemCustomAttributeField(
    definition: CustomAttributeDefinitionEntity,
    viewModel: BaseItemViewModel,
    showFunctionControl: Boolean
) {
    val fieldKey = itemCustomFieldName(definition)
    val includeKey = itemCustomIncludeFieldName(definition)

    when (definition.type) {
        CustomAttributeDefinitionEntity.TYPE_TEXT -> ItemTextValueField(
            label = definition.name,
            value = itemStringValue(viewModel.getFieldValue(fieldKey)),
            hint = "输入${definition.name}",
            onValueChange = { viewModel.saveFieldValue(fieldKey, it) }
        )
        CustomAttributeDefinitionEntity.TYPE_DATE -> ItemDateField(
            label = definition.name,
            value = itemStringValue(viewModel.getFieldValue(fieldKey)),
            onValueChange = { viewModel.saveFieldValue(fieldKey, it) }
        )
        CustomAttributeDefinitionEntity.TYPE_BOOLEAN -> ItemFunctionSwitchRow(
            title = definition.name,
            checked = itemBooleanValue(viewModel.getFieldValue(fieldKey)),
            onCheckedChange = { viewModel.saveFieldValue(fieldKey, it) }
        )
        CustomAttributeDefinitionEntity.TYPE_PRICE -> {
            ItemNumberWithUnitField(
                label = definition.name,
                value = itemStringValue(viewModel.getFieldValue(fieldKey)),
                unit = definition.unit.orEmpty().ifBlank {
                    itemStringValue(viewModel.getFieldValue("币种")).ifBlank { "CNY" }
                },
                units = itemCurrencyOptions(),
                hint = "输入金额",
                onValueChange = { viewModel.saveFieldValue(fieldKey, it) },
                onUnitChange = { }
            )
            if (showFunctionControl) {
                val includeInTotal = when (val value = viewModel.getFieldValue(includeKey)) {
                    is Boolean -> value
                    else -> true
                }
                ItemAttributeActionRow(
                    title = "不计入总价",
                    checked = !includeInTotal,
                    onCheckedChange = { viewModel.saveFieldValue(includeKey, !it) }
                )
            }
        }
        else -> ItemNumberWithUnitField(
            label = definition.name,
            value = itemStringValue(viewModel.getFieldValue(fieldKey)),
            unit = definition.unit.orEmpty().ifBlank { "-" },
            units = listOf(definition.unit.orEmpty().ifBlank { "-" }),
            hint = "输入数值",
            onValueChange = { viewModel.saveFieldValue(fieldKey, it) },
            onUnitChange = { }
        )
    }
}

@Composable
fun ItemSectionAddButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        OutlinedButton(
            enabled = enabled,
            onClick = onClick,
            shape = CircleShape,
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Text("+", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFieldPickerSheet(
    title: String,
    availableFields: List<String>,
    customAttributeDefinitions: List<CustomAttributeDefinitionEntity>,
    onDismiss: () -> Unit,
    onFieldSelected: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (availableFields.isEmpty()) {
                    item {
                        Text(
                            text = "当前没有可补充的字段",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(availableFields) { field ->
                        val definition = customAttributeDefinitions.firstOrNull {
                            itemCustomFieldName(it) == field || itemCustomIncludeFieldName(it) == field
                        }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onFieldSelected(field) },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.10f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = itemDisplayNameForField(field, definition),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (definition != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "类型：${definition.type}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
        }
    }
}

@Composable
fun ItemSaveBar(
    modifier: Modifier = Modifier,
    buttonText: String,
    isSaveEnabled: Boolean,
    onSave: () -> Unit
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        blurRadius = 28.dp,
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                enabled = isSaveEnabled,
                onClick = onSave
            ) {
                Text(buttonText)
            }
        }
    }
}

fun itemBuildSupplementFields(
    selectedFieldNames: Set<String>,
    defaultFields: List<String>,
    optionalFields: List<String>,
    customAttributeDefinitions: List<CustomAttributeDefinitionEntity>
): List<String> {
    return buildList {
        addAll(defaultFields)
        addAll(optionalFields.filter { selectedFieldNames.contains(it) })
        addAll(customAttributeDefinitions.map { itemCustomFieldName(it) }.filter { selectedFieldNames.contains(it) })
    }
}

fun itemBuildFunctionFields(
    selectedFieldNames: Set<String>,
    defaultFields: List<String>,
    optionalFields: List<String>,
    selectedCustomDefinitions: List<CustomAttributeDefinitionEntity>
): List<String> {
    return buildList {
        addAll(defaultFields)
        addAll(optionalFields.filter { selectedFieldNames.contains(it) })
        addAll(
            selectedCustomDefinitions
                .filter { it.type == CustomAttributeDefinitionEntity.TYPE_PRICE }
                .map { itemCustomIncludeFieldName(it) }
        )
    }
}

fun itemAddFieldToSection(
    viewModel: BaseItemViewModel,
    fieldName: String,
    group: String
) {
    val current = viewModel.selectedFields.value?.toMutableSet() ?: mutableSetOf()
    if (current.any { it.name == fieldName }) return
    current.add(
        Field(
            group = group,
            name = fieldName,
            isSelected = true
        )
    )
    viewModel.setSelectedFields(current)
}

fun itemRemoveFieldFromCurrentForm(
    viewModel: BaseItemViewModel,
    fieldName: String
) {
    val current = viewModel.selectedFields.value?.filterNot { it.name == fieldName }?.toSet().orEmpty()
    viewModel.setSelectedFields(current)
    when {
        fieldName.startsWith("custom_") -> {
            viewModel.clearFieldValue(fieldName)
            val parts = fieldName.split("_", limit = 3)
            val defId = parts.getOrNull(1)
            if (!defId.isNullOrBlank()) {
                viewModel.clearFieldValue("custom_include_$defId")
            }
        }
        fieldName == "位置" -> {
            viewModel.clearFieldValue("位置")
            viewModel.clearFieldValue("位置_area")
            viewModel.clearFieldValue("位置_container")
            viewModel.clearFieldValue("位置_sublocation")
        }
        fieldName == "地点" -> {
            viewModel.clearFieldValue("地点")
            viewModel.clearFieldValue("地点_纬度")
            viewModel.clearFieldValue("地点_经度")
        }
        fieldName == "保质期" || fieldName == "保修期" -> {
            viewModel.clearFieldValue(fieldName)
            viewModel.clearFieldValue("${fieldName}_unit")
        }
        fieldName == "单价" || fieldName == "总价" -> {
            viewModel.clearFieldValue(fieldName)
            viewModel.clearFieldValue("${fieldName}_unit")
        }
        else -> viewModel.clearFieldValue(fieldName)
    }
}

fun itemDisplayNameForField(
    fieldName: String,
    definition: CustomAttributeDefinitionEntity?
): String {
    return when {
        definition != null && fieldName == itemCustomIncludeFieldName(definition) -> "${definition.name}计入总价值"
        definition != null -> definition.name
        fieldName == "单价" -> "购入价格"
        fieldName == "不计入总数量" -> "不计入物品总数量"
        else -> fieldName
    }
}

fun itemCustomFieldName(definition: CustomAttributeDefinitionEntity): String {
    return "custom_${definition.id}_${definition.name}"
}

fun itemCustomIncludeFieldName(definition: CustomAttributeDefinitionEntity): String {
    return "custom_include_${definition.id}"
}

fun itemStringValue(value: Any?): String {
    return when (value) {
        null -> ""
        is String -> value
        is Pair<*, *> -> value.first?.toString().orEmpty()
        else -> value.toString()
    }
}

fun itemPeriodValue(value: Any?): String {
    return when (value) {
        is Pair<*, *> -> value.first?.toString().orEmpty()
        is String -> value
        else -> ""
    }
}

fun itemBooleanValue(value: Any?): Boolean {
    return when (value) {
        is Boolean -> value
        is Number -> value.toInt() != 0
        is String -> value.equals("true", ignoreCase = true) || value == "1"
        else -> false
    }
}

private fun itemCategoryEmoji(category: String): String {
    return when (category) {
        "食品" -> "🍎"
        "药品" -> "💊"
        "日用品" -> "🧴"
        "电子产品" -> "💻"
        "衣物" -> "👕"
        "文具" -> "📝"
        "其他" -> "📦"
        else -> "📦"
    }
}

private fun syncItemStructuredLocation(
    viewModel: BaseItemViewModel,
    area: String,
    container: String,
    sublocation: String
) {
    val fullLocation = listOf(area, container, sublocation)
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .joinToString("-")
    viewModel.saveFieldValue("位置", fullLocation.ifBlank { null })
}

private fun itemParseDate(value: String): Date? {
    return runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(value)
    }.getOrNull()
}
