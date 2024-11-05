package dev.arkbuilders.arkmemo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.arkbuilders.arklib.ResourceId
import dev.arkbuilders.arkmemo.di.IO_DISPATCHER
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.models.SaveNoteResult
import dev.arkbuilders.arkmemo.models.TextNote
import dev.arkbuilders.arkmemo.models.VoiceNote
import dev.arkbuilders.arkmemo.repo.NotesRepo
import dev.arkbuilders.arkmemo.utils.extractDuration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import kotlin.io.path.pathString

@HiltViewModel
class NotesViewModel
    @Inject
    constructor(
        @Named(IO_DISPATCHER) private val iODispatcher: CoroutineDispatcher,
        private val textNotesRepo: NotesRepo<TextNote>,
        private val graphicNotesRepo: NotesRepo<GraphicNote>,
        private val voiceNotesRepo: NotesRepo<VoiceNote>,
    ) : ViewModel() {
        private val notes = MutableStateFlow(listOf<Note>())
        private val mSaveNoteResultLiveData = MutableLiveData<SaveNoteResult>()
        private var searchJob: Job? = null

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

        fun readAllNotes(onSuccess: (notes: List<Note>) -> Unit) {
            viewModelScope.launch(iODispatcher) {
                notes.value = textNotesRepo.read() + graphicNotesRepo.read() + voiceNotesRepo.read()
                notes.value.let {
                    withContext(Dispatchers.Main) {
                        onSuccess(it.sortedByDescending { note -> note.resource?.modified })
                    }
                }
            }
        }

        fun searchNote(
            keyword: String,
            onSuccess: (notes: List<Note>) -> Unit,
        ) {
            searchJob?.cancel()
            searchJob =
                viewModelScope.launch(iODispatcher) {
                    // Add a delay to restart the search job if there are 2 consecutive search events
                    // triggered within 0.5 second window.
                    delay(500)
                    notes.collectLatest {
                        val filteredNotes =
                            it
                                .filter { note ->
                                    note.title.contains(keyword, true)
                                }
                                // Keep the search result ordered chronologically
                                .sortedByDescending { note -> note.resource?.modified }
                        withContext(Dispatchers.Main) {
                            onSuccess(filteredNotes)
                        }
                    }
                }
        }

        fun onSaveClick(
            note: Note,
            parentNote: Note? = null,
            showProgress: (Boolean) -> Unit,
        ) {
            val noteResId = note.resource?.id
            viewModelScope.launch(iODispatcher) {
                withContext(Dispatchers.Main) {
                    showProgress(true)
                }

                fun handleResult(result: SaveNoteResult) {
                    if (result == SaveNoteResult.SUCCESS_NEW ||
                        result == SaveNoteResult.SUCCESS_UPDATED
                    ) {
                        if (result == SaveNoteResult.SUCCESS_NEW) {
                            parentNote?.let { onDeleteConfirmed(parentNote) {} }
                        }
                        add(note, noteResId)
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

        fun onDeleteConfirmed(
            note: Note,
            onSuccess: () -> Unit,
        ) {
            viewModelScope.launch(iODispatcher) {
                when (note) {
                    is TextNote -> textNotesRepo.delete(note)
                    is GraphicNote -> graphicNotesRepo.delete(note)
                    is VoiceNote -> voiceNotesRepo.delete(note)
                }

                this@NotesViewModel.notes.value =
                    this@NotesViewModel.notes.value.toMutableList()
                        .apply { remove(note) }
                withContext(Dispatchers.Main) {
                    onSuccess.invoke()
                }
            }
        }

        private fun add(
            note: Note,
            parentResId: ResourceId? = null,
        ) {
            val notes = this.notes.value.toMutableList()
            note.resource?.let {
                notes.removeIf { it.resource?.id == parentResId ?: note.resource?.id }
            }
            if (note is VoiceNote) {
                note.duration = extractDuration(note.path.pathString)
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
