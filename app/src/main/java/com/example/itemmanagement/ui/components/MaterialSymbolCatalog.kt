package com.example.itemmanagement.ui.components

import kotlin.LazyThreadSafetyMode

data class MaterialSymbolInfo(
    val name: String,
    val codePoint: Int,
    val tags: List<String> = emptyList()
)

data class MaterialSymbolCategory(
    val key: String,
    val icons: List<MaterialSymbolInfo>
)

val MATERIAL_SYMBOL_CATEGORIES: List<MaterialSymbolCategory> by lazy(LazyThreadSafetyMode.NONE) {
    listOf(
        materialSymbols_home_category,
        materialSymbols_action_category,
        materialSymbols_content_category,
        materialSymbols_communication_category,
        materialSymbols_social_category,
        materialSymbols_notification_category,
        materialSymbols_av_category,
        materialSymbols_image_category,
        materialSymbols_maps_category,
        materialSymbols_places_category,
        materialSymbols_navigation_category,
        materialSymbols_device_category,
        materialSymbols_hardware_category,
        materialSymbols_editor_category,
        materialSymbols_file_category,
        materialSymbols_search_category,
        materialSymbols_toggle_category,
        materialSymbols_alert_category
    )
}

val MATERIAL_SYMBOL_LOOKUP: Map<String, MaterialSymbolInfo> by lazy(LazyThreadSafetyMode.NONE) {
    buildMap {
        MATERIAL_SYMBOL_CATEGORIES.forEach { category ->
            category.icons.forEach { icon ->
                put(icon.name, icon)
            }
        }
    }
}
