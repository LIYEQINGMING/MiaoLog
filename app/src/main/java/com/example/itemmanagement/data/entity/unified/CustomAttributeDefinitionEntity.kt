package com.example.itemmanagement.data.entity.unified

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 物品自定义属性定义实体
 * 允许用户定义强类型的属性（如：价格类“定金”，日期类“保修截止日”）
 */
@Entity(tableName = "custom_attribute_definitions")
data class CustomAttributeDefinitionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** 属性名称（如：定金、尾款、保修截止日） */
    val name: String,
    
    /** 属性类型（PRICE, DATE, TEXT, NUMBER, BOOLEAN） */
    val type: String,
    
    /** 默认单位（如：元，天，个月） */
    val unit: String? = null,
    
    /** 是否系统预置（系统预置不可删除） */
    val isSystemBuiltIn: Boolean = false,
    
    /** 对应的图标名称 */
    val iconName: String? = null,
    
    /** 价格语义角色（仅PRICE类型使用） */
    val priceRole: String? = null,
    
    /** 支持的动作列表（JSON字符串，仅PRICE类型使用） */
    val supportedActions: String? = null,
    
    /** 是否支持周期规则（仅PRICE类型使用） */
    val supportsRecurrence: Boolean? = null,
    
    /** 默认动作值（JSON字符串，仅PRICE类型使用） */
    val defaultActions: String? = null,
    
    /** 默认币种（仅PRICE类型使用） */
    val defaultCurrency: String? = null
) {
    companion object {
        const val TYPE_PRICE = "PRICE"
        const val TYPE_DATE = "DATE"
        const val TYPE_TEXT = "TEXT"
        const val TYPE_NUMBER = "NUMBER"
        const val TYPE_BOOLEAN = "BOOLEAN"
        
        const val ROLE_PURCHASE_UNIT_PRICE = "PURCHASE_UNIT_PRICE"
        const val ROLE_PURCHASE_TOTAL_PRICE = "PURCHASE_TOTAL_PRICE"
        const val ROLE_DEPOSIT_PRICE = "DEPOSIT_PRICE"
        const val ROLE_BALANCE_PRICE = "BALANCE_PRICE"
        const val ROLE_SUBSCRIPTION_PRICE = "SUBSCRIPTION_PRICE"
        const val ROLE_CURRENT_VALUE_PRICE = "CURRENT_VALUE_PRICE"
        const val ROLE_CUSTOM_PRICE = "CUSTOM_PRICE"
        
        const val ACTION_INCLUDE_IN_TOTAL = "includeInTotal"
        const val ACTION_INCLUDE_IN_AVERAGE = "includeInAverage"
        const val ACTION_INCLUDE_IN_DAILY_VALUE = "includeInDailyValue"
    }
}