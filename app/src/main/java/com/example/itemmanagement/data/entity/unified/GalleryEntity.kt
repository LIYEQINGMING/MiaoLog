package com.example.itemmanagement.data.entity.unified

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 展厅/画廊实体
 * 用于将多个物品组织成一个特定主题的展示集合
 */
@Entity(tableName = "galleries")
data class GalleryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** 展厅名称 */
    val name: String,
    
    /** 展厅描述 */
    val description: String? = null,
    
    /** 封面图片路径 */
    val coverImagePath: String? = null,
    
    /** 展厅布局样式（如：瀑布流、大图、网格） */
    val layoutStyle: String = "GRID",
    
    /** 创建时间 */
    val createdAt: Date = Date(),
    
    /** 更新时间 */
    val updatedAt: Date = Date(),
    
    /** 是否公开/可见 */
    val isVisible: Boolean = true
)