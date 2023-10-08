package dev.arkbuilders.arkmemo.data.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import dev.arkbuilders.arkmemo.data.repositories.TextNotesRepository
import dev.arkbuilders.arkmemo.models.TextNote
import javax.inject.Inject

@HiltViewModel
class EditTextNotesViewModel @Inject constructor(): ViewModel() {

    private val iODispatcher = Dispatchers.IO

    @Inject lateinit var repo: TextNotesRepository

    fun saveNote(note: TextNote){
        viewModelScope.launch(iODispatcher) {
            repo.saveNote(note)
        }
    }
}