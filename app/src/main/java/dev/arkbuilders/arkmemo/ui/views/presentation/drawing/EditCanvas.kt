package dev.arkbuilders.arkmemo.ui.views.presentation.drawing

import android.graphics.Matrix
import android.graphics.PointF
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.toSize
import dev.arkbuilders.arkmemo.ui.views.presentation.edit.EditViewModel
import dev.arkbuilders.arkmemo.ui.views.presentation.edit.TransparencyChessBoardCanvas
import dev.arkbuilders.arkmemo.ui.views.presentation.edit.crop.CropWindow.Companion.computeDeltaX
import dev.arkbuilders.arkmemo.ui.views.presentation.edit.crop.CropWindow.Companion.computeDeltaY
import dev.arkbuilders.arkmemo.ui.views.presentation.picker.toDp
import dev.arkbuilders.arkmemo.ui.views.presentation.utils.calculateRotationFromOneFingerGesture

@Composable
fun EditCanvas(viewModel: EditViewModel) {
    val editManager = viewModel.editManager
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    fun resetScaleAndTranslate() {
        editManager.apply {
            if (
                isRotateMode.value || isCropMode.value || isResizeMode.value ||
                isBlurMode.value
            ) {
                scale = 1f; zoomScale = scale; offset = Offset.Zero
            }
        }
    }

    Box(contentAlignment = Alignment.Center) {
        val modifier = Modifier.size(
            editManager.availableDrawAreaSize.value.width.toDp(),
            editManager.availableDrawAreaSize.value.height.toDp()
        ).graphicsLayer {
            resetScaleAndTranslate()

            // Eraser leaves black line instead of erasing without this hack, it uses BlendMode.SrcOut
            // https://stackoverflow.com/questions/65653560/jetpack-compose-applying-porterduffmode-to-image
            // Provide a slight opacity to for compositing into an
            // offscreen buffer to ensure blend modes are applied to empty pixel information
            // By default any alpha != 1.0f will use a compositing layer by default
            alpha = 0.99f

            scaleX = scale
            scaleY = scale
            translationX = offset.x
            translationY = offset.y
        }
        TransparencyChessBoardCanvas(modifier, editManager)
        BackgroundCanvas(modifier, editManager)
        DrawCanvas(modifier, viewModel)
    }
    if (
        editManager.isRotateMode.value || editManager.isZoomMode.value ||
        editManager.isPanMode.value
    ) {
        Canvas(
            Modifier.fillMaxSize()
                .pointerInput(Any()) {
                    forEachGesture {
                        awaitPointerEventScope {
                            awaitFirstDown()
                            do {
                                val event = awaitPointerEvent()
                                when (true) {
                                    (editManager.isRotateMode.value) -> {
                                        val angle = event
                                            .calculateRotationFromOneFingerGesture(
                                                editManager.calcCenter()
                                            )
                                        editManager.rotate(angle)
                                        editManager.invalidatorTick.value++
                                    }
                                    else -> {
                                        if (editManager.isZoomMode.value) {
                                            scale *= event.calculateZoom()
                                            editManager.zoomScale = scale
                                        }
                                        if (editManager.isPanMode.value) {
                                            val pan = event.calculatePan()
                                            offset = Offset(
                                                offset.x + pan.x,
                                                offset.y + pan.y
                                            )
                                        }
                                    }
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    }
                }
        ) {}
    }
}

@Composable
fun BackgroundCanvas(modifier: Modifier, editManager: EditManager) {
    Canvas(modifier) {
        editManager.apply {
            invalidatorTick.value
            var matrix = matrix
            if (
                isCropMode.value || isRotateMode.value ||
                isResizeMode.value || isBlurMode.value
            )
                matrix = editMatrix
            drawIntoCanvas { canvas ->
                backgroundImage.value?.let {
                    canvas.nativeCanvas.drawBitmap(
                        it.asAndroidBitmap(),
                        matrix,
                        null
                    )
                } ?: run {
                    val rect = Rect(
                        Offset.Zero,
                        imageSize.toSize()
                    )
                    canvas.nativeCanvas.setMatrix(matrix)
                    canvas.drawRect(rect, backgroundPaint)
                    canvas.clipRect(rect, ClipOp.Intersect)
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DrawCanvas(modifier: Modifier, viewModel: EditViewModel) {
    val context = LocalContext.current
    val editManager = viewModel.editManager
    var path = Path()
    val currentPoint = PointF(0f, 0f)
    val drawModifier = if (editManager.isCropMode.value) Modifier.fillMaxSize()
    else modifier

    fun handleDrawEvent(action: Int, eventX: Float, eventY: Float) {
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                path.reset()
                path.moveTo(eventX, eventY)
                currentPoint.x = eventX
                currentPoint.y = eventY
                editManager.apply {
                    drawOperation.draw(path)
                    applyOperation()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                path.quadraticBezierTo(
                    currentPoint.x,
                    currentPoint.y,
                    (eventX + currentPoint.x) / 2,
                    (eventY + currentPoint.y) / 2
                )
                currentPoint.x = eventX
                currentPoint.y = eventY
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                // draw a dot
                if (eventX == currentPoint.x &&
                    eventY == currentPoint.y
                ) {
                    path.lineTo(currentPoint.x, currentPoint.y)
                }

                editManager.clearRedoPath()
                editManager.updateRevised()
                path = Path()
            }
            else -> {}
        }
    }

    fun handleCropEvent(action: Int, eventX: Float, eventY: Float) {
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                currentPoint.x = eventX
                currentPoint.y = eventY
                editManager.cropWindow.detectTouchedSide(
                    Offset(eventX, eventY)
                )
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX =
                    computeDeltaX(currentPoint.x, eventX)
                val deltaY =
                    computeDeltaY(currentPoint.y, eventY)

                editManager.cropWindow.setDelta(
                    Offset(
                        deltaX,
                        deltaY
                    )
                )
                currentPoint.x = eventX
                currentPoint.y = eventY
            }
        }
    }

    fun handleEyeDropEvent(action: Int, eventX: Float, eventY: Float) {
        viewModel.applyEyeDropper(action, eventX.toInt(), eventY.toInt())
    }

    fun handleBlurEvent(action: Int, eventX: Float, eventY: Float) {
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                currentPoint.x = eventX
                currentPoint.y = eventY
            }
            MotionEvent.ACTION_MOVE -> {
                val position = Offset(
                    currentPoint.x,
                    currentPoint.y
                )
                val delta = Offset(
                    computeDeltaX(currentPoint.x, eventX),
                    computeDeltaY(currentPoint.y, eventY)
                )
                editManager.blurOperation.move(position, delta)
                currentPoint.x = eventX
                currentPoint.y = eventY
            }
            else -> {}
        }
    }

    Canvas(
        modifier = drawModifier.pointerInteropFilter { event ->
            val eventX = event.x
            val eventY = event.y
            val tmpMatrix = Matrix()
            editManager.matrix.invert(tmpMatrix)
            val mappedXY = floatArrayOf(
                event.x / editManager.zoomScale,
                event.y / editManager.zoomScale
            )
            tmpMatrix.mapPoints(mappedXY)
            val mappedX = mappedXY[0]
            val mappedY = mappedXY[1]

            when (true) {
                editManager.isResizeMode.value -> {}
                editManager.isBlurMode.value -> handleBlurEvent(
                    event.action,
                    eventX,
                    eventY
                )

                editManager.isCropMode.value -> handleCropEvent(
                    event.action,
                    eventX,
                    eventY
                )

                editManager.isEyeDropperMode.value -> handleEyeDropEvent(
                    event.action,
                    event.x,
                    event.y
                )

                else -> handleDrawEvent(event.action, mappedX, mappedY)
            }
            editManager.invalidatorTick.value++
            true
        }
    ) {
        // force recomposition on invalidatorTick change
        editManager.invalidatorTick.value
        drawIntoCanvas { canvas ->
            editManager.apply {
                var matrix = this.matrix
                if (isRotateMode.value || isResizeMode.value || isBlurMode.value)
                    matrix = editMatrix
                if (isCropMode.value) matrix = Matrix()
                canvas.nativeCanvas.setMatrix(matrix)
                if (isResizeMode.value) return@drawIntoCanvas
                if (isBlurMode.value) {
                    editManager.blurOperation.draw(context, canvas)
                    return@drawIntoCanvas
                }
                if (isCropMode.value) {
                    editManager.cropWindow.show(canvas)
                    return@drawIntoCanvas
                }
                val rect = Rect(
                    Offset.Zero,
                    imageSize.toSize()
                )
                canvas.drawRect(
                    rect,
                    Paint().also { it.color = Color.Transparent }
                )
                canvas.clipRect(rect, ClipOp.Intersect)
                if (drawPaths.isNotEmpty()) {
                    drawPaths.forEach {
                        canvas.drawPath(it.path, it.paint)
                    }
                }
            }
        }
    }
}
