package com.example.itemmanagement.ui.components

import kotlin.LazyThreadSafetyMode

internal val materialSymbols_search_chunk1: List<MaterialSymbolInfo> by lazy(LazyThreadSafetyMode.NONE) {
    listOf(
        MaterialSymbolInfo(name = "bathroom", codePoint = 61405, tags = listOf("accessibility", "accessible", "amenities", "bar", "bath", "bathroom", "building", "closet")),
        MaterialSymbolInfo(name = "bed", codePoint = 61407, tags = listOf("apartment", "bed", "bedroom", "bedtime", "blanket", "comfort", "double", "dreaming")),
        MaterialSymbolInfo(name = "bedroom_baby", codePoint = 61408, tags = listOf("babies", "baby", "bed", "bedroom", "care", "child", "childhood", "children")),
        MaterialSymbolInfo(name = "bedroom_child", codePoint = 61409, tags = listOf("accommodation", "baby", "bed", "bedroom", "building", "child", "children", "cot")),
        MaterialSymbolInfo(name = "bedroom_parent", codePoint = 61410, tags = listOf("adult", "bed", "bedroom", "building", "child", "children", "domestic", "double")),
        MaterialSymbolInfo(name = "blender", codePoint = 61411, tags = listOf("appliance", "base", "beverage preparation", "blades", "blend", "blender", "container", "cook")),
        MaterialSymbolInfo(name = "camera_indoor", codePoint = 61417, tags = listOf("architecture", "automation", "building", "camera", "cctv", "device", "dome camera", "electronic")),
        MaterialSymbolInfo(name = "camera_outdoor", codePoint = 61418, tags = listOf("architecture", "building", "business", "camera", "cctv", "commercial", "dome camera", "estate")),
        MaterialSymbolInfo(name = "chair", codePoint = 61421, tags = listOf("balcony", "bedroom", "chair", "comfort", "couch", "decoration", "dining", "furniture")),
        MaterialSymbolInfo(name = "chair_alt", codePoint = 61422, tags = listOf("basic", "cahir", "chair", "comfortable", "furniture", "graphic", "home", "house")),
        MaterialSymbolInfo(name = "coffee", codePoint = 61423, tags = listOf("barista", "beverage", "break", "breakfast", "cafe", "cafe culture", "cafe menu", "caffeine")),
        MaterialSymbolInfo(name = "coffee_maker", codePoint = 61424, tags = listOf("appliance", "appliances", "automation", "beverage", "breakfast", "brew", "cafe", "caffeine")),
        MaterialSymbolInfo(name = "dining", codePoint = 61428, tags = listOf("beverage", "bistro", "breakfast", "cafe", "cafeteria", "canteen", "cuisine", "cutlery")),
        MaterialSymbolInfo(name = "door_back", codePoint = 61436, tags = listOf("access", "back", "barrier", "close", "closed", "diagonal line", "door", "doorway")),
        MaterialSymbolInfo(name = "door_front", codePoint = 61437, tags = listOf("access", "access point", "building", "close", "closed", "door", "doorway", "dwelling")),
        MaterialSymbolInfo(name = "door_sliding", codePoint = 61438, tags = listOf("access", "architecture", "auto", "automatic", "barrier", "basic", "building", "close")),
        MaterialSymbolInfo(name = "doorbell", codePoint = 61439, tags = listOf("alarm", "alert", "app icon", "basic", "bell", "button", "chime", "door")),
        MaterialSymbolInfo(name = "feed", codePoint = 61449, tags = listOf("abstract", "article", "atom", "blog", "broadcasting", "circle", "communication", "content")),
        MaterialSymbolInfo(name = "flatware", codePoint = 61452, tags = listOf("cafe", "cafeteria", "catering", "cooking", "cutlery", "diner", "dining", "dinner")),
        MaterialSymbolInfo(name = "garage", codePoint = 61457, tags = listOf("auto", "automobile", "automotive", "building", "car", "cars", "closed", "direction")),
        MaterialSymbolInfo(name = "light", codePoint = 61482, tags = listOf("abstract", "adjust", "basic", "bright", "brightness", "bulb", "ceiling", "concept")),
        MaterialSymbolInfo(name = "living", codePoint = 61483, tags = listOf("account", "account icon", "avatar", "basic", "bust", "chair", "comfort", "couch")),
        MaterialSymbolInfo(name = "manage_search", codePoint = 61487, tags = listOf("administration", "configure", "control", "discover", "explore", "filter", "find", "find and manage")),
        MaterialSymbolInfo(name = "podcasts", codePoint = 61512, tags = listOf("audio", "audio player", "bars", "broadcast", "casting", "circle", "communication", "content")),
        MaterialSymbolInfo(name = "shower", codePoint = 61537, tags = listOf("apartment", "bath", "bathroom", "cleaning", "cleanliness", "closet", "droplet", "droplets")),
        MaterialSymbolInfo(name = "table_bar", codePoint = 60114, tags = listOf("analysis", "analytics", "bar", "business", "cafe", "chart", "columns", "data")),
        MaterialSymbolInfo(name = "table_restaurant", codePoint = 60102, tags = listOf("bar", "bistro", "bistro table", "book table", "booking", "cafe", "cafe table", "dining")),
        MaterialSymbolInfo(name = "window", codePoint = 61576, tags = listOf("aperture", "app", "application", "application window", "basic shape", "border", "browser window", "close")),
        MaterialSymbolInfo(name = "yard", codePoint = 61577, tags = listOf("area", "backyard", "building", "compound", "dwelling", "estate", "exterior", "flower"))
    )
}

internal val materialSymbols_search_category: MaterialSymbolCategory by lazy(LazyThreadSafetyMode.NONE) {
    MaterialSymbolCategory(
        key = "search",
        icons = buildList {
            addAll(materialSymbols_search_chunk1)
        }
    )
}
