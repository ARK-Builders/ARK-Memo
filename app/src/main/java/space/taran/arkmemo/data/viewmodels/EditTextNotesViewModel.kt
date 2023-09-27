package space.taran.arkmemo.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import space.taran.arkmemo.data.repositories.TextNotesRepository
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.preferences.MemoPreferences
import javax.inject.Inject

@HiltViewModel
class EditTextNotesViewModel @Inject constructor(): ViewModel() {

    private val iODispatcher = Dispatchers.IO

    @Inject lateinit var repo: TextNotesRepository

    fun init() {
        repo.init(
            MemoPreferences.getInstance().getPath()!!,
            viewModelScope
        )
    }

    fun onSaveNoteClick(note: TextNote) {
        viewModelScope.launch(iODispatcher) {
            repo.saveNote(note)
        }
    }
}