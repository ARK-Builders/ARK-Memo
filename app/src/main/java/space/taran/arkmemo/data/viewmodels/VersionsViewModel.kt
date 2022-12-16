package space.taran.arkmemo.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import space.taran.arkmemo.data.VersionMeta
import space.taran.arkmemo.data.repositories.TextNotesRepository
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.models.Version
import space.taran.arkmemo.space.taran.arkmemo.utils.CODES_DELETING_NOTE
import javax.inject.Inject

@HiltViewModel
class VersionsViewModel @Inject constructor(): ViewModel() {

    @Inject lateinit var textNotesRepo: TextNotesRepository
    private val iODispatcher = Dispatchers.IO
    private val textNotes: MutableStateFlow<List<TextNote>> by lazy{
        MutableStateFlow(listOf())
    }
    private val version: MutableStateFlow<Version?> by lazy{
        MutableStateFlow(Version(Version.Content(listOf(""))))
    }

    fun deleteNote(note:TextNote, versionFrom: Version){
        viewModelScope.launch(iODispatcher) {
            when (textNotesRepo.deleteNoteFromVersion(note,versionFrom)) {
                CODES_DELETING_NOTE.SUCCESS_NOTE_AND_VERSION_DELETED.code -> {
                    version.value = null//special value for closing VersionsFragment.
                }
                CODES_DELETING_NOTE.SUCCESS_NOTE_DELETED_VERSION_CHANGED.code -> {
                    val secondNoteFromV = textNotesRepo.getSecondNoteFromVersion(versionFrom)
                    val newMeta = VersionMeta( secondNoteFromV.meta!!.id, secondNoteFromV.meta.id )
                    val newListIdInVersion = versionFrom.content.idList.drop(1)
                    val newVersion = Version( Version.Content(newListIdInVersion), newMeta )
                    version.value = newVersion
                    textNotes.value = textNotesRepo.getAllNotesFromVersion(newVersion)
                }
                CODES_DELETING_NOTE.SUCCESS.code -> {
                    val versionWithoutNote = Version(textNotesRepo.getVersionContent(versionFrom.meta!!.rootResourceId),versionFrom.meta )
                    version.value = versionWithoutNote
                    textNotes.value = textNotesRepo.getAllNotesFromVersion(versionWithoutNote)
                }
            }
        }
    }

    fun getAllNotes(): StateFlow<List<TextNote>>{
        viewModelScope.launch(iODispatcher){
            if(version.value != null && version.value!!.meta != null){//this is for avoiding trying to get notes from null
                //when its needed to close VersionsFragment because there is no more members in version and this got deleted.
                textNotes.value = textNotesRepo.getAllNotesFromVersion(version.value!!)
            }
        }
        return textNotes
    }

    fun getVersion(): StateFlow<Version?>{
        return version
    }

    fun setVersion(ver:Version){
        version.value = ver
    }
}