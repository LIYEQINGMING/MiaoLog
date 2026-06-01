package com.example.itemmanagement.data.model

data class HomeFunctionConfig(
    val showExpiringEntry: Boolean = true,
    val showExpiredEntry: Boolean = true,
    val showLowStockEntry: Boolean = true,
    val showShoppingListEntry: Boolean = true
) {
    fun hasAnyVisible(): Boolean {
        return showExpiringEntry || showExpiredEntry || showLowStockEntry || showShoppingListEntry
    }
}