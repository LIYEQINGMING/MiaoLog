package com.example.itemmanagement.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

data class VectorCategory(
    val name: String,
    val icons: List<String>
)

val CATEGORIZED_VECTORS = listOf(
    VectorCategory("音视频", listOf(
        "_10k", "_1k", "_1kPlus", "_2k", "_2kPlus", "_3k", "_3kPlus", "_4k", "_4kPlus", "_5g", 
        "_5k", "_5kPlus", "_6k", "_6kPlus", "_7k", "_7kPlus", "_8k", "_8kPlus", "_9k", "_9kPlus", 
        "AddToQueue", "Airplay", "Album", "ArtTrack", "AudioFile"
    )),
    VectorCategory("图片", listOf(
        "_10mp", "_11mp", "_12mp", "_13mp", "_14mp", "_15mp", "_16mp", "_17mp", "_18mp", 
        "_19mp", "_20mp", "_21mp", "_22mp", "_23mp", "_24mp", "_2mp", "_30fpsSelect", "_3mp", 
        "_4mp", "_5mp", "_60fpsSelect", "_6mp", "_7mp", "_8mp", "_9mp"
    )),
    VectorCategory("操作", listOf(
        "_123", "_3dRotation", "Abc", "Accessibility", "AccessibilityNew", "Accessible", 
        "AccessibleForward", "AccountBalance", "AccountBalanceWallet", "AccountBox", "AccountCircle", 
        "AddCard", "AddHome", "AddShoppingCart", "AddTask", "AddToDrive", "Addchart", "AdminPanelSettings", 
        "AdsClick", "Alarm", "AlarmAdd", "AlarmOff", "AlarmOn", "AllInbox", "AllOut"
    )),
    VectorCategory("社交", listOf(
        "_18UpRating", "_6FtApart", "AddModerator", "AddReaction", "Architecture", "AssistWalker", 
        "BackHand", "Blind", "Boy", "Cake", "CatchingPokemon", "CleanHands", "Co2", "Compost", 
        "ConnectWithoutContact", "Construction", "Cookie", "Coronavirus", "CrueltyFree", 
        "Cyclone", "Deck", "Diversity1", "Diversity2", "Diversity3", "Domain"
    )),
    VectorCategory("设备", listOf(
        "_1xMobiledata", "_30fps", "_3gMobiledata", "_4gMobiledata", "_4gPlusMobiledata", 
        "_60fps", "AccessAlarm", "AccessAlarms", "AccessTime", "AccessTimeFilled", "AdUnits", 
        "AddAlarm", "AddToHomeScreen", "Air", "AirplaneTicket", "AirplanemodeActive", "AirplanemodeInactive", 
        "Aod", "Battery0Bar", "Battery1Bar", "Battery2Bar", "Battery3Bar", "Battery4Bar", 
        "Battery5Bar", "Battery6Bar"
    )),
    VectorCategory("地图", listOf(
        "_360", "AddBusiness", "AddLocation", "AddLocationAlt", "AddRoad", "Agriculture", 
        "AirlineStops", "Airlines", "AltRoute", "Atm", "Attractions", "Badge", "BakeryDining", 
        "Beenhere", "BikeScooter", "BreakfastDining", "BrunchDining", "BusAlert", "CarCrash", 
        "CarRental", "CarRepair", "Castle", "Category", "Celebration", "Church"
    )),
    VectorCategory("通讯", listOf(
        "_3p", "AddIcCall", "AlternateEmail", "AppRegistration", "Business", "Call", "CallEnd", 
        "CallMade", "CallMerge", "CallMissed", "CallMissedOutgoing", "CallReceived", "CallSplit", 
        "CancelPresentation", "CellTower", "CellWifi", "Chat", "ChatBubble", "ChatBubbleOutline", 
        "ClearAll", "CoPresent", "Comment", "CommentsDisabled", "ContactEmergency", "ContactMail"
    )),
    VectorCategory("地点", listOf(
        "AcUnit", "AirportShuttle", "AllInclusive", "Apartment", "BabyChangingStation", "Backpack", 
        "Balcony", "Bathtub", "BeachAccess", "Bento", "Bungalow", "BusinessCenter", "Cabin", 
        "Carpenter", "Casino", "Chalet", "ChargingStation", "Checkroom", "ChildCare", "ChildFriendly", 
        "CorporateFare", "Cottage", "Countertops", "Crib", "Desk"
    )),
    VectorCategory("通知", listOf(
        "AccountTree", "Adb", "AirlineSeatFlat", "AirlineSeatFlatAngled", "AirlineSeatIndividualSuite", 
        "AirlineSeatLegroomExtra", "AirlineSeatLegroomNormal", "AirlineSeatLegroomReduced", 
        "AirlineSeatReclineExtra", "AirlineSeatReclineNormal", "BluetoothAudio", "ConfirmationNumber", 
        "DirectionsOff", "DiscFull", "DoDisturb", "DoDisturbAlt", "DoDisturbOff", "DoDisturbOn", 
        "DoNotDisturb", "DoNotDisturbAlt", "DoNotDisturbOff", "DoNotDisturbOn", "DriveEta", 
        "EnhancedEncryption", "EventAvailable"
    )),
    VectorCategory("警告", listOf(
        "AddAlert", "AutoDelete", "Error", "ErrorOutline", "NotificationImportant", "Warning", 
        "WarningAmber"
    )),
    VectorCategory("内容", listOf(
        "AddBox", "AddCircle", "AddCircleOutline", "AddLink", "Archive", "Attribution", "Backspace", 
        "Ballot", "Biotech", "Block", "Bolt", "Calculate", "ChangeCircle", "Clear", "ContentCopy", 
        "ContentCut", "ContentPaste", "ContentPasteGo", "ContentPasteOff", "ContentPasteSearch", 
        "CopyAll", "Create", "DeleteSweep", "Deselect", "Drafts"
    )),
    VectorCategory("编辑", listOf(
        "AddChart", "AddComment", "AlignHorizontalCenter", "AlignHorizontalLeft", "AlignHorizontalRight", 
        "AlignVerticalBottom", "AlignVerticalCenter", "AlignVerticalTop", "AreaChart", "AttachFile", 
        "AttachMoney", "AutoGraph", "BarChart", "BorderAll", "BorderBottom", "BorderClear", 
        "BorderColor", "BorderHorizontal", "BorderInner", "BorderLeft", "BorderOuter", "BorderRight", 
        "BorderStyle", "BorderTop", "BorderVertical"
    )),
    VectorCategory("导航", listOf(
        "AddHomeWork", "AppSettingsAlt", "Apps", "AppsOutage", "ArrowBack", "ArrowBackIos", 
        "ArrowBackIosNew", "ArrowDownward", "ArrowDropDown", "ArrowDropDownCircle", "ArrowDropUp", 
        "ArrowForward", "ArrowForwardIos", "ArrowLeft", "ArrowRight", "ArrowUpward", "AssistantDirection", 
        "Campaign", "Cancel", "Check", "ChevronLeft", "ChevronRight", "Close", "DoubleArrow", 
        "East"
    )),
    VectorCategory("硬件", listOf(
        "AdfScanner", "BrowserNotSupported", "BrowserUpdated", "Cast", "CastConnected", "CastForEducation", 
        "Computer", "ConnectedTv", "DesktopMac", "DesktopWindows", "DeveloperBoard", "DeveloperBoardOff", 
        "DeviceHub", "DeviceUnknown", "DevicesOther", "Dock", "Earbuds", "EarbudsBattery", 
        "Gamepad", "Headphones", "HeadphonesBattery", "Headset", "HeadsetMic", "HeadsetOff", 
        "HomeMax"
    )),
    VectorCategory("文件", listOf(
        "Approval", "AttachEmail", "Attachment", "Cloud", "CloudCircle", "CloudDone", "CloudDownload", 
        "CloudOff", "CloudQueue", "CloudSync", "CloudUpload", "CreateNewFolder", "Difference", 
        "Download", "DownloadDone", "DownloadForOffline", "Downloading", "DriveFileMove", 
        "DriveFileMoveRtl", "DriveFileRenameOutline", "DriveFolderUpload", "FileDownload", 
        "FileDownloadDone", "FileDownloadOff", "FileOpen"
    )),
    VectorCategory("家居", listOf(
        "AutoMode", "Blinds", "BlindsClosed", "BroadcastOnHome", "BroadcastOnPersonal", "Curtains", 
        "CurtainsClosed", "ElectricBolt", "ElectricMeter", "EnergySavingsLeaf", "GasMeter", 
        "HeatPump", "ModeFanOff", "NestCamWiredStand", "OilBarrel", "Propane", "PropaneTank", 
        "RollerShades", "RollerShadesClosed", "SensorDoor", "SensorOccupied", "SensorWindow", 
        "ShieldMoon", "SolarPower", "VerticalShades"
    )),
    VectorCategory("搜索", listOf(
        "Bathroom", "Bed", "BedroomBaby", "BedroomChild", "BedroomParent", "Blender", "CameraIndoor", 
        "CameraOutdoor", "Chair", "ChairAlt", "Coffee", "CoffeeMaker", "Dining", "DoorBack", 
        "DoorFront", "DoorSliding", "Doorbell", "Feed", "Flatware", "Garage", "Light", "Living", 
        "ManageSearch", "Podcasts", "Shower"
    )),
    VectorCategory("开关", listOf(
        "CheckBox", "CheckBoxOutlineBlank", "IndeterminateCheckBox", "RadioButtonChecked", 
        "RadioButtonUnchecked", "StarBorder", "StarBorderPurple500", "StarOutline", "StarPurple500", 
        "ToggleOff", "ToggleOn"
    )),
)

fun getVectorIconByName(name: String): ImageVector? {
    return when (name) {
        "_10k" -> Icons.Rounded._10k
        "_1k" -> Icons.Rounded._1k
        "_1kPlus" -> Icons.Rounded._1kPlus
        "_2k" -> Icons.Rounded._2k
        "_2kPlus" -> Icons.Rounded._2kPlus
        "_3k" -> Icons.Rounded._3k
        "_3kPlus" -> Icons.Rounded._3kPlus
        "_4k" -> Icons.Rounded._4k
        "_4kPlus" -> Icons.Rounded._4kPlus
        "_5g" -> Icons.Rounded._5g
        "_5k" -> Icons.Rounded._5k
        "_5kPlus" -> Icons.Rounded._5kPlus
        "_6k" -> Icons.Rounded._6k
        "_6kPlus" -> Icons.Rounded._6kPlus
        "_7k" -> Icons.Rounded._7k
        "_7kPlus" -> Icons.Rounded._7kPlus
        "_8k" -> Icons.Rounded._8k
        "_8kPlus" -> Icons.Rounded._8kPlus
        "_9k" -> Icons.Rounded._9k
        "_9kPlus" -> Icons.Rounded._9kPlus
        "AddToQueue" -> Icons.Rounded.AddToQueue
        "Airplay" -> Icons.Rounded.Airplay
        "Album" -> Icons.Rounded.Album
        "ArtTrack" -> Icons.Rounded.ArtTrack
        "AudioFile" -> Icons.Rounded.AudioFile
        "_10mp" -> Icons.Rounded._10mp
        "_11mp" -> Icons.Rounded._11mp
        "_12mp" -> Icons.Rounded._12mp
        "_13mp" -> Icons.Rounded._13mp
        "_14mp" -> Icons.Rounded._14mp
        "_15mp" -> Icons.Rounded._15mp
        "_16mp" -> Icons.Rounded._16mp
        "_17mp" -> Icons.Rounded._17mp
        "_18mp" -> Icons.Rounded._18mp
        "_19mp" -> Icons.Rounded._19mp
        "_20mp" -> Icons.Rounded._20mp
        "_21mp" -> Icons.Rounded._21mp
        "_22mp" -> Icons.Rounded._22mp
        "_23mp" -> Icons.Rounded._23mp
        "_24mp" -> Icons.Rounded._24mp
        "_2mp" -> Icons.Rounded._2mp
        "_30fpsSelect" -> Icons.Rounded._30fpsSelect
        "_3mp" -> Icons.Rounded._3mp
        "_4mp" -> Icons.Rounded._4mp
        "_5mp" -> Icons.Rounded._5mp
        "_60fpsSelect" -> Icons.Rounded._60fpsSelect
        "_6mp" -> Icons.Rounded._6mp
        "_7mp" -> Icons.Rounded._7mp
        "_8mp" -> Icons.Rounded._8mp
        "_9mp" -> Icons.Rounded._9mp
        "_123" -> Icons.Rounded._123
        "_3dRotation" -> Icons.Rounded._3dRotation
        "Abc" -> Icons.Rounded.Abc
        "Accessibility" -> Icons.Rounded.Accessibility
        "AccessibilityNew" -> Icons.Rounded.AccessibilityNew
        "Accessible" -> Icons.Rounded.Accessible
        "AccessibleForward" -> Icons.Rounded.AccessibleForward
        "AccountBalance" -> Icons.Rounded.AccountBalance
        "AccountBalanceWallet" -> Icons.Rounded.AccountBalanceWallet
        "AccountBox" -> Icons.Rounded.AccountBox
        "AccountCircle" -> Icons.Rounded.AccountCircle
        "AddCard" -> Icons.Rounded.AddCard
        "AddHome" -> Icons.Rounded.AddHome
        "AddShoppingCart" -> Icons.Rounded.AddShoppingCart
        "AddTask" -> Icons.Rounded.AddTask
        "AddToDrive" -> Icons.Rounded.AddToDrive
        "Addchart" -> Icons.Rounded.Addchart
        "AdminPanelSettings" -> Icons.Rounded.AdminPanelSettings
        "AdsClick" -> Icons.Rounded.AdsClick
        "Alarm" -> Icons.Rounded.Alarm
        "AlarmAdd" -> Icons.Rounded.AlarmAdd
        "AlarmOff" -> Icons.Rounded.AlarmOff
        "AlarmOn" -> Icons.Rounded.AlarmOn
        "AllInbox" -> Icons.Rounded.AllInbox
        "AllOut" -> Icons.Rounded.AllOut
        "_18UpRating" -> Icons.Rounded._18UpRating
        "_6FtApart" -> Icons.Rounded._6FtApart
        "AddModerator" -> Icons.Rounded.AddModerator
        "AddReaction" -> Icons.Rounded.AddReaction
        "Architecture" -> Icons.Rounded.Architecture
        "AssistWalker" -> Icons.Rounded.AssistWalker
        "BackHand" -> Icons.Rounded.BackHand
        "Blind" -> Icons.Rounded.Blind
        "Boy" -> Icons.Rounded.Boy
        "Cake" -> Icons.Rounded.Cake
        "CatchingPokemon" -> Icons.Rounded.CatchingPokemon
        "CleanHands" -> Icons.Rounded.CleanHands
        "Co2" -> Icons.Rounded.Co2
        "Compost" -> Icons.Rounded.Compost
        "ConnectWithoutContact" -> Icons.Rounded.ConnectWithoutContact
        "Construction" -> Icons.Rounded.Construction
        "Cookie" -> Icons.Rounded.Cookie
        "Coronavirus" -> Icons.Rounded.Coronavirus
        "CrueltyFree" -> Icons.Rounded.CrueltyFree
        "Cyclone" -> Icons.Rounded.Cyclone
        "Deck" -> Icons.Rounded.Deck
        "Diversity1" -> Icons.Rounded.Diversity1
        "Diversity2" -> Icons.Rounded.Diversity2
        "Diversity3" -> Icons.Rounded.Diversity3
        "Domain" -> Icons.Rounded.Domain
        "_1xMobiledata" -> Icons.Rounded._1xMobiledata
        "_30fps" -> Icons.Rounded._30fps
        "_3gMobiledata" -> Icons.Rounded._3gMobiledata
        "_4gMobiledata" -> Icons.Rounded._4gMobiledata
        "_4gPlusMobiledata" -> Icons.Rounded._4gPlusMobiledata
        "_60fps" -> Icons.Rounded._60fps
        "AccessAlarm" -> Icons.Rounded.AccessAlarm
        "AccessAlarms" -> Icons.Rounded.AccessAlarms
        "AccessTime" -> Icons.Rounded.AccessTime
        "AccessTimeFilled" -> Icons.Rounded.AccessTimeFilled
        "AdUnits" -> Icons.Rounded.AdUnits
        "AddAlarm" -> Icons.Rounded.AddAlarm
        "AddToHomeScreen" -> Icons.Rounded.AddToHomeScreen
        "Air" -> Icons.Rounded.Air
        "AirplaneTicket" -> Icons.Rounded.AirplaneTicket
        "AirplanemodeActive" -> Icons.Rounded.AirplanemodeActive
        "AirplanemodeInactive" -> Icons.Rounded.AirplanemodeInactive
        "Aod" -> Icons.Rounded.Aod
        "Battery0Bar" -> Icons.Rounded.Battery0Bar
        "Battery1Bar" -> Icons.Rounded.Battery1Bar
        "Battery2Bar" -> Icons.Rounded.Battery2Bar
        "Battery3Bar" -> Icons.Rounded.Battery3Bar
        "Battery4Bar" -> Icons.Rounded.Battery4Bar
        "Battery5Bar" -> Icons.Rounded.Battery5Bar
        "Battery6Bar" -> Icons.Rounded.Battery6Bar
        "_360" -> Icons.Rounded._360
        "AddBusiness" -> Icons.Rounded.AddBusiness
        "AddLocation" -> Icons.Rounded.AddLocation
        "AddLocationAlt" -> Icons.Rounded.AddLocationAlt
        "AddRoad" -> Icons.Rounded.AddRoad
        "Agriculture" -> Icons.Rounded.Agriculture
        "AirlineStops" -> Icons.Rounded.AirlineStops
        "Airlines" -> Icons.Rounded.Airlines
        "AltRoute" -> Icons.Rounded.AltRoute
        "Atm" -> Icons.Rounded.Atm
        "Attractions" -> Icons.Rounded.Attractions
        "Badge" -> Icons.Rounded.Badge
        "BakeryDining" -> Icons.Rounded.BakeryDining
        "Beenhere" -> Icons.Rounded.Beenhere
        "BikeScooter" -> Icons.Rounded.BikeScooter
        "BreakfastDining" -> Icons.Rounded.BreakfastDining
        "BrunchDining" -> Icons.Rounded.BrunchDining
        "BusAlert" -> Icons.Rounded.BusAlert
        "CarCrash" -> Icons.Rounded.CarCrash
        "CarRental" -> Icons.Rounded.CarRental
        "CarRepair" -> Icons.Rounded.CarRepair
        "Castle" -> Icons.Rounded.Castle
        "Category" -> Icons.Rounded.Category
        "Celebration" -> Icons.Rounded.Celebration
        "Church" -> Icons.Rounded.Church
        "_3p" -> Icons.Rounded._3p
        "AddIcCall" -> Icons.Rounded.AddIcCall
        "AlternateEmail" -> Icons.Rounded.AlternateEmail
        "AppRegistration" -> Icons.Rounded.AppRegistration
        "Business" -> Icons.Rounded.Business
        "Call" -> Icons.Rounded.Call
        "CallEnd" -> Icons.Rounded.CallEnd
        "CallMade" -> Icons.Rounded.CallMade
        "CallMerge" -> Icons.Rounded.CallMerge
        "CallMissed" -> Icons.Rounded.CallMissed
        "CallMissedOutgoing" -> Icons.Rounded.CallMissedOutgoing
        "CallReceived" -> Icons.Rounded.CallReceived
        "CallSplit" -> Icons.Rounded.CallSplit
        "CancelPresentation" -> Icons.Rounded.CancelPresentation
        "CellTower" -> Icons.Rounded.CellTower
        "CellWifi" -> Icons.Rounded.CellWifi
        "Chat" -> Icons.Rounded.Chat
        "ChatBubble" -> Icons.Rounded.ChatBubble
        "ChatBubbleOutline" -> Icons.Rounded.ChatBubbleOutline
        "ClearAll" -> Icons.Rounded.ClearAll
        "CoPresent" -> Icons.Rounded.CoPresent
        "Comment" -> Icons.Rounded.Comment
        "CommentsDisabled" -> Icons.Rounded.CommentsDisabled
        "ContactEmergency" -> Icons.Rounded.ContactEmergency
        "ContactMail" -> Icons.Rounded.ContactMail
        "AcUnit" -> Icons.Rounded.AcUnit
        "AirportShuttle" -> Icons.Rounded.AirportShuttle
        "AllInclusive" -> Icons.Rounded.AllInclusive
        "Apartment" -> Icons.Rounded.Apartment
        "BabyChangingStation" -> Icons.Rounded.BabyChangingStation
        "Backpack" -> Icons.Rounded.Backpack
        "Balcony" -> Icons.Rounded.Balcony
        "Bathtub" -> Icons.Rounded.Bathtub
        "BeachAccess" -> Icons.Rounded.BeachAccess
        "Bento" -> Icons.Rounded.Bento
        "Bungalow" -> Icons.Rounded.Bungalow
        "BusinessCenter" -> Icons.Rounded.BusinessCenter
        "Cabin" -> Icons.Rounded.Cabin
        "Carpenter" -> Icons.Rounded.Carpenter
        "Casino" -> Icons.Rounded.Casino
        "Chalet" -> Icons.Rounded.Chalet
        "ChargingStation" -> Icons.Rounded.ChargingStation
        "Checkroom" -> Icons.Rounded.Checkroom
        "ChildCare" -> Icons.Rounded.ChildCare
        "ChildFriendly" -> Icons.Rounded.ChildFriendly
        "CorporateFare" -> Icons.Rounded.CorporateFare
        "Cottage" -> Icons.Rounded.Cottage
        "Countertops" -> Icons.Rounded.Countertops
        "Crib" -> Icons.Rounded.Crib
        "Desk" -> Icons.Rounded.Desk
        "AccountTree" -> Icons.Rounded.AccountTree
        "Adb" -> Icons.Rounded.Adb
        "AirlineSeatFlat" -> Icons.Rounded.AirlineSeatFlat
        "AirlineSeatFlatAngled" -> Icons.Rounded.AirlineSeatFlatAngled
        "AirlineSeatIndividualSuite" -> Icons.Rounded.AirlineSeatIndividualSuite
        "AirlineSeatLegroomExtra" -> Icons.Rounded.AirlineSeatLegroomExtra
        "AirlineSeatLegroomNormal" -> Icons.Rounded.AirlineSeatLegroomNormal
        "AirlineSeatLegroomReduced" -> Icons.Rounded.AirlineSeatLegroomReduced
        "AirlineSeatReclineExtra" -> Icons.Rounded.AirlineSeatReclineExtra
        "AirlineSeatReclineNormal" -> Icons.Rounded.AirlineSeatReclineNormal
        "BluetoothAudio" -> Icons.Rounded.BluetoothAudio
        "ConfirmationNumber" -> Icons.Rounded.ConfirmationNumber
        "DirectionsOff" -> Icons.Rounded.DirectionsOff
        "DiscFull" -> Icons.Rounded.DiscFull
        "DoDisturb" -> Icons.Rounded.DoDisturb
        "DoDisturbAlt" -> Icons.Rounded.DoDisturbAlt
        "DoDisturbOff" -> Icons.Rounded.DoDisturbOff
        "DoDisturbOn" -> Icons.Rounded.DoDisturbOn
        "DoNotDisturb" -> Icons.Rounded.DoNotDisturb
        "DoNotDisturbAlt" -> Icons.Rounded.DoNotDisturbAlt
        "DoNotDisturbOff" -> Icons.Rounded.DoNotDisturbOff
        "DoNotDisturbOn" -> Icons.Rounded.DoNotDisturbOn
        "DriveEta" -> Icons.Rounded.DriveEta
        "EnhancedEncryption" -> Icons.Rounded.EnhancedEncryption
        "EventAvailable" -> Icons.Rounded.EventAvailable
        "AddAlert" -> Icons.Rounded.AddAlert
        "AutoDelete" -> Icons.Rounded.AutoDelete
        "Error" -> Icons.Rounded.Error
        "ErrorOutline" -> Icons.Rounded.ErrorOutline
        "NotificationImportant" -> Icons.Rounded.NotificationImportant
        "Warning" -> Icons.Rounded.Warning
        "WarningAmber" -> Icons.Rounded.WarningAmber
        "AddBox" -> Icons.Rounded.AddBox
        "AddCircle" -> Icons.Rounded.AddCircle
        "AddCircleOutline" -> Icons.Rounded.AddCircleOutline
        "AddLink" -> Icons.Rounded.AddLink
        "Archive" -> Icons.Rounded.Archive
        "Attribution" -> Icons.Rounded.Attribution
        "Backspace" -> Icons.Rounded.Backspace
        "Ballot" -> Icons.Rounded.Ballot
        "Biotech" -> Icons.Rounded.Biotech
        "Block" -> Icons.Rounded.Block
        "Bolt" -> Icons.Rounded.Bolt
        "Calculate" -> Icons.Rounded.Calculate
        "ChangeCircle" -> Icons.Rounded.ChangeCircle
        "Clear" -> Icons.Rounded.Clear
        "ContentCopy" -> Icons.Rounded.ContentCopy
        "ContentCut" -> Icons.Rounded.ContentCut
        "ContentPaste" -> Icons.Rounded.ContentPaste
        "ContentPasteGo" -> Icons.Rounded.ContentPasteGo
        "ContentPasteOff" -> Icons.Rounded.ContentPasteOff
        "ContentPasteSearch" -> Icons.Rounded.ContentPasteSearch
        "CopyAll" -> Icons.Rounded.CopyAll
        "Create" -> Icons.Rounded.Create
        "DeleteSweep" -> Icons.Rounded.DeleteSweep
        "Deselect" -> Icons.Rounded.Deselect
        "Drafts" -> Icons.Rounded.Drafts
        "AddChart" -> Icons.Rounded.AddChart
        "AddComment" -> Icons.Rounded.AddComment
        "AlignHorizontalCenter" -> Icons.Rounded.AlignHorizontalCenter
        "AlignHorizontalLeft" -> Icons.Rounded.AlignHorizontalLeft
        "AlignHorizontalRight" -> Icons.Rounded.AlignHorizontalRight
        "AlignVerticalBottom" -> Icons.Rounded.AlignVerticalBottom
        "AlignVerticalCenter" -> Icons.Rounded.AlignVerticalCenter
        "AlignVerticalTop" -> Icons.Rounded.AlignVerticalTop
        "AreaChart" -> Icons.Rounded.AreaChart
        "AttachFile" -> Icons.Rounded.AttachFile
        "AttachMoney" -> Icons.Rounded.AttachMoney
        "AutoGraph" -> Icons.Rounded.AutoGraph
        "BarChart" -> Icons.Rounded.BarChart
        "BorderAll" -> Icons.Rounded.BorderAll
        "BorderBottom" -> Icons.Rounded.BorderBottom
        "BorderClear" -> Icons.Rounded.BorderClear
        "BorderColor" -> Icons.Rounded.BorderColor
        "BorderHorizontal" -> Icons.Rounded.BorderHorizontal
        "BorderInner" -> Icons.Rounded.BorderInner
        "BorderLeft" -> Icons.Rounded.BorderLeft
        "BorderOuter" -> Icons.Rounded.BorderOuter
        "BorderRight" -> Icons.Rounded.BorderRight
        "BorderStyle" -> Icons.Rounded.BorderStyle
        "BorderTop" -> Icons.Rounded.BorderTop
        "BorderVertical" -> Icons.Rounded.BorderVertical
        "AddHomeWork" -> Icons.Rounded.AddHomeWork
        "AppSettingsAlt" -> Icons.Rounded.AppSettingsAlt
        "Apps" -> Icons.Rounded.Apps
        "AppsOutage" -> Icons.Rounded.AppsOutage
        "ArrowBack" -> Icons.Rounded.ArrowBack
        "ArrowBackIos" -> Icons.Rounded.ArrowBackIos
        "ArrowBackIosNew" -> Icons.Rounded.ArrowBackIosNew
        "ArrowDownward" -> Icons.Rounded.ArrowDownward
        "ArrowDropDown" -> Icons.Rounded.ArrowDropDown
        "ArrowDropDownCircle" -> Icons.Rounded.ArrowDropDownCircle
        "ArrowDropUp" -> Icons.Rounded.ArrowDropUp
        "ArrowForward" -> Icons.Rounded.ArrowForward
        "ArrowForwardIos" -> Icons.Rounded.ArrowForwardIos
        "ArrowLeft" -> Icons.Rounded.ArrowLeft
        "ArrowRight" -> Icons.Rounded.ArrowRight
        "ArrowUpward" -> Icons.Rounded.ArrowUpward
        "AssistantDirection" -> Icons.Rounded.AssistantDirection
        "Campaign" -> Icons.Rounded.Campaign
        "Cancel" -> Icons.Rounded.Cancel
        "Check" -> Icons.Rounded.Check
        "ChevronLeft" -> Icons.Rounded.ChevronLeft
        "ChevronRight" -> Icons.Rounded.ChevronRight
        "Close" -> Icons.Rounded.Close
        "DoubleArrow" -> Icons.Rounded.DoubleArrow
        "East" -> Icons.Rounded.East
        "AdfScanner" -> Icons.Rounded.AdfScanner
        "BrowserNotSupported" -> Icons.Rounded.BrowserNotSupported
        "BrowserUpdated" -> Icons.Rounded.BrowserUpdated
        "Cast" -> Icons.Rounded.Cast
        "CastConnected" -> Icons.Rounded.CastConnected
        "CastForEducation" -> Icons.Rounded.CastForEducation
        "Computer" -> Icons.Rounded.Computer
        "ConnectedTv" -> Icons.Rounded.ConnectedTv
        "DesktopMac" -> Icons.Rounded.DesktopMac
        "DesktopWindows" -> Icons.Rounded.DesktopWindows
        "DeveloperBoard" -> Icons.Rounded.DeveloperBoard
        "DeveloperBoardOff" -> Icons.Rounded.DeveloperBoardOff
        "DeviceHub" -> Icons.Rounded.DeviceHub
        "DeviceUnknown" -> Icons.Rounded.DeviceUnknown
        "DevicesOther" -> Icons.Rounded.DevicesOther
        "Dock" -> Icons.Rounded.Dock
        "Earbuds" -> Icons.Rounded.Earbuds
        "EarbudsBattery" -> Icons.Rounded.EarbudsBattery
        "Gamepad" -> Icons.Rounded.Gamepad
        "Headphones" -> Icons.Rounded.Headphones
        "HeadphonesBattery" -> Icons.Rounded.HeadphonesBattery
        "Headset" -> Icons.Rounded.Headset
        "HeadsetMic" -> Icons.Rounded.HeadsetMic
        "HeadsetOff" -> Icons.Rounded.HeadsetOff
        "HomeMax" -> Icons.Rounded.HomeMax
        "Approval" -> Icons.Rounded.Approval
        "AttachEmail" -> Icons.Rounded.AttachEmail
        "Attachment" -> Icons.Rounded.Attachment
        "Cloud" -> Icons.Rounded.Cloud
        "CloudCircle" -> Icons.Rounded.CloudCircle
        "CloudDone" -> Icons.Rounded.CloudDone
        "CloudDownload" -> Icons.Rounded.CloudDownload
        "CloudOff" -> Icons.Rounded.CloudOff
        "CloudQueue" -> Icons.Rounded.CloudQueue
        "CloudSync" -> Icons.Rounded.CloudSync
        "CloudUpload" -> Icons.Rounded.CloudUpload
        "CreateNewFolder" -> Icons.Rounded.CreateNewFolder
        "Difference" -> Icons.Rounded.Difference
        "Download" -> Icons.Rounded.Download
        "DownloadDone" -> Icons.Rounded.DownloadDone
        "DownloadForOffline" -> Icons.Rounded.DownloadForOffline
        "Downloading" -> Icons.Rounded.Downloading
        "DriveFileMove" -> Icons.Rounded.DriveFileMove
        "DriveFileMoveRtl" -> Icons.Rounded.DriveFileMoveRtl
        "DriveFileRenameOutline" -> Icons.Rounded.DriveFileRenameOutline
        "DriveFolderUpload" -> Icons.Rounded.DriveFolderUpload
        "FileDownload" -> Icons.Rounded.FileDownload
        "FileDownloadDone" -> Icons.Rounded.FileDownloadDone
        "FileDownloadOff" -> Icons.Rounded.FileDownloadOff
        "FileOpen" -> Icons.Rounded.FileOpen
        "AutoMode" -> Icons.Rounded.AutoMode
        "Blinds" -> Icons.Rounded.Blinds
        "BlindsClosed" -> Icons.Rounded.BlindsClosed
        "BroadcastOnHome" -> Icons.Rounded.BroadcastOnHome
        "BroadcastOnPersonal" -> Icons.Rounded.BroadcastOnPersonal
        "Curtains" -> Icons.Rounded.Curtains
        "CurtainsClosed" -> Icons.Rounded.CurtainsClosed
        "ElectricBolt" -> Icons.Rounded.ElectricBolt
        "ElectricMeter" -> Icons.Rounded.ElectricMeter
        "EnergySavingsLeaf" -> Icons.Rounded.EnergySavingsLeaf
        "GasMeter" -> Icons.Rounded.GasMeter
        "HeatPump" -> Icons.Rounded.HeatPump
        "ModeFanOff" -> Icons.Rounded.ModeFanOff
        "NestCamWiredStand" -> Icons.Rounded.NestCamWiredStand
        "OilBarrel" -> Icons.Rounded.OilBarrel
        "Propane" -> Icons.Rounded.Propane
        "PropaneTank" -> Icons.Rounded.PropaneTank
        "RollerShades" -> Icons.Rounded.RollerShades
        "RollerShadesClosed" -> Icons.Rounded.RollerShadesClosed
        "SensorDoor" -> Icons.Rounded.SensorDoor
        "SensorOccupied" -> Icons.Rounded.SensorOccupied
        "SensorWindow" -> Icons.Rounded.SensorWindow
        "ShieldMoon" -> Icons.Rounded.ShieldMoon
        "SolarPower" -> Icons.Rounded.SolarPower
        "VerticalShades" -> Icons.Rounded.VerticalShades
        "Bathroom" -> Icons.Rounded.Bathroom
        "Bed" -> Icons.Rounded.Bed
        "BedroomBaby" -> Icons.Rounded.BedroomBaby
        "BedroomChild" -> Icons.Rounded.BedroomChild
        "BedroomParent" -> Icons.Rounded.BedroomParent
        "Blender" -> Icons.Rounded.Blender
        "CameraIndoor" -> Icons.Rounded.CameraIndoor
        "CameraOutdoor" -> Icons.Rounded.CameraOutdoor
        "Chair" -> Icons.Rounded.Chair
        "ChairAlt" -> Icons.Rounded.ChairAlt
        "Coffee" -> Icons.Rounded.Coffee
        "CoffeeMaker" -> Icons.Rounded.CoffeeMaker
        "Dining" -> Icons.Rounded.Dining
        "DoorBack" -> Icons.Rounded.DoorBack
        "DoorFront" -> Icons.Rounded.DoorFront
        "DoorSliding" -> Icons.Rounded.DoorSliding
        "Doorbell" -> Icons.Rounded.Doorbell
        "Feed" -> Icons.Rounded.Feed
        "Flatware" -> Icons.Rounded.Flatware
        "Garage" -> Icons.Rounded.Garage
        "Light" -> Icons.Rounded.Light
        "Living" -> Icons.Rounded.Living
        "ManageSearch" -> Icons.Rounded.ManageSearch
        "Podcasts" -> Icons.Rounded.Podcasts
        "Shower" -> Icons.Rounded.Shower
        "CheckBox" -> Icons.Rounded.CheckBox
        "CheckBoxOutlineBlank" -> Icons.Rounded.CheckBoxOutlineBlank
        "IndeterminateCheckBox" -> Icons.Rounded.IndeterminateCheckBox
        "RadioButtonChecked" -> Icons.Rounded.RadioButtonChecked
        "RadioButtonUnchecked" -> Icons.Rounded.RadioButtonUnchecked
        "StarBorder" -> Icons.Rounded.StarBorder
        "StarBorderPurple500" -> Icons.Rounded.StarBorderPurple500
        "StarOutline" -> Icons.Rounded.StarOutline
        "StarPurple500" -> Icons.Rounded.StarPurple500
        "ToggleOff" -> Icons.Rounded.ToggleOff
        "ToggleOn" -> Icons.Rounded.ToggleOn
        else -> null
    }
}
