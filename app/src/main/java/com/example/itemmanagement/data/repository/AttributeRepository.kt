package com.example.itemmanagement.data.repository

import com.example.itemmanagement.data.dao.attribute.AttributeDefinitionDao
import com.example.itemmanagement.data.dao.attribute.RuleDefinitionDao
import com.example.itemmanagement.data.entity.attribute.AttributeDefinitionEntity
import com.example.itemmanagement.data.entity.attribute.RuleDefinitionEntity
import com.example.itemmanagement.data.model.attribute.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AttributeRepository(
    private val attributeDefinitionDao: AttributeDefinitionDao,
    private val ruleDefinitionDao: RuleDefinitionDao
) {
    private val gson = Gson()

    private data class LegacyRuleBindingPayload(
        val ruleId: String,
        val inputRole: String,
        val requiredDependencies: List<String> = emptyList(),
        val optionalDependencies: List<String> = emptyList(),
        val outputKeys: List<String> = emptyList(),
    )

    val itemSystemAttributes: List<AttributeDefinition> = listOf(
        AttributeDefinition(
            id = "attr_system_purchase_price",
            key = "purchase_price",
            name = "购入价格",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.NUMBER,
            valueProperties = AttributeValueProperties(
                numberFormat = AttributeNumberFormat.PRICE,
                defaultUnit = "CNY",
                allowedUnits = listOf("CNY", "USD", "EUR", "JPY", "GBP", "HKD", "TWD"),
                allowUnitSwitch = true,
                decimalPlaces = 2,
                allowNegative = false,
            ),
            valueSource = AttributeValueSource.INPUT,
            interactionMode = AttributeInputMode.PRICE_INPUT,
            icon = "payments",
            templateId = "template_attr_purchase_price",
            ruleBindings = listOf(
                RuleBindingInstance(
                    id = "binding_purchase_price_include_total_price",
                    ruleId = "rule_system_include_total_price",
                    entryAttributeId = "attr_system_purchase_price",
                    entrySlotKey = "amount",
                    slotBindings = listOf(
                        RuleSlotBinding.AttributeInput(
                            slotKey = "amount",
                            attributeId = "attr_system_purchase_price",
                            attributeKeySnapshot = "purchase_price",
                            attributeNameSnapshot = "购入价格",
                        ),
                        RuleSlotBinding.SystemOutput(
                            slotKey = "计入总价",
                            systemVariableKey = SystemVariableKey.GLOBAL_TOTAL_VALUE,
                        ),
                    ),
                ),
                RuleBindingInstance(
                    id = "binding_purchase_price_average_value",
                    ruleId = "rule_system_average_value",
                    entryAttributeId = "attr_system_purchase_price",
                    entrySlotKey = "amount",
                    slotBindings = listOf(
                        RuleSlotBinding.AttributeInput(
                            slotKey = "amount",
                            attributeId = "attr_system_purchase_price",
                            attributeKeySnapshot = "purchase_price",
                            attributeNameSnapshot = "购入价格",
                        ),
                        RuleSlotBinding.AttributeInput(
                            slotKey = "startDate",
                            attributeId = "attr_system_purchase_date",
                            attributeKeySnapshot = "purchase_date",
                            attributeNameSnapshot = "购买日期",
                        ),
                        RuleSlotBinding.ReadonlyOutput(
                            slotKey = "平均价值",
                            displayLabel = "平均价值",
                        ),
                    ),
                )
            ),
            description = "物品补充信息中的购入价格系统属性"
        ),
        AttributeDefinition(
            id = "attr_system_purchase_date",
            key = "purchase_date",
            name = "购买日期",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.DATE,
            optionSource = AttributeOptionSource.INPUT,
            inputMode = AttributeInputMode.DATE_PICKER,
            isMultiValue = false,
            icon = "event",
            templateId = "template_attr_date",
            description = "物品补充信息中的购买日期系统属性"
        ),
        AttributeDefinition(
            id = "attr_system_status",
            key = "item_status",
            name = "状态",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.SELECT,
            valueProperties = AttributeValueProperties(
                optionItems = listOf("服役中", "未购买", "待补款", "已退役", "已过期"),
                selectionOptionSource = AttributeSelectionOptionSource.SYSTEM,
                isMultiValue = false,
            ),
            valueSource = AttributeValueSource.FIXED,
            interactionMode = AttributeInputMode.SINGLE_SELECT,
            icon = "flag",
            templateId = "template_attr_fixed_option",
            description = "物品当前状态"
        ),
        AttributeDefinition(
            id = "attr_system_tags",
            key = "item_tags",
            name = "标签",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.SELECT,
            valueProperties = AttributeValueProperties(
                selectionOptionSource = AttributeSelectionOptionSource.USER,
                allowCustomOptions = true,
                isMultiValue = true,
            ),
            valueSource = AttributeValueSource.INPUT,
            interactionMode = AttributeInputMode.TAG_INPUT,
            icon = "sell",
            templateId = "template_attr_text",
            description = "物品标签"
        ),
        AttributeDefinition(
            id = "attr_system_total_price",
            key = "item_total_price",
            name = "总价",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.NUMBER,
            optionSource = AttributeOptionSource.INPUT,
            inputMode = AttributeInputMode.PRICE_INPUT,
            isMultiValue = false,
            icon = "payments",
            templateId = "template_attr_purchase_price",
            description = "物品总价"
        ),
        AttributeDefinition(
            id = "attr_system_currency",
            key = "item_currency",
            name = "币种",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.SELECT,
            valueProperties = AttributeValueProperties(
                optionItems = listOf("CNY", "USD", "EUR", "JPY", "GBP", "HKD", "TWD"),
                selectionOptionSource = AttributeSelectionOptionSource.SYSTEM,
                isMultiValue = false,
                defaultValue = "CNY",
            ),
            valueSource = AttributeValueSource.FIXED,
            interactionMode = AttributeInputMode.SINGLE_SELECT,
            icon = "currency_exchange",
            templateId = "template_attr_fixed_option",
            description = "价格相关币种"
        ),
        AttributeDefinition(
            id = "attr_system_purchase_channel",
            key = "item_purchase_channel",
            name = "购买渠道",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.TEXT,
            optionSource = AttributeOptionSource.INPUT,
            inputMode = AttributeInputMode.TEXT_INPUT,
            isMultiValue = false,
            icon = "storefront",
            templateId = "template_attr_text",
            description = "购买渠道"
        ),
        AttributeDefinition(
            id = "attr_system_store_name",
            key = "item_store_name",
            name = "商家名称",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.TEXT,
            optionSource = AttributeOptionSource.INPUT,
            inputMode = AttributeInputMode.TEXT_INPUT,
            isMultiValue = false,
            icon = "store",
            templateId = "template_attr_text",
            description = "购买商家名称"
        ),
        AttributeDefinition(
            id = "attr_system_note",
            key = "item_note",
            name = "备注",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.TEXT,
            optionSource = AttributeOptionSource.INPUT,
            inputMode = AttributeInputMode.TEXT_INPUT,
            isMultiValue = false,
            icon = "notes",
            templateId = "template_attr_text",
            description = "补充备注"
        ),
        AttributeDefinition(
            id = "attr_system_location",
            key = "item_location",
            name = "位置",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.TEXT,
            optionSource = AttributeOptionSource.INPUT,
            inputMode = AttributeInputMode.TEXT_INPUT,
            isMultiValue = false,
            icon = "inventory_2",
            templateId = "template_attr_text",
            description = "存放位置"
        ),
        AttributeDefinition(
            id = "attr_system_place",
            key = "item_place",
            name = "地点",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.TEXT,
            optionSource = AttributeOptionSource.INPUT,
            inputMode = AttributeInputMode.TEXT_INPUT,
            isMultiValue = false,
            icon = "place",
            templateId = "template_attr_text",
            description = "地点信息"
        ),
        AttributeDefinition(
            id = "attr_system_serial_number",
            key = "item_serial_number",
            name = "序列号",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.TEXT,
            optionSource = AttributeOptionSource.INPUT,
            inputMode = AttributeInputMode.TEXT_INPUT,
            isMultiValue = false,
            icon = "pin",
            templateId = "template_attr_text",
            description = "序列号"
        ),
        AttributeDefinition(
            id = "attr_system_capacity",
            key = "item_capacity",
            name = "容量",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.NUMBER,
            optionSource = AttributeOptionSource.INPUT,
            inputMode = AttributeInputMode.NUMBER_INPUT,
            isMultiValue = false,
            icon = "storage",
            templateId = "template_attr_text",
            description = "容量"
        ),
        AttributeDefinition(
            id = "attr_system_rating",
            key = "item_rating",
            name = "评分",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.NUMBER,
            optionSource = AttributeOptionSource.INPUT,
            inputMode = AttributeInputMode.NUMBER_INPUT,
            isMultiValue = false,
            icon = "star",
            templateId = "template_attr_text",
            description = "评分"
        ),
        AttributeDefinition(
            id = "attr_system_production_date",
            key = "item_production_date",
            name = "生产日期",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.DATE,
            optionSource = AttributeOptionSource.INPUT,
            inputMode = AttributeInputMode.DATE_PICKER,
            isMultiValue = false,
            icon = "event",
            templateId = "template_attr_date",
            description = "生产日期"
        ),
        AttributeDefinition(
            id = "attr_system_shelf_life",
            key = "item_shelf_life",
            name = "保质期",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.NUMBER,
            optionSource = AttributeOptionSource.INPUT,
            inputMode = AttributeInputMode.NUMBER_INPUT,
            isMultiValue = false,
            icon = "schedule",
            templateId = "template_attr_text",
            description = "保质期"
        ),
        AttributeDefinition(
            id = "attr_system_expiration_date",
            key = "item_expiration_date",
            name = "保质过期时间",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.DATE,
            optionSource = AttributeOptionSource.INPUT,
            inputMode = AttributeInputMode.DATE_PICKER,
            isMultiValue = false,
            icon = "event_busy",
            templateId = "template_attr_date",
            description = "保质过期时间"
        ),
        AttributeDefinition(
            id = "attr_system_warranty_period",
            key = "item_warranty_period",
            name = "保修期",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.NUMBER,
            optionSource = AttributeOptionSource.INPUT,
            inputMode = AttributeInputMode.NUMBER_INPUT,
            isMultiValue = false,
            icon = "build",
            templateId = "template_attr_text",
            description = "保修期"
        ),
        AttributeDefinition(
            id = "attr_system_warranty_expiry",
            key = "item_warranty_expiry",
            name = "保修到期时间",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.DATE,
            optionSource = AttributeOptionSource.INPUT,
            inputMode = AttributeInputMode.DATE_PICKER,
            isMultiValue = false,
            icon = "event_available",
            templateId = "template_attr_date",
            description = "保修到期时间"
        ),
        AttributeDefinition(
            id = "attr_system_subscription_flag",
            key = "item_subscription_flag",
            name = "订阅制",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.BOOLEAN,
            optionSource = AttributeOptionSource.INPUT,
            inputMode = AttributeInputMode.BOOLEAN_SWITCH,
            isMultiValue = false,
            icon = "autorenew",
            templateId = "template_attr_text",
            description = "是否订阅制"
        ),
        AttributeDefinition(
            id = "attr_system_auto_renew",
            key = "item_auto_renew",
            name = "自动续费",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.BOOLEAN,
            optionSource = AttributeOptionSource.INPUT,
            inputMode = AttributeInputMode.BOOLEAN_SWITCH,
            isMultiValue = false,
            icon = "sync",
            templateId = "template_attr_text",
            description = "是否自动续费"
        ),
        AttributeDefinition(
            id = "attr_system_billing_cycle",
            key = "item_billing_cycle",
            name = "扣费周期",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.SELECT,
            valueProperties = AttributeValueProperties(
                optionItems = listOf("DAY", "MONTH", "QUARTER", "YEAR"),
                selectionOptionSource = AttributeSelectionOptionSource.SYSTEM,
                isMultiValue = false,
                defaultValue = "MONTH",
            ),
            valueSource = AttributeValueSource.FIXED,
            interactionMode = AttributeInputMode.SINGLE_SELECT,
            icon = "repeat",
            templateId = "template_attr_fixed_option",
            description = "扣费周期"
        ),
        AttributeDefinition(
            id = "attr_system_open_status",
            key = "item_open_status",
            name = "开封状态",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.SELECT,
            valueProperties = AttributeValueProperties(
                optionItems = listOf("未开封", "已开封"),
                selectionOptionSource = AttributeSelectionOptionSource.SYSTEM,
                isMultiValue = false,
            ),
            valueSource = AttributeValueSource.FIXED,
            interactionMode = AttributeInputMode.SINGLE_SELECT,
            icon = "inventory",
            templateId = "template_attr_fixed_option",
            description = "开封状态"
        ),
        AttributeDefinition(
            id = "attr_system_season",
            key = "item_season",
            name = "季节",
            ownerType = AttributeOwnerType.SYSTEM,
            valueType = AttributeValueType.SELECT,
            valueProperties = AttributeValueProperties(
                optionItems = listOf("春", "夏", "秋", "冬"),
                selectionOptionSource = AttributeSelectionOptionSource.SYSTEM,
                isMultiValue = true,
            ),
            valueSource = AttributeValueSource.FIXED,
            interactionMode = AttributeInputMode.MULTI_SELECT,
            icon = "wb_sunny",
            templateId = "template_attr_fixed_option",
            description = "季节适用性"
        )
    )

    // 预置系统模板
    val systemAttributeTemplates: List<AttributeTemplate> = listOf(
        AttributeTemplate(
            id = "template_attr_text",
            key = "template_text",
            name = "文本模板",
            category = "FOUNDATION",
            defaultName = "新建文本属性",
            defaultIcon = "notes",
            defaultValueType = AttributeValueType.TEXT,
            defaultValueProperties = AttributeValueProperties(textMultiline = false),
            defaultValueSource = AttributeValueSource.INPUT,
            defaultInteractionMode = AttributeInputMode.TEXT_INPUT,
            description = "适合品牌、备注、编号等普通字段"
        ),
        AttributeTemplate(
            id = "template_attr_date",
            key = "template_date",
            name = "日期模板",
            category = "FOUNDATION",
            defaultName = "新建日期属性",
            defaultIcon = "event",
            defaultValueType = AttributeValueType.DATE,
            defaultValueProperties = AttributeValueProperties(includeTime = false),
            defaultValueSource = AttributeValueSource.INPUT,
            defaultInteractionMode = AttributeInputMode.DATE_PICKER,
            description = "适合购买日期、到期时间、纪念日等日期字段"
        ),
        AttributeTemplate(
            id = "template_attr_fixed_option",
            key = "template_fixed_option",
            name = "固定选项模板",
            category = "FOUNDATION",
            defaultName = "新建单选属性",
            defaultIcon = "checklist",
            defaultValueType = AttributeValueType.SELECT,
            defaultValueProperties = AttributeValueProperties(
                selectionOptionSource = AttributeSelectionOptionSource.USER,
                isMultiValue = false,
            ),
            defaultValueSource = AttributeValueSource.FIXED,
            defaultInteractionMode = AttributeInputMode.SINGLE_SELECT,
            description = "适合品牌、状态、等级等选择字段"
        ),
        AttributeTemplate(
            id = "template_attr_purchase_price",
            key = "template_purchase_price",
            name = "购入价格属性模板",
            category = "PRICE_FAMILY",
            defaultName = "购入价格",
            defaultIcon = "payments",
            defaultValueType = AttributeValueType.NUMBER,
            defaultValueProperties = AttributeValueProperties(
                numberFormat = AttributeNumberFormat.PRICE,
                defaultUnit = "CNY",
                allowedUnits = listOf("CNY", "USD", "EUR", "JPY", "GBP", "HKD", "TWD"),
                allowUnitSwitch = true,
                decimalPlaces = 2,
                allowNegative = false,
            ),
            defaultValueSource = AttributeValueSource.INPUT,
            defaultInteractionMode = AttributeInputMode.PRICE_INPUT,
            description = "适合带统计规则的价格字段",
            defaultRuleBindings = listOf(
                RuleBindingInstance(
                    id = "template_binding_purchase_price_include_total_price",
                    ruleId = "rule_system_include_total_price",
                    entrySlotKey = "amount",
                    slotBindings = listOf(
                        RuleSlotBinding.AttributeInput(
                            slotKey = "amount",
                            attributeKeySnapshot = "purchase_price",
                            attributeNameSnapshot = "购入价格",
                        ),
                        RuleSlotBinding.SystemOutput(
                            slotKey = "计入总价",
                            systemVariableKey = SystemVariableKey.GLOBAL_TOTAL_VALUE,
                        ),
                    ),
                    creationSource = RuleBindingCreationSource.RULE_TEMPLATE,
                ),
                RuleBindingInstance(
                    id = "template_binding_purchase_price_average_value",
                    ruleId = "rule_system_average_value",
                    entrySlotKey = "amount",
                    slotBindings = listOf(
                        RuleSlotBinding.AttributeInput(
                            slotKey = "amount",
                            attributeKeySnapshot = "purchase_price",
                            attributeNameSnapshot = "购入价格",
                        ),
                        RuleSlotBinding.AttributeInput(
                            slotKey = "startDate",
                            attributeKeySnapshot = "purchase_date",
                            attributeNameSnapshot = "购买日期",
                        ),
                        RuleSlotBinding.ReadonlyOutput(
                            slotKey = "平均价值",
                            displayLabel = "平均价值",
                        ),
                    ),
                    creationSource = RuleBindingCreationSource.RULE_TEMPLATE,
                )
            )
        ),
        AttributeTemplate(
            id = "template_attr_deposit",
            key = "template_deposit",
            name = "定金属性模板",
            category = "CORNERSTONE",
            defaultName = "定金",
            defaultIcon = "account_balance_wallet",
            defaultValueType = AttributeValueType.NUMBER,
            defaultValueProperties = AttributeValueProperties(
                numberFormat = AttributeNumberFormat.PRICE,
                defaultUnit = "CNY",
                allowedUnits = listOf("CNY", "USD", "EUR", "JPY", "GBP", "HKD", "TWD"),
                allowUnitSwitch = true,
                decimalPlaces = 2,
                allowNegative = false,
            ),
            defaultValueSource = AttributeValueSource.INPUT,
            defaultInteractionMode = AttributeInputMode.PRICE_INPUT,
            description = "适合分期、预付、尾款等场景",
            defaultRuleBindings = listOf(
                RuleBindingInstance(
                    id = "template_binding_deposit_remaining_payment",
                    ruleId = "rule_system_remaining_payment",
                    entrySlotKey = "deposit",
                    slotBindings = listOf(
                        RuleSlotBinding.AttributeInput(
                            slotKey = "deposit",
                            attributeKeySnapshot = "deposit",
                            attributeNameSnapshot = "定金",
                        ),
                        RuleSlotBinding.AttributeInput(
                            slotKey = "totalPrice",
                            attributeKeySnapshot = "item_total_price",
                            attributeNameSnapshot = "总价",
                        ),
                        RuleSlotBinding.ReadonlyOutput(
                            slotKey = "待付尾款",
                            displayLabel = "待付尾款",
                        ),
                    ),
                    creationSource = RuleBindingCreationSource.RULE_TEMPLATE,
                )
            )
        )
    )

    val systemRuleTemplates: List<RuleTemplate> = listOf(
        RuleTemplate(
            id = "template_rule_include_total_price",
            key = "rule_system_include_total_price",
            name = "计入总价规则模板",
            category = "ACCUMULATION",
            computationType = RuleComputationType.ACCUMULATION,
            triggerModes = listOf(RuleTriggerMode.ON_VALUE_CHANGED, RuleTriggerMode.ON_SAVE),
            slots = listOf(
                RuleSlotDefinition(
                    key = "amount",
                    name = "金额",
                    direction = RuleSlotDirection.INPUT,
                    valueType = RuleSlotValueType.NUMBER,
                    sourceType = RuleSlotSourceType.ATTRIBUTE_INPUT,
                ),
                RuleSlotDefinition(
                    key = "计入总价",
                    name = "计入总价",
                    direction = RuleSlotDirection.OUTPUT,
                    valueType = RuleSlotValueType.NUMBER,
                    sourceType = RuleSlotSourceType.SYSTEM_OUTPUT,
                    systemVariableKey = SystemVariableKey.GLOBAL_TOTAL_VALUE,
                ),
            ),
            defaultExpressionDefinition = RuleExpressionDefinition(
                expression = "amount",
                referencedSlotKeys = listOf("amount"),
            ),
            outputStrategies = listOf(
                RuleOutputStrategyDefinition(
                    slotKey = "计入总价",
                    targetType = RuleOutputTargetType.SYSTEM_VARIABLE,
                    systemVariableKey = SystemVariableKey.GLOBAL_TOTAL_VALUE,
                )
            ),
            description = "适合将金额字段纳入总价统计"
        ),
        RuleTemplate(
            id = "template_rule_remaining_payment",
            key = "rule_system_remaining_payment",
            name = "待付尾款规则模板",
            category = "DIFFERENCE",
            computationType = RuleComputationType.DIFFERENCE,
            triggerModes = listOf(RuleTriggerMode.ON_ATTRIBUTE_SELECTED, RuleTriggerMode.ON_VALUE_CHANGED),
            slots = listOf(
                RuleSlotDefinition(
                    key = "totalPrice",
                    name = "总价",
                    direction = RuleSlotDirection.INPUT,
                    valueType = RuleSlotValueType.NUMBER,
                    sourceType = RuleSlotSourceType.ATTRIBUTE_INPUT,
                ),
                RuleSlotDefinition(
                    key = "deposit",
                    name = "定金",
                    direction = RuleSlotDirection.INPUT,
                    valueType = RuleSlotValueType.NUMBER,
                    sourceType = RuleSlotSourceType.ATTRIBUTE_INPUT,
                ),
                RuleSlotDefinition(
                    key = "待付尾款",
                    name = "待付尾款",
                    direction = RuleSlotDirection.OUTPUT,
                    valueType = RuleSlotValueType.NUMBER,
                    sourceType = RuleSlotSourceType.READONLY_OUTPUT,
                ),
            ),
            defaultExpressionDefinition = RuleExpressionDefinition(
                expression = "totalPrice - deposit",
                referencedSlotKeys = listOf("totalPrice", "deposit"),
            ),
            outputStrategies = listOf(
                RuleOutputStrategyDefinition(
                    slotKey = "待付尾款",
                    targetType = RuleOutputTargetType.READONLY_RESULT,
                )
            ),
            description = "适合总价 - 定金类场景"
        ),
        RuleTemplate(
            id = "template_rule_average_value",
            key = "rule_system_average_value",
            name = "平均价值规则模板",
            category = "AVERAGE",
            computationType = RuleComputationType.AVERAGE,
            triggerModes = listOf(RuleTriggerMode.ON_ATTRIBUTE_SELECTED, RuleTriggerMode.ON_VALUE_CHANGED),
            slots = listOf(
                RuleSlotDefinition(
                    key = "amount",
                    name = "购入价格",
                    direction = RuleSlotDirection.INPUT,
                    valueType = RuleSlotValueType.NUMBER,
                    sourceType = RuleSlotSourceType.ATTRIBUTE_INPUT,
                ),
                RuleSlotDefinition(
                    key = "startDate",
                    name = "购买日期",
                    direction = RuleSlotDirection.INPUT,
                    valueType = RuleSlotValueType.DATE,
                    sourceType = RuleSlotSourceType.ATTRIBUTE_INPUT,
                ),
                RuleSlotDefinition(
                    key = "endDate",
                    name = "结束日期",
                    direction = RuleSlotDirection.INPUT,
                    valueType = RuleSlotValueType.DATE,
                    sourceType = RuleSlotSourceType.ATTRIBUTE_INPUT,
                ),
                RuleSlotDefinition(
                    key = "currentDate",
                    name = "当前日期",
                    direction = RuleSlotDirection.INPUT,
                    valueType = RuleSlotValueType.SYSTEM_DATE_TIME,
                    sourceType = RuleSlotSourceType.SYSTEM_INPUT,
                    systemVariableKey = SystemVariableKey.CURRENT_DATE,
                ),
                RuleSlotDefinition(
                    key = "平均价值",
                    name = "平均价值",
                    direction = RuleSlotDirection.OUTPUT,
                    valueType = RuleSlotValueType.NUMBER,
                    sourceType = RuleSlotSourceType.READONLY_OUTPUT,
                ),
            ),
            defaultExpressionDefinition = RuleExpressionDefinition(
                expression = "amount / dateDiff(startDate, coalesce(endDate, currentDate), \"DAY\")",
                referencedSlotKeys = listOf("amount", "startDate", "endDate", "currentDate"),
            ),
            outputStrategies = listOf(
                RuleOutputStrategyDefinition(
                    slotKey = "平均价值",
                    targetType = RuleOutputTargetType.READONLY_RESULT,
                )
            ),
            description = "适合价格与时间跨度联动分析"
        ),
        RuleTemplate(
            id = "template_rule_subscription_paid",
            key = "rule_system_subscription_paid",
            name = "订阅已支付规则模板",
            category = "ACCUMULATION",
            computationType = RuleComputationType.ACCUMULATION,
            triggerModes = listOf(RuleTriggerMode.ON_ATTRIBUTE_SELECTED, RuleTriggerMode.ON_VALUE_CHANGED),
            slots = listOf(
                RuleSlotDefinition(
                    key = "subscriptionPrice",
                    name = "订阅价格",
                    direction = RuleSlotDirection.INPUT,
                    valueType = RuleSlotValueType.NUMBER,
                    sourceType = RuleSlotSourceType.ATTRIBUTE_INPUT,
                ),
                RuleSlotDefinition(
                    key = "paymentDates",
                    name = "支付时间",
                    direction = RuleSlotDirection.INPUT,
                    valueType = RuleSlotValueType.DATE,
                    sourceType = RuleSlotSourceType.ATTRIBUTE_INPUT,
                ),
                RuleSlotDefinition(
                    key = "startDate",
                    name = "订阅开始时间",
                    direction = RuleSlotDirection.INPUT,
                    valueType = RuleSlotValueType.DATE,
                    sourceType = RuleSlotSourceType.ATTRIBUTE_INPUT,
                ),
                RuleSlotDefinition(
                    key = "已支付金额",
                    name = "已支付金额",
                    direction = RuleSlotDirection.OUTPUT,
                    valueType = RuleSlotValueType.NUMBER,
                    sourceType = RuleSlotSourceType.READONLY_OUTPUT,
                ),
                RuleSlotDefinition(
                    key = "已支付次数",
                    name = "已支付次数",
                    direction = RuleSlotDirection.OUTPUT,
                    valueType = RuleSlotValueType.NUMBER,
                    sourceType = RuleSlotSourceType.READONLY_OUTPUT,
                ),
            ),
            defaultExpressionDefinition = RuleExpressionDefinition(
                expression = "subscriptionPrice",
                referencedSlotKeys = listOf("subscriptionPrice", "paymentDates", "startDate"),
            ),
            outputStrategies = listOf(
                RuleOutputStrategyDefinition(
                    slotKey = "已支付金额",
                    targetType = RuleOutputTargetType.READONLY_RESULT,
                ),
                RuleOutputStrategyDefinition(
                    slotKey = "已支付次数",
                    targetType = RuleOutputTargetType.READONLY_RESULT,
                ),
            ),
            description = "适合订阅、周期付款类场景"
        )
    )

    fun getAllAttributes(): Flow<List<AttributeDefinition>> {
        return attributeDefinitionDao.getAllDefinitions().map { entities ->
            entities.map { it.toModel() }
        }
    }

    fun getAllAttributeEntities(): Flow<List<AttributeDefinitionEntity>> {
        return attributeDefinitionDao.getAllDefinitions()
    }

    suspend fun getAttributeById(id: String): AttributeDefinition? {
        return attributeDefinitionDao.getDefinitionById(id)?.toModel()
    }

    suspend fun saveAttribute(attribute: AttributeDefinition) {
        attributeDefinitionDao.insert(normalizeAttributeDefinition(attribute).toEntity())
    }

    suspend fun deleteAttribute(attribute: AttributeDefinition) {
        attributeDefinitionDao.delete(attribute.toEntity())
    }

    suspend fun ensureItemSystemAttributes() {
        val existingIds = attributeDefinitionDao.getAllDefinitions().first().map { it.id }.toSet()
        itemSystemAttributes
            .filterNot { it.id in existingIds }
            .forEach { saveAttribute(it) }
        migrateDevelopmentRuleModelData()
    }

    // --- Rule Definitions ---

    fun getAllRules(): Flow<List<RuleDefinition>> {
        return ruleDefinitionDao.getAllDefinitions().map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun ensureSystemRules() {
        val existingIds = ruleDefinitionDao.getAllDefinitions().first().map { it.id }.toSet()
        buildSystemRuleDefinitions()
            .filterNot { it.id in existingIds }
            .forEach { saveRule(it) }
    }

    suspend fun getRuleById(id: String): RuleDefinition? {
        return ruleDefinitionDao.getDefinitionById(id)?.toModel()
    }

    suspend fun saveRule(rule: RuleDefinition) {
        ruleDefinitionDao.insert(normalizeRuleDefinition(rule).toEntity())
    }

    suspend fun deleteRule(rule: RuleDefinition) {
        ruleDefinitionDao.delete(rule.toEntity())
    }

    suspend fun migrateDevelopmentRuleModelData() {
        attributeDefinitionDao.getAllDefinitions().first()
            .map { entity -> normalizeAttributeDefinition(entity.toModel()).toEntity() }
            .forEach { entity -> attributeDefinitionDao.insert(entity) }
        ruleDefinitionDao.getAllDefinitions().first()
            .map { entity -> normalizeRuleDefinition(entity.toModel()).toEntity() }
            .forEach { entity -> ruleDefinitionDao.insert(entity) }
    }

    private fun buildSystemRuleDefinitions(): List<RuleDefinition> {
        return systemRuleTemplates.map { template ->
            RuleDefinition(
                id = template.key,
                key = template.key,
                name = template.name.removeSuffix("规则模板").removeSuffix("模板"),
                computationType = template.computationType,
                triggerModes = template.triggerModes,
                slots = template.slots,
                expressionDefinition = template.defaultExpressionDefinition,
                outputStrategies = template.outputStrategies,
                description = template.description,
                templateId = template.id,
            )
        }
    }

    // --- Entity/Model Mappers ---

    private fun AttributeDefinitionEntity.toModel(): AttributeDefinition {
        val typeOptionItems = object : TypeToken<List<String>>() {}.type
        val typeRuleBindings = object : TypeToken<List<RuleBinding>>() {}.type
        val typeLegacyRuleBindings = object : TypeToken<List<LegacyRuleBindingPayload>>() {}.type
        val typeValueProperties = object : TypeToken<AttributeValueProperties>() {}.type
        val legacyOptionItems = try { gson.fromJson<List<String>>(optionItemsJson, typeOptionItems) ?: emptyList() } catch (e: Exception) { emptyList() }
        val parsedValueProperties = try { gson.fromJson<AttributeValueProperties>(valuePropertiesJson, typeValueProperties) } catch (e: Exception) { null }
        val parsedRuleBindings = try {
            gson.fromJson<List<RuleBinding>>(ruleBindingsJson, typeRuleBindings)
                ?.map(::normalizeRuleBinding)
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        val fallbackRuleBindings = if (parsedRuleBindings.isEmpty()) {
            try {
                gson.fromJson<List<LegacyRuleBindingPayload>>(ruleBindingsJson, typeLegacyRuleBindings)
                    ?.map { legacy ->
                        RuleBindingInstance(
                            id = "${legacy.ruleId}_${legacy.inputRole}",
                            ruleId = legacy.ruleId,
                            entrySlotKey = legacy.inputRole,
                            slotBindings = buildList {
                                add(
                                    RuleSlotBinding.AttributeInput(
                                        slotKey = legacy.inputRole,
                                        attributeNameSnapshot = name,
                                    )
                                )
                                legacy.requiredDependencies.forEach { dependency ->
                                    add(
                                        RuleSlotBinding.AttributeInput(
                                            slotKey = dependency,
                                            attributeNameSnapshot = dependency,
                                            isRequired = true,
                                        )
                                    )
                                }
                                (legacy.optionalDependencies - legacy.requiredDependencies.toSet()).forEach { dependency ->
                                    add(
                                        RuleSlotBinding.AttributeInput(
                                            slotKey = dependency,
                                            attributeNameSnapshot = dependency,
                                            isRequired = true,
                                        )
                                    )
                                }
                                legacy.outputKeys.forEach { outputKey ->
                                    add(
                                        RuleSlotBinding.ReadonlyOutput(
                                            slotKey = outputKey,
                                            displayLabel = outputKey,
                                        )
                                    )
                                }
                            },
                            creationSource = RuleBindingCreationSource.MIGRATION,
                        )
                    }
                    ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        val valueProperties = parsedValueProperties ?: legacyValueProperties(
            valueType = valueType,
            optionSource = optionSource,
            isMultiValue = isMultiValue,
            optionItems = legacyOptionItems,
            inputMode = inputMode,
        )

        return AttributeDefinition(
            id = id,
            key = key,
            name = name,
            ownerType = ownerType,
            valueType = valueType,
            optionSource = valueSource.toLegacyOptionSource(valueProperties),
            inputMode = interactionMode,
            isMultiValue = valueProperties.isMultiValue,
            optionItems = valueProperties.optionItems,
            valueProperties = valueProperties,
            valueSource = valueSource,
            interactionMode = interactionMode,
            icon = icon,
            templateId = templateId,
            ruleBindings = parsedRuleBindings.ifEmpty { fallbackRuleBindings }.map(::normalizeRuleBinding),
            description = description
        )
    }

    private fun AttributeDefinition.toEntity(): AttributeDefinitionEntity {
        val legacyOptionSource = valueSource.toLegacyOptionSource(valueProperties)
        return AttributeDefinitionEntity(
            id = id,
            key = key,
            name = name,
            ownerType = ownerType,
            valueType = valueType,
            valueSource = valueSource,
            interactionMode = interactionMode,
            valuePropertiesJson = gson.toJson(valueProperties),
            optionSource = legacyOptionSource,
            inputMode = interactionMode,
            isMultiValue = valueProperties.isMultiValue,
            optionItemsJson = gson.toJson(valueProperties.optionItems),
            icon = icon,
            templateId = templateId,
            ruleBindingsJson = gson.toJson(ruleBindings),
            description = description
        )
    }

    private fun RuleDefinitionEntity.toModel(): RuleDefinition {
        val typeStringList = object : TypeToken<List<String>>() {}.type
        val typeTriggerModeList = object : TypeToken<List<RuleTriggerMode>>() {}.type
        val typeSystemInputList = object : TypeToken<List<RuleSystemInputDefinition>>() {}.type
        val typeOutputTargetList = object : TypeToken<List<RuleOutputTargetDefinition>>() {}.type
        val typeSlotList = object : TypeToken<List<RuleSlotDefinition>>() {}.type
        val typeOutputStrategyList = object : TypeToken<List<RuleOutputStrategyDefinition>>() {}.type
        val parsedTriggerModes = try {
            gson.fromJson<List<RuleTriggerMode>>(triggerModesJson, typeTriggerModeList) ?: emptyList<RuleTriggerMode>()
        } catch (e: Exception) {
            emptyList()
        }.ifEmpty { listOf(RuleTriggerMode.ON_VALUE_CHANGED) }
        val parsedSlots = try {
            gson.fromJson<List<RuleSlotDefinition>>(inputRolesJson, typeSlotList) ?: emptyList<RuleSlotDefinition>()
        } catch (e: Exception) {
            emptyList()
        }
        val parsedOutputStrategies = try {
            gson.fromJson<List<RuleOutputStrategyDefinition>>(requiredDependenciesJson, typeOutputStrategyList)
                ?: emptyList<RuleOutputStrategyDefinition>()
        } catch (e: Exception) {
            emptyList()
        }
        if (parsedSlots.isNotEmpty()) {
            return normalizeRuleDefinition(
                RuleDefinition(
                id = id,
                key = key,
                name = name,
                computationType = computationType,
                triggerModes = parsedTriggerModes,
                slots = parsedSlots,
                expressionDefinition = expression?.takeIf { it.isNotBlank() }?.let {
                    RuleExpressionDefinition(expression = it)
                },
                outputStrategies = parsedOutputStrategies,
                description = description,
            ))
        }
        val inputRoles = try { gson.fromJson<List<String>>(inputRolesJson, typeStringList) ?: emptyList<String>() } catch (e: Exception) { emptyList<String>() }
        val requiredDependencies = try { gson.fromJson<List<String>>(requiredDependenciesJson, typeStringList) ?: emptyList<String>() } catch (e: Exception) { emptyList<String>() }
        val optionalDependencies = try { gson.fromJson<List<String>>(optionalDependenciesJson, typeStringList) ?: emptyList<String>() } catch (e: Exception) { emptyList<String>() }
        val outputKeys = try { gson.fromJson<List<String>>(outputKeysJson, typeStringList) ?: emptyList<String>() } catch (e: Exception) { emptyList<String>() }
        val systemInputs = try { gson.fromJson<List<RuleSystemInputDefinition>>(systemInputsJson, typeSystemInputList) ?: emptyList<RuleSystemInputDefinition>() } catch (e: Exception) { emptyList<RuleSystemInputDefinition>() }
        val outputTargets = try { gson.fromJson<List<RuleOutputTargetDefinition>>(outputTargetsJson, typeOutputTargetList) ?: emptyList<RuleOutputTargetDefinition>() } catch (e: Exception) { emptyList<RuleOutputTargetDefinition>() }
        val legacyAttributeRoles = inputRoles.filterNot { role -> systemInputs.any { it.role == role } }
        val requiredAttributeNames = (requiredDependencies + optionalDependencies).toMutableList()
        val legacySlots = buildList {
            legacyAttributeRoles.forEachIndexed { index, role ->
                val displayName = requiredAttributeNames.getOrElse(index) { role }
                add(
                    RuleSlotDefinition(
                        key = role,
                        name = displayName,
                        direction = RuleSlotDirection.INPUT,
                        valueType = RuleSlotValueType.TEXT,
                        sourceType = RuleSlotSourceType.ATTRIBUTE_INPUT,
                        isRequired = true,
                    )
                )
            }
            systemInputs.forEach { input ->
                add(
                    RuleSlotDefinition(
                        key = input.role,
                        name = input.role,
                        direction = RuleSlotDirection.INPUT,
                        valueType = RuleSlotValueType.SYSTEM_DATE_TIME,
                        sourceType = RuleSlotSourceType.SYSTEM_INPUT,
                        isRequired = input.required,
                        systemVariableKey = input.variableKey,
                    )
                )
            }
            outputKeys.forEach { outputKey ->
                val target = outputTargets.firstOrNull { it.outputKey == outputKey.trim() }
                add(
                    RuleSlotDefinition(
                        key = outputKey.trim(),
                        name = outputKey.trim(),
                        direction = RuleSlotDirection.OUTPUT,
                        valueType = RuleSlotValueType.TEXT,
                        sourceType = when (target?.targetType) {
                            RuleOutputTargetType.ATTRIBUTE_VALUE -> RuleSlotSourceType.ATTRIBUTE_OUTPUT
                            RuleOutputTargetType.SYSTEM_VARIABLE -> RuleSlotSourceType.SYSTEM_OUTPUT
                            else -> RuleSlotSourceType.READONLY_OUTPUT
                        },
                        systemVariableKey = target?.variableKey,
                    )
                )
            }
        }
        val legacyOutputStrategies = outputTargets.map { target ->
            RuleOutputStrategyDefinition(
                slotKey = target.outputKey.trim(),
                targetType = target.targetType,
                systemVariableKey = target.variableKey,
            )
        }
        return normalizeRuleDefinition(RuleDefinition(
            id = id,
            key = key,
            name = name,
            computationType = computationType,
            triggerModes = parsedTriggerModes,
            slots = legacySlots,
            expressionDefinition = expression?.takeIf { it.isNotBlank() }?.let {
                RuleExpressionDefinition(expression = it, referencedSlotKeys = inputRoles + outputKeys)
            },
            outputStrategies = legacyOutputStrategies,
            description = description
        ))
    }

    private fun RuleDefinition.toEntity(): RuleDefinitionEntity {
        return RuleDefinitionEntity(
            id = id,
            key = key,
            name = name,
            computationType = computationType,
            triggerModesJson = gson.toJson(triggerModes),
            inputRolesJson = gson.toJson(slots),
            requiredDependenciesJson = gson.toJson(outputStrategies),
            optionalDependenciesJson = gson.toJson(emptyList<String>()),
            outputKeysJson = gson.toJson(emptyList<String>()),
            systemInputsJson = gson.toJson(emptyList<RuleSystemInputDefinition>()),
            outputTargetsJson = gson.toJson(emptyList<RuleOutputTargetDefinition>()),
            expression = expression,
            description = description
        )
    }

    private fun normalizeAttributeDefinition(attribute: AttributeDefinition): AttributeDefinition {
        return attribute.copy(
            ruleBindings = attribute.ruleBindings.map(::normalizeRuleBinding),
        )
    }

    private fun normalizeRuleBinding(binding: RuleBinding): RuleBinding {
        return binding.copy(
            slotBindings = binding.slotBindings.map { slotBinding ->
                when (slotBinding) {
                    is RuleSlotBinding.AttributeInput -> slotBinding.copy(isRequired = true)
                    else -> slotBinding
                }
            }
        )
    }

    private fun normalizeRuleDefinition(rule: RuleDefinition): RuleDefinition {
        return rule.copy(
            slots = rule.slots.map(::normalizeRuleSlotDefinition),
        )
    }

    private fun normalizeRuleSlotDefinition(slot: RuleSlotDefinition): RuleSlotDefinition {
        return if (slot.direction == RuleSlotDirection.INPUT &&
            slot.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT
        ) {
            slot.copy(isRequired = true)
        } else {
            slot
        }
    }
}
