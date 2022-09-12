package space.taran.arkmemo.data.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.taran.arkmemo.data.repositories.TextNotesRepository
import space.taran.arkmemo.models.TextNote
import javax.inject.Inject

@HiltViewModel
class TextNotesViewModel @Inject constructor(): ViewModel() {

    @Inject lateinit var repository: TextNotesRepository

    fun saveNote(note: TextNote){
        repository.saveNote(note)
    }

    fun getAllNotes() = repository.getAllNotes()

}