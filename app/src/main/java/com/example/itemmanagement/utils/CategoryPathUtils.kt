package com.example.itemmanagement.utils

val DEFAULT_CATEGORY_ROOTS = listOf("食品", "药品", "日用品", "电子产品", "衣物", "文具", "其他")
val DEFAULT_CATEGORY_ICONS = listOf("📦", "💻", "🎧", "👕", "🍎", "💊", "🧴", "📝", "📚", "🏠", "🛠️", "🎮")
val DEFAULT_CATEGORY_SAMPLE_PATHS = listOf(
    "食品 / 零食",
    "食品 / 饮料",
    "药品 / 感冒药",
    "日用品 / 清洁用品",
    "电子产品 / 手机",
    "电子产品 / 充电器",
    "衣物 / 外套",
    "文具 / 剪刀",
    "文具 / 尺子",
    "文具 / 文件夹",
    "其他 / 工具",
)

const val CATEGORY_PATH_SEPARATOR = " / "

fun normalizeCategoryPath(path: String?): String {
    if (path.isNullOrBlank()) return ""
    return path
        .split("/", "\\")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(CATEGORY_PATH_SEPARATOR)
}

fun splitCategoryPath(path: String?): List<String> {
    val normalized = normalizeCategoryPath(path)
    if (normalized.isBlank()) return emptyList()
    return normalized.split(CATEGORY_PATH_SEPARATOR)
}

fun joinCategoryPath(parentPath: String?, childName: String): String {
    val normalizedChild = normalizeCategoryPath(childName)
    if (normalizedChild.isBlank()) return normalizeCategoryPath(parentPath)

    val normalizedParent = normalizeCategoryPath(parentPath)
    return if (normalizedParent.isBlank()) {
        normalizedChild
    } else {
        "$normalizedParent$CATEGORY_PATH_SEPARATOR$normalizedChild"
    }
}

fun categoryPathDisplayName(path: String?): String {
    return splitCategoryPath(path).lastOrNull().orEmpty()
}

fun categoryPathParent(path: String?): String {
    val segments = splitCategoryPath(path)
    if (segments.size <= 1) return ""
    return segments.dropLast(1).joinToString(CATEGORY_PATH_SEPARATOR)
}

fun categoryPathMatchesOrDescendant(path: String, targetPath: String): Boolean {
    val normalizedPath = normalizeCategoryPath(path)
    val normalizedTarget = normalizeCategoryPath(targetPath)
    if (normalizedPath.isBlank() || normalizedTarget.isBlank()) return false
    return normalizedPath == normalizedTarget ||
        normalizedPath.startsWith("$normalizedTarget$CATEGORY_PATH_SEPARATOR")
}

fun replaceCategoryPathPrefix(path: String, oldPrefix: String, newPrefix: String): String {
    val normalizedPath = normalizeCategoryPath(path)
    val normalizedOldPrefix = normalizeCategoryPath(oldPrefix)
    val normalizedNewPrefix = normalizeCategoryPath(newPrefix)
    if (normalizedPath == normalizedOldPrefix) {
        return normalizedNewPrefix
    }
    if (!normalizedPath.startsWith("$normalizedOldPrefix$CATEGORY_PATH_SEPARATOR")) {
        return normalizedPath
    }
    val suffix = normalizedPath.removePrefix("$normalizedOldPrefix$CATEGORY_PATH_SEPARATOR")
    return joinCategoryPath(normalizedNewPrefix, suffix)
}

fun extractDeletedPathMarkers(rawValues: List<String>): Set<String> {
    return rawValues.asSequence()
        .filter { it.startsWith("DELETED:") }
        .map { it.removePrefix("DELETED:") }
        .map(::normalizeCategoryPath)
        .filter { it.isNotBlank() }
        .toSet()
}

fun extractEditedPathMarkers(rawValues: List<String>): Map<String, String> {
    return rawValues.asSequence()
        .filter { it.startsWith("EDIT:") }
        .mapNotNull { marker ->
            val payload = marker.removePrefix("EDIT:")
            val parts = payload.split("->", limit = 2)
            if (parts.size != 2) return@mapNotNull null
            val oldPath = normalizeCategoryPath(parts[0])
            val newPath = normalizeCategoryPath(parts[1])
            if (oldPath.isBlank() || newPath.isBlank()) return@mapNotNull null
            oldPath to newPath
        }
        .toMap()
}

fun stripPathMarkers(rawValues: List<String>): List<String> {
    return rawValues
        .filterNot { it.startsWith("DELETED:") || it.startsWith("EDIT:") }
        .map(::normalizeCategoryPath)
        .filter { it.isNotBlank() }
}

fun combineCategoryPath(category: String?, subCategory: String?): String {
    val normalizedCategory = normalizeCategoryPath(category)
    val normalizedSubCategory = normalizeCategoryPath(subCategory)
    return when {
        normalizedCategory.isBlank() -> normalizedSubCategory
        normalizedSubCategory.isBlank() -> normalizedCategory
        normalizedCategory.endsWith("$CATEGORY_PATH_SEPARATOR$normalizedSubCategory") -> normalizedCategory
        else -> "$normalizedCategory$CATEGORY_PATH_SEPARATOR$normalizedSubCategory"
    }
}

data class CategoryPathNode(
    val path: String,
    val name: String,
    val hasChildren: Boolean,
)

fun buildCategoryChildNodes(
    allPaths: List<String>,
    parentPath: String?,
): List<CategoryPathNode> {
    val normalizedParent = normalizeCategoryPath(parentPath)
    val prefix = if (normalizedParent.isBlank()) "" else "$normalizedParent$CATEGORY_PATH_SEPARATOR"

    return allPaths
        .asSequence()
        .map(::normalizeCategoryPath)
        .filter { it.isNotBlank() && it != normalizedParent }
        .mapNotNull { fullPath ->
            if (prefix.isNotBlank()) {
                if (!fullPath.startsWith(prefix)) {
                    return@mapNotNull null
                }
                val remainder = fullPath.removePrefix(prefix)
                if (remainder.isBlank()) return@mapNotNull null
                val firstSegment = remainder.split(CATEGORY_PATH_SEPARATOR).first()
                joinCategoryPath(normalizedParent, firstSegment)
            } else {
                fullPath.split(CATEGORY_PATH_SEPARATOR).firstOrNull()
            }
        }
        .distinct()
        .sorted()
        .map { childPath ->
            val normalizedChildPath = normalizeCategoryPath(childPath)
            CategoryPathNode(
                path = normalizedChildPath,
                name = categoryPathDisplayName(normalizedChildPath),
                hasChildren = allPaths.any {
                    val normalized = normalizeCategoryPath(it)
                    normalized != normalizedChildPath &&
                        normalized.startsWith("$normalizedChildPath$CATEGORY_PATH_SEPARATOR")
                }
            )
        }
        .toList()
}

fun defaultCategoryIcon(categoryName: String): String {
    return when (categoryName) {
        "食品" -> "🍎"
        "药品" -> "💊"
        "日用品" -> "🧴"
        "电子产品" -> "💻"
        "音频设备" -> "🎧"
        "衣物" -> "👕"
        "文具" -> "📝"
        "其他" -> "📦"
        else -> "📦"
    }
}
