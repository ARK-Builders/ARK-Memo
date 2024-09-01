package dev.arkbuilders.arkmemo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.arkbuilders.arkmemo.di.IO_DISPATCHER
import dev.arkbuilders.arkmemo.media.ArkMediaPlayer
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.models.SaveNoteResult
import dev.arkbuilders.arkmemo.models.TextNote
import dev.arkbuilders.arkmemo.models.VoiceNote
import dev.arkbuilders.arkmemo.repo.NotesRepo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class NotesViewModel
    @Inject
    constructor(
        @Named(IO_DISPATCHER) private val iODispatcher: CoroutineDispatcher,
        private val textNotesRepo: NotesRepo<TextNote>,
        private val graphicNotesRepo: NotesRepo<GraphicNote>,
        private val voiceNotesRepo: NotesRepo<VoiceNote>,
        private val arkMediaPlayer: ArkMediaPlayer,
    ) : ViewModel() {
        private val notes = MutableStateFlow(listOf<Note>())
        private val mSaveNoteResultLiveData = MutableLiveData<SaveNoteResult>()

        fun init(extraBlock: () -> Unit) {
            val initJob =
                viewModelScope.launch(iODispatcher) {
                    textNotesRepo.init()
                    graphicNotesRepo.init()
                    voiceNotesRepo.init()
                }
            viewModelScope.launch {
                initJob.join()
                extraBlock()
            }
        }

        fun readAllNotes() {
            viewModelScope.launch(iODispatcher) {
                notes.value = textNotesRepo.read() + graphicNotesRepo.read() + voiceNotesRepo.read()
            }
        }

        fun onSaveClick(
            note: Note,
            showProgress: (Boolean) -> Unit,
        ) {
            viewModelScope.launch(iODispatcher) {
                withContext(Dispatchers.Main) {
                    showProgress(true)
                }

                fun handleResult(result: SaveNoteResult) {
                    if (result == SaveNoteResult.SUCCESS) {
                        add(note)
                    }
                    mSaveNoteResultLiveData.postValue(result)
                }
                when (note) {
                    is TextNote -> {
                        textNotesRepo.save(note) { result ->
                            handleResult(result)
                        }
                    }
                    is GraphicNote -> {
                        graphicNotesRepo.save(note) { result ->
                            handleResult(result)
                        }
                    }
                    is VoiceNote -> {
                        voiceNotesRepo.save(note) { result ->
                            handleResult(result)
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    showProgress(false)
                }
            }
        }

        fun onDeleteConfirmed(note: Note) {
            viewModelScope.launch(iODispatcher) {
                remove(note)
                when (note) {
                    is TextNote -> textNotesRepo.delete(note)
                    is GraphicNote -> graphicNotesRepo.delete(note)
                    is VoiceNote -> voiceNotesRepo.delete(note)
                }
            }
        }

        fun getNotes(emit: (List<Note>) -> Unit) {
            viewModelScope.launch(iODispatcher) {
                notes.collectLatest {
                    withContext(Dispatchers.Main) {
                        emit(it)
                    }
                }
            }
        }

        private fun add(note: Note) {
            val notes = this.notes.value.toMutableList()
            note.resource?.let {
                notes.removeIf { it.resource?.id == note.resource?.id }
            }
            notes.add(note)
            this.notes.value = notes
        }

        private fun remove(note: Note) {
            val notes = this.notes.value.toMutableList()
            notes.remove(note)
            this.notes.value = notes
        }

        fun getSaveNoteResultLiveData(): LiveData<SaveNoteResult> {
            return mSaveNoteResultLiveData
        }
    }
