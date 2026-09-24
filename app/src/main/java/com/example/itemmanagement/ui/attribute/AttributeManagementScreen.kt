package com.example.itemmanagement.ui.attribute

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import com.example.itemmanagement.data.model.attribute.AttributeInputMode
import com.example.itemmanagement.data.model.attribute.AttributeNumberFormat
import com.example.itemmanagement.data.model.attribute.AttributeValueSource
import com.example.itemmanagement.data.model.attribute.AttributeValueType
import com.example.itemmanagement.data.model.attribute.RuleActivationMode
import com.example.itemmanagement.data.model.attribute.RuleCanvasConnection
import com.example.itemmanagement.data.model.attribute.RuleCanvasDefinition
import com.example.itemmanagement.data.model.attribute.RuleCanvasLayoutMode
import com.example.itemmanagement.data.model.attribute.RuleCanvasNode
import com.example.itemmanagement.data.model.attribute.RuleCanvasNodeStyleHint
import com.example.itemmanagement.data.model.attribute.RuleCanvasNodeType
import com.example.itemmanagement.data.model.attribute.RuleCanvasSlotConfig
import com.example.itemmanagement.data.model.attribute.RuleCanvasSlotValueSource
import com.example.itemmanagement.data.model.attribute.RuleComputationType
import com.example.itemmanagement.data.model.attribute.RuleOutputTargetType
import com.example.itemmanagement.data.model.attribute.RuleOutputUpdateMode
import com.example.itemmanagement.data.model.attribute.RuleScheduleSourceMode
import com.example.itemmanagement.data.model.attribute.RuleScheduleTimeSourceType
import com.example.itemmanagement.data.model.attribute.RuleSlotDirection
import com.example.itemmanagement.data.model.attribute.RuleSlotSourceType
import com.example.itemmanagement.data.model.attribute.RuleSlotValueType
import com.example.itemmanagement.data.model.attribute.RuleTriggerMode
import com.example.itemmanagement.ui.attribute.model.*
import com.example.itemmanagement.ui.components.GlassCard
import com.example.itemmanagement.ui.components.MiaoIcon
import com.example.itemmanagement.ui.main.LiquidBackground
import com.example.itemmanagement.ui.theme.LiquidGlassTheme
import com.example.itemmanagement.data.model.attribute.findSystemVariableKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttributeManagementScreen(
    uiState: AttributeManagementUiState,
    onClose: () -> Unit,
    onNavigateBackInPage: () -> Unit,
    onTabSelected: (AttributeManagementTab) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCycleAttributeSourceFilter: () -> Unit,
    onCycleAttributeValueTypeFilter: () -> Unit,
    onCycleAttributeRuleBindingFilter: () -> Unit,
    onClearAttributeFilters: () -> Unit,
    onCycleRuleSourceFilter: () -> Unit,
    onCycleRuleManagementTypeFilter: () -> Unit,
    onCycleRuleUsageFilter: () -> Unit,
    onClearRuleFilters: () -> Unit,
    onOpenCreateAttribute: () -> Unit,
    onOpenCreateRule: () -> Unit,
    onOpenAttributeDetail: (AttributeListItemUiModel) -> Unit,
    onOpenRuleDetail: (RuleListItemUiModel) -> Unit,
    onOpenEditAttribute: (String) -> Unit,
    onDuplicateAttribute: (String) -> Unit,
    onOpenEditRule: (String) -> Unit,
    onSaveAttributeDraft: (AttributeEditorDraftUiModel) -> Unit,
    onOpenRuleBindingWizard: (AttributeEditorDraftUiModel, String?) -> Unit,
    onSaveRuleBindingWizard: (RuleBindingWizardDraftUiModel) -> Unit,
    onSaveRuleDraft: (RuleEditorDraftUiModel) -> Unit,
    onDeleteAttribute: (AttributeListItemUiModel) -> Unit,
    onDeleteRule: (RuleListItemUiModel) -> Unit,
    onDeleteRuleById: (String) -> Unit,
    onDismissDialog: () -> Unit,
    onConfirmDeleteAttribute: () -> Unit,
    onConfirmDeleteRule: () -> Unit,
    onMessageConsumed: () -> Unit,
) {
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        if (!uiState.message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(uiState.message)
            onMessageConsumed()
        }
    }

    BackHandler {
        if (uiState.dialogState != null) {
            onDismissDialog()
        } else if (uiState.routeState != AttributeManagementRouteState.List) {
            onNavigateBackInPage()
        } else {
            onClose()
        }
    }

    LiquidGlassTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            LiquidBackground(modifier = Modifier.fillMaxSize())

            Column(modifier = Modifier.fillMaxSize()) {
                when (val routeState = uiState.routeState) {
                    AttributeManagementRouteState.List -> {
                        val isRuleEntryMode = uiState.entryMode == AttributeManagementEntryMode.RULES
                        AttributeManagementHeader(
                            title = if (isRuleEntryMode) "规则管理" else "属性管理",
                            searchQuery = uiState.searchQuery,
                            entryMode = uiState.entryMode,
                            selectedTab = uiState.selectedTab,
                            isListMode = true,
                            onBack = onClose,
                            onTabSelected = onTabSelected,
                            onSearchQueryChange = onSearchQueryChange,
                            onPrimaryActionClick = if (isRuleEntryMode) onOpenCreateRule else onOpenCreateAttribute,
                        )

                        if (isRuleEntryMode) {
                            RuleFilterSection(
                                filters = uiState.rulePane.filters,
                                totalCount = uiState.rulePane.totalCount,
                                systemCount = uiState.rulePane.systemCount,
                                customCount = uiState.rulePane.customCount,
                                boundCount = uiState.rulePane.boundCount,
                                onCycleSource = onCycleRuleSourceFilter,
                                onCycleRuleType = onCycleRuleManagementTypeFilter,
                                onCycleUsage = onCycleRuleUsageFilter,
                                onClearFilters = onClearRuleFilters,
                            )
                            RuleListPane(
                                paneState = uiState.rulePane,
                                listState = listState,
                                onOpenRuleDetail = onOpenRuleDetail,
                                onEditRule = onOpenEditRule,
                                onDeleteRule = onDeleteRule,
                            )
                        } else {
                            AttributeFilterSection(
                                filters = uiState.attributePane.filters,
                                totalCount = uiState.attributePane.totalCount,
                                onCycleSource = onCycleAttributeSourceFilter,
                                onCycleValueType = onCycleAttributeValueTypeFilter,
                                onCycleRuleBinding = onCycleAttributeRuleBindingFilter,
                                onClearFilters = onClearAttributeFilters,
                            )
                            AttributeListPane(
                                paneState = uiState.attributePane,
                                listState = listState,
                                onOpenAttributeDetail = onOpenAttributeDetail,
                                onEditAttribute = onOpenEditAttribute,
                                onDuplicateAttribute = onDuplicateAttribute,
                                onDeleteAttribute = onDeleteAttribute,
                            )
                        }
                    }

                    is AttributeManagementRouteState.AttributeDetail -> {
                        SecondaryPageHeader(
                            title = routeState.detail.name,
                            subtitle = "属性详情",
                            onBack = onNavigateBackInPage,
                        )
                        AttributeDetailPage(
                            detail = routeState.detail,
                            onEdit = { onOpenEditAttribute(routeState.detail.id) },
                            onDelete = {
                                onDeleteAttribute(
                                    AttributeListItemUiModel(
                                        id = routeState.detail.id,
                                        name = routeState.detail.name,
                                        icon = routeState.detail.icon,
                                        sourceLabel = routeState.detail.sourceLabel,
                                        valueTypeLabel = routeState.detail.valueDefinition.valueType,
                                        valueSourceLabel = routeState.detail.valueDefinition.valueSource,
                                        interactionModeLabel = routeState.detail.valueDefinition.interactionMode,
                                        propertySummary = routeState.detail.valueDefinition.properties.joinToString(" / ") { "${it.label}：${it.value}" },
                                        templateName = routeState.detail.templateName,
                                        ruleSummary = if (routeState.detail.ruleBindings.isEmpty()) "无" else routeState.detail.ruleBindings.joinToString(" / ") { it.ruleName },
                                        usageCountText = routeState.detail.usageCountText,
                                        isSystemBuiltIn = routeState.detail.isSystemBuiltIn,
                                    )
                                )
                            },
                        )
                    }

                    is AttributeManagementRouteState.TemplateDetail -> {
                        SecondaryPageHeader(
                            title = "模板功能已停用",
                            subtitle = "请返回属性管理或规则管理继续操作",
                            onBack = onNavigateBackInPage,
                        )
                        EmptyStateCard(
                            title = "模板管理已移除",
                            description = "属性现在直接新建，规则也直接在规则管理中创建，不再通过模板入口操作。",
                            modifier = Modifier.padding(16.dp),
                        )
                    }

                    is AttributeManagementRouteState.RuleDetail -> {
                        SecondaryPageHeader(
                            title = routeState.detail.name,
                            subtitle = "规则详情",
                            onBack = onNavigateBackInPage,
                        )
                        RuleDetailPage(
                            detail = routeState.detail,
                            onEdit = { onOpenEditRule(routeState.detail.id) },
                            onDelete = { onDeleteRuleById(routeState.detail.id) },
                        )
                    }

                    is AttributeManagementRouteState.AttributeEditor -> {
                        SecondaryPageHeader(
                            title = routeState.draft.title,
                            subtitle = if (routeState.draft.isEditMode) "属性编辑页" else "属性创建页",
                            onBack = onNavigateBackInPage,
                        )
                        AttributeEditorPage(
                            draft = routeState.draft,
                            onSave = onSaveAttributeDraft,
                            onOpenRuleBindingWizard = onOpenRuleBindingWizard,
                        )
                    }

                    is AttributeManagementRouteState.RuleEditor -> {
                        SecondaryPageHeader(
                            title = routeState.draft.title,
                            subtitle = if (routeState.draft.isEditMode) "规则编辑页" else "规则创建页",
                            onBack = onNavigateBackInPage,
                        )
                        RuleWorkbenchPage(
                            draft = routeState.draft,
                            onSave = onSaveRuleDraft,
                        )
                    }

                    is AttributeManagementRouteState.RuleBindingWizard -> {
                        SecondaryPageHeader(
                            title = routeState.draft.title,
                            subtitle = "规则绑定页",
                            onBack = onNavigateBackInPage,
                        )
                        RuleBindingWizardPage(
                            draft = routeState.draft,
                            onSave = onSaveRuleBindingWizard,
                        )
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
                        bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                    )
            )

        }
    }

    when (val dialogState = uiState.dialogState) {
        is AttributeManagementDialogState.ConfirmDeleteAttribute -> {
            AlertDialog(
                onDismissRequest = onDismissDialog,
                title = { Text("删除属性") },
                text = {
                    Text(
                        "该属性已被 ${dialogState.usageCount} 个物品使用，删除后相关属性值会一起移除，是否继续？"
                    )
                },
                confirmButton = {
                    TextButton(onClick = onConfirmDeleteAttribute) {
                        Text("删除")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissDialog) {
                        Text("取消")
                    }
                }
            )
        }

        is AttributeManagementDialogState.ConfirmDeleteRule -> {
            AlertDialog(
                onDismissRequest = onDismissDialog,
                title = { Text("删除规则") },
                text = {
                    val affectedSummary = if (dialogState.affectedAttributeNames.isEmpty()) {
                        "当前没有属性绑定该规则。"
                    } else {
                        "当前有 ${dialogState.affectedAttributeCount} 个属性绑定该规则：${dialogState.affectedAttributeNames.joinToString("、")}。"
                    }
                    Text("$affectedSummary 删除后会自动移除这些属性上的规则绑定，是否继续？")
                },
                confirmButton = {
                    TextButton(onClick = onConfirmDeleteRule) {
                        Text("删除")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissDialog) {
                        Text("取消")
                    }
                }
            )
        }

        null -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttributeManagementHeader(
    title: String,
    searchQuery: String,
    entryMode: AttributeManagementEntryMode,
    selectedTab: AttributeManagementTab,
    isListMode: Boolean,
    onBack: () -> Unit,
    onTabSelected: (AttributeManagementTab) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onPrimaryActionClick: (() -> Unit)?,
) {
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val headerTopPadding = (statusBarTopPadding - 8.dp).coerceAtLeast(0.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = headerTopPadding, start = 16.dp, end = 16.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            blurRadius = 28.dp,
            contentPadding = 10.dp,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        modifier = Modifier.size(36.dp),
                        onClick = onBack,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    if (isListMode && onPrimaryActionClick != null) {
                        IconButton(
                            modifier = Modifier.size(36.dp),
                            onClick = onPrimaryActionClick,
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "创建")
                        }
                    } else {
                        Spacer(modifier = Modifier.size(36.dp))
                    }
                }

                if (isListMode) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        placeholder = {
                            Text(
                                when (entryMode) {
                                    AttributeManagementEntryMode.ATTRIBUTES -> "输入关键词"
                                    AttributeManagementEntryMode.RULES -> "输入关键词"
                                }
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }
}

@Composable
private fun SecondaryPageHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
) {
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val headerTopPadding = (statusBarTopPadding - 8.dp).coerceAtLeast(0.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = headerTopPadding, start = 16.dp, end = 16.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            blurRadius = 28.dp,
            contentPadding = 10.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    modifier = Modifier.size(36.dp),
                    onClick = onBack,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.size(36.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AttributeFilterSection(
    filters: AttributeListFilters,
    totalCount: Int,
    onCycleSource: () -> Unit,
    onCycleValueType: () -> Unit,
    onCycleRuleBinding: () -> Unit,
    onClearFilters: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        blurRadius = 24.dp,
        contentPadding = 14.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "属性视图 · 共 $totalCount 项",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = filters.source != AttributeSourceFilter.ALL,
                    onClick = onCycleSource,
                    label = { Text(filters.source.displayName) },
                )
                FilterChip(
                    selected = filters.valueType != AttributeValueTypeFilter.ALL,
                    onClick = onCycleValueType,
                    label = { Text(filters.valueType.displayName) },
                )
                FilterChip(
                    selected = filters.ruleBinding != RuleBindingFilter.ALL,
                    onClick = onCycleRuleBinding,
                    label = { Text(filters.ruleBinding.displayName) },
                )
                if (filters != AttributeListFilters()) {
                    FilterChip(
                        selected = false,
                        onClick = onClearFilters,
                        label = { Text("清空筛选") },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RuleFilterSection(
    filters: RuleListFilters,
    totalCount: Int,
    systemCount: Int,
    customCount: Int,
    boundCount: Int,
    onCycleSource: () -> Unit,
    onCycleRuleType: () -> Unit,
    onCycleUsage: () -> Unit,
    onClearFilters: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        blurRadius = 24.dp,
        contentPadding = 14.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "规则视图 · 共 $totalCount 项",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "系统 $systemCount · 自定义 $customCount · 已绑定 $boundCount",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = filters.source != RuleSourceFilter.ALL,
                    onClick = onCycleSource,
                    label = { Text(filters.source.displayName) },
                )
                FilterChip(
                    selected = filters.ruleType != RuleTypeFilter.ALL,
                    onClick = onCycleRuleType,
                    label = { Text(filters.ruleType.displayName) },
                )
                FilterChip(
                    selected = filters.usage != RuleUsageFilter.ALL,
                    onClick = onCycleUsage,
                    label = { Text(filters.usage.displayName) },
                )
                if (filters != RuleListFilters()) {
                    FilterChip(
                        selected = false,
                        onClick = onClearFilters,
                        label = { Text("清空筛选") },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TemplateFilterSection(
    filters: TemplateListFilters,
    totalCount: Int,
    onCycleTemplateType: () -> Unit,
    onCycleTemplateCategory: () -> Unit,
    onCycleRuleType: () -> Unit,
    onClearFilters: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        blurRadius = 24.dp,
        contentPadding = 14.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "模板视图 · 共 $totalCount 项",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = filters.templateType != TemplateTypeFilter.ALL,
                    onClick = onCycleTemplateType,
                    label = { Text(filters.templateType.displayName) },
                )
                FilterChip(
                    selected = filters.templateCategory != TemplateCategoryFilter.ALL,
                    onClick = onCycleTemplateCategory,
                    label = { Text(filters.templateCategory.displayName) },
                )
                FilterChip(
                    selected = filters.ruleType != RuleTypeFilter.ALL,
                    onClick = onCycleRuleType,
                    label = { Text(filters.ruleType.displayName) },
                )
                if (filters != TemplateListFilters()) {
                    FilterChip(
                        selected = false,
                        onClick = onClearFilters,
                        label = { Text("清空筛选") },
                    )
                }
            }
        }
    }
}

@Composable
private fun AttributeListPane(
    paneState: AttributeListPaneState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onOpenAttributeDetail: (AttributeListItemUiModel) -> Unit,
    onEditAttribute: (String) -> Unit,
    onDuplicateAttribute: (String) -> Unit,
    onDeleteAttribute: (AttributeListItemUiModel) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = paneState.contentState) {
            ListContentState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            ListContentState.Empty -> {
                EmptyStateCard(
                    title = "还没有属性",
                    description = "可以先创建一个属性定义，再在物品页按需使用。",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                )
            }

            ListContentState.SearchEmpty -> {
                EmptyStateCard(
                    title = "没有找到匹配属性",
                    description = "可以调整关键词，继续搜索属性名称、值类型或值来源。",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                )
            }

            ListContentState.FilterEmpty -> {
                EmptyStateCard(
                    title = "当前筛选条件下没有属性",
                    description = "可以清空筛选，或调整来源和值类型后再看。",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                )
            }

            ListContentState.Data -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 24.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(paneState.items, key = { it.id }) { item ->
                        AttributeCard(
                            item = item,
                            onOpen = { onOpenAttributeDetail(item) },
                            onEdit = { onEditAttribute(item.id) },
                            onDuplicate = { onDuplicateAttribute(item.id) },
                            onDelete = { onDeleteAttribute(item) },
                        )
                    }
                }
            }

            is ListContentState.Error -> {
                EmptyStateCard(
                    title = "属性加载失败",
                    description = state.message,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun RuleListPane(
    paneState: RuleListPaneState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onOpenRuleDetail: (RuleListItemUiModel) -> Unit,
    onEditRule: (String) -> Unit,
    onDeleteRule: (RuleListItemUiModel) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = paneState.contentState) {
            ListContentState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            ListContentState.Empty -> {
                EmptyStateCard(
                    title = "还没有规则",
                    description = "可以先新建规则，再在物品页按需绑定和使用。",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                )
            }

            ListContentState.SearchEmpty -> {
                EmptyStateCard(
                    title = "没有找到匹配规则",
                    description = "可以调整关键词，继续搜索规则名称、依赖项或输出定义。",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                )
            }

            ListContentState.FilterEmpty -> {
                EmptyStateCard(
                    title = "当前条件下没有规则",
                    description = "请尝试调整筛选条件。",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                )
            }

            ListContentState.Data -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 24.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(paneState.items, key = { it.id }) { item ->
                        RuleCard(
                            item = item,
                            onOpen = { onOpenRuleDetail(item) },
                            onEdit = { onEditRule(item.id) },
                            onDelete = { onDeleteRule(item) },
                        )
                    }
                }
            }

            is ListContentState.Error -> {
                EmptyStateCard(
                    title = "规则加载失败",
                    description = state.message,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun TemplateListPane(
    paneState: TemplateListPaneState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onOpenTemplateDetail: (TemplateListItemUiModel) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = paneState.contentState) {
            ListContentState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            ListContentState.Empty -> {
                EmptyStateCard(
                    title = "暂无模板",
                    description = "当前没有可用模板，请稍后重试。",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                )
            }

            ListContentState.SearchEmpty -> {
                EmptyStateCard(
                    title = "没有找到匹配模板",
                    description = "可以调整关键词，或切换模板类型后再试。",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                )
            }

            ListContentState.FilterEmpty -> {
                EmptyStateCard(
                    title = "当前条件下没有模板",
                    description = "可以切换模板类型、规则类型或值类型筛选。",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                )
            }

            ListContentState.Data -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 24.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(paneState.items, key = { it.id }) { item ->
                        TemplateCard(
                            item = item,
                            onOpen = { onOpenTemplateDetail(item) },
                        )
                    }
                }
            }

            is ListContentState.Error -> {
                EmptyStateCard(
                    title = "模板加载失败",
                    description = state.message,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun AttributeCard(
    item: AttributeListItemUiModel,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(22.dp),
        contentPadding = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 52.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${item.sourceLabel} · ${item.valueTypeLabel} · ${item.valueSourceLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = item.propertySummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "更多操作")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("查看属性详情") },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onOpen()
                        },
                    )
                    if (!item.isSystemBuiltIn) {
                        DropdownMenuItem(
                            text = { Text("编辑属性") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("复制属性") },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onDuplicate()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("删除属性") },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleCard(
    item: RuleListItemUiModel,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(22.dp),
        contentPadding = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 52.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ListItemIcon(icon = item.icon)
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${item.sourceLabel} · ${item.ruleTypeLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "启用：${item.activationSummary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "触发：${item.triggerSummary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "槽位：${item.slotCountSummary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "输入：${item.inputSummary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "输出：${item.outputSummary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = item.affectedAttributeCountText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (item.affectedAttributeNames.isNotEmpty()) {
                        Text(
                            text = "关联属性：${item.affectedAttributeNames.take(3).joinToString(" / ")}${if (item.affectedAttributeNames.size > 3) " 等" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "更多操作")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("查看规则详情") },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onOpen()
                        },
                    )
                    if (!item.isSystemBuiltIn) {
                        DropdownMenuItem(
                            text = { Text("编辑规则") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("删除规则") },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(
    item: TemplateListItemUiModel,
    onOpen: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(22.dp),
        contentPadding = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 52.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ListItemIcon(icon = item.icon)
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${item.templateType.displayName} · ${item.badgeText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = item.summaryLine1,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = item.summaryLine2,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = item.countText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "更多操作")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("查看模板详情") },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onOpen()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ListItemIcon(
    icon: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(40.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            MiaoIcon(
                icon = icon,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        contentPadding = 18.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AttributeDetailPage(
    detail: AttributeDetailUiModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 32.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            DetailSummaryCard(
                icon = detail.icon,
                title = detail.name,
                summary = "${detail.sourceLabel} · ${detail.usageCountText}",
            )
        }
        item {
            SectionCard(title = "属性定义") {
                DetailTextBlock("属性名", detail.name)
                DetailTextBlock("属性描述", detail.description ?: "暂无描述")
                DetailTextBlock("值类型", detail.valueDefinition.valueType)
                DetailTextBlock("值来源", detail.valueDefinition.valueSource)
            }
        }
        item {
            SectionCard(title = "操作") {
                Text(
                    text = if (detail.isSystemBuiltIn) {
                        "系统属性当前阶段仅支持查看。"
                    } else {
                        "自定义属性当前仅支持修改名称与说明，值类型和值来源保持不变。"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (detail.isEditable) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onEdit)
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Text("编辑属性", fontWeight = FontWeight.Medium)
                    }
                }
                if (detail.isDeletable) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onDelete)
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Text("删除属性", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleDetailPage(
    detail: RuleDetailUiModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 32.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            DetailSummaryCard(
                icon = detail.icon,
                title = detail.name,
                summary = "${detail.sourceLabel} · ${detail.ruleType}",
            )
        }
        item {
            SectionCard(title = "规则概览") {
                DetailLine("规则类型", detail.ruleType)
                DetailLine("启用控制", detail.activationSummary)
                DetailLine("触发方式", if (detail.triggerModes.isEmpty()) "无" else detail.triggerModes.joinToString(" / "))
                detail.scheduleSummary?.let { DetailLine("周期调度", it) }
                detail.scheduleEventSummary?.let { DetailLine("事件生成", it) }
            }
        }
        item {
            SectionCard(title = "输入槽位") {
                if (detail.inputSlots.isEmpty()) {
                    Text("当前没有输入槽位。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    detail.inputSlots.forEach { slot ->
                        RuleSlotSummaryCard(slot)
                    }
                }
            }
        }
        item {
            SectionCard(title = "输出槽位") {
                if (detail.outputSlots.isEmpty()) {
                    Text("当前没有输出槽位。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    detail.outputSlots.forEach { slot ->
                        RuleSlotSummaryCard(slot)
                    }
                }
            }
        }
        item {
            SectionCard(title = "输出策略") {
                if (detail.outputStrategies.isEmpty()) {
                    Text("当前没有额外输出策略。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    detail.outputStrategies.forEach { strategy ->
                        DetailLine(
                            strategy.slotKey,
                            "${strategy.targetLabel} · ${strategy.updateModeLabel}"
                        )
                    }
                }
            }
        }
        item {
            SectionCard(title = "规则说明") {
                DetailLine("表达式", detail.expression ?: "未配置")
                DetailLine("描述", detail.description ?: "暂无说明")
            }
        }
        item {
            SectionCard(title = "绑定属性") {
                DetailLine("绑定统计", detail.boundAttributeCountText)
                if (detail.boundAttributes.isEmpty()) {
                    Text("当前没有属性绑定该规则。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    detail.boundAttributes.forEach { attributeName ->
                        Text(attributeName, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            SectionCard(title = "操作") {
                Text(
                    text = if (detail.isSystemBuiltIn) {
                        "系统规则当前阶段仅支持查看。"
                    } else {
                        "自定义规则允许继续编辑结构、依赖和输出定义。"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (detail.isEditable) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onEdit)
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Text("编辑规则", fontWeight = FontWeight.Medium)
                    }
                }
                if (detail.isDeletable) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onDelete)
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Text("删除规则", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateDetailPage(
    detail: TemplateDetailUiModel,
    onCreateFromTemplate: () -> Unit,
    onCreateRuleFromTemplate: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 32.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            DetailSummaryCard(
                icon = detail.icon,
                title = detail.name,
                summary = if (detail is TemplateDetailUiModel.AttributeTemplateDetail) "属性模板" else "规则模板",
            )
        }
        when (detail) {
            is TemplateDetailUiModel.AttributeTemplateDetail -> {
                item {
                    SectionCard(title = "默认边界") {
                        DetailLine("模板分类", detail.categoryLabel)
                        DetailLine("值类型", detail.defaultDefinition.valueType)
                        DetailLine("值来源", detail.defaultDefinition.valueSource)
                        if (detail.defaultDefinition.interactionMode.isNotBlank()) {
                            DetailLine("交互方式", detail.defaultDefinition.interactionMode)
                        }
                        detail.defaultDefinition.properties.forEach { property ->
                            DetailLine(property.label, property.value)
                        }
                    }
                }
                item {
                    SectionCard(title = "默认规则绑定") {
                        if (detail.defaultRuleBindings.isEmpty()) {
                            Text("当前模板不预置规则绑定", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            detail.defaultRuleBindings.forEach { binding ->
                                Text("• $binding", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                item {
                    SectionCard(title = "使用说明") {
                        DetailLine("典型用途", detail.usageHint)
                        DetailLine("派生统计", detail.derivedAttributeCountText)
                    }
                }
                item {
                    PrimaryActionCard(
                        title = "从该模板创建属性",
                        description = "带出默认字段边界和规则绑定，再做微调。",
                        actionLabel = "继续创建",
                        onClick = onCreateFromTemplate,
                    )
                }
            }

            is TemplateDetailUiModel.RuleTemplateDetail -> {
                item {
                    SectionCard(title = "规则边界") {
                        DetailLine("规则类型", detail.ruleType)
                        DetailLine("启用控制", detail.activationSummary)
                        DetailLine("触发方式", if (detail.triggerModes.isEmpty()) "无" else detail.triggerModes.joinToString(" / "))
                        detail.scheduleSummary?.let { DetailLine("周期调度", it) }
                        detail.scheduleEventSummary?.let { DetailLine("事件生成", it) }
                        DetailLine("引用统计", detail.referenceCountText)
                    }
                }
                item {
                    SectionCard(title = "输入槽位") {
                        if (detail.inputSlots.isEmpty()) {
                            Text("当前模板未预置输入槽位。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            detail.inputSlots.forEach { slot ->
                                RuleSlotSummaryCard(slot)
                            }
                        }
                    }
                }
                item {
                    SectionCard(title = "输出槽位") {
                        if (detail.outputSlots.isEmpty()) {
                            Text("当前模板未预置输出槽位。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            detail.outputSlots.forEach { slot ->
                                RuleSlotSummaryCard(slot)
                            }
                        }
                    }
                }
                item {
                    SectionCard(title = "输出策略") {
                        if (detail.outputStrategies.isEmpty()) {
                            Text("当前模板未预置输出策略。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            detail.outputStrategies.forEach { output ->
                                DetailLine(output.slotKey, "${output.targetLabel} / ${output.updateModeLabel}")
                            }
                        }
                    }
                }
                item {
                    SectionCard(title = "表达式说明") {
                        DetailLine("默认表达式", detail.expression ?: "未预置")
                        DetailLine("模板说明", detail.description ?: "暂无补充说明")
                    }
                }
                item {
                    PrimaryActionCard(
                        title = "从该模板创建规则",
                        description = "带出槽位结构、默认表达式和输出策略，再做微调。",
                        actionLabel = "继续创建",
                        onClick = onCreateRuleFromTemplate,
                    )
                }
            }
        }
    }
}

@Composable
private fun AttributeEditorPage(
    draft: AttributeEditorDraftUiModel,
    onSave: (AttributeEditorDraftUiModel) -> Unit,
    onOpenRuleBindingWizard: (AttributeEditorDraftUiModel, String?) -> Unit,
) {
    var name by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.name) }
    var description by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.description) }
    var valueType by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.valueType) }
    var valueSource by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.valueSource) }
    var numberFormat by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.numberFormat) }
    var unitCategory by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.unitCategory) }
    var defaultUnit by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.defaultUnit) }
    var decimalPlacesText by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.decimalPlacesText) }
    var allowNegative by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.allowNegative) }
    var booleanInputMode by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.booleanInputMode) }
    var trueLabel by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.trueLabel) }
    var falseLabel by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.falseLabel) }
    var optionItemsText by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.optionItemsText) }
    var isMultiSelect by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.isMultiSelect) }
    var systemVariableKey by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.systemVariableKey) }
    var defaultValue by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.defaultValue) }
    var ruleCandidates by remember(draft.id, draft.templateId) { mutableStateOf(draft.ruleCandidates) }
    var pendingCreatedAttributes by remember(draft.id, draft.templateId) { mutableStateOf(draft.pendingCreatedAttributes) }
    var pendingRemoveBinding by remember(draft.id, draft.templateId) { mutableStateOf<RuleBindingCandidateUiModel?>(null) }
    val compatibleSystemVariables = draft.systemVariableOptions.filter { it.valueType == valueType }
    val selectedSystemVariable = draft.systemVariableOptions.firstOrNull { it.key == systemVariableKey }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 32.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            DetailSummaryCard(
                icon = attributeEditorPreviewIcon(valueType, numberFormat, unitCategory),
                title = if (name.isBlank()) "未命名属性" else name,
                summary = if (draft.isEditMode) "仅修改名称与说明" else "自定义属性定义",
            )
        }
        item {
            SectionCard(title = "基本信息") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("属性名称") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("说明") },
                    minLines = 2,
                )
            }
        }
        if (draft.isEditMode) {
            item {
                SectionCard(title = "值定义") {
                    Text(
                        text = "编辑已有属性时，当前仅支持修改名称和说明，值类型、值来源和值属性保持不变。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    DetailTextBlock("值类型", attributeValueTypeLabel(valueType))
                    DetailTextBlock("值来源", attributeValueSourceLabel(valueSource))
                    DetailTextBlock("值属性", buildAttributePropertySummaryForEditor(
                        valueType = valueType,
                        valueSource = valueSource,
                        numberFormat = numberFormat,
                        unitCategory = unitCategory,
                        defaultUnit = defaultUnit,
                        decimalPlacesText = decimalPlacesText,
                        allowNegative = allowNegative,
                        booleanInputMode = booleanInputMode,
                        trueLabel = trueLabel,
                        falseLabel = falseLabel,
                        optionItemsText = optionItemsText,
                        isMultiSelect = isMultiSelect,
                        systemVariableLabel = selectedSystemVariable?.displayName,
                        defaultValue = defaultValue,
                    ))
                }
            }
        } else {
            item {
                SectionCard(title = "值定义") {
                    EnumChipGroup(
                        title = "值类型",
                        options = AttributeValueType.values().toList(),
                        selected = valueType,
                        label = { attributeValueTypeLabel(it) },
                        onSelected = {
                            valueType = it
                            val currentOption = draft.systemVariableOptions.firstOrNull { option -> option.key == systemVariableKey }
                            if (currentOption != null && currentOption.valueType != it) {
                                systemVariableKey = ""
                            }
                        },
                    )
                    EnumChipGroup(
                        title = "值来源",
                        options = listOf(AttributeValueSource.INPUT, AttributeValueSource.FIXED, AttributeValueSource.SYSTEM),
                        selected = valueSource,
                        label = { attributeValueSourceLabel(it) },
                        onSelected = { valueSource = it },
                    )
                    Text(
                        text = "流程：值类型 -> 值来源 -> 值属性配置",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    when (valueType) {
                        AttributeValueType.TEXT -> {
                            Text(
                                text = "文本类型当前不配置额外值属性。",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }

                        AttributeValueType.NUMBER -> {
                            EnumChipGroup(
                                title = "数值格式",
                                options = listOf(
                                    AttributeNumberFormat.PLAIN,
                                    AttributeNumberFormat.PERCENTAGE,
                                    AttributeNumberFormat.WITH_UNIT,
                                ),
                                selected = numberFormat,
                                label = { attributeNumberFormatLabel(it) },
                                onSelected = { numberFormat = it },
                            )
                            if (numberFormat == AttributeNumberFormat.WITH_UNIT) {
                                EnumChipGroup(
                                    title = "推荐单位类型",
                                    options = suggestedUnitCategories(),
                                    selected = unitCategory,
                                    label = { it },
                                    onSelected = { unitCategory = it },
                                )
                                OutlinedTextField(
                                    value = unitCategory,
                                    onValueChange = { unitCategory = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("单位类型") },
                                    supportingText = { Text("例如：价格、面积、长度、重量") },
                                    singleLine = true,
                                )
                                OutlinedTextField(
                                    value = defaultUnit,
                                    onValueChange = { defaultUnit = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("具体单位") },
                                    supportingText = { Text("例如：CNY、m2、cm、kg") },
                                    singleLine = true,
                                )
                                val suggestedUnits = remember(unitCategory) { suggestedUnits(unitCategory) }
                                if (suggestedUnits.isNotEmpty()) {
                                    EnumChipGroup(
                                        title = "常用单位",
                                        options = suggestedUnits,
                                        selected = defaultUnit,
                                        label = { it },
                                        onSelected = { defaultUnit = it },
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = decimalPlacesText,
                                onValueChange = { decimalPlacesText = it.filter(Char::isDigit) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("小数位数") },
                                singleLine = true,
                            )
                            FilterChip(
                                selected = allowNegative,
                                onClick = { allowNegative = !allowNegative },
                                label = { Text(if (allowNegative) "允许负数" else "不允许负数") },
                            )
                        }

                        AttributeValueType.DATE -> {
                            Text(
                                text = "日期类型当前仅配置值来源，录入时统一使用日期输入框与日期选择器。",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }

                        AttributeValueType.BOOLEAN -> {
                            EnumChipGroup(
                                title = "交互方式",
                                options = listOf(AttributeInputMode.BOOLEAN_SWITCH, AttributeInputMode.TEXT_INPUT),
                                selected = booleanInputMode,
                                label = { booleanAttributeInputModeLabel(it) },
                                onSelected = { booleanInputMode = it },
                            )
                            OutlinedTextField(
                                value = trueLabel,
                                onValueChange = { trueLabel = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("真值文案") },
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = falseLabel,
                                onValueChange = { falseLabel = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("假值文案") },
                                singleLine = true,
                            )
                        }

                        AttributeValueType.SELECT -> {
                            FilterChip(
                                selected = isMultiSelect,
                                onClick = { isMultiSelect = !isMultiSelect },
                                label = { Text(if (isMultiSelect) "当前为多选" else "当前为单选") },
                            )
                            OutlinedTextField(
                                value = optionItemsText,
                                onValueChange = { optionItemsText = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("属性选项") },
                                supportingText = { Text("每行一个选项，也支持逗号分隔") },
                                minLines = 3,
                            )
                        }
                    }
                    if (valueSource == AttributeValueSource.FIXED) {
                        OutlinedTextField(
                            value = defaultValue,
                            onValueChange = { defaultValue = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("固定值") },
                            supportingText = {
                                Text(
                                    "该值将作为属性的固定结果保存，不需要用户输入。"
                                )
                            },
                        )
                    }
                    if (valueSource == AttributeValueSource.SYSTEM) {
                        Text(
                            text = "系统来源会把属性值直接绑定到 App 内部系统级变量。当前仅开放已实现变量，降级变量只展示说明，不可选用。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (selectedSystemVariable != null && selectedSystemVariable.valueType != valueType) {
                            Text(
                                text = "已选系统变量“${selectedSystemVariable.displayName}”与当前值类型不一致，请重新选择。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        if (compatibleSystemVariables.isEmpty()) {
                            Text(
                                text = "当前值类型下暂无可绑定的系统级变量。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            compatibleSystemVariables.forEach { option ->
                                SystemVariableOptionCard(
                                    option = option,
                                    isSelected = option.key == systemVariableKey,
                                    onClick = {
                                        if (option.isSelectable) {
                                            systemVariableKey = option.key
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            PrimaryActionCard(
                title = draft.saveButtonText,
                description = if (draft.isEditMode) "保存当前属性名称与说明的修改。" else "创建新的属性定义。",
                actionLabel = draft.saveButtonText,
                onClick = {
                    onSave(
                        draft.copy(
                            name = name,
                            description = description,
                            valueType = valueType,
                            valueSource = valueSource,
                            numberFormat = numberFormat,
                            unitCategory = unitCategory,
                            defaultUnit = defaultUnit,
                            decimalPlacesText = decimalPlacesText,
                            allowNegative = allowNegative,
                            booleanInputMode = booleanInputMode,
                            trueLabel = trueLabel,
                            falseLabel = falseLabel,
                            optionItemsText = optionItemsText,
                            isMultiSelect = isMultiSelect,
                            systemVariableKey = systemVariableKey,
                            defaultValue = defaultValue,
                            ruleCandidates = ruleCandidates,
                            pendingCreatedAttributes = pendingCreatedAttributes,
                        )
                    )
                },
            )
        }
    }

    pendingRemoveBinding?.let { candidate ->
        AlertDialog(
            onDismissRequest = { pendingRemoveBinding = null },
            title = { Text("移除规则绑定") },
            text = {
                Text(
                    "将移除“${candidate.ruleName}”绑定。\n" +
                        "依赖属性：${summarizeBindingDependencies(candidate)}\n" +
                        "输出影响：${summarizeBindingOutputs(candidate)}\n" +
                        "快捷创建：${summarizeBindingQuickCreatedAttributes(candidate)}\n" +
                        "移除后这些槽位关联会一并解除。"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updatedCandidates = ruleCandidates.map {
                            if (it.ruleId == candidate.ruleId) {
                                it.copy(
                                    selectedEntrySlotKey = "",
                                    slotBindings = emptyList(),
                                    isComplete = false,
                                )
                            } else {
                                it
                            }
                        }
                        ruleCandidates = updatedCandidates
                        val remainingQuickCreatedIds = updatedCandidates
                            .flatMap { binding -> binding.slotBindings }
                            .filter { it.isQuickCreatedAttribute }
                            .mapNotNull { it.attributeId }
                            .toSet()
                        pendingCreatedAttributes = pendingCreatedAttributes.filter { pending ->
                            pending.id in remainingQuickCreatedIds
                        }
                        pendingRemoveBinding = null
                    }
                ) {
                    Text("移除")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemoveBinding = null }) {
                    Text("取消")
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RuleBindingWizardPage(
    draft: RuleBindingWizardDraftUiModel,
    onSave: (RuleBindingWizardDraftUiModel) -> Unit,
) {
    var selectedRuleId by rememberSaveable(draft.attributeName, draft.selectedRuleId) {
        mutableStateOf(draft.selectedRuleId)
    }
    var selectedEntrySlotKey by rememberSaveable(draft.attributeName, draft.selectedEntrySlotKey) {
        mutableStateOf(draft.selectedEntrySlotKey)
    }
    var slotBindings by remember(draft.attributeName, draft.selectedRuleId, draft.selectedEntrySlotKey) {
        mutableStateOf(draft.slotBindings)
    }
    val selectedRule = draft.compatibleRules.firstOrNull { it.ruleId == selectedRuleId }
    val selectedEntrySlot = selectedRule?.allSlots?.firstOrNull { it.key == selectedEntrySlotKey }

    fun switchRule(ruleId: String) {
        val candidate = draft.compatibleRules.firstOrNull { it.ruleId == ruleId } ?: return
        selectedRuleId = ruleId
        selectedEntrySlotKey = candidate.entrySlotOptions.firstOrNull()?.key
        slotBindings = candidate.defaultSlotBindings
    }

    fun switchEntrySlot(slotKey: String) {
        selectedEntrySlotKey = slotKey
        slotBindings = slotBindings.map { slot ->
            if (slot.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT) {
                when (slot.slotKey) {
                    slotKey -> slot.copy(
                        attributeId = draft.attributeDraft.id,
                        attributeName = draft.attributeName,
                    )
                    else -> {
                        if (slot.attributeName == draft.attributeName && slot.attributeId == draft.attributeDraft.id) {
                            slot.copy(attributeId = null, attributeName = "")
                        } else {
                            slot
                        }
                    }
                }
            } else {
                slot
            }
        }
    }

    fun updateSlot(slotKey: String, transform: (RuleBindingSlotDraftUiModel) -> RuleBindingSlotDraftUiModel) {
        slotBindings = slotBindings.map { slot ->
            if (slot.slotKey == slotKey) transform(slot) else slot
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 32.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            DetailSummaryCard(
                icon = "rule",
                title = draft.attributeName,
                summary = "为当前属性配置规则绑定实例",
            )
        }
        item {
            SectionCard(title = "Step 1 · 选择规则") {
                if (draft.compatibleRules.isEmpty()) {
                    Text("当前没有兼容该属性值类型的规则。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    draft.compatibleRules.forEach { candidate ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { switchRule(candidate.ruleId) }
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                FilterChip(
                                    selected = candidate.ruleId == selectedRuleId,
                                    onClick = { switchRule(candidate.ruleId) },
                                    label = { Text("${candidate.ruleName} · ${candidate.ruleTypeLabel}") },
                                )
                                Text(candidate.triggerSummary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("输入：${candidate.inputSlotSummary}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("输出：${candidate.outputSlotSummary}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    candidate.allSlots.forEach { slot ->
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = if (slot.isCompatibleWithCurrentAttribute) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                            },
                                        ) {
                                            Text(
                                                text = slot.name,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (slot.isCompatibleWithCurrentAttribute) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                            )
                                        }
                                    }
                                }
                                val compatibleSlotNames = candidate.allSlots
                                    .filter { it.isCompatibleWithCurrentAttribute }
                                    .joinToString("、") { it.name }
                                Text(
                                    text = if (compatibleSlotNames.isBlank()) {
                                        "当前属性与该规则没有可直接匹配的属性输入槽位。"
                                    } else {
                                        "已高亮当前属性可进入的槽位：$compatibleSlotNames"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            SectionCard(title = "Step 2 · 入口槽位") {
                if (selectedRule == null) {
                    Text("请先选择规则。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else if (selectedRule.entrySlotOptions.isEmpty()) {
                    Text("该规则当前没有可作为入口的属性输入槽位。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(
                        text = "已高亮槽位均可直接承载当前属性，选择后系统会自动把“${draft.attributeName}”放入该入口槽位。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    EnumChipGroup(
                        title = "当前属性可占据的槽位",
                        options = selectedRule.entrySlotOptions,
                        selected = selectedRule.entrySlotOptions.firstOrNull { it.key == selectedEntrySlotKey }
                            ?: selectedRule.entrySlotOptions.first(),
                        label = { "${it.name} · ${it.valueTypeLabel}" },
                        onSelected = { switchEntrySlot(it.key) },
                    )
                }
            }
        }
        item {
            SectionCard(title = "Step 3 · 补齐剩余槽位") {
                Text(
                    text = "编辑已有绑定时，替换属性输入槽位会同步变更依赖；调整属性输出槽位会改变该规则写入的属性范围。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val remainingSlots = slotBindings.filter { it.slotKey != selectedEntrySlotKey }
                if (remainingSlots.isEmpty()) {
                    Text("当前没有需要补齐的剩余槽位。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    remainingSlots.forEach { slot ->
                        RuleBindingSlotEditorCard(
                            slot = slot,
                            systemVariableOptions = draft.attributeDraft.systemVariableOptions,
                            onUpdateSlot = { transform -> updateSlot(slot.slotKey, transform) },
                        )
                    }
                }
            }
        }
        item {
            SectionCard(title = "Step 4 · 预览与保存") {
                DetailLine("当前属性", draft.attributeName)
                DetailLine("当前规则", selectedRule?.ruleName ?: "未选择")
                DetailLine("入口槽位", selectedEntrySlot?.name ?: selectedEntrySlotKey ?: "未选择")
                DetailLine("依赖属性", summarizeBindingDependencies(slotBindings, selectedEntrySlotKey))
                DetailLine("输出影响", summarizeBindingOutputs(slotBindings))
                DetailLine("快捷创建", summarizeBindingQuickCreatedAttributes(slotBindings, selectedEntrySlotKey))
                DetailLine(
                    "完整性",
                    if (slotBindings.all { !it.isRequired || isRuleBindingSlotDraftSatisfied(it) }) "已满足保存条件" else "仍有必填槽位未配置"
                )
            }
        }
        item {
            PrimaryActionCard(
                title = draft.saveButtonText,
                description = "保存后会回到属性编辑页，并把该规则绑定实例写入当前属性草稿。",
                actionLabel = draft.saveButtonText,
                onClick = {
                    onSave(
                        draft.copy(
                            selectedRuleId = selectedRuleId,
                            selectedEntrySlotKey = selectedEntrySlotKey,
                            slotBindings = slotBindings,
                        )
                    )
                },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RuleBindingSlotEditorCard(
    slot: RuleBindingSlotDraftUiModel,
    systemVariableOptions: List<SystemVariableOptionUiModel>,
    onUpdateSlot: ((RuleBindingSlotDraftUiModel) -> RuleBindingSlotDraftUiModel) -> Unit,
) {
    val compatibleSystemVariables = systemVariableOptions.filter { option ->
        ruleSlotMatchesSystemValueType(slot.valueType, option.valueType)
    }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "${slot.slotName} · ${ruleSlotSourceTypeLabel(slot.sourceType)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "${ruleSlotValueTypeLabel(slot.valueType)} · ${if (slot.isRequired) "必填" else "可选"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            when (slot.sourceType) {
                RuleSlotSourceType.ATTRIBUTE_INPUT,
                RuleSlotSourceType.ATTRIBUTE_OUTPUT -> {
                    OutlinedTextField(
                        value = slot.attributeName,
                        onValueChange = { value ->
                            onUpdateSlot {
                                current -> current.copy(
                                    attributeName = value,
                                    attributeId = null,
                                    isQuickCreatedAttribute = false,
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(if (slot.direction == RuleSlotDirection.INPUT) "绑定属性" else "输出属性") },
                        supportingText = {
                            Text(
                                if (slot.isQuickCreatedAttribute) {
                                    "该名称会在保存绑定时自动补齐为新属性。"
                                } else if (slot.allowQuickCreateAttribute) {
                                    "支持直接输入新名字，保存绑定时会自动创建并补齐该属性。"
                                } else {
                                    "可输入已有属性名完成绑定。"
                                }
                            )
                        },
                        singleLine = true,
                    )
                    if (slot.attributeOptions.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            slot.attributeOptions.take(8).forEach { option ->
                                FilterChip(
                                    selected = option.name == slot.attributeName,
                                    onClick = {
                                        onUpdateSlot { current ->
                                            current.copy(
                                                attributeId = option.id,
                                                attributeName = option.name,
                                                isQuickCreatedAttribute = false,
                                            )
                                        }
                                    },
                                    label = { Text(option.name) },
                                )
                            }
                        }
                    }
                }
                RuleSlotSourceType.CONFIG_INPUT -> {
                    OutlinedTextField(
                        value = slot.configValue,
                        onValueChange = { value -> onUpdateSlot { current -> current.copy(configValue = value) } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("配置值") },
                        singleLine = true,
                    )
                }
                RuleSlotSourceType.SYSTEM_INPUT,
                RuleSlotSourceType.SYSTEM_OUTPUT -> {
                    if (compatibleSystemVariables.isEmpty()) {
                        Text("当前没有可选系统变量。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        compatibleSystemVariables.forEach { option ->
                            SystemVariableOptionCard(
                                option = option,
                                isSelected = option.key == slot.systemVariableKey,
                                onClick = {
                                    if (option.isSelectable) {
                                        onUpdateSlot { current -> current.copy(systemVariableKey = option.key) }
                                    }
                                },
                            )
                        }
                    }
                }
                RuleSlotSourceType.READONLY_OUTPUT -> {
                    Text("该槽位为只读输出，无需配置落点。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (slot.direction == RuleSlotDirection.OUTPUT) {
                Text(
                    text = "更新方式：${ruleOutputUpdateModeLabel(slot.outputUpdateMode)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RuleSlotSummaryCard(
    slot: RuleSlotSummaryUiModel,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "${slot.name} · ${slot.requiredLabel}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "${slot.key} · ${slot.valueTypeLabel} · ${slot.sourceTypeLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (slot.description.isNotBlank()) {
                Text(
                    text = slot.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun summarizeBindingDependencies(
    candidate: RuleBindingCandidateUiModel,
): String = summarizeBindingDependencies(candidate.slotBindings, candidate.selectedEntrySlotKey)

private fun summarizeBindingDependencies(
    slotBindings: List<RuleBindingSlotDraftUiModel>,
    selectedEntrySlotKey: String?,
): String {
    return slotBindings
        .filter { slot ->
            slot.direction == RuleSlotDirection.INPUT &&
                slot.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT &&
                slot.slotKey != selectedEntrySlotKey &&
                slot.attributeName.isNotBlank()
        }
        .joinToString(" / ") { slot -> "${slot.slotName} -> ${slot.attributeName}" }
        .ifBlank { "无额外依赖属性" }
}

private fun summarizeBindingOutputs(
    candidate: RuleBindingCandidateUiModel,
): String = summarizeBindingOutputs(candidate.slotBindings)

private fun summarizeBindingOutputs(
    slotBindings: List<RuleBindingSlotDraftUiModel>,
): String {
    return slotBindings
        .filter { slot ->
            slot.direction == RuleSlotDirection.OUTPUT &&
                slot.sourceType == RuleSlotSourceType.ATTRIBUTE_OUTPUT &&
                slot.attributeName.isNotBlank()
        }
        .joinToString(" / ") { slot ->
            "${slot.slotName} -> ${slot.attributeName}（${ruleOutputUpdateModeLabel(slot.outputUpdateMode)}）"
        }
        .ifBlank { "无属性输出落点" }
}

private fun summarizeBindingQuickCreatedAttributes(
    candidate: RuleBindingCandidateUiModel,
): String = summarizeBindingQuickCreatedAttributes(candidate.slotBindings, candidate.selectedEntrySlotKey)

private fun summarizeBindingQuickCreatedAttributes(
    slotBindings: List<RuleBindingSlotDraftUiModel>,
    selectedEntrySlotKey: String?,
): String {
    return slotBindings
        .filter { slot ->
            slot.isQuickCreatedAttribute &&
                slot.slotKey != selectedEntrySlotKey &&
                slot.attributeName.isNotBlank()
        }
        .joinToString(" / ") { slot -> "${slot.attributeName}（来自 ${slot.slotName}）" }
        .ifBlank { "无" }
}

private fun isRuleBindingSlotDraftSatisfied(
    slotDraft: RuleBindingSlotDraftUiModel,
): Boolean {
    return when (slotDraft.sourceType) {
        RuleSlotSourceType.ATTRIBUTE_INPUT,
        RuleSlotSourceType.ATTRIBUTE_OUTPUT -> slotDraft.attributeName.isNotBlank()
        RuleSlotSourceType.CONFIG_INPUT -> slotDraft.configValue.isNotBlank()
        RuleSlotSourceType.SYSTEM_INPUT,
        RuleSlotSourceType.SYSTEM_OUTPUT -> slotDraft.systemVariableKey.isNotBlank()
        RuleSlotSourceType.READONLY_OUTPUT -> true
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RuleEditorPage(
    draft: RuleEditorDraftUiModel,
    onSave: (RuleEditorDraftUiModel) -> Unit,
) {
    var name by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.name) }
    var description by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.description) }
    var computationType by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.computationType) }
    var activationMode by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.activationMode) }
    var toggleLabelWhenEnabled by rememberSaveable(draft.id, draft.templateId) {
        mutableStateOf(draft.toggleLabelWhenEnabled)
    }
    var toggleLabelWhenDisabled by rememberSaveable(draft.id, draft.templateId) {
        mutableStateOf(draft.toggleLabelWhenDisabled)
    }
    var toggleAnchorSlotKey by rememberSaveable(draft.id, draft.templateId) {
        mutableStateOf(draft.toggleAnchorSlotKey)
    }
    var toggleDefaultEnabled by rememberSaveable(draft.id, draft.templateId) {
        mutableStateOf(draft.toggleDefaultEnabled)
    }
    var triggerModes by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.triggerModes) }
    var expression by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.expression) }
    var slots by remember(draft.id, draft.templateId) {
        mutableStateOf(
            draft.slots.ifEmpty {
                listOf(
                    newEditorRuleSlotDraft(RuleSlotDirection.INPUT),
                    newEditorRuleSlotDraft(RuleSlotDirection.OUTPUT),
                )
            }
        )
    }

    fun updateSlot(slotId: String, transform: (RuleSlotDraftUiModel) -> RuleSlotDraftUiModel) {
        slots = slots.map { slot ->
            if (slot.id == slotId) transform(slot) else slot
        }
    }

    fun addSlot(direction: RuleSlotDirection) {
        slots = slots + newEditorRuleSlotDraft(direction)
    }

    fun removeSlot(slotId: String) {
        val current = slots
        if (current.size <= 1) return
        slots = current.filterNot { it.id == slotId }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 32.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            DetailSummaryCard(
                icon = "rule",
                title = if (name.isBlank()) "未命名规则" else name,
                summary = draft.templateName?.let { "基于 $it" } ?: "自定义规则定义",
            )
        }
        item {
            SectionCard(title = "基本信息") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("规则名称") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("说明") },
                    minLines = 2,
                )
                EnumChipGroup(
                    title = "规则类型",
                    options = RuleComputationType.values().toList(),
                    selected = computationType,
                    label = { ruleComputationTypeLabel(it) },
                    onSelected = { computationType = it },
                )
                Text(
                    text = "触发方式可多选，决定规则在表单中的自动运行时机。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RuleTriggerMode.entries.forEach { mode: RuleTriggerMode ->
                        FilterChip(
                            selected = mode in triggerModes,
                            onClick = {
                                val nextModes: List<RuleTriggerMode> = if (mode in triggerModes) {
                                    triggerModes.filterNot { it == mode }
                                } else {
                                    triggerModes + mode
                                }
                                triggerModes = nextModes
                            },
                            label = { Text(ruleTriggerModeLabel(mode)) },
                        )
                    }
                }
                if (!draft.templateName.isNullOrBlank()) {
                    DetailLine("来源模板", draft.templateName)
                }
            }
        }
        item {
            SectionCard(title = "槽位定义") {
                Text(
                    text = "槽位就是表达式里的变量。先定义输入/输出槽位，再约束它们来自属性、配置或系统变量。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                RuleSlotSection(
                    title = "输入槽位",
                    description = "用于接收属性输入、配置输入或系统输入。",
                    slots = slots.filter { it.direction == RuleSlotDirection.INPUT },
                    systemVariableOptions = draft.systemVariableOptions,
                    onAddSlot = { addSlot(RuleSlotDirection.INPUT) },
                    onUpdateSlot = ::updateSlot,
                    onRemoveSlot = ::removeSlot,
                )
                RuleSlotSection(
                    title = "输出槽位",
                    description = "用于声明规则写回目标，可写回属性、只读结果或系统变量。",
                    slots = slots.filter { it.direction == RuleSlotDirection.OUTPUT },
                    systemVariableOptions = draft.systemVariableOptions,
                    onAddSlot = { addSlot(RuleSlotDirection.OUTPUT) },
                    onUpdateSlot = ::updateSlot,
                    onRemoveSlot = ::removeSlot,
                )
            }
        }
        item {
            SectionCard(title = "计算说明") {
                OutlinedTextField(
                    value = expression,
                    onValueChange = { expression = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("表达式") },
                    supportingText = {
                        Text("已接入基础表达式入口，支持变量、四则、比较、逻辑、if(cond,a,b)、coalesce、dateDiff。")
                    },
                    minLines = 4,
                )
            }
        }
        item {
            PrimaryActionCard(
                title = draft.saveButtonText,
                description = if (draft.isEditMode) "保存当前规则定义的修改。" else "创建新的规则定义。",
                actionLabel = draft.saveButtonText,
                onClick = {
                    onSave(
                        draft.copy(
                            name = name,
                            description = description,
                            computationType = computationType,
                            triggerModes = triggerModes,
                            slots = slots,
                            expression = expression,
                        )
                    )
                },
            )
        }
    }
}

@Composable
private fun RuleSlotSection(
    title: String,
    description: String,
    slots: List<RuleSlotDraftUiModel>,
    systemVariableOptions: List<SystemVariableOptionUiModel>,
    onAddSlot: () -> Unit,
    onUpdateSlot: (String, (RuleSlotDraftUiModel) -> RuleSlotDraftUiModel) -> Unit,
    onRemoveSlot: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (slots.isEmpty()) {
            Text(
                text = "当前还没有槽位，先添加一个。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            slots.forEachIndexed { index, slot ->
                RuleSlotEditorCard(
                    slot = slot,
                    index = index,
                    systemVariableOptions = systemVariableOptions,
                    onUpdateSlot = { transform -> onUpdateSlot(slot.id, transform) },
                    onRemove = { onRemoveSlot(slot.id) },
                )
            }
        }
        TextButton(onClick = onAddSlot) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("添加${title.removeSuffix("槽位")}")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RuleSlotEditorCard(
    slot: RuleSlotDraftUiModel,
    index: Int,
    systemVariableOptions: List<SystemVariableOptionUiModel>,
    onUpdateSlot: ((RuleSlotDraftUiModel) -> RuleSlotDraftUiModel) -> Unit,
    onRemove: () -> Unit,
) {
    val compatibleSystemVariables = systemVariableOptions.filter { option ->
        ruleSlotMatchesSystemValueType(slot.valueType, option.valueType)
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${ruleSlotDirectionLabel(slot.direction)}槽位 ${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
                TextButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("删除")
                }
            }
            EnumChipGroup(
                title = "槽位方向",
                options = RuleSlotDirection.entries.toList(),
                selected = slot.direction,
                label = { ruleSlotDirectionLabel(it) },
                onSelected = { direction ->
                    onUpdateSlot {
                        it.copy(
                            direction = direction,
                            sourceType = defaultSourceTypeForDirection(direction),
                            isRequired = if (direction == RuleSlotDirection.INPUT) true else it.isRequired,
                            outputTargetType = defaultOutputTargetTypeForDirection(direction),
                        )
                    }
                },
            )
            OutlinedTextField(
                value = slot.key,
                onValueChange = { value -> onUpdateSlot { it.copy(key = value) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("槽位 key") },
                supportingText = { Text("表达式中使用的变量名，例如 amount / currentDate") },
                singleLine = true,
            )
            OutlinedTextField(
                value = slot.name,
                onValueChange = { value -> onUpdateSlot { it.copy(name = value) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("显示名称") },
                supportingText = { Text("用于绑定和详情页展示，例如 金额 / 当前日期") },
                singleLine = true,
            )
            EnumChipGroup(
                title = "值类型",
                options = RuleSlotValueType.entries.toList(),
                selected = slot.valueType,
                label = { ruleSlotValueTypeLabel(it) },
                onSelected = { valueType ->
                    onUpdateSlot { current ->
                        val nextCompatibleSystemVariables = systemVariableOptions.filter { option ->
                            ruleSlotMatchesSystemValueType(valueType, option.valueType)
                        }
                        val nextSystemVariable = current.systemVariableKey.takeIf { key ->
                            nextCompatibleSystemVariables.any { option -> option.key == key }
                        }.orEmpty()
                        current.copy(
                            valueType = valueType,
                            systemVariableKey = nextSystemVariable,
                        )
                    }
                },
            )
            EnumChipGroup(
                title = "来源类型",
                options = sourceTypeOptionsForDirection(slot.direction),
                selected = slot.sourceType,
                label = { ruleSlotSourceTypeLabel(it) },
                onSelected = { sourceType ->
                    onUpdateSlot {
                        it.copy(
                            sourceType = sourceType,
                            isRequired = if (
                                slot.direction == RuleSlotDirection.INPUT &&
                                sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT
                            ) {
                                true
                            } else {
                                it.isRequired
                            },
                            outputTargetType = defaultOutputTargetType(sourceType),
                            systemVariableKey = if (
                                sourceType == RuleSlotSourceType.SYSTEM_INPUT ||
                                sourceType == RuleSlotSourceType.SYSTEM_OUTPUT
                            ) {
                                it.systemVariableKey
                            } else {
                                ""
                            },
                        )
                    }
                },
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (slot.direction == RuleSlotDirection.INPUT &&
                    slot.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT
                ) {
                    FilterChip(
                        selected = true,
                        onClick = {},
                        enabled = false,
                        label = { Text("属性输入固定必填") },
                    )
                } else {
                    FilterChip(
                        selected = slot.isRequired,
                        onClick = { onUpdateSlot { it.copy(isRequired = !it.isRequired) } },
                        label = { Text(if (slot.isRequired) "当前必填" else "当前可选") },
                    )
                }
                if (slot.direction == RuleSlotDirection.INPUT &&
                    slot.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT
                ) {
                    FilterChip(
                        selected = slot.allowQuickCreateAttribute,
                        onClick = {
                            onUpdateSlot {
                                it.copy(allowQuickCreateAttribute = !it.allowQuickCreateAttribute)
                            }
                        },
                        label = {
                            Text(
                                if (slot.allowQuickCreateAttribute) {
                                    "允许快捷建属性"
                                } else {
                                    "不允许快捷建属性"
                                }
                            )
                        },
                    )
                }
            }
            if (slot.direction == RuleSlotDirection.OUTPUT) {
                EnumChipGroup(
                    title = "输出目标",
                    options = outputTargetOptionsForSourceType(slot.sourceType),
                    selected = slot.outputTargetType,
                    label = { ruleOutputTargetTypeLabel(it) },
                    onSelected = { targetType ->
                        onUpdateSlot { current -> current.copy(outputTargetType = targetType) }
                    },
                )
                EnumChipGroup(
                    title = "更新方式",
                    options = RuleOutputUpdateMode.entries.toList(),
                    selected = slot.outputUpdateMode,
                    label = { ruleOutputUpdateModeLabel(it) },
                    onSelected = { mode ->
                        onUpdateSlot { current -> current.copy(outputUpdateMode = mode) }
                    },
                )
            }
            if (
                slot.sourceType == RuleSlotSourceType.SYSTEM_INPUT ||
                slot.sourceType == RuleSlotSourceType.SYSTEM_OUTPUT
            ) {
                if (compatibleSystemVariables.isEmpty()) {
                    Text(
                        text = "当前值类型下没有可选系统变量。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    compatibleSystemVariables.forEach { option ->
                        SystemVariableOptionCard(
                            option = option,
                            isSelected = option.key == slot.systemVariableKey,
                            onClick = {
                                if (option.isSelectable) {
                                    onUpdateSlot { current ->
                                        current.copy(systemVariableKey = option.key)
                                    }
                                }
                            },
                        )
                    }
                }
            }
            OutlinedTextField(
                value = slot.description,
                onValueChange = { value -> onUpdateSlot { it.copy(description = value) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("槽位说明") },
                minLines = 2,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RuleWorkbenchPage(
    draft: RuleEditorDraftUiModel,
    onSave: (RuleEditorDraftUiModel) -> Unit,
) {
    var name by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.name) }
    var description by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.description) }
    var computationType by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.computationType) }
    var activationMode by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(draft.activationMode) }
    var toggleLabelWhenEnabled by rememberSaveable(draft.id, draft.templateId) {
        mutableStateOf(draft.toggleLabelWhenEnabled)
    }
    var toggleLabelWhenDisabled by rememberSaveable(draft.id, draft.templateId) {
        mutableStateOf(draft.toggleLabelWhenDisabled)
    }
    var toggleAnchorSlotKey by rememberSaveable(draft.id, draft.templateId) {
        mutableStateOf(draft.toggleAnchorSlotKey)
    }
    var toggleDefaultEnabled by rememberSaveable(draft.id, draft.templateId) {
        mutableStateOf(draft.toggleDefaultEnabled)
    }
    var isScheduledEnabled by rememberSaveable(draft.id, draft.templateId) {
        mutableStateOf(draft.isScheduledEnabled)
    }
    var scheduleTimeSourceType by rememberSaveable(draft.id, draft.templateId) {
        mutableStateOf(draft.scheduleTimeSourceType)
    }
    var scheduleFixedValue by rememberSaveable(draft.id, draft.templateId) {
        mutableStateOf(draft.scheduleFixedValue)
    }
    var scheduleSlotKeys by rememberSaveable(draft.id, draft.templateId) {
        mutableStateOf(draft.scheduleSlotKeys)
    }
    var scheduleDescription by rememberSaveable(draft.id, draft.templateId) {
        mutableStateOf(draft.scheduleDescription)
    }
    var generateCalendarEvent by rememberSaveable(draft.id, draft.templateId) {
        mutableStateOf(draft.generateCalendarEvent)
    }
    var scheduleEventTitleTemplate by rememberSaveable(draft.id, draft.templateId) {
        mutableStateOf(draft.scheduleEventTitleTemplate)
    }
    var scheduleEventDateSlotKey by rememberSaveable(draft.id, draft.templateId) {
        mutableStateOf(draft.scheduleEventDateSlotKey)
    }
    var scheduleEventDedupeKeyStrategy by rememberSaveable(draft.id, draft.templateId) {
        mutableStateOf(draft.scheduleEventDedupeKeyStrategy)
    }

    val initialSlots = remember(draft.id, draft.templateId) {
        ensureRuleWorkbenchOperands(
            expression = draft.expression,
            existingOperands = draft.slots,
        )
    }
    var slots by remember(draft.id, draft.templateId) { mutableStateOf(initialSlots) }
    var expressionField by rememberSaveable(
        draft.id,
        draft.templateId,
        stateSaver = TextFieldValue.Saver,
    ) {
        mutableStateOf(
            TextFieldValue(
                text = draft.expression,
                selection = TextRange(draft.expression.length),
            )
        )
    }
    var canvasState by remember(draft.id, draft.templateId) {
        mutableStateOf(
            buildRuleWorkbenchCanvasState(
                expression = draft.expression,
                slots = initialSlots,
                canvasDefinition = draft.canvasDefinition,
            )
        )
    }
    var isCanvasFullscreen by rememberSaveable(draft.id, draft.templateId) { mutableStateOf(false) }
    var showOperandConfigDialog by remember { mutableStateOf<String?>(null) }

    val inputSlots = slots.filter { it.direction == RuleSlotDirection.INPUT }
    val outputSlots = slots.filter { it.direction == RuleSlotDirection.OUTPUT }
    val attributeInputSlots = inputSlots.filter { it.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT }
    val toggleAnchorOptions = listOf("" to "仅在规则卡片显示") + attributeInputSlots.map {
        it.key to it.key
    }
    val scheduleSlotOptions = inputSlots.map { it.key to it.key }
    val scheduleEventDateSlotOptions = slots.map { it.key to it.key }

    LaunchedEffect(scheduleSlotOptions) {
        val validKeys = scheduleSlotOptions.map { it.first }.toSet()
        scheduleSlotKeys = scheduleSlotKeys.filter { it in validKeys }
    }

    fun syncExpressionFromCanvas(nodes: List<CanvasNode>) {
        val positionedNodes = ensureRuleWorkbenchNodePositions(nodes)
        val nextExpression = buildRuleWorkbenchExpression(positionedNodes)
        expressionField = TextFieldValue(
            text = nextExpression,
            selection = TextRange(nextExpression.length),
        )
        canvasState = canvasState.copy(
            nodes = positionedNodes,
            connections = buildRuleWorkbenchConnections(positionedNodes),
            selectedNodeId = canvasState.selectedNodeId.takeIf { selectedId ->
                positionedNodes.any { it.id == selectedId }
            },
        )
    }

    fun syncCanvasFromExpression(text: String) {
        val nextSlots = ensureRuleWorkbenchOperands(
            expression = text,
            existingOperands = slots,
            retainUnreferenced = false,
        )
        if (nextSlots != slots) {
            slots = nextSlots
        }
        val rebuiltState = buildRuleWorkbenchCanvasState(
            expression = text,
            slots = nextSlots,
            selectedNodeId = canvasState.selectedNodeId,
        )
        canvasState = rebuiltState.copy(
            scale = canvasState.scale,
            offset = canvasState.offset,
        )
    }

    fun updateSlot(slotId: String, transform: (RuleSlotDraftUiModel) -> RuleSlotDraftUiModel) {
        slots = slots.map { slot ->
            if (slot.id == slotId) transform(slot) else slot
        }
        val updatedSlot = slots.firstOrNull { it.id == slotId }
        val updatedDisplayText = updatedSlot?.let { slot ->
            slot.key
        }
        val updatedNodes = canvasState.nodes.map { node ->
            if (node.operandConfig?.id == slotId) {
                node.copy(
                    operandConfig = updatedSlot,
                    displayText = updatedDisplayText,
                )
            } else {
                node
            }
        }
        syncExpressionFromCanvas(updatedNodes)
    }

    fun addOperand() {
        val nextSlot = createRuleWorkbenchOperandDraft(
            existingSlots = slots,
        )
        slots = slots + nextSlot
        val newNode = nextSlot.toOperandCanvasNode(nodeId = "operand_${canvasState.nodes.size}_${nextSlot.id}")
        syncExpressionFromCanvas(canvasState.nodes + newNode)
    }

    fun removeOperand(slotId: String) {
        val current = slots
        slots = current.filterNot { it.id == slotId }
        syncExpressionFromCanvas(canvasState.nodes.filterNot { it.operandConfig?.id == slotId })
    }

    fun updateOperandInCanvas(slotId: String, updatedSlot: RuleSlotDraftUiModel) {
        val previousKey = slots.firstOrNull { it.id == slotId }?.key
        slots = slots.map { if (it.id == slotId) updatedSlot else it }
        if (!previousKey.isNullOrBlank() && previousKey != updatedSlot.key) {
            if (toggleAnchorSlotKey == previousKey) toggleAnchorSlotKey = updatedSlot.key
            scheduleSlotKeys = scheduleSlotKeys.map { if (it == previousKey) updatedSlot.key else it }
            if (scheduleEventDateSlotKey == previousKey) scheduleEventDateSlotKey = updatedSlot.key
        }
        syncExpressionFromCanvas(
            canvasState.nodes.map { node ->
                if (node.operandConfig?.id == slotId) {
                    node.copy(
                        operandConfig = updatedSlot,
                        displayText = updatedSlot.key,
                    )
                } else {
                    node
                }
            }
        )
    }

    fun deleteCanvasNode(nodeId: String) {
        val node = canvasState.nodes.firstOrNull { it.id == nodeId }
        val remainingNodes = canvasState.nodes.filterNot { it.id == nodeId }
        node?.operandConfig?.id?.let { operandId ->
            if (remainingNodes.none { it.operandConfig?.id == operandId }) {
                slots = slots.filterNot { it.id == operandId }
            }
        }
        syncExpressionFromCanvas(remainingNodes)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 32.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            DetailSummaryCard(
                icon = "rule",
                title = if (name.isBlank()) "未命名规则" else name,
                summary = buildString {
                    append("自定义规则")
                    append(" · ")
                    append("${inputSlots.size} 个输入操作数")
                    append(" / ")
                    append("${outputSlots.size} 个输出操作数")
                },
            )
        }
        item {
            SectionCard(title = "基本信息") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("规则名称") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("规则说明") },
                    minLines = 2,
                )
            }
        }
        item {
            SectionCard(title = "启用控制") {
                FilterChip(
                    selected = activationMode == RuleActivationMode.USER_TOGGLE,
                    onClick = {
                        activationMode = if (activationMode == RuleActivationMode.USER_TOGGLE) {
                            RuleActivationMode.ALWAYS_ON
                        } else {
                            RuleActivationMode.USER_TOGGLE
                        }
                    },
                    label = {
                        Text(
                            if (activationMode == RuleActivationMode.USER_TOGGLE) {
                                "允许用户在物品页控制此规则"
                            } else {
                                "默认不向物品页暴露规则开关"
                            }
                        )
                    },
                )
                Text(
                    text = if (activationMode == RuleActivationMode.ALWAYS_ON) {
                        "当前规则启用后会默认参与计算，但不会在物品页属性下生成启停开关。"
                    } else {
                        "适合“计入总价值 / 不计入总价值”“自动续费 / 暂停续费”这类业务动作。规则定义负责文案与默认状态，物品规则实例负责当前启停。"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (activationMode == RuleActivationMode.USER_TOGGLE) {
                    OutlinedTextField(
                        value = toggleLabelWhenEnabled,
                        onValueChange = { toggleLabelWhenEnabled = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("启用态文案") },
                        supportingText = { Text("例如：计入总价值") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = toggleLabelWhenDisabled,
                        onValueChange = { toggleLabelWhenDisabled = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("停用态文案") },
                        supportingText = { Text("例如：不计入总价值") },
                        singleLine = true,
                    )
                    EnumChipGroup(
                        title = "开关显示位置",
                        options = toggleAnchorOptions,
                        selected = toggleAnchorOptions.firstOrNull { it.first == toggleAnchorSlotKey }
                            ?: toggleAnchorOptions.first(),
                        label = { it.second },
                        onSelected = { option -> toggleAnchorSlotKey = option.first },
                    )
                    FilterChip(
                        selected = toggleDefaultEnabled,
                        onClick = { toggleDefaultEnabled = !toggleDefaultEnabled },
                        label = { Text(if (toggleDefaultEnabled) "默认启用" else "默认停用") },
                    )
                    if (attributeInputSlots.isEmpty()) {
                        Text(
                            text = "当前还没有来自属性的输入操作数，所以开关暂时只会显示在规则卡片里。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        item {
            SectionCard(title = "周期调度") {
                Text(
                    text = "启用后，规则会生成调度事件，并由后续运行器按日历规则执行。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FilterChip(
                    selected = isScheduledEnabled,
                    onClick = { isScheduledEnabled = !isScheduledEnabled },
                    label = { Text(if (isScheduledEnabled) "已启用周期调度" else "未启用周期调度") },
                )
                if (isScheduledEnabled) {
                    EnumChipGroup(
                        title = "调度时间来源",
                        options = RuleScheduleTimeSourceType.entries.toList(),
                        selected = scheduleTimeSourceType,
                        label = {
                            when (it) {
                                RuleScheduleTimeSourceType.FIXED -> "固定值"
                                RuleScheduleTimeSourceType.SLOT -> "来自操作数"
                            }
                        },
                        onSelected = { scheduleTimeSourceType = it },
                    )
                    when (scheduleTimeSourceType) {
                        RuleScheduleTimeSourceType.FIXED -> {
                            OutlinedTextField(
                                value = scheduleFixedValue,
                                onValueChange = { scheduleFixedValue = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("固定调度值") },
                                supportingText = { Text("例如：每月1日 / 每周一 / 09:00") },
                                singleLine = true,
                            )
                        }
                        RuleScheduleTimeSourceType.SLOT -> {
                            if (scheduleSlotOptions.isEmpty()) {
                                Text(
                                    text = "当前还没有输入操作数，暂时无法读取调度时间。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                Text(
                                    text = "选择哪些输入操作数用于推导周期调度时间。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    scheduleSlotOptions.forEach { option ->
                                        FilterChip(
                                            selected = option.first in scheduleSlotKeys,
                                            onClick = {
                                                scheduleSlotKeys = if (option.first in scheduleSlotKeys) {
                                                    scheduleSlotKeys - option.first
                                                } else {
                                                    scheduleSlotKeys + option.first
                                                }
                                            },
                                            label = { Text(option.second) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = scheduleDescription,
                        onValueChange = { scheduleDescription = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("调度说明") },
                        supportingText = { Text("用于补充说明调度如何被解释。") },
                        minLines = 2,
                    )
                    OutlinedTextField(
                        value = scheduleEventTitleTemplate,
                        onValueChange = { scheduleEventTitleTemplate = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("事件标题") },
                        supportingText = { Text("例如：自动续费提醒") },
                        singleLine = true,
                    )
                    if (scheduleEventDateSlotOptions.isNotEmpty()) {
                        EnumChipGroup(
                            title = "事件日期操作数",
                            options = listOf("" to "不指定") + scheduleEventDateSlotOptions,
                            selected = (listOf("" to "不指定") + scheduleEventDateSlotOptions)
                                .firstOrNull { it.first == scheduleEventDateSlotKey }
                                ?: (listOf("" to "不指定") + scheduleEventDateSlotOptions).first(),
                            label = { it.second },
                            onSelected = { option -> scheduleEventDateSlotKey = option.first },
                        )
                    }
                    OutlinedTextField(
                        value = scheduleEventDedupeKeyStrategy,
                        onValueChange = { scheduleEventDedupeKeyStrategy = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("事件去重策略") },
                        supportingText = { Text("例如：ruleId+billingDay") },
                        singleLine = true,
                    )
                }
            }
        }
        item {
            SectionCard(title = "公式画布") {
                Text(
                    text = "从素材栏选择操作数、运算符、函数或分组符加入画布；点击操作数节点可配置方向、值来源和说明。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                DetailTextBlock("当前画布", "${slots.size} 个操作数（${inputSlots.size} 输入 / ${outputSlots.size} 输出）")
                RuleCanvasWorkbenchPanel(
                    canvasState = canvasState,
                    onCanvasNodesChange = ::syncExpressionFromCanvas,
                    onViewportChange = { scale, offset ->
                        canvasState = canvasState.copy(scale = scale, offset = offset)
                    },
                    onPaletteItemSelected = { item ->
                        when (item) {
                            PaletteItem.Operand -> addOperand()
                            is PaletteItem.Operator -> {
                                syncExpressionFromCanvas(
                                    canvasState.nodes + CanvasNode(
                                        type = CanvasNodeType.OPERATOR,
                                        position = Offset.Zero,
                                        operatorSymbol = item.symbol,
                                        displayText = item.label,
                                    )
                                )
                            }
                            is PaletteItem.Function -> {
                                syncExpressionFromCanvas(
                                    canvasState.nodes + listOf(
                                        CanvasNode(
                                            type = CanvasNodeType.FUNCTION,
                                            position = Offset.Zero,
                                            functionName = item.name,
                                            displayText = item.label,
                                        ),
                                        CanvasNode(
                                            type = CanvasNodeType.GROUPING,
                                            position = Offset.Zero,
                                            groupingSymbol = "(",
                                            displayText = "(",
                                        ),
                                        CanvasNode(
                                            type = CanvasNodeType.GROUPING,
                                            position = Offset.Zero,
                                            groupingSymbol = ")",
                                            displayText = ")",
                                        ),
                                    )
                                )
                            }
                            is PaletteItem.Grouping -> {
                                syncExpressionFromCanvas(
                                    canvasState.nodes + CanvasNode(
                                        type = CanvasNodeType.GROUPING,
                                        position = Offset.Zero,
                                        groupingSymbol = item.symbol,
                                        displayText = item.label,
                                    )
                                )
                            }
                        }
                    },
                    onNodeSelected = { nodeId ->
                        canvasState = canvasState.copy(selectedNodeId = nodeId)
                        val node = canvasState.nodes.find { it.id == nodeId }
                        if (node?.type == CanvasNodeType.OPERAND && node.operandConfig != null) {
                            showOperandConfigDialog = node.operandConfig.id
                        }
                    },
                    onNodeDeleted = ::deleteCanvasNode,
                    onToggleFullscreen = { isCanvasFullscreen = true },
                    isFullscreen = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(9f / 16f),
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "公式编码",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                OutlinedTextField(
                    value = expressionField,
                    onValueChange = {
                        expressionField = it
                        syncCanvasFromExpression(it.text)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("公式编码") },
                    minLines = 3,
                )
            }
        }
        item {
            PrimaryActionCard(
                title = draft.saveButtonText,
                description = buildString {
                    append(if (draft.isEditMode) "保存当前规则修改。" else "创建新的规则定义。")
                    append(" 当前已配置 ${slots.size} 个操作数，其中 ${inputSlots.size} 个输入、${outputSlots.size} 个输出。")
                },
                actionLabel = draft.saveButtonText,
                onClick = {
                    onSave(
                        draft.copy(
                            name = name,
                            description = description,
                            computationType = computationType,
                            triggerModes = draft.triggerModes,
                            activationMode = activationMode,
                            toggleLabelWhenEnabled = toggleLabelWhenEnabled,
                            toggleLabelWhenDisabled = toggleLabelWhenDisabled,
                            toggleAnchorSlotKey = toggleAnchorSlotKey,
                            toggleDefaultEnabled = toggleDefaultEnabled,
                            isScheduledEnabled = isScheduledEnabled,
                            scheduleSourceMode = when (scheduleTimeSourceType) {
                                RuleScheduleTimeSourceType.FIXED -> RuleScheduleSourceMode.MANUAL_CALENDAR_RULE
                                RuleScheduleTimeSourceType.SLOT -> RuleScheduleSourceMode.SLOT_DRIVEN
                            },
                            scheduleTimeSourceType = scheduleTimeSourceType,
                            scheduleFixedValue = scheduleFixedValue,
                            scheduleSlotKeys = scheduleSlotKeys,
                            scheduleDescription = scheduleDescription,
                            manualScheduleRule = draft.manualScheduleRule,
                            slotDrivenScheduleConfig = draft.slotDrivenScheduleConfig,
                            generateCalendarEvent = generateCalendarEvent,
                            scheduleEventTitleTemplate = scheduleEventTitleTemplate,
                            scheduleEventDateSlotKey = scheduleEventDateSlotKey,
                            scheduleEventDedupeKeyStrategy = scheduleEventDedupeKeyStrategy,
                            canvasDefinition = buildRuleWorkbenchCanvasDefinition(canvasState.nodes),
                            slots = slots,
                            expression = expressionField.text,
                        )
                    )
                },
            )
        }
    }

    if (isCanvasFullscreen) {
        BackHandler { isCanvasFullscreen = false }
        Dialog(
            onDismissRequest = { isCanvasFullscreen = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = false,
                decorFitsSystemWindows = false,
            ),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface,
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val rotateCanvas = maxHeight > maxWidth
                    val fullscreenCanvasWidth = if (rotateCanvas) maxHeight else maxWidth
                    val fullscreenCanvasHeight = if (rotateCanvas) maxWidth else maxHeight

                    Box(
                        modifier = Modifier
                            .requiredSize(
                                width = fullscreenCanvasWidth,
                                height = fullscreenCanvasHeight,
                            )
                            .align(Alignment.Center)
                            .graphicsLayer {
                                rotationZ = if (rotateCanvas) 90f else 0f
                            },
                    ) {
                        DraggableRuleCanvas(
                            canvasState = canvasState,
                            onNodesChange = ::syncExpressionFromCanvas,
                            onViewportChange = { scale, offset ->
                                canvasState = canvasState.copy(scale = scale, offset = offset)
                            },
                            onNodeSelected = { nodeId ->
                                canvasState = canvasState.copy(selectedNodeId = nodeId)
                                val node = canvasState.nodes.find { it.id == nodeId }
                                if (node?.type == CanvasNodeType.OPERAND && node.operandConfig != null) {
                                    showOperandConfigDialog = node.operandConfig.id
                                }
                            },
                            onNodeDeleted = ::deleteCanvasNode,
                            onToggleFullscreen = { isCanvasFullscreen = false },
                            isFullscreen = true,
                            modifier = Modifier.fillMaxSize(),
                        )

                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        ) {
                            IconButton(
                                onClick = { isCanvasFullscreen = false },
                                modifier = Modifier.size(48.dp),
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回规则页")
                            }
                        }
                    }
                }
            }
        }
    }

    // 操作数配置弹窗
    showOperandConfigDialog?.let { slotId ->
        val slot = slots.find { it.id == slotId }
        if (slot != null) {
            OperandConfigDialog(
                slot = slot,
                systemVariableOptions = draft.systemVariableOptions,
                onDismiss = { showOperandConfigDialog = null },
                onSave = { updatedSlot ->
                    updateOperandInCanvas(slotId, updatedSlot)
                    showOperandConfigDialog = null
                },
                onDelete = {
                    removeOperand(slotId)
                    showOperandConfigDialog = null
                },
            )
        }
    }
}

@Composable
private fun RuleWorkbenchVariableSection(
    title: String,
    description: String,
    slots: List<RuleSlotDraftUiModel>,
    expandedSlotIds: Set<String>,
    systemVariableOptions: List<SystemVariableOptionUiModel>,
    onToggleExpansion: (String) -> Unit,
    onUpdateSlot: (String, (RuleSlotDraftUiModel) -> RuleSlotDraftUiModel) -> Unit,
    onRemoveSlot: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (slots.isEmpty()) {
            Text(
                text = "当前还没有变量，可以先从上面的快捷入口添加。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            slots.forEachIndexed { index, slot ->
                RuleWorkbenchVariableCard(
                    slot = slot,
                    index = index,
                    isExpanded = slot.id in expandedSlotIds,
                    systemVariableOptions = systemVariableOptions,
                    onToggleExpansion = { onToggleExpansion(slot.id) },
                    onUpdateSlot = { transform -> onUpdateSlot(slot.id, transform) },
                    onRemove = { onRemoveSlot(slot.id) },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RuleCanvasBoard(
    slots: List<RuleSlotDraftUiModel>,
    decoder: RuleWorkbenchDecoderModel,
    onSlotClick: (String) -> Unit,
) {
    if (slots.isEmpty()) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.28f),
        ) {
            Text(
                text = "画布还没有槽位。先从上方素材栏加入空输入槽位或空输出槽位。",
                modifier = Modifier.padding(14.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.26f),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "规则画布",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            RuleCanvasLane(
                title = "输入槽位",
                subtitle = "来自属性、系统变量或固定值",
                slots = slots.filter { it.direction == RuleSlotDirection.INPUT },
                onSlotClick = onSlotClick,
            )
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.26f),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "公式轨道",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    RuleCanvasFormulaTrack(decoder = decoder)
                }
            }
            RuleCanvasLane(
                title = "输出槽位",
                subtitle = "规则最终写回的位置",
                slots = slots.filter { it.direction == RuleSlotDirection.OUTPUT },
                onSlotClick = onSlotClick,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RuleCanvasFormulaTrack(
    decoder: RuleWorkbenchDecoderModel,
) {
    val tokenScrollState = rememberScrollState()
    if (decoder.tokens.isEmpty()) {
        Text(
            text = "还没有公式，可以先从上方工具箱点入变量、运算符或函数片段。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(tokenScrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        decoder.tokens.forEach { token ->
            RuleCanvasFormulaTokenChip(token = token)
        }
    }
}

@Composable
private fun RuleCanvasFormulaTokenChip(
    token: RuleWorkbenchTokenModel,
) {
    val containerColor = when (token.kind) {
        RuleWorkbenchTokenKind.VARIABLE -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f)
        RuleWorkbenchTokenKind.FUNCTION -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.75f)
        RuleWorkbenchTokenKind.LITERAL -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
        RuleWorkbenchTokenKind.SYMBOL -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f)
        RuleWorkbenchTokenKind.UNKNOWN -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.72f)
    }
    val label = when (token.kind) {
        RuleWorkbenchTokenKind.VARIABLE -> "槽位"
        RuleWorkbenchTokenKind.FUNCTION -> "函数"
        RuleWorkbenchTokenKind.LITERAL -> "值"
        RuleWorkbenchTokenKind.SYMBOL -> "符号"
        RuleWorkbenchTokenKind.UNKNOWN -> "待修正"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = token.text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RuleCanvasLane(
    title: String,
    subtitle: String,
    slots: List<RuleSlotDraftUiModel>,
    onSlotClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (slots.isEmpty()) {
            Text(
                text = "当前还没有槽位。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                slots.forEach { slot ->
                    RuleCanvasSlotChip(
                        slot = slot,
                        isSelected = false,
                        onClick = { onSlotClick(slot.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleCanvasSlotChip(
    slot: RuleSlotDraftUiModel,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val isIncomplete = slot.key.isBlank()
    val containerColor = when {
        isIncomplete -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
        slot.direction == RuleSlotDirection.OUTPUT -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        slot.sourceType == RuleSlotSourceType.SYSTEM_INPUT || slot.sourceType == RuleSlotSourceType.SYSTEM_OUTPUT ->
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
        slot.sourceType == RuleSlotSourceType.CONFIG_INPUT ->
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
    }
    val borderLabel = when {
        isIncomplete -> "待完善"
        slot.direction == RuleSlotDirection.OUTPUT -> "输出"
        slot.sourceType == RuleSlotSourceType.SYSTEM_INPUT || slot.sourceType == RuleSlotSourceType.SYSTEM_OUTPUT -> "系统"
        slot.sourceType == RuleSlotSourceType.CONFIG_INPUT -> "固定值"
        else -> "外部输入"
    }
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        } else {
            containerColor
        },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = slot.key.ifBlank { "未命名槽位" },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = buildString {
                    append(borderLabel)
                    if (isSelected) append(" · 当前选中")
                    append(" · ${ruleSlotValueTypeLabel(slot.valueType)}")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun buildRuleWorkbenchCanvasState(
    expression: String,
    slots: List<RuleSlotDraftUiModel>,
    selectedNodeId: String? = null,
    canvasDefinition: RuleCanvasDefinition? = null,
    existingNodes: List<CanvasNode> = emptyList(),
): CanvasState {
    val nodes = buildRuleWorkbenchCanvasNodes(
        expression = expression,
        slots = slots,
        canvasDefinition = canvasDefinition,
        existingNodes = existingNodes,
    )
    return CanvasState(
        nodes = nodes,
        connections = buildRuleWorkbenchConnections(nodes),
        selectedNodeId = selectedNodeId.takeIf { candidate -> nodes.any { it.id == candidate } },
    )
}

private fun buildRuleWorkbenchCanvasNodes(
    expression: String,
    slots: List<RuleSlotDraftUiModel>,
    canvasDefinition: RuleCanvasDefinition? = null,
    existingNodes: List<CanvasNode> = emptyList(),
): List<CanvasNode> {
    val operandMap = slots.associateBy { it.key.trim() }
    val parsedNodes = if (expression.isBlank()) {
        emptyList()
    } else {
        val tokens = lexRuleWorkbenchExpression(expression)
        tokens.mapIndexed { index, token ->
            when (token.type) {
                RuleWorkbenchLexTokenType.IDENTIFIER -> {
                    val nextToken = tokens.getOrNull(index + 1)
                    if (token.text.lowercase(Locale.ROOT) in RuleWorkbenchLiteralKeywords) {
                        CanvasNode(
                            id = "literal_${index}_${token.text}",
                            type = CanvasNodeType.LITERAL,
                            position = Offset.Zero,
                            literalValue = token.text,
                            displayText = token.text,
                        )
                    } else if (nextToken?.text == "(") {
                        CanvasNode(
                            id = "func_${index}_${token.text}",
                            type = CanvasNodeType.FUNCTION,
                            position = Offset.Zero,
                            functionName = token.text,
                            displayText = token.text,
                        )
                    } else {
                        operandMap[token.text]?.toOperandCanvasNode(nodeId = "operand_${index}_${token.text}") ?: CanvasNode(
                            id = "operand_${index}_${token.text}",
                            type = CanvasNodeType.OPERAND,
                            position = Offset.Zero,
                            operandConfig = null,
                            displayText = token.text,
                        )
                    }
                }
                RuleWorkbenchLexTokenType.NUMBER,
                RuleWorkbenchLexTokenType.STRING -> CanvasNode(
                    id = "literal_${index}_${token.text.hashCode()}",
                    type = CanvasNodeType.LITERAL,
                    position = Offset.Zero,
                    literalValue = token.text,
                    displayText = token.text,
                )
                RuleWorkbenchLexTokenType.SYMBOL -> if (token.text in RuleWorkbenchGroupingSymbols) {
                    CanvasNode(
                        id = "group_${index}_${token.text.hashCode()}",
                        type = CanvasNodeType.GROUPING,
                        position = Offset.Zero,
                        groupingSymbol = token.text,
                        displayText = token.text,
                    )
                } else {
                    CanvasNode(
                        id = "op_${index}_${token.text.hashCode()}",
                        type = CanvasNodeType.OPERATOR,
                        position = Offset.Zero,
                        operatorSymbol = token.text,
                        displayText = token.text,
                    )
                }
            }
        }
    }

    val remainingExistingNodes = existingNodes.toMutableList()
    val restoredNodes = parsedNodes.mapIndexed { index, parsedNode ->
        val matchingIndex = remainingExistingNodes.indexOfFirst { existingNode ->
            existingNode.ruleWorkbenchNodeToken() == parsedNode.ruleWorkbenchNodeToken()
        }
        if (matchingIndex >= 0) {
            val existingNode = remainingExistingNodes.removeAt(matchingIndex)
            parsedNode.copy(id = existingNode.id, position = existingNode.position)
        } else {
            val persistedNode = canvasDefinition?.nodes?.getOrNull(index)
            val persistedPosition = if (persistedNode?.x != null && persistedNode.y != null) {
                Offset(persistedNode.x, persistedNode.y)
            } else {
                null
            }
            parsedNode.copy(position = persistedPosition ?: Offset.Zero)
        }
    }
    return ensureRuleWorkbenchNodePositions(restoredNodes)
}

private fun buildRuleWorkbenchExpression(nodes: List<CanvasNode>): String {
    val tokens = orderRuleWorkbenchNodes(nodes).mapNotNull { node ->
        when (node.type) {
            CanvasNodeType.OPERAND -> node.operandConfig?.key?.trim()?.takeIf { it.isNotBlank() }
                ?: node.displayText?.trim()?.takeIf { it.isNotBlank() }
            CanvasNodeType.OPERATOR -> node.operatorSymbol?.trim()?.takeIf { it.isNotBlank() }
                ?: node.displayText?.trim()?.takeIf { it.isNotBlank() }
            CanvasNodeType.FUNCTION -> node.functionName?.trim()?.takeIf { it.isNotBlank() }
                ?: node.displayText?.trim()?.takeIf { it.isNotBlank() }
            CanvasNodeType.GROUPING -> node.groupingSymbol?.trim()?.takeIf { it.isNotBlank() }
                ?: node.displayText?.trim()?.takeIf { it.isNotBlank() }
            CanvasNodeType.LITERAL -> node.literalValue?.trim()?.takeIf { it.isNotBlank() }
                ?: node.displayText?.trim()?.takeIf { it.isNotBlank() }
        }
    }
    if (tokens.isEmpty()) return ""
    return buildString {
        tokens.forEachIndexed { index, token ->
            if (index > 0 && needsRuleWorkbenchSpace(tokens[index - 1], token)) {
                append(' ')
            }
            append(token)
        }
    }
}

private fun needsRuleWorkbenchSpace(previous: String, current: String): Boolean {
    if (previous in setOf("(", "{", "[")) return false
    if (current in setOf(")", "}", "]", ",")) return false
    if (current in setOf("(", "{", "[")) return false
    if (previous == ",") return true
    if (previous.any { it.isLetterOrDigit() || it == '_' } && current.any { it.isLetterOrDigit() || it == '_' }) {
        return true
    }
    return true
}

private fun buildRuleWorkbenchConnections(nodes: List<CanvasNode>): List<CanvasConnection> {
    return orderRuleWorkbenchNodes(nodes).map { it.id }.zipWithNext { fromId, toId ->
        CanvasConnection(fromNodeId = fromId, toNodeId = toId)
    }
}

private fun ensureRuleWorkbenchNodePositions(nodes: List<CanvasNode>): List<CanvasNode> {
    val positionedNodes = nodes.filter { it.position != Offset.Zero }
    val lastPositionedNode = orderRuleWorkbenchNodes(positionedNodes).lastOrNull()
    var nextX = lastPositionedNode?.let {
        it.position.x + it.widthDp() + RULE_CANVAS_NODE_GAP_DP
    } ?: 48f
    var nextY = lastPositionedNode?.position?.y ?: 48f

    return nodes.map { node ->
        if (node.position != Offset.Zero) {
            node
        } else {
            if (nextX + node.widthDp() > RULE_CANVAS_WIDTH_DP - RULE_CANVAS_PADDING_DP) {
                nextX = 48f
                nextY += RULE_CANVAS_NODE_HEIGHT_DP + 24f
            }
            val positionedNode = node.copy(position = Offset(nextX, nextY))
            nextX += node.widthDp() + RULE_CANVAS_NODE_GAP_DP
            positionedNode
        }
    }
}

private fun orderRuleWorkbenchNodes(nodes: List<CanvasNode>): List<CanvasNode> {
    val remaining = nodes.sortedWith(compareBy<CanvasNode> { it.position.y }.thenBy { it.position.x }).toMutableList()
    val ordered = mutableListOf<CanvasNode>()
    while (remaining.isNotEmpty()) {
        val rowAnchorY = remaining.first().position.y
        val rowNodes = remaining
            .filter { abs(it.position.y - rowAnchorY) <= RULE_CANVAS_NODE_HEIGHT_DP / 2f }
            .sortedBy { it.position.x }
        ordered += rowNodes
        remaining.removeAll(rowNodes.toSet())
    }
    return ordered
}

private fun CanvasNode.ruleWorkbenchNodeToken(): String = when (type) {
    CanvasNodeType.OPERAND -> "operand:${operandConfig?.key ?: displayText.orEmpty()}"
    CanvasNodeType.OPERATOR -> "operator:${operatorSymbol ?: displayText.orEmpty()}"
    CanvasNodeType.FUNCTION -> "function:${functionName ?: displayText.orEmpty()}"
    CanvasNodeType.GROUPING -> "grouping:${groupingSymbol ?: displayText.orEmpty()}"
    CanvasNodeType.LITERAL -> "literal:${literalValue ?: displayText.orEmpty()}"
}

private fun buildRuleWorkbenchCanvasDefinition(
    nodes: List<CanvasNode>,
): RuleCanvasDefinition {
    val orderedNodes = orderRuleWorkbenchNodes(nodes)
    val canvasNodes = orderedNodes.mapIndexed { index, node ->
        when (node.type) {
            CanvasNodeType.OPERAND -> {
                val slot = node.operandConfig
                RuleCanvasNode(
                    id = node.id,
                    type = RuleCanvasNodeType.SLOT,
                    row = node.position.y.roundToInt(),
                    column = index,
                    x = node.position.x,
                    y = node.position.y,
                    label = slot?.key ?: node.displayText.orEmpty(),
                    slotConfig = slot?.let {
                        RuleCanvasSlotConfig(
                            slotKey = it.key.ifBlank { null },
                            displayName = it.key,
                            direction = it.direction,
                            valueType = it.valueType,
                            valueSource = when (it.sourceType) {
                                RuleSlotSourceType.SYSTEM_INPUT,
                                RuleSlotSourceType.SYSTEM_OUTPUT -> RuleCanvasSlotValueSource.SYSTEM_VARIABLE
                                RuleSlotSourceType.CONFIG_INPUT -> RuleCanvasSlotValueSource.FIXED_VALUE
                                else -> RuleCanvasSlotValueSource.EXTERNAL_INPUT
                            },
                            systemVariableKey = findSystemVariableKeyOrNull(it.systemVariableKey),
                            fixedValue = it.configValue.takeIf { value -> value.isNotBlank() },
                            isRequired = it.isRequired,
                            description = it.description.ifBlank { null },
                        )
                    },
                    isPlaceholder = slot == null || slot.key.isBlank(),
                    styleHint = when {
                        slot == null || slot.key.isBlank() -> RuleCanvasNodeStyleHint.NEEDS_CONFIGURATION
                        slot.direction == RuleSlotDirection.OUTPUT -> RuleCanvasNodeStyleHint.OUTPUT_SLOT
                        slot.sourceType == RuleSlotSourceType.SYSTEM_INPUT || slot.sourceType == RuleSlotSourceType.SYSTEM_OUTPUT ->
                            RuleCanvasNodeStyleHint.SYSTEM_SOURCE
                        slot.sourceType == RuleSlotSourceType.CONFIG_INPUT -> RuleCanvasNodeStyleHint.FIXED_VALUE
                        else -> RuleCanvasNodeStyleHint.INPUT_SLOT
                    },
                )
            }
            CanvasNodeType.OPERATOR -> RuleCanvasNode(
                id = node.id,
                type = RuleCanvasNodeType.OPERATOR,
                row = node.position.y.roundToInt(),
                column = index,
                x = node.position.x,
                y = node.position.y,
                label = node.operatorSymbol ?: node.displayText.orEmpty(),
                operatorKey = node.operatorSymbol,
                styleHint = RuleCanvasNodeStyleHint.DEFAULT,
            )
            CanvasNodeType.FUNCTION -> RuleCanvasNode(
                id = node.id,
                type = RuleCanvasNodeType.FUNCTION,
                row = node.position.y.roundToInt(),
                column = index,
                x = node.position.x,
                y = node.position.y,
                label = node.functionName ?: node.displayText.orEmpty(),
                functionKey = node.functionName,
                styleHint = RuleCanvasNodeStyleHint.DEFAULT,
            )
            CanvasNodeType.GROUPING -> RuleCanvasNode(
                id = node.id,
                type = RuleCanvasNodeType.OPERATOR,
                row = node.position.y.roundToInt(),
                column = index,
                x = node.position.x,
                y = node.position.y,
                label = node.groupingSymbol ?: node.displayText.orEmpty(),
                operatorKey = node.groupingSymbol,
                styleHint = RuleCanvasNodeStyleHint.DEFAULT,
            )
            CanvasNodeType.LITERAL -> RuleCanvasNode(
                id = node.id,
                type = RuleCanvasNodeType.LITERAL,
                row = node.position.y.roundToInt(),
                column = index,
                x = node.position.x,
                y = node.position.y,
                label = node.literalValue ?: node.displayText.orEmpty(),
                literalValue = node.literalValue,
                literalValueType = RuleSlotValueType.TEXT,
                styleHint = RuleCanvasNodeStyleHint.FIXED_VALUE,
            )
        }
    }
    return RuleCanvasDefinition(
        version = 2,
        layoutMode = RuleCanvasLayoutMode.FLOW_ROW,
        nodes = canvasNodes,
        connections = buildRuleWorkbenchConnections(orderedNodes).map { connection ->
            RuleCanvasConnection(fromNodeId = connection.fromNodeId, toNodeId = connection.toNodeId)
        },
    )
}

private fun RuleSlotDraftUiModel.toOperandCanvasNode(
    nodeId: String = id,
): CanvasNode {
    return CanvasNode(
        id = nodeId,
        type = CanvasNodeType.OPERAND,
        position = Offset.Zero,
        operandConfig = this,
        displayText = key,
    )
}

private fun findSystemVariableKeyOrNull(storageKey: String) = findSystemVariableKey(storageKey)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RuleWorkbenchVariableCard(
    slot: RuleSlotDraftUiModel,
    index: Int,
    isExpanded: Boolean,
    systemVariableOptions: List<SystemVariableOptionUiModel>,
    onToggleExpansion: () -> Unit,
    onUpdateSlot: ((RuleSlotDraftUiModel) -> RuleSlotDraftUiModel) -> Unit,
    onRemove: () -> Unit,
) {
    val compatibleSystemVariables = systemVariableOptions.filter { option ->
        ruleSlotMatchesSystemValueType(slot.valueType, option.valueType)
    }
    val title = slot.name.ifBlank {
        slot.key.ifBlank {
            if (slot.direction == RuleSlotDirection.INPUT) {
                "输入变量 ${index + 1}"
            } else {
                "结果变量 ${index + 1}"
            }
        }
    }
    val subtitle = buildString {
        append(ruleSlotSourceTypeLabel(slot.sourceType))
        append(" · ")
        append(ruleSlotValueTypeLabel(slot.valueType))
        append(" · ")
        append(
            if (slot.direction == RuleSlotDirection.INPUT) {
                if (slot.isRequired) "绑定时必填" else "绑定时可选"
            } else {
                ruleOutputUpdateModeLabel(slot.outputUpdateMode)
            }
        )
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .padding(end = 96.dp)
                        .fillMaxWidth(),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                TextButton(onClick = onToggleExpansion) {
                    Text(if (isExpanded) "收起" else "展开")
                }
                TextButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("删除")
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = true,
                    onClick = {},
                    enabled = false,
                    label = { Text(ruleSlotDirectionLabel(slot.direction)) },
                )
                FilterChip(
                    selected = true,
                    onClick = {},
                    enabled = false,
                    label = { Text(ruleSlotSourceTypeLabel(slot.sourceType)) },
                )
                FilterChip(
                    selected = true,
                    onClick = {},
                    enabled = false,
                    label = { Text(ruleSlotValueTypeLabel(slot.valueType)) },
                )
                if (slot.direction == RuleSlotDirection.INPUT && slot.allowQuickCreateAttribute) {
                    FilterChip(
                        selected = true,
                        onClick = {},
                        enabled = false,
                        label = { Text("允许快捷建属性") },
                    )
                }
                if (slot.direction == RuleSlotDirection.OUTPUT) {
                    FilterChip(
                        selected = true,
                        onClick = {},
                        enabled = false,
                        label = { Text(ruleOutputTargetTypeLabel(slot.outputTargetType)) },
                    )
                }
            }
            if (slot.description.isNotBlank()) {
                Text(
                    text = slot.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isExpanded) {
                HorizontalDivider()
                OutlinedTextField(
                    value = slot.key,
                    onValueChange = { value -> onUpdateSlot { it.copy(key = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("变量标识") },
                    supportingText = { Text("公式里引用这个变量时使用的名字，例如 amount / currentDate") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = slot.name,
                    onValueChange = { value -> onUpdateSlot { it.copy(name = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("变量名称") },
                    supportingText = { Text("用于页面里展示，例如 购买金额 / 当前日期") },
                    singleLine = true,
                )
                EnumChipGroup(
                    title = "变量值类型",
                    options = RuleSlotValueType.entries.toList(),
                    selected = slot.valueType,
                    label = { ruleSlotValueTypeLabel(it) },
                    onSelected = { valueType ->
                        onUpdateSlot { current ->
                            val nextCompatibleSystemVariables = systemVariableOptions.filter { option ->
                                ruleSlotMatchesSystemValueType(valueType, option.valueType)
                            }
                            val nextSystemVariable = current.systemVariableKey.takeIf { key ->
                                nextCompatibleSystemVariables.any { option -> option.key == key }
                            }.orEmpty()
                            current.copy(
                                valueType = valueType,
                                systemVariableKey = nextSystemVariable,
                            )
                        }
                    },
                )
                EnumChipGroup(
                    title = "变量来源",
                    options = sourceTypeOptionsForDirection(slot.direction),
                    selected = slot.sourceType,
                    label = { ruleSlotSourceTypeLabel(it) },
                    onSelected = { sourceType ->
                        onUpdateSlot {
                            it.copy(
                                sourceType = sourceType,
                                isRequired = if (
                                    slot.direction == RuleSlotDirection.INPUT &&
                                    sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT
                                ) {
                                    true
                                } else {
                                    it.isRequired
                                },
                                outputTargetType = defaultOutputTargetType(sourceType),
                                systemVariableKey = if (
                                    sourceType == RuleSlotSourceType.SYSTEM_INPUT ||
                                    sourceType == RuleSlotSourceType.SYSTEM_OUTPUT
                                ) {
                                    it.systemVariableKey
                                } else {
                                    ""
                                },
                            )
                        }
                    },
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (slot.direction == RuleSlotDirection.INPUT &&
                        slot.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT
                    ) {
                        FilterChip(
                            selected = true,
                            onClick = {},
                            enabled = false,
                            label = { Text("属性输入固定必填") },
                        )
                    } else {
                        FilterChip(
                            selected = slot.isRequired,
                            onClick = { onUpdateSlot { it.copy(isRequired = !it.isRequired) } },
                            label = { Text(if (slot.isRequired) "绑定时必填" else "绑定时可选") },
                        )
                    }
                    if (slot.direction == RuleSlotDirection.INPUT &&
                        slot.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT
                    ) {
                        FilterChip(
                            selected = slot.allowQuickCreateAttribute,
                            onClick = {
                                onUpdateSlot {
                                    it.copy(allowQuickCreateAttribute = !it.allowQuickCreateAttribute)
                                }
                            },
                            label = {
                                Text(if (slot.allowQuickCreateAttribute) "允许快捷建属性" else "不允许快捷建属性")
                            },
                        )
                    }
                }
                if (slot.direction == RuleSlotDirection.OUTPUT) {
                    EnumChipGroup(
                        title = "结果落点",
                        options = outputTargetOptionsForSourceType(slot.sourceType),
                        selected = slot.outputTargetType,
                        label = { ruleOutputTargetTypeLabel(it) },
                        onSelected = { targetType ->
                            onUpdateSlot { current -> current.copy(outputTargetType = targetType) }
                        },
                    )
                    EnumChipGroup(
                        title = "更新方式",
                        options = RuleOutputUpdateMode.entries.toList(),
                        selected = slot.outputUpdateMode,
                        label = { ruleOutputUpdateModeLabel(it) },
                        onSelected = { mode ->
                            onUpdateSlot { current -> current.copy(outputUpdateMode = mode) }
                        },
                    )
                }
                if (
                    slot.sourceType == RuleSlotSourceType.SYSTEM_INPUT ||
                    slot.sourceType == RuleSlotSourceType.SYSTEM_OUTPUT
                ) {
                    if (compatibleSystemVariables.isEmpty()) {
                        Text(
                            text = "当前值类型下没有可选系统变量。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        compatibleSystemVariables.forEach { option ->
                            SystemVariableOptionCard(
                                option = option,
                                isSelected = option.key == slot.systemVariableKey,
                                onClick = {
                                    if (option.isSelectable) {
                                        onUpdateSlot { current ->
                                            current.copy(systemVariableKey = option.key)
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = slot.description,
                    onValueChange = { value -> onUpdateSlot { it.copy(description = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("变量说明") },
                    minLines = 2,
                )
            }
        }
    }
}

@Composable
private fun RuleWorkbenchExpressionPreview(
    outputSlots: List<RuleSlotDraftUiModel>,
    expression: String,
    referencedVariables: List<String>,
) {
    val outputSummary = outputSlots
        .map { slot -> slot.name.ifBlank { slot.key.ifBlank { "结果" } } }
        .ifEmpty { listOf("结果变量") }
        .joinToString(" / ")
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "实时公式预览",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "$outputSummary = ${expression.ifBlank { "..." }}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = if (referencedVariables.isEmpty()) {
                    "还没有识别到已定义变量引用"
                } else {
                    "已识别变量：${referencedVariables.joinToString(" / ")}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RuleWorkbenchSnippetGroup(
    title: String,
    snippets: List<RuleWorkbenchSnippetModel>,
    onSnippetClick: (RuleWorkbenchSnippetModel) -> Unit,
) {
    if (snippets.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            snippets.forEach { snippet ->
                FilterChip(
                    selected = false,
                    onClick = { onSnippetClick(snippet) },
                    label = { Text(snippet.label) },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RuleWorkbenchDecoderCard(
    decoder: RuleWorkbenchDecoderModel,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "公式解码",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            if (decoder.warnings.isEmpty()) {
                Text(
                    text = "结构看起来已经比较完整，可以继续微调变量或公式。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                decoder.warnings.forEach { warning ->
                    Text(
                        text = "· $warning",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (decoder.knownVariableReferences.isNotEmpty()) {
                DetailLine("已识别变量", decoder.knownVariableReferences.joinToString(" / "))
            }
            if (decoder.unusedVariableKeys.isNotEmpty()) {
                DetailLine("尚未使用", decoder.unusedVariableKeys.joinToString(" / "))
            }
            if (decoder.functionCalls.isNotEmpty()) {
                DetailLine("已识别函数", decoder.functionCalls.joinToString(" / "))
            }
            if (decoder.tokens.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    decoder.tokens.forEach { token ->
                        FilterChip(
                            selected = token.kind == RuleWorkbenchTokenKind.VARIABLE,
                            onClick = {},
                            enabled = false,
                            label = { Text(token.text) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleCanvasWorkbenchPanel(
    canvasState: CanvasState,
    onCanvasNodesChange: (List<CanvasNode>) -> Unit,
    onViewportChange: (scale: Float, offset: Offset) -> Unit,
    onPaletteItemSelected: (PaletteItem) -> Unit,
    onNodeSelected: (String) -> Unit,
    onNodeDeleted: (String) -> Unit,
    onToggleFullscreen: () -> Unit,
    isFullscreen: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RuleCanvasPalette(
            onPaletteItemSelected = onPaletteItemSelected,
            modifier = Modifier.fillMaxWidth(),
        )
        DraggableRuleCanvas(
            canvasState = canvasState,
            onNodesChange = onCanvasNodesChange,
            onViewportChange = onViewportChange,
            onNodeSelected = onNodeSelected,
            onNodeDeleted = onNodeDeleted,
            onToggleFullscreen = onToggleFullscreen,
            isFullscreen = isFullscreen,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

private data class RuleWorkbenchSnippetGroupModel(
    val title: String,
    val snippets: List<RuleWorkbenchSnippetModel>,
)

private data class RuleWorkbenchSnippetModel(
    val label: String,
    val fragment: String,
    val cursorOffsetFromEnd: Int = 0,
)

private data class RuleWorkbenchDecoderModel(
    val tokens: List<RuleWorkbenchTokenModel>,
    val functionCalls: List<String>,
    val knownVariableReferences: List<String>,
    val unknownVariableReferences: List<String>,
    val unusedVariableKeys: List<String>,
    val warnings: List<String>,
)

private data class RuleWorkbenchTokenModel(
    val text: String,
    val kind: RuleWorkbenchTokenKind,
)

private enum class RuleWorkbenchTokenKind {
    VARIABLE,
    FUNCTION,
    LITERAL,
    SYMBOL,
    UNKNOWN,
}

private data class RuleWorkbenchLexToken(
    val type: RuleWorkbenchLexTokenType,
    val text: String,
)

private enum class RuleWorkbenchLexTokenType {
    IDENTIFIER,
    NUMBER,
    STRING,
    SYMBOL,
}

private val RuleWorkbenchGroupingSymbols = setOf("(", ")", "{", "}", "[", "]")
private val RuleWorkbenchLiteralKeywords = setOf("true", "false", "null")

private fun ensureRuleWorkbenchOperands(
    expression: String,
    existingOperands: List<RuleSlotDraftUiModel>,
    retainUnreferenced: Boolean = true,
): List<RuleSlotDraftUiModel> {
    val tokens = lexRuleWorkbenchExpression(expression)
    val referencedNames = tokens
        .mapIndexedNotNull { index, token ->
            if (token.type != RuleWorkbenchLexTokenType.IDENTIFIER) return@mapIndexedNotNull null
            if (tokens.getOrNull(index + 1)?.text == "(") {
                return@mapIndexedNotNull null
            }
            token.text.takeUnless { it.lowercase(Locale.ROOT) in RuleWorkbenchLiteralKeywords }
        }
        .distinct()
    if (referencedNames.isEmpty()) {
        return if (retainUnreferenced) existingOperands else emptyList()
    }

    var result = if (retainUnreferenced) {
        existingOperands
    } else {
        existingOperands.filter { it.key in referencedNames }
    }
    referencedNames.forEach { name ->
        if (result.none { it.key == name }) {
            result = result + createRuleWorkbenchOperandDraft(
                existingSlots = result,
                preferredName = name,
            )
        }
    }
    return result
}

private fun createRuleWorkbenchOperandDraft(
    existingSlots: List<RuleSlotDraftUiModel>,
    preferredName: String? = null,
): RuleSlotDraftUiModel {
    val existingKeys = existingSlots.map { it.key.trim() }.filter { it.isNotBlank() }.toSet()
    var index = 1
    var operandName = preferredName?.trim().orEmpty()
    if (operandName.isBlank()) {
        operandName = "操作数$index"
        while (operandName in existingKeys) {
            index += 1
            operandName = "操作数$index"
        }
    }
    return createRuleWorkbenchSlotDraft(
        direction = RuleSlotDirection.INPUT,
        sourceType = RuleSlotSourceType.ATTRIBUTE_INPUT,
        existingSlots = existingSlots,
    ).copy(
        id = "rule_workbench_operand_${System.nanoTime()}",
        key = operandName,
        name = operandName,
    )
}

private fun createRuleWorkbenchSlotDraft(
    direction: RuleSlotDirection,
    sourceType: RuleSlotSourceType,
    existingSlots: List<RuleSlotDraftUiModel>,
): RuleSlotDraftUiModel {
    val normalizedSource = sourceTypeOptionsForDirection(direction)
        .firstOrNull { it == sourceType }
        ?: defaultSourceTypeForDirection(direction)
    val existingKeys = existingSlots.map { it.key.trim() }.filter { it.isNotBlank() }.toSet()
    val namePrefix = when (normalizedSource) {
        RuleSlotSourceType.ATTRIBUTE_INPUT -> "属性输入"
        RuleSlotSourceType.CONFIG_INPUT -> "配置输入"
        RuleSlotSourceType.SYSTEM_INPUT -> "系统输入"
        RuleSlotSourceType.ATTRIBUTE_OUTPUT -> "结果变量"
        RuleSlotSourceType.READONLY_OUTPUT -> "只读结果"
        RuleSlotSourceType.SYSTEM_OUTPUT -> "系统输出"
    }
    var sameTypeIndex = 1
    var variableName = "$namePrefix$sameTypeIndex"
    while (variableName in existingKeys) {
        sameTypeIndex += 1
        variableName = "$namePrefix$sameTypeIndex"
    }
    return RuleSlotDraftUiModel(
        id = "rule_workbench_slot_${System.nanoTime()}_${direction.name.lowercase()}",
        key = variableName,
        name = variableName,
        direction = direction,
        sourceType = normalizedSource,
        isRequired = direction == RuleSlotDirection.INPUT,
        allowQuickCreateAttribute = normalizedSource == RuleSlotSourceType.ATTRIBUTE_INPUT,
        outputTargetType = defaultOutputTargetType(normalizedSource),
    )
}

private fun buildRuleWorkbenchSnippetGroups(
    computationType: RuleComputationType,
    inputSlots: List<RuleSlotDraftUiModel>,
    outputSlots: List<RuleSlotDraftUiModel>,
): List<RuleWorkbenchSnippetGroupModel> {
    val inputKeys = inputSlots.mapNotNull { slot ->
        slot.key.trim().takeIf { it.isNotBlank() }
    }
    val numericInputs = inputSlots
        .filter { it.valueType == RuleSlotValueType.NUMBER }
        .mapNotNull { slot -> slot.key.trim().takeIf { it.isNotBlank() } }
    val dateInputs = inputSlots
        .filter { it.valueType == RuleSlotValueType.DATE || it.valueType == RuleSlotValueType.SYSTEM_DATE_TIME }
        .mapNotNull { slot -> slot.key.trim().takeIf { it.isNotBlank() } }
    val booleanInputs = inputSlots
        .filter { it.valueType == RuleSlotValueType.BOOLEAN }
        .mapNotNull { slot -> slot.key.trim().takeIf { it.isNotBlank() } }
    val outputKeys = outputSlots.mapNotNull { slot ->
        slot.key.trim().takeIf { it.isNotBlank() }
    }
    val firstInput = inputKeys.getOrNull(0) ?: "value1"
    val secondInput = inputKeys.getOrNull(1) ?: "value2"
    val firstNumeric = numericInputs.getOrNull(0) ?: firstInput
    val secondNumeric = numericInputs.getOrNull(1) ?: secondInput
    val firstDate = dateInputs.getOrNull(0) ?: "startDate"
    val secondDate = dateInputs.getOrNull(1) ?: "endDate"
    val firstBoolean = booleanInputs.getOrNull(0) ?: "condition"
    val variableSnippets = (inputSlots + outputSlots).mapNotNull { slot ->
        val key = slot.key.trim().takeIf { it.isNotBlank() } ?: return@mapNotNull null
        RuleWorkbenchSnippetModel(
            label = slot.name.ifBlank { key },
            fragment = key,
        )
    }

    val presetSnippets = when (computationType) {
        RuleComputationType.SUM -> listOf(
            RuleWorkbenchSnippetModel(
                label = "整段求和",
                fragment = numericInputs.ifEmpty { listOf(firstNumeric, secondNumeric) }.joinToString(" + "),
            ),
        )
        RuleComputationType.DIFFERENCE -> listOf(
            RuleWorkbenchSnippetModel(
                label = "整段差值",
                fragment = "$firstNumeric - $secondNumeric",
            ),
        )
        RuleComputationType.AVERAGE -> listOf(
            RuleWorkbenchSnippetModel(
                label = "整段平均",
                fragment = "($firstNumeric + $secondNumeric) / 2",
            ),
        )
        RuleComputationType.CYCLE -> listOf(
            RuleWorkbenchSnippetModel(
                label = "按周期计数",
                fragment = "cycleCount($firstDate, $secondDate, currentDate, \"MONTH\")",
            ),
        )
        RuleComputationType.ACCUMULATION -> listOf(
            RuleWorkbenchSnippetModel(
                label = "累计结构",
                fragment = "coalesce($firstNumeric, 0) + $secondNumeric",
            ),
        )
        RuleComputationType.CUSTOM -> listOf(
            RuleWorkbenchSnippetModel(
                label = "条件判断",
                fragment = "if($firstBoolean, $firstInput, $secondInput)",
            ),
            RuleWorkbenchSnippetModel(
                label = "日期差",
                fragment = "dateDiff($firstDate, $secondDate, \"DAY\")",
            ),
        )
    }

    return listOf(
        RuleWorkbenchSnippetGroupModel(
            title = "快捷片段",
            snippets = listOf(
                RuleWorkbenchSnippetModel("括号", "()", cursorOffsetFromEnd = 1),
                RuleWorkbenchSnippetModel("加", " + "),
                RuleWorkbenchSnippetModel("减", " - "),
                RuleWorkbenchSnippetModel("乘", " * "),
                RuleWorkbenchSnippetModel("除", " / "),
                RuleWorkbenchSnippetModel("大于", " > "),
                RuleWorkbenchSnippetModel("并且", " && "),
                RuleWorkbenchSnippetModel("或者", " || "),
            ),
        ),
        RuleWorkbenchSnippetGroupModel(
            title = "变量引用",
            snippets = variableSnippets.ifEmpty {
                listOf(RuleWorkbenchSnippetModel("示例变量", firstInput))
            },
        ),
        RuleWorkbenchSnippetGroupModel(
            title = "函数片段",
            snippets = listOf(
                RuleWorkbenchSnippetModel("if", "if($firstBoolean, $firstInput, $secondInput)"),
                RuleWorkbenchSnippetModel("coalesce", "coalesce($firstInput, $secondInput)"),
                RuleWorkbenchSnippetModel("dateDiff", "dateDiff($firstDate, $secondDate, \"DAY\")"),
                RuleWorkbenchSnippetModel("cycleCount", "cycleCount($firstDate, $secondDate, currentDate, \"MONTH\")"),
                RuleWorkbenchSnippetModel("round", "round($firstNumeric)"),
                RuleWorkbenchSnippetModel("abs", "abs($firstNumeric)"),
            ),
        ),
        RuleWorkbenchSnippetGroupModel(
            title = "规则预设",
            snippets = presetSnippets.ifEmpty {
                listOf(
                    RuleWorkbenchSnippetModel(
                        label = "直接返回变量",
                        fragment = firstInput,
                    )
                )
            },
        ),
    )
}

private fun decodeRuleWorkbenchExpression(
    expression: String,
    slots: List<RuleSlotDraftUiModel>,
): RuleWorkbenchDecoderModel {
    val tokens = lexRuleWorkbenchExpression(expression)
    val slotKeys = slots.mapNotNull { slot ->
        slot.key.trim().takeIf { it.isNotBlank() }
    }
    val slotKeySet = slotKeys.toSet()
    val reservedKeywords = setOf("true", "false", "null")
    val functionCalls = linkedSetOf<String>()
    val knownVariables = linkedSetOf<String>()
    val unknownVariables = linkedSetOf<String>()
    val decodedTokens = mutableListOf<RuleWorkbenchTokenModel>()

    tokens.forEachIndexed { index, token ->
        if (token.type == RuleWorkbenchLexTokenType.IDENTIFIER) {
            val nextToken = tokens.getOrNull(index + 1)
            val normalized = token.text.lowercase(Locale.ROOT)
            when {
                nextToken?.text == "(" -> {
                    functionCalls += token.text
                    decodedTokens += RuleWorkbenchTokenModel(token.text, RuleWorkbenchTokenKind.FUNCTION)
                }
                normalized in reservedKeywords -> {
                    decodedTokens += RuleWorkbenchTokenModel(token.text, RuleWorkbenchTokenKind.LITERAL)
                }
                token.text in slotKeySet -> {
                    knownVariables += token.text
                    decodedTokens += RuleWorkbenchTokenModel(token.text, RuleWorkbenchTokenKind.VARIABLE)
                }
                else -> {
                    unknownVariables += token.text
                    decodedTokens += RuleWorkbenchTokenModel(token.text, RuleWorkbenchTokenKind.UNKNOWN)
                }
            }
        } else {
            decodedTokens += RuleWorkbenchTokenModel(
                text = token.text,
                kind = when (token.type) {
                    RuleWorkbenchLexTokenType.NUMBER,
                    RuleWorkbenchLexTokenType.STRING -> RuleWorkbenchTokenKind.LITERAL
                    RuleWorkbenchLexTokenType.SYMBOL -> RuleWorkbenchTokenKind.SYMBOL
                    RuleWorkbenchLexTokenType.IDENTIFIER -> RuleWorkbenchTokenKind.UNKNOWN
                },
            )
        }
    }

    val warnings = mutableListOf<String>()
    if (expression.isBlank()) {
        warnings += "还没有公式，可以先点上面的变量或函数片段开始拼装。"
    }
    if (slots.none { it.direction == RuleSlotDirection.OUTPUT }) {
        warnings += "还没有结果变量，规则结果暂时没有落点。"
    }
    if (unknownVariables.isNotEmpty()) {
        warnings += "存在未定义变量引用：${unknownVariables.joinToString(" / ")}"
    }
    if (expression.isNotBlank() && slots.any { it.direction == RuleSlotDirection.INPUT } && knownVariables.isEmpty()) {
        warnings += "公式里还没有引用任何已定义输入变量。"
    }

    return RuleWorkbenchDecoderModel(
        tokens = decodedTokens,
        functionCalls = functionCalls.toList(),
        knownVariableReferences = knownVariables.toList(),
        unknownVariableReferences = unknownVariables.toList(),
        unusedVariableKeys = slotKeys.filterNot { it in knownVariables },
        warnings = warnings,
    )
}

private fun lexRuleWorkbenchExpression(
    expression: String,
): List<RuleWorkbenchLexToken> {
    if (expression.isBlank()) return emptyList()
    val tokens = mutableListOf<RuleWorkbenchLexToken>()
    var index = 0
    while (index < expression.length) {
        val ch = expression[index]
        when {
            ch.isWhitespace() -> index += 1
            ch.isDigit() -> {
                val start = index
                while (index < expression.length && (expression[index].isDigit() || expression[index] == '.')) {
                    index += 1
                }
                tokens += RuleWorkbenchLexToken(
                    type = RuleWorkbenchLexTokenType.NUMBER,
                    text = expression.substring(start, index),
                )
            }
            ch == '"' || ch == '\'' -> {
                val quote = ch
                val start = index
                index += 1
                while (index < expression.length && expression[index] != quote) {
                    index += 1
                }
                if (index < expression.length) {
                    index += 1
                }
                tokens += RuleWorkbenchLexToken(
                    type = RuleWorkbenchLexTokenType.STRING,
                    text = expression.substring(start, index),
                )
            }
            ch.isLetter() || ch == '_' -> {
                val start = index
                while (index < expression.length && (expression[index].isLetterOrDigit() || expression[index] == '_')) {
                    index += 1
                }
                tokens += RuleWorkbenchLexToken(
                    type = RuleWorkbenchLexTokenType.IDENTIFIER,
                    text = expression.substring(start, index),
                )
            }
            else -> {
                val twoChar = expression.substring(index, minOf(index + 2, expression.length))
                val symbol = when (twoChar) {
                    ">=", "<=", "==", "!=", "&&", "||" -> {
                        index += 2
                        twoChar
                    }
                    else -> {
                        val single = expression[index].toString()
                        index += 1
                        single
                    }
                }
                tokens += RuleWorkbenchLexToken(
                    type = RuleWorkbenchLexTokenType.SYMBOL,
                    text = symbol,
                )
            }
        }
    }
    return tokens
}

@Composable
private fun SystemVariableOptionCard(
    option: SystemVariableOptionUiModel,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = option.isSelectable, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
        },
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "${option.displayName} · ${option.statusLabel}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "值类型：${option.valueTypeLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (option.description.isNotBlank()) {
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            option.availabilityMessage?.takeIf { it.isNotBlank() }?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (option.isSelectable) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }
        }
    }
}

@Composable
private fun DetailSummaryCard(
    icon: String,
    title: String,
    summary: String,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        contentPadding = 18.dp,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            ) {
                MiaoIcon(
                    icon = icon,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        contentPadding = 18.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun DetailLine(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(12.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DetailTextBlock(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun buildAttributePropertySummaryForEditor(
    valueType: AttributeValueType,
    valueSource: AttributeValueSource,
    numberFormat: AttributeNumberFormat,
    unitCategory: String,
    defaultUnit: String,
    decimalPlacesText: String,
    allowNegative: Boolean,
    booleanInputMode: AttributeInputMode,
    trueLabel: String,
    falseLabel: String,
    optionItemsText: String,
    isMultiSelect: Boolean,
    systemVariableLabel: String?,
    defaultValue: String,
): String {
    val segments = mutableListOf<String>()
    when (valueType) {
        AttributeValueType.TEXT -> segments += "文本"
        AttributeValueType.NUMBER -> {
            segments += "格式：${attributeNumberFormatLabel(numberFormat)}"
            if (numberFormat == AttributeNumberFormat.WITH_UNIT) {
                if (unitCategory.isNotBlank()) segments += "单位类型：$unitCategory"
                if (defaultUnit.isNotBlank()) segments += "默认单位：$defaultUnit"
            }
            if (decimalPlacesText.isNotBlank()) segments += "小数位：$decimalPlacesText"
            segments += if (allowNegative) "允许负数" else "不允许负数"
        }
        AttributeValueType.DATE -> segments += "日期"
        AttributeValueType.BOOLEAN -> {
            segments += "交互：${booleanAttributeInputModeLabel(booleanInputMode)}"
            if (trueLabel.isNotBlank()) segments += "真值：$trueLabel"
            if (falseLabel.isNotBlank()) segments += "假值：$falseLabel"
        }
        AttributeValueType.SELECT -> {
            segments += if (isMultiSelect) "多选" else "单选"
            val options = optionItemsText
                .lineSequence()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toList()
            if (options.isNotEmpty()) {
                segments += "选项：${options.joinToString(" / ")}"
            }
        }
    }
    when (valueSource) {
        AttributeValueSource.FIXED -> if (defaultValue.isNotBlank()) {
            segments += "固定值：$defaultValue"
        }
        AttributeValueSource.SYSTEM -> if (!systemVariableLabel.isNullOrBlank()) {
            segments += "系统变量：$systemVariableLabel"
        }
        AttributeValueSource.INPUT -> Unit
    }
    return segments.joinToString(" · ").ifBlank { "无额外值属性" }
}

@Composable
private fun RuleBindingCard(
    binding: RuleBindingUiModel,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(binding.ruleName, fontWeight = FontWeight.SemiBold)
            DetailLine("输入角色", binding.inputRole)
            DetailLine(
                "必需依赖",
                if (binding.requiredDependencies.isEmpty()) "无" else binding.requiredDependencies.joinToString(" / ")
            )
            DetailLine(
                "可选依赖",
                if (binding.optionalDependencies.isEmpty()) "无" else binding.optionalDependencies.joinToString(" / ")
            )
            DetailLine("输出", binding.outputLabel)
        }
    }
}

@Composable
private fun PrimaryActionCard(
    title: String,
    description: String,
    actionLabel: String,
    onClick: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        contentPadding = 18.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 88.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Text(actionLabel, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> EnumChipGroup(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelected(option) },
                    label = { Text(label(option)) },
                )
            }
        }
    }
}

private fun attributeValueTypeLabel(type: AttributeValueType): String = when (type) {
    AttributeValueType.TEXT -> "文本"
    AttributeValueType.NUMBER -> "数值"
    AttributeValueType.DATE -> "日期"
    AttributeValueType.BOOLEAN -> "布尔"
    AttributeValueType.SELECT -> "选择"
}

private fun attributeValueSourceLabel(source: AttributeValueSource): String = when (source) {
    AttributeValueSource.INPUT -> "输入"
    AttributeValueSource.FIXED -> "固定值"
    AttributeValueSource.SYSTEM -> "系统"
}

private fun attributeInputModeLabel(mode: AttributeInputMode): String = when (mode) {
    AttributeInputMode.TEXT_INPUT -> "文本输入"
    AttributeInputMode.NUMBER_INPUT -> "数值输入"
    AttributeInputMode.PRICE_INPUT -> "价格输入"
    AttributeInputMode.DATE_PICKER -> "日期选择"
    AttributeInputMode.BOOLEAN_SWITCH -> "布尔开关"
    AttributeInputMode.SINGLE_SELECT -> "单选"
    AttributeInputMode.MULTI_SELECT -> "多选"
    AttributeInputMode.TAG_INPUT -> "标签输入"
}

private fun attributeNumberFormatLabel(format: AttributeNumberFormat): String = when (format) {
    AttributeNumberFormat.PLAIN -> "普通数值"
    AttributeNumberFormat.PRICE -> "量值"
    AttributeNumberFormat.PERCENTAGE -> "百分比"
    AttributeNumberFormat.WITH_UNIT -> "量值"
}

private fun ruleActivationModeLabel(mode: RuleActivationMode): String = when (mode) {
    RuleActivationMode.ALWAYS_ON -> "始终运行"
    RuleActivationMode.USER_TOGGLE -> "用户开关控制"
}

private fun booleanAttributeInputModeLabel(mode: AttributeInputMode): String = when (mode) {
    AttributeInputMode.BOOLEAN_SWITCH -> "按钮"
    AttributeInputMode.TEXT_INPUT -> "输入"
    else -> attributeInputModeLabel(mode)
}

private fun attributeEditorPreviewIcon(
    valueType: AttributeValueType,
    numberFormat: AttributeNumberFormat,
    unitCategory: String,
): String {
    return when (valueType) {
        AttributeValueType.TEXT -> "notes"
        AttributeValueType.NUMBER -> {
            if (numberFormat == AttributeNumberFormat.WITH_UNIT && unitCategory == "价格") "payments" else "straighten"
        }
        AttributeValueType.DATE -> "event"
        AttributeValueType.BOOLEAN -> "toggle_on"
        AttributeValueType.SELECT -> "checklist"
    }
}

private fun suggestedUnitCategories(): List<String> = listOf(
    "价格",
    "面积",
    "长度",
    "重量",
    "体积",
    "容量",
    "时间",
)

private fun suggestedUnits(unitCategory: String): List<String> = when (unitCategory.trim()) {
    "价格" -> listOf("CNY", "USD", "EUR", "JPY", "GBP", "HKD", "TWD")
    "面积" -> listOf("m2", "cm2", "km2", "亩", "ha")
    "长度" -> listOf("mm", "cm", "m", "km", "in", "ft")
    "重量" -> listOf("mg", "g", "kg", "t", "lb")
    "体积", "容量" -> listOf("ml", "L", "m3")
    "时间" -> listOf("秒", "分钟", "小时", "天", "月", "年")
    else -> emptyList()
}

private fun ruleComputationTypeLabel(type: RuleComputationType): String = when (type) {
    RuleComputationType.SUM -> "求和"
    RuleComputationType.DIFFERENCE -> "差值"
    RuleComputationType.AVERAGE -> "平均值"
    RuleComputationType.CYCLE -> "周期"
    RuleComputationType.ACCUMULATION -> "累计"
    RuleComputationType.CUSTOM -> "自定义"
}

private fun ruleTriggerModeLabel(mode: RuleTriggerMode): String = when (mode) {
    RuleTriggerMode.ON_ATTRIBUTE_SELECTED -> "添加属性时"
    RuleTriggerMode.ON_VALUE_CHANGED -> "属性值变更时"
    RuleTriggerMode.ON_FORM_OPENED -> "打开表单时"
    RuleTriggerMode.ON_SAVE -> "保存时"
    RuleTriggerMode.ON_SYSTEM_INPUT_CHANGED -> "系统输入变化时"
    RuleTriggerMode.SCHEDULED -> "周期调度"
}

private fun ruleSlotDirectionLabel(direction: RuleSlotDirection): String = when (direction) {
    RuleSlotDirection.INPUT -> "输入"
    RuleSlotDirection.OUTPUT -> "输出"
}

private fun ruleSlotValueTypeLabel(type: RuleSlotValueType): String = when (type) {
    RuleSlotValueType.TEXT -> "文本"
    RuleSlotValueType.NUMBER -> "数值"
    RuleSlotValueType.DATE -> "日期"
    RuleSlotValueType.BOOLEAN -> "布尔"
    RuleSlotValueType.SELECT -> "选择"
    RuleSlotValueType.CYCLE_UNIT -> "周期单位"
    RuleSlotValueType.SYSTEM_DATE_TIME -> "系统时间"
}

private fun ruleSlotSourceTypeLabel(type: RuleSlotSourceType): String = when (type) {
    RuleSlotSourceType.ATTRIBUTE_INPUT -> "属性输入"
    RuleSlotSourceType.CONFIG_INPUT -> "配置输入"
    RuleSlotSourceType.SYSTEM_INPUT -> "系统输入"
    RuleSlotSourceType.ATTRIBUTE_OUTPUT -> "属性输出"
    RuleSlotSourceType.READONLY_OUTPUT -> "只读输出"
    RuleSlotSourceType.SYSTEM_OUTPUT -> "系统输出"
}

private fun ruleOutputTargetTypeLabel(type: RuleOutputTargetType): String = when (type) {
    RuleOutputTargetType.READONLY_RESULT -> "只读结果"
    RuleOutputTargetType.ATTRIBUTE_VALUE -> "属性值"
    RuleOutputTargetType.SYSTEM_VARIABLE -> "系统变量"
}

private fun ruleOutputUpdateModeLabel(mode: RuleOutputUpdateMode): String = when (mode) {
    RuleOutputUpdateMode.OVERWRITE -> "覆盖"
    RuleOutputUpdateMode.ACCUMULATE -> "累计"
}

private fun newEditorRuleSlotDraft(direction: RuleSlotDirection): RuleSlotDraftUiModel {
    return RuleSlotDraftUiModel(
        id = "slot_${System.nanoTime()}_${direction.name.lowercase()}",
        direction = direction,
        sourceType = defaultSourceTypeForDirection(direction),
        outputTargetType = defaultOutputTargetTypeForDirection(direction),
    )
}

private fun defaultSourceTypeForDirection(direction: RuleSlotDirection): RuleSlotSourceType = when (direction) {
    RuleSlotDirection.INPUT -> RuleSlotSourceType.ATTRIBUTE_INPUT
    RuleSlotDirection.OUTPUT -> RuleSlotSourceType.READONLY_OUTPUT
}

private fun defaultOutputTargetTypeForDirection(direction: RuleSlotDirection): RuleOutputTargetType = when (direction) {
    RuleSlotDirection.INPUT -> RuleOutputTargetType.READONLY_RESULT
    RuleSlotDirection.OUTPUT -> RuleOutputTargetType.READONLY_RESULT
}

private fun defaultOutputTargetType(sourceType: RuleSlotSourceType): RuleOutputTargetType = when (sourceType) {
    RuleSlotSourceType.ATTRIBUTE_OUTPUT -> RuleOutputTargetType.ATTRIBUTE_VALUE
    RuleSlotSourceType.SYSTEM_OUTPUT -> RuleOutputTargetType.SYSTEM_VARIABLE
    else -> RuleOutputTargetType.READONLY_RESULT
}

private fun sourceTypeOptionsForDirection(direction: RuleSlotDirection): List<RuleSlotSourceType> = when (direction) {
    RuleSlotDirection.INPUT -> listOf(
        RuleSlotSourceType.ATTRIBUTE_INPUT,
        RuleSlotSourceType.CONFIG_INPUT,
        RuleSlotSourceType.SYSTEM_INPUT,
    )
    RuleSlotDirection.OUTPUT -> listOf(
        RuleSlotSourceType.ATTRIBUTE_OUTPUT,
        RuleSlotSourceType.READONLY_OUTPUT,
        RuleSlotSourceType.SYSTEM_OUTPUT,
    )
}

private fun outputTargetOptionsForSourceType(sourceType: RuleSlotSourceType): List<RuleOutputTargetType> {
    return listOf(defaultOutputTargetType(sourceType))
}

private fun ruleSlotMatchesSystemValueType(
    slotValueType: RuleSlotValueType,
    attributeValueType: AttributeValueType,
): Boolean {
    return when (slotValueType) {
        RuleSlotValueType.TEXT -> attributeValueType == AttributeValueType.TEXT
        RuleSlotValueType.NUMBER -> attributeValueType == AttributeValueType.NUMBER
        RuleSlotValueType.DATE -> attributeValueType == AttributeValueType.DATE
        RuleSlotValueType.BOOLEAN -> attributeValueType == AttributeValueType.BOOLEAN
        RuleSlotValueType.SELECT -> attributeValueType == AttributeValueType.SELECT
        RuleSlotValueType.CYCLE_UNIT -> {
            attributeValueType == AttributeValueType.TEXT || attributeValueType == AttributeValueType.SELECT
        }
        RuleSlotValueType.SYSTEM_DATE_TIME -> {
            attributeValueType == AttributeValueType.DATE || attributeValueType == AttributeValueType.TEXT
        }
    }
}
