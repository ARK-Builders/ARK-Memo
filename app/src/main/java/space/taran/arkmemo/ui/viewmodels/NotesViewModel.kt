package space.taran.arkmemo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.taran.arkmemo.data.repositories.GraphicNotesRepo
import space.taran.arkmemo.data.repositories.TextNotesRepo
import space.taran.arkmemo.models.BaseNote
import space.taran.arkmemo.models.GraphicNote
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.preferences.MemoPreferences
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val textNotesRepo: TextNotesRepo,
    private val memoPreferences: MemoPreferences
) : ViewModel() {


    @Inject lateinit var graphicNotesRepo: GraphicNotesRepo

    private val iODispatcher = Dispatchers.IO

    private val notes = MutableStateFlow(listOf<BaseNote>())

    fun init() {
        viewModelScope.launch(iODispatcher) {
            textNotesRepo.init(
                memoPreferences.getPath()!!,
                viewModelScope
            )
            graphicNotesRepo.init(
                MemoPreferences.getNotesStorage()!!,
                viewModelScope
            )
            notes.value = textNotesRepo.read() + graphicNotesRepo.read()
        }
    }

    fun onSaveClick(note: BaseNote, showProgress: (Boolean) -> Unit) {
        viewModelScope.launch(iODispatcher) {
            withContext(Dispatchers.Main) {
                showProgress(true)

            }
            when (note) {
                is TextNote -> {
                    textNotesRepo.save(note)
                }
                is GraphicNote -> {
                    graphicNotesRepo.save(note)
                }
            }
            add(note)
            withContext(Dispatchers.Main) {
                showProgress(false)
            }
        }
    }

    fun onDeleteClick(note: BaseNote) {
        viewModelScope.launch(iODispatcher) {
            remove(note)
            when (note) {
                is TextNote -> textNotesRepo.delete(note)
                is GraphicNote -> graphicNotesRepo.delete(note)
            }
        }
    }

    fun getTextNotes(emit: (List<BaseNote>) -> Unit) {
        viewModelScope.launch(iODispatcher) {
            notes.collect {
                withContext(Dispatchers.Main) {
                    emit(it)
                }
            }
        }
    }

    private fun add(note: BaseNote) {
        val notes = this.notes.value.toMutableList()
        notes.add(note)
        this.notes.value = notes
    }

    private fun remove(note: BaseNote) {
        val notes = this.notes.value.toMutableList()
        notes.remove(note)
        this.notes.value = notes
    }
}