package com.example.itemmanagement.utils

import android.content.Context

data class UpdateInfo(
    val versionName: String,
    val versionCode: Int,
    val releaseNotes: String,
    val downloadUrl: String
)

object OnlineUpdateChecker {
    suspend fun checkForUpdate(context: Context): UpdateInfo? {
        return null // Return null to bypass for now
    }
}