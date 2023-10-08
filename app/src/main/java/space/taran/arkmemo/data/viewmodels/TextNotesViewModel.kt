package space.taran.arkmemo.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.taran.arkmemo.data.repositories.TextNotesRepo
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.preferences.MemoPreferences
import javax.inject.Inject

@HiltViewModel
class TextNotesViewModel @Inject constructor(): ViewModel() {

    @Inject lateinit var textNotesRepo: TextNotesRepo

    private val iODispatcher = Dispatchers.IO

    fun init() {
        viewModelScope.launch(iODispatcher) {
            textNotesRepo.init(
                MemoPreferences.getInstance().getPath()!!,
                viewModelScope
            )
            textNotesRepo.read()
        }
    }

    fun onDelete(note: TextNote) {
        viewModelScope.launch(iODispatcher) {
            textNotesRepo.delete(note)
        }
    }

    fun getTextNotes(emit: (List<TextNote>) -> Unit) {
        viewModelScope.launch(iODispatcher) {
            textNotesRepo.textNotes.collect {
                withContext(Dispatchers.Main) {
                    emit(it)
                }
            }
        }
    }
}