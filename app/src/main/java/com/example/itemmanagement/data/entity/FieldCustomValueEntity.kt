package com.example.itemmanagement.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "field_custom_values")
data class FieldCustomValueEntity(
    @PrimaryKey
    val storageKey: String,
    val valuesJson: String,
    val updatedAt: Long = System.currentTimeMillis()
)