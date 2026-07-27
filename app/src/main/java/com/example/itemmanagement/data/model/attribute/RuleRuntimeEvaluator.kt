package com.example.itemmanagement.data.model.attribute

import com.example.itemmanagement.data.entity.attribute.AttributeDefinitionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

data class RuleRuntimeOutputState(
    val title: String,
    val value: String,
    val placeholder: String,
    val supportText: String? = null,
    val targetType: RuleOutputTargetType = RuleOutputTargetType.READONLY_RESULT,
    val systemVariableKey: SystemVariableKey? = null,
)

data class RuleRuntimeEvaluationContext(
    val triggerDefinition: AttributeDefinitionEntity,
    val rule: RuleDefinition,
    val binding: RuleBinding,
    val outputKey: String,
    val allDefinitions: List<AttributeDefinitionEntity>,
    val fieldNameProvider: (AttributeDefinitionEntity) -> String,
    val fieldValueProvider: (String) -> Any?,
    val systemValueProvider: (SystemVariableKey) -> Any? = ::defaultRuleSystemValue,
)

fun evaluateRuleRuntimeOutput(
    context: RuleRuntimeEvaluationContext,
): RuleRuntimeOutputState {
    val outputTarget = resolveEffectiveRuleOutputTargets(context.rule)
        .firstOrNull { it.outputKey == context.outputKey.trim() }
        ?: resolveRuleOutputTarget(
            ruleId = context.rule.id,
            outputKey = context.outputKey,
        )
    val roleInputs = resolveRuleRoleInputs(context)
    val computed = evaluateExpressionRule(context, roleInputs)
        ?: when {
            isRemainingPaymentRule(context.rule, context.outputKey) -> evaluateRemainingPayment(context, roleInputs)
            isAverageValueRule(context.rule, context.outputKey) -> evaluateAverageValue(context, roleInputs)
            isSubscriptionPaidRule(context.rule, context.outputKey) -> evaluateSubscriptionPaid(context, roleInputs)
            else -> evaluateGenericRule(context, roleInputs)
        }
    return computed.copy(
        targetType = outputTarget.targetType,
        systemVariableKey = outputTarget.variableKey,
    )
}

private data class RuleRoleInput(
    val role: String,
    val sourceLabel: String,
    val fieldName: String? = null,
    val rawValue: Any? = null,
)

private fun resolveRuleRoleInputs(
    context: RuleRuntimeEvaluationContext,
): Map<String, RuleRoleInput> {
    val resolved = linkedMapOf<String, RuleRoleInput>()

    val triggerFieldName = context.fieldNameProvider(context.triggerDefinition)
    resolved[context.binding.entrySlotKey] = RuleRoleInput(
        role = context.binding.entrySlotKey,
        sourceLabel = context.triggerDefinition.name,
        fieldName = triggerFieldName,
        rawValue = context.fieldValueProvider(triggerFieldName),
    )

    context.binding.slotBindings.forEach { slotBinding ->
        when (slotBinding) {
            is RuleSlotBinding.AttributeInput -> {
                if (slotBinding.slotKey == context.binding.entrySlotKey) {
                    return@forEach
                }
                val dependencyDefinition = context.allDefinitions.firstOrNull { definition ->
                    (slotBinding.attributeId != null && definition.id == slotBinding.attributeId) ||
                        (!slotBinding.attributeNameSnapshot.isNullOrBlank() && definition.name == slotBinding.attributeNameSnapshot)
                }
                val fieldName = dependencyDefinition?.let(context.fieldNameProvider)
                    ?: slotBinding.attributeNameSnapshot
                resolved[slotBinding.slotKey] = RuleRoleInput(
                    role = slotBinding.slotKey,
                    sourceLabel = slotBinding.attributeNameSnapshot ?: fieldName ?: slotBinding.slotKey,
                    fieldName = fieldName,
                    rawValue = fieldName?.let(context.fieldValueProvider),
                )
            }

            is RuleSlotBinding.ConfigInput -> {
                resolved[slotBinding.slotKey] = RuleRoleInput(
                    role = slotBinding.slotKey,
                    sourceLabel = "配置输入",
                    rawValue = slotBinding.rawValue,
                )
            }

            is RuleSlotBinding.SystemInput -> {
                resolved[slotBinding.slotKey] = RuleRoleInput(
                    role = slotBinding.slotKey,
                    sourceLabel = slotBinding.systemVariableKey.displayName,
                    rawValue = context.systemValueProvider(slotBinding.systemVariableKey),
                )
            }

            else -> Unit
        }
    }

    context.rule.inputSlots()
        .filter { it.sourceType == RuleSlotSourceType.SYSTEM_INPUT }
        .forEach { slot ->
            val key = slot.systemVariableKey ?: return@forEach
            if (resolved.containsKey(slot.key)) {
                return@forEach
            }
            resolved[slot.key] = RuleRoleInput(
                role = slot.key,
                sourceLabel = key.displayName,
                rawValue = context.systemValueProvider(key),
            )
        }

    context.rule.inputSlots()
        .filter { it.sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT }
        .forEach { slot ->
            if (resolved.containsKey(slot.key)) {
                return@forEach
            }
            val fallbackFieldName = slot.name.takeIf { it.isNotBlank() }
            resolved[slot.key] = RuleRoleInput(
                role = slot.key,
                sourceLabel = slot.name,
                fieldName = fallbackFieldName,
                rawValue = fallbackFieldName?.let(context.fieldValueProvider),
            )
        }

    return resolved
}

private fun numericRoleValues(
    rule: RuleDefinition,
    inputs: Map<String, RuleRoleInput>,
): List<Double> {
    return rule.inputSlots().mapNotNull { slot -> numericValue(inputs[slot.key]?.rawValue) }
}

private fun dateRoleValue(
    inputs: Map<String, RuleRoleInput>,
    role: String,
): Date? {
    return dateValue(inputs[role]?.rawValue)
}

private fun numberRoleValue(
    inputs: Map<String, RuleRoleInput>,
    role: String,
): Double? {
    return numericValue(inputs[role]?.rawValue)
}

private fun roleInput(
    inputs: Map<String, RuleRoleInput>,
    role: String,
): RuleRoleInput? {
    return inputs[role]
}

private fun legacySubscriptionPriceInput(inputs: Map<String, RuleRoleInput>): RuleRoleInput? {
    return inputs["subscriptionPrice"] ?: inputs["amount"]
}

private fun paymentDatesInput(inputs: Map<String, RuleRoleInput>): RuleRoleInput? {
    return inputs["paymentDates"]
}

private fun amountInput(inputs: Map<String, RuleRoleInput>): RuleRoleInput? {
    return inputs["amount"]
}

private fun totalPriceInput(inputs: Map<String, RuleRoleInput>): RuleRoleInput? {
    return inputs["totalPrice"]
}

private fun depositInput(inputs: Map<String, RuleRoleInput>): RuleRoleInput? {
    return inputs["deposit"]
}

private fun currentDateInput(inputs: Map<String, RuleRoleInput>): RuleRoleInput? {
    return inputs["currentDate"]
}

private fun endDateInput(inputs: Map<String, RuleRoleInput>): RuleRoleInput? {
    return inputs["endDate"]
}

private fun startDateInput(inputs: Map<String, RuleRoleInput>): RuleRoleInput? {
    return inputs["startDate"]
}

private fun remainingPaymentSupportText(
    context: RuleRuntimeEvaluationContext,
    inputs: Map<String, RuleRoleInput>,
): String {
    return buildBinarySupportText(
        left = totalPriceInput(inputs)?.sourceLabel ?: "总价",
        right = depositInput(inputs)?.sourceLabel ?: context.triggerDefinition.name,
    )
}

private fun inferRemainingPaymentUnit(
    context: RuleRuntimeEvaluationContext,
    inputs: Map<String, RuleRoleInput>,
): String? {
    return inferUnit(
        primaryFieldName = totalPriceInput(inputs)?.fieldName,
        fallbackFieldName = depositInput(inputs)?.fieldName,
        fieldValueProvider = context.fieldValueProvider,
    )
}

private fun inferAverageValueUnit(
    context: RuleRuntimeEvaluationContext,
    amountInput: RuleRoleInput?,
): String? {
    return inferUnit(
        primaryFieldName = amountInput?.fieldName,
        fallbackFieldName = amountInput?.fieldName,
        fieldValueProvider = context.fieldValueProvider,
    )
}

private fun inferSubscriptionUnit(
    context: RuleRuntimeEvaluationContext,
    priceInput: RuleRoleInput?,
): String? {
    return inferUnit(
        primaryFieldName = priceInput?.fieldName,
        fallbackFieldName = priceInput?.fieldName,
        fieldValueProvider = context.fieldValueProvider,
    )
}

private fun roleSupportText(
    context: RuleRuntimeEvaluationContext,
    inputs: Map<String, RuleRoleInput>,
): String {
    return inputs.values.joinToString(" / ") { "${it.role} <- ${it.sourceLabel}" }
        .ifBlank { context.rule.name }
}

private fun resolveRuleRoleInputsLegacyFallback(
    context: RuleRuntimeEvaluationContext,
): Map<String, RuleRoleInput> {
    val systemInputs = resolveEffectiveRuleSystemInputs(context.rule).associateBy { it.role }
    val resolved = linkedMapOf<String, RuleRoleInput>()

    val triggerFieldName = context.fieldNameProvider(context.triggerDefinition)
    resolved[context.binding.inputRole] = RuleRoleInput(
        role = context.binding.inputRole,
        sourceLabel = context.triggerDefinition.name,
        fieldName = triggerFieldName,
        rawValue = context.fieldValueProvider(triggerFieldName),
    )

    systemInputs.values.forEach { input ->
        val key = input.variableKey ?: return@forEach
        resolved[input.role] = RuleRoleInput(
            role = input.role,
            sourceLabel = key.displayName,
            rawValue = context.systemValueProvider(key),
        )
    }

    val remainingRoles = context.rule.inputRoles.filterNot { resolved.containsKey(it) }
    val dependencyQueue = context.binding.requiredDependencies + context.binding.optionalDependencies
    remainingRoles.zip(dependencyQueue).forEach { (role, dependencyName) ->
        val dependencyDefinition = context.allDefinitions.firstOrNull { it.name == dependencyName }
        val fieldName = dependencyDefinition?.let(context.fieldNameProvider) ?: dependencyName
        resolved[role] = RuleRoleInput(
            role = role,
            sourceLabel = dependencyName,
            fieldName = fieldName,
            rawValue = context.fieldValueProvider(fieldName),
        )
    }

    return resolved
}

private fun evaluateRemainingPayment(
    context: RuleRuntimeEvaluationContext,
    inputs: Map<String, RuleRoleInput>,
): RuleRuntimeOutputState {
    val total = numericValue(inputs["totalPrice"]?.rawValue)
    val deposit = numericValue(inputs["deposit"]?.rawValue)
    val supportText = buildBinarySupportText(
        left = inputs["totalPrice"]?.sourceLabel ?: "总价",
        right = inputs["deposit"]?.sourceLabel ?: context.triggerDefinition.name,
    )
    val unit = inferUnit(
        primaryFieldName = inputs["totalPrice"]?.fieldName,
        fallbackFieldName = inputs["deposit"]?.fieldName,
        fieldValueProvider = context.fieldValueProvider,
    )
    return if (total != null && deposit != null) {
        RuleRuntimeOutputState(
            title = context.outputKey,
            value = formatNumberWithOptionalUnit(total - deposit, unit),
            placeholder = "待补齐总价与定金",
            supportText = supportText,
        )
    } else {
        RuleRuntimeOutputState(
            title = context.outputKey,
            value = "",
            placeholder = "待补齐总价与定金后生成",
            supportText = supportText,
        )
    }
}

private fun evaluateAverageValue(
    context: RuleRuntimeEvaluationContext,
    inputs: Map<String, RuleRoleInput>,
): RuleRuntimeOutputState {
    val amountInput = inputs["amount"]
    val amount = numericValue(amountInput?.rawValue)
    val startDateInput = inputs["startDate"]
    val endDate = dateValue(inputs["endDate"]?.rawValue)
        ?: dateValue(inputs["currentDate"]?.rawValue)
        ?: Date()
    val startDate = dateValue(startDateInput?.rawValue)
    val supportText = "${amountInput?.sourceLabel ?: context.triggerDefinition.name} / 时间跨度"
    val unit = inferUnit(
        primaryFieldName = amountInput?.fieldName,
        fallbackFieldName = amountInput?.fieldName,
        fieldValueProvider = context.fieldValueProvider,
    )
    val daySpan = if (startDate != null) {
        max(1L, abs(endDate.time - startDate.time) / (24L * 60L * 60L * 1000L))
    } else {
        null
    }
    return if (amount != null && daySpan != null) {
        RuleRuntimeOutputState(
            title = context.outputKey,
            value = buildString {
                append(formatNumberWithOptionalUnit(amount / daySpan.toDouble(), unit))
                append("/天")
            },
            placeholder = "待补齐价格与起始日期",
            supportText = supportText,
        )
    } else {
        RuleRuntimeOutputState(
            title = context.outputKey,
            value = "",
            placeholder = "待补齐价格与起始日期后生成",
            supportText = supportText,
        )
    }
}

private fun evaluateSubscriptionPaid(
    context: RuleRuntimeEvaluationContext,
    inputs: Map<String, RuleRoleInput>,
): RuleRuntimeOutputState {
    val priceInput = inputs["subscriptionPrice"] ?: inputs["amount"]
    val price = numericValue(priceInput?.rawValue)
    val paymentCount = paymentCount(inputs["paymentDates"]?.rawValue)
    val unit = inferUnit(
        primaryFieldName = priceInput?.fieldName,
        fallbackFieldName = priceInput?.fieldName,
        fieldValueProvider = context.fieldValueProvider,
    )
    val supportText = "${priceInput?.sourceLabel ?: context.triggerDefinition.name} * 支付次数"
    return when (context.outputKey.trim()) {
        "已支付次数" -> {
            if (paymentCount != null) {
                RuleRuntimeOutputState(
                    title = context.outputKey,
                    value = paymentCount.toString(),
                    placeholder = "待补齐支付时间",
                    supportText = supportText,
                )
            } else {
                RuleRuntimeOutputState(
                    title = context.outputKey,
                    value = "",
                    placeholder = "待补齐支付时间后生成",
                    supportText = supportText,
                )
            }
        }

        else -> {
            if (price != null && paymentCount != null) {
                RuleRuntimeOutputState(
                    title = context.outputKey,
                    value = formatNumberWithOptionalUnit(price * paymentCount, unit),
                    placeholder = "待补齐订阅价格与支付时间",
                    supportText = supportText,
                )
            } else {
                RuleRuntimeOutputState(
                    title = context.outputKey,
                    value = "",
                    placeholder = "待补齐订阅价格与支付时间后生成",
                    supportText = supportText,
                )
            }
        }
    }
}

private fun evaluateGenericRule(
    context: RuleRuntimeEvaluationContext,
    inputs: Map<String, RuleRoleInput>,
): RuleRuntimeOutputState {
    val numericInputs = numericRoleValues(context.rule, inputs)
    val fallback = when (context.rule.computationType) {
        RuleComputationType.SUM,
        RuleComputationType.ACCUMULATION -> numericInputs.takeIf { it.isNotEmpty() }?.sum()

        RuleComputationType.DIFFERENCE -> if (numericInputs.size >= 2) numericInputs[0] - numericInputs[1] else null

        RuleComputationType.AVERAGE -> if (numericInputs.isNotEmpty()) numericInputs.average() else null

        RuleComputationType.CYCLE,
        RuleComputationType.CUSTOM -> null
    }
    val supportText = inputs.values.joinToString(" / ") { "${it.role} <- ${it.sourceLabel}" }
        .ifBlank { context.rule.name }
    return if (fallback != null) {
        RuleRuntimeOutputState(
            title = context.outputKey,
            value = formatNumberWithOptionalUnit(fallback, null),
            placeholder = "待补齐规则输入",
            supportText = supportText,
        )
    } else {
        RuleRuntimeOutputState(
            title = context.outputKey,
            value = "",
            placeholder = "当前版本暂未计算该规则输出",
            supportText = supportText,
        )
    }
}

private fun evaluateExpressionRule(
    context: RuleRuntimeEvaluationContext,
    inputs: Map<String, RuleRoleInput>,
): RuleRuntimeOutputState? {
    val expression = context.rule.expressionDefinition?.expression?.trim().orEmpty()
    if (expression.isBlank() || context.rule.outputSlots().size != 1) {
        return null
    }
    val outputSlot = context.rule.outputSlots().firstOrNull { it.key == context.outputKey }
        ?: context.rule.outputSlots().firstOrNull()
        ?: return null
    val result = runCatching {
        evaluateRuleExpression(
            expression = expression,
            variables = buildExpressionVariables(context, inputs),
        )
    }.getOrNull() ?: return null
    val unit = inferExpressionOutputUnit(context, inputs)
    return RuleRuntimeOutputState(
        title = context.outputKey,
        value = formatExpressionValue(result, outputSlot.valueType, unit),
        placeholder = "待补齐规则输入",
        supportText = roleSupportText(context, inputs),
    )
}

private fun buildExpressionVariables(
    context: RuleRuntimeEvaluationContext,
    inputs: Map<String, RuleRoleInput>,
): Map<String, Any?> {
    val variables = linkedMapOf<String, Any?>()
    inputs.forEach { (role, input) ->
        variables[role] = input.rawValue
    }
    variables["outputKey"] = context.outputKey
    variables["ruleName"] = context.rule.name
    variables["now"] = Date()
    return variables
}

private fun inferExpressionOutputUnit(
    context: RuleRuntimeEvaluationContext,
    inputs: Map<String, RuleRoleInput>,
): String? {
    val numericFieldNames = context.rule.inputSlots()
        .mapNotNull { slot ->
            val input = inputs[slot.key] ?: return@mapNotNull null
            input.fieldName?.takeIf { numericValue(input.rawValue) != null }
        }
    return numericFieldNames.firstNotNullOfOrNull { fieldName ->
        stringValue(context.fieldValueProvider("${fieldName}_unit")).takeIf { it.isNotBlank() }
    } ?: stringValue(context.fieldValueProvider("币种")).takeIf { it.isNotBlank() }
}

private fun formatExpressionValue(
    value: Any?,
    valueType: RuleSlotValueType,
    unit: String?,
): String {
    return when (valueType) {
        RuleSlotValueType.NUMBER -> numericValue(value)?.let { formatNumberWithOptionalUnit(it, unit) }
            ?: stringValue(value)
        RuleSlotValueType.BOOLEAN -> if (booleanValue(value)) "true" else "false"
        RuleSlotValueType.DATE,
        RuleSlotValueType.SYSTEM_DATE_TIME -> formatDateForExpression(dateValue(value) ?: return stringValue(value))
        else -> stringValue(value)
    }
}

private fun evaluateRuleExpression(
    expression: String,
    variables: Map<String, Any?>,
): Any? {
    val parser = RuleExpressionParser(expression, variables)
    val value = parser.parseExpression()
    parser.ensureFullyConsumed()
    return value
}

private class RuleExpressionParser(
    private val source: String,
    private val variables: Map<String, Any?>,
) {
    private val tokens: List<RuleExpressionToken> = RuleExpressionTokenizer(source).tokenize()
    private var index: Int = 0

    fun parseExpression(): Any? = parseLogicalOr()

    fun ensureFullyConsumed() {
        if (!peek().isEnd) {
            error("Unexpected token: ${peek().text}")
        }
    }

    private fun parseLogicalOr(): Any? {
        var left = parseLogicalAnd()
        while (match("||")) {
            left = booleanValue(left) || booleanValue(parseLogicalAnd())
        }
        return left
    }

    private fun parseLogicalAnd(): Any? {
        var left = parseEquality()
        while (match("&&")) {
            left = booleanValue(left) && booleanValue(parseEquality())
        }
        return left
    }

    private fun parseEquality(): Any? {
        var left = parseComparison()
        while (true) {
            left = when {
                match("==") -> equalsValue(left, parseComparison())
                match("!=") -> !equalsValue(left, parseComparison())
                else -> return left
            }
        }
    }

    private fun parseComparison(): Any? {
        var left = parseAddition()
        while (true) {
            left = when {
                match(">=") -> compareValues(left, parseAddition()) >= 0
                match("<=") -> compareValues(left, parseAddition()) <= 0
                match(">") -> compareValues(left, parseAddition()) > 0
                match("<") -> compareValues(left, parseAddition()) < 0
                else -> return left
            }
        }
    }

    private fun parseAddition(): Any? {
        var left = parseMultiplication()
        while (true) {
            left = when {
                match("+") -> plusValues(left, parseMultiplication())
                match("-") -> (numericValue(left) ?: 0.0) - (numericValue(parseMultiplication()) ?: 0.0)
                else -> return left
            }
        }
    }

    private fun parseMultiplication(): Any? {
        var left = parseUnary()
        while (true) {
            left = when {
                match("*") -> (numericValue(left) ?: 0.0) * (numericValue(parseUnary()) ?: 0.0)
                match("/") -> {
                    val divisor = numericValue(parseUnary()) ?: 0.0
                    if (divisor == 0.0) null else (numericValue(left) ?: 0.0) / divisor
                }
                match("%") -> {
                    val divisor = numericValue(parseUnary()) ?: 0.0
                    if (divisor == 0.0) null else (numericValue(left) ?: 0.0) % divisor
                }
                else -> return left
            }
        }
    }

    private fun parseUnary(): Any? {
        return when {
            match("!") -> !booleanValue(parseUnary())
            match("-") -> -(numericValue(parseUnary()) ?: 0.0)
            else -> parsePrimary()
        }
    }

    private fun parsePrimary(): Any? {
        val token = peek()
        return when {
            match("(") -> {
                val value = parseExpression()
                consume(")")
                value
            }
            token.type == RuleExpressionTokenType.NUMBER -> {
                advance().text.toDoubleOrNull()
            }
            token.type == RuleExpressionTokenType.STRING -> {
                advance().literal
            }
            token.type == RuleExpressionTokenType.IDENTIFIER -> parseIdentifier()
            token.isEnd -> null
            else -> error("Unexpected token: ${token.text}")
        }
    }

    private fun parseIdentifier(): Any? {
        val identifier = advance().text
        val normalized = identifier.lowercase(Locale.ROOT)
        if (match("(")) {
            val arguments = mutableListOf<Any?>()
            if (!check(")")) {
                do {
                    arguments += parseExpression()
                } while (match(","))
            }
            consume(")")
            return invokeFunction(identifier, arguments)
        }
        return when (normalized) {
            "true" -> true
            "false" -> false
            "null" -> null
            else -> variables[identifier]
        }
    }

    private fun invokeFunction(
        name: String,
        arguments: List<Any?>,
    ): Any? {
        return when (name.lowercase(Locale.ROOT)) {
            "if" -> {
                val condition = booleanValue(arguments.getOrNull(0))
                if (condition) arguments.getOrNull(1) else arguments.getOrNull(2)
            }
            "coalesce" -> arguments.firstOrNull { !isNullLike(it) }
            "datediff" -> dateDiff(arguments)
            "count", "paymentcount" -> paymentCount(arguments.getOrNull(0))
            "hasvalue" -> !isNullLike(arguments.getOrNull(0))
            "abs" -> numericValue(arguments.getOrNull(0))?.let(::abs)
            "max" -> arguments.mapNotNull(::numericValue).maxOrNull()
            "min" -> arguments.mapNotNull(::numericValue).minOrNull()
            "round" -> numericValue(arguments.getOrNull(0))?.let { kotlin.math.round(it).toLong().toDouble() }
            "floor" -> numericValue(arguments.getOrNull(0))?.let(::floor)
            "ceil" -> numericValue(arguments.getOrNull(0))?.let(::ceil)
            "cyclecount" -> cycleCount(arguments)
            else -> null
        }
    }

    private fun dateDiff(arguments: List<Any?>): Any? {
        val start = dateValue(arguments.getOrNull(0)) ?: return null
        val end = dateValue(arguments.getOrNull(1)) ?: return null
        val unit = stringValue(arguments.getOrNull(2)).ifBlank { "DAY" }.uppercase(Locale.ROOT)
        val millis = abs(end.time - start.time)
        val days = max(1L, millis / (24L * 60L * 60L * 1000L))
        return when (unit) {
            "DAY", "DAYS" -> days.toDouble()
            "WEEK", "WEEKS" -> max(1L, days / 7L).toDouble()
            "MONTH", "MONTHS" -> max(1L, days / 30L).toDouble()
            "QUARTER", "QUARTERS" -> max(1L, days / 90L).toDouble()
            "YEAR", "YEARS" -> max(1L, days / 365L).toDouble()
            else -> days.toDouble()
        }
    }

    private fun cycleCount(arguments: List<Any?>): Any? {
        val start = dateValue(arguments.getOrNull(0)) ?: return null
        val end = dateValue(arguments.getOrNull(1))
        val current = dateValue(arguments.getOrNull(2)) ?: return null
        val unit = stringValue(arguments.getOrNull(3)).ifBlank { "MONTH" }.uppercase(Locale.ROOT)
        val effectiveEnd = listOfNotNull(end, current).minByOrNull { it.time } ?: current
        if (effectiveEnd.before(start)) {
            return 0.0
        }
        val daySpan = max(0L, abs(effectiveEnd.time - start.time) / (24L * 60L * 60L * 1000L))
        val cycles = when (unit) {
            "DAY", "DAYS" -> daySpan + 1
            "WEEK", "WEEKS" -> daySpan / 7L + 1
            "MONTH", "MONTHS" -> daySpan / 30L + 1
            "QUARTER", "QUARTERS" -> daySpan / 90L + 1
            "YEAR", "YEARS" -> daySpan / 365L + 1
            else -> daySpan + 1
        }
        return cycles.toDouble()
    }

    private fun plusValues(
        left: Any?,
        right: Any?,
    ): Any? {
        val leftNumber = numericValue(left)
        val rightNumber = numericValue(right)
        return if (leftNumber != null && rightNumber != null) {
            leftNumber + rightNumber
        } else {
            stringValue(left) + stringValue(right)
        }
    }

    private fun compareValues(
        left: Any?,
        right: Any?,
    ): Int {
        val leftNumber = numericValue(left)
        val rightNumber = numericValue(right)
        if (leftNumber != null && rightNumber != null) {
            return leftNumber.compareTo(rightNumber)
        }
        val leftDate = dateValue(left)
        val rightDate = dateValue(right)
        if (leftDate != null && rightDate != null) {
            return leftDate.compareTo(rightDate)
        }
        return stringValue(left).compareTo(stringValue(right))
    }

    private fun equalsValue(
        left: Any?,
        right: Any?,
    ): Boolean {
        val leftNumber = numericValue(left)
        val rightNumber = numericValue(right)
        if (leftNumber != null && rightNumber != null) {
            return leftNumber == rightNumber
        }
        return stringValue(left) == stringValue(right)
    }

    private fun match(expected: String): Boolean {
        if (!check(expected)) {
            return false
        }
        advance()
        return true
    }

    private fun consume(expected: String) {
        if (!match(expected)) {
            error("Expected token: $expected")
        }
    }

    private fun check(expected: String): Boolean {
        return peek().text == expected
    }

    private fun advance(): RuleExpressionToken {
        return tokens[index++]
    }

    private fun peek(): RuleExpressionToken {
        return tokens.getOrElse(index) { RuleExpressionToken.end() }
    }
}

private data class RuleExpressionToken(
    val type: RuleExpressionTokenType,
    val text: String,
    val literal: String? = null,
) {
    val isEnd: Boolean
        get() = type == RuleExpressionTokenType.END

    companion object {
        fun end(): RuleExpressionToken = RuleExpressionToken(RuleExpressionTokenType.END, "")
    }
}

private enum class RuleExpressionTokenType {
    NUMBER,
    STRING,
    IDENTIFIER,
    SYMBOL,
    END,
}

private class RuleExpressionTokenizer(
    private val source: String,
) {
    private var index: Int = 0

    fun tokenize(): List<RuleExpressionToken> {
        val tokens = mutableListOf<RuleExpressionToken>()
        while (index < source.length) {
            val ch = source[index]
            when {
                ch.isWhitespace() -> index++
                ch.isDigit() -> tokens += readNumber()
                ch == '"' || ch == '\'' -> tokens += readString(ch)
                ch.isLetter() || ch == '_' -> tokens += readIdentifier()
                else -> tokens += readSymbol()
            }
        }
        tokens += RuleExpressionToken.end()
        return tokens
    }

    private fun readNumber(): RuleExpressionToken {
        val start = index
        while (index < source.length && (source[index].isDigit() || source[index] == '.')) {
            index++
        }
        return RuleExpressionToken(
            type = RuleExpressionTokenType.NUMBER,
            text = source.substring(start, index),
        )
    }

    private fun readString(quote: Char): RuleExpressionToken {
        index++
        val value = buildString {
            while (index < source.length && source[index] != quote) {
                append(source[index])
                index++
            }
        }
        if (index < source.length && source[index] == quote) {
            index++
        }
        return RuleExpressionToken(
            type = RuleExpressionTokenType.STRING,
            text = value,
            literal = value,
        )
    }

    private fun readIdentifier(): RuleExpressionToken {
        val start = index
        while (index < source.length && (source[index].isLetterOrDigit() || source[index] == '_')) {
            index++
        }
        return RuleExpressionToken(
            type = RuleExpressionTokenType.IDENTIFIER,
            text = source.substring(start, index),
        )
    }

    private fun readSymbol(): RuleExpressionToken {
        val twoChar = source.substring(index, minOf(index + 2, source.length))
        val symbol = when (twoChar) {
            ">=", "<=", "==", "!=", "&&", "||" -> {
                index += 2
                twoChar
            }
            else -> {
                val single = source[index].toString()
                index++
                single
            }
        }
        return RuleExpressionToken(
            type = RuleExpressionTokenType.SYMBOL,
            text = symbol,
        )
    }
}

private fun booleanValue(value: Any?): Boolean {
    return when (value) {
        is Boolean -> value
        is Number -> value.toDouble() != 0.0
        is String -> when (value.trim().lowercase(Locale.ROOT)) {
            "", "0", "false", "null" -> false
            else -> true
        }
        else -> value != null
    }
}

private fun isNullLike(value: Any?): Boolean {
    return when (value) {
        null -> true
        is String -> value.isBlank() || value.equals("null", ignoreCase = true)
        else -> false
    }
}

private fun formatDateForExpression(value: Date): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(value)
}

private fun isRemainingPaymentRule(rule: RuleDefinition, outputKey: String): Boolean {
    val normalized = normalizeRuleToken(rule.id) + normalizeRuleToken(rule.key)
    return outputKey.trim() == "待付尾款" || normalized.contains("remaining_payment")
}

private fun isAverageValueRule(rule: RuleDefinition, outputKey: String): Boolean {
    val normalized = normalizeRuleToken(rule.id) + normalizeRuleToken(rule.key)
    return outputKey.trim() == "平均价值" || normalized.contains("average_value")
}

private fun isSubscriptionPaidRule(rule: RuleDefinition, outputKey: String): Boolean {
    val normalized = normalizeRuleToken(rule.id) + normalizeRuleToken(rule.key)
    return outputKey.trim() == "已支付金额" ||
        outputKey.trim() == "已支付次数" ||
        normalized.contains("subscription_paid")
}

private fun normalizeRuleToken(value: String): String {
    return value.trim().lowercase(Locale.ROOT)
}

private fun buildBinarySupportText(left: String, right: String): String {
    return "$left - $right"
}

private fun inferUnit(
    primaryFieldName: String?,
    fallbackFieldName: String?,
    fieldValueProvider: (String) -> Any?,
): String? {
    return listOf(primaryFieldName, fallbackFieldName)
        .filterNotNull()
        .firstNotNullOfOrNull { fieldName ->
            stringValue(fieldValueProvider("${fieldName}_unit")).takeIf { it.isNotBlank() }
        }
        ?: stringValue(fieldValueProvider("币种")).takeIf { it.isNotBlank() }
}

private fun defaultRuleSystemValue(key: SystemVariableKey): Any? {
    return when (key) {
        SystemVariableKey.CURRENT_DATE -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        SystemVariableKey.CURRENT_TIME -> SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        SystemVariableKey.CURRENT_CALENDAR_TIME -> Date()
        SystemVariableKey.CURRENT_TIMEZONE -> java.util.TimeZone.getDefault().id
        else -> null
    }
}

private fun paymentCount(value: Any?): Double? {
    return when (value) {
        is Collection<*> -> value.filterNotNull().size.toDouble()
        is Array<*> -> value.filterNotNull().size.toDouble()
        is String -> value.split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .takeIf { it.isNotEmpty() }
            ?.size
            ?.toDouble()

        null -> null
        else -> 1.0
    }
}

private fun numericValue(value: Any?): Double? {
    return when (value) {
        is Number -> value.toDouble()
        is String -> value.trim().toDoubleOrNull()
        is Pair<*, *> -> value.first?.toString()?.trim()?.toDoubleOrNull()
        else -> null
    }
}

private fun dateValue(value: Any?): Date? {
    val raw = when (value) {
        is Date -> return value
        is String -> value.trim()
        is Pair<*, *> -> value.first?.toString().orEmpty().trim()
        else -> return null
    }
    if (raw.isBlank()) {
        return null
    }
    return runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(raw)
    }.getOrNull()
}

private fun stringValue(value: Any?): String {
    return when (value) {
        null -> ""
        is String -> value
        is Pair<*, *> -> value.first?.toString().orEmpty()
        else -> value.toString()
    }
}

private fun formatNumberWithOptionalUnit(value: Double, unit: String?): String {
    val formatted = if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format(Locale.getDefault(), "%.2f", value)
    }
    return unit?.takeIf { it.isNotBlank() }?.let { "$formatted $it" } ?: formatted
}
