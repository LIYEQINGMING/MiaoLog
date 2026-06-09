package com.example.itemmanagement.ui.category

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

class CategoryFragment : Fragment() {

    private val viewModel: CategoryManagementViewModel by viewModels {
        val app = requireActivity().application as ItemManagementApplication
        CategoryManagementViewModelFactory(app.repository)
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
                CategoryManagementScreen(
                    uiState = uiState,
                    onCreateCategory = viewModel::createCategory,
                    onRenameCategory = viewModel::renameCategory,
                    onDeleteCategory = viewModel::deleteCategory,
                    onClose = { findNavController().navigateUp() },
                    onMessageConsumed = viewModel::consumeMessage
                )
            }
        }
    }

}
