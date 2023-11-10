package dev.arkbuilders.arkmemo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.arkbuilders.arkmemo.data.repositories.SaveNoteCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dev.arkbuilders.arkmemo.data.repositories.TextNotesRepo
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

    fun onSaveClick(note: TextNote, showProgress: (Boolean) -> Unit) {
        viewModelScope.launch(iODispatcher) {
            withContext(Dispatchers.Main) {
                showProgress(true)

            }
            textNotesRepo.save(note, object : SaveNoteCallback {
                override fun onSaveNote(result: SaveNoteResult) {
                    if (result == SaveNoteResult.SUCCESS) {
                        add(note)
                    }
                    mSaveNoteResultLiveData.postValue(result)
                }

            })

            withContext(Dispatchers.Main) {
                showProgress(false)
            }
        }
    }

    fun onDelete(note: TextNote) {
        viewModelScope.launch(iODispatcher) {
            remove(note)
            textNotesRepo.delete(note)
        }
    }

    fun getTextNotes(emit: (List<TextNote>) -> Unit) {
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