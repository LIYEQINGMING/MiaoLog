package com.example.itemmanagement.ui.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.itemmanagement.data.entity.attribute.AttributeDefinitionEntity
import com.example.itemmanagement.data.model.attribute.RuleDefinition
import com.example.itemmanagement.ui.components.GlassCard
import com.example.itemmanagement.ui.components.ITEM_BASE_OPTIONAL_FIELDS
import com.example.itemmanagement.ui.components.ItemCategoryPickerRow
import com.example.itemmanagement.ui.components.ItemCompactTextRow
import com.example.itemmanagement.ui.components.ItemFieldPickerSheet
import com.example.itemmanagement.ui.components.ItemFormScaffold
import com.example.itemmanagement.ui.components.ItemImageSection
import com.example.itemmanagement.ui.components.ItemQuantityRow
import com.example.itemmanagement.ui.components.ItemReadonlyRuleOutputCard
import com.example.itemmanagement.ui.components.ItemRuleBindingSection
import com.example.itemmanagement.ui.components.ItemSectionAddButton
import com.example.itemmanagement.ui.components.ItemSupplementFieldItem
import com.example.itemmanagement.ui.components.itemAddFieldToSection
import com.example.itemmanagement.ui.components.itemApplyRuleRuntimeOutputsFromBindings
import com.example.itemmanagement.ui.components.itemBooleanValue
import com.example.itemmanagement.ui.components.itemBuildReadonlyRuleOutputFieldsFromBindings
import com.example.itemmanagement.ui.components.itemBuildSupplementFields
import com.example.itemmanagement.ui.components.itemCustomFieldName
import com.example.itemmanagement.ui.components.itemRemoveFieldFromCurrentForm
import com.example.itemmanagement.ui.components.itemResolveDerivedAttributeFieldNamesFromBindings
import com.example.itemmanagement.ui.components.itemResolveRuleOutputStateFromBindings
import com.example.itemmanagement.ui.components.itemRuleOutputDefinitionId
import com.example.itemmanagement.ui.components.itemRuleOutputKey
import com.example.itemmanagement.ui.components.itemSelectedCustomDefinitions
import com.example.itemmanagement.ui.components.itemStringValue

private val EditBaseDefaultFields = listOf("名称", "分类", "数量")
private val EditBaseOptionalFields = ITEM_BASE_OPTIONAL_FIELDS.toList()

private enum class EditFieldSection {
    SUPPLEMENT
}

private enum class EditEditorTab {
    ATTRIBUTES,
    RULES,
}

@Composable
fun EditItemScreen(
    viewModel: EditItemViewModel,
    customAttributeDefinitions: List<AttributeDefinitionEntity>,
    ruleDefinitions: List<RuleDefinition>,
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
    val itemRuleBindings by viewModel.itemRuleBindings.observeAsState(emptyList())
    val canUndo by viewModel.canUndo.observeAsState(false)
    val hasUnsavedChanges by viewModel.hasUnsavedChanges.observeAsState(false)

    val name = itemStringValue(viewModel.getFieldValue("名称"))
    val selectedFieldNames = remember(selectedFields) { selectedFields.map { it.name }.toSet() }

    var activeSheet by rememberSaveable { mutableStateOf<EditFieldSection?>(null) }
    var pendingDeleteField by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTab by rememberSaveable { mutableIntStateOf(EditEditorTab.ATTRIBUTES.ordinal) }

    val baseFields = remember(selectedFieldNames, fieldVersion) {
        EditBaseDefaultFields + EditBaseOptionalFields.filter { selectedFieldNames.contains(it) }
    }
    val supplementFields = remember(selectedFieldNames, customAttributeDefinitions, fieldVersion) {
        buildEditSupplementFields(selectedFieldNames, customAttributeDefinitions)
    }
    val selectedCustomDefinitions = remember(selectedFields, customAttributeDefinitions) {
        itemSelectedCustomDefinitions(selectedFields, customAttributeDefinitions)
    }
    val readonlyDerivedFieldNames = remember(itemRuleBindings, customAttributeDefinitions, ruleDefinitions) {
        itemResolveDerivedAttributeFieldNamesFromBindings(
            itemRuleBindings = itemRuleBindings,
            allDefinitions = customAttributeDefinitions,
            ruleDefinitions = ruleDefinitions,
        )
    }
    val readonlyRuleOutputFields = remember(itemRuleBindings, customAttributeDefinitions, ruleDefinitions) {
        itemBuildReadonlyRuleOutputFieldsFromBindings(
            itemRuleBindings = itemRuleBindings,
            allDefinitions = customAttributeDefinitions,
            ruleDefinitions = ruleDefinitions,
        )
    }
    val availableSupplementFields = remember(selectedCustomDefinitions, customAttributeDefinitions) {
        val selectedIds = selectedCustomDefinitions.map { it.id }.toSet()
        customAttributeDefinitions
            .filterNot { it.id in selectedIds }
            .map { itemCustomFieldName(it) }
            .distinct()
    }

    LaunchedEffect(itemRuleBindings, customAttributeDefinitions, ruleDefinitions, fieldVersion) {
        itemApplyRuleRuntimeOutputsFromBindings(
            viewModel = viewModel,
            itemRuleBindings = itemRuleBindings,
            allDefinitions = customAttributeDefinitions,
            ruleDefinitions = ruleDefinitions,
        )
    }

    BackHandler(onBack = onNavigateBack)

    val headerContent: @Composable () -> Unit = {
        EditHeaderCard(
            canUndo = canUndo,
            hasUnsavedChanges = hasUnsavedChanges,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            onBackClick = onNavigateBack,
            onUndoClick = { viewModel.undoLastChange() },
            onDeleteClick = onRequestDelete,
        )
    }

    if (selectedTab == EditEditorTab.ATTRIBUTES.ordinal) {
        ItemFormScaffold(
            headerContent = headerContent,
            imageSection = {
                ItemImageSection(
                    photoUris = photoUris.map { it.toString() },
                    onPickPhoto = onPickPhoto,
                    onTakePhoto = onTakePhoto,
                    onRemovePhoto = onRemovePhoto,
                )
            },
            baseSection = {
                EditBaseFieldsContent(
                    viewModel = viewModel,
                    visibleFields = baseFields,
                    onShowCategoryPicker = onShowCategoryPicker,
                )
            },
            supplementSection = {
                supplementFields.forEach { fieldName ->
                    ItemSupplementFieldItem(
                        fieldName = fieldName,
                        viewModel = viewModel,
                        customAttributeDefinitions = customAttributeDefinitions,
                        readonlyDerivedFieldNames = readonlyDerivedFieldNames,
                        locked = fieldName in readonlyDerivedFieldNames,
                        onDelete = { pendingDeleteField = fieldName },
                    )
                }
                ItemSectionAddButton(
                    enabled = availableSupplementFields.isNotEmpty(),
                    onClick = { activeSheet = EditFieldSection.SUPPLEMENT },
                )
            },
            functionSection = null,
            saveButtonText = "保存修改",
            isSaveEnabled = name.isNotBlank(),
            showSaveButton = hasUnsavedChanges,
            onSave = onSave,
        )
    } else {
        ItemFormScaffold(
            headerContent = headerContent,
            imageSection = null,
            baseSection = {
                ItemRuleBindingSection(
                    viewModel = viewModel,
                    itemRuleBindings = itemRuleBindings,
                    ruleDefinitions = ruleDefinitions,
                    availableAttributeDefinitions = selectedCustomDefinitions,
                )
            },
            supplementSection = {
                if (selectedCustomDefinitions.isEmpty()) {
                    Text(
                        text = "先在属性页确认这个物品已经添加了需要参与规则的属性。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else if (readonlyRuleOutputFields.isEmpty()) {
                    Text(
                        text = "当前规则还没有只读输出预览。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    readonlyRuleOutputFields.forEach { fieldName ->
                        val bindingId = itemRuleOutputDefinitionId(fieldName) ?: return@forEach
                        val outputKey = itemRuleOutputKey(fieldName) ?: return@forEach
                        val outputState = itemResolveRuleOutputStateFromBindings(
                            bindingId = bindingId,
                            outputKey = outputKey,
                            viewModel = viewModel,
                            itemRuleBindings = itemRuleBindings,
                            allDefinitions = customAttributeDefinitions,
                            ruleDefinitions = ruleDefinitions,
                        )
                        ItemReadonlyRuleOutputCard(
                            title = outputState.title,
                            value = outputState.value,
                            placeholder = outputState.placeholder,
                            supportText = outputState.supportText,
                        )
                    }
                }
            },
            functionSection = null,
            saveButtonText = "保存修改",
            isSaveEnabled = name.isNotBlank(),
            showSaveButton = hasUnsavedChanges,
            onSave = onSave,
        )
    }

    if (activeSheet != null) {
        ItemFieldPickerSheet(
            title = editSectionTitle(activeSheet!!),
            availableFields = when (activeSheet) {
                EditFieldSection.SUPPLEMENT -> availableSupplementFields
                null -> emptyList()
            },
            customAttributeDefinitions = customAttributeDefinitions,
            onDismiss = { activeSheet = null },
            onFieldSelected = { field ->
                itemAddFieldToSection(
                    viewModel = viewModel,
                    fieldName = field,
                    group = editSectionGroup(activeSheet!!),
                )
                activeSheet = null
            }
        )
    }

    pendingDeleteField?.let { fieldName ->
        AlertDialog(
            onDismissRequest = { pendingDeleteField = null },
            title = { Text("移除属性") },
            text = { Text("将从当前编辑表单中移除 $fieldName") },
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
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onBackClick: () -> Unit,
    onUndoClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        contentPadding = 16.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                    Column {
                        Text(
                            text = "编辑物品",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = if (hasUnsavedChanges) "当前有未保存的修改" else "内容已同步",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(enabled = canUndo, onClick = onUndoClick) {
                        Icon(Icons.Default.Undo, contentDescription = "撤销")
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "更多")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("删除物品") },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onDeleteClick()
                                },
                            )
                        }
                    }
                }
            }

            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { onTabSelected(0) }, text = { Text("属性") })
                Tab(selected = selectedTab == 1, onClick = { onTabSelected(1) }, text = { Text("规则") })
            }
        }
    }
}

@Composable
private fun EditBaseFieldsContent(
    viewModel: EditItemViewModel,
    visibleFields: List<String>,
    onShowCategoryPicker: () -> Unit,
) {
    val currentName = itemStringValue(viewModel.getFieldValue("名称"))
    val categoryPath = itemStringValue(viewModel.getFieldValue("分类"))
    val categoryIcon = remember(categoryPath) { viewModel.getCategoryIcon(categoryPath) }

    ItemCompactTextRow(
        label = "物品名称",
        value = currentName,
        placeholder = currentName.ifBlank { "请输入物品名称" },
        required = true,
        onValueChange = { viewModel.saveFieldValue("名称", it) },
    )

    ItemCategoryPickerRow(
        label = "分类",
        value = categoryPath,
        iconText = categoryIcon,
        onClick = onShowCategoryPicker,
    )

    ItemQuantityRow(
        viewModel = viewModel,
        showExcludeFromTotalCountAction = true,
        excludeFromTotalCount = itemBooleanValue(viewModel.getFieldValue("不计入总数量")),
        onExcludeFromTotalCountChange = { viewModel.saveFieldValue("不计入总数量", it) },
    )

    visibleFields.filterNot { it in EditBaseDefaultFields }.forEach { fieldName ->
        ItemCompactTextRow(
            label = fieldName,
            value = itemStringValue(viewModel.getFieldValue(fieldName)),
            placeholder = "请输入$fieldName",
            onValueChange = { viewModel.saveFieldValue(fieldName, it) },
        )
    }
}

private fun buildEditSupplementFields(
    selectedFieldNames: Set<String>,
    customAttributeDefinitions: List<AttributeDefinitionEntity>,
): List<String> {
    return itemBuildSupplementFields(
        selectedFieldNames = selectedFieldNames,
        defaultFields = emptyList(),
        optionalFields = emptyList(),
        customAttributeDefinitions = customAttributeDefinitions,
    )
}

private fun editSectionTitle(section: EditFieldSection): String {
    return when (section) {
        EditFieldSection.SUPPLEMENT -> "补充其他属性"
    }
}

private fun editSectionGroup(section: EditFieldSection): String {
    return when (section) {
        EditFieldSection.SUPPLEMENT -> "补充信息"
    }
}
