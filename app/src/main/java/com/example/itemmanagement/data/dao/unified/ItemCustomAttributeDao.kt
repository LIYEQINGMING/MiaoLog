package com.example.itemmanagement.data.dao.unified

import androidx.room.*
import com.example.itemmanagement.data.entity.unified.ItemCustomAttributeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemCustomAttributeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attribute: ItemCustomAttributeEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attributes: List<ItemCustomAttributeEntity>)
    
    @Update
    suspend fun update(attribute: ItemCustomAttributeEntity)
    
    @Delete
    suspend fun delete(attribute: ItemCustomAttributeEntity)
    
    @Query("SELECT * FROM item_custom_attributes WHERE itemId = :itemId")
    fun getAttributesByItemId(itemId: Long): Flow<List<ItemCustomAttributeEntity>>
    
    @Query("SELECT * FROM item_custom_attributes WHERE definitionId = :definitionId")
    suspend fun getAttributesByDefinitionId(definitionId: Long): List<ItemCustomAttributeEntity>
    
    @Query("SELECT * FROM item_custom_attributes")
    fun getAllFlow(): Flow<List<ItemCustomAttributeEntity>>

    @Query("DELETE FROM item_custom_attributes WHERE itemId = :itemId")
    suspend fun deleteByItemId(itemId: Long)
    
    @Query("DELETE FROM item_custom_attributes WHERE itemId = :itemId AND definitionId = :definitionId")
    suspend fun deleteByItemIdAndDefinitionId(itemId: Long, definitionId: Long)
}
