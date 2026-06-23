package com.example.itemmanagement.ui.attribute

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.itemmanagement.ItemManagementApplication

class AttributeFragment : Fragment() {

    private val viewModel: AttributeManagementViewModel by viewModels {
        AttributeManagementViewModelFactory(
            (requireActivity().application as ItemManagementApplication).attributeRepository
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
                    onCycleTemplateTypeFilter = viewModel::cycleTemplateTypeFilter,
                    onCycleTemplateCategoryFilter = viewModel::cycleTemplateCategoryFilter,
                    onCycleRuleTypeFilter = viewModel::cycleRuleTypeFilter,
                    onClearTemplateFilters = viewModel::clearTemplateFilters,
                    onOpenCreateAttribute = viewModel::openCreateAttribute,
                    onOpenCreateFromTemplate = viewModel::openCreateFromTemplate,
                    onOpenAttributeDetail = viewModel::openAttributeDetail,
                    onOpenRuleDetail = viewModel::openRuleDetail,
                    onOpenTemplateDetail = viewModel::openTemplateDetail,
                    onCreateFromTemplate = viewModel::createFromTemplate,
                    onSaveAttributeDraft = viewModel::saveAttributeDraft,
                    onDeleteAttribute = viewModel::requestDeleteAttribute,
                    onDismissDialog = viewModel::dismissDialog,
                    onConfirmDeleteAttribute = viewModel::confirmDeleteAttribute,
                    onMessageConsumed = viewModel::consumeMessage,
                )
            }
        }
    }
}
