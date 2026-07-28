package com.example.itemmanagement.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
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
import com.example.itemmanagement.data.entity.template.ItemTemplateEntity
import com.example.itemmanagement.data.model.attribute.RuleDefinition
import com.example.itemmanagement.ui.components.GlassCard
import com.example.itemmanagement.ui.components.ITEM_BASE_OPTIONAL_FIELDS
import com.example.itemmanagement.ui.components.ItemCategoryPickerRow
import com.example.itemmanagement.ui.components.ItemCompactTextRow
import com.example.itemmanagement.ui.components.ItemFieldPickerSheet
import com.example.itemmanagement.ui.components.ItemFormScaffold
import com.example.itemmanagement.ui.components.ItemImageSection
import com.example.itemmanagement.ui.components.ItemQuantityRow
import com.example.itemmanagement.ui.components.ItemRuleBindingSection
import com.example.itemmanagement.ui.components.ItemSectionAddButton
import com.example.itemmanagement.ui.components.ItemSupplementFieldItem
import com.example.itemmanagement.ui.components.itemAddFieldToSection
import com.example.itemmanagement.ui.components.itemApplyRuleRuntimeOutputsFromBindings
import com.example.itemmanagement.ui.components.itemBuildSupplementFields
import com.example.itemmanagement.ui.components.itemCustomFieldName
import com.example.itemmanagement.ui.components.itemIsBaseIntrinsicField
import com.example.itemmanagement.ui.components.itemRemoveFieldFromCurrentForm
import com.example.itemmanagement.ui.components.itemResolveAttributeToggleStates
import com.example.itemmanagement.ui.components.itemResolveDerivedAttributeFieldNamesFromBindings
import com.example.itemmanagement.ui.components.itemSelectedCustomDefinitions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BaseDefaultFields = listOf("名称", "分类", "数量")
private val BaseOptionalFields = ITEM_BASE_OPTIONAL_FIELDS.toList()

private enum class AddFieldSection {
    SUPPLEMENT
}

private enum class AddEditorTab {
    ATTRIBUTES,
    RULES,
}

@Composable
fun AddItemScreen(
    viewModel: AddItemViewModel,
    selectedTemplate: ItemTemplateEntity?,
    customAttributeDefinitions: List<AttributeDefinitionEntity>,
    ruleDefinitions: List<RuleDefinition>,
    onChooseTemplate: () -> Unit,
    onPickPhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    onRemovePhoto: (Int) -> Unit,
    onShowCategoryPicker: () -> Unit,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit
) {
    val fieldVersion by viewModel.fieldVersion.observeAsState(0)
    val selectedFields by viewModel.selectedFields.observeAsState(emptySet())
    val photoUris by viewModel.photoUris.observeAsState(emptyList())
    val itemRuleBindings by viewModel.itemRuleBindings.observeAsState(emptyList())
    val mergedAttributeDefinitions = remember(customAttributeDefinitions, fieldVersion) {
        (customAttributeDefinitions + viewModel.getTransientAttributeDefinitions()).distinctBy { it.id }
    }

    val name = (viewModel.getFieldValue("名称") as? String).orEmpty()
    val selectedFieldNames = remember(selectedFields) { selectedFields.map { it.name }.toSet() }
    val templateLockedFields = remember(selectedTemplate, mergedAttributeDefinitions) {
        buildTemplateLockedFields(selectedTemplate, mergedAttributeDefinitions)
    }

    var activeSheet by rememberSaveable { mutableStateOf<AddFieldSection?>(null) }
    var pendingDeleteField by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTab by rememberSaveable { mutableIntStateOf(AddEditorTab.ATTRIBUTES.ordinal) }

    LaunchedEffect(Unit) {
        if ((viewModel.getFieldValue("添加日期") as? String).isNullOrBlank()) {
            viewModel.saveFieldValue("添加日期", formatDate(Date()))
        }
        if ((viewModel.getFieldValue("数量") as? String).isNullOrBlank()) {
            viewModel.saveFieldValue("数量", "1")
        }
    }

    val baseFields = remember(selectedFieldNames, fieldVersion) {
        BaseDefaultFields + BaseOptionalFields.filter { selectedFieldNames.contains(it) }
    }
    val supplementFields = remember(selectedFieldNames, mergedAttributeDefinitions, fieldVersion) {
        buildSupplementFields(selectedFieldNames, mergedAttributeDefinitions)
    }
    val selectedRuleAttributeDefinitions = remember(selectedFieldNames, mergedAttributeDefinitions) {
        mergedAttributeDefinitions.filter { definition ->
            itemCustomFieldName(definition) in selectedFieldNames
        }
    }
    val selectedCustomDefinitions = remember(selectedFields, mergedAttributeDefinitions) {
        itemSelectedCustomDefinitions(selectedFields, mergedAttributeDefinitions)
    }
    val readonlyDerivedFieldNames = remember(itemRuleBindings, mergedAttributeDefinitions, ruleDefinitions) {
        itemResolveDerivedAttributeFieldNamesFromBindings(
            itemRuleBindings = itemRuleBindings,
            allDefinitions = mergedAttributeDefinitions,
            ruleDefinitions = ruleDefinitions,
        )
    }
    LaunchedEffect(itemRuleBindings, mergedAttributeDefinitions, ruleDefinitions, fieldVersion) {
        itemApplyRuleRuntimeOutputsFromBindings(
            viewModel = viewModel,
            itemRuleBindings = itemRuleBindings,
            allDefinitions = mergedAttributeDefinitions,
            ruleDefinitions = ruleDefinitions,
        )
    }

    val availableSupplementFields = remember(selectedCustomDefinitions, mergedAttributeDefinitions) {
        val selectedIds = selectedCustomDefinitions.map { it.id }.toSet()
        mergedAttributeDefinitions
            .filterNot { it.id in selectedIds }
            .map { itemCustomFieldName(it) }
            .distinct()
    }

    val headerContent: @Composable () -> Unit = {
        AddHeaderCard(
            selectedTemplate = selectedTemplate,
            photoCount = photoUris.size,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            onChooseTemplate = onChooseTemplate,
            onNavigateBack = onNavigateBack,
        )
    }

    if (selectedTab == AddEditorTab.ATTRIBUTES.ordinal) {
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
                BaseFieldsContent(
                    viewModel = viewModel,
                    visibleFields = baseFields,
                    customAttributeDefinitions = mergedAttributeDefinitions,
                    itemRuleBindings = itemRuleBindings,
                    ruleDefinitions = ruleDefinitions,
                    onShowCategoryPicker = onShowCategoryPicker,
                )
            },
            supplementSection = {
                supplementFields.forEach { fieldName ->
                    ItemSupplementFieldItem(
                        fieldName = fieldName,
                        viewModel = viewModel,
                        customAttributeDefinitions = mergedAttributeDefinitions,
                        itemRuleBindings = itemRuleBindings,
                        ruleDefinitions = ruleDefinitions,
                        readonlyDerivedFieldNames = readonlyDerivedFieldNames,
                        locked = fieldName in templateLockedFields || fieldName in readonlyDerivedFieldNames,
                        onDelete = { pendingDeleteField = fieldName },
                    )
                }
                ItemSectionAddButton(
                    enabled = availableSupplementFields.isNotEmpty(),
                    onClick = { activeSheet = AddFieldSection.SUPPLEMENT },
                )
            },
            functionSection = null,
            saveButtonText = "保存物品",
            isSaveEnabled = name.isNotBlank(),
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
                    availableAttributeDefinitions = selectedRuleAttributeDefinitions,
                )
            },
            supplementSection = {},
            functionSection = null,
            saveButtonText = "保存物品",
            isSaveEnabled = name.isNotBlank(),
            onSave = onSave,
        )
    }

    if (activeSheet != null) {
        ItemFieldPickerSheet(
            title = sectionTitle(activeSheet!!),
            availableFields = when (activeSheet) {
                AddFieldSection.SUPPLEMENT -> availableSupplementFields
                null -> emptyList()
            },
            customAttributeDefinitions = mergedAttributeDefinitions,
            onDismiss = { activeSheet = null },
            onFieldSelected = { field ->
                itemAddFieldToSection(
                    viewModel = viewModel,
                    fieldName = field,
                    group = sectionGroup(activeSheet!!),
                )
                activeSheet = null
            }
        )
    }

    pendingDeleteField?.let { fieldName ->
        AlertDialog(
            onDismissRequest = { pendingDeleteField = null },
            title = { Text("移除属性") },
            text = { Text("将从当前表单中移除 $fieldName") },
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
private fun AddHeaderCard(
    selectedTemplate: ItemTemplateEntity?,
    photoCount: Int,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onChooseTemplate: () -> Unit,
    onNavigateBack: () -> Unit,
) {
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
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                    Column {
                        Text(
                            text = "新增物品",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = selectedTemplate?.templateName ?: "通用录入",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                AssistChip(
                    onClick = {},
                    label = { Text("图片 $photoCount") },
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onChooseTemplate) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedTemplate == null) "选择模板" else "切换模板")
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
private fun BaseFieldsContent(
    viewModel: AddItemViewModel,
    visibleFields: List<String>,
    customAttributeDefinitions: List<AttributeDefinitionEntity>,
    itemRuleBindings: List<com.example.itemmanagement.data.model.attribute.RuleBindingInstance>,
    ruleDefinitions: List<RuleDefinition>,
    onShowCategoryPicker: () -> Unit,
) {
    val categoryPath = (viewModel.getFieldValue("分类") as? String).orEmpty()
    val categoryIcon = remember(categoryPath) { viewModel.getCategoryIcon(categoryPath) }
    val quantityToggleStates = remember(itemRuleBindings, ruleDefinitions, customAttributeDefinitions) {
        itemResolveAttributeToggleStates(
            fieldName = "数量",
            itemRuleBindings = itemRuleBindings,
            ruleDefinitions = ruleDefinitions,
            allDefinitions = customAttributeDefinitions,
        )
    }

    ItemCompactTextRow(
        label = "物品名称",
        value = (viewModel.getFieldValue("名称") as? String).orEmpty(),
        placeholder = "请输入物品名称",
        required = true,
        onValueChange = { viewModel.saveFieldValue("名称", it) },
    )

    ItemCategoryPickerRow(
        label = "分类",
        value = categoryPath,
        iconText = categoryIcon,
        onClick = onShowCategoryPicker,
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ItemQuantityRow(
            viewModel = viewModel,
        )
        quantityToggleStates.forEach { toggleState ->
            com.example.itemmanagement.ui.components.ItemAttributeActionRow(
                title = toggleState.label.ifBlank { toggleState.ruleName },
                checked = toggleState.binding.isEnabled,
                onCheckedChange = { enabled ->
                    viewModel.addOrUpdateItemRuleBinding(toggleState.binding.copy(isEnabled = enabled))
                }
            )
        }
    }

    visibleFields.filterNot { it in BaseDefaultFields }.forEach { fieldName ->
        ItemCompactTextRow(
            label = fieldName,
            value = (viewModel.getFieldValue(fieldName) as? String).orEmpty(),
            placeholder = "请输入$fieldName",
            onValueChange = { viewModel.saveFieldValue(fieldName, it) },
        )
    }
}

private fun buildSupplementFields(
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

private fun buildTemplateLockedFields(
    selectedTemplate: ItemTemplateEntity?,
    customAttributeDefinitions: List<AttributeDefinitionEntity>,
): Set<String> {
    if (selectedTemplate == null) return emptySet()
    val templateAttributeIds = parseTemplateAttributeIds(selectedTemplate)
    val templateSelectedNames = selectedTemplate.selectedFields
        .split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .toSet()
    return buildSet {
        addAll(templateSelectedNames.filter(::itemIsBaseIntrinsicField))
        addAll(
            customAttributeDefinitions
                .filter { it.id in templateAttributeIds || itemCustomFieldName(it) in templateSelectedNames }
                .map { itemCustomFieldName(it) }
        )
    }
}

private fun sectionTitle(section: AddFieldSection): String {
    return when (section) {
        AddFieldSection.SUPPLEMENT -> "补充其他属性"
    }
}

private fun sectionGroup(section: AddFieldSection): String {
    return when (section) {
        AddFieldSection.SUPPLEMENT -> "补充信息"
    }
}

private fun parseTemplateAttributeIds(template: ItemTemplateEntity): List<String> {
    if (template.customAttributeIds.isNullOrBlank()) {
        return emptyList()
    }
    val gson = Gson()
    val stringType = object : TypeToken<List<String>>() {}.type
    val legacyType = object : TypeToken<List<Long>>() {}.type
    val stringIds = runCatching {
        gson.fromJson<List<String>>(template.customAttributeIds, stringType).orEmpty()
    }.getOrDefault(emptyList())
    if (stringIds.isNotEmpty()) {
        return stringIds
    }
    return runCatching {
        gson.fromJson<List<Long>>(template.customAttributeIds, legacyType)
            .orEmpty()
            .map { it.toString() }
    }.getOrDefault(emptyList())
}

private fun formatDate(date: Date): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
}
