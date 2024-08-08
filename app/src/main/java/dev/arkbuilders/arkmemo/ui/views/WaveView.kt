package dev.arkbuilders.arkmemo.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class WaveView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint().also {
        it.color = Color.LTGRAY
        it.style = Paint.Style.FILL
    }

    private val bars = ArrayDeque<Rect>()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (bars.isNotEmpty()) {
            bars.forEach {
                canvas?.drawRect(it, paint)
            }
        }
    }

    fun invalidateWave(amplitude: Int) {
        computeWave(amplitude)
        invalidate()
    }

    fun resetWave() {
        if (bars.isNotEmpty()) bars.clear()
    }

    private fun computeWave(amplitude: Int) {
        if (bars.isNotEmpty()) {
            bars.forEachIndexed { index, rect ->
                val right = width - ((index + 1) * (BAR_WIDTH + BAR_INTERVAL))
                val left = right - BAR_WIDTH
                rect.right = right
                rect.left = left
            }
        }
        val barHeight = ((amplitude.toFloat() / MAX_AMPLITUDE) * height).toInt()
        val top = (height - MIN_BAR_HEIGHT - barHeight) / 2
        bars.addFirst(Rect(width - BAR_WIDTH, top, width, top + barHeight + MIN_BAR_HEIGHT))
    }

    companion object {
        private const val MIN_BAR_HEIGHT = 4
        private const val BAR_WIDTH = 4
        private const val BAR_INTERVAL = 2
        private const val MAX_AMPLITUDE = 32762f / 10f
    }
}
