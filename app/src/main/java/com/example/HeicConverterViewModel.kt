package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class ConversionStatus {
    PENDING,      // Wait
    CONVERTING,   // Converting
    SUCCESS,      // Success
    FAILED        // Failed
}

data class ImageItem(
    val uri: Uri,
    val name: String,
    val size: Long,
    val status: ConversionStatus,
    val progress: Float = 0f,
    val errorMsg: String? = null,
    val convertedUri: Uri? = null,
    val outputFormat: String = "JPEG"
)

class HeicConverterViewModel : ViewModel() {

    private val _selectedImages = MutableStateFlow<List<ImageItem>>(emptyList())
    val selectedImages: StateFlow<List<ImageItem>> = _selectedImages.asStateFlow()

    private val _outputFormat = MutableStateFlow("JPEG") // "JPEG" or "PNG"
    val outputFormat: StateFlow<String> = _outputFormat.asStateFlow()

    private val _compressQuality = MutableStateFlow(100) // 1 - 100
    val compressQuality: StateFlow<Int> = _compressQuality.asStateFlow()

    private val _preserveMetadata = MutableStateFlow(true)
    val preserveMetadata: StateFlow<Boolean> = _preserveMetadata.asStateFlow()

    private val _cleanExif = MutableStateFlow(false)
    val cleanExif: StateFlow<Boolean> = _cleanExif.asStateFlow()

    private val _customSavePathUri = MutableStateFlow<Uri?>(null)
    val customSavePathUri: StateFlow<Uri?> = _customSavePathUri.asStateFlow()

    private val _customSavePathName = MutableStateFlow<String?>(null)
    val customSavePathName: StateFlow<String?> = _customSavePathName.asStateFlow()

    private val _isSettingsExpanded = MutableStateFlow(false)
    val isSettingsExpanded: StateFlow<Boolean> = _isSettingsExpanded.asStateFlow()

    private val _isConverting = MutableStateFlow(false)
    val isConverting: StateFlow<Boolean> = _isConverting.asStateFlow()

    private val _logText = MutableStateFlow("离线环境就绪。选择图片后即可开始转换。\n")
    val logText: StateFlow<String> = _logText.asStateFlow()

    private val _filterOnlyHeic = MutableStateFlow(true)
    val filterOnlyHeic: StateFlow<Boolean> = _filterOnlyHeic.asStateFlow()

    fun setFilterOnlyHeic(filter: Boolean) {
        _filterOnlyHeic.value = filter
    }

    fun toggleSettingsExpanded() {
        _isSettingsExpanded.update { !it }
    }

    fun setOutputFormat(format: String) {
        _outputFormat.value = format
    }

    fun setCompressQuality(quality: Int) {
        _compressQuality.value = quality
    }

    fun setPreserveMetadata(preserve: Boolean) {
        _preserveMetadata.value = preserve
        _cleanExif.value = !preserve
    }

    fun setCleanExif(clean: Boolean) {
        _cleanExif.value = clean
        _preserveMetadata.value = !clean
    }

    fun setCustomSavePath(context: Context, uri: Uri?, name: String?) {
        _customSavePathUri.value = uri
        _customSavePathName.value = name
        appendLog(
            if (uri != null) {
                context.getString(R.string.log_custom_save_path, name ?: "")
            } else {
                context.getString(R.string.log_default_save_path)
            }
        )
    }

    fun addSelectedImages(context: Context, uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val newList = uris.map { uri ->
                val name = getFileName(context, uri)
                val size = getFileSize(context, uri)
                ImageItem(
                    uri = uri,
                    name = name,
                    size = size,
                    status = ConversionStatus.PENDING,
                    outputFormat = _outputFormat.value
                )
            }
            _selectedImages.update { current ->
                // Avoid adding duplicate URIs
                val existingUris = current.map { it.uri }.toSet()
                current + newList.filter { it.uri !in existingUris }
            }
            appendLog(context.getString(R.string.log_import_success, newList.size))
            FirebaseAnalyticsHelper.logSelectImages(newList.size)
        }
    }

    fun removeImage(context: Context, index: Int) {
        _selectedImages.update { current ->
            val list = current.toMutableList()
            if (index in list.indices) {
                val removed = list.removeAt(index)
                appendLog(context.getString(R.string.log_remove_image, removed.name))
            }
            list
        }
    }

    fun clearAllImages(context: Context) {
        _selectedImages.value = emptyList()
        appendLog(context.getString(R.string.log_clear_all_done))
        viewModelScope.launch(Dispatchers.IO) {
            clearSharedCache(context)
        }
    }

    fun startConversion(context: Context) {
        val currentImages = _selectedImages.value
        if (currentImages.isEmpty()) {
            appendLog(context.getString(R.string.log_no_images))
            return
        }

        if (_isConverting.value) return

        _isConverting.value = true
        appendLog(context.getString(R.string.log_start_conversion, currentImages.size))
        FirebaseAnalyticsHelper.logStartConversion(currentImages.size, _outputFormat.value)

        viewModelScope.launch(Dispatchers.IO) {
            // Delete old temporary files to protect disk space
            clearSharedCache(context)

            val format = _outputFormat.value
            val quality = _compressQuality.value
            val preserve = _preserveMetadata.value
            val clean = _cleanExif.value
            val savePathUri = _customSavePathUri.value

            // Prefetch existing file names in the target SAF directory ONCE to avoid extremely slow nested listFiles queries
            val existingNames = mutableSetOf<String>()
            if (savePathUri != null) {
                appendLog(context.getString(R.string.log_reading_save_path))
                try {
                    val pickedDir = DocumentFile.fromTreeUri(context, savePathUri)
                    pickedDir?.listFiles()?.forEach { file ->
                        file.name?.let { existingNames.add(it.lowercase()) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            currentImages.forEachIndexed { index, item ->
                if (item.status == ConversionStatus.SUCCESS) {
                    // Skip completed, or recheck
                }
                
                // Update item status to CONVERTING
                updateItemStatus(index, ConversionStatus.CONVERTING, 0.1f)
                appendLog(context.getString(R.string.log_converting_progress, index + 1, currentImages.size, item.name))

                convertSingleImage(
                    context = context,
                    item = item,
                    outputFormat = format,
                    compressQuality = quality,
                    preserveMetadata = preserve,
                    cleanExif = clean,
                    customSavePathUri = savePathUri,
                    existingNamesInTree = existingNames,
                    onProgress = { prg ->
                        updateItemStatus(index, ConversionStatus.CONVERTING, prg)
                    },
                    onSuccess = { finalUri ->
                        updateItemSuccess(index, finalUri)
                        appendLog(context.getString(R.string.log_convert_success_progress, index + 1, currentImages.size))
                    },
                    onFailure = { error ->
                        updateItemFailure(index, error)
                        appendLog(context.getString(R.string.log_convert_failed_progress, index + 1, currentImages.size, error))
                    }
                )
            }
            _isConverting.value = false
            appendLog(context.getString(R.string.log_batch_completed))
            val successCount = _selectedImages.value.count { it.status == ConversionStatus.SUCCESS }
            val failedCount = _selectedImages.value.count { it.status == ConversionStatus.FAILED }
            FirebaseAnalyticsHelper.logConversionSuccess(successCount, failedCount, format)
        }
    }

    private fun updateItemStatus(index: Int, status: ConversionStatus, progress: Float) {
        _selectedImages.update { current ->
            val list = current.toMutableList()
            if (index in list.indices) {
                list[index] = list[index].copy(status = status, progress = progress)
            }
            list
        }
    }

    private fun updateItemSuccess(index: Int, convertedUri: Uri) {
        _selectedImages.update { current ->
            val list = current.toMutableList()
            if (index in list.indices) {
                list[index] = list[index].copy(
                    status = ConversionStatus.SUCCESS,
                    progress = 1.0f,
                    convertedUri = convertedUri,
                    outputFormat = _outputFormat.value
                )
            }
            list
        }
    }

    private fun updateItemFailure(index: Int, errorMsg: String) {
        _selectedImages.update { current ->
            val list = current.toMutableList()
            if (index in list.indices) {
                list[index] = list[index].copy(
                    status = ConversionStatus.FAILED,
                    progress = 0f,
                    errorMsg = errorMsg
                )
            }
            list
        }
    }

    private fun appendLog(text: String) {
        _logText.update { current ->
            current + "• $text\n"
        }
    }

    private fun convertSingleImage(
        context: Context,
        item: ImageItem,
        outputFormat: String,
        compressQuality: Int,
        preserveMetadata: Boolean,
        cleanExif: Boolean,
        customSavePathUri: Uri?,
        existingNamesInTree: MutableSet<String>,
        onProgress: (Float) -> Unit,
        onSuccess: (Uri) -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            onProgress(0.1f)
            val originalName = item.name
            val filenameWithoutExt = originalName.substringBeforeLast(".")
            val ext = outputFormat.lowercase()
            val mimeType = if (outputFormat == "JPEG") "image/jpeg" else "image/png"

            // 1. Get original EXIF orientation
            var orientation = ExifInterface.ORIENTATION_NORMAL
            try {
                context.contentResolver.openInputStream(item.uri)?.use { stream ->
                    val exif = ExifInterface(stream)
                    orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2. Decode original file locally
            onProgress(0.3f)
            val decodedBitmap = decodeUriToBitmap(context, item.uri)
            if (decodedBitmap == null) {
                onFailure(context.getString(R.string.error_decode_failed))
                return
            }

            val bitmap = rotateBitmapIfNeeded(decodedBitmap, orientation)

            onProgress(0.6f)
            // 2. Write to local Cache directory first for absolute safety in applying EXIF attributes
            val tempFile = File(context.cacheDir, "heic_conv_${System.currentTimeMillis()}.${ext}")
            val fos = FileOutputStream(tempFile)

            val compressFormat = if (outputFormat == "JPEG") {
                Bitmap.CompressFormat.JPEG
            } else {
                Bitmap.CompressFormat.PNG
            }

            bitmap.compress(compressFormat, compressQuality, fos)
            fos.flush()
            fos.close()
            bitmap.recycle() // Clean memory immediately

            onProgress(0.8f)

            // 3. Process metadata if requested and EXIF washing is disabled
            if (preserveMetadata && !cleanExif) {
                copyExif(context, item.uri, tempFile)
            }

            // 4. Save to destination
            val resultUri: Uri
            if (customSavePathUri != null) {
                // Save directly to SAF picked directory
                val pickedDir = DocumentFile.fromTreeUri(context, customSavePathUri)
                if (pickedDir == null || !pickedDir.exists()) {
                    onFailure(context.getString(R.string.error_custom_path_invalid))
                    return
                }

                val uniqueName = getUniqueFileName(existingNamesInTree, filenameWithoutExt, ext)
                val createdFile = pickedDir.createFile(mimeType, uniqueName)
                if (createdFile == null) {
                    onFailure(context.getString(R.string.error_create_file_failed))
                    return
                }

                val pfdOutput = context.contentResolver.openOutputStream(createdFile.uri)
                if (pfdOutput == null) {
                    onFailure(context.getString(R.string.error_write_stream_failed))
                    return
                }

                // Highly optimized buffered stream copy (instant write)
                tempFile.inputStream().use { input ->
                    pfdOutput.use { out ->
                        input.copyTo(out)
                    }
                }

                // Add to tracker
                existingNamesInTree.add(uniqueName.lowercase())

                resultUri = createdFile.uri
                tempFile.delete() // Clean up temporaries
            } else {
                // Safe-save in readable cache subfolder for sharing sheet standard procedures
                val sharedDir = File(context.cacheDir, "converted_images")
                if (!sharedDir.exists()) {
                    sharedDir.mkdirs()
                }

                val targetFile = File(sharedDir, "${filenameWithoutExt}.${ext}")
                var uniqueCacheFile = targetFile
                var counter = 1
                while (uniqueCacheFile.exists()) {
                    uniqueCacheFile = File(sharedDir, "${filenameWithoutExt}_${counter}.${ext}")
                    counter++
                }

                tempFile.renameTo(uniqueCacheFile)
                val authority = "${context.packageName}.fileprovider"
                resultUri = FileProvider.getUriForFile(context, authority, uniqueCacheFile)
            }

            onProgress(1.0f)
            onSuccess(resultUri)

        } catch (e: Exception) {
            onFailure(context.getString(R.string.error_conversion_err, e.message ?: "unknown"))
        }
    }

    private fun decodeUriToBitmap(context: Context, uri: Uri): Bitmap? {
        val resolver = context.contentResolver
        try {
            resolver.openInputStream(uri)?.use { stream ->
                return BitmapFactory.decodeStream(stream)
            }
        } catch (oom: OutOfMemoryError) {
            try {
                // Fallback: scale to avoid OutOfMemory
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 2
                }
                resolver.openInputStream(uri)?.use { stream ->
                    return BitmapFactory.decodeStream(stream, null, options)
                }
            } catch (e: Exception) {
                return null
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = android.graphics.Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.postScale(1f, -1f)
                matrix.postRotate(180f)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postScale(-1f, 1f)
                matrix.postRotate(270f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postScale(-1f, 1f)
                matrix.postRotate(90f)
            }
            else -> return bitmap // No rotation needed
        }
        return try {
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap) {
                bitmap.recycle()
            }
            rotated
        } catch (e: OutOfMemoryError) {
            bitmap // Return original on OOM
        }
    }

    private fun copyExif(context: Context, srcUri: Uri, destFile: File) {
        try {
            context.contentResolver.openInputStream(srcUri)?.use { input ->
                val srcExif = ExifInterface(input)
                val destExif = ExifInterface(destFile.absolutePath)

                val exifTags = listOf(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.TAG_DATETIME,
                    ExifInterface.TAG_DATETIME_ORIGINAL,
                    ExifInterface.TAG_DATETIME_DIGITIZED,
                    ExifInterface.TAG_MAKE,
                    ExifInterface.TAG_MODEL,
                    ExifInterface.TAG_FLASH,
                    ExifInterface.TAG_GPS_LATITUDE,
                    ExifInterface.TAG_GPS_LATITUDE_REF,
                    ExifInterface.TAG_GPS_LONGITUDE,
                    ExifInterface.TAG_GPS_LONGITUDE_REF,
                    ExifInterface.TAG_GPS_ALTITUDE,
                    ExifInterface.TAG_GPS_ALTITUDE_REF,
                    ExifInterface.TAG_GPS_TIMESTAMP,
                    ExifInterface.TAG_GPS_DATESTAMP,
                    ExifInterface.TAG_GPS_PROCESSING_METHOD,
                    ExifInterface.TAG_WHITE_BALANCE,
                    ExifInterface.TAG_EXPOSURE_TIME,
                    ExifInterface.TAG_F_NUMBER,
                    ExifInterface.TAG_ISO_SPEED_RATINGS,
                    ExifInterface.TAG_FOCAL_LENGTH,
                    ExifInterface.TAG_SUBSEC_TIME,
                    ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
                    ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
                    ExifInterface.TAG_ARTIST,
                    ExifInterface.TAG_COPYRIGHT,
                    ExifInterface.TAG_EXPOSURE_PROGRAM,
                    ExifInterface.TAG_SHUTTER_SPEED_VALUE,
                    ExifInterface.TAG_APERTURE_VALUE,
                    ExifInterface.TAG_BRIGHTNESS_VALUE,
                    ExifInterface.TAG_SCENE_TYPE,
                    ExifInterface.TAG_COLOR_SPACE,
                    ExifInterface.TAG_LENS_MAKE,
                    ExifInterface.TAG_LENS_MODEL,
                    ExifInterface.TAG_LENS_SPECIFICATION,
                    ExifInterface.TAG_SOFTWARE,
                    ExifInterface.TAG_USER_COMMENT
                )

                for (tag in exifTags) {
                    if (tag == ExifInterface.TAG_ORIENTATION) {
                        try {
                            destExif.setAttribute(tag, ExifInterface.ORIENTATION_NORMAL.toString())
                        } catch (e: Exception) {}
                        continue
                    }
                    val value = srcExif.getAttribute(tag)
                    if (value != null) {
                        try {
                            destExif.setAttribute(tag, value)
                        } catch (e: Exception) {
                            // Suppress individual coordinate or tag set errors to guarantee completion
                        }
                    }
                }

                try {
                    val latLong = FloatArray(2)
                    if (srcExif.getLatLong(latLong)) {
                        destExif.setLatLong(latLong[0].toDouble(), latLong[1].toDouble())
                    }
                    val altitude = srcExif.getAltitude(0.0)
                    if (altitude != 0.0) {
                        destExif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, altitude.toString())
                    }
                } catch (e: Exception) {
                    // Suppress lat long set failures
                }

                destExif.saveAttributes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getUniqueFileName(existingNames: Set<String>, baseName: String, extension: String): String {
        var name = "$baseName.$extension"
        var counter = 1
        while (existingNames.contains(name.lowercase())) {
            name = "$baseName ($counter).$extension"
            counter++
        }
        return name
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val index = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = c.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "image_${System.currentTimeMillis()}"
    }

    private fun getFileSize(context: Context, uri: Uri): Long {
        var size: Long = 0
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val index = c.getColumnIndex(OpenableColumns.SIZE)
                    if (index != -1) {
                        size = c.getLong(index)
                    }
                }
            }
        }
        return size
    }

    fun clearSharedCache(context: Context) {
        try {
            val sharedDir = File(context.cacheDir, "converted_images")
            if (sharedDir.exists() && sharedDir.isDirectory) {
                sharedDir.listFiles()?.forEach { file ->
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
