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
class EditTextNotesViewModel @Inject constructor(): ViewModel() {

    private val iODispatcher = Dispatchers.IO

    @Inject lateinit var repo: TextNotesRepo

    fun init() {
        viewModelScope.launch(iODispatcher) {
            repo.init(
                MemoPreferences.getInstance().getPath()!!,
                viewModelScope
            )
        }
    }

    fun onSaveClick(note: TextNote, showProgress: (Boolean) -> Unit) {
        viewModelScope.launch(iODispatcher) {
            withContext(Dispatchers.Main) {
                showProgress(true)
            }
            repo.save(note)
            withContext(Dispatchers.Main) {
                showProgress(false)
            }
        }
    }
}