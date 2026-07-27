package com.example.itemmanagement.ui.profile.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.itemmanagement.data.repository.AppSystemSourceRepository
import com.example.itemmanagement.data.repository.UserProfileRepository

/**
 * 应用设置ViewModel工厂类
 */
class AppSettingsViewModelFactory(
    private val userProfileRepository: UserProfileRepository,
    private val appSystemSourceRepository: AppSystemSourceRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppSettingsViewModel(userProfileRepository, appSystemSourceRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
