package space.taran.arkmemo.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.arkbuilders.arklib.ResourceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import space.taran.arkmemo.data.repo.notes.text.TextNotesRepo
import space.taran.arkmemo.data.models.TextNote
import javax.inject.Inject

@HiltViewModel
class EditTextNotesViewModel @Inject constructor(): ViewModel() {

    private val iODispatcher = Dispatchers.IO

    @Inject lateinit var repo: TextNotesRepo

    fun saveNote(
        note: TextNote,
        addToVersion: (TextNote, ResourceId) -> Unit
    ) {
        viewModelScope.launch(iODispatcher) {
            val newNoteId = repo.saveNote(note)
            addToVersion(note, newNoteId)
        }
    }
}