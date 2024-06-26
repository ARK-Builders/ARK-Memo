package dev.arkbuilders.arkmemo.ui.viewmodels

import android.graphics.Paint
import android.graphics.Path
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.arkbuilders.arkmemo.graphics.SVG
import dev.arkbuilders.arkmemo.graphics.Size
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.ui.adapters.BrushColor
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorBlack
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorBlue
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorGreen
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorGrey
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorOrange
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorPurple
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorRed
import dev.arkbuilders.arkmemo.ui.adapters.BrushSize
import dev.arkbuilders.arkmemo.ui.adapters.BrushSizeHuge
import dev.arkbuilders.arkmemo.ui.adapters.BrushSizeLarge
import dev.arkbuilders.arkmemo.ui.adapters.BrushSizeMedium
import dev.arkbuilders.arkmemo.ui.adapters.BrushSizeSmall
import dev.arkbuilders.arkmemo.ui.adapters.BrushSizeTiny
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GraphicNotesViewModel @Inject constructor(): ViewModel() {

    private var paintColor = dev.arkbuilders.arkmemo.graphics.Color.BLACK.code
    private var strokeWidth = Size.TINY.value

    val paint get() = Paint().also {
        it.color = paintColor
        it.style = Paint.Style.STROKE
        it.strokeWidth = strokeWidth
        it.strokeCap = Paint.Cap.ROUND
        it.strokeJoin = Paint.Join.ROUND
        it.isAntiAlias = true
    }

    private val editPaths = ArrayDeque<DrawPath>()

    private var svg = SVG()
    private val svgLiveData = MutableLiveData<SVG>()
    val observableSvgLiveData = svgLiveData as LiveData<SVG>

    fun onNoteOpened(note: GraphicNote) {
        viewModelScope.launch {
            if (editPaths.isNotEmpty()) editPaths.clear()
            editPaths.addAll(note.svg?.getPaths()!!)
            svg = note.svg.copy()
        }
    }

    fun onDrawPath(path: DrawPath) {
        editPaths.addLast(path)
        svg.addPath(path)
        svgLiveData.postValue(svg)
    }

    fun paths(): Collection<DrawPath> = editPaths

    fun svg(): SVG = svg

    fun setPaintColor(color: BrushColor) {
        paintColor = when (color) {
            is BrushColorBlack -> dev.arkbuilders.arkmemo.graphics.Color.BLACK.code
            is BrushColorGrey  -> dev.arkbuilders.arkmemo.graphics.Color.GRAY.code
            is BrushColorRed   -> dev.arkbuilders.arkmemo.graphics.Color.RED.code
            is BrushColorOrange -> dev.arkbuilders.arkmemo.graphics.Color.ORANGE.code
            is BrushColorGreen -> dev.arkbuilders.arkmemo.graphics.Color.GREEN.code
            is BrushColorBlue  -> dev.arkbuilders.arkmemo.graphics.Color.BLUE.code
            is BrushColorPurple -> dev.arkbuilders.arkmemo.graphics.Color.PURPLE.code
        }
    }

    fun setBrushSize(size: BrushSize) {
        strokeWidth = when(size) {
            is BrushSizeTiny -> Size.TINY.value
            is BrushSizeSmall -> Size.SMALL.value
            is BrushSizeMedium -> Size.MEDIUM.value
            is BrushSizeLarge -> Size.LARGE.value
            is BrushSizeHuge -> Size.HUGE.value
        }
    }
}

data class DrawPath(
    val path: Path,
    val paint: Paint,
)