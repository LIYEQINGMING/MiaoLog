package com.example.itemmanagement.data.repository

import com.example.itemmanagement.data.dao.template.ItemTemplateDao
import com.example.itemmanagement.data.entity.template.ItemTemplateEntity

class ItemTemplateRepository(private val dao: ItemTemplateDao) {
    suspend fun getTemplateById(id: Long): ItemTemplateEntity? {
        return dao.getTemplateById(id)
    }

    suspend fun getAllVisibleTemplates(): List<ItemTemplateEntity> {
        return dao.getAllVisibleTemplates()
    }

    suspend fun useTemplate(id: Long) {
        dao.useTemplate(id)
    }
}