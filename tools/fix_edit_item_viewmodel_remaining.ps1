$path = 'app/src/main/java/com/example/itemmanagement/ui/edit/EditItemViewModel.kt'
$content = Get-Content -Raw -Encoding UTF8 $path

$buildUnified = @'
    private fun buildUnifiedItemEntityFromFields(): com.example.itemmanagement.data.entity.unified.UnifiedItemEntity {
        val original = originalUnifiedItem
        val season = when (val seasonValue = getFieldValue("季节")) {
            is Set<*> -> seasonValue.filterIsInstance<String>().joinToString(",").ifBlank { null }
            is Collection<*> -> seasonValue.filterIsInstance<String>().joinToString(",").ifBlank { null }
            is String -> seasonValue.trim().ifBlank { null }
            else -> null
        }
        val currencyCode = getFieldValue("币种")?.toString()?.takeIf { it.isNotBlank() }
            ?: getFieldValue("单价_unit")?.toString()?.takeIf { it.isNotBlank() }
            ?: original?.currencyCode
            ?: "CNY"

        return com.example.itemmanagement.data.entity.unified.UnifiedItemEntity(
            id = itemId,
            name = getFieldValue("名称")?.toString()?.trim().orEmpty(),
            category = normalizeCategoryPath(getFieldValue("分类")?.toString()).ifBlank { "未指定" },
            subCategory = null,
            brand = getFieldValue("品牌")?.toString()?.takeIf { it.isNotBlank() },
            specification = getFieldValue("规格")?.toString()?.takeIf { it.isNotBlank() },
            customNote = getFieldValue("备注")?.toString()?.takeIf { it.isNotBlank() },
            capacity = getFieldValue("容量")?.toString()?.toDoubleOrNull(),
            capacityUnit = getFieldValue("容量_unit")?.toString()?.takeIf { it.isNotBlank() },
            rating = getFieldValue("评分")?.toString()?.toDoubleOrNull(),
            season = season,
            serialNumber = getFieldValue("序列号")?.toString()?.takeIf { it.isNotBlank() },
            locationAddress = getFieldValue("地点")?.toString()?.takeIf { it.isNotBlank() },
            locationLatitude = getFieldValue("地点_纬度")?.toString()?.toDoubleOrNull(),
            locationLongitude = getFieldValue("地点_经度")?.toString()?.toDoubleOrNull(),
            currencyCode = currencyCode,
            excludeFromTotalValue = getBooleanFieldValue("不计入总价", original?.excludeFromTotalValue ?: false),
            excludeFromTotalCount = getBooleanFieldValue("不计入总数", original?.excludeFromTotalCount ?: false),
            isSubscription = getBooleanFieldValue("订阅", original?.isSubscription ?: false),
            autoRenew = getBooleanFieldValue("自动续费", original?.autoRenew ?: false),
            subscriptionCycle = if (fieldValues.containsKey("扣费周期")) {
                getFieldValue("扣费周期")?.toString()?.takeIf { it.isNotBlank() }
            } else {
                original?.subscriptionCycle
            },
            templateId = original?.templateId,
            createdDate = original?.createdDate ?: originalItem?.addDate ?: Date(),
            updatedDate = Date()
        )
    }
'@

$applyEntityBacked = @'
    private fun applyEntityBackedFields(
        unifiedItem: com.example.itemmanagement.data.entity.unified.UnifiedItemEntity,
        inventoryDetail: com.example.itemmanagement.data.entity.unified.InventoryDetailEntity?
    ) {
        saveFieldValue("状态", displayStatusValue(inventoryDetail?.status ?: com.example.itemmanagement.data.model.ItemStatus.IN_STOCK))
        saveFieldValue("不计入总价", unifiedItem.excludeFromTotalValue)
        saveFieldValue("不计入总数", unifiedItem.excludeFromTotalCount)
        saveFieldValue("订阅", unifiedItem.isSubscription)
        saveFieldValue("自动续费", unifiedItem.autoRenew)
        saveFieldValue("币种", unifiedItem.currencyCode)

        val currentFields = (_selectedFields.value ?: emptySet()).toMutableSet()
        if (unifiedItem.isSubscription) {
            currentFields.add(Field("补充信息", "订阅", true, getEditModeOrder("订阅")))
        }
        if (unifiedItem.autoRenew) {
            currentFields.add(Field("补充信息", "自动续费", true, getEditModeOrder("自动续费")))
        }
        unifiedItem.subscriptionCycle?.takeIf { it.isNotBlank() }?.let { cycle ->
            saveFieldValue("扣费周期", cycle)
            currentFields.add(Field("补充信息", "扣费周期", true, getEditModeOrder("扣费周期")))
        }
        _selectedFields.value = currentFields
    }
'@

$periodBlock = @'
    private fun parsePeriodFieldToDays(fieldName: String): Int? {
        val value = getFieldValue(fieldName)
        return when (value) {
            is Pair<*, *> -> convertTodays(value.first.toString().toIntOrNull() ?: 0, value.second.toString())
            is String -> {
                val amount = value.toIntOrNull() ?: return null
                val unit = getFieldValue("${fieldName}_unit")?.toString().orEmpty().ifBlank { "天" }
                convertTodays(amount, unit)
            }
            else -> null
        }
    }

    private fun parseStatusFieldValue(
        value: String?,
        fallback: com.example.itemmanagement.data.model.ItemStatus
    ): com.example.itemmanagement.data.model.ItemStatus {
        return when (value) {
            "服役中" -> com.example.itemmanagement.data.model.ItemStatus.IN_STOCK
            "已过期" -> com.example.itemmanagement.data.model.ItemStatus.EXPIRED
            "已退役" -> com.example.itemmanagement.data.model.ItemStatus.DISCARDED
            else -> fallback
        }
    }

    private fun displayStatusValue(status: com.example.itemmanagement.data.model.ItemStatus): String {
        return when (status) {
            com.example.itemmanagement.data.model.ItemStatus.EXPIRED -> "已过期"
            com.example.itemmanagement.data.model.ItemStatus.DISCARDED -> "已退役"
            else -> "服役中"
        }
    }
'@

$buildItem = @'
    private fun buildItemFromFields(): Item {
        return Item(
            id = itemId,
            name = getFieldValue("名称")?.toString() ?: "",
            quantity = (getFieldValue("数量")?.toString())?.toDoubleOrNull() ?: 0.0,
            unit = getFieldValue("数量_unit")?.toString() ?: "个",
            category = normalizeCategoryPath(getFieldValue("分类")?.toString()).ifBlank { "未指定" },
            subCategory = null,
            brand = getFieldValue("品牌")?.toString(),
            specification = getFieldValue("规格")?.toString(),
            customNote = getFieldValue("备注")?.toString(),
            price = (getFieldValue("单价")?.toString())?.toDoubleOrNull(),
            priceUnit = getFieldValue("单价_unit")?.toString() ?: "元",
            rating = (getFieldValue("评分")?.toString())?.toDoubleOrNull().also {
                Log.d("EditItemViewModel", "buildItemFromFields - 评分: fieldValue=${getFieldValue("评分")}, parsed=$it")
            },
            productionDate = parseDate(getFieldValue("生产日期")?.toString()),
            expirationDate = parseDate(getFieldValue("保质过期时间")?.toString()),
            purchaseDate = parseDate(getFieldValue("购买日期")?.toString()),
            addDate = originalItem?.addDate ?: Date(),
            location = buildLocationFromFields(),
            photos = convertUrisToPhotos(_photoUris.value ?: emptyList()),
            tags = buildTagsFromSelectedTags(),
            openStatus = getOpenStatusFromField(),
            openDate = null,
            status = com.example.itemmanagement.data.model.ItemStatus.IN_STOCK,
            stockWarningThreshold = (getFieldValue("库存预警")?.toString())?.toIntOrNull(),
            purchaseChannel = getFieldValue("购买渠道")?.toString(),
            storeName = getFieldValue("商家名称")?.toString(),
            season = when (val seasonValue = getFieldValue("季节")) {
                is Set<*> -> seasonValue.filterIsInstance<String>().joinToString(",")
                is Collection<*> -> seasonValue.filterIsInstance<String>().joinToString(",")
                is String -> seasonValue
                else -> null
            }.also {
                Log.d("EditItemViewModel", "buildItemFromFields - 季节: fieldValue=${getFieldValue("季节")}, result=$it")
            },
            capacity = (getFieldValue("容量")?.toString())?.toDoubleOrNull(),
            capacityUnit = getFieldValue("容量_unit")?.toString(),
            totalPrice = (getFieldValue("总价")?.toString())?.toDoubleOrNull(),
            totalPriceUnit = getFieldValue("总价_unit")?.toString() ?: "元",
            shelfLife = getShelfLifeFromField(),
            warrantyPeriod = null,
            warrantyEndDate = null,
            serialNumber = getFieldValue("序列号")?.toString(),
            locationAddress = getFieldValue("地点")?.toString().also {
                android.util.Log.d("EditItemViewModel", "📍 读取地点地址: $it")
            },
            locationLatitude = getFieldValue("地点_纬度")?.toString()?.toDoubleOrNull().also {
                android.util.Log.d("EditItemViewModel", "📍 读取地点纬度: $it")
            },
            locationLongitude = getFieldValue("地点_经度")?.toString()?.toDoubleOrNull().also {
                android.util.Log.d("EditItemViewModel", "📍 读取地点经度: $it")
            },
            isHighTurnover = false
        ).also {
            android.util.Log.d("EditItemViewModel", "📍 构建的Item - 地点: ${it.locationAddress}, 纬度: ${it.locationLatitude}, 经度: ${it.locationLongitude}")
        }
    }

    private fun getOpenStatusFromField(): com.example.itemmanagement.data.model.OpenStatus? {
        val statusStr = getFieldValue("开封状态")?.toString()
        return when (statusStr) {
            "已开封" -> com.example.itemmanagement.data.model.OpenStatus.OPENED
            "未开封" -> com.example.itemmanagement.data.model.OpenStatus.UNOPENED
            else -> null
        }
    }

    private fun parseDate(dateStr: String?): Date? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }
'@

$prepareForm = @'
    @Deprecated("使用统一架构，此方法已废弃")
    fun prepareFormFromShoppingItem(shoppingItemEntity: Any) {
        return
    }
'@

$content = [regex]::Replace($content, '(?s)    private fun buildUnifiedItemEntityFromFields\(\): com\.example\.itemmanagement\.data\.entity\.unified\.UnifiedItemEntity \{.*?    private fun applyEntityBackedFields\(', $buildUnified + "`r`n" + $applyEntityBacked + "`r`n    private fun applyEntityBackedFields(", 1)
$content = [regex]::Replace($content, '(?s)    private fun applyEntityBackedFields\(\r?\n.*?    private fun parsePeriodFieldToDays\(', $applyEntityBacked + "`r`n    private fun parsePeriodFieldToDays(", 1)
$content = [regex]::Replace($content, '(?s)    private fun parsePeriodFieldToDays\(fieldName: String\): Int\? \{.*?    private fun buildItemFromFields\(\): Item \{', $periodBlock + "`r`n    private fun buildItemFromFields(): Item {", 1)
$content = [regex]::Replace($content, '(?s)    private fun buildItemFromFields\(\): Item \{.*?    private fun getOpenStatusFromField\(\): com\.example\.itemmanagement\.data\.model\.OpenStatus\? \{', $buildItem + "`r`n    private fun getOpenStatusFromField(): com.example.itemmanagement.data.model.OpenStatus? {", 1)
$content = [regex]::Replace($content, '(?s)    private fun getOpenStatusFromField\(\): com\.example\.itemmanagement\.data\.model\.OpenStatus\? \{.*?    private fun getShelfLifeFromField\(\): Int\? \{', $buildItem + "`r`n    private fun getShelfLifeFromField(): Int? {", 1)
$content = [regex]::Replace($content, '(?s)    private fun parseDate\(dateStr: String"\): Date\? \{"\r?\n.*?    private fun buildTagsFromSelectedTags\(\): List<com\.example\.itemmanagement\.data\.model\.Tag> \{', $buildItem + "`r`n    private fun buildTagsFromSelectedTags(): List<com.example.itemmanagement.data.model.Tag> {", 1)
$content = [regex]::Replace($content, '(?s)    @Deprecated\("使用统一架构，此方法已废弃"\)\r?\n    fun prepareFormFromShoppingItem\(shoppingItemEntity: Any"\) \{\".*?    /\*\*', $prepareForm + "`r`n    /**", 1)

[System.IO.File]::WriteAllText((Resolve-Path $path), $content, [System.Text.UTF8Encoding]::new($false))
