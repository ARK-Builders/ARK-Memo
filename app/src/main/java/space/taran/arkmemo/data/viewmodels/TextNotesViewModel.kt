package space.taran.arkmemo.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import space.taran.arkmemo.data.repo.notes.text.TextNotesRepo
import space.taran.arkmemo.data.models.TextNote
import javax.inject.Inject

@HiltViewModel
class TextNotesViewModel @Inject constructor(): ViewModel() {

    @Inject lateinit var textNotesRepo: TextNotesRepo
    private val iODispatcher = Dispatchers.IO

    fun deleteNote(note: TextNote){
        viewModelScope.launch(iODispatcher) {
            textNotesRepo.deleteNote(note)
        }
    }

    suspend fun collectAllNotes(emit: suspend (List<TextNote>) -> Unit) {
        textNotesRepo.textNotes.collect {
            emit(it)
        }
    }
}