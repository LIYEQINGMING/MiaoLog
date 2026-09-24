package com.example.itemmanagement.data.entity.unified

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.itemmanagement.data.model.attribute.RuleBindingCreationSource
import com.example.itemmanagement.data.model.attribute.RuleBindingInstance
import com.example.itemmanagement.data.model.attribute.RuleBindingStatus
import com.example.itemmanagement.data.model.attribute.RuleRuntimeState
import com.example.itemmanagement.data.model.attribute.RuleSlotBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(
    tableName = "item_rule_bindings",
    foreignKeys = [
        ForeignKey(
            entity = UnifiedItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("itemId"),
        Index("ruleId"),
        Index(value = ["itemId", "ruleId"]),
    ]
)
data class ItemRuleBindingEntity(
    @PrimaryKey
    val id: String,
    val itemId: Long,
    val ruleId: String,
    val entryAttributeId: String? = null,
    val entrySlotKey: String,
    val slotBindingsJson: String,
    val togglePlacementSlotKey: String? = null,
    val isEnabled: Boolean = true,
    val runtimeStateJson: String? = null,
    val status: RuleBindingStatus = RuleBindingStatus.ACTIVE,
    val creationSource: RuleBindingCreationSource = RuleBindingCreationSource.ATTRIBUTE_MANAGEMENT,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

private val itemRuleBindingGson = Gson()
private val itemRuleSlotBindingListType = object : TypeToken<List<RuleSlotBinding>>() {}.type
private val itemRuleRuntimeStateType = object : TypeToken<RuleRuntimeState>() {}.type

fun ItemRuleBindingEntity.toModel(): RuleBindingInstance {
    val slotBindings = runCatching {
        itemRuleBindingGson.fromJson<List<RuleSlotBinding>>(slotBindingsJson, itemRuleSlotBindingListType)
            ?.filter { it.slotKey.isNotBlank() }
            .orEmpty()
    }.getOrElse { emptyList() }
    val resolvedEntrySlotKey = entrySlotKey
        .takeIf { it.isNotBlank() }
        ?: slotBindings.firstOrNull()?.slotKey
        ?: "entry"
    val runtimeState = runCatching {
        runtimeStateJson
            ?.takeIf { it.isNotBlank() }
            ?.let { itemRuleBindingGson.fromJson<RuleRuntimeState>(it, itemRuleRuntimeStateType) }
    }.getOrNull()

    return RuleBindingInstance(
        id = id,
        ruleId = ruleId,
        entryAttributeId = entryAttributeId,
        entrySlotKey = resolvedEntrySlotKey,
        slotBindings = slotBindings,
        togglePlacementSlotKey = togglePlacementSlotKey,
        isEnabled = isEnabled,
        runtimeState = runtimeState,
        status = status,
        creationSource = creationSource,
        description = description,
    )
}

fun RuleBindingInstance.toItemRuleBindingEntity(
    itemId: Long,
    createdAt: Long = System.currentTimeMillis(),
    updatedAt: Long = System.currentTimeMillis(),
): ItemRuleBindingEntity {
    return ItemRuleBindingEntity(
        id = id,
        itemId = itemId,
        ruleId = ruleId,
        entryAttributeId = entryAttributeId,
        entrySlotKey = entrySlotKey,
        slotBindingsJson = itemRuleBindingGson.toJson(slotBindings),
        togglePlacementSlotKey = togglePlacementSlotKey,
        isEnabled = isEnabled,
        runtimeStateJson = runtimeState?.let(itemRuleBindingGson::toJson),
        status = status,
        creationSource = creationSource,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
