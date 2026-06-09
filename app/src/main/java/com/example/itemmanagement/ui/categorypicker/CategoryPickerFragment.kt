package com.example.itemmanagement.ui.categorypicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.itemmanagement.ItemManagementApplication
import com.example.itemmanagement.R
import com.example.itemmanagement.ui.components.ItemCategoryPickerSheet
import com.example.itemmanagement.ui.theme.LiquidGlassTheme
import com.example.itemmanagement.utils.normalizeCategoryPath

class CategoryPickerFragment : Fragment() {

    private val viewModel: CategoryPickerViewModel by viewModels {
        val app = requireActivity().application as ItemManagementApplication
        CategoryPickerViewModelFactory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.refresh(arguments?.getString(ARG_CURRENT_VALUE))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LiquidGlassTheme {
                    val uiState by viewModel.uiState.collectAsState()
                    ItemCategoryPickerSheet(
                        currentValue = arguments?.getString(ARG_CURRENT_VALUE).orEmpty(),
                        options = uiState.options,
                        iconMap = uiState.iconMap,
                        onDismiss = { findNavController().navigateUp() },
                        onCreateCategory = { path, icon ->
                            viewModel.createCategory(path, icon) { _ ->
                                // 仅创建分类并刷新列表，不自动选定退出
                                // ViewModel.createCategory 内部已经调用了 refresh()
                            }
                        },
                        onSelectCategory = { path ->
                            deliverResult(path)
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        hideBottomNavigation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showBottomNavigation()
    }

    private fun deliverResult(path: String) {
        findNavController().previousBackStackEntry
            ?.savedStateHandle
            ?.set(RESULT_KEY, normalizeCategoryPath(path))
        findNavController().navigateUp()
    }

    private fun hideBottomNavigation() {
        activity?.findViewById<View>(R.id.nav_view)?.visibility = View.GONE
    }

    private fun showBottomNavigation() {
        activity?.findViewById<View>(R.id.nav_view)?.visibility = View.VISIBLE
    }

    companion object {
        const val RESULT_KEY = "category_picker_result"
        const val ARG_CURRENT_VALUE = "currentValue"

        fun args(currentValue: String?): Bundle {
            return bundleOf(ARG_CURRENT_VALUE to currentValue)
        }
    }
}
