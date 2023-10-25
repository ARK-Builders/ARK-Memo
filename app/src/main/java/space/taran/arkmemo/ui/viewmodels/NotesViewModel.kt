package space.taran.arkmemo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.taran.arkmemo.data.repositories.TextNotesRepo
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.preferences.MemoPreferences
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(): ViewModel() {

    @Inject lateinit var textNotesRepo: TextNotesRepo

    private val iODispatcher = Dispatchers.IO

    private val notes = MutableStateFlow(listOf<TextNote>())

    fun init() {
        viewModelScope.launch(iODispatcher) {
            textNotesRepo.init(
                MemoPreferences.getInstance().getPath()!!,
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
            textNotesRepo.save(note)
            add(note)
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
}