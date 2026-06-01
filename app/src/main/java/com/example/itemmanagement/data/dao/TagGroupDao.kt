package com.example.itemmanagement.data.dao

import androidx.room.*
import com.example.itemmanagement.data.entity.TagGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagGroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tagGroup: TagGroupEntity): Long
    
    @Update
    suspend fun update(tagGroup: TagGroupEntity)
    
    @Delete
    suspend fun delete(tagGroup: TagGroupEntity)
    
    @Query("SELECT * FROM tag_groups ORDER BY displayOrder ASC, name ASC")
    fun getAllTagGroups(): Flow<List<TagGroupEntity>>
    
    @Query("SELECT * FROM tag_groups WHERE id = :id")
    suspend fun getById(id: Long): TagGroupEntity?
}