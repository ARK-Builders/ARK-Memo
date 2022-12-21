package space.taran.arkmemo.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import space.taran.arkmemo.data.repositories.TextNotesRepository
import space.taran.arkmemo.models.TextNote
import javax.inject.Inject

@HiltViewModel
class EditTextNotesViewModel @Inject constructor() : ViewModel() {

    private val iODispatcher = Dispatchers.IO

    @Inject
    lateinit var repo: TextNotesRepository

    private val saveNoteResult: MutableSharedFlow<Long> by lazy {
        MutableSharedFlow(0)
    }

    fun saveNote(note: TextNote, rootResourceId: Long? = null): SharedFlow<Long> {
        viewModelScope.launch(iODispatcher) {
            saveNoteResult.emit(repo.saveNote(note, rootResourceId))
            saveNoteResult.emit(0L)
        }
        return saveNoteResult
    }
}