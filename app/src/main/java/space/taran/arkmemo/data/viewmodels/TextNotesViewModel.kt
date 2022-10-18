package space.taran.arkmemo.data.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.taran.arkmemo.data.repositories.TextNotesRepository
import space.taran.arkmemo.models.TextNote
import javax.inject.Inject

@HiltViewModel
class TextNotesViewModel @Inject constructor(): ViewModel() {

    @Inject lateinit var textNotesRepo: TextNotesRepository
    private val iODispatcher = Dispatchers.IO
    private val textNotes: MutableStateFlow<List<TextNote>> by lazy{
        MutableStateFlow(listOf())
    }

    fun deleteNote(note: TextNote){
        viewModelScope.launch(iODispatcher) {
            textNotesRepo.deleteNote(note)
            textNotes.value = textNotesRepo.getAllNotes()
        }
    }

    fun getAllNotes(): StateFlow<List<TextNote>>{
        viewModelScope.launch(iODispatcher) {
                textNotes.value = textNotesRepo.getAllNotes()
        }
        return textNotes
    }
}