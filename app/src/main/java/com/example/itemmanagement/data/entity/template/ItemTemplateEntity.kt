package com.example.itemmanagement.data.entity.template

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "item_templates")
data class ItemTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** 模板名称 */
    val templateName: String,
    
    /** 模板描述 */
    val description: String? = null,
    
    /** 模板图标 */
    val icon: String? = null,
    
    /** 基础字段配置（逗号分隔） */
    val selectedFields: String,
    
    /** 自定义属性配置列表 (JSON array of definitionIds) */
    val customAttributeIds: String? = null,
    
    /** 默认值配置 (JSON map) */
    val fieldDefaultValues: String? = null,
    
    /** 使用次数统计 */
    val useCount: Int = 0,
    
    /** 最后使用时间 */
    val lastUsedTime: Long = 0,
    
    /** 创建时间 */
    val createdAt: Date = Date(),
    
    /** 是否用户可见/可用（允许软删除） */
    val isVisible: Boolean = true
)