package com.example.itemmanagement.data.model.template

import com.google.gson.Gson

/**
 * 兼容现有模板默认值 JSON 的轻量数据结构。
 * 当前仅承载“创建物品时带出的默认字段值”，不代表新的属性模板主模型。
 */
data class TemplateFieldDefaults(
    val singleValues: Map<String, String> = emptyMap(),
    val multiValues: Map<String, List<String>> = emptyMap()
) {
    companion object {
        fun fromJson(json: String?): TemplateFieldDefaults? {
            if (json.isNullOrBlank()) return null
            return try {
                Gson().fromJson(json, TemplateFieldDefaults::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}
