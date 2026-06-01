package com.example.itemmanagement.data.dao.template

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.itemmanagement.data.entity.template.ItemTemplateEntity

@Dao
interface ItemTemplateDao {
    @Query("SELECT * FROM item_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): ItemTemplateEntity?

    @Query("SELECT * FROM item_templates WHERE isVisible = 1 ORDER BY useCount DESC, lastUsedTime DESC")
    suspend fun getAllVisibleTemplates(): List<ItemTemplateEntity>

    @Query("UPDATE item_templates SET useCount = useCount + 1, lastUsedTime = :timestamp WHERE id = :id")
    suspend fun useTemplate(id: Long, timestamp: Long = System.currentTimeMillis())
    
    @Insert
    suspend fun insert(template: ItemTemplateEntity): Long
}