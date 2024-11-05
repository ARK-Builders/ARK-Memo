package dev.arkbuilders.arkmemo.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import dev.arkbuilders.arkmemo.graphics.SVGCommand
import dev.arkbuilders.arkmemo.ui.viewmodels.DrawPath
import dev.arkbuilders.arkmemo.ui.viewmodels.GraphicNotesViewModel
import dev.arkbuilders.arkmemo.utils.getBrushSizeId
import dev.arkbuilders.arkmemo.utils.getStrokeColor

class NotesCanvas(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var currentX = 0f
    private var currentY = 0f
    private lateinit var viewModel: GraphicNotesViewModel
    private var path = Path()

    override fun onDraw(canvas: Canvas) {
        val paths = viewModel.paths()
        if (paths.isNotEmpty()) {
            paths.forEach { path ->
                canvas.drawPath(path.path, path.paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        var finishDrawing = false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                viewModel.svg().apply {
                    addCommand(
                        SVGCommand.MoveTo(x, y).apply {
                            paintColor = viewModel.paint.color.getStrokeColor()
                            brushSizeId = viewModel.paint.strokeWidth.getBrushSizeId()
                        },
                    )
                }
                currentX = x
                currentY = y
            }
            MotionEvent.ACTION_MOVE -> {
                val x2 = (currentX + x) / 2
                val y2 = (currentY + y) / 2
                path.quadTo(currentX, currentY, x2, y2)
                viewModel.svg().apply {
                    addCommand(
                        SVGCommand.AbsQuadTo(currentX, currentY, x2, y2).apply {
                            paintColor = viewModel.paint.color.getStrokeColor()
                            brushSizeId = viewModel.paint.strokeWidth.getBrushSizeId()
                        },
                    )
                }
                currentX = x
                currentY = y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                path = Path()
                finishDrawing = true
            }
        }

        if (!finishDrawing) {
            val drawPath = DrawPath(path, viewModel.paint)
            viewModel.onDrawPath(drawPath)
            invalidate()
        }

        return true
    }

    fun setViewModel(viewModel: GraphicNotesViewModel) {
        this.viewModel = viewModel
    }
}
