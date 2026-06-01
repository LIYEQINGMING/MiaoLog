package com.example.itemmanagement.data.entity.unified

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import java.util.Date

/**
 * 统一物品实体 - 统一架构的核心表
 * 存储所有物品的基础信息，不区分状态
 */
@Entity(
    tableName = "unified_items",
    indices = [
        Index(value = ["isSubscription"]),
        Index(value = ["templateId"])
    ]
)
data class UnifiedItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** 物品名称 */
    val name: String,
    
    /** 分类 */
    val category: String,
    
    /** 子分类（可选）*/
    val subCategory: String? = null,
    
    /** 品牌（可选）*/
    val brand: String? = null,
    
    /** 规格说明（可选）*/
    val specification: String? = null,
    
    /** 自定义备注（可选）*/
    val customNote: String? = null,
    
    // === 物品固有属性（从详情表提升）===
    /** 容量/规格数值 */
    val capacity: Double? = null,
    
    /** 容量单位 */
    val capacityUnit: String? = null,
    
    /** 物品评分（0-5星）*/
    val rating: Double? = null,
    
    /** 适用季节 */
    val season: String? = null,
    
    /** 序列号/SKU */
    val serialNumber: String? = null,
    
    /** 地点地址 */
    val locationAddress: String? = null,
    
    /** 地点纬度 */
    val locationLatitude: Double? = null,
    
    /** 地点经度 */
    val locationLongitude: Double? = null,
    
    /** 货币单位 */
    val currencyCode: String = "CNY",
    
    /** 统计控制：是否不纳入总价值统计 */
    val excludeFromTotalValue: Boolean = false,
    
    /** 统计控制：是否不纳入总数量统计 */
    val excludeFromTotalCount: Boolean = false,
    
    /** 订阅制属性：是否为订阅制 */
    val isSubscription: Boolean = false,
    
    /** 订阅制属性：是否自动续费 */
    val autoRenew: Boolean = false,
    
    /** 订阅制属性：付款周期 (如 DAY, MONTH, QUARTER, YEAR) */
    val subscriptionCycle: String? = null,
    
    /** 模板关联：创建该物品使用的模板 ID */
    val templateId: Long? = null,
    
    /** 创建时间 */
    val createdDate: Date = Date(),
    
    /** 最后更新时间 */
    val updatedDate: Date = Date()
)

