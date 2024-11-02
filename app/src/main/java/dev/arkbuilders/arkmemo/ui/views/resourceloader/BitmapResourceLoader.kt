package dev.arkbuilders.arkmemo.ui.views.resourceloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dev.arkbuilders.arkmemo.di.DIManager
import dev.arkbuilders.arkmemo.ui.views.presentation.drawing.EditManager
import java.nio.file.Path

class BitmapResourceLoader(
    val context: Context = DIManager.component.app(),
    val editManager: EditManager
) : CanvasResourceLoader {

    private val glideBuilder = Glide
        .with(context)
        .asBitmap()
        .skipMemoryCache(true)
        .diskCacheStrategy(DiskCacheStrategy.NONE)

    private lateinit var bitMapResource: ImageBitmap
    override suspend fun loadResourceInto(path: Path, editManager: EditManager) {
        loadImage(path)
    }
    override suspend fun getResource() {

    }

    private fun loadImage(
        resourcePath: Path,
    ) {
        glideBuilder
            .load(resourcePath.toFile())
            .loadInto()
    }


    private fun RequestBuilder<Bitmap>.loadInto() {
        into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(
                bitmap: Bitmap,
                transition: Transition<in Bitmap>?
            ) {
                editManager.apply {
                    backgroundImage.value = bitmap.asImageBitmap()
                    setOriginalBackgroundImage(backgroundImage.value)
                    scaleToFit()
                }
            }

            override fun onLoadCleared(placeholder: Drawable?) {}
        })
    }
}