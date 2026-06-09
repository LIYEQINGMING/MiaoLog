package com.example.itemmanagement.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.itemmanagement.data.repository.UnifiedItemRepository
import com.example.itemmanagement.utils.DEFAULT_CATEGORY_ROOTS
import com.example.itemmanagement.utils.DEFAULT_CATEGORY_SAMPLE_PATHS
import com.example.itemmanagement.utils.categoryPathMatchesOrDescendant
import com.example.itemmanagement.utils.categoryPathParent
import com.example.itemmanagement.utils.extractDeletedPathMarkers
import com.example.itemmanagement.utils.extractEditedPathMarkers
import com.example.itemmanagement.utils.joinCategoryPath
import com.example.itemmanagement.utils.normalizeCategoryPath
import com.example.itemmanagement.utils.replaceCategoryPathPrefix
import com.example.itemmanagement.utils.stripPathMarkers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoryManagementUiState(
    val isLoading: Boolean = true,
    val allPaths: List<String> = emptyList(),
    val rawOptions: List<String> = emptyList(),
    val iconMap: Map<String, String> = emptyMap(),
    val usageCounts: Map<String, Int> = emptyMap(),
    val message: String? = null,
)

class CategoryManagementViewModel(
    private val repository: UnifiedItemRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryManagementUiState())
    val uiState: StateFlow<CategoryManagementUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            loadState()
        }
    }

    fun consumeMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun createCategory(parentPath: String, name: String, icon: String) {
        viewModelScope.launch {
            val targetPath = joinCategoryPath(parentPath, name)
            if (targetPath.isBlank()) {
                postMessage("请输入分类名称")
                return@launch
            }

            val state = _uiState.value
            if (state.allPaths.any { normalizeCategoryPath(it) == targetPath }) {
                postMessage("同一层级下已存在同名分类")
                return@launch
            }

            val updatedRawOptions = (state.rawOptions + targetPath).distinct()
            val updatedIconMap = state.iconMap.toMutableMap().apply {
                if (icon.isNotBlank()) {
                    this[targetPath] = icon
                }
            }
            persistCategoryCatalog(updatedRawOptions, updatedIconMap, "已创建分类")
        }
    }

    fun renameCategory(targetPath: String, newName: String, icon: String) {
        viewModelScope.launch {
            val normalizedTargetPath = normalizeCategoryPath(targetPath)
            val normalizedNewPath = joinCategoryPath(categoryPathParent(normalizedTargetPath), newName)
            if (normalizedTargetPath.isBlank() || normalizedNewPath.isBlank()) {
                postMessage("分类名称不能为空")
                return@launch
            }

            val state = _uiState.value
            val affectedPaths = state.allPaths.filter {
                categoryPathMatchesOrDescendant(it, normalizedTargetPath)
            }
            val conflictExists = normalizedNewPath != normalizedTargetPath &&
                state.allPaths.any { existing ->
                    val normalizedExisting = normalizeCategoryPath(existing)
                    normalizedExisting == normalizedNewPath && normalizedExisting !in affectedPaths
                }
            if (conflictExists) {
                postMessage("同一层级下已存在同名分类")
                return@launch
            }

            if (normalizedNewPath != normalizedTargetPath) {
                state.usageCounts.keys
                    .filter { categoryPathMatchesOrDescendant(it, normalizedTargetPath) }
                    .forEach { oldPath ->
                        repository.updateCategoryPath(
                            oldPath,
                            replaceCategoryPathPrefix(oldPath, normalizedTargetPath, normalizedNewPath)
                        )
                    }
            }

            val deletedMarkers = extractDeletedPathMarkers(state.rawOptions).toMutableSet()
            val editMarkers = extractEditedPathMarkers(state.rawOptions).toMutableMap()
            val customPaths = stripPathMarkers(state.rawOptions).toMutableList()

            if (DEFAULT_CATEGORY_ROOTS.contains(normalizedTargetPath)) {
                deletedMarkers.remove(normalizedTargetPath)
                if (normalizedNewPath == normalizedTargetPath) {
                    editMarkers.remove(normalizedTargetPath)
                } else {
                    editMarkers[normalizedTargetPath] = normalizedNewPath
                }
            }

            val updatedCustomPaths = customPaths
                .map { path ->
                    if (categoryPathMatchesOrDescendant(path, normalizedTargetPath)) {
                        replaceCategoryPathPrefix(path, normalizedTargetPath, normalizedNewPath)
                    } else {
                        path
                    }
                }
                .distinct()

            val updatedRawOptions = buildList {
                addAll(deletedMarkers.map { "DELETED:$it" })
                addAll(editMarkers.map { (oldPath, newPath) -> "EDIT:$oldPath->$newPath" })
                addAll(updatedCustomPaths)
            }

            val updatedIconMap = state.iconMap.entries.associate { (path, savedIcon) ->
                val resolvedPath = if (categoryPathMatchesOrDescendant(path, normalizedTargetPath)) {
                    replaceCategoryPathPrefix(path, normalizedTargetPath, normalizedNewPath)
                } else {
                    path
                }
                resolvedPath to savedIcon
            }.toMutableMap().apply {
                if (icon.isNotBlank()) {
                    this[normalizedNewPath] = icon
                } else if (normalizedNewPath != normalizedTargetPath) {
                    remove(normalizedNewPath)
                }
            }

            persistCategoryCatalog(updatedRawOptions, updatedIconMap, "已更新分类")
        }
    }

    fun deleteCategory(targetPath: String) {
        viewModelScope.launch {
            val normalizedTargetPath = normalizeCategoryPath(targetPath)
            if (normalizedTargetPath.isBlank()) {
                return@launch
            }

            val state = _uiState.value
            val hasChildren = state.allPaths.any {
                val normalizedPath = normalizeCategoryPath(it)
                normalizedPath != normalizedTargetPath &&
                    normalizedPath.startsWith("$normalizedTargetPath / ")
            }
            if (hasChildren) {
                postMessage("该分类下还有子分类，暂不允许直接删除")
                return@launch
            }

            val itemCount = state.usageCounts[normalizedTargetPath] ?: 0
            if (itemCount > 0) {
                repository.updateCategoryPath(normalizedTargetPath, "未分类")
            }

            val deletedMarkers = extractDeletedPathMarkers(state.rawOptions).toMutableSet()
            val editMarkers = extractEditedPathMarkers(state.rawOptions).toMutableMap()
            val customPaths = stripPathMarkers(state.rawOptions).toMutableList()

            if (DEFAULT_CATEGORY_ROOTS.contains(normalizedTargetPath)) {
                deletedMarkers.add(normalizedTargetPath)
                editMarkers.remove(normalizedTargetPath)
            } else {
                customPaths.removeAll { normalizeCategoryPath(it) == normalizedTargetPath }
            }
            if (itemCount > 0 && "未分类" !in customPaths) {
                customPaths.add("未分类")
            }

            val updatedRawOptions = buildList {
                addAll(deletedMarkers.map { "DELETED:$it" })
                addAll(editMarkers.map { (oldPath, newPath) -> "EDIT:$oldPath->$newPath" })
                addAll(customPaths.distinct())
            }
            val updatedIconMap = state.iconMap.toMutableMap().apply {
                remove(normalizedTargetPath)
            }
            persistCategoryCatalog(updatedRawOptions, updatedIconMap, "已删除分类")
        }
    }

    private suspend fun persistCategoryCatalog(
        rawOptions: List<String>,
        iconMap: Map<String, String>,
        successMessage: String,
    ) {
        repository.saveStoredCustomOptions("分类", rawOptions.distinct())
        repository.saveCategoryIconMap(iconMap.filterKeys { it.isNotBlank() })
        loadState(successMessage)
    }

    private suspend fun loadState(message: String? = null) {
        _uiState.value = _uiState.value.copy(isLoading = true, message = message)

        val rawOptions = repository.getStoredCustomOptions("分类")
        val deletedMarkers = extractDeletedPathMarkers(rawOptions)
        val editMarkers = extractEditedPathMarkers(rawOptions)
        val customPaths = stripPathMarkers(rawOptions)
        val usageCounts = repository.getCategoryUsageSummaries()
            .associate { normalizeCategoryPath(it.path) to it.itemCount }
            .filterKeys { it.isNotBlank() }
        val rootPaths = DEFAULT_CATEGORY_ROOTS
            .filterNot { deletedMarkers.contains(it) }
            .map { editMarkers[it] ?: it }
        val allPaths = (rootPaths + DEFAULT_CATEGORY_SAMPLE_PATHS + customPaths + usageCounts.keys)
            .map(::normalizeCategoryPath)
            .filter { it.isNotBlank() }
            .distinct()
            .sortedWith(compareBy<String> { it.split(" / ").size }.thenBy { it })

        _uiState.value = CategoryManagementUiState(
            isLoading = false,
            allPaths = allPaths,
            rawOptions = rawOptions,
            iconMap = repository.getCategoryIconMap().mapKeys { normalizeCategoryPath(it.key) },
            usageCounts = usageCounts,
            message = message,
        )
    }

    private fun postMessage(message: String) {
        _uiState.value = _uiState.value.copy(message = message)
    }
}

class CategoryManagementViewModelFactory(
    private val repository: UnifiedItemRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryManagementViewModel::class.java)) {
            return CategoryManagementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
