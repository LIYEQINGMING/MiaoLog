package com.example.itemmanagement.data.dao.unified

import androidx.room.*
import com.example.itemmanagement.data.entity.unified.GalleryEntity
import com.example.itemmanagement.data.entity.unified.GalleryItemCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface GalleryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gallery: GalleryEntity): Long
    
    @Update
    suspend fun update(gallery: GalleryEntity)
    
    @Delete
    suspend fun delete(gallery: GalleryEntity)
    
    @Query("SELECT * FROM galleries ORDER BY createdAt DESC")
    fun getAllGalleries(): Flow<List<GalleryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItemToGallery(crossRef: GalleryItemCrossRef)
    
    @Delete
    suspend fun removeItemFromGallery(crossRef: GalleryItemCrossRef)
    
    @Query("SELECT itemId FROM gallery_item_cross_ref WHERE galleryId = :galleryId ORDER BY displayOrder ASC")
    fun getItemIdsForGallery(galleryId: Long): Flow<List<Long>>
}