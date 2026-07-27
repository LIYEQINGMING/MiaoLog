package com.example.itemmanagement.data.model.attribute

import com.example.itemmanagement.data.entity.attribute.AttributeDefinitionEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Locale

data class AttributeRuleDependencyHint(
    val triggerAttributeName: String,
    val autoFillInputs: List<String>,
    val readonlyOutputs: List<String>,
)

data class AttributeRuleFieldExpansion(
    val resolvedFieldNames: List<String>,
    val autoAddedFieldNames: List<String>,
    val readonlyOutputs: List<String>,
)

data class AttributeRuleOutputProjection(
    val attributeId: String,
    val attributeName: String,
    val fieldName: String,
    val outputKey: String,
    val targetType: RuleOutputTargetType,
    val targetAttributeId: String? = null,
    val targetAttributeKey: String? = null,
    val targetFieldName: String? = null,
    val systemVariableKey: SystemVariableKey? = null,
)

private data class RuleResolutionNode<T>(
    val id: String,
    val key: String,
    val name: String,
    val fieldName: String,
    val bindings: List<RuleBinding>,
    val source: T,
)

private val ruleResolutionGson = Gson()
private val ruleBindingListType = object : TypeToken<List<RuleBinding>>() {}.type

fun resolveAttributeRuleDependencyHints(
    attribute: AttributeDefinition,
    allAttributes: List<AttributeDefinition>,
    ruleDefinitions: List<RuleDefinition> = emptyList(),
): List<AttributeRuleDependencyHint> {
    val nodes = allAttributes.map { definition ->
        RuleResolutionNode(
            id = definition.id,
            key = definition.key,
            name = definition.name,
            fieldName = definition.name,
            bindings = definition.ruleBindings,
            source = definition,
        )
    }
    val target = nodes.firstOrNull { it.id == attribute.id } ?: return emptyList()
    return buildDependencyHints(target, nodes, ruleDefinitions)
}

fun resolveAttributeRuleDependencyHints(
    attribute: AttributeDefinitionEntity,
    allAttributes: List<AttributeDefinitionEntity>,
    fieldNameProvider: (AttributeDefinitionEntity) -> String,
    ruleDefinitions: List<RuleDefinition> = emptyList(),
): List<AttributeRuleDependencyHint> {
    val nodes = buildEntityRuleResolutionNodes(allAttributes, fieldNameProvider)
    val target = nodes.firstOrNull { it.id == attribute.id } ?: return emptyList()
    return buildDependencyHints(target, nodes, ruleDefinitions)
}

fun expandAttributeFieldSelectionWithRules(
    selectedFieldNames: Collection<String>,
    selectedAttributeIds: Collection<String> = emptyList(),
    allAttributes: List<AttributeDefinitionEntity>,
    fieldNameProvider: (AttributeDefinitionEntity) -> String,
    ruleDefinitions: List<RuleDefinition> = emptyList(),
): AttributeRuleFieldExpansion {
    val nodes = buildEntityRuleResolutionNodes(allAttributes, fieldNameProvider)
    return buildFieldExpansion(selectedFieldNames, selectedAttributeIds, nodes, ruleDefinitions)
}

fun findRequiredBySelectedFieldNames(
    targetFieldName: String,
    targetAttributeId: String? = null,
    selectedFieldNames: Collection<String>,
    selectedAttributeIds: Collection<String> = emptyList(),
    allAttributes: List<AttributeDefinitionEntity>,
    fieldNameProvider: (AttributeDefinitionEntity) -> String,
    ruleDefinitions: List<RuleDefinition> = emptyList(),
): List<String> {
    val nodes = buildEntityRuleResolutionNodes(allAttributes, fieldNameProvider)
    return buildRequiredBySelectedFieldNames(
        targetFieldName = targetFieldName,
        targetAttributeId = targetAttributeId,
        selectedFieldNames = selectedFieldNames,
        selectedAttributeIds = selectedAttributeIds,
        nodes = nodes,
        ruleDefinitions = ruleDefinitions,
    )
}

fun expandAttributeDefinitionsWithRules(
    selectedDefinitions: List<AttributeDefinitionEntity>,
    allDefinitions: List<AttributeDefinitionEntity>,
    fieldNameProvider: (AttributeDefinitionEntity) -> String,
    ruleDefinitions: List<RuleDefinition> = emptyList(),
): List<AttributeDefinitionEntity> {
    val expansion = expandAttributeFieldSelectionWithRules(
        selectedFieldNames = selectedDefinitions.map(fieldNameProvider),
        selectedAttributeIds = selectedDefinitions.map { it.id },
        allAttributes = allDefinitions,
        fieldNameProvider = fieldNameProvider,
        ruleDefinitions = ruleDefinitions,
    )
    val byFieldName = allDefinitions.associateBy(fieldNameProvider)
    return expansion.resolvedFieldNames.mapNotNull(byFieldName::get).distinctBy { it.id }
}

fun resolveRuleOutputProjections(
    selectedFieldNames: Collection<String>,
    selectedAttributeIds: Collection<String> = emptyList(),
    allAttributes: List<AttributeDefinitionEntity>,
    fieldNameProvider: (AttributeDefinitionEntity) -> String,
    ruleDefinitions: List<RuleDefinition> = emptyList(),
): List<AttributeRuleOutputProjection> {
    val nodes = buildEntityRuleResolutionNodes(allAttributes, fieldNameProvider)
    val selectedNodeIds = resolveSelectedNodeIds(selectedFieldNames, selectedAttributeIds, nodes)
    val byAttributeId = allAttributes.associateBy { it.id }
    val byAttributeKey = allAttributes.associateBy { normalizedRuleLookupKey(it.key) }
    return nodes
        .filter { it.id in selectedNodeIds }
        .flatMap { node ->
            node.bindings.flatMap { binding ->
                resolveBindingOutputTargets(binding, ruleDefinitions).mapNotNull { outputTarget ->
                    val normalized = outputTarget.outputKey.trim()
                    if (normalized.isEmpty()) {
                        null
                    } else {
                        val targetDefinition = outputTarget.attributeId
                            ?.let(byAttributeId::get)
                            ?: outputTarget.attributeKey?.let { byAttributeKey[normalizedRuleLookupKey(it)] }
                        AttributeRuleOutputProjection(
                            attributeId = node.id,
                            attributeName = node.name,
                            fieldName = node.fieldName,
                            outputKey = normalized,
                            targetType = outputTarget.targetType,
                            targetAttributeId = targetDefinition?.id ?: outputTarget.attributeId,
                            targetAttributeKey = outputTarget.attributeKey,
                            targetFieldName = targetDefinition?.let(fieldNameProvider),
                            systemVariableKey = outputTarget.variableKey,
                        )
                    }
                }
            }
        }
        .distinctBy { "${it.attributeId}:${it.outputKey}:${it.targetType}:${it.targetAttributeId}:${it.systemVariableKey}" }
}

fun resolveItemRuleOutputProjections(
    bindings: List<RuleBinding>,
    allAttributes: List<AttributeDefinitionEntity>,
    fieldNameProvider: (AttributeDefinitionEntity) -> String,
    ruleDefinitions: List<RuleDefinition> = emptyList(),
): List<AttributeRuleOutputProjection> {
    if (bindings.isEmpty()) {
        return emptyList()
    }
    val byAttributeId = allAttributes.associateBy { it.id }
    val byAttributeKey = allAttributes.associateBy { normalizedRuleLookupKey(it.key) }
    return bindings.flatMap { binding ->
        val triggerAttributeId = binding.entryAttributeId
            ?: binding.slotBindings
                .filterIsInstance<RuleSlotBinding.AttributeInput>()
                .firstOrNull { it.slotKey == binding.entrySlotKey }
                ?.attributeId
        val triggerDefinition = triggerAttributeId?.let(byAttributeId::get)
        resolveBindingOutputTargets(binding, ruleDefinitions).mapNotNull { outputTarget ->
            val normalized = outputTarget.outputKey.trim()
            if (normalized.isEmpty()) {
                null
            } else {
                val targetDefinition = outputTarget.attributeId
                    ?.let(byAttributeId::get)
                    ?: outputTarget.attributeKey?.let { byAttributeKey[normalizedRuleLookupKey(it)] }
                AttributeRuleOutputProjection(
                    attributeId = binding.id,
                    attributeName = triggerDefinition?.name ?: binding.ruleId,
                    fieldName = triggerDefinition?.let(fieldNameProvider).orEmpty(),
                    outputKey = normalized,
                    targetType = outputTarget.targetType,
                    targetAttributeId = targetDefinition?.id ?: outputTarget.attributeId,
                    targetAttributeKey = outputTarget.attributeKey,
                    targetFieldName = targetDefinition?.let(fieldNameProvider),
                    systemVariableKey = outputTarget.variableKey,
                )
            }
        }
    }.distinctBy { "${it.attributeId}:${it.outputKey}:${it.targetType}:${it.targetAttributeId}:${it.systemVariableKey}" }
}

private fun buildEntityRuleResolutionNodes(
    attributes: List<AttributeDefinitionEntity>,
    fieldNameProvider: (AttributeDefinitionEntity) -> String,
): List<RuleResolutionNode<AttributeDefinitionEntity>> {
    return attributes.map { definition ->
        RuleResolutionNode(
            id = definition.id,
            key = definition.key,
            name = definition.name,
            fieldName = fieldNameProvider(definition),
            bindings = parseRuleBindings(definition.ruleBindingsJson),
            source = definition,
        )
    }
}

private fun parseRuleBindings(ruleBindingsJson: String): List<RuleBinding> {
    if (ruleBindingsJson.isBlank()) {
        return emptyList()
    }
    return runCatching {
        ruleResolutionGson.fromJson<List<RuleBinding>>(ruleBindingsJson, ruleBindingListType)
            ?.filter { it.ruleId.isNotBlank() }
            .orEmpty()
    }.getOrElse { emptyList() }
}

private fun <T> buildDependencyHints(
    target: RuleResolutionNode<T>,
    nodes: List<RuleResolutionNode<T>>,
    ruleDefinitions: List<RuleDefinition>,
): List<AttributeRuleDependencyHint> {
    val byId = nodes.associateBy { it.id }
    val byKey = nodes.groupBy { normalizedRuleLookupKey(it.key) }
    val byName = nodes.groupBy { normalizedRuleLookupKey(it.name) }
    return target.bindings.mapNotNull { binding ->
        val autoFillInputs = resolveRequiredDependencies(
            binding = binding,
            triggerNodeId = target.id,
            byId = byId,
            byKey = byKey,
            byName = byName,
            ruleDefinitions = ruleDefinitions,
        )
            .map { it.name }
        val readonlyOutputs = resolveOutputKeys(binding, ruleDefinitions)
            .map(String::trim)
            .filter { it.isNotEmpty() }
            .distinct()
        if (autoFillInputs.isEmpty() && readonlyOutputs.isEmpty()) {
            null
        } else {
            AttributeRuleDependencyHint(
                triggerAttributeName = target.name,
                autoFillInputs = autoFillInputs,
                readonlyOutputs = readonlyOutputs,
            )
        }
    }
}

private fun <T> buildFieldExpansion(
    selectedFieldNames: Collection<String>,
    selectedAttributeIds: Collection<String>,
    nodes: List<RuleResolutionNode<T>>,
    ruleDefinitions: List<RuleDefinition>,
): AttributeRuleFieldExpansion {
    val selected = linkedSetOf<String>()
    selectedFieldNames
        .map(String::trim)
        .filter { it.isNotEmpty() }
        .forEach(selected::add)

    val resolved = LinkedHashSet(selected)
    val autoAdded = linkedSetOf<String>()
    val readonlyOutputs = linkedSetOf<String>()
    val byId = nodes.associateBy { it.id }
    val byKey = nodes.groupBy { normalizedRuleLookupKey(it.key) }
    val byName = nodes.groupBy { normalizedRuleLookupKey(it.name) }
    val pending = ArrayDeque(resolveSelectedNodeIds(selectedFieldNames, selectedAttributeIds, nodes).mapNotNull(byId::get))

    while (pending.isNotEmpty()) {
        val node = pending.removeFirst()
        node.bindings.forEach { binding ->
            resolveBindingOutputTargets(binding, ruleDefinitions)
                .forEach { target ->
                    when (target.targetType) {
                        RuleOutputTargetType.READONLY_RESULT,
                        RuleOutputTargetType.SYSTEM_VARIABLE -> {
                            target.outputKey.trim()
                                .takeIf { it.isNotEmpty() }
                                ?.let(readonlyOutputs::add)
                        }

                        RuleOutputTargetType.ATTRIBUTE_VALUE -> {
                            val outputNode = resolveTargetNode(
                                target = target,
                                triggerNodeId = node.id,
                                byId = byId,
                                byKey = byKey,
                            )
                            outputNode?.fieldName?.let { targetFieldName ->
                                if (resolved.add(targetFieldName)) {
                                    autoAdded.add(targetFieldName)
                                    pending.addLast(outputNode)
                                }
                            }
                        }
                    }
                }

            resolveRequiredDependencies(
                binding = binding,
                triggerNodeId = node.id,
                byId = byId,
                byKey = byKey,
                byName = byName,
                ruleDefinitions = ruleDefinitions,
            ).forEach { dependency ->
                if (resolved.add(dependency.fieldName)) {
                    autoAdded.add(dependency.fieldName)
                    pending.addLast(dependency)
                }
            }
        }
    }

    return AttributeRuleFieldExpansion(
        resolvedFieldNames = resolved.toList(),
        autoAddedFieldNames = autoAdded.toList(),
        readonlyOutputs = readonlyOutputs.toList(),
    )
}

private fun <T> buildRequiredBySelectedFieldNames(
    targetFieldName: String,
    targetAttributeId: String?,
    selectedFieldNames: Collection<String>,
    selectedAttributeIds: Collection<String>,
    nodes: List<RuleResolutionNode<T>>,
    ruleDefinitions: List<RuleDefinition>,
): List<String> {
    val selectedNodeIds = resolveSelectedNodeIds(selectedFieldNames, selectedAttributeIds, nodes)
    val byId = nodes.associateBy { it.id }
    val byKey = nodes.groupBy { normalizedRuleLookupKey(it.key) }
    val byName = nodes.groupBy { normalizedRuleLookupKey(it.name) }
    val requiredBy = linkedSetOf<String>()

    selectedNodeIds.forEach { nodeId ->
        val node = byId[nodeId] ?: return@forEach
        if (node.fieldName == targetFieldName || (targetAttributeId != null && node.id == targetAttributeId)) {
            return@forEach
        }
        val dependsOnTarget = node.bindings.any { binding ->
            resolveRequiredDependencies(
                binding = binding,
                triggerNodeId = node.id,
                byId = byId,
                byKey = byKey,
                byName = byName,
                ruleDefinitions = ruleDefinitions,
            ).any { dependency ->
                dependency.fieldName == targetFieldName ||
                    (targetAttributeId != null && dependency.id == targetAttributeId)
            }
        }
        if (dependsOnTarget) {
            requiredBy.add(node.name)
        }
    }

    return requiredBy.toList()
}

private fun <T> resolveRequiredDependencies(
    binding: RuleBinding,
    triggerNodeId: String,
    byId: Map<String, RuleResolutionNode<T>>,
    byKey: Map<String, List<RuleResolutionNode<T>>>,
    byName: Map<String, List<RuleResolutionNode<T>>>,
    ruleDefinitions: List<RuleDefinition>,
): List<RuleResolutionNode<T>> {
    val resolved = linkedSetOf<RuleResolutionNode<T>>()
    resolveBindingRequiredDependencies(binding, ruleDefinitions).forEach { dependency ->
        val matches = when {
            !dependency.attributeId.isNullOrBlank() -> listOfNotNull(byId[dependency.attributeId])
            !dependency.attributeKey.isNullOrBlank() -> byKey[normalizedRuleLookupKey(dependency.attributeKey)].orEmpty()
            !dependency.attributeName.isNullOrBlank() -> byName[normalizedRuleLookupKey(dependency.attributeName)].orEmpty()
            else -> emptyList()
        }
        matches
            .filter { it.id != triggerNodeId }
            .forEach(resolved::add)
    }
    return resolved.toList()
}

private data class RuleDependencyLookup(
    val attributeId: String? = null,
    val attributeKey: String? = null,
    val attributeName: String? = null,
)

private fun resolveBindingRequiredDependencies(
    binding: RuleBinding,
    ruleDefinitions: List<RuleDefinition>,
): List<RuleDependencyLookup> {
    val bindingDependencies = binding.slotBindings
        .filterIsInstance<RuleSlotBinding.AttributeInput>()
        .filter { it.slotKey != binding.entrySlotKey && it.isRequired }
        .map {
            RuleDependencyLookup(
                attributeId = it.attributeId,
                attributeKey = it.attributeKeySnapshot,
                attributeName = it.attributeNameSnapshot,
            )
        }
        .filter {
            !it.attributeId.isNullOrBlank() ||
                !it.attributeKey.isNullOrBlank() ||
                !it.attributeName.isNullOrBlank()
        }
    if (bindingDependencies.isNotEmpty()) {
        return bindingDependencies
    }
    val rule = ruleDefinitions.firstOrNull { it.id == binding.ruleId || it.key == binding.ruleId }
    return rule?.requiredDependencies
        ?.takeIf { it.isNotEmpty() }
        ?.map { RuleDependencyLookup(attributeName = it) }
        ?: emptyList()
}

private fun resolveOutputKeys(
    binding: RuleBinding,
    ruleDefinitions: List<RuleDefinition>,
): List<String> {
    val bindingOutputs = binding.slotBindings
        .filter {
            it is RuleSlotBinding.AttributeOutput ||
                it is RuleSlotBinding.ReadonlyOutput ||
                it is RuleSlotBinding.SystemOutput
        }
        .map { it.slotKey }
    if (bindingOutputs.isNotEmpty()) {
        return bindingOutputs
    }
    val rule = ruleDefinitions.firstOrNull { it.id == binding.ruleId || it.key == binding.ruleId }
    return rule?.outputKeys?.takeIf { it.isNotEmpty() } ?: emptyList()
}

private fun resolveBindingOutputTargets(
    binding: RuleBinding,
    ruleDefinitions: List<RuleDefinition>,
): List<RuleOutputTargetDefinition> {
    val bindingTargets = binding.slotBindings.mapNotNull { slotBinding ->
        when (slotBinding) {
            is RuleSlotBinding.AttributeOutput -> RuleOutputTargetDefinition(
                outputKey = slotBinding.slotKey,
                targetType = RuleOutputTargetType.ATTRIBUTE_VALUE,
                attributeId = slotBinding.attributeId,
                attributeKey = slotBinding.attributeKeySnapshot,
            )

            is RuleSlotBinding.ReadonlyOutput -> RuleOutputTargetDefinition(
                outputKey = slotBinding.slotKey,
                targetType = RuleOutputTargetType.READONLY_RESULT,
            )

            is RuleSlotBinding.SystemOutput -> RuleOutputTargetDefinition(
                outputKey = slotBinding.slotKey,
                targetType = RuleOutputTargetType.SYSTEM_VARIABLE,
                variableKey = slotBinding.systemVariableKey,
            )

            else -> null
        }
    }
    if (bindingTargets.isNotEmpty()) {
        return bindingTargets
    }
    val rule = ruleDefinitions.firstOrNull { it.id == binding.ruleId || it.key == binding.ruleId }
    return if (rule != null) {
        resolveEffectiveRuleOutputTargets(rule).ifEmpty {
            resolveOutputKeys(binding, ruleDefinitions).map { outputKey ->
                RuleOutputTargetDefinition(
                    outputKey = outputKey.trim(),
                    targetType = RuleOutputTargetType.READONLY_RESULT,
                )
            }
        }
    } else {
        resolveOutputKeys(binding, ruleDefinitions).map { outputKey ->
            resolveRuleOutputTarget(binding.ruleId, outputKey)
        }
    }
}

private fun <T> resolveSelectedNodeIds(
    selectedFieldNames: Collection<String>,
    selectedAttributeIds: Collection<String>,
    nodes: List<RuleResolutionNode<T>>,
): LinkedHashSet<String> {
    val normalizedFieldNames = selectedFieldNames
        .map(String::trim)
        .filter { it.isNotEmpty() }
        .toSet()
    val normalizedAttributeIds = selectedAttributeIds
        .map(String::trim)
        .filter { it.isNotEmpty() }
        .toSet()
    return buildSet {
        nodes.forEach { node ->
            if (node.fieldName in normalizedFieldNames || node.id in normalizedAttributeIds) {
                add(node.id)
            }
        }
    }.toCollection(LinkedHashSet())
}

private fun <T> resolveTargetNode(
    target: RuleOutputTargetDefinition,
    triggerNodeId: String,
    byId: Map<String, RuleResolutionNode<T>>,
    byKey: Map<String, List<RuleResolutionNode<T>>>,
): RuleResolutionNode<T>? {
    val byAttributeId = target.attributeId
        ?.takeIf { it.isNotBlank() }
        ?.let(byId::get)
        ?.takeIf { it.id != triggerNodeId }
    if (byAttributeId != null) {
        return byAttributeId
    }
    return target.attributeKey
        ?.takeIf { it.isNotBlank() }
        ?.let { byKey[normalizedRuleLookupKey(it)].orEmpty() }
        ?.firstOrNull { it.id != triggerNodeId }
}

private fun normalizedRuleLookupKey(value: String): String {
    return value.trim().lowercase(Locale.ROOT)
}
