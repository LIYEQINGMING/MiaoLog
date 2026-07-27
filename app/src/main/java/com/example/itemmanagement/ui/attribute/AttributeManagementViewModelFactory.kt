package com.example.itemmanagement.ui.attribute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.itemmanagement.data.repository.AppSystemSourceRepository
import com.example.itemmanagement.data.repository.AttributeRepository

class AttributeManagementViewModelFactory(
    private val repository: AttributeRepository,
    private val appSystemSourceRepository: AppSystemSourceRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttributeManagementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AttributeManagementViewModel(repository, appSystemSourceRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
