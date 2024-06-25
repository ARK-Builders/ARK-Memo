package dev.arkbuilders.arkmemo.ui.viewmodels

import android.graphics.Paint
import android.graphics.Path
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.arkbuilders.arkmemo.graphics.SVG
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.ui.adapters.BrushColor
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorBlack
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorBlue
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorGreen
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorGrey
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorOrange
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorPurple
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorRed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GraphicNotesViewModel @Inject constructor(): ViewModel() {

    private val _notes = MutableStateFlow(listOf<GraphicNote>())
    val notes: StateFlow<List<GraphicNote>> = _notes
    private var paintColor = dev.arkbuilders.arkmemo.graphics.Color.BLACK.code

    private var strokeWidth = 10f

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

    fun onChangeColor(color: Int) {
        paintColor = color
    }

    fun onChangeStrokeWidth(width: Float) {
        strokeWidth = width
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
}

data class DrawPath(
    val path: Path,
    val paint: Paint,
)