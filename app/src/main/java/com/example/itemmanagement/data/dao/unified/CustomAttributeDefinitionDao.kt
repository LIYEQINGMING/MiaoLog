package com.example.itemmanagement.data.dao.unified

import androidx.room.*
import com.example.itemmanagement.data.entity.unified.CustomAttributeDefinitionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomAttributeDefinitionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(definition: CustomAttributeDefinitionEntity): Long
    
    @Update
    suspend fun update(definition: CustomAttributeDefinitionEntity)
    
    @Delete
    suspend fun delete(definition: CustomAttributeDefinitionEntity)
    
    @Query("SELECT * FROM custom_attribute_definitions")
    fun getAllDefinitions(): Flow<List<CustomAttributeDefinitionEntity>>
    
    @Query("SELECT * FROM custom_attribute_definitions WHERE type = :type")
    fun getDefinitionsByType(type: String): Flow<List<CustomAttributeDefinitionEntity>>
    
    @Query("SELECT * FROM custom_attribute_definitions WHERE id = :id")
    suspend fun getDefinitionById(id: Long): CustomAttributeDefinitionEntity?
}