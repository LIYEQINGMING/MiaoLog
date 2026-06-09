package com.example.itemmanagement.ui.components

import android.net.Uri

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * 通用图标渲染组件
 */
@Composable
fun MiaoIcon(
    icon: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp
) {
    val source = IconSource.fromPersistString(icon)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when (source) {
            is IconSource.Emoji -> Text(text = source.code, fontSize = fontSize)
            is IconSource.Vector -> Text(text = "📍", fontSize = fontSize) // 占位，待集成 Material Symbols
            is IconSource.Custom -> Text(text = "🖼️", fontSize = fontSize) // 占位，待集成上传功能
            IconSource.None -> Text(text = "❓", fontSize = fontSize)
        }
    }
}

/**
 * 图标来源统一模型
 */
sealed class IconSource {
    /**
     * 表情符号类型
     * @param code Unicode 字符
     */
    data class Emoji(val code: String) : IconSource()

    /**
     * 矢量图标类型 (Google Material Symbols)
     * @param name 图标名称
     */
    data class Vector(val name: String) : IconSource()

    /**
     * 自定义图片类型
     * @param uri 图片的本地或网络路径
     */
    data class Custom(val uri: String) : IconSource()

    /**
     * 无图标
     */
    object None : IconSource()

    /**
     * 转换为存储用的字符串
     */
    fun toPersistString(): String {
        return when (this) {
            is Emoji -> "EMOJI:$code"
            is Vector -> "VECTOR:$name"
            is Custom -> "CUSTOM:$uri"
            is None -> ""
        }
    }

    companion object {
        /**
         * 从存储的字符串解析回 IconSource
         */
        fun fromPersistString(value: String?): IconSource {
            if (value.isNullOrBlank()) return None
            return when {
                value.startsWith("EMOJI:") -> Emoji(value.removePrefix("EMOJI:"))
                value.startsWith("VECTOR:") -> Vector(value.removePrefix("VECTOR:"))
                value.startsWith("CUSTOM:") -> Custom(value.removePrefix("CUSTOM:"))
                // 兼容旧版本直接存储 Emoji 的情况
                else -> Emoji(value)
            }
        }
    }
}

/**
 * Emoji 分类模型
 */
data class EmojiCategory(
    val name: String,
    val icon: String,
    val emojis: List<String>
)

/**
 * 标准分类的 Emoji 资源库
 */
val CATEGORIZED_EMOJIS = listOf(
    EmojiCategory("常用", "⭐", listOf("📦", "🏠", "💻", "👕", "🍎", "💊", "🛠️", "🎮", "📚", "📱", "📷", "🔋", "💡", "🔑", "💼")),
    EmojiCategory("人物与表情", "😀", listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "🙃", "😉", "😊", "😇", "🥰", "😍", "🤩", "😘", "😗", "☺", "😚",
        "😋", "😛", "😜", "🤪", "😝", "🤑", "🤗", "🤭", "🤫", "🤔", "🤐", "🤨", "😐", "😑", "😶", "😏", "😒", "🙄", "😬", "🤥",
        "😌", "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕", "🤢", "🤮", "🤧", "🥵", "🥶", "🥴", "😵", "🤯", "🤠", "🥳", "😎", "🤓"
    )),
    EmojiCategory("自然与动物", "🌵", listOf(
        "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐻‍❄️", "🐨", "🐯", "🦁", "🐮", "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🐤",
        "🦆", "🦅", "🦉", "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🐛", "🦋", "🐌", "🐞", "🐜", "🦟", "🦗", "🕷️", "🦂", "🐢", "🐍",
        "🦎", "🐙", "🦑", "🦐", "🦞", "🦀", "🐡", "🐠", "🐟", "🐬", "🐳", "🐋", "🦈", "🐊", "🐅", "🐆", "🦓", "🦍", "🐘", "🦛"
    )),
    EmojiCategory("食物与饮品", "🍏", listOf(
        "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐", "🍈", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🍆", "🥑",
        "🥦", "🥬", "🥒", "🌶️", "🌽", "🥕", "🥔", "🍠", "🥐", "🍞", "🥖", "🥨", "🥯", "🥞", "🧇", "🧀", "🍖", "🍗", "🥩", "🥓"
    )),
    EmojiCategory("活动与娱乐", "⚽", listOf(
        "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱", "🪀", "🏓", "🏸", "🏒", "🏑", "🥍", "🏏", "🥅", "⛳", "🪁",
        "🏹", "🎣", "🤿", "🥊", "🥋", "⛸️", "🎿", "🛷", "🥌", "🎯", "🪀", "🪁", "🎮", "🕹️", "🎰", "🎲", "🧩", "🧸", "🎨", "🎭"
    )),
    EmojiCategory("旅行与地点", "🚗", listOf(
        "🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑", "🚒", "🚐", "🚚", "🚛", "🚜", "🛵", "🏍️", "🛺", "🚲", "🛴", "🛹", "🛼",
        "🚏", "🛣️", "🛤️", "🛢️", "⛽", "🚨", "🚥", "🚦", "🛑", "🚧", "⚓", "⛵", "🛶", "🚤", "🛳️", "⛴️", "🚢", "✈️", "🛩️", "🛫"
    )),
    EmojiCategory("物品与工具", "💡", listOf(
        "⌚", "📱", "💻", "⌨️", "🖱️", "🖨️", "📷", "📽️", "🎞️", "📞", "☎️", "📟", "📠", "📺", "📻", "🎙️", "🎚️", "🎛️", "🧭", "⏱️",
        "⏲️", "⏳", "⌛", "🔋", "🔌", "💡", "🔦", "🕯️", "🪔", "🧱", "⛓️", "🧲", "🔫", "💣", "🧨", "🪓", "🔪", "🗡️", "⚔️", "🛡️"
    )),
    EmojiCategory("符号与标志", "🔣", listOf(
        "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔", "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "☮️",
        "✝️", "☪️", "🕉️", "☸️", "✡️", "🔯", "🕎", "☯️", "☦️", "🛐", "⛎", "♈", "♉", "♊", "♋", "♌", "♍", "♎", "♏", "♐"
    ))
)
