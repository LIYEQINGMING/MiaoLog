package com.example.itemmanagement.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.itemmanagement.ItemManagementApplication
import com.example.itemmanagement.R
import com.example.itemmanagement.ui.analysis.InventoryAnalysisViewModel
import com.example.itemmanagement.ui.analysis.InventoryAnalysisViewModelFactory
import com.example.itemmanagement.ui.theme.LiquidGlassTheme

class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels {
        val app = requireActivity().application as ItemManagementApplication
        HomeViewModelFactory(app.repository, app.userProfileRepository)
    }
    
    private val analysisViewModel: InventoryAnalysisViewModel by viewModels {
        val app = requireActivity().application as ItemManagementApplication
        InventoryAnalysisViewModelFactory(app.repository)
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
                    HomeDashboard(
                        homeViewModel = homeViewModel,
                        analysisViewModel = analysisViewModel,
                        onNavigateToItem = { itemId ->
                            val bundle = androidx.core.os.bundleOf("itemId" to itemId)
                            findNavController().navigate(R.id.navigation_item_detail, bundle)
                        },
                        onNavigateToList = { listType, title ->
                            val bundle = androidx.core.os.bundleOf(
                                "listType" to listType,
                                "title" to title
                            )
                            findNavController().navigate(R.id.action_navigation_home_to_itemListFragment, bundle)
                        },
                        onNavigateToShoppingList = {
                            findNavController().navigate(R.id.navigation_shopping_list_management)
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.refreshData()
        analysisViewModel.refresh()
    }
    
    fun refreshData() {
        homeViewModel.refreshData()
        analysisViewModel.refresh()
    }
}