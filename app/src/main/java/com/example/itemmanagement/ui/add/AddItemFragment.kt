package com.example.itemmanagement.ui.add

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.itemmanagement.ItemManagementApplication
import com.example.itemmanagement.R
import com.example.itemmanagement.data.AppDatabase
import com.example.itemmanagement.data.entity.template.ItemTemplateEntity
import com.example.itemmanagement.data.entity.unified.CustomAttributeDefinitionEntity
import com.example.itemmanagement.data.model.template.TemplateFieldDefaults
import com.example.itemmanagement.data.repository.ItemTemplateRepository
import com.example.itemmanagement.ui.base.ItemStateCacheViewModel
import com.example.itemmanagement.ui.categorypicker.CategoryPickerFragment
import com.example.itemmanagement.ui.common.FieldProperties
import com.example.itemmanagement.ui.common.ValidationType
import com.example.itemmanagement.ui.template.TemplateSelectionBottomSheet
import com.example.itemmanagement.ui.theme.LiquidGlassTheme
import com.example.itemmanagement.utils.combineCategoryPath
import com.example.itemmanagement.utils.SnackbarHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddItemFragment : Fragment() {

    private val cacheViewModel: ItemStateCacheViewModel by lazy {
        ViewModelProvider(requireActivity())[ItemStateCacheViewModel::class.java]
    }

    private val viewModel: AddItemViewModel by viewModels {
        val app = (requireActivity().application as ItemManagementApplication)
        AddItemViewModelFactory(app.repository, cacheViewModel, app.warrantyRepository)
    }

    private var currentTemplateId: Long = -1L
    private var currentTemplate by mutableStateOf<ItemTemplateEntity?>(null)
    private var customAttributeDefinitions by mutableStateOf<List<CustomAttributeDefinitionEntity>>(emptyList())

    private var currentPhotoUri: Uri? = null
    private var currentPhotoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LiquidGlassTheme {
                    AddItemScreen(
                        viewModel = viewModel,
                        selectedTemplate = currentTemplate,
                        customAttributeDefinitions = customAttributeDefinitions,
                        onChooseTemplate = { showTemplateSelectionDialog() },
                        onEditFields = { showEditFieldsDialog() },
                        onPickPhoto = { checkAndRequestStoragePermission() },
                        onTakePhoto = { checkAndRequestCameraPermission() },
                        onRemovePhoto = { viewModel.removePhotoUri(it) },
                        onShowCategoryPicker = { openCategoryPicker() },
                        onSave = { viewModel.performSave() }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureActionBar()
        hideBottomNavigation()
        observeCategoryPickerResult()
        observeViewModel()
        initializeScreen()
    }

    override fun onResume() {
        super.onResume()
        hideBottomNavigation()
        configureActionBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        configureActionBar()
        showBottomNavigation()
        restoreActionBar()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun configureActionBar() {
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.let { actionBar ->
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close)
            actionBar.title = "新增物品"
        }
    }

    private fun restoreActionBar() {
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.let { actionBar ->
            actionBar.setDisplayHomeAsUpEnabled(false)
            actionBar.setHomeAsUpIndicator(null)
            actionBar.title = ""
        }
    }

    private fun observeCategoryPickerResult() {
        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>(CategoryPickerFragment.RESULT_KEY)
            ?.observe(viewLifecycleOwner) { selectedCategoryPath ->
                if (selectedCategoryPath.isNullOrBlank()) {
                    return@observe
                }
                viewModel.saveFieldValue("分类", selectedCategoryPath)
                viewModel.clearFieldValue("子分类")
                findNavController().currentBackStackEntry
                    ?.savedStateHandle
                    ?.remove<String>(CategoryPickerFragment.RESULT_KEY)
            }
    }

    private fun openCategoryPicker() {
        findNavController().navigate(
            R.id.categoryPickerFragment,
            CategoryPickerFragment.args(viewModel.getFieldValue("分类") as? String)
        )
    }

    private fun initializeScreen() {
        currentTemplateId = resolveRequestedTemplateId()
        val hasCache = cacheViewModel.hasAddItemCache()
        val cache = cacheViewModel.getAddItemCache()

        ensureDefaultDate()

        val sourceType = arguments?.getString("sourceType")
        val sourceItemId = arguments?.getLong("sourceItemId", -1L) ?: -1L
        if (!hasCache && sourceType == "SHOPPING_LIST" && sourceItemId > 0) {
            viewModel.loadFromShoppingList(sourceItemId)
        }

        when {
            currentTemplateId > 0 -> evaluateTemplateApplication(cache, hasCache)
            hasCache -> {
                currentTemplate = null
                customAttributeDefinitions = emptyList()
                viewModel.saveFieldValue("模板ID", null)
            }

            else -> applyDefaultFieldSelection()
        }
    }

    private fun resolveRequestedTemplateId(): Long {
        val argumentTemplateId = arguments?.getLong("templateId", -1L) ?: -1L
        if (argumentTemplateId > 0) {
            return argumentTemplateId
        }

        return when (val cachedTemplateId = viewModel.getFieldValue("模板ID")) {
            is Long -> cachedTemplateId
            is Int -> cachedTemplateId.toLong()
            is String -> cachedTemplateId.toLongOrNull() ?: -1L
            else -> -1L
        }
    }

    private fun observeViewModel() {
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank() && view != null) {
                SnackbarHelper.showError(requireView(), message)
            }
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { success ->
            success?.let {
                if (it) {
                    showSaveSuccessDialog()
                }
                viewModel.onSaveResultConsumed()
            }
        }
    }

    private fun showSaveSuccessDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("保存成功")
            .setMessage("物品已成功加入记录。是否继续新增下一件？")
            .setNegativeButton("返回") { _, _ ->
                findNavController().navigateUp()
            }
            .setPositiveButton("继续添加") { _, _ ->
                prepareNextItem()
            }
            .show()
    }

    private fun prepareNextItem() {
        if (currentTemplateId > 0 && currentTemplate != null) {
            applyTemplate(
                templateId = currentTemplateId,
                preloadedTemplate = currentTemplate,
                preloadedSignature = buildTemplateSignature(currentTemplate!!),
                incrementUsage = false,
                showMessage = false
            )
        } else {
            applyDefaultFieldSelection()
        }

        if (view != null) {
            SnackbarHelper.showSuccess(requireView(), "已准备好继续添加下一件物品")
        }
    }

    private fun showClearConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("清空当前内容")
            .setMessage("会清空已填写的内容与图片，但保留当前模板和字段布局。")
            .setNegativeButton("取消", null)
            .setPositiveButton("清空") { _, _ ->
                if (currentTemplateId > 0 && currentTemplate != null) {
                    applyTemplate(
                        templateId = currentTemplateId,
                        preloadedTemplate = currentTemplate,
                        preloadedSignature = buildTemplateSignature(currentTemplate!!),
                        incrementUsage = false,
                        showMessage = false
                    )
                } else {
                    applyDefaultFieldSelection()
                }
            }
            .show()
    }

    private fun showEditFieldsDialog() {
        EditFieldsFragment.newInstance(viewModel, false)
            .show(childFragmentManager, "EditFieldsFragment")
    }

    private fun showTemplateSelectionDialog() {
        TemplateSelectionBottomSheet(
            onTemplateSelected = { template ->
                if (template.id > 0) {
                    viewModel.clearStateAndCache()
                    applyTemplate(
                        templateId = template.id,
                        preloadedTemplate = template,
                        preloadedSignature = buildTemplateSignature(template)
                    )
                } else {
                    currentTemplateId = -1L
                    currentTemplate = null
                    customAttributeDefinitions = emptyList()
                    applyDefaultFieldSelection()
                }
            },
            onManageTemplates = {
                findNavController().navigate(R.id.navigation_template_management)
            }
        ).show(childFragmentManager, "TemplateSelection")
    }

    private fun applyDefaultFieldSelection() {
        currentTemplateId = -1L
        currentTemplate = null
        customAttributeDefinitions = emptyList()
        viewModel.clearStateAndCache()
        viewModel.setSelectedFields(defaultFields().toSet())
        viewModel.saveFieldValue("模板ID", null)
        ensureDefaultDate()
    }

    private fun defaultFields(): List<Field> {
        return listOf(
            Field("基础信息", "名称", true),
            Field("基础信息", "分类", true),
            Field("基础信息", "数量", true),
            Field("补充信息", "状态", true),
            Field("补充信息", "标签", true)
        )
    }

    private fun ensureDefaultDate() {
        if ((viewModel.getFieldValue("添加日期") as? String).isNullOrBlank()) {
            viewModel.saveFieldValue(
                "添加日期",
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
        }
    }

    private fun evaluateTemplateApplication(
        cache: ItemStateCacheViewModel.AddItemCache,
        hasCache: Boolean
    ) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val database = AppDatabase.getDatabase(requireContext())
                val repository = ItemTemplateRepository(database.itemTemplateDao())
                val template = repository.getTemplateById(currentTemplateId)
                if (template == null) {
                    withContext(Dispatchers.Main) {
                        if (!hasCache && view != null) {
                            SnackbarHelper.showError(requireView(), "未找到所选模板，已切换为通用录入")
                        }
                        applyDefaultFieldSelection()
                    }
                    return@launch
                }

                val templateSignature = buildTemplateSignature(template)
                val shouldReapply = !hasCache || cache.lastTemplateSignature != templateSignature

                withContext(Dispatchers.Main) {
                    if (shouldReapply) {
                        viewModel.clearStateAndCache()
                        applyTemplate(
                            templateId = template.id,
                            preloadedTemplate = template,
                            preloadedSignature = templateSignature,
                            incrementUsage = false,
                            showMessage = false
                        )
                    } else {
                        loadTemplateMetadata(template)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AddItemFragment", "加载模板失败", e)
                withContext(Dispatchers.Main) {
                    if (!hasCache && view != null) {
                        SnackbarHelper.showError(requireView(), "加载模板失败，请稍后重试")
                    }
                    if (!hasCache) {
                        applyDefaultFieldSelection()
                    }
                }
            }
        }
    }

    private fun applyTemplate(
        templateId: Long,
        preloadedTemplate: ItemTemplateEntity? = null,
        preloadedSignature: String? = null,
        incrementUsage: Boolean = true,
        showMessage: Boolean = true
    ) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val database = AppDatabase.getDatabase(requireContext())
                val repository = ItemTemplateRepository(database.itemTemplateDao())
                val template = preloadedTemplate ?: repository.getTemplateById(templateId)

                if (template == null) {
                    withContext(Dispatchers.Main) {
                        if (view != null) {
                            SnackbarHelper.showError(requireView(), "无法应用模板")
                        }
                        applyDefaultFieldSelection()
                    }
                    return@launch
                }

                if (incrementUsage) {
                    repository.useTemplate(template.id)
                }

                val definitions = loadCustomAttributeDefinitions(template, database)

                withContext(Dispatchers.Main) {
                    currentTemplateId = template.id
                    currentTemplate = template
                    customAttributeDefinitions = definitions

                    val fields = mutableSetOf<Field>()
                    fields.add(Field("基础信息", "名称", true))
                    template.selectedFields
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .filterNot { it == "子分类" }
                        .forEach { fieldName ->
                            fields.add(
                                Field(
                                    group = resolveFieldGroup(fieldName),
                                    name = fieldName,
                                    isSelected = true
                                )
                            )
                        }

                    definitions.forEachIndexed { index, definition ->
                        val fieldName = customFieldName(definition)
                        viewModel.setFieldProperties(
                            fieldName,
                            FieldProperties(
                                validationType = when (definition.type) {
                                    CustomAttributeDefinitionEntity.TYPE_DATE -> ValidationType.DATE
                                    CustomAttributeDefinitionEntity.TYPE_PRICE,
                                    CustomAttributeDefinitionEntity.TYPE_NUMBER -> ValidationType.NUMBER
                                    else -> ValidationType.TEXT
                                },
                                hint = "请输入${definition.name}",
                                unit = definition.unit,
                                unitOptions = definition.unit?.let { listOf(it) },
                                isCustomizable = false
                            )
                        )
                        viewModel.saveFieldValue("custom_meta_${definition.id}_type", definition.type)
                        if (viewModel.getFieldValue("custom_include_${definition.id}") == null) {
                            viewModel.saveFieldValue("custom_include_${definition.id}", true)
                        }
                        fields.add(
                            Field(
                                group = "补充信息",
                                name = fieldName,
                                isSelected = true,
                                order = 1000 + index
                            )
                        )
                    }

                    viewModel.setSelectedFields(fields)
                    ensureDefaultDate()
                    applyTemplateDefaultValues(template.fieldDefaultValues)
                    viewModel.saveFieldValue("模板ID", template.id)
                    cacheViewModel.getAddItemCache().lastTemplateSignature =
                        preloadedSignature ?: buildTemplateSignature(template)

                    if (showMessage && view != null) {
                        SnackbarHelper.showSuccess(requireView(), "已应用模板：${template.templateName}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AddItemFragment", "应用模板失败", e)
                withContext(Dispatchers.Main) {
                    if (view != null) {
                        SnackbarHelper.showError(requireView(), "应用模板失败")
                    }
                }
            }
        }
    }

    private suspend fun loadCustomAttributeDefinitions(
        template: ItemTemplateEntity,
        database: AppDatabase
    ): List<CustomAttributeDefinitionEntity> {
        if (template.customAttributeIds.isNullOrBlank()) {
            return emptyList()
        }

        return try {
            val type = object : com.google.gson.reflect.TypeToken<List<Long>>() {}.type
            val ids: List<Long> = com.google.gson.Gson().fromJson(template.customAttributeIds, type)
                ?: emptyList()
            ids.mapNotNull { database.customAttributeDefinitionDao().getDefinitionById(it) }
        } catch (e: Exception) {
            android.util.Log.e("AddItemFragment", "解析模板高级属性失败", e)
            emptyList()
        }
    }

    private fun loadTemplateMetadata(template: ItemTemplateEntity) {
        currentTemplateId = template.id
        currentTemplate = template
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val definitions = loadCustomAttributeDefinitions(template, AppDatabase.getDatabase(requireContext()))
            withContext(Dispatchers.Main) {
                customAttributeDefinitions = definitions
                definitions.forEach { definition ->
                    viewModel.saveFieldValue("custom_meta_${definition.id}_type", definition.type)
                }
            }
        }
    }

    private fun applyTemplateDefaultValues(fieldDefaultsJson: String?) {
        val defaults = TemplateFieldDefaults.fromJson(fieldDefaultsJson) ?: return
        val resolvedCategory = viewModel.resolveTemplateSingleValue("分类", defaults.singleValues["分类"])
        val resolvedSubCategory = viewModel.resolveTemplateSingleValue(
            "子分类",
            defaults.singleValues["子分类"],
            resolvedCategory
        )
        val combinedCategoryPath = combineCategoryPath(resolvedCategory, resolvedSubCategory)

        if (combinedCategoryPath.isNotBlank()) {
            viewModel.saveFieldValue("分类", combinedCategoryPath)
            viewModel.clearFieldValue("子分类")
        }

        defaults.singleValues.forEach { (field, value) ->
            if (field == "分类" || field == "子分类") {
                return@forEach
            }
            val resolvedValue = viewModel.resolveTemplateSingleValue(field, value)
            if (!resolvedValue.isNullOrBlank()) {
                viewModel.saveFieldValue(field, resolvedValue)
            }
        }

        defaults.multiValues["标签"]?.let { tags ->
            val resolvedTags = viewModel.resolveTemplateMultiValues("标签", tags)
            val tagSet = resolvedTags.filter { it.isNotBlank() }.toSet()
            if (tagSet.isNotEmpty()) {
                viewModel.updateSelectedTags("标签", tagSet)
                viewModel.saveFieldValue("标签", tagSet)
            }
        }
    }

    private fun resolveFieldGroup(fieldName: String): String {
        return when (fieldName) {
            "名称", "分类", "数量", "品牌", "规格" -> "基础信息"
            "状态", "标签", "单价", "总价", "币种", "购买日期", "购买渠道", "商家名称",
            "备注", "位置", "地点", "序列号", "容量", "评分", "生产日期", "保质期",
            "保质过期时间", "保修期", "保修到期时间", "订阅制", "自动续费", "扣费周期",
            "开封状态", "季节" -> "补充信息"
            "不计入总价值", "不计入总数量" -> "补充信息"
            else -> "补充信息"
        }
    }

    private fun customFieldName(definition: CustomAttributeDefinitionEntity): String {
        return "custom_${definition.id}_${definition.name}"
    }

    private fun buildTemplateSignature(template: ItemTemplateEntity): String {
        return "${template.id}:${template.selectedFields}:${template.fieldDefaultValues ?: ""}:${template.lastUsedTime}"
    }

    private fun hideBottomNavigation() {
        activity?.findViewById<View>(R.id.nav_view)?.visibility = View.GONE
    }

    private fun showBottomNavigation() {
        activity?.findViewById<View>(R.id.nav_view)?.visibility = View.VISIBLE
    }

    private fun checkAndRequestCameraPermission() {
        val cameraPermission = Manifest.permission.CAMERA
        when {
            ContextCompat.checkSelfPermission(requireContext(), cameraPermission) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }

            shouldShowRequestPermissionRationale(cameraPermission) -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("需要相机权限")
                    .setMessage("拍照录入需要使用相机权限。")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("继续") { _, _ ->
                        requestCameraPermission.launch(cameraPermission)
                    }
                    .show()
            }

            else -> requestCameraPermission.launch(cameraPermission)
        }
    }

    private fun checkAndRequestStoragePermission() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }

            shouldShowRequestPermissionRationale(permission) -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("需要相册权限")
                    .setMessage("从相册添加图片需要读取图片权限。")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("继续") { _, _ ->
                        requestStoragePermission.launch(permission)
                    }
                    .show()
            }

            else -> requestStoragePermission.launch(permission)
        }
    }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchCamera()
            } else if (view != null) {
                SnackbarHelper.showError(requireView(), "未获得相机权限")
            }
        }

    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openGallery()
            } else if (view != null) {
                SnackbarHelper.showError(requireView(), "未获得图片读取权限")
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                currentPhotoUri?.let { uri ->
                    lifecycleScope.launch {
                        val compressedUri = withContext(Dispatchers.IO) { compressImage(uri) }
                        if (compressedUri != null) {
                            viewModel.addPhotoUri(compressedUri)
                            if (view != null) {
                                SnackbarHelper.showSuccess(requireView(), "照片已添加")
                            }
                        } else if (view != null) {
                            SnackbarHelper.showError(requireView(), "图片处理失败")
                        }
                    }
                }
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    lifecycleScope.launch {
                        val compressedUri = withContext(Dispatchers.IO) { compressImage(uri) }
                        if (compressedUri != null) {
                            viewModel.addPhotoUri(compressedUri)
                            if (view != null) {
                                SnackbarHelper.showSuccess(requireView(), "照片已添加")
                            }
                        } else if (view != null) {
                            SnackbarHelper.showError(requireView(), "图片处理失败")
                        }
                    }
                }
            }
        }

    private fun launchCamera() {
        try {
            currentPhotoFile = createTempImageFile("CAMERA")
            currentPhotoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                currentPhotoFile!!
            )

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            cameraLauncher.launch(intent)
        } catch (e: Exception) {
            if (view != null) {
                SnackbarHelper.showError(requireView(), "无法启动相机：${e.message}")
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        galleryLauncher.launch(intent)
    }

    private fun createTempImageFile(prefix: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "${prefix}_${timeStamp}"
        val storageDir = requireContext().getExternalFilesDir("Photos")
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    private fun compressImage(uri: Uri): Uri? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }

            val maxDimension = 1280
            var sampleSize = 1
            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                val heightRatio = Math.round(options.outHeight.toFloat() / maxDimension.toFloat())
                val widthRatio = Math.round(options.outWidth.toFloat() / maxDimension.toFloat())
                sampleSize = if (heightRatio < widthRatio) widthRatio else heightRatio
            }

            val compressOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmap = requireContext().contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, compressOptions)
            } ?: return null

            val compressedFile = createTempImageFile("COMPRESSED")
            FileOutputStream(compressedFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 82, out)
            }
            bitmap.recycle()

            FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                compressedFile
            )
        } catch (_: Exception) {
            null
        }
    }
}
