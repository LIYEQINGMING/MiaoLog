package com.example.itemmanagement.data.dao.attribute

import androidx.room.*
import com.example.itemmanagement.data.entity.attribute.RuleDefinitionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDefinitionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(definition: RuleDefinitionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(definitions: List<RuleDefinitionEntity>)
    
    @Update
    suspend fun update(definition: RuleDefinitionEntity)
    
    @Delete
    suspend fun delete(definition: RuleDefinitionEntity)
    
    @Query("SELECT * FROM rule_definitions ORDER BY createdAt DESC")
    fun getAllDefinitions(): Flow<List<RuleDefinitionEntity>>
    
    @Query("SELECT * FROM rule_definitions WHERE id = :id")
    suspend fun getDefinitionById(id: String): RuleDefinitionEntity?
    
    @Query("SELECT COUNT(*) FROM rule_definitions")
    suspend fun getCount(): Int
}
