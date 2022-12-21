package space.taran.arkmemo.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import space.taran.arkmemo.data.repositories.TextNotesRepository
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.models.Version
import javax.inject.Inject

@HiltViewModel
class TextNotesViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var textNotesRepo: TextNotesRepository
    private val iODispatcher = Dispatchers.IO
    private val textNotes: MutableStateFlow<List<TextNote>> by lazy {
        MutableStateFlow(listOf())
    }
    private val versions: MutableStateFlow<List<Version>> by lazy {
        MutableStateFlow(listOf())
    }

    fun deleteNote(note: TextNote, version: Version) {
        viewModelScope.launch(iODispatcher) {
            textNotesRepo.deleteNote(note, version)
            textNotes.value = textNotesRepo.getAllNotesWithHistory()
            versions.value = textNotesRepo.getAllVersions()
        }
    }

    fun getAllNotes(): StateFlow<List<TextNote>> {
        viewModelScope.launch(iODispatcher) {
            textNotes.value = textNotesRepo.getAllNotesWithHistory()
        }
        return textNotes
    }

    fun getAllVersions(): StateFlow<List<Version>> {
        viewModelScope.launch(iODispatcher) {
            versions.value = textNotesRepo.getAllVersions()
        }
        return versions
    }
}