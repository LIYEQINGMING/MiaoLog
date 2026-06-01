package com.example.itemmanagement.ui.calendar

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
import com.example.itemmanagement.ui.theme.LiquidGlassTheme

class ItemCalendarFragment : Fragment() {

    private val viewModel: ItemCalendarViewModel by viewModels {
        ItemCalendarViewModelFactory(
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
                    ItemCalendarScreen(
                        viewModel = viewModel,
                        onNavigateToItem = { itemId ->
                            val bundle = androidx.core.os.bundleOf("itemId" to itemId)
                            findNavController().navigate(R.id.navigation_item_detail, bundle)
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 隐藏MainActivity的顶部导航栏和底部导航栏
        val activity = requireActivity() as? com.example.itemmanagement.MainActivity
        activity?.supportActionBar?.hide()
        activity?.findViewById<View>(R.id.nav_view)?.visibility = View.GONE
        
        viewModel.forceCleanupAutoEvents()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        
        // 恢复MainActivity的顶部导航栏和底部导航栏
        val activity = requireActivity() as? com.example.itemmanagement.MainActivity
        activity?.supportActionBar?.show()
        activity?.findViewById<View>(R.id.nav_view)?.visibility = View.VISIBLE
    }
}
