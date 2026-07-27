$path = 'app/src/main/java/com/example/itemmanagement/ui/edit/EditItemViewModel.kt'
$content = Get-Content -Raw -Encoding UTF8 $path

$initBlock = @'
    /**
     * 初始化编辑模式的默认字段
     */
    private fun initializeEditModeFields() {
        _selectedFields.value = setOf(
            Field("基础信息", "名称", true, getEditModeOrder("名称")),
            Field("基础信息", "数量", true, getEditModeOrder("数量")),
            Field("基础信息", "位置", true, getEditModeOrder("位置")),
            Field("基础信息", "备注", true, getEditModeOrder("备注")),
            Field("分类", "分类", true, getEditModeOrder("分类")),
            Field("日期类", "添加日期", true, getEditModeOrder("添加日期"))
        )
    }
'@

$content = [regex]::Replace(
    $content,
    '(?s)    /\*\*\r?\n     \* 初始化编辑模式的默认字段.*?    override fun clearStateAndCache\(\) \{',
    $initBlock + "`r`n    override fun clearStateAndCache() {",
    1
)

$loadBlock = @'
    /**
     * 将物品数据填充到表单字段
     */
    private fun loadItemData(item: Item, warranty: WarrantyEntity? = null) {
        Log.d("EditItemViewModel", "开始加载物品数据: ${item.name}")
        originalItem = item
        fieldValues.clear()

        val fieldsToShow = mutableSetOf<Field>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        saveFieldValue("名称", item.name)
        fieldsToShow.add(Field("基础信息", "名称", true, getEditModeOrder("名称")))

        val quantityStr = if (item.isQuantityUserInput) {
            if (item.quantity == item.quantity.toInt().toDouble()) {
                item.quantity.toInt().toString()
            } else {
                item.quantity.toString()
            }
        } else {
            null
        }
        quantityStr?.let {
            saveFieldValue("数量", it)
            fieldsToShow.add(Field("基础信息", "数量", true, getEditModeOrder("数量")))
            item.unit.takeIf { unit -> unit.isNotBlank() }?.let { unit ->
                saveFieldValue("数量_unit", unit)
            }
        }

        val categoryPath = combineCategoryPath(item.category, item.subCategory)
        if (categoryPath.isNotBlank() && categoryPath != "未指定") {
            saveFieldValue("分类", categoryPath)
            fieldsToShow.add(Field("分类", "分类", true, getEditModeOrder("分类")))
        }

        item.location?.let { location ->
            if (!location.area.isNullOrBlank() && location.area != "未指定") {
                saveFieldValue("位置_area", location.area)
                fieldsToShow.add(Field("基础信息", "位置", true, getEditModeOrder("位置")))
                location.container?.takeIf { it.isNotBlank() }?.let { saveFieldValue("位置_container", it) }
                location.sublocation?.takeIf { it.isNotBlank() }?.let { saveFieldValue("位置_sublocation", it) }
            }
        }

        item.productionDate?.let {
            saveFieldValue("生产日期", dateFormat.format(it))
            fieldsToShow.add(Field("日期类", "生产日期", true, getEditModeOrder("生产日期")))
        }
        item.expirationDate?.let {
            saveFieldValue("保质过期时间", dateFormat.format(it))
            fieldsToShow.add(Field("日期类", "保质过期时间", true, getEditModeOrder("保质过期时间")))
        }
        item.purchaseDate?.let {
            saveFieldValue("购买日期", dateFormat.format(it))
            fieldsToShow.add(Field("日期类", "购买日期", true, getEditModeOrder("购买日期")))
        }
        saveFieldValue("添加日期", dateFormat.format(item.addDate))
        fieldsToShow.add(Field("日期类", "添加日期", true, getEditModeOrder("添加日期")))

        item.price?.takeIf { it > 0 }?.let {
            saveFieldValue("单价", it.toString())
            fieldsToShow.add(Field("数字类", "单价", true, getEditModeOrder("单价")))
            item.priceUnit?.takeIf { unit -> unit.isNotBlank() }?.let { unit ->
                saveFieldValue("单价_unit", unit)
            }
        }

        item.customNote?.takeIf { it.isNotBlank() }?.let {
            saveFieldValue("备注", it)
            fieldsToShow.add(Field("基础信息", "备注", true, getEditModeOrder("备注")))
        }

        item.rating?.let {
            saveFieldValue("评分", it.toString())
            fieldsToShow.add(Field("数字类", "评分", true, getEditModeOrder("评分")))
        }

        item.openStatus?.let { status ->
            saveFieldValue(
                "开封状态",
                when (status) {
                    com.example.itemmanagement.data.model.OpenStatus.OPENED -> "已开封"
                    com.example.itemmanagement.data.model.OpenStatus.UNOPENED -> "未开封"
                }
            )
            fieldsToShow.add(Field("基础信息", "开封状态", true, getEditModeOrder("开封状态")))
        }

        item.purchaseChannel?.takeIf { it.isNotBlank() && it != "未指定" }?.let {
            saveFieldValue("购买渠道", it)
            fieldsToShow.add(Field("商业信息", "购买渠道", true, getEditModeOrder("购买渠道")))
        }
        item.storeName?.takeIf { it.isNotBlank() && it != "未指定" }?.let {
            saveFieldValue("商家名称", it)
            fieldsToShow.add(Field("商业信息", "商家名称", true, getEditModeOrder("商家名称")))
        }
        item.serialNumber?.takeIf { it.isNotBlank() }?.let {
            saveFieldValue("序列号", it)
            fieldsToShow.add(Field("商业信息", "序列号", true, getEditModeOrder("序列号")))
        }

        item.season?.takeIf { it.isNotBlank() && it != "未指定" }?.let { seasonString ->
            val seasonSet = seasonString.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
            saveFieldValue("季节", seasonSet)
            fieldsToShow.add(Field("分类", "季节", true, getEditModeOrder("季节")))
        }

        item.capacity?.takeIf { it > 0 }?.let {
            saveFieldValue("容量", it.toString())
            fieldsToShow.add(Field("数字类", "容量", true, getEditModeOrder("容量")))
            item.capacityUnit?.takeIf { unit -> unit.isNotBlank() }?.let { unit ->
                saveFieldValue("容量_unit", unit)
            }
        }

        item.totalPrice?.takeIf { it > 0 }?.let {
            saveFieldValue("总价", it.toString())
            fieldsToShow.add(Field("数字类", "总价", true, getEditModeOrder("总价")))
            item.totalPriceUnit?.takeIf { unit -> unit.isNotBlank() }?.let { unit ->
                saveFieldValue("总价_unit", unit)
            }
        }

        item.shelfLife?.takeIf { it > 0 }?.let {
            val (value, unit) = convertDaysToAppropriateUnit(it)
            saveFieldValue("保质期", Pair(value, unit))
            saveFieldValue("保质期_unit", unit)
            fieldsToShow.add(Field("日期类", "保质期", true, getEditModeOrder("保质期")))
        }

        warranty?.takeIf { it.warrantyPeriodMonths > 0 }?.let {
            val (value, unit) = if (it.warrantyPeriodMonths % 12 == 0) {
                Pair((it.warrantyPeriodMonths / 12).toString(), "年")
            } else {
                Pair(it.warrantyPeriodMonths.toString(), "月")
            }
            saveFieldValue("保修期", Pair(value, unit))
            saveFieldValue("保修期_unit", unit)
            fieldsToShow.add(Field("日期类", "保修期", true, getEditModeOrder("保修期")))
            saveFieldValue("保修到期时间", dateFormat.format(it.warrantyEndDate))
            fieldsToShow.add(Field("日期类", "保修到期时间", true, getEditModeOrder("保修到期时间")))
        }

        if (item.tags.isNotEmpty()) {
            saveFieldValue("标签", item.tags.map { it.name }.toSet())
            fieldsToShow.add(Field("分类", "标签", true, getEditModeOrder("标签")))
        }

        item.locationAddress?.takeIf { it.isNotBlank() && it != "未指定" }?.let { address ->
            saveFieldValue("地点", address)
            fieldsToShow.add(Field("位置", "地点", true, getEditModeOrder("地点")))
            item.locationLatitude?.let { saveFieldValue("地点_纬度", it.toString()) }
            item.locationLongitude?.let { saveFieldValue("地点_经度", it.toString()) }
        }

        _selectedFields.value = fieldsToShow
        _photoUris.value = if (item.photos.isNotEmpty()) {
            item.photos.map { Uri.parse(it.uri) }
        } else {
            emptyList()
        }
        Log.d("EditItemViewModel", "当前fieldValues内容: $fieldValues")
    }
'@

$content = [regex]::Replace(
    $content,
    '(?s)    /\*\*\r?\n     \* 将物品数据填充到表单字段.*?    private suspend fun buildInventoryDetailFromFields\(itemId: Long\):',
    $loadBlock + "`r`n    private suspend fun buildInventoryDetailFromFields(itemId: Long):",
    1
)

[System.IO.File]::WriteAllText((Resolve-Path $path), $content, [System.Text.UTF8Encoding]::new($false))
