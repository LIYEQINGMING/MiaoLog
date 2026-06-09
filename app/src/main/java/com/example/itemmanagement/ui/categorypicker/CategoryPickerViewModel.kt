package com.example.itemmanagement.ui.categorypicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.itemmanagement.data.repository.UnifiedItemRepository
import com.example.itemmanagement.utils.DEFAULT_CATEGORY_ICONS
import com.example.itemmanagement.utils.DEFAULT_CATEGORY_ROOTS
import com.example.itemmanagement.utils.DEFAULT_CATEGORY_SAMPLE_PATHS
import com.example.itemmanagement.utils.categoryPathDisplayName
import com.example.itemmanagement.utils.defaultCategoryIcon
import com.example.itemmanagement.utils.extractDeletedPathMarkers
import com.example.itemmanagement.utils.extractEditedPathMarkers
import com.example.itemmanagement.utils.normalizeCategoryPath
import com.example.itemmanagement.utils.stripPathMarkers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoryPickerUiState(
    val options: List<String> = emptyList(),
    val iconMap: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true
)

class CategoryPickerViewModel(
    private val repository: UnifiedItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryPickerUiState())
    val uiState: StateFlow<CategoryPickerUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh(currentValue: String? = null) {
        viewModelScope.launch {
            val rawCategoryOptions = repository.getStoredCustomOptions("分类")
            val deletedPaths = extractDeletedPathMarkers(rawCategoryOptions)
            val editedPaths = extractEditedPathMarkers(rawCategoryOptions)
            val storedCategoryPaths = stripPathMarkers(rawCategoryOptions)
            val usagePaths = repository.getCategoryUsageSummaries().map { normalizeCategoryPath(it.path) }
            val iconMap = repository.getCategoryIconMap()
                .mapKeys { normalizeCategoryPath(it.key) }

            val rootCategories = DEFAULT_CATEGORY_ROOTS
                .filterNot { deletedPaths.contains(it) }
                .map { editedPaths[it] ?: it }

            val options = (
                rootCategories +
                    DEFAULT_CATEGORY_SAMPLE_PATHS +
                    storedCategoryPaths +
                    usagePaths +
                    listOfNotNull(currentValue)
                )
                .map(::normalizeCategoryPath)
                .filter { it.isNotBlank() }
                .distinct()
                .sortedWith(compareBy<String> { it.count { ch -> ch == '/' } }.thenBy { it })

            _uiState.value = CategoryPickerUiState(
                options = options,
                iconMap = iconMap,
                isLoading = false
            )
        }
    }

    fun createCategory(path: String, icon: String? = null, onCreated: (String) -> Unit) {
        val normalizedPath = normalizeCategoryPath(path)
        if (normalizedPath.isBlank()) return

        viewModelScope.launch {
            val currentOptions = repository.getStoredCustomOptions("分类").toMutableList()
            if (!currentOptions.contains(normalizedPath)) {
                currentOptions.add(normalizedPath)
                repository.saveStoredCustomOptions("分类", currentOptions)
            }

            val currentIcons = repository.getCategoryIconMap().toMutableMap()
            if (!currentIcons.containsKey(normalizedPath) || icon != null) {
                currentIcons[normalizedPath] = icon ?: defaultCategoryIcon(categoryPathDisplayName(normalizedPath))
                repository.saveCategoryIconMap(currentIcons)
            }

            refresh(normalizedPath)
            onCreated(normalizedPath)
        }
    }
}

class CategoryPickerViewModelFactory(
    private val repository: UnifiedItemRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryPickerViewModel::class.java)) {
            return CategoryPickerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
