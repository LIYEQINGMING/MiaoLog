package com.example.itemmanagement.ui.template

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.itemmanagement.data.AppDatabase
import com.example.itemmanagement.data.entity.template.ItemTemplateEntity
import com.example.itemmanagement.data.repository.ItemTemplateRepository
import com.example.itemmanagement.ui.components.GlassCard
import com.example.itemmanagement.ui.components.springClick
import com.example.itemmanagement.ui.theme.LiquidGlassTheme
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TemplateSelectionBottomSheet(
    private val onTemplateSelected: (ItemTemplateEntity) -> Unit,
    private val onManageTemplates: () -> Unit
) : BottomSheetDialogFragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LiquidGlassTheme {
                    TemplateSelectionScreen(
                        onTemplateSelected = {
                            onTemplateSelected(it)
                            dismiss()
                        },
                        onManageTemplates = {
                            onManageTemplates()
                            dismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TemplateSelectionScreen(
    onTemplateSelected: (ItemTemplateEntity) -> Unit,
    onManageTemplates: () -> Unit
) {
    val context = LocalContext.current
    var templates by remember { mutableStateOf<List<ItemTemplateEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val database = AppDatabase.getDatabase(context)
        val repository = ItemTemplateRepository(database.itemTemplateDao())
        val dbTemplates = repository.getAllVisibleTemplates()
        
        if (dbTemplates.isEmpty()) {
            // Provide default if empty
            templates = listOf(
                ItemTemplateEntity(
                    id = -1,
                    templateName = "通用模板",
                    description = "默认的基础物品模板",
                    selectedFields = "品牌,规格",
                    customAttributeIds = "[\"attr_system_status\",\"attr_system_tags\",\"attr_system_purchase_date\"]"
                )
            )
        } else {
            templates = dbTemplates
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "选择模板",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onManageTemplates) {
                Icon(Icons.Default.Settings, contentDescription = "Manage Templates")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(templates) { template ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .springClick { onTemplateSelected(template) },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = template.templateName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (!template.description.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = template.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
