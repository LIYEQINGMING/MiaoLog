package com.example.itemmanagement.ui.analysis

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.itemmanagement.ItemManagementApplication
import com.example.itemmanagement.ui.theme.LiquidGlassTheme

class InventoryAnalysisFragment : Fragment() {

    private val viewModel: InventoryAnalysisViewModel by viewModels {
        InventoryAnalysisViewModelFactory(
            (requireActivity().application as ItemManagementApplication).repository
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
                LiquidGlassTheme {
                    StatisticsChartsScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 仅隐藏顶部栏，底部导航保持常驻
        val activity = requireActivity() as? com.example.itemmanagement.MainActivity
        activity?.supportActionBar?.hide()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        
        // 恢复顶部栏状态
        val activity = requireActivity() as? com.example.itemmanagement.MainActivity
        activity?.supportActionBar?.show()
    }
}
