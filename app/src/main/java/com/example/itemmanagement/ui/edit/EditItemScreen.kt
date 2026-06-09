package com.example.itemmanagement.ui.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.itemmanagement.data.entity.unified.CustomAttributeDefinitionEntity
import com.example.itemmanagement.ui.components.GlassCard
import com.example.itemmanagement.ui.components.ItemCategoryPickerRow
import com.example.itemmanagement.ui.components.ItemCompactSelectRow
import com.example.itemmanagement.ui.components.ItemCompactTextRow
import com.example.itemmanagement.ui.components.ItemFieldPickerSheet
import com.example.itemmanagement.ui.components.ItemFormScaffold
import com.example.itemmanagement.ui.components.ItemImageSection
import com.example.itemmanagement.ui.components.ItemQuantityRow
import com.example.itemmanagement.ui.components.ItemSectionAddButton
import com.example.itemmanagement.ui.components.ItemSupplementFieldItem
import com.example.itemmanagement.ui.components.ItemTagEditorField
import com.example.itemmanagement.ui.components.SwipeRevealDeleteContainer
import com.example.itemmanagement.ui.components.itemAddFieldToSection
import com.example.itemmanagement.ui.components.itemBooleanValue
import com.example.itemmanagement.ui.components.itemBuildSupplementFields
import com.example.itemmanagement.ui.components.itemCustomFieldName
import com.example.itemmanagement.ui.components.itemRemoveFieldFromCurrentForm
import com.example.itemmanagement.ui.components.itemStringValue

private val EditBaseDefaultFields = listOf("名称", "分类", "数量")
private val EditBaseOptionalFields = listOf("品牌", "规格")
private val EditSupplementDefaultFields = listOf("状态", "标签")
private val EditSupplementOptionalFields = listOf(
    "单价",
    "总价",
    "币种",
    "购买日期",
    "购买渠道",
    "商家名称",
    "备注",
    "位置",
    "地点",
    "序列号",
    "容量",
    "评分",
    "生产日期",
    "保质期",
    "保质过期时间",
    "保修期",
    "保修到期时间",
    "订阅制",
    "自动续费",
    "扣费周期",
    "开封状态",
    "季节"
)
private val EditStatusOptions = listOf("服役中", "未购买", "待补款", "已退役", "已过期")

private enum class EditFieldSection {
    BASE,
    SUPPLEMENT
}

@Composable
fun EditItemScreen(
    viewModel: EditItemViewModel,
    customAttributeDefinitions: List<CustomAttributeDefinitionEntity>,
    onPickPhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    onRemovePhoto: (Int) -> Unit,
    onShowCategoryPicker: () -> Unit,
    onNavigateBack: () -> Unit,
    onRequestDelete: () -> Unit,
    onSave: () -> Unit
) {
    val fieldVersion by viewModel.fieldVersion.observeAsState(0)
    val selectedFields by viewModel.selectedFields.observeAsState(emptySet())
    val photoUris by viewModel.photoUris.observeAsState(emptyList())
    val selectedTags by viewModel.selectedTags.observeAsState(emptyMap())
    val canUndo by viewModel.canUndo.observeAsState(false)
    val hasUnsavedChanges by viewModel.hasUnsavedChanges.observeAsState(false)

    val name = itemStringValue(viewModel.getFieldValue("名称"))
    val selectedFieldNames = remember(selectedFields) { selectedFields.map { it.name }.toSet() }

    var activeSheet by rememberSaveable { mutableStateOf<EditFieldSection?>(null) }
    var pendingDeleteField by rememberSaveable { mutableStateOf<String?>(null) }

    val baseFields = remember(selectedFieldNames, fieldVersion) {
        EditBaseDefaultFields + EditBaseOptionalFields.filter { selectedFieldNames.contains(it) }
    }
    val supplementFields = remember(selectedFieldNames, customAttributeDefinitions, fieldVersion) {
        buildEditSupplementFields(selectedFieldNames, customAttributeDefinitions)
    }
    val tagSuggestions = remember(selectedTags, fieldVersion) {
        val defaults = viewModel.getFieldProperties("标签").options.orEmpty()
        (defaults + viewModel.getCustomTags("标签") + selectedTags["标签"].orEmpty()).distinct()
    }
    val availableBaseFields = remember(baseFields) {
        EditBaseOptionalFields.filterNot { baseFields.contains(it) }
    }
    val availableSupplementFields = remember(supplementFields, customAttributeDefinitions) {
        val customFields = customAttributeDefinitions.map { itemCustomFieldName(it) }
        (EditSupplementOptionalFields + customFields)
            .distinct()
            .filterNot { supplementFields.contains(it) || it in EditSupplementDefaultFields }
    }

    BackHandler(onBack = onNavigateBack)

    ItemFormScaffold(
        headerContent = {
            EditHeaderCard(
                canUndo = canUndo,
                hasUnsavedChanges = hasUnsavedChanges,
                onBackClick = onNavigateBack,
                onUndoClick = { viewModel.undoLastChange() },
                onDeleteClick = onRequestDelete
            )
        },
        imageSection = {
            ItemImageSection(
                photoUris = photoUris.map { it.toString() },
                onPickPhoto = onPickPhoto,
                onTakePhoto = onTakePhoto,
                onRemovePhoto = onRemovePhoto
            )
        },
        baseSection = {
            EditBaseFieldsContent(
                viewModel = viewModel,
                visibleFields = baseFields,
                onDeleteField = { pendingDeleteField = it },
                onShowCategoryPicker = onShowCategoryPicker
            )
            ItemSectionAddButton(
                enabled = availableBaseFields.isNotEmpty(),
                onClick = { activeSheet = EditFieldSection.BASE }
            )
        },
        supplementSection = {
            ItemCompactSelectRow(
                label = "状态",
                value = itemStringValue(viewModel.getFieldValue("状态")),
                placeholder = "选择状态",
                options = EditStatusOptions,
                onValueSelected = { viewModel.saveFieldValue("状态", it) }
            )

            ItemTagEditorField(
                label = "标签",
                selectedTags = selectedTags["标签"].orEmpty(),
                suggestions = tagSuggestions,
                onCreateTag = { tag -> viewModel.addCustomTag("标签", tag) },
                onTagsChange = {
                    viewModel.updateSelectedTags("标签", it)
                    viewModel.saveFieldValue("标签", it)
                }
            )

            supplementFields
                .filterNot { it in EditSupplementDefaultFields }
                .forEach { fieldName ->
                    ItemSupplementFieldItem(
                        fieldName = fieldName,
                        viewModel = viewModel,
                        customAttributeDefinitions = customAttributeDefinitions,
                        onDelete = { pendingDeleteField = fieldName }
                    )
                }

            ItemSectionAddButton(
                enabled = availableSupplementFields.isNotEmpty(),
                onClick = { activeSheet = EditFieldSection.SUPPLEMENT }
            )
        },
        saveButtonText = "保存修改",
        isSaveEnabled = name.isNotBlank(),
        onSave = onSave
    )

    if (activeSheet != null) {
        ItemFieldPickerSheet(
            title = editSectionTitle(activeSheet!!),
            availableFields = when (activeSheet) {
                EditFieldSection.BASE -> availableBaseFields
                EditFieldSection.SUPPLEMENT -> availableSupplementFields
                null -> emptyList()
            },
            customAttributeDefinitions = customAttributeDefinitions,
            onDismiss = { activeSheet = null },
            onFieldSelected = { field ->
                itemAddFieldToSection(viewModel, field, editSectionGroup(activeSheet!!))
                activeSheet = null
            }
        )
    }

    pendingDeleteField?.let { fieldName ->
        AlertDialog(
            onDismissRequest = { pendingDeleteField = null },
            title = { Text("移除属性") },
            text = { Text("将从当前编辑表单中移除“$fieldName”。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemRemoveFieldFromCurrentForm(viewModel, fieldName)
                        pendingDeleteField = null
                    }
                ) {
                    Text("移除")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteField = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun EditHeaderCard(
    canUndo: Boolean,
    hasUnsavedChanges: Boolean,
    onBackClick: () -> Unit,
    onUndoClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        contentPadding = 18.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Column {
                    Text(
                        text = "编辑物品",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (hasUnsavedChanges) "有未保存修改" else "当前修改已同步到表单",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(enabled = canUndo, onClick = onUndoClick) {
                    Icon(Icons.Default.Undo, contentDescription = "撤回")
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
                            text = { Text("删除物品") },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditBaseFieldsContent(
    viewModel: EditItemViewModel,
    visibleFields: List<String>,
    onDeleteField: (String) -> Unit,
    onShowCategoryPicker: () -> Unit
) {
    val categoryPath = itemStringValue(viewModel.getFieldValue("分类"))
    val categoryIcon = remember(categoryPath) { viewModel.getCategoryIcon(categoryPath) }

    ItemCompactTextRow(
        label = "名称",
        value = itemStringValue(viewModel.getFieldValue("名称")),
        placeholder = "请输入名称",
        required = true,
        onValueChange = { viewModel.saveFieldValue("名称", it) }
    )

    ItemCategoryPickerRow(
        label = "分类",
        value = categoryPath,
        iconText = categoryIcon,
        onClick = onShowCategoryPicker
    )

    ItemQuantityRow(
        viewModel = viewModel,
        showExcludeFromTotalCountAction = true,
        excludeFromTotalCount = itemBooleanValue(viewModel.getFieldValue("不计入总数量")),
        onExcludeFromTotalCountChange = { viewModel.saveFieldValue("不计入总数量", it) }
    )

    visibleFields
        .filterNot { it in EditBaseDefaultFields }
        .forEach { fieldName ->
            EditBaseOptionalFieldItem(onDelete = { onDeleteField(fieldName) }) {
                when (fieldName) {
                    else -> {
                        ItemCompactTextRow(
                            label = fieldName,
                            value = itemStringValue(viewModel.getFieldValue(fieldName)),
                            placeholder = "请输入$fieldName",
                            onValueChange = { viewModel.saveFieldValue(fieldName, it) }
                        )
                    }
                }
            }
        }

}

private fun buildEditSupplementFields(
    selectedFieldNames: Set<String>,
    customAttributeDefinitions: List<CustomAttributeDefinitionEntity>
): List<String> {
    return itemBuildSupplementFields(
        selectedFieldNames = selectedFieldNames,
        defaultFields = EditSupplementDefaultFields,
        optionalFields = EditSupplementOptionalFields,
        customAttributeDefinitions = customAttributeDefinitions
    )
}

@Composable
private fun EditBaseOptionalFieldItem(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    SwipeRevealDeleteContainer(onDeleteClick = onDelete, content = content)
}

private fun editSectionTitle(section: EditFieldSection): String {
    return when (section) {
        EditFieldSection.BASE -> "补充基础属性"
        EditFieldSection.SUPPLEMENT -> "补充其他属性"
    }
}

private fun editSectionGroup(section: EditFieldSection): String {
    return when (section) {
        EditFieldSection.BASE -> "基础信息"
        EditFieldSection.SUPPLEMENT -> "补充信息"
    }
}
