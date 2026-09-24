package com.example.itemmanagement.ui.attribute

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.itemmanagement.ItemManagementApplication
import com.example.itemmanagement.ui.attribute.model.AttributeManagementEntryMode

class RuleManagementFragment : Fragment() {

    private val viewModel: AttributeManagementViewModel by viewModels {
        AttributeManagementViewModelFactory(
            (requireActivity().application as ItemManagementApplication).attributeRepository,
            (requireActivity().application as ItemManagementApplication).appSystemSourceRepository,
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LaunchedEffect(Unit) {
                    viewModel.initializeEntryMode(AttributeManagementEntryMode.RULES)
                }
                val uiState by viewModel.uiState.collectAsState()
                AttributeManagementScreen(
                    uiState = uiState,
                    onClose = { findNavController().navigateUp() },
                    onNavigateBackInPage = viewModel::navigateBackWithinAttributeManagement,
                    onTabSelected = viewModel::selectTab,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onCycleAttributeSourceFilter = viewModel::cycleAttributeSourceFilter,
                    onCycleAttributeValueTypeFilter = viewModel::cycleAttributeValueTypeFilter,
                    onCycleAttributeRuleBindingFilter = viewModel::cycleAttributeRuleBindingFilter,
                    onClearAttributeFilters = viewModel::clearAttributeFilters,
                    onCycleRuleSourceFilter = viewModel::cycleRuleSourceFilter,
                    onCycleRuleManagementTypeFilter = viewModel::cycleRuleManagementTypeFilter,
                    onCycleRuleUsageFilter = viewModel::cycleRuleUsageFilter,
                    onClearRuleFilters = viewModel::clearRuleFilters,
                    onOpenCreateAttribute = viewModel::openCreateAttribute,
                    onOpenCreateRule = viewModel::openCreateRule,
                    onOpenAttributeDetail = viewModel::openAttributeDetail,
                    onOpenRuleDetail = viewModel::openRuleDetail,
                    onOpenEditAttribute = viewModel::openEditAttribute,
                    onDuplicateAttribute = viewModel::duplicateAttribute,
                    onOpenEditRule = viewModel::openEditRule,
                    onSaveAttributeDraft = viewModel::saveAttributeDraft,
                    onOpenRuleBindingWizard = viewModel::openRuleBindingWizard,
                    onSaveRuleBindingWizard = viewModel::saveRuleBindingWizard,
                    onSaveRuleDraft = viewModel::saveRuleDraft,
                    onDeleteAttribute = viewModel::requestDeleteAttribute,
                    onDeleteRule = viewModel::requestDeleteRule,
                    onDeleteRuleById = viewModel::requestDeleteRule,
                    onDismissDialog = viewModel::dismissDialog,
                    onConfirmDeleteAttribute = viewModel::confirmDeleteAttribute,
                    onConfirmDeleteRule = viewModel::confirmDeleteRule,
                    onMessageConsumed = viewModel::consumeMessage,
                )
            }
        }
    }
}
