$path = 'app/src/main/java/com/example/itemmanagement/ui/edit/EditItemViewModel.kt'
$lines = Get-Content -Encoding UTF8 $path

$head = $lines[0..713]
$tail = $lines[785..($lines.Length - 1)]

$midBlock = @'
    private fun parseDate(dateStr: String?): Date? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
        } catch (_: Exception) {
            null
        }
    }

    private fun getBooleanFieldValue(fieldName: String, defaultValue: Boolean = false): Boolean {
        return when (val value = fieldValues[fieldName]) {
            is Boolean -> value
            is Number -> value.toInt() != 0
            is String -> value.equals("true", ignoreCase = true) || value == "1"
            null -> defaultValue
            else -> defaultValue
        }
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

    private fun convertTodays(value: Int, unit: String): Int {
        return when (unit) {
            "天" -> value
            "周" -> value * 7
            "月" -> value * 30
            "年" -> value * 365
            else -> value
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
'@

$lines = $head + ($midBlock -split "`r?`n") + $tail
Set-Content -Encoding UTF8 $path $lines
