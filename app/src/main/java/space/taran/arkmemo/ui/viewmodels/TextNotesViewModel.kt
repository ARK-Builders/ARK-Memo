package space.taran.arkmemo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.taran.arkmemo.data.repositories.TextNotesRepo
import space.taran.arkmemo.models.TextNote
import javax.inject.Inject

@HiltViewModel
class TextNotesViewModel @Inject constructor(): ViewModel() {

    @Inject lateinit var repo: TextNotesRepo

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


}