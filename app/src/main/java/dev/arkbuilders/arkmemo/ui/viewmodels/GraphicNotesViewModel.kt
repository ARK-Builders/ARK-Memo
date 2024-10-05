package dev.arkbuilders.arkmemo.ui.viewmodels

import android.graphics.Paint
import android.graphics.Path
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.arkbuilders.arkmemo.di.IO_DISPATCHER
import dev.arkbuilders.arkmemo.graphics.Color
import dev.arkbuilders.arkmemo.graphics.SVG
import dev.arkbuilders.arkmemo.graphics.Size
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.repo.NotesRepo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class GraphicNotesViewModel @Inject constructor(
    @Named(IO_DISPATCHER) private val iODispatcher: CoroutineDispatcher,
    private val graphicNotesRepo: NotesRepo<GraphicNote>,

    ): ViewModel() {


    private var paintColor = Color.BLACK.code
    private var lastPaintColor = paintColor
    private var strokeWidth = Size.TINY.value

    init {
        viewModelScope.launch(iODispatcher) {
            graphicNotesRepo.init()
        }
    }
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

    fun onSave(note: GraphicNote) {
        viewModelScope.launch {
            graphicNotesRepo.save(note) { }
        }
    }

    fun onDrawPath(path: DrawPath) {
        editPaths.addLast(path)
        svg.addPath(path)
        svgLiveData.postValue(svg)
    }

    fun paths(): Collection<DrawPath> = editPaths

    fun svg(): SVG = svg

    fun setPaintColor(color: Int) {
        paintColor = color
        lastPaintColor = paintColor
    }

    fun setBrushSize(size: Float) {
        strokeWidth = size
    }

    fun setEraseMode(eraseMode: Boolean) {
        paintColor = if (eraseMode) {
            Color.WHITE.code
        } else {
            lastPaintColor
        }
    }
}

data class DrawPath(
    val path: Path,
    val paint: Paint
)