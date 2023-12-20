package dev.arkbuilders.arkmemo.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.arkbuilders.arklib.ResourceId
import dev.arkbuilders.arkmemo.models.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import dev.arkbuilders.arkmemo.models.Version
import dev.arkbuilders.arkmemo.repo.versions.VersionStorage
import dev.arkbuilders.arkmemo.repo.versions.VersionStorageRepo
import javax.inject.Inject

@HiltViewModel
class VersionsViewModel @Inject constructor(
    private val versionStorageRepo: VersionStorageRepo
): ViewModel() {

    private lateinit var versionStorage: VersionStorage
    private val latestResourceId = MutableStateFlow<ResourceId?>(null)

    fun init() {
        viewModelScope.launch {
            versionStorage = versionStorageRepo.provide()
        }
    }

    fun getLatestNotes(notes: List<Note>, emit: (List<Note>) -> Unit) {
        viewModelScope.launch {
            val latestNotes = notes.filter {
                getChildNotParentIds().contains(it.resource?.id) ||
                        isNotVersioned(it.resource?.id!!)
            }
            emit(latestNotes)
        }
    }

    fun getLatestNoteFamilyTree(notes: List<Note>, emit: (List<Note>) -> Unit) {
        viewModelScope.launch {
            latestResourceId.collectLatest { id ->
                id?.let {
                    val familyTree = notes.filter {
                        getParentIds(id).contains(it.resource?.id) || it.resource?.id == id
                    }
                    emit(familyTree)
                }
            }
        }
    }

    fun updateLatestResourceId(newId: ResourceId) {
        viewModelScope.launch {
            latestResourceId.emit(newId)
        }
    }

    fun getParentIds(id: ResourceId): List<ResourceId> =
        versionStorage.parentsTreeByChild(id)[id]!!

    fun createVersion(oldId: ResourceId, newId: ResourceId) {
        viewModelScope.launch(Dispatchers.IO) {
                val version = Version(oldId, newId)
                versionStorage.add(version)
            }
        }

    fun getChildNotParentIds(): List<ResourceId> = versionStorage.childrenNotParents()

    fun isNotVersioned(id: ResourceId): Boolean = !isVersioned(id)

    fun isVersioned(id: ResourceId): Boolean = versionStorage.contains(id)

    fun onDelete(id: ResourceId) {
        viewModelScope.launch {
            versionStorage.forget(id)
        }
    }

    fun isLatestResource(id: ResourceId): Boolean = versionStorage.isLatestResourceVersion(id)
}