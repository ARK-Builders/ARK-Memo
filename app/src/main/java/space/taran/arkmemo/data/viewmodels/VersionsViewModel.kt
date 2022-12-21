package space.taran.arkmemo.data.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import space.taran.arkmemo.data.VersionMeta
import space.taran.arkmemo.data.repositories.TextNotesRepository
import space.taran.arkmemo.data.repositories.VersionsRepository
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.models.Version
import space.taran.arkmemo.space.taran.arkmemo.utils.CODES_DELETING_NOTE
import java.io.FileNotFoundException
import javax.inject.Inject

@HiltViewModel
class VersionsViewModel @Inject constructor(): ViewModel() {

    @Inject lateinit var textNotesRepo: TextNotesRepository
    @Inject lateinit var versionsRepo: VersionsRepository
    private val iODispatcher = Dispatchers.IO
    private val textNotes: MutableStateFlow<List<TextNote>> by lazy{
        MutableStateFlow(listOf())
    }
    private val version: MutableStateFlow<Version?> by lazy{
        MutableStateFlow(Version(Version.Content(listOf())))
    }

    fun deleteNote(note:TextNote){
        viewModelScope.launch(iODispatcher) {
            refreshVersion()//needs to refresh version since EditTextNotesViewModel could have changed Version content.
            when (versionsRepo.deleteNoteFromVersion(note, version.value!!.meta!!.rootResourceId)) {
                CODES_DELETING_NOTE.SUCCESS_NOTE_AND_VERSION_DELETED.code -> {
                    version.value = null//special value for closing VersionsFragment.
                }
                CODES_DELETING_NOTE.SUCCESS_NOTE_DELETED_VERSION_CHANGED.code -> {
                    val secondNoteId = version.value!!.content.idList[0]
                    val newMeta = VersionMeta( secondNoteId )
                    val newVContent = versionsRepo.getVersionContent(secondNoteId)
                    val newVersion = Version( newVContent, newMeta )
                    version.value = newVersion
                    textNotes.value = textNotesRepo.getAllNotesFromVersion(secondNoteId)
                }
                CODES_DELETING_NOTE.SUCCESS.code -> {
                    val versionWithoutNote = Version(versionsRepo.getVersionContent(version.value!!.meta!!.rootResourceId),version.value!!.meta )
                    version.value = versionWithoutNote
                    textNotes.value = textNotesRepo.getAllNotesFromVersion(version.value!!.meta!!.rootResourceId)
                }
            }
        }
    }

    fun getAllNotes(): StateFlow<List<TextNote>>{
        viewModelScope.launch(iODispatcher){
            if( version.value != null ){//this is for avoiding trying to get notes from null
                //when its needed to close VersionsFragment because there is no more members in version and this got deleted.
                textNotes.value = textNotesRepo.getAllNotesFromVersion(version.value!!.meta!!.rootResourceId)
            }
        }
        return textNotes
    }

    fun getVersion(): StateFlow<Version?>{
        return version
    }

    private fun refreshVersion(){
        version.value = Version( versionsRepo.getVersionContent(version.value!!.meta!!.rootResourceId) , version.value!!.meta )
    }

    fun setVersion(ver:Version){
        version.value = ver
    }
}