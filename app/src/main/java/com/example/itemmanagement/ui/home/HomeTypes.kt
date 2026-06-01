package com.example.itemmanagement.ui.home

enum class ItemViewType {
    WATERFALL,
    LIST
}

enum class ItemSortType(val label: String) {
    DEFAULT("智能推荐"),
    CREATE_TIME_DESC("最新添加"),
    CREATE_TIME_ASC("最早添加"),
    PRICE_DESC("价格最高"),
    PRICE_ASC("价格最低"),
    NAME("名称")
}
