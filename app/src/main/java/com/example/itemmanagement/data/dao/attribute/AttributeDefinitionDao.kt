package com.example.itemmanagement.data.dao.attribute

import androidx.room.*
import com.example.itemmanagement.data.entity.attribute.AttributeDefinitionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttributeDefinitionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(definition: AttributeDefinitionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(definitions: List<AttributeDefinitionEntity>)
    
    @Update
    suspend fun update(definition: AttributeDefinitionEntity)
    
    @Delete
    suspend fun delete(definition: AttributeDefinitionEntity)
    
    @Query("SELECT * FROM attribute_definitions ORDER BY createdAt DESC")
    fun getAllDefinitions(): Flow<List<AttributeDefinitionEntity>>
    
    @Query("SELECT * FROM attribute_definitions WHERE id = :id")
    suspend fun getDefinitionById(id: String): AttributeDefinitionEntity?
    
    @Query("SELECT COUNT(*) FROM attribute_definitions")
    suspend fun getCount(): Int
}
