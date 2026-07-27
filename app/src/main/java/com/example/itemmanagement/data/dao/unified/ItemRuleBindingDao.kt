package com.example.itemmanagement.data.dao.unified

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.itemmanagement.data.entity.unified.ItemRuleBindingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemRuleBindingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(binding: ItemRuleBindingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bindings: List<ItemRuleBindingEntity>)

    @Query("SELECT * FROM item_rule_bindings WHERE itemId = :itemId ORDER BY createdAt ASC, id ASC")
    fun observeByItemId(itemId: Long): Flow<List<ItemRuleBindingEntity>>

    @Query("SELECT * FROM item_rule_bindings WHERE itemId = :itemId ORDER BY createdAt ASC, id ASC")
    suspend fun getByItemId(itemId: Long): List<ItemRuleBindingEntity>

    @Query("DELETE FROM item_rule_bindings WHERE itemId = :itemId")
    suspend fun deleteByItemId(itemId: Long)
}
