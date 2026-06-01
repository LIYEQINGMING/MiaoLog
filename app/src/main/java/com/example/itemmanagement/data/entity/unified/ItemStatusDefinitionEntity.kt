package com.example.itemmanagement.data.entity.unified

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 物品状态定义实体
 * 抽象原本硬编码的枚举状态，支持用户自定义扩展
 */
@Entity(tableName = "item_status_definitions")
data class ItemStatusDefinitionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** 状态名称（如：服役中、未购买、挂闲鱼） */
    val name: String,
    
    /** 是否为系统内置（内置状态不可被删除） */
    val isSystemBuiltIn: Boolean,
    
    /** 状态对应的 UI 颜色（如 #4CAF50） */
    val colorHex: String? = null,
    
    /** 状态对应的 UI 图标名 */
    val iconName: String? = null
) {
    companion object {
        const val STATUS_IN_SERVICE = 1L
        const val STATUS_NOT_PURCHASED = 2L
        const val STATUS_RETIRED = 3L
        const val STATUS_EXPIRED = 4L
        const val STATUS_PENDING_FINAL_PAYMENT = 5L
    }
}