package space.taran.arkmemo.data.viewmodels

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import space.taran.arkmemo.data.repositories.GraphicalNotesRepo
import space.taran.arkmemo.models.GraphicalNote
import space.taran.arkmemo.utils.SVG
import java.util.Stack
import javax.inject.Inject

@HiltViewModel
class GraphicalNotesViewModel @Inject constructor(): ViewModel() {

    @Inject lateinit var repo: GraphicalNotesRepo

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

    private val svg = SVG()

    init {
        if (editPaths.isNotEmpty()) editPaths.clear()
    }

    fun onSaveClick(note: GraphicalNote) {
        viewModelScope.launch {
            repo.save(note)
        }
    }

    fun onDeleteClick(note: GraphicalNote) {
        viewModelScope.launch {
            repo.delete(note)
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