package com.example.itemmanagement.ui.components

import kotlin.LazyThreadSafetyMode

internal val materialSymbols_alert_chunk1: List<MaterialSymbolInfo> by lazy(LazyThreadSafetyMode.NONE) {
    listOf(
        MaterialSymbolInfo(name = "add_alert", codePoint = 57347, tags = listOf("+", "active", "add", "add attention", "add important", "add notification", "add reminder", "add warning")),
        MaterialSymbolInfo(name = "auto_delete", codePoint = 59980, tags = listOf("archive", "auto", "auto delete", "automatic", "bin", "can", "clean up", "clear")),
        MaterialSymbolInfo(name = "error", codePoint = 57344, tags = listOf("!", "alert", "attention", "bug", "caution", "circle", "critical", "danger")),
        MaterialSymbolInfo(name = "error_outline", codePoint = 57345, tags = listOf("!", "alert", "attention", "bug", "caution", "circle", "critical", "danger")),
        MaterialSymbolInfo(name = "notification_important", codePoint = 57348, tags = listOf("!", "active", "alarm", "alert", "app icon", "attention", "bell", "caution")),
        MaterialSymbolInfo(name = "warning", codePoint = 57346, tags = listOf("!", "alert", "app", "attention", "caution", "danger", "error", "exclamation")),
        MaterialSymbolInfo(name = "warning_amber", codePoint = 61571, tags = listOf("!", "alert", "amber", "app", "attention", "caution", "danger", "error"))
    )
}

internal val materialSymbols_alert_category: MaterialSymbolCategory by lazy(LazyThreadSafetyMode.NONE) {
    MaterialSymbolCategory(
        key = "alert",
        icons = buildList {
            addAll(materialSymbols_alert_chunk1)
        }
    )
}
