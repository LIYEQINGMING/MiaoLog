package com.example.itemmanagement.ui.analysis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.itemmanagement.data.model.InventoryAnalysisData
import com.example.itemmanagement.ui.components.GlassCard
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartAnimationType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsChartsScreen(viewModel: InventoryAnalysisViewModel) {
    val analysisData by viewModel.analysisData.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(true)
    val error by viewModel.error.observeAsState()
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = { Text("数据图表", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = error ?: "Error", color = MaterialTheme.colorScheme.error)
            }
        } else if (analysisData != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Categories Chart
                ChartCard(
                    title = "物品分类占比",
                    chartModel = buildCategoryChart(analysisData!!)
                )

                // Locations Chart
                ChartCard(
                    title = "存放位置分布",
                    chartModel = buildLocationChart(analysisData!!)
                )
                
                // Monthly Trend Chart
                ChartCard(
                    title = "月度录入趋势",
                    chartModel = buildTrendChart(analysisData!!)
                )

                Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom nav
            }
        }
    }
}

@Composable
fun ChartCard(title: String, chartModel: AAChartModel) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        contentPadding = 16.dp
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            AndroidView(
                factory = { context ->
                    AAChartView(context).apply {
                        aa_drawChartWithChartModel(chartModel)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }
    }
}

private fun buildCategoryChart(data: InventoryAnalysisData): AAChartModel {
    val pieData = data.categoryAnalysis.map { 
        arrayOf(it.category, it.count)
    }.toTypedArray()
    
    return AAChartModel()
        .chartType(AAChartType.Pie)
        .title("")
        .dataLabelsEnabled(true)
        .legendEnabled(false)
        .backgroundColor("#00000000") // Transparent
        .animationType(AAChartAnimationType.EaseInOutQuart)
        .colorsTheme(arrayOf("#0A84FF", "#5E5CE6", "#64D2FF", "#FF3B30", "#FF9F0A") as Array<Any>)
        .series(
            arrayOf(
                AASeriesElement()
                    .name("数量")
                    .data(pieData as Array<Any>)
            ) as Array<Any>
        )
}

private fun buildLocationChart(data: InventoryAnalysisData): AAChartModel {
    return AAChartModel()
        .chartType(AAChartType.Column)
        .title("")
        .dataLabelsEnabled(false)
        .legendEnabled(false)
        .yAxisTitle("")
        .backgroundColor("#00000000")
        .animationType(AAChartAnimationType.EaseInOutQuart)
        .categories(data.locationAnalysis.map { it.location }.toTypedArray())
        .colorsTheme(arrayOf("#5E5CE6") as Array<Any>)
        .series(
            arrayOf(
                AASeriesElement()
                    .name("数量")
                    .data(data.locationAnalysis.map { it.count }.toTypedArray() as Array<Any>)
            ) as Array<Any>
        )
}

private fun buildTrendChart(data: InventoryAnalysisData): AAChartModel {
    val sortedData = data.monthlyTrends.sortedBy { it.month }
    val categories = sortedData.map { 
        if (it.month.length >= 7) it.month.substring(5) else it.month 
    }.toTypedArray()

    return AAChartModel()
        .chartType(AAChartType.Line)
        .title("")
        .dataLabelsEnabled(false)
        .legendEnabled(false)
        .yAxisTitle("")
        .backgroundColor("#00000000")
        .animationType(AAChartAnimationType.EaseInOutQuart)
        .categories(categories)
        .colorsTheme(arrayOf("#0A84FF") as Array<Any>)
        .series(
            arrayOf(
                AASeriesElement()
                    .name("新增数量")
                    .data(sortedData.map { it.count }.toTypedArray() as Array<Any>)
            ) as Array<Any>
        )
}