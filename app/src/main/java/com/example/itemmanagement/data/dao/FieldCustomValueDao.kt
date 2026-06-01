package com.example.itemmanagement.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.itemmanagement.data.entity.FieldCustomValueEntity

@Dao
interface FieldCustomValueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FieldCustomValueEntity)
    
    @Query("SELECT * FROM field_custom_values WHERE storageKey = :key")
    suspend fun getByKey(key: String): FieldCustomValueEntity?

    @Query("DELETE FROM field_custom_values WHERE storageKey = :key")
    suspend fun deleteByKey(key: String)
}