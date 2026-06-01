package com.example.itemmanagement.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import com.example.itemmanagement.ItemManagementApplication
import com.example.itemmanagement.R
import com.example.itemmanagement.data.model.Item
import com.example.itemmanagement.data.repository.UnifiedItemRepository
import com.example.itemmanagement.ui.analysis.InventoryAnalysisViewModel
import com.example.itemmanagement.ui.analysis.InventoryAnalysisViewModelFactory
import com.example.itemmanagement.ui.components.GlassCard
import com.example.itemmanagement.ui.home.HomeViewModel
import com.example.itemmanagement.ui.home.ItemCard
import com.example.itemmanagement.ui.theme.LiquidGlassTheme
import java.util.Calendar

class SearchFragment : Fragment() {

    private val searchViewModel: SearchViewModel by viewModels {
        val app = requireActivity().application as ItemManagementApplication
        SearchViewModelFactory(app.repository)
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
                    SearchScreen(
                        viewModel = searchViewModel,
                        analysisViewModel = analysisViewModel,
                        onNavigateToItem = { itemId ->
                            val bundle = androidx.core.os.bundleOf("itemId" to itemId)
                            findNavController().navigate(R.id.navigation_item_detail, bundle)
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        analysisViewModel.refresh()
    }
}

class SearchViewModel(
    repository: UnifiedItemRepository
) : ViewModel() {
    private val allItems = repository.getAllItems().asLiveData()
    private val query = MediatorLiveData<String>().apply { value = "" }

    val filteredItems = MediatorLiveData<List<Item>>().apply {
        fun refresh() {
            val items = allItems.value.orEmpty()
            val keyword = query.value.orEmpty().trim()
            value = if (keyword.isBlank()) {
                items.sortedByDescending { it.addDate.time }
            } else {
                items.filter { item ->
                    item.name.contains(keyword, ignoreCase = true) ||
                        item.category.contains(keyword, ignoreCase = true) ||
                        item.subCategory?.contains(keyword, ignoreCase = true) == true ||
                        item.brand?.contains(keyword, ignoreCase = true) == true ||
                        item.location?.getFullLocationString()?.contains(keyword, ignoreCase = true) == true ||
                        item.locationAddress?.contains(keyword, ignoreCase = true) == true ||
                        item.tags.any { tag -> tag.name.contains(keyword, ignoreCase = true) }
                }.sortedByDescending { it.addDate.time }
            }
        }

        addSource(allItems) { refresh() }
        addSource(query) { refresh() }
    }

    val queryText: LiveData<String> = query

    fun setQuery(value: String) {
        query.value = value
    }
}

class SearchViewModelFactory(
    private val repository: UnifiedItemRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
private fun SearchScreen(
    viewModel: SearchViewModel,
    analysisViewModel: InventoryAnalysisViewModel,
    onNavigateToItem: (Long) -> Unit
) {
    val query by viewModel.queryText.observeAsState("")
    val items by viewModel.filteredItems.observeAsState(emptyList())
    val analysisData by analysisViewModel.analysisData.observeAsState()

    val recentAddedCount = rememberRecentAddedCount(items)

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "检索",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                contentPadding = 8.dp
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::setQuery,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("搜索物品、品牌、分类、标签、位置") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "搜索") },
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SearchInsightCard(
                    modifier = Modifier.weight(1f),
                    title = "最近新增",
                    value = recentAddedCount.toString(),
                    hint = "近 30 天"
                )
                SearchInsightCard(
                    modifier = Modifier.weight(1f),
                    title = "即将到期",
                    value = (analysisData?.inventoryStats?.expiringItems ?: 0).toString(),
                    hint = "需要关注"
                )
                SearchInsightCard(
                    modifier = Modifier.weight(1f),
                    title = "空间",
                    value = (analysisData?.inventoryStats?.locationsCount ?: 0).toString(),
                    hint = "记录位置"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (query.isBlank()) "全部记录" else "搜索结果",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (query.isBlank()) {
                    "这里汇聚你全部的个人物品记录，输入关键词即可快速筛选。"
                } else {
                    "当前匹配到 ${items.size} 条结果。"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (items.isEmpty()) {
            SearchEmptyState(query = query)
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalItemSpacing = 14.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                items(items, key = { it.id }) { item ->
                    ItemCard(
                        displayItem = HomeViewModel.HomeDisplayItem(item = item),
                        onClick = { onNavigateToItem(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchInsightCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    hint: String
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        contentPadding = 12.dp
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = hint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SearchEmptyState(query: String) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (query.isBlank()) "还没有可检索的记录" else "没有找到匹配内容",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (query.isBlank()) {
                    "添加第一条记录后，这里会成为你最快的物品入口。"
                } else {
                    "试试更换关键词，或者从名称、品牌、标签、位置这些维度检索。"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun rememberRecentAddedCount(items: List<Item>): Int {
    val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }
    return items.count { it.addDate.after(calendar.time) }
}
