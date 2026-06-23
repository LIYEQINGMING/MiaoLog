package com.example.itemmanagement.data.repository

import com.example.itemmanagement.data.dao.attribute.AttributeDefinitionDao
import com.example.itemmanagement.data.dao.attribute.RuleDefinitionDao
import com.example.itemmanagement.data.entity.attribute.AttributeDefinitionEntity
import com.example.itemmanagement.data.entity.attribute.RuleDefinitionEntity
import com.example.itemmanagement.data.model.attribute.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AttributeRepository(
    private val attributeDefinitionDao: AttributeDefinitionDao,
    private val ruleDefinitionDao: RuleDefinitionDao
) {
    private val gson = Gson()

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
            defaultOptionSource = AttributeOptionSource.INPUT,
            defaultInputMode = AttributeInputMode.TEXT_INPUT,
            defaultMultiValue = false,
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
            defaultOptionSource = AttributeOptionSource.INPUT,
            defaultInputMode = AttributeInputMode.DATE_PICKER,
            defaultMultiValue = false,
            description = "适合购买日期、到期时间、纪念日等日期字段"
        ),
        AttributeTemplate(
            id = "template_attr_fixed_option",
            key = "template_fixed_option",
            name = "固定选项模板",
            category = "FOUNDATION",
            defaultName = "新建单选属性",
            defaultIcon = "checklist",
            defaultValueType = AttributeValueType.TEXT,
            defaultOptionSource = AttributeOptionSource.FIXED_OPTIONS_USER,
            defaultInputMode = AttributeInputMode.SINGLE_SELECT,
            defaultMultiValue = false,
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
            defaultOptionSource = AttributeOptionSource.INPUT,
            defaultInputMode = AttributeInputMode.PRICE_INPUT,
            defaultMultiValue = false,
            description = "适合带统计规则的价格字段",
            defaultRuleBindings = listOf(
                RuleBinding(
                    ruleId = "rule_system_include_total_price",
                    inputRole = "amount",
                    outputKeys = listOf("计入总价")
                ),
                RuleBinding(
                    ruleId = "rule_system_average_value",
                    inputRole = "amount",
                    requiredDependencies = listOf("购买日期"),
                    optionalDependencies = listOf("结束日期"),
                    outputKeys = listOf("平均价值")
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
            defaultOptionSource = AttributeOptionSource.INPUT,
            defaultInputMode = AttributeInputMode.PRICE_INPUT,
            defaultMultiValue = false,
            description = "适合分期、预付、尾款等场景",
            defaultRuleBindings = listOf(
                RuleBinding(
                    ruleId = "rule_system_remaining_payment",
                    inputRole = "deposit",
                    requiredDependencies = listOf("总价"),
                    outputKeys = listOf("待付尾款")
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
            inputRoles = listOf("amount"),
            requiredDependencies = listOf("购入价格"),
            outputKeys = listOf("计入总价"),
            description = "适合将金额字段纳入总价统计"
        ),
        RuleTemplate(
            id = "template_rule_remaining_payment",
            key = "rule_system_remaining_payment",
            name = "待付尾款规则模板",
            category = "DIFFERENCE",
            computationType = RuleComputationType.DIFFERENCE,
            inputRoles = listOf("totalPrice", "deposit"),
            requiredDependencies = listOf("总价", "定金"),
            outputKeys = listOf("待付尾款"),
            description = "适合总价 - 定金类场景"
        ),
        RuleTemplate(
            id = "template_rule_average_value",
            key = "rule_system_average_value",
            name = "平均价值规则模板",
            category = "AVERAGE",
            computationType = RuleComputationType.AVERAGE,
            inputRoles = listOf("amount", "startDate", "endDate"),
            requiredDependencies = listOf("购入价格", "购买日期"),
            optionalDependencies = listOf("结束日期"),
            outputKeys = listOf("平均价值"),
            description = "适合价格与时间跨度联动分析"
        ),
        RuleTemplate(
            id = "template_rule_subscription_paid",
            key = "rule_system_subscription_paid",
            name = "订阅已支付规则模板",
            category = "ACCUMULATION",
            computationType = RuleComputationType.ACCUMULATION,
            inputRoles = listOf("subscriptionPrice", "paymentDates"),
            requiredDependencies = listOf("订阅价格", "支付时间"),
            optionalDependencies = listOf("订阅开始时间"),
            outputKeys = listOf("已支付金额", "已支付次数"),
            description = "适合订阅、周期付款类场景"
        )
    )

    fun getAllAttributes(): Flow<List<AttributeDefinition>> {
        return attributeDefinitionDao.getAllDefinitions().map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun getAttributeById(id: String): AttributeDefinition? {
        return attributeDefinitionDao.getDefinitionById(id)?.toModel()
    }

    suspend fun saveAttribute(attribute: AttributeDefinition) {
        attributeDefinitionDao.insert(attribute.toEntity())
    }

    suspend fun deleteAttribute(attribute: AttributeDefinition) {
        attributeDefinitionDao.delete(attribute.toEntity())
    }

    // --- Rule Definitions ---

    fun getAllRules(): Flow<List<RuleDefinition>> {
        return ruleDefinitionDao.getAllDefinitions().map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun getRuleById(id: String): RuleDefinition? {
        return ruleDefinitionDao.getDefinitionById(id)?.toModel()
    }

    suspend fun saveRule(rule: RuleDefinition) {
        ruleDefinitionDao.insert(rule.toEntity())
    }

    suspend fun deleteRule(rule: RuleDefinition) {
        ruleDefinitionDao.delete(rule.toEntity())
    }

    // --- Entity/Model Mappers ---

    private fun AttributeDefinitionEntity.toModel(): AttributeDefinition {
        val typeOptionItems = object : TypeToken<List<String>>() {}.type
        val typeRuleBindings = object : TypeToken<List<RuleBinding>>() {}.type

        return AttributeDefinition(
            id = id,
            key = key,
            name = name,
            ownerType = ownerType,
            valueType = valueType,
            optionSource = optionSource,
            inputMode = inputMode,
            isMultiValue = isMultiValue,
            optionItems = try { gson.fromJson(optionItemsJson, typeOptionItems) ?: emptyList() } catch (e: Exception) { emptyList() },
            icon = icon,
            templateId = templateId,
            ruleBindings = try { gson.fromJson(ruleBindingsJson, typeRuleBindings) ?: emptyList() } catch (e: Exception) { emptyList() },
            description = description
        )
    }

    private fun AttributeDefinition.toEntity(): AttributeDefinitionEntity {
        return AttributeDefinitionEntity(
            id = id,
            key = key,
            name = name,
            ownerType = ownerType,
            valueType = valueType,
            optionSource = optionSource,
            inputMode = inputMode,
            isMultiValue = isMultiValue,
            optionItemsJson = gson.toJson(optionItems),
            icon = icon,
            templateId = templateId,
            ruleBindingsJson = gson.toJson(ruleBindings),
            description = description
        )
    }

    private fun RuleDefinitionEntity.toModel(): RuleDefinition {
        val typeStringList = object : TypeToken<List<String>>() {}.type
        return RuleDefinition(
            id = id,
            key = key,
            name = name,
            computationType = computationType,
            inputRoles = try { gson.fromJson(inputRolesJson, typeStringList) ?: emptyList() } catch (e: Exception) { emptyList() },
            requiredDependencies = try { gson.fromJson(requiredDependenciesJson, typeStringList) ?: emptyList() } catch (e: Exception) { emptyList() },
            optionalDependencies = try { gson.fromJson(optionalDependenciesJson, typeStringList) ?: emptyList() } catch (e: Exception) { emptyList() },
            outputKeys = try { gson.fromJson(outputKeysJson, typeStringList) ?: emptyList() } catch (e: Exception) { emptyList() },
            expression = expression,
            description = description
        )
    }

    private fun RuleDefinition.toEntity(): RuleDefinitionEntity {
        return RuleDefinitionEntity(
            id = id,
            key = key,
            name = name,
            computationType = computationType,
            inputRolesJson = gson.toJson(inputRoles),
            requiredDependenciesJson = gson.toJson(requiredDependencies),
            optionalDependenciesJson = gson.toJson(optionalDependencies),
            outputKeysJson = gson.toJson(outputKeys),
            expression = expression,
            description = description
        )
    }
}
