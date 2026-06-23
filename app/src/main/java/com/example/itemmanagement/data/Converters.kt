package com.example.itemmanagement.data

import android.net.Uri
import androidx.room.TypeConverter
import com.example.itemmanagement.data.model.*
import com.example.itemmanagement.data.entity.WarrantyStatus
import com.example.itemmanagement.data.entity.BorrowStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromOpenStatus(value: OpenStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toOpenStatus(value: String?): OpenStatus? {
        return value?.let { OpenStatus.valueOf(it) }
    }

    @TypeConverter
    fun fromItemStatus(value: ItemStatus): String {
        return value.name
    }

    @TypeConverter
    fun toItemStatus(value: String): ItemStatus {
        return ItemStatus.valueOf(value)
    }

    @TypeConverter
    fun fromUriList(value: List<Uri>?): String {
        if (value == null) return ""
        return gson.toJson(value.map { it.toString() })
    }

    @TypeConverter
    fun toUriList(value: String): List<Uri> {
        if (value.isEmpty()) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        val stringList = gson.fromJson<List<String>>(value, type)
        return stringList.map { Uri.parse(it) }
    }

    // Calendar Event related converters
    @TypeConverter
    fun fromEventType(value: EventType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toEventType(value: String?): EventType? {
        return value?.let { EventType.valueOf(it) }
    }

    @TypeConverter
    fun fromPriority(value: Priority?): String? {
        return value?.name
    }

    @TypeConverter
    fun toPriority(value: String?): Priority? {
        return value?.let { Priority.valueOf(it) }
    }

    @TypeConverter
    fun fromRecurrenceType(value: RecurrenceType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toRecurrenceType(value: String?): RecurrenceType? {
        return value?.let { RecurrenceType.valueOf(it) }
    }

    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        return gson.toJson(value ?: emptyList<Int>())
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    // Warranty related converters
    @TypeConverter
    fun fromWarrantyStatus(value: WarrantyStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toWarrantyStatus(value: String?): WarrantyStatus? {
        return value?.let { WarrantyStatus.valueOf(it) }
    }

    // BorrowStatus 转换器
    @TypeConverter
    fun fromBorrowStatus(value: BorrowStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toBorrowStatus(value: String?): BorrowStatus? {
        return value?.let { BorrowStatus.valueOf(it) }
    }

    // Attribute related converters
    @TypeConverter
    fun fromAttributeOwnerType(value: com.example.itemmanagement.data.model.attribute.AttributeOwnerType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toAttributeOwnerType(value: String?): com.example.itemmanagement.data.model.attribute.AttributeOwnerType? {
        return value?.let { com.example.itemmanagement.data.model.attribute.AttributeOwnerType.valueOf(it) }
    }

    @TypeConverter
    fun fromAttributeValueType(value: com.example.itemmanagement.data.model.attribute.AttributeValueType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toAttributeValueType(value: String?): com.example.itemmanagement.data.model.attribute.AttributeValueType? {
        return value?.let { com.example.itemmanagement.data.model.attribute.AttributeValueType.valueOf(it) }
    }

    @TypeConverter
    fun fromAttributeOptionSource(value: com.example.itemmanagement.data.model.attribute.AttributeOptionSource?): String? {
        return value?.name
    }

    @TypeConverter
    fun toAttributeOptionSource(value: String?): com.example.itemmanagement.data.model.attribute.AttributeOptionSource? {
        return value?.let { com.example.itemmanagement.data.model.attribute.AttributeOptionSource.valueOf(it) }
    }

    @TypeConverter
    fun fromAttributeInputMode(value: com.example.itemmanagement.data.model.attribute.AttributeInputMode?): String? {
        return value?.name
    }

    @TypeConverter
    fun toAttributeInputMode(value: String?): com.example.itemmanagement.data.model.attribute.AttributeInputMode? {
        return value?.let { com.example.itemmanagement.data.model.attribute.AttributeInputMode.valueOf(it) }
    }

    @TypeConverter
    fun fromRuleComputationType(value: com.example.itemmanagement.data.model.attribute.RuleComputationType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toRuleComputationType(value: String?): com.example.itemmanagement.data.model.attribute.RuleComputationType? {
        return value?.let { com.example.itemmanagement.data.model.attribute.RuleComputationType.valueOf(it) }
    }
} 