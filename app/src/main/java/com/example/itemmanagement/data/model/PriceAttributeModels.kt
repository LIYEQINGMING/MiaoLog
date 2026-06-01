package com.example.itemmanagement.data.model

import com.example.itemmanagement.data.entity.unified.CustomAttributeDefinitionEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class PriceActionConfig(
    val includeInTotal: Boolean? = null,
    val includeInAverage: Boolean? = null,
    val includeInDailyValue: Boolean? = null
)

data class PriceRecurrenceConfig(
    val isRecurring: Boolean? = null,
    val recurrenceType: String? = null,
    val autoRenew: Boolean? = null,
    val nextChargeDate: Long? = null
)

data class PriceAttributeValue(
    val amount: Double? = null,
    val currency: String? = null,
    val date: Long? = null,
    val actions: PriceActionConfig = PriceActionConfig(),
    val recurrence: PriceRecurrenceConfig = PriceRecurrenceConfig()
)

object PriceAttributeHelper {
    private val gson = Gson()

    fun parseSupportedActions(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun parseDefaultActions(json: String?): PriceActionConfig {
        if (json.isNullOrBlank()) return PriceActionConfig()
        return try {
            gson.fromJson(json, PriceActionConfig::class.java)
        } catch (e: Exception) {
            PriceActionConfig()
        }
    }

    fun toSupportedActionsJson(actions: List<String>): String {
        return gson.toJson(actions)
    }

    fun toDefaultActionsJson(config: PriceActionConfig): String {
        return gson.toJson(config)
    }

    fun createBuiltInPurchasePriceDefinition(): CustomAttributeDefinitionEntity {
        return CustomAttributeDefinitionEntity(
            name = "购入价格",
            type = CustomAttributeDefinitionEntity.TYPE_PRICE,
            priceRole = CustomAttributeDefinitionEntity.ROLE_PURCHASE_UNIT_PRICE,
            supportedActions = toSupportedActionsJson(
                listOf(
                    CustomAttributeDefinitionEntity.ACTION_INCLUDE_IN_TOTAL,
                    CustomAttributeDefinitionEntity.ACTION_INCLUDE_IN_AVERAGE,
                    CustomAttributeDefinitionEntity.ACTION_INCLUDE_IN_DAILY_VALUE
                )
            ),
            supportsRecurrence = false,
            defaultActions = toDefaultActionsJson(
                PriceActionConfig(
                    includeInTotal = true,
                    includeInAverage = true,
                    includeInDailyValue = true
                )
            ),
            defaultCurrency = "CNY",
            isSystemBuiltIn = true
        )
    }

    fun createBuiltInTotalPriceDefinition(): CustomAttributeDefinitionEntity {
        return CustomAttributeDefinitionEntity(
            name = "总价",
            type = CustomAttributeDefinitionEntity.TYPE_PRICE,
            priceRole = CustomAttributeDefinitionEntity.ROLE_PURCHASE_TOTAL_PRICE,
            supportedActions = toSupportedActionsJson(
                listOf(
                    CustomAttributeDefinitionEntity.ACTION_INCLUDE_IN_TOTAL
                )
            ),
            supportsRecurrence = false,
            defaultActions = toDefaultActionsJson(
                PriceActionConfig(
                    includeInTotal = true,
                    includeInAverage = false,
                    includeInDailyValue = false
                )
            ),
            defaultCurrency = "CNY",
            isSystemBuiltIn = true
        )
    }

    fun createBuiltInSubscriptionPriceDefinition(): CustomAttributeDefinitionEntity {
        return CustomAttributeDefinitionEntity(
            name = "订阅价格",
            type = CustomAttributeDefinitionEntity.TYPE_PRICE,
            priceRole = CustomAttributeDefinitionEntity.ROLE_SUBSCRIPTION_PRICE,
            supportedActions = toSupportedActionsJson(
                listOf(
                    CustomAttributeDefinitionEntity.ACTION_INCLUDE_IN_TOTAL
                )
            ),
            supportsRecurrence = true,
            defaultActions = toDefaultActionsJson(
                PriceActionConfig(
                    includeInTotal = false,
                    includeInAverage = false,
                    includeInDailyValue = false
                )
            ),
            defaultCurrency = "CNY",
            isSystemBuiltIn = true
        )
    }
}
