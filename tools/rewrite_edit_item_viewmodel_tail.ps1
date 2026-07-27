$path = 'app/src/main/java/com/example/itemmanagement/ui/edit/EditItemViewModel.kt'
$lines = Get-Content -Encoding UTF8 $path

$head = $lines[0..1014]
$tail = $lines[1518..($lines.Length - 1)]

$newBlock = @'
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
            },
            capacity = (getFieldValue("容量")?.toString())?.toDoubleOrNull(),
            capacityUnit = getFieldValue("容量_unit")?.toString(),
            totalPrice = (getFieldValue("总价")?.toString())?.toDoubleOrNull(),
            totalPriceUnit = getFieldValue("总价_unit")?.toString() ?: "元",
            shelfLife = getShelfLifeFromField(),
            warrantyPeriod = null,
            warrantyEndDate = null,
            serialNumber = getFieldValue("序列号")?.toString(),
            locationAddress = getFieldValue("地点")?.toString(),
            locationLatitude = getFieldValue("地点_纬度")?.toString()?.toDoubleOrNull(),
            locationLongitude = getFieldValue("地点_经度")?.toString()?.toDoubleOrNull(),
            isHighTurnover = false
        )
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
        } catch (_: Exception) {
            null
        }
    }

    private fun buildTagsFromSelectedTags(): List<com.example.itemmanagement.data.model.Tag> {
        val tags = mutableListOf<com.example.itemmanagement.data.model.Tag>()
        _selectedTags.value?.forEach { (_, tagNames) ->
            tagNames.forEach { tagName ->
                tags.add(com.example.itemmanagement.data.model.Tag(name = tagName))
            }
        }
        return tags
    }

    private fun buildLocationFromFields(): com.example.itemmanagement.data.model.Location? {
        val area = getFieldValue("位置_area")?.toString()
        if (area.isNullOrBlank()) return null
        return com.example.itemmanagement.data.model.Location(
            area = area,
            container = getFieldValue("位置_container")?.toString(),
            sublocation = getFieldValue("位置_sublocation")?.toString()
        )
    }

    private fun convertUrisToPhotos(uris: List<Uri>): List<com.example.itemmanagement.data.model.Photo> {
        return uris.mapIndexed { index, uri ->
            com.example.itemmanagement.data.model.Photo(
                uri = uri.toString(),
                isMain = index == 0,
                displayOrder = index
            )
        }
    }

    private suspend fun saveOrUpdateWarrantyInfo() {
        Log.d("EditItemViewModel", "🔧 开始检查保修信息...")
        val warrantyPeriod = when (val warrantyValue = getFieldValue("保修期")) {
            is Pair<*, *> -> (warrantyValue.first as? String)?.toIntOrNull()
            is String -> warrantyValue.toIntOrNull()
            else -> null
        }
        val warrantyUnit = when (val warrantyValue = getFieldValue("保修期")) {
            is Pair<*, *> -> warrantyValue.second as? String
            else -> getFieldValue("保修期_unit") as? String
        } ?: "月"
        val warrantyEndDate = parseDate(getFieldValue("保修到期时间")?.toString())
        val purchaseDate = parseDate(getFieldValue("购买日期")?.toString()) ?: Date()

        if (warrantyRepository == null) {
            Log.w("EditItemViewModel", "⚠️ 未提供WarrantyRepository，无法保存保修信息")
            return
        }

        try {
            val existingWarranty = warrantyRepository.getWarrantyByItemId(itemId)
            if (warrantyPeriod == null || warrantyPeriod <= 0) {
                if (existingWarranty != null) {
                    warrantyRepository.deleteWarranty(existingWarranty)
                }
                return
            }

            val warrantyMonths = when (warrantyUnit) {
                "年" -> warrantyPeriod * 12
                "月" -> warrantyPeriod
                "日" -> maxOf(1, warrantyPeriod / 30)
                else -> warrantyPeriod
            }
            val calculatedEndDate = warrantyEndDate ?: calculateWarrantyEndDate(purchaseDate, warrantyMonths)

            if (existingWarranty != null) {
                val updatedWarranty = existingWarranty.copy(
                    purchaseDate = purchaseDate,
                    warrantyPeriodMonths = warrantyMonths,
                    warrantyEndDate = calculatedEndDate,
                    updatedDate = Date()
                )
                warrantyRepository.updateWarranty(updatedWarranty)
            } else {
                val warrantyEntity = WarrantyEntity(
                    itemId = itemId,
                    purchaseDate = purchaseDate,
                    warrantyPeriodMonths = warrantyMonths,
                    warrantyEndDate = calculatedEndDate,
                    receiptImageUris = null,
                    notes = "从编辑物品界面创建",
                    status = if (calculatedEndDate.before(Date())) WarrantyStatus.EXPIRED else WarrantyStatus.ACTIVE,
                    warrantyProvider = null,
                    contactInfo = null,
                    createdDate = Date(),
                    updatedDate = Date()
                )
                warrantyRepository.insertWarranty(warrantyEntity)
            }
        } catch (e: Exception) {
            Log.e("EditItemViewModel", "保修信息保存失败，但不影响物品更新", e)
        }
    }
'@

$body = $newBlock -split "`r?`n"
$lines = $head + $body + $tail
Set-Content -Encoding UTF8 $path $lines
