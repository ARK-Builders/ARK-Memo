package dev.arkbuilders.arkmemo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dev.arkbuilders.arkmemo.data.repositories.NotesRepo
import space.taran.arkmemo.di.IO_DISPATCHER
import space.taran.arkmemo.models.BaseNote
import space.taran.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.models.TextNote
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class NotesViewModel @Inject constructor(
    @Named(IO_DISPATCHER) private val iODispatcher: CoroutineDispatcher,
    private val textNotesRepo: NotesRepo<TextNote>,
    private val graphicNotesRepo: NotesRepo<GraphicNote>,
    private val memoPreferences: MemoPreferences
) : ViewModel() {

    private val notes = MutableStateFlow(listOf<BaseNote>())

    fun init(read: () -> Unit) {
        val initJob = viewModelScope.launch(iODispatcher) {
            textNotesRepo.init()
            graphicNotesRepo.init()
        }
        viewModelScope.launch {
            initJob.join()
            read()
        }
    }

    fun readAllNotes() {
        viewModelScope.launch(iODispatcher) {
            notes.value = textNotesRepo.read() + graphicNotesRepo.read()
        }
    }

    fun onSaveClick(note: BaseNote, showProgress: (Boolean) -> Unit) {
        viewModelScope.launch(iODispatcher) {
            withContext(Dispatchers.Main) {
                showProgress(true)
            }
            when (note) {
                is TextNote -> {
                    textNotesRepo.save(note)
                }
                is GraphicNote -> {
                    graphicNotesRepo.save(note)
                }
            }
            add(note)
            withContext(Dispatchers.Main) {
                showProgress(false)
            }
        }
    }

    fun onDeleteClick(note: BaseNote) {
        viewModelScope.launch(iODispatcher) {
            remove(note)
            when (note) {
                is TextNote -> textNotesRepo.delete(note)
                is GraphicNote -> graphicNotesRepo.delete(note)
            }
        }
    }

    fun getTextNotes(emit: (List<BaseNote>) -> Unit) {
        viewModelScope.launch(iODispatcher) {
            notes.collect {
                withContext(Dispatchers.Main) {
                    emit(it)
                }
            }
        }
    }

    private fun add(note: BaseNote) {
        val notes = this.notes.value.toMutableList()
        note.resourceMeta?.let {
            notes.removeIf { it.resourceMeta?.id == note.resourceMeta?.id }
        }
        notes.add(note)
        this.notes.value = notes
    }

    private fun remove(note: BaseNote) {
        val notes = this.notes.value.toMutableList()
        notes.remove(note)
        this.notes.value = notes
    }
}