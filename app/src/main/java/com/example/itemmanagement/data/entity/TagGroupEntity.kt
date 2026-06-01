package com.example.itemmanagement.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 标签分组实体
 * 允许对标签进行分类管理
 */
@Entity(tableName = "tag_groups")
data class TagGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** 分组名称 */
    val name: String,
    
    /** 显示顺序 */
    val displayOrder: Int = 0,
    
    /** 创建时间 */
    val createdAt: Date = Date()
)