package dev.arkbuilders.arkmemo.ui.views.presentation.edit

import android.graphics.Matrix
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.toSize
import dev.arkbuilders.arkmemo.ui.views.presentation.drawing.EditManager

private class TransparencyChessBoard {
    fun create(boardSize: Size, canvas: Canvas, matrix: Matrix) {
        val numberOfBoxesOnHeight = (boardSize.height / SQUARE_SIZE).toInt()
        val numberOfBoxesOnWidth = (boardSize.width / SQUARE_SIZE).toInt()
        var color = DARK
        val paint = Paint().also {
            it.color = color
        }
        val width = SQUARE_SIZE * (numberOfBoxesOnWidth + 1)
        val height = SQUARE_SIZE * (numberOfBoxesOnHeight + 1)
        val widthDelta = width - boardSize.width
        val heightDelta = height - boardSize.height
        canvas.nativeCanvas.setMatrix(matrix)
        0.rangeTo(numberOfBoxesOnWidth).forEach { i ->
            0.rangeTo(numberOfBoxesOnHeight).forEach { j ->
                var rectWidth = SQUARE_SIZE
                var rectHeight = SQUARE_SIZE
                val offsetX = SQUARE_SIZE * i
                val offsetY = SQUARE_SIZE * j
                if (i == numberOfBoxesOnWidth && widthDelta > 0) {
                    rectWidth = SQUARE_SIZE - widthDelta
                }
                if (j == numberOfBoxesOnHeight && heightDelta > 0) {
                    rectHeight = SQUARE_SIZE - heightDelta
                }
                val offset = Offset(offsetX, offsetY)
                val box = Rect(offset, Size(rectWidth, rectHeight))
                if (j == 0) {
                    if (color == paint.color) {
                        switchPaintColor(paint)
                    }
                    color = paint.color
                }
                switchPaintColor(paint)
                canvas.drawRect(box, paint)
            }
        }
    }

    private fun switchPaintColor(paint: Paint) {
        if (paint.color == DARK)
            paint.color = LIGHT
        else paint.color = DARK
    }

    companion object {
        private const val SQUARE_SIZE = 100f
        private val LIGHT = Color.White
        private val DARK = Color.LightGray
    }
}

private fun transparencyChessBoard(
    canvas: Canvas,
    size: Size,
    matrix: Matrix
) {
    TransparencyChessBoard().create(size, canvas, matrix)
}

@Composable
fun TransparencyChessBoardCanvas(modifier: Modifier, editManager: EditManager) {
    Canvas(modifier.background(Color.Transparent)) {
        editManager.invalidatorTick.value
        drawIntoCanvas { canvas ->
            transparencyChessBoard(
                canvas,
                editManager.imageSize.toSize(),
                editManager.backgroundMatrix
            )
        }
    }
}
