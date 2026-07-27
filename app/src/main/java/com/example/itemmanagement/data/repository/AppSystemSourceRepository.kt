package com.example.itemmanagement.data.repository

import com.example.itemmanagement.data.model.attribute.AppSystemSourceSnapshot
import com.example.itemmanagement.data.model.attribute.AppSystemSourceValue
import com.example.itemmanagement.data.model.attribute.AttributeValueType
import com.example.itemmanagement.data.model.attribute.SystemVariableKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppSystemSourceRepository(
    private val unifiedItemRepository: UnifiedItemRepository,
    private val userProfileRepository: UserProfileRepository,
) {
    fun getDefaultCurrency(): String {
        return unifiedItemRepository.getBaseCurrencyCode()
    }

    fun saveDefaultCurrency(currencyCode: String) {
        unifiedItemRepository.setBaseCurrencyCode(currencyCode.trim().uppercase(Locale.ROOT))
    }

    suspend fun getSystemVariableValue(key: SystemVariableKey): AppSystemSourceValue {
        val now = Date()
        val timeZoneId = java.util.TimeZone.getDefault().id
        return when (key) {
            SystemVariableKey.ACCOUNT_CREATED_AT -> {
                val profile = userProfileRepository.getUserProfile()
                AppSystemSourceValue(
                    variableKey = key,
                    valueType = AttributeValueType.DATE,
                    dateValue = profile.joinDate,
                    textValue = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(profile.joinDate),
                )
            }

            SystemVariableKey.APP_MEMBERSHIP_DAYS -> {
                val profile = userProfileRepository.getUserProfile()
                val days = profile.getUsageDays().toDouble()
                AppSystemSourceValue(
                    variableKey = key,
                    valueType = AttributeValueType.NUMBER,
                    numberValue = days,
                )
            }

            SystemVariableKey.CURRENT_DATE -> AppSystemSourceValue(
                variableKey = key,
                valueType = AttributeValueType.DATE,
                textValue = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now),
                dateValue = now,
                configValues = mapOf("timezone" to timeZoneId),
            )

            SystemVariableKey.CURRENT_TIME -> AppSystemSourceValue(
                variableKey = key,
                valueType = AttributeValueType.TEXT,
                textValue = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now),
                dateValue = now,
                configValues = mapOf("timezone" to timeZoneId),
            )

            SystemVariableKey.CURRENT_CALENDAR_TIME -> AppSystemSourceValue(
                variableKey = key,
                valueType = AttributeValueType.DATE,
                textValue = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(now),
                dateValue = now,
                configValues = mapOf(
                    "timezone" to timeZoneId,
                    "weekStart" to "MONDAY",
                ),
            )

            SystemVariableKey.CURRENT_TIMEZONE -> AppSystemSourceValue(
                variableKey = key,
                valueType = AttributeValueType.TEXT,
                textValue = timeZoneId,
            )

            SystemVariableKey.CURRENT_USER_ID -> {
                val profile = userProfileRepository.getUserProfile()
                AppSystemSourceValue(
                    variableKey = key,
                    valueType = AttributeValueType.TEXT,
                    textValue = profile.userId,
                )
            }

            SystemVariableKey.CURRENT_LOCATION -> AppSystemSourceValue(
                variableKey = key,
                valueType = AttributeValueType.TEXT,
                textValue = null,
                isAvailable = false,
                availabilityMessage = "当前定位依赖权限与运行态定位能力，当前先保留变量定义，尚未接入实时定位读取",
                configValues = mapOf("precisionMode" to "COARSE"),
            )

            SystemVariableKey.GLOBAL_TOTAL_VALUE -> unifiedItemRepository.getGlobalTotalValueSnapshot()

            SystemVariableKey.GLOBAL_TOTAL_COUNT -> unifiedItemRepository.getGlobalTotalCountSnapshot()
        }
    }

    suspend fun getSnapshot(): AppSystemSourceSnapshot {
        return AppSystemSourceSnapshot(
            variables = SystemVariableKey.entries.map { key -> getSystemVariableValue(key) },
        )
    }
}
