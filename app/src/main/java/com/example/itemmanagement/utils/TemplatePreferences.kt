package com.example.itemmanagement.utils

import android.content.Context

object TemplatePreferences {
    fun getDefaultTemplateId(context: Context): Long {
        val prefs = context.getSharedPreferences("template_prefs", Context.MODE_PRIVATE)
        return prefs.getLong("default_template_id", -1L)
    }

    fun setDefaultTemplateId(context: Context, id: Long) {
        val prefs = context.getSharedPreferences("template_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("default_template_id", id).apply()
    }
}