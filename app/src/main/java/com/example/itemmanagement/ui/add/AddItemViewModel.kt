package com.example.itemmanagement.ui.add

import androidx.lifecycle.viewModelScope
import com.example.itemmanagement.data.entity.attribute.AttributeDefinitionEntity
import com.example.itemmanagement.data.model.attribute.AttributeDefinition
import com.example.itemmanagement.data.model.attribute.AttributeInputMode
import com.example.itemmanagement.data.model.attribute.AttributeOwnerType
import com.example.itemmanagement.data.model.attribute.AttributeValueSource
import com.example.itemmanagement.data.model.attribute.AttributeValueType
import com.example.itemmanagement.data.model.attribute.defaultValuePropertiesFor
import com.example.itemmanagement.data.model.attribute.toLegacyOptionSource
import com.example.itemmanagement.data.repository.UnifiedItemRepository
import com.example.itemmanagement.data.repository.WarrantyRepository
import com.example.itemmanagement.data.repository.AttributeRepository
import com.example.itemmanagement.data.entity.LocationEntity
import com.example.itemmanagement.data.entity.PhotoEntity
import com.example.itemmanagement.data.entity.TagEntity
import com.example.itemmanagement.data.entity.WarrantyEntity
import com.example.itemmanagement.data.entity.WarrantyStatus
import com.example.itemmanagement.data.entity.unified.ItemCustomAttributeEntity
import com.example.itemmanagement.data.entity.unified.UnifiedItemEntity
import com.example.itemmanagement.data.entity.unified.InventoryDetailEntity
import com.example.itemmanagement.data.entity.unified.ItemStateType
import com.example.itemmanagement.data.model.attribute.RuleDefinition
import kotlinx.coroutines.flow.first
import com.example.itemmanagement.data.mapper.toItemEntity
import com.example.itemmanagement.data.mapper.toLocationEntity
import com.example.itemmanagement.data.mapper.toItem
import com.example.itemmanagement.data.model.ItemStatus
import com.example.itemmanagement.data.model.OpenStatus
import com.example.itemmanagement.data.model.Item
import com.example.itemmanagement.ui.common.FieldProperties
import com.example.itemmanagement.ui.common.ValidationType  
import com.example.itemmanagement.ui.common.DisplayStyle
import com.example.itemmanagement.ui.base.BaseItemViewModel
import com.example.itemmanagement.ui.base.ItemStateCacheViewModel
import com.example.itemmanagement.ui.components.itemAttributeTypeKey
import com.example.itemmanagement.ui.components.itemBuildCustomAttributeEntities
import com.example.itemmanagement.ui.components.itemBuildFormField
import com.example.itemmanagement.ui.components.itemBuildBoundRuleBinding
import com.example.itemmanagement.ui.components.itemCurrencyOptions
import com.example.itemmanagement.ui.components.itemCustomFieldName
import com.example.itemmanagement.ui.components.itemCustomIncludeFieldName
import com.example.itemmanagement.ui.components.itemCustomMetaTypeFieldName
import com.example.itemmanagement.ui.components.itemEnrichFieldReference
import com.example.itemmanagement.ui.components.itemIsPriceAttribute
import com.example.itemmanagement.utils.combineCategoryPath
import com.example.itemmanagement.utils.normalizeCategoryPath
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import com.google.gson.Gson

/**
 * 添加物品 ViewModel
 * 
 * 继承自 BaseItemViewModel，专门处理添加新物品的业务逻辑。
 * 使用独立的缓存空间，确保与编辑模式的数据完全隔离。
 */
class AddItemViewModel(
    repository: UnifiedItemRepository,
    cacheViewModel: ItemStateCacheViewModel,
    private val attributeRepository: AttributeRepository,
    private val warrantyRepository: WarrantyRepository? = null  // 保修仓库（可选，保持向后兼容）
) : BaseItemViewModel(repository, cacheViewModel) {

    // 来源信息（用于转入流程）
    private var sourceType: String? = null
    private var sourceItemId: Long? = null
    private var sourceShoppingDetail: com.example.itemmanagement.data.entity.unified.ShoppingDetailEntity? = null
    private var customAttributeDefinitionsById: Map<String, AttributeDefinitionEntity> = emptyMap()
    private var availableAttributeDefinitions: List<AttributeDefinitionEntity> = emptyList()
    private var availableRuleDefinitions: List<RuleDefinition> = emptyList()
    private val transientAttributeDefinitions = mutableListOf<AttributeDefinitionEntity>()

    init {
        Log.d("AddItemViewModel", "=== 初始化添加ViewModel ===")
        Log.d("AddItemViewModel", "📦 WarrantyRepository状态: ${if (warrantyRepository != null) "已注入✅" else "未注入❌"}")
        
        // 初始化字段属性
        initializeAllFieldProperties()
        
        // 尝试从缓存恢复数据，如果有的话
        if (cacheViewModel.hasAddItemCache()) {
            Log.d("AddItemViewModel", "发现添加缓存，从缓存恢复数据")
            loadFromCache()
        } else {
            Log.d("AddItemViewModel", "没有添加缓存，字段将通过模板初始化")
            // 字段初始化现在由Fragment通过applyTemplate()完成
            // 不再在ViewModel的init中自动初始化默认字段
        }
        
        Log.d("AddItemViewModel", "添加ViewModel初始化完成，当前fieldValues: $fieldValues")
    }

    fun registerAttributeDefinitions(definitions: List<AttributeDefinitionEntity>) {
        availableAttributeDefinitions = (definitions + transientAttributeDefinitions).distinctBy { it.id }
        customAttributeDefinitionsById = availableAttributeDefinitions.associateBy { it.id }
        val enrichedFields = (_selectedFields.value ?: emptySet())
            .map { field -> itemEnrichFieldReference(field, availableAttributeDefinitions) }
            .toSet()
        if (enrichedFields != _selectedFields.value) {
            _selectedFields.value = enrichedFields
            _fieldVersion.value = (_fieldVersion.value ?: 0) + 1
            saveToCache()
        }
        syncSuggestedSystemRuleBindings()
    }

    override fun getTransientAttributeDefinitions(): List<AttributeDefinitionEntity> {
        return transientAttributeDefinitions.toList()
    }

    override suspend fun createQuickAttribute(
        name: String,
        valueType: AttributeValueType,
    ): AttributeDefinitionEntity? {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) {
            return null
        }

        val existing = (availableAttributeDefinitions + transientAttributeDefinitions)
            .firstOrNull { it.name == normalizedName }
        if (existing != null) {
            ensureQuickAttributeSelected(existing)
            return existing
        }

        val interactionMode = quickCreateInputMode(valueType)
        val valueProperties = defaultValuePropertiesFor(valueType)
        val definition = AttributeDefinition(
            id = "attr_quick_${UUID.randomUUID()}",
            key = "quick_${System.currentTimeMillis()}_${normalizedName.hashCode().toString().replace('-', 'n')}",
            name = normalizedName,
            ownerType = AttributeOwnerType.CUSTOM,
            valueType = valueType,
            valueSource = AttributeValueSource.INPUT,
            inputMode = interactionMode,
            interactionMode = interactionMode,
            optionSource = AttributeValueSource.INPUT.toLegacyOptionSource(valueProperties),
            valueProperties = valueProperties,
            optionItems = valueProperties.optionItems,
            isMultiValue = valueProperties.isMultiValue,
            icon = quickCreateIcon(valueType),
            ruleBindings = emptyList(),
            description = null,
        )
        attributeRepository.saveAttribute(definition)
        val entity = definition.toQuickCreatedEntity()
        transientAttributeDefinitions.removeAll { it.id == entity.id }
        transientAttributeDefinitions.add(0, entity)
        availableAttributeDefinitions = (availableAttributeDefinitions + entity).distinctBy { it.id }
        customAttributeDefinitionsById = (availableAttributeDefinitions + transientAttributeDefinitions)
            .distinctBy { it.id }
            .associateBy { it.id }
        ensureQuickAttributeSelected(entity)
        return entity
    }

    fun registerRuleDefinitions(rules: List<RuleDefinition>) {
        availableRuleDefinitions = rules
        syncSuggestedSystemRuleBindings()
    }

    override fun setSelectedFields(fields: Set<Field>) {
        super.setSelectedFields(fields)
        syncSuggestedSystemRuleBindings()
    }

    /**
     * 从购物清单加载物品数据（用于转入库存流程）
     */
    fun loadFromShoppingList(shoppingItemId: Long) {
        viewModelScope.launch {
            try {
                Log.d("AddItemViewModel", "开始从购物清单加载物品: itemId=$shoppingItemId")
                
                // 记录来源信息
                sourceType = "SHOPPING_LIST"
                sourceItemId = shoppingItemId
                
                // ⭐ 使用 Repository 的公共方法查询完整购物物品
                Log.d("AddItemViewModel", "通过 Repository 查询完整购物物品...")
                
                val item = repository.getCompleteShoppingItem(shoppingItemId)
                
                if (item == null) {
                    Log.e("AddItemViewModel", "❌ 未找到购物物品: itemId=$shoppingItemId")
                    _errorMessage.value = "未找到购物物品"
                    return@launch
                }
                
                Log.d("AddItemViewModel", "✓ 成功获取购物物品:")
                Log.d("AddItemViewModel", "  - 名称: ${item.name}")
                Log.d("AddItemViewModel", "  - 数量: ${item.quantity} ${item.unit}")
                Log.d("AddItemViewModel", "  - 照片: ${item.photos.size} 张")
                Log.d("AddItemViewModel", "  - 标签: ${item.tags.size} 个")
                Log.d("AddItemViewModel", "  - 购物详情: ${if (item.shoppingDetail != null) "存在" else "不存在"}")
                
                // 加载物品数据
                loadItemData(item)
                
                
            } catch (e: Exception) {
                Log.e("AddItemViewModel", "加载购物物品失败", e)
                _errorMessage.value = "加载失败: ${e.message}"
            }
        }
    }

    /**
     * 加载物品数据到字段（提取为独立方法以便复用）
     */
    private fun loadItemData(item: Item) {
        val shoppingDetail = item.shoppingDetail
        if (shoppingDetail == null) {
            Log.e("AddItemViewModel", "购物详情为空: itemId=${item.id}")
            _errorMessage.value = "购物详情为空"
            return
        }
        
        // 保存购物详情，用于后续转换
        sourceShoppingDetail = shoppingDetail
        
        Log.d("AddItemViewModel", "开始预填充字段...")
        
        // 预填充基础信息
        fieldValues["名称"] = item.name
        Log.d("AddItemViewModel", "  ✓ 名称 = ${item.name}")
        
        val categoryPath = combineCategoryPath(item.category, item.subCategory)
        fieldValues["分类"] = categoryPath.ifBlank { item.category }
        Log.d("AddItemViewModel", "  ✓ 分类 = ${fieldValues["分类"]}")
        item.brand?.let { 
            fieldValues["品牌"] = it 
            Log.d("AddItemViewModel", "  ✓ 品牌 = $it")
        }
        item.specification?.let { 
            fieldValues["规格"] = it 
            Log.d("AddItemViewModel", "  ✓ 规格 = $it")
        }
        item.customNote?.let { 
            fieldValues["备注"] = it 
            Log.d("AddItemViewModel", "  ✓ 备注 = $it")
        }
        
        // 预填充数量和单位
        fieldValues["数量"] = shoppingDetail.quantity.toString()
        fieldValues["数量_unit"] = shoppingDetail.quantityUnit
        Log.d("AddItemViewModel", "  ✓ 数量 = ${shoppingDetail.quantity} ${shoppingDetail.quantityUnit}")
        
        // 预填充价格信息（优先使用实际价格，否则使用预估价格）
        val price = shoppingDetail.actualPrice ?: shoppingDetail.estimatedPrice
        val priceUnit = if (shoppingDetail.actualPrice != null) {
            shoppingDetail.actualPriceUnit
        } else {
            shoppingDetail.estimatedPriceUnit
        }
        price?.let {
            fieldValues["单价"] = it.toString()
            fieldValues["单价_unit"] = priceUnit
            Log.d("AddItemViewModel", "  ✓ 单价 = $it $priceUnit")
        }
        
        // 预填充其他字段
        shoppingDetail.storeName?.let { 
            fieldValues["商家名称"] = it 
            Log.d("AddItemViewModel", "  ✓ 商家名称 = $it")
        }
        item.capacity?.let { 
            fieldValues["容量"] = it.toString() 
            Log.d("AddItemViewModel", "  ✓ 容量 = $it")
        }
        item.capacityUnit?.let { 
            fieldValues["容量_unit"] = it 
            Log.d("AddItemViewModel", "  ✓ 容量单位 = $it")
        }
        item.rating?.let { 
            fieldValues["评分"] = it.toFloat() 
            Log.d("AddItemViewModel", "  ✓ 评分 = $it")
        }
        item.season?.let { 
            fieldValues["季节"] = it.split(",").toSet() 
            Log.d("AddItemViewModel", "  ✓ 季节 = $it")
        }
        item.serialNumber?.let { 
            fieldValues["序列号"] = it 
            Log.d("AddItemViewModel", "  ✓ 序列号 = $it")
        }
        
        // 加载照片
        if (item.photos.isNotEmpty()) {
            Log.d("AddItemViewModel", "开始加载 ${item.photos.size} 张照片...")
            val photoUriList = item.photos.mapNotNull { photo ->
                try {
                    android.net.Uri.parse(photo.uri).also {
                        Log.d("AddItemViewModel", "  ✓ 照片URI: ${photo.uri}")
                    }
                } catch (e: Exception) {
                    Log.e("AddItemViewModel", "  ✗ 解析照片URI失败: ${photo.uri}", e)
                    null
                }
            }
            _photoUris.value = photoUriList
            Log.d("AddItemViewModel", "✅ 照片加载完成: ${photoUriList.size} 张")
        } else {
            Log.d("AddItemViewModel", "没有照片需要加载")
        }
        
        // 加载标签
        if (item.tags.isNotEmpty()) {
            Log.d("AddItemViewModel", "开始加载 ${item.tags.size} 个标签...")
            val tagsByCategory = item.tags.groupBy { "默认" }.mapValues { entry ->
                entry.value.map { it.name }.toSet()
            }
            _selectedTags.value = tagsByCategory
            Log.d("AddItemViewModel", "✅ 标签加载完成")
        } else {
            Log.d("AddItemViewModel", "没有标签需要加载")
        }
        
        Log.d("AddItemViewModel", "✅ 预填充完成，字段数: ${fieldValues.size}")
        
        // 触发UI更新
        Log.d("AddItemViewModel", "触发UI更新...")
        _selectedFields.value = _selectedFields.value
        Log.d("AddItemViewModel", "UI更新已触发")
    }

    // --- 实现抽象方法 ---

    override fun getCurrentCache(): Any {
        return cacheViewModel.getAddItemCache()
    }

    override fun getCacheKey(): String {
        return "ADD_ITEM"
    }

    override fun loadDataFromCache() {
        Log.d("AddItemViewModel", "开始从缓存加载数据")
        val cache = cacheViewModel.getAddItemCache()
        Log.d("AddItemViewModel", "缓存内容: fieldValues=${cache.fieldValues}, selectedFields=${cache.selectedFields}")
        // 类型安全的缓存加载，不需要类型转换
        fieldValues = cache.fieldValues.toMutableMap()
        _selectedFields.value = cache.selectedFields
        _photoUris.value = cache.photoUris
        _selectedTags.value = cache.selectedTags
        _itemRuleBindings.value = cache.ruleBindings
        customOptionsMap = cache.customOptions.toMutableMap()
        customUnitsMap = cache.customUnits.toMutableMap()
        customTagsMap = cache.customTags.toMutableMap()
        Log.d("AddItemViewModel", "缓存加载完成，当前fieldValues: $fieldValues")
    }

    override fun saveDataToCache() {
        val cache = cacheViewModel.getAddItemCache()
        // 类型安全的缓存保存，不需要类型转换
        cache.fieldValues = fieldValues.toMutableMap()
        cache.selectedFields = _selectedFields.value ?: setOf()
        cache.photoUris = _photoUris.value ?: emptyList()
        cache.selectedTags = _selectedTags.value ?: mapOf()
        cache.ruleBindings = _itemRuleBindings.value ?: emptyList()
        cache.customOptions = customOptionsMap.toMutableMap()
        cache.customUnits = customUnitsMap.toMutableMap()
        cache.customTags = customTagsMap.toMutableMap()
    }

    override suspend fun saveOrUpdateItem() {
        Log.d("AddItemViewModel", "开始保存物品数据")
        
        // 详细记录所有字段值
        Log.d("AddItemViewModel", "=== 保存前字段值详情 ===")
        fieldValues.forEach { (key, value) ->
            Log.d("AddItemViewModel", "字段 '$key': $value (${value?.javaClass?.simpleName})")
        }
        
        // 验证数据
        val item = buildItemFromFields()
        Log.d("AddItemViewModel", "构建的Item对象: $item")
        
        val (isValid, errorMessage) = validateItem(item)
        
        if (!isValid) {
            Log.e("AddItemViewModel", "数据验证失败: $errorMessage")
            _errorMessage.value = errorMessage ?: "数据验证失败"
            _saveResult.value = false
            return
        }

        try {
            // 检查是否是从购物清单转入
            if (sourceType == "SHOPPING_LIST" && sourceItemId != null && sourceItemId!! > 0 && sourceShoppingDetail != null) {
                // 从购物清单转入库存
                Log.d("AddItemViewModel", "从购物清单转入库存: itemId=$sourceItemId")
                
                // 构建更新后的 UnifiedItem（用户可能修改了部分字段）
                val updatedUnifiedItem = buildUnifiedItemFromFields().copy(id = sourceItemId!!)
                
                // 更新 UnifiedItem
                repository.updateUnifiedItem(updatedUnifiedItem)
                
                // 构建库存详情
                val inventoryDetail = buildInventoryDetailFromFields().copy(itemId = sourceItemId!!)
                
                // 标记购物详情为已购买
                val updatedShoppingDetail = sourceShoppingDetail!!.copy(
                    isPurchased = true,
                    purchaseDate = Date()
                )
                repository.updateShoppingDetail(updatedShoppingDetail)
                
                // 执行状态转换
                repository.transferShoppingToInventory(
                    itemId = sourceItemId!!,
                    shoppingDetail = updatedShoppingDetail,
                    inventoryDetail = inventoryDetail
                )
                
                // 提取并保存高级自定义属性
                val customAttributes = buildCustomAttributeEntities(sourceItemId!!)
                if (customAttributes.isNotEmpty()) {
                    repository.saveCustomAttributes(customAttributes)
                }
                repository.replaceItemRuleBindings(sourceItemId!!, _itemRuleBindings.value ?: emptyList())
                
                // 🛒 添加日历事件：记录购物清单转入库存操作
                addCalendarEventForShoppingTransferred(sourceItemId!!, updatedUnifiedItem.name, updatedUnifiedItem.category)
                
                Log.d("AddItemViewModel", "购物清单转入成功")
            } else {
                // 正常添加新物品
                val unifiedItem = buildUnifiedItemFromFields()
                val inventoryDetail = buildInventoryDetailFromFields()
                
                android.util.Log.d("AddItemViewModel", "🔧 构建完成的InventoryDetail: locationId=${inventoryDetail.locationId}")
                
                // 构建标签列表
                val tags = buildTagsFromFields()
                android.util.Log.d("AddItemViewModel", "🏷️ 构建的标签列表: ${tags.map { it.name }}")
                
                // 构建照片列表
                val photos = buildPhotosFromUris()
                android.util.Log.d("AddItemViewModel", "📸 构建的照片列表: ${photos.size}张")
                
                // 保存到数据库（使用新的统一架构）并获取itemId
                val itemId = repository.addInventoryItem(unifiedItem, inventoryDetail, tags, photos)
                android.util.Log.d("AddItemViewModel", "✅ 物品保存成功: itemId=$itemId")
                
                // 提取并保存高级自定义属性
                val customAttributes = buildCustomAttributeEntities(itemId)
                if (customAttributes.isNotEmpty()) {
                    repository.saveCustomAttributes(customAttributes)
                    android.util.Log.d("AddItemViewModel", "✅ 高级自定义属性保存成功: ${customAttributes.size}个")
                }
                repository.replaceItemRuleBindings(itemId, _itemRuleBindings.value ?: emptyList())
                
                // 打印保修相关字段值
                android.util.Log.d("AddItemViewModel", "📋 保修字段检查:")
                android.util.Log.d("AddItemViewModel", "  - 保修期字段值: ${fieldValues["保修期"]}")
                android.util.Log.d("AddItemViewModel", "  - 保修期单位字段值: ${fieldValues["保修期_unit"]}")
                android.util.Log.d("AddItemViewModel", "  - 保修到期时间字段值: ${fieldValues["保修到期时间"]}")
                android.util.Log.d("AddItemViewModel", "  - 购买日期字段值: ${fieldValues["购买日期"]}")
                
                // 保存保修信息（如果有）
                saveWarrantyInfoIfNeeded(itemId)
                
                // 📦 添加日历事件：记录添加物品操作
                addCalendarEventForItemAdded(itemId, unifiedItem.name, unifiedItem.category)
            }
            
            // 物品保存成功
            
            _saveResult.value = true
            _errorMessage.value = "物品添加成功"
            cacheViewModel.clearAddItemCache()
            
        } catch (e: Exception) {
            Log.e("AddItemViewModel", "保存失败", e)
            _errorMessage.value = e.message ?: "添加失败：未知错误"
            _saveResult.value = false
        }
    }

    private fun syncSuggestedSystemRuleBindings() {
        if (availableAttributeDefinitions.isEmpty() || availableRuleDefinitions.isEmpty()) {
            return
        }

        val selectedFieldNames = (_selectedFields.value ?: emptySet()).map { it.name }.toSet()
        val definitionsByKey = availableAttributeDefinitions.associateBy { it.key }
        val currentBindings = _itemRuleBindings.value ?: emptyList()
        val updatedBindings = currentBindings.toMutableList()

        fun hasFieldContext(fieldName: String): Boolean {
            return fieldName in selectedFieldNames || fieldValues.containsKey(fieldName)
        }

        fun ensureRuleBinding(
            ruleId: String,
            shouldAdd: Boolean,
            slotAttributeKeys: Map<String, String>,
        ) {
            if (!shouldAdd || updatedBindings.any { it.ruleId == ruleId }) {
                return
            }
            val rule = availableRuleDefinitions.firstOrNull { it.id == ruleId || it.key == ruleId } ?: return
            updatedBindings += itemBuildBoundRuleBinding(
                rule = rule,
                slotAttributeDefinitionsBySlotKey = slotAttributeKeys.mapValues { (_, attributeKey) ->
                    definitionsByKey[attributeKey]
                },
            )
        }

        ensureRuleBinding(
            ruleId = "rule_system_include_total_count",
            shouldAdd = hasFieldContext("数量"),
            slotAttributeKeys = mapOf(
                "quantity" to "item_quantity",
            ),
        )
        ensureRuleBinding(
            ruleId = "rule_system_include_total_price",
            shouldAdd = hasFieldContext("单价"),
            slotAttributeKeys = mapOf(
                "amount" to "purchase_price",
            ),
        )
        ensureRuleBinding(
            ruleId = "rule_system_periodic_charge",
            shouldAdd = hasFieldContext("扣费周期") || hasFieldContext("扣费日"),
            slotAttributeKeys = mapOf(
                "amount" to "purchase_price",
                "billingCycle" to "item_billing_cycle",
                "billingDay" to "item_billing_day",
            ),
        )

        if (updatedBindings != currentBindings) {
            super.setItemRuleBindings(updatedBindings)
        }
    }

    // --- 私有辅助方法 ---

    /**
     * 从字段值构建UnifiedItemEntity对象
     */
    private fun buildUnifiedItemFromFields(): UnifiedItemEntity {
        val name = (fieldValues["名称"] as? String)?.trim() ?: ""
        val category = normalizeCategoryPath(fieldValues["分类"] as? String).ifBlank { "未指定" }
        val brand = (fieldValues["品牌"] as? String)?.takeIf { it.isNotBlank() }
        val specification = (fieldValues["规格"] as? String)?.takeIf { it.isNotBlank() }
        val customNote = (fieldValues["备注"] as? String)?.takeIf { it.isNotBlank() }
        
        // 提取capacity、rating、season、serialNumber（现在属于UnifiedItemEntity）
        val capacity = (fieldValues["容量"] as? String)?.toDoubleOrNull()
        val capacityUnit = fieldValues["容量_unit"] as? String
        val rating = when (val ratingValue = fieldValues["评分"]) {
            is Float -> ratingValue.toDouble()
            is Double -> ratingValue
            is String -> ratingValue.toDoubleOrNull()
            else -> null
        }
        val seasonSet = when (val seasonValue = fieldValues["季节"]) {
            is Set<*> -> seasonValue.filterIsInstance<String>().toSet()
            is String -> seasonValue.split(",").map { it.trim() }.toSet()
            else -> emptySet()
        }
        val season = if (seasonSet.isNotEmpty()) seasonSet.joinToString(",") else null
        val serialNumber = fieldValues["序列号"] as? String
        
        // GPS地点信息
        val locationAddress = (fieldValues["地点"] as? String).also {
            android.util.Log.d("AddItemViewModel", "📍 保存地点地址: $it")
        }
        val locationLatitude = (fieldValues["地点_纬度"] as? String)?.toDoubleOrNull().also {
            android.util.Log.d("AddItemViewModel", "📍 保存地点纬度: $it")
        }
        val locationLongitude = (fieldValues["地点_经度"] as? String)?.toDoubleOrNull().also {
            android.util.Log.d("AddItemViewModel", "📍 保存地点经度: $it")
        }
        val createdDate = parseDate(fieldValues["添加日期"] as? String) ?: Date()
        val currencyCode = (fieldValues["币种"] as? String)
            ?.takeIf { it.isNotBlank() }
            ?: (fieldValues["单价_unit"] as? String)?.takeIf { it.isNotBlank() }
            ?: "CNY"
        val templateId = when (val rawTemplateId = fieldValues["模板ID"]) {
            is Long -> rawTemplateId
            is Int -> rawTemplateId.toLong()
            is String -> rawTemplateId.toLongOrNull()
            else -> null
        }?.takeIf { it > 0 }
        val includeTotalValueEnabled = hasActiveRuleBinding("rule_system_include_total_price")
        val includeTotalCountEnabled = hasActiveRuleBinding("rule_system_include_total_count")
        val periodicChargeEnabled = hasActiveRuleBinding("rule_system_periodic_charge")
        val hasPeriodicChargeRule = hasAnyRuleBinding("rule_system_periodic_charge")
        val subscriptionCycle = (fieldValues["扣费周期"] as? String)?.takeIf { it.isNotBlank() }
        val billingDay = (fieldValues["扣费日"] as? String)?.takeIf { it.isNotBlank() }
        
        return UnifiedItemEntity(
            id = 0, // 新物品，ID为0
            name = name,
            category = category,
            subCategory = null,
            brand = brand,
            specification = specification,
            customNote = customNote,
            capacity = capacity,
            capacityUnit = capacityUnit,
            rating = rating,
            season = season,
            serialNumber = serialNumber,
            locationAddress = locationAddress,
            locationLatitude = locationLatitude,
            locationLongitude = locationLongitude,
            currencyCode = currencyCode,
            excludeFromTotalValue = !includeTotalValueEnabled,
            excludeFromTotalCount = !includeTotalCountEnabled,
            isSubscription = hasPeriodicChargeRule || !subscriptionCycle.isNullOrBlank() || !billingDay.isNullOrBlank(),
            autoRenew = periodicChargeEnabled,
            subscriptionCycle = subscriptionCycle,
            templateId = templateId,
            createdDate = createdDate,
            updatedDate = Date()
        ).also {
            android.util.Log.d("AddItemViewModel", "📍 构建的UnifiedItemEntity - 地点: ${it.locationAddress}, 纬度: ${it.locationLatitude}, 经度: ${it.locationLongitude}")
        }
    }

    /**
     * 从字段值构建InventoryDetailEntity对象
     */
    private suspend fun buildInventoryDetailFromFields(): InventoryDetailEntity {
        android.util.Log.d("AddItemViewModel", "🔧 开始构建InventoryDetailEntity")
        
        // 基础字段 - 处理数量
        val rawQuantityStr = (fieldValues["数量"] as? String)?.trim()
        val quantityStr = rawQuantityStr?.takeIf { it.isNotEmpty() }
        
        val quantity: Double
        val quantityUnit: String
        val isQuantityUserInput: Boolean
        
        if (quantityStr != null) {
            // 用户填写了数量
            quantity = quantityStr.toDoubleOrNull() ?: 1.0
            quantityUnit = (fieldValues["数量_unit"] as? String)?.takeIf { it.isNotBlank() } ?: "个"
            isQuantityUserInput = true
        } else {
            // 用户没有填写数量，使用默认值
            quantity = 1.0
            quantityUnit = "个"
            isQuantityUserInput = false
        }
        
        // 位置信息
        android.util.Log.d("AddItemViewModel", "📍 开始提取位置信息...")
        val locationId = extractLocationId()
        android.util.Log.d("AddItemViewModel", "📍 位置ID结果: $locationId")
        
        // 日期字段
        val productionDate = parseDate(fieldValues["生产日期"] as? String)
        val expirationDate = parseDate(fieldValues["保质过期时间"] as? String)
        val purchaseDate = parseDate(fieldValues["购买日期"] as? String)
        // 保修信息已移至 WarrantyEntity，不再存储在 InventoryDetailEntity
        // val warrantyEndDate = parseDate(fieldValues["保修到期时间"] as? String)
        
        // 开封状态
        val openStatus = when (fieldValues["开封状态"] as? String) {
            "已开封" -> OpenStatus.OPENED
            "未开封" -> OpenStatus.UNOPENED
            else -> null
        }
        
        // 价格信息
        val price = (fieldValues["单价"] as? String)?.toDoubleOrNull()
        val priceUnit = fieldValues["单价_unit"] as? String ?: "元"
        val totalPrice = (fieldValues["总价"] as? String)?.toDoubleOrNull()
        val totalPriceUnit = fieldValues["总价_unit"] as? String ?: "元"
        
        // 其他字段
        val stockWarningThreshold = (fieldValues["库存预警值"] as? String)?.toIntOrNull()
        val purchaseChannel = fieldValues["购买渠道"] as? String
        val storeName = fieldValues["商家名称"] as? String
        // 注意：capacity, rating, season, serialNumber 已移至 UnifiedItemEntity
        
        // 期限字段
        val shelfLife = parsePeriodFieldToDays("保质期")
        
        val warrantyPeriod = when (val warrantyValue = fieldValues["保修期"]) {
            is Pair<*, *> -> (warrantyValue.first as? String)?.toIntOrNull()
            is String -> warrantyValue.toIntOrNull()
            else -> null
        }
        
        return InventoryDetailEntity(
            itemId = 0, // 将由Repository设置
            quantity = quantity,
            unit = quantityUnit,
            isQuantityUserInput = isQuantityUserInput,
            locationId = locationId,
            productionDate = productionDate,
            expirationDate = expirationDate,
            openStatus = openStatus,
            openDate = null,
            status = parseStatusFieldValue(fieldValues["状态"] as? String),
            stockWarningThreshold = stockWarningThreshold,
            price = price,
            priceUnit = priceUnit,
            purchaseChannel = purchaseChannel,
            storeName = storeName,
            totalPrice = totalPrice,
            totalPriceUnit = totalPriceUnit,
            purchaseDate = purchaseDate,
            shelfLife = shelfLife,
            // 保修信息已移至 WarrantyEntity，不再存储在 InventoryDetailEntity
            // warrantyPeriod = warrantyPeriod,
            // warrantyEndDate = warrantyEndDate,
            isHighTurnover = false,
            wasteDate = null
            // 注意：capacity, rating, season, serialNumber 已移至 UnifiedItemEntity
        )
    }

    /**
     * 提取位置ID - 实现位置查询和创建逻辑
     */
    private suspend fun extractLocationId(): Long? {
        val area = fieldValues["位置_area"] as? String
        val container = fieldValues["位置_container"] as? String  
        val sublocation = fieldValues["位置_sublocation"] as? String
        
        android.util.Log.d("AddItemViewModel", "📍 位置字段值: area='$area', container='$container', sublocation='$sublocation'")
        
        // 如果没有区域信息，返回null
        if (area.isNullOrBlank()) {
            android.util.Log.d("AddItemViewModel", "📍 没有位置区域信息，跳过位置保存")
            return null
        }
        
        try {
            // 查找或创建位置实体
            val locationEntity = LocationEntity(
                id = 0, // 新位置，ID为0
                area = area,
                container = container,
                sublocation = sublocation
            )
            
            // 使用Repository保存位置并返回ID
            android.util.Log.d("AddItemViewModel", "📍 准备保存位置实体: $locationEntity")
            val locationId = repository.findOrCreateLocation(locationEntity)
            android.util.Log.d("AddItemViewModel", "📍 位置保存成功，ID: $locationId")
            
            return locationId
            
        } catch (e: Exception) {
            android.util.Log.e("AddItemViewModel", "📍 位置处理失败", e)
            return null
        }
    }

    /**
     * 从URI列表构建照片实体列表
     */
    private fun buildPhotosFromUris(): List<PhotoEntity> {
        val photoUris = _photoUris.value ?: emptyList()
        android.util.Log.d("AddItemViewModel", "📸 开始构建照片实体: ${photoUris.size}个URI")
        
        return photoUris.mapIndexed { index, uri ->
            android.util.Log.d("AddItemViewModel", "📸 构建照片[$index]: uri='$uri'")
            PhotoEntity(
                id = 0, // 新照片，ID为0
                itemId = 0, // 将在repository中设置
                uri = uri.toString(),
                displayOrder = index,
                isMain = index == 0 // 第一张照片设为主照片
            )
        }
    }

    /**
     * 从字段值构建标签列表
     */
    private fun buildTagsFromFields(): List<TagEntity> {
        val tagsSet = when (val tagsValue = fieldValues["标签"]) {
            is Set<*> -> tagsValue.filterIsInstance<String>()
            is List<*> -> tagsValue.filterIsInstance<String>()
            is String -> listOf(tagsValue)
            else -> emptyList()
        }
        
        android.util.Log.d("AddItemViewModel", "🏷️ 解析标签字段: 原始值=${fieldValues["标签"]}, 解析结果=$tagsSet")
        
        return tagsSet.map { tagName ->
            TagEntity(
                id = 0, // 新标签，ID为0
                name = tagName.trim(),
                color = "#6200EE" // 默认颜色
            )
        }
    }

    /**
     * 从字段值构建Item对象（保留兼容性）
     */
    private fun buildItemFromFields(): Item {
        // 基础字段
        val name = (fieldValues["名称"] as? String)?.trim() ?: ""
        val quantityStr = (fieldValues["数量"] as? String)?.trim()
        val quantity = quantityStr?.toDoubleOrNull() ?: 0.0  // 如果没有输入数量，默认0
        
        // 位置信息构建
        val location = buildLocationFromFields()
        
        // 日期信息解析
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val productionDate = parseDate(fieldValues["生产日期"] as? String)
        val expirationDate = parseDate(fieldValues["保质过期时间"] as? String)
        val purchaseDate = parseDate(fieldValues["购买日期"] as? String)
        val addDate = parseDate(fieldValues["添加日期"] as? String) ?: Date()
        // 保修信息已移至 WarrantyEntity
        // val warrantyEndDate = parseDate(fieldValues["保修到期时间"] as? String)
        
        // 开封状态
        val openStatus = when (fieldValues["开封状态"] as? String) {
            "已开封" -> com.example.itemmanagement.data.model.OpenStatus.OPENED
            "未开封" -> com.example.itemmanagement.data.model.OpenStatus.UNOPENED
            else -> null
        }
        
        // 标签信息
        val tags = buildTagsFromSelectedTags()
        
        // 获取数量单位 - 只有当用户输入了数量时才使用单位，否则为null
        val quantityUnit = if (quantityStr != null && quantity > 0) {
            fieldValues["数量_unit"] as? String ?: "个"
        } else {
            null
        }
        
        // 获取布尔字段
        
        val isHighTurnover = when (fieldValues["高周转"]) {
            is Boolean -> fieldValues["高周转"] as Boolean
            is String -> (fieldValues["高周转"] as String).toBoolean()
            else -> false
        }
        
        // 获取评分 - 支持多种类型
        val rating = when (val ratingValue = fieldValues["评分"]) {
            is Float -> ratingValue.toDouble()
            is Double -> ratingValue
            is String -> ratingValue.toDoubleOrNull()
            else -> null
        }
        
        // 获取季节 - 期望是Set<String>
        val seasonSet = when (val seasonValue = fieldValues["季节"]) {
            is Set<*> -> seasonValue.mapNotNull { it as? String }.toSet()
            is Collection<*> -> seasonValue.mapNotNull { it as? String }.toSet()
            is String -> if (seasonValue.isNotEmpty()) setOf(seasonValue) else emptySet()
            else -> emptySet()
        }
        val seasonString = if (seasonSet.isNotEmpty()) seasonSet.joinToString(",") else null
        
        // 获取期限字段 - 处理Pair类型
        val shelfLife = when (val shelfLifeValue = fieldValues["保质期"]) {
            is Pair<*, *> -> (shelfLifeValue.first as? String)?.toIntOrNull()
            is String -> shelfLifeValue.toIntOrNull()
            else -> null
        }
        
        val warrantyPeriod = when (val warrantyValue = fieldValues["保修期"]) {
            is Pair<*, *> -> (warrantyValue.first as? String)?.toIntOrNull()
            is String -> warrantyValue.toIntOrNull()
            else -> null
        }
        
        // 获取单位字段
        val priceUnit = fieldValues["单价_unit"] as? String
        val totalPriceUnit = fieldValues["总价_unit"] as? String
        val capacityUnit = fieldValues["容量_unit"] as? String ?: fieldValues["容量单位"] as? String
        val shelfLifeUnit = when (val shelfLifeValue = fieldValues["保质期"]) {
            is Pair<*, *> -> shelfLifeValue.second as? String
            else -> fieldValues["保质期_unit"] as? String
        }
        val warrantyUnit = when (val warrantyValue = fieldValues["保修期"]) {
            is Pair<*, *> -> warrantyValue.second as? String
            else -> fieldValues["保修期_unit"] as? String
        }

        return Item(
            id = 0, // 新物品，ID为0
            name = name,
            quantity = quantity,
            unit = quantityUnit ?: "",  // 如果为null，使用空字符串
            location = location,
            category = normalizeCategoryPath(fieldValues["分类"] as? String),
            productionDate = productionDate,
            expirationDate = expirationDate,
            openStatus = openStatus,
            openDate = null,
            brand = fieldValues["品牌"] as? String,
            specification = fieldValues["规格"] as? String,
            stockWarningThreshold = (fieldValues["库存预警值"] as? String)?.toIntOrNull(),
            price = (fieldValues["单价"] as? String)?.toDoubleOrNull(),
            priceUnit = priceUnit,
            purchaseChannel = fieldValues["购买渠道"] as? String,
            storeName = fieldValues["商家名称"] as? String,
            subCategory = null,
            customNote = fieldValues["备注"] as? String,
            season = seasonString,
            capacity = (fieldValues["容量"] as? String)?.toDoubleOrNull(),
            capacityUnit = capacityUnit,
            rating = rating,
            totalPrice = (fieldValues["总价"] as? String)?.toDoubleOrNull(),
            totalPriceUnit = totalPriceUnit,
            purchaseDate = purchaseDate,
            shelfLife = shelfLife,
            // 保修信息已移至 WarrantyEntity
            warrantyPeriod = null,
            warrantyEndDate = null,
            serialNumber = fieldValues["序列号"] as? String,
            locationAddress = (fieldValues["地点"] as? String).also { 
                android.util.Log.d("AddItemViewModel", "📍 保存地点地址: $it")
            },
            locationLatitude = (fieldValues["地点_纬度"] as? String)?.toDoubleOrNull().also {
                android.util.Log.d("AddItemViewModel", "📍 保存地点纬度: $it")
            },
            locationLongitude = (fieldValues["地点_经度"] as? String)?.toDoubleOrNull().also {
                android.util.Log.d("AddItemViewModel", "📍 保存地点经度: $it")
            },
            addDate = addDate,
            isHighTurnover = false,
            tags = tags
        ).also {
            android.util.Log.d("AddItemViewModel", "📍 构建的UnifiedItemEntity - 地点: ${it.locationAddress}, 纬度: ${it.locationLatitude}, 经度: ${it.locationLongitude}")
        }
    }
    
    /**
     * 从字段构建位置信息
     */
    private fun buildLocationFromFields(): com.example.itemmanagement.data.model.Location? {
        val locationStr = fieldValues["位置"] as? String
        return if (!locationStr.isNullOrBlank() && locationStr != "未指定") {
            val parts = locationStr.split("-")
            when (parts.size) {
                1 -> com.example.itemmanagement.data.model.Location(area = parts[0], container = null, sublocation = null)
                2 -> com.example.itemmanagement.data.model.Location(area = parts[0], container = parts[1], sublocation = null)
                3 -> com.example.itemmanagement.data.model.Location(area = parts[0], container = parts[1], sublocation = parts[2])
                else -> null
            }
        } else {
            null
        }
    }
    
    /**
     * 解析日期字符串
     */
    private fun parseDate(dateStr: String?): Date? {
        if (dateStr.isNullOrBlank()) return null
        
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        )
        
        for (format in formats) {
            try {
                return format.parse(dateStr)
            } catch (e: Exception) {
                // 继续尝试下一个格式
            }
        }
        
        return null
    }
    
    /**
     * 从选中的标签构建标签列表
     */
    private fun buildTagsFromSelectedTags(): List<com.example.itemmanagement.data.model.Tag> {
        val tags = mutableListOf<com.example.itemmanagement.data.model.Tag>()
        
        _selectedTags.value?.forEach { (fieldName, tagNames) ->
            tagNames.forEach { tagName ->
                tags.add(
                    com.example.itemmanagement.data.model.Tag(
                        name = tagName,
                        color = "#6200EE" // 默认颜色
                    )
                )
            }
        }
        
        return tags
    }

    private fun getBooleanFieldValue(fieldName: String): Boolean {
        return when (val value = fieldValues[fieldName]) {
            is Boolean -> value
            is Number -> value.toInt() != 0
            is String -> value.equals("true", ignoreCase = true) || value == "1"
            else -> false
        }
    }

    private fun hasAnyRuleBinding(ruleId: String): Boolean {
        return (_itemRuleBindings.value ?: emptyList()).any { binding ->
            binding.ruleId == ruleId
        }
    }

    private fun hasActiveRuleBinding(ruleId: String): Boolean {
        return (_itemRuleBindings.value ?: emptyList()).any { binding ->
            binding.ruleId == ruleId && binding.isRuntimeActive
        }
    }

    private fun parsePeriodFieldToDays(fieldName: String): Int? {
        return when (val value = fieldValues[fieldName]) {
            is Pair<*, *> -> convertToDays(value.first?.toString()?.toIntOrNull() ?: 0, value.second?.toString().orEmpty())
            is String -> {
                val amount = value.toIntOrNull() ?: return null
                val unit = (fieldValues["${fieldName}_unit"] as? String).orEmpty().ifBlank { "天" }
                convertToDays(amount, unit)
            }
            else -> null
        }
    }

    private fun convertToDays(value: Int, unit: String): Int {
        return when (unit) {
            "天" -> value
            "周" -> value * 7
            "月" -> value * 30
            "年" -> value * 365
            else -> value
        }
    }

    private fun parseStatusFieldValue(value: String?): ItemStatus {
        return when (value) {
            "已过期" -> ItemStatus.EXPIRED
            "已退役" -> ItemStatus.DISCARDED
            else -> ItemStatus.IN_STOCK
        }
    }

    private fun buildCustomAttributeEntities(itemId: Long): List<ItemCustomAttributeEntity> {
        return itemBuildCustomAttributeEntities(
            itemId = itemId,
            fieldValues = fieldValues,
            definitionsById = customAttributeDefinitionsById,
            fallbackCurrencyCode = (fieldValues["币种"] as? String)?.takeIf { it.isNotBlank() }
                ?: (fieldValues["单价_unit"] as? String)?.takeIf { it.isNotBlank() }
                ?: "CNY",
        )
    }

    /**
     * 验证Item对象
     */
    private fun validateItem(item: Item): Pair<Boolean, String?> {
        return when {
            item.name.isBlank() -> Pair(false, "物品名称不能为空")
            item.quantity < 0.0 -> Pair(false, "数量不能为负数")
            else -> Pair(true, null)
        }
    }

    /**
     * 初始化默认日期值
     */
    private fun initializeDefaultDateValues() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        
        // 设置添加日期的默认值为当前日期
        saveFieldValue("添加日期", currentDate)
    }

    /**
     * 初始化所有字段属性
     * 从原始AddItemViewModel复制完整的字段属性定义
     */
    private fun initializeAllFieldProperties() {
        // 调用父类的基础属性初始化
        super.initializeDefaultFieldProperties()
        
        // 添加更多字段属性定义...
        setFieldProperties("备注", FieldProperties(
            validationType = ValidationType.TEXT,
            hint = "请输入备注",
            isMultiline = true,
            maxLines = 5
        ))

        setFieldProperties("子分类", FieldProperties(
            options = emptyList(),
            isCustomizable = true
        ))

        setFieldProperties("标签", FieldProperties(
            displayStyle = DisplayStyle.TAG,
            isMultiSelect = true,
            isCustomizable = true,
            options = listOf("重要", "易碎", "易腐", "贵重", "常用")
        ))
        
        // 继续添加其他字段...
    }

    /**
     * 保存保修信息到独立的保修管理系统
     * ✅ 新架构：直接从fieldValues读取保修信息
     */
    private suspend fun saveWarrantyInfoIfNeeded(itemId: Long) {
        android.util.Log.d("AddItemViewModel", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        android.util.Log.d("AddItemViewModel", "🔧 [WARRANTY_SAVE] 开始检查保修信息...")
        android.util.Log.d("AddItemViewModel", "🔧 [WARRANTY_SAVE] itemId: $itemId")
        
        // 提取保修期
        val warrantyValue = fieldValues["保修期"]
        android.util.Log.d("AddItemViewModel", "🔧 [WARRANTY_SAVE] 保修期原始值: $warrantyValue (类型: ${warrantyValue?.javaClass?.simpleName})")
        
        val warrantyPeriod = when (warrantyValue) {
            is Pair<*, *> -> {
                android.util.Log.d("AddItemViewModel", "🔧 [WARRANTY_SAVE] 保修期是Pair类型: first=${warrantyValue.first}, second=${warrantyValue.second}")
                (warrantyValue.first as? String)?.toIntOrNull()
            }
            is String -> {
                android.util.Log.d("AddItemViewModel", "🔧 [WARRANTY_SAVE] 保修期是String类型: $warrantyValue")
                warrantyValue.toIntOrNull()
            }
            else -> {
                android.util.Log.d("AddItemViewModel", "🔧 [WARRANTY_SAVE] 保修期是其他类型或null")
                null
            }
        }
        android.util.Log.d("AddItemViewModel", "🔧 [WARRANTY_SAVE] 解析后的保修期: $warrantyPeriod")
        
        // 提取保修期单位
        val warrantyUnit = when (val warrantyValue = fieldValues["保修期"]) {
            is Pair<*, *> -> warrantyValue.second as? String
            else -> fieldValues["保修期_unit"] as? String
        } ?: "月"
        android.util.Log.d("AddItemViewModel", "🔧 [WARRANTY_SAVE] 保修期单位: $warrantyUnit")
        
        // 提取保修到期日期
        val warrantyEndDateStr = fieldValues["保修到期时间"] as? String
        android.util.Log.d("AddItemViewModel", "🔧 [WARRANTY_SAVE] 保修到期时间字符串: $warrantyEndDateStr")
        val warrantyEndDate = parseDate(warrantyEndDateStr)
        android.util.Log.d("AddItemViewModel", "🔧 [WARRANTY_SAVE] 解析后的保修到期日期: $warrantyEndDate")
        
        // 提取购买日期
        val purchaseDateStr = fieldValues["购买日期"] as? String
        android.util.Log.d("AddItemViewModel", "🔧 [WARRANTY_SAVE] 购买日期字符串: $purchaseDateStr")
        val purchaseDate = parseDate(purchaseDateStr) ?: Date()
        android.util.Log.d("AddItemViewModel", "🔧 [WARRANTY_SAVE] 解析后的购买日期: $purchaseDate")
        
        // 检查是否有保修信息
        if (warrantyPeriod == null || warrantyPeriod <= 0) {
            android.util.Log.w("AddItemViewModel", "⚠️ [WARRANTY_SAVE] 无有效保修期信息，跳过保修信息保存")
            android.util.Log.d("AddItemViewModel", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            return
        }
        
        // 检查是否有WarrantyRepository依赖
        if (warrantyRepository == null) {
            android.util.Log.e("AddItemViewModel", "❌ [WARRANTY_SAVE] 未提供WarrantyRepository，无法保存保修信息")
            android.util.Log.d("AddItemViewModel", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            return
        }
        
        android.util.Log.d("AddItemViewModel", "✅ [WARRANTY_SAVE] WarrantyRepository已注入，准备保存保修信息")
        
        try {
            // 转换保修期为月数
            val warrantyMonths = when (warrantyUnit) {
                "年" -> {
                    android.util.Log.d("AddItemViewModel", "🔧 [WARRANTY_SAVE] 保修期单位是年，转换为月: ${warrantyPeriod * 12}")
                    warrantyPeriod * 12
                }
                "月" -> {
                    android.util.Log.d("AddItemViewModel", "🔧 [WARRANTY_SAVE] 保修期单位是月，保持不变: $warrantyPeriod")
                    warrantyPeriod
                }
                "日" -> {
                    val months = maxOf(1, warrantyPeriod / 30)
                    android.util.Log.d("AddItemViewModel", "🔧 [WARRANTY_SAVE] 保修期单位是日，转换为月: $months")
                    months
                }
                else -> {
                    android.util.Log.d("AddItemViewModel", "🔧 [WARRANTY_SAVE] 保修期单位未知($warrantyUnit)，按月处理: $warrantyPeriod")
                    warrantyPeriod
                }
            }
            
            // 计算保修到期日期（如果没有手动设置）
            val calculatedEndDate = warrantyEndDate ?: run {
                android.util.Log.d("AddItemViewModel", "🔧 [WARRANTY_SAVE] 未手动设置到期日期，开始计算...")
                val calendar = Calendar.getInstance().apply {
                    time = purchaseDate
                    add(Calendar.MONTH, warrantyMonths)
                }
                android.util.Log.d("AddItemViewModel", "🔧 [WARRANTY_SAVE] 计算的到期日期: ${calendar.time}")
                calendar.time
            }
            
            android.util.Log.d("AddItemViewModel", "🔧 [WARRANTY_SAVE] 准备构建WarrantyEntity:")
            android.util.Log.d("AddItemViewModel", "  - itemId: $itemId")
            android.util.Log.d("AddItemViewModel", "  - purchaseDate: $purchaseDate")
            android.util.Log.d("AddItemViewModel", "  - warrantyPeriodMonths: $warrantyMonths")
            android.util.Log.d("AddItemViewModel", "  - warrantyEndDate: $calculatedEndDate")
            
            // 构建保修实体
            val warrantyEntity = WarrantyEntity(
                itemId = itemId,
                purchaseDate = purchaseDate,
                warrantyPeriodMonths = warrantyMonths,
                warrantyEndDate = calculatedEndDate,
                receiptImageUris = null,
                notes = "从添加物品界面自动创建",
                status = if (calculatedEndDate.before(Date())) WarrantyStatus.EXPIRED else WarrantyStatus.ACTIVE,
                warrantyProvider = null,
                contactInfo = null,
                createdDate = Date(),
                updatedDate = Date()
            )
            
            android.util.Log.d("AddItemViewModel", "🔧 [WARRANTY_SAVE] 开始调用warrantyRepository.insertWarranty()...")
            
            // 保存到保修系统
            val warrantyId = warrantyRepository.insertWarranty(warrantyEntity)
            
            android.util.Log.d("AddItemViewModel", "✅ [WARRANTY_SAVE] 保修信息保存成功!")
            android.util.Log.d("AddItemViewModel", "  - warrantyId: $warrantyId")
            android.util.Log.d("AddItemViewModel", "  - period: ${warrantyMonths}月")
            android.util.Log.d("AddItemViewModel", "  - endDate: $calculatedEndDate")
            android.util.Log.d("AddItemViewModel", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            
        } catch (e: Exception) {
            android.util.Log.e("AddItemViewModel", "❌ [WARRANTY_SAVE] 保存失败: ${e.message}", e)
            android.util.Log.e("AddItemViewModel", "❌ [WARRANTY_SAVE] 异常堆栈:", e)
            android.util.Log.d("AddItemViewModel", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            // 不影响主流程，仅记录错误
        }
    }
    
    /**
     * 同步保修信息到独立的保修管理系统
     * @Deprecated 使用saveWarrantyInfoIfNeeded(itemId)替代
     */
    @Deprecated("使用saveWarrantyInfoIfNeeded(itemId)替代")
    private fun syncWarrantyInfoIfNeeded(itemId: Long, item: Item) {
        // 检查是否有WarrantyRepository依赖
        if (warrantyRepository == null) {
            Log.d("AddItemViewModel", "未提供WarrantyRepository，跳过保修信息同步")
            return
        }
        
        // 检查是否有保修相关信息需要同步
        val hasWarrantyPeriod = item.warrantyPeriod != null && item.warrantyPeriod > 0
        val hasWarrantyEndDate = item.warrantyEndDate != null
        val hasPurchaseDate = item.purchaseDate != null
        
        if (!hasWarrantyPeriod && !hasWarrantyEndDate) {
            Log.d("AddItemViewModel", "没有保修信息，跳过同步")
            return
        }
        
        viewModelScope.launch {
            try {
                // 构建保修实体
                val warrantyEntity = WarrantyEntity(
                    itemId = itemId,
                    purchaseDate = item.purchaseDate ?: item.addDate, // 如果没有购买日期，使用添加日期
                    warrantyPeriodMonths = convertWarrantyPeriodToMonths(item.warrantyPeriod),
                    warrantyEndDate = item.warrantyEndDate ?: calculateWarrantyEndDate(
                        item.purchaseDate ?: item.addDate,
                        convertWarrantyPeriodToMonths(item.warrantyPeriod)
                    ),
                    receiptImageUris = null, // 简单保修信息不包含图片
                    notes = "从添加物品界面自动同步",
                    status = if (item.warrantyEndDate?.before(Date()) == true) WarrantyStatus.EXPIRED else WarrantyStatus.ACTIVE,
                    warrantyProvider = null, // 简单保修信息不包含服务商
                    contactInfo = null,
                    createdDate = Date(),
                    updatedDate = Date()
                )
                
                // 保存到保修系统
                val warrantyId = warrantyRepository.insertWarranty(warrantyEntity)
                Log.d("AddItemViewModel", "保修信息同步成功，WarrantyId: $warrantyId")
                
            } catch (e: Exception) {
                Log.w("AddItemViewModel", "保修信息同步失败，但不影响物品添加", e)
                // 不影响主流程，仅记录警告
            }
        }
    }
    
    /**
     * 将保修期转换为月数
     * 支持从原有的保修期字段转换
     */
    private fun convertWarrantyPeriodToMonths(warrantyPeriod: Int?): Int {
        if (warrantyPeriod == null || warrantyPeriod <= 0) return 12 // 默认12个月
        
        // 获取保修期单位
        val warrantyUnit = when (val warrantyValue = fieldValues["保修期"]) {
            is Pair<*, *> -> warrantyValue.second as? String
            else -> fieldValues["保修期_unit"] as? String
        } ?: "月"
        
        return when (warrantyUnit) {
            "年" -> warrantyPeriod * 12
            "月" -> warrantyPeriod
            "日" -> maxOf(1, warrantyPeriod / 30) // 至少1个月
            else -> warrantyPeriod // 默认当作月处理
        }
    }
    
    /**
     * 根据购买日期和保修期计算保修到期日期
     */
    private fun calculateWarrantyEndDate(purchaseDate: Date, warrantyMonths: Int): Date {
        val calendar = Calendar.getInstance().apply {
            time = purchaseDate
            add(Calendar.MONTH, warrantyMonths)
        }
        return calendar.time
    }

    /**
     * 从购物清单项目预填充表单数据
     */
    @Deprecated("使用统一架构，此方法已废弃")
    fun prepareFormFromShoppingItem(shoppingItemEntity: Any?) {
        // TODO: 使用统一架构重新实现此功能
        return
    }
    
    /**
     * 📦 添加日历事件：记录添加物品操作
     */
    private suspend fun addCalendarEventForItemAdded(itemId: Long, itemName: String, category: String) {
        try {
            val event = com.example.itemmanagement.data.entity.CalendarEventEntity(
                itemId = itemId,
                eventType = com.example.itemmanagement.data.model.EventType.ITEM_ADDED,
                title = "添加物品：$itemName",
                description = "分类：$category",
                eventDate = java.util.Date(),
                reminderDays = emptyList(), // 操作记录不需要提醒
                priority = com.example.itemmanagement.data.model.Priority.LOW,
                isCompleted = true, // 操作记录默认为已完成
                recurrenceType = null
            )
            repository.addCalendarEvent(event)
            android.util.Log.d("AddItemViewModel", "📅 已添加日历事件：添加物品 - $itemName")
        } catch (e: Exception) {
            android.util.Log.e("AddItemViewModel", "添加日历事件失败", e)
        }
    }
    
    /**
     * 🛒 添加日历事件：记录购物清单转入库存操作
     */
    private suspend fun addCalendarEventForShoppingTransferred(itemId: Long, itemName: String, category: String) {
        try {
            val event = com.example.itemmanagement.data.entity.CalendarEventEntity(
                itemId = itemId,
                eventType = com.example.itemmanagement.data.model.EventType.SHOPPING_TRANSFERRED,
                title = "购物入库：$itemName",
                description = "分类：$category",
                eventDate = java.util.Date(),
                reminderDays = emptyList(), // 操作记录不需要提醒
                priority = com.example.itemmanagement.data.model.Priority.LOW,
                isCompleted = true, // 操作记录默认为已完成
                recurrenceType = null
            )
            repository.addCalendarEvent(event)
            android.util.Log.d("AddItemViewModel", "📅 已添加日历事件：购物入库 - $itemName")
        } catch (e: Exception) {
            android.util.Log.e("AddItemViewModel", "添加购物入库日历事件失败", e)
        }
    }

    private fun ensureQuickAttributeSelected(definition: AttributeDefinitionEntity) {
        val fieldName = itemCustomFieldName(definition)
        setFieldProperties(
            fieldName,
            FieldProperties(
                validationType = quickCreateValidationType(definition.valueType),
                hint = "请输入${definition.name}",
                unit = if (itemIsPriceAttribute(definition)) "CNY" else null,
                unitOptions = if (itemIsPriceAttribute(definition)) itemCurrencyOptions() else null,
                isCustomizable = false,
            )
        )
        saveFieldValue(itemCustomMetaTypeFieldName(definition.id), itemAttributeTypeKey(definition))
        if (getFieldValue(itemCustomIncludeFieldName(definition.id)) == null) {
            saveFieldValue(itemCustomIncludeFieldName(definition.id), true)
        }
        val mergedDefinitions = (availableAttributeDefinitions + transientAttributeDefinitions)
            .distinctBy { it.id }
        val updatedFields = (_selectedFields.value ?: emptySet()).toMutableSet().apply {
            removeAll { it.name == fieldName }
            add(
                itemBuildFormField(
                    group = "补充信息",
                    fieldName = fieldName,
                    isSelected = true,
                    order = 4000 + size,
                    attributeDefinitions = mergedDefinitions,
                )
            )
        }
        setSelectedFields(updatedFields)
    }

    private fun quickCreateInputMode(valueType: AttributeValueType): AttributeInputMode {
        return when (valueType) {
            AttributeValueType.NUMBER -> AttributeInputMode.NUMBER_INPUT
            AttributeValueType.DATE -> AttributeInputMode.DATE_PICKER
            AttributeValueType.BOOLEAN -> AttributeInputMode.BOOLEAN_SWITCH
            AttributeValueType.SELECT -> AttributeInputMode.SINGLE_SELECT
            AttributeValueType.TEXT -> AttributeInputMode.TEXT_INPUT
        }
    }

    private fun quickCreateValidationType(valueType: AttributeValueType): ValidationType {
        return when (valueType) {
            AttributeValueType.NUMBER -> ValidationType.NUMBER
            AttributeValueType.DATE -> ValidationType.DATE
            else -> ValidationType.TEXT
        }
    }

    private fun quickCreateIcon(valueType: AttributeValueType): String {
        return when (valueType) {
            AttributeValueType.TEXT -> "text_fields"
            AttributeValueType.NUMBER -> "straighten"
            AttributeValueType.DATE -> "calendar_month"
            AttributeValueType.BOOLEAN -> "toggle_on"
            AttributeValueType.SELECT -> "checklist"
        }
    }

    private fun AttributeDefinition.toQuickCreatedEntity(): AttributeDefinitionEntity {
        val gson = Gson()
        return AttributeDefinitionEntity(
            id = id,
            key = key,
            name = name,
            ownerType = ownerType,
            valueType = valueType,
            valueSource = valueSource,
            interactionMode = interactionMode,
            valuePropertiesJson = gson.toJson(valueProperties),
            optionSource = optionSource,
            inputMode = inputMode,
            isMultiValue = isMultiValue,
            optionItemsJson = gson.toJson(optionItems),
            icon = icon,
            templateId = templateId,
            ruleBindingsJson = gson.toJson(ruleBindings),
            description = description,
        )
    }
}
