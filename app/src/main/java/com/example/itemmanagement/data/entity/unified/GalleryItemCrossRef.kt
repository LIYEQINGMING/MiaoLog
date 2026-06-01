package com.example.itemmanagement.data.entity.unified

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * 展厅与物品的多对多关系表
 */
@Entity(
    tableName = "gallery_item_cross_ref",
    primaryKeys = ["galleryId", "itemId"],
    foreignKeys = [
        ForeignKey(
            entity = GalleryEntity::class,
            parentColumns = ["id"],
            childColumns = ["galleryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UnifiedItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("galleryId"),
        Index("itemId")
    ]
)
data class GalleryItemCrossRef(
    val galleryId: Long,
    val itemId: Long,
    
    /** 在展厅中的展示顺序 */
    val displayOrder: Int = 0,
    
    /** 为该物品在当前展厅中的特定备注 */
    val customRemark: String? = null
)