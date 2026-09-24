package com.example.itemmanagement.ui.attribute

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.itemmanagement.data.model.attribute.RuleSlotDirection
import com.example.itemmanagement.data.model.attribute.RuleSlotSourceType
import com.example.itemmanagement.ui.attribute.model.RuleSlotDraftUiModel
import com.example.itemmanagement.ui.attribute.model.SystemVariableOptionUiModel

/**
 * 操作数配置弹窗。底层继续复用规则槽位草稿，避免破坏已有规则数据。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperandConfigDialog(
    slot: RuleSlotDraftUiModel,
    systemVariableOptions: List<SystemVariableOptionUiModel>,
    onDismiss: () -> Unit,
    onSave: (RuleSlotDraftUiModel) -> Unit,
    onDelete: () -> Unit,
) {
    var variableName by remember(slot.id, slot.key, slot.name) {
        mutableStateOf(slot.key.ifBlank { slot.name })
    }
    var direction by remember { mutableStateOf(slot.direction) }
    var valueType by remember { mutableStateOf(slot.valueType) }
    var sourceType by remember { mutableStateOf(slot.sourceType) }
    var systemVariableKey by remember { mutableStateOf(slot.systemVariableKey) }
    var configValue by remember { mutableStateOf(slot.configValue) }
    var description by remember { mutableStateOf(slot.description) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "操作数配置",
                    style = MaterialTheme.typography.headlineSmall,
                )

                OutlinedTextField(
                    value = variableName,
                    onValueChange = { variableName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("操作数名称") },
                    supportingText = {
                        Text(
                            if (isValidRuleVariableName(variableName)) {
                                "画布节点和公式编码使用同一名称，支持中英文、数字和下划线"
                            } else {
                                "名称需以中英文或下划线开头，且不能包含空格或符号"
                            }
                        )
                    },
                    isError = variableName.isNotBlank() && !isValidRuleVariableName(variableName),
                    singleLine = true,
                )

                Text(
                    text = "节点方向",
                    style = MaterialTheme.typography.labelLarge,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = direction == RuleSlotDirection.INPUT,
                        onClick = {
                            direction = RuleSlotDirection.INPUT
                            if (sourceType == RuleSlotSourceType.ATTRIBUTE_OUTPUT ||
                                sourceType == RuleSlotSourceType.READONLY_OUTPUT ||
                                sourceType == RuleSlotSourceType.SYSTEM_OUTPUT
                            ) {
                                sourceType = RuleSlotSourceType.ATTRIBUTE_INPUT
                            }
                        },
                        label = { Text("输入") },
                    )
                    FilterChip(
                        selected = direction == RuleSlotDirection.OUTPUT,
                        onClick = {
                            direction = RuleSlotDirection.OUTPUT
                            sourceType = when (sourceType) {
                                RuleSlotSourceType.SYSTEM_INPUT -> RuleSlotSourceType.SYSTEM_OUTPUT
                                else -> RuleSlotSourceType.ATTRIBUTE_OUTPUT
                            }
                        },
                        label = { Text("输出") },
                    )
                }

                Text(
                    text = "值来源",
                    style = MaterialTheme.typography.labelLarge,
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = sourceType == RuleSlotSourceType.ATTRIBUTE_INPUT || sourceType == RuleSlotSourceType.ATTRIBUTE_OUTPUT,
                        onClick = {
                            sourceType = if (direction == RuleSlotDirection.INPUT) {
                                RuleSlotSourceType.ATTRIBUTE_INPUT
                            } else {
                                RuleSlotSourceType.ATTRIBUTE_OUTPUT
                            }
                        },
                        label = { Text("物品属性") },
                    )
                    FilterChip(
                        selected = sourceType == RuleSlotSourceType.SYSTEM_INPUT || sourceType == RuleSlotSourceType.SYSTEM_OUTPUT,
                        onClick = {
                            sourceType = if (direction == RuleSlotDirection.INPUT) {
                                RuleSlotSourceType.SYSTEM_INPUT
                            } else {
                                RuleSlotSourceType.SYSTEM_OUTPUT
                            }
                        },
                        label = { Text("系统变量") },
                    )
                    FilterChip(
                        selected = sourceType == RuleSlotSourceType.CONFIG_INPUT,
                        onClick = { sourceType = RuleSlotSourceType.CONFIG_INPUT },
                        enabled = direction == RuleSlotDirection.INPUT,
                        label = { Text("固定值") },
                    )
                }

                if (sourceType == RuleSlotSourceType.SYSTEM_INPUT || sourceType == RuleSlotSourceType.SYSTEM_OUTPUT) {
                    Text(
                        text = "系统变量",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    systemVariableOptions.forEach { option ->
                        FilterChip(
                            selected = systemVariableKey == option.key,
                            onClick = { systemVariableKey = option.key },
                            label = { Text(option.displayName) },
                        )
                    }
                }

                if (sourceType == RuleSlotSourceType.CONFIG_INPUT) {
                    OutlinedTextField(
                        value = configValue,
                        onValueChange = { configValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("固定值") },
                        singleLine = true,
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("说明") },
                    minLines = 2,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("删除")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Button(
                        enabled = variableName.isNotBlank() && isValidRuleVariableName(variableName),
                        onClick = {
                            val canonicalName = variableName.trim()
                            onSave(
                                slot.copy(
                                    key = canonicalName,
                                    name = canonicalName,
                                    direction = direction,
                                    valueType = valueType,
                                    sourceType = sourceType,
                                    systemVariableKey = systemVariableKey,
                                    configValue = configValue,
                                    description = description,
                                )
                            )
                        },
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}

private fun isValidRuleVariableName(value: String): Boolean {
    val candidate = value.trim()
    if (candidate.isEmpty()) return false
    if (!candidate.first().isLetter() && candidate.first() != '_') return false
    return candidate.all { it.isLetterOrDigit() || it == '_' }
}
