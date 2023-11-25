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

class NotesCanvas(context: Context, attrs: AttributeSet): View(context, attrs) {

    private var currentX = 0f
    private var currentY = 0f
    private lateinit var viewModel: GraphicNotesViewModel
    private var path = Path()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewModel.svg().setViewBox(w.toFloat(), h.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val paths = viewModel.paths()
        if (paths.isNotEmpty()) {
            paths.forEach {
                canvas.drawPath(it.path, it.paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                viewModel.svg().apply {
                    addCommand(SVGCommand.MoveTo(x, y))
                }
                currentX = x
                currentY = y
            }
            MotionEvent.ACTION_MOVE -> {
                val x2 = (currentX + x) / 2
                val y2 = (currentY + y) / 2
                path.quadTo(currentX, currentY, x2, y2)
                viewModel.svg().apply {
                    addCommand(SVGCommand.AbsQuadTo(currentX, currentY, x2, y2))
                }
                currentX = x
                currentY = y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                path = Path()
            }
        }
        val drawPath = DrawPath(path, viewModel.paint)
        viewModel.onDrawPath(drawPath)
        invalidate()
        return true
    }

    fun setViewModel(viewModel: GraphicNotesViewModel) {
        this.viewModel = viewModel
    }
}