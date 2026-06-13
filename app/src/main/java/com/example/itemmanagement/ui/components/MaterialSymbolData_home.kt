package com.example.itemmanagement.ui.components

import kotlin.LazyThreadSafetyMode

internal val materialSymbols_home_chunk1: List<MaterialSymbolInfo> by lazy(LazyThreadSafetyMode.NONE) {
    listOf(
        MaterialSymbolInfo(name = "auto_mode", codePoint = 60448, tags = listOf("a", "ai", "alphabet", "around", "arrow", "arrows", "artificial", "auto")),
        MaterialSymbolInfo(name = "blinds", codePoint = 57990, tags = listOf("adjust", "automation", "blinds", "close", "control", "cover", "covering", "curtains")),
        MaterialSymbolInfo(name = "blinds_closed", codePoint = 60447, tags = listOf("architecture", "blinds", "block light", "building", "closed", "cover", "covering", "curtains")),
        MaterialSymbolInfo(name = "broadcast_on_home", codePoint = 63736, tags = listOf("airplay", "audio", "broadcast", "cast", "communication", "connect", "connectivity", "content")),
        MaterialSymbolInfo(name = "broadcast_on_personal", codePoint = 63737, tags = listOf("account", "antenna", "avatar", "broadcast", "communication", "connection", "digital", "feed")),
        MaterialSymbolInfo(name = "curtains", codePoint = 60446, tags = listOf("bedroom", "blinds", "closed", "cover", "curtains", "decor", "decoration", "drapes")),
        MaterialSymbolInfo(name = "curtains_closed", codePoint = 60445, tags = listOf("barrier", "blinds", "block", "closed", "cloth", "cover", "covering", "curtains")),
        MaterialSymbolInfo(name = "electric_bolt", codePoint = 60444, tags = listOf("alert", "battery", "bolt", "charging", "current", "danger", "dynamic", "electric")),
        MaterialSymbolInfo(name = "electric_meter", codePoint = 60443, tags = listOf("analog", "bill", "billing", "bolt", "building", "charge", "circle", "consumption")),
        MaterialSymbolInfo(name = "energy_savings_leaf", codePoint = 60442, tags = listOf("battery saver", "button", "configuration", "conservation", "control", "eco", "eco-friendly", "ecology")),
        MaterialSymbolInfo(name = "gas_meter", codePoint = 60441, tags = listOf("analog indicator", "analog meter", "circular gauge", "circular meter", "consumption", "dashboard", "dashboard element", "dial")),
        MaterialSymbolInfo(name = "heat_pump", codePoint = 60440, tags = listOf("air conditioner", "air conditioning", "air flow", "appliance", "building", "climate control", "commercial", "cool")),
        MaterialSymbolInfo(name = "mode_fan_off", codePoint = 60439, tags = listOf("air", "air conditioner", "angular", "appliance", "blades", "circle", "climate", "controls")),
        MaterialSymbolInfo(name = "nest_cam_wired_stand", codePoint = 60438, tags = listOf("bedroom", "camera", "camera on stand", "cctv", "device", "electronic device", "electronics", "film")),
        MaterialSymbolInfo(name = "oil_barrel", codePoint = 60437, tags = listOf("barrel", "black gold", "commodity", "container", "crude oil", "diesel", "droplet", "drum")),
        MaterialSymbolInfo(name = "propane", codePoint = 60436, tags = listOf("barbecue", "bottle", "cap", "container", "cooking", "cylinder", "energy", "flammable")),
        MaterialSymbolInfo(name = "propane_tank", codePoint = 60435, tags = listOf("barbecue", "bbq", "bottle", "camping", "container", "cooking", "cylinder", "empty")),
        MaterialSymbolInfo(name = "roller_shades", codePoint = 60434, tags = listOf("adjust", "automation", "basic", "blinds", "close", "closed", "control", "cover")),
        MaterialSymbolInfo(name = "roller_shades_closed", codePoint = 60433, tags = listOf("blinds", "blinds closed", "building control", "closed", "cover", "curtains", "curtains closed", "down")),
        MaterialSymbolInfo(name = "sensor_door", codePoint = 61877, tags = listOf("access", "access point", "alarm", "alert", "automation", "close", "detect", "door")),
        MaterialSymbolInfo(name = "sensor_occupied", codePoint = 60432, tags = listOf("activated", "active", "alarm", "alert", "automation", "body", "body response", "connection")),
        MaterialSymbolInfo(name = "sensor_window", codePoint = 61876, tags = listOf("alarm", "area", "border", "boundary", "camera", "data", "detection", "device")),
        MaterialSymbolInfo(name = "shield_moon", codePoint = 60073, tags = listOf("certified", "confidential", "crescent", "crescent moon", "dark mode", "defense", "do not disturb", "encrypted")),
        MaterialSymbolInfo(name = "solar_power", codePoint = 60431, tags = listOf("array", "clean energy", "clean power", "eco", "electric power", "electricity", "energy", "generation")),
        MaterialSymbolInfo(name = "vertical_shades", codePoint = 60430, tags = listOf("adjust", "bars", "blinds", "building", "control", "cover", "covering", "curtain")),
        MaterialSymbolInfo(name = "vertical_shades_closed", codePoint = 60429, tags = listOf("barrier", "blinds", "closed", "cover", "covering", "curtains", "domestic", "home")),
        MaterialSymbolInfo(name = "wind_power", codePoint = 60428, tags = listOf("abstract", "air flow", "alternative energy", "blades", "clean energy", "climate", "eco", "eco-friendly"))
    )
}

internal val materialSymbols_home_category: MaterialSymbolCategory by lazy(LazyThreadSafetyMode.NONE) {
    MaterialSymbolCategory(
        key = "home",
        icons = buildList {
            addAll(materialSymbols_home_chunk1)
        }
    )
}
