package com.example.itemmanagement.ui.edit

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.navigation.fragment.navArgs
import com.example.itemmanagement.ItemManagementApplication
import com.example.itemmanagement.R
import com.example.itemmanagement.data.entity.unified.CustomAttributeDefinitionEntity
import com.example.itemmanagement.ui.base.ItemStateCacheViewModel
import com.example.itemmanagement.ui.categorypicker.CategoryPickerFragment
import com.example.itemmanagement.ui.theme.LiquidGlassTheme
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

class EditItemFragment : Fragment() {

    private val args: EditItemFragmentArgs by navArgs()

    private val cacheViewModel: ItemStateCacheViewModel by lazy {
        ViewModelProvider(requireActivity())[ItemStateCacheViewModel::class.java]
    }

    private val viewModel: EditItemViewModel by viewModels {
        val app = requireActivity().application as ItemManagementApplication
        EditItemViewModelFactory(app.repository, cacheViewModel, args.itemId, app.warrantyRepository)
    }

    private var customAttributeDefinitions by mutableStateOf<List<CustomAttributeDefinitionEntity>>(emptyList())
    private var currentPhotoUri: Uri? = null
    private var currentPhotoFile: File? = null

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LiquidGlassTheme {
                    EditItemScreen(
                        viewModel = viewModel,
                        customAttributeDefinitions = customAttributeDefinitions,
                        onPickPhoto = { checkAndRequestStoragePermission() },
                        onTakePhoto = { checkAndRequestCameraPermission() },
                        onRemovePhoto = { viewModel.removePhotoUri(it) },
                        onShowCategoryPicker = { openCategoryPicker() },
                        onNavigateBack = { handleNavigateBack() },
                        onRequestDelete = { showDeleteConfirmDialog() },
                        onSave = { viewModel.performSave() }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideBottomNavigation()
        hideActionBar()
        observeCategoryPickerResult()
        observeViewModel()
        loadCustomAttributeDefinitions()
    }

    override fun onResume() {
        super.onResume()
        hideBottomNavigation()
        hideActionBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showBottomNavigation()
        showActionBar()
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

        viewModel.deleteResult.observe(viewLifecycleOwner) { success ->
            success?.let {
                if (it) {
                    showDeleteResultDialog()
                } else if (view != null) {
                    SnackbarHelper.showError(requireView(), "删除失败")
                }
                viewModel.onDeleteResultConsumed()
            }
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

    private fun loadCustomAttributeDefinitions() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val app = requireActivity().application as ItemManagementApplication
                val definitions = app.repository.getAllCustomAttributeDefinitions()
                withContext(Dispatchers.Main) {
                    customAttributeDefinitions = definitions
                    viewModel.bindCustomAttributeDefinitions(definitions)
                }
            } catch (e: Exception) {
                android.util.Log.e("EditItemFragment", "加载高级自定义属性失败", e)
            }
        }
    }

    private fun handleNavigateBack() {
        if (viewModel.hasUnsavedChanges.value == true) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("确认退出")
                .setMessage("当前还有未保存的修改，确定要返回吗？修改会保留为草稿。")
                .setNegativeButton("继续编辑", null)
                .setPositiveButton("退出") { _, _ ->
                    findNavController().navigateUp()
                }
                .show()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun showDeleteConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("确认删除")
            .setMessage("确定要删除这个物品吗？")
            .setNegativeButton("取消", null)
            .setPositiveButton("删除") { _, _ ->
                viewModel.deleteItem()
            }
            .show()
    }

    private fun showSaveSuccessDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("保存成功")
            .setMessage("物品信息已成功更新。")
            .setPositiveButton("确定") { _, _ ->
                findNavController().navigateUp()
            }
            .show()
    }

    private fun showDeleteResultDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("已删除")
            .setMessage("删除的物品将在回收站暂存30天。")
            .setPositiveButton("确定") { _, _ ->
                findNavController().navigateUp()
            }
            .show()
    }

    private fun hideActionBar() {
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }

    private fun showActionBar() {
        (activity as? AppCompatActivity)?.supportActionBar?.show()
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
