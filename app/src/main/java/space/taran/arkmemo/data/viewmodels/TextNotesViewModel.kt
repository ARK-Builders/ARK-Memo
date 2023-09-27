package space.taran.arkmemo.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import space.taran.arkmemo.data.repositories.TextNotesRepository
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.preferences.MemoPreferences
import javax.inject.Inject

@HiltViewModel
class TextNotesViewModel @Inject constructor(): ViewModel() {

    @Inject lateinit var textNotesRepo: TextNotesRepository

    private val iODispatcher = Dispatchers.IO

    fun init() {
        textNotesRepo.init(
            MemoPreferences.getInstance().getPath()!!,
            viewModelScope
        )
    }

    fun deleteNote(note: TextNote){
        viewModelScope.launch(iODispatcher) {
            textNotesRepo.deleteNote(note)
        }
    }

    fun getAllLatestNotes(emit: (List<TextNote>) -> Unit){
        viewModelScope.launch(Dispatchers.Main) {
            textNotesRepo.textNotes.collectLatest {
                emit(it)
            }
        }
    }
}