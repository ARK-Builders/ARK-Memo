package dev.arkbuilders.arkmemo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.arkbuilders.arklib.ResourceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dev.arkbuilders.arkmemo.repo.text.TextNotesRepo
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.models.SaveNoteResult
import dev.arkbuilders.arkmemo.models.TextNote
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val textNotesRepo: TextNotesRepo,
    private val memoPreferences: MemoPreferences
) : ViewModel() {


    private val iODispatcher = Dispatchers.IO

    private val notes = MutableStateFlow(listOf<TextNote>())
    private val mSaveNoteResultLiveData = MutableLiveData<SaveNoteResult>()

    fun init() {
        viewModelScope.launch(iODispatcher) {
            textNotesRepo.init(
                memoPreferences.getPath()!!,
                viewModelScope
            )
            notes.value = textNotesRepo.read()
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

    fun onDelete(note: Note) {
        viewModelScope.launch(iODispatcher) {
            when(note) {
                is TextNote -> {
                    remove(note)
                    textNotesRepo.delete(note)
                }
            }
        }
    }

    fun getNotes(emit: (List<TextNote>) -> Unit) {
        viewModelScope.launch(iODispatcher) {
            notes.collect {
                withContext(Dispatchers.Main) {
                    emit(it)
                }
            }
        }
    }

    private fun add(note: TextNote) {
        val notes = this.notes.value.toMutableList()
        notes.add(note)
        this.notes.value = notes
    }

    private fun remove(note: TextNote) {
        val notes = this.notes.value.toMutableList()
        notes.remove(note)
        this.notes.value = notes
    }

    fun getSaveNoteResultLiveData(): LiveData<SaveNoteResult> {
        return mSaveNoteResultLiveData
    }
}