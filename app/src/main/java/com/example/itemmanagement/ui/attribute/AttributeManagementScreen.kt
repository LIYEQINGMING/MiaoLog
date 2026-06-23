package com.example.itemmanagement.ui.attribute

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.itemmanagement.ui.attribute.model.*
import com.example.itemmanagement.ui.components.GlassCard
import com.example.itemmanagement.ui.components.MiaoIcon
import com.example.itemmanagement.ui.main.LiquidBackground
import com.example.itemmanagement.ui.theme.LiquidGlassTheme

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
    onCycleTemplateTypeFilter: () -> Unit,
    onCycleTemplateCategoryFilter: () -> Unit,
    onCycleRuleTypeFilter: () -> Unit,
    onClearTemplateFilters: () -> Unit,
    onOpenCreateAttribute: () -> Unit,
    onOpenCreateFromTemplate: () -> Unit,
    onOpenAttributeDetail: (AttributeListItemUiModel) -> Unit,
    onOpenRuleDetail: (RuleListItemUiModel) -> Unit,
    onOpenTemplateDetail: (TemplateListItemUiModel) -> Unit,
    onCreateFromTemplate: (String) -> Unit,
    onSaveAttributeDraft: () -> Unit,
    onDeleteAttribute: (AttributeListItemUiModel) -> Unit,
    onDismissDialog: () -> Unit,
    onConfirmDeleteAttribute: () -> Unit,
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
                        AttributeManagementHeader(
                            title = "属性管理",
                            searchQuery = uiState.searchQuery,
                            selectedTab = uiState.selectedTab,
                            isListMode = true,
                            onBack = onClose,
                            onTabSelected = onTabSelected,
                            onSearchQueryChange = onSearchQueryChange,
                            onPrimaryActionClick = when (uiState.selectedTab) {
                                AttributeManagementTab.ATTRIBUTES -> onOpenCreateAttribute
                                AttributeManagementTab.RULES -> null
                                AttributeManagementTab.TEMPLATES -> onOpenCreateFromTemplate
                            },
                        )

                        when (uiState.selectedTab) {
                            AttributeManagementTab.ATTRIBUTES -> {
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
                                    onDeleteAttribute = onDeleteAttribute,
                                )
                            }

                            AttributeManagementTab.RULES -> {
                                RuleListPane(
                                    paneState = uiState.rulePane,
                                    listState = listState,
                                    onOpenRuleDetail = onOpenRuleDetail,
                                )
                            }

                            AttributeManagementTab.TEMPLATES -> {
                                TemplateFilterSection(
                                    filters = uiState.templatePane.filters,
                                    totalCount = uiState.templatePane.totalCount,
                                    onCycleTemplateType = onCycleTemplateTypeFilter,
                                    onCycleTemplateCategory = onCycleTemplateCategoryFilter,
                                    onCycleRuleType = onCycleRuleTypeFilter,
                                    onClearFilters = onClearTemplateFilters,
                                )
                                TemplateListPane(
                                    paneState = uiState.templatePane,
                                    listState = listState,
                                    onOpenTemplateDetail = onOpenTemplateDetail,
                                )
                            }
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
                            onDelete = {
                                onDeleteAttribute(
                                    AttributeListItemUiModel(
                                        id = routeState.detail.id,
                                        name = routeState.detail.name,
                                        icon = routeState.detail.icon,
                                        sourceLabel = routeState.detail.sourceLabel,
                                        valueTypeLabel = routeState.detail.valueDefinition.valueType,
                                        optionSourceLabel = routeState.detail.valueDefinition.optionSource,
                                        multiValueLabel = routeState.detail.valueDefinition.multiValueLabel,
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
                            title = routeState.detail.name,
                            subtitle = "模板详情",
                            onBack = onNavigateBackInPage,
                        )
                        TemplateDetailPage(
                            detail = routeState.detail,
                            onCreateFromTemplate = {
                                if (routeState.detail is TemplateDetailUiModel.AttributeTemplateDetail) {
                                    onCreateFromTemplate(routeState.detail.id)
                                }
                            },
                        )
                    }

                    is AttributeManagementRouteState.RuleDetail -> {
                        SecondaryPageHeader(
                            title = routeState.detail.name,
                            subtitle = "规则详情",
                            onBack = onNavigateBackInPage,
                        )
                        RuleDetailPage(detail = routeState.detail)
                    }

                    is AttributeManagementRouteState.CreateFromTemplate -> {
                        SecondaryPageHeader(
                            title = routeState.draft.templateName,
                            subtitle = "从模板创建属性",
                            onBack = onNavigateBackInPage,
                        )
                        CreateAttributeFromTemplatePage(
                            draft = routeState.draft,
                            onSave = onSaveAttributeDraft,
                        )
                    }

                    AttributeManagementRouteState.CreateAttribute -> {
                        SecondaryPageHeader(
                            title = "新建属性",
                            subtitle = "属性创建页",
                            onBack = onNavigateBackInPage,
                        )
                        CreateAttributePage(
                            onSave = onSaveAttributeDraft,
                            onOpenCreateFromTemplate = onOpenCreateFromTemplate,
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

        null -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttributeManagementHeader(
    title: String,
    searchQuery: String,
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
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        AttributeManagementTab.values().forEachIndexed { index, tab ->
                            SegmentedButton(
                                selected = tab == selectedTab,
                                onClick = { onTabSelected(tab) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = AttributeManagementTab.values().size,
                                ),
                                label = { Text(tab.displayName) },
                            )
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        placeholder = {
                            Text(
                                when (selectedTab) {
                                    AttributeManagementTab.ATTRIBUTES -> "搜索属性、模板名、规则名"
                                    AttributeManagementTab.RULES -> "搜索规则、依赖项、输出定义"
                                    AttributeManagementTab.TEMPLATES -> "搜索模板、规则类型、输出定义"
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
    AttributeManagementHeader(
        title = title,
        searchQuery = "",
        selectedTab = AttributeManagementTab.ATTRIBUTES,
        isListMode = false,
        onBack = onBack,
        onTabSelected = {},
        onSearchQueryChange = {},
        onPrimaryActionClick = null,
    )
    Text(
        text = subtitle,
        modifier = Modifier.padding(horizontal = 16.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
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
                    description = "可以先从基础模板或价格模板创建一个属性。",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                )
            }

            ListContentState.SearchEmpty -> {
                EmptyStateCard(
                    title = "没有找到匹配属性",
                    description = "可以调整关键词，或从模板创建新属性。",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                )
            }

            ListContentState.FilterEmpty -> {
                EmptyStateCard(
                    title = "当前筛选条件下没有属性",
                    description = "可以清空筛选，或切换模板来源后再看。",
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
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = paneState.contentState) {
            ListContentState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            ListContentState.Empty -> {
                EmptyStateCard(
                    title = "还没有规则",
                    description = "后续可以在这里管理系统规则和自定义规则。",
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ListItemIcon(icon = item.icon)
                Column(
                    modifier = Modifier.weight(1f),
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
                        text = "${item.sourceLabel} · ${item.valueTypeLabel} · ${item.optionSourceLabel} · ${item.multiValueLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "来源模板：${item.templateName ?: "无"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "规则：${item.ruleSummary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = item.usageCountText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Box {
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
) {
    var menuExpanded by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(22.dp),
        contentPadding = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ListItemIcon(icon = item.icon)
                Column(
                    modifier = Modifier.weight(1f),
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
                        text = item.dependencySummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Box {
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ListItemIcon(icon = item.icon)
                Column(
                    modifier = Modifier.weight(1f),
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
            Box {
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
            SectionCard(title = "字段定义") {
                DetailLine("值类型", detail.valueDefinition.valueType)
                DetailLine("选项来源", detail.valueDefinition.optionSource)
                DetailLine("输入方式", detail.valueDefinition.inputMode)
                DetailLine("值数量", detail.valueDefinition.multiValueLabel)
                DetailLine("来源模板", detail.templateName ?: "无")
            }
        }
        item {
            SectionCard(title = "规则绑定") {
                if (detail.ruleBindings.isEmpty()) {
                    Text("当前未绑定规则", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    detail.ruleBindings.forEach { binding ->
                        RuleBindingCard(binding)
                    }
                }
            }
        }
        item {
            SectionCard(title = "依赖补齐说明") {
                if (detail.dependencyHints.isEmpty()) {
                    Text("该属性当前不会触发表单依赖自动补齐。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    detail.dependencyHints.forEach { hint ->
                        DetailLine("触发属性", hint.triggerAttributeName)
                        DetailLine("自动补齐输入", hint.autoFillInputs.joinToString(" / "))
                        DetailLine("只读输出", hint.readonlyOutputs.joinToString(" / "))
                    }
                }
            }
        }
        item {
            SectionCard(title = "操作") {
                Text(
                    text = if (detail.isEditable) "当前属性允许继续编辑字段定义与规则绑定范围。" else "系统属性当前阶段仅允许查看，不开放编辑。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
            SectionCard(title = "规则结构") {
                DetailLine("规则类型", detail.ruleType)
                DetailLine("输入角色", if (detail.inputRoles.isEmpty()) "无" else detail.inputRoles.joinToString(" / "))
                DetailLine(
                    "必需依赖",
                    if (detail.requiredDependencies.isEmpty()) "无" else detail.requiredDependencies.joinToString(" / ")
                )
                DetailLine(
                    "可选依赖",
                    if (detail.optionalDependencies.isEmpty()) "无" else detail.optionalDependencies.joinToString(" / ")
                )
                DetailLine("输出定义", if (detail.outputs.isEmpty()) "无" else detail.outputs.joinToString(" / "))
            }
        }
        item {
            SectionCard(title = "规则说明") {
                DetailLine("表达式", detail.expression ?: "未配置")
                DetailLine("描述", detail.description ?: "暂无说明")
            }
        }
        item {
            SectionCard(title = "操作") {
                Text(
                    text = if (detail.isSystemBuiltIn) {
                        "系统规则当前阶段仅支持查看。"
                    } else {
                        "自定义规则的编辑与删除能力会在后续阶段接入。"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TemplateDetailPage(
    detail: TemplateDetailUiModel,
    onCreateFromTemplate: () -> Unit,
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
                        DetailLine("选项来源", detail.defaultDefinition.optionSource)
                        DetailLine("输入方式", detail.defaultDefinition.inputMode)
                        DetailLine("值数量", detail.defaultDefinition.multiValueLabel)
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
                    SectionCard(title = "规则结构") {
                        DetailLine("规则类型", detail.ruleType)
                        DetailLine("输入角色", detail.inputRoles.joinToString(" / "))
                        DetailLine("必需依赖", detail.requiredDependencies.joinToString(" / "))
                        DetailLine(
                            "可选依赖",
                            if (detail.optionalDependencies.isEmpty()) "无" else detail.optionalDependencies.joinToString(" / ")
                        )
                        DetailLine("输出定义", detail.outputs.joinToString(" / "))
                        DetailLine("引用统计", detail.referenceCountText)
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateAttributePage(
    onSave: () -> Unit,
    onOpenCreateFromTemplate: () -> Unit,
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
            SectionCard(title = "当前定位") {
                Text("这里是新建属性页骨架。当前阶段优先推荐从模板创建属性，而不是直接空白起手。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            PrimaryActionCard(
                title = "从模板创建属性",
                description = "带出默认值类型、选项来源、输入方式和规则绑定。",
                actionLabel = "选择模板",
                onClick = onOpenCreateFromTemplate,
            )
        }
        item {
            PrimaryActionCard(
                title = "保存当前骨架页",
                description = "后续可在这里接入真正的属性编辑表单。",
                actionLabel = "模拟保存",
                onClick = onSave,
            )
        }
    }
}

@Composable
private fun CreateAttributeFromTemplatePage(
    draft: CreateAttributeFromTemplateUiModel,
    onSave: () -> Unit,
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
                icon = draft.templateIcon,
                title = draft.templateName,
                summary = "当前将从模板生成一个新属性",
            )
        }
        item {
            SectionCard(title = "模板默认值") {
                DetailLine("初始属性名", draft.initialAttributeName)
                DetailLine("值类型", draft.valueDefinition.valueType)
                DetailLine("选项来源", draft.valueDefinition.optionSource)
                DetailLine("输入方式", draft.valueDefinition.inputMode)
                DetailLine("值数量", draft.valueDefinition.multiValueLabel)
            }
        }
        item {
            SectionCard(title = "默认规则绑定") {
                if (draft.defaultRuleBindings.isEmpty()) {
                    Text("当前模板不预置规则绑定", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    draft.defaultRuleBindings.forEach { binding ->
                        Text("• $binding", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            SectionCard(title = "当前可编辑范围") {
                draft.editableHints.forEach { hint ->
                    Text("• $hint", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            PrimaryActionCard(
                title = "保存模板创建结果",
                description = "后续可在这里接入真正的表单编辑和持久化逻辑。",
                actionLabel = "模拟保存",
                onClick = onSave,
            )
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Text(actionLabel, fontWeight = FontWeight.Medium)
            }
        }
    }
}
