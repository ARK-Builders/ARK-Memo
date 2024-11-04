package dev.arkbuilders.arkmemo.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class WaveView(context: Context, attrs: AttributeSet): View(context, attrs) {

    var waveColor = Color.WHITE
    private val paint by lazy {
        Paint().also {
            it.color = waveColor
            it.style = Paint.Style.FILL
        }
    }

    private val bars = ArrayDeque<Rect>()
    private val radius = 10f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bars.isNotEmpty()) {
            bars.forEach {
                canvas.drawRoundRect(it.left.toFloat(), it.top.toFloat(), it.right.toFloat(),
                    it.bottom.toFloat(), radius, radius, paint)
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
        if (height <= 0) return
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
        private const val BAR_WIDTH = 6
        private const val BAR_INTERVAL = 12
        const val MAX_AMPLITUDE = 32762f / 10f
    }
}
