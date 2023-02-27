package space.taran.arkmemo.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import space.taran.arklib.ResourceId
import space.taran.arkmemo.data.models.TextNote
import space.taran.arkmemo.data.models.Version
import space.taran.arkmemo.data.repo.versions.VersionStorage
import space.taran.arkmemo.data.repo.versions.VersionStorageRepo
import javax.inject.Inject

@HiltViewModel
class VersionsViewModel @Inject constructor(): ViewModel() {

    @Inject lateinit var versionStorageRepo: VersionStorageRepo
    private lateinit var versionStorage: VersionStorage
    private val latestVersion = MutableStateFlow<ResourceId?>(null)

    suspend fun init() {
        versionStorage = versionStorageRepo.provide()
    }

    fun emitLatestVersionNoteId(newNoteId: ResourceId) {
        viewModelScope.launch {
            latestVersion.emit(newNoteId)
        }
    }

    suspend fun collectLatestVersionNoteId( emit: suspend (ResourceId) -> Unit) {
        latestVersion.collectLatest {
            emit(it!!)
        }
    }

    fun getNoteParentsFromVersions(note: TextNote): List<ResourceId> {
        return versionStorage.parentsTreeByChild(note.meta?.id!!)[note.meta.id]!!
    }

    fun addNoteToVersions(note: TextNote, newId: ResourceId) {
        viewModelScope.launch(Dispatchers.IO) {
            if (note.meta != null) {
                val version = Version(
                    note.meta.id,
                    newId
                )
                versionStorage.add(version)
            }
        }
    }

    fun getLatestVersions() = versionStorage.childrenNotParents()

    fun isNotVersionedYet(note: TextNote) = note.meta == null ||
            !versionStorage.contains(note.meta.id)

    fun isVersioned(note: TextNote) = !isNotVersionedYet(note)

    fun forgetNoteFromVersions(note: TextNote) {
        viewModelScope.launch {
            val id = note.meta?.id!!
            versionStorage.forget(id)
        }
    }

    fun isLatestVersion(note: TextNote) = note.meta != null &&
            versionStorage.isLatestVersion(note.meta.id)
}