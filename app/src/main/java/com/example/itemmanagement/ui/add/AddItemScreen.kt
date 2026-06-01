package com.example.itemmanagement.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.itemmanagement.data.entity.template.ItemTemplateEntity
import com.example.itemmanagement.data.entity.unified.CustomAttributeDefinitionEntity
import com.example.itemmanagement.ui.components.GlassCard
import com.example.itemmanagement.ui.components.ItemCategoryPickerRow
import com.example.itemmanagement.ui.components.ItemCategoryPickerSheet
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
import com.example.itemmanagement.ui.components.itemBuildSupplementFields
import com.example.itemmanagement.ui.components.itemBooleanValue
import com.example.itemmanagement.ui.components.itemCustomFieldName
import com.example.itemmanagement.ui.components.itemRemoveFieldFromCurrentForm
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BaseDefaultFields = listOf("名称", "分类", "数量")
private val BaseOptionalFields = listOf("子分类", "品牌", "规格")
private val SupplementDefaultFields = listOf("状态", "标签")
private val SupplementOptionalFields = listOf(
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
private val StatusOptions = listOf("服役中", "未购买", "待补款", "已退役", "已过期")

private enum class AddFieldSection {
    BASE,
    SUPPLEMENT
}

@Composable
fun AddItemScreen(
    viewModel: AddItemViewModel,
    selectedTemplate: ItemTemplateEntity?,
    customAttributeDefinitions: List<CustomAttributeDefinitionEntity>,
    onChooseTemplate: () -> Unit,
    onEditFields: () -> Unit,
    onPickPhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    onRemovePhoto: (Int) -> Unit,
    onSave: () -> Unit
) {
    val fieldVersion by viewModel.fieldVersion.observeAsState(0)
    val selectedFields by viewModel.selectedFields.observeAsState(emptySet())
    val photoUris by viewModel.photoUris.observeAsState(emptyList())
    val selectedTags by viewModel.selectedTags.observeAsState(emptyMap())

    val name = (viewModel.getFieldValue("名称") as? String).orEmpty()
    val category = (viewModel.getFieldValue("分类") as? String).orEmpty()
    val selectedFieldNames = remember(selectedFields) { selectedFields.map { it.name }.toSet() }
    val templateLockedFields = remember(selectedTemplate, customAttributeDefinitions) {
        buildTemplateLockedFields(selectedTemplate, customAttributeDefinitions)
    }

    var activeSheet by rememberSaveable { mutableStateOf<AddFieldSection?>(null) }
    var pendingDeleteField by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if ((viewModel.getFieldValue("添加日期") as? String).isNullOrBlank()) {
            viewModel.saveFieldValue("添加日期", formatDate(Date()))
        }
        if ((viewModel.getFieldValue("数量") as? String).isNullOrBlank()) {
            viewModel.saveFieldValue("数量", "1")
        }
        if ((viewModel.getFieldValue("状态") as? String).isNullOrBlank()) {
            viewModel.saveFieldValue("状态", StatusOptions.first())
        }
    }

    val baseFields = remember(selectedFieldNames, fieldVersion) {
        BaseDefaultFields + BaseOptionalFields.filter { selectedFieldNames.contains(it) }
    }
    val supplementFields = remember(selectedFieldNames, customAttributeDefinitions, fieldVersion) {
        buildSupplementFields(selectedFieldNames, customAttributeDefinitions)
    }
    val tagSuggestions = remember(selectedTags, fieldVersion) {
        val defaults = viewModel.getFieldProperties("标签").options.orEmpty()
        (defaults + viewModel.getCustomTags("标签") + selectedTags["标签"].orEmpty()).distinct()
    }

    val availableBaseFields = remember(baseFields) {
        BaseOptionalFields.filterNot { baseFields.contains(it) }
    }
    val availableSupplementFields = remember(supplementFields, customAttributeDefinitions) {
        val customFields = customAttributeDefinitions.map { itemCustomFieldName(it) }
        (SupplementOptionalFields + customFields)
            .distinct()
            .filterNot { supplementFields.contains(it) || it in SupplementDefaultFields }
    }
    ItemFormScaffold(
        headerContent = {
            TemplateHeaderCard(
                selectedTemplate = selectedTemplate,
                photoCount = photoUris.size,
                onChooseTemplate = onChooseTemplate,
                onEditFields = onEditFields
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
            BaseFieldsContent(
                viewModel = viewModel,
                category = category,
                visibleFields = baseFields,
                lockedFields = templateLockedFields,
                onDeleteField = { pendingDeleteField = it }
            )
            ItemSectionAddButton(
                enabled = availableBaseFields.isNotEmpty(),
                onClick = { activeSheet = AddFieldSection.BASE }
            )
        },
        supplementSection = {
            ItemCompactSelectRow(
                label = "状态",
                value = (viewModel.getFieldValue("状态") as? String).orEmpty(),
                placeholder = "选择状态",
                options = StatusOptions,
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
                .filterNot { it in SupplementDefaultFields }
                .forEach { fieldName ->
                    ItemSupplementFieldItem(
                        fieldName = fieldName,
                        viewModel = viewModel,
                        customAttributeDefinitions = customAttributeDefinitions,
                        locked = fieldName in templateLockedFields,
                        onDelete = { pendingDeleteField = fieldName }
                    )
                }

            ItemSectionAddButton(
                enabled = availableSupplementFields.isNotEmpty(),
                onClick = { activeSheet = AddFieldSection.SUPPLEMENT }
            )
        },
        saveButtonText = "保存物品",
        isSaveEnabled = name.isNotBlank(),
        onSave = onSave
    )

    if (activeSheet != null) {
        ItemFieldPickerSheet(
            title = sectionTitle(activeSheet!!),
            availableFields = when (activeSheet) {
                AddFieldSection.BASE -> availableBaseFields
                AddFieldSection.SUPPLEMENT -> availableSupplementFields
                null -> emptyList()
            },
            customAttributeDefinitions = customAttributeDefinitions,
            onDismiss = { activeSheet = null },
            onFieldSelected = { field ->
                itemAddFieldToSection(viewModel, field, sectionGroup(activeSheet!!))
                activeSheet = null
            }
        )
    }

    pendingDeleteField?.let { fieldName ->
        AlertDialog(
            onDismissRequest = { pendingDeleteField = null },
            title = { Text("移除属性") },
            text = { Text("将从当前表单中移除“$fieldName”，不会修改模板本身。") },
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
private fun TemplateHeaderCard(
    selectedTemplate: ItemTemplateEntity?,
    photoCount: Int,
    onChooseTemplate: () -> Unit,
    onEditFields: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        contentPadding = 18.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "新增物品",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = selectedTemplate?.templateName ?: "通用录入",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AssistChip(onClick = {}, label = { Text("图片 $photoCount") })
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onChooseTemplate) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedTemplate == null) "选择模板" else "切换模板")
                }
                OutlinedButton(onClick = onEditFields) {
                    Icon(Icons.Default.Tune, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("字段布局")
                }
            }
        }
    }
}

@Composable
private fun BaseFieldsContent(
    viewModel: AddItemViewModel,
    category: String,
    visibleFields: List<String>,
    lockedFields: Set<String>,
    onDeleteField: (String) -> Unit
) {
    var showCategorySheet by rememberSaveable { mutableStateOf(false) }

    ItemCompactTextRow(
        label = "名称",
        value = (viewModel.getFieldValue("名称") as? String).orEmpty(),
        placeholder = "请输入名称",
        required = true,
        onValueChange = { viewModel.saveFieldValue("名称", it) }
    )

    ItemCategoryPickerRow(
        label = "分类",
        value = (viewModel.getFieldValue("分类") as? String).orEmpty(),
        onClick = { showCategorySheet = true }
    )

    ItemQuantityRow(
        viewModel = viewModel,
        showExcludeFromTotalCountAction = true,
        excludeFromTotalCount = itemBooleanValue(viewModel.getFieldValue("不计入总数量")),
        onExcludeFromTotalCountChange = { viewModel.saveFieldValue("不计入总数量", it) }
    )

    visibleFields
        .filterNot { it in BaseDefaultFields }
        .forEach { fieldName ->
            when (fieldName) {
                "子分类" -> {
                    if (category.isNotBlank()) {
                        BaseOptionalFieldItem(
                            locked = fieldName in lockedFields,
                            onDelete = { onDeleteField(fieldName) }
                        ) {
                            ItemCompactSelectRow(
                                label = "子分类",
                                value = (viewModel.getFieldValue("子分类") as? String).orEmpty(),
                                placeholder = "选择子分类",
                                options = viewModel.getSubCategoriesForCategory(category),
                                onValueSelected = { viewModel.saveFieldValue("子分类", it) }
                            )
                        }
                    }
                }
                "品牌", "规格" -> {
                    BaseOptionalFieldItem(
                        locked = fieldName in lockedFields,
                        onDelete = { onDeleteField(fieldName) }
                    ) {
                        ItemCompactTextRow(
                            label = fieldName,
                            value = (viewModel.getFieldValue(fieldName) as? String).orEmpty(),
                            placeholder = "请输入$fieldName",
                            onValueChange = { viewModel.saveFieldValue(fieldName, it) }
                        )
                    }
                }
            }
        }

    if (showCategorySheet) {
        ItemCategoryPickerSheet(
            currentValue = (viewModel.getFieldValue("分类") as? String).orEmpty(),
            options = (viewModel.getFieldProperties("分类").options.orEmpty() + viewModel.getCustomOptions("分类")).distinct(),
            onDismiss = { showCategorySheet = false },
            onCreateCategory = { newCategory ->
                viewModel.addCustomOption("分类", newCategory)
                viewModel.saveFieldValue("分类", newCategory)
                viewModel.updateSubCategoryOptions(newCategory)
                showCategorySheet = false
            },
            onSelectCategory = { selectedCategory ->
                viewModel.saveFieldValue("分类", selectedCategory)
                viewModel.updateSubCategoryOptions(selectedCategory)
                showCategorySheet = false
            }
        )
    }
}

private fun buildSupplementFields(
    selectedFieldNames: Set<String>,
    customAttributeDefinitions: List<CustomAttributeDefinitionEntity>
): List<String> {
    return itemBuildSupplementFields(
        selectedFieldNames = selectedFieldNames,
        defaultFields = SupplementDefaultFields,
        optionalFields = SupplementOptionalFields,
        customAttributeDefinitions = customAttributeDefinitions
    )
}

private fun buildTemplateLockedFields(
    selectedTemplate: ItemTemplateEntity?,
    customAttributeDefinitions: List<CustomAttributeDefinitionEntity>
): Set<String> {
    if (selectedTemplate == null) return emptySet()
    return buildSet {
        addAll(
            selectedTemplate.selectedFields
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
        )
        addAll(customAttributeDefinitions.map { itemCustomFieldName(it) })
    }
}

@Composable
private fun BaseOptionalFieldItem(
    locked: Boolean,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    SwipeRevealDeleteContainer(
        enabled = !locked,
        onDeleteClick = onDelete,
        content = content
    )
}

private fun sectionTitle(section: AddFieldSection): String {
    return when (section) {
        AddFieldSection.BASE -> "补充基础属性"
        AddFieldSection.SUPPLEMENT -> "补充其他属性"
    }
}

private fun sectionGroup(section: AddFieldSection): String {
    return when (section) {
        AddFieldSection.BASE -> "基础信息"
        AddFieldSection.SUPPLEMENT -> "补充信息"
    }
}

private fun formatDate(date: Date): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
}
