package com.example.itemmanagement.data.model.attribute

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 兼容当前物品表单中仍在使用的旧价格字段结构。
 *
 * 在最新设计里：
 * - 金额、日期等是属性输入值
 * - 是否计入总价、是否参与均价等应逐步迁移为规则绑定或规则输入
 * - 周期扣费也应逐步迁移为规则模型
 *
 * 因此这里不再继续承载“内置价格模板工厂”或“价格能力注册表”职责，
 * 只保留旧表单渲染和历史值解析所需的最小兼容结构。
 */
data class PriceActionConfig(
    val includeInTotal: Boolean? = null,
    val includeInAverage: Boolean? = null,
    val includeInDailyValue: Boolean? = null,
)

data class PriceRecurrenceConfig(
    val isRecurring: Boolean? = null,
    val recurrenceType: String? = null,
    val autoRenew: Boolean? = null,
    val nextChargeDate: Long? = null,
)

data class PriceAttributeValue(
    val amount: Double? = null,
    val currency: String? = null,
    val date: Long? = null,
    val actions: PriceActionConfig = PriceActionConfig(),
    val recurrence: PriceRecurrenceConfig = PriceRecurrenceConfig(),
)

object LegacyPriceFieldSupport {
    private val gson = Gson()

    fun parseSupportedActions(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
