package com.example.itemmanagement.data.entity.unified

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 物品自定义属性值实体
 * 记录某个物品具体的自定义属性值
 */
@Entity(
    tableName = "item_custom_attributes",
    foreignKeys = [
        ForeignKey(
            entity = UnifiedItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CustomAttributeDefinitionEntity::class,
            parentColumns = ["id"],
            childColumns = ["definitionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("itemId"),
        Index("definitionId"),
        Index(value = ["itemId", "definitionId"], unique = true)
    ]
)
data class ItemCustomAttributeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** 关联的物品ID */
    val itemId: Long,
    
    /** 关联的属性定义ID */
    val definitionId: Long,
    
    /** 文本值（用于 TEXT 类型） */
    val valueText: String? = null,
    
    /** 数字值（用于 NUMBER, PRICE, BOOLEAN 类型） */
    val valueNumber: Double? = null,
    
    /** 日期值（用于 DATE 类型） */
    val valueDate: Long? = null,
    
    /** 是否计入总价值/总数（特有配置：如“定金”计入总价值，而“快递费”不计入） */
    val includeInTotal: Boolean = true,
    
    /** 价格金额（仅PRICE类型使用） */
    val priceAmount: Double? = null,
    
    /** 币种（仅PRICE类型使用） */
    val priceCurrency: String? = null,
    
    /** 价格日期（仅PRICE类型使用） */
    val priceDate: Long? = null,
    
    /** 是否参与均价计算（仅PRICE类型使用） */
    val includeInAverage: Boolean? = null,
    
    /** 是否参与日均价值计算（仅PRICE类型使用） */
    val includeInDailyValue: Boolean? = null,
    
    /** 是否周期性（仅PRICE类型使用） */
    val isRecurring: Boolean? = null,
    
    /** 周期类型（仅PRICE类型使用） */
    val recurrenceType: String? = null,
    
    /** 是否自动续费（仅PRICE类型使用） */
    val autoRenew: Boolean? = null,
    
    /** 下次扣费日（仅PRICE类型使用） */
    val nextChargeDate: Long? = null
)