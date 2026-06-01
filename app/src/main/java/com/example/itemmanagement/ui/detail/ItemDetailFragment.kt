package com.example.itemmanagement.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.itemmanagement.ItemManagementApplication
import com.example.itemmanagement.R
import com.example.itemmanagement.ui.theme.LiquidGlassTheme

/**
 * Material Design 3 精美物品详情Fragment - 基于Compose重写
 */
class ItemDetailFragment : Fragment() {

    private val viewModel: ItemDetailViewModel by viewModels {
        ItemDetailViewModelFactory(
            (requireActivity().application as ItemManagementApplication).repository
        )
    }

    private val args: ItemDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LiquidGlassTheme {
                    ItemDetailScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            findNavController().navigateUp()
                        },
                        onNavigateToEdit = { itemId ->
                            val bundle = androidx.core.os.bundleOf("itemId" to itemId)
                            findNavController().navigate(R.id.action_navigation_item_detail_to_editItemFragment, bundle)
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
        
        // 加载数据
        viewModel.loadItem(args.itemId)
        
        // 观察删除成功
        viewModel.navigateBack.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        
        // 恢复MainActivity的顶部导航栏和底部导航栏
        val activity = requireActivity() as? com.example.itemmanagement.MainActivity
        activity?.supportActionBar?.show()
    }
}
