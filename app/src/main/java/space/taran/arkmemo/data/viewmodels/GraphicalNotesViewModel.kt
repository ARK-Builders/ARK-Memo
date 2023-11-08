package space.taran.arkmemo.data.viewmodels

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.arkbuilders.arklib.ResourceId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
<<<<<<<< HEAD:app/src/main/java/space/taran/arkmemo/data/viewmodels/GraphicalNotesViewModel.kt
import space.taran.arkmemo.data.repositories.GraphicalNotesRepo
import space.taran.arkmemo.models.GraphicalNote
========
import space.taran.arkmemo.data.repositories.NotesRepo
import space.taran.arkmemo.models.GraphicNote
>>>>>>>> 4679884 (Improving graphic notes):app/src/main/java/space/taran/arkmemo/data/viewmodels/GraphicNotesViewModel.kt
import space.taran.arkmemo.utils.SVG
import java.util.Stack
import javax.inject.Inject

@HiltViewModel
<<<<<<<< HEAD:app/src/main/java/space/taran/arkmemo/data/viewmodels/GraphicalNotesViewModel.kt
class GraphicalNotesViewModel @Inject constructor(): ViewModel() {

    @Inject lateinit var repo: GraphicalNotesRepo
========
class GraphicNotesViewModel @Inject constructor(
    private val repo: NotesRepo<GraphicNote>
): ViewModel() {
>>>>>>>> 4679884 (Improving graphic notes):app/src/main/java/space/taran/arkmemo/data/viewmodels/GraphicNotesViewModel.kt

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

<<<<<<<< HEAD:app/src/main/java/space/taran/arkmemo/data/viewmodels/GraphicalNotesViewModel.kt
    init {
        if (editPaths.isNotEmpty()) editPaths.clear()
    }

    fun onSaveClick(note: GraphicalNote) {
========
    fun updatePathsByNote(note: GraphicNote) {
>>>>>>>> 4679884 (Improving graphic notes):app/src/main/java/space/taran/arkmemo/data/viewmodels/GraphicNotesViewModel.kt
        viewModelScope.launch {
            if (editPaths.isNotEmpty()) editPaths.clear()
            editPaths.addAll(note.svg?.getPaths()!!)
            svg = note.svg.copy()
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