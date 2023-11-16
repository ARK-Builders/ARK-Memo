package dev.arkbuilders.arkmemo.ui.viewmodels

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.arkbuilders.arkmemo.data.repositories.NotesRepo
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.utils.SVG
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Stack
import javax.inject.Inject

@HiltViewModel
class GraphicNotesViewModel @Inject constructor(
    private val repo: NotesRepo<GraphicNote>
): ViewModel() {

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

    private val editPaths = Stack<DrawPath>()

    private var svg = SVG()

    fun updatePathsByNote(note: GraphicNote) {
        viewModelScope.launch {
            if (editPaths.isNotEmpty()) editPaths.clear()
            editPaths.addAll(note.svg?.getPaths()!!)
            svg = note.svg.copy()
        }
    }

    fun onDrawPath(path: DrawPath) {
        editPaths.add(path)
    }

    fun onChangeColor(color: Int) {
        paintColor = color
    }

    fun onChangeStrokeWidth(width: Float) {
        strokeWidth = width
    }

    fun paths() = editPaths

    fun svg() = svg
}

data class DrawPath(
    val path: Path,
    val paint: Paint
)