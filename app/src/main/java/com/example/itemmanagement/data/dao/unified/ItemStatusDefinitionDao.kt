package com.example.itemmanagement.data.dao.unified

import androidx.room.*
import com.example.itemmanagement.data.entity.unified.ItemStatusDefinitionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 状态定义数据访问接口
 * 管理状态定义的增删改查
 */
@Dao
interface ItemStatusDefinitionDao {
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(statusDefinition: ItemStatusDefinitionEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(statusDefinitions: List<ItemStatusDefinitionEntity>): List<Long>
    
    @Update
    suspend fun update(statusDefinition: ItemStatusDefinitionEntity)
    
    @Delete
    suspend fun delete(statusDefinition: ItemStatusDefinitionEntity)
    
    @Query("SELECT * FROM item_status_definitions ORDER BY id ASC")
    fun getAllStatusDefinitions(): Flow<List<ItemStatusDefinitionEntity>>
    
    @Query("SELECT * FROM item_status_definitions ORDER BY id ASC")
    suspend fun getAllStatusDefinitionsSync(): List<ItemStatusDefinitionEntity>
    
    @Query("SELECT * FROM item_status_definitions WHERE id = :id")
    suspend fun getStatusDefinitionById(id: Long): ItemStatusDefinitionEntity?
    
    @Query("SELECT * FROM item_status_definitions WHERE isSystemBuiltIn = 1 ORDER BY id ASC")
    suspend fun getBuiltInStatusDefinitions(): List<ItemStatusDefinitionEntity>
    
    @Query("SELECT * FROM item_status_definitions WHERE isSystemBuiltIn = 0 ORDER BY id ASC")
    suspend fun getCustomStatusDefinitions(): List<ItemStatusDefinitionEntity>
}