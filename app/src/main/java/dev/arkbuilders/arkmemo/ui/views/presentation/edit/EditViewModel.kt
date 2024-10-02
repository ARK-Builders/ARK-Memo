package dev.arkbuilders.arkmemo.ui.views.presentation.edit

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.view.MotionEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.ui.views.data.Preferences
import dev.arkbuilders.arkmemo.ui.views.data.Resolution
import dev.arkbuilders.arkmemo.di.DIManager
import dev.arkbuilders.arkmemo.ui.views.presentation.drawing.EditManager
import dev.arkbuilders.arkmemo.ui.views.presentation.edit.resize.ResizeOperation
import timber.log.Timber
import java.io.File
import java.nio.file.Path
import kotlin.io.path.outputStream
import kotlin.system.measureTimeMillis

class EditViewModel(
    private val primaryColor: Long,
    private val launchedFromIntent: Boolean,
    private val imagePath: Path?,
    private val imageUri: String?,
    private val maxResolution: Resolution,
    private val prefs: Preferences
) : ViewModel() {
    val editManager = EditManager()

    var strokeSliderExpanded by mutableStateOf(false)
    var menusVisible by mutableStateOf(true)
    var strokeWidth by mutableStateOf(5f)
    var showSavePathDialog by mutableStateOf(false)
    val showOverwriteCheckbox = mutableStateOf(imagePath != null)
    var showExitDialog by mutableStateOf(false)
    var showMoreOptionsPopup by mutableStateOf(false)
    var imageSaved by mutableStateOf(false)
    var isSavingImage by mutableStateOf(false)
    var showEyeDropperHint by mutableStateOf(false)
    val showConfirmClearDialog = mutableStateOf(false)
    var isLoaded by mutableStateOf(false)
    var exitConfirmed = false
        private set
    val bottomButtonsScrollIsAtStart = mutableStateOf(true)
    val bottomButtonsScrollIsAtEnd = mutableStateOf(false)

    private val _usedColors = mutableListOf<Color>()
    val usedColors: List<Color> = _usedColors

    init {
        if (imageUri == null && imagePath == null) {
            viewModelScope.launch {
                editManager.initDefaults(
                    prefs.readDefaults(),
                    maxResolution
                )
            }
        }
        viewModelScope.launch {
            _usedColors.addAll(prefs.readUsedColors())

            val color = if (_usedColors.isNotEmpty()) {
                _usedColors.last()
            } else {
                val defaultColor = Color(primaryColor.toULong())

                _usedColors.add(defaultColor)
                defaultColor
            }

            editManager.setPaintColor(color)
        }
    }

    fun loadImage() {
        isLoaded = true
        imagePath?.let {
            loadImageWithPath(
                DIManager.component.app(),
                imagePath,
                editManager
            )
            return
        }
        imageUri?.let {
            loadImageWithUri(
                DIManager.component.app(),
                imageUri,
                editManager
            )
            return
        }
        editManager.scaleToFit()
    }

    fun saveImage(context: Context, path: Path) {
        viewModelScope.launch(Dispatchers.IO) {
            isSavingImage = true
            val combinedBitmap = getEditedImage()

            path.outputStream().use { out ->
                combinedBitmap.asAndroidBitmap()
                    .compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            MediaScannerConnection.scanFile(
                context,
                arrayOf(path.toString()),
                arrayOf("image/*")
            ) { _, _ -> }
            imageSaved = true
            isSavingImage = false
            showSavePathDialog = false
        }
    }

    fun shareImage(context: Context) =
        viewModelScope.launch(Dispatchers.IO) {
            val intent = Intent(Intent.ACTION_SEND)
            val uri = getCachedImageUri(context)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            context.apply {
                startActivity(
                    Intent.createChooser(
                        intent,
                        getString(R.string.share)
                    )
                )
            }
        }

    fun getImageUri(
        context: Context = DIManager.component.app(),
        bitmap: Bitmap? = null,
        name: String = ""
    ) = getCachedImageUri(context, bitmap, name)

    private fun getCachedImageUri(
        context: Context,
        bitmap: Bitmap? = null,
        name: String = ""
    ): Uri {
        var uri: Uri? = null
        val imageCacheFolder = File(context.cacheDir, "images")
        val imgBitmap = bitmap ?: getEditedImage().asAndroidBitmap()
        try {
            imageCacheFolder.mkdirs()
            val file = File(imageCacheFolder, "image$name.png")
            file.outputStream().use { out ->
                imgBitmap
                    .compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Timber.tag("Cached image path").d(file.path.toString())
            uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return uri!!
    }

    fun trackColor(color: Color) {
        _usedColors.remove(color)
        _usedColors.add(color)

        val excess = _usedColors.size - KEEP_USED_COLORS
        repeat(excess) {
            _usedColors.removeFirst()
        }

        viewModelScope.launch {
            prefs.persistUsedColors(usedColors)
        }
    }

    fun toggleEyeDropper() {
        editManager.toggleEyeDropper()
    }
    fun cancelEyeDropper() {
        editManager.setPaintColor(usedColors.last())
    }

    fun applyEyeDropper(action: Int, x: Int, y: Int) {
        try {
            val bitmap = getEditedImage().asAndroidBitmap()
            val imageX = (x * editManager.bitmapScale.x).toInt()
            val imageY = (y * editManager.bitmapScale.y).toInt()
            val pixel = bitmap.getPixel(imageX, imageY)
            val color = Color(pixel)
            if (color == Color.Transparent) {
                showEyeDropperHint = true
                return
            }
            when (action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
                    trackColor(color)
                    toggleEyeDropper()
                    menusVisible = true
                }
            }
            editManager.setPaintColor(color)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun getCombinedImageBitmap(): ImageBitmap {
        val size = editManager.imageSize
        val drawBitmap = ImageBitmap(
            size.width,
            size.height,
            ImageBitmapConfig.Argb8888
        )
        val combinedBitmap =
            ImageBitmap(size.width, size.height, ImageBitmapConfig.Argb8888)

        val time = measureTimeMillis {
            val backgroundPaint = Paint().also {
                it.color = editManager.backgroundColor.value
            }
            val drawCanvas = Canvas(drawBitmap)
            val combinedCanvas = Canvas(combinedBitmap)
            val matrix = Matrix().apply {
                if (editManager.rotationAngles.isNotEmpty()) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    setRotate(
                        editManager.rotationAngle.value,
                        centerX.toFloat(),
                        centerY.toFloat()
                    )
                }
            }
            combinedCanvas.drawRect(
                Rect(Offset.Zero, size.toSize()),
                backgroundPaint
            )
            combinedCanvas.nativeCanvas.setMatrix(matrix)
            editManager.backgroundImage.value?.let {
                combinedCanvas.drawImage(
                    it,
                    Offset.Zero,
                    Paint()
                )
            }
            editManager.drawPaths.forEach {
                drawCanvas.drawPath(it.path, it.paint)
            }
            combinedCanvas.drawImage(drawBitmap, Offset.Zero, Paint())
        }
        Timber.tag("edit-viewmodel: getCombinedImageBitmap").d(
            "processing edits took ${time / 1000} s ${time % 1000} ms"
        )
        return combinedBitmap
    }

    fun getEditedImage(): ImageBitmap {
        val size = editManager.imageSize
        var bitmap = ImageBitmap(
            size.width,
            size.height,
            ImageBitmapConfig.Argb8888
        )
        var pathBitmap: ImageBitmap? = null
        val time = measureTimeMillis {
            editManager.apply {
                val matrix = Matrix()
                if (editManager.drawPaths.isNotEmpty()) {
                    pathBitmap = ImageBitmap(
                        size.width,
                        size.height,
                        ImageBitmapConfig.Argb8888
                    )
                    val pathCanvas = Canvas(pathBitmap!!)
                    editManager.drawPaths.forEach {
                        pathCanvas.drawPath(it.path, it.paint)
                    }
                }
                backgroundImage.value?.let {
                    val canvas = Canvas(bitmap)
                    if (prevRotationAngle == 0f && drawPaths.isEmpty()) {
                        bitmap = it
                        return@let
                    }
                    if (prevRotationAngle != 0f) {
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        matrix.setRotate(prevRotationAngle, centerX, centerY)
                    }
                    canvas.nativeCanvas.drawBitmap(
                        it.asAndroidBitmap(),
                        matrix,
                        null
                    )
                    if (drawPaths.isNotEmpty()) {
                        canvas.nativeCanvas.drawBitmap(
                            pathBitmap?.asAndroidBitmap()!!,
                            matrix,
                            null
                        )
                    }
                } ?: run {
                    val canvas = Canvas(bitmap)
                    if (prevRotationAngle != 0f) {
                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        matrix.setRotate(
                            prevRotationAngle,
                            centerX.toFloat(),
                            centerY.toFloat()
                        )
                        canvas.nativeCanvas.setMatrix(matrix)
                    }
                    canvas.drawRect(
                        Rect(Offset.Zero, size.toSize()),
                        backgroundPaint
                    )
                    if (drawPaths.isNotEmpty()) {
                        canvas.drawImage(
                            pathBitmap!!,
                            Offset.Zero,
                            Paint()
                        )
                    }
                }
            }
        }
        Timber.tag("edit-viewmodel: getEditedImage").d(
            "processing edits took ${time / 1000} s ${time % 1000} ms"
        )
        return bitmap
    }
    fun confirmExit() = viewModelScope.launch {
        exitConfirmed = true
        isLoaded = false
        delay(2_000)
        exitConfirmed = false
        isLoaded = true
    }

    fun applyOperation() {
        editManager.applyOperation()
        menusVisible = true
    }

    fun cancelOperation() {
        editManager.apply {
            if (isRotateMode.value) {
                toggleRotateMode()
                cancelRotateMode()
                menusVisible = true
            }
            if (isCropMode.value) {
                toggleCropMode()
                cancelCropMode()
                menusVisible = true
            }
            if (isResizeMode.value) {
                toggleResizeMode()
                cancelResizeMode()
                menusVisible = true
            }
            if (isEyeDropperMode.value) {
                toggleEyeDropper()
                cancelEyeDropper()
                menusVisible = true
            }
            if (isBlurMode.value) {
                toggleBlurMode()
                blurOperation.cancel()
                menusVisible = true
            }
            scaleToFit()
        }
    }

    fun persistDefaults(color: Color, resolution: Resolution) {
        viewModelScope.launch {
            prefs.persistDefaults(color, resolution)
        }
    }

    companion object {
        private const val KEEP_USED_COLORS = 20
    }
}

class EditViewModelFactory @AssistedInject constructor(
    @Assisted private val primaryColor: Long,
    @Assisted private val launchedFromIntent: Boolean,
    @Assisted private val imagePath: Path?,
    @Assisted private val imageUri: String?,
    @Assisted private val maxResolution: Resolution,
    private val prefs: Preferences,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditViewModel(
            primaryColor,
            launchedFromIntent,
            imagePath,
            imageUri,
            maxResolution,
            prefs,
        ) as T
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted primaryColor: Long,
            @Assisted launchedFromIntent: Boolean,
            @Assisted imagePath: Path?,
            @Assisted imageUri: String?,
            @Assisted maxResolution: Resolution,
        ): EditViewModelFactory
    }
}

private fun loadImageWithPath(
    context: Context,
    image: Path,
    editManager: EditManager
) {
    initGlideBuilder(context)
        .load(image.toFile())
        .loadInto(editManager)
}

private fun loadImageWithUri(
    context: Context,
    uri: String,
    editManager: EditManager
) {
    initGlideBuilder(context)
        .load(uri.toUri())
        .loadInto(editManager)
}

private fun initGlideBuilder(context: Context) = Glide
    .with(context)
    .asBitmap()
    .skipMemoryCache(true)
    .diskCacheStrategy(DiskCacheStrategy.NONE)

private fun RequestBuilder<Bitmap>.loadInto(
    editManager: EditManager
) {
    into(object : CustomTarget<Bitmap>() {
        override fun onResourceReady(
            bitmap: Bitmap,
            transition: Transition<in Bitmap>?
        ) {
            editManager.apply {
                val image = bitmap.asImageBitmap()
                backgroundImage.value = image
                setOriginalBackgroundImage(image)
                scaleToFit()
            }
        }

        override fun onLoadCleared(placeholder: Drawable?) {}
    })
}

fun resize(
    imageBitmap: ImageBitmap,
    maxWidth: Int,
    maxHeight: Int
): ImageBitmap {
    val bitmap = imageBitmap.asAndroidBitmap()
    val width = bitmap.width
    val height = bitmap.height

    val bitmapRatio = width.toFloat() / height.toFloat()
    val maxRatio = maxWidth.toFloat() / maxHeight.toFloat()

    var finalWidth = maxWidth
    var finalHeight = maxHeight

    if (maxRatio > bitmapRatio) {
        finalWidth = (maxHeight.toFloat() * bitmapRatio).toInt()
    } else {
        finalHeight = (maxWidth.toFloat() / bitmapRatio).toInt()
    }
    return Bitmap
        .createScaledBitmap(bitmap, finalWidth, finalHeight, true)
        .asImageBitmap()
}

fun fitImage(
    imageBitmap: ImageBitmap,
    maxWidth: Int,
    maxHeight: Int
): ImageViewParams {
    val bitmap = imageBitmap.asAndroidBitmap()
    val width = bitmap.width
    val height = bitmap.height

    val bitmapRatio = width.toFloat() / height.toFloat()
    val maxRatio = maxWidth.toFloat() / maxHeight.toFloat()

    var finalWidth = maxWidth
    var finalHeight = maxHeight

    if (maxRatio > bitmapRatio) {
        finalWidth = (maxHeight.toFloat() * bitmapRatio).toInt()
    } else {
        finalHeight = (maxWidth.toFloat() / bitmapRatio).toInt()
    }
    return ImageViewParams(
        IntSize(
            finalWidth,
            finalHeight,
        ),
        ResizeOperation.Scale(
            finalWidth.toFloat() / width.toFloat(),
            finalHeight.toFloat() / height.toFloat()
        )
    )
}

fun fitBackground(
    resolution: IntSize,
    maxWidth: Int,
    maxHeight: Int
): ImageViewParams {

    val width = resolution.width
    val height = resolution.height

    val resolutionRatio = width.toFloat() / height.toFloat()
    val maxRatio = maxWidth.toFloat() / maxHeight.toFloat()

    var finalWidth = maxWidth
    var finalHeight = maxHeight

    if (maxRatio > resolutionRatio) {
        finalWidth = (maxHeight.toFloat() * resolutionRatio).toInt()
    } else {
        finalHeight = (maxWidth.toFloat() / resolutionRatio).toInt()
    }
    return ImageViewParams(
        IntSize(
            finalWidth,
            finalHeight,
        ),
        ResizeOperation.Scale(
            finalWidth.toFloat() / width.toFloat(),
            finalHeight.toFloat() / height.toFloat()
        )
    )
}
class ImageViewParams(
    val drawArea: IntSize,
    val scale: ResizeOperation.Scale
)
