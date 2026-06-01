package com.example.itemmanagement.data.entity.unified

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 物品状态管理实体
 * 管理物品在不同状态之间的流转
 * 一个物品可以同时拥有多个状态（如同时在心愿单和购物清单中）
 */
@Entity(
    tableName = "item_states",
    foreignKeys = [
        ForeignKey(
            entity = UnifiedItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("itemId"),
        Index("statusId"),
        Index("isActive"),
        Index(value = ["itemId", "statusId", "isActive"]),  // 组合索引，优化常用查询
        Index("activatedDate"),
        Index("contextId")
    ]
)
data class ItemStateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** 关联的统一物品ID */
    val itemId: Long,
    
    /** 关联的状态定义ID */
    val statusId: Long = 1L,
    
    /** 遗留支持：旧的状态类型字符串 */
    val stateType: ItemStateType? = null,
    
    /** 是否激活（软删除标记） */
    val isActive: Boolean = true,
    
    /** 状态激活时间 */
    val activatedDate: Date = Date(),
    
    /** 状态停用时间（null表示仍激活） */
    val deactivatedDate: Date? = null,
    
    /** 上下文ID（可选）
     * - 对于SHOPPING状态：存储shopping_list_id
     * - 对于其他状态：通常为null
     */
    val contextId: Long? = null,
    
    /** 状态元数据（JSON格式存储额外信息） */
    val metadata: String? = null,
    
    /** 状态转换原因/备注 */
    val notes: String? = null,
    
    /** 创建时间 */
    val createdDate: Date = Date()
) {
    /**
     * 停用当前状态
     */
    fun deactivate(reason: String? = null): ItemStateEntity {
        return this.copy(
            isActive = false,
            deactivatedDate = Date(),
            notes = reason
        )
    }
    
    /**
     * 检查状态是否过期（激活超过30天且已停用）
     */
    fun isExpired(): Boolean {
        if (isActive || deactivatedDate == null) return false
        
        val now = System.currentTimeMillis()
        val deactivatedTime = deactivatedDate.time
        val daysPassed = (now - deactivatedTime) / (1000 * 60 * 60 * 24)
        
        return daysPassed > 30
    }
    
    /**
     * 获取状态持续时间（天数）
     */
    fun getDurationDays(): Int {
        val endTime = deactivatedDate?.time ?: System.currentTimeMillis()
        val startTime = activatedDate.time
        return ((endTime - startTime) / (1000 * 60 * 60 * 24)).toInt()
    }
    
    /**
     * 检查是否为购物清单状态
     */
    fun isShoppingState(): Boolean {
        return (statusId == ItemStatusDefinitionEntity.STATUS_NOT_PURCHASED || stateType == ItemStateType.SHOPPING) && contextId != null
    }
    
    /**
     * 获取状态显示标签
     */
    fun getStateDisplayLabel(): String {
        return when {
            statusId == ItemStatusDefinitionEntity.STATUS_NOT_PURCHASED || stateType == ItemStateType.SHOPPING -> if (contextId != null) "购物清单" else "待购买"
            statusId == ItemStatusDefinitionEntity.STATUS_IN_SERVICE || stateType == ItemStateType.INVENTORY -> "库存"
            statusId == ItemStatusDefinitionEntity.STATUS_RETIRED || stateType == ItemStateType.DELETED -> "已删除"
            statusId == ItemStatusDefinitionEntity.STATUS_EXPIRED -> "已过期"
            statusId == ItemStatusDefinitionEntity.STATUS_PENDING_FINAL_PAYMENT -> "待补款"
            else -> "未知状态"
        }
    }
}