package dev.arkbuilders.arkmemo.ui.viewmodels

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.graphics.SVG
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Stack
import javax.inject.Inject

@HiltViewModel
class GraphicNotesViewModel @Inject constructor(): ViewModel() {

    private val _notes = MutableStateFlow(listOf<GraphicNote>())
    val notes: StateFlow<List<GraphicNote>> = _notes
    private var paintColor = Color.BLACK

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
    }

    fun onChangeColor(color: Int) {
        paintColor = color
    }

    fun onChangeStrokeWidth(width: Float) {
        strokeWidth = width
    }

    fun paths(): ArrayDeque<DrawPath> = editPaths

    fun svg(): SVG = svg
}

data class DrawPath(
    val path: Path,
    val paint: Paint
)