package com.example.itemmanagement

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.example.itemmanagement.ui.theme.LiquidGlassTheme
import com.example.itemmanagement.ui.main.LiquidBackground
import com.example.itemmanagement.ui.main.CapsuleBottomNavigation
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.itemmanagement.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.color.DynamicColors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var currentSystemBarTopInset: Int = 0
    
    // 🎯 跟踪TopBar当前状态，避免重复操作导致的闪现
    private var isTopBarVisible: Boolean = false
    private var isTopBarTitleEnabled: Boolean = false
    
    // Compose 状态
    private var currentNavSelection by mutableStateOf(R.id.navigation_home)
    
    // 通知权限申请器
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Snackbar.make(binding.root, "通知权限已授权", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(binding.root, "通知权限被拒绝，可能无法收到提醒通知", Snackbar.LENGTH_LONG).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 启用 Material You 动态颜色（Android 12+）
        DynamicColors.applyToActivityIfAvailable(this)
        
        // 启用 Edge-to-Edge 显示
        enableEdgeToEdgeDisplay()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 处理系统窗口内边距
        setupWindowInsets()

        // 设置 Material 3 工具栏
        setSupportActionBar(binding.toolbar)
        
        // 恢复或初始化TopBar状态
        isTopBarVisible = savedInstanceState?.getBoolean("isTopBarVisible", true) ?: true
        isTopBarTitleEnabled = savedInstanceState?.getBoolean("isTopBarTitleEnabled", true) ?: true

        // 初始化导航组件
        setupNavigation(savedInstanceState)
        
        // 处理通知点击导航
        handleNotificationNavigation()
        
        // 检查并申请通知权限
        checkAndRequestNotificationPermission()
        
        // 检查并显示版本更新日志
        checkAndShowUpdateLog()
        
        // 设置现代返回键处理
        setupBackPressedCallback()
        
        // 注意：新架构不再需要Activity级别的ViewModel和导航监听器
        // 每个Fragment都有自己独立的ViewModel，避免数据污染
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isTopBarVisible", isTopBarVisible)
        outState.putBoolean("isTopBarTitleEnabled", isTopBarTitleEnabled)
    }
    
    /**
     * 启用 Edge-to-Edge 显示
     */
    private fun enableEdgeToEdgeDisplay() {
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // 设置系统栏颜色为透明，避免白条
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            window.statusBarColor = android.graphics.Color.TRANSPARENT
        }
    }
    
    /**
     * 处理系统窗口内边距
     */
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            currentSystemBarTopInset = systemBars.top
            
            // 给AppBarLayout设置顶部内边距（当它可见时）
            if (binding.appBarLayout.visibility == android.view.View.VISIBLE) {
                binding.appBarLayout.setPadding(0, systemBars.top, 0, 0)
            }
            
            // 根据TopBar可见性动态调整Fragment容器内边距
            adjustFragmentPadding(currentSystemBarTopInset)
            
            insets
        }
    }
    
    /**
     * 设置导航组件
     */
    private fun setupNavigation(savedInstanceState: Bundle?) {
        navController = findNavController(R.id.nav_host_fragment)
        
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_warehouse,
                R.id.navigation_inventory_analysis,
                R.id.navigation_profile,
                R.id.nav_category,
                R.id.nav_attribute,
                R.id.nav_settings
            )
        )

        // 设置ActionBar
        setupActionBarWithNavController(navController, appBarConfiguration)
        
        // 设置导航监听器，动态显示/隐藏TopBar
        setupNavigationListener()
        
        // 🔧 修复主题切换后ActionBar消失的问题：手动触发当前目的地的ActionBar状态
        navController.currentDestination?.let { destination ->
            android.util.Log.d("MainActivity", "🎯 setupNavigation: 当前目的地=${resources.getResourceEntryName(destination.id)}")
            val isRecreated = savedInstanceState != null
            
            // 同步Compose导航状态
            currentNavSelection = resolveBottomNavSelection(destination.id)
            
            when (destination.id) {
                R.id.navigation_home,
                R.id.navigation_warehouse,
                R.id.navigation_inventory_analysis,
                R.id.navigation_profile,
                R.id.nav_category,
                R.id.nav_attribute,
                R.id.categoryPickerFragment,
                R.id.navigation_function -> {
                    hideTopBar()
                }
                R.id.addItemFragment -> {
                    showTopBar()
                }
                else -> {
                    showTopBar(forceRefresh = isRecreated)
                }
            }
        }
        
        // 设置 Compose 背景与底部导航栏
        setupComposeViews()
    }

    private fun setupComposeViews() {
        binding.composeBackground.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LiquidGlassTheme {
                    LiquidBackground()
                }
            }
        }

        binding.navView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LiquidGlassTheme {
                    CapsuleBottomNavigation(
                        currentSelectionId = currentNavSelection,
                        onItemSelected = { destId ->
                            if (destId == R.id.navigation_add_item) {
                                val defaultTemplateId = com.example.itemmanagement.utils.TemplatePreferences.getDefaultTemplateId(this@MainActivity)
                                val bundle = androidx.core.os.bundleOf("templateId" to defaultTemplateId)
                                navController.navigate(R.id.addItemFragment, bundle)
                            } else {
                                // 检查是否正在切换到“我的”标签页
                                val isSwitchingToProfile = destId == R.id.navigation_profile
                                
                                // 使用标准行为，但对于“我的”标签页，如果当前正在展示分类管理等子页面，则重置到首页
                                val options = androidx.navigation.NavOptions.Builder()
                                    .setLaunchSingleTop(true)
                                    .setRestoreState(!isSwitchingToProfile) // 如果是切换到“我的”，则不恢复状态，确保进入“我的”主页
                                    .setPopUpTo(navController.graph.startDestinationId, false, true)
                                    .build()
                                navController.navigate(destId, null, options)
                            }
                        },
                        onItemReselected = { destId ->
                            when (destId) {
                                R.id.navigation_home -> navController.popBackStack(R.id.navigation_home, false)
                                R.id.navigation_warehouse -> navController.popBackStack(R.id.navigation_warehouse, false)
                                R.id.navigation_inventory_analysis -> navController.popBackStack(R.id.navigation_inventory_analysis, false)
                                R.id.navigation_profile -> navController.popBackStack(R.id.navigation_profile, false)
                            }
                        },
                        onAddLongClick = {
                            vibrateDevice(50)
                            showTemplateSelectionDialog()
                        }
                    )
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    /**
     * 设置现代返回键处理
     */
    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 使用现代导航处理
                if (!navController.navigateUp(appBarConfiguration)) {
                    finish()
                }
            }
        })
    }

    /**
     * 设置导航监听器，动态显示/隐藏TopBar
     */
    private fun setupNavigationListener() {
        // 追踪上一个目的地，用于检测是否从添加/编辑页面返回
        var previousDestinationId: Int? = null
        
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // 默认显示底部导航，除非是特定页面
            binding.navView.visibility = when (destination.id) {
                R.id.addItemFragment,
                R.id.editItemFragment,
                R.id.nav_category,
                R.id.nav_attribute,
                R.id.categoryPickerFragment,
                R.id.navigation_item_detail,
                R.id.navigation_map_picker,
                R.id.navigation_map_viewer -> View.GONE
                else -> View.VISIBLE
            }

            currentNavSelection = resolveBottomNavSelection(destination.id)

            // 检查是否从添加/编辑/详情页面返回到首页，如果是则刷新首页
            if (destination.id == R.id.navigation_home && previousDestinationId != null) {
                when (previousDestinationId) {
                    R.id.addItemFragment,
                    R.id.editItemFragment,
                    R.id.navigation_item_detail -> {
                        // 获取HomeFragment并刷新数据
                        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                        val currentFragment = navHostFragment?.childFragmentManager?.primaryNavigationFragment
                        if (currentFragment is com.example.itemmanagement.ui.home.HomeFragment) {
                            currentFragment.refreshData()
                        }
                    }
                }
            }
            
            when (destination.id) {
                // 主要导航页面 - 隐藏TopBar
                R.id.navigation_home,
                R.id.navigation_warehouse,
                R.id.navigation_inventory_analysis,
                R.id.navigation_profile,
                R.id.nav_category,
                R.id.nav_attribute,
                R.id.categoryPickerFragment -> {
                    hideTopBar()
                }
                // 功能页面 - 隐藏TopBar（像首页一样）
                R.id.navigation_function -> {
                    hideTopBar()
                }
                // 添加物品页面 - 显示TopBar
                R.id.addItemFragment -> {
                    showTopBar()
                }
                // 地图查看页面 - 显示TopBar，Fragment自己会隐藏底部导航
                R.id.navigation_map_viewer -> {
                    showTopBar()
                }
                // 地图选点页面 - 显示TopBar，Fragment自己会隐藏底部导航
                R.id.navigation_map_picker -> {
                    showTopBar()
                }
                // 其他页面 - 显示TopBar
                else -> {
                    showTopBar()
                }
            }
            
            // 记录当前目的地，作为下次的previous
            previousDestinationId = destination.id
        }
    }

    private fun resolveBottomNavSelection(destinationId: Int): Int {
        return when (destinationId) {
            R.id.navigation_home,
            R.id.navigation_item_list,
            R.id.navigation_shopping_list,
            R.id.navigation_shopping_list_management,
            R.id.navigation_template_management,
            R.id.navigation_add_shopping_item,
            R.id.navigation_shopping_item_detail,
            R.id.navigation_edit_shopping_item,
            R.id.navigation_transfer_to_inventory_fullscreen,
            R.id.addItemFragment,
            R.id.editItemFragment,
            R.id.categoryPickerFragment,
            R.id.navigation_item_detail,
            R.id.navigation_map_picker,
            R.id.navigation_map_viewer -> R.id.navigation_home

            R.id.navigation_warehouse -> R.id.navigation_warehouse

            R.id.navigation_inventory_analysis,
            R.id.navigation_item_calendar,
            R.id.navigation_reminder_settings -> R.id.navigation_inventory_analysis

            R.id.navigation_profile,
            R.id.navigation_app_settings,
            R.id.navigation_edit_profile,
            R.id.navigation_recycle_bin,
            R.id.navigation_donation,
            R.id.navigation_about_app,
            R.id.nav_category,
            R.id.nav_attribute -> R.id.navigation_profile

            else -> currentNavSelection
        }
    }
    
    private fun visibilityToString(visibility: Int): String {
        return when (visibility) {
            android.view.View.VISIBLE -> "VISIBLE"
            android.view.View.INVISIBLE -> "INVISIBLE"
            android.view.View.GONE -> "GONE"
            else -> "UNKNOWN($visibility)"
        }
    }
    
    /**
     * 显示TopBar
     */
    private fun showTopBar(forceRefresh: Boolean = false) {
        android.util.Log.d("MainActivity", "👁️ showTopBar called, 当前状态: visible=$isTopBarVisible, forceRefresh=$forceRefresh")
        // ✅ 只在状态发生变化时才执行操作，避免重复导致的闪现
        if (!isTopBarVisible || forceRefresh) {
            if (forceRefresh) {
                android.util.Log.d("MainActivity", "  🔄 强制刷新TopBar状态")
            } else {
                android.util.Log.d("MainActivity", "  ✅ TopBar从隐藏变为可见")
            }
            binding.appBarLayout.visibility = android.view.View.VISIBLE
            isTopBarVisible = true
            updateFragmentConstraints(true)
            adjustFragmentPadding(currentSystemBarTopInset)
            
            // 🔧 强制显示ActionBar（修复Activity重建后ActionBar消失的问题）
            supportActionBar?.show()
        } else {
            android.util.Log.d("MainActivity", "  ⏭️ TopBar已经可见，跳过")
        }
        
        // 标题状态单独管理
        if (!isTopBarTitleEnabled || forceRefresh) {
            android.util.Log.d("MainActivity", "  📝 启用TopBar标题")
            supportActionBar?.setDisplayShowTitleEnabled(true)
            isTopBarTitleEnabled = true
        }
        
        // 恢复默认背景色（从透明状态恢复）
        restoreDefaultTopBarBackground()
    }
    
    /**
     * 显示TopBar并设置标题
     */
    private fun showTopBarWithTitle(title: String) {
        // ✅ 只在状态发生变化时才执行操作，避免重复导致的闪现
        if (!isTopBarVisible) {
            binding.appBarLayout.visibility = android.view.View.VISIBLE
            isTopBarVisible = true
            updateFragmentConstraints(true)
            adjustFragmentPadding(currentSystemBarTopInset)
        }
        
        // 标题状态和内容管理
        if (!isTopBarTitleEnabled) {
            supportActionBar?.setDisplayShowTitleEnabled(true)
            isTopBarTitleEnabled = true
        }
        supportActionBar?.title = title
        
        // 恢复默认背景色（从透明状态恢复）
        restoreDefaultTopBarBackground()
    }
    
    /**
     * 显示透明TopBar并设置标题（专用于功能界面）
     */
    private fun showTransparentTopBarWithTitle(title: String) {
        // ✅ 只在状态发生变化时才执行操作，避免重复导致的闪现
        if (!isTopBarVisible) {
            binding.appBarLayout.visibility = android.view.View.VISIBLE
            isTopBarVisible = true
            updateFragmentConstraints(true)
            adjustFragmentPadding(currentSystemBarTopInset)
        }
        
        // 标题状态和内容管理
        if (!isTopBarTitleEnabled) {
            supportActionBar?.setDisplayShowTitleEnabled(true)
            isTopBarTitleEnabled = true
        }
        supportActionBar?.title = title
        
        // 设置透明背景 - 只影响当前TopBar
        binding.toolbar.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        binding.appBarLayout.setBackgroundColor(android.graphics.Color.TRANSPARENT)
    }
    
    /**
     * 恢复TopBar的默认背景色
     */
    private fun restoreDefaultTopBarBackground() {
        // 恢复Toolbar的默认背景色（从样式中获取）
        val typedArray = theme.obtainStyledAttributes(intArrayOf(R.attr.colorSurfaceContainerHigh))
        val defaultColor = typedArray.getColor(0, android.graphics.Color.WHITE)
        typedArray.recycle()
        
        binding.toolbar.setBackgroundColor(defaultColor)
        binding.appBarLayout.setBackgroundColor(defaultColor)
    }
    
    /**
     * 显示TopBar但禁用标题（用于添加物品页面）
     */
    private fun showTopBarWithoutTitle() {
        // ✅ 只在TopBar不可见时才显示，避免重复操作
        if (!isTopBarVisible) {
            binding.appBarLayout.visibility = android.view.View.VISIBLE
            isTopBarVisible = true
            updateFragmentConstraints(true)
            adjustFragmentPadding(currentSystemBarTopInset)
        }
        
        // 标题状态管理
        if (isTopBarTitleEnabled) {
            supportActionBar?.setDisplayShowTitleEnabled(false)
            isTopBarTitleEnabled = false
        }
        // 立即清空标题，防止闪现
        supportActionBar?.title = ""
        
        // 恢复默认背景色（从透明状态恢复）
        restoreDefaultTopBarBackground()
    }
    
    /**
     * 隐藏TopBar
     */
    private fun hideTopBar() {
        android.util.Log.d("MainActivity", "🙈 hideTopBar called, 当前状态: visible=$isTopBarVisible")
        // ✅ 只在TopBar可见时才隐藏，避免重复操作导致的闪现
        if (isTopBarVisible) {
            android.util.Log.d("MainActivity", "  ✅ TopBar从可见变为隐藏")
            // 立即清空标题，防止隐藏过程中的闪现
            supportActionBar?.title = ""
            supportActionBar?.setDisplayShowTitleEnabled(false)
            binding.appBarLayout.visibility = android.view.View.GONE
            
            // 更新状态
            isTopBarVisible = false
            isTopBarTitleEnabled = false
            
            // 重新调整Fragment约束
            updateFragmentConstraints(false)
            adjustFragmentPadding(currentSystemBarTopInset)
        } else {
            android.util.Log.d("MainActivity", "  ⏭️ TopBar已经隐藏，跳过")
        }
    }

    /**
     * 更新Fragment约束
     */
    private fun updateFragmentConstraints(showTopBar: Boolean) {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val fragmentView = navHostFragment?.view
        val layoutParams = fragmentView?.layoutParams as? androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        layoutParams?.let { params ->
            if (showTopBar) {
                params.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
                params.topToBottom = R.id.appBarLayout
            } else {
                params.topToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
                params.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            }
            fragmentView.layoutParams = params
        }
    }
    
    /**
     * 动态调整Fragment容器内边距
     */
    private fun adjustFragmentPadding(statusBarHeight: Int) {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val topPadding = when {
            binding.appBarLayout.visibility == android.view.View.VISIBLE -> 0
            shouldFragmentManageStatusBarInset() -> 0
            else -> statusBarHeight
        }
        navHostFragment?.view?.setPadding(0, topPadding, 0, 0)
    }

    private fun shouldFragmentManageStatusBarInset(): Boolean {
        if (!::navController.isInitialized) {
            return false
        }
        return when (navController.currentDestination?.id) {
            R.id.nav_category,
            R.id.categoryPickerFragment -> true
            else -> false
        }
    }

    /**
     * 处理通知点击导航
     */
    private fun handleNotificationNavigation() {
        val navigateTo = intent.getStringExtra("navigate_to")
        if (navigateTo == "expiration_reminder") {
            // 导航到事件日历页面
            navController.navigate(R.id.navigation_item_calendar)
        }
    }
    
    /**
     * 检查并申请通知权限
     */
    fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 权限已获取，无需操作
                }
                else -> {
                    // 申请权限
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
    
    /**
     * 检查并显示版本更新日志
     */
    private fun checkAndShowUpdateLog() {
        // 延迟1秒显示，确保界面已完全加载
        binding.root.postDelayed({
            if (!isFinishing && !isDestroyed) {
                // 先检查在线更新
                checkOnlineUpdate()
                
                // 然后检查本地更新日志（首次安装或更新后）
                val shouldShow = com.example.itemmanagement.utils.VersionUpdateManager.shouldShowUpdateDialog(this)
                if (shouldShow) {
                    val dialog = com.example.itemmanagement.ui.dialog.UpdateLogDialog.newInstance(isFirstLaunch = true)
                    dialog.show(supportFragmentManager, "UpdateLogDialog")
                }
            }
        }, 1000)
    }
    
    /**
     * 检查在线更新
     */
    private fun checkOnlineUpdate() {
        lifecycleScope.launch {
            try {
                val updateInfo = com.example.itemmanagement.utils.OnlineUpdateChecker.checkForUpdate(this@MainActivity)
                if (updateInfo != null) {
                    if (!isFinishing && !isDestroyed) {
                        // 发现新版本，显示更新对话框
                        val dialog = com.example.itemmanagement.ui.dialog.OnlineUpdateDialog.newInstance(updateInfo)
                        dialog.show(supportFragmentManager, "OnlineUpdateDialog")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "检查在线更新失败", e)
                // 静默失败，不影响用户体验
            }
        }
    }
    
    /**
     * 检查通知权限是否已获取
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 13以下版本不需要权限
        }
    }
    
    /**
     * 震动反馈
     */
    private fun vibrateDevice(milliseconds: Long) {
        try {
            val vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    android.os.VibrationEffect.createOneShot(
                        milliseconds,
                        android.os.VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(milliseconds)
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "震动失败", e)
        }
    }

    /**
     * 显示模板选择对话框
     */
    private fun showTemplateSelectionDialog() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val currentFragment = navHostFragment?.childFragmentManager?.fragments?.firstOrNull()
        
        // 创建模板选择对话框
        val dialog = com.example.itemmanagement.ui.template.TemplateSelectionBottomSheet(
            onTemplateSelected = { template ->
                // 用户选择了模板，跳转到添加界面并传递模板ID
                val bundle = androidx.core.os.bundleOf("templateId" to template.id)
                navController.navigate(R.id.addItemFragment, bundle)
            },
            onManageTemplates = {
                // 跳转到模板管理界面
                try {
                    navController.navigate(R.id.action_home_to_template_management)
                } catch (e: Exception) {
                    // 如果当前不在home，直接导航到模板管理
                    navController.navigate(R.id.navigation_template_management)
                }
            }
        )
        
        // 显示对话框
        currentFragment?.childFragmentManager?.let {
            dialog.show(it, "TemplateSelection")
        } ?: run {
            // 如果无法获取当前Fragment，使用Activity的FragmentManager
            dialog.show(supportFragmentManager, "TemplateSelection")
        }
    }
    
    /**
     * 每次打开APP显示气泡提示（除非用户点击了"不再显示"）
     */
    private fun showFirstTimeTipIfNeeded() {
        // Compose bottom nav currently doesn't easily support attaching popups to specific items without layout coordinates.
        // We'll skip this for now or rewrite it as a Compose tooltip later.
    }
} 
