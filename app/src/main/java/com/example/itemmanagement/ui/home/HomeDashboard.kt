package com.example.itemmanagement.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.itemmanagement.R
import com.example.itemmanagement.ui.analysis.InventoryAnalysisViewModel
import com.example.itemmanagement.ui.components.GlassCard
import com.example.itemmanagement.ui.components.springClick
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid


@Composable
fun HomeDashboard(
    homeViewModel: HomeViewModel,
    analysisViewModel: InventoryAnalysisViewModel,
    onNavigateToItem: (Long) -> Unit,
    onNavigateToList: (String, String) -> Unit,
    onNavigateToShoppingList: () -> Unit
) {
    val items by homeViewModel.items.observeAsState(emptyList())
    val analysisData by analysisViewModel.analysisData.observeAsState()
    val viewType by homeViewModel.viewType.observeAsState(ItemViewType.WATERFALL)
    val sortType by homeViewModel.sortType.observeAsState(ItemSortType.DEFAULT)
    val selectedCategory by homeViewModel.selectedCategory.observeAsState(null)
    val availableCategories by homeViewModel.availableCategories.observeAsState(emptyList())
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "首页",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            contentPadding = 24.dp
        ) {
            Column {
                Text(
                    text = "资产总览",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(40.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "总价值",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("¥ %.2f", analysisData?.inventoryStats?.totalValue ?: 0.0),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Column {
                        Text(
                            text = "日均价值",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("¥ %.2f", analysisData?.inventoryStats?.dailyAverageValue ?: 0.0),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "物品 ${analysisData?.inventoryStats?.totalItems ?: 0}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "分类 ${analysisData?.inventoryStats?.categoriesCount ?: 0}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "标签 ${analysisData?.inventoryStats?.tagsCount ?: 0}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Toolbar for View Toggle and Sort
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "物品清单",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Filter Menu
                Box {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (selectedCategory != null) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("全部", color = if (selectedCategory == null) MaterialTheme.colorScheme.primary else Color.Unspecified) },
                            onClick = {
                                homeViewModel.setCategoryFilter(null)
                                showFilterMenu = false
                            }
                        )
                        availableCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category, color = if (selectedCategory == category) MaterialTheme.colorScheme.primary else Color.Unspecified) },
                                onClick = {
                                    homeViewModel.setCategoryFilter(category)
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
                
                // Sort Menu
                Box {
                    TextButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = sortType.label)
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        ItemSortType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.label) },
                                onClick = {
                                    homeViewModel.setSortType(type)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
                
                // View Toggle
                IconButton(onClick = {
                    homeViewModel.setViewType(
                        if (viewType == ItemViewType.WATERFALL) ItemViewType.LIST else ItemViewType.WATERFALL
                    )
                }) {
                    Icon(
                        imageVector = if (viewType == ItemViewType.WATERFALL) Icons.Outlined.ViewList else Icons.Outlined.GridView,
                        contentDescription = "Toggle View"
                    )
                }
            }
        }

        if (viewType == ItemViewType.WATERFALL) {
            // Item Waterfall
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalItemSpacing = 16.dp,
                modifier = Modifier.weight(1f)
            ) {
                items(items, key = { it.item.id }) { displayItem ->
                    ItemCard(displayItem = displayItem, onClick = { onNavigateToItem(displayItem.item.id) })
                }
            }
        } else {
            // Item List
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                lazyItems(items, key = { it.item.id }) { displayItem ->
                    ItemListCard(displayItem = displayItem, onClick = { onNavigateToItem(displayItem.item.id) })
                }
            }
        }
    }
}

@Composable
fun ItemListCard(
    displayItem: HomeViewModel.HomeDisplayItem,
    onClick: () -> Unit
) {
    val item = displayItem.item
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .springClick(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        contentPadding = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main Photo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray.copy(alpha = 0.2f))
            ) {
                val primaryPhoto = item.photos.firstOrNull { it.isMain } ?: item.photos.firstOrNull()
                
                if (primaryPhoto != null) {
                    AsyncImage(
                        model = primaryPhoto.uri,
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_image_placeholder),
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center).size(32.dp),
                        tint = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.category ?: "未分类",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (displayItem.showReason && !displayItem.reasonText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = displayItem.reasonText,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
            }
            
            // Price or Date on the right
            if (item.price != null && item.price > 0) {
                Text(
                    text = String.format("¥%.2f", item.price),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ItemCard(
    displayItem: HomeViewModel.HomeDisplayItem,
    onClick: () -> Unit
) {
    val item = displayItem.item
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .springClick(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        contentPadding = 0.dp
    ) {
        Column {
            // Main Photo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.Gray.copy(alpha = 0.2f))
            ) {
                val primaryPhoto = item.photos.firstOrNull { it.isMain } ?: item.photos.firstOrNull()
                
                if (primaryPhoto != null) {
                    AsyncImage(
                        model = primaryPhoto.uri,
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_image_placeholder),
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center).size(48.dp),
                        tint = Color.Gray
                    )
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.category ?: "未分类",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (displayItem.showReason && !displayItem.reasonText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = displayItem.reasonText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
