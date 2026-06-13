package com.example.itemmanagement.ui.components

import kotlin.LazyThreadSafetyMode

internal val materialSymbols_toggle_chunk1: List<MaterialSymbolInfo> by lazy(LazyThreadSafetyMode.NONE) {
    listOf(
        MaterialSymbolInfo(name = "check_box", codePoint = 59444, tags = listOf("activate", "agree", "approved", "binary", "box", "button", "check", "check box")),
        MaterialSymbolInfo(name = "check_box_outline_blank", codePoint = 59445, tags = listOf("blank", "boundary", "box", "button", "check", "checkbox", "choice", "component")),
        MaterialSymbolInfo(name = "indeterminate_check_box", codePoint = 59657, tags = listOf("app", "application", "box", "box with line", "button", "check", "checkbox", "checkbox with line")),
        MaterialSymbolInfo(name = "radio_button_checked", codePoint = 59447, tags = listOf("active", "app", "application", "bullet", "button", "checked", "checked circle", "checked state")),
        MaterialSymbolInfo(name = "radio_button_unchecked", codePoint = 59446, tags = listOf("blank radio button", "bullet", "button", "choice", "choose", "circle", "control", "deselect")),
        MaterialSymbolInfo(name = "star", codePoint = 59448, tags = listOf("add to favorite", "best", "bookmark", "border", "empty", "empty star", "favorite", "five pointed star")),
        MaterialSymbolInfo(name = "star_border", codePoint = 59450, tags = listOf("add to favorite", "best", "bookmark", "border", "empty", "empty star", "favorite", "five pointed star")),
        MaterialSymbolInfo(name = "star_border_purple500", codePoint = 61593, tags = listOf("500", "add to favorite", "best", "bookmark", "border", "empty", "empty star", "favorite")),
        MaterialSymbolInfo(name = "star_half", codePoint = 59449, tags = listOf("achievement", "assessment", "bookmark", "completion", "evaluation", "favorite", "favorite icon", "feedback")),
        MaterialSymbolInfo(name = "star_outline", codePoint = 61551, tags = listOf("add to favorite", "bookmark", "border", "empty", "empty star", "favorite", "five pointed star", "five points")),
        MaterialSymbolInfo(name = "star_purple500", codePoint = 61594, tags = listOf("500", "add to favorite", "best", "bookmark", "border", "empty", "empty star", "favorite")),
        MaterialSymbolInfo(name = "toggle_off", codePoint = 59893, tags = listOf("action", "active", "button", "choice", "circle", "components", "configuration", "control")),
        MaterialSymbolInfo(name = "toggle_on", codePoint = 59894, tags = listOf("activate", "active", "button", "circle", "component", "components", "configuration", "control"))
    )
}

internal val materialSymbols_toggle_category: MaterialSymbolCategory by lazy(LazyThreadSafetyMode.NONE) {
    MaterialSymbolCategory(
        key = "toggle",
        icons = buildList {
            addAll(materialSymbols_toggle_chunk1)
        }
    )
}
