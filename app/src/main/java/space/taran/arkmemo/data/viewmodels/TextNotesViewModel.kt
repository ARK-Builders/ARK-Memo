package space.taran.arkmemo.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import space.taran.arkmemo.data.repo.notes.text.TextNotesRepo
import space.taran.arkmemo.data.models.TextNote
import javax.inject.Inject

@HiltViewModel
class TextNotesViewModel @Inject constructor(): ViewModel() {

    @Inject lateinit var textNotesRepo: TextNotesRepo
    private val iODispatcher = Dispatchers.IO
    private val textNotes: MutableStateFlow<List<TextNote>> by lazy{
        MutableStateFlow(listOf())
    }

    fun deleteNote(note: TextNote){
        viewModelScope.launch(iODispatcher) {
            textNotesRepo.deleteNote(note)
            textNotes.emit(textNotesRepo.getAllNotes())
        }
    }

    suspend fun collectAllNotes(emit: suspend (List<TextNote>) -> Unit) {
        viewModelScope.launch(iODispatcher) {
            textNotes.value = textNotesRepo.getAllNotes()
        }
        textNotes.collect {
            emit(it)
        }
    }

    fun computeId() {

    }
    fun noteExists(note: TextNote): Boolean {
        var exists = false
        viewModelScope.launch {
            textNotes.collect { notes ->
                exists = notes.any {
                    it.meta?.id == note.meta?.id
                }
            }
        }
        return exists
    }
}