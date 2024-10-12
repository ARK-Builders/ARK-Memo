package dev.arkbuilders.arkmemo.ui.views.presentation.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import dev.arkbuilders.arkmemo.ui.views.presentation.edit.crop.CropWindow
import dev.arkbuilders.arkmemo.ui.views.presentation.edit.resize.ResizeOperation
import dev.arkbuilders.arkmemo.ui.views.presentation.edit.rotate.RotateOperation

fun Bitmap.crop(cropParams: CropWindow.CropParams): Bitmap = Bitmap.createBitmap(
    this,
    cropParams.x,
    cropParams.y,
    cropParams.width,
    cropParams.height
)

fun Bitmap.resize(scale: ResizeOperation.Scale): Bitmap {
    val matrix = Matrix()
    matrix.postScale(scale.x, scale.y)
    return Bitmap.createBitmap(
        this,
        0,
        0,
        width,
        height,
        matrix,
        true
    )
}

fun Matrix.rotate(angle: Float, center: RotateOperation.Center) {
    this.postRotate(angle, center.x, center.y)
}
