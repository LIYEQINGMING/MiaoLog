package com.example.itemmanagement.data.model.template

import com.google.gson.Gson

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