package com.example.itemmanagement.data.dao.unified

// Removed imports of ItemStateType
import androidx.room.*
import com.example.itemmanagement.data.entity.unified.ItemStateEntity
import com.example.itemmanagement.data.entity.unified.ItemStateType
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 状态统计数据类
 */
data class StateCountInfo(
    val statusId: Long,
    val count: Int
)

/**
 * 每日状态统计数据类
 */
data class DailyStateInfo(
    val date: String,
    val count: Int
)

/**
 * 物品状态数据访问接口
 * 管理物品状态的流转和查询
 */
@Dao
interface ItemStateDao {
    
    // === 基础CRUD操作 ===
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(state: ItemStateEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(states: List<ItemStateEntity>): List<Long>
    
    @Update
    suspend fun update(state: ItemStateEntity)
    
    @Delete
    suspend fun delete(state: ItemStateEntity)
    
    // === 状态查询 ===
    
    @Query("SELECT * FROM item_states WHERE itemId = :itemId ORDER BY activatedDate DESC")
    suspend fun getByItemId(itemId: Long): List<ItemStateEntity>
    
    @Query("SELECT * FROM item_states WHERE itemId = :itemId AND isActive = 1 ORDER BY activatedDate DESC")
    suspend fun getActiveStatesByItemId(itemId: Long): List<ItemStateEntity>
    
    @Query("SELECT * FROM item_states WHERE itemId = :itemId AND statusId = :statusId")
    suspend fun getStatesByItemIdAndType(itemId: Long, statusId: Long): List<ItemStateEntity>
    
    @Query("SELECT * FROM item_states WHERE itemId = :itemId AND statusId = :statusId AND isActive = 1")
    suspend fun getActiveStateByItemIdAndType(itemId: Long, statusId: Long): ItemStateEntity?
    
    @Query("SELECT * FROM item_states WHERE itemId = :itemId AND statusId = :statusId AND isActive = 0 ORDER BY deactivatedDate DESC")
    suspend fun getInactiveStatesByItemIdAndType(itemId: Long, statusId: Long): List<ItemStateEntity>
    
    @Query("SELECT * FROM item_states WHERE statusId = :statusId AND isActive = 1 ORDER BY activatedDate DESC")
    fun getActiveStatesByType(statusId: Long): Flow<List<ItemStateEntity>>
    
    @Query("SELECT DISTINCT itemId FROM item_states WHERE statusId = :statusId AND isActive = 1")
    suspend fun getItemIdsByActiveStateType(statusId: Long): List<Long>
    
    // === 状态管理操作 ===
    
    /**
     * 激活指定物品的指定状态类型
     */
    @Query("""
        UPDATE item_states 
        SET isActive = 1, activatedDate = :activateDate 
        WHERE itemId = :itemId AND statusId = :statusId
    """)
    suspend fun activateState(itemId: Long, statusId: Long, activateDate: Date = Date())
    
    /**
     * 停用指定物品的指定状态类型
     */
    @Query("""
        UPDATE item_states 
        SET isActive = 0, deactivatedDate = :deactivateDate, notes = :reason
        WHERE itemId = :itemId AND statusId = :statusId AND isActive = 1
    """)
    suspend fun deactivateState(
        itemId: Long, 
        statusId: Long, 
        reason: String? = null,
        deactivateDate: Date = Date()
    ): Int
    
    /**
     * 停用指定物品的所有状态
     */
    @Query("""
        UPDATE item_states 
        SET isActive = 0, deactivatedDate = :deactivateDate, notes = :reason
        WHERE itemId = :itemId AND isActive = 1
    """)
    suspend fun deactivateAllStates(
        itemId: Long, 
        reason: String? = null,
        deactivateDate: Date = Date()
    ): Int
    
    /**
     * 停用指定类型的所有状态
     */
    @Query("""
        UPDATE item_states 
        SET isActive = 0, deactivatedDate = :deactivateDate
        WHERE statusId = :statusId AND isActive = 1
    """)
    suspend fun deactivateAllStatesByType(statusId: Long, deactivateDate: Date = Date()): Int
    
    // === 状态检查 ===
    
    @Query("SELECT COUNT(*) > 0 FROM item_states WHERE itemId = :itemId AND statusId = :statusId AND isActive = 1")
    suspend fun hasActiveState(itemId: Long, statusId: Long): Boolean
    
    @Query("SELECT COUNT(*) FROM item_states WHERE itemId = :itemId AND isActive = 1")
    suspend fun getActiveStateCount(itemId: Long): Int
    
    @Query("SELECT statusId FROM item_states WHERE itemId = :itemId AND isActive = 1")
    suspend fun getActiveStateTypes(itemId: Long): List<Long>
    
    // === 购物清单相关查询 ===
    
    @Query("SELECT itemId FROM item_states WHERE statusId = :shoppingStatusId AND contextId = :shoppingListId AND isActive = 1")
    suspend fun getItemIdsByShoppingListId(shoppingListId: Long, shoppingStatusId: Long = 1L): List<Long>
    
    @Query("SELECT * FROM item_states WHERE statusId = :shoppingStatusId AND contextId = :shoppingListId AND isActive = 1 ORDER BY activatedDate DESC")
    fun getShoppingStatesByListId(shoppingListId: Long, shoppingStatusId: Long = 1L): Flow<List<ItemStateEntity>>
    
    // === 统计查询 ===
    
    @Query("SELECT COUNT(DISTINCT itemId) FROM item_states WHERE statusId = :statusId AND isActive = 1")
    suspend fun getActiveItemCountByStateType(statusId: Long): Int
    
    @Query("""
        SELECT statusId, COUNT(DISTINCT itemId) as count 
        FROM item_states 
        WHERE isActive = 1 
        GROUP BY statusId
    """)
    suspend fun getActiveItemCountByAllStates(): List<StateCountInfo>
    
    @Query("""
        SELECT DATE(activatedDate / 1000, 'unixepoch') as date, COUNT(*) as count
        FROM item_states 
        WHERE statusId = :statusId 
        AND activatedDate >= :startDate
        GROUP BY DATE(activatedDate / 1000, 'unixepoch')
        ORDER BY date DESC
    """)
    suspend fun getStateActivationHistory(statusId: Long, startDate: Date): List<DailyStateInfo>
    
    // === 清理操作 ===
    
    @Query("DELETE FROM item_states WHERE isActive = 0 AND deactivatedDate < :beforeDate")
    suspend fun cleanupOldInactiveStates(beforeDate: Date): Int
    
    @Query("DELETE FROM item_states WHERE itemId = :itemId")
    suspend fun deleteAllStatesForItem(itemId: Long): Int
    
    // === 事务操作 ===
    
    /**
     * 物品状态流转事务：停用旧状态，激活新状态
     */
    @Transaction
    suspend fun transitionItemState(
        itemId: Long,
        fromStatusId: Long,
        toStatusId: Long,
        contextId: Long? = null,
        reason: String? = null
    ) {
        // 停用原状态
        deactivateState(itemId, fromStatusId, "转换到新状态")
        
        // 激活新状态
        insert(ItemStateEntity(
            itemId = itemId,
            statusId = toStatusId,
            isActive = true,
            contextId = contextId,
            notes = reason
        ))
    }
    
    /**
     * 批量状态流转
     */
    @Transaction
    suspend fun batchTransitionStates(
        itemIds: List<Long>,
        fromStatusId: Long,
        toStatusId: Long,
        contextId: Long? = null
    ) {
        itemIds.forEach { itemId ->
            transitionItemState(itemId, fromStatusId, toStatusId, contextId)
        }
    }
}

/**
 * 状态激活统计数据类
 */
data class StateActivationStat(
    val date: String,
    val count: Int
)
