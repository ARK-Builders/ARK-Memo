package dev.arkbuilders.arkmemo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.arkbuilders.arklib.ResourceId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dev.arkbuilders.arkmemo.repo.text.TextNotesRepo
import dev.arkbuilders.arkmemo.models.SaveNoteResult
import dev.arkbuilders.arkmemo.repo.NotesRepo
import dev.arkbuilders.arkmemo.di.IO_DISPATCHER
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.models.TextNote
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class NotesViewModel @Inject constructor(
    @Named(IO_DISPATCHER) private val iODispatcher: CoroutineDispatcher,
    private val textNotesRepo: NotesRepo<TextNote>,
    private val graphicNotesRepo: NotesRepo<GraphicNote>,
) : ViewModel() {

    private val notes = MutableStateFlow(listOf<Note>())
    private val mSaveNoteResultLiveData = MutableLiveData<SaveNoteResult>()

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

    fun onSaveClick(
        note: Note,
        showProgress: (Boolean) -> Unit,
        saveVersion: (ResourceId, ResourceId) -> Unit
    ) {
        viewModelScope.launch(iODispatcher) {
            withContext(Dispatchers.Main) {
                showProgress(true)
            }
            val oldId = note.resource?.id
            val isNewResource = oldId == null
            when (note) {
                is TextNote -> textNotesRepo.save(note) { result ->
                    if (result == SaveNoteResult.SUCCESS) {
                        val newId = note.resource?.id!!
                        add(note)
                        if (!isNewResource) saveVersion(oldId!!, newId)
                    }
                    mSaveNoteResultLiveData.postValue(result)
                }
            }
            withContext(Dispatchers.Main) {
                showProgress(false)
            }
        }
    }

    fun onDeleteConfirmed(note: Note) {
        viewModelScope.launch(iODispatcher) {
            remove(note)
            when (note) {
                is TextNote -> textNotesRepo.delete(note)
                is GraphicNote -> graphicNotesRepo.delete(note)
            }
        }
    }

    fun getNotes(emit: (List<Note>) -> Unit) {
        viewModelScope.launch(iODispatcher) {
            notes.collectLatest {
                withContext(Dispatchers.Main) {
                    emit(it)
                }
            }
        }
    }

    private fun add(note: Note) {
        val notes = this.notes.value.toMutableList()
        note.resource?.let {
            notes.removeIf { it.resource?.id == note.resource?.id }
        }
        notes.add(note)
        this.notes.value = notes
    }

    private fun remove(note: Note) {
        val notes = this.notes.value.toMutableList()
        notes.remove(note)
        this.notes.value = notes
    }

    fun getSaveNoteResultLiveData(): LiveData<SaveNoteResult> {
        return mSaveNoteResultLiveData
    }
}