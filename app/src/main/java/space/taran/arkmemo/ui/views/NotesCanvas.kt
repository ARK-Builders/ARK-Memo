package space.taran.arkmemo.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import space.taran.arkmemo.data.viewmodels.DrawPath
import space.taran.arkmemo.data.viewmodels.GraphicalNotesViewModel
import space.taran.arkmemo.utils.SVG
import java.util.Stack

class NotesCanvas(context: Context, attrs: AttributeSet): View(context, attrs) {

    private var currentX = 0f
    private var currentY = 0f
    private lateinit var viewModel: GraphicalNotesViewModel
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
                    writeData(moveTo(x, y))
                }
                currentX = x
                currentY = y
            }
            MotionEvent.ACTION_MOVE -> {
                val x2 = (currentX + x) / 2
                val y2 = (currentY + y) / 2
                path.quadTo(currentX, currentY, x2, y2)
                viewModel.svg().apply {
                    writeData(quadraticBezierTo(currentX, currentY, x2, y2))
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

    fun setViewModel(viewModel: GraphicalNotesViewModel) {
        this.viewModel = viewModel
    }
}