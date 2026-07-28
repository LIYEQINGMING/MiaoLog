package com.example.itemmanagement.data.model.attribute

import java.util.Date

enum class SystemVariableCategory {
    PROFILE,
    TIME,
    LOCATION,
    DEVICE,
    AGGREGATION,
    RULE_DERIVED,
}

enum class SystemVariableProviderType {
    PROFILE,
    CLOCK,
    LOCATION_SERVICE,
    DEVICE_CONTEXT,
    AGGREGATION,
    RULE_OUTPUT,
}

enum class SystemVariableWritePolicy {
    READ_ONLY,
    RULE_WRITABLE,
}

enum class SystemVariableRefreshPolicy {
    ON_ACCESS,
    ON_PROFILE_CHANGE,
    ON_LOCATION_UPDATE,
    ON_AGGREGATE_RECALCULATE,
    ON_RULE_EVALUATION,
}

enum class SystemVariableConfigValueType {
    TEXT,
    NUMBER,
    BOOLEAN,
    SELECT,
}

enum class SystemVariableImplementationStatus {
    IMPLEMENTED,
    DEGRADED,
    PLANNED,
}

enum class SystemVariableKey(val storageKey: String, val displayName: String) {
    ACCOUNT_CREATED_AT("account_created_at", "账户创建时间"),
    APP_MEMBERSHIP_DAYS("app_membership_days", "已加入 App 时长"),
    CURRENT_DATE("current_date", "当前日期"),
    CURRENT_TIME("current_time", "当前时间"),
    CURRENT_CALENDAR_TIME("current_calendar_time", "统一日历时间"),
    CURRENT_TIMEZONE("current_timezone", "当前时区"),
    CURRENT_USER_ID("current_user_id", "当前用户标识"),
    CURRENT_LOCATION("current_location", "当前定位"),
    GLOBAL_TOTAL_VALUE("global_total_value", "全局总价值"),
    GLOBAL_TOTAL_COUNT("global_total_count", "全局总数量"),
}

enum class RuleInputSourceType {
    ATTRIBUTE,
    SYSTEM_VARIABLE,
}

enum class RuleOutputTargetType {
    READONLY_RESULT,
    ATTRIBUTE_VALUE,
    SYSTEM_VARIABLE,
}

enum class RuleSlotDirection {
    INPUT,
    OUTPUT,
}

enum class RuleSlotValueType {
    TEXT,
    NUMBER,
    DATE,
    BOOLEAN,
    SELECT,
    CYCLE_UNIT,
    SYSTEM_DATE_TIME,
}

enum class RuleSlotSourceType {
    ATTRIBUTE_INPUT,
    CONFIG_INPUT,
    SYSTEM_INPUT,
    ATTRIBUTE_OUTPUT,
    READONLY_OUTPUT,
    SYSTEM_OUTPUT,
}

enum class RuleOutputUpdateMode {
    OVERWRITE,
    ACCUMULATE,
}

enum class RuleTriggerMode {
    ON_ATTRIBUTE_SELECTED,
    ON_VALUE_CHANGED,
    ON_FORM_OPENED,
    ON_SAVE,
    ON_SYSTEM_INPUT_CHANGED,
}

enum class RuleActivationMode {
    ALWAYS_ON,
    USER_TOGGLE,
}

enum class RuleBindingStatus {
    DRAFT,
    ACTIVE,
    INVALID,
}

enum class RuleBindingCreationSource {
    ATTRIBUTE_MANAGEMENT,
    RULE_TEMPLATE,
    QUICK_CREATE_ATTRIBUTE,
    MIGRATION,
}

data class RuleSystemInputDefinition(
    val role: String,
    val sourceType: RuleInputSourceType,
    val variableKey: SystemVariableKey? = null,
    val required: Boolean = true,
)

data class RuleOutputTargetDefinition(
    val outputKey: String,
    val targetType: RuleOutputTargetType,
    val attributeId: String? = null,
    val attributeKey: String? = null,
    val variableKey: SystemVariableKey? = null,
)

data class RuleRuntimeMetadata(
    val ruleId: String,
    val systemInputs: List<RuleSystemInputDefinition> = emptyList(),
    val outputTargets: List<RuleOutputTargetDefinition> = emptyList(),
)

data class RuleToggleUiConfig(
    val labelWhenEnabled: String = "已启用",
    val labelWhenDisabled: String = "已停用",
    val anchorSlotKey: String? = null,
    val defaultEnabled: Boolean = true,
)

data class SystemVariableConfigDefinition(
    val key: String,
    val label: String,
    val valueType: SystemVariableConfigValueType,
    val required: Boolean = false,
    val options: List<String> = emptyList(),
    val description: String? = null,
)

data class SystemVariableDefinition(
    val key: SystemVariableKey,
    val valueType: AttributeValueType,
    val category: SystemVariableCategory,
    val providerType: SystemVariableProviderType,
    val writePolicy: SystemVariableWritePolicy,
    val refreshPolicy: SystemVariableRefreshPolicy,
    val implementationStatus: SystemVariableImplementationStatus = SystemVariableImplementationStatus.PLANNED,
    val description: String? = null,
    val configDefinitions: List<SystemVariableConfigDefinition> = emptyList(),
)

data class AppSystemSourceValue(
    val variableKey: SystemVariableKey,
    val valueType: AttributeValueType,
    val textValue: String? = null,
    val numberValue: Double? = null,
    val dateValue: Date? = null,
    val unit: String? = null,
    val isAvailable: Boolean = true,
    val availabilityMessage: String? = null,
    val configValues: Map<String, String> = emptyMap(),
)

data class AppSystemSourceSnapshot(
    val variables: List<AppSystemSourceValue>,
)

fun builtInSystemVariableDefinitions(): List<SystemVariableDefinition> {
    return listOf(
        SystemVariableDefinition(
            key = SystemVariableKey.ACCOUNT_CREATED_AT,
            valueType = AttributeValueType.DATE,
            category = SystemVariableCategory.PROFILE,
            providerType = SystemVariableProviderType.PROFILE,
            writePolicy = SystemVariableWritePolicy.READ_ONLY,
            refreshPolicy = SystemVariableRefreshPolicy.ON_PROFILE_CHANGE,
            implementationStatus = SystemVariableImplementationStatus.IMPLEMENTED,
            description = "用户账户或 App 本地资料创建时间",
        ),
        SystemVariableDefinition(
            key = SystemVariableKey.APP_MEMBERSHIP_DAYS,
            valueType = AttributeValueType.NUMBER,
            category = SystemVariableCategory.PROFILE,
            providerType = SystemVariableProviderType.PROFILE,
            writePolicy = SystemVariableWritePolicy.READ_ONLY,
            refreshPolicy = SystemVariableRefreshPolicy.ON_ACCESS,
            implementationStatus = SystemVariableImplementationStatus.IMPLEMENTED,
            description = "根据账户创建时间动态计算的加入时长",
        ),
        SystemVariableDefinition(
            key = SystemVariableKey.CURRENT_DATE,
            valueType = AttributeValueType.DATE,
            category = SystemVariableCategory.TIME,
            providerType = SystemVariableProviderType.CLOCK,
            writePolicy = SystemVariableWritePolicy.READ_ONLY,
            refreshPolicy = SystemVariableRefreshPolicy.ON_ACCESS,
            implementationStatus = SystemVariableImplementationStatus.IMPLEMENTED,
            description = "系统当前日期",
            configDefinitions = listOf(
                SystemVariableConfigDefinition(
                    key = "timezone",
                    label = "时区",
                    valueType = SystemVariableConfigValueType.TEXT,
                    description = "日期解释与展示使用的时区",
                ),
            ),
        ),
        SystemVariableDefinition(
            key = SystemVariableKey.CURRENT_TIME,
            valueType = AttributeValueType.TEXT,
            category = SystemVariableCategory.TIME,
            providerType = SystemVariableProviderType.CLOCK,
            writePolicy = SystemVariableWritePolicy.READ_ONLY,
            refreshPolicy = SystemVariableRefreshPolicy.ON_ACCESS,
            implementationStatus = SystemVariableImplementationStatus.IMPLEMENTED,
            description = "系统当前时间",
            configDefinitions = listOf(
                SystemVariableConfigDefinition(
                    key = "timezone",
                    label = "时区",
                    valueType = SystemVariableConfigValueType.TEXT,
                    description = "时间解释与展示使用的时区",
                ),
            ),
        ),
        SystemVariableDefinition(
            key = SystemVariableKey.CURRENT_CALENDAR_TIME,
            valueType = AttributeValueType.DATE,
            category = SystemVariableCategory.TIME,
            providerType = SystemVariableProviderType.CLOCK,
            writePolicy = SystemVariableWritePolicy.READ_ONLY,
            refreshPolicy = SystemVariableRefreshPolicy.ON_ACCESS,
            implementationStatus = SystemVariableImplementationStatus.IMPLEMENTED,
            description = "统一供规则读取的日历时间上下文",
            configDefinitions = listOf(
                SystemVariableConfigDefinition(
                    key = "timezone",
                    label = "时区",
                    valueType = SystemVariableConfigValueType.TEXT,
                    description = "统一日历时间使用的时区",
                ),
                SystemVariableConfigDefinition(
                    key = "weekStart",
                    label = "每周起始日",
                    valueType = SystemVariableConfigValueType.SELECT,
                    options = listOf("MONDAY", "SUNDAY"),
                    description = "周期与周历规则计算所用的起始日",
                ),
            ),
        ),
        SystemVariableDefinition(
            key = SystemVariableKey.CURRENT_TIMEZONE,
            valueType = AttributeValueType.TEXT,
            category = SystemVariableCategory.TIME,
            providerType = SystemVariableProviderType.CLOCK,
            writePolicy = SystemVariableWritePolicy.READ_ONLY,
            refreshPolicy = SystemVariableRefreshPolicy.ON_ACCESS,
            implementationStatus = SystemVariableImplementationStatus.IMPLEMENTED,
            description = "当前设备使用的时区标识",
        ),
        SystemVariableDefinition(
            key = SystemVariableKey.CURRENT_USER_ID,
            valueType = AttributeValueType.TEXT,
            category = SystemVariableCategory.PROFILE,
            providerType = SystemVariableProviderType.PROFILE,
            writePolicy = SystemVariableWritePolicy.READ_ONLY,
            refreshPolicy = SystemVariableRefreshPolicy.ON_PROFILE_CHANGE,
            implementationStatus = SystemVariableImplementationStatus.IMPLEMENTED,
            description = "当前用户资料中的稳定用户标识",
        ),
        SystemVariableDefinition(
            key = SystemVariableKey.CURRENT_LOCATION,
            valueType = AttributeValueType.TEXT,
            category = SystemVariableCategory.LOCATION,
            providerType = SystemVariableProviderType.LOCATION_SERVICE,
            writePolicy = SystemVariableWritePolicy.READ_ONLY,
            refreshPolicy = SystemVariableRefreshPolicy.ON_LOCATION_UPDATE,
            implementationStatus = SystemVariableImplementationStatus.DEGRADED,
            description = "当前设备定位或最近一次有效定位",
            configDefinitions = listOf(
                SystemVariableConfigDefinition(
                    key = "precisionMode",
                    label = "定位精度",
                    valueType = SystemVariableConfigValueType.SELECT,
                    options = listOf("COARSE", "PRECISE"),
                    description = "定位取值时使用的大致定位或精确定位",
                ),
            ),
        ),
        SystemVariableDefinition(
            key = SystemVariableKey.GLOBAL_TOTAL_VALUE,
            valueType = AttributeValueType.NUMBER,
            category = SystemVariableCategory.AGGREGATION,
            providerType = SystemVariableProviderType.AGGREGATION,
            writePolicy = SystemVariableWritePolicy.RULE_WRITABLE,
            refreshPolicy = SystemVariableRefreshPolicy.ON_AGGREGATE_RECALCULATE,
            implementationStatus = SystemVariableImplementationStatus.IMPLEMENTED,
            description = "所有计入总价值的价格聚合结果",
            configDefinitions = listOf(
                SystemVariableConfigDefinition(
                    key = "currency",
                    label = "基准币种",
                    valueType = SystemVariableConfigValueType.TEXT,
                    required = true,
                    description = "全局总价值统一换算与展示使用的币种",
                ),
            ),
        ),
        SystemVariableDefinition(
            key = SystemVariableKey.GLOBAL_TOTAL_COUNT,
            valueType = AttributeValueType.NUMBER,
            category = SystemVariableCategory.AGGREGATION,
            providerType = SystemVariableProviderType.AGGREGATION,
            writePolicy = SystemVariableWritePolicy.RULE_WRITABLE,
            refreshPolicy = SystemVariableRefreshPolicy.ON_AGGREGATE_RECALCULATE,
            implementationStatus = SystemVariableImplementationStatus.IMPLEMENTED,
            description = "当前计入总数量统计的全局数量结果",
        ),
    )
}

fun findSystemVariableDefinition(key: SystemVariableKey): SystemVariableDefinition? {
    return builtInSystemVariableDefinitions().firstOrNull { it.key == key }
}

fun findSystemVariableKey(storageKey: String): SystemVariableKey? {
    val normalizedKey = storageKey.trim()
    return SystemVariableKey.entries.firstOrNull { it.storageKey == normalizedKey }
}

fun ruleRuntimeMetadata(ruleId: String): RuleRuntimeMetadata? {
    return when (ruleId) {
        "rule_system_include_total_price" -> RuleRuntimeMetadata(
            ruleId = ruleId,
            systemInputs = listOf(
                RuleSystemInputDefinition(
                    role = "baseCurrency",
                    sourceType = RuleInputSourceType.SYSTEM_VARIABLE,
                    variableKey = SystemVariableKey.GLOBAL_TOTAL_VALUE,
                ),
            ),
            outputTargets = listOf(
                RuleOutputTargetDefinition(
                    outputKey = "计入总价",
                    targetType = RuleOutputTargetType.SYSTEM_VARIABLE,
                    variableKey = SystemVariableKey.GLOBAL_TOTAL_VALUE,
                ),
            ),
        )

        "rule_system_average_value" -> RuleRuntimeMetadata(
            ruleId = ruleId,
            systemInputs = listOf(
                RuleSystemInputDefinition(
                    role = "currentDate",
                    sourceType = RuleInputSourceType.SYSTEM_VARIABLE,
                    variableKey = SystemVariableKey.CURRENT_DATE,
                    required = false,
                ),
            ),
            outputTargets = listOf(
                RuleOutputTargetDefinition(
                    outputKey = "平均价值",
                    targetType = RuleOutputTargetType.READONLY_RESULT,
                ),
            ),
        )

        "rule_system_remaining_payment" -> RuleRuntimeMetadata(
            ruleId = ruleId,
            outputTargets = listOf(
                RuleOutputTargetDefinition(
                    outputKey = "待付尾款",
                    targetType = RuleOutputTargetType.READONLY_RESULT,
                ),
            ),
        )

        else -> null
    }
}

fun resolveRuleOutputTarget(ruleId: String, outputKey: String): RuleOutputTargetDefinition {
    val normalizedOutputKey = outputKey.trim()
    return ruleRuntimeMetadata(ruleId)
        ?.outputTargets
        ?.firstOrNull { it.outputKey == normalizedOutputKey }
        ?: RuleOutputTargetDefinition(
            outputKey = normalizedOutputKey,
            targetType = RuleOutputTargetType.READONLY_RESULT,
        )
}

fun resolveEffectiveRuleSystemInputs(rule: RuleDefinition): List<RuleSystemInputDefinition> {
    return rule.systemInputs.ifEmpty { ruleRuntimeMetadata(rule.id)?.systemInputs.orEmpty() }
}

fun resolveEffectiveRuleOutputTargets(rule: RuleDefinition): List<RuleOutputTargetDefinition> {
    return rule.outputTargets.ifEmpty { ruleRuntimeMetadata(rule.id)?.outputTargets.orEmpty() }
}

fun resolveEffectiveRuleTriggerModes(rule: RuleDefinition): List<RuleTriggerMode> {
    return rule.triggerModes.ifEmpty { listOf(RuleTriggerMode.ON_VALUE_CHANGED) }
}
